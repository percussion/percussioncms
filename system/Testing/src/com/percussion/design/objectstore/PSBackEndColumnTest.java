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
package com.percussion.design.objectstore;

import com.percussion.xml.PSXmlDocumentBuilder;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Unit tests for the PSBackEndColumn class.
 */
public class PSBackEndColumnTest extends TestCase
{
   public PSBackEndColumnTest(String name)
   {
      super(name);
   }

   public void testConstructors() throws Exception
   {
      // create a valid table and column, create another column that should be
      // equal
      {
         PSBackEndTable tab = new PSBackEndTable("foo");
         PSBackEndColumn col = new PSBackEndColumn(tab, "bar");
         PSBackEndColumn otherCol = new PSBackEndColumn(tab, "bar");
         assertEquals(col, otherCol);
      }

      // try creating a column with a null table, make sure it throws
      {
         boolean didThrow = false;
         try
         {
            PSBackEndColumn col = new PSBackEndColumn(null, "foo");
         }
         catch (IllegalArgumentException e)
         {
            didThrow = true;
         }
         assertTrue("Caught cons with null table?", didThrow);
      }

      // try creating a column with a null col name, make sure it throws
      {
         boolean didThrow = false;
         try
         {
            PSBackEndTable tab = new PSBackEndTable("foo");
            PSBackEndColumn col = new PSBackEndColumn(tab, null);
         }
         catch (IllegalArgumentException e)
         {
            didThrow = true;
         }
         assertTrue("Caught cons with null col name?", didThrow);
      }

      // try creating a column with an empty col name, make sure it throws
      {
         boolean didThrow = false;
         try
         {
            PSBackEndTable tab = new PSBackEndTable("foo");
            PSBackEndColumn col = new PSBackEndColumn(tab, "");
         }
         catch (IllegalArgumentException e)
         {
            didThrow = true;
         }
         assertTrue("Caught cons with empty col name?", didThrow);
      }
   }

   public void testGetSetColumn() throws Exception
   {
      String colName = "zoo";
      PSBackEndTable tab = new PSBackEndTable("foo");
      PSBackEndColumn col = new PSBackEndColumn(tab, colName);
      assertEquals(colName, col.getColumn());
      col.setColumn("bar");
      assertEquals("bar", col.getColumn());

      boolean didThrow = false;
      try
      {
         col.setColumn("");
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue("Caught setColumn with empty col name?", didThrow);
      assertEquals("Col name unchanged after illegal setColumn?", "bar", col.getColumn());

      didThrow = false;
      try
      {
         col.setColumn(null);
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue("Caught setColumn with null col name?", didThrow);
      assertEquals("Col name unchanged after illegal setColumn?", "bar", col.getColumn());
   }

   public void testGetSetTable() throws Exception
   {
      PSBackEndTable tab = new PSBackEndTable("baz");
      PSBackEndColumn col = new PSBackEndColumn(tab, "foo");
      assertEquals(tab, col.getTable());
      PSBackEndTable otherTab = new PSBackEndTable("box");
      col.setTable(otherTab);
      assertEquals(otherTab, col.getTable());
   }

   /**
    * Tests the XML serialization by using <code>fromXml()</code> to create an
    * instance from the output of another instance's <code>toXml()</code>.
    * These instances should be equal.
    *
    * @throws Exception if the test failed.
    */
   public void testXml() throws Exception
   {
      PSBackEndTable tab = new PSBackEndTable( "baz" );
      PSBackEndColumn col = new PSBackEndColumn( tab, "foo" );
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element el = col.toXml( doc );
      PSBackEndTable tab2 = new PSBackEndTable( "zap" );
      PSBackEndColumn col2 = new PSBackEndColumn( tab2, "hohos" );
      assertTrue( !col2.equals( col ) );
      col2.fromXml( el, null, null );
      assertEquals( col, col2 );
      assertEquals( col, new PSBackEndColumn( el, null, null ) );
   }

   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new PSBackEndColumnTest("testConstructors"));
      suite.addTest(new PSBackEndColumnTest("testGetSetColumn"));
      suite.addTest(new PSBackEndColumnTest("testGetSetTable"));
      suite.addTest(new PSBackEndColumnTest("testXml"));
      return suite;
   }
}
