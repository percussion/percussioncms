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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.security;

import com.percussion.design.objectstore.PSAuthentication;
import com.percussion.design.objectstore.PSDirectory;
import com.percussion.error.PSRuntimeException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.naming.CommunicationException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import org.apache.commons.lang.StringUtils;

/**
 * Common base class for security providers which use JNDI (including
 * PSDirectoryProvider, PSDirectoryConnProvider, etc.).
 */
public abstract class PSJndiProvider extends PSSecurityProvider
{
   /**
    * Constructs a new JNDI provider using the given provider attributes.
    *
    * @param spType The security provider type. Must not be
    * <code>null</code>.
    *
    * @param spInstance The identifying name of this security
    * provider instance (for example, "Zeus2 Domain Provider").
    * Must not be <code>null</code>.
    *
    * @param doCataloging <code>true</code> if this instance is being
    * created to support cataloging; <code>false</code> otherwise.

    * @param providerAttributes Specifies provider attributes, not 
    *    <code>null</code>. A local copy is made of the supplied attributes so 
    *    that the original is not changed in any way. 
    *    Some values are required -- see table below.
    *    If the provider attributes are empty, it is assumed that this is a
    *    directory connection security provider and the properties used for 
    *    each directory will be set through 
    *    {@link #setProviderProperties(PSDirectory, PSAuthentication)}. 
    *    Otherwise the properties are initialized with this constructor. The
    *    property description is the same for all and described in the table
    *    below.
    * <p>The attributes which may be specified are:
    * <table border="1">
    * <tr><th>Setting</th><th>Description</th></tr>
    * <tr>
    *   <td>providerClassName</td>
    *   <td>REQUIRED: The JNDI provider's Java class name. Each JNDI
    *   vendor supplies a set of Java classes for using their provider. This
    *   class must implement the javax.naming.spi.InitialContextFactory
    *   interface (as required by JNDI for all service providers). For instance:
    *   <ul>
    *     <li>LDAP: com.sun.jndi.ldap.LdapCtxFactory</li>
    *     <li>NIS: com.sun.jndi.nis.NISCtxFactory</li>
    *     <li>Novell: com.novell.service.nw.NetWareInitialContextFactory</li>
    *   </ul>
    *   </td>
    * </tr>
    * <tr>
    *   <td>providerURL</td>
    *   <td>REQUIRED: The URL required to access the JNDI provider. JNDI URLs
    *  are provider-specific. Some common URL formats are:
    *   <ul>
    *     <li>LDAP: ldap://host:port (eg, ldap://srv1:389)</li>
    *     <li>NIS: nis://host/domain (eg, nis://srv1/mydomain.com)</li>
    *     <li>Novell: nds://host (eg, nds://nwsrv1)</li>
    *   </ul>
    *   </td>
    * </tr>
    * <tr>
    *   <td>authenticationScheme</td>
    *   <td>For connection based authentication, the JNDI provider
    *   specific authentication mechanism must be specified. Some common
    *   schemes are:
    *   <ul>
    *     <li>none - no authentication is performed (act as an anonymous
    *     user)</li>
    *     <li>simple - authentication is performed with a clear-text
    *     password</li>
    *     <li>CRAM-MD5 - this is a common challenge/response mechanism
    *     used by LDAP v3 servers</li>
    *   </ul>
    *   </td>
    * </tr>
    * <tr>
    *   <td>authenticationPrincipal</td>
    *   <td>For directory based authentication, we may need a user id
    *   to make the connection to the JNDI provider. This will be used
    *   for catalog requests (listing users, etc.) as well as
    *   authentication.</td>
    * </tr>
    * <tr>
    *   <td>authenticationCredential</td>
    *   <td>For directory based authentication, the associated password
    *   for the specified authentication principal.</td>
    * </tr>
    * <tr>
    *   <td>principalAttribute</td>
    *   <td>For directory based authentication, the attribute (column)
    *   we will search to find the user id being authenticated.</td>
    * </tr>
    * <tr>
    *   <td>credentialAttribute</td>
    *   <td>For directory based authentication, the attribute (column)
    *   we will search to find the password being authenticated.</td>
    * </tr>
    * <tr>
    *   <td>credentialFilterClassName</td>
    *   <td>For directory based authentication, the password may be
    *   stored using some type of hash algorithm. To compare the clear
    *   text password, it must first be run through the same hash
    *   algorithm. A Java class capable of performing this against
    *   the clear text password can be specified to handle this.</td>
    * </tr>
    * <tr>
    *   <td>userAttributes</td>
    *   <td>All user attributes will exist with attribute alias as key and
    *   attribute name as value. The attribute aliases and values will be stored
    *   with the user context after the user has been authenticated. The user
    *   entry in the directory may contain many attributes, such as e-mail
    *   address, phone number, etc. Specifying attributes to store with the
    *   user context allows applications to access the user's attributes in
    *   conditionals, etc.</td>
    * </tr>
    * </table>
    *
    * @throws IllegalArgumentException If any param is invalid.
    * @throws PSRuntimeException If the filter class cannot be initialized.
    */
   @SuppressWarnings("unchecked") PSJndiProvider(String spType, String spInstance, 
      Properties providerAttributes, boolean doCataloging)
   {
      super(spType, spInstance);

      if (providerAttributes == null)
         throw new IllegalArgumentException("providerAttributes cannot be null");

      // create a local copy so that the original will not be changed in any way
      Properties providerAttrs = new Properties();
      providerAttrs.putAll(providerAttributes);
      
      boolean isDirectoryProvider = providerAttributes.isEmpty();
      if (!isDirectoryProvider)
      {
         /*
          * Get all required and optional attributes and remove them from the
          * supplied properties.
          */
         m_providerClassName = PSJndiUtils.getProperty(providerAttrs,
            PROPS_PROVIDER_CLASS_NAME, true);
      
         m_providerURL = PSJndiUtils.getProperty(providerAttrs, 
            PROPS_PROVIDER_URL, true);

         // auth scheme describes how we process passwords before comparison
         m_authenticationScheme = PSJndiUtils.getProperty(providerAttrs,
            PROPS_AUTH_SCHEME, false);

         m_authenticationPrincipal = PSJndiUtils.getProperty(providerAttrs,
            PROPS_AUTH_PRINCIPAL, false);

         m_authenticationCredential = PSJndiUtils.getProperty(providerAttrs,
            PROPS_AUTH_CRED, false);
         if (null == m_authenticationCredential)
            m_authenticationCredential = "";

         m_principalAttribute = PSJndiUtils.getProperty(providerAttrs,
            PROPS_ATTR_PRINCIPAL, false);

         m_credentialAttribute = PSJndiUtils.getProperty(providerAttrs,
            PROPS_ATTR_CRED, false);

         // get the class name of the password filter, if provided
         String filterClass = PSJndiUtils.getProperty(providerAttrs,
            PROPS_CRED_FILTER, false);

         if (filterClass != null)
            m_credentialFilter = PSJndiUtils.initPasswordFilter(filterClass);
         else
            m_credentialFilter = null;

         // All attributes remaining in the map now are user attributes.
         m_userAttributes.putAll(providerAttrs);
        
         m_baseContext = PSJndiUtils.getBaseContext(m_providerURL);
         if (m_baseContext == null)
            throw new IllegalArgumentException("malformed provider url");
         
         if ((m_authenticationScheme == null) || 
            (m_authenticationScheme.length() == 0))
            m_authenticationScheme = DEFAULT_AUTH_SCHEME;
      }

      // If this SP is to be used for cataloging, need to prep a connection
      m_cataloging = doCataloging;
   }
   
