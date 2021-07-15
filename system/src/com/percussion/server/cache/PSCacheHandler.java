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

package com.percussion.server.cache;

import com.percussion.design.objectstore.PSServerCacheSettings;
import com.percussion.server.IPSRequestHandler;
import com.percussion.server.IPSServerErrors;
import com.percussion.server.PSBaseResponse;
import com.percussion.server.PSCachedResponse;
import com.percussion.util.PSCharSetsConstants;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.w3c.dom.Document;

/**
 * Base class for all cache handlers, implements common functionality.  Each
 * cache handler is responsible for creating and managing its own cache for a
 * specific set of request types.
 */
public abstract class PSCacheHandler implements IPSCacheHandler
{
   /**
    * Called by derived classes to initialize common settings.
    * 
    * @param keySize The size of the array that must be used to specify a set
    * of keys that uniquely identifies an item in the cache. Must be greater 
    * than <code>0</code>.
    * @param cacheSettings The server cache settings.  May not be 
    * <code>null</code>.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    * @throws IllegalStateException if the <code>PSCacheManager</code> has not 
    * been initialized.
    */
   PSCacheHandler(int keySize, PSServerCacheSettings cacheSettings) 
   {
      if (keySize <= 0)
         throw new IllegalArgumentException("keySize must be greater than 0");
         
      if (cacheSettings == null)
         throw new IllegalArgumentException("cacheSettings may not be null");
         
      m_cacheSettings = cacheSettings;
      initCache(keySize);
   }

   /**
    * Reinitializes this handler with the specified settings.  Will flush and 
    * shutdown the current cache and create a new cache using the new settings.
    * All cache access will be blocked during execution of this method.
    * 
    * @param cacheSettings The server cache settings.  May not be 
    * <code>null</code>.  A copy of the passed object is kept locally.  Changes
    * to this object after it is passed in will not be recognized. You must 
    * pass it again to this method.
    * 
    * @throws IllegalArgumentException if <code>cacheSettings</code> is
    * <code>null</code>
    * @throws IllegalStateException if the <code>PSCacheManager</code> has not 
    * been initialized.
    */
   void reinitialize(PSServerCacheSettings cacheSettings) 
   {
      if (cacheSettings == null)
         throw new IllegalArgumentException("cacheSettings may not be null");
      
      PSMultiLevelCache oldCache = null;
      PSCacheStatistics stats = null;
      PSCacheMemoryManager memMgr = null;
      
      synchronized (m_cacheMonitor)
      {
         if (m_shutdown)
            return;
               
         m_cacheSettings = cacheSettings;
         
         oldCache = m_cache;
         stats = m_stats;
         memMgr = m_memoryManager;
         initCache(oldCache.getKeySize());
      }
      
      oldCache.removeCacheModifiedListener(memMgr);
      oldCache.removeCacheModifiedListener(stats);
      oldCache.removeCacheAccessedListener(stats);
      oldCache.shutdown();
   }
   
   // see IPSCacheHandler interface
   public boolean isResponseCacheable(PSBaseResponse response)
   {
      if (response == null)
         throw new IllegalArgumentException("response may not be null");
      
      return isDataCacheable(response.getContentLength());      
   }
   
   // see IPSCacheHandler interface
   public PSCachedResponse retrieveResponse(PSCacheContext context)
      throws PSCacheException
   {
      if (context == null)
         throw new IllegalArgumentException("context may not be null");

      if (!isRequestCacheable(context))
         throw new IllegalArgumentException("current request is not cacheable");

      // extract keys and get response from cache
      Object[] keys = getKeys(context);
      PSCachedResponse response = null;
      if (keys != null)
      {
         PSMultiLevelCache cache = getCache();
         if (cache != null)
         {
            response = (PSCachedResponse)cache.retrieveItem(keys, 
               ITEM_TYPE_RESPONSE);
            if (response != null)
            {
               logDebugMessage("retrieved response for item with keys: ", keys);
            }
            else
            {
               logDebugMessage(
                  "failed to located response for item with keys: ", keys);
            }
         }

      }
      return response;
   }

   /**
    * Caches the supplied response object.
    *
    * See base class for more info.
    */
   public void storeResponse(PSCacheContext context, PSCachedResponse response)
      throws PSCacheException
   {
      if (context == null)
         throw new IllegalArgumentException("context may not be null");
      if (response == null)
         throw new IllegalArgumentException("response may not be null");

      if (!isRequestCacheable(context))
         throw new IllegalArgumentException("current request is not cacheable");

      Object[] keys = getKeys(context);
      if (keys != null)
      {
         // if response is not cacheable, just return
         if (!isResponseCacheable(response))
         {
            logDebugMessage("response not cacheable for item with keys: ",
               keys);
            return;
         }

         PSMultiLevelCache cache = getCache();
         if (cache != null)
         {
            cache.addItem(keys, response, response.getContentLength(), 
               ITEM_TYPE_RESPONSE);

            logDebugMessage("storing response for item with keys: ", keys);
         }
      }
   }
   
