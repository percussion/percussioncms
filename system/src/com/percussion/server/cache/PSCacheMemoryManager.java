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

import com.percussion.design.objectstore.PSServerCacheSettings;
import com.percussion.error.PSInternalError;
import com.percussion.server.IPSServerErrors;
import com.percussion.server.PSConsole;
import com.percussion.server.PSServer;
import com.percussion.server.PSServerLogHandler;

import java.io.File;
import java.io.NotSerializableException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;



/**
 * Monitors disk and memory usage by cached items and causes the least recently
 * used items to be written to disk when memory use reaches a predefined
 * threshold, and the oldest disk items to be flushed as disk usage reaches its
 * limit. The cache manager ensures there is one instance of this class used
 * by all cache instances. Memory is managed for the entire server across all
 * cache instances. Set this as a listener on each cache so it can keep track
 * of all items and whether they are in memory or on disk.
 */
class PSCacheMemoryManager extends Thread
   implements IPSCacheAccessedListener, IPSCacheModifiedListener
{
   /**
    * Creates a thread to monitor cache memory and disk usage. If caching is
    * not enabled, will do nothing.
    *
    * @param cacheSettings The cache settings that are used to determine if
    * caching is enabled, and to provide memory and disk usage values.  May not
    * be <code>null</code>.
    *
    * @throws IllegalArgumentException if <code>cacheSettings</code> is null.
    */
   public PSCacheMemoryManager(PSServerCacheSettings cacheSettings)
   {
      if (cacheSettings == null)
        throw new IllegalArgumentException("cacheSettings cannot be null");

      setName("Cache Memory Manager");
      setDaemon(true);

      /**
       * Cache settings are initialized once, since the cache manager recreates
       * a new memory manager each time the cache settings are changed.
       */
      m_isEnabled = cacheSettings.isEnabled();
      m_maxMemoryUsage = cacheSettings.getMaxMemoryUsage();
      m_maxDiskUsage = cacheSettings.getMaxDiskUsage();

      if (m_isEnabled)
      {
         log("Starting cache memory manager.");
         start();
      }
   }

   /**
    * Starts a thread to monitor memory and disk usage.  Will process events
    * when items added to the cache and when items are retrieved from disk.  As
    * necessary, the least recently used items in memory will be moved to disk,
    * and the least recently used disk items will be flushed.  As events are
    * received through the {@link IPSCacheAccessedListener} and
    * {@link IPSCacheModifiedListener} interfaces, they are queued and processed
    * asynchronously by this thread.
    */
   public void run()
   {
      while (!m_shutdown)
      {
         try
         {
            PSCacheEvent event = null;
            synchronized(m_eventQueue)
            {
               if (m_eventQueue.isEmpty())
                  m_eventQueue.wait();

               event = (PSCacheEvent) m_eventQueue.removeFirst();
            }
            handleEvent(event);

            long memory = reachedLimit(m_maxMemoryUsage, m_usedMemorySpace);
            if (memory > 0)
            {
               if (PSCacheManager.getInstance().isDebugLoggingEnabled())
                  log("Memory limit reached, freeing: " + memory + "bytes " +
                     formatUsage(m_usedMemorySpace, m_usedDiskSpace));

               freeMemory(memory);
            }

            long disk = reachedLimit(m_maxDiskUsage, m_usedDiskSpace);
            if (disk > 0)
            {
               if (PSCacheManager.getInstance().isDebugLoggingEnabled())
                  log("Disk limit reached, freeing: " + disk + "bytes " +
                     formatUsage(m_usedMemorySpace, m_usedDiskSpace));

               freeDisk(disk);
            }
         }
         catch (InterruptedException e)
         {
            m_shutdown = true;
         }
         catch (Throwable t)
         {
            // make sure this never dies unexpected
            PSConsole.printMsg("CacheMemoryManager", t);
         }
      }
      synchronized(m_eventQueue)
      {
         m_eventQueue.clear();
      }
      m_itemsInMemory.clear();
      m_itemsOnDisk.clear();

      log("Finished cache memory manager.");
   }

   /**
    * Shuts down the memory manager thread and releases any resources.  It is
    * safe to call this method if the thread is not currently running.
    */
   public void shutdown()
   {
      synchronized(m_monitor)
      {
         if (m_shutdown)
            return;

         m_shutdown = true;
      }
      interrupt();

      log("Shutting down cache memory manager.");
   }

   // see IPSCacheAccessedListener interface
   public void cacheAccessed(PSCacheEvent event)
   {
      if (event == null)
         throw new IllegalArgumentException("event may not be null");

      if (m_shutdown)
         return;

      synchronized(m_eventQueue)
      {
         m_eventQueue.add(event);
         m_eventQueue.notify();
      }
   }

   // see IPSCacheModifiedListener interface
   public void setCache(PSMultiLevelCache cache)
   {
      if (cache == null)
         throw new IllegalArgumentException("cache may not be null");

      if (m_shutdown)
         return;

      Integer cacheId = new Integer(cache.getCacheId());
      if (m_caches.get(cacheId) != null)
         return;

      synchronized(m_monitor)
      {
         m_caches.put(new Integer(cache.getCacheId()), cache);

         /**
          * The cache handler already signed us up for modified events, so don't
          * register again.
          *
          * We don't subscribe accessed events since we are not interested in
          * cache missed events.
          */
      }

      log("New cache added.");
   }

   // see IPSCacheModifiedListener interface
   public void cacheModified(PSCacheEvent event)
   {
      if (event == null)
         throw new IllegalArgumentException("event may not be null");

      if (m_shutdown)
         return;

      if (event.getAction() == PSCacheEvent.CACHE_ITEM_ADDED)
      {
         Object o = event.getObject();
         if (o instanceof PSCacheItem)
         {
            PSCacheItem item = (PSCacheItem) o;
            item.addCacheAccessedListener(this);
         }
      }

      synchronized(m_eventQueue)
      {
         m_eventQueue.add(event);
         m_eventQueue.notify();
      }
   }

   /**
    * Convenience method to log debug messages.
    *
    * @param msg the message to log, not <code>null</code>.
    * @throws IllegalArgumentException if the supplied message is
    *    <code>null</code>.
    */
   private void log(String msg)
   {
      if (msg == null)
        throw new IllegalArgumentException("log message cannot be null");

      PSCacheManager.getInstance().logDebugMessage(msg);
   }

   /**
    * Formats a usage String for the supplied parameters.
    *
    * @param memUsage the current memory usage in bytes.
    * @param diskUsage the current disk usage in bytes.
    * @return the usage String like
    *    "Memory: 124000 bytes(1000000 bytes) Disk: 5230000 bytes(-1 bytes)".
    */
   private String formatUsage(long memUsage, long diskUsage)
   {
      return "Memory: " + memUsage + "bytes (" + m_maxMemoryUsage + " bytes)" +
         "Disk: " + diskUsage + "bytes (" + m_maxDiskUsage + " bytes)";
   }

   /**
    * Calculates if the limit has been reached based on the supplied maximum
    * size and current usage.
    *
    * @param max the maximum size allowed in bytes, -1 if unlimited, 0 or
    *    greater otherwise.
    * @param current the current size used in bytes, always greater or equal
    *    to 0.
    * @return the amount of space to be freed in bytes, always greater or equal
    *    to 0.
    * @throws IllegalArgumentException for any illegal parameter passed.
    */
   private long reachedLimit(long max, long current)
   {
      if (max < -1)
        throw new IllegalArgumentException("max must be greater or equal to -1");

      if (current < 0)
        throw new IllegalArgumentException("current must be greater or equal to 0");

      if (max == -1 )
         return 0;

      if( max == 0)
         return current;

      // we use a 5% threshold
      long threshold = max / 20;
      if (current < (max - threshold))
         return 0;

      /**
       * We free memory/disk in chunks of 10% of the maximum size including
       * everything above the maximum size.
       */
      long releaseAmount = threshold * 2;
      if (current > max)
         releaseAmount += (current - max);

      return releaseAmount;
   }

   /**
    * Frees the specified amount of memory by moving items to disk or flushing
    * the least recent used items from memory. This method handles both,
    * the memory and disk updates needed.
    * The <code>CACHE_ITEM_STORED_TO_DISK</code> action resulting from this
    * method must be ignored.
    *
    * @param amount the amount of memory to free up in bytes, assumed greater
    *    then 0.
    */
   private void freeMemory(long amount)
   {
      long marker = m_usedMemorySpace - amount;
      while (!m_itemsInMemory.isEmpty() && m_usedMemorySpace > marker)
      {
         PSCacheItem item = (PSCacheItem) m_itemsInMemory.removeFirst();
         if (item != null)
         {
            m_usedMemorySpace -= item.getSize();

            if (itemFits(item, m_maxDiskUsage))
            {
               // we have a disk cache that fits the item, move it there
               try
               {
                  item.toDisk(getCacheLocation());

                  m_itemsOnDisk.add(item);
                  m_usedDiskSpace += item.getSize();

                  if (PSCacheManager.getInstance().isDebugLoggingEnabled())
                     log("Memory to disk. Size moved: " + item.getSize() +
                        "bytes " +
                        formatUsage(m_usedMemorySpace, m_usedDiskSpace));
               }
               catch (NotSerializableException e)
               {
                  Object[] args =
                  {
                     item.toString(),
                     e.getLocalizedMessage()
                  };
                  PSServerLogHandler.logMessage(new PSInternalError(
                     IPSServerErrors.CACHE_STORE_TO_DISK_FAILURE, args));

                  flushItem(item);
               }
               catch (PSCacheException e)
               {
                  Object[] args =
                  {
                     item.toString(),
                     e.getLocalizedMessage()
                  };
                  PSServerLogHandler.logMessage(new PSInternalError(
                     IPSServerErrors.CACHE_STORE_TO_DISK_FAILURE, args));

                  flushItem(item);
               }
            }
            else
            {
               // flush it if it didn't fit
               if (PSCacheManager.getInstance().isDebugLoggingEnabled())
                  log("Item did not fit into disk. The size was: " +
                     item.getSize() + " Available disk was: " +
                     m_maxDiskUsage + "bytes Flushing it instead.");

               flushItem(item);
            }
         }
      }
   }

   /**
    * Frees the specified amount of space on disk by flushing the least
    * recent used item(s).
    *
    * @param amount the amount of disk space to free in bytes, assumed greater
    *    then 0.
    */
   private void freeDisk(long amount)
   {
      long marker = m_usedDiskSpace - amount;
      while (!m_itemsOnDisk.isEmpty() && m_usedDiskSpace > marker)
      {
         PSCacheItem item = (PSCacheItem) m_itemsOnDisk.removeFirst();
         if (item != null)
         {
            flushItem(item);
            m_usedDiskSpace -= item.getSize();

            if (PSCacheManager.getInstance().isDebugLoggingEnabled())
               log("Disk to flush. Size flused: " + item.getSize() +
                  "bytes " + formatUsage(m_usedMemorySpace, m_usedDiskSpace));
         }
      }
   }

   /**
    * Creates the cache file location.
    *
    * @return the cache file location, never <code>null</code>.
    */
   private File getCacheLocation()
   {
      return new File(PSServer.getRxDir(),CACHE_DIR);
   }

   /**
    * Handles the supplied event for all cases. This method does only update
    * the size counters and item lists. Besides that it also makes sure that
    * items are not too big to fit into memory and/or disk. If we would allow
    * this, one really big item could flush the entire cache.
    *
    * @param event the event to be handled, assumed not <code>null</code>.
    */
   private void handleEvent(PSCacheEvent event)
   {
      Object o = event.getObject();
      if (!(o instanceof PSCacheItem))
         return;

      PSCacheItem item = (PSCacheItem) o;

      switch (event.getAction())
      {
         case PSCacheEvent.CACHE_ITEM_ACCESSED_FROM_DISK:
            /**
             * If the object was accessed from disk, its been moved into
             * memory and released from disk.
             * If we did not find the item in the on disk list, we skip it.
             */
            if (m_itemsOnDisk.remove(item))
            {
               m_usedDiskSpace -= item.getSize();

               m_itemsInMemory.add(item);
               m_usedMemorySpace += item.getSize();

               if (PSCacheManager.getInstance().isDebugLoggingEnabled())
                  log("Disk to memory. Size: " + item.getSize() + "bytes " +
                     formatUsage(m_usedMemorySpace, m_usedDiskSpace));
            }
            break;

         case PSCacheEvent.CACHE_ITEM_STORED_TO_DISK:
            /**
             * The object was moved to disk, so it was released from memory.
             * If we cannot find the item in the in memory list, we skip it.
             */
            if (m_itemsInMemory.remove(item))
            {
               m_usedMemorySpace -= item.getSize();

               m_itemsOnDisk.add(item);
               m_usedDiskSpace += item.getSize();

               if (PSCacheManager.getInstance().isDebugLoggingEnabled())
                  log("Memory to disk. Size: " + item.getSize() + "bytes " +
                     formatUsage(m_usedMemorySpace, m_usedDiskSpace));
            }
            break;

         case PSCacheEvent.CACHE_ITEM_ADDED:
            /**
             * A new item was added to the cache. Update the memory/disk size
             * counters and item lists. If the item is too big to fit into
             * the available space, move it to disk or even flush it.
             */
            addCacheItem(item, m_maxMemoryUsage, m_maxDiskUsage);
            break;

         case PSCacheEvent.CACHE_ITEM_REMOVED:
            item.removeCacheAccessedListener(this);
            if (item.isInMemory())
            {
               if (m_itemsInMemory.remove(item))
               {
                  m_usedMemorySpace -= item.getSize();

                  if (PSCacheManager.getInstance().isDebugLoggingEnabled())
                     log("Removed from memory. Size: " + item.getSize() +
                        "bytes " +
                        formatUsage(m_usedMemorySpace, m_usedDiskSpace));
               }
            }
            else
            {
               if (m_itemsOnDisk.remove(item))
               {
                  m_usedDiskSpace -= item.getSize();

                  if (PSCacheManager.getInstance().isDebugLoggingEnabled())
                     log("Removed from disk. Size: " + item.getSize() +
                        "bytes " +
                        formatUsage(m_usedMemorySpace, m_usedDiskSpace));
               }
            }
            break;

         case PSCacheEvent.CACHE_ITEM_ACCESSED_FROM_MEMORY:
            /**
             * The item was accesed from memory. Move it to the end of the in
             * memory list to keep the list in order based on last accessed
             * time.
             */
            if(m_itemsInMemory.remove(item))
            {
               m_itemsInMemory.add(item);
            }
            break;

         case PSCacheEvent.CACHE_ITEM_NOT_FOUND:
            // nothing to do
            break;
      }
   }

   /**
    * Add the provided item to the cache. Either it will be added to memory,
    * disk or if it does not fit it will be flushed.
    *
    * @param item the item added, assumed not <code>null</code>.
    * @param maxMemoryUsage the maximum memory available, -1 if unlimited,
    *    greater or equals 0 otherwise.
    * @param maxDiskUsage the maximum disk space available, -1 if unlimited,
    *    greater or equals 0 otherwise.
    */
   private void addCacheItem(PSCacheItem item, long maxMemoryUsage,
      long maxDiskUsage)
   {
      if (item.isInMemory())
      {
         if (itemFits(item, maxMemoryUsage))
         {
            if (m_itemsInMemory.add(item))
            {
               m_usedMemorySpace += item.getSize();

               if (PSCacheManager.getInstance().isDebugLoggingEnabled())
                  log("Added to memory. Size: " + item.getSize() + " " +
                     formatUsage(m_usedMemorySpace, m_usedDiskSpace));
            }
         }
         else
         {
            if (PSCacheManager.getInstance().isDebugLoggingEnabled())
               log("Item did not fit into memory. The size was: " +
                  item.getSize() + " Available memory was: " + maxMemoryUsage +
                  " Adding it to disk instead.");

            try
            {
               item.toDisk(getCacheLocation());
               addCacheItemToDisk(item, maxMemoryUsage, maxDiskUsage);
            }
            catch (NotSerializableException e)
            {
               Object[] args =
               {
                  item.toString(),
                  e.getLocalizedMessage()
               };
               PSServerLogHandler.logMessage(new PSInternalError(
                  IPSServerErrors.CACHE_STORE_TO_DISK_FAILURE, args));

               flushItem(item);
            }
            catch (PSCacheException e)
            {
               Object[] args =
               {
                  item.toString(),
                  e.getLocalizedMessage()
               };
               PSServerLogHandler.logMessage(new PSInternalError(
                  IPSServerErrors.CACHE_STORE_TO_DISK_FAILURE, args));

               flushItem(item);
            }
         }
      }
      else
      {
         addCacheItemToDisk(item, maxMemoryUsage, maxDiskUsage);
      }
   }

   /**
    * Tries to add the supplied item to disk. Flushes the item if it does not
    * fit into the maximum disk space allowed.
    *
    * @param item the item to be added to disk, assumed not <code>null</code>.
    * @param maxMemoryUsage the maximum memory available in bytes, -1 if
    *    unlimited, greater or equal to 0 otherwise.
    * @param maxDiskUsage the maximum disk space available in bytes, -1 if
    *    unlimited, greater or equal to 0 otherwise.
    */
   private void addCacheItemToDisk(PSCacheItem item, long maxMemoryUsage,
      long maxDiskUsage)
   {
      if (itemFits(item, maxDiskUsage))
      {
         if (m_itemsOnDisk.add(item))
         {
            m_usedDiskSpace += item.getSize();

            if (PSCacheManager.getInstance().isDebugLoggingEnabled())
               log("Added to disk. Size: " + item.getSize() + " " +
                  formatUsage(m_usedMemorySpace, m_usedDiskSpace));
         }
      }
      else
      {
         if (PSCacheManager.getInstance().isDebugLoggingEnabled())
            log("Item did not fit into disk. The size was: " +
               item.getSize() + " Available disk was: " + maxDiskUsage +
               " Flushing it instead.");

         flushItem(item);
      }
   }

   /**
    * Tests if the supplied item fits into the provided maximum space. To avoid
    * moving / flushing too many other cached items if a really big item was
    * added, we consider only items that have a size less then the maximum
    * devided by 2 to fit.
    *
    * @param item the item to test, assumed not <code>null</code>.
    * @param max the maximal size available, -1 if unlimited, 0 or greater
    *    otherwise.
    * @return <code>true</code> if it fits, <code>false</code> otherwise.
    */
   private boolean itemFits(PSCacheItem item, long max)
   {
      if (max == -1)
         return true;

      return (item.getSize() < (max / 2));
   }

   /**
    * Flushes the supplied item from the cache.
    *
    * @param item the item to flush, assumed not <code>null</code>.
    */
   private void flushItem(PSCacheItem item)
   {
      PSMultiLevelCache cache = (PSMultiLevelCache) m_caches.get(
         new Integer(item.getCacheId()));

      cache.flush(item.getKeys());
   }

   /**
       * Cleans up disk cache folder in the event the server crashes.
       * @return <code>true</code> if it successfully deletes,
       * <code>false</code> otherwise.
       */

      static boolean cleanDiskCache()
      {
         File dir = new File(PSServer.getRxDir(),CACHE_DIR);
         return delete(dir);
      }

      /**
    * If the supplied File is a directory, it deletes
    * it and all its children, recursively.
    * Deletes a folder recursively.
    * @param dir - folder to be removed.If <code>null</code>
    * it returns true  <code>true</code>.
    * @return <code>true</code> if there are no errors,
    * <code>false</code> otherwise.
    */
      static boolean delete(File dir)
      {
         if (null == dir)
            return true;
         if (dir.isDirectory())
         {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++)
               delete(new File(dir, children[i]));
         }
         return dir.delete();
   }

   /**
    * The directory used to store cached files to disk, relative from the
    * Rhythmyx installation root. Never <code>null</code> or changed.
    */
   private static final String CACHE_DIR = "cache";

   /**
    * The amount of memory space used by the current cache in bytes.
    */
   private long m_usedMemorySpace = 0;

   /**
    * The amount of disk space used by the current cache in bytes.
    */
   private long m_usedDiskSpace = 0;

   /**
    * All events are queued in this list and then processed later. A list of
    * <code>PSCacheEvent</code> objects, never <code>null</code>, may be empty.
    */
   private LinkedList m_eventQueue = new LinkedList();

   /**
    * A flag to indicate that caching is enabled or disabled. Initialized in
    * the contructor, never changed after that.
    */
   private boolean m_isEnabled = false;

   /**
    * The maximum amount of memory to be used for caching in bytes. Initialized
    * in the constructor, never changed after that. -1 if unlimited, greater
    * or equal to 0 otherwise.
    */
   private long m_maxMemoryUsage = -1;

   /**
    * The maximum amount of disk space to be used for caching in bytes.
    * Initialized in the constructor, never changed after that. -1 if
    * unlimited, greater or equal to 0 otherwise.
    */
   private long m_maxDiskUsage = -1;

   /**
    * Flag to indicate it the memory manager is shutting down.  If
    * <code>true</code>, the memory manager has begun shutdown, otherwise
    * <code>false</code>.
    */
   private boolean m_shutdown = false;

   /**
    * A map of caches for which this is manageing the memory. Set through calls
    * to {@link #setCache()}. The keys are Integers of the cache id and the
    * values are <code>PSMultiLevelCache</code> objects. Never
    * <code>null</code>, might be empty.
    */
   private Map m_caches = new HashMap();

   /**
    * A list of all cached items (<code>PSCacheItem</code>) that are currently
    * in memory. The list is always ordered by accessed time, the oldest item
    * being first. Never <code>null</code>, may be empty.
    */
   private LinkedList m_itemsInMemory = new LinkedList();

   /**
    * A list of all cached items (<code>PSCacheItem</code>) that are currently
    * on disk. The list is always ordered by accessed time, the oldest item
    * beeing first. Never <code>null</code>, may be empty.
    */
   private LinkedList m_itemsOnDisk = new LinkedList();

   /**
    * Object used to synchronize on the memory manager, never <code>null</code>.
    */
   private Object m_monitor = new Object();
}
