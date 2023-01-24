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

package com.percussion.deployer.server;

import com.percussion.deployer.objectstore.PSDatasourceMap;
import com.percussion.deployer.objectstore.PSDbmsInfo;
import com.percussion.deployer.objectstore.PSDbmsMap;
import com.percussion.deployer.objectstore.PSDbmsMapping;
import com.percussion.utils.testing.IntegrationTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertTrue;

/**
 * Unit test class for the <code>PSDbmsMapManager</code> class.
 */
@Category(IntegrationTest.class)
public class PSDbmsMapManagerTest
{

    public PSDbmsMapManagerTest(){
   }

   /**
    * Test saving and retrieving <code>PSDbmsMap</code> object
    *
    * @throws Exception If there are any errors.
    */
   @Test
   public void testSaveThenGet() throws Exception
   {

      try {

         // Test SAVE
         PSDbmsInfo dbms1 = new PSDbmsInfo("driver", "server", "db", "orig",
            "uid", "pwd", false);
         PSDbmsInfo dbms2 = new PSDbmsInfo("driver2", "server2", "db", "orig",
            "uid", "pwd", false);
         PSDbmsInfo dbms3 = new PSDbmsInfo("driver3", "server3", "db", "orig",
            "uid", "pwd", false);
         PSDbmsMapping mapping1 = new PSDbmsMapping(
               new PSDatasourceMap(dbms1.getDatasource(), dbms2.getDatasource()));

         PSDbmsMapping mapping2 = new PSDbmsMapping(
               new PSDatasourceMap(dbms3.getDatasource(), ""));

         PSDbmsMap mapSave = new PSDbmsMap("PSDbmsMap:ManagerTest1:ben");
         mapSave.addMapping(mapping1);
         mapSave.addMapping(mapping2);

         PSDbmsMapManager.saveDbmsMap(mapSave);

         // Test GET
         PSDbmsMap mapGet = PSDbmsMapManager.getDbmsMap(
            "PSDbmsMap:ManagerTest1:ben");

         assertTrue(mapGet.equals(mapSave));

      }
      catch (Exception e)
      {
         System.out.println("\nCaught ERROR: " + e.toString());
      }
   }

}