   // see IPSCacheHandler interface
   public Document retrieveDocument(PSCacheContext context) 
      throws PSCacheException
   {
      if (context == null)
         throw new IllegalArgumentException("context may not be null");

      if (!isRequestCacheable(context))
         throw new IllegalArgumentException("current request is not cacheable");

      // extract keys and get document from cache
      Object[] keys = getKeys(context);
      Document doc = null;
      if (keys != null)
      {
         PSMultiLevelCache cache = getCache();
         if (cache != null)
         {
            byte[] bytes = (byte[])cache.retrieveItem(keys, ITEM_TYPE_DOCUMENT);
            if (bytes != null)
            {
               try
               {
                  // may have cached an empty document, so check for that
                  if (bytes.length == 0)
                     doc = PSXmlDocumentBuilder.createXmlDocument();
                  else
                     doc = PSXmlDocumentBuilder.createXmlDocument(
                        new ByteArrayInputStream(bytes), false);
               }
               catch (Exception e)
               {
                  throw new PSCacheException(
                     IPSServerErrors.CACHE_UNEXPECTED_EXCEPTION, 
                     e.getLocalizedMessage());
               }
               logDebugMessage("retrieved document for item with keys: ", keys);
            }
            else
            {
               logDebugMessage(
                  "failed to located document for item with keys: ", keys);
            }
         }

      }
      return doc;      
   }
   
   // see IPSCacheHandler interface
   public void storeDocument(PSCacheContext context, Document doc)
      throws PSCacheException
   {
      if (context == null)
         throw new IllegalArgumentException("context may not be null");
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");

      if (!isRequestCacheable(context))
         throw new IllegalArgumentException("current request is not cacheable");

      Object[] keys = getKeys(context);
      if (keys != null)
      {
         
         
         try
         {
            byte[] data;
            
            /*
             * if doc is empty, store an emtpy byte array.  Serializing an empty
             * doc may not produce an empty byte array (if it writes the xml tag),
             * and this will cause problems in retrieveDocument()
             */
            if (doc.getDocumentElement() == null)
               data = new byte[0];
            else
               data = PSXmlDocumentBuilder.toString(doc).getBytes(
                  PSCharSetsConstants.rxStdEnc());         
         
            // if doc is not cacheable, just return         
            if (!isDataCacheable(data.length))
            {
               logDebugMessage("document not cacheable for item with keys: ",
                  keys);
               return;
            }

            PSMultiLevelCache cache = getCache();
            if (cache != null)
            {
               cache.addItem(keys, data, data.length, ITEM_TYPE_DOCUMENT);

               logDebugMessage("storing document for item with keys: ", keys);
            }
         }
         catch (UnsupportedEncodingException e)
         {
            // would never happen
            throw new RuntimeException("Cannot store doc in cache: " + 
               e.getLocalizedMessage());
         }
      }      
   }
      
   // see IPSCacheHandler interface
   public PSCachedResultPage retrieveMergedResults(PSCacheContext context)
      throws PSCacheException
   {
      if (context == null)
         throw new IllegalArgumentException("context may not be null");

      if (!isRequestCacheable(context))
         throw new IllegalArgumentException("current request is not cacheable");

      // extract keys and get document from cache
      Object[] keys = getKeys(context);
      PSCachedResultPage data = null;
      if (keys != null)
      {
         PSMultiLevelCache cache = getCache();
         if (cache != null)
         {
            data = (PSCachedResultPage)cache.retrieveItem(keys, ITEM_TYPE_MERGED);
            if (data != null)
            {
               logDebugMessage("retrieved merged result for item with keys: ", 
                  keys);
            }
            else
            {
               logDebugMessage(
                  "failed to located merged results for item with keys: ", keys);
            }
         }
      }
      return data;               
   }
   
