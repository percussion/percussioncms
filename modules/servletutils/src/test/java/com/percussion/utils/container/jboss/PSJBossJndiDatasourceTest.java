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

package com.percussion.utils.container.jboss;
import org.junit.Ignore;


/**
 * Test case for the {@link PSJBossJndiDatasource} class.
 */
@Ignore
public class PSJBossJndiDatasourceTest 
{
/***
   
    * Tests the constructor that takes the datasource props.
    * 
    * @throws Exception if the test fails
    -/
   public void testCtor() throws Exception
   {
      Class[] params = new Class[7];
      for (int i = 0; i < params.length; i++)
      {
         params[i] = String.class;
      }
      
      PSTestUtils.testCtor(PSJBossJndiDatasource.class, params, new Object[]
      {"name", "driverName", "classname", "mapping", "server", "uid", "pwd"},
         false);
      PSTestUtils.testCtor(PSJBossJndiDatasource.class, params, new Object[]
      {"", "driverName", "classname", "mapping", "server", "uid", "pwd"}, true);
      PSTestUtils.testCtor(PSJBossJndiDatasource.class, params, new Object[]
      {null, "driverName", "classname", "mapping", "server", "uid", "pwd"},
         true);
      PSTestUtils.testCtor(PSJBossJndiDatasource.class, params, new Object[]
      {"name", "", "classname", "mapping", "server", "uid", "pwd"}, true);
      PSTestUtils.testCtor(PSJBossJndiDatasource.class, params, new Object[]
      {"name", null, "classname", "mapping", "server", "uid", "pwd"}, true);
      PSTestUtils.testCtor(PSJBossJndiDatasource.class, params, new Object[]
      {"name", "driverName", "", "mapping", "server", "uid", "pwd"}, true);
      PSTestUtils.testCtor(PSJBossJndiDatasource.class, params, new Object[]
      {"name", "driverName", null, "mapping", "server", "uid", "pwd"}, true);
      PSTestUtils.testCtor(PSJBossJndiDatasource.class, params, new Object[]
      {"name", "driverName", "classname", "", "server", "uid", "pwd"}, true);
      PSTestUtils.testCtor(PSJBossJndiDatasource.class, params, new Object[]
      {"name", "driverName", "classname", null, "server", "uid", "pwd"}, true);
      PSTestUtils.testCtor(PSJBossJndiDatasource.class, params, new Object[]
      {"name", "driverName", "classname", "mapping", "", "uid", "pwd"}, true);
      PSTestUtils.testCtor(PSJBossJndiDatasource.class, params, new Object[]
      {"name", "driverName", "classname", "mapping", null, "uid", "pwd"}, true);
      PSTestUtils.testCtor(PSJBossJndiDatasource.class, params, new Object[]
      {"name", "driverName", "classname", "mapping", "server", "", ""}, false);
      PSTestUtils.testCtor(PSJBossJndiDatasource.class, params, new Object[]
      {"name", "driverName", "classname", "mapping", "server", null, null},
         false);
   }

   /**
    * Tests the ctor that takes an element, and the toXml() method.
    * 
    * @throws Exception if the test fails
    -/
   public void testXml() throws Exception
   {
      PSJBossJndiDatasource ds = PSServer.getContainerUtils().getNewJndiDatasource("name", "jtds:sqlserver",
         "classname", "mapping", "server", "uid", "pwd");
      
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      assertTrue(ds.equals(PSServer.getContainerUtils().getNewJndiDatasource(ds.toXml(doc))));
      PSJBossJndiDatasource ds2 = PSServer.getContainerUtils().getNewJndiDatasource("name", "jtds:sqlserver",
         "classname", "mapping", "server", null, null);
      assertTrue(ds2.equals(PSServer.getContainerUtils().getNewJndiDatasource(ds2.toXml(doc))));
      
      // test security domain setting
      ds.setSecurityDomain("myDomain");
      ds2 = PSServer.getContainerUtils().getNewJndiDatasource(ds.toXml(doc));
      assertTrue(!ds.equals(ds2));
      assertNull(ds2.getUserId());
      assertNull(ds2.getPassword());
      assertTrue(ds2.equals(PSServer.getContainerUtils().getNewJndiDatasource(ds2.toXml(doc))));
      
      // test conn checker/exception sorter
      ds = PSServer.getContainerUtils().getNewJndiDatasource("name", "oracle:thin", "classname", "mapping",
         "server", "uid", "pwd");
      assertTrue(ds.equals(PSServer.getContainerUtils().getNewJndiDatasource(ds.toXml(doc))));
   }

   /**
    * Tests all set and get methdos
    * 
    * @throws Exception if the test fails
    -/
   public void testAccessors() throws Exception
   {
      PSJBossJndiDatasource ds = PSServer.getContainerUtils().getNewJndiDatasource("name", "jtds:sqlserver",
         "classname", "mapping", "server", "uid", "pwd");
      assertEquals("name", ds.getName());
      assertEquals("jtds:sqlserver", ds.getDriverName());
      assertEquals("classname", ds.getDriverClassName());
      assertEquals("mapping", ds.getContainerTypeMapping());
      assertEquals("server", ds.getServer());
      assertEquals("uid", ds.getUserId());
      assertEquals("pwd", ds.getPassword());
      
      PSTestUtils.testSetter(ds, "Name", "name2", false);
      PSTestUtils.testSetter(ds, "DriverName", "oracle:thin", false);
      PSTestUtils.testSetter(ds, "DriverClassName", "classname2", false);
      PSTestUtils.testSetter(ds, "ContainerTypeMapping", "mapping2", false);
      PSTestUtils.testSetter(ds, "Server", "server2", false);
      PSTestUtils.testSetter(ds, "UserId", "uid2", false);
      PSTestUtils.testSetter(ds, "Password", "pwd2", false);
      
      PSTestUtils.testSetter(ds, "Name", "", true);
      PSTestUtils.testSetter(ds, "DriverName", "", true);
      PSTestUtils.testSetter(ds, "DriverClassName", "", true);
      PSTestUtils.testSetter(ds, "ContainerTypeMapping", "", true);
      PSTestUtils.testSetter(ds, "Server", "", true);
      PSTestUtils.testSetter(ds, "UserId", "", false);
      PSTestUtils.testSetter(ds, "Password", "", false);
 
      PSTestUtils.testSetter(ds, "Name", null, true);
      PSTestUtils.testSetter(ds, "DriverName", null, true);
      PSTestUtils.testSetter(ds, "DriverClassName", null, true);
      PSTestUtils.testSetter(ds, "ContainerTypeMapping", null, true);
      PSTestUtils.testSetter(ds, "Server", null, true);
      PSTestUtils.testSetter(ds, "UserId", null, false);
      PSTestUtils.testSetter(ds, "Password", null, false);
      
      PSTestUtils.testSetter(ds, "SecurityDomain", null, false);
      PSTestUtils.testSetter(ds, "SecurityDomain", "test", false);
      PSTestUtils.testSetter(ds, "SecurityDomain", "", true);
  
   } 
   **/
}



