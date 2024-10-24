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
package com.percussion.services.datasource;

import com.percussion.utils.jdbc.PSDatasourceConfig;
import com.percussion.xml.PSXmlDocumentBuilder;

import junit.framework.TestCase;

import org.w3c.dom.Document;

/**
 * Unit test for the {@link PSDatasourceConfig} class.
 */
public class PSDatasourceConfigTest extends TestCase
{
   /**
    * Construct a test case
    * 
    * @param name The name of the test.
    */
   public PSDatasourceConfigTest(String name)
   {
      super(name);
   }

   /**
    * Test constructors 
    * 
    * @throws Exception If there are any errors or failures.
    */
   public void testCtor() throws Exception
   {
      String name = "test";
      String dsName = "testds";
      String origin = "dbo";
      String database = "rxrhino";
      
      PSDatasourceConfig config;
      
      //test valid, no origin or db
      config = new PSDatasourceConfig(name, dsName, null, null);
      assertEquals(name, config.getName());
      assertEquals(dsName, config.getDataSource());
      assertEquals("", config.getOrigin());
      assertEquals("", config.getDatabase());

      // test valid, empty origin or db
      config = new PSDatasourceConfig(name, dsName, "", "");
      assertEquals(name, config.getName());
      assertEquals(dsName, config.getDataSource());
      assertEquals("", config.getOrigin());
      assertEquals("", config.getDatabase());

      // test valid, with origin/db
      config = new PSDatasourceConfig(name, dsName, origin, database);
      assertEquals(name, config.getName());
      assertEquals(dsName, config.getDataSource());
      assertEquals(origin, config.getOrigin());
    
      // test invalid
      boolean didThrow = false;
      try
      {
         config = new PSDatasourceConfig(null, dsName, origin, database);
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);

      didThrow = false;
      try
      {
         config = new PSDatasourceConfig("", dsName, origin, database);
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);

      didThrow = false;
      try
      {
         config = new PSDatasourceConfig(name, null, origin, database);
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);
      
      didThrow = false;
      try
      {
         config = new PSDatasourceConfig(name, "", origin, database);
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);      
   }

   /**
    * Test setter and getter methods
    * 
    * @throws Exception If there are any errors or failures.
    */
   public void testAccessors() throws Exception
   {
      String name = "test";
      String dsName = "testds";
      String origin = "dbo";
      String database = "rxrhino";
      String name2 = "test2";
      String dsName2 = "testds2";
      String origin2 = "dbo2";
      String database2 = "rxrhino2";

      
      PSDatasourceConfig config;
      
      // construct and basic getter test
      config = new PSDatasourceConfig(name, dsName, origin, database);
      assertEquals(name, config.getName());
      assertEquals(dsName, config.getDataSource());
      assertEquals(origin, config.getOrigin());
      assertEquals(database, config.getDatabase());

      // set values and get
      config.setName(name2);
      config.setDataSource(dsName2);
      config.setOrigin(origin2);
      config.setDatabase(database2);
      assertEquals(name2, config.getName());
      assertEquals(dsName2, config.getDataSource());
      assertEquals(origin2, config.getOrigin());
      assertEquals(database2, config.getDatabase());
      
      // test empty and null origin
      config.setOrigin("");
      assertEquals("", config.getOrigin());
      config.setOrigin(null);
      assertEquals("", config.getOrigin());

      config.setDatabase("");
      assertEquals("", config.getDatabase());
      config.setOrigin(null);
      assertEquals("", config.getDatabase());
      
      // test empty and null name and dsname
      boolean didThrow = false;
      try
      {
         config.setName(null);
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);      
      
      didThrow = false;
      try
      {
         config.setName("");
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);
      
      didThrow = false;
      try
      {
         config.setDataSource(null);
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);
      
      didThrow = false;
      try
      {
         config.setDataSource("");
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);
   }
   
