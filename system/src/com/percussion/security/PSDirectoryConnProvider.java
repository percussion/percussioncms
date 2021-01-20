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

import com.percussion.design.objectstore.PSAttributeList;
import com.percussion.design.objectstore.PSAuthentication;
import com.percussion.design.objectstore.PSDirectory;
import com.percussion.design.objectstore.PSDirectorySet;
import com.percussion.design.objectstore.PSProvider;
import com.percussion.design.objectstore.PSReference;
import com.percussion.design.objectstore.PSServerConfiguration;
import com.percussion.design.objectstore.PSSubject;
import com.percussion.error.PSRuntimeException;
import com.percussion.server.PSServer;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.security.auth.callback.CallbackHandler;

/**
 * This class uses a directory server as an authentication proxy, meaning
 * that authentication will be successful if the given credentials allow us
 * to log in to the directory server. No actual directory lookups need to
 * be performed, because a successful login to the directory server is all
 * we need.
 */
public class PSDirectoryConnProvider extends PSJndiProvider
{
   /**
    * Convenience constructor that calls 
    * {@link #PSDirectoryConnProvider(Properties, String, boolean) 
    * this(providerAttrs, providerInstance, true)}.
    */
   public PSDirectoryConnProvider(Properties providerAttrs, 
      String providerInstance) throws NamingException
   {
      this(providerAttrs, providerInstance, true);
   }

   /**
    * Construct an instance of this provider which can optionally be used for
    * cataloging.
    *
    * @param providerAttrs The provider attributes. Must not be
    * <CODE>null</CODE>. Must contain all required attributes described in
    * the constructors for {@link com.percussion.security.PSJndiProvider
    * PSJndiProvider}.
    *
    * @param providerInstance The name of this provider instance. Cannot be
    * <CODE>null</CODE>.
    *
    * @param prepCataloging <CODE>true</CODE> if a catalog connection should be
    * established, false otherwise.
    *
    * @throws NamingException If uid or pw defined by the providerAttrs is 
    *    incorrect for logging into directory.
    * @throws PSRuntimeException If the password filter cannot be initialized.
    * @throws IllegalArgumentException If any param is invalid.
    */
   public PSDirectoryConnProvider(Properties providerAttrs, 
      String providerInstance, boolean prepCataloging) throws NamingException
   {
      super(SP_NAME, providerInstance, providerAttrs, prepCataloging);
   }

