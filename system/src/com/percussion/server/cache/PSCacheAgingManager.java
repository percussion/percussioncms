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

import com.percussion.server.PSConsole;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Monitors cache for expired items and flushes them.
 */
class PSCacheAgingManager implements IPSCacheModifiedListener
{
   /**
    * Creates a thread to monitor the cache for aged items.  If aging time is
    * set to unlimited, the thread will not start.
    * 
    * @param agingTime The amount of time (in mins) to allow an object to be 
    * present in the cache before it is flushed. Provide <code>-1</code> to  
    * allow unlimited time (never expires), or else a number greater than <code>
    * 0</code>.
    * 
    * @throws IllegalArgumentException if <code>agingTime</code> is invalid.
    */
   PSCacheAgingManager(long agingTime)
   {
      if(agingTime != -1 && agingTime <= 0)
      {
         throw new IllegalArgumentException("agingTime may be either '-1' to " +
            "specify unlimited time or a number greater than 0.");
      }
      
      if(agingTime == -1)
      {
         m_run = false;
         m_agingTime = agingTime;
      }
      else
      {
         m_agingTime = agingTime*MIN_TO_MILLISEC;               
         
         m_agingThread = new Thread(AGING_THREAD)
         {
            /**
             * Starts a thread to monitor the cache for aged items. Will 
             * determine the next item that will age and will sleep until that 
             * time. If there are no items in the cache, will block waiting on 
             * an event that adds an item to the cache.  When an item has aged,
             * it is flushed from the cache. 
             */
            public void run()
            {
               PSCacheItem itemToExpire = null;
               while(m_run)
               {
                  try {            
                     long diff = 0; 
                     synchronized(m_cachedItems)
                     {
                        //Flush all the items that are reached or almost 
                        //reaching the expiry time.
                        while(diff < MIN_AGE_TIME)
                        {   
                           if(itemToExpire != null)
                           {                              
                              //Check whether the item exists in cached items
                              //or not ( may be removed by remove thread). 
                              if(m_cachedItems.remove( itemToExpire ))
                              {  
                                 PSCacheManager.getInstance().logDebugMessage(
                                    "Flushing the item with keys " + 
                                    itemToExpire + " as it is aged.");                                 
                                 m_cache.flush( itemToExpire.getKeys() );
                              }
                           }
                           
                           if(m_cachedItems.isEmpty())
                              m_cachedItems.wait();   
                              
                           itemToExpire = (PSCacheItem)m_cachedItems.get(0);

                           diff = itemToExpire.getCreatedDate().getTime() + 
                              m_agingTime - System.currentTimeMillis();
                        }
                     }
                     sleep(diff);
                  }
                  catch(InterruptedException e)
                  {
                     m_run = false;
                  }
                  catch(Throwable t)
                  {
                     PSConsole.printMsg(AGING_THREAD, t);
                  }
               }   
            }           
         };
         m_agingThread.setDaemon( true );
         m_agingThread.start();
         
         m_queueThread = new Thread(QUEUE_THREAD)
         {
            /**
             * This adds/removes all the items that are queued by the cache item 
             * events to/from the cached items list. Ignores if the items that
             * are to be removed are already removed by aging thread.
             */
            public void run()
            {
               while(m_run)
               {
                  try {                  
                     PSCacheEvent event;
                     synchronized(m_queuedItems)
                     {
                        if(m_queuedItems.isEmpty())
                           m_queuedItems.wait();

                        event = (PSCacheEvent) m_queuedItems.removeFirst();
                     }
                           
                     synchronized(m_cachedItems)
                     {
                        if(event.getAction() == PSCacheEvent.CACHE_ITEM_ADDED)
                        {
                           m_cachedItems.add( event.getObject() );
                           m_cachedItems.notify();
                        }
                        else 
                           m_cachedItems.remove( event.getObject() );
                     }
                  }
                  catch(InterruptedException e)
                  {
                     m_run = false;                  
                  }
                  catch(Throwable t)
                  {
                     PSConsole.printMsg(QUEUE_THREAD, t);
                  }
               }
            }
         };
         m_queueThread.setDaemon( true );
         m_queueThread.start();         
      }      
   }   

   /**
    * Shuts downs the threads created by this object and releases any resources.
    * It is safe to call this method even if the threads are not currently 
    * running.
    */
   void shutdown()
   {
      if(m_run)
      {
         m_agingThread.interrupt();
         m_queueThread.interrupt();      
         m_run = false;      
         m_cachedItems.clear();
         m_queuedItems.clear();
      }
      m_cache = null;
   }
   