   /**
    * Set the security provider properties for the supplied parameters.
    * 
    * @param directory the directory for which to set the provider properties,
    *    not <code>null</code>.
    * @param authentication the authentication for which to set the provider
    *    properties, not <code>null</code>.
    */
   @SuppressWarnings("unchecked")
   public void setProviderProperties(PSDirectory directory, 
      PSAuthentication authentication)
   {
      if (directory == null)
         throw new IllegalArgumentException("directory cannot be null");
         
      if (authentication == null)
         throw new IllegalArgumentException("authentication cannot be null");
         
      m_providerClassName = directory.getFactory();
      m_providerURL = directory.getProviderUrl();

      m_authenticationScheme = authentication.getScheme();
      m_authenticationPrincipal = authentication.getUser(
         directory.getProviderUrl());
      m_authenticationCredential = authentication.getPassword();
      m_principalAttribute = authentication.getUserAttr();
         
      String filterClass = authentication.getFilterExtension();
      if (filterClass != null && filterClass.trim().length() > 0)
         m_credentialFilter = PSJndiUtils.initPasswordFilter(filterClass);
            
      if (directory.getAttributes() != null)
      {
         Iterator attributes = directory.getAttributes().iterator();
         while (attributes.hasNext())
         {
            String attribute = (String) attributes.next();
            m_userAttributes.put(attribute, attribute);
         }
      }
        
      m_baseContext = PSJndiUtils.getBaseContext(m_providerURL);
      if (m_baseContext == null)
         throw new IllegalArgumentException("malformed provider url");
         
      if ((m_authenticationScheme == null) || 
         (m_authenticationScheme.length() == 0))
         m_authenticationScheme = DEFAULT_AUTH_SCHEME;
   }

