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
package com.percussion.services.catalog;

import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.filter.IPSFilterService;
import com.percussion.services.filter.PSFilterServiceLocator;
import com.percussion.services.publisher.IPSPublisherService;
import com.percussion.services.publisher.PSPublisherServiceLocator;

import java.util.List;

import com.percussion.utils.testing.IntegrationTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit test basic methods for enumerating data in the services. Each service is
 * tested using common code for the enumeration and "saving" portion. Specific
 * code is required for each service to test the "loading" portion
 * 
 * @author dougrand
 * 
 */
@Category(IntegrationTest.class)
public class PSCatalogBasicUnitTest
{
   public static IPSAssemblyService asm = PSAssemblyServiceLocator
         .getAssemblyService();

   public static IPSFilterService fsvc = PSFilterServiceLocator
         .getFilterService();

   public static IPSPublisherService psvc = PSPublisherServiceLocator
         .getPublisherService();

   @Test
   public void testAssemblyEnumeration() throws PSCatalogException, PSNotFoundException {
      doEnumerationTest(asm);
   }

   @Test
   public void testFilterEnumeration() throws PSCatalogException, PSNotFoundException {
      doEnumerationTest(fsvc);
   }

   @Test
   public void testPublisherEnumeration() throws PSCatalogException, PSNotFoundException {
      doEnumerationTest(psvc);
   }

   private void doEnumerationTest(IPSCataloger cat) throws PSCatalogException, PSNotFoundException {
      // Check getTypes
      PSTypeEnum[] types = cat.getTypes();
      
      assertNotNull(types);
      assertTrue(types.length >= 1);
      
      // Now get data for each type
      for(PSTypeEnum t : types)
      {
         List<IPSCatalogSummary> sumaries = cat.getSummaries(t);
         assertNotNull(sumaries);
         if (sumaries.size() == 0)
         {
            System.err.println("Warning: no elements returned for type " + t);
         }
         for(IPSCatalogSummary s : sumaries)
         {
            String xml = cat.saveByType(s.getGUID());
            assertNotNull(xml);
            assertTrue(xml.length() > 0);
            System.out.println(s);
         }
      }
      
      
   }
}
