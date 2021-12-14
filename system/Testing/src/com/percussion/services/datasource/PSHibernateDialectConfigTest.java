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


