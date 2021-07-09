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
      catch (IllegalArgumentException e)
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
