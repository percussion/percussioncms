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