   /** @see IPSSecurityProvider */
   public PSUserEntry authenticate(String uid, String pw, 
      CallbackHandler callbackHandler)
      throws PSAuthenticationFailedException
   {
      // fail if null uid      
      if (uid == null)
      {
         throw new PSAuthenticationFailedException(
            SP_NAME, m_spInstance, "null");         
      }
         
      if (pw == null)
         pw = "";
         
      PSProvider provider = getDirectoryProvider();
      if (provider == null)
         throw new IllegalStateException(
            "must set a directory provider before authenticating");

      PSServerConfiguration config = PSServer.getServerConfiguration();
      PSDirectorySet directorySet = config.getDirectorySet(
         provider.getReference().getName());
      if (directorySet == null)
      {
         String[] args =
         {
               provider.getReference().getName()
         };
         throw new PSAuthenticationFailedException(
            IPSSecurityErrors.DIR_REFERENCED_DIRECTORYSET_NOT_FOUND, args);
      }
            
      StringBuffer errorMessage = new StringBuffer();
      Iterator<?> references = directorySet.iterator();
      while (references.hasNext())
      {
         try
         {
            PSDirectory directory = config.getDirectory(
               ((PSReference) references.next()).getName());
            if (directory == null)
            {
               String[] args =
               {
                  "Directory is null."
               };
               throw new PSAuthenticationFailedException(
                  IPSSecurityErrors.REFERENCED_DIRECTORY_NOT_FOUND, args);
            }
         
            PSAuthentication authentication = config.getAuthentication(
               directory.getAuthenticationRef().getName());
            if (authentication == null)
            {
               String[] args =
               {
                  "Authentication for Directory " + directory.getName() + " is null."
               };
               throw new PSAuthenticationFailedException(
                  IPSSecurityErrors.REFERENCED_AUTHENTICATION_NOT_FOUND, args);
            }
            
            setProviderProperties(directory, authentication);
            Iterator<?> groupProviders = 
               getGroupProviders();
            while (groupProviders.hasNext())
            {
               PSJndiGroupProvider groupProvider = 
                  (PSJndiGroupProvider) groupProviders.next();
               groupProvider.setProviderUrl(directory.getProviderUrl());
            }
            
            String encryptedPassword = getEncryptedPassword(pw);
            
            /* 
             * If no password has been supplied, the default behavior seems to be 
             * that the connection is made anonymously no matter what
             * authentication scheme has been specified. This will allow you to 
             * log in with a valid username and no password, even if the user 
             * has a password setup in the directory. To block this, we will 
             * not allow logins with no password unless the authentication 
             * scheme is "none".
             */
            if (encryptedPassword.length() == 0 &&
                !getAuthenticationScheme().equals(AUTH_SCHEME_NONE))
            {
               Object[] args = {getInstance()};
               throw new PSAuthenticationFailedException(
                  IPSSecurityErrors.DIR_PASSWORD_REQUIRED, args);
            }

            DirContext context = null; 
            try
            {
               Hashtable<String, Object> env = new Hashtable<String, Object>();
               env.put(Context.INITIAL_CONTEXT_FACTORY, getProviderClassName());
               env.put(Context.PROVIDER_URL, PSJndiUtils.getEncodedProviderUrl(
                  getProviderURL()));
               env.put(Context.SECURITY_AUTHENTICATION, 
                  getAuthenticationScheme());
         
               String objectAttributeName = 
                  directorySet.getRequiredAttributeName(
                     PSDirectorySet.OBJECT_ATTRIBUTE_KEY);
               String fullUserName = PSJndiUtils.getFullUserName(
                  uid, objectAttributeName, getBaseContext());
               env.put(Context.SECURITY_PRINCIPAL, fullUserName);
               env.put(Context.SECURITY_CREDENTIALS, 
                  PSJndiUtils.getFullPassword(encryptedPassword, 
                     getCredentialAttribute()));
               PSJndiUtils.addConnectionPooling(env);

               try
               {
                  // creating the context does the authentication for us
                  context = new InitialDirContext(env);
               }
               catch (NamingException e)
               {
                  // try authenticating doing a search
                  int scope = directory.isShallowCatalogOption() ?
                     SearchControls.ONELEVEL_SCOPE : 
                        SearchControls.SUBTREE_SCOPE;
                        
                  context = authenticate(uid, objectAttributeName, 
                     encryptedPassword, env, scope, authentication, directory);
                  
                  /*
                   * The full user name might have changed if we authenticate
                   * through a search and must be updated.
                   */
                  fullUserName = (String) env.get(Context.SECURITY_PRINCIPAL);
               }

               // get user attributes
               PSUserAttributes attributeValues = null;
               if (m_directoryCataloger != null)
               {
                  PSSubject subject = m_directoryCataloger.getAttributes(uid);
                  PSAttributeList attributes = subject.getAttributes();
                  if (attributes != null && attributes.size() > 0)
                     attributeValues = new PSUserAttributes(attributes);
               }
            
               // process group providers
               PSGroupEntry[] groupEntries = new PSGroupEntry[0];
               if (getGroupProviders().hasNext())
               {
                  groupEntries = getGroupEntries(uid);
               }

               return new PSUserEntry(uid, 0, groupEntries, attributeValues, 
                  PSUserEntry.createSignature(uid, pw));
            }
            catch (NamingException e)
            {
               String[] args =
               {
                  directory.getName(),
                  authentication.getName(),
                       e.toString()
               };
               
               throw new PSAuthenticationFailedException(
                  IPSSecurityErrors.DIRECTORY_AUTHENTICATION_FAILED, args);
            }
            finally
            {
               if (context != null)
                  try { context.close(); } 
                  catch (NamingException e) { /* noop */ };
            }
         }
         catch (PSAuthenticationFailedException e)
         {
            errorMessage.append(e.getLocalizedMessage() + "\n");
         }
      }
      
      throw new PSAuthenticationFailedException(getInstance(),
         m_spInstance, uid, errorMessage.toString());
   }

   /** @see IPSSecurityProvider */
   public IPSSecurityProviderMetaData getMetaData()
   {
      return new PSDirectoryConnProviderMetaData(this);
   }
   
