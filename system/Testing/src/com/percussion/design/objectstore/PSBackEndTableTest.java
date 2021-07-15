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
