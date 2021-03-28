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

import com.percussion.data.PSDataExtractionException;
import com.percussion.design.objectstore.PSLiteral;
import com.percussion.design.objectstore.PSLiteralSet;
import com.percussion.design.objectstore.PSProvider;
import com.percussion.design.objectstore.PSServerConfiguration;
import com.percussion.error.PSException;
import com.percussion.log.PSLogManager;
import com.percussion.log.PSLogServerWarning;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSRequest;
import com.percussion.server.PSUserSession;
import com.percussion.utils.io.PathUtils;
import com.percussion.legacy.security.deprecated.PSCryptographer;
import com.percussion.legacy.security.deprecated.PSLegacyEncrypter;
import com.percussion.utils.security.PSRemoteUserCallback;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.log4j.Logger;

/**
 * The PSSecurityProvider abstract class is extended by the various security
 * providers supported within E2. Security providers are used to authenticate
 * users and to associate attributes with users.
 * <p>
 * <em>Its recommended that providers also check {@link #getRemoteUser(CallbackHandler)} instead of just
 * the supplied user id.</em>
 *
 * @author     Tas Giakouminakis
 * @author adamgent (augmented)
 * @version    1.0
 * @since      1.0
 */
public abstract class PSSecurityProvider implements IPSSecurityProvider
{
   /**
    * Construct an instance of this provider.
    *
    * @param spType the security provider type of the subclass, not
    *    <code>null<code> or empty.
    * @param spInstance the security provider instance name, not 
    *    <code>null<code> or empty.
    */
   protected PSSecurityProvider(String spType, String spInstance)
   {
      if (spType == null)
         throw new IllegalArgumentException("spType cannot be null");
         
      spType = spType.trim();
      if (spType.length() == 0)
         throw new IllegalArgumentException("spType canot be empty");
         
      if (spInstance == null)
         throw new IllegalArgumentException("spInstance cannot be null");
         
      spInstance = spInstance.trim();
      if (spInstance.length() == 0)
         throw new IllegalArgumentException("spInstance cannot be empty");
         
      m_spType = spType;
      m_spInstance = spInstance;
   }

   /** @see IPSSecurityProvider */
   public String getType()
   {
      return m_spType;
   }

   /** @see IPSSecurityProvider */
   public int getTypeId()
   {
      return getSecurityProviderTypeFromXmlFlag(m_spType);
   }

   /** @see IPSSecurityProvider */
   public String getInstance()
   {
      return m_spInstance;
   }

   /**
    * Get the String representation of the specified provider type
    *
    * @param providerType the security provider type, should be one of the
    *    <code>SP_TYPE_xxx</code> value
    * @return the String representation, never <code>null</code>, may be empty
    *    no provider type matches the specified type
    */
   public static String getSecurityProviderTypeString(int providerType)
   {
      String sRet = null;
      Iterator<Map.Entry<String,Integer>> it = ms_mapTypeName2TypeId.entrySet().iterator();
      while (it.hasNext())
      {
         Map.Entry<String,Integer> item = it.next();
         Integer value = (Integer)item.getValue();
         if (value.intValue() == providerType)
         {
            sRet = (String) item.getKey();
            break;
         }
      }
      if (sRet == null)
         sRet = "";
         
      return sRet;
   }

   /**
    * The opposite of {@link #getSecurityProviderTypeString(int)
    * getSecurityProviderTypeString}. This method can be used to obtain the
    * security provider type id of any string returned by <code>
    * getSecurityProviderTypeString</code>.
    *
    * @param xmlFlag one of the XML_FLAG_... types, may be <code>null<code>.
    * @return the type id that matches the supplied flag, or 0 if a match is
    *    not found. The comparison is case sensitive.
    */
   public static int getSecurityProviderTypeFromXmlFlag(String xmlFlag)
   {
      int providerId = 0;

      Integer typeId = ms_mapTypeName2TypeId.get(xmlFlag);

      if (typeId != null)
         providerId = typeId.intValue();

      return providerId;
   }
   
   /**
    * Determines if the internal server user ({@link #INTERNAL_USER_NAME}) has
    * been authenticated for the supplied request context.
    *
    * @param request The request context to check, may not be <code>null</code>.
    * @return <code>true</code> if the internal user has been authenticated,
    *    <code>false</code> otherwise.
    * @throws IllegalArgumentException if <code>request</code> is invalid.
    * @throws PSDataExtractionException if the information required cannot be
    *    extracted from the supplied context.
    */
   public static boolean isInternalUser(IPSRequestContext request)
      throws PSDataExtractionException
   {
      if (request == null)
         throw new IllegalArgumentException("request may not be null");

      boolean isInternalUser = false;

      String userCtx = "User/Name";
      PSLiteralSet userSet = (PSLiteralSet)request.getUserContextInformation(
         userCtx, null);

      if (userSet != null)
      {
         Iterator<?> userNames = userSet.iterator();

         String userName = null;
         while (userNames.hasNext() && !isInternalUser)
         {
            userName = ((PSLiteral)userNames.next()).getValueText();
            if (INTERNAL_USER_NAME.equals(userName))
            {
               isInternalUser = true;
            }
         }
      }

      return isInternalUser;
   }

