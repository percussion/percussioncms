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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Unit test for PSJdbcPrimaryKey.
 */
public class PSJdbcPrimaryKeyTest extends TestCase
{
   public PSJdbcPrimaryKeyTest(String name)
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
         PSJdbcPrimaryKey pk = new PSJdbcPrimaryKey(cols.iterator(),
            PSJdbcTableComponent.ACTION_CREATE);
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
         PSJdbcPrimaryKey pk = new PSJdbcPrimaryKey(cols.iterator(),
            PSJdbcTableComponent.ACTION_CREATE);
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
         PSJdbcPrimaryKey pk = new PSJdbcPrimaryKey(cols.iterator(),
            PSJdbcTableComponent.ACTION_CREATE);
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
         PSJdbcPrimaryKey pk = new PSJdbcPrimaryKey(cols.iterator(),
            PSJdbcTableComponent.ACTION_CREATE);
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

      PSJdbcPrimaryKey pk = new PSJdbcPrimaryKey(cols.iterator(),
         PSJdbcTableComponent.ACTION_DELETE);

      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element el = pk.toXml(doc);

      PSJdbcPrimaryKey pk2 = new PSJdbcPrimaryKey(el);
      assertEquals(pk, pk2);

   }

   /**
    * Tests that the ctor assigns the fields
    */  
   public void testGetters() throws Exception
   {
      ArrayList cols = new ArrayList();
      cols.add("col1");
      cols.add("col2");
      cols.add("col3");

      PSJdbcPrimaryKey pk = new PSJdbcPrimaryKey("james", cols.iterator(),
         PSJdbcTableComponent.ACTION_DELETE);
      assertEquals( "james", pk.getName() );
      assertEquals( PSJdbcTableComponent.ACTION_DELETE, pk.getAction() );
      
      pk = new PSJdbcPrimaryKey(null, cols.iterator(), 
         PSJdbcTableComponent.ACTION_NONE);
      assertEquals( "", pk.getName() );
      assertEquals( PSJdbcTableComponent.ACTION_NONE, pk.getAction() );
      
   }
   
   // collect all tests into a TestSuite and return it
   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new PSJdbcPrimaryKeyTest("testDef"));
      suite.addTest(new PSJdbcPrimaryKeyTest("testGetters"));
       return suite;
   }

}