   /**
    * Filters the password through the supplied credential filter,
    * or returns the password itself if no credential filter is
    * supplied.
    *
    * @param pw The cleartext password. If <code>null</code>, will
    * be treated as "".
    *
    * @return The (possibly filtered) version of the password,
    * which may be the password itself. May also be <code>null</code>.
    */
   protected Object filterPassword(String pw)
   {
      if (pw == null)
         pw = "";

      Object ret = pw;
      if (m_credentialFilter != null)
         ret = m_credentialFilter.encrypt(pw);

      return ret;
   }

   /**
    * Checks all Rhythmyx roles for members that are groups provided by this
    * provider instance, and returns group entries for any of those that the
    * specified user is a member of.
    *
    * @param userName The user name.  May not be <code>null</code> or empty.
    *
    * @return An array of group entries, with the security access set to
    * <code>0</code>.  Never <code>null</code>, may be empty.
    *
    * @throws IllegalArgumentException if userName is <code>null</code>.
    * @throws NamingException if any errors occur.
    */
   @SuppressWarnings({"unused","unchecked"})
   protected PSGroupEntry[] getGroupEntries(String userName)
      throws NamingException
   {
      if (userName == null || StringUtils.isBlank(userName))
         throw new IllegalArgumentException(
            "userName may not be null or empty");

      Set memberSet = new HashSet();

      Iterator providers = getGroupProviders();
      while (providers.hasNext())
      {
         IPSGroupProvider provider = (IPSGroupProvider) providers.next();
         Iterator<String> groups = provider.getUserGroups(
            userName.toString()).iterator();
         while (groups.hasNext())
         {
            PSGroupEntry groupEntry = new PSGroupEntry(groups.next(), 0);
            memberSet.add(groupEntry);
         }
      }

      PSGroupEntry[] groupArray = new PSGroupEntry[memberSet.size()];
      memberSet.toArray(groupArray);

      return groupArray;
   }

   /**
    * Prepares a cataloging connection.
    *
    * @throws NamingException If we could not authenticate
    * with the cataloging server.
    */
   synchronized private void prepCataloging() throws NamingException
   {
      if (m_cataloging && m_dirContext == null)
      {
         m_dirContext = createInitialContext();
      }
   }

   /**
    * Convenience version of {@link #createInitialContext(String)} that calls
    * <code>createInitialContext({@link #m_providerURL})</code>
    */
   private InitialDirContext createInitialContext() throws NamingException
   {
      return createInitialContext(m_providerURL);
   }

