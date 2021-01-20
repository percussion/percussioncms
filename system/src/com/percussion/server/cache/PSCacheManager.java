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
package com.percussion.server.cache;

import com.percussion.cms.IPSConstants;
import com.percussion.design.objectstore.PSServerCacheSettings;
import com.percussion.server.IPSHandlerInitListener;
import com.percussion.server.IPSRequestHandler;
import com.percussion.server.IPSServerErrors;
import com.percussion.server.PSCachedResponse;
import com.percussion.server.PSConsole;
import com.percussion.server.PSServer;
import com.percussion.util.IPSHtmlParameters;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

/**
 * Singleton class to manage server-wide caching.  Is impelemented as a set of
 * {@link IPSCacheHandler} objects, each of which is responsible for handling
 * the caching of a certain class of requests.  Responses to requests are cached
 * and may be retrieved by a subsequent request.  The keys used to store and
 * retrieve the responses, as well as the automatic invalidation of stale or
 * dirty items in the cache is managed by the lower level handler.
 */
public class PSCacheManager implements IPSHandlerInitListener
{
   /**
    * Private ctor to enforce the singleton design pattern.  Creates a cache
    * manager with the supplied settings.  Use {@link #getInstance()} to obtain
    * the singleton instance of this class.  The first caller must call
    * {@link #init(PSServerCacheSettings)} before the instance is used.
    */
   private PSCacheManager()
   {

   }

   /**
    * Intializes the storage and aging settings for the manager.  Must be called
    * before the instance of the class is used and once before the server
    * intializes any application handlers.  May be called again to reinitalize
    * the manager with new settings.
    *
    * @param cacheSettings The server's cache settings to use for memory
    * management and aging time.  May not be <code>null</code>.  If
    * {@link PSServerCacheSettings#isEnabled()} returns <code>false</code>, no
    * caching will be performed.
    *
    * @throws IllegalArgumentException if <code>cacheSettings</code> is
    * @throws PSCacheException if Cache handlers fail to start.
    * <code>null</code>.
    */
   public void init(PSServerCacheSettings cacheSettings) throws PSCacheException
   {
      if (cacheSettings == null)
         throw new IllegalArgumentException("cacheSettings may not be null");

      if (!m_started && m_inited)
         throw new PSCacheException(IPSServerErrors.CACHE_START_FAILED);

      // create basic manager objects
      PSCacheMemoryManager oldMemMgr = null;
      PSCacheStatistics oldStats = null;
      boolean startup = false;
      boolean reinit = false;
      synchronized (m_initMonitor)
      {
         // determine if this is a start, restart, or shutdown
         if (!m_inited)
         {
            startup = true;
            PSConsole.printMsg(SUBSYSTEM, "Initializing cache");
         }
         else
         {
            reinit = true;
            if (cacheSettings.isEnabled())
            {
               PSConsole.printMsg(SUBSYSTEM,
                  "Restarting cache with new settings");
            }
            else if (m_cacheSettings.isEnabled())
              PSConsole.printMsg(SUBSYSTEM, "Stopping cache");
         }

         oldMemMgr = m_memoryManager;
         oldStats = m_stats;

         m_memoryManager = new PSCacheMemoryManager(cacheSettings);
         m_stats = new PSCacheStatistics(cacheSettings);
         m_cacheSettings = cacheSettings;
         m_inited = true;
      }

      // start, restart, or shutdown the handlers.
      if (startup)
      {
         // create the instances, which will be used to setup the
         // the tableChanged listener during server start up
         if(Boolean.getBoolean((String)PSServer.getServerProps().get("useXMLAssemblerCache")))
         addHandler(new PSAssemblerCacheHandler(cacheSettings));
         addHandler(new PSResourceCacheHandler(cacheSettings));
         
         // creates the item & folder relationship cache manager instances
         m_folderRelationships = PSFolderRelationshipCache.createInstance();
      }
      else if (reinit)
      {
         // temporarily remove all handlers
         List<PSCacheHandler> oldHandlers = removeHandlers();

         // reinitialize each handler and then
         // add it back to the handler list, synchronizing on the
         // m_handlerMonitor monitor during the add.
         Iterator<PSCacheHandler> handlers = oldHandlers.iterator();
         while (handlers.hasNext())
         {
            PSCacheHandler handler = (PSCacheHandler)handlers.next();
            handler.reinitialize(cacheSettings);
            // add it back to the list
            addHandler(handler);
         }
         
         // reinitialize item & folder relationship caching if needed
         resetFolderCache();
      }
      
      // now shutdown old managers
      if (oldMemMgr != null)
         oldMemMgr.shutdown();

      if (oldStats != null)
         oldStats.shutdown();
   }

