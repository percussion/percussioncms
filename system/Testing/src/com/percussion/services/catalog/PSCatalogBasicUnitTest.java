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