   /**
    * Creates an initial directory context that can be used for searching
    * and lookup. In JNDI, all naming and directory operations are performed
    * relative to a context. There are no absolute roots. Therefore the
    * JNDI defines an initial context, InitialContext, which provides a
    * starting point for naming and directory operations. Once you have
    * an initial context, you can use it to look up other contexts and
    * objects.
    *
    * @param url The Url that specifies the server and entry in the server to
    * use as location of the initial context.  May not be <code>null</code> or
    * empty.
    *
    * @return A new InitialDirContext
    *
    * @throws NamingException If an error occurred.
    * @throws IllegalArgumentException if url is <code>null</code> or empty.
    */
   @SuppressWarnings("unchecked")
   synchronized public InitialDirContext createInitialContext(String url)
      throws NamingException
   {
      if (url == null || url.trim().length() == 0)
         throw new IllegalArgumentException("url may not be null or empty");

      Hashtable env = new Hashtable();   // the connection properties

      // need to set the context factory (provided by user)
      env.put(Context.INITIAL_CONTEXT_FACTORY, m_providerClassName);
      env.put(Context.PROVIDER_URL, PSJndiUtils.getEncodedProviderUrl(url));

      // Add connectionPool needs to go after setup of security_protocol
      // to set correct socket factory
      PSJndiUtils.addConnectionPooling(env);

      // If necessary, we will authenticate with the given credentials.
      env.put(Context.SECURITY_AUTHENTICATION, m_authenticationScheme);
      if (m_authenticationPrincipal != null)
      {
         String prefix = "";
         if (m_principalAttribute != null)
            prefix = m_principalAttribute + "=";
         env.put(Context.SECURITY_PRINCIPAL, prefix + 
            m_authenticationPrincipal);
         env.put(Context.SECURITY_CREDENTIALS, m_authenticationCredential);
      }

      return new InitialDirContext(env);
   }

   /**
    * Get the encrypted password.
    * 
    * @param pw the password to encrypted, not <code>null</code>, may be empty.
    * @return the encrypted password if a password filter was set, the passed
    *    in password otherwise, never <code>null</code>, may be empty.
    */
   protected String getEncryptedPassword(String pw)
   {
      if (m_credentialFilter != null)
         return m_credentialFilter.encrypt(pw);
         
      return pw;
   }

   /**
    * Returns a modifiable copy of the catalog DirContext. It is the
    * caller's responsibility to close this when no longer needed.
    *
    * @return   The catalog directory context, or <code>null</code> if this
    * is not a cataloging instance.
    */
   synchronized protected DirContext getCatalogContext()
      throws NamingException
   {
      if (!m_cataloging)   // if this was not for catalog use, we're done
         return null;

      /* TODO: need a way to check the stack (SecurityManager.getClassContext)
       * to see if we're being called by the PSDirectoryProviderMetaData class
       */

      // Try to create a thread-safe dup of the directory context so that
      // it can be accessed independently. The Javadoc documentation for
      // Context.lookup() states: "If name is empty, returns a new instance
      // of this context (which represents the same naming context as this
      // context, but its environment may be modified independently and it
      // may be accessed concurrently)."
      DirContext dc = null;
      try {
         // Make sure we have a valid initial context
         prepCataloging();
         // returns a thread-safe duplicate of m_dirContext
         if (m_dirContext == null)
         {
            // Couldn't establish connection to LDAP server
            return null;
         }

         dc = (DirContext)m_dirContext.lookup("");
         return dc;
      }
      catch (CommunicationException ex) {
         // Connection to the LDAP server was broken.  Re-create it and
         // try again.  If this fails, pass the exception up the
         // stack.

         // Clear the DirectoryContext so that prepCataloging() will create
         // another one.   Leave it null if we fail to re-connect.
         m_dirContext = null;
         prepCataloging();
         dc = (DirContext)m_dirContext.lookup("");
         return dc;
      }
      // can't get here.
   }
   
   /**
    * Get group providers to use with this provider
    * 
    * @return The providers, never <code>null</code>, may be empty.
    */
   protected abstract Iterator<IPSGroupProvider> getGroupProviders();   