   /**
    * Must be called before any requests will be cached.  Must be called
    * after the server has initialized all application handlers.
    * <code>init()</code> must be called before this method.  This method should
    * only be called once after the first call to <code>init()</code>.  It
    * should not be called after subsequent calls to <code>init()</code> are
    * made to enable, disable, or reinitialize the cache.
    *
    * @throws IllegalStateException if <code>init()</code> has not been called.
    */
   public void start()
   {
      checkInit();
      synchronized (m_handlerMonitor)
      {
         if (!m_started)
         {
            try
            {
               m_memoryManager.cleanDiskCache();
               PSCacheHandler handler = null;
               Iterator<PSCacheHandler> handlers = null;
               handlers = m_cacheHandlers.iterator();
               while (handlers.hasNext())
               {
                  handler = (PSCacheHandler)handlers.next();
                  handler.start();
                  m_started = true;
                  PSConsole.printMsg(SUBSYSTEM, "Started " + handler.getType() + 
                     " cache");
               }
               
               // creates the item & folder relationship cache if enabled
               resetFolderCache();
            }
            catch (PSCacheException e)
            {
               // print to console and log
               m_started = false;
               PSCacheException ex = new PSCacheException(
               IPSServerErrors.CACHE_NOT_STARTED, e.getLocalizedMessage());
               PSConsole.printMsg(SUBSYSTEM, ex);
            }
         }
      }
   }

   /**
    * Checks if Caching has started or not.
    * <br>
    * <em>IMPORTANT Server always starts the cache handlers but that does not
    * mean that caching has started.
    * It is only when cache settings are enabled
    * and of course there are no handler start exceptions is when actual
    * caching begins.
    * So there is a difference in starting the cache handlers
    * and the actual start of caching.
    * @return true if caching started or else false.
    */
   public boolean hasCacheStarted()
   {
      return (m_cacheSettings.isEnabled() && m_started);
   }

   /**
    * Gets the singleton instance of this class.  The first caller must also
    * call {@link #init(PSServerCacheSettings) init} before the instance is used,
    * and should ensure that a reference to that instance is maintained to avoid
    * garbage collection.
    *
    * @return The singleton instance of this class, never <code>null</code>.
    */
   public static PSCacheManager getInstance()
   {
      if (ms_cacheManager == null)
         ms_cacheManager = new PSCacheManager();

      return ms_cacheManager;
   }

   /**
    * Allows the caller to determine if the cache manager has been initialized
    * before trying to access it. 
    * 
    * @return <code>true</code> if {@link #getInstance()} will succeed,
    * <code>false</code> otherwise.
    */
   public static boolean isAvailable()
   {
      return ms_cacheManager != null;
   }


