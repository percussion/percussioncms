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

