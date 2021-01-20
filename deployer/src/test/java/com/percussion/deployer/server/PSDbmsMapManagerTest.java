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
