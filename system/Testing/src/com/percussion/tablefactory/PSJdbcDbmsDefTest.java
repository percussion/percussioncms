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
package com.percussion.tablefactory;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * Unit test for PSJdbcDbmsDef.
 */
public class PSJdbcDbmsDefTest
{

   @Rule
   public TemporaryFolder temporaryFolder = new TemporaryFolder();

   private String rxdeploydir;

   @Before
   public void setup(){
      rxdeploydir = System.getProperty("rxdeploydir");
      System.setProperty("rxdeploydir",temporaryFolder.getRoot().getAbsolutePath());
   }

   @After
   public void teardown(){
      //Reset the deploy dir property if it was set prior to test
      if(rxdeploydir != null)
         System.setProperty("rxdeploydir",rxdeploydir);
   }


   public PSJdbcDbmsDefTest() {}

   /**
    * Test the def
    */
   @Test
   public void testDef() throws Exception
   {
      Properties props = new Properties();
      props.setProperty(PSJdbcDbmsDef.DB_BACKEND_PROPERTY, "MSSQL");
      props.setProperty(PSJdbcDbmsDef.DB_DRIVER_CLASS_NAME_PROPERTY,
         "com.inet.tds.TdsDriver");
      props.setProperty(PSJdbcDbmsDef.DB_DRIVER_NAME_PROPERTY, "inetdae7");
      props.setProperty(PSJdbcDbmsDef.DB_SERVER_PROPERTY, "Fry");
      props.setProperty(PSJdbcDbmsDef.DB_NAME_PROPERTY, "rxMaster");
      props.setProperty(PSJdbcDbmsDef.DB_SCHEMA_PROPERTY, "dbo");
      props.setProperty(PSJdbcDbmsDef.UID_PROPERTY, "Rhythmyx");
      props.setProperty(PSJdbcDbmsDef.PWD_PROPERTY, "Rhythmyx");

      PSJdbcDbmsDef def = new PSJdbcDbmsDef(props);

      assertEquals(props.getProperty(PSJdbcDbmsDef.DB_BACKEND_PROPERTY),
         def.getBackEndDB());
      assertEquals(props.getProperty(PSJdbcDbmsDef.DB_DRIVER_CLASS_NAME_PROPERTY),
         def.getDriverClassName());
      assertEquals(props.getProperty(PSJdbcDbmsDef.DB_DRIVER_NAME_PROPERTY),
         def.getDriver());
      assertEquals(props.getProperty(PSJdbcDbmsDef.DB_SERVER_PROPERTY),
         def.getServer());
      assertEquals(props.getProperty(PSJdbcDbmsDef.DB_NAME_PROPERTY),
         def.getDataBase());
      assertEquals(props.getProperty(PSJdbcDbmsDef.DB_SCHEMA_PROPERTY),
         def.getSchema());
      assertEquals(props.getProperty(PSJdbcDbmsDef.UID_PROPERTY),
         def.getUserId());
      assertEquals(props.getProperty(PSJdbcDbmsDef.PWD_PROPERTY),
         def.getPassword());

      PSJdbcDbmsDef def2 = new PSJdbcDbmsDef(props);
      assertTrue(def.equals(def2));

   }


}
