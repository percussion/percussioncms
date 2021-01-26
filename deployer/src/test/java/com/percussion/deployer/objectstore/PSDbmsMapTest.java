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


package com.percussion.deployer.objectstore;

import com.percussion.xml.PSXmlDocumentBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit test class for the <code>PSDbmsMapTest</code> class.
 */
public class PSDbmsMapTest
{
   @Rule
   public TemporaryFolder temporaryFolder = new TemporaryFolder();
   private String rxdeploydir;

   @Before
   public void setup() throws IOException {

      rxdeploydir = System.getProperty("rxdeploydir");
      System.setProperty("rxdeploydir", temporaryFolder.getRoot().getAbsolutePath());
   }

   @After
   public void teardown(){
      if(rxdeploydir != null)
         System.setProperty("rxdeploydir",rxdeploydir);
   }

   /**
    * Construct this unit test
    *
    */
    public PSDbmsMapTest()
   {
      super();
   }

   /**
    * Test constructing this object using parameters
    *
    * @throws Exception If there are any errors.
    */
   @Test
   public void testConstructor() throws Exception
   {
      PSDbmsInfo dbms1 = new PSDbmsInfo("rx-ds", "driver", "server", "db",
            "orig", "uid", "pwd", false);
      PSDbmsInfo dbms2 = new PSDbmsInfo("rx-ds1", "driver1", "server1", "db",
            "orig", "uid", "pwd", false);
      PSDbmsInfo dbms3 = new PSDbmsInfo("rx-ds2", "driver2", "server2", "db",
            "orig", "uid", "pwd", false);
      PSDbmsInfo dbms4 = new PSDbmsInfo("rx-ds3", "driver3", "server3", "db",
            "orig", "uid", "pwd", false);

      PSDbmsMapping mapping1 = new PSDbmsMapping(new PSDatasourceMap(dbms1
            .getDatasource(), dbms2.getDatasource()));

      PSDbmsMapping mapping2 = new PSDbmsMapping(new PSDatasourceMap(dbms3
            .getDatasource(), dbms4.getDatasource()));

      // these should work fine
      assertTrue(testCtorValid("srcServer1", mapping1));
      assertTrue(testCtorValid("srcServer2", mapping2));

      // should be a problem
      assertTrue(!testCtorValid(null, mapping1));
      assertTrue(!testCtorValid("", mapping1));
      assertTrue(!testCtorValid("aaa", null));
   }

   /**
    * Tests the equals and getMapping methods
    *
    * @throws Exception if there are any errors.
    */
   @Test
   public void testEquals() throws Exception
   {
      PSDbmsInfo dbms1 = new PSDbmsInfo("rx-ds", "driver", "server", "db",
            "orig", "uid", "pwd", false);
      PSDbmsInfo dbms2 = new PSDbmsInfo("rx-ds2", "driver2", "server2", "db",
            "orig", "uid", "pwd", false);

      PSDbmsInfo same_dbms1 = new PSDbmsInfo("rx-ds", "driver", "server", "db",
            "orig", "uid", "pwd", false);
      PSDbmsInfo same_dbms2 = new PSDbmsInfo("rx-ds2", "driver2", "server2",
            "db", "orig", "uid", "pwd", false);

      PSDbmsInfo dbms3 = new PSDbmsInfo("rx-ds3", "driver3", "server3", "db",
            "orig", "uid", "pwd", false);
      PSDbmsInfo dbms4 = new PSDbmsInfo("rx-ds4", "driver4", "server3", "db",
            "orig", "uid", "pwd", false);

      PSDbmsMapping mapping1 = new PSDbmsMapping(new PSDatasourceMap(dbms1
            .getDatasource(), dbms2.getDatasource()));

      PSDbmsMapping same_mapping1 = new PSDbmsMapping(new PSDatasourceMap(
            same_dbms1.getDatasource(), same_dbms2.getDatasource()));

      PSDbmsMap map1 = new PSDbmsMap("srcServer1");
      map1.addMapping(same_mapping1);
      PSDbmsMap same_map1 = new PSDbmsMap("srcServer1");
      same_map1.addMapping(same_mapping1);

      // check equal
      assertTrue(map1.equals(same_map1));

      PSDbmsMapping mapping3 = new PSDbmsMapping(new PSDatasourceMap(dbms3
            .getDatasource(), dbms4.getDatasource()));
      map1.addMapping(mapping3);

      // check not equal
      assertTrue(!map1.equals(same_map1));

      // test getMapping()
      PSDbmsMapping mapping1_get = map1.getMapping(dbms1.getDatasource());
      assertTrue(mapping1_get.equals(mapping1));

      PSDbmsMapping mapping3_get = map1.getMapping(dbms3.getDatasource());
      assertTrue(mapping3_get.equals(mapping3));

      assertTrue(!mapping3_get.equals(mapping1_get));

      // check copy
      same_map1.copyFrom(map1);
      assertEquals(map1, same_map1);
   }

   /**
    * Tests all Xml functions, and uses equals as well.
    *
    * @throws Exception if there are any errors.
    */
   @Test
   public void testXml() throws Exception
   {
      PSDbmsInfo dbms1 = new PSDbmsInfo("rx-ds", "driver", "server", "db",
            "orig", "uid", "pwd", false);
      PSDbmsInfo dbms1_1 = new PSDbmsInfo("rx-ds1_1", "driver", "server1_1",
            "db", "orig", "uid", "pwd", false);
      PSDbmsInfo dbms2 = new PSDbmsInfo("rx-ds2", "driver2", "server2", "db",
            "orig", "uid", "pwd", false);
      PSDbmsInfo dbms3 = new PSDbmsInfo("rx-ds3", "driver3", "server3", "db",
            "orig", "uid", "pwd", false);
      PSDbmsMapping mapping1 = new PSDbmsMapping(new PSDatasourceMap(dbms1
            .getDatasource(), dbms1_1.getDatasource()));
      PSDbmsMapping mapping2 = new PSDbmsMapping(new PSDatasourceMap(dbms2
            .getDatasource(), ""));
      PSDbmsMapping mapping3 = new PSDbmsMapping(new PSDatasourceMap(dbms3
            .getDatasource(), ""));

      PSDbmsMap map1 = new PSDbmsMap("srcServer");
      map1.addMapping(mapping1);
      map1.addMapping(mapping2);
      map1.addMapping(mapping3);

      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element mapEl = map1.toXml(doc);

      PSDbmsMap mapFromXml = new PSDbmsMap(mapEl);

      assertTrue(map1.equals(mapFromXml));
   }

   /**
    * Constructs a <code>PSDbmsMap</code> object using the
    * supplied params and catches any exception.  For params,
    * see {@link PSDbmsMap} ctor.
    *
    * @return <code>true</code> if no exceptions were caught, <code>false</code>
    * otherwise.
    */
   private boolean testCtorValid(String srcServer, PSDbmsMapping mapping)
   {
      try
      {
         PSDbmsMap map = new PSDbmsMap(srcServer);
         map.addMapping(mapping);
      }
      catch (Exception ex)
      {
         return false;
      }

      return true;
   }

}
