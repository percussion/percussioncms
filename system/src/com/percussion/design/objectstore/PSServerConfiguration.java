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

package com.percussion.design.objectstore;

import com.percussion.error.PSIllegalArgumentException;
import com.percussion.security.PSSecurityProvider;
import com.percussion.security.TLSSocketFactory;
import com.percussion.server.PSUserSessionManager;
import com.percussion.util.PSCollection;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The PSServerConfiguration class is used to manipulate an E2 server's
 * configuration. Use the PSObjectStore class to load a PSServerConfiguration
 * object from an E2 server (getServerConfiguration). The returned object
 * can be read and/or written (assuming you have the apporpriate access).
 * Once you're ready to send the local changes back to the server, call the
 * PSObjectStore's saveServerConfiguration method. See the PSObjectStore
 * class for more details.
 *
 * @see         PSObjectStore
 *
 * @author      Tas Giakouminakis
 * @version      1.0
 * @since      1.0
 */
@SuppressWarnings(value={"unchecked"})
public class PSServerConfiguration implements IPSDocument
{
   /**
    * Construct a Java object from its XML representation. See the
    * {@link #toXml() toXml} method for a description of the XML object.
    *
    * @param      sourceDoc      the XML document to construct this
    * object from
    *
    * @throws   PSUnknownDocTypeException if the XML document is not of the
    * appropriate type
    *
    * @throws   PSUnknownNodeTypeException if an XML element node is not of the
    * appropriate type
    */
   public PSServerConfiguration(Document sourceDoc)
      throws PSUnknownDocTypeException, PSUnknownNodeTypeException
   {
      this();
      fromXml(sourceDoc);
   }

   /**
    * Construct an empty server configuration object.
    */
   public PSServerConfiguration()
   {
      m_securityProviders = new PSCollection(PSSecurityProviderInstance.class);
      m_serverCacheSettings = new PSServerCacheSettings();
   }

   /**
    * Get the number of days the log has been running.
    *
    * @return      the number of days
    */
   public int getRunningLogDays()
   {
      return m_runningLogDays;
   }

   /**
    * Set the number of days the log has been running.
    * 
    * @param days The number of days.  Any value < 0 is treated as 0.
    */
   public void setRunningLogDays(int days)
   {
      if (days <= 0)
         days = 0;

      m_runningLogDays = days;
      setModified(true);
   }

   /**
    * Get the id assigned to this server configuration.
    *
    * @return        the server configuration id
    */
   public int getId()
   {
      return m_id;
   }

   /**
    * Returns the server type.
    * 
    * @return one of the values in SERVER_TYPE_XXX.
    */
   public int getServerType()
   {
      return m_serverType;
   }
   
   /**
    * The Rx server attempts to authenticate a user against all security provider
    * instances until one is successful or all fail. Normally, only info about
    * the last attempted provider is returned. If this flag is <code>true</code>,
    * then info about all attempted providers is returned. Because this would
    * provide more info for a hacker, this flag is present to allow an admin to
    * limit the return of this information.<p/>
    * The default value for this flag is <code>true</code>.
    *
    * @return <code>true</code> if error messages from all security providers
    * can be returned to the user, <code>false</code> means only a generic
    * 'authentication failed' msg is returned.
    *
    * Note: We may want to disallow any provider info if this flag is <code>
    * false</code>.
    */
   public boolean allowDetailedAuthenticationMessages()
   {
      return m_allowAuthDetails;
   }

   /**
    * See {@link #allowDetailedAuthenticationMessages()
    * allowDetailedAuthenticationMessages()} for details.
    *
    * @param allow <code>true</code> allows info about all failed providers to
    * be returned, <code>false</code> limits the info to a generic authentication
    * failed msg.
    */
   public void setAllowDetailedAuthenticationMessages( boolean allow )
   {
      m_allowAuthDetails = allow;
   }

   /**
    * Get the server's request root. This is combined with the application's
    * request root to determine if an incoming request should be serviced.
    * For instance, if the server root is <code>/E2</code> and the
    * application root is <code>/MyApp</code>, only URLs whose path begins
    * with <code>/E2/MyApp</code> will be considered for processing. This
    * helps alleviate strain from the web server as E2 can more quickly
    * determine what is an E2 request rather than a standard web server
    * request.
    *
    * @return      the server's request root
    */
   public String getRequestRoot()
   {
      return m_requestRoot;
   }

   /**
    * Set the server's request root. This is combined with the application's
    * request root to determine if an incoming request should be serviced.
    * For instance, if the server root is <code>/E2</code> and the
    * application root is <code>/MyApp</code>, only URLs whose path begins
    * with <code>/E2/MyApp</code> will be considered for processing. This
    * helps alleviate strain from the web server as E2 can more quickly
    * determine what is an E2 request rather than a standard web server
    * request.
    *
    * @param   requestRoot         the new server configuration request root.
    *                                                                                                                    This is limited to 50 characters.
    *
    * @throws   PSIllegalArgumentException      if requestRoot exceeds the
    *                                                                                                                                   specified size limit
    */
   public void setRequestRoot(String requestRoot)
      throws PSIllegalArgumentException
   {
      requestRoot = requestRoot.trim();
      int length = requestRoot.length();
      if (length > MAX_REQ_ROOT_LEN) {
         Object[] args = { new Integer(MAX_REQ_ROOT_LEN),
            new Integer(length) };
         throw new PSIllegalArgumentException(
            IPSObjectStoreErrors.SRV_ROOT_TOO_BIG, args);
      }

      m_requestRoot = requestRoot;
      setModified(true);
   }

   /**
    * Get the server's access control list (ACL). This contains the
    * names of users, groups and roles and the type of access they have for
    * this server.
    *
    * @return      the server's ACL
    */
   public PSAcl getAcl()
   {
      return m_acl;
   }

   /**
    * Overwrite the server's ACL with the specified ACL. If you only
    * want to modify a limited number of entries, add an entry, etc. use
    * getAcl to get the existing ACL and modify the returned object directly.
    * <p>
    * The PSAcl object supplied to this method will be stored with the
    * PSServerConfiguration object. Any subsequent changes made to the object by
    * the caller will also effect the server.
    *
    * @param      acl                        the new ACL for the server
    *
    * @throws   PSIllegalArgumentException   if acl is invalid. This occurs
    * when the ACL is null, empty or contains no entries with admin access to
    * the server.
    *
    * @see         PSAcl
    */   
   public void setAcl(PSAcl acl) throws PSIllegalArgumentException
   {
      if (null == acl)
         throw new PSIllegalArgumentException(
         IPSObjectStoreErrors.SRV_ACL_NULL);

      PSCollection entries = acl.getEntries();
      int size = entries.size();
      if (0 == size)
         throw new PSIllegalArgumentException(
         IPSObjectStoreErrors.SRV_ACL_EMPTY);

      int adminAccess =   PSAclEntry.SACE_ADMINISTER_SERVER;

      ConcurrentHashMap names = new ConcurrentHashMap();
      boolean hasAdminAccess = false;
      PSAclEntry ace;
      String key;
      int level;
      for (int i = 0; i < size; i++) {
         ace = (PSAclEntry)entries.get(i);

         key = ace.getName();

         if (names.put(key, ace) != null)   /* we've seen this already */
            throw new PSIllegalArgumentException(
            IPSObjectStoreErrors.ACL_ENTRYLIST_DUPLICATE, ace.getName());

         level = ace.getAccessLevel();
         if ((level & adminAccess) == adminAccess)
            hasAdminAccess = true;
      }

      if (!hasAdminAccess)
         throw new PSIllegalArgumentException(
         IPSObjectStoreErrors.SRV_ACL_NO_ADMIN);

      m_acl = acl;
      setModified(true);
   }

   /**
    * Get the java plugin configuration.
    *  
    * @return The config, may be <code>null</code>.
    */
   public PSJavaPluginConfig getJavaPluginConfig()
   {
      return m_javaPluginConfig;
   }

   /**
    * Set the plugin configuration
    *
    * @param pluginConfig The config to use, may be <code>null</code>.
    */
   public void setJavaPluginConfig(PSJavaPluginConfig pluginConfig)
   {
     m_javaPluginConfig = pluginConfig;
     setModified(true);
   }
   
   /**
    * Get the search engine configuration object.
    * 
    * @return Never <code>null</code>.
    */
   public PSSearchConfig getSearchConfig()
   {
      return m_searchConfig;
   }

   /**
    * Set a new search engine configuration object
    * @param config
    */
   public void setSearchConfig(PSSearchConfig config)
   {
      m_searchConfig = config;
   }
   

   /**
    * Is user session management permitted on this server?
    *
    * @return         <code>true</code> if sessions are permitted,
    * <code>false</code> otherwise
    */
   public boolean isUserSessionEnabled()
   {
      return m_userSessions;
   }

   /**
    * Enable or disable user session management on the server. When a
    * user makes a connection to the E2 server, a session can be established
    * to maintain state information across requests.
    * <p>
    * If this is disabled, applications cannot enable user session
    * management. They can, however, disable for applications if it is
    * enabled by default for the server.
    *
    * @param   enable   <code>true</code> to enable user session management,
    * <code>false</code> to disable it
    *
    * @see               PSApplication#setUserSessionEnabled
    */
   public void setUserSessionEnabled(boolean enable)
   {
      m_userSessions = enable;
      setModified(true);
   }

   /**
    * Get the default timeout interval for user sessions. When a user
    * makes a connection to the E2 server, a session can be established
    * to maintain state information across requests. When sessions are
    * maintained, they must be terminated after a period of inactivity.
    * This setting determines how long to wait before removing the
    * inactive session.
    * <p>
    * Applications can override this setting.
    *
    * @return         the timeout interval for user sessions, in seconds
    *
    * @see            PSApplication#getUserSessionTimeout
    */
   public int getUserSessionTimeout()
   {
      return m_sessionTimeout;
   }
   
