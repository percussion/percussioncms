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
import com.percussion.server.PSConsole;

import java.util.LinkedList;

/**
 * Collects statistics based on cache usage.  See {@link
 * PSCacheStatisticsSnapshot} for more information about the statistics.
 * A separate thread is used to asynchronously process any cache events that
 * will affect the statistics compiled by this class.  Point-in-time snapshots
 * may thus be requested without blocking the requests that generate these
 * events.
 */
class PSCacheStatistics extends Thread
   implements IPSCacheAccessedListener, IPSCacheModifiedListener
{
   /**
    * Creates an instance of this object and starts the thread that will process
    * any events queued by calls to the <code>cacheAccessed</code> or
    * <code>cacheModified</code> methods.  If caching is not enabled, will
    * not start the thread and provide snapshots with all <code>0</code> values.
    *
    * @param cacheSettings The cache settings that are used to determine if
    * caching is enabled.  May not be <code>null</code>.
    *
    * @throws IllegalArgumentException if <code>cacheSettings</code> is <code>
    * null</code>.
    */
   public PSCacheStatistics(PSServerCacheSettings cacheSettings)
   {
      super(THREAD_NAME);

      if(cacheSettings == null)
         throw new IllegalArgumentException("cacheSettings may not be null");

      setDaemon(true);

      if(cacheSettings.isEnabled())
         start();
      else
         m_run = false;
   }

   // see IPSCacheAccessedListener interface
   public void cacheAccessed(PSCacheEvent e)
   {
      if(e == null)
         throw new IllegalArgumentException("e may not be null");

      if(!m_run)
         return;

      queueEvent( e );
   }

   /**
    * Processes the cache modified event to update the statistics only if the
    * cache is enabled and this thread is running. Adds itself as an access
    * listener to the cached item if the event is <code>
    * PSCacheEvent.CACHE_ITEM_ADDED</code>. See {@link IPSCacheModifiedListener}
    * for more description.
    *
    * @param e the cached event to queue, may not be <code>null</code>
    *
    * @throws IllegalArgumentException if e is <code>null</code>.
    */
   public void cacheModified(PSCacheEvent e)
   {
      if(e == null)
         throw new IllegalArgumentException("e may not be null");

      if(!m_run)
         return;

      int action = e.getAction();
      if(action == PSCacheEvent.CACHE_ITEM_ADDED)
      {
         PSCacheItem item = (PSCacheItem) e.getObject();
         item.addCacheAccessedListener(this);
      }

      queueEvent( e );
   }

   /**
    * Queues the event to process (update cache statistics).
    *
    * @param e the cached event to queue, assumed not to be <code>null</code>
    *
    */
   private void queueEvent(PSCacheEvent e)
   {
      synchronized(m_cachedEvents)
      {
         m_cachedEvents.addLast( e );
         m_cachedEvents.notify();
      }
   }

   // see IPSCacheModifiedListener interface
   public void setCache(PSMultiLevelCache cache)
   {
      if(cache == null)
         throw new IllegalArgumentException("cache may not be null");

      m_cache = cache;
   }

   /**
    * Creates a point in time snapshot of the statistics collected.
    *
    * @return A snapshot of the current stats, never <code>null</code>.
    */
   public PSCacheStatisticsSnapshot getSnapshot()
   {
      PSCacheStatisticsSnapshot snapshot = null;
      synchronized(m_cacheMonitor)
      {
         snapshot = new PSCacheStatisticsSnapshot(
            m_diskHits, m_diskItems, m_diskUsage, m_memItems, m_memUsage,
            m_misses, m_totalHits);
      }

      return snapshot;
   }

   /**
    * All events received thru the {@link IPSCacheAccessedListener} and
    * {@link IPSCacheModifiedListener} interfaces are queued and
    * processed asynchronously by this method and update the statistics
    * accordingly.
    */
   public void run()
   {
      while(m_run)
      {
         try {

            PSCacheEvent event;
            synchronized(m_cachedEvents)
            {
               if(m_cachedEvents.size() == 0)
                  m_cachedEvents.wait();
               event = (PSCacheEvent)m_cachedEvents.removeFirst();
            }

            //processes the event to update statistics
            synchronized(m_cacheMonitor)
            {
               processEvent(event);
            }
         }
         catch(InterruptedException e)
         {
            m_run = false;
         }
         catch(Throwable t)
         {
            PSConsole.printMsg(THREAD_NAME, t);
         }
      }
      synchronized(m_cachedEvents)
      {
         m_cachedEvents.clear();
      }
      m_cache = null;
   }

   /**
    * Updates the cache statistics by processing the supplied event.
    * The cache statistics are calculated as following based on cached event
    * action.
    * <table border=1>
    * <tr><th>Event action</th><th>Updated statistics</th>
    * <tr><td><code>PSCacheEvent.CACHE_ITEM_ADDED</code></td><td>If it is added
    * to memory, it increments the number of items in memory by 1 and memory
    * usage by item size, otherwise it increments the number of items in disk
    * and disk usage. Currently this event happens only when an item is added to
    * memory. To disk it fires <code>PSCacheEvent.CACHE_ITEM_STORED_TO_DISK
    * </code></td></tr>
    * <tr><td><code>PSCacheEvent.CACHE_ITEM_REMOVED</code></td><td>If it is
    * removed from memory, it decrements the number of items in memory by 1 and
    * memory usage by item size, otherwise it decrements the number of items in
    * disk and disk usage.</td></tr>
    * <tr><td><code>PSCacheEvent.CACHE_ITEM_ACCESSED_FROM_MEMORY</code></td>
    * <td>Increments the total number of hits by 1.</td></tr>
    * <tr><td><code>PSCacheEvent.CACHE_ITEM_ACCESSED_FROM_DISK</code></td><td>
    * Increments the total number of hits and disk hits by 1. Decrements the
    * number of items in disk by 1 and disk usage by item size and increments
    * the number of items in memory by 1 and memory usage by item size</td></tr>
    * <tr><td><code>PSCacheEvent.CACHE_ITEM_NOT_FOUND</code></td>
    * <td>Increments the total number of misses by 1.</td></tr>
    * <tr><td><code>PSCacheEvent.CACHE_ITEM_STORED_TO_DISK</code></td>
    * <td>Increments the number of items in disk by 1 and disk usage by item
    * size and decrements the number of items in memory by 1 and memory usage by
    * item size.</td></tr>
    * </table>
    *
    * @param event the cached event queued, assumed not to be <code>null</code>
    *
    */
   private void processEvent(PSCacheEvent event)
   {
      int action = event.getAction();
      PSCacheItem item = (PSCacheItem) event.getObject();

      switch(action)
      {
         case PSCacheEvent.CACHE_ITEM_ADDED:
            if(item != null)
            {
               long size = item.getSize();
               m_memUsage += size;
               m_memItems++;
            }
            break;

         case PSCacheEvent.CACHE_ITEM_REMOVED:
            if(item != null)
            {
               long size = item.getSize();
               if(item.isInMemory())
               {
                  m_memUsage -= size;
                  m_memItems--;
               }
               else
               {
                  m_diskUsage -= size;
                  m_diskItems--;
               }
            }
            break;

         case PSCacheEvent.CACHE_ITEM_ACCESSED_FROM_MEMORY:
            m_totalHits++;
            break;

         case PSCacheEvent.CACHE_ITEM_ACCESSED_FROM_DISK:
            m_totalHits++;
            m_diskHits++;
            m_memItems++;
            m_diskItems--;
            if(item != null)
            {
               long size = item.getSize();
               m_memUsage += size;
               m_diskUsage -= size;
            }
            break;

         case PSCacheEvent.CACHE_ITEM_NOT_FOUND:
            m_misses++;
            break;

         case PSCacheEvent.CACHE_ITEM_STORED_TO_DISK:
            m_memItems--;
            m_diskItems++;
            if(item != null)
            {
               long size = item.getSize();
               m_memUsage -= size;
               m_diskUsage += size;
            }
            break;
      }
   }

   /**
    * Terminates the thread which processes the queued cache events.
    */
   public void shutdown()
   {
      interrupt();
      m_run = false;
   }

   /**
    * The flag to indicate whether the thread should continue to run or stop.
    * Initialized to <code>true</code> and set to <code>false</code> when <code>
    * shutdown()</code> is called to stop the thread or during ctor if the cache
    * is not enabled.
    */
   private boolean m_run = true;

   /**
    * The list of cached events that are queued, initialized to an empty list
    * and adds the events to the list when either {@link #cacheAccessed} or
    * {@link #cacheModified} are called and removes the events from the list
    * when they get processed by this thread. Never <code>null</code>
    */
   private LinkedList m_cachedEvents = new LinkedList();

   /**
    * Object to use to synchronize to get the cache statistics snapshot.
    * Never <code>null</code>.
    */
   private Object m_cacheMonitor = new Object();

   /**
    * The instance of the cache that this listener is listening to for events.
    * Gets set when this listener is registered with this cache instance and
    * set to <code>null</code> when <code>shutdown()</code> is called.
    */
   private PSMultiLevelCache m_cache;

   /**
    * The number of failed attempts to retrieve an item from cache. Initialized
    * to <code>0</code> and modified as the cached events occur.
    */
   private long m_misses = 0;

   /**
    * The number of successful attempts to retrieve an item from cache either
    * from memory or disk. Initialized to <code>0</code> and modified as the
    * cached events occur.
    */
   private long m_totalHits = 0;

   /**
    * The amount of memory used by the items cached in memory. Initialized to
    * <code>0</code> and modified as the cached events occur.
    */
   private long m_memUsage = 0;

   /**
    * The amount of disk space used by the items cached in disk. Initialized
    * to <code>0</code> and modified as the cached events occur.
    */
   private long m_diskUsage = 0;

   /**
    * The number of items cached in memory. Initialized to <code>0</code> and
    * modified as the cached events occur.
    */
   private long m_memItems = 0;

   /**
    * The number of items cached in disk. Initialized to <code>0</code> and
    * modified as the cached events occur.
    */
   private long m_diskItems = 0;

   /**
    * The number of items accessed from disk. Initialized to <code>0</code> and
    * modified as the cached events occur.
    */
   private long m_diskHits = 0;

   /**
    * The name of this thread.
    */
   private static final String THREAD_NAME = "CacheStatistics";
}