   /**
    * Returns the first cache handler that will cache the request in the 
    * supplied cache context.  Determines this by calling 
    * {@link IPSCacheHandler#isRequestCacheable(context) isRequestCacheable()} 
    * on each handler, so the caller will not need to make this check before 
    * calling 
    * {@link IPSCacheHandler#storeResponse(PSCacheContext, PSCachedResponse)} or 
    * {@link IPSCacheHandler#retrieveResponse(PSCacheContext)}.
    *
    * @param context The current runtime context used to determine if the 
    * current request is cacheable.  May not be <code>null</code>.
    *
    * @return The cache handler, or <code>null</code> if this request is not
    * cacheable.
    *
    * @throws IllegalArgumentException if <code>context</code> is
    * <code>null</code>.
    * @throws IllegalStateException if the <code>init()</code> method has not
    * been called.
    */
   public IPSCacheHandler getCacheHandler(PSCacheContext context)
   {
      if (context == null)
         throw new IllegalArgumentException("context may not be null");
      checkInit();

      // see if the page (assembler & resource) cache is off for the request
      String isCacheOff =
         context.getRequest().getParameter(
            IPSHtmlParameters.SYS_IS_PAGE_CACHE_OFF);
      if (IPSConstants.BOOLEAN_TRUE.equals(isCacheOff))
         return null;

      IPSCacheHandler handler = null;
      Iterator<PSCacheHandler> handlers = getCacheHandlers();
      while (handlers.hasNext() && handler == null)
      {
         IPSCacheHandler test = (IPSCacheHandler)handlers.next();
         if (test.isRequestCacheable(context))
            handler = test;
      }
      return handler;
   }

   /**
    * Get the first cache handler that will cache the specified type of request.  
    * Comparison is made case-insensitivily.  See 
    * {@link IPSCacheHandler#getType()} for more info.
    * 
    * @param type The type, may not be <code>null</code> or empty.
    * 
    * @return The handler for the specified type, or <code>null</code> if no
    * handler is found or if caching is not enabled.
    */
   public IPSCacheHandler getCacheHandler(String type)
   {
      if (type == null || type.trim().length() == 0)
         throw new IllegalArgumentException("type may not be null or empty");
      
      IPSCacheHandler handler = null;
      Iterator<PSCacheHandler> handlers = getCacheHandlers();
      while (handlers.hasNext() && handler == null)
      {
         IPSCacheHandler test = (IPSCacheHandler)handlers.next();
         if (test.getType().equalsIgnoreCase(type))
            handler = test;
      }
      
      return handler;
   }

   /**
    * Get the supported cache types.
    * 
    * @return An iterator over zero or more enabled cache types as 
    * <code>String</code> objects.  Each type identifies an active 
    * cache handler responsible for the specified type of request, 
    * case-insensitively. Never <code>null</code>.
    */
   public Iterator<String> getCacheTypes()
   {
      return m_handlerTypes.iterator();
   }
   
   /**
    * Used to register for the appropriate change events so that dirty cache
    * items may be detected and automatically flushed.
    *
    * @param requestHandler If the handler is of the correct type, one or more
    * of the lower level {@link PSCacheHandler} objects may register themselves
    * as listeners of certain change events.  May not be <code>null</code>.
    *
    * @throws IllegalArgumentException if <code>requestHandler</code> is
    * <code>null</code>.
    * @throws IllegalStateException if the <code>init()</code> method has not
    * been called.
    */
   public void initHandler(IPSRequestHandler requestHandler)
   {
      if (requestHandler == null)
         throw new IllegalArgumentException("requestHandler may not be null");

      checkInit();

      m_folderRelationships.getItemCache().initNotifyListener(requestHandler);
      
      Iterator<PSCacheHandler> handlers = m_cacheHandlers.iterator();
      while (handlers.hasNext())
      {
         PSCacheHandler handler = (PSCacheHandler)handlers.next();
         handler.initHandler(requestHandler);
      }
   }

   /**
    * See {@link IPSHandlerInitListener#shutdownHandler(IPSRequestHandler)}
    * for more information.
    * 
    * @throws IllegalStateException if the <code>init()</code> method has not
    * been called.
    */
   public void shutdownHandler(IPSRequestHandler requestHandler)
   {
      if (requestHandler == null)
         throw new IllegalArgumentException("requestHandler may not be null");

      checkInit();

      Iterator<PSCacheHandler> handlers = m_cacheHandlers.iterator();
      while (handlers.hasNext())
      {
         PSCacheHandler handler = (PSCacheHandler)handlers.next();
         handler.shutdownHandler(requestHandler);
      }
   }

   /**
    * Gets the instance of the memory manager that will manage memory and disk
    * usage for the server.
    *
    * @return The memory manager, never <code>null</code>.
    *
    * @throws IllegalStateException if the <code>init()</code> method has not
    * been called.
    */
   PSCacheMemoryManager getMemoryManager()
   {
      checkInit();
      return m_memoryManager;
   }

