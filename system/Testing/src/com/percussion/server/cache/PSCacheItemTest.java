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

import java.io.File;
import java.util.Date;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for the {@link PSCacheItem} class.
 */
public class PSCacheItemTest extends TestCase
{
   // see base class
   public PSCacheItemTest(String name)
   {
      super(name);
   }

   /**
    * Test constructing the <code>PSCacheItem</code> class.
    * 
    * @throws Exception if any errors occur.
    */
   public void testCtor() throws Exception
   {
      PSCacheItem item = null;
      boolean didThrow = false;
      
      // test valid object
      String test = "myTest";
      Object[] keys = {"one", "two", "Three"};
      long size = 256;
      item = new PSCacheItem(0, test, keys, size);
      
      //test null object
      didThrow = false;
      try 
      {
         item = new PSCacheItem(0, null, keys, size);
      }
      catch (Exception ex) 
      {
         didThrow = true;
      }
      assertTrue("ctor with null object", didThrow);
      
      //test null keys
      didThrow = false;
      try 
      {
         item = new PSCacheItem(0, test, null, size);
      }
      catch (Exception ex) 
      {
         didThrow = true;
      }
      assertTrue("ctor with null keys", didThrow);
      
      //test negative size
      didThrow = false;
      try 
      {
         item = new PSCacheItem(0, test, keys, -1);
      }
      catch (Exception ex) 
      {
         didThrow = true;
      }
      assertTrue("ctor with negative size", didThrow);
      
   }
   
   /**
    * Tests accessing a <code>PSCacheItem</code> once it is constructed.
    * 
    * @throws Exception if any errors occur.
    */
   public void dontTestAccess() throws Exception
   {
      PSCacheItem item = null;
      String test = "myTest";
      Object[] keys = {"one", "two", "Three"};
      long size = 256;
      int id = 25;
      Date start = new Date();
      item = new PSCacheItem(id, test, keys, size);
      Date end = new Date();
      assertTrue("test is in memory after ctor", item.isInMemory());
      assertTrue("test is on disk after ctor", !item.isOnDisk());
      assertEquals("test getObject from memory", test, item.getObject());
      assertEquals("test getCacheid", id, item.getCacheId());
      assertEquals("test getKeys", keys, item.getKeys());
      assertEquals("test getSize", size, item.getSize());
      Date created = item.getCreatedDate();
      
      assertTrue("test getCreatedDate", !created.before(start) && 
         !created.after(end));

      Date accessed = item.getLastAccessedDate();
      assertTrue("test last accessed from ctor", !accessed.before(start) &&
         !accessed.after(end));
         
      start = new Date();
      Object o = item.getObject();
      end = new Date();
      accessed = item.getLastAccessedDate();
      assertTrue("test last accessed after getObject", !accessed.before(start) 
         && !accessed.after(end));
      
      assertTrue("test is in memory after access from memory", 
         item.isInMemory());
      assertTrue("test is on disk after access from memory", !item.isOnDisk());
      
      item.release();
      assertNull(item.getObject());
         
   }

   /**
    * Tests storing a <code>PSCacheItem</code> on disk and then accessing it.
    * 
    * @throws Exception if any errors occur.
    */
   public void testDiskOps() throws Exception
   {
      PSCacheItem item = null;
      String test = "myTest";
      Object[] keys = {"one", "two", "Three"};
      long size = 256;
      int id = 25;
      item = new PSCacheItem(id, test, keys, size);
      
      assertTrue("item is in memory", item.isInMemory());
      
      item.toDisk(new File("."));
      assertTrue("item is on disk", item.isOnDisk());
      assertTrue("item is not in memory", !item.isInMemory());
      
      Date start = new Date();
      Object o = item.getObject();
      Date end = new Date();
      assertEquals("item from disk is equal", test, o);
      assertTrue("item is in memory after getObject from disk", 
         item.isInMemory());
      assertTrue("item is not on disk after getObject from disk", 
         !item.isOnDisk());
      Date accessed = item.getLastAccessedDate();
      assertTrue("test last accessed after getObject from disk", 
         !accessed.before(start) 
         && !accessed.after(end));
         
      item.toDisk(new File("."));
      item.release();
      assertNull(item.getObject());
   }
   
   /**
    * Tests using listeners for access and modified events.
    * 
    * @throws Exception if any errors occur.
    */
   public void testListeners() throws Exception
   {
      PSCacheItem item = null;
      String test = "myTest";
      Object[] keys = {"one", "two", "Three"};
      long size = 256;
      int id = 25;
      item = new PSCacheItem(id, test, keys, size);
      
      IPSCacheAccessedListener accessListener = new IPSCacheAccessedListener()
      {
         public void cacheAccessed(PSCacheEvent e)
         {
            m_accessAction = e.getAction();
            m_accessItem = (PSCacheItem)e.getObject();
         }
      };
      item.addCacheAccessedListener(accessListener);
      
      IPSCacheModifiedListener modifyListener = new IPSCacheModifiedListener()
      {
         public void cacheModified(PSCacheEvent e)
         {
            m_modifyAction = e.getAction();
            m_modifyItem = (PSCacheItem)e.getObject();
         }
         
         public void setCache(PSMultiLevelCache cache)         
         {
            //noop
         }
      };
      item.addCacheModifiedListener(modifyListener);
      
      m_accessAction = -1;
      m_accessItem = null;
      item.getObject();
      assertEquals(PSCacheEvent.CACHE_ITEM_ACCESSED_FROM_MEMORY, 
         m_accessAction);
      assertTrue(item == m_accessItem);
         
      m_modifyAction = -1;
      m_modifyItem = null;
      item.toDisk(new File("."));
      assertEquals(PSCacheEvent.CACHE_ITEM_STORED_TO_DISK, 
         m_modifyAction);
      assertTrue(item == m_modifyItem);
         
      m_accessAction = -1;
      m_accessItem = null;
      m_modifyAction = -1;
      m_modifyItem = null;
      item.getObject();
      assertEquals(PSCacheEvent.CACHE_ITEM_ACCESSED_FROM_DISK, 
         m_accessAction);
      assertTrue(item == m_accessItem);
         
   }

   
   // collect all tests into a TestSuite and return it - see base class
   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new PSCacheItemTest("testCtor"));
      // suite.addTest(new PSCacheItemTest("testAccess")); todo: commented out due to unreliable timing issue
      suite.addTest(new PSCacheItemTest("testDiskOps"));
      suite.addTest(new PSCacheItemTest("testListeners"));
      return suite;
   }
 
   /**
    * Stores action caused by access listener event.
    */
   private int m_accessAction = -1;
   
   /**
    * Stores item passed by access listener event.
    */
   private PSCacheItem m_accessItem = null;
   
   /**
    * Stores action caused by modify listener event.
    */
   private int m_modifyAction = -1;
   
   /**
    * Stores item passed by access listener event.
    */
   private PSCacheItem m_modifyItem = null;
}
