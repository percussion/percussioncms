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

package com.percussion.server;

import com.percussion.cms.objectstore.PSCmsObject;
import com.percussion.cms.objectstore.PSFolder;
import com.percussion.cms.objectstore.PSInvalidContentTypeException;
import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.cms.objectstore.server.PSFieldRetriever;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.conn.PSServerException;
import com.percussion.data.IPSInternalRequestHandler;
import com.percussion.data.PSCachedStylesheet;
import com.percussion.data.PSDataExtractionException;
import com.percussion.data.PSInternalRequestCallException;
import com.percussion.data.PSMetaDataCache;
import com.percussion.data.PSTableMetaData;
import com.percussion.debug.PSDebugManager;
import com.percussion.design.objectstore.IPSJavaPluginConfig;
import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSAclEntry;
import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSAttribute;
import com.percussion.design.objectstore.PSContentEditor;
import com.percussion.design.objectstore.PSContentEditorSharedDef;
import com.percussion.design.objectstore.PSContentEditorSystemDef;
import com.percussion.design.objectstore.PSDataSet;
import com.percussion.design.objectstore.PSErrorWebPages;
import com.percussion.design.objectstore.PSLogger;
import com.percussion.design.objectstore.PSMacroDefinition;
import com.percussion.design.objectstore.PSMacroDefinitionSet;
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.design.objectstore.PSNotifier;
import com.percussion.design.objectstore.PSSearchConfig;
import com.percussion.design.objectstore.PSServerCacheSettings;
import com.percussion.design.objectstore.PSServerConfiguration;
import com.percussion.design.objectstore.PSSystemValidationException;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.design.objectstore.server.IPSApplicationListener;
import com.percussion.design.objectstore.server.IPSObjectStoreHandler;
import com.percussion.design.objectstore.server.IPSServerConfigurationListener;
import com.percussion.design.objectstore.server.PSApplicationSummary;
import com.percussion.design.objectstore.server.PSObjectStoreStatistics;
import com.percussion.design.objectstore.server.PSServerXmlObjectStore;
import com.percussion.design.objectstore.server.PSValidatorAdapter;
import com.percussion.design.objectstore.server.PSXmlObjectStoreHandler;
import com.percussion.design.server.PSDesignerConnectionHandler;
import com.percussion.error.PSErrorHandler;
import com.percussion.error.PSErrorManager;
import com.percussion.error.PSInternalError;
import com.percussion.error.PSRuntimeException;
import com.percussion.extension.IPSExtensionHandler;
import com.percussion.extension.IPSExtensionManager;
import com.percussion.extension.PSDatabaseFunctionManager;
import com.percussion.extension.PSExtensionManager;
import com.percussion.i18n.PSTmxResourceBundle;
import com.percussion.install.InstallUtil;
import com.percussion.log.PSLogHandler;
import com.percussion.log.PSLogManager;
import com.percussion.log.PSLogServerStop;
import com.percussion.log.PSLogServerWarning;
import com.percussion.process.IPSShutdownListener;
import com.percussion.search.IPSSearchErrors;
import com.percussion.search.PSAdminLockedException;
import com.percussion.search.PSSearchAdmin;
import com.percussion.search.PSSearchEngine;
import com.percussion.search.PSSearchException;
import com.percussion.search.PSSearchIndexEventQueue;
import com.percussion.security.PSAclHandler;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthenticationRequiredException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.security.PSEntry;
import com.percussion.security.PSRoleManager;
import com.percussion.security.PSSecurityProvider;
import com.percussion.security.PSSecurityProviderPool;
import com.percussion.security.PSSecurityToken;
import com.percussion.security.PSThreadRequestUtils;
import com.percussion.security.PSEncryptionException;
import com.percussion.security.PSEncryptor;
import com.percussion.security.IPSDecryptor;
import com.percussion.security.IPSKey;
import com.percussion.security.IPSSecretKey;
import com.percussion.security.PSEncryptionKeyFactory;
import com.percussion.server.cache.PSCacheException;
import com.percussion.server.cache.PSCacheManager;
import com.percussion.server.content.PSFormContentParser;
import com.percussion.services.PSBaseServiceLocator;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.services.locking.IPSObjectLockService;
import com.percussion.services.locking.PSObjectLockServiceLocator;
import com.percussion.services.locking.data.PSObjectLock;
import com.percussion.services.notification.PSNotificationHelper;
import com.percussion.services.utils.jexl.PSServiceJexlEvaluatorBase;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSCharSets;
import com.percussion.util.PSCollection;
import com.percussion.util.PSFormatVersion;
import com.percussion.util.PSProperties;
import com.percussion.util.PSServerShutdownHelper;
import com.percussion.util.PSSqlHelper;
import com.percussion.utils.container.IPSConnector;
import com.percussion.utils.container.IPSContainerUtils;
import com.percussion.utils.container.PSContainerUtilsFactory;
import com.percussion.utils.io.PathUtils;
import com.percussion.utils.jdbc.PSConnectionDetail;
import com.percussion.utils.jdbc.PSConnectionHelper;
import com.percussion.legacy.security.deprecated.PSLegacyEncrypter;
import com.percussion.utils.types.PSPair;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.serialization.PSObjectSerializer;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Level;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.apache.commons.lang.Validate.notNull;
/**
 * The PSServer class is the main E2 server application. The server,
 * when started, performs the following tasks:
 * <ul>
 * <li>loads server configuration settings</li>
 * <li>initializes the log mananger
 *     ({@link com.percussion.log.PSLogManager PSLogManager})</li>
 * <li>initializes the error mananger
 *     ({@link com.percussion.error.PSErrorManager PSErrorManager})</li>
 * <li>loads all applications from the object store</li>
 * <li>registers the object store listener
 *     ({@link com.percussion.server.PSObjectStoreListener
 *             PSObjectStoreListener})</li>
 * <li>creates all application handlers</li>
 * <li>starts the request listener threads</li>
 * <li> ??? </li>
 * </ul>
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSServer {


   /**
    * The property to define the broken link logic on publishing managed links
    * to be configurable based on a server property
    * brokenManagedLinkBehavior=
    *
    * deadLink - Broken links will be replaced by #
    *
    * removeLink - Broken links will be removed completely
    *
    * leaveLink - Link will be left broken
    */
    public static final String BROKEN_MANAGED_LINK_BEHAVIOR = "brokenManagedLinkBehavior";

   //Three options defined for above Property
   public static final String BROKEN_MANAGED_LINK_BEHAVIOR_DEADLINK = "deadLink";
   public static final String BROKEN_MANAGED_LINK_BEHAVIOR_REMOVELINK = "removeLink";
   public static final String BROKEN_MANAGED_LINK_BEHAVIOR_LEAVELINK = "leaveLink";

   /**
    * No external construction of this allowed.
    */
   private PSServer()
   {
      super();
   }

   /**
    * Returns a complete version string, such as Version 1.2  Build 20001012 (257)
    *
    * @return the version string including build; never <code>null</code>;
    *         will be empty if the server hasn't initialized the version yet
    */
   public static String getVersionString()
   {
      if (ms_version != null)
         return ms_version.getVersionString();
      else
         return "";
   }

   /**
    * Returns the version in the format major.minor (1.0, 2.4, ...)
    *
    * @return the version string; never <code>null</code>; will be empty
    *         if the server hasn't initialized the version yet
    */
   public static String getVersion()
   {
      if (ms_version != null)
         return ms_version.getVersion();
      else
         return "";
   }

   /**
    * Get the root directory to use when looking up files and directories
    * with server configuration and applications.
    * Default value returned by this method is a directory specified
    * by "rxdeploydir" system property. If this property
    * does not exist, contains invalid value or points to inexistent directory
    * then this method returns current directory.
    * Initialization from system property was created to specify Rhythmyx
    * root directory during unit tests.
    * @return the current root dir, never <code>null</code> and will never
    * change after initialization
    */
   public static File getRxDir()
   {
      return PathUtils.getRxDir(null);
      }

   /**
    * If the passed url is a file url and is "relative" to the root, then
    * resolve it to be relative to the real filesystem root. Add the JSESSIONID
    * cookie by rewriting the URL for http connections that are coming back to
    * this application.
    *
    * @param url url to check, must never be <code>null</code>
    * @return either the original URL for absolute paths or non-file protocols,
    * or a new URL with the path fixed up.
    * @throws IOException if there's a problem transforming the url
    */
   public static URL getResolvedURL(URL url) throws IOException
   {
      if (url.getProtocol().equals("file") && url.getFile().startsWith("/") == false)
      {
         File absFile = new File(getRxDir(), url.getFile());
         if (!absFile.exists())
         {
            throw new IOException("File not found for URL: " + absFile);
         }
         return new URL("file", url.getHost(), absFile.getAbsolutePath());
      }
      else if (url.getProtocol().startsWith("http"))
      {
         String host = url.getHost();
         int port = url.getPort();
         String path = url.getPath();

         boolean isloopback =
                 (host.equals("localhost") || host.equals("127.0.0.1") ||
                         host.equals(getHostName()) || host.equals(getHostAddress())) &&
                         (port == getSslListenerPort() || port == getListenerPort()) &&
                         path.startsWith(getRequestRoot());

         // String sessionid = (String) PSRequestInfo
         //       .getRequestInfo(PSRequestInfo.KEY_JSESSIONID);
         String sessionid = "";
         if (isloopback)
         {
            path = path + ";jsessionid=" + sessionid;
         }
         if (! StringUtils.isEmpty(url.getQuery()))
         {
            path = path + "?" + url.getQuery();
         }
         String scheme = PSServer.getProperty("proxyScheme");
         if(PSServer.isRequestBehindProxy(null) &&  !(host.equals("localhost") || host.equals("127.0.0.1"))){
            int ports  = Integer.valueOf(PSServer.getProperty("proxyPort"));
            String domainName = PSServer.getProperty("publicCmsHostname");
            return new URL(scheme, domainName, ports, path);
         }else{
            return new URL(url.getProtocol(), host, port, path);
         }
      }
      else
      {
         return url;
      }
   } // new

   /**
    * Get the configuration directory
    * @return the configuration directory relative to the root directory,
    * never <code>null</code>
    */
   public static String getRxConfigDir()
   {
      return getRxFile(SERVER_DIR);
   }

   /**
    * Get the directory that contains all other configuration directories.
    * 
    * @return the directory relative to the root directory, never
    * <code>null</code>. If it doesn't exist, an IllegalArgumentException is
    * thrown.
    */
   public static String getBaseConfigDir()
   {
      return getRxFile(BASE_CONFIG_DIR);
   }
   
   /**
    * Return the string path to a file or directory that is relative to the
    * rhythmyx root
    * 
    * @param path Input path, never <code>null</code> or empty
    * @return a fully qualified path to the file or directory desired. If it
    * doesn't exist, an IllegalArgumentException is thrown.
    */
   public static String getRxFile(String path)
   {
      if (path == null)
      {
         throw new IllegalArgumentException("path may not be null");
      }
      File item = new File(getRxDir(), path);
      if (item.exists() == false)
      {
         throw new IllegalArgumentException("file does not exist: " + item.getAbsolutePath());
      }
      return item.getAbsolutePath();
   }

   /**
    * Gets the extension manager. Meant for use only by the object store handler
    * so that it may install extensions.  Now extended to allow external
    * server objects to interact with the manager (using <code>null</code> as
    * the parameter.
    */
   public static IPSExtensionManager getExtensionManager(IPSObjectStoreHandler osHandler)
   {
      if ((osHandler == ms_objectStore) || (osHandler == null))
         return ms_extensionMgr;
      else
         throw new IllegalArgumentException("unknown objectstore handler");
   }

   /**
    * Set the extension manager for use in unit tests only!
    * 
    * @param manager the manager, never <code>null</code>.
    */
   public static void setExtensionManager(IPSExtensionManager manager)
   {
      if (manager == null)
      {
         throw new IllegalArgumentException("manager may not be null");
      }
      ms_extensionMgr = manager;
   }
   
   /**
    * Initializes the server, starting all required services, listeners, and
    * handlers.
    *
    * @param config The servlet configuration for the front-end servlet.
    *
    * @param args The arguments passed into the <code>main</code> method, in
    * case any of them are needed here.  May be not be <code>null</code>, may
    * be empty.
    *
    * @param toInit A set of flags to indicate what should be initialized.  Each
    * of the <code>INITED_xxx</code> flags are Or'd against this to determine
    * which services should be initialized.
    *
    * @return <code>true</code> if the server is successfully initialized,
    * <code>false</code> otherwise.
    */
   @SuppressWarnings(value="unchecked")
   public static boolean init(ServletConfig config, String[] args, int toInit)
   {
      
      if (args == null)
         throw new IllegalArgumentException("args may not be null");

      PSItemDefManager itemDefManager = PSItemDefManager.getInstance();
      try
      {
         try {
         itemDefManager.deferUpdateNotifications();

         ResourceBundle serverBundle =
          ResourceBundle.getBundle("com.percussion.server.PSStringResources");

         /*
          * We use the log4j logger built into the application server
          */
         //print copyright messages
         PSConsole.printMsg("Server",
            serverBundle.getString("copyright"), null, Level.OFF);
         PSConsole.printMsg("Server",
            serverBundle.getString("thirdPartyCopyright"), null, Level.OFF);

         PSConsole.printInfoMsg("Server",
            IPSServerErrors.SERVER_INIT_START, null, Level.OFF);

         initVersion();
         PSConsole.printMsg("Server", getVersionString(), null, Level.OFF);

         initResourceMap();
         
         /* load server configuration settings */
         if (!loadConfig())
            return false;

         // set up the server name
         try
         {
            ms_hostName = InetAddress.getLocalHost().getHostName();
            String firstBindAddress = getFirstBindAddress();
            if (firstBindAddress == null)
            {
               ms_serverName = InetAddress.getLocalHost().getHostName();
               ms_hostAddress = InetAddress.getLocalHost().getHostAddress();
            }
            else
            {
               ms_serverName = firstBindAddress;
               ms_hostAddress = firstBindAddress;
            }
         }
         catch (Throwable e)
         {
            // not a critical error
            ms_serverName = "localhost";
            ms_hostName = ms_serverName;
            ms_hostAddress = "127.0.0.1";
         }
         
         
         // Initialize the singleton with the class registry from this package
         PSObjectSerializer.getInstance().registerBeanClasses(PSServer.class);
         
         /*
          * get singleton of lock manager early on since anyone might start to
          * use it
          */
         PSServerLockManager.createInstance();

         // setup server port(s)
         loadListenerInfo();

         // load macro definitions
         initMacros();

         /* we need the objectstore handler to initialize the datasource
          * resolver
          */
         if (!initObjectStoreHandler())
            return false;

         /* initialize the datasource resolver. this must be done before
          * initializing the log
          */
         if (0 != (toInit & INITED_DB_POOL))
         {
            PSMetaDataCache.getInstance();
            ms_WhatsUp |= INITED_DB_POOL;
         }

         checkDatabaseCompatibility();
         if (0 != (toInit & INITED_LOG))
         {
            if (!initLogHandling())
               return false;
         }

         /* now we can log the server's starting */
         com.percussion.log.PSLogServerStart msg = new
            com.percussion.log.PSLogServerStart(ms_serverName);
         ms_serverLogHandler.write(msg);

         /* initialize the error mananger/handler */
         if (0 != (toInit & INITED_ERROR))
            initErrorHandling();

         /* Only install mgr if the flag is set, otherwise we run w/ no
            security. */
         if ( ms_srvConfig.getUseSandboxSecurity())
            System.setSecurityManager( new SecurityManager());

         if (0 != (toInit & INITED_EXTENSION))
         {
            Properties extProps = new Properties();
            extProps.put( IPSExtensionHandler.INIT_PARAM_CONFIG_FILENAME,
               IPSExtensionHandler.DEFAULT_CONFIG_FILENAME);

            ms_extensionMgr = new PSExtensionManager();
            
            // Register global listener for PSServiceJexlEvaluatorBase. The 
            // null passed ref indicates a global listener
            PSServiceJexlEvaluatorBase globalExtListener =
               new PSServiceJexlEvaluatorBase(false);
            ms_extensionMgr.registerListener(null, globalExtListener);
            
            String extStr = IPSExtensionHandler.EXTENSIONS_SUBDIR
                  + File.separator;
            File extDir = new File(PSServer.getRxFile(extStr));
            extDir.mkdir();
            ms_extensionMgr.init(extDir, extProps, true);
            ms_WhatsUp |= INITED_EXTENSION;
         }

         if (0 != (toInit & INITED_DATABASE_FUNCTION))
         {
            Properties dbFuncProps = new Properties();
            dbFuncProps.put(
               PSDatabaseFunctionManager.SYS_DB_FUNCTIONS_FILE,
               PSDatabaseFunctionManager.DEFAULT_SYS_DB_FUNCTIONS_FILE);
            dbFuncProps.put(
               PSDatabaseFunctionManager.USER_DB_FUNCTIONS_FILE,
               PSDatabaseFunctionManager.DEFAULT_USER_DB_FUNCTIONS_FILE);

            PSDatabaseFunctionManager.createInstance(dbFuncProps);
            ms_WhatsUp |= INITED_DATABASE_FUNCTION;
         }

         /* initialize security check and security provider pool */
         if (0 != (toInit & INITED_SECURITY))
         {
            initSecurity();
            ms_WhatsUp |= INITED_SECURITY;
         }

         // initialize the system control manager
         ms_sysCtrlMgr = PSSystemControlManager.createInstance(new File(getRxDir(), "sys_resources"));
         
         /* initialize the custom control manager */
         ms_customCtrlMgr = PSCustomControlManager.getInstance();
         ms_customCtrlMgr.init(getRxDir());
                
         /* JS: moved this to after initializing security so we can create
          * AclHandlers for each app as we create the app summaries in the
          * init of the PSXmlObjectStoreHandler instead of doing it lazily
          * when the summaries are requested by the designer.
          */
         PSApplication[] apps = null;
         /* initialize the object store (also inits request handlers) */
         if (0 != (toInit & INITED_OBJECT_STORE))
            apps = initObjectStore();

         /* initialize the user session mananger */
         PSUserSessionManager.init(ms_srvConfig, ms_objectStore);

         PSThreadRequestUtils.initServerThreadRequest();
         
         /* once the db pool and security pool is up, we can load the
          * runnable applications from the object store
          */
         if (0 != (toInit & INITED_REQ_HANDLERS))
            initRequestHandlers(apps);
         
         } finally {
               itemDefManager.commitUpdateNotifications();
         }
         // once the request handlers have started, we can start search services
         
         /*
          * Fix for search queue starting up too soon:
          * http://bugs/browse/CML-4793
          */
         
         
       
         if (!isCaseSensitiveURL())
            PSConsole.printInfoMsg("Server",
               IPSServerErrors.SERVER_INIT_CASE_INSENSITIVE_URLS, null);

        
         
         // create the lock file to indicate the server is running
         createServerStartupFileLock(SERVER_FILE_LOCK);

          // can package manager run without cachemanager started?
          ms_cacheManager.start();


          // Send message to Configuration Service:
         //     -Server Initialization complete, install the Packages.
         if (0 != (toInit & INITED_PACKAGE_INSTALL))
         {
            PSNotificationHelper.notifyServerInitComplete(getRxDir());
            ms_WhatsUp |= INITED_PACKAGE_INSTALL;
         }


         try
         {
            initSearch(ms_allApps);
         }
         catch (PSSearchException e)
         {
            throw new RuntimeException(e);
         }
         
         //free up memory
         ms_allApps = null;
         
         PSConsole.printInfoMsg("Server",
               IPSServerErrors.SERVER_INIT_END, null, Level.OFF);

        
         return true;
      }
      catch (Throwable e)
      {
         
         /* trap for unforeseen exceptions, allowing us to log the failure */
         Object[] params = { stackToString(e) };
         PSServerLogHandler.handleTerminalError(
            IPSServerErrors.EXCEPTION_NOT_CAUGHT, params);
         return false;
      }
      finally
      {
         itemDefManager.commitUpdateNotifications();
      }
   }

   public static String stackToString(Throwable t)
   {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      t.printStackTrace(pw);
      String sStackTrace = t.getMessage() + " Stack=:/n"+sw.toString(); // stack trace as a string
      return sStackTrace;
   }

   /**
    * A file lock created after server init. When this lock is acquired
    * it means the server is running. Must be called only once and that must be
    * at the startup time. A destroy method will release the lock
    * See {@link #destroyStartupFileLock(FileLock)}
    * @param  name the file to lock, never <code>null</code> or empty. This will
    * be a hidden file
    * @return  FileLock on successfully locking the resource file, else return
    * <code>null</code>
    */
   public static FileLock createServerStartupFileLock(String name)
   {
      if ( StringUtils.isBlank(name))
         throw new IllegalArgumentException("lock file name may not be null");
         
      String lockFile = getRxDir() + File.separator + name;
      
      FileLock lock = null;
      try
      {
         RandomAccessFile lockF = new RandomAccessFile(lockFile, "rw");
         FileChannel channel = lockF.getChannel();

         lock = channel.tryLock(0, 1, false);
         if (lock == null)
         {
            PSConsole.printWarnMsg("Server",
                  IPSServerErrors.RUNNING_SERVER_LOCK_NOT_ACQUIRED,
                  new String[]
                  {"Already locked"});
         }
      }
      catch (FileNotFoundException e)
      {
         PSConsole.printWarnMsg("Server",
               IPSServerErrors.RUNNING_SERVER_LOCK_NOT_ACQUIRED, new String[] {e
                     .getLocalizedMessage()});
      }
      catch (Throwable e)
      {
         PSConsole.printWarnMsg("Server",
               IPSServerErrors.RUNNING_SERVER_LOCK_NOT_ACQUIRED, new String[] {e
                     .getLocalizedMessage()});
      }
      ms_serverStartupLock = lock;
      return lock;
   }
   
   /**
    * Check if the file lock exists and release it. Must be called during
    * shutdown of the server. If the server is killed abruptly without handling
    * the shutdown hooks, even then this lock will be released by the system. 
    * 
    * @param lf the filelock to test and release may be <code>null</code>
    *
    */
   public static void destroyStartupFileLock(FileLock lf)
   {
      if ( lf == null )
         return;
      try
      {
         lf.release();
         lf.channel().close();
      }
      catch (Throwable e)
      {
         PSConsole.printMsg("Server",
               "Could not release lock on the running server");
      }
   }

   /**
    * Check that the database is compatible with Rhythmyx. This currently
    * checks that the database supports unicode. See
    * {@link PSServer#checkDatabaseCompatibility()} for details.
    */
   private static void checkDatabaseCompatibility()
   {
      /*
       * Check the database for Unicode compatibility
       */
      Connection conn = null;
      PSConnectionDetail connDetail= null;
      Log logger = LogFactory.getLog(PSServer.class);
      logger.info("Check database compatibility with unicode");
      try
      {
         // Use default connection
         conn = PSConnectionHelper.getDbConnection();
         connDetail = PSConnectionHelper.getConnectionDetail();
         
         boolean unicode = PSSqlHelper.supportsUnicode(conn, connDetail);
         if (!unicode)
         {
            logger.warn("!!!Database does not support unicode!!!");
         }
      }
      catch(Throwable e)
      {
         logger.error("Problem trying to discover if db supports unicode", e);
      }
      finally
      {
         if (conn != null)
         {
            try
            {
               conn.close();
            }
            catch (SQLException e)
            {
               logger.error("Problem closing connection", e);
            }
         }
      }
   }


   /**
    * Gets the container's JMX server mbean and looks up the http and https
    * listener ports.
    */
   private static void loadListenerInfo()
   {
      ms_listenerPort = -1;
      ms_sslListenerPort = 0;
      IPSContainerUtils containerUtils = PSContainerUtilsFactory.getInstance();
      ms_listenerPort = containerUtils.getConnectorInfo().getHttpConnector().map(IPSConnector::getPort).orElse(9992);

      ms_sslListenerPort =  containerUtils.getConnectorInfo().getHttpsConnector().map(IPSConnector::getPort).orElse(8443);
      
      // must at least get one listener port
      if (ms_listenerPort == -1 && ms_sslListenerPort == 0)
         throw new RuntimeException(
            "Failed to locate http listener port via JMX lookup");


         PSConsole.printMsg("Server", "Listening for HTTP requests on port: " +
                 ms_listenerPort);


         PSConsole.printMsg("Server", "Listening for HTTPS requests on port: " +
                 ms_sslListenerPort);

   }


   /**
    * Sets up the version field by reading com.percussion.util properties file
    */
   private static void initVersion()
   {

      ms_version = new PSFormatVersion(PSServer.class, "/com/percussion/util/Version.properties");
   }

   /**
    * Get the server's log handler. Logging through the server's log handler
    * should only be done when an application context does not exist.
    */
   public static PSLogHandler getLogHandler()
   {
      return ms_serverLogHandler;
   }

   /**
    * Get the server's error handler. Error handling through the server's
    * error handler should only be done when an application context does
    * not exist.
    */
   public static PSErrorHandler getErrorHandler()
   {
      return ms_serverErrorHandler;
   }

   /**
    * Check the server access level for the user's security token and throw
    * an exception if access is denied.
    *
    * @param tok The user's security token.
    * @param level The desired access level.
    *
    * @throws PSAuthorizationException if the user does not have the desired
    * access level.
    * @throws PSAuthenticationRequiredException if we have not yet tried to
    * authenticate the user.
    */
   public static void checkAccessLevel(PSSecurityToken tok, int level)
      throws PSAuthorizationException, PSAuthenticationRequiredException
   {
      if ((INITED_SECURITY & ms_WhatsUp) == 0)
      {
         // we have not initialized security yet
         throw new PSAuthorizationException(
            com.percussion.security.IPSSecurityErrors.SECURITY_NOT_INITIALIZED,
               null);
      }

      /* verify the user has the appropriate access for what they
       * are trying to do
       */
      int accessLevel = ms_AclHandler.getUserAccessLevel(tok);
      if ((accessLevel & level) != level)
      {
         throw new PSAuthorizationException(tok.getResourceType(),
            tok.getResourceName(), tok.getUserSessionId());
      }
   }

   /**
    * Check the server access level for the specified request and throw
    * an exception if access is denied.
    *
    * @param   request     the request object to check
    *
    * @param   level       the desired access level
    */
   public static void checkAccessLevel(PSRequest request, int level)
      throws PSAuthorizationException, PSAuthenticationRequiredException,
         PSAuthenticationFailedException
   {
      if ((INITED_SECURITY & ms_WhatsUp) == 0)
      {
         // we have not initialized security yet
         throw new PSAuthorizationException(
            com.percussion.security.IPSSecurityErrors.SECURITY_NOT_INITIALIZED, null);
      }

      if (!PSUserSessionManager.doesSessionExist(request))
      {
         IPSRequestHandler rh = getRequestHandler(request);
         if (rh != null && rh instanceof IPSValidateSession)
            throw new PSAuthenticationRequiredException("Designer Connection", null);
      }

      if (level == PSAclEntry.SACE_ADMINISTER_SERVER) {
         // first see if this is a local console command since it's an admin
         // request
         if (PSConsole.isConsoleUser(request))
            return;
      }

      /* These three string-type variables are added in by Jian Huang, on 04/25/1999*/
      String resourceType = request.getAppName();
      String resourceName = request.getRequestPage();
      String sessionId    = request.getUserSessionId();

      /* verify the user has the appropriate access for what they
       * are trying to do
       */
      int accessLevel = ms_AclHandler.getUserAccessLevel(request);
      if ((accessLevel & level) != level) {
         throw new PSAuthorizationException(resourceType, resourceName, sessionId);
      }
   }

   /**
    * Gets the server configuration object for this server.
    */
   public static PSServerConfiguration getServerConfiguration()
   {
      return ms_srvConfig;
   }

   /**
    * Gets the internal request object used to execute a request against the
    * specified resource.  The internal request object will have its own
    * request context seeded with the values of, but independent from, this
    * request context.  (Changes made to the request context during the
    * internal request will not be reflected in the original request context.)
    * Any parameters specified in a query string as part of
    * <code>resource</code> or included <code>extraParams</code> will be added
    * to the internal request's context.
    *
    * @param path specifies the application and page of the dataset to make
    * an internal request of.  May optionally include a "query string" --
    * name/value pairs, separated by equals, delimited by ampersand, and
    * identified as the portion of the path following a question mark.  May be
    * as little as "<code>appName/pageName</code>" or as much as
    * "<code>http://127.0.0.1:9992/Rhythmyx/AppTest/nov.xml?alpha=bravo&test=5
    * </code>".  Not <code>null</code> or empty.
    *
    * @param request the current request.  Will be cloned to provide the
    * inital state for the internal request.  Not <code>null</code>.
    *
    * @param extraParams an optional group of parameters to be added to the
    * internal request's context.  Skipped if <code>null</code>.
    *
    * @param inheritParams If <code>true</code>, the internal request
    * context will contain copies of all of the parameters in the current
    * request in addition to any parameters from the query string in
    * <code>path</code> and <code>extraParams</code>.
    * <p>
    * If <code>false</code>, the internal request context will only
    * have those parameters supplied by the query string in
    * <code>path</code> and <code>extraParams</code>.
    *
    * @param inputDoc If not <code>null</code>, it is set as the input doc
    *    on the cloned request object before it is passed to the resource.
    *
    * @return a new <code>IPSInternalRequest</code>, or <code>null</code> if
    * no handler could be found for the specified path
    *
    * @throws IllegalArgumentException if the specified path is
    * <code>null</code>, empty, or not in the correct format; or if
    * <code>request</code> is <code>null</code>.
    */
   public static PSInternalRequest getInternalRequest(String path,
                                                      PSRequest request,
                                                      Map extraParams,
                                                      boolean inheritParams,
                                                      Document inputDoc)
   {
      if ( path == null || path.trim().length() == 0)
         throw new IllegalArgumentException( "Path may not be null or empty" );
      if ( request == null)
         throw new IllegalArgumentException( "Request may not be null" );

      final String QUERY_STRING_MARKER = "?";

      // make copy of provided request, as we will be mutating the parameters
      PSRequest clonedRequest = request.cloneRequest();

      if (!inheritParams)
         clonedRequest.setParameters( new HashMap() );
      if (extraParams != null)
         clonedRequest.putAllParameters( extraParams );
      if ( null != inputDoc)
         clonedRequest.setInputDocument(inputDoc);

      // see if this request has query parameters
      int paramPos = path.indexOf( QUERY_STRING_MARKER );
      String queryParams = null;
      if (paramPos != -1) {
         queryParams = path.substring(paramPos+1);
      }

      // skip over query params
      int endPos = ( paramPos != -1 ? paramPos : path.length() );

      // search backwards to find page name (includes the extension, if any)
      int pagePos = path.lastIndexOf( "/", endPos );
      if (-1 == pagePos) // path contained no slashes
         throw new IllegalArgumentException(
            "Invalid internal request string (missing application name)");
      if (pagePos + 1 == endPos)  // path has slash at last character
         throw new IllegalArgumentException(
            "Invalid internal request string (missing page name)");
      String pageName = path.substring( pagePos + 1, endPos );

      // search backwards to find the app name
      if (0 == pagePos) // path has only one slash, at first character
         throw new IllegalArgumentException(
            "Invalid internal request string (missing application name)");
      int appPos = path.lastIndexOf( "/", pagePos - 1 );
      // if not found, appPos == -1 so we'll take from beginning of string
      String appName = path.substring( appPos + 1, pagePos );

      if (isResourcePathTranslatable())
      {
         String pageRootName = pageName;
         int dotPosition = pageName.lastIndexOf('.');
         if (dotPosition > 0)
            pageRootName = pageName.substring(0, dotPosition);
         PSPair<String, String> p = getTranslatedTarget(appName, pageRootName);
         if (p != null)
         {
            appName = p.getFirst();
            pageName = p.getSecond();
         }
      }
      // buildRequestFileURL
      StringBuffer requestURL = new StringBuffer();
      requestURL.append( PSServer.makeRequestRoot(null) );
      requestURL.append( "/" );
      requestURL.append( appName );
      requestURL.append( "/" );
      requestURL.append( pageName );
      clonedRequest.setRequestFileURL( requestURL.toString() );

      // add params from query string
      if (queryParams != null) {
         try
         {
            HashMap params = new HashMap();
            PSFormContentParser.parseParameterString( params, queryParams );
            clonedRequest.putAllParameters( params );
         } catch (PSRequestParsingException e)
         {
            throw new IllegalArgumentException(
               "Invalid query params in internal request string: " +
               e.getLocalizedMessage() );
         }
      }

      PSApplicationHandler appHandler = getApplicationHandler( appName );
      if (appHandler != null)
      {
         clonedRequest.setApplicationHandler(appHandler);
         IPSInternalRequestHandler ih =
            appHandler.getInternalRequestHandler(clonedRequest);
         if (ih != null)
            return new PSInternalRequest(clonedRequest, ih);
      }
      return null;
   }


   /**
    * Convenience method just like
    * {@link #getInternalRequest(String,PSRequest,Map,boolean,Document)},
    * except it gets the request from the supplied context and calls the other
    * method.
    *
    * @param ctx the request context which cannot be <code>null</code> and
    *    must be an instance of <code>PSRequestContext</code>.
    */
   public static PSInternalRequest getInternalRequest(String path,
                                                      IPSRequestContext ctx,
                                                      Map extraParams,
                                                      boolean inheritParams,
                                                      Document inputDoc)
   {
      if (!(ctx instanceof PSRequestContext))
      {
         throw new IllegalArgumentException(
               "ctx cannot be null and must be of type PSRequestContext");
      }
      PSRequest req = ((PSRequestContext)ctx).getRequest();
      return getInternalRequest(path, req, extraParams, inheritParams,
            inputDoc);
   }

   /**
    * Gets the request from the given request context.
    * 
    * @param ctx the request context, never <code>null</code>.
    * 
    * @return the request that is contained in the request context, never
    * <code>null</code>.
    */
   public static PSRequest getRequest(IPSRequestContext ctx)
   {
      if (ctx == null)
         throw new IllegalArgumentException("ctx may not be null.");
      
      return ((PSRequestContext)ctx).getRequest();
   }

   /**
    * Convenience method that calls the other 5 parameter version as follows:
    * {@link #getInternalRequest(String,PSRequest,Map,boolean)
    * getInternalRequest(path, erquest, extraParams, inheritParams, null}.
    */
   public static PSInternalRequest getInternalRequest(String path,
                                                      PSRequest request,
                                                      Map extraParams,
                                                      boolean inheritParams)
   {
      return getInternalRequest(path, request, extraParams, inheritParams,
            null);
   }


   /**
    * Get the internal request handler capable of handling the specified
    *    request.
    *
    * @param   request  the request to analyze in the format
    *                   appName/resource, never <code>null</code>
    *
    * @return           the internal request handler, <code>null</code> if
    *                   no suitable handler was found
    *
    * @throws  IllegalArgumentException if the request string is invalid
    * <B>
    * <I>TODO:- Update this documentation when category support is
    * implemented</I>
    */
   public static IPSInternalRequestHandler getInternalRequestHandler(
      String request)
   {
      IPSInternalRequestHandler irh = null;

      if ((request == null) ||
          (request.indexOf("/") < 1) ||
          (request.indexOf("/") == (request.length() - 1)) )
      {
         throw new IllegalArgumentException("Invalid request string");
      }

      /* Get the app root (currently app name) */
      String appRoot = request.substring(0, request.indexOf("/"));
      String pageName = request.substring(request.indexOf("/") + 1);
      if (isResourcePathTranslatable())
      {
         PSPair<String, String> p = getTranslatedTarget(appRoot, pageName);
         if (p != null)
         {
            appRoot = p.getFirst();
            pageName = p.getSecond();
         }
      }
      
      PSApplicationHandler ah = getApplicationHandler(appRoot);

      if (ah != null)
      {
         irh = ah.getInternalRequestHandler(pageName);
      }

      return irh;
   }

   /**
    * Contains the mappings described by the
    * {@link #getTranslatedTarget(String, String)} method. 
    */
   private static Map<PSPair<String, String>, PSPair<String, String>> 
      ms_resourceMap = 
         new HashMap<PSPair<String, String>, PSPair<String, String>>();
   /**
    * Initialize the resource map
    */
   static void initResourceMap()
   {
      Exception ex = null;
      File f = null;
      try
      {
         f = new File(getRxConfigDir(), "resourceMap.properties");
         if (f.exists())
         {
            // each entry is like 
            // appname/resourceName=appname2/resourceName2
            // The server will then redirect every request that arrives for
            // appname/resourceName to appName2/resourceName2
            Properties map = new Properties();
            try(FileInputStream fis = new FileInputStream(f)) {
               map.load(fis);
            }
            Enumeration e = map.propertyNames();
            while (e.hasMoreElements())
            {
               String key = e.nextElement().toString();
               String target = map.getProperty(key, "");
               if (StringUtils.isBlank(key) || StringUtils.isBlank(target)
                     || key.lastIndexOf('/') < 0 || target.lastIndexOf('/') < 0)
               {
                  String msg = 
                     "Skipping invalid entry in resource map file: {0}={1}";
                  PSConsole.printMsg("Server", MessageFormat.format(msg, key,
                        target));
                  continue;
               }
               String msg = "Adding resource map entry: {0}={1}";
               PSConsole.printMsg("Server", MessageFormat.format(msg, key,
                     target));
               
               int pos = key.lastIndexOf('/');
               String sourceAppName = key.substring(0, pos);
               String sourcePageName = key.substring(pos+1);
               
               pos = target.lastIndexOf('/');
               String targetAppName = target.substring(0, pos);
               String targetPageName = target.substring(pos+1);
               
               ms_resourceMap.put(
                     new PSPair<String, String>(
                           sourceAppName.toLowerCase(), 
                           sourcePageName.toLowerCase()),
                     new PSPair<String, String>(targetAppName, targetPageName));
               //also put just application with null for request page
               ms_resourceMap.put(
                     new PSPair<String, String>(
                           sourceAppName.toLowerCase(), 
                           null),
                     new PSPair<String, String>(targetAppName, null));
               
            }
         }
      }
      catch (FileNotFoundException e)
      {
         // should never happen because we check first
         ex = e;
      }
      catch (IOException e)
      {
         //just warn user
         ex = e;
      }
      catch (IllegalArgumentException e)
      {
         //ignore - config directory not found, maybe testing
      }
      
      if (ex != null)
      {
         PSConsole.printMsg("Server", ex,
               "Failed to load xml application resource map file: "
                     + (f == null ? "null" : f.getAbsolutePath()));
      }
   }

   /**
    * See {@link #getTranslatedTarget(String, String)}.
    * 
    * @return <code>true</code> if a translation could occur when the
    * referenced method was called, <code>false</code> otherwise.
    */
   private static boolean isResourcePathTranslatable()
   {
      return !ms_resourceMap.isEmpty();
   }

   public static boolean isRequestBehindProxy(HttpServletRequest request) {
       if(request == null){
           return Boolean.valueOf(PSServer.getProperty("requestBehindProxy", "false"));
       }else{
          try{
             String oldPath = request.getRequestURL().toString();
             URL requesturl = new URL(oldPath);
             if(requesturl.getHost().equalsIgnoreCase("localhost") ||  requesturl.getHost().equalsIgnoreCase("127.0.0.1")  ){
                return false;
             }else{
                return Boolean.valueOf(PSServer.getProperty("requestBehindProxy", "false"));
             }
          }catch (Exception e){
             return false;
          }

       }

   }
   
   /**
    * Server requests can be 'forwarded' by specifying a mapping in a file of
    * the form
    * <p>
    * appName/resourceName=appName2/resourceName2
    * </p>. This method does the lookup and either returns the translated value
    * or the supplied values if a mapping is not found.
    * <p>
    * To save a little time, call {@link #isResourcePathTranslatable()} first to
    * see if this method may perform a conversion.
    * <p>
    * The purpose of this is to provide backwards compatibility from 5.x to 6.x.
    * In 6.0, we moved content editors into their own application and needed
    * a way to redirect requests to the old resource to the new resource 
    * (mainly for Generic Word and any user created extensions that may be
    * calling the editor directly.). If no mapping is found with appName and
    * pageName, then tries to get the mapping with just appName by passing 
    * <code>null</code> for pageName. If found the pageName is added as second 
    * part. 
    * 
    * @param appName Assumed not <code>null</code> or empty.
    * @param pageName Assumed not <code>null</code> or empty.
    * @return <code>null</code> if no mapping is present. Otherwise, the first
    * entry is the translated app name and the 2nd entry is the translated
    * resource name. Both entries are non-empty strings.
    */
   private static PSPair<String, String> getTranslatedTarget(String appName,
         String pageName)
   {
      PSPair<String, String> key = new PSPair<String, String>(
            appName.toLowerCase(), pageName.toLowerCase());
      PSPair<String, String> p = ms_resourceMap.get(key);
      
      //If p is null try try to find the resource with just appname
      if(p == null)
      {
         p = ms_resourceMap.get(new PSPair<String, String>(
               appName.toLowerCase(), null));
         if(p!=null)
            p.setSecond(pageName);
      }
      
      //If p is not null print console message about the translation
      if (p != null)
      {
         String msg = "Translating application request {0}/{1} to {2}/{3}.";
         PSConsole.printMsg("Server", MessageFormat.format(msg, appName,
               pageName, p.getFirst(), p.getSecond()));
      }
      return p;
   }
   
   /**
    * Returns the internal request handler for the provided URL. The URL has
    * to be in one of the following formats:
    * <ul>
    * <li>
    *    http://host:port/serverRoot/appRoot/page.ext?...
    * </li>
    * <li>
    *    http://host/serverRoot/appRoot/page.ext?...
    * </li>
    * </ul>
    *
    * @param request the request URL we want the internal request handler for,
    *    may be <code>null</code>.
    * @return the internal request handler or <code>null</code> if not found.
    *
    * @deprecated This method does not consider page selection criteria or
    * supported mime types when selecting a page.  Use
    * {@link #getInternalRequest(String, PSRequest, Map, boolean)} instead.
    */
   @SuppressWarnings(value="unchecked")
   public static IPSInternalRequestHandler getInternalRequestHandler(
      URL request)
   {
      IPSInternalRequestHandler irh = null;
      if (request != null)
      {
         String path = request.getPath();
         String serverRoot = getRequestRoot();
         int pos = path.indexOf(serverRoot);
         if (pos >= 0)
            path = path.substring(pos+serverRoot.length()+1);

         String appRoot = path.substring(0, path.indexOf("/"));
         String requestPage = path.substring(path.indexOf("/") + 1);
         if (isResourcePathTranslatable())
         {
            PSPair<String, String> p = getTranslatedTarget(appRoot, requestPage);
            if (p != null)
            {
               appRoot = p.getFirst();
               requestPage = p.getSecond();
            }
         }

         PSApplicationHandler ah = getApplicationHandler(appRoot);
         if (ah != null)
         {
            pos = requestPage.indexOf('.');
            if (pos > 0)
               requestPage = requestPage.substring(0, pos);
            PSApplication app = ah.getApplicationDefinition();
            String resource = null;
            Iterator<PSDataSet> datasets = app.getDataSets().iterator();
            while (datasets.hasNext() && resource == null)
            {
               PSDataSet dataset = datasets.next();
               if (dataset.getRequestor().getRequestPage().equalsIgnoreCase(
                  requestPage))
                  resource = dataset.getName();
            }

            if (resource != null)
               irh = ah.getInternalRequestHandler(resource);
         }
      }

      return irh;
   }

   /**
    * Get the request handler capable of handling the specified request. If all
    * handlers have not been initialized yet, an error is reported to the
    * requestor and <code>null</code> is returned.
    *
    * @param req the request to analyze, may not be <code>null</code>.
    *
    * @return the request handler, may be <code>null</code> if a handler was not
    * found to handle the supplied request or if all handlers have not yet been
    * initialized.
    *
    * @throws IllegalArgumentException if <code>req</code> is <code>null</code>.
    */
   public static IPSRequestHandler getRequestHandler(PSRequest req)
   {
      if (req == null)
         throw new IllegalArgumentException("req may not be null");

      boolean requestsFinishedInit = isInited(INITED_REQ_HANDLERS);
      if (!requestsFinishedInit)
      {
         /* Respond that the server is still on its way up */
         PSResponse resp = req.getResponse();
         com.percussion.error.PSServerUnavailableError err = new
            com.percussion.error.PSServerUnavailableError(
               IPSServerErrors.SERVER_INITIALIZING_ERROR_MSG, null);

         PSServer.ms_serverErrorHandler.reportError(resp, err);
         return null;
      }

      IPSRequestHandler rh = null;

      if (isResourcePathTranslatable())
      {
         String reqPage = req.getRequestPage(false);
         String appName = req.getAppName();
         PSPair<String, String> p = getTranslatedTarget(appName, reqPage);
         if (p != null)
         {
            String reqRoot = req.getRequestRoot();
            reqRoot = reqRoot.substring(0, reqRoot.lastIndexOf('/') + 1)
                  + p.getFirst();
            
            req.setRequestFileURL(reqRoot + "/" + p.getSecond()
                  + req.getRequestPageExtension());
         }
      }
      
      String reqType = req.getCgiVariable(IPSCgiVariables.CGI_PS_REQUEST_TYPE);
      if (reqType != null)
      {
         /*
          * get the request type header field (PS-Request-Type) if this is not
          * specified, it's a data request
          */
         rh = ms_RequestHandlers.get(reqType);
         if (rh == null)
         {
            if (reqType.equalsIgnoreCase("data"))
            {
               rh = ms_RequestHandlers.get("data-"
                     + req.getCgiVariable(IPSCgiVariables.CGI_PS_APP_NAME, "")
                           .toLowerCase());
            }
            else if (reqType.toLowerCase().startsWith("design-objectstore-"))
            {
               rh = ms_RequestHandlers.get("design-objectstore-*");
            }
            else if (reqType.toLowerCase().startsWith("hook-"))
            {
               rh = ms_RequestHandlers.get("hook-*");
            }

         }
      }

      // check for a match using the request root amongst the rooted handlers
      if (rh == null)
      {
         String reqRoot = req.getRequestRoot();
         if (!isCaseSensitiveURL())
            reqRoot = reqRoot.toLowerCase();

         rh = ms_rootedRequestHandlers.get(reqRoot);

         if ((null == rh) && (ms_hasDefaultApp))
         {
            // use the default application if no application root specified
            // (ms_hasDefaultApp will not be true unless the default app is ready)
            if (reqRoot.endsWith("/"))
               reqRoot = reqRoot.substring(0,reqRoot.length()-1);
            if (reqRoot.equals(getRequestRoot()))
            {
               rh = ms_rootedRequestHandlers.get(getRequestRoot());
               req.setRequestFileURL(ms_requestRoot + "/" + ms_defaultAppName);
            }
         }
      }

      // make sure handler able to process this type of reqeuest method
      String reqMethod = req.getServletRequest().getMethod();
      if ( rh != null )
      {
         ArrayList methodList = (ArrayList)ms_requestHandlerTypes.get(rh);
         if (methodList == null || !methodList.contains(reqMethod.toUpperCase()))
         {
            // don't want to allow this to be used
            rh = null;
         }
      }

      return rh;
   }

   /**
    * Get the application handler for the specified application.
    *
    * @param   appName     the name of the app to locate
    *
    * @return              the application handler
    */
   public static PSApplicationHandler getApplicationHandler(String appName)
   {
      if (ms_RequestHandlers == null) return null; // Only for unit testing
      
      return (PSApplicationHandler)ms_RequestHandlers.get(
         "data-" + appName.toLowerCase());
   }

   public static PSApplicationSummary[] getApplicationSummaries(PSRequest req)
   {
      return ms_objectStore.getApplicationSummaryObjects(req);
   }

   /**
    * Determine whether the specified application is active on the
    *    server.
    *
    * @param         appName  The name of the application to check
    *
    * @return        whether the specified application is active
    */
   public static boolean isApplicationActive(String appName)
   {

      Object ah = ms_RequestHandlers.get("data-" +
         appName.toLowerCase());

      return (ah != null);
   }

   /**
    * Get the server statistics object. This is updated constantly by
    * the running applications. Any user of this object should minimize the
    * time spent in synchronized methods to avoid overall performance
    * degredation.
    *
    * @return        the server statistics
    */
   public static PSServerStatistics getStatistics()
   {
      return ms_Statistics;
   }

   /**
    * Get the object store statistics object. This is updated whenever an
    * action is performed against the object store. Any user of this object
    * should minimize the time spent in synchronized methods to avoid
    * overall performance degredation.
    *
    * @return        the object store statistics
    */
   public static PSObjectStoreStatistics getObjectStoreStatistics()
   {
      return ms_objectStore.getStatistics();
   }

   /**
    * Schedule a shut down of the application server, which will in turn cause
    * the rx servlet to shutdown. This is the method to use when trying to shut
    * down the server from within any server code. Method returns and the
    * shutdown is requested asynchronously from a separate thread.
    * 
    * @param downWhen the amount of time, in milliseconds, to wait before
    * shutting down the server, ignored if not > 0.
    */
   public static void scheduleShutdown(final long downWhen)
   {
      Thread thread = new Thread() {

         @Override
         public void run()
         {
            // sleep if requested
            if (downWhen > 0)
            {
               try
               {
                  Thread.sleep(downWhen);
               }
               catch (InterruptedException e)
               {
                  // oh well, keep going
               }
            }
            
            // now try to shutdown the app server
            try
            {
               System.exit(0);
            }
            catch (Throwable e)
            {
               PSConsole.printMsg("Server", e, 
                  "Failed to initiate server shutdown");
            }
         }};

      thread.setDaemon(true);
      thread.start();
   }


   /**
    * Loads the system definition for Content Editors. All fields are fixed
    * up for data type and mime type; and the backend columns for all fields
    * are fully defined.
    *
    * @return The content editor system def object.  May be <code>null</code>
    * if there was an error loading the def from the Xml, or one wasn't found.
    */
   public static PSContentEditorSystemDef getContentEditorSystemDef()
   {
      PSContentEditorSystemDef systemDef = null;
      try
      {
         systemDef = PSServerXmlObjectStore.getInstance()
               .getContentEditorSystemDef();
      }
      catch (RuntimeException e)
      {
         // let these propagate
         throw e;
      }
      catch (Throwable e)
      {
         /* Some IO or parsing exception probably - log it and return the
          * empty config - resource handler will deal with any missing data.
          */
         int msgCode = IPSServerErrors.CE_SYSTEM_DEF_LOAD;
         Object[] args = {e.toString()};
         PSLogManager.write(
            new PSLogServerWarning(
            msgCode, args, true, "Server"));
      }

      return systemDef;
   }

   /**
    * Gets the shared definition for Content Editors.  Delegates to
    * <code>PSServerXmlObjectStore</code>.
    *
    * @return The content editor shared def object. May be <code>null</code>
    *    if there was an error loading the def from the Xml. If none was found,
    *    an empty shared def will be returned.
    * @see PSServerXmlObjectStore#getContentEditorSharedDef()
    */
   public static PSContentEditorSharedDef getContentEditorSharedDef()
   {
      PSContentEditorSharedDef sharedDef = null;
      try
      {
         sharedDef =
            PSServerXmlObjectStore.getInstance().getContentEditorSharedDef();

         // create an empty shared def if there is none
         if (sharedDef == null)
            sharedDef = new PSContentEditorSharedDef();
      }
      catch (RuntimeException e)
      {
         // let these propagate
         throw e;
      }
      catch (Throwable e)
      {
         /* Some IO or parsing exception probably - log it and return the
          * empty config - resource handler will deal with any missing
          * data.
          */
         int msgCode = IPSServerErrors.CE_SHARED_DEF_LOAD;
         Object[] args = {"unknown", e.toString()};
         PSLogManager.write(
            new PSLogServerWarning( msgCode, args, true, "Server") );
      }

      return sharedDef;
   }

   /**
    * Gets the Acl entries matching the specified criteria from all active
    * applications and the server's Acl.
    *
    * @param type The type of Acl entry to locate.  Must be one of the
    * <code>PSAclEntry.ACE_TYPE_xxx</code> value.
    *
    * @return An Iterator over <code>0</code> or more <code>PSEntry</code>
    * objects.  The specific type of entry will be determined by the type of Acl
    * entry requested.  Never <code>null</code>. The list will not contain
    * duplicate entries based on name, providerType, and providerInstance.  The
    * access level of the entries returned will be undefined and should be
    * ignored.
    *
    * @throws IllegalArgumentException if <code>type</code> does not represent a
    * valid type.
    */
   @SuppressWarnings(value="unchecked")
   public static Iterator<PSEntry> getAclEntries(int type)
   {
      Map<String,PSEntry> entryMap = new HashMap<String,PSEntry>();

      // add app handler entries
      Iterator<IPSRequestHandler> handlers = 
         ms_RequestHandlers.values().iterator();
      while (handlers.hasNext())
      {
         IPSRequestHandler rh = handlers.next();
         if (rh instanceof PSApplicationHandler)
         {
            PSApplicationHandler ah = (PSApplicationHandler)rh;
            /* this will validate the type, don't want to duplicate that code
             * here
             */
            Iterator<PSEntry> entries = ah.getAclEntries(type);
            while (entries.hasNext())
            {
               PSEntry entry = entries.next();
               String key = entry.getName();
               entryMap.put(key, entry);
            }
         }
      }

      // add server entries
      Iterator<PSEntry> entries = ms_AclHandler.getAclEntries(type);
      while (entries.hasNext())
      {
         PSEntry entry = entries.next();
         String key = entry.getName();
         entryMap.put(key, entry);
      }

      return entryMap.values().iterator();
   }

   /**
    * Load the configuration files.
    * 
    * @return <code>true</code> if it is successfully loaded, <code>false</code>
    * if there were errors.
    */
   private static boolean loadConfig() 
   {
      PSConsole.printInfoMsg("Server",
            IPSServerErrors.LOADING_CONFIG, (Object[])null);

      /* load server configuration settings for:
       *    - Server (log, request queue, etc.)
       *    - ObjectStore
       */
      try 
      {
         // load the server properties file

         File propFile = PSProperties.getConfig(ENTRY_NAME, PROPS_SERVER,
               getRxConfigDir());

         ms_serverProps = new PSProperties(propFile.getPath());

         if (!ms_serverProps.isEmpty())
         {

           /* Determine whether to pause and wait for user input before exiting
             * ms_noPauseOnExit will be set to true once all initialization
             * is complete.
             */

            String noPauseOnExit =
                             ms_serverProps.getProperty(PROP_NO_PAUSE_ON_EXIT);
            if ( null != noPauseOnExit &&
                 noPauseOnExit.trim().equalsIgnoreCase("true"))
            {
               ms_noPauseOnExit  = true;
            }

            String isEncrypted = ms_serverProps.getProperty("pwEncrypted");
            if (isEncrypted != null &&
               isEncrypted.trim().equalsIgnoreCase("yes"))
            {
               // decrypt the password
               String password = "";
               try {
                  password = PSEncryptor.getInstance(PSEncryptionKeyFactory.AES_GCM_ALGORIYTHM,null).decrypt(ms_serverProps.getProperty("loginPw"));
               }catch(PSEncryptionException | java.lang.IllegalArgumentException e) {
                  password = eatLasagna(ms_serverProps.getProperty("loginId"),
                          ms_serverProps.getProperty("loginPw"));
               }

               ms_serverProps.setProperty("loginPw", password);
            }

            // should we interpret URL's case sensitive
            String caseSensitiveUrl =
               ms_serverProps.getProperty("caseSensitiveUrl");
            if (caseSensitiveUrl != null)
               ms_caseSensitiveUrl =
                  caseSensitiveUrl.trim().equalsIgnoreCase("true");

            // must cms field and fieldset names be unique?
            String requireUniqueFields = ms_serverProps.getProperty(
               PROP_UNIQUE_FIELD_NAMES);
            if (requireUniqueFields != null)
               ms_requireUniqueFieldNames =
                  requireUniqueFields.trim().equalsIgnoreCase("true");

            // and load the object store props from the location
            // defined in the server props file
            ms_objectStoreProps  = new PSProperties(
                  getRxFile(ms_serverProps.getProperty(PROPS_OBJECT_STORE_VAR,
                  PROPS_OBJECT_STORE)));

         }
         
         return true;
      } 
      catch (Throwable e) 
      {
         /* this is thrown if one of our config files is missing, etc.
          * we really can't recover from this, so spit out an error.
          */
         Object[] params = { e.getMessage() };
         PSServerLogHandler.handleTerminalError(
                              IPSServerErrors.LOAD_CONFIG_FAILURE, params);
         return false;
      }
   }

   /**
    * Initialize the object store handler and load the server configuration.
    * @return <code>true</code> if it is successfully initialized, 
    * <code>false</code> if not.
    *
    * @throws PSServerException if the server is not responding.
    * @throws PSNotFoundException if the server configuation is not be found.
    */
   @SuppressWarnings(value="unchecked")
   private static boolean initObjectStoreHandler() throws PSServerException,
      PSNotFoundException
   {
      try
      {
         ms_objectStore = new PSXmlObjectStoreHandler(ms_objectStoreProps);
         PSServerXmlObjectStore.createInstance(
            (PSXmlObjectStoreHandler)ms_objectStore);
      }
      catch (Throwable e)
      {
         /* the objectDirectory setting is missing or points to an
          * invalid directory.
          * we really can't recover from this, so spit out an error.
          */
         Object[] params = { e.getMessage() };
         PSServerLogHandler.handleTerminalError(
                           IPSServerErrors.OBJECT_STORE_INIT_FAILED, params);
         
         return false;
      }

      /* load the server configuration object */
      ms_srvConfig = ms_objectStore.getServerConfigurationObject();
      if (ms_srvConfig != null)
      {
         updateServerRequestRoot(ms_srvConfig);

         String type = (ms_srvConfig.getServerType() == 
            PSServerConfiguration.SERVER_TYPE_PUBLISHING_HUB) ? PSServerConfiguration.XML_ATTR_TYPE_PUBLISHING_HUB
               : PSServerConfiguration.XML_ATTR_TYPE_SYSTEM_MASTER;
         PSConsole.printMsg("Server", "Server type: " + type);
      }
      
      return true;
   }

   /**
    * Initialize the object store by loading all the apps and registering
    * the change listener.
    *
    * @return  PSApplication[] All of the ENABLED application objects defined
    * in the object store.
    *
    * @throws  PSServerException
    * @throws  PSAuthorizationException
    */
   private static PSApplication[] initObjectStore()
      throws PSServerException, PSAuthorizationException
   {
      PSConsole.printInfoMsg("Server",
         IPSServerErrors.OBJECT_STORE_INIT, (Object[])null);

      PSApplication[] apps = ms_objectStore.init();
      ms_allApps = apps;
      ArrayList<PSApplication> appList = new ArrayList<PSApplication>(apps.length);
      for (int i = 0; i < apps.length; i++)
      {
         PSApplication app = apps[i];
         if (app.isEnabled())
            appList.add(app);
      }

      /* switch the log handler to use the logger defined in the cfg */
      PSLogger logger = ms_srvConfig.getLogger();
      if (logger != null) {
         // now do the switch
         ms_serverLogHandler = new PSLogHandler(logger);
      }

      int days = ms_srvConfig.getRunningLogDays();
      com.percussion.log.PSLogManager.setRunningLogDays(days);

      /* switch the error handler to use the handler defined in the cfg */
      PSErrorWebPages errWebPages = ms_srvConfig.getErrorWebPages();
      PSNotifier notifier = ms_srvConfig.getNotifier();
      ms_serverErrorHandler = new PSErrorHandler(errWebPages, notifier);

      /* register the object store listener (PSObjectStoreListener) */
      PSServer server = new PSServer();
      ms_objectStore.addServerListener(server.new PSServerConfigChangeListener());
      ms_objectStore.addApplicationListener(server.new PSServerAppChangeListener());

      ms_WhatsUp |= INITED_OBJECT_STORE;

      return appList.toArray(new PSApplication[appList.size()]);
   }

   /**
    * Returns the current server request root. If case sensitivity is disabled,
    * the request root is lower cased before returning it. The leading slash is
    * part of the root.
    *
    * @return the server request root as it is if case sensitivity is enabled,
    *    lower cased if case sensitivity is disabled.
    */
   public static String getRequestRoot()
   {
      return isCaseSensitiveURL() ? ms_requestRoot
            : ms_requestRoot.toLowerCase();
   }

   /**
    * Returns <code>true</code> if case sensitivity for the server and
    * application root is enabled, <code>false</code> otherwise.
    *
    * @return code>true</code> if case sensitivity for the server and
    * application root is enabled, <code>false</code> otherwise.
    */
   public static boolean isCaseSensitiveURL()
   {
      return ms_caseSensitiveUrl;
   }

   /**
    * Returns  BROKEN_MANAGED_LINK_BEHAVIOR
     *
    * @return BROKEN_MANAGED_LINK_BEHAVIOR Options defined in Server.properties
    */
   public static String getBrokenLinkBehavior()
   {
      return ms_serverProps.getProperty(BROKEN_MANAGED_LINK_BEHAVIOR);
   }

   /**
    * Determines if content editors are required to have unique names among all
    * fields and fieldsets. If value is <code>true</code>, then when content
    * editors are initialized, if duplicate names are found, the application
    * should fail to start.  If value is <code>false</code> or the property is
    * not defined, if there are duplicates a warning should be written to the
    * console and the log, but the application should be allowed to initialize.
    *
    * @return <code>true</code> if unique names are required, <code>false</code>
    * otherwise.
    */
   public static boolean requireUniqueFieldNames()
   {
      return ms_requireUniqueFieldNames;
   }

   static void updateServerRequestRoot(PSServerConfiguration cfg)
   {
      String serverRoot = cfg.getRequestRoot();
      if (serverRoot != null)  {
         if (serverRoot.startsWith("/"))
            ms_requestRoot = serverRoot;
         else
            ms_requestRoot = "/" + serverRoot;
      }
   }

   private static void initSecurity()
   {
      PSConsole.printInfoMsg("Server",
         IPSServerErrors.SEC_POOL_INIT, (Object[])null);

      /* initialize server security by:
       *    - initializing the security provider pool
       *    - loading the public roles
       *    - start ACL handler (must be done AFTER loading roles!!!)
       *    - load data encryptor settings
       */
      PSRoleManager.getInstance();

      // this loads security provider instances
      com.percussion.security.PSSecurityProviderPool.init(ms_srvConfig);

      // load the server ACL
      ms_AclHandler = new PSAclHandler(ms_srvConfig.getAcl());
   }

   public static String makeRequestRoot(String appRoot)
   {
      String reqRoot = ms_requestRoot;

      /* add the app request root */
      if ((appRoot != null) && (appRoot.length() > 0)) {
         if (reqRoot == null)
            reqRoot = "/" + appRoot;
         else
            reqRoot += "/" + appRoot;
      }

      return reqRoot;
   }

   public static void startApplication(String appName)
      throws PSNotFoundException, PSServerException, PSSystemValidationException
   {
      startApplication(ms_objectStore.getApplicationObject(appName));
   }

   public static PSApplicationHandler startApplication(PSApplication app)
      throws PSNotFoundException, PSSystemValidationException
   {
      PSValidatorAdapter validateContext = new PSValidatorAdapter(ms_objectStore);
      validateContext.throwOnErrors(true);

      /* If the app is hidden, it will not respond to any type of external
       * requests, will not be listed in the rooted request handlers, and
       * will not print a console message to the console on startup.
       */
      if (!app.isHidden())
      {
         PSConsole.printInfoMsg("Server", IPSServerErrors.APPLICATION_INIT,
            new Object[] { app.getName() } );
      }

      app.validate(validateContext);

      /* create the application handler */
      PSApplicationHandler ah = new PSApplicationHandler(app, ms_objectStore,
         ms_extensionMgr);
      ms_RequestHandlers.put("data-" + app.getName().toLowerCase(), ah);

      if (!app.isHidden())
      {
         ms_requestHandlerTypes.put(ah, getStdAppRequestMethods());

         String appRoot = app.getRequestRoot();
         int msgCode = 0;
         if (appRoot == null) {
            //this is not allowed!!
            msgCode = IPSObjectStoreErrors.APP_ROOT_REQD;
            Object[] args = { "application path not found", "server/startApplication" };
            throw new PSNotFoundException(msgCode, args);
         }

         // check if the application being started is the default application for the server
         if (app.getName().equalsIgnoreCase(ms_defaultAppName)){
            ms_rootedRequestHandlers.put(getRequestRoot(), ah);
            ms_hasDefaultApp = true;
         }

         /* and store the mapping from request root to app */
         String reqRoot = makeRequestRoot(appRoot);
         if (!isCaseSensitiveURL())
            reqRoot = reqRoot.toLowerCase();

         Object o = ms_rootedRequestHandlers.put(reqRoot, ah);
         if (o != null) {
            //warn that we've got one by this name already!!
            PSApplicationHandler appHandler = (PSApplicationHandler)o;
            msgCode = IPSObjectStoreErrors.APP_REQUEST_ROOTS_DUP;
            Object[] args = { appHandler.getName(), appHandler.getRequestRoot() };
            PSLogManager.write(
               new PSLogServerWarning(
               msgCode, args, true, "Server"));
         }

         fireAppHandlerStateChanged(ah, PSHandlerStateEvent.HANDLER_EVENT_STARTED);

         PSConsole.printInfoMsg("Server",
            IPSServerErrors.APPLICATION_INIT_COMPLETED,
            new Object[] { app.getName() } );
      }
      return ah;
   }

   /**
    * Get the status of all request handlers.
    *
    * @param doc the document for which to create the status element, not
    *    <code>null</code>.
    * @param full <code>true</code> to request a full status, <code>false</code>
    *    for a summary status only.
    * @return the element containing all request handlers status
    *    information, never <code>null</code>.
    * @throws IllegalArgumentException if the provided document is
    *    <code>null</code>.
    */
   @SuppressWarnings(value="unchecked")
   public static Element getRequestHandlersStatus(Document doc, boolean full)
   {
      if (doc == null)
         throw new IllegalArgumentException("the document cannot be null");

      Element requestHandlers = doc.createElement("Handlers");
      Iterator<String> rh = ms_RequestHandlers.keySet().iterator();
      while (rh.hasNext())
      {
         Element handler = doc.createElement("Handler");
         String name = rh.next();
         handler.setAttribute("name", name);

         IPSRequestHandler h = ms_RequestHandlers.get(name);
         handler.setAttribute("class", h.getClass().getName());

         List suppTypes = ms_requestHandlerTypes.get(h);
         Element types = doc.createElement("SupportedTypes");
         if (suppTypes != null)
            types.appendChild(
               doc.createTextNode(suppTypes.toString()));
         handler.appendChild(types);

         requestHandlers.appendChild(handler);
      }

      Element rootedHandlers = doc.createElement("RootedHandlers");
      Iterator<String> rrh = ms_rootedRequestHandlers.keySet().iterator();
      while (rrh.hasNext())
      {
         Element handler = doc.createElement("Handler");
         String name = rrh.next();
         handler.setAttribute("name", name);

         IPSRequestHandler h =
            ms_rootedRequestHandlers.get(name);
         handler.setAttribute("class", h.getClass().getName());

         if (full && h instanceof PSApplicationHandler)
         {
            PSApplicationHandler a = (PSApplicationHandler) h;
            Map cache = a.getStylesheetCache();
            if (cache != null)
            {
               Iterator<PSCachedStylesheet> stylesheets =
                  cache.values().iterator();
               while (stylesheets.hasNext())
               {
                  PSCachedStylesheet stylesheet = stylesheets.next();
                  handler.appendChild(
                     stylesheet.getStylesheetStatus(doc));
               }
            }
         }

         List suppTypes = ms_requestHandlerTypes.get(h);
         Element types = doc.createElement("SupportedTypes");
         if (suppTypes != null)
            types.appendChild(
               doc.createTextNode(suppTypes.toString()));
         handler.appendChild(types);

         rootedHandlers.appendChild(handler);
      }

      Element handlers = doc.createElement("RequestHandlers");
      handlers.setAttribute("size",
         new Integer(ms_RequestHandlers.size() +
            ms_rootedRequestHandlers.size()).toString());
      handlers.appendChild(requestHandlers);
      handlers.appendChild(rootedHandlers);

      return handlers;
   }

   /**
    * Initialize the internal and application data request handlers. A data
    * handler is started for each resource in each app in the supplied list.
    * The apps are started in priority order based on each apps start priority
    * property.
    *
    * @param apps All applications that need to be started. If an entry in
    *    the array is <code>null</code> it is skipped. If <code>null</code> is
    *    supplied, no app handlers will be started.
    */
   @SuppressWarnings(value="unchecked")
   private static void initRequestHandlers(PSApplication[] apps)
      throws PSNotFoundException, PSServerException, PSCacheException
   {
      PSConsole.printInfoMsg("Server",
            IPSServerErrors.REQ_HANDLER_INIT, (Object[])null);

      ms_RequestHandlers      = new Hashtable<String,IPSRequestHandler>();
      ms_rootedRequestHandlers      = new Hashtable<String,IPSRequestHandler>();
      ms_requestHandlerTypes = new Hashtable<IPSRequestHandler,List<String>>();
      IPSRequestHandler handler;

      if (ms_srvConfig != null)
         ms_defaultAppName = ms_srvConfig.getDefaultApplication();

      /* add the catalog request handlers */
      handler =
         new com.percussion.design.catalog.data.server.PSDataCatalogHandler();
      ms_RequestHandlers.put("design-catalog-data", handler);
      ms_requestHandlerTypes.put(handler, getStdHandlerRequestMethods());

      handler =
         new com.percussion.design.catalog.exit.server.PSExitCatalogHandler(
            ms_extensionMgr);
      ms_RequestHandlers.put("design-catalog-exit", handler);
      ms_requestHandlerTypes.put(handler, getStdHandlerRequestMethods());

      handler = new
         com.percussion.design.catalog.file.server.PSFileCatalogHandler();
      ms_RequestHandlers.put("design-catalog-file", handler);
      ms_requestHandlerTypes.put(handler, getStdHandlerRequestMethods());
   
      handler = new
         com.percussion.design.catalog.function.server.
         PSFunctionCatalogHandler();
      ms_RequestHandlers.put("design-catalog-function", handler);
      ms_requestHandlerTypes.put(handler, getStdHandlerRequestMethods());

      handler = new
         com.percussion.design.catalog.macro.server.PSCatalogHandler();
      ms_RequestHandlers.put("design-catalog-macro", handler);
      ms_requestHandlerTypes.put(handler, getStdHandlerRequestMethods());

      handler =
         new com.percussion.design.catalog.mail.server.PSMailCatalogHandler();
      ms_RequestHandlers.put("design-catalog-mail", handler);
      ms_requestHandlerTypes.put(handler, getStdHandlerRequestMethods());

      handler = new
         com.percussion.design.catalog.security.server.
         PSSecurityCatalogHandler();
      ms_RequestHandlers.put("design-catalog-security", handler);
      ms_requestHandlerTypes.put(handler, getStdHandlerRequestMethods());

      handler = new
         com.percussion.design.catalog.system.server.PSSystemCatalogHandler();
      ms_RequestHandlers.put("design-catalog-system", handler);
      ms_requestHandlerTypes.put(handler, getStdHandlerRequestMethods());
      
      handler =
         new com.percussion.design.catalog.xml.server.PSXmlCatalogHandler();
      ms_RequestHandlers.put("design-catalog-xml", handler);
      ms_requestHandlerTypes.put(handler, getStdHandlerRequestMethods());

      /* add the admin request (remote console) handler */
      handler = new PSRemoteConsoleHandler();
      ms_RequestHandlers.put("admin", handler);
      ms_requestHandlerTypes.put(handler, getStdHandlerRequestMethods());

      /* add the hook request handler */
      handler = new PSHookRequestHandler();
      ms_RequestHandlers.put("hook-*", handler);
      ms_requestHandlerTypes.put(handler,
         PSHookRequestHandler.getStdHookRequestMethods());

      /* add the object store request handlers */
      ms_RequestHandlers.put("design-objectstore-*", ms_objectStore);
      ms_requestHandlerTypes.put(ms_objectStore, getStdHandlerRequestMethods());

      /* add the login/logout handlers */
      PSDesignerConnectionHandler hDesCon = new PSDesignerConnectionHandler();
      ms_RequestHandlers.put("design-open", hDesCon);
      ms_RequestHandlers.put("design-close", hDesCon);
      ms_requestHandlerTypes.put(hDesCon, getStdHandlerRequestMethods());
      
      setEstimatedStatistics();
      
      /* init and add the loadable handlers */
      initLoadableRequestHandlers();

      // create the cache manager
      ms_cacheManager = PSCacheManager.getInstance();
      ms_cacheManager.init(getServerConfiguration().getServerCacheSettings());

      ms_handlerInitListeners.add(ms_cacheManager);

      // create the search index queue
      ms_searchIndexQueue = PSSearchIndexEventQueue.getInstance();
      ms_handlerInitListeners.add(ms_searchIndexQueue);

      /* now do all the application handlers */
      if (apps != null) {
         /* sort apps in start priorty order, then start each one */
         Comparator<PSApplication> c = new Comparator<PSApplication>() {
            /* This comparison is inconsistent with equals. It orders the
               list in order from highest startPriority to lowest. */
            public int compare(PSApplication x, PSApplication y)
            {
               int xPri = x.getStartPriority();
               int yPri = y.getStartPriority();
               return xPri < yPri ? 1 : (xPri > yPri ? -1 : 0);
            }
         };
         List<PSApplication> sortedApps = Arrays.asList(apps);
         Collections.sort(sortedApps, c);
         Iterator<PSApplication> iter = sortedApps.iterator();
         while (iter.hasNext())
         {
            PSApplication app = iter.next();
            try
            {
               startApplication(app);
            }
            catch (PSSystemValidationException e)
            {
               PSConsole.printMsg("Server",
                  e.getErrorCode(), e.getErrorArguments());

               int appId = 0;
               if(app!=null)
                  appId = app.getId();
               
               PSServerLogHandler.handleValidationError(appId, e);
            }
         }
      }

      // now that all the app have started, start the cache
       // do package install first.
      //ms_cacheManager.start();

      ms_WhatsUp |= INITED_REQ_HANDLERS;
   }

   /**
    * Sets the estimate (table) statistics, which is specified by the the
    * server property {@link #PROP_ESTIMATE_STATS}. 
    * The value of the property is consisted of a set of table-name and row 
    * pairs with ';' (semicolon) delimiters between the pairs. Each pair is 
    * separated with a ',' (comma).
    *  
    * For example:   
    *    RXPUBDOCS,1000000;RXRELATEDCONTENT,1000000
    */
   private static void setEstimatedStatistics()
   {
      Log log = LogFactory.getLog(PSServer.class);
      String estStats = (String)ms_serverProps.get(PROP_ESTIMATE_STATS);
      log.debug("Server property '" + PROP_ESTIMATE_STATS + "' =" + estStats);
      if (estStats == null || estStats.trim().length() == 0)
         return;
      
      Map<String,Integer> estimateStats = new HashMap<String,Integer>();
      StringTokenizer st = new StringTokenizer(estStats, ";");
      Integer rows;
      // process each pair and ignore bad (unexpected) data
      while (st.hasMoreTokens()) 
      {
         String pairString = st.nextToken();
         String[] pair = pairString.split(",");
         if (pair.length == 2)
         {
            try
            {
               rows = new Integer(pair[1]);
            }
            catch (NumberFormatException e)
            {
               // ignore bad data
               log.warn("Discard bad Estimate Statistics: '" + pairString + "'");
               continue;
            }
            log.debug("add Estimate Statistics: (" + pair[0] + "," + rows + ")");
            estimateStats.put(pair[0], rows);
         }
         else
         {
            // ignore bad data
            log.warn("Discard bad Estimate Statistics: '" + pairString + "'");
         }
      }
      
      PSTableMetaData.setEstimateStatistics(estimateStats);
   }
   
   /**
    * Initializes all loadable request handlers, adding them to the request
    * handler map, and their request roots to the rooted handler map. Constructs
    * the loadable handler using the signature (IPSObjectStoreHandler,
    * IPSExtensionManager) if such a ctor is available.  Otherwise, constructs
    * with an empty ctor.
    *
    * @throws PSServerException for any errors.
    */
   @SuppressWarnings(value="unchecked")
   private static void initLoadableRequestHandlers() throws PSServerException
   {
      PSRequestHandlerConfiguration handlerConfig =
         new PSRequestHandlerConfiguration();
      Iterator<PSRequestHandlerDef> defs = handlerConfig.getHandlerDefs();
      while (defs.hasNext())
      {
         // create the handler
         PSRequestHandlerDef def = defs.next();
         IPSLoadableRequestHandler loadableHandler = null;
         try
         {
            /* Use a constructor with the signature (IPSObjectStoreHandler,
               IPSExtensionManager) if one exists.  Otherwise, use the empty
               contructor */
            Class handlerClass = Class.forName( def.getClassName() );
            try
            {
               Constructor handlerCtor = handlerClass.getConstructor( new Class[]
                  { IPSObjectStoreHandler.class, IPSExtensionManager.class }
               );
               loadableHandler = (IPSLoadableRequestHandler)
                  handlerCtor.newInstance( new Object[] {
                     ms_objectStore, ms_extensionMgr } );
            } catch (NoSuchMethodException e)
            {
               // that's ok, the parametered ctor is optional
               loadableHandler = (IPSLoadableRequestHandler)
                  handlerClass.newInstance();
            }
         }
         catch (Throwable e)
         {
            // catch any error here and rethrow
            Object[] args = {def.getClassName(), e.toString()};
            throw new PSServerException(
               IPSServerErrors.LOADABLE_REQUEST_HANDLER_CREATE_ERROR, args);
         }

         /* Add to master request handler list, using handler name, and
          * extdata as the root
          */
         ms_RequestHandlers.put("extdata-" + def.getHandlerName(),
            loadableHandler);

         // add each request root to the rooted handler list
         Iterator<String> roots = def.getRequestRoots();
         List<String> requestRoots = new ArrayList<String>();
         while (roots.hasNext())
         {
            String rootName = roots.next();
            String requestRoot = rootName;
            // add the server to the root
            requestRoot = makeRequestRoot(requestRoot);
            // handle case insensitivity
            if (!isCaseSensitiveURL())
               requestRoot = requestRoot.toLowerCase();

            requestRoots.add(requestRoot);

            /* Add it to the rooted handler list so requests can be identified
             * by the request root
             */
            ms_rootedRequestHandlers.put(requestRoot, loadableHandler);

            // for each root, add it's allowed reqeust methods
            Iterator<String> methods = def.getRequestMethods(rootName);
            while (methods.hasNext())
            {
               addRequestHandlerType(loadableHandler, methods.next());
            }
         }

         // initialize the handler
         File cfgFile = def.getConfigFile();
         FileInputStream in = null;
         if (cfgFile != null)
         {
            try
            {
               in = new FileInputStream(cfgFile);
            }
            catch(FileNotFoundException e)
            {
               throw new PSServerException(
                  IPSServerErrors.REQUEST_HANDLER_CONFIG_NOT_FOUND,
                     cfgFile.getPath());
            }
         }
         loadableHandler.init(requestRoots, in);
      }
   }

   /**
    * Get all defined macros for this server.
    *
    * @return the macro set defined in this server, never <code>null</code>,
    *    may be empty.
    */
   public static PSMacroDefinitionSet getMacros()
   {
      return ms_macros;
   }

   /**
    * Load all defind macro definitions from the server configuration directory.
    *
    * @throws IOException for file IO errors from the macro definition file.
    * @throws PSUnknownNodeTypeException for invalid XML macro definitions.
    * @throws SAXException for XML parsing exceptions.
    */
   @SuppressWarnings(value="unchecked")
   private static void initMacros() throws IOException,
      PSUnknownNodeTypeException, SAXException
   {
      PSConsole.printInfoMsg("Server",
         IPSServerErrors.MACROS_INIT, (Object[]) null);

      try
      {
         File systemMacrosFile = new File(getRxFile(SERVER_DIR) + "/sys_macros.xml");
         Document doc = PSXmlDocumentBuilder.createXmlDocument(
            new FileInputStream(systemMacrosFile), false);
         Element root = doc.getDocumentElement();

         PSMacroDefinitionSet systemMacros = new PSMacroDefinitionSet();
         if (root != null)
            systemMacros = new PSMacroDefinitionSet(root);

         PSMacroDefinitionSet userMacros = new PSMacroDefinitionSet();
         File userMacrosFile = new File(getRxFile(SERVER_DIR) + "/rx_macros.xml");
         if (userMacrosFile.exists())
         {
            try
            {
               doc = PSXmlDocumentBuilder.createXmlDocument(
                  new FileInputStream(userMacrosFile), false);
               root = doc.getDocumentElement();
               if (root != null)
                  userMacros = new PSMacroDefinitionSet(doc.getDocumentElement());
            }
            catch (SAXParseException e)
            {
               Object[] args =
               {
                  userMacrosFile.toString(),
                  e.getLocalizedMessage(),
               };
               PSConsole.printMsg("Server",
                  IPSServerErrors.INVALID_USER_MACROS, args);
            }
         }

         ms_macros = new PSMacroDefinitionSet();
         ms_macros.addAll(systemMacros);

         for (int i=0; i<userMacros.size(); i++)
         {
            PSMacroDefinition userMacro = (PSMacroDefinition) userMacros.get(i);
            if (systemMacros.getMacroDefinition(userMacro.getName()) == null)
               ms_macros.add(userMacro);
            else
               ms_macros.set(i, userMacro);
         }
      }
      catch (FileNotFoundException e)
      {
         // no macros specified
         ms_macros = new PSMacroDefinitionSet();
      }
   }

   /**
    * Initialize the server's log handler.
    * @return <code>true</code> if it is successfully initialized, 
    * <code>false</code> otherwise.
    */
   @SuppressWarnings(value="unchecked")
   private static boolean initLogHandling()
   {
      PSConsole.printInfoMsg("Server", IPSServerErrors.LOG_MGR_INIT,
         (Object[]) null);

      boolean didInit = false;
      try
      { 
         /* all logging exceptions are non-recoverable! */
         PSLogManager.init(ms_serverProps);
         didInit = true;
      }
      catch (IllegalArgumentException e)
      { /* bad config file */
         Object[] params =
         {e.getMessage()};
         PSServerLogHandler.handleTerminalError(
            IPSServerErrors.LOG_INIT_BAD_CONFIG, params);
      }
      catch (IOException e)
      { /* file logging problem */
         Object[] params =
         {e.getMessage()};
         PSServerLogHandler.handleTerminalError(
            IPSServerErrors.LOG_INIT_FILE_ERROR, params);
      }
      catch (java.sql.SQLException e)
      { /* SQL logging problem */
         Object[] params =
         {e.getMessage()};
         PSServerLogHandler.handleTerminalError(
            IPSServerErrors.LOG_INIT_SQL_ERROR, params);
      }
      catch (ClassNotFoundException e)
      { /* JDBC driver not found */
         Object[] params =
         {e.getMessage()};
         PSServerLogHandler.handleTerminalError(
            IPSServerErrors.LOG_INIT_DRIVER_ERROR, params);
      }
      catch (SAXException e)
      { /* JDBC driver not found */
         Object[] params =
         {e.getMessage()};
         PSServerLogHandler.handleTerminalError(
            IPSServerErrors.LOG_INIT_XML_FILE_ERROR, params);
      }
      catch (NamingException e)
      { /* bad configuration? */
         Object[] params =
         {e.getMessage()};
         PSServerLogHandler.handleTerminalError(
            IPSServerErrors.LOG_INIT_BAD_CONFIG, params);
      }
      
      if (!didInit)
         return false;
      
      /* if the object store's not up yet, we create a default log handler.
       * once the object store is activated, a real log handler will be
       * created as well.
       */
      PSLogger logger = null;
      if (ms_srvConfig != null)  // use the server's logger if available
         logger = ms_srvConfig.getLogger();

      if (logger == null) {
         /* The object store must not be up yet.
          * We'll enable all logging by default.
          */
         logger = new PSLogger();

         /* we're defaulting to everything on */
         logger.setErrorLoggingEnabled(true);
         logger.setServerStartStopLoggingEnabled(true);
         logger.setAppStartStopLoggingEnabled(true);
         logger.setAppStatisticsLoggingEnabled(true);
         logger.setBasicUserActivityLoggingEnabled(true);
         logger.setDetailedUserActivityLoggingEnabled(true);
         logger.setMultipleHandlerLoggingEnabled(true);
      }

      ms_serverLogHandler = new PSLogHandler(logger);
      PSDebugManager.getDebugManager();
      ms_WhatsUp |= INITED_LOG;
      
      return true;
   }
   
   private static void initErrorHandling() {
      PSConsole.printInfoMsg("Server",
            IPSServerErrors.ERROR_MGR_INIT, (Object[])null);

      PSErrorManager.init();

      /* we'll use default error page and notifier for the error handler
       * constructor until we start the object store. initObjectStore
       * will then create the real object.
       */
      com.percussion.util.PSMapClassToObject errorPages =
         new com.percussion.util.PSMapClassToObject();
      PSNotifier notifier = null;

      ms_serverErrorHandler = new PSErrorHandler(errorPages, true, notifier);

      ms_WhatsUp |= INITED_ERROR;
   }

   /**
    * Get the first bind address. The bind address is the value of
    * "bindAddress" property, which is defined in server.properties.
    *
    * @return The retrieved value. It may be <code>null</code> if the value of
    *    "bindAddress" property is empty or undefined.
    */
   private static String getFirstBindAddress()
   {
      String addresses = (String)ms_serverProps.get(PROP_REQLISTENER_HOST);

      if (addresses == null || addresses.trim().length() == 0)
      {
         return null;
      }
      else
      {
         StringTokenizer tok = new StringTokenizer(addresses, ";");
         String address = null;
         if (tok.hasMoreTokens())
            address = tok.nextToken().trim();

         if (address == null || address.trim().length() == 0)
            return null;
         else
            return address;
      }
   }

   private static boolean isInited(int initType) {
      return ((ms_WhatsUp & initType) == initType);
   }

   /**
    * Determines whether the server has completed initialization
    *
    * @return <code>true</code> if the server has completed initialization;
    *    <code>false</code> otherwise.
    */
   public static boolean isInitialized()
   {
      return isInited(INITED_ALL);
   }


   /**
    * Shutdown the specified application.
    *
    * @param   appName     the name of the app to shut down
    *
    * @return              <code>true</code> if the app was located and shut
    *                      down; <code>false</code> otherwise
    */
   public static boolean shutdownApplication(String appName)
   {
      String appKey = "data-" + appName.toLowerCase();

      // if this app exists, it must be in the master hash
      PSApplicationHandler rh =
         (PSApplicationHandler)ms_RequestHandlers.get(appKey);
      if (rh == null)
         return false;

      // we can shut the handler down now
      rh.shutdown();

      // check if the application being shut down is the default application for the server
      if (appName.equalsIgnoreCase(ms_defaultAppName))
         ms_rootedRequestHandlers.remove(getRequestRoot());

      // delete this from the request handler hash (key = data-<appname>)
      if (ms_RequestHandlers != null)
         ms_RequestHandlers.remove(appKey);

      // and from the rooted apps (we only support rooted apps at this time)
      String fullReqRoot = rh.getFullRequestRoot();
      if (!isCaseSensitiveURL())
         fullReqRoot = fullReqRoot.toLowerCase();

      ms_rootedRequestHandlers.remove(fullReqRoot);

      // remove from request types
      ms_requestHandlerTypes.remove(rh);

      fireAppHandlerStateChanged(rh, PSHandlerStateEvent.HANDLER_EVENT_STOPPED);

      return true;
   }

   /**
    * Register handler state listener with the server.
    * @param listener handler state event lister to register with the server,
    * must not be <code>null</code>.
    * @param handlerName unique name of the handler, must not be
    * <code>null</code> or empty. If a handler with this name already
    * registered previously, the state events will be merged or ORed.
    * @param events One or more state events {@link PSHandlerStateEvent#
    * HANDLER_EVENT_STARTED HANDLER_EVENT_XXX} ORed together. No validation
    * is done on the event flags.
    * @see IPSHandlerStateListener
    * @see PSHandlerStateEvent
    */
   @SuppressWarnings(value="unchecked")
   static public void addHandlerStateListener(
      IPSHandlerStateListener listener,
      String handlerName,
      int events)
   {
      if (listener == null)
      {
         throw new IllegalArgumentException("listener must not be null");
      }
      if (handlerName == null || handlerName.length()<1)
      {
         throw new IllegalArgumentException(
            "handlerName must not be null or empty");
      }

      Map listenerEventMap = (Map) ms_handlerStateListenerMap.get(handlerName);
      if (listenerEventMap == null)
         listenerEventMap = new HashMap();

      int flags = events;
      Integer eventsObj = (Integer) listenerEventMap.get(listener);
      if (eventsObj != null)
         flags = eventsObj.intValue() | events;
      listenerEventMap.put(listener, new Integer(flags));

      ms_handlerStateListenerMap.put(handlerName, listenerEventMap);
   }

   /**
    * Fire the state changed event for all listeners registered  for the event.
    * @param ah handler whose state has changed and to be notified to the
    * listeners, never <code>null</code>. The call is synchronous and hence
    * it is the listener's responsibilty to deal with any processes that take
    * long time to finish.
    * @param stateEvent a valid state event, one of the
    * {@link PSHandlerStateEvent#HANDLER_EVENT_STARTED HANDLER_EVENT_XXX}
    * flags.
    */
   @SuppressWarnings(value="unchecked")
   static private void fireAppHandlerStateChanged(
      PSApplicationHandler ah,
      int stateEvent)
   {
      String handlerName = ah.getName();
      if (!ms_handlerStateListenerMap.containsKey(handlerName))
      {
         //No listeners have been registered for the application
         return;
      }
      Map listenerEvents = (Map) ms_handlerStateListenerMap.get(handlerName);
      //Get iterator of all registered listeners for the application handler
      Iterator listeners = listenerEvents.keySet().iterator();
      while (listeners.hasNext())
      {
         IPSHandlerStateListener listener =
            (IPSHandlerStateListener) listeners.next();
         Integer eventsObj = (Integer) listenerEvents.get(listener);
         int flags = eventsObj.intValue();
         if (stateEvent != (flags & stateEvent))
            continue;
         //Notify the listener
         listener.stateChanged(
            new PSHandlerStateEvent(handlerName, stateEvent));
      }
   }

   private static void shutdownRequestHandlers()
   {
      // we're not synchronizing on this as the shutdown method is the
      // only one that calls us, and it guarantees no overlap

      /* now go through the handlers and shut each one down.  May be dupes in
       * in this list, so keep track of who's been shut down so we don't call
       * shutdown more than once.  Also create and loop through a local copy
       * of the handlers as each shutdown may modify the actual table which
       * could result in a failure (CML-3019).
       */
      if (ms_RequestHandlers != null) {
         Set<IPSRequestHandler> stoppedHandlers =
            new HashSet<IPSRequestHandler>();
         Hashtable<String, IPSRequestHandler> reqHandlers = 
            new Hashtable<String, IPSRequestHandler>();
         reqHandlers.putAll(ms_RequestHandlers);
         for (Enumeration<IPSRequestHandler> e = reqHandlers.elements();
            e.hasMoreElements(); )
         {
            IPSRequestHandler rh = e.nextElement();
            if (!stoppedHandlers.contains(rh))
            {
               rh.shutdown();
               stoppedHandlers.add(rh);
            }
         }
         ms_RequestHandlers = null;
         stoppedHandlers = null;
      }

      ms_cacheManager.shutdown();

      // no longer need this
      ms_rootedRequestHandlers = null;

      // no longer need this
      ms_requestHandlerTypes = null;

      // no longer need this
      ms_handlerInitListeners = null;
   }

   /**
    * Get a collection of the rooted app handlers
    *
    */
   static Collection getRootedAppHandlers()
   {
      return ms_rootedRequestHandlers.values();
   }

   /**
    * Get the list of listeners to be notified of request handler
    * initialization.
    *
    * @return An iterator over zero or more {@link IPSHandlerInitListener}
    * objects, never <code>null</code>;
    */
   static Iterator getHandlerInitListeners()
   {
      return ms_handlerInitListeners.iterator();
   }

   /**
    * Add a listener to the list. These listeners are called whenever there
    * is a change to the applications. Such listeners can then add further
    * listeners to the specific applications being started or shutdown.
    *
    * @param listener the listener, never <code>null</code>
    */
   public static void addInitListener(IPSHandlerInitListener listener)
   {
      if (listener == null)
      {
         throw new IllegalArgumentException("listener may not be null");
      }
      ms_handlerInitListeners.add(listener);
   }


   /**
    * Notifies all registered {@link IPSHandlerInitListener} of an init event
    * passing the supplied request handler
    *
    * @param rh The request handler being initialized, may not be
    * <code>null</code>.
    */
   @SuppressWarnings(value="unchecked")
   public static void notifyHandlerInitListeners(IPSRequestHandler rh)
   {
      if (rh == null)
         throw new IllegalArgumentException("rh may not be null");

      Iterator<IPSHandlerInitListener> listeners =
         PSServer.getHandlerInitListeners();
      while (listeners.hasNext())
      {
         IPSHandlerInitListener listener = listeners.next();
         listener.initHandler(rh);
      }
   }

   /**
    * Notifies all registered {@link IPSHandlerInitListener} of a shutdown event
    * passing the supplied request handler
    *
    * @param rh The request handler being shutdown, may not be
    * <code>null</code>.
    */
   @SuppressWarnings(value="unchecked")
   public static void notifyHandlerShutdownListeners(IPSRequestHandler rh)
   {
      if (rh == null)
         throw new IllegalArgumentException("rh may not be null");

      Iterator<IPSHandlerInitListener> listeners =
         PSServer.getHandlerInitListeners();
      while (listeners.hasNext())
      {
         IPSHandlerInitListener listener = listeners.next();
         listener.shutdownHandler(rh);
      }
   }

   /**
    * Query if the server is shutting down. 
    * @return <code>true</code> if the server is shutting down
    */
   public static boolean isShuttingDown()
   {
      return ms_shuttingDown;
   }
   
   /**
    * Shut down the server.  This method should only be called from the destroy
    * method of the servlet.  Use {@link #scheduleShutdown(long)} if you need
    * to shut the server down from elsewhere in the server code.
    */
   public static void shutdown()
   {
      synchronized (PSServer.class)
      {
         if (ms_shuttingDown) /* are we already in the shutdown?! */
         {
            return;
         }
         ms_shuttingDown = true;
      }
      
      // stop if never initialized at all
      if (ms_WhatsUp == INITED_NONE)
         return;

      /*
       * Remove expired design object locks. This may only be done on a normal
       * server shutdown to make sure the no expired locks are removed after
       * a server crash.
       */
      IPSObjectLockService service = 
         PSObjectLockServiceLocator.getLockingService();
      List<PSObjectLock> expiredLocks = service.findExpiredLocks();
      if (expiredLocks != null && !expiredLocks.isEmpty())
         service.releaseLocks(expiredLocks);
      
      PSConsole.printInfoMsg("Server",
         IPSServerErrors.SERVER_TERM_START, null, Level.OFF);

      // shutdown the error page cache
      PSPageCache.shutdown();

      /* shut down the db pool */
      if (isInited(INITED_DB_POOL)) {
         PSConsole.printInfoMsg("Server",
            IPSServerErrors.DB_POOL_TERM, (Object[])null);

         ms_WhatsUp ^= INITED_DB_POOL;
      }

      /* TODO: shut down the object store */
      // if (ms_WhatsUp & INITED_OBJECT_STORE)

      /*
       * shut down search before request handlers as search may be using
       * applications
       */
      try
      {
         // TODO - ignore for now
         shutdownQueues();
         ms_delayedInitExecutor.shutdownNow();
      }
      catch (Throwable e)
      {
         Object[] args = { e.getLocalizedMessage() };
         PSServerLogHandler.logMessage(new PSInternalError(
            IPSServerErrors.EXCEPTION_NOT_CAUGHT, args));
      }

      // Prevent update notifications during shutdown
      PSItemDefManager itemDefManager = PSItemDefManager.getInstance();
      itemDefManager.clearListeners();

      /* shut down the request handlers */
      if (isInited(INITED_REQ_HANDLERS)) {
         PSConsole.printInfoMsg("Server",
            IPSServerErrors.REQ_HANDLER_TERM, (Object[])null);

         try { /* catch all exceptions so we can continue processing */
            shutdownRequestHandlers();
         } catch (Throwable t) {
            Object[] args = { t.toString() };
            PSServerLogHandler.logMessage(new PSInternalError(
               IPSServerErrors.REQ_HANDLER_TERM_EXCEPTION, args));
         }

         ms_WhatsUp ^= INITED_REQ_HANDLERS;
      }

      // shut down the extension manager
      if (isInited(INITED_EXTENSION))
      {
         try
         {
            ms_extensionMgr.shutdown();
         }
         catch (Throwable t)
         {
            Object[] args = { t.toString() };
            PSServerLogHandler.logMessage(new PSInternalError(
               IPSServerErrors.EXTENSION_MGR_TERM_EXCEPTION, args));
         }

         ms_WhatsUp ^= INITED_REQ_HANDLERS;
      }

      //Shutdown the session manager before the tmx since uses it the tmx bundle.
      PSUserSessionManager.shutdown();
      //Make sure to call terminte() of the TMS resource object.
      PSTmxResourceBundle.getInstance().terminate();

      /* shut down the log/error facilities */
      if (isInited(INITED_ERROR)) {
         PSConsole.printInfoMsg("Server",
            IPSServerErrors.ERROR_MGR_TERM, (Object[])null);

         try
         { /* catch all exceptions so we can continue processing */
            PSErrorManager.close();
         } catch (Throwable t) {
            Object[] args = { t.toString() };
            PSServerLogHandler.logMessage(new PSInternalError(
               IPSServerErrors.ERROR_MGR_TERM_EXCEPTION, args));
         }

         try
         {
            ms_serverErrorHandler.shutdown();
         } catch (Throwable e)
         {
            /* shutdown throws no errors; this block ignores runtime errors
               so we can continue the shutdown process */
         }

         ms_WhatsUp ^= INITED_ERROR;
      }


       //ShutDown Derby
       InstallUtil.shutDownDerby();

      if (isInited(INITED_LOG)) {
         /* log the server's shutdown before we kill the log */
         PSLogServerStop msg = new PSLogServerStop(ms_serverName);
         ms_serverLogHandler.write(msg);

         PSConsole.printInfoMsg("Server", IPSServerErrors.LOG_MGR_TERM, null);

         try { /* catch all exceptions so we can continue processing */
            PSLogManager.close();
         } catch (Throwable t) {
            Object[] args = { t.toString() };
            PSConsole.printMsg("Server", IPSServerErrors.LOG_MGR_TERM_EXCEPTION,
                                                                         args);
         }

         ms_WhatsUp ^= INITED_LOG;
      }
      
      // Destroy spring configuration
      PSBaseServiceLocator.destroy();

      // Destroy any server lockfile
      destroyStartupFileLock(ms_serverStartupLock);

      
      PSConsole.printInfoMsg("Server",
         IPSServerErrors.SERVER_TERM_END, null, Level.OFF);
   }

   /**
     * Inform user of a terminal error and that the server is shutting down.
     *
     * @param waitIndefinitely If <code>true</code>, pause the system and then
     * wait forever.  If <code>false</code> and server has not completed
     * intializing, give the user 10 seconds to press [Enter] and cause the
     * system to pause until they press [Enter] again. Should return after 10
     * seconds if there is no user input possible.  If <code>false</code> and
     * system has completed initializing, then this method will simply return.
     */
   private static void pauseOnExitForUser(boolean waitIndefinitely)
   {
      if (ms_noPauseOnExit && !waitIndefinitely)
      {
         return;
      }
      else
      {
         // need to set this to true in case we throw an exception from in this
         // method and then come back since the <code>main</code> method calls
         // this in its finally block so we don't try to pause again.
         ms_noPauseOnExit = true;
      }

      // only prompt user if not waiting indefinitely.
      if (!waitIndefinitely)
      {
         PSConsole.printMsg("Server", "Fatal server error - shutting down",
            new String[] {"Press [Enter] in the next 10 seconds to pause -"});


         /* Wait a total of 10 seconds to allow user to respond.
          * Check for a response every .5 seconds
          * If they respond, issue a message and wait for them to Press [Enter].
          * Otherwise return. We use this 2 step process to avoid waiting
          * forever in case the user does not have access to the input, but
          * the input stream is not null (this is the case with the Rhythmyx
          * Daemon at the point of this implementation.)
          */
         int totalWaitTimeMillis = 10000;
         int pollingIntervalMillis = 500;

         try
         {
            for (int elapsedTime = 0;
                 elapsedTime <= totalWaitTimeMillis;
                 elapsedTime += pollingIntervalMillis )
            {
            // getUserInput(true) means don't wait for user response
            // getUserInput(false) means wait for user response
               if (null != PSConsole.getUserInput(true))
               {
                  PSConsole.printMsg("Server", "Waiting for your input",
                     new String[] {"Press [Enter] to continue -"});
                  PSConsole.getUserInput(false);
                  return;
               }

               Thread.sleep(pollingIntervalMillis);
            }
         }
         catch (InterruptedException inte)
         {
            return;
         }
         catch (Throwable evt)
         {
            return;
         }
         PSConsole.printMsg("Server", "No user response - Exiting");
      }
      else if (!ms_noInputConsole)  // make sure we can get console input
      {
         // just wait till we get a response
         PSConsole.printMsg("Server", "Waiting for your input",
            new String[] {"Press [Enter] to continue -"});
         PSConsole.getUserInput(false);
      }

   }

   /**
    * Get the ODBC_HOME variable for DSN resolution.
    *
    * @return  the ODBC_HOME property value, or <code>null</code>
    *          if none has been specified or server properties is absent.
    */
   public static String getOdbcIniFile()
   {
      if (ms_serverProps == null)
         return null;
      else
         return ms_serverProps.getProperty("ODBCINI");
   }

   /**
    * Get the server properties.
    *
    * @return  the server properties as java <code>Properties</code>
    * Object loaded during server initialization, may be <code>null</code>
    */
   public static Properties getServerProps()
   {
         return ms_serverProps;
   }
   
   /**
    * Gets the specified server property.
    * @param key the key of the property, not <code>null</code>.
    * @return the value of the property, it may be <code>null</code> if not exist.
    */
   public static String getProperty(String key)
   {
      notNull(key);
      
      if (ms_serverProps == null)
         return null;
      
      return ms_serverProps.getProperty(key);
   }

   public static String getProperty(String key, String defaultValue)
   {
      notNull(key);
      if (ms_serverProps == null)
         return null;
      return ms_serverProps.getProperty(key) == null ? defaultValue : ms_serverProps.getProperty(key);
   }

   public static String getProxyURL(HttpServletRequest httpReq, boolean withoutParam) {
      try{
         String oldPath = httpReq.getRequestURL().toString();
         int port  = Integer.valueOf(PSServer.getProperty("proxyPort",""+getListenerPort()));
         String scheme = PSServer.getProperty("proxyScheme", "http");
         String domainName = PSServer.getProperty("publicCmsHostname", "localhost");
         oldPath += httpReq.getQueryString() == null? "" : "?" + httpReq.getQueryString();
         URL oldUrl = new URL(oldPath);
         if( domainName.equalsIgnoreCase(oldUrl.getHost()) ){
            if(withoutParam){
               String proxyURL= "";
               if(port==443 || port ==80){
                  proxyURL = proxyURL+scheme+"://"+domainName;
               }else{
                  proxyURL = proxyURL+scheme+"://"+domainName+":"+port;
               }
               return proxyURL;
            }else{
               URL newUrl = new URL(scheme, domainName, (port == 80 || port == 443) ? -1 :port , oldUrl.getFile());
               return newUrl.toExternalForm();
            }

         }
      }catch (MalformedURLException ex){
         return "";
      }
      return "";
   }


   
   /**
    * Get the default server character set to use when dealing with
    * escaped values sent in an HttpRequest.  When this is set in the
    * server's properties file, it is recommended to use the IANA
    * mime-preferred name for the character set or the Java canonical name
    * (preferred).
    *
    * @return The default server character set, never <code>null</code>
    * or empty.  Returns {@link PSCharSets#rxJavaEnc()} if no entry exists in
    * server.properties. This is the default character set for the Rhythmyx
    * server.
    */
   public static String getDefaultServerHttpCharset()
   {
      String charset = null;

      if (ms_serverProps != null)
         charset = ms_serverProps.getProperty(SERVER_DEFAULT_HTTP_CHARSET);

      if ((charset == null) || (charset.length() == 0))
      {
         return PSCharSets.rxJavaEnc();
      }

      return charset;
   }

   /**
    * Get the default server character set to use when dealing with
    * uploaded files.  When this is set in the server's properties file, it is
    * recommended to use the IANA mime-preferred name for the character set or
    * the Java canonical name (preferred).
    *
    * @return The default server character set, if <code>null</code>,
    * it indicates to use the default methods when handling file streams
    * (the methods which take no character set encoding names).  Never empty.
    */
   public static String getDefaultServerFileCharset()
   {
      String charset = null;

      if (ms_serverProps != null)
         charset = ms_serverProps.getProperty(SERVER_DEFAULT_FILE_CHARSET);

      if ((charset == null) || (charset.length() == 0))
      {
         /* The default is the system file encoding default */
         return null;
      }

      return charset;
   }

   /**
    * Returns the listener port that the server is listening for requests
    * on.
    *
    * @return  The port we are listening on (for example: 8888)
    */
   static public int getListenerPort()
   {
      return ms_listenerPort;
   }

   public static int getListenerPort(HttpServletRequest request){
       int ret = ms_listenerPort;
       if(request.getServerPort()!=ms_listenerPort){
           ret = request.getServerPort();
       }
       return ret;
   }

   /**
    * Returns the SSL listener port that the server is listening for secure
    * requests on.
    *
    * @return the SSL port the server is listening for secure requests, 0 if
    *    SSL is not enabled.
    */
   static public int getSslListenerPort()
   {
      return ms_sslListenerPort;
   }

    /***
     * Returns the SSL listener port but takes into account
     * requests made by a reverse proxy server.
     * @param request The servlet request
     * @return
     */
   public static int getSslListenerPort(HttpServletRequest request){

       int ret = ms_sslListenerPort;

       if(request.getServerPort()!=ret){
           ret = request.getServerPort();
       }
       return ret;
   }

   /**
    * Returns the host address of this machine. This is determined by
    * calling <code>InetAddress.getLocalHost().getHostAddress()</code> when
    * the server starts up.
    * <p>
    * However, the bind address will be returned if there is one defined in the
    * "bindAddress" property.
    *
    * @return The first address that is defined in the "bindAddress" property
    *    if the value of the property is not empty; otherwise it is the address
    *    of the machine in a %d.%d.%d.%d format, <code>127.0.0.1</code> if the
    *    IP address cannot be determined. Never <code>null</code> or empty.
    */
   static public String getHostAddress()
   {
      return ms_hostAddress;
   }

   /**
    * Returns the host name of this machine.  This is determined by
    * calling <code>InetAddress.getLocalHost().getHostName()</code> when
    * the server starts up.
    *
    * @return the returned value from
    *    <code>InetAddress.getLocalHost().getHostName()</code>;  otherwise it
    *    is the <code>localhost</code> if the name cannot be determined,
    *    never <code>null</code> or empty.
    */
   static public String getHostName()
   {
      return ms_hostName;
   }

   /**
    * Returns the host name of this machine.  This is determined by
    * calling <code>InetAddress.getLocalHost().getHostName()</code> or
    * the first entry in the "bindAddress" (if it is not empty) when the server
    * starts up.
    *
    * @return The first address that is defined in the "bindAddress" property
    *    if the value of the property is not empty; otherwise it is the
    *    <code>localhost</code> if the name cannot be determined, never
    *    <code>null</code> or empty.
    */
   static public String getServerName()
   {
      return ms_serverName;
   }

    /**
     * Returns the servername but takes into account the scenario when behind a reverse proxy.
     * @param request A valid http request
     * @return
     */
   public static String getServerName(HttpServletRequest request){
       String ret = ms_serverName;
       if(!ms_serverName.equals(request.getServerName())){
           ret = request.getServerName();
       }
       return ret;
   }

   /**
    * Returns the fully qualified host name (e.g. foo.company.com). This is
    * normally determined by calling
    * <code>InetAddress.getByName(getHostAddress()).getHostName()</code>. The
    * documentation on this method is not very specific, therefore we keep a
    * backdoor open in case we run into a problem. To use the backdoor,
    * specify an entry <code>domain=company.com</code>. If this property is
    * found, the fully qualified host name returned is the simple
    * host name (got through <code>getHostName()</code>) with the specified
    * domain appended. The domain can be specified with or without a leading
    * dot. Returns the simple host name if the fully qualified name could not
    * be determined and prints a warning to the server console.
    * <p>
    * However, the bind address will be returned if there is one defined in the
    * "bindAddress" property.
    *
    * @return the fully qualified host name if the "bindAddress" property is
    *    undefined; otherwise it is the first address that is defined in the
    *    "bindAddress" property, never <code>null</code> or empty.
    */
   static public String getFullyQualifiedHostName()
   {
      String bindAddress = getFirstBindAddress();
      if (bindAddress != null)
      {
         return bindAddress;
      }
         String domain = getServerProps().getProperty("domain");
         if (domain != null)
         {
            String separator = ".";
            if (domain.startsWith(separator))
               return getHostName() + domain;
            else
               return getHostName() + separator + domain;
         }

         String hostName = getHostName();
         try
         {
            hostName = InetAddress.getByName(getHostAddress()).getHostName();
         }
         catch (UnknownHostException e)
         {
            // this should not happen but if it does we like to know
            Object[] args =
            {
               hostName
            };

            PSConsole.printMsg("Server", IPSServerErrors.UNKNOWN_HOST, args);
         }

         return hostName;
      }

   /**
    * Get the resource bundle for all non-error string resources used in the
    * server.
    *
    * @return the resource bundle, never <code>null</code>.
    */
   public static ResourceBundle getRes()
   {
      return ms_bundle;
   }

   /**
    * Returns the key value to use as part one with the Rhythmyx encyrption
    * algorithm.
    *
    * @return The key, never <code>null</code> or empty.
    */
   @Deprecated
   public static String getPartOneKey()
   {
      // get the encrypted constant and decrypt.
      return PSLegacyEncrypter.getInstance(null).getPartOneKey();
   }

   /**
    * Returns the key value to use as part two with the Rhythmyx encyrption
    * algorithm.
    *
    * @return The key, never <code>null</code> or empty.
    */
   @Deprecated
   public static String getPartTwoKey()
   {
      // get the encrypted constant and decrypt.
      return PSLegacyEncrypter.getInstance(null).getPartTwoKey();
   }



   /**
    * Get a specified system object, which is an entry in the object table
    *
    * @param typeId The object type id, which is the primary key of the
    *    object table.
    *
    * @return The requested object. It may be <code>null</code> if cannot
    *    found one.
    */
   public static PSCmsObject getCmsObject(int typeId)
   {
      if (m_cmsObjectMap == null)  // do lazy query
      {
         synchronized (PSServer.class)
         {
            if (m_cmsObjectMap == null)  // do lazy query
            {
               Map<Integer, PSCmsObject> m = new ConcurrentHashMap<Integer,PSCmsObject>(8, 0.9f, 1);

         IPSCmsObjectMgr cmsMgr = PSCmsObjectMgrLocator.getObjectManager();
         List<PSCmsObject> cmsObjects = cmsMgr.findAllCmsObjects();

         for (PSCmsObject obj : cmsObjects)
         {
            m.put(obj.getTypeId(), obj);
         }
         m_cmsObjectMap = m;
      }
         }
      }

      return m_cmsObjectMap.get(new Integer(typeId));
   }

   /**
    * The same as {@link #getCmsObject(int) getCmsObject}, except this method
    * will only throw RuntimeException if an error occurs. This should only
    * be called if the caller does not know what to do with the exceptions
    * which may throw from this method.
    * 
    * @return maybe null so be careful, if its null this is bad.
    */
   public static PSCmsObject getCmsObjectRequired(int typeId)
   {
      try
      {
         PSCmsObject o = getCmsObject(typeId);
         if (o == null)
            PSConsole.printMsg("Server", "Failed to load cms object type: " + typeId);
         return o;
      }
      catch (Throwable e) // this may not possible, otherwise fatal error
      {
         PSConsole.printMsg("Server", "Failed to load cms object type: " + typeId);
         throw new RuntimeException(
            "Failed to get cms object for object type of: " + typeId);
      }
   }

   /**
    * Version of {@link #verifyCommunity(PSRequest)} that takes the request
    * context.  This is temporarily implemented to support the
    * sys_commAuthenticateUser exit until that exit is deprecated completely.
    * At that time this method will be removed, and so should be considered
    * deprecated.
    *
    * @param ctx the request context which cannot be <code>null</code> and
    *    must be an instance of <code>PSRequestContext</code>.
    *
    * @throws IllegalArgumentException if <code>ctx</code> is invalid.
    * @throws PSServerException if there are any other errors.
    *
    * @see #verifyCommunity(PSRequest)
    */
   public static void verifyCommunity(IPSRequestContext ctx)
      throws PSServerException
   {
      if (!(ctx instanceof PSRequestContext))
         throw new IllegalArgumentException(
               "ctx cannot be null and must be of type PSRequestContext");

      PSRequest req = ((PSRequestContext)ctx).getRequest();
      verifyCommunity(req);
   }

   /**
    * Determines the user's current community and verifies it is valid.  Checks
    * the user's session, a cookie, the override parameter, or the subject
    * attributes of the user in that order for the current community.  The
    * result is then tested to be sure the user belongs to that community.  The
    * result if successful is then set in the user's session private object.
    *
    * @param request The current request, may not be <code>null</code>.
    *
    * @throws PSServerException If the user's community is not valid or
    * if there is an error trying to verify it.
    */
   public static void verifyCommunity(PSRequest request)
      throws PSServerException
   {
      if (request == null)
         throw new IllegalArgumentException("request may not be null");

      String userCommunity = null;
      PSUserSession sess = request.getUserSession();
      List list = null;
      try
      {
         /* if communities is disabled the community value is "0" which is the
          * default.
          */
         if(!"yes".equals(ms_serverProps.getProperty("communities_enabled",
            "no")))
         {
            sess.setPrivateObject(IPSHtmlParameters.SYS_COMMUNITY, "10");
            return;
         }

         PSRequestContext ctx = new PSRequestContext(request);

         /*
          * Internal user is not in a community.
          */
         if (PSSecurityProvider.isInternalUser(ctx))
         {
            return;
         }

         // check if user is authenticated, if not, use system community for now
         String realUser = sess.getRealAuthenticatedUserEntry();
         if (realUser == null)
         {
            request.getUserSession().setPrivateObject(
               IPSHtmlParameters.SYS_COMMUNITY, SYSTEM_COMMUNITY);
            return;
         }

         // get community from session objects
         Object commid = sess.getPrivateObject(IPSHtmlParameters.SYS_COMMUNITY);

         // if community does not exist as session object
         if (null == commid || commid.toString().length() < 1)
         {
            //get from cookies
            userCommunity = request.getCookie(
               IPSHtmlParameters.SYS_COMMUNITY, null);
         }
         else
         {
            userCommunity = commid.toString();
         }

         // see if trying to override with html param
         String test = request.getParameter(
            IPSHtmlParameters.SYS_OVERRIDE_COMMUNITYID);
         if (test != null && test.trim().length() != 0)
            userCommunity = test;

         //Try to get the default community for the user from the server
         if(null == userCommunity || userCommunity.trim().length() < 1)
            userCommunity = getUserDefaultCommunity(request);

         if(null == userCommunity || userCommunity.trim().length() < 1)
         {
            //user has no community, he gets a system community value
            //of SYSTEM_COMMUNITY
            userCommunity = SYSTEM_COMMUNITY;
         }

         // if system community, then no need to validate
         if(!(userCommunity.equals(SYSTEM_COMMUNITY)))
         {
            if (list == null)
               list = request.getUserSession().getUserCommunities(request);

            if (null == list || !list.contains(userCommunity))
            {
               String override =
                  request.getParameter(
                     IPSHtmlParameters.SYS_OVERRIDE_COMMUNITYID);
               String fallback =
                  request.getParameter(
                     IPSHtmlParameters.SYS_FALLBACK_COMMUNITYID);
               if (list == null
                  || override == null
                  || (override != null
                     && (fallback == null || fallback.equals("true") == false)))
               {
                  //user is not member of his role-communities, user fails
                  //authentication
                  Object[] args = { realUser, userCommunity };
                  userCommunity = null;
                  throw new PSServerException(
                     IPSServerErrors
                        .COMMUNITIES_AUTHENTICATION_FAILED_INVALID_COMMUNITY,
                     args);
               }
               else
               {
                  // Default the user community
                  userCommunity = getUserDefaultCommunity(request);
                  // If there isn't one then throw an exception anyway
                  if (userCommunity == null || userCommunity.trim().length() == 0)
                  {
                     Object[] args = { realUser, "" };
                     userCommunity = null;
                     throw new PSServerException(
                        IPSServerErrors
                           .COMMUNITIES_AUTHENTICATION_FAILED_INVALID_COMMUNITY,
                        args);
                  }
               }
            }
         }

         // now save the result
         sess.setPrivateObject(IPSHtmlParameters.SYS_COMMUNITY, userCommunity);
      }
      catch(PSDataExtractionException e)
      {
         Object[] args = {e.toString()};
         throw new PSServerException(
            IPSServerErrors.COMMUNITIES_AUTHENTICATION_FAILED_ERROR, args);
      }
      catch(PSInternalRequestCallException e)
      {
         Object[] args = {e.toString()};
         throw new PSServerException(
            IPSServerErrors.COMMUNITIES_AUTHENTICATION_FAILED_ERROR, args);
      }
   }

   /**
    * This method retrieves the default community from the first role that
    * belongs to the user. If user belongs to multiple roles, the first
    * non-empty value is considered.
    *
    * @return community id of the default community
    *
    * @throws PSInternalRequestCallException if there is an error retrieving
    * the default community
    */
   private static String getUserDefaultCommunity(PSRequest request)
      throws PSInternalRequestCallException
   {
      return request.getUserSession().getCommunityId(request,
             getUserRoleAttribute(request, SYS_DEFAULTCOMMUNITY));
   }

   /**
    * This method retrieves the value of the given attribute for the user role.
    * If user happens to be in multiple roles the first non empty value is
    * considered
    *
    * @param req The current request, assumed not <code>null</code>.
    * @param srcAttrName Name of the role attribute to retrieve, assumed not
    * <code>null</code> or empty.
    *
    * @return value of the given attribute, may be <code>null</code>, never
    * empty.
    */
   @SuppressWarnings(value="unchecked")
   private static String getUserRoleAttribute(PSRequest req, String srcAttrName)
   {
      String attrValue = null;

      // user request context as it provides convenience methods.
      PSRequestContext request = new PSRequestContext(req);
      List roles = request.getSubjectRoles();
      Object role = null;
      List roleAttribs = null;
      PSAttribute attr = null;
      List attrList = null;
      String attrName = null;
      boolean found = false;
      for(int i=0; roles != null && i<roles.size() && !found; i++)
      {
         role = roles.get(i);
         if(role == null)
            continue;
         roleAttribs = request.getRoleAttributes(role.toString().trim());
         for(int j=0; roleAttribs != null && j<roleAttribs.size() && !found;
            j++)
         {
            attr = (PSAttribute)roleAttribs.get(j);
            if(attr == null)
               continue;
            attrName = attr.getName();
            if(attrName.equals(srcAttrName))
            {
               attrList = attr.getValues();
               if(attrList != null && attrList.size() > 0)
               {
                  // we take only the first attribute
                  attrValue = (String)attrList.get(0);
               }
            }
            if(attrValue != null && attrValue.length() > 0)
               found = true;
         }
      }

      return attrValue;
   }


   /**
    * This is used to cache the object table (PSX_OBJECTS). It is set by
    * <code>getCmsObject</code> once, never <code>null</code> after that.
    */
   private volatile static Map<Integer,PSCmsObject> m_cmsObjectMap = null;


   /**
    * Adds a handler to the request type list, with the appropriate type.
    * Handler may have already been added to the list with a different type.
    * Types are uppercased before adding them.
    *
    * @param handler The request handler
    * @param type The type of request (i.e. "GET" or "POST")
    * @throws IllegalArgumentException if handler or type is <code>null</code>.
    */
   private static void addRequestHandlerType(IPSRequestHandler handler,
      String type)
   {
      if (handler == null)
         throw new IllegalArgumentException("handler may not be null");

      if (type == null)
         throw new IllegalArgumentException("type may not be null");

      List<String> typeList = ms_requestHandlerTypes.get(handler);
      if (typeList == null)
         typeList = new ArrayList<String>();

      typeList.add(type.toUpperCase());

      ms_requestHandlerTypes.put(handler, typeList);
   }

   /**
    * Returns list of request methods used by applications.  Currently
    * includes the "POST", "GET" and "HEAD" types.
    *
    * @return The ArrayList, never <code>null</code>.
    */
   private static List<String> getStdAppRequestMethods()
   {
      if (m_stdAppRequestTypes == null)
      {
         synchronized(PSServer.class)
         {
            if (m_stdAppRequestTypes == null)
            {
         m_stdAppRequestTypes = new ArrayList<String>();
         m_stdAppRequestTypes.add("POST");
         m_stdAppRequestTypes.add("GET");
         m_stdAppRequestTypes.add("HEAD");
      }
         }
      }

      return m_stdAppRequestTypes;
   }

   /**
    * Returns list of request methods used by most request handlers. Currently
    * includes the "POST" type.
    *
    * @return The ArrayList, never <code>null</code>.
    */
   private static List<String> getStdHandlerRequestMethods()
   {
      if (m_stdHandlerRequestTypes == null)
      {
         synchronized(PSServer.class)
         {
            if (m_stdHandlerRequestTypes == null)
            {
         m_stdHandlerRequestTypes = new ArrayList<String>();
         m_stdHandlerRequestTypes.add("POST");
      }
         }
      }

      return m_stdHandlerRequestTypes;
   }




   /**
    * Initializes the search subsystem.
    *
    * @param apps An array of applications known to the server. This
    * array is walked to find all content types. The array may be
    * <code>null</code>.
    *
    * @throws PSSearchException if there are any errors.
    */
   @SuppressWarnings(value="unchecked")
   private static void initSearch(PSApplication[] apps)
   throws PSSearchException
   {
      PSSearchConfig searchConfig = getServerConfiguration().getSearchConfig();
      
      PSConsole.printMsg("Server", "Initializing search engine");
      Properties props = new Properties();
      props.setProperty(PSSearchEngine.PROP_CLASSNAME,
            "com.percussion.search.lucene.PSSearchEngineImpl");

      Map customProps = searchConfig.getCustomProps();
      Iterator propIter = customProps.keySet().iterator();
      while (propIter.hasNext())
      {
         String prop = (String) propIter.next();
         props.setProperty(prop, customProps.get(prop).toString());
      }

      PSSearchException se = null;
      try
      {
         ms_searchEngine = PSSearchEngine.getInstance(props);
      }
      catch (PSSearchException ex)
      {
         se = ex;
      }

      /**
       * If FTS is not enabled, the engine is only required for text extraction,
       * so we can just return.  If not enabled and the engine failed to
       * initialize, then log a non-fatal error and return.  If FTS is enabled,
       * then any failure to initialize the engine is fatal.
       */
      if (searchConfig.isFtsEnabled() == false)
      {
         if (se != null)
         {
            PSConsole.printMsg("Server", se, ms_bundle.getString(
               "textExtractionUnavailable"));
         }
         return;
      }
      else if (se != null)
      {
         throw (PSSearchException) se.fillInStackTrace();
      }

      PSSearchAdmin sa = null;
      try
      {
         // TODO: admin configuration here

         // walk all applications and create a set of known content types
         Set<Long> knownContentTypeIds = new HashSet<Long>();
         if (apps != null)
         {
            for (int i = 0; i < apps.length; i++)
            {
               PSApplication app = apps[i];
               if (app != null)
               {
                  PSCollection datasets = app.getDataSets();
                  if (datasets != null)
                  {
                     for (Iterator iter = datasets.iterator(); iter.hasNext();)
                     {
                        PSDataSet dataset = (PSDataSet) iter.next();
                        if (dataset instanceof PSContentEditor)
                        {
                           PSContentEditor ce = (PSContentEditor) dataset;
                           knownContentTypeIds.add(ce.getContentType());
                        }
                     }
                  }
               }
            }
         }

         sa = ms_searchEngine.getSearchAdmin(true);
         // Verify existing information in external engine
         sa.verify(knownContentTypeIds);
         // Save any changes caused by the above updates
         sa.save();
         //Clear the index locks
         sa.clearIndexLocks();
         
         PSItemDefManager mgr = PSItemDefManager.getInstance();
         long[] contentIds =
               mgr.getContentTypeIds(PSItemDefManager.COMMUNITY_ANY);
         long[] ids = new long[contentIds.length+1];
         System.arraycopy(contentIds, 0, ids, 0, contentIds.length);
         /*
          * the folder content type is generally not visible as a
          * content type, but we want to index it so it becomes searchable
          */
         ids[ids.length-1] = PSFolder.FOLDER_CONTENT_TYPE_ID;
         for (int i=0; i < ids.length; i++)
         {
            PSItemDefinition def =
                  mgr.getItemDef(ids[i], PSItemDefManager.COMMUNITY_ANY);
            boolean reindex = sa.update(def, new PSFieldRetriever(ids[i]),
                  false);
            if (reindex)
            {
               PSConsole.printMsg("Server",
                     def.getName() + " requires manual re-indexing.");
            }
         }
         // Save any changes caused by the above updates
         sa.save();
         if (!ms_searchEngine.isAvailable())
            ms_searchEngine.start();

         ms_searchIndexQueue.start();
         PSConsole.printMsg("Server", "Search engine initialized.");
         
         String indexStr = searchConfig.getCustomProp(
               PSSearchConfig.INDEX_ON_STARTUP);
         boolean doIndexAll = indexStr != null && 
              (indexStr.equalsIgnoreCase("yes") || 
                    indexStr.equalsIgnoreCase("true")) ? true : false;
         
         String indexTypeStr = searchConfig.getCustomProp(
               PSSearchConfig.INDEX_TYPE_ON_STARTUP);
         boolean doIndexType = false;
         if (!doIndexAll && !StringUtils.isBlank(indexTypeStr))
         {
            try
            {
               long typeId = mgr.contentTypeNameToId(indexTypeStr);
               ids = new long[1];
               ids[0] = typeId;
               doIndexType = true;
            }
            catch (Exception e)
            {
               PSConsole.printMsg("Server", "Failed to locate content type id for indexing specified name: " + indexTypeStr);
            }
         }
         
         if (doIndexAll || doIndexType)
         {
            // Index all content types on initial startup of new install
            String typeStr = doIndexAll ? "all content types " : "content type " + indexTypeStr;

            
            PSConsole.printMsg("Server", "Indexing " + typeStr + " for search.");
            for (int i = 0; i < ids.length; i++)
               ms_searchIndexQueue.indexContentType((int) ids[i]);
            
            // don't want to index on subsequent startups
            searchConfig.addCustomProp(PSSearchConfig.INDEX_ON_STARTUP, "no");
            
            // "clear" index by type on startup rather than remove so the upgrade plugin won't re-add it
            searchConfig.removeCustomProp(PSSearchConfig.INDEX_TYPE_ON_STARTUP);
            searchConfig.addCustomProp(PSSearchConfig.INDEX_TYPE_ON_STARTUP, "");
            
            
            // save updated server config
            File rxconfig = new File(getRxConfigDir());
            File config = new File(rxconfig, "config.xml");

            try
            {
               ms_srvConfig.setSearchConfig(searchConfig);
               PSConsole.printMsg(PSSearchEngine.SUBSYSTEM_NAME,"resetting index_on_startup property");
                try(FileOutputStream out = new FileOutputStream(config)) {
                   PSXmlDocumentBuilder.write(ms_srvConfig.toXml(), out);
                }
            }
            catch (IOException e)
            {
               PSConsole.printMsg(PSSearchEngine.SUBSYSTEM_NAME, e);
            }
         }
      }
      catch (PSAdminLockedException ale)
      {
         //should never happen since we are the first to use it
         throw new PSRuntimeException(ale.getErrorCode(),
               ale.getErrorArguments());
      }
      catch (PSInvalidContentTypeException icte)
      {
         //should never happen as we got the ids straight from the mgr
         throw new PSRuntimeException(icte.getErrorCode(),
               icte.getErrorArguments());
      }
      catch (IOException e)
      {
         // Should never happen since the configuration files should
         // always be writable
         PSConsole.printMsg(PSSearchEngine.SUBSYSTEM_NAME, e);
         throw new
            PSRuntimeException(IPSSearchErrors.SEARCH_ENGINE_FAILED_INIT);
      }
      finally
      {
         if (null != sa)
            ms_searchEngine.releaseSearchAdmin(sa);
      }
   }

   /**
    * Shuts down the import and search queue (or sub-system).
    *
    * @throws PSSearchException if there are any errors.
    */
   private static void shutdownQueues() throws PSSearchException
   {
      PSConsole.printMsg("Server", "Shutting down import engine.");
      PSNotificationHelper.notifyServerShutdown(getRxDir());
      
      PSConsole.printMsg("Server", "Shutting down search engine.");
      ms_searchIndexQueue.shutdown();

      if (ms_searchEngine != null) ms_searchEngine.shutdown(false);
   }

   
   /**
    * the FileLock that is acquired when the server is running. 
    */
   private static FileLock ms_serverStartupLock = null;
   
   /**
    * the lock file resource name that will be created. This is created
    *  in the RxHome directory when the server is inited. A client such as
    *  installer can check the existence of this file and warn the user that
    *  installation cannot proceed if the server is running as some 
    *  installations need to update the JRE.
    */
   public static final String SERVER_FILE_LOCK = "server_run_lock";
   
   /**
    * The host name of the machine. It is either the value of
    * <code>InetAddress.getLocalHost().getHostName()</code> or the
    * <code>localhost</code> if the host name cannot be determined.
    */
   private static String ms_hostName = "localhost";

   /**
    * The name of the local server (this server).
    */
   private static String ms_serverName     = "localhost";

   /**
    * The server request root (path)
    */
   private static String   ms_requestRoot    = "/Rhythmyx";

   /**
    * The default application's name
    */
   static String   ms_defaultAppName = null;

   private static boolean  ms_hasDefaultApp  = false;

   /**
    * This is the server's log handler, which can be accessed by the
    * server package through the getLogHandler method.  Assigned in the methods
    * <code>initObjectStore</code> and <code>initLogHandling</code>.
    */
   static PSLogHandler ms_serverLogHandler = null;

   /**
    * This is the server's error handler, which can be accessed by the
    * server package through the getErrorHandler method.
    */
   static PSErrorHandler   ms_serverErrorHandler = null;

   /**
    * The ACL handler for this server
    */
   static PSAclHandler         ms_AclHandler = null;

   private static PSServerStatistics   ms_Statistics = new PSServerStatistics();

   /** The global extension manager */
   private static IPSExtensionManager ms_extensionMgr;
   
   /* flags telling us what's been initialized */
   public static final int INITED_NONE             = 0x00000000;
   public static final int INITED_LOG              = 0x00000001;
   public static final int INITED_ERROR            = 0x00000002;
   public static final int INITED_DB_POOL          = 0x00000004;
   public static final int INITED_OBJECT_STORE     = 0x00000008;
   public static final int INITED_REQ_HANDLERS     = 0x00000010;
   public static final int INITED_SECURITY         = 0x00000020;
   public static final int INITED_EXTENSION        = 0x00000040;
   public static final int INITED_DATABASE_FUNCTION = 0x00000080;
   public static final int INITED_PACKAGE_INSTALL  = 0x00000100;

   // INITED_ALL should be the sum of all the INITED flags
   public static final int INITED_ALL              = 0x000001FF;

   private static final String PROPS_OBJECT_STORE_VAR = "objectStoreProperties";
   private static final String PROPS_OBJECT_STORE     = "rxconfig/Server/objectstore.properties";
   public static final String PROPS_SERVER           = "server.properties";

   private static final String PROP_REQLISTENER_HOST  = "bindAddress";
   private static final String PROP_NO_PAUSE_ON_EXIT  = "noPauseOnExit";
   private static final String PROP_ESTIMATE_STATS = "estimateStatistics";

   /**
    * Property indicating if character encoding settings in xsl stylesheets
    * will be modified to reflect the encoding specified in the resource
    * via the workbench.
    */
   public static final String PROP_ALLOW_XSL_ENCODING_MODS =
      "allowXslEncodingMods";

   /**
    * Property that when set to true will cause all non-xhtml namespace
    * declarations to be stripped from the resulting output of a XSL
    * stylesheet transformation.
    */
   public static final String PROP_CLEANUP_NAMESPACES =
      "cleanupNamespaces";

   /**
    * Property that determines the response close delay
    * in milliseconds.
    */
   public static final String PROP_RESP_CLOSE_DELAY =
      "responseCloseDelay";

   /**
    * Property to indicate if server should require all cms fieldset and field
    * names to be unique. See {@link #requireUniqueFieldNames()} for more info.
    */
   private static final String PROP_UNIQUE_FIELD_NAMES =
      "requireUniqueFieldNames";

   private static IPSObjectStoreHandler   ms_objectStore          = null;
   private static int                     ms_WhatsUp              = INITED_NONE;
   private static PSProperties            ms_objectStoreProps     = null;
   private static PSProperties            ms_serverProps          = new PSProperties();
   static Hashtable<String,IPSRequestHandler> ms_RequestHandlers      = null;

   /**
    * List of request handlers that will process requests based on the request
    * root of the URL.
    */
   private static Hashtable<String,IPSRequestHandler> ms_rootedRequestHandlers
      = null;
   protected static PSConsole             ms_console              = null;

   private static boolean                 ms_shuttingDown         = false;
   private static boolean                 ms_noPauseOnExit        = false;
   /**
    * this flag specifies whether or not to create an input console. if
    * <code>true</code> we will not create an input console, otherwise we
    * will. by default we will create an input console.
    */
   private static boolean ms_noInputConsole = false;

   /**
    * Flag which indicates whether (<code>true</code>) or not (<code>false
    * </code>) URL's are interpreted case sensitive.
    */
   private static boolean                 ms_caseSensitiveUrl     = false;

   /**
    * Flag to indicate if unique field and fieldset names for content editors
    * should be enforced.  Initially <code>false</code>, value is possibly set
    * to <code>true</code> when the server properties are loaded. See
    * {@link #requireUniqueFieldNames()} for more info.
    */
   private static boolean ms_requireUniqueFieldNames = false;

   /**
    * The server configuration object, as built from the object store
    */
   static PSServerConfiguration ms_srvConfig = null;

   /**
    * Stores the server version information
    */
   private static PSFormatVersion ms_version = null;

   /**
    * The port we are listening on, set by initSocketListeners
    */
   public static int ms_listenerPort = -1;

   /**
    * The SSL port we are listening on, set by initSocketListeners. This will
    * be 0 is SSL support is disabled.
    */
   public static int ms_sslListenerPort;

   /**
    * The host address of the local machine, determined by
    * InetAddress.getLocalHost()
    */
   public static String ms_hostAddress;

   /**
    * This variable contains all applications known to the server, whether
    * enabled or not. It is set in <code>initObjectStore</code> and cleared
    * when the initialization is complete.
    */
   protected static PSApplication[] ms_allApps;

   /**
    * List of allowable request methods for applications.  Initialized in
    * first call to {@link #getStdAppRequestMethods()}, never <code>null</code>
    * after that.
    */
   private static List<String> m_stdAppRequestTypes = null;

   /**
    * List of allowable request methods for most request handlers.  Initialized
    * in first call to {@link #getStdHandlerRequestMethods()}, never
    * <code>null</code> after that.
    */
   private static List<String> m_stdHandlerRequestTypes = null;

   /**
    * List of request handlers and what types of request methods they can handle
    * (i.e. GET or POST) - all types should be uppercased.
    */
   private static Hashtable<IPSRequestHandler,List<String>>
      ms_requestHandlerTypes = new Hashtable<IPSRequestHandler,List<String>>();


   /**
    * Constant for the name of the entry that reperesents workflow's name/value
    * pair.
    */
   public static final String ENTRY_NAME = "server_config_base_dir";

   /**
    * Constant for the directory containing all other configuration directories.
    */
   public static final String BASE_CONFIG_DIR = "rxconfig";

   /**
    * Constant for the directory containing server configs.
    * Assumed to be relative to the Rx directory. No trailing slash.
    */
   public static final String SERVER_DIR = BASE_CONFIG_DIR + "/Server";

   /**
    * The server default http charset property.
    */
   public static final String SERVER_DEFAULT_HTTP_CHARSET = "defaultHttpCharacterSet";

   /**
    * The server default file charset property.
    */
   public static final String SERVER_DEFAULT_FILE_CHARSET = "defaultFileCharacterSet";


   /**
    * Cache manager to manage caching of cms pages.  Intialized and started in
    * <code>initRequestHandlers()</code>, never <code>null</code> after that.
    */
   static PSCacheManager ms_cacheManager = null;

   /**
    * Queue to asynchronously process change events and update the search index.
    * Initialized and started in the {@link #initSearch(PSApplication[])} method,
    * never <code>null</code> after that.  Shutdown by {@link #shutdownQueues()}
    */
   private static PSSearchIndexEventQueue ms_searchIndexQueue = null;

   /**
    * Resource bundle for non-error string resources used in the server.
    */
   private static ResourceBundle ms_bundle = ResourceBundle.getBundle(
            "com.percussion.server.PSStringResources");

   /**
    * Value of the system default community, hardcoded to 1.
    */
   private static final String SYSTEM_COMMUNITY = "1";

   /**
    * Name of user default community properties.
    */
   private static final String SYS_DEFAULTCOMMUNITY = "sys_defaultCommunity";

   /**
    * A set with all defined macros for this server. Initialized in
    * {@link #init(ServletConfig, String[], int)}, never changed after that.
    */
   private static PSMacroDefinitionSet ms_macros;

   /**
    * Map of all handler state listeners. The key for the map is the name of
    * the handler (string) and the value is a map of listener events. The key
    * of this map is the {@link IPSHandlerStateListener listener} and the
    * value is and integer object of applicable events ORed together. This is
    * to facilitate registering multiple handler state listeners and state
    * events for each handler.
    */
   private static Map ms_handlerStateListenerMap = new HashMap();

   /**
    * List of listeners to be notified of handler initializations.  Listeners
    * are added during server init, never <code>null</code> or empty after that.
    */
   private static List<IPSHandlerInitListener> ms_handlerInitListeners =
      new ArrayList<IPSHandlerInitListener>();


   /**
    * Search engine singleton, initialized during server init by
    * <code>initSearch()</code>, never <code>null</code> after that.
    */
   private static PSSearchEngine ms_searchEngine = null;

   /**
    * Custom control manager singleton, initialized during server init, never
    * <code>null</code> after that.
    */
   private static PSCustomControlManager ms_customCtrlMgr = null;
   
   /**
    * System control manager singleton, initialized during server init, never
    * <code>null</code> after that.
    */
   private static PSSystemControlManager ms_sysCtrlMgr = null;
   
   /**
    * Executor for executing threads a little after server startup.
    * It is current the Runnable/Callables responsibility to delay (sleep).
    * Never <code>null</code>.
    */
   private static ExecutorService ms_delayedInitExecutor = Executors.newSingleThreadExecutor();
   
   // inner class to help shutdown of this Server: shared by PSProcessDaemon
   class PSServerShutdown extends PSServerShutdownHelper
      implements IPSShutdownListener
   {
      public void psShutdown()
      {
         PSServer.scheduleShutdown(1);
      }
   }

   class PSServerAppChangeListener implements IPSApplicationListener
   {
      PSServerAppChangeListener()
      {
         super();
      }

      /* ******* IPSApplicationListener Interface Implementation ******* */

      /**
       * Changes have been made to the application.
       * <P>
       * If the application has been modified, including a rename, the
       * applicationRenamed method will be called first, then the
       * applicationUpdated method. If a rename did not occur,
       * applicationRenamed will not be called.
       *
       * @param   app         the application object
       */
      public void applicationUpdated(PSApplication app)
         throws PSSystemValidationException, PSServerException, PSNotFoundException
      {
         String appName = app.getName();
         shutdownApplication(appName);

         if (app.isEnabled()){
            startApplication(appName);
         }
      }

      /**
       * A new application has been created.
       *
       * @param   app         the application object
       */
      public void applicationCreated(PSApplication app)
         throws PSSystemValidationException, PSServerException, PSNotFoundException
      {
         String appName = app.getName();
         if (app.isEnabled()){
            startApplication(appName);
         }
      }

      /**
       * The name of the application has been changed.
       * <P>
       * If additional changes have also been made to the application, the
       * applicationRenamed method will be called first, then the
       * applicationUpdated method.
       *
       * @param   app         the application object
       *
       * @param   oldName     the original name of the application
       *
       * @param   newName     the new name of the application
       */
      public void applicationRenamed(
         PSApplication app, String oldName, String newName)
      {
         String appKey = "data-" + oldName.toLowerCase();

         // if this app exists, it must be in the master hash
         PSApplicationHandler rh = (PSApplicationHandler)ms_RequestHandlers.get(appKey);
         if (rh != null){
            synchronized (ms_RequestHandlers){
               ms_RequestHandlers.remove(appKey);
               ms_RequestHandlers.put("data-" + newName.toLowerCase(), rh);
            }
         }
      }

      /**
       * The application has been removed from the object store.
       * It is guaranteed that no other information has changed.
       *
       * @param   app         the application object
       */
      public void applicationRemoved(PSApplication app)
      {
         String appName = app.getName();
         shutdownApplication(appName);
      }
   }

   /**
    * Listens for server configuration changes and updates the appropriate
    * parts as required.
    */
   class PSServerConfigChangeListener implements IPSServerConfigurationListener
   {
      /**
       * Handle notification of changes to the server configuration object.
       *
       * @param config the configuration object, may be <code>null</code> in
       *    which case this does nothing.
       */
      public void configurationUpdated(PSServerConfiguration config)
      {
         if (config == null)
            return;

         ms_defaultAppName = config.getDefaultApplication();
         updateServerRequestRoot(config);

         // security provider pool update, just reinitialize
         PSSecurityProviderPool.init(config);

         // acl handler update
         com.percussion.design.objectstore.PSAcl oldAcl = ms_srvConfig.getAcl();
         com.percussion.design.objectstore.PSAcl newAcl = config.getAcl();

         boolean hasChanged = false;
         if ((oldAcl != null) && (newAcl != null) && !(oldAcl.equals(newAcl)))
         {
            hasChanged = true;
         }
         if (hasChanged)
            ms_AclHandler = new PSAclHandler(newAcl);

         // Security manager update (sandbox security)
         if (ms_srvConfig.getUseSandboxSecurity() !=
            config.getUseSandboxSecurity())
         {
            System.setSecurityManager( ms_srvConfig.getUseSandboxSecurity() ?
               new SecurityManager() : null );
         }

         // logger update
         PSLogger loggerOld = ms_srvConfig.getLogger();
         PSLogger loggerNew = config.getLogger();
         if ((loggerNew != null) && !(loggerOld.equals(loggerNew)))
            ms_serverLogHandler = new PSLogHandler(loggerNew);

         // error handler update
         PSErrorWebPages errWebPagesOld = ms_srvConfig.getErrorWebPages();
         PSErrorWebPages errWebPagesNew = config.getErrorWebPages();
         PSNotifier notifierOld = ms_srvConfig.getNotifier();
         PSNotifier notifierNew = config.getNotifier();

         if (((errWebPagesNew != null) &&
            !(errWebPagesOld.equals(errWebPagesNew))) ||
            ((notifierNew != null) && !(notifierOld.equals(notifierNew))))
         {
            ms_serverErrorHandler = new PSErrorHandler(errWebPagesNew,
               notifierNew);
         }

         // cache update
         PSServerCacheSettings newCacheSettings =
            config.getServerCacheSettings();
         PSServerCacheSettings oldCacheSettings =
            ms_srvConfig.getServerCacheSettings();
         if (!newCacheSettings.equals(oldCacheSettings))
         {
            try
            {
               ms_cacheManager.init(newCacheSettings);
            }
            catch(PSCacheException pscex)
            {
               if (newCacheSettings.isEnabled())
                  throw new RuntimeException(pscex.getLocalizedMessage());
            }
         }

         //If plugin config or view refresh setting changes flush the cache on
         // plugin resources.
         if(!ms_srvConfig.getJavaPluginConfig().equals(
            config.getJavaPluginConfig()))
         {
            String[] plgApps = IPSJavaPluginConfig.PLUGIN_APPLICATION_LIST;
            for(int i=0; i<plgApps.length; i++)
            {
               ms_cacheManager.flushApplication(plgApps[i]);
            }
         }

         ms_srvConfig = config;  // update our copy to the new one now
      }
   }

   /***
    * Decrypts the specified string
    * @deprecated since 8.0.2
    * @param uid user name
    * @param str password
    * @return encrypted password
    */
   @Deprecated
   private static String eatLasagna(String uid, String str)
   {
      if ((str == null) || (str.equals("")))
         return "";

      int partone = PSLegacyEncrypter.getInstance(null).OLD_SECURITY_KEY().hashCode();
      int parttwo;
      if (uid == null || uid.equals(""))
         parttwo = PSLegacyEncrypter.getInstance(null).OLD_SECURITY_KEY2().hashCode();
      else
         parttwo = uid.hashCode();

      partone /= 7;
      parttwo /= 13;

      try {
         int padLen = 0;
         ByteArrayOutputStream bOut = new ByteArrayOutputStream();
         com.percussion.util.PSBase64Decoder.decode(
            new ByteArrayInputStream(str.getBytes(PSCharSets.rxJavaEnc())), bOut);

         IPSKey key = PSEncryptionKeyFactory.getKeyGenerator(PSEncryptionKeyFactory.DES_ALGORITHM);
         if ( key instanceof IPSSecretKey)
         {
            IPSSecretKey secretKey = (IPSSecretKey)key;
            byte[] baOuter = new byte[8];
            for (int i = 0; i < 4; i++)
               baOuter[i] = (byte)((partone >> i) & 0xFF);
            for (int i = 4; i < 8; i++)
               baOuter[i] = (byte)((parttwo >> (i-4)) & 0xFF);

            secretKey.setSecret(baOuter);
            IPSDecryptor decr = secretKey.getDecryptor();

            ByteArrayOutputStream bOut2 = new ByteArrayOutputStream();
            decr.decrypt(new ByteArrayInputStream(bOut.toByteArray()), bOut2);
            byte[] bTemp = bOut2.toByteArray();

            byte[] baInner = new byte[8];
            System.arraycopy(bTemp, 0, baInner, 0, 4);
            System.arraycopy(bTemp, bTemp.length - 4, baInner, 4, 4);
            int innerDataLength = bTemp.length - 8;

            for (int i = 0; i < 8; i++)
               baInner[i] ^= (byte) ((1 << i) & innerDataLength);

            padLen = baInner[0];

            secretKey.setSecret(baInner);
            bOut = new ByteArrayOutputStream();
            decr.decrypt(
               new ByteArrayInputStream(bTemp, 4, innerDataLength), bOut);
         }

         String ret = bOut.toString(PSCharSets.rxJavaEnc());
         // pad must be between 1 and 7 bytes, fix for bug id Rx-99-11-0049
         if ((padLen > 0) && (padLen  < 8))
            ret = ret.substring(0, ret.length() - padLen);

         return ret;
      } catch (Throwable e) {
         // we were returning null which caused a decryption error downstream
         // now we return ""
         return "";
      }
   }
     
   public static void setRxDir(File apath)
   {
        PathUtils.setRxDir(apath);
   }
     
}
