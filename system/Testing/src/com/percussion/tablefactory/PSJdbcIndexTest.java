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

import static com.percussion.tablefactory.PSJdbcTableComponent.IS_EXACT_MATCH;

import com.percussion.xml.PSXmlDocumentBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import junit.framework.TestCase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Unit test for PSJdbcIndex.
 */
public class PSJdbcIndexTest extends TestCase
{
   /**
    * Test the def.
    */
   public void testDef() throws Exception
   {
      // build a def with a dupe name
      List<String> cols = new ArrayList<String>();
      cols.add("col1");
      cols.add("col2");
      cols.add("col1");

      boolean caught = false;
      try
      {
         createIndex(cols);
      }
      catch (PSJdbcTableFactoryException e)
      {
         caught = true;
      }
      assertTrue(caught);

      // build def with null name
      cols = new ArrayList<String>();
      cols.add("col1");
      cols.add(null);

      caught = false;
      try
      {
         createIndex(cols);
      }
      catch (PSJdbcTableFactoryException e)
      {
         caught = true;
      }
      assertTrue(caught);

      // build def with empty name
      cols = new ArrayList<String>();
      cols.add("col1");
      cols.add("");

      caught = false;
      try
      {
         createIndex(cols);
      }
      catch (PSJdbcTableFactoryException e)
      {
         caught = true;
      }
      assertTrue(caught);

      // build def with empty list
      cols = new ArrayList<String>();

      caught = false;
      try
      {
         createIndex(cols);
      }
      catch (IllegalArgumentException e)
      {
         caught = true;
      }
      assertTrue(caught);

      // build valid def
      cols = new ArrayList<String>();
      cols.add("col1");
      cols.add("col2");
      cols.add("col3");

      PSJdbcIndex index = new PSJdbcIndex("index1", cols.iterator(),
         PSJdbcTableComponent.ACTION_DELETE);

      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element el = index.toXml(doc);

      PSJdbcIndex index2 = new PSJdbcIndex(el);
      assertEquals(index, index2);
   }
   
   /**
    * Tests the lists comparison.
    */
   public void testCompare() throws PSJdbcTableFactoryException
   {
      final List<String> cols = new ArrayList<String>();
      cols.add("col1");
      cols.add("column2");

      final PSJdbcIndex index = createIndex(cols);
      assertEquals(IS_EXACT_MATCH, index.compare(index, 0));
      assertEquals(IS_EXACT_MATCH, index.compare(createIndex(cols), 0));
      
      final List<String> revCols = new ArrayList<String>(cols);
      Collections.reverse(revCols); 
      assertFalse(cols.equals(revCols));
      assertFalse(index.compare(createIndex(revCols), 0) == IS_EXACT_MATCH);
   }

   /**
    * Creates a new index object for the provided columns.
    * @param cols the index columns. Assumed not <code>null</code>.
    * @return the newly created index. Not <code>null</code>.
    * @throws PSJdbcTableFactoryException if index object creation fails.
    */
   private PSJdbcIndex createIndex(final List<String> cols)
         throws PSJdbcTableFactoryException
   {
      return new PSJdbcIndex("index1", cols.iterator(),
            PSJdbcTableComponent.ACTION_CREATE);
   }
}
