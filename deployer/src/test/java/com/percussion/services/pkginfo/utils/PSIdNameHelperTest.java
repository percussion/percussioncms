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
package com.percussion.services.pkginfo.utils;

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.pkginfo.PSIdNameServiceLocator;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.testing.IntegrationTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test id-name helper
 */
@Category(IntegrationTest.class)
public class PSIdNameHelperTest
{
   /**
    * Test various methods to perform id-name translation.
    * 
    * @throws Exception if the test fails.
    */
   @Test
   public void testAll() throws Exception
      
   {
      try
      {
         // test supported type round-trip
         String depName = "Application";
         PSTypeEnum depType = PSTypeEnum.APPLICATION;
         
         // make sure it's supported
         assertTrue(PSIdNameHelper.isSupported(depType));
         
         // get a guid, check the type, and verify the name
         IPSGuid guid1 = PSIdNameHelper.getGuid(depName, depType);
         assertEquals(guid1.getType(), depType.getOrdinal());
         String name = PSIdNameHelper.getName(guid1);
         assertEquals(name, depName);
         
         // get the guid for the returned name and type, should match original
         IPSGuid guid2 = PSIdNameHelper.getGuid(name, 
               PSTypeEnum.valueOf(guid1.getType()));
         assertEquals(guid2, guid1);
         
         // test mixed case name, returned guid and name should match originals
         IPSGuid guidMixedCase = PSIdNameHelper.getGuid("ApplicatioN", depType);
         assertEquals(guidMixedCase, guid1);
         String nameMixedCase = PSIdNameHelper.getName(guidMixedCase);
         assertEquals(nameMixedCase, depName);

         // test unsupported type
         IPSGuid guid3 = PSIdNameHelper.getGuid("11", PSTypeEnum.WORKFLOW);
         assertNotNull(guid3);
         assertFalse(guid3.equals(guid1));
         
         boolean exception = false;
         try
         {
            name = PSIdNameHelper.getName(guid3);
         }
         catch (IllegalArgumentException e)
         {
            exception = true;
         }
         
         assertTrue(exception);
      }
      finally
      {
         // clean up
         PSIdNameServiceLocator.getIdNameService().deleteAll();
      }
   }
}
