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

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class PSMultiLevelCacheTest extends TestCase
{
   // see base class
   public PSMultiLevelCacheTest(String name)
   {
      super(name);
   }
   
   /**
    * Test creating caches
    * 
    * @throws Exception if the test fails or anything goes wrong.
    */
   public void testCtor() throws Exception
   {
      PSMultiLevelCache testCache = null;
      boolean didThrow;
      
      // should succeed
      testCache = new PSMultiLevelCache(1, -1);
      testCache = new PSMultiLevelCache(1, 5);
      testCache = new PSMultiLevelCache(6, 25);
      
      // test invalid keysize
      try
      {
         didThrow = false;
         testCache = new PSMultiLevelCache(0, -1);
      }
      catch (Exception e)
      {
         didThrow = true;
      }
      assertTrue("allowed invalid keysize", didThrow);
      
      try
      {
         didThrow = false;
         testCache = new PSMultiLevelCache(-1, 5);
      }
      catch (Exception e)
      {
         didThrow = true;
      }
      assertTrue("allowed invalid keysize", didThrow);
      
      
      // test invalid aging
      try
      {
         didThrow = false;
         testCache = new PSMultiLevelCache(1, 0);
      }
      catch (Exception e)
      {
         didThrow = true;
      }
      assertTrue("allowed invalid agingTime", didThrow);
      
   }
   
   /**
    * Test adding and retrieving items from the cache
    * 
    * @throws Exception if the test fails or anything goes wrong.
    */
   public void testAccess() throws Exception
   {
      // test one level
      PSMultiLevelCache cache;
      cache = new PSMultiLevelCache(1, -1);
      addAndRetrieve(cache, new Object[] {"a"}, "itema", 1);
      addAndRetrieve(cache, new Object[] {"b"}, "itemb", 0);
      addAndRetrieve(cache, new Object[] {"b"}, "itemb2", 0);
      addAndRetrieve(cache, new Object[] {"c"}, new Integer(5), 256);
      
      // test invalid params
      addBadItem(cache, null, "bad", 1);
      addBadItem(cache, new Object[] {"a", "b"}, "bad", 1);
      addBadItem(cache, new Object[] {null}, "bad", 1);
      addBadItem(cache, new Object[] {"a"}, null, 1);
      addBadItem(cache, new Object[] {"b"}, "itemb", -1);
      
      getBadItem(cache, new Object[] {null});
      getBadItem(cache, new Object[] {"a", "b"});
      getBadItem(cache, new Object[] {});
      
      // test 3 levels
      cache = new PSMultiLevelCache(3, -1);
      int x = 0;
      for (int i = 0; i < 3; i++)
      {
         for (int j = 0; j < 3; j++) 
         {
            for (int k = 0; k < 3; k++, x++) 
            {
               addAndRetrieve(cache, new Object[] {i + "", j + "", k + ""}, 
                  new Integer(x), x + 1);
            }
         }
      }
      
      // test miss
      assertNull(cache.retrieveItem(new Object[] {"1", "2", "4"}, 
         CACHE_ITEM_TYPE));
      
      // test invalid keys
      addBadItem(cache, null, "bad", 1);
      addBadItem(cache, new Object[] {"a", "b"}, "bad", 2);
      addBadItem(cache, new Object[] {"a", null, "c"}, "bad", 3);
      addBadItem(cache, new Object[] {null, "b", "c"}, "bad", 4);
      addBadItem(cache, new Object[] {"a", "b", null}, "bad", 5);
      addBadItem(cache, new Object[] {"a", "b", "c", "d"}, "bad", 6);
      
      getBadItem(cache, new Object[] {"a", "b"});
      getBadItem(cache, new Object[] {"a", "b", "c", "d"});
      getBadItem(cache, new Object[] {"a", null, "c"});
      getBadItem(cache, new Object[] {null, "b", "c"});
      getBadItem(cache, new Object[] {"a", "b", null});
   }
   
   /**
    * Test various flush operations
    * 
    * @throws Exception
    */
   public void testFlush() throws Exception
   {
      PSMultiLevelCache cache;
      cache = new PSMultiLevelCache(3, -1);
      
      // load cache
      int x = 0;
      for (int i = 0; i < 3; i++)
      {
         for (int j = 0; j < 3; j++) 
         {
            for (int k = 0; k < 3; k++, x++) 
            {
               addAndRetrieve(cache, new Object[] {i + "", j + "", k + ""}, 
                  new Integer(x), x + 1);
            }
         }
      }
      
      // now flush a leaf and be sure we can still get others in that branch
      Object[] keys = {"0", "1", "2"};
      cache.flush(keys);
      assertNull("retrieve flushed item returns null", 
         cache.retrieveItem(keys, CACHE_ITEM_TYPE));
         
      assertNotNull("retrieve item after flushing another", 
         cache.retrieveItem(new Object[] {"0", "0", "0"}, CACHE_ITEM_TYPE));
      assertNotNull("retrieve item after flushing another", 
         cache.retrieveItem(new Object[] {"0", "0", "1"}, CACHE_ITEM_TYPE));
      assertNotNull("retrieve item after flushing another", 
         cache.retrieveItem(new Object[] {"0", "0", "2"}, CACHE_ITEM_TYPE));
      assertNotNull("retrieve item after flushing another", 
         cache.retrieveItem(new Object[] {"0", "1", "0"}, CACHE_ITEM_TYPE));
      assertNotNull("retrieve item after flushing another", 
         cache.retrieveItem(new Object[] {"0", "1", "1"}, CACHE_ITEM_TYPE));
      assertNotNull("retrieve item after flushing another", 
         cache.retrieveItem(new Object[] {"0", "2", "0"}, CACHE_ITEM_TYPE));
      assertNotNull("retrieve item after flushing another", 
         cache.retrieveItem(new Object[] {"0", "2", "1"}, CACHE_ITEM_TYPE));
      assertNotNull("retrieve item after flushing another", 
         cache.retrieveItem(new Object[] {"0", "2", "2"}, CACHE_ITEM_TYPE));
      assertNotNull("retrieve item after flushing another", 
         cache.retrieveItem(new Object[] {"1", "0", "0"}, CACHE_ITEM_TYPE));
      assertNotNull("retrieve item after flushing another", 
         cache.retrieveItem(new Object[] {"1", "0", "1"}, CACHE_ITEM_TYPE));
      assertNotNull("retrieve item after flushing another", 
         cache.retrieveItem(new Object[] {"1", "0", "2"}, CACHE_ITEM_TYPE));
      assertNotNull("retrieve item after flushing another", 
         cache.retrieveItem(new Object[] {"1", "1", "0"}, CACHE_ITEM_TYPE));
      assertNotNull("retrieve item after flushing another", 
         cache.retrieveItem(new Object[] {"1", "1", "1"}, CACHE_ITEM_TYPE));
      assertNotNull("retrieve item after flushing another", 
         cache.retrieveItem(new Object[] {"1", "1", "2"}, CACHE_ITEM_TYPE));
      assertNotNull("retrieve item after flushing another", 
         cache.retrieveItem(new Object[] {"1", "2", "0"}, CACHE_ITEM_TYPE));
      assertNotNull("retrieve item after flushing another", 
         cache.retrieveItem(new Object[] {"1", "2", "1"}, CACHE_ITEM_TYPE));
      assertNotNull("retrieve item after flushing another", 
         cache.retrieveItem(new Object[] {"1", "2", "2"}, CACHE_ITEM_TYPE));
      assertNotNull("retrieve item after flushing another", 
         cache.retrieveItem(new Object[] {"2", "0", "0"}, CACHE_ITEM_TYPE));
      assertNotNull("retrieve item after flushing another", 
         cache.retrieveItem(new Object[] {"2", "0", "1"}, CACHE_ITEM_TYPE));
      assertNotNull("retrieve item after flushing another", 
         cache.retrieveItem(new Object[] {"2", "0", "2"}, CACHE_ITEM_TYPE));
      assertNotNull("retrieve item after flushing another", 
         cache.retrieveItem(new Object[] {"2", "1", "0"}, CACHE_ITEM_TYPE));
      assertNotNull("retrieve item after flushing another", 
         cache.retrieveItem(new Object[] {"2", "1", "1"}, CACHE_ITEM_TYPE));
      assertNotNull("retrieve item after flushing another", 
         cache.retrieveItem(new Object[] {"2", "1", "2"}, CACHE_ITEM_TYPE));
      assertNotNull("retrieve item after flushing another", 
         cache.retrieveItem(new Object[] {"2", "2", "0"}, CACHE_ITEM_TYPE));
      assertNotNull("retrieve item after flushing another", 
         cache.retrieveItem(new Object[] {"2", "2", "1"}, CACHE_ITEM_TYPE));
      assertNotNull("retrieve item after flushing another", 
         cache.retrieveItem(new Object[] {"2", "2", "2"}, CACHE_ITEM_TYPE));

      // flush a branch
      cache.flush(new Object[] {"0", "2", null});
      assertNull("retreive item in flushed branch returns null", 
         cache.retrieveItem(new Object[] {"0", "2", "0"}, CACHE_ITEM_TYPE));
      assertNull("retreive item in flushed branch returns null", 
         cache.retrieveItem(new Object[] {"0", "2", "1"}, CACHE_ITEM_TYPE));
      assertNull("retreive item in flushed branch returns null", 
         cache.retrieveItem(new Object[] {"0", "2", "2"}, CACHE_ITEM_TYPE));
         
      assertNotNull("retrieve item after flushing another", 
         cache.retrieveItem(new Object[] {"0", "0", "0"}, CACHE_ITEM_TYPE));
      assertNotNull("retrieve item after flushing another", 
         cache.retrieveItem(new Object[] {"0", "0", "1"}, CACHE_ITEM_TYPE));
      assertNotNull("retrieve item after flushing another", 
         cache.retrieveItem(new Object[] {"0", "0", "2"}, CACHE_ITEM_TYPE));
      assertNotNull("retrieve item after flushing another", 
         cache.retrieveItem(new Object[] {"0", "1", "0"}, CACHE_ITEM_TYPE));
      assertNotNull("retrieve item after flushing another", 
         cache.retrieveItem(new Object[] {"0", "1", "1"}, CACHE_ITEM_TYPE));
      assertNotNull("retrieve item after flushing another", 
         cache.retrieveItem(new Object[] {"1", "0", "0"}, CACHE_ITEM_TYPE));
      assertNotNull("retrieve item after flushing another", 
         cache.retrieveItem(new Object[] {"1", "0", "1"}, CACHE_ITEM_TYPE));
      assertNotNull("retrieve item after flushing another", 
         cache.retrieveItem(new Object[] {"1", "0", "2"}, CACHE_ITEM_TYPE));
      assertNotNull("retrieve item after flushing another", 
         cache.retrieveItem(new Object[] {"1", "1", "0"}, CACHE_ITEM_TYPE));
      assertNotNull("retrieve item after flushing another", 
         cache.retrieveItem(new Object[] {"1", "1", "1"}, CACHE_ITEM_TYPE));
      assertNotNull("retrieve item after flushing another", 
         cache.retrieveItem(new Object[] {"1", "1", "2"}, CACHE_ITEM_TYPE));
      assertNotNull("retrieve item after flushing another", 
         cache.retrieveItem(new Object[] {"1", "2", "0"}, CACHE_ITEM_TYPE));
      assertNotNull("retrieve item after flushing another", 
         cache.retrieveItem(new Object[] {"1", "2", "1"}, CACHE_ITEM_TYPE));
      assertNotNull("retrieve item after flushing another", 
         cache.retrieveItem(new Object[] {"1", "2", "2"}, CACHE_ITEM_TYPE));
      assertNotNull("retrieve item after flushing another", 
         cache.retrieveItem(new Object[] {"2", "0", "0"}, CACHE_ITEM_TYPE));
      assertNotNull("retrieve item after flushing another", 
         cache.retrieveItem(new Object[] {"2", "0", "1"}, CACHE_ITEM_TYPE));
      assertNotNull("retrieve item after flushing another", 
         cache.retrieveItem(new Object[] {"2", "0", "2"}, CACHE_ITEM_TYPE));
      assertNotNull("retrieve item after flushing another", 
         cache.retrieveItem(new Object[] {"2", "1", "0"}, CACHE_ITEM_TYPE));
      assertNotNull("retrieve item after flushing another", 
         cache.retrieveItem(new Object[] {"2", "1", "1"}, CACHE_ITEM_TYPE));
      assertNotNull("retrieve item after flushing another", 
         cache.retrieveItem(new Object[] {"2", "1", "2"}, CACHE_ITEM_TYPE));
      assertNotNull("retrieve item after flushing another", 
         cache.retrieveItem(new Object[] {"2", "2", "0"}, CACHE_ITEM_TYPE));
      assertNotNull("retrieve item after flushing another", 
         cache.retrieveItem(new Object[] {"2", "2", "1"}, CACHE_ITEM_TYPE));
      assertNotNull("retrieve item after flushing another", 
         cache.retrieveItem(new Object[] {"2", "2", "2"}, CACHE_ITEM_TYPE));
         

      // flush middle branch
      cache.flush(new Object[] {null, "1", null});               
      assertNull("retreive item in flushed branch returns null", 
         cache.retrieveItem(new Object[] {"0", "1", "0"}, CACHE_ITEM_TYPE));
      assertNull("retreive item in flushed branch returns null", 
         cache.retrieveItem(new Object[] {"0", "1", "1"}, CACHE_ITEM_TYPE));
      assertNull("retreive item in flushed branch returns null", 
         cache.retrieveItem(new Object[] {"1", "1", "0"}, CACHE_ITEM_TYPE));
      assertNull("retreive item in flushed branch returns null", 
         cache.retrieveItem(new Object[] {"1", "1", "1"}, CACHE_ITEM_TYPE));
      assertNull("retreive item in flushed branch returns null", 
         cache.retrieveItem(new Object[] {"1", "1", "2"}, CACHE_ITEM_TYPE));
      assertNull("retreive item in flushed branch returns null", 
         cache.retrieveItem(new Object[] {"2", "1", "0"}, CACHE_ITEM_TYPE));
      assertNull("retreive item in flushed branch returns null", 
         cache.retrieveItem(new Object[] {"2", "1", "1"}, CACHE_ITEM_TYPE));
      assertNull("retreive item in flushed branch returns null", 
         cache.retrieveItem(new Object[] {"2", "1", "2"}, CACHE_ITEM_TYPE));

      assertNotNull("retrieve item after flushing another", 
         cache.retrieveItem(new Object[] {"0", "0", "0"}, CACHE_ITEM_TYPE));
      assertNotNull("retrieve item after flushing another", 
         cache.retrieveItem(new Object[] {"0", "0", "1"}, CACHE_ITEM_TYPE));
      assertNotNull("retrieve item after flushing another", 
         cache.retrieveItem(new Object[] {"0", "0", "2"}, CACHE_ITEM_TYPE));
      assertNotNull("retrieve item after flushing another", 
         cache.retrieveItem(new Object[] {"1", "0", "0"}, CACHE_ITEM_TYPE));
      assertNotNull("retrieve item after flushing another", 
         cache.retrieveItem(new Object[] {"1", "0", "1"}, CACHE_ITEM_TYPE));
      assertNotNull("retrieve item after flushing another", 
         cache.retrieveItem(new Object[] {"1", "0", "2"}, CACHE_ITEM_TYPE));
      assertNotNull("retrieve item after flushing another", 
         cache.retrieveItem(new Object[] {"1", "2", "0"}, CACHE_ITEM_TYPE));
      assertNotNull("retrieve item after flushing another", 
         cache.retrieveItem(new Object[] {"1", "2", "1"}, CACHE_ITEM_TYPE));
      assertNotNull("retrieve item after flushing another", 
         cache.retrieveItem(new Object[] {"1", "2", "2"}, CACHE_ITEM_TYPE));
      assertNotNull("retrieve item after flushing another", 
         cache.retrieveItem(new Object[] {"2", "0", "0"}, CACHE_ITEM_TYPE));
      assertNotNull("retrieve item after flushing another", 
         cache.retrieveItem(new Object[] {"2", "0", "1"}, CACHE_ITEM_TYPE));
      assertNotNull("retrieve item after flushing another", 
         cache.retrieveItem(new Object[] {"2", "0", "2"}, CACHE_ITEM_TYPE));
      assertNotNull("retrieve item after flushing another", 
         cache.retrieveItem(new Object[] {"2", "2", "0"}, CACHE_ITEM_TYPE));
      assertNotNull("retrieve item after flushing another", 
         cache.retrieveItem(new Object[] {"2", "2", "1"}, CACHE_ITEM_TYPE));
      assertNotNull("retrieve item after flushing another", 
         cache.retrieveItem(new Object[] {"2", "2", "2"}, CACHE_ITEM_TYPE));
         

         
      // flush top branch
      cache.flush(new Object[] {"0", null, null});               
      assertNull("retreive item in flushed branch returns null", 
         cache.retrieveItem(new Object[] {"0", "0", "0"}, CACHE_ITEM_TYPE));
      assertNull("retreive item in flushed branch returns null", 
         cache.retrieveItem(new Object[] {"0", "0", "1"}, CACHE_ITEM_TYPE));
      assertNull("retreive item in flushed branch returns null", 
         cache.retrieveItem(new Object[] {"0", "0", "2"}, CACHE_ITEM_TYPE));

      assertNotNull("retrieve item after flushing another", 
         cache.retrieveItem(new Object[] {"1", "0", "0"}, CACHE_ITEM_TYPE));
      assertNotNull("retrieve item after flushing another", 
         cache.retrieveItem(new Object[] {"1", "0", "1"}, CACHE_ITEM_TYPE));
      assertNotNull("retrieve item after flushing another", 
         cache.retrieveItem(new Object[] {"1", "0", "2"}, CACHE_ITEM_TYPE));
      assertNotNull("retrieve item after flushing another", 
         cache.retrieveItem(new Object[] {"1", "2", "0"}, CACHE_ITEM_TYPE));
      assertNotNull("retrieve item after flushing another", 
         cache.retrieveItem(new Object[] {"1", "2", "1"}, CACHE_ITEM_TYPE));
      assertNotNull("retrieve item after flushing another", 
         cache.retrieveItem(new Object[] {"1", "2", "2"}, CACHE_ITEM_TYPE));
      assertNotNull("retrieve item after flushing another", 
         cache.retrieveItem(new Object[] {"2", "0", "0"}, CACHE_ITEM_TYPE));
      assertNotNull("retrieve item after flushing another", 
         cache.retrieveItem(new Object[] {"2", "0", "1"}, CACHE_ITEM_TYPE));
      assertNotNull("retrieve item after flushing another", 
         cache.retrieveItem(new Object[] {"2", "0", "2"}, CACHE_ITEM_TYPE));
      assertNotNull("retrieve item after flushing another", 
         cache.retrieveItem(new Object[] {"2", "2", "0"}, CACHE_ITEM_TYPE));
      assertNotNull("retrieve item after flushing another", 
         cache.retrieveItem(new Object[] {"2", "2", "1"}, CACHE_ITEM_TYPE));
      assertNotNull("retrieve item after flushing another", 
         cache.retrieveItem(new Object[] {"2", "2", "2"}, CACHE_ITEM_TYPE));
      
      // flush based on partial key
      cache.flush(new Object[] {"2", null, "0"});               
      assertNull("retreive item in flushed branch returns null", 
         cache.retrieveItem(new Object[] {"2", "0", "0"}, CACHE_ITEM_TYPE));
      assertNull("retreive item in flushed branch returns null", 
         cache.retrieveItem(new Object[] {"2", "2", "0"}, CACHE_ITEM_TYPE));
         
      assertNotNull("retrieve item after flushing another", 
         cache.retrieveItem(new Object[] {"1", "0", "0"}, CACHE_ITEM_TYPE));
      assertNotNull("retrieve item after flushing another", 
         cache.retrieveItem(new Object[] {"1", "0", "1"}, CACHE_ITEM_TYPE));
      assertNotNull("retrieve item after flushing another", 
         cache.retrieveItem(new Object[] {"1", "0", "2"}, CACHE_ITEM_TYPE));
      assertNotNull("retrieve item after flushing another", 
         cache.retrieveItem(new Object[] {"1", "2", "0"}, CACHE_ITEM_TYPE));
      assertNotNull("retrieve item after flushing another", 
         cache.retrieveItem(new Object[] {"1", "2", "1"}, CACHE_ITEM_TYPE));
      assertNotNull("retrieve item after flushing another", 
         cache.retrieveItem(new Object[] {"1", "2", "2"}, CACHE_ITEM_TYPE));
      assertNotNull("retrieve item after flushing another", 
         cache.retrieveItem(new Object[] {"2", "0", "1"}, CACHE_ITEM_TYPE));
      assertNotNull("retrieve item after flushing another", 
         cache.retrieveItem(new Object[] {"2", "0", "2"}, CACHE_ITEM_TYPE));
      assertNotNull("retrieve item after flushing another", 
         cache.retrieveItem(new Object[] {"2", "2", "1"}, CACHE_ITEM_TYPE));
      assertNotNull("retrieve item after flushing another", 
         cache.retrieveItem(new Object[] {"2", "2", "2"}, CACHE_ITEM_TYPE));
      
      // flush whole thing
      cache.flush();
      assertNull("retrieve item after flush returns null", 
         cache.retrieveItem(new Object[] {"1", "0", "0"}, CACHE_ITEM_TYPE));
      assertNull("retrieve item after flush returns null", 
         cache.retrieveItem(new Object[] {"1", "0", "1"}, CACHE_ITEM_TYPE));
      assertNull("retrieve item after flush returns null", 
         cache.retrieveItem(new Object[] {"1", "0", "2"}, CACHE_ITEM_TYPE));
      assertNull("retrieve item after flush returns null", 
         cache.retrieveItem(new Object[] {"1", "2", "0"}, CACHE_ITEM_TYPE));
      assertNull("retrieve item after flush returns null", 
         cache.retrieveItem(new Object[] {"1", "2", "1"}, CACHE_ITEM_TYPE));
      assertNull("retrieve item after flush returns null", 
         cache.retrieveItem(new Object[] {"1", "2", "2"}, CACHE_ITEM_TYPE));
      assertNull("retrieve item after flush returns null", 
         cache.retrieveItem(new Object[] {"2", "0", "1"}, CACHE_ITEM_TYPE));
      assertNull("retrieve item after flush returns null", 
         cache.retrieveItem(new Object[] {"2", "0", "2"}, CACHE_ITEM_TYPE));
      assertNull("retrieve item after flush returns null", 
         cache.retrieveItem(new Object[] {"2", "2", "1"}, CACHE_ITEM_TYPE));
      assertNull("retrieve item after flush returns null", 
         cache.retrieveItem(new Object[] {"2", "2", "2"}, CACHE_ITEM_TYPE));
   }
   
   
   /**
    * Tests using listeners for access and modified events.
    * 
    * @throws Exception if any errors occur.
    */
   public void testListeners() throws Exception
   {
      PSMultiLevelCache cache;
      cache = new PSMultiLevelCache(3, -1);
      
      IPSCacheAccessedListener accessListener = new IPSCacheAccessedListener()
      {
         public void cacheAccessed(PSCacheEvent e)
         {
            m_accessAction = e.getAction();
            m_accessObject = e.getObject();
         }
      };
      cache.addCacheAccessedListener(accessListener);
      
      IPSCacheModifiedListener modifyListener = new IPSCacheModifiedListener()
      {
         public void cacheModified(PSCacheEvent e)
         {
            m_modifyActions.add(new Integer(e.getAction()));
            m_modifyObjects.add(e.getObject());
         }
         
         public void setCache(PSMultiLevelCache cache)         
         {
            m_modifyCache = cache;
         }
      };
      m_modifyCache = null;
      cache.addCacheModifiedListener(modifyListener);
      
      assertSame(cache, m_modifyCache);

      // add an item and see if we get the event.    
      m_modifyActions.clear();
      m_modifyObjects.clear();
      Object[] keys = {"a", "b", "c"};
      cache.addItem(keys, "test", 10000, CACHE_ITEM_TYPE);
      assertEquals("add event action", m_modifyActions.get(0), 
         new Integer(PSCacheEvent.CACHE_ITEM_ADDED));
      assertEquals("add event object", 
         ((PSCacheItem)m_modifyObjects.get(0)).getObject(), "test");
      
      // replace the item, should get both a flush and an add
      m_modifyActions.clear();
      m_modifyObjects.clear();
      cache.addItem(keys, "test2", 10000, CACHE_ITEM_TYPE);
      assertEquals("replace event action", m_modifyActions.get(0), 
         new Integer(PSCacheEvent.CACHE_ITEM_REMOVED));
      assertNull("replace event object", 
         ((PSCacheItem)m_modifyObjects.get(0)).getObject());
      assertEquals("replace event keys", 
         ((PSCacheItem)m_modifyObjects.get(0)).getKeys(), keys);
      assertEquals("replace event action", m_modifyActions.get(1), 
         new Integer(PSCacheEvent.CACHE_ITEM_ADDED));
      assertEquals("replace event object", 
         ((PSCacheItem)m_modifyObjects.get(1)).getObject(), "test2");
      
            
      // flush an item, should get flush event
      m_modifyActions.clear();
      m_modifyObjects.clear();
      cache.flush(keys);
      assertEquals("flush event action", m_modifyActions.get(0), 
         new Integer(PSCacheEvent.CACHE_ITEM_REMOVED));
      assertNull("flush event object", 
         ((PSCacheItem)m_modifyObjects.get(0)).getObject());
      assertEquals("flush event keys", 
         ((PSCacheItem)m_modifyObjects.get(0)).getKeys(), keys);
      
      // retrieve non-existant item, should get miss event.
      m_accessAction = -1;
      m_accessObject = "test";
      cache.retrieveItem(new Object[] {"1", "2", "3"}, CACHE_ITEM_TYPE);
      assertEquals(m_accessAction, PSCacheEvent.CACHE_ITEM_NOT_FOUND);
      assertNull(m_accessObject);
   }
   
   
   /**
    * Add and retrieves the supplied object using the supplied keys, comparing
    * the result to be sure it is the same object.
    * 
    * @param cache The cache to add to, assumed not <code>null</code>.
    * @param keys The keys to use, assumed to be valid keys.
    * @param item The item to add, assumed not <code>null</code>.
    * @param size The size of the item to add.
    * 
    * @throws Exception if the comparison fails or anything goes wrong.
    */
   private void addAndRetrieve(PSMultiLevelCache cache, Object[] keys, 
      Object item, long size) throws Exception
   {
      cache.addItem(keys, item, size, CACHE_ITEM_TYPE);
      Object result = cache.retrieveItem(keys, CACHE_ITEM_TYPE);
      assertSame("add and retrieve did not return same object", item, result);
   }
   
   /**
    * Ensure that adding the specified item fails.
    * 
    * @param cache The cache to add to, assumed not <code>null</code>.
    * @param keys The keys to use, assumed to be valid keys.
    * @param item The item to add, assumed not <code>null</code>.
    * @param size The size of the item to add.
    * 
    * @throws Exception if the test fails or anything goes wrong.
    */
   private void addBadItem(PSMultiLevelCache cache, Object[] keys, 
      Object item, long size) throws Exception
   {
      boolean didThrow = false;
      try 
      {
         cache.addItem(keys, item, size, CACHE_ITEM_TYPE);
      }
      catch (Exception ex) 
      {
         didThrow = true;
      }
      assertTrue("add bad item did not throw", didThrow); 
   }
   
   /**
    * Ensure that retrieving the specified item fails.
    * 
    * @param cache The cache to add to, assumed not <code>null</code>.
    * @param keys The keys to use, assumed to be valid keys.
    * 
    * @throws Exception if the test fails or anything goes wrong.
    */
   private void getBadItem(PSMultiLevelCache cache, Object[] keys) 
      throws Exception
   {
      boolean didThrow = false;
      try 
      {
         cache.retrieveItem(keys, CACHE_ITEM_TYPE);
      }
      catch (Exception ex) 
      {
         didThrow = true;
      }
      assertTrue("get bad item did not throw", didThrow); 
   }
   
   
   // collect all tests into a TestSuite and return it - see base class
   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new PSMultiLevelCacheTest("testCtor"));
      suite.addTest(new PSMultiLevelCacheTest("testAccess"));
      suite.addTest(new PSMultiLevelCacheTest("testFlush"));
      suite.addTest(new PSMultiLevelCacheTest("testListeners"));
      return suite;
   }
 
   /**
    * Stores action caused by access listener event.
    */
   private int m_accessAction = -1;
   
   /**
    * Stores object passed by access listener event.
    */
   private Object m_accessObject = null;
   
   /**
    * Stores actions caused by modify listener event.
    */
   private List m_modifyActions = new ArrayList();
   
   /**
    * Stores objects passed by access listener event.
    */
   private List m_modifyObjects = new ArrayList();
   
   /**
    * Stores cache set by the callback after adding a modified listener.
    */
   private PSMultiLevelCache m_modifyCache = null;
   
   /**
    * Constant for type of item in the cache.
    */
   private static final String CACHE_ITEM_TYPE = "foo";
}
