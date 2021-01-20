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
package com.percussion.services.guidmgr.impl;


import com.percussion.data.PSIdGenerator;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.services.PSMissingBeanConfigurationException;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidHelper;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.guidmgr.data.PSDesignGuid;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.testing.IntegrationTest;
import org.apache.cactus.ServletTestCase;
import org.junit.experimental.categories.Category;

import java.util.*;
import java.util.concurrent.*;

/**
 * Test guid manager
 * @author dougrand
 */
@Category(IntegrationTest.class)
public class PSGuidManagerTest extends ServletTestCase
{

   /**
    * @param arg0
    */
   public PSGuidManagerTest(String arg0) {
      super(arg0);
   }

   public void testThreading() throws InterruptedException, ExecutionException
   { 
      
      ExecutorService executor = Executors.newFixedThreadPool(20);
      Set<Future<List<Long>>> set = new HashSet<Future<List<Long>>>();
      final int itemsInSet = 500;
      int testSets= 100;
      final IPSGuidManager mgr = PSGuidManagerLocator.getGuidMgr();

      Callable<List<Long>> callable = new Callable<List<Long>>(){

         @Override
         public List<Long> call() throws Exception
         {
            List<Long> ids = new ArrayList<Long>();
            for (int i = 0; i<itemsInSet; i++)
               ids.add(PSGuidHelper.generateNextLong(PSTypeEnum.PUB_REFERENCE_ID));

            return ids;
         }

      };

      for (int i=0; i<testSets; i++)
      {
         Future<List<Long>> future = executor.submit(callable);
         set.add(future);
      }

      List<Long> resultList = new ArrayList<Long>();

      for (Future<List<Long>> results : set)
      {
         resultList.addAll(results.get());
      }

      Collections.sort(resultList);

      long firstId = resultList.get(0);
      System.out.println("First Id Found = "+firstId);
      for (int i=1; i<resultList.size(); i++)
      {
         if (resultList.get(i) != firstId+i)
         {
            assertTrue("Non consecutive id returned firstId = "+firstId+ " expecting "+ firstId+i, firstId+i==resultList.get(i));
         }
         System.out.println("Id="+resultList.get(i));
      }

   }

   
   /**
    * Test the allocation object's behavior. The allocation object holds the
    * current allocated range of guids to be dispensed.
    * 
    * @throws Exception
    */
   public void testAllocation() throws Exception
   {
      Allocation a = new Allocation(1, 5);
      
      assertEquals(a.next(), 1);
 
      assertEquals(a.next(), 2);

      assertEquals(a.next(), 3);

      assertEquals(a.next(), 4);


   }
   
   /**
    * Test locator to find service
    * 
    * @throws PSMissingBeanConfigurationException
    */
   public void testLocator() throws PSMissingBeanConfigurationException
   {
      PSGuidManagerLocator.getGuidMgr();
   }
   
   /**
    * Test contentid extractor
    */
   public void testExtractor() 
   {
      IPSGuidManager mgr = PSGuidManagerLocator.getGuidMgr();
      List<IPSGuid> ids = new ArrayList<IPSGuid>();
      
      ids.add(mgr.makeGuid(1, PSTypeEnum.ACL));
      
      // Check error cases
      try
      {
         mgr.extractContentIds(ids);
         fail();
      }
      catch(IllegalArgumentException e)
      {
         // OK
      }
      
      try
      {
         ids.clear();
         mgr.extractContentIds(ids);
         fail();
      }
      catch(IllegalArgumentException e)
      {
         // OK
      }
      
      ids.add(new PSLegacyGuid(301, 1));
      List<Integer> cids = mgr.extractContentIds(ids);
      assertEquals(1, cids.size());
      assertEquals(301, cids.get(0).intValue());
      ids.add(new PSLegacyGuid(302, 1));
      cids = mgr.extractContentIds(ids);
      assertEquals(2, cids.size());
      assertEquals(301, cids.get(0).intValue());
      assertEquals(302, cids.get(1).intValue());
   }
   
