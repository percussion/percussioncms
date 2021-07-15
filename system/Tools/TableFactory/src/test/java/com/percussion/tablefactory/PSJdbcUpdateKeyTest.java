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

import com.percussion.xml.PSXmlDocumentBuilder;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for PSJdbcUpdateKey.
 */
public class PSJdbcUpdateKeyTest extends TestCase
{
   public PSJdbcUpdateKeyTest(String name)
   {
      super(name);
   }

   /**
    * Test the def
    */
   public void testDef() throws Exception
   {
      // build a def with a dupe name
      List cols = new ArrayList();
      cols.add("col1");
      cols.add("col2");
      cols.add("col1");

      boolean caught = false;
      try
      {
         PSJdbcUpdateKey uc = new PSJdbcUpdateKey(cols.iterator());
      }
      catch (PSJdbcTableFactoryException e)
      {
         caught = true;
      }
      assertTrue(caught);

      // build def with null name
      cols = new ArrayList();
      cols.add("col1");
      cols.add(null);

      caught = false;
      try
      {
         PSJdbcUpdateKey uc = new PSJdbcUpdateKey(cols.iterator());
      }
      catch (IllegalArgumentException e)
      {
         caught = true;
      }
      assertTrue(caught);

      // build def with empty name
      cols = new ArrayList();
      cols.add("col1");
      cols.add("");

      caught = false;
      try
      {
         PSJdbcUpdateKey uc = new PSJdbcUpdateKey(cols.iterator());
      }
      catch (PSJdbcTableFactoryException e)
      {
         caught = true;
      }
      assertTrue(caught);

      // build def with empty list
      cols = new ArrayList();

      caught = false;
      try
      {
         PSJdbcUpdateKey uc = new PSJdbcUpdateKey(cols.iterator());
      }
      catch (IllegalArgumentException e)
      {
         caught = true;
      }
      assertTrue(caught);

      // build valid def
      cols = new ArrayList();
      cols.add("col1");
      cols.add("col2");
      cols.add("col3");

      PSJdbcUpdateKey uc = new PSJdbcUpdateKey(cols.iterator());

      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element el = uc.toXml(doc);

      PSJdbcUpdateKey uc2 = new PSJdbcUpdateKey(el);
      assertEquals(uc, uc2);
   }

   // collect all tests into a TestSuite and return it
   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new PSJdbcUpdateKeyTest("testDef"));
       return suite;
   }

}