   /**
    * Gets the connection properties with which this provider was
    * constructed. The password will not be included in these
    * properties because it is unencrypted. This method is meant
    * to be used by the metadata.
    *
    * @return The connection properties.  Never <code>null</code>, will only
    * contain properties for which non-<code>null</code> values were supplied.
    */
   Properties getConnectionProperties()
   {
      // NOTE: Do NOT put the password in the properties, because we
      // don't know where this info is going.
      Properties props = new Properties();
      if (m_providerClassName != null)
         props.setProperty(PROPS_PROVIDER_CLASS_NAME, m_providerClassName);
      if (m_providerURL != null)
         props.setProperty(PROPS_PROVIDER_URL, m_providerURL);
      if (m_authenticationScheme != null)
         props.setProperty(PROPS_AUTH_SCHEME, m_authenticationScheme);
      if (m_authenticationPrincipal != null)
         props.setProperty(PROPS_AUTH_PRINCIPAL, m_authenticationPrincipal);
      if (m_principalAttribute != null)
         props.setProperty(PROPS_ATTR_PRINCIPAL, m_principalAttribute);
      if (m_credentialAttribute != null)
         props.setProperty(PROPS_ATTR_CRED, m_credentialAttribute);
      if (m_credentialFilter != null)
         props.setProperty(PROPS_CRED_FILTER,
            m_credentialFilter.getClass().getName());

      return props;
   }

   /**
    * For directory based authentication, the attribute (column)
    * we will search to find the user id being authenticated.
    *
    * @return the principal attribute; may be <code>null</code>
    */
   String getPrincipalAttribute()
   {
      return m_principalAttribute;
   }
   
   /**
    * @return the credential attribute name used to find the user password, 
    *    may be <code>null<code>, not empty.
    */
   String getCredentialAttribute()
   {
      return m_credentialAttribute;
   }

  /**
    * The URL required to access the JNDI provider. JNDI URLs are
    * provider specific. Some common URL formats are:
    * <ul>
    *   <li>LDAP: ldap://host:port (eg, ldap://srv1:389)</li>
    *   <li>NIS: nis://host/domain (eg, nis://srv1/mydomain.com)</li>
    *   <li>Novell: nds://host (eg, nds://nwsrv1)</li>
    * </ul>
    *
    * @return Provide URL, as a String.  May be <code>null</code>
    */
   String getProviderURL()
   {
      return m_providerURL;
   }

   /**
    * For cataloging and directory based authentication, the associated
    * password for the specified authentication principal.
    * @return The credential as a String; never <code>null</code>, may be empty.
    */
   String getAuthenticationCredential()
   {
      return m_authenticationCredential;
   }

   /**
    * The JNDI provider's Java class name. Each JNDI vendor supplies
    * a set of Java classes for using their provider. This class must
    * implement the javax.naming.spi.InitialContextFactory interface
    * (as required by JNDI for all service providers). For instance:
    * <ul>
    *   <li>LDAP: com.sun.jndi.ldap.LdapCtxFactory</li>
    *   <li>NIS: com.sun.jndi.nis.NISCtxFactory</li>
    *   <li>Novell: com.novell.service.nw.NetWareInitialContextFactory</li>
    * </ul>
    *
    * @return Class name, as a String; never <code>null</code> or empty.
    */
   String getProviderClassName()
   {
      return m_providerClassName;
   }


   /**
    * For cataloging and directory based authentication, we may need a user id
    * to make the connection to the JNDI provider. This will be used
    * for catalog requests (listing users, etc.) as well as
    * authentication.
    *
    * @return The user id.  This is optional; may be <code>null</code>
    */
   String getAuthenticationPrincipal()
   {
      return m_authenticationPrincipal;
   }

   /**
    * For cataloging and connection based authentication, the JNDI provider
    * specific authentication mechanism must be specified. Some common
    * schemes are:
    * <ul>
    *   <li>none - no authentication is performed (act as an anonymous
    *   user)</li>
    *   <li>simple - authentication is performed with a clear-text
    *   password</li>
    *   <li>CRAM-MD5 - this is a common challenge/response mechanism
    *   used by LDAP v3 servers</li>
    * </ul>
    *
    * @return The authentication scheme; ever <code>null</code>
    */
   String getAuthenticationScheme()
   {
      return m_authenticationScheme;
   }