   /**
    * Causes the item involved in the supplied event to be added or removed from
    * this manager's internal list of item references (such list access must be 
    * synchronized with any list access from the threads that monitors the items
    * ). See {@link IPSCacheModifiedListener#cacheModified(PSCacheEvent)} for 
    * more info.
    * 
    * @param e the cache event, may not be <code>null</code>
    * 
    * @throws IllegalArgumentException if the event or the item involved in the
    * event is <code>null</code> or the item involved with event does not belong
    * to the cache that this listener is listening to.
    * @throws IllegalStateException if the cache that this listener is listening
    * to for events is not yet set.
    */
   public void cacheModified(PSCacheEvent e)
   {
      if(e == null)
         throw new IllegalArgumentException("e may not be null");
         
      //if shutdown is called or unlimited aging time nothing to do.
      if(!m_run)
         return;               

      if(e.getObject() == null)
      {
         throw new IllegalArgumentException(
           "The event 'e' does not involve with any object for processing.");
      }
         
      if(m_cache == null)
      {
         throw new IllegalStateException(
            "The cache instance that this listener is listening " + 
            "to for events is not yet set.");
      }
        
      if( ((PSCacheItem)e.getObject()).getCacheId() != m_cache.getCacheId())
      {
         throw new IllegalArgumentException( 
            "The item involved with supplied event 'e' does not belong to the" +
            " cache that this listener is listening to.");
      }

      if(e.getAction() == PSCacheEvent.CACHE_ITEM_ADDED || 
         e.getAction() == PSCacheEvent.CACHE_ITEM_REMOVED)
      {
         synchronized(m_queuedItems)
         {
            m_queuedItems.addLast( e );
            m_queuedItems.notify();
         }
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
    * The amount of time in milliseconds that an item can be in the cache. 
    * The value of '-1' indicates that the items are never expired. Set in ctor 
    * and never modified after that.
    */
   private long m_agingTime;
   
   /**
    * The flag to indicate whether the threads should continue to run or stop.
    * Initialized to <code>true</code> and set to <code>false</code> when <code>
    * shutdown()</code> is called to stop the threads or during ctor if the 
    * aging time is unlimited.
    */
   private boolean m_run = true;
   
   /**
    * The instance of the cache that this listener is listening to for events.
    * Gets set when this listener is registered with this cache instance and
    * set to <code>null</code> when <code>shutdown()</code> is called.
    */
   private PSMultiLevelCache m_cache;

   /**
    * The thread that checks and flushes the items that are aged in the cache.
    * Initialized in ctor and never <code>null</code> or modified after that. 
    * This thread will run only if the aging time is not unlimited. If it is 
    * running, this will be stopped when <code>shutdown()</code> is called.
    */   
   private Thread m_agingThread;
   
   /**
    * The thread that adds/removes the items that are added/removed from the 
    * cache to/from the cached items list. Initialized in ctor and never <code>
    * null</code> or modified after that. This thread will run only if the aging
    * time is not unlimited. If it is running, this will be stopped when <code>
    * shutdown()</code> is called.
    */
   private Thread m_queueThread;
   
   /**
    * The list of {@link PSCacheItem}s that are added to cache. Initialized 
    * to an empty list and adds/removes the items to/from the list when queue
    * thread processes the item added/removed events queue.
    */
   private List m_cachedItems = new ArrayList();   
   
   /**
    * The list of {@link PSCacheEvent}s whose action is either {@link 
    * PSCacheEvent#CACHE_ITEM_ADDED} or {@link PSCacheEvent#CACHE_ITEM_REMOVED}. 
    * Initialized to an empty list and adds the items to the list when {@link 
    * #cacheModified} is called with the above mentioned events. The events get
    * removed from the list when the queue thread processes that event.
    */
   private LinkedList m_queuedItems = new LinkedList();      
 
   /**
    * The constant to use to convert minutes to milli seconds.
    */
   private final static long MIN_TO_MILLISEC = 60000;  
   
   /**
    * The time difference in milli seconds that is used to consider an item has
    * expired if the current time is less than item's expiry time just by this 
    * or less than this amount of time (1/10th of minimum allowed aging time(1 
    * min) ).
    */
   private final static long MIN_AGE_TIME = 10000; 
   
   /**
    * The name of the aging thread created by this object.
    */
   private final static String AGING_THREAD = "CacheAgingManager";
   
   /**
    * The name of the queue thread.
    */
   private final static String QUEUE_THREAD = AGING_THREAD + "Queue";
}
