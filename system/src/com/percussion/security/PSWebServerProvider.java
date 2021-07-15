/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.security;

import com.percussion.server.IPSCgiVariables;
import com.percussion.servlets.PSSecurityFilter;
import com.percussion.utils.security.PSRemoteUserCallback;
import com.percussion.utils.security.PSRequestHeadersCallback;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.commons.lang.StringUtils;

/**
 * The PSWebServerProvider class is used to access the security services
 * provided by the web server. This does not actually cause the web server to
 * authenticate users, etc. It merely checks that the user authenticated through
 * the web server, and what their credentials are. In certain cases, such as SSL
 * authentication, attributes are defined for the certificate subject and
 * issuer.
 * <p>
 * This must check the various CGI variables, as each web server has its own
 * storage mechanism for the authentication info. The authentication schemes we
 * currently know about are: <TABLE BORDER="1">
 * <TR>
 * <TH>Web Server</TH>
 * <TH>Authentication Type</TH>
 * <TH>CGI Variable(s)</TH>
 * </TR>
 * <TR>
 * <TD>Microsoft IIS</TD>
 * <TD>SSL Authentication</TD>
 * <TD>Subject: CERT_SUBJECT
 * <P>
 * Issuer: CERT_ISSUER</TD>
 * </TR>
 * <TR>
 * <TD>Microsoft IIS</TD>
 * <TD>NTLM</TD>
 * <TD>Userid: AUTH_USER
 * <P>
 * Password: N/A</TD>
 * </TR>
 * <TR>
 * <TD>Microsoft IIS</TD>
 * <TD>Basic</TD>
 * <TD>Userid: AUTH_USER
 * <P>
 * Password: AUTH_PASSWORD</TD>
 * </TR>
 * </TABLE>
 * 
 * @author Tas Giakouminakis
 * @version 1.0
 * @since 1.0
 */
public class PSWebServerProvider extends PSSecurityProvider
{
   /**
    * Construct an instance of this provider.
    */
   public PSWebServerProvider(Properties props, String providerInstance)
   {
      super(SP_NAME, providerInstance);
      m_properties = props;

      // Ensure that if any of AUTHENTICATED_USER_HEADER, USER_ROLE_LIST_HEADER,
      // or ROLE_LIST_DELIMITER are specified, all of them are specified.

      String userNameProp = m_properties.getProperty(AUTHENTICATED_USER_HEADER);
      String roleListHeaderNameProp = m_properties
         .getProperty(USER_ROLE_LIST_HEADER);
      String roleListDelimiterProp = m_properties
         .getProperty(ROLE_LIST_DELIMITER);
      boolean userNamePropExists = (userNameProp == null);
      boolean roleListHeaderNamePropExists = (roleListHeaderNameProp == null);
      boolean roleListDelimiterPropExists = (roleListDelimiterProp == null);
      if ((userNamePropExists != roleListHeaderNamePropExists)
         || (userNamePropExists != roleListDelimiterPropExists))
      {
         throw new IllegalStateException("For a PSXSecurityProviderInstance"
            + " of type WebServer, either all of the parameters "
            + AUTHENTICATED_USER_HEADER + ", " + USER_ROLE_LIST_HEADER
            + ", and " + ROLE_LIST_DELIMITER + " must be specified, or"
            + " none must be specified. Check rxconfig/server/config.xml.");
      }
   }

   /**
    * @see IPSSecurityProvider
    */
   public PSUserEntry authenticate(String uid, String pw,
      CallbackHandler callbackHandler) throws PSAuthenticationFailedException,
      IOException, UnsupportedCallbackException
   {
      // get headers with callback
      PSRequestHeadersCallback headersCallback = new PSRequestHeadersCallback();
      PSRemoteUserCallback remoteUserCallBack = new PSRemoteUserCallback();
      callbackHandler.handle(new Callback[]
      {headersCallback, remoteUserCallBack});
      Map<String, String> headers = headersCallback.getHeaders();
      String remoteUserName = remoteUserCallBack.getRemoteUser();

      return authenticate(remoteUserName, headers);
   }

