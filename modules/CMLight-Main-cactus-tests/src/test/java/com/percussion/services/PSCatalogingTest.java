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
package com.percussion.services;

import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.catalog.IPSCatalogSummary;
import com.percussion.services.catalog.IPSCataloger;
import com.percussion.services.catalog.PSCatalogException;
import com.percussion.services.catalog.PSTypeEnum;
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
   private void testCataloging(IPSCataloger cat) throws PSCatalogException
   {
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
         throws PSCatalogException
   {
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