   /**
    * The base context of the directory that is specified by the {@link
    * #m_providerURL} as specified by ldap://host:port/context.  For instance,
    * if the URL specified is "ldap://srv1:389/o=myOrg,c=us", then the value
    * will be "o=myOrg,c=us".  If the base is not specified, this will contain
    * an empty string.  If a base cannot be determined from this url,
    * this value will be <code>null</code>.
    *
    * @return Base context as a String; may be <code>null</code>
    */
   String getBaseContext()
   {
      return m_baseContext;
   }

   /**
    * The JNDI provider's Java class name. Each JNDI vendor supplies
    * a set of Java classes for using their provider. This class must
    * implement the javax.naming.spi.InitialContextFactory interface
    * (as required by JNDI for all service providers). For instance:
    * <ul>
    *   <li>LDAP: com.sun.jndi.ldap.LdapCtxFactory</li>
    *   <li>NIS: com.sun.jndi.nis.NISCtxFactory</li>
    *   <li>Novell: com.novell.service.nw.NetWareInitialContextFactory</li>
    * </ul>
    *
    * Set only in constructor; must not be <code>null</code>
    */
   private String m_providerClassName;

   /**
    * The URL required to access the JNDI provider. JNDI URLs are
    * provider specific. Some common URL formats are:
    * <ul>
    *   <li>LDAP: ldap://host:port (eg, ldap://srv1:389)</li>
    *   <li>NIS: nis://host/domain (eg, nis://srv1/mydomain.com)</li>
    *   <li>Novell: nds://host (eg, nds://nwsrv1)</li>
    * </ul>
    *
    * Set only at construction time; may be <code>null</code>
    */
   private String m_providerURL;

   /**
    * The base context of the directory that is specified by the {@link
    * #m_providerURL} as specified by ldap://host:port/context.  For instance,
    * if the URL specified is "ldap://srv1:389/o=myOrg,c=us", then the value
    * will be "o=myOrg,c=us".  If the base is not specified, this will contain
    * an empty string.  If a base cannot be determined from this url,
    * this value will be <code>null</code>.  Value is determined during
    * construction, never modified after that.
    */
   private String m_baseContext;

   /**
    * For cataloging and connection based authentication, the JNDI provider
    * specific authentication mechanism must be specified. Some common
    * schemes are:
    * <ul>
    *   <li>none - no authentication is performed (act as an anonymous
    *   user)</li>
    *   <li>simple - authentication is performed with a clear-text
    *   password</li>
    *   <li>CRAM-MD5 - this is a common challenge/response mechanism
    *   used by LDAP v3 servers</li>
    * </ul>
    *
    * Set only in constructor; may be <code>null</code>
    */
   private String m_authenticationScheme;

   /**
    * For cataloging and directory based authentication, we may need a user id
    * to make the connection to the JNDI provider. This will be used
    * for catalog requests (listing users, etc.) as well as
    * authentication.
    *
    * Set only in constructor; may be <code>null</code>.
    */
   private String m_authenticationPrincipal;

   /**
    * For cataloging and directory based authentication, the associated
    * password for the specified authentication principal.
    *
    * <p>Set only at construction time; never <code>null
    * </code>, may be empty.
    */
   private String m_authenticationCredential;

   /**
    * For directory based authentication, the attribute (column)
    * we will search to find the user id being authenticated.
    * <p>Set only at construction; may be <code>null</code>
    */
   private String m_principalAttribute;

   /**
    *
    * For directory based authentication, the attribute (column)
    * we will search to find the password being authenticated.
    * <p>Set only at construction; may be <code>null</code>
    */
   private String m_credentialAttribute;

   /**
    * If this instance can be used for cataloging, or if we're using
    * a directory-based authentication model, this is the DC to use.
    *
    * <p> This field may be set at any time after construction. It may
    * be <code>null</code> if the PSJndiProvider isn't meant for cataloging,
    * or if a previous connection to the server was terminated.
    *
    * @see #m_cataloging
    */
   private DirContext m_dirContext = null;

