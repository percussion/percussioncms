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
package com.percussion.tablefactory;

import com.percussion.xml.PSXmlDocumentBuilder;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

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
      catch (PSJdbcTableFactoryException e)
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