   /**
    * Get the default timeout warning interval for user sessions.
    * 
    * This is initially only used by the web client.
    * 
    * 
    * <p>
    * Applications can override this setting.
    *
    * @return         the time in seconds before timeout to warn,
    *
    * @see            PSApplication#getUserSessionWarning
    */
   public int getUserSessionWarning()
   {
      return m_sessionWarning;
   }
   
   /**
    * Set the default timeout interval warning for user sessions. When a user
    * makes a connection to the E2 server, a session can be established
    * to maintain state information across requests. 
    * <P>
    * Applications can override this setting.
    *
    * @param   timeout      the timeout interval for user sessions, in seconds
    *
    * @see      PSApplication#setUserSessionTimeout
    */
   public void setUserSessionTimeout(int timeout)
   {
      m_sessionTimeout = timeout;
      setModified(true);
   }

   /**
    * Set the default timeout interval warning for user sessions. When a user
    * makes a connection to the E2 server, a session can be established
    * to maintain state information across requests. 
    * <P>
    * Applications can override this setting.
    *
    * @param   timeout      the timeout interval for user sessions, in seconds
    *
    * @see      PSApplication#setUserSessionTimeout
    */
   public void setUserSessionWarning(int warning)
   {
      m_sessionWarning = warning;
      setModified(true);
   }
   
   /**
    * Get the maximum number of user sessions cached by the server. If this
    * number is exceeded, the oldest open sessions are reaped starting with
    * anonymous sessions first. The reaping process will clean out one 4th of
    * the maximum number of open sessions allowed.
    *
    * @return the maximum number of open sessions allowed. Always greater
    *    than MINIMAL_REQUIRED_OPEN_SESSIONS.
    */
   public int getMaxOpenUserSessions()
   {
      return m_maxOpenSessions;
   }

   /**
    * Set the maximum number of open user sessions allowed.
    *
    * @param max the maximum number of open user sessions allowed, must be
    *    greater than MINIMAL_REQUIRED_OPEN_SESSIONS. If an invalid number
    *    is provided, the default will be used.
    */
   public void setMaxOpenUserSessions(int max)
   {
      if (max < MINIMAL_REQUIRED_OPEN_SESSIONS)
         max = DEFAULT_OPEN_SESSIONS;

      m_maxOpenSessions = max;
      setModified(true);
   }

   /**
    * Get the log settings for this server. Various levels of logging
    * can be defined, which may be used for a variety of tasks ranging from
    * usage tracking to application debugging.
    * <P>
    * Applications may choose to disable options defined in the server's
    * log settings. They cannot, however, enable options which the server
    * does not permit.
    *
    * @return      the PSLogger object defining the log settings (may be null)
    *
    * @see         PSApplication#getLogger
    */
   public PSLogger getLogger()
   {
      return m_logger;
   }

   /**
    * Overwrite the log settings associated with this server configuration with the
    * specified object. If you only want to modify certain settings, use
    * getLogger to get the existing object and modify the returned object
    * directly. Be sure to check that getLogger did not return null, which
    * signifies log settings have never been created.
    * <p>
    * The PSLogger object supplied to this method will be stored with the
    * PSServerConfiguration object. Any subsequent changes made to the object
    * by the caller will also effect the server configuration.
    * <P>
    * Applications may choose to disable options defined in the server's
    * log settings. They cannot, however, enable options which the server
    * does not permit.
    *
    * @param   logger   the new log settings or <code>null</code> to
    * prevent logging on the server
    *
    * @see         #getLogger
    * @see         PSLogger
    * @see         PSApplication#getLogger
    */
   public void setLogger(PSLogger logger)
   {
      m_logger = logger;
      setModified(true);
   }

  
   /**
    * Get the security provider instance definitions which will be used
    * for user authentication. When a user logs into E2, their credentials
    * will be checked against the specified security provider(s).
    *
    * @return      a collection containing the security provider instance
    * definitions used for authentication (may be null)
    * (PSSecurityProviderInstance objects)
    *
    * @see         PSSecurityProviderInstance
    */
   public PSCollection getSecurityProviderInstances()
   {
      return m_securityProviders;
   }

   /**
    * Overwrite the default back-end connections associated with this server
    * configuration with the specified collection. If you only want to modify
    * certain connection settings, add new connections, etc. use
    * getBackEndConnections to get the existing collection and modify the
    * returned collection directly.
    * <p>
    * The PSCollection object supplied to this method will be stored with the
    * PSServerConfiguration object. Any subsequent changes made to the object by
    * the caller will also effect the server configuration.
    * 
    * @param insts a collection containing the security provider instance
    * definitions used for authentication (PSSecurityProviderInstance objects)
    * 
    * 
    * @throws PSIllegalArgumentException if the collection does not contain
    * object of the appropriate type
    * 
    * @see #getSecurityProviderInstances
    * @see PSSecurityProviderInstance
    */
   public void setSecurityProviderInstances(PSCollection insts)
      throws PSIllegalArgumentException
   {
      if (insts != null) {
         if (!com.percussion.design.objectstore.PSSecurityProviderInstance
            .class.isAssignableFrom(insts.getMemberClassType()))
         {
            Object[] args = { "Security Provider Instance",
               "PSSecurityProviderInstance", insts.getMemberClassName() };
            throw new PSIllegalArgumentException(
               IPSObjectStoreErrors.COLL_BAD_CONTENT_TYPE, args);
         }
      }

      m_securityProviders = insts;
      setModified(true);
   }

   /**
    * Get the default web pages being returned on error for this server.
    * Errors returned to users can be customized by
    * defining an alternative web page E2 will return when the given error
    * is encountered. When E2 hits an error, it provdes error information
    * in the form of an XML document. To provide diagnostic information to
    * the requestor, it may be preferred to use style sheets which E2 can
    * merge with the XML document to return a descriptive error page.
    * <p>
    * This may be null, in which case E2 will use its default error pages.
    * <P>
    * Each application can override the error pages being used.
    *
    * @return      the error pages object (may be <code>null</code>)
    *
    * @see         PSApplication#getErrorWebPages
    */
   public PSErrorWebPages getErrorWebPages()
   {
      return m_errorWebPages;
   }

   /**
    * Overwrite the error pages associated with the server with the
    * specified object. If you only want to modify certain settings, use
    * getErrorWebPages to get the existing object and modify the returned
    * object directly. Be sure to check that getErrorWebPage did not return
    * null, which signifies an error page has not been defined.
    * <p>
    * The PSErrorWebPages object supplied to this method will be stored with
    * the PSServerConfiguration object. Any subsequent changes made to the
    * object by the caller will also effect the application.
    * <p>
    * This may be null, in which case E2 will use its default error pages.
    * <P>
    * Each application can override the error pages being used.
    *
    *
    * @param   page      the new error web pages
    *
    * @see      #getErrorWebPages
    * @see      PSApplication#getErrorWebPages
    */
   public void setErrorWebPages(PSErrorWebPages page)
   {
      m_errorWebPages = page;
      setModified(true);
   }

   /**
    * Get the mail notification settings associated with the server
    * configuration.
    * Notification can be sent in response to various scenarios. See the
    * PSNotifier object for more info.
    *
    * @return     the mail notification settings associated with the server
    *             configuration
    * @see        PSNotifier
    */
   public PSNotifier getNotifier()
   {
      return m_notifier;
   }

   /**
    * Overwrite the server configuration's mail notification object with the
    * specified mail notification object. If you only want to modify some mail
    * notification settings, use getNotifier to get the existing object and
    * modify the returned object directly.
    * <p>
    * The PSNotifier object supplied to this method will be stored with the
    * PSServerConfiguration object. Any subsequent changes made to the object by the
    * caller will also effect the server configuration.
    *
    * @param notifier the new mail notification settings for the
    * server configuration
    * @see                 #getNotifier
    * @see                 PSNotifier
    */
   public void setNotifier(PSNotifier notifier)
   {
      m_notifier = notifier;
      setModified(true);
   }

   /**
    * Determine if this config has been modified since the last save operation.
    * 
    * @return <code>true</code> if it has been saved, <code>false</code>
    * otherwise.
    */
   public boolean isModified()
   {
      return m_modified;
   }

   /**
    * This method is used to update the server configuration status to modified.
    * Ex: setAppName chnages the local app name. This will not be returned to server
    * until save on server configuration is called. This flag will be used to find
    * weather any changes are done to server configuration that needs to be saved.
    * This will be mostly managed by server configuration class. Made public because,
    * may be required in some cases to explictly set it.
    *
    * @param bModified     <code>true</code> if the server configuration is changed,
    *                     else false
    */
   public void setModified(boolean bModified)
   {
      m_modified = bModified;
   }

   /**
    * @author   chadloder
    *
    * @version 1.5 1999/05/07
    *
    * Gets the shutdown delay in milliseconds between the time when the shutdown
    * request was issued and when the server begins to shut down.
    *
    * @return   long   The delay in milliseconds.
    */
   public long getShutDownDelayMS()
   {
      return m_shutDownDelayMsec;
   }

   /**
    * Get the application which will be activated when a request is made to the
    * server without explicitly specifying an application. For instance, if
    * Rhythmyx is the server's request root and MyApp is the application specified
    * as the default, a request for http://myserver/Rhythmyx will be routed to
    * http://myserver/Rhythmyx/MyApp
    *
    * @return  the name of the default application or null if there is no default.
    */
   public String getDefaultApplication()
   {
      return m_defaultAppName;
   }

   /**
    * Set the application which will be activated when a request is made to the
    * server without explicitly specifying an application. For instance, if
    * Rhythmyx is the server's request root and MyApp is the application specified
    * as the default, a request for http://myserver/Rhythmyx will be routed to
    * http://myserver/Rhythmyx/MyApp
    *
    * @param   appName  the name of the application
    */
   public void setDefaultApplication(String appName)
   {
      if (appName != null)
      {
         appName = appName.trim();
         if (appName.length() == 0)
            appName = null;
      }

      m_defaultAppName = appName;
      setModified(true);
   }

