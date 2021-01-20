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
package com.percussion.services.pkginfo;

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.pkginfo.data.PSIdName;
import com.percussion.services.pkginfo.impl.PSIdNameService;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.testing.IntegrationTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Test case for the {@link PSIdNameService} class.
 */
@Category(IntegrationTest.class)
public class PSIdNameServiceTest
{
   /**
    * Test saving and loading id-name mappings.
    * 
    * @throws Exception if the test fails.
    */
   @Test
   public void testIdNameMappings() throws Exception
   {
      IPSIdNameService svc = null;
      
      try
      {
         svc = PSIdNameServiceLocator.getIdNameService();
         
         // make sure we've got the service
         assertNotNull(svc);
         
         // test save
         String name1 = "acl";
         PSTypeEnum type1 = PSTypeEnum.ACL;
         IPSGuid guid1 = new PSGuid(type1, 301);
         PSIdName mapping1 = new PSIdName(guid1.toString(), name1);
         
         svc.saveIdName(mapping1);
         
         // test find saved item
         IPSGuid guid2 = svc.findId(name1, type1);
         assertEquals(guid2, guid1);
         String name2 = svc.findName(guid2);
         assertEquals(name2, name1);
         
         // test find name case in-sensitive
         String name3 = "ACL";
         IPSGuid guid3 = svc.findId(name3, type1);
         assertEquals(guid3, guid1);
         
         // test find non-existent id
         String name4 = "workflow";
         PSTypeEnum type4 = PSTypeEnum.WORKFLOW;
         IPSGuid guid4 = svc.findId(name4, type4);
         assertNull(guid4);         
         
         // test find non-existent name
         IPSGuid guid5 = new PSGuid(type4, 11);
         String name5 = svc.findName(guid5);
         assertNull(name5);
      }
      finally
      {
         // cleanup
         if (svc != null)
         {
            svc.deleteAll();
         }
      }
   }
}