   /**
    * Authenticates by searching for the object and object attribute name for
    * the supplied parameters. If the search was successful, a new context is 
    * created which does the authentication for us.
    * 
    * @param user the user to authenticate, assumed not <code>null</code>.
    * @param userAttributeName the attribute name of the user, assumed not
    *    <code>null</code>.
    * @param password the password to authenticate with, assumed not 
    *    <code>null</code>.
    * @param env the cataloging environment, assumed not <code>null</code>.
    * @param scope the sope in which to search,  assumed to be one of
    *    <code>SearchControls.xxx_SCOPE</code>.
    * @param authentication the authentication used for the currently processed 
    *    directory, assumed not <code>null</code>.
    * @param directory the directory used for the authentication, assumed not
    *    <code>null</code>.
    * @return a valid context if the user was authenticated, <code>null</code>
    *    otherwise.
    * @throws NamingException for all naming errors.
    */
   private DirContext authenticate(String user, String userAttributeName, 
      String password, Hashtable<String, Object> env, int scope, 
      PSAuthentication authentication, PSDirectory directory) 
      throws NamingException
   {
      // get the authentication environment
      Hashtable<String, Object> environment = getEnvironment(user, userAttributeName, 
         password, env, scope, authentication, directory);

      // creating the context does the authentication for us
      return new InitialDirContext(environment);
   }
   
   /**
    * Setup the environment required for authentication. First the object is
    * looked up for the supplied user and user attribute name. If exactly
    * one result is found, the object's attribute name and value are added
    * to the supplied environment.
    * 
    * @param user the user for which to search, assumed not <code>null</code>.
    * @param userAttributeName the user attribute name to search for,
    *    assumed not <code>null</code>.
    * @param password the password to authenticate with, assumed not 
    *    <code>null</code>.
    * @param env the environment setup for cataloging, assumed not
    *    <code>null</code>.
    * @param scope the sope in which to search,  assumed to be one of
    *    <code>SearchControls.xxx_SCOPE</code>.
    * @param the authentication used for the currently processed directory, 
    *    assumed not <code>null</code>.
    * @param directory the directory used for the authentication, assumed not
    *    <code>null</code>.
    * @return the environment setup for authentication, never <code>null</code>.
    * @throws NamingException for any naming errors.
    */
   private Hashtable<String, Object> getEnvironment(String user, String userAttributeName, 
      String password, Hashtable<String, Object> env, int scope,
      PSAuthentication authentication, PSDirectory directory) 
      throws NamingException
   {
      // remove and save current principal and credentials
      Object principal = env.remove(Context.SECURITY_PRINCIPAL);
      Object credential = env.remove(Context.SECURITY_CREDENTIALS);
      PSJndiUtils.addConnectionPooling(env);
      // add principal and credentials which have cataloging access
      env.put(Context.SECURITY_PRINCIPAL, authentication.getPrincipal(
         directory.getProviderUrl()));
      env.put(Context.SECURITY_CREDENTIALS, authentication.getCredentials());
            
      DirContext context = new InitialDirContext(env);
      NamingEnumeration<?> results = null;
      try
      {
         /*
          * Setup the search controls for the supplied scope. We always take the
          * first user found in case there are more users matching the supplied
          * filter.
          */
         SearchControls searchControls = new SearchControls();
         searchControls.setSearchScope(scope);
         searchControls.setCountLimit(1);
      
         Map<String, String> filter = new HashMap<String, String>();
         filter.put(userAttributeName, user);

         boolean isRelative = true;
         /*
          * The plain attribute name of the found user, e.g. cn, sn.
          */
         String objectAttributeName = null;
         /*
          * The plain user name of the found user without the attribute 
          * prefix and path.
          */
         String objectAttributeUser = null;
         /*
          * The remaining path of the found user without the name and 
          * prepending separator.
          */
         String objectAttributePath = null;
         results = context.search("", 
            PSJndiUtils.buildFilter(filter), searchControls);
         if (results.hasMore())
         {
            SearchResult result = (SearchResult) results.next();
            
            NameParser parser = context.getNameParser("");
            String resName = result.getName();
            
             /* Strip any quotes around the name - this happens if the name
              * contains certain characters
              */
            if (resName.startsWith("\"") && resName.endsWith("\"") && 
               resName.length() > 1)
            {
               resName = resName.substring(1, resName.length() - 1);
            }
            
            Name name = parser.parse(resName);
            
            String objectName = name.get(name.size()-1);
            int pos = objectName.indexOf("=");
            if (pos > 0)
            {
               isRelative = result.isRelative();
               objectAttributeName = objectName.substring(0, pos);
               objectAttributeUser = objectName.substring(pos+1);
            }
            
            if (name.size() > 1)
            {
               name.remove(name.size()-1);
               objectAttributePath = name.toString();
            }
         }
         
         if (objectAttributeName != null && objectAttributeUser != null)
         {
            // if we found the principal attribute and it's value we use it
            String baseContext = "";
            if (isRelative)
            {
               if (objectAttributePath != null)
                  baseContext += objectAttributePath + ",";
                  
               baseContext += getBaseContext();
            }
            else if (objectAttributePath != null)
               baseContext += objectAttributePath;
            
            env.put(Context.SECURITY_PRINCIPAL, 
               PSJndiUtils.getFullUserName(objectAttributeUser, 
                  objectAttributeName, baseContext));
            env.put(Context.SECURITY_CREDENTIALS, 
               PSJndiUtils.getFullPassword(password, getCredentialAttribute()));
         }
         else
         {
            // otherwise restore the original principal and credential
            env.put(Context.SECURITY_PRINCIPAL, principal);
            env.put(Context.SECURITY_CREDENTIALS, credential);
         }
         
         return env;
      }
      finally
      {
         if (results != null)
            try {results.close();} catch(NamingException e) {}
            
         try {context.close();} catch(NamingException e) {}
      }
   }

