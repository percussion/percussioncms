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
package com.percussion.server;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This class provides a container to cache pages. The user should 
 * create the singleton object, so its never collected during the entire 
 * lifetime of the server.
 */
public class PSPageCache extends Thread
{
   /**
    * Get the singleton instance object for this class, will be created if
    * it does not exist yet.
    *
    * @return the singleton object for this class.
    */
   public static PSPageCache getInstance()
   {
      if (ms_instance == null)
         ms_instance = new PSPageCache();
      
      return ms_instance;
   }
    
   
   /**
    * Wakes up every minute and removes expired error pages from the 
    * cache.
    */
   public void run()
   {
      /*
       * The thread sleep time (in milli seconds) after each cleanup cycle.
       */
      long sleepTime = 1*60*1000;

      while (!ms_stopped)
      {
         try
         {
            cleanCache();
            
            Thread.sleep(sleepTime);
         } 
         catch (Exception e)
         {
            continue;
         }
      }
   }

   public static synchronized void cleanCache()
   {
      Date current = new Date(System.currentTimeMillis());
      Iterator<Integer> pages = ms_cache.keySet().iterator();
      while (pages.hasNext())
      {
         Integer key = pages.next();
         
         @SuppressWarnings("rawtypes")
         List list = ms_cache.get(key);
         Date timestamp = (Date) list.get(0);
         if (timestamp.before(current))
            pages.remove();
      }
   }

   /**
    * Clear the cache and stop the cleanup process.
    */
   public static void shutdown()
   {
      ms_cache.clear();
      ms_stopped = true;
   }
   
   /**
    * Adds the provided page to the cache with a newly created unique cacheid. 
    * Creates the expiration date after which the page will be removed from 
    * this cache.
    *
    * @param page the error page document, not <code>null</code>.
    * @return the unique cacheid created for this page.
    * @throws IllegalArgumentException if the provided page is 
    *    <code>null</code>.
    */
   public static synchronized int addPage(Document page)
   {
      if (page == null)
         throw new IllegalArgumentException("page cannot be null");
      
      List<Object> list = Collections.synchronizedList(new ArrayList<Object>(2));
      list.add(new Date(System.currentTimeMillis() + ms_cacheTimeout));
      list.add(page);
      
      int id = ms_nextId++;
      if (ms_nextId == Integer.MAX_VALUE)
         ms_nextId = 0;
      
      ms_cache.put(new Integer(id), list);
      
      return id;
   }
   
   /**
    * Get the page for the provided cacheid. The timestamp will be reset to
    * the current time.
    *
    * @param the cacheid of the page we are looking for, might be 
    *    <code>null</code>.
    * @return the page document or <code>null</code> if not found.
    */
   public static Document getPage(Integer id)
   {
      if (id != null)
      {
         List<Object> list = ms_cache.get(id);
         if (list != null)
         {
            list.set(0, new Date(System.currentTimeMillis() + ms_cacheTimeout));
            return (Document) list.get(1);
         }
      }
      
      return null;
   }
    
   /**
    * Private since we only allow a singleton object.
    */
   PSPageCache()
   {
      setDaemon(true);
      start();
   }
   
   public long getCacheSize()
   {
      return ms_cache.size();
   }

   /**
    * Contains the single instance of this class. <code>null</code> until the
    * first time {@link #getInstance} is called. Never <code>null</code> 
    * after that.
    */
   private volatile static PSPageCache ms_instance = null;
   
   /**
    * The page cache where the key is the cacheid (Integer) and the value
    * is a list of objects, the first element representing the time (Date) 
    * this entry was created and the secon element being the page (Document).
    */
   private static Map<Integer, List<Object>> ms_cache = createFIFOCache();
  
   
   /**
    * Storage for the next cache id. Will be incremented each time a new page
    * is added to the cache. If the maximal number is reached it will start
    * over again at 0.
    */
   private static int ms_nextId = 0;

   /**
    * Set this flag to <code>true</code> to stop the page cache cleanup
    * process.
    */
   private static boolean ms_stopped = false;

   /**
    * @return the cacheTimeout
    */
   public static long getCacheTimeout()
   {
      return ms_cacheTimeout;
   }

   /**
    * @param cacheTimeout the cacheTimeout to set
    */
   public static void setCacheTimeout(long cacheTimeout)
   {
      ms_cacheTimeout = cacheTimeout;
   }

   public static Map<Integer, List<Object>> createFIFOCache()
   {
      
       LinkedHashMap<Integer, List<Object>> unSyncedHash = new  LinkedHashMap<Integer, List<Object>>(){
         private static final int MAX_ENTRIES = 1000;
         
         @Override
         protected boolean removeEldestEntry(Map.Entry eldest) {
            return size() > MAX_ENTRIES;
         }
     };
     
     return (Map<Integer, List<Object>>) Collections.synchronizedMap(unSyncedHash);
   }
   /**
    * The page cache timeout (in milli seconds) after which each cached 
    * page will be removed from the cache. Only leave the errors in cache for 5 minutes.
    */
   private static long ms_cacheTimeout = 5 *60*1000;
}