   /**
    * Gets the remote user from call-back handler.
    * This will return the user id set by normal J2EE authentication.
    * Its recommended that providers use this user id if its not null or blank instead of
    * the user id supplied by {@link #authenticate(String, String, CallbackHandler)}. 
    * @param callbackHandler not <code>null</code>
    * @return maybe null or empty which means no remote user.
    * @throws IOException
    * @throws UnsupportedCallbackException
    */
   protected String getRemoteUser(CallbackHandler callbackHandler) throws IOException, UnsupportedCallbackException {
      try
      {
         PSRemoteUserCallback remoteUserCallBack = new PSRemoteUserCallback();
         callbackHandler.handle(new Callback[]
         {remoteUserCallBack});
         return remoteUserCallBack.getRemoteUser();
      }
      catch (Exception e)
      {
         ms_logger.warn("Exception while getting remote user:", e);
      }
      return null;
   }

   /**
    * Checks the supplied type against all of the available types and returns
    * a flag indicating if it was found or not. Not all types are available on
    * all OS platforms.
    *
    * @param type One of the SP_TYPE_... values.
    * @return <code>true</code> if type is one of the security providers
    *    supported on this platform, <code>false</code> otherwise.
    */
   public static boolean isSupportedType(int type)
   {
      boolean supported =
         type == SP_TYPE_BETABLE
            || type == SP_TYPE_WEB_SERVER
            || type == SP_TYPE_SPNEGO
            || type == SP_TYPE_HOST_ADDRESS
            || type == SP_TYPE_ROLE
            || type == SP_TYPE_ODBC
            || type == SP_TYPE_DIRCONN
            || type == SP_TYPE_ANY
            || type == SP_TYPE_J2EE
            || type == SP_TYPE_RXINTERNAL;

      return supported;
   }

   /**
    * See if the specified user entries already exists in the user session.
    *
    * @param      req         the request context
    *
    * @param      entry       the entry to check
    *
    * @return                 <code>true</code> if it does
    */
   protected boolean isUserEntryDefined(PSRequest req, PSUserEntry entry)
   {
      if (req == null)
         return false;

      PSUserSession sess = req.getUserSession();
      if (sess == null)
         return false;

      PSUserEntry[] users = sess.getAuthenticatedUserEntries();
      int size = (users == null) ? 0 : users.length;
      for (int i = 0; i < size; i++) 
      {
         // is match checks SP info and name only, so this will match
         // even though we skipped group/attribute info
         if (users[i].isMatch(entry))
            return true;
      }

      return false;
   }

   // no javadoc on purpose
   final protected String appendProcessing(String uid, String str)
   {
      if ((str == null) || (str.equals("")))
         return "";

      String partTwo = uid;
      if (uid == null || uid.equals(""))
         partTwo = PSLegacyEncrypter.getInstance(
                 PathUtils.getRxDir().getAbsolutePath().concat(PSEncryptor.SECURE_DIR)
         ).getPartTwoKey();

      try {
         return PSEncryptor.getInstance("AES",
                 PathUtils.getRxDir().getAbsolutePath().concat(PSEncryptor.SECURE_DIR)).decrypt(str);
      } catch (PSEncryptionException e) {
         return PSCryptographer.decrypt(PSLegacyEncrypter.getInstance(
                 PathUtils.getRxDir().getAbsolutePath().concat(PSEncryptor.SECURE_DIR)
         ).getPartOneKey(), partTwo, str);
      }

   }

   /**
    * Get the default directory provider.
    * 
    * @return the default directory provider if one is specified,
    *    <code>null</code> otherwise.
    */
   public PSProvider getDefaultDirectoryProvider()
   {
      return null;
   }

   /**
    * Convenience method that calls {@link #instantiateProvider(PSProvider, 
    * Properties, PSServerConfiguration) 
    * instantiateProvider(provider, properties, null)}. See that method for more 
    * details.
    */
   public static Object instantiateProvider(PSProvider provider,
      Properties properties)
   {
      return instantiateProvider(provider, properties, null);
   }
   