   /**
    * Get the directory cataloger associated with this security provider.
    * 
    * @return the directory cataloger used with this security provider,
    *    may be <code>null</code> if none is used.
    */
   public IPSDirectoryCataloger getDirectoryCataloger()
   {
      return m_directoryCataloger;
   }
   
   // see base class
   protected Iterator<IPSGroupProvider> getGroupProviders()
   {
      return m_directoryCataloger.getGroupProviders();
   }
   
   /**
    * Convenience method that calls {@link #setDirectoryProvider(PSProvider, 
    * Properties, PSServerConfiguration) 
    * setDirectoryProvider(PSProvider, Properties, null)}.
    */
   public void setDirectoryProvider(PSProvider provider, Properties props)
   {
      setDirectoryProvider(provider, props, null);
   }
   
   /**
    * Get the directory provider associated with this security provider.
    * 
    * @return the currently used directory provider, may be <code>null</code>.
    */
   public PSProvider getDirectoryProvider()
   {
      return m_directoryProvider; 
   }
   
   /**
    * Set the supplied directory provider.
    * 
    * @param provider the provider to set, <code>null</code> if no directory
    *    cataloging is required.
    * @param props the security provider properties, never <code>null</code>,
    *    may be empty, the method takes ownership.
    */
   public void setDirectoryProvider(PSProvider provider, Properties props, 
      PSServerConfiguration config)
   {
      m_directoryProvider = provider;
      if (provider != null)
      {
         if (!provider.isProviderFor(
            PSDirectoryServerCataloger.class.getName()))
               throw new IllegalArgumentException(
                  "provider must be for PSDirectoryServerCataloger");
               
         props = new Properties();
         props.put(PSSecurityProvider.PROVIDER_NAME, 
            provider.getReference().getName());
      }
      
      setDirectoryCataloger(provider, props, config);
   }
   
   /**
    * Set and instatiate the directory cataloger for the supplied provider.
    * 
    * @param provider the directory cataloger provider, may be <code>null</code>
    *    to remove the current directory cataloger.
    * @param properties the configuration properties required to instantiate
    *    the requested directory provider, assumed not <code>null</code>, may
    *    be empty.
    * @param config the server configuration, may be <code>null</code>.
    */
   private void setDirectoryCataloger(PSProvider provider,
      Properties properties, PSServerConfiguration config)
   {
      if (provider == null)
         m_directoryCataloger = null;
      else if (config == null)
         m_directoryCataloger = 
            (IPSDirectoryCataloger) instantiateProvider(
               provider, properties);
      else
         m_directoryCataloger = 
            (IPSDirectoryCataloger) instantiateProvider(
               provider, properties, config);
   }
   
   /**
    * The name of this security provider.
    */
   public static final String SP_NAME = "DirectoryConn";

   /**
    * The class name of this security provider.
    */
   public static final String SP_CLASSNAME = 
      PSDirectoryConnProvider.class.getName();
   
   
   /**
    * The directory provider used with this security provider, may be
    * <code>null</code> if no directory cataloging is needed. Initialized
    * through {@link #setDirectoryProvider(PSProvider, Properties)}.
    */
   private PSProvider m_directoryProvider = null;

   /**
    * The directory cataloger used with this security provider, may be
    * <code>null</code> if the security provider does not need directory
    * cataloging.
    */
   private IPSDirectoryCataloger m_directoryCataloger = null;
}
