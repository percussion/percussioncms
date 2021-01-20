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

package com.percussion.design.objectstore.legacy;

import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSAcl;
import com.percussion.design.objectstore.PSAuthentication;
import com.percussion.design.objectstore.PSDataEncryptor;
import com.percussion.design.objectstore.PSDirectory;
import com.percussion.design.objectstore.PSDirectorySet;
import com.percussion.design.objectstore.PSErrorWebPages;
import com.percussion.design.objectstore.PSGroupProviderInstance;
import com.percussion.design.objectstore.PSJavaPluginConfig;
import com.percussion.design.objectstore.PSJdbcDriverConfig;
import com.percussion.design.objectstore.PSLogger;
import com.percussion.design.objectstore.PSNotifier;
import com.percussion.design.objectstore.PSRoleProvider;
import com.percussion.design.objectstore.PSSearchConfig;
import com.percussion.design.objectstore.PSServerCacheSettings;
import com.percussion.design.objectstore.PSServerConfiguration;
import com.percussion.design.objectstore.PSUnknownDocTypeException;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.error.PSIllegalArgumentException;
import com.percussion.security.PSSecurityProvider;
import com.percussion.util.PSCollection;
import com.percussion.xml.PSXmlTreeWalker;

import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Provides access to obsolete legacy objects defined in a server configuration.
 * Will restore them via {@link #fromXml(Document)}, but will not save them
 * when {@link PSServerConfiguration#toXml()} is called.
 */
public class PSLegacyServerConfig extends PSServerConfiguration
{
   /**
    * Construct a Java object from its XML representation. See the
    * {@link #toXml() toXml} method for a description of the XML object.  
    * Additionally supports loading the legacy 
    * BackEndConnections/PSXBackendConnection, 
    * Credentials/PSXBackendCredential, and
    * SecurityProviderInstance/PSXSecurityProviderInstance elements.  The newly
    * supported Jdbc Driver Configuration/PSXJdbcDriverConfig elements will also
    * be loaded.
    * 
    * @param sourceDoc the XML document to construct this object from
    * 
    * @throws PSUnknownNodeTypeException if an XML element node is not of the
    * appropriate type
    * @throws PSUnknownDocTypeException if the XML document is not of the
    * appropriate type
    */
   public PSLegacyServerConfig(Document sourceDoc)
      throws PSUnknownDocTypeException, PSUnknownNodeTypeException
   {
      this();
      fromXml(sourceDoc);
   }
   
   /**
    * Construct an empty legacy server configuration object.
    */
   @SuppressWarnings("unchecked") PSLegacyServerConfig()
   {
      super();
      m_backEndConnections = new PSCollection(PSLegacyBackEndConnection.class);
      m_backEndCredentials = new PSCollection(PSLegacyBackEndCredential.class);
      m_securityProviders = new PSCollection(PSLegacySecurityProviderInstance.class);
      m_serverCacheSettings = new PSServerCacheSettings();
   }
   
   /**
    * This method is called to populate a PSServerConfiguration Java object
    * from a PSXserver configuration XML document. See the
    * {@link #toXml() toXml} method for a description of the XML object.
    *
    * @throws     PSUnknownDocTypeException  if the XML document is not
    *                                        of type PSXserver configuration
    */
   public void fromXml(Document sourceDoc)
   throws PSUnknownDocTypeException, PSUnknownNodeTypeException
   {
      if (null == sourceDoc)
         throw new PSUnknownDocTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL, ms_NodeType);

      Element root = sourceDoc.getDocumentElement();
      if (root == null)
         throw new PSUnknownDocTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL, ms_NodeType);

      //make sure we got the correct root node tag
      if (false == ms_NodeType.equals (root.getNodeName()))
      {
         Object[] args = { ms_NodeType, root.getNodeName() };
         throw new PSUnknownDocTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }

      // get the server type
      String sType = root.getAttribute(XML_TYPE_ATTR);
      if (XML_ATTR_TYPE_PUBLISHING_HUB.equalsIgnoreCase(sType))
         m_serverType = SERVER_TYPE_PUBLISHING_HUB;
      else
         m_serverType = SERVER_TYPE_SYSTEM_MASTER;

      //Read PSXServerConfiguration object attributes
      PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceDoc);
      String sTemp = tree.getElementData("id");
      try {
         m_id = Integer.parseInt(sTemp);
      } catch (Exception e) {
         Object[] args = { ms_NodeType, ((sTemp == null) ? "null" : sTemp) };
         throw new PSUnknownDocTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ID, args);
      }

      //Get requestRoot from XML
      try {
         setRequestRoot(tree.getElementData("requestRoot"));
      } catch (PSIllegalArgumentException e) {
         throw new PSUnknownDocTypeException(ms_NodeType, "requestRoot", e);
      }

      //Get defaultAppName from XML
      setDefaultApplication(tree.getElementData("defaultAppName"));

      String allow = tree.getElementData( NN_ALLOW_AUTH_DETAILS );
      if ( null != allow )
         setAllowDetailedAuthenticationMessages( allow.equals( XML_BOOL_TRUE ));

      int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN;
      int nextFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS;
      firstFlags |= PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
      nextFlags  |= PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

      org.w3c.dom.Node cur = tree.getCurrent();   // cur = <PSXServerConfiguration>

      //construct new   PSAcl from xml
      if (tree.getNextElement(PSAcl.ms_NodeType, firstFlags) != null) {
         m_acl = new PSAcl();
         m_acl.fromXml((Element)tree.getCurrent(), this, null);
      }

      tree.setCurrent(cur);

      //create the data PSDataEncryptor object from XML node
      if (tree.getNextElement("DataEncryption", firstFlags) != null) {
         if (tree.getNextElement(PSDataEncryptor.ms_NodeType, firstFlags) != null) {
            m_dataEncryptor = new PSDataEncryptor(false);   // off by default
            m_dataEncryptor.fromXml((Element)tree.getCurrent(), this, null);
         }
      }

      tree.setCurrent(cur);

      //create the admin PSDataEncryptor object from XML node
      if (tree.getNextElement("AdminEncryption", firstFlags) != null) {
         if (tree.getNextElement(PSDataEncryptor.ms_NodeType, firstFlags) != null) {
            m_adminEncryptor = new PSDataEncryptor(false);   // off by default
            m_adminEncryptor.fromXml((Element)tree.getCurrent(), this, null);
         }
      }

      tree.setCurrent(cur);

      // get the performance settings from the Performance XML node
      if (tree.getNextElement("Performance", firstFlags) != null) {
         //create maxThreadsPerApp element
         sTemp = tree.getElementData("maxThreadsPerApp", false);
         if (sTemp == null)
            m_maxThreadsPerApp = 0;
         else {
            try {
               m_maxThreadsPerApp = Integer.parseInt(sTemp);
            } catch (NumberFormatException e) {
               Object[] args = { ms_NodeType, "maxThreadsPerApp", sTemp };
               throw new PSUnknownDocTypeException(
                  IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
            }
         }

         //create minThreadsOnServer element
         sTemp = tree.getElementData("minThreadsOnServer", false);
         if(sTemp == null)
            m_minThreadsOnServer = 0;
         else {
            try {
               m_minThreadsOnServer = Integer.parseInt(sTemp);
            } catch (NumberFormatException e) {
               Object[] args = { ms_NodeType, "minThreadsOnServer", sTemp };
               throw new PSUnknownDocTypeException(
                  IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
            }
         }

         //create maxThreadsOnServer element
         sTemp = tree.getElementData("maxThreadsOnServer", false);
         if (sTemp == null)
            m_maxThreadsOnServer = 0;
         else {
            try {
               m_maxThreadsOnServer = Integer.parseInt(sTemp);
            } catch (NumberFormatException e) {
               Object[] args = { ms_NodeType, "maxThreadsOnServer", sTemp };
               throw new PSUnknownDocTypeException(
                  IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
            }
         }

         //create idleThreadTimeout element
         sTemp = tree.getElementData("idleThreadTimeout", false);
         if (sTemp == null)
            m_idleThreadTimeout = 0;
         else {
            try {
               m_idleThreadTimeout = Integer.parseInt(sTemp);
            } catch (NumberFormatException e) {
               Object[] args = { ms_NodeType, "idleThreadTimeout", sTemp };
               throw new PSUnknownDocTypeException(
                  IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
            }
         }

         //create maxRequestsInQueuePerApp element
         sTemp = tree.getElementData("maxRequestsInQueuePerApp", false);
         if (sTemp == null)
            m_maxRequestsInQueuePerApp = 0;
         else {
            try {
               m_maxRequestsInQueuePerApp = Integer.parseInt(sTemp);
            } catch (NumberFormatException e) {
               Object[] args = { ms_NodeType, "maxRequestsInQueuePerApp", sTemp };
               throw new PSUnknownDocTypeException(
                  IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
            }
         }

         //create maxRequestsInQueueOnServer element
         sTemp = tree.getElementData("maxRequestsInQueueOnServer", false);
         if (sTemp == null)
            m_maxRequestsInQueueOnServer = 0;
         else {
            try {
               m_maxRequestsInQueueOnServer = Integer.parseInt(sTemp);
            } catch (NumberFormatException e) {
               Object[] args = { ms_NodeType, "maxRequestsInQueueOnServer", sTemp };
               throw new PSUnknownDocTypeException(
                  IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
            }
         }

         //create maxRequestTime element
         sTemp = tree.getElementData("maxRequestTime", false);
         if (sTemp == null)
            m_maxRequestTime = 0;
         else {
            try {
               m_maxRequestTime = Integer.parseInt(sTemp);
            } catch (NumberFormatException e) {
               Object[] args = { ms_NodeType, "maxRequestTime", sTemp };
               throw new PSUnknownDocTypeException(
                  IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
            }
         }

         //create userSessionEnabled element
         sTemp = tree.getElementData("userSessionEnabled", false);
         if (sTemp != null)
            m_userSessions = sTemp.equalsIgnoreCase("yes");

         //create user sessions time out
         sTemp = tree.getElementData("userSessionTimeout", false);
         if (sTemp == null)
            m_sessionTimeout = 0;
         else {
            try {
               m_sessionTimeout = Integer.parseInt(sTemp);
            } catch (NumberFormatException e) {
               Object[] args = { ms_NodeType, "userSessionTimeout", sTemp };
               throw new PSUnknownDocTypeException(
                  IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
            }
         }

         // create maximum open user sessions
         sTemp = tree.getElementData("maxOpenUserSessions", false);
         if (sTemp == null)
            m_maxOpenSessions = DEFAULT_OPEN_SESSIONS;
         else
         {
            try
            {
               setMaxOpenUserSessions(Integer.parseInt(sTemp));
            }
            catch (NumberFormatException e)
            {
               Object[] args = { ms_NodeType, "maxOpenUserSessions", sTemp };
               throw new PSUnknownDocTypeException(
                  IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
            }
         }
      }

      tree.setCurrent(cur);

      //get PSLogger object form XML
      m_logger = null;
      if (tree.getNextElement(PSLogger.ms_NodeType, firstFlags) != null) {
         m_logger = new PSLogger();
         m_logger.fromXml((Element)tree.getCurrent(), this, null);
      }

      tree.setCurrent(cur);

      //get running log days from XML
      // default is to truncate the log after 2 days
      m_runningLogDays = 2;
      if (tree.getNextElement("LogTruncation", firstFlags) != null) {
         sTemp = tree.getElementData("runningLogDays", false);
         try {
            if (sTemp != null)
               m_runningLogDays = Integer.parseInt(sTemp);
         } catch (NumberFormatException e) {
            Object[] args = { ms_NodeType, "runningLogDays", sTemp };
            throw new PSUnknownDocTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
         }
      }

      tree.setCurrent(cur);

      //create PSErrorWebPages from XML
      m_errorWebPages = null;
      if (tree.getNextElement(PSErrorWebPages.ms_NodeType, firstFlags) != null) {
         m_errorWebPages = new PSErrorWebPages(false);
         m_errorWebPages.fromXml((Element)tree.getCurrent(), this, null);
      }

      //restore JDBC driver configs
      tree.setCurrent(cur);
      m_jdbcDriverConfigs.clear();
      if (tree.getNextElement(XML_JDBC_DRIVER_CONFIGS_ELEM, firstFlags) != null)
      {
         Element configEl = tree.getNextElement(
            PSJdbcDriverConfig.XML_NODE_NAME, firstFlags); 
         while (configEl != null)
         {
            m_jdbcDriverConfigs.add(new PSJdbcDriverConfig(configEl));
            configEl = tree.getNextElement(PSJdbcDriverConfig.XML_NODE_NAME, 
               nextFlags);
         }
      }
      
      m_beLoginTimeoutSeconds = DEFAULT_LOGIN_TIMEOUT;

      tree.setCurrent(cur);

      Node saveNode; // save the node we're on to get back up the tree

      m_backEndConnections.clear();
      m_backEndCredentials.clear();

      if (tree.getNextElement("BackEndConnections", firstFlags) != null) {
         saveNode = tree.getCurrent();
         if (tree.getNextElement(PSLegacyBackEndConnection.ms_NodeType, true, true) != null) {
            PSLegacyBackEndConnection conn;
            // do the connection pools
            do{
               conn = new PSLegacyBackEndConnection();
               conn.fromXml((Element)tree.getCurrent(), this, null);
               m_backEndConnections.add(conn);
            } while (tree.getNextElement(PSLegacyBackEndConnection.ms_NodeType, true, true) != null);
         }  // end of if PSXBackEndConnection

         tree.setCurrent(saveNode);

         if (tree.getNextElement(PSLegacyBackEndCredential.ms_NodeType, true, true) != null) {
            do{
               PSLegacyBackEndCredential cred = new PSLegacyBackEndCredential();
               cred.fromXml((Element)tree.getCurrent(), this, null);
               m_backEndCredentials.add(cred);
            } while (tree.getNextElement(PSLegacyBackEndCredential.ms_NodeType, true, true) != null);
         }  // end of if PSXBackEndCredential

         tree.setCurrent(saveNode);

         // and finally the login timeout
         sTemp = tree.getElementData("backEndLoginTimeout", false);
         try {
            if ((sTemp != null) && (sTemp.length() != 0))
               m_beLoginTimeoutSeconds = Integer.parseInt(sTemp);
         } catch (Exception e) {
            Object[] args = { ms_NodeType, "backEndLoginTimeout",
               ((sTemp == null) ? "" : sTemp) };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
         }
      }

      tree.setCurrent(cur);

      m_securityProviders.clear();
      m_groupProviders.clear();
      Element securityProviders = tree.getNextElement("SecurityProviders",
         firstFlags);
      if (securityProviders != null)
      {
         String curNodeType = PSLegacySecurityProviderInstance.ms_NodeType;
         if (tree.getNextElement(curNodeType, firstFlags) != null)
         {
            PSLegacySecurityProviderInstance spInst = null;
            do
            {
               spInst = new PSLegacySecurityProviderInstance();
               spInst.fromXml((Element)tree.getCurrent(), this, null);
               /* Certain platforms don't support certain providers. If a bad
                  entry gets in the config, don't propagate it. */
               if ( PSSecurityProvider.isSupportedType( spInst.getType()))
                  m_securityProviders.add(spInst);
            } while(tree.getNextElement(curNodeType, nextFlags) != null);

            // now load any group providers
            tree.setCurrent(securityProviders);
            Element groupProviders = tree.getNextElement(
               XML_GROUP_PROVIDERS_ELEMENT, firstFlags);
            if (groupProviders != null)
            {
               curNodeType = PSGroupProviderInstance.XML_NODE_NAME;
               Element groupProvider = tree.getNextElement(curNodeType,
                  firstFlags);
               if (groupProvider == null)
               {
                  Object[] args = {XML_GROUP_PROVIDERS_ELEMENT, curNodeType,
                     "null" };
                  throw new PSUnknownNodeTypeException(
                        IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
               }
               while (groupProvider != null)
               {
                  m_groupProviders.add(PSGroupProviderInstance.newInstance(
                     groupProvider));

                  groupProvider = tree.getNextElement(curNodeType, nextFlags);
               }
            }


         }
      }    // end of if SecurityProviders

      tree.setCurrent(cur);

      Element cacheSettings = tree.getNextElement(
         PSServerCacheSettings.XML_NODE_NAME, firstFlags);
      if (cacheSettings != null) {
         m_serverCacheSettings = new PSServerCacheSettings(
            cacheSettings, this, null);
      } else {
         m_serverCacheSettings = new PSServerCacheSettings();
      }

      tree.setCurrent(cur);

      // Get sandbox security disable flag, if it exists
      sTemp = tree.getElementData("DisableSandboxSecurity", false);
      if (sTemp != null) {
         m_useSandboxSecurity = !(sTemp.equalsIgnoreCase("y"));
      } else {
         m_useSandboxSecurity = true;
      }

      //create PSNotifier elements
      if (tree.getNextElement(PSNotifier.ms_NodeType, firstFlags) != null) {
         if (null == m_notifier)
            m_notifier = new PSNotifier();

         m_notifier.fromXml((Element)tree.getCurrent(), this, null);
      }

      // If the BrowserUISettings aren't specified in the tree, the
      // init method will use default values.
      m_browserUISettings = initBrowserUISettings(root);

      tree.setCurrent(root);

      //Add default PSJavaPluginConfig element if PSXJavaPluginConfig element
      //does not exist.
      Element pluginConfig = tree.getNextElement(
         PSJavaPluginConfig.XML_NODE_NAME, true);
      if(pluginConfig!=null)
         m_javaPluginConfig = new PSJavaPluginConfig(pluginConfig);
      else
         m_javaPluginConfig = new PSJavaPluginConfig();
      
      tree.setCurrent(root);
      Element searchConfig = tree.getNextElement(
         PSSearchConfig.XML_NODE_NAME, true);
      if (searchConfig != null)
      {
         m_searchConfig = new PSSearchConfig(searchConfig, this, null);
      }
      else
      {
         m_searchConfig = new PSSearchConfig();
      }

      // get authentications
      tree.setCurrent(root);
      m_authentications.clear();
      Element authentications = tree.getNextElement(XML_AUTHENTICATIONS_ELEM,
         firstFlags);
      if (authentications != null)
      {
         Element authentication = tree.getNextElement(
            PSAuthentication.XML_NODE_NAME, firstFlags);
         while (authentication != null)
         {
            m_authentications.add(new PSAuthentication(authentication,
               this, null));

            authentication = tree.getNextElement(
               PSAuthentication.XML_NODE_NAME, nextFlags);
         }
      }

      // get directory services
      tree.setCurrent(root);
      m_directories.clear();
      m_directorySets.clear();
      Element directoryServices = tree.getNextElement(
         XML_DIRECTORY_SERVICES_ELEM, firstFlags);
      if (directoryServices != null)
      {
         Element directories = tree.getNextElement(XML_DIRECTORIES_ELEM,
            firstFlags);
         if (directories != null)
         {
            Element directory = tree.getNextElement(
               PSDirectory.XML_NODE_NAME, firstFlags);
            while (directory != null)
            {
               m_directories.add(new PSDirectory(directory, this, null));

               directory = tree.getNextElement(
                  PSDirectory.XML_NODE_NAME, nextFlags);
            }
         }

         tree.setCurrent(directoryServices);
         Element directorySets = tree.getNextElement(
            XML_DIRECTORY_SETS_ELEM, firstFlags);
         if (directorySets != null)
         {
            Element directorySet = tree.getNextElement(
               PSDirectorySet.XML_NODE_NAME, firstFlags);
            while (directorySet != null)
            {
               m_directorySets.add(new PSDirectorySet(directorySet));

               directorySet = tree.getNextElement(
                  PSDirectorySet.XML_NODE_NAME, nextFlags);
            }
         }
      }

      // get role services
      tree.setCurrent(root);
      m_roleProviders.clear();
      Element roleProviders = tree.getNextElement(
         XML_ROLE_SERVICES_ELEM, firstFlags);
      if (roleProviders != null)
      {
         Element roleProvider = tree.getNextElement(
            PSRoleProvider.XML_NODE_NAME, firstFlags);
         while (roleProvider != null)
         {
            m_roleProviders.add(new PSRoleProvider(roleProvider, this, null));

            roleProvider = tree.getNextElement(
               PSRoleProvider.XML_NODE_NAME, nextFlags);
         }
      }
   }

   /**
    * Get the server's default data encryption settings. Through this object,
    * E2 can force users to make requests through SSL. It can even be used to
    * enforce the key strength is appropriate for the given server. This
    * allows the server's data to be sent over secure channels. Incoming
    * requests from users, however, can still be sent in the clear. For this
    * reason, care must be taken when designing web pages so that forms
    * containing sensitive data, including user ids and passwords, are
    * submitted using HTTPS, not HTTP.
    *
    * @return      the server's data encrytion settings or <code>null</code> if
    * one has not been previously defined
    */
   public PSDataEncryptor getDefaultDataEncryptor()
   {
      return m_dataEncryptor;
   }

   /**
    * Overwrite the server's default data encryption object with the
    * specified data encryption object. If you only want to modify some data
    * encryption settings, use getDataEncryptor to get the existing object
    * and modify the returned object directly.
    * <p>
    * The PSDataEncryptor object supplied to this method will be stored with
    * the PSServerConfiguration object. Any subsequent changes made to the object by
    * the caller will also effect the server.
    *
    * @param   encryptor   the new data encryptor for the server or
    * <code>null</code> to disable this functionality
    *
    * @see                  PSDataEncryptor
    */
   public void setDataEncryptor(PSDataEncryptor encryptor)
   {
      m_dataEncryptor = encryptor;
      setModified(true);
   }

   /**
    * Get the server's admin encryption settings. Through this object,
    * E2 can force users to make administration requests through SSL.
    * It can even be used to enforce the key strength is appropriate
    * for the given server. This
    * allows admin responses to be sent over secure channels. Incoming
    * requests from users, however, can still be sent in the clear. For this
    * reason, care must be taken when designing web pages so that forms
    * containing sensitive data, including user ids and passwords, are
    * submitted using HTTPS, not HTTP.
    *
    * @return      the server's admin encrytion settings or <code>null</code> if
    * one has not been previously defined
    */
   public PSDataEncryptor getAdminEncryptor()
   {
      return m_adminEncryptor;
   }

   /**
    * Overwrite the server's admin encryption object with the
    * specified object. If you only want to modify some
    * encryption settings, use getAdminEncryptor to get the existing object
    * and modify the returned object directly.
    * <p>
    * The PSDataEncryptor object supplied to this method will be stored with
    * the PSServerConfiguration object. Any subsequent changes made to the
    * object by the caller will also effect the server.
    *
    * @param   encryptor   the new data encryptor for the server or
    * <code>null</code> to disable this functionality
    *
    * @see                  #getAdminEncryptor
    * @see                  PSDataEncryptor
    */
   public void setAdminEncryptor(PSDataEncryptor encryptor)
   {
      m_adminEncryptor = encryptor;
      setModified(true);
   }

   /**
    * Get the maximum number of threads any given application may
    * consume. The server maintains a pool of threads, which can grow as
    * activity increases. The number of threads can be limited to avoid the
    * resource problems that arise from excessive thread use.
    * <P>
    * This may not be an exact number as the total number of
    * threads used by the server can be limited. For instance, if the
    * application limit is set to 50 and the server limit is set to 200,
    * ten applications cannot each use 50 threads. That would bring the
    * threads in use to 500, which is well beyond the server limit of 200.
    * <P>
    * The thread limit can also be set on an application, to allow less
    * critical applications from consuming resources. An application
    * cannot exceed the server defined limit.
    *
    * @return         the maximum number of threads an application is
    * permitted to use
    *
    * @see            #getMaxThreadsOnServer
    */
   public int getMaxThreadsPerApp()
   {
      return m_maxThreadsPerApp;
   }

   /**
    * Set the maximum number of threads any given application may
    * consume. The server maintains a pool of threads, which can grow as
    * activity increases. The number of threads can be limited to avoid the
    * resource problems that arise from excessive thread use.
    * <P>
    * This may not be an exact number as the total number of
    * threads used by the server can be limited. For instance, if the
    * application limit is set to 50 and the server limit is set to 200,
    * ten applications cannot each use 50 threads. That would bring the
    * threads in use to 500, which is well beyond the server limit of 200.
    * <P>
    * The thread limit can also be set on an application, to allow less
    * critical applications from consuming resources.
    *
    * @param   max   the maximum number of threads an application is
    * permitted to use
    *
    * @see            #setMaxThreadsOnServer
    */
   public void setMaxThreadsPerApp(int max)
   {
      m_maxThreadsPerApp = max;
      setModified(true);
   }

   /**
    * Get the maximum number of threads the server may assign to applications
    * for servicing user requests. The server maintains a pool of threads,
    * which can grow as activity increases. The number of threads can be
    * limited to avoid the resource problems that arise from excessive
    * thread use.
    *
    * @return      the maximum number of threads the server may use for
    * servicing requests
    */
   public int getMaxThreadsOnServer()
   {
      return m_maxThreadsOnServer;
   }

   /**
    * Set the maximum number of threads the server may assign to applications
    * for servicing user requests. The server maintains a pool of threads,
    * which can grow as activity increases. The number of threads can be
    * limited to avoid the resource problems that arise from excessive
    * thread use.
    *
    * @param   max   set the maximum number of threads the server may use for
    * servicing requests
    */
   public void setMaxThreadsOnServer(int max)
   {
      m_maxThreadsOnServer = max;
      setModified(true);
   }

   /**
    * Get the minimum number of threads the server should make available
    * for servicing user requests. The server maintains a pool of threads,
    * which can grow and shrink as activity increases or decreases.
    * The minimum number of threads can be set to speed processing for the
    * expected request load.
    *
    * @return the minimum number of threads the server should make
    * available for servicing requests
    *
    * @see            #setIdleThreadTimeout
    */
   public int getMinThreadsOnServer()
   {
      return m_minThreadsOnServer;
   }

   /**
    * Set the minimum number of threads the server should make available
    * for servicing user requests. The server maintains a pool of threads,
    * which can grow and shrink as activity increases or decreases.
    * The minimum number of threads can be set to speed processing for the
    * expected request load.
    *
    * @param   min   the minimum number of threads the server should make
    * available for servicing requests
    *
    * @see            #setIdleThreadTimeout
    */
   public void setMinThreadsOnServer(int min)
   {
      m_minThreadsOnServer = min;
      setModified(true);
   }

   /**
    * Get the amount of idle time, in minutes, that will cause a thread
    * to be terminated. Even if a thread stays idle beyond the idle time
    * limit, it will not be terminated if that would cause the number of
    * available threads to fall below the minimum number of threads required.
    *
    * @return            the amount of idle time, in seconds, which will
    * cause an idle thread to terminate
    *
    * @see               #getMinThreadsOnServer
    */
   public int getIdleThreadTimeout()
   {
      return m_idleThreadTimeout;
   }

   /**
    * Set the amount of idle time, in minutes, that will cause a thread
    * to be terminated. Even if a thread stays idle beyond the idle time
    * limit, it will not be terminated if that would cause the number of
    * available threads to fall below the minimum number of threads required.
    *
    * @param   seconds   the amount of idle time, in seconds, which will
    * cause an idle thread to terminate
    *
    * @see               #getMinThreadsOnServer
    */
   public void setIdleThreadTimeout(int seconds)
   {
      m_idleThreadTimeout = seconds;
   }

   /**
    * Get the maximum number of requests which may be queued for processing
    * by an application. When this limit is exceeded, the user is notified
    * that the server is too busy (HTTP status code 503).
    * <P>
    * This may not be an exact number as the total number of requests in the
    * queue can be limited by the server. For instance, if the
    * application limit is set to 50 and the server limit is set to 200,
    * ten applications cannot each queue 50 requests. That would bring the
    * number of requests queued to 500, which is well beyond the server
    * limit of 200.
    * <P>
    * The request queue limit can also be set on an application, to allow
    * less critical applications from consuming resources. An application
    * cannot exceed the server defined limit.
    *
    * @return         the maximum number of requests to queue
    *
    * @see            #getMaxRequestsInQueueOnServer
    */
   public int getMaxRequestsInQueuePerApp()
   {
      return m_maxRequestsInQueuePerApp;
   }

   /**
    * Set the maximum number of requests which may be queued for processing
    * by an application. When this limit is exceeded, the user is notified
    * that the server is too busy (HTTP status code 503).
    * <P>
    * This may not be an exact number as the total number of requests in the
    * queue can be limited by the server. For instance, if the
    * application limit is set to 50 and the server limit is set to 200,
    * ten applications cannot each queue 50 requests. That would bring the
    * number of requests queued to 500, which is well beyond the server
    * limit of 200.
    * <P>
    * The request queue limit can also be set on an application, to allow
    * less critical applications from consuming resources.
    *
    * @param   max   the maximum number of requests which can be queued.
    * Use 0 to prevent queueing. Use -1 for unlimited
    * queueing.
    *
    * @see            #setMaxRequestsInQueueOnServer
    */
   public void setMaxRequestsInQueuePerApp(int max)
   {
      m_maxRequestsInQueuePerApp = max;
      setModified(true);
   }

   /**
    * Get the maximum number of requests which may be queued for processing
    * by the server. When this limit is exceeded, the user is notified
    * that the server is too busy (HTTP status code 503).
    *
    * @return        the maximum number of requests to queue
    */
   public int getMaxRequestsInQueueOnServer()
   {
      return m_maxRequestsInQueueOnServer;
   }

   /**
    * Set the maximum number of requests which may be queued for processing
    * by the server. When this limit is exceeded, the user is notified
    * that the server is too busy (HTTP status code 503).
    *
    * @param   max   the maximum number of requests which can be queued.
    * Use 0 to prevent queueing. Use -1 for unlimited queueing.
    */
   public void setMaxRequestsInQueueOnServer(int max)
   {
      m_maxRequestsInQueueOnServer = max;
      setModified(true);
   }

   /**
    * Get the maximum amount of time to spend servicing a request.
    * <P>
    * The request time limit can also be set on an application basis.
    * Unlike threads and the request queue, the application may exceed
    * the value specified on the server.
    *
    * @return         the maximum amount of time to spend servicing a request,
    * in seconds
    *
    */
   public int getMaxRequestTime()
   {
      return m_maxRequestTime;
   }

   /**
    * Set the maximum amount of time to spend servicing a request.
    * <P>
    * The request time limit can also be set on an application basis.
    * Unlike threads and the request queue, the application may exceed
    * the value specified on the server.
    *
    * @param   max   the maximum amount of time to spend servicing a request,
    * in seconds
    *
    */
   public void setMaxRequestTime(int max)
   {
      m_maxRequestTime = max;
      setModified(true);
   }
   
   /**
    * Returns a list of legacy backend credential objects
    * 
    * @return A list of zero or backend credentials, never <code>null</code>.
    */
   public List<PSLegacyBackEndCredential> getBackEndCredentials()
   {
      return m_backEndCredentials;
   }

   /**
    * Returns a list of legacy backend connections.
    * 
    * @return A list of zero or more backend connections, 
    * never <code>null</code>.
    */
   public List<PSLegacyBackEndConnection> getBackEndConnections()
   {
      return m_backEndConnections;
   }

   /**
    * List of backend connections instantiated during construction, never 
    * <code>null</code> or modified after that.
    */
   private List<PSLegacyBackEndConnection> m_backEndConnections;
   
   /**
    * List of backend credentials instantiated during construction, never 
    * <code>null</code> or modified after that.
    */
   private List<PSLegacyBackEndCredential> m_backEndCredentials;
   
   private   int                     m_beLoginTimeoutSeconds         = 60;
   private   PSDataEncryptor         m_dataEncryptor = null;
   private   PSDataEncryptor         m_adminEncryptor = null;
   private   int                     m_maxThreadsPerApp            = 0;
   private   int                     m_maxThreadsOnServer            = 0;
   private   int                     m_minThreadsOnServer            = 0;
   private   int                     m_idleThreadTimeout            = 0;

   private   int                     m_maxRequestsInQueuePerApp      = 0;
   private   int                     m_maxRequestsInQueueOnServer   = 0;

   private   int                     m_maxRequestTime               = 0;

}