   // see IPSCacheHandler interface
   public void storeMergedResults(PSCacheContext context, PSCachedResultPage data)
      throws PSCacheException
   {
      if (context == null)
         throw new IllegalArgumentException("context may not be null");
      if (data == null)
         throw new IllegalArgumentException("data may not be null");

      if (!isRequestCacheable(context))
         throw new IllegalArgumentException("current request is not cacheable");

      Object[] keys = getKeys(context);
      if (keys != null)
      {
         // if data is not cacheable, just return
         if (!isDataCacheable(data.getLength()))
         {
            logDebugMessage("merged results not cacheable for item with keys: ",
               keys);
            return;
         }

         PSMultiLevelCache cache = getCache();
         if (cache != null)
         {
            cache.addItem(keys, data, data.getLength(), ITEM_TYPE_MERGED);

            logDebugMessage("storing merged results for item with keys: ", 
               keys);
         }
      }      
   }
      
   // see IPSCacheHandler interface
   public boolean isResultDataCacheable(byte[] bytes)
   {
      return isDataCacheable(bytes.length);
   }

   /**
    * Determines if the specified size is cacheable.
    * 
    * @param size The size of the data to cache.
    * 
    * @return <code>true</code> if it is cacheable, <code>false</code>
    * otherwise.
    */
   private boolean isDataCacheable(long size)
   {

      boolean isCacheable = true;
   
      long maxPageSize = m_cacheSettings.getMaxPageSize();   
      if (maxPageSize != -1 && size > maxPageSize)
         isCacheable = false;
   
      return isCacheable;
   }

   /**
    * Allows cache handler to perform any startup operations that must wait
    * until after the server has intialized the application handlers.  No
    * requests or listeners event will occur until after this method has 
    * completed.  Derived classes should override this method to perform any
    * startup operations.
    * @throws PSCacheException 
    */
   @SuppressWarnings("unused") void start() throws PSCacheException
   {
      //noop
   }
   
   /**
    * Used to register for the appropriate change events so that dirty cache
    * items may be detected and automatically flushed. 
    * 
    * @param requestHandler If the handler is of the correct type, derived
    * classes may register themselves as listeners of certain change events.  
    * May not be <code>null</code>.
    * 
    * @throws IllegalArgumentException if <code>requestHandler</code> is 
    * <code>null</code>.
    */
   abstract void initHandler(IPSRequestHandler requestHandler);

   /**
    * Notifies the listener that the hander is is shutting down, so that it may 
    * free any resources.
    * 
    * @param requestHandler The request handler that is shutting down.  May not 
    * be <code>null</code>.
    */
   abstract void shutdownHandler(IPSRequestHandler requestHandler);
   
   /**
    * Clears all cached responses and frees any related resources.  Derived 
    * classes should override this method if they have any resources to release
    * at shutdown, first calling <code>super()</code> in order to free any 
    * commonly held resources by this class.  Once shutdown, a handler cannot be 
    * used again.
    */
   void shutdown()
   {
      PSMultiLevelCache cache = null;
      synchronized(m_cacheMonitor)
      {
         if (m_shutdown)
            return;
         m_shutdown = true;
         cache = m_cache;
         m_cache = null;
      }
      cache.removeCacheModifiedListener(m_memoryManager);
      cache.removeCacheModifiedListener(m_stats);
      cache.removeCacheAccessedListener(m_stats);
      cache.shutdown(); 
      
   }
   
   /**
    * Flushes all cached responses.
    */
   void flush()
   {
      PSMultiLevelCache cache = getCache();
      if (cache != null)
         cache.flush();
   }
   
   /**
    * Flushes cache items based on the supplied keys.  The keys supplied may
    * identify one or more cached items.  Any items identified by the supplied
    * keys are flushed.
    * 
    * @param keys A map of key names and their values.  The keys that are
    * supported is determined by the derived class.   For a handler to process
    * the flush request, all keys required by that handler must have an entry
    * in the map.  To omit a key, use <code>null</code> or an empty 
    * <code>String</code> for the value of the entry.  If keys are not supported
    * by the handler, the call is silently ignored.
    * 
    * @throws IllegalArgumentException if <code>keys</code> is 
    * <code>null</code>.
    */
   abstract void flush(Map keys);

   /**
    * Flushes all pages cached by the supplied application.
    * 
    * @param appName The name of the application to flush, may not be
    * <code>null</code> or empty.
    * 
    * @throws IllegalArgumentException if <code>appName</code> is invalid.
    */
   abstract void flushApplication(String appName);
   
   /**
    * Flushes all pages cached by the supplied session id.  This method will
    * not flush anything by default.  Derived classes should override this
    * method if they cache based on session id.
    * 
    * @param sessionId Identifies the session for which all cached pages should
    * be flushed, may not be <code>null</code> or empty.
    * 
    * @throws IllegalArgumentException if <code>sessionId</code> is invalid.
    */
   void flushSession(@SuppressWarnings("unused") String sessionId)
   {
   }
   