   /**
    * Test various methods to generate guids
    * 
    * @throws PSMissingBeanConfigurationException
    */
   public void testGenerateGuid() throws PSMissingBeanConfigurationException
   {
      IPSGuidManager mgr = PSGuidManagerLocator.getGuidMgr();
      
      IPSGuid guid = mgr.createGuid(PSTypeEnum.INVALID);
      System.out.println(guid.toString());
      assertEquals(mgr.getHostId(), guid.getHostId());
      assertEquals(PSTypeEnum.INVALID.getOrdinal(), guid.getType());
      
      List<IPSGuid> guids = mgr.createGuids(PSTypeEnum.INVALID, 110);
      Iterator<IPSGuid> iter = guids.iterator();
      IPSGuid last = iter.next();
      System.out.println(last.toString());
      while(iter.hasNext())
      {
         guid = iter.next();
         assertEquals(1, guid.getUUID() - last.getUUID());
         last = guid;
      }
      System.out.println(last.toString());
      assertEquals(guids.size(), 110);
   }
   
   /**
    * Next next long counter
    * 
    * @throws PSMissingBeanConfigurationException
    */
   public void testNextLong() throws PSMissingBeanConfigurationException
   {
      IPSGuidManager mgr = PSGuidManagerLocator.getGuidMgr();
      
      long a = mgr.createLongId(PSTypeEnum.PUB_REFERENCE_ID);
      long b = mgr.createLongId(PSTypeEnum.PUB_REFERENCE_ID);
      long c = mgr.createLongId(PSTypeEnum.PUB_REFERENCE_ID);
      long d = mgr.createLongId(PSTypeEnum.PUB_REFERENCE_ID);
      
      assertTrue(b > a);
      assertTrue(c > b);
      assertTrue(d > c);
      
      // Do a loop that covers more than a single block
      long last = d;
      for(int i = 0; i < 300; i++)
      {
         long next = mgr.createLongId(PSTypeEnum.PUB_REFERENCE_ID);
         assertTrue("Next " + next + " must be one more than last " + last, 
               (next - last) == 1);
         last = next;
      }
   }
   
   /**
    * Create a repository guid
    * 
    * @throws Exception
    */
   public void testGeneratedRepositoryGuid() throws Exception
   {
      IPSGuidManager mgr = PSGuidManagerLocator.getGuidMgr();
      
      IPSGuid guid = mgr.createGuid((byte) 1, PSTypeEnum.INVALID);
      System.out.println(guid.toString());
      assertEquals(PSTypeEnum.INVALID.getOrdinal(), guid.getType());
      
      List<IPSGuid> guids = mgr.createGuids((byte) 1, PSTypeEnum.INVALID, 110);
      Iterator<IPSGuid> iter = guids.iterator();
      IPSGuid last = iter.next();
      System.out.println(last.toString());
      while(iter.hasNext())
      {
         guid = iter.next();
         assertEquals(1, guid.getUUID() - last.getUUID());
         last = guid;
      }
      System.out.println(last.toString());
      assertEquals(guids.size(), 110);
   }
   
   /**
    * Test allocation
    * @throws Exception
    */
   public void testNextNumber() throws Exception
   {
      IPSGuidManager mgr = PSGuidManagerLocator.getGuidMgr();
      IPSGuid tguid = mgr.createGuid(PSTypeEnum.TEMPLATE);
      
      assertEquals(0, tguid.getHostId());
      assertTrue(tguid.getUUID() < Integer.MAX_VALUE);
   }
   
   /**
    * Test next number behavior. The guid manager handles next number 
    * allocations as well as guid allocations
    * 
    * @throws Exception
    */
   public void testIdGenerator() throws Exception
   {
      int id = PSIdGenerator.getNextId("slotid");
      assertTrue(id != 0);
      
      int ids[] = PSIdGenerator.getNextIdBlock("variantid",14);
      int lastid = 0;
      for(int i = 0; i < ids.length; i++)
      {
         assertTrue(ids[i] != 0);
         assertTrue(ids[i] > lastid);
         lastid = ids[i];
      }
      
      int nextid = PSIdGenerator.getNextId("slotid");
      assertTrue(nextid > id);
      System.out.println("Last slotid " + nextid);
      
   }