   /**
    * Performs the authentication given the callback information. Package access
    * for unit testing purposes only.
    * 
    * @param remoteUserName The name of the remote user if known, may be
    * <code>null</code> or empty.
    * 
    * @param headers The request headers, never <code>null</code>, may be
    * empty.
    * 
    * @return The authenticated user entry.
    * 
    * @throws PSAuthenticationFailedException
    */
   PSUserEntry authenticate(String remoteUserName, Map<String, String> headers)
      throws PSAuthenticationFailedException
   {
      PSUserEntry entry = null;

      // check for certificate authentication
      String var = PSSecurityFilter.getCertAuth(headers);
      if (var != null)
      {
         // parse the subject into attributes
         PSUserAttributes attribs = new PSUserAttributes();

         parseCertificateString("client/", var, attribs);
         String cn = (String) attribs.get("client/cn");
         if (cn == null)
            cn = var;

         // do the same for CERT_ISSUER
         var = PSSecurityFilter.getCertIssuer(headers);
         if (var != null)
            parseCertificateString("ca/", var, attribs);

         return new PSUserEntry(cn, 0, null, attribs, null);
      }

      // make sure they were really authenticated by the web server
      String authType = PSSecurityFilter.getAuthType(headers);
      if (StringUtils.isBlank(authType))
      {
         throw new PSAuthenticationFailedException(SP_NAME, m_spInstance,
            remoteUserName);
      }

      // determine if we have custom headers defined
      if (m_properties != null)
      {
         String userHeaderName = m_properties
            .getProperty(AUTHENTICATED_USER_HEADER);

         String roleListHeaderName = m_properties
            .getProperty(USER_ROLE_LIST_HEADER);

         String user = null;
         String roles = null;
         if (userHeaderName != null && roleListHeaderName != null)
         {
            user = PSSecurityFilter.getNormalizedHeader(
                  userHeaderName, headers);
            roles = PSSecurityFilter.getNormalizedHeader(
                  roleListHeaderName, headers);
         }

         PSRoleEntry[] roleEntries = null;
         if (roles != null)
         {
            String delim = m_properties.getProperty(ROLE_LIST_DELIMITER);
            if (StringUtils.isBlank(delim))
               delim = DELIMITER;
            
            String[] roleList = roles.split(delim);
            if (roleList.length > 0)
            {
               roleEntries = new PSRoleEntry[roleList.length];
               for (int i = 0; i < roleList.length; i++)
               {
                  roleEntries[i] = new PSRoleEntry(roleList[i], 0);
               }
            }
         }
         
         if (user != null)
         {
            entry = new PSUserEntry(user, 0, null, roleEntries, null, null);
         }
      }
      else
      {
         String name = PSSecurityFilter.getNormalizedHeader(
               IPSCgiVariables.CGI_AUTH_USER_NAME, headers);
         if (name == null)
         {
            name = remoteUserName;
         }

         if (name != null)
            entry = new PSUserEntry(name, 0, null, null, null);
      }

      if (entry == null)
         throw new PSAuthenticationFailedException(SP_NAME, m_spInstance,
            remoteUserName);

      return entry;
   }

   /** @see IPSSecurityProvider */
   public IPSSecurityProviderMetaData getMetaData()
   {
      if (m_metaData == null)
         m_metaData = new PSWebServerProviderMetaData(this);

      return m_metaData;
   }

   /**
    * Tokenize the certificate definition into attributes. All attributes are
    * stored in lower case to ease locating them later. The attribute header
    * which is specified will also be converted to lower case.
    * 
    * @param attribHeader the header to use when creating the attribute. For
    * instance, use "client/" for client certificates, "ca/" for certificate
    * authorities, etc., may be <code>null</code> or empty.
    * @param cert the certificate definition string, may be <code>null</code>
    * or empty.
    * @param attribs the object to store the attributes in, may be
    * <code>null</code>.
    */
   @SuppressWarnings("unchecked")
   public static void parseCertificateString(String attribHeader, String cert,
      PSUserAttributes attribs)
   {
      if ((attribs == null) || (cert == null) || (cert.length() == 0))
         return; // nothing to do (though we may want to throw illegal arg)

      if (attribHeader == null)
         attribHeader = "";
      else
         attribHeader = attribHeader.toLowerCase();

      StringTokenizer tok = new StringTokenizer(cert, ",=");
      while (tok.hasMoreTokens())
      {
         // unless this is an invalid cert string, there will always be
         // a key=value pair for each comma delimited token

         // also, we store attributes in lower case to ease locating them
         String attrib = tok.nextToken().trim().toLowerCase();
         String value = tok.nextToken().trim();

         attribs.put(attribHeader + attrib, value);
      }
   }

   /**
    * Get the property for the supplied key.
    * 
    * @param key the property key, may be <code>null</code> or empty.
    * @return the property found for the supplied key, <code>null</code> if
    * not found.
    */
   public String getProperty(String key)
   {
      if (key == null)
         return null;

      return m_properties.getProperty(key);
   }

   /**
    * The internal name of this security provider.
    */
   public static final String SP_NAME = "WebServer";

   /**
    * The property name used to store the HTTP header name used for
    * authenticated users.
    */
   public static final String AUTHENTICATED_USER_HEADER = "AuthenticatedUserHeader";

   /**
    * The property name used to store the HTTP header name used for user role
    * lists.
    */
   public static final String USER_ROLE_LIST_HEADER = "UserRoleListHeader";

   /**
    * The property name used to store the role list delimiter.
    */
   public static final String ROLE_LIST_DELIMITER = "RoleListDelimiter";

   /**
    * The default roles list delimiter used.
    */
   public static final String DELIMITER = ";";

   /**
    * The attribute name for the Rhythmyx session.
    */
   public static final String RX_SESSION = "RxSession";

   /**
    * The web server security provider properties. Initialized in the
    * constructor, may be <code>null</code> or empty.
    */
   private Properties m_properties = null;
}