   /**
    * Get an object that contains certain UI option settings. The values for
    * this object may be set in the config/config.xml file on the
    * server. If they're not in that file, or if we've created the
    * PSServerConfiguration instance without referencing that file, then
    * the values are defaulted by the BrowserUISettings instance initializer.
    *
    * At most one such object is associated with this PSServerConfiguration
    * instance; this method creates it (with default values) if it doesn't
    * currently exist.
    *
    * @return the BrowserUISettings instance associated with this
    * PSServerConfiguration instance; never null
    *
    * @see     BrowserUISettings
    */
   private BrowserUISettings getBrowserUISettings()
   {
      if (m_browserUISettings == null)
      {
         m_browserUISettings = new BrowserUISettings();
      }
      return m_browserUISettings;
   }
   
   /**
    * Called from the legacy server configuration class to initializecertain UI
    * option settings from the supplied element node.  If the BrowserUISettings 
    * aren't specified in the tree, the constructor will use default values. 
    *
    * @param root The immediate parent element of the
    * <code>BrowserUISettings</code> object.  May not be <code>null</code>.
    * @return the BrowserUISettings instance associated with this
    * PSServerConfiguration instance; never null
    * 
    * @throws IllegalArgumentException if root is null
    */
   protected BrowserUISettings initBrowserUISettings(Element root)
    throws IllegalArgumentException
   {
      if (root == null)
         throw new IllegalArgumentException("root may not be null");
      
      return new BrowserUISettings(root);
   }

   /* *************** IPSDocument Interface Implementation *************** */