   /**
    * The map of user attributes with alias as key and attribute name as value.
    * This defines the desired attributes to store with the user context after
    * the user has been authenticated. The user entry in the directory may
    * contain many attributes, such as e-mail address, phone number, etc.
    * Specifying attributes to store with the user context allows applications
    * to access the user's attributes in conditionals, etc. Uses the attribute
    * names for cataloging values from the security provider and attribute
    * aliases for storing the values in authenticated user entry. Updated in the
    * constructor and never modified after that.
    */
   private Map m_userAttributes = new HashMap();

   /**
    * For directory based authentication, the password may be
    * stored using some type of hash algorithm. To compare the clear
    * text password, it must first be run through the same hash
    * algorithm. A Java class capable of performing this against
    * the clear text password can be specified to handle this.
    */
   private IPSPasswordFilter m_credentialFilter = null;

   /**
    * Indicates whether this provider was created for the purpose of cataloging.
    */
   private boolean m_cataloging = false;

   /**
    * The JNDI provider's Java class name. Each JNDI vendor supplies
    * a set of Java classes for using their provider. This class must
    * implement the javax.naming.spi.InitialContextFactory interface
    * (as required by JNDI for all service providers). For instance:
    * <ul>
    *   <li>LDAP: com.sun.jndi.ldap.LdapCtxFactory</li>
    *   <li>NIS: com.sun.jndi.nis.NISCtxFactory</li>
    *   <li>Novell: com.novell.service.nw.NetWareInitialContextFactory</li>
    * </ul>
    */
   public static final String PROPS_PROVIDER_CLASS_NAME = "providerClassName";

   /**
    * The URL required to access the JNDI provider. JNDI URLs are
    * provider specific. Some common URL formats are:
    * <ul>
    *   <li>LDAP: ldap://host:port (eg, ldap://srv1:389)</li>
    *   <li>NIS: nis://host/domain (eg, nis://srv1/mydomain.com)</li>
    *   <li>Novell: nds://host (eg, nds://nwsrv1)</li>
    * </ul>
    */
   public static final String PROPS_PROVIDER_URL = "providerURL";

   /**
    * For connection based authentication, the JNDI provider
    * specific authentication mechanism must be specified. Some common
    * schemes are:
    * <ul>
    *   <li>none - no authentication is performed (act as an anonymous
    *   user)</li>
    *   <li>simple - authentication is performed with a clear-text
    *   password</li>
    *   <li>CRAM-MD5 - this is a common challenge/response mechanism
    *   used by LDAP v3 servers</li>
    * </ul>
    */
   public static final String PROPS_AUTH_SCHEME = "authenticationScheme";

   /**
    * The default authentication scheme used if none is specified - "simple".
    * See @link {#PROPS_AUTH_SCHEME}
    */
   public static final String DEFAULT_AUTH_SCHEME = "simple";

   /**
    * Authentication scheme that does not perform any authentication.  See
    * {@link #PROPS_AUTH_SCHEME}.
    */
   public static final String AUTH_SCHEME_NONE = "none";

   /**
    * For directory based authentication, we may need a user id
    * to make the connection to the JNDI provider. This will be used
    * for catalog requests (listing users, etc.) as well as
    * authentication.
    */
   public static final String PROPS_AUTH_PRINCIPAL = "authenticationPrincipal";

   /**
    * For directory based authentication, the associated password
    * for the specified authentication principal.
    */
   public static final String PROPS_AUTH_CRED = "authenticationCredential";

   /**
    * For directory based authentication, the attribute (column)
    * we will search to find the user id being authenticated.
    */
   public static final String PROPS_ATTR_PRINCIPAL = "principalAttribute";

   /**
    *
    * For directory based authentication, the attribute (column)
    * we will search to find the password being authenticated.
    */
   public static final String PROPS_ATTR_CRED = "credentialAttribute";

   /**
    * The property name of the credential filter class.
    */
   public static final String PROPS_CRED_FILTER = "credentialFilterClassName";

   /**
    * Name of the attribute used to identify the object classes of an entry.
    */
   public static final String OBJECT_CLASS_ATTR = "objectclass";

   /**
    * Name of the attribute used to identify an object classes value that
    * denotes a person.
    */
   public static final String OBJECT_CLASS_PERSON_VAL = "person";
}