   /**
    * Test equals
    * 
    * @throws Exception If there are any errors or failures.
    */
   public void testEquals() throws Exception
   {
      String name = "test";
      String dsName = "testds";
      String origin = "dbo";
      String database = "rxrhino";
      String name2 = "test2";
      String dsName2 = "testds2";
      String origin2 = "dbo2";
      String database2 = "rxrhino2";

      
      PSDatasourceConfig config1;
      PSDatasourceConfig config2;
      
      config1 = new PSDatasourceConfig(name, dsName, origin, database);
      config2 = new PSDatasourceConfig(name, dsName, origin, database);
      assertTrue(config1.equals(config2));
      assertTrue(config2.equals(config1));
      
      config2.setName(name2);
      assertTrue(!config1.equals(config2));
      assertTrue(!config2.equals(config1));
      
      config2.setName(name);
      config2.setDataSource(dsName2);
      assertTrue(!config1.equals(config2));
      assertTrue(!config2.equals(config1));

      config2.setDataSource(dsName);
      config2.setOrigin(origin2);
      assertTrue(!config1.equals(config2));
      assertTrue(!config2.equals(config1));

      config2.setOrigin(origin);
      assertTrue(config1.equals(config2));
      assertTrue(config2.equals(config1));
      
      config1.setOrigin(null);
      config2.setOrigin("");
      assertTrue(config1.equals(config2));
      assertTrue(config2.equals(config1));
      
      config2.setOrigin(origin);
      assertTrue(!config1.equals(config2));
      assertTrue(!config2.equals(config1));
      
      config1.setOrigin(origin);
      config2.setDatabase(database2);
      assertTrue(!config1.equals(config2));
      assertTrue(!config2.equals(config1));
      
      config1.setDatabase(null);
      config2.setDatabase("");
      assertTrue(config1.equals(config2));
      assertTrue(config2.equals(config1));
      
      config2.setDatabase(database);
      assertTrue(!config1.equals(config2));
      assertTrue(!config2.equals(config1));      
   }
   
   /**
    * Test copy and clone.
    * 
    * @throws Exception If there are any errors or failures.
    */
   public void testCopy() throws Exception
   {
      String name = "test";
      String dsName = "testds";
      String origin = "dbo";
      String database = "database";
      
      PSDatasourceConfig config1;
      PSDatasourceConfig config2;
      
      config1 = new PSDatasourceConfig(name, dsName, origin, database);
      config2 = new PSDatasourceConfig(config1);
      assertEquals(config1, config2);
      config2 = (PSDatasourceConfig) config1.clone();
      assertEquals(config1, config2);
      
      config1 = new PSDatasourceConfig(name, dsName, null, null);
      config2 = new PSDatasourceConfig(config1);
      assertEquals(config1, config2);
      config2 = (PSDatasourceConfig) config1.clone();
      assertEquals(config1, config2);
      
      config1 = new PSDatasourceConfig(name, dsName, "", "");
      config2 = new PSDatasourceConfig(config1);
      assertEquals(config1, config2);
      config2 = (PSDatasourceConfig) config1.clone();
      assertEquals(config1, config2);
   }
   
   /**
    * Test xml serialization
    * 
    * @throws Exception If there are any errors or failures.
    */
   public void testXml() throws Exception
   {
      String name = "test";
      String dsName = "testds";
      String origin = "dbo";
      String database = "database";      
      
      PSDatasourceConfig config1;
      PSDatasourceConfig config2;
      
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      config1 = new PSDatasourceConfig(name, dsName, origin, database);
      config2 = new PSDatasourceConfig(config1.toXml(doc));
      assertEquals(config1, config2);
      
      config1.setOrigin(null);
      config2 = new PSDatasourceConfig(config1.toXml(doc));
      assertEquals(config1, config2);
      
      config1.setDatabase(null);
      config2 = new PSDatasourceConfig(config1.toXml(doc));
      assertEquals(config1, config2);      
   }
}