   /**
    * Returns a point in time snapshot of the cache statistics.
    *
    * @return The statistics snapshot, never <code>null</code>.
    *
    * @throws IllegalStateException if the <code>init()</code> method has not
    * been called.
    */
   public PSCacheStatisticsSnapshot getStatisticsSnapShot()
   {
      checkInit();
      return m_stats.getSnapshot();
   }

   /**
    * Returns the current cache settings of this instance.
    *
    * @return The cache settings, never <code>null</code>.
    *
    * @throws IllegalStateException if the <code>init()</code> method has not
    * been called.
    */
   public PSServerCacheSettings getServerCacheSettings()
   {
      checkInit();
      synchronized(m_initMonitor)
      {
         return m_cacheSettings;
      }
   }

   /**
    * Returns the object that tracks statistics for all caches.
    *
    * @return The statistics object, never <code>null</code>.
    *
    * @throws IllegalStateException if the <code>init()</code> method has not
    * been called.
    */
   PSCacheStatistics getStatistics()
   {
      checkInit();
      return m_stats;
   }

   /**
    * Clears all cached responses and frees any related resources.
    */
   public void shutdown()
   {
      PSConsole.printMsg(SUBSYSTEM, "Shutting down cache.");

      // shutdown each handler
      synchronized(m_handlerMonitor)
      {
         Iterator<PSCacheHandler> handlers = m_cacheHandlers.iterator();
         while (handlers.hasNext())
         {
            PSCacheHandler handler = (PSCacheHandler)handlers.next();
            handler.shutdown();
         }
         m_cacheHandlers.clear();
      }

      // now shutdown the stats and memory manager
      synchronized(m_initMonitor)
      {
         m_stats.shutdown();
         m_memoryManager.shutdown();
      }
   }

   /**
    * Flushes all cached responses.
    *
    * @throws IllegalStateException if the <code>init()</code> method has not
    * been called.
    */
   public void flush()
   {
      checkInit();

      Iterator<PSCacheHandler> handlers = getCacheHandlers();
      while (handlers.hasNext())
      {
         PSCacheHandler handler = (PSCacheHandler)handlers.next();
         handler.flush();
      }
   }

   /**
    * Flushes cache items based on the supplied keys.  The keys supplied may
    * identify one or more cached items.  Any items identified by the supplied
    * keys are flushed.
    *
    * @param keys A map of key names and their values.  The keys that are
    * supported is determined by the low-level handler.  The keys are passed to
    * the <code>flush()</code> method of each handler.  May not be
    * <code>null</code>.
    *
    * @throws IllegalArgumentException if <code>keys</code> is
    * <code>null</code>.
    * @throws IllegalStateException if the <code>init()</code> method has not
    * been called.
    *
    * @see PSCacheHandler and any derived classes for more information.
    */
   public void flush(Map keys)
   {
      if (keys == null)
         throw new IllegalArgumentException("keys may not be null");

      checkInit();

      Iterator handlers = getCacheHandlers();
      while (handlers.hasNext())
      {
         PSCacheHandler handler = (PSCacheHandler)handlers.next();
         handler.flush(keys);
      }
   }

   /**
    * Flushes cache items stored by the supplied application.
    *
    * @param appName The name of the application to flush, may not be
    * <code>null</code> or empty.
    *
    * @throws IllegalArgumentException if <code>appName</code> is invalid.
    * @throws IllegalStateException if the <code>init()</code> method has not
    * been called.
    */
   public void flushApplication(String appName)
   {
      if (appName == null || appName.trim().length() == 0)
         throw new IllegalArgumentException("appName may not be null or empty");

      checkInit();

      Iterator handlers = getCacheHandlers();
      while (handlers.hasNext())
      {
         PSCacheHandler handler = (PSCacheHandler)handlers.next();
         handler.flushApplication(appName);
      }
   }

