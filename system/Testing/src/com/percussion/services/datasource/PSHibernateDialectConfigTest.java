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

import com.percussion.utils.tools.PSTestUtils;
import com.percussion.xml.PSXmlDocumentBuilder;
import junit.framework.TestCase;
import org.w3c.dom.Document;

import java.util.HashMap;
import java.util.Map;

/**
 * Test case for the {@link PSHibernateDialectConfig} class.
 */
public class PSHibernateDialectConfigTest extends TestCase
{
   /**
    * Test accessor methods
    * 
    * @throws Exception if the test fails.
    */
   public void testAccessors() throws Exception
   {
      PSHibernateDialectConfig cfg = new PSHibernateDialectConfig();
      
      Map<String, String> map = new HashMap<String, String>();
      map.put("test1", "testClass");
      map.put("test2", "testClass2");
      map.put("test3", "testClass3");
      
      PSTestUtils.testSetter(cfg, "Dialects", null, Map.class, true);
      PSTestUtils.testSetter(cfg, "Dialects", map, Map.class, false);
      
      for (Map.Entry<String, String> entry : map.entrySet())
      {
         assertEquals(entry.getValue(), cfg.getDialectClassName(
            entry.getKey()));
         cfg.setDialect(entry.getKey(), entry.getValue() + "test");
         assertEquals(entry.getValue() + "test", cfg.getDialectClassName(
            entry.getKey()));
      }

      Map<String, String> badmap = new HashMap<String, String>(map);
      badmap.put("test1", null);
      PSTestUtils.testSetter(cfg, "Dialects", null, Map.class, true);
      badmap.putAll(map);
      PSTestUtils.testSetter(cfg, "Dialects", null, Map.class, true);
      
      badmap.put("test2", "");
      PSTestUtils.testSetter(cfg, "Dialects", null, Map.class, true);
      badmap.putAll(map);
      
      badmap.put("", "foo");
      PSTestUtils.testSetter(cfg, "Dialects", null, Map.class, true);
      badmap.putAll(map);
   }
   
   /**
    * Test xml serialization
    * 
    * @throws Exception
    */
   public void testXml() throws Exception
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      PSHibernateDialectConfig cfg = new PSHibernateDialectConfig();
      PSHibernateDialectConfig cfg2 = new PSHibernateDialectConfig();
      cfg2.fromXml(cfg.toXml(doc));
      assertEquals(cfg.getDialects(), cfg2.getDialects());
      
      Map<String, String> map = new HashMap<String, String>();
      map.put("test1", "testClass");
      cfg.setDialects(map);
      cfg2 = new PSHibernateDialectConfig();
      cfg2.fromXml(cfg.toXml(doc));
      assertEquals(cfg.getDialects(), cfg2.getDialects());      
      
      map.put("test2", "testClass2");
      map.put("test3", "testClass3");
      cfg.setDialects(map);
      cfg2 = new PSHibernateDialectConfig();
      cfg2.fromXml(cfg.toXml(doc));
      assertEquals(cfg.getDialects(), cfg2.getDialects());
   }
}