   /**
    * Instantiates a new cataloger for the requested provider. Errors will
    * be logged and printed to the console.
    *
    * @param provider the provider for which to instantiate a new cataloger,
    *    not <code>null</code>.
    * @param properties the properties required to initialize the requested
    *    cataloger, not <code>null</code>, may be empty.
    * @param config the server configuration used to construct the provider,
    *    may be <code>null</code>.
    * @return a new cataloger instance of the requested provider,
    *    <code>null</code> if it failed to be instantiated.
    */
   public static Object instantiateProvider(PSProvider provider,
      Properties properties, PSServerConfiguration config)
   {
      if (provider == null)
         throw new IllegalArgumentException("provider cannot be null");
         
      Object cataloger = null;
      String origin = "SecurityProvider";
      try
      {
         Class<?> c = Class.forName(provider.getClassName());
         Constructor<?> ctor = c.getConstructor(
            new Class[] 
            { 
               Class.forName(Properties.class.getName()), 
               Class.forName(PSServerConfiguration.class.getName())
            });

         cataloger = ctor.newInstance(
            new Object[] 
            { 
               properties,
               config
            });
      }
      catch (ClassNotFoundException e)
      {
         Object[] args =
         {
            provider.getClassName(),
            PSException.getStackTraceAsString(e)
         };

         PSLogManager.write(new PSLogServerWarning(
            IPSSecurityErrors.CATALOG_PROVIDER_CLASS_NOT_FOUND, args, true,
               origin));
      }
      catch (InstantiationException e)
      {
         Object[] args =
         {
            provider.getClassName(),
            PSException.getStackTraceAsString(e)
         };

         PSLogManager.write(new PSLogServerWarning(
            IPSSecurityErrors.CATALOG_PROVIDER_INSTANTIATION_FAILED, args, true,
               origin));
      }
      catch (IllegalAccessException e)
      {
         Object[] args =
         {
            provider.getClassName(),
            PSException.getStackTraceAsString(e)
         };

         PSLogManager.write(new PSLogServerWarning(
            IPSSecurityErrors.CATALOG_PROVIDER_ILLEGAL_ACCESS, args, true,
               origin));
      }
      catch (InvocationTargetException e)
      {
         Object[] args =
         {
            provider.getClassName(),
            PSException.getStackTraceAsString(e)
         };

         PSLogManager.write(new PSLogServerWarning(
            IPSSecurityErrors.CATALOG_PROVIDER_INVOCATION_TARGET_ERROR, args,
               true, origin));
      }
      catch (NoSuchMethodException e)
      {
         Object[] args =
         {
            provider.getClassName(),
            PSException.getStackTraceAsString(e)
         };

         PSLogManager.write(new PSLogServerWarning(
            IPSSecurityErrors.CATALOG_PROVIDER_NO_SUCH_METHOD, args,
               true, origin));
      }

      return cataloger;
   }

   /**
    * The security provider type for the Host Address security provider.
    */
   public static final int SP_TYPE_HOST_ADDRESS = 0x00000001;

   /**
    * The security provider type for the Web Server security provider.
    */
   public static final int SP_TYPE_WEB_SERVER = 0x00000002;

   /**
    * The security provider type for the Role security provider.
    */
   public static final int SP_TYPE_ROLE = 0x00000008;

   /**
    * The security provider type for the ODBC security provider.
    */
   public static final int SP_TYPE_ODBC = 0x00000010;

   /**
    * The security provider type for the directory connection security
    * provider.
    */
   public static final int SP_TYPE_DIRCONN = 0x00000040;

   /**
    * The security provider type for the back end table security provider.
    */
   public static final int SP_TYPE_BETABLE = 0x00000080;
   
   /**
    * The security provider type for the J2EE security provider
    */
   public static final int SP_TYPE_J2EE = 0x00000200;

   /**
    * The security provider type for Spnego/Kerberos.
    */
   public static final int SP_TYPE_SPNEGO = 0x00000400;   

   /**
    * This is a special provider that can be used by requests made internally.
    * No external request can be validated using this provider. The purpose
    * is to allow access to apps w/o a user request. The sp instance is not
    * recognized when this type is used. The default user for this type is
    * uid={@link #INTERNAL_USER_NAME}, no pw.
    */
   public static final int SP_TYPE_RXINTERNAL = 0x00000100;

   /**
    * This is used to signify any security provider type (where appropriate).
    */
   public static final int SP_TYPE_ANY = 0xFFFFFFFF;

   /**
    * The XML representation of the security provider flag
    * <code>SP_TYPE_BETABLE</code>.
    */
   public static final String XML_FLAG_SP_BETABLE  = "BackEndTable";