   /**
    * Flushes all pages cached by the supplied session id.
    *
    * @param sessionId Identifies the session for which all cached pages should
    * be flushed, may not be <code>null</code> or empty.
    *
    * @throws IllegalArgumentException if <code>sessionId</code> is invalid.
    * @throws IllegalStateException if the <code>init()</code> method has not
    * been called.
    */
   public void flushSession(String sessionId)
   {
      if (sessionId == null || sessionId.trim().length() == 0)
         throw new IllegalArgumentException(
            "sessionId may not be null or empty");

      checkInit();

      Iterator handlers = getCacheHandlers();
      while (handlers.hasNext())
      {
         PSCacheHandler handler = (PSCacheHandler)handlers.next();
         handler.flushSession(sessionId);
      }
   }

   /**
    * Reinitialize the folder cache according to current cache configuration. It
    * flushes and folder cache and reload from the repository if the folder
    * cache is enabled; otherwise it stops or shuts down the folder cache if 
    * the folder cache is disabled.
    * <p>
    * Note: Must call {@link #init(PSServerCacheSettings)}to create the
    * instances for folder cache first.
    * 
    * @throws IllegalStateException
    *            if the folder cache instances have not been created yet.
    * @throws PSCacheException
    *            if an error occurs.
    */
   void resetFolderCache() throws PSCacheException
   {
      if (m_folderRelationships == null)
         throw new IllegalStateException(
               "folder cache instances must be created first");
      
      m_folderRelationships.reinitialize(m_cacheSettings
            .isFolderCacheEnabled()); 
   }
   
   /**
    * Formats and logs the specified message to the server console using
    * {@link #DEBUG_SUBSYSTEM}) as the subsystem.  Will ignore the message if
    * not running in debug mode.
    *
    * @param message The message to write, may not be <code>null</code> or
    * empty.
    *
    * @throws IllegalArgumentException if <code>message</code> is
    * <code>null</code> or empty.
    */
   public void logDebugMessage(String message)
   {
      if (message == null || message.trim().length() == 0)
         throw new IllegalArgumentException("message may not be null or empty");
      if (m_debugMode)
      {
         synchronized(m_debugMontior)
         {
            PSConsole.printMsg(DEBUG_SUBSYSTEM, message);
         }
      }
   }

   /**
    * Determines if debug logging is enabled.
    *
    * @return <code>true</code> if debug messages are written to the console,
    * <code>false</code> if not.
    */
   public boolean isDebugLoggingEnabled()
   {
      return m_debugMode;
   }

   /**
    * Turns debug logging on and off.  May be called regardless of whether or
    * not caching is currently enabled.
    *
    * @param enabled If <code>true</code>, debug messages will be written to
    * the console.  If <code>false</code>, they will not.
    *
    * @see #isDebugLoggingEnabled()
    * @see #logDebugMessage(String)
    */
   public void setIsDebugLoggingEnabled(boolean enabled)
   {
      m_debugMode = enabled;
   }

   /**
    * Checks to see if the <code>init</code> method has been called.  If not,
    * throws an exception.  Ensures callers will block while initilzation is
    * happening.
    *
    * @throws IllegalStateException if the <code>init()</code> method has not
    * been called.
    */
   private void checkInit()
   {
      synchronized(m_initMonitor)
      {
         if (!m_inited)
            throw new IllegalStateException(
               "The Cache Manager must be intialized before it can be used.");
      }
   }

   /**
    * Gets an iterator over <code>0</code> or more <code>PSCacheHandler</code>
    * objects.  Iterator is backed by a copy of the handler list so there will
    * not be concurrent modification exceptions.
    *
    * @return The iterator, never <code>null</code>.  If cache is not currently
    * enabled, will be empty.
    */
   private Iterator<PSCacheHandler> getCacheHandlers()
   {
      List<PSCacheHandler> handlers = new ArrayList<PSCacheHandler>();
      if (getServerCacheSettings().isEnabled())
      {
         synchronized (m_handlerMonitor)
         {
            if (m_started)
               handlers.addAll(m_cacheHandlers);
         }
      }
      return handlers.iterator();
   }

