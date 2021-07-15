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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for the {@link PSCacheStatistics} class.
 */
public class PSCacheStatisticsTest extends TestCase
{
   // see base class
   public PSCacheStatisticsTest(String name)
   {
      super(name);
   }

   // collect all tests into a TestSuite and return it - see base class
   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new PSCacheStatisticsTest("testCacheDisabled"));
      suite.addTest(new PSCacheStatisticsTest("testStatistics"));
      return suite;
   }

   /**
    * Tests that the cache statistics thread does not start unless the caching
    * is enabled.
    * 
    * @throws Exception
    */
   public void testCacheDisabled() throws Exception
   {
      //Test that thread is not started since caching is disabled.
      PSServerCacheSettings settings = new PSServerCacheSettings();
      PSCacheStatistics statistics = new PSCacheStatistics(settings);

      Object test = new Object();
      Object[] keys = {"key1", "key2"};
      long size = 256;
      int id = 1;

      //fire some events
      fireEvent(statistics, PSCacheEvent.CACHE_ITEM_ADDED, 
         id, test, keys, size);
      fireEvent(statistics, PSCacheEvent.CACHE_ITEM_ADDED, 
         id, test, keys, size);
      fireEvent(statistics, PSCacheEvent.CACHE_ITEM_STORED_TO_DISK, 
         id, test, keys, size);
      fireEvent(statistics, PSCacheEvent.CACHE_ITEM_ACCESSED_FROM_MEMORY, 
         id, test, keys, size);
      fireEvent(statistics, PSCacheEvent.CACHE_ITEM_ACCESSED_FROM_DISK, 
         id, test, keys, size);   
      fireEvent(statistics, PSCacheEvent.CACHE_ITEM_NOT_FOUND, 
         id, test, keys, size);                                                       
         
      sleep( 50 );           
      
      assertSnapshotValues(statistics, 0, 0, 0, 0, 0, 0, 0);
   }
   
   /**
    * Tests the <code>PSCacheStatistics</code> for statistics snapshot by firing
    * different events.
    * 
    * @throws Exception
    */
   public void testStatistics() throws Exception
   {
      PSServerCacheSettings settings = new PSServerCacheSettings(
         true, true, 100*1024*1024, 1000*1024*1024, 100*1024, -1);
      PSCacheStatistics statistics = new PSCacheStatistics(settings);
      
      Object test = new Object();
      Object[] keys = {"key1", "key2"};
      long size = 256;
      int id = 1;
      
      fireEvent(statistics, PSCacheEvent.CACHE_ITEM_ADDED, 
         id, test, keys, size);
      fireEvent(statistics, PSCacheEvent.CACHE_ITEM_ADDED, 
         id, test, keys, size);
      fireEvent(statistics, PSCacheEvent.CACHE_ITEM_ADDED, 
         id, test, keys, size); 
                 
      sleep( 50 );
      assertSnapshotValues(statistics, 3*size, 0, 0, 0, size, 0, 0);
      
      fireEvent(statistics, PSCacheEvent.CACHE_ITEM_ADDED, 
         id, test, keys, size);         
      fireEvent(statistics, PSCacheEvent.CACHE_ITEM_STORED_TO_DISK, 
         id, test, keys, size);         
      fireEvent(statistics, PSCacheEvent.CACHE_ITEM_ACCESSED_FROM_MEMORY, 
         id, test, keys, size);
      fireEvent(statistics, PSCacheEvent.CACHE_ITEM_ACCESSED_FROM_MEMORY, 
         id, test, keys, size);
      fireEvent(statistics, PSCacheEvent.CACHE_ITEM_ACCESSED_FROM_MEMORY, 
         id, test, keys, size);
         
      sleep( 50 );
      assertSnapshotValues(statistics, 3*size, 3, size, 3, size, 0, 100);         
      
      fireEvent(statistics, PSCacheEvent.CACHE_ITEM_ACCESSED_FROM_DISK, 
         id, test, keys, size); 
      fireEvent(statistics, PSCacheEvent.CACHE_ITEM_NOT_FOUND, 
         id, test, keys, size);                             
         
      sleep( 50 );        
      assertSnapshotValues(statistics, 4*size, 4, 0, 5, size, 25, 80);
     
      fireEvent(statistics, PSCacheEvent.CACHE_ITEM_REMOVED, 
         id, test, keys, size);
      fireEvent(statistics, PSCacheEvent.CACHE_ITEM_REMOVED, 
         id, test, keys, size);
         
      sleep( 50 );
      assertSnapshotValues(statistics, 2*size, 4, 0, 5, size, 25, 80);
   }
   
   /**
    * Makes the current thread sleep for specified milli seconds.
    * 
    * @param millis the number of milli seconds to sleep, assumed to be >= 0.
    */
   private void sleep( long millis)
   {
      try {
         Thread.currentThread().sleep( millis );
      }
      catch(InterruptedException e)
      {
      }
   }
   
   /**
    * Fires the event specified by <code>action</code> on the <code>statistics
    * </code> object.
    * 
    * @param statistics the object that needs to be notified, assumed not to be
    * <code>null</code>
    * @param action the event action, assumed to be one of <code>
    * PSCacheEvent.CACHE_xxx</code> types.
    * @param id id of the cache in which the cached item is stored.
    * @param obj the cached object, assumed not to be <code>null</code>
    * @param keys the keys to identify the cached item, assumed not to be <code>
    * null</code>
    * @param size the size of cached object in bytes, assumed to be >= 0
    */
   private void fireEvent(PSCacheStatistics statistics, int action, 
      int id, Object obj, Object[] keys, long size)
   {
      PSCacheEvent event = new PSCacheEvent(
         action, new PSCacheItem(id, obj, keys, size) );
         
      switch(action)
      {
         case PSCacheEvent.CACHE_ITEM_ADDED:
         case PSCacheEvent.CACHE_ITEM_REMOVED:
         case PSCacheEvent.CACHE_ITEM_STORED_TO_DISK:
            statistics.cacheModified( event );
            break;
            
         case PSCacheEvent.CACHE_ITEM_ACCESSED_FROM_DISK:
         case PSCacheEvent.CACHE_ITEM_ACCESSED_FROM_MEMORY:
         case PSCacheEvent.CACHE_ITEM_NOT_FOUND:
            statistics.cacheAccessed( event );
            break;
      }
   }
   
   /**
    * Asserts that cache statistics snapshot values are same as expected values.
    * 
    * @param statistics the object from which to get the snapshot, assumed not
    * to be <code>null</code>.
    * @param memUsage the amount of memory usage, assumed not to be < 0.
    * @param totalHits the number of total hits, assumed not to be < 0. 
    * @param diskUsage the amount of disk usage, assumed not to be < 0.
    * @param totalRequests the total requests, assumed not to be < 0.
    * @param avgItemSize the average item size, assumed not to be < 0.
    * @param diskHitRate the disk hit rate to access an item, assumed not to be
    * < 0.
    * @param hitRate the cache hit rate to access an item, assumed not to be 
    * < 0.
    */
   private void assertSnapshotValues(PSCacheStatistics statistics, 
      long memUsage, long totalHits, long diskUsage, long totalRequests, 
      long avgItemSize, int diskHitRate, int hitRate)
   {
      PSCacheStatisticsSnapshot snapshot = statistics.getSnapshot();
      assertEquals(snapshot.getMemoryUsage(), memUsage);
      assertEquals(snapshot.getTotalHits(), totalHits);
      assertEquals(snapshot.getDiskUsage(), diskUsage);
      assertEquals(snapshot.getTotalRequests(), totalRequests);
      assertEquals(snapshot.getAvgItemSize(), avgItemSize);
      assertEquals(snapshot.getDiskHitRate(), diskHitRate);
      assertEquals(snapshot.getHitRate(), hitRate);
   }
}