   /**
    * The XML representation of the security provider flag
    * <code>SP_TYPE_HOST_ADDRESS</code>.
    */
   public static final String XML_FLAG_SP_HOST = "HostAddress";

   /**
    * The XML representation of the security provider flag
    * <code>SP_TYPE_ODBC</code>.
    */
   public static final String XML_FLAG_SP_ODBC = "ODBC";

   /**
    * The XML representation of the security provider flag
    * <code>SP_TYPE_WEB_SERVER</code>.
    */
   public static final String XML_FLAG_SP_WEB = "WebServer";

   /**
    * The XML representation of the Spnego security provider flag
    * <code>XML_FLAG_SP_SPNEGO</code>.
    */
   public static final String XML_FLAG_SP_SPNEGO = "Spnego";

   /**
    * The XML representation of the security provider flag
    * <code>SP_TYPE_DIRCONN</code>.
    */
   public static final String XML_FLAG_SP_DIRCONN = "DirectoryConn";

   /**
    * The XML representation of the security provider flag
    * <code>SP_TYPE_ROLE</code>.
    */
   public static final String XML_FLAG_SP_ROLE = "Role";
   
   /**
    * The XML representation of the security provider flag
    * <code>SP_TYPE_J2EE</code>
    */
   public static final String XML_FLAG_SP_J2EE = "J2EE";

   /**
    * The XML representation of the security provider flag
    * <code>SP_TYPE_ANY</code>.
    */
   public static final String XML_FLAG_SP_ANY = "Any";

   /**
    * The textual representation for {@link #SP_TYPE_RXINTERNAL}.
    */
   public static final String XML_FLAG_SP_INTERNAL = "RxInternal";

   /**
    * If you use the {@link #SP_TYPE_RXINTERNAL} security provider, you
    * should use this as the name of the user in the app's acl.
    */
   public static final String INTERNAL_USER_NAME = "rxserver";

   /**
    * The security provider type <code>String</code>. Initialized while 
    * constructed, never <code>null<code>, empty or changed after that.
    */
   protected String m_spType = null;

   /**
    * The security provider instance string. Initialized in constructor. Never
    * changed after that, never <code>null</code> or empty.
    */
   protected String m_spInstance = null;
   
   /**
    * The security providers meta data. Initialized with the first call to
    * {@link IPSSecurityProvider#getMetaData()}, never <code>null<code> 
    * or changed after that.
    */
   protected IPSSecurityProviderMetaData m_metaData = null;

   /**
    * Map containing the name of the security provider (<code>String</code>
    * object) as key and security provider type (<code>Integer</code> object)
    * as value. Initialized in the static block. Never modified after
    * initialization. Used to quickly translate from string based type names
    * to the corresponding numeric type id.
    */
   private static Map<String, Integer> ms_mapTypeName2TypeId = 
      new HashMap<String, Integer>();

   /**
    * never null.
    */
   private static final Logger ms_logger = Logger.getLogger(PSSecurityProvider.class);

   /**
    * The property key used to specify the provider reference. The referenced 
    * provider definition will be used to initialize the cataloger.
    */
   public static final String PROVIDER_NAME = "providerName";
   static 
   {
      ms_mapTypeName2TypeId.put(XML_FLAG_SP_WEB,
         new Integer(SP_TYPE_WEB_SERVER));
      ms_mapTypeName2TypeId.put(XML_FLAG_SP_SPNEGO,
         new Integer(SP_TYPE_SPNEGO));
      ms_mapTypeName2TypeId.put(XML_FLAG_SP_HOST,
         new Integer(SP_TYPE_HOST_ADDRESS));
      ms_mapTypeName2TypeId.put(XML_FLAG_SP_ODBC, 
         new Integer(SP_TYPE_ODBC));
      ms_mapTypeName2TypeId.put(XML_FLAG_SP_BETABLE,
         new Integer(SP_TYPE_BETABLE));
      ms_mapTypeName2TypeId.put(XML_FLAG_SP_DIRCONN,
         new Integer(SP_TYPE_DIRCONN));
      ms_mapTypeName2TypeId.put(XML_FLAG_SP_ROLE, 
         new Integer(SP_TYPE_ROLE));
      ms_mapTypeName2TypeId.put(XML_FLAG_SP_ANY, 
         new Integer(SP_TYPE_ANY));
      ms_mapTypeName2TypeId.put(XML_FLAG_SP_INTERNAL,
         new Integer(SP_TYPE_RXINTERNAL));
      ms_mapTypeName2TypeId.put(XML_FLAG_SP_J2EE,
         new Integer(SP_TYPE_J2EE));
   }
}