   /**
    * Removes all handlers from the handler list, and returns a new list
    * containing the removed handlers.
    *
    * @return The list, never <code>null</code>, may be empty.
    */
   private List<PSCacheHandler> removeHandlers()
   {
      List<PSCacheHandler> handlers = new ArrayList<PSCacheHandler>();
      synchronized (m_handlerMonitor)
      {
         handlers.addAll(m_cacheHandlers);
         m_cacheHandlers.clear();
      }
      m_handlerTypes.clear();
      
      return handlers;
   }

   /**
    * Adds a handler to the handler list.
    *
    * @param handler The handler, assumed not <code>null</code>.
    * 
    * @throws PSCacheException if a handler with the same type has already been
    * added.
    */
   private void addHandler(PSCacheHandler handler) throws PSCacheException
   {
      if (!m_handlerTypes.add(handler.getType()))
      {
         throw new PSCacheException(IPSServerErrors.CACHE_HANDLER_DUPE_TYPE, 
            handler.getType());
      }
      
      synchronized (m_handlerMonitor)
      {
         m_cacheHandlers.add(handler);
      }
   }

   /**
    * Constant to use for subsystem when writing messages to the console.
    */
   public static final String SUBSYSTEM = "Cache";

   /**
    * Constant to use for subsystem when writing debug messages to the console.
    * @see #logDebugMessage(String)
    */
   private static final String DEBUG_SUBSYSTEM = SUBSYSTEM + " [debug]";

   /**
    * The singleton instance of this class.  Initialized by the first call to
    * {@link #getInstance()}, never <code>null</code> after that.
    */
   private static PSCacheManager ms_cacheManager = null;

   /**
    * Flag to indicate if running in debug mode, and if messages sent to
    * {@link #logDebugMessage(String)} should print to the console.  If
    * <code>true</code>, they will print out, if <code>false</code> they will
    * not.  Intially <code>false</code>, may be modified by a call to
    * {@link #setIsDebugLoggingEnabled(boolean)}.
    */
   private boolean m_debugMode = false;

   /**
    * Monitor object to synchronize console logging to avoid interleaving of
    * messages.  Never <code>null</code>, immutable.
    */
   private Object m_debugMontior = new Object();

   /**
    * The setting provided to the <code>init()</code> method.  Never
    * <code>null</code> after the first call to that method, may be replaced by
    * subsequent calls to <code>init()</code>.
    */
   private PSServerCacheSettings m_cacheSettings;

   /**
    * The object used to track the statistics across all cache handlers. Never
    * <code>null</code> after the first call to that method, may be replaced by
    * subsequent calls to <code>init()</code>.
    */
   private PSCacheStatistics m_stats = null;

   /**
    * The object used to manage memory and disk resources across all cache
    * handlers. Never <code>null</code> after the first call to that method, may
    * be replaced by subsequent calls to <code>init()</code>.
    */
   private PSCacheMemoryManager m_memoryManager = null;

   /**
    * The list of cache handlers, never <code>null</code>, handlers are added
    * and removed during calls to <code>init()</code>.
    */
   private List<PSCacheHandler> m_cacheHandlers = new ArrayList<PSCacheHandler>();

   /**
    * Flag to indicate if <code>init</code> has been called at least once.
    */
   private boolean m_inited = false;

   /**
    * Object to synchronize access to the <code>m_inited</code> flag. Never
    * <code>null</code>.
    */
   private Object m_initMonitor = new Object();

   /**
    * Object to synchronize access to <code>m_cacheHandlers</code>. Never
    * <code>null</code>.
    */
   private Object m_handlerMonitor = new Object();

   /**
    * Flag to indicate if <code>start</code> has been called at least once.
    */
   private boolean m_started = false;
   
   /**
    * The types of each supported cache handler.  Intialized during the 
    * <code>init()</code> method, never <code>null</code> or modified after
    * that.
    */
   private Set<String> m_handlerTypes = new HashSet<String>();
   
   /**
    * The folder relationship cache. Initialized by {@link #start()} 
    */
   private PSFolderRelationshipCache m_folderRelationships = null;
}