   /**
    * Test the factory methods used to create concrete guids from external
    * representations
    * 
    * @throws Exception
    */
   public void testFactoryMethods() throws Exception
   {
      IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
      
      IPSGuid val = gmgr.makeGuid(1, PSTypeEnum.ACL);
      assertNotNull(val);
      assertEquals(PSGuid.class, val.getClass());
      assertEquals(1, val.getUUID());
      assertEquals(PSTypeEnum.ACL.getOrdinal(), val.getType());
      
      IPSGuid val2 = gmgr.makeGuid("1", PSTypeEnum.ACL);
      assertEquals(val, val2);
      
      PSDesignGuid dg = new PSDesignGuid(PSTypeEnum.ACL, 1L);
      long raw = dg.getValue();
      
      val2 = gmgr.makeGuid(raw, PSTypeEnum.ACL);
      assertEquals(val, val2);
      
      try
      {
         val2 = gmgr.makeGuid(raw, PSTypeEnum.ACTION);
         fail("Supplied type doesn't match type in raw value.");
      }
      catch (Exception success) {}
      
      val2 = gmgr.makeGuid("0-17-1", PSTypeEnum.ACL);
      assertEquals(val, val2);
      
      val2 = gmgr.makeGuid("0-1", PSTypeEnum.ACL);
      assertEquals(val, val2);
      
      final int ITEM_ID = 501;
      final int ITEM_REV = 2;
      PSLocator loc = new PSLocator(ITEM_ID, ITEM_REV);
      IPSGuid val3 = gmgr.makeGuid(loc);
      assertNotNull(val3);
      assertEquals(PSLegacyGuid.class, val3.getClass());
      assertEquals(ITEM_ID, val3.getUUID());
      assertEquals(ITEM_REV, val3.getHostId());
      
      PSLocator loc2 = gmgr.makeLocator(val3);
      assertEquals(loc, loc2);

      // test item id conversions
      val = gmgr.makeGuid(val3.longValue(), PSTypeEnum.LEGACY_CONTENT);
      assertEquals(PSLegacyGuid.class, val.getClass());
      assertEquals(PSTypeEnum.LEGACY_CONTENT.getOrdinal(), val.getType());
      assertEquals(ITEM_REV, val.getHostId());
      assertEquals(val3, val);
      
      val = gmgr.makeGuid(val3.getUUID(), PSTypeEnum.LEGACY_CONTENT);
      assertEquals(PSLegacyGuid.class, val.getClass());
      assertEquals(PSTypeEnum.LEGACY_CONTENT.getOrdinal(), val.getType());
      assertEquals(0xFFFFFFL, val.getHostId());
      assertFalse(val.equals(val3));
      
      val = gmgr.makeGuid(String.valueOf(ITEM_ID), PSTypeEnum.LEGACY_CONTENT);
      assertEquals(PSLegacyGuid.class, val.getClass());
      assertEquals(PSTypeEnum.LEGACY_CONTENT.getOrdinal(), val.getType());
      assertEquals(0xFFFFFFL, val.getHostId());
      assertFalse(val3.equals(val));
      assertEquals(ITEM_ID, val.getUUID());
            
      val = gmgr.makeGuid(ITEM_ID, PSTypeEnum.LEGACY_CONTENT);
      assertEquals(PSLegacyGuid.class, val.getClass());
      assertEquals(PSTypeEnum.LEGACY_CONTENT.getOrdinal(), val.getType());
      assertEquals(val.getHostId(), 0xFFFFFFL);
      assertFalse(val3.equals(val));
      assertEquals(ITEM_ID, val.getUUID());
      
      val = gmgr.makeGuid(val3.longValue(), PSTypeEnum.LEGACY_CONTENT);
      assertEquals(PSLegacyGuid.class, val.getClass());
      assertEquals(PSTypeEnum.LEGACY_CONTENT.getOrdinal(), val.getType());
      assertEquals(val3, val);
   }
}