   /**
    * Get the cache used by this handler.
    * 
    * @return The cache, may be <code>null</code> if <code>shutdown</code> has
    * been called.
    */
   protected PSMultiLevelCache getCache()
   {
      synchronized(m_cacheMonitor)
      {
         return m_cache;
      }
   }

   /**
    * Logs a flush debug message with the provided keys.
    *
    * @param keys The keys, be <code>null</code> if flushing the entire cache.
    */
   protected void logFlushMessage(Object[] keys)
   {
      String msg = "Flushing " + getType() + " cache";
      if (keys != null)
         msg += " with keys:  ";
      logDebugMessage(msg, keys);
   }

   /**
    * Logs the specified debug message appending the <code>String</code>
    * representation of the keys onto the message.
    *
    * @param message The message, may not be <code>null</code> or empty.
    * @param keys The keys, may be <code>null</code> if message is not key
    * specific.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    */
   protected void logDebugMessage(String message, Object[] keys)
   {
      if (message == null)
         throw new IllegalArgumentException("message may not be null");
      
      if (PSCacheManager.getInstance().isDebugLoggingEnabled())
      {
         if (keys != null)
            message += PSCacheItem.toString(keys);
         PSCacheManager.getInstance().logDebugMessage(message);
      }
   }
   
   /**
    * Get the keys used to cache or retrieve a page using the supplied context.
    * 
    * @param context The current cache context, may not be <code>null</code>.
    * 
    * @return An array of <code>String</code> objects that comprise the cache
    * key for this item, or <code>null</code> if all required keys are not
    * supplied by the request.
    * 
    * @throws IllegalArgumentException if <code>context</code> is 
    * <code>null</code>.
    * @throws PSCacheException if there are any errors.
    */
   protected abstract Object[] getKeys(PSCacheContext context)  
      throws PSCacheException;
   
   /**
    * Creates the cache, and gets the memory and stats managers and add them
    * as listeners to the cache.
    * 
    * @param keySize The size of the array that must be used to specify a set
    * of keys that uniquely identifies an item in the cache. Assumed to be
    * greater than <code>0</code>.
    * 
    * @throws IllegalStateException if the cache manager has not been 
    * intialized.
    */
   private void initCache(int keySize)
   {
      m_cache = new PSMultiLevelCache(keySize, 
         m_cacheSettings.getAgingTime());
      
      PSCacheManager mgr = PSCacheManager.getInstance();
      m_stats = mgr.getStatistics();
      m_memoryManager = mgr.getMemoryManager();
      // need to add stats before memory manager so it gets add events first
      m_cache.addCacheModifiedListener(m_stats);
      m_cache.addCacheAccessedListener(m_stats);
      m_cache.addCacheModifiedListener(m_memoryManager);
   }
   
   /**
    * Constant to indicate a cached item's object is a response object.
    */
   private static final String ITEM_TYPE_RESPONSE = "response";
   
   /**
    * Constant to indicate a cached item's object is a result document.
    */
   private static final String ITEM_TYPE_DOCUMENT = "document";
   
   /**
    * Constant to indicate a cached item's object is a merged result.
    */   
   private static final String ITEM_TYPE_MERGED = "merged";
   
   /**
    * The cache settings the cache this handler and its cache.  Set during 
    * construction and modified by calls to <code>reinitialize()</code>, never 
    * <code>null</code> after construction.
    */
   private PSServerCacheSettings m_cacheSettings;

   /**
    * The cache used by this handler.  Initialized during construction, replaced
    * during calls to <code>reinitialize</code>, and set to <code>null</code>
    * during <code>shutdown</code>
    */
   private PSMultiLevelCache m_cache;
 
   /**
    * Object used to synchronize access to the handler's cache.  Never 
    * <code>null</code>, immutable.
    */
   private Object m_cacheMonitor = new Object();  
   
   /**
    * Latch to indicate shutdown
    */
   private boolean m_shutdown = false;
   
   /**
    * The object used to track cache statistics for all caches, set during
    * {@link #initCache(int)}, modified by calls to 
    * {@link #reinitialize(PSServerCacheSettings)}.  Never <code>null</code> 
    * after it is first initialized.
    */
   private PSCacheStatistics m_stats;
   
   /**
    * The object used to manage memory resources for all caches, set during
    * {@link #initCache(int)}, modified by calls to 
    * {@link #reinitialize(PSServerCacheSettings)}.  Never <code>null</code> 
    * after it is first initialized.
    */
   private PSCacheMemoryManager m_memoryManager;
}
