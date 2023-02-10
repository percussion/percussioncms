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
package com.percussion.design.objectstore;

import com.percussion.utils.tools.PSTestUtils;
import com.percussion.xml.PSXmlDocumentBuilder;

import org.apache.commons.lang.ArrayUtils;
import org.w3c.dom.Document;

import junit.framework.TestCase;

/**
 * Test case for the {@link PSJdbcDriverConfig} class.
 */
public class PSJdbcDriverConfigTest extends TestCase
{
   /**
    * Test the parameterized ctor
    * 
    * @throws Exception if the test fails
    */
   public void testCtor() throws Exception
   {
      String[] args = (String[]) ArrayUtils.clone(ARGS);
      PSJdbcDriverConfig cfg = (PSJdbcDriverConfig) PSTestUtils.testCtor(
         PSJdbcDriverConfig.class, PARAMS, args, false);
      assertEquals(args[0], cfg.getDriverName());
      assertEquals(args[1], cfg.getClassName());
      assertEquals(args[2], cfg.getContainerTypeMapping());

      
      for (int i = 0; i < args.length; i++)
      {
         String val = args[i];
         args[i] = null;
         PSTestUtils.testCtor(PSJdbcDriverConfig.class, PARAMS, args, true);
         args[i] = "";
         PSTestUtils.testCtor(PSJdbcDriverConfig.class, PARAMS, args, true);
         args[i] = val;
      }      
   }
   
   /**
    * Tests XML serialization methods.
    * 
    * @throws Exception if the test fails.
    */
   public void testXml() throws Exception
   {
      String[] args = (String[]) ArrayUtils.clone(ARGS);
      PSJdbcDriverConfig cfg = (PSJdbcDriverConfig) PSTestUtils.testCtor(
         PSJdbcDriverConfig.class, PARAMS, args, false);
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      assertEquals(cfg, new PSJdbcDriverConfig(cfg.toXml(doc)));
   }
   
   /**
    * Tests equals and hashcode method
    * 
    * @throws Exception if the test fails
    */
   public void testEquals() throws Exception
   {
      String[] args = (String[]) ArrayUtils.clone(ARGS);
      PSJdbcDriverConfig cfg = (PSJdbcDriverConfig) PSTestUtils.testCtor(
         PSJdbcDriverConfig.class, PARAMS, args, false);
      PSJdbcDriverConfig cfg2 = (PSJdbcDriverConfig) PSTestUtils.testCtor(
         PSJdbcDriverConfig.class, PARAMS, args, false);
      assertEquals(cfg, cfg2);
      assertEquals(cfg.hashCode(), cfg2.hashCode());
      
      for (int i = 0; i < args.length; i++)
      {
         String val = args[i];
         args[i] = "foo";
         assertTrue(!cfg.equals(PSTestUtils.testCtor(PSJdbcDriverConfig.class, 
            PARAMS, args, false)));
         args[i] = val;
      }      
   }
   
   /**
    * Test set and get methods.
    * 
    * @throws Exception if the test fails
    */
   public void testSetters() throws Exception
   {
      String[] args = (String[]) ArrayUtils.clone(ARGS);
      PSJdbcDriverConfig cfg = (PSJdbcDriverConfig) PSTestUtils.testCtor(
         PSJdbcDriverConfig.class, PARAMS, args, false);
      
      for (int i = 0; i < PROPS.length; i++)
      {
         PSTestUtils.testSetter(cfg, PROPS[i], ARGS[i] + "Test", false);
         PSTestUtils.testSetter(cfg, PROPS[i], null, true);
         PSTestUtils.testSetter(cfg, PROPS[i], "", true);
      }
   }
   
   private static final Class[] PARAMS = {String.class, String.class, String.class};
   private static String DRIVER = "driver";
   private static String CLASS_NAME = "className";
   private static String CT_MAPPING = "ctMapping";
   private static final String[] ARGS = {DRIVER, CLASS_NAME, CT_MAPPING};
   private static final String[] PROPS = {"DriverName", "ClassName", 
      "ContainerTypeMapping"};
}