   /**
    * This method is called to create a PSXServerConfiguration XML document
    * containing the data described in this object.
    * <p>
    * The structure of the XML document is:
    * <pre><code>
    *   &lt;!--
    *    PSXServerConfiguration is used to manipulate an E2 server's
    *    configuration.
    *
    *    Object References:
    *
    *    PSXAcl - the server's access control list (ACL). This
    *    contains the names of users, groups and roles and the type of
    *    access they have for this server.
    *
    *    PSXLogger - the log settings for this server. Various levels
    *    of logging can be defined, which may be used for a variety of
    *    tasks ranging from usage tracking to application debugging.
    *
    *    PSXErrorWebPages - the web pages being return on error for this
    *    server. Errors can be customized to return an alternative web page
    *    when the given error is encountered. When E2 hits an error, it
    *    provides error information in the form of an XML document. To
    *    provide diagnostic information to the requestor, it may be preferred
    *    to use style sheets which E2 can merge with the XML document to
    *    return a descriptive error page.
    *
    *    PSXNotifier - the mail notification settings associated with the
    *    server. Notification can be sent in response to various
    *    scenarios. See the PSXNotifier object for more info.
    *   --&gt;
    *   &lt;!ELEMENT PSXServerConfiguration   (requestRoot?, defaultAppName?, PSXAcl,
    *                                          DataEncryption?, AdminEncryption?,
    *                                          Performance?, PSXLogger?
    *                                          PSXErrorWebPages?,
    *                                          BackEndConnections,
    *                                          SecurityProviders,
    *                                          DisableSandBoxSecurity?,
    *                                          PSXServerCacheSettings?,
    *                                          PSXNotifier?,
    *                                          BrowserUISettings?
    *                                          JndiConnectionPool?
    *                                         )&gt;
    *
    *   &lt;!--
    *    Attributes associated with the server configuration:
    *
    *    id - the internal identifier for this configuration. This should
    *    only be set by the E2 engine.
    *   --&gt;
    *   &lt;!ATTLIST
    *         id       ID         #REQUIRED
    *         type     (ContentHub, PublishingHub, Both) "Both"
    *   &gt;
    *
    *   &lt;!--
    *    the server's request root. This is combined with the application
    *    request root to determine if an incoming request should
    *    be serviced. For instance, if the server root is /E2 and the
    *    application root is /MyApp, only URLs whose path begins with
    *    /E2/MyApp will be considered for processing. This helps alleviate
    *    strain from the web server as E2 can more quickly determine what
    *    is an E2 request rather than a standard web server request. This
    *    is limited to 50 characters.
    *   --&gt;
    *   &lt;!ELEMENT requestRoot         (#PCDATA)&gt;
    *
    *   &lt;!ELEMENT defaultAppName    (#PCDATA)&gt;
    *
    *   &lt;!ELEMENT DataEncryption      (PSXDataEncryptor)&gt;
    *
    *   &lt;!ELEMENT AdminEncryption   (PSXDataEncryptor)&gt;
    *
    *   &lt;!ELEMENT Performance         (maxThreadsPerApp?,
    *                                    minThreadsOnServer?,
    *                                    maxThreadsOnServer?,
    *                                    idleThreadTimeout?,
    *                                  maxRequestsInQueuePerApp?,
    *                                  maxRequestsInQueueOnServer?,
    *                                    maxRequestTime?, userSessionEnabled?,
    *                                  userSessionTimeout?)&gt;
    *
    *   &lt;!--
    *    the maximum number of threads any given application may
    *    consume. The server maintains a pool of threads, which can grow as
    *    activity increases. The number of threads can be limited to avoid the
    *    resource problems that arise from excessive thread use.
    *
    *    This may not be an exact number as the total number of
    *    threads used by the server can be limited. For instance, if the
    *    application limit is set to 50 and the server limit is set to 200,
    *    ten applications cannot each use 50 threads. That would bring the
    *    threads in use to 500, which is well beyond the server limit of 200.
    *
    *    The thread limit can also be set on an application, to allow less
    *    critical applications from consuming resources.
    *   --&gt;
    *   &lt;!ELEMENT maxThreadsPerApp            (#PCDATA)&gt;
    *
    *   &lt;!--
    *    the minimum number of threads the server should make available
    *    for servicing user requests. The server maintains a pool of threads,
    *    which can grow and shrink as activity increases or decreases.
    *    The minimum number of threads can be set to speed processing for the
    *    expected request load.
    *   --&gt;
    *   &lt;!ELEMENT minThreadsOnServer         (#PCDATA)&gt;
    *
    *   &lt;!--
    *    the maximum number of threads the server may assign to applications
    *    for servicing user requests. The server maintains a pool of threads,
    *    which can grow as activity increases. The number of threads can be
    *    limited to avoid the resource problems that arise from excessive
    *    thread use.
    *   --&gt;
    *   &lt;!ELEMENT maxThreadsOnServer         (#PCDATA)&gt;
    *
    *   &lt;!--
    *      the amount of idle time, in minutes, that will cause a thread
    *    to be terminated. Even if a thread stays idle beyond the idle time
    *    limit, it will not be terminated if that would cause the number of
    *    available threads to fall below the minimum number of threads
    *      required.
    *   --&gt;
    *   &lt;!ELEMENT idleThreadTimeout            (#PCDATA)&gt;
    *
    *   &lt;!--
    *    the maximum number of requests which may be queued for processing
    *    by an application. When this limit is exceeded, the user is notified
    *    that the server is too busy (HTTP status code 503).
    *
    *    This may not be an exact number as the total number of requests in the
    *    queue can be limited by the server. For instance, if the
    *    application limit is set to 50 and the server limit is set to 200,
    *    ten applications cannot each queue 50 requests. That would bring the
    *    number of requests queued to 500, which is well beyond the server
    *    limit of 200.
    *
    *    The request queue limit can also be set on an application, to allow
    *    less critical applications from consuming resources. An application
    *    cannot exceed the server defined limit.
    *   --&gt;
    *   &lt;!ELEMENT maxRequestsInQueuePerApp   (#PCDATA)&gt;
    *
    *   &lt;!--
    *    the maximum number of requests which may be queued for processing
    *    by the server. When this limit is exceeded, the user is notified
    *    that the server is too busy (HTTP status code 503).
    *   --&gt;
    *   &lt;!ELEMENT maxRequestsInQueueOnServer   (#PCDATA)&gt;
    *
    *   &lt;!--
    *    The maximum amount of time to spend servicing a request.
    *
    *    The request time limit can also be set on an application basis.
    *    Unlike threads and the request queue, the application may exceed
    *    the value specified on the server.
    *   --&gt;
    *   &lt;!ELEMENT maxRequestTime               (#PCDATA)&gt;
    *
    *   &lt;!--
    *    is user session management on the server. When a user
    *    makes a connection to the E2 server, a session can be established
    *    to maintain state information across requests.
    *
    *    If this is disabled, applications cannot enable user session
    *    management. They can, however, disable for applications if it is
    *    enabled by default for the server.
    *   --&gt;
    *   &lt;!ELEMENT userSessionEnabled         (%PSXIsEnabled)&gt;
    *
    *   &lt;!--
    *    The default timeout interval for user sessions. When a user
    *    makes a connection to the E2 server, a session can be established
    *    to maintain state information across requests. When sessions are
    *    maintained, they must be terminated after a period of inactivity.
    *    This setting determines how long to wait before removing the
    *    inactive session.
    *
    *    Applications can override this setting.
    *   --&gt;
    *   &lt;!ELEMENT userSessionTimeout         (#PCDATA)&gt;
    *
    *   &lt;!--
    *    PSXConnectionConfig - the datasource configurations which
    *      will be used for data access through this server. Datasource
    *      configurations define the data source and origin.
    *   --&gt;
    *   &lt;!ELEMENT DatasourceConfigs         (PSXConnectionConfig*)&gt;
    *
    *   &lt;!--
    *    PSXSecurityProviderInstance - the security provider instance
    *    definitions which will be used for user authentication. When a
    *    user logs into E2, their credentials will be checked against
    *    the specified security provider(s).
    *   --&gt;
    *  &lt;!ELEMENT SecurityProviders (PSXSecurityProviderInstance*)&gt;
    *
    *
    *  &lt;!--
    *       The settings for the server's cache.  See
    *       {@link PSServerCacheSettings} for the full Xml format.
    *    -->
    *  &lt;!ELEMENT PSXServerCacheSettings (EMPTY)&gt;
    *
    *  &lt;!-- Should we use a sandbox protected by a security manager to
    *      prevent exits from accessing system resources? This restricts file
    *      access, network access, etc. When this is disabled (if the value is
    *      "y"), full access to the system is available to the exit.
    *    --&gt;
    *  &lt;!ELEMENT DisableSandBoxSecurity         (#PCDATA)&gt;
    *  &lt;!-- Placeholder for certain UI behavior properties --&gt;
    *  &lt;!ELEMENT BrowserUISettings  (ContentActions, SearchSettings)&gt;
    *  &lt;!--
    *       Describes how to link content items to actions on them. Values are:
    *       actionMenu -- Use an active menu (4.5+ behavior)
    *       ceLink -- Use a CE Url (4.0 behavior), or
    *       both -- Use <bold>both</bold> an active menu and a CE Url
    *  --&gt;
    *  &lt;!ELEMENT ContentActions EMPTY &gt;
    *     &lt;!ATTLIST ContentActions
    *          uiType (actionMenu | ceLink | both) "actionMenu"
    *     &gt;
    *
    *  &lt;!--
    *       If "yes", default searches to the user's currently selected community.
    *       If "no", searches all communities for content.
    *   --&gt;
    *  &lt;!ELEMENT SearchSettings EMPTY &gt;
    *  &lt;!ATTLIST SearchSettings
    *     useCurrentCommunity (yes | no) "yes"
    *  &gt;
    *
    *  &lt;!ELEMENT DirectoryServices (Directories*, DirectorySets*)&gt;
    *  &lt;!ELEMENT Directories (PSXDirectory*, GroupProviders?)&gt;

    *  &lt;!--
    *    PSXGroupProviderInstance - the group provider instance
    *    definitions which will be used for user authentication and cataloging.
    *    Used to locate groups and determine their membership.  Only
    *    PSXGroupProviderInstances that are referenced by at least one
    *    PSXSecurityProviderInstance are included.  If there are no group
    *    providers to include, this element is excluded.
    *    --&gt;
    *  &lt;!ELEMENT GroupProviders (PSXGroupProviderInstance+)&gt;
    *
    *  &lt;!--
    *       The JNDI connection pooling configuration.
    *   --&gt;
    *  &lt;!ELEMENT JndiConnectionPool (Property*) &gt;
    *  &lt;!ELEMENT Property (EMPTY) &gt;
    *  &lt;!ATTLIST Property
    *     name #REQUIIRED
    *     value #REQUIIRED
    *  &gt;
    * </code></pre>
    *
    * @return      the newly created PSXServerConfiguration XML document
    */
   public Document toXml()
   {
      int         size;
      Element      node;
      Document       doc = PSXmlDocumentBuilder.createXmlDocument();

      //create PSXserver configuration object and add attributes
      Element root = PSXmlDocumentBuilder.createRoot(doc, ms_NodeType);
      root.setAttribute ("id", String.valueOf(m_id));
      
      // set the server type attribute  
      if ( m_serverType == SERVER_TYPE_PUBLISHING_HUB)
         root.setAttribute (XML_TYPE_ATTR, XML_ATTR_TYPE_PUBLISHING_HUB);
      else
         root.setAttribute (XML_TYPE_ATTR, XML_ATTR_TYPE_SYSTEM_MASTER);
      
      //create requestRoot element
      PSXmlDocumentBuilder.addElement(   doc, root, "requestRoot", m_requestRoot);

      //create defaultAppName element
      if (m_defaultAppName != null)
         PSXmlDocumentBuilder.addElement( doc, root, "defaultAppName",
            m_defaultAppName);

      //create PSAcl elements
      if (m_acl != null)
         root.appendChild(m_acl.toXml(doc));


      // allow authentication details element
      PSXmlDocumentBuilder.addElement(   doc, root, NN_ALLOW_AUTH_DETAILS,
         (m_allowAuthDetails ? XML_BOOL_TRUE : XML_BOOL_FALSE ));

      // create the Performance element and its children
      node = PSXmlDocumentBuilder.addEmptyElement(doc, root, "Performance");


      //create userSessionEnabled element
      PSXmlDocumentBuilder.addElement(   doc, node, "userSessionEnabled",
         (m_userSessions ? "yes" : "no"));

      //create user sessions time out
      PSXmlDocumentBuilder.addElement(   doc, node, "userSessionTimeout",
         String.valueOf(m_sessionTimeout));

      //create user sessions time out
      PSXmlDocumentBuilder.addElement(   doc, node, "userSessionWarning",
         String.valueOf(m_sessionWarning));

      //create maximum open user sessions
      PSXmlDocumentBuilder.addElement(doc, node, "maxOpenUserSessions",
         String.valueOf(m_maxOpenSessions));

      //create PSLogger elements
      if (m_logger != null)
         root.appendChild(m_logger.toXml(doc));

      // create the LogTruncation element and its children
      node = PSXmlDocumentBuilder.addEmptyElement(doc, root, "LogTruncation");
      PSXmlDocumentBuilder.addElement( doc, node, "runningLogDays",
         String.valueOf(m_runningLogDays));

      //create PSErrorWebPages elements
      if (m_errorWebPages != null)
         root.appendChild(m_errorWebPages.toXml(doc));

      // create the JDBC driver configs
      node = PSXmlDocumentBuilder.addEmptyElement(doc, root, 
         XML_JDBC_DRIVER_CONFIGS_ELEM);
      for (PSJdbcDriverConfig config : m_jdbcDriverConfigs)
      {
         node.appendChild(config.toXml(doc));
      }
      
      //create m_securityProviders elements, create set of group providers used.
      if (m_securityProviders != null)
      {
         node = PSXmlDocumentBuilder.addEmptyElement(doc, root,
            "SecurityProviders");
         size = m_securityProviders.size();
         for(int i=0; i < size; i++)
         {
            PSSecurityProviderInstance provider =
               (PSSecurityProviderInstance)m_securityProviders.get(i);
            node.appendChild(provider.toXml(doc));
         }
      }

      if(m_serverCacheSettings != null)
         root.appendChild(m_serverCacheSettings.toXml(doc));

      // add DisableSandboxSecurity flag
      PSXmlDocumentBuilder.addElement(doc, root, "DisableSandboxSecurity",
         m_useSandboxSecurity ? "n" : "y");

      //create PSNotifier elements
      if (m_notifier != null)
         root.appendChild(m_notifier.toXml(doc));

      // Insert the BrowserUISettings directly under the document root
      this.getBrowserUISettings().toXml(doc, root);

      // Insert the PSXJavaPluginConfig directly under the document root
      if(m_javaPluginConfig != null)
         root.appendChild(m_javaPluginConfig.toXml(doc));
         
      // Insert the SearchConfig directly under the document root
      if (m_searchConfig != null)
         root.appendChild(m_searchConfig.toXml(doc));

      // set authentications
      if (!m_authentications.isEmpty())
      {
         Element authentications = PSXmlDocumentBuilder.addEmptyElement(doc,
            root, XML_AUTHENTICATIONS_ELEM);

         Iterator sources = m_authentications.iterator();
         while (sources.hasNext())
         {
            PSAuthentication source = (PSAuthentication) sources.next();
            authentications.appendChild(source.toXml(doc));
         }
      }

      // set directory services
      if (!m_directories.isEmpty() || !m_directorySets.isEmpty())
      {
         Set groupProviderNames = new HashSet();
         Element directoryServices = PSXmlDocumentBuilder.addEmptyElement(doc,
            root, XML_DIRECTORY_SERVICES_ELEM);

         if (!m_directories.isEmpty())
         {
            Element directories = PSXmlDocumentBuilder.addEmptyElement(doc,
               directoryServices, XML_DIRECTORIES_ELEM);

            Iterator sources = m_directories.iterator();
            while (sources.hasNext())
            {
               PSDirectory source = (PSDirectory) sources.next();
               directories.appendChild(source.toXml(doc));
               
               // add any groups
               if (source.getGroupProviderNames().hasNext())
               {
                  PSCollection names = new PSCollection(
                     source.getGroupProviderNames());
                  groupProviderNames.addAll(names);
               }               
            }
            
            // add group providers that are actually used
            if (!groupProviderNames.isEmpty())
            {
               Element groupsNode = PSXmlDocumentBuilder.addEmptyElement(doc,
                  directories, XML_GROUP_PROVIDERS_ELEMENT);
               Iterator groupProviders = m_groupProviders.iterator();
               while (groupProviders.hasNext())
               {
                  IPSGroupProviderInstance groupProvider =
                     (IPSGroupProviderInstance)groupProviders.next();

                  if (groupProviderNames.contains(groupProvider.getName()))
                  {
                     groupsNode.appendChild(groupProvider.toXml(doc));
                  }
               }
            }                     
         }
   

         if (!m_directorySets.isEmpty())
         {
            Element directorySets = PSXmlDocumentBuilder.addEmptyElement(doc,
               directoryServices, XML_DIRECTORY_SETS_ELEM);

            Iterator sources = m_directorySets.iterator();
            while (sources.hasNext())
            {
               PSDirectorySet source = (PSDirectorySet) sources.next();
               directorySets.appendChild(source.toXml(doc));
            }
         }
      }
      
      // set role services
      if (!m_roleProviders.isEmpty())
      {
         Element roleServices = PSXmlDocumentBuilder.addEmptyElement(doc,
            root, XML_ROLE_SERVICES_ELEM);

         Iterator sources = m_roleProviders.iterator();
         while (sources.hasNext())
         {
            PSRoleProvider source = (PSRoleProvider) sources.next();
            roleServices.appendChild(source.toXml(doc));
         }
      }

      // set the JNDI connection pool configuration
      Element jndiConnectionPool = PSXmlDocumentBuilder.addEmptyElement(doc,
         root, JNDI_CONNECTION_POOL_ELEM);
      Enumeration keys = m_jndiConnectionPool.keys();
      while (keys.hasMoreElements())
      {
         String key = (String) keys.nextElement();
         String value = m_jndiConnectionPool.getProperty(key);
         
         Element property = PSXmlDocumentBuilder.addEmptyElement(doc,
            root, PROPERTY_ELEM);
         property.setAttribute(PROPERTY_NAME_ATTR, key);
         property.setAttribute(PROPERTY_VALUE_ATTR, value);
         
         jndiConnectionPool.appendChild(property);
      }

      return doc;
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


      // get the performance settings from the Performance XML node
      if (tree.getNextElement("Performance", firstFlags) != null) {

         //create userSessionEnabled element
         sTemp = tree.getElementData("userSessionEnabled", false);
         if (sTemp != null)
            m_userSessions = sTemp.equalsIgnoreCase("yes");

         //create user sessions time out
         sTemp = tree.getElementData("userSessionTimeout", false);
         if (sTemp == null)
            m_sessionTimeout = 1800;
         else {
            try {
               m_sessionTimeout = Integer.parseInt(sTemp);
            } catch (NumberFormatException e) {
               Object[] args = { ms_NodeType, "userSessionTimeout", sTemp };
               throw new PSUnknownDocTypeException(
                  IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
            }
         }
       //create user sessions time out
         sTemp = tree.getElementData("userSessionWarning", false);
         if (sTemp == null)
            m_sessionWarning = m_sessionTimeout>120 ? 60 : -1;
         else {
            try {
               m_sessionWarning = Integer.parseInt(sTemp);
            } catch (NumberFormatException e) {
               Object[] args = { ms_NodeType, "userSessionWarning", sTemp };
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

      // restore JDBC driver configs
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
      
      tree.setCurrent(cur);

      m_securityProviders.clear();
      Element securityProviders = tree.getNextElement("SecurityProviders",
         firstFlags);
      if (securityProviders != null)
      {
         String curNodeType = PSSecurityProviderInstance.ms_NodeType;
         if (tree.getNextElement(curNodeType, firstFlags) != null)
         {
            PSSecurityProviderInstance spInst = null;
            do
            {
               spInst = new PSSecurityProviderInstance();
               spInst.fromXml((Element)tree.getCurrent(), this, null);
               /* Certain platforms don't support certain providers. If a bad
                  entry gets in the config, don't propagate it. */
               if ( PSSecurityProvider.isSupportedType( spInst.getType()))
                  m_securityProviders.add(spInst);
            } while(tree.getNextElement(curNodeType, nextFlags) != null);
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
      // constructor will use default values.
      m_browserUISettings = new BrowserUISettings(root);

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
      m_groupProviders.clear();
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
            
            // now load any group providers
            tree.setCurrent(directories);
            Element groupProviders = tree.getNextElement(
               XML_GROUP_PROVIDERS_ELEMENT, firstFlags);
            if (groupProviders != null)
            {
               String curNodeType = PSGroupProviderInstance.XML_NODE_NAME;
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
      
      // get the JNDI connection pool configuration
      tree.setCurrent(root);
      m_jndiConnectionPool.clear();
      m_jndiConnectionPool.putAll(ms_jndiConnectionPoolDefaults);
      Element jndiConnectionPool = tree.getNextElement(
         JNDI_CONNECTION_POOL_ELEM, firstFlags);
      if (jndiConnectionPool != null)
      {
         Element property = tree.getNextElement(PROPERTY_ELEM, firstFlags);
         while (property != null)
         {
            String name = property.getAttribute(PROPERTY_NAME_ATTR);
            String value = property.getAttribute(PROPERTY_VALUE_ATTR);
            if (name != null)
               m_jndiConnectionPool.setProperty(name, value);

            property = tree.getNextElement(PROPERTY_ELEM, nextFlags);
         }
      }
   }

   /**
    * Get an instance of a group provider.
    *
    * @param name The name of the provider, may not be <code>null</code> or
    * empty.  Match it case sensitive.
    * @param type The type of group provider.  One of the
    * <code>PSSecurityProvider.SP_TYPE_xxx</code> types.
    *
    * @return The matching group provider, or <code>null</code> if no matching
    * provider is found.  Changes to this provider will be reflected in this
    * object.
    *
    * @throws IllegalArgumentException if name is <code>null</code> or empty.
    */
   public IPSGroupProviderInstance getGroupProviderInstance(String name,
      int type)
   {
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null or empty");

      IPSGroupProviderInstance result = null;

      Iterator i = m_groupProviders.iterator();
      while (i.hasNext() && result == null)
      {
         IPSGroupProviderInstance gp = (IPSGroupProviderInstance)i.next();
         if (name.equals(gp.getName()) && type == gp.getType())
            result = gp;
      }

      return result;
   }

   /**
    * Sets the supplied group provider instance in this objects collection.
    * If a group provider with a matching name (case sensitive) and type exists
    * in the collection, it will be replaced.  If not, it will be added.
    *
    * @param inst The group provider instance to set, may not be
    * <code>null</code>.
    *
    * @return The original provider if it is being replaced, or
    * <code>null</code> if a match was not found.
    *
    * @throws IllegalArgumentException if inst is <code>null</code>.
    */
   public IPSGroupProviderInstance setGroupProviderInstance(
      IPSGroupProviderInstance inst)
   {
      if (inst == null)
         throw new IllegalArgumentException("inst may not be null");

      IPSGroupProviderInstance oldInst = null;
      for (int i = 0; i < m_groupProviders.size() && oldInst == null; i++)
      {
         IPSGroupProviderInstance group =
            (IPSGroupProviderInstance)m_groupProviders.get(i);
         if (group.getName().equals(inst.getName()) && group.getType() ==
            inst.getType())
         {
            oldInst = (IPSGroupProviderInstance)m_groupProviders.set(i, inst);
         }
      }

      if (oldInst == null)
         m_groupProviders.add(inst);

      return oldInst;
   }

   /**
    * Gets the collection of IPSGroupProviderInstance objects.  Any changes to
    * this collection are reflected in this object.
    *
    * @return The collection, never <code>null</code>, may be empty.
    */
   public PSCollection getGroupProviderInstances()
   {
      return m_groupProviders;
   }

   /**
    * Gets the collection of IPSGroupProviderInstance objects matching the
    * specified type.  Any changes to the objects in this collection are
    * reflected in this object.
    *
    * @param type The type of group provider.  One of the
    * <code>PSSecurityProvider.SP_TYPE_xxx</code> types.
    *
    * @return The collection, never <code>null</code>, may be empty.
    */
   public PSCollection getGroupProviderInstances(int type)
   {
      PSCollection result = new PSCollection(IPSGroupProviderInstance.class);

      Iterator i = m_groupProviders.iterator();
      while (i.hasNext())
      {
         IPSGroupProviderInstance gp = (IPSGroupProviderInstance)i.next();
         if (type == gp.getType())
            result.add(gp);
      }

      return result;
   }

   /**
    * Get the authentication for the supplied name.
    *
    * @param name the name of the authentication to get, not <code>null</code>
    *    or empty. Authentication names are case sensitive.
    * @return the authentication object or <code>null</code> if not found.
    */
   public PSAuthentication getAuthentication(String name)
   {
      if (name == null)
         throw new IllegalArgumentException("name cannot be null");

      name = name.trim();
      if (name.length() == 0)
         throw new IllegalArgumentException("name cannot be empty");

      Iterator authentications = m_authentications.iterator();
      while (authentications.hasNext())
      {
         PSAuthentication authentication =
            (PSAuthentication) authentications.next();
         if (authentication.getName().equals(name))
            return authentication;
      }

      return null;
   }

   /**
    * Get an iterator over all authentications.
    * 
    * @return all available authentications, never <code>null</code>, may be
    *    empty.
    */
   public Iterator getAuthentications()
   {
      return m_authentications.iterator();
   }

   /**
    * Add the supplied authentication to the collection. A new authentication
    * will be appended if it does not exist yet, otherwise the existing
    * authentication is replaced with the new one.
    *
    * @param authentication the new authentication to be added or replaced,
    *    not <code>null</code>.
    * @return the replaced authentication or <code>null</code> if it did
    *    not exist already.
    */
   public PSAuthentication addAuthentication(PSAuthentication authentication)
   {
      if (authentication == null)
         throw new IllegalArgumentException("authentication cannot be null");

      PSAuthentication replaced = removeAuthentication(authentication.getName());
      m_authentications.add(authentication);

      setModified(true);
      return replaced;
   }

   /**
    * Remove the authentication with the supplied name.
    *
    * @param name the name of the authentication to be removed, not
    *    <code>null</code> or empty. The name compairsion is case sensitive.
    * @return the removed authentication or <code>null</code> if none was
    *    found for the supplied name.
    */
   public PSAuthentication removeAuthentication(String name)
   {
      PSAuthentication auth = getAuthentication(name);
      if (auth != null)
         m_authentications.remove(auth);

      setModified(true);
      return auth;
   }
   
   /**
    * Removes all authentications.
    */
   public void removeAllAuthentications()
   {
      m_authentications.clear();
   }

   /**
    * Get the directory for the supplied name.
    *
    * @param name the name of the directory to get, not <code>null</code>
    *    or empty. Directory names are case sensitive.
    * @return the directory object or <code>null</code> if not found.
    */
   public PSDirectory getDirectory(String name)
   {
      if (name == null)
         throw new IllegalArgumentException("name cannot be null");

      name = name.trim();
      if (name.length() == 0)
         throw new IllegalArgumentException("name cannot be empty");

      Iterator directories = m_directories.iterator();
      while (directories.hasNext())
      {
         PSDirectory directory = (PSDirectory) directories.next();
         if (directory.getName().equals(name))
            return directory;
      }

      return null;
   }

   /**
    * Get an iterator over all directories.
    * 
    * @return all available directories, never <code>null</code>, may be
    *    empty.
    */
   public Iterator getDirectories()
   {
      return m_directories.iterator();
   }

   /**
    * Add the supplied directory to the collection. A new directory
    * will be appended if it does not exist yet, otherwise the existing
    * directory is replaced with the new one.
    *
    * @param directory the new directory to be added or replaced,
    *    not <code>null</code>.
    * @return the replaced directory or <code>null</code> if it did
    *    not exist already.
    */
   public PSDirectory addDirectory(PSDirectory directory)
   {
      if (directory == null)
         throw new IllegalArgumentException("directory cannot be null");

      PSDirectory replaced = removeDirectory(directory.getName());
      m_directories.add(directory);

      setModified(true);
      return replaced;
   }

   /**
    * Remove the directory with the supplied name.
    *
    * @param name the name of the directory to be removed, not
    *    <code>null</code> or empty. The name compairsion is case sensitive.
    * @return the removed directory or <code>null</code> if none was
    *    found for the supplied name.
    */
   public PSDirectory removeDirectory(String name)
   {
      PSDirectory directory = getDirectory(name);
      if (directory != null)
         m_directories.remove(directory);

      setModified(true);
      return directory;
   }
   
   /**
    * Removes all directories.
    */
   public void removeAllDirectories()
   {
      m_directories.clear();
   }

   /**
    * Get the directory set for the supplied name.
    *
    * @param name the name of the directory set to get, not <code>null</code>
    *    or empty. Directory set names are case sensitive.
    * @return the directory set object or <code>null</code> if not found.
    */
   public PSDirectorySet getDirectorySet(String name)
   {
      if (name == null)
         throw new IllegalArgumentException("name cannot be null");

      name = name.trim();
      if (name.length() == 0)
         throw new IllegalArgumentException("name cannot be empty");

      Iterator directorySets = m_directorySets.iterator();
      while (directorySets.hasNext())
      {
         PSDirectorySet directorySet = (PSDirectorySet) directorySets.next();
         if (directorySet.getName().equals(name))
            return directorySet;
      }

      return null;
   }

   /**
    * Get an iterator over all directory sets.
    * 
    * @return all available directory sets, never <code>null</code>, may be
    *    empty.
    */
   public Iterator getDirectorySets()
   {
      return m_directorySets.iterator();
   }

   /**
    * Add the supplied directory set to the collection. A new directory set
    * will be appended if it does not exist yet, otherwise the existing
    * directory set is replaced with the new one.
    *
    * @param directorySet the new directory set to be added or replaced,
    *    not <code>null</code>.
    * @return the replaced directory set or <code>null</code> if it did
    *    not exist already.
    */
   public PSDirectorySet addDirectorySet(PSDirectorySet directorySet)
   {
      if (directorySet == null)
         throw new IllegalArgumentException("directorySet cannot be null");

      PSDirectorySet replaced = removeDirectorySet(directorySet.getName());
      m_directorySets.add(directorySet);

      setModified(true);
      return replaced;
   }

   /**
    * Remove the directory set with the supplied name.
    *
    * @param name the name of the directory set to be removed, not
    *    <code>null</code> or empty. The name compairsion is case sensitive.
    * @return the removed directory set or <code>null</code> of none was
    *    found for the supplied name.
    */
   public PSDirectorySet removeDirectorySet(String name)
   {
      PSDirectorySet directorySet = getDirectorySet(name);
      if (directorySet != null)
         m_directorySets.remove(directorySet);

      setModified(true);
      return directorySet;
   }
   
   /**
    * Removes all directory sets.
    */
   public void removeAllDirectorySets()
   {
      m_directorySets.clear();
   }

   /**
    * Get the role provider for the supplied name.
    *
    * @param name the name of the role provider to get, not <code>null</code>
    *    or empty. Role provider names are case sensitive.
    * @return the role provider object or <code>null</code> if not found.
    */
   public PSRoleProvider getRoleProvider(String name)
   {
      if (name == null)
         throw new IllegalArgumentException("name cannot be null");

      name = name.trim();
      if (name.length() == 0)
         throw new IllegalArgumentException("name cannot be empty");

      Iterator roleProviders = m_roleProviders.iterator();
      while (roleProviders.hasNext())
      {
         PSRoleProvider roleProvider = (PSRoleProvider) roleProviders.next();
         if (roleProvider.getName().equals(name))
            return roleProvider;
      }

      return null;
   }

   /**
    * @return all available role providers, never <code>null</code>, may be
    *    empty.
    */
   public Iterator getRoleProviders()
   {
      return m_roleProviders.iterator();
   }

   /**
    * Add the supplied role provider to the collection. A new roel provider
    * will be appended if it does not exist yet, otherwise the existing
    * role provider is replaced with the new one.
    *
    * @param roleProvider the new role provider to be added or replaced,
    *    not <code>null</code>.
    * @return the replaced role provider or <code>null</code> if it did
    *    not exist already.
    */
   public PSRoleProvider addRoleProvider(PSRoleProvider roleProvider)
   {
      if (roleProvider == null)
         throw new IllegalArgumentException("roleProvider cannot be null");

      PSRoleProvider replaced = removeRoleProvider(roleProvider.getName());
      m_roleProviders.add(roleProvider);

      setModified(true);
      return replaced;
   }

   /**
    * Remove the role provider with the supplied name.
    *
    * @param name the name of the role provider to be removed, not
    *    <code>null</code> or empty. The name compairsion is case sensitive.
    * @return the removed role provider or <code>null</code> of none was
    *    found for the supplied name.
    */
   public PSRoleProvider removeRoleProvider(String name)
   {
      PSRoleProvider roleProvider = getRoleProvider(name);
      if (roleProvider != null)
         m_roleProviders.remove(roleProvider);

      setModified(true);
      return roleProvider;
   }
   
   /**
    * Removes all role providers.
    */
   public void removeAllRoleProviders()
   {
      m_roleProviders.clear();
   }

   /**
    * Validates the entire configuration within the given validation context.
    * <OL>
    * <LI>Static validation on all fields. This means that all tests are
    * performed that can be performed from a static method that takes the
    * field as its sole argument.
    * <BR>
    * This includes: verifying the presence (not-null) of fields that are
    * always required, verifying that the length and value of non-null
    * fields is within preset ranges (such as the length of name strings,
    * the value of integers that can never be negative or zero).
    * <LI>Validate all IPSComponent objects recursively.
    * <LI>Validate the semantics of all collections and relations of
    * IPSComponent objects. This includes checking for uniqueness of
    * things that need to be unique within their collection (like
    * request pages).
    * </OL>
    * @author   chadloder
    *
    * @version 1.26 1999/06/07
    *
    * @param   cxt
    *
    * @throws PSSystemValidationException
    *
    */
   public void validate(IPSValidationContext cxt) throws PSSystemValidationException
   {
      if (m_acl != null)
         m_acl.validate(cxt);


      if (m_logger != null)
         m_logger.validate(cxt);

      if (m_errorWebPages != null)
         m_errorWebPages.validate(cxt);

      if (m_securityProviders != null)
         validateCollection(m_securityProviders, cxt);

      if (m_notifier != null)
         m_notifier.validate(cxt);

   }

   private void validateCollection(PSCollection coll, IPSValidationContext cxt)
      throws PSSystemValidationException
   {
      for (int i = 0; i < coll.size(); i++)
      {
         Object o = coll.get(i);
         if (o instanceof IPSComponent)
         {
            IPSComponent comp = (IPSComponent)o;
            comp.validate(cxt);
         }
      }
   }

   @Override
   public boolean equals(Object o)
   {
      if (!(o instanceof PSServerConfiguration))
         return false;

      PSServerConfiguration other = (PSServerConfiguration)o;
      if (m_id != other.m_id)
         return false;

      if (!compare(m_requestRoot, other.m_requestRoot))
         return false;

      if (!compare(m_defaultAppName, other.m_defaultAppName))
         return false;

      if (!compare(m_acl, other.m_acl))
         return false;


      if (!compare(m_logger, other.m_logger))
         return false;

      if (!compare(m_errorWebPages, other.m_errorWebPages))
         return false;

      if (!compare(m_securityProviders, other.m_securityProviders))
         return false;

      if (!compare(m_notifier, other.m_notifier))
         return false;

      if (m_runningLogDays != other.m_runningLogDays)
         return false;

      if (m_userSessions != other.m_userSessions)
         return false;

      if (m_sessionTimeout != other.m_sessionTimeout)
         return false;
      
      if (m_sessionWarning != other.m_sessionWarning)
         return false;

      if (m_maxOpenSessions != other.m_maxOpenSessions)
         return false;

      if (m_modified != other.m_modified)
         return false;

      if (m_useSandboxSecurity != other.m_useSandboxSecurity)
         return false;

      if ( m_allowAuthDetails != other.m_allowAuthDetails )
         return false;

      if (m_groupProviders.size()!= other.m_groupProviders.size()) {
         return false;
      }else if (!compare(m_groupProviders, other.m_groupProviders)) {
         return false;
      }


      if (!compare(m_authentications, other.m_authentications))
         return false;

      if (!compare(m_jdbcDriverConfigs, other.m_jdbcDriverConfigs))
         return false;

      
      return true;
   }

   /**
    * Generates code of the object. Overrides {@link Object#hashCode()}.
    */
   @Override
   public int hashCode()
   {
      return m_id;
   }

   private static boolean compare(Object a, Object b)
   {
      if (a == null || b == null)
      {
         if (a != null || b != null)
            return false;
      }
      else if (!a.equals(b))
         return false;

      return true;
   }

   /**
    * Are we using a sandbox protected by a security manager to prevent
    * exits from accessing system resources? This restricts file
    * access, network access, etc. When this is disabled, full access
    * to the system is available to the exit.
    *
    * @return   <code>true</code> if a secure sandbox is in use,
    * <code>false</code> otherwise
    */
   public boolean getUseSandboxSecurity()
   {
      return m_useSandboxSecurity;
   }

   /**
    * Should we use a sandbox protected by a security manager to prevent
    * exits from accessing system resources? This restricts file
    * access, network access, etc. When this is disabled, full access
    * to the system is available to the exit.
    *
    * @param   enable      <code>true</code> to use a secure sandbox,
    *                                                                                                                                                                                                                                         <code>false</code> otherwise.
    */
   public void setUseSandboxSecurity(boolean enable)
   {
      m_useSandboxSecurity = enable;
   }

   /**
    * Gets the server's cache settings.  Changes to this object are not
    * reflected in this object.
    * {@link #setServerCacheSettings(PSServerCacheSettings)} must be called to
    * update this object.
    *
    * @return A copy of the cache settings contained by this object.  Never
    * <code>null</code>.
    */
   public PSServerCacheSettings getServerCacheSettings()
   {
      return (PSServerCacheSettings)m_serverCacheSettings.clone();
   }

   /**
    * Sets the server's cache settings, replacing the current settings.
    *
    * @param settings The settings object, may not be <code>null</code>.
    *
    * @throws IllegalArgumentException if settings is <code>null</code>
    */
   public void setServerCacheSettings(PSServerCacheSettings settings)
   {
      if(settings == null)
         throw new IllegalArgumentException("settings may not be null.");

      m_serverCacheSettings = settings;
   }

   /**
    * Get the maximum size the cache may grow to in KB -- that is, 1 = 1KB,
    * for an application.
    * To speed the processing of requests, results can be cached.
    * As the cache can grow, it may be useful to limit its size.
    * <P>
    * This may not be an exact number as the total cache across all
    * applications cannot exceed the server defined limit.
    * For instance, if the application limit is set to 1 MB and the server
    * limit is set to 20 MB, 25 applications cannot each cache 1 MB of data.
    * That would bring the cache size to 25 MB, which is well beyond the
    * server limit of 20 MB.
    * <P>
    * Caching is enabled in the data selector object of a query pipe
    * associated with an application. The maximum cache size used for the
    * pipe can be set there as well.
    *
    * @return            the maximum cache size in KB; -1 if there is no
    * upper bound on the cache size
    *
    * @see               PSDataSelector
    * @deprecated kept for current query cache handler to work. Should be
    * removed when <code>PSQueryCacheHandler</code> is removed. Should remove
    * the constant for default value also.
    */
   public int getMaxCacheSizePerApp()
   {
      return DEFAULT_APP_CACHE_SIZE;
   }

   /**
    * Get the maximum size the cache may grow to in KB -- that is, 1 = 1KB.
    * To speed the processing of requests, results can be cached.
    * As the cache can grow, it may be useful to limit its size.
    * <P>
    * Caching is enabled in the data selector object of a query pipe
    * associated with an application. The maximum cache size used for the
    * pipe can be set there as well.
    *
    * @return            the maximum cache size in KB; -1 if there is no
    * upper bound on the cache size
    *
    * @see               PSDataSelector
    * @deprecated kept for current query cache handler to work. Should be
    * removed when <code>PSQueryCacheHandler</code> is removed. Should remove
    * the constant for default value also.
    */
   public int getMaxCacheSizeOnServer()
   {
      return DEFAULT_SERVER_CACHE_SIZE;
   }

   
   /**
    * Determines if an external search engine has been installed, configured and
    * available on this server.
    *  
    * @return <code>true</code> if a search engine is available, 
    * <code>false</code> if not.
    */
   public boolean isSearchEngineAvailable()
   {
      return m_searchConfig.isFtsEnabled();
   }
   
   /**
    * Is JNDI connection pooling enabled?
    * 
    * @return <code>true</code> if it is, <code>false</code> otherwise.
    */
   public boolean isJndiConnectionPoolingEnabled()
   {
      String enabled = m_jndiConnectionPool.getProperty(
         "com.sun.jndi.ldap.connect.pool");
      return enabled != null && enabled.equalsIgnoreCase("true");
   }
   
   /**
    * Get the current JNDI connection pooling configuration.
    * 
    * @return the current configuration, never <code>null</code>, may be empty.
    *    Changes to the returned object will not affect this server 
    *    configuration.
    */
   public Properties getJndiConnectionPoolingConfig()
   {
      Properties result = new Properties();
      result.putAll(m_jndiConnectionPool);
      
      return result;
   }
   
   /**
    * Set a new JNDI connection pooling configuration.
    * 
    * @param properties the new JNDI configuration, may be <code>null</code> or
    *    empty. This object will never make any changes to the supplied 
    *    properties.
    */
   public void setJndiConnectionPoolingConfig(Properties properties)
   {
      m_jndiConnectionPool.clear();
      if (properties != null)
         m_jndiConnectionPool.putAll(properties);
   }

   /**
    * Get the list of JDBC driver configurations, used to help configure JNDI
    * datasources.
    * 
    * @return The list, never <code>null</code>.  A reference to the list held
    * by this object is returned, so that modifications to the list affect the
    * state of this object.
    */
   public List<PSJdbcDriverConfig> getJdbcDriverConfigs()
   {
      return m_jdbcDriverConfigs;
   }
   
   /**
    * Set the list of JDBC driver configurations.  See 
    * {@link #getJdbcDriverConfigs()} for more info.
    * 
    * @param configs The new list, may not be <code>null</code>.  The current 
    * list is cleared and all configs in the supplied list are added to the list 
    * held by this object.
    */
   public void setJdbcDriverConfigs(List<PSJdbcDriverConfig> configs)
   {
      if (configs == null)
         throw new IllegalArgumentException("configs may not be null");
      
      m_jdbcDriverConfigs.clear();
      m_jdbcDriverConfigs.addAll(configs);
      setModified(true);
   }
   
   /**
    * The server's cache settings, gets initialized when the server
    * configuration is read from xml, and never <code>null</code> after that.
    */
   protected PSServerCacheSettings m_serverCacheSettings;

   protected  boolean                  m_useSandboxSecurity;

   protected   int                     m_id = 0;   //server configuration Id generated by server
   private   String                  m_requestRoot = "";
   private  String                  m_defaultAppName = null;

   protected   PSAcl                     m_acl = null;            //acl object
   protected   PSLogger                  m_logger = null;
   protected   PSErrorWebPages         m_errorWebPages = null;
   protected   PSCollection            m_securityProviders = null;
   protected   PSNotifier               m_notifier = null;

   
   /** Should detailed info about authentication failures be returned. */
   private  boolean                 m_allowAuthDetails = false;

   // default is to truncate the log after 2 days
   protected  int                     m_runningLogDays               = 2;


   protected   boolean                  m_userSessions         = false;
   protected   int                     m_sessionTimeout      = 0;   // in secods
   protected   int                     m_sessionWarning      = 0;   // in secods
   protected PSJavaPluginConfig        m_javaPluginConfig = null;
   
   /**
    * Search configuration information. Never <code>null</code>, defaults
    * by creating one using the default ctor. Can be replaced by a setter
    * or in the <code>fromXml</code> method. 
    */
   protected PSSearchConfig m_searchConfig = new PSSearchConfig();
   
   /**
    * The maximum number of open user sessions cached by the server.
    */
   protected int m_maxOpenSessions = DEFAULT_OPEN_SESSIONS;

   private   boolean                  m_modified = false;   // Save will use this to update data to server. Can be used to bring Save prompts

   private static final int         MAX_REQ_ROOT_LEN   = 50;

   /* package access on this so they may reference each other in fromXml */
   public static final String      ms_NodeType = "PSXServerConfiguration";

   public static final int DEFAULT_APP_CACHE_SIZE         = 10240;      // 10 MB
   public static final int DEFAULT_SERVER_CACHE_SIZE      = 102400;   // 100 MB
   public static final int   DEFAULT_LOGIN_TIMEOUT         = 60;

   /**
    * The default maximum open user sessions used if not specified or the
    * number specified is invalid.
    */
   public static final int DEFAULT_OPEN_SESSIONS = 2000;

   /**
    * The minimal number of required open sessions needed to guarantee that the
    * amount of open sessions is not exceeded more than the maximum number of
    * requests the rhythmyx server can handle in one second without using too
    * much CPU time.
    */
   public static final int MINIMAL_REQUIRED_OPEN_SESSIONS =
      PSUserSessionManager.MAX_REQUESTS_PER_SECOND;

   // the number of milliseconds to delay before shutting down
   private long m_shutDownDelayMsec = 5000L;


   /** The element name for the m_allowAuthDetails member. */
   protected static final String NN_ALLOW_AUTH_DETAILS = "AllowDetailedAuthErrorMsgs";

   /** The text printed in the xml file for boolean elements when the value is
      <code>true</code>. */
   protected static final String XML_BOOL_TRUE = "yes";
   /** The text printed in the xml file for boolean elements when the value is
      <code>false</code>. */
   protected static final String XML_BOOL_FALSE = "no";

   /**
    *  Collection of IPSGroupProviderInstance objects, never <code>null</code>,
    *  may be empty.
    */
   protected PSCollection m_groupProviders = new PSCollection(
      IPSGroupProviderInstance.class);

   /**
    * A collection of authentications used to catalog directory services.
    * Initialized while constructed, never <code>null</code> after that, may
    * be empty.
    */
   protected PSCollection m_authentications =
      new PSCollection(PSAuthentication.class);

   /**
    * A collection of directories used to catalog directory services.
    * Initialized while constructed, never <code>null</code> after that, may
    * be empty.
    */
   protected PSCollection m_directories =
      new PSCollection(PSDirectory.class);

   /**
    * A collection of directory sets used to catalog directory services.
    * Initialized while constructed, never <code>null</code> after that, may
    * be empty.
    */
   protected PSCollection m_directorySets =
      new PSCollection(PSDirectorySet.class);

   /**
    * A collection of role providers used to determine role memberships.
    * Initialized while constructed, never <code>null</code> after that, may
    * be empty.
    */
   protected PSCollection m_roleProviders =
      new PSCollection(PSRoleProvider.class);
   
   /**
    * The active JNDI connection pool configuration, initialized while
    * constructed, never <code>null</code>, may be empty. Defaults to 
    * {@link #ms_jndiConnectionPoolDefaults} if not supplied.
    */
   private Properties m_jndiConnectionPool = new Properties();
   
   /**
    * The default connection pooling setting used if nothing else was supplied
    * with the server configuration file from which this was initialized. These
    * settings allow users to enable / disable and configure connection
    * pooling.
    * Connection poling is enabled by default for all authentication types and
    * supported protocols. Consult the JDK documentation for all properties
    * and their description.
    */
   private static Properties ms_jndiConnectionPoolDefaults = new Properties();

    public static final String SECURE_SOCKET_FACTORY = "secureSocketFactory";

   static
   {
      // enable connection pooling
      ms_jndiConnectionPoolDefaults.setProperty(
         "com.sun.jndi.ldap.connect.pool", "true");
      
      // use connection pooling for all protocols
      ms_jndiConnectionPoolDefaults.setProperty(
         "com.sun.jndi.ldap.connect.pool.protocol",
         "plain ssl");

       // use connection pooling for all protocols
       ms_jndiConnectionPoolDefaults.setProperty(
               SECURE_SOCKET_FACTORY,
               TLSSocketFactory.class.getName());

   }
   
   /**
    * List of driver configs, never <code>null</code> after construction, may
    * be empty.
    */
   protected List<PSJdbcDriverConfig> m_jdbcDriverConfigs = 
      new ArrayList<PSJdbcDriverConfig>();

   // Xml constants
   protected static final String XML_GROUP_PROVIDERS_ELEMENT = "GroupProviders";
   protected static final String XML_SELECTIVE_VIEW_REFRESH_ATTR = 
      "selectiveViewRefresh";

   protected static final String XML_AUTHENTICATIONS_ELEM = "Authentications";
   protected static final String XML_DIRECTORY_SERVICES_ELEM = "DirectoryServices";
   protected static final String XML_DIRECTORIES_ELEM = "Directories";
   protected static final String XML_DIRECTORY_SETS_ELEM = "DirectorySets";
   protected static final String XML_ROLE_SERVICES_ELEM = "RoleServices";
   protected static final String XML_JDBC_DRIVER_CONFIGS_ELEM = 
      "JdbcDriverConfigs";   
   protected static final String JNDI_CONNECTION_POOL_ELEM = 
      "JndiConnectionPool";
   protected static final String PROPERTY_ELEM = "Property";
   protected static final String PROPERTY_NAME_ATTR = "name";
   protected static final String PROPERTY_VALUE_ATTR = "value";

   /**
    * One of the attribute name at the root element. 
    */
   protected static final String XML_TYPE_ATTR = "serverType";
  
   
   /**
    * One of the allowed values for {@link #XML_TYPE_ATTR} attribute. 
    */
   public static final String XML_ATTR_TYPE_PUBLISHING_HUB = "Publishing Hub";
   /**
    * One of the allowed values for {@link #XML_TYPE_ATTR} attribute. 
    */
   public static String XML_ATTR_TYPE_SYSTEM_MASTER = "System Master";
   
   /**
    *  Helper class for managing certain UI behavior properties.
    *  Note that this class defines <code>toXml</code> and <code>fromXml</code>,
    *  but does <bold>not</bold> implement <code>IPSComponent</code> (or
    *  any interface, for that matter)
    */
   private static final class BrowserUISettings
   {

      /**
       * Create a new BrowserUISettings object with default values.
       */
      private BrowserUISettings()
      {
      }

      /**
       * Create a new <code>BrowserUISettings</code> object, using values
       * found in a <code>BrowserUISettings</code> element in the tree.
       * If the values are not found, use default values instead (no exception
       * is thrown.)
       *
       * @param parent    The immediate parent element of the
       * <code>BrowserUISettings</code> object. Assumed not
       * <code>null</code>, but is allowed not to contain a
       * <code>BrowserUISettings</code> element.
       */
      private BrowserUISettings(Element parent)
      {
         fromXml(parent);
      }

      /**
       * Add the class's info to the Document under the specified element.
       *
       * @param doc     The <code>Document</code> in which we're adding
       * elements. Assumed non-<code>null</code>.
       *
       * @param parent     The parent <code>Element</code> of the
       * <code>BrowserUISettings</code> element.
       * Assumed not <code>null</code>.
       */
      private void toXml(Document doc, Element parent)
      {
         Element browserNode =
            PSXmlDocumentBuilder.addEmptyElement(doc, parent,
                                                "BrowserUISettings");
         Element contentActionsNode =
            PSXmlDocumentBuilder.addEmptyElement(
               doc,
               browserNode,
               "ContentActions");
         contentActionsNode.setAttribute("uiType", m_contentAction);

         Element searchSettingsNode =
            PSXmlDocumentBuilder.addEmptyElement(
               doc,
               browserNode,
               "SearchSettings");
         searchSettingsNode.setAttribute("useCurrentCommunity",
            (m_searchCurrentCommunity ? XML_BOOL_TRUE : XML_BOOL_FALSE));
      }

      /**
       * Read the class's info from a BrowserUISettings element within
       * the specified parent element. The BrowserUISettings is not
       * required to exist in the parent. If the element is not found,
       * the class instance uses default values (no exception is thrown.)
       *
       * @param parent The parent Element in which the BrowserUISettings
       * would be found. Assumed not <code>null</code>.
       */
      private void fromXml(Element parent)
      {
         PSXmlTreeWalker tree = new PSXmlTreeWalker(parent);
         Element browserNode =
            tree.getNextElement(
               "BrowserUISettings",
               PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);

         if (browserNode == null)
         {
            return;
         }
         Element contentActionNode =
            tree.getNextElement(
               "ContentActions",
               PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN
                  | PSXmlTreeWalker.GET_NEXT_RESET_CURRENT);
         if (contentActionNode != null)
         {
            String ceValue = contentActionNode.getAttribute("uiType");
            if (ceValue.equalsIgnoreCase(CONTENT_ACTION_ACTIVE_MENU))
            {
               m_contentAction = CONTENT_ACTION_ACTIVE_MENU;
            }
            else if (ceValue.equalsIgnoreCase(CONTENT_ACTION_CE_LINK))
            {
               m_contentAction = CONTENT_ACTION_CE_LINK;
            }
            else if (ceValue.equalsIgnoreCase(CONTENT_ACTION_BOTH))
            {
               m_contentAction = CONTENT_ACTION_BOTH;
            }
         // else use the field's default value
         }

         Element searchNode =
            tree.getNextElement(
               "SearchSettings",
               (PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS
                  | PSXmlTreeWalker.GET_NEXT_RESET_CURRENT));
         if (searchNode != null)
         {
            String searchValue = searchNode.getAttribute("useCurrentCommunity");
            // anything but an explicit "no" defaults to a "yes" value
             m_searchCurrentCommunity =
              !searchValue.equalsIgnoreCase(XML_BOOL_FALSE);
         }

      }

      /** Value for ContentActions setting: Use Action Menu (4.5 default UI) */
      static final String CONTENT_ACTION_ACTIVE_MENU = "actionMenu";
      /** Value for ContentActions setting: Use CE Url (4.0 default UI) */
      static final String CONTENT_ACTION_CE_LINK = "ceLink";
      /** Value for ContentActions setting: Use both Action Menu and CE Url */
      static final String CONTENT_ACTION_BOTH = "both";


      /**
       * Enumerated value indicating how the menuing system in the browser
       * should work. Contains one of the <code>CONTENT_ACTION_xxx</code>
       * values. Defaults to <code>CONTENT_ACTION_ACTIVE_MENU</code>.
       * Initialized via constructor (which may invoke
       * {@link #fromXml(Element) fromXml}), then never changed.
       */
      String m_contentAction = CONTENT_ACTION_ACTIVE_MENU;


      /**
       * Indicates whether a search should be restricted to the currently
       * selected community, or operate across all content readable by
       * the user. Defaults to <code>true</code>
       * Initialized via constructor (which may invoke
       * {@link #fromXml(Element) fromXml}), then never changed.
       */
      boolean m_searchCurrentCommunity = true;
   }

   /** UI Behavior properties. Assigned a value either in the
    * {@link PSServerConfiguration.BrowserUISettings#fromXml(Element) fromXml}
    * method, or in {@link #getBrowserUISettings() getBrowserUISettings}
    * (hence, do not access directly, as it may be <code>null</code>
    *
    * These properties are not used by the server (or configured via
    * the Rhythmyx Server Administrator), but are used by style sheets
    * as content forms are generated. The config/config.xml file on the
    * server may be hand-edited to change these settings.
    *
    * @see #getBrowserUISettings()
    */
   protected BrowserUISettings m_browserUISettings = null;

   /**
    * The server type, one of the values in SERVER_TYPE_XXX. Default to 
    * {@link #SERVER_TYPE_SYSTEM_MASTER}.
    */
   protected int m_serverType = SERVER_TYPE_SYSTEM_MASTER;

   /**
    * A server used for both content contributor and publishing contents.
    */
   public static final int SERVER_TYPE_SYSTEM_MASTER = 0;

   /**
    * A server used for publishing contents only.
    */
   public static final int SERVER_TYPE_PUBLISHING_HUB = 1;
}



