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

import com.percussion.xml.PSXmlDocumentBuilder;

import org.w3c.dom.Document;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class PSBackEndTableTest extends TestCase
{
   public PSBackEndTableTest(String name)
   {
      super(name);
   }

   public void testEquals() throws Exception
   {
      PSBackEndTable tab = new PSBackEndTable();
      PSBackEndTable otherTab = new PSBackEndTable();
      assertEquals(tab, otherTab);

      tab = new PSBackEndTable("foobar");
      assertEquals(tab.getAlias(), "foobar");
      assertTrue(!tab.equals(otherTab));

      otherTab.setAlias("foobarbaz");
      assertTrue(!tab.equals(otherTab));
      otherTab.setAlias("foobar");
      assertEquals(tab, otherTab);

      tab.setDataSource("foods");
      assertEquals("foods", tab.getDataSource());
      assertTrue(!tab.equals(otherTab));
      otherTab.setDataSource("foods");
      assertEquals(tab, otherTab);
      assertEquals("foods", otherTab.getDataSource());


      tab.setTable("footable");
      assertEquals(tab.getTable(), "footable");
      assertTrue(!tab.equals(otherTab));
      otherTab.setTable("footable");
      assertEquals(tab, otherTab);
      assertEquals("footable", otherTab.getTable());

   }

   public void testCopyFrom() throws Exception
   {
      PSBackEndTable tab = new PSBackEndTable();
      PSBackEndTable otherTab = new PSBackEndTable();
      assertEquals(tab, otherTab);

      tab = new PSBackEndTable("foobar");
      assertEquals(tab.getAlias(), "foobar");
      assertTrue(!tab.equals(otherTab));

      otherTab.copyFrom(tab);
      assertEquals(tab, otherTab);

      tab.setDataSource("foods");
      assertEquals("foods", tab.getDataSource());
      assertTrue(!tab.equals(otherTab));
      otherTab.copyFrom(tab);
      assertEquals(tab, otherTab);

      tab.setTable("footable");
      assertEquals(tab.getTable(), "footable");
      assertTrue(!tab.equals(otherTab));
      otherTab.copyFrom(tab);
      assertEquals(tab, otherTab);
   }

   public void testXml() throws Exception
   {
      PSBackEndTable tab = new PSBackEndTable();
      PSBackEndTable otherTab = new PSBackEndTable();
      assertEquals(tab, otherTab);

      tab = new PSBackEndTable("foobar");
      assertEquals(tab.getAlias(), "foobar");
      assertTrue(!tab.equals(otherTab));

      tab.setDataSource("abc");
      tab.setTable("mno");

      Document doc = PSXmlDocumentBuilder.createXmlDocument();

      otherTab.fromXml(tab.toXml(doc), null, null);
      assertEquals(tab, otherTab);

      doc = PSXmlDocumentBuilder.createXmlDocument();
      tab.setDataSource("foods");
      assertEquals("foods", tab.getDataSource());
      assertTrue(!tab.equals(otherTab));
      otherTab.fromXml(tab.toXml(doc), null, null);
      assertEquals(tab, otherTab);

      doc = PSXmlDocumentBuilder.createXmlDocument();
      tab.setTable("footable");
      assertEquals(tab.getTable(), "footable");
      assertTrue(!tab.equals(otherTab));
      otherTab.fromXml(tab.toXml(doc), null, null);
      assertEquals(tab, otherTab);
   }

   // collect all tests into a TestSuite and return it
   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new PSBackEndTableTest("testEquals"));
      suite.addTest(new PSBackEndTableTest("testCopyFrom"));
      suite.addTest(new PSBackEndTableTest("testXml"));
      return suite;
   }
}
