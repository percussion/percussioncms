/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.percussion.services;

import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.catalog.IPSCatalogSummary;
import com.percussion.services.catalog.IPSCataloger;
import com.percussion.services.catalog.PSCatalogException;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.filter.IPSFilterService;
import com.percussion.services.filter.PSFilterServiceLocator;
import com.percussion.services.publisher.IPSPublisherService;
import com.percussion.services.publisher.PSPublisherServiceLocator;

import java.util.List;

import com.percussion.utils.testing.IntegrationTest;
import org.apache.cactus.ServletTestCase;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runners.MethodSorters;

/**
 * Test cataloging interface on the services
 * 
 * @author dougrand
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Category(IntegrationTest.class)
public class PSCatalogingTest extends ServletTestCase
{
   /**
    * Get assembly service  
    */
   static IPSAssemblyService asm = PSAssemblyServiceLocator
         .getAssemblyService();
   /**
    * Get filter service
    */
   static IPSFilterService fsvc = PSFilterServiceLocator.getFilterService();
   /**
    * Get publisher service 
    */
   static IPSPublisherService pub = PSPublisherServiceLocator.getPublisherService();

   /**
    * @throws Exception
    */
   
   @Test
   public void test010FilterCataloging() throws Exception
   {
      testCataloging(fsvc);
   }
   
   /**
    * 
    */
   
   @Test
   public void test020AsmTypes()
   {
      checkTypes(asm.getTypes(), 2);
   }
   
   /**
    * 
    */
   
   @Test
   public void test030FilterTypes()
   {
      checkTypes(fsvc.getTypes(), 1);
   }
   
   /**
    * 
    */
   
   @Test
   public void test040PublisherTypes()
   {
      checkTypes(pub.getTypes(), 1);
   }

   /**
    * @param types
    * @param expectedCount
    */
   private void checkTypes(PSTypeEnum types[], int expectedCount)
   {
      assertNotNull(types);
      assertEquals(expectedCount, types.length);
   }

   /**
    * @throws Exception
    */
   
   @Test
   public void test050AsmCataloging() throws Exception
   {
      testCataloging(asm);
   }
   
   


   
   /**
    * @throws Exception
    */
   
   @Test
   public void test060PublisherCataloging() throws Exception
   {
      testCataloging(pub);
   }
  
   
   
   /**
    * @param cat
    * @throws PSCatalogException
    */
   private void testCataloging(IPSCataloger cat) throws PSCatalogException, PSNotFoundException {
      for(PSTypeEnum type : cat.getTypes())
      {
         testCataloging(cat, type);
      }
   }

   /**
    * @param cat
    * @param type
    * @throws PSCatalogException
    */
   private void testCataloging(IPSCataloger cat, PSTypeEnum type)
           throws PSCatalogException, PSNotFoundException {
      List<IPSCatalogSummary> sums = cat.getSummaries(type);

      assertNotNull(sums);
      assertTrue(sums.size() > 0);
      int limit = 4;

      // Serialize each, then restore each
      for (IPSCatalogSummary s : sums)
      {
         // Keep the time and the number of objects reasonable
         if (limit-- < 0) break; 
         String value = cat.saveByType(s.getGUID());

         // Restore
         try
         {
            cat.loadByType(type, value);
         }
         catch(PSCatalogException ce)
         {
            throw ce;
         }
         catch(RuntimeException e)
         {
            throw e;
         }
      }
   }

}
