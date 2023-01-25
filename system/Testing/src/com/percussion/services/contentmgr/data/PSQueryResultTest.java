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
package com.percussion.services.contentmgr.data;

import com.percussion.services.contentmgr.IPSContentPropertyConstants;
import com.percussion.utils.types.PSPair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.ItemNotFoundException;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

import junit.framework.TestCase;

/**
 * Test most of query result, but not the node iterator (at least directly)
 * since that requires a servlet test case. That will be tested as part of 
 * the general query test.
 * 
 * @author dougrand
 */
public class PSQueryResultTest extends TestCase
{   
   private static final String[] columns =
   { "rx:able", "rx:baker", "rx:charlie"};
   
   private static int id = 0;

   private List<PSPair<String,Boolean>> buildSort()
   {
      List<PSPair<String,Boolean>> sort = new ArrayList<PSPair<String,Boolean>>();
      sort.add(new PSPair<String,Boolean>("rx:able", true));
      sort.add(new PSPair<String,Boolean>("rx:baker", false));
      return sort;
   }
   
   private PSRow buildRow(String able, String baker, String charlie)
   {
      Map<String,Object> data = new HashMap<String,Object>();
      
      data.put("rx:able",able);
      data.put("rx:baker",baker);
      data.put("rx:charlie",charlie);
      data.put(IPSContentPropertyConstants.RX_SYS_CONTENTID,Integer.toString(id++));
      data.put(IPSContentPropertyConstants.RX_SYS_REVISION,"1");
      
      return new PSRow(data);
   }
   
   private PSQueryResult buildResult()
   {
      List<PSPair<String,Boolean>> sort = buildSort();
      PSQueryResult r = new PSQueryResult(columns, new PSRowComparator(sort));
      
      r.addRow(buildRow("a0", "b1", "c1"));
      r.addRow(buildRow("a0", "b0", "c2"));
      r.addRow(buildRow("a2", "b2", "c5"));
      r.addRow(buildRow("a1", "b0", "c4"));
      r.addRow(buildRow("a3", "b3", "c6"));
      r.addRow(buildRow("a1", "b1", "c3"));
      
      return r;
   }
   
   /**
    * Test that we are getting an appropriate result back, includes checking
    * for count, and correct behavior when incorrect properties are requested
    * 
    * @throws Exception
    */
   public void testResult() throws Exception
   {
      PSQueryResult r = buildResult();
      RowIterator riter = r.getRows();
      
      assertEquals(riter.getSize(), 6);
      assertEquals(riter.getPosition(), 0);
      riter.skip(2);
      assertEquals(riter.getPosition(), 2);
      
      riter = r.getRows();
      Row row = riter.nextRow();
      assertEquals(row.getValues().length, 3);
      
      try
      {
         row.getValue("rx:foo");
         throw new Exception("Asking for missing property didn't throw");
      }
      catch(ItemNotFoundException re)
      {
         // Correct
      }
      
      try
      {
         row.getValue("rx:sys_contentid");
         throw new Exception("Asking for hidden property didn't throw");
      }
      catch(ItemNotFoundException re)
      {
         // Correct
      }      
   }
   
   /**
    * As the comment says, test the sort ordering built into the query 
    * result implementation
    * 
    * @throws RepositoryException
    */
   public void testSortOrder() throws RepositoryException
   {
      PSQueryResult r = buildResult();
      RowIterator riter = r.getRows();
      
      Row r1 = riter.nextRow();
      Row r2 = riter.nextRow();
      Row r3 = riter.nextRow();
      Row r4 = riter.nextRow();
      Row r5 = riter.nextRow();
      Row r6 = riter.nextRow();
      
      /*
       * Sorted
       * "a0", "b1", "c1"
       * "a0", "b0", "c2"
       * "a1", "b1", "c3"
       * "a1", "b0", "c4"
       * "a2", "b2", "c5"
       * "a3", "b3", "c6"
       */
      // Check able
      assertEquals(r1.getValue("rx:able").getString(), "a0");
      assertEquals(r2.getValue("rx:able").getString(), "a0");
      assertEquals(r3.getValue("rx:able").getString(), "a1");
      assertEquals(r4.getValue("rx:able").getString(), "a1");
      assertEquals(r5.getValue("rx:able").getString(), "a2");
      assertEquals(r6.getValue("rx:able").getString(), "a3");
      
      // Check baker
      assertEquals(r1.getValue("rx:baker").getString(), "b1");
      assertEquals(r2.getValue("rx:baker").getString(), "b0");
      assertEquals(r3.getValue("rx:baker").getString(), "b1");
      assertEquals(r4.getValue("rx:baker").getString(), "b0");
      assertEquals(r5.getValue("rx:baker").getString(), "b2");
      assertEquals(r6.getValue("rx:baker").getString(), "b3");
      
      // Check charlie
      assertEquals(r1.getValue("rx:charlie").getString(), "c1");
      assertEquals(r2.getValue("rx:charlie").getString(), "c2");
      assertEquals(r3.getValue("rx:charlie").getString(), "c3");
      assertEquals(r4.getValue("rx:charlie").getString(), "c4");
      assertEquals(r5.getValue("rx:charlie").getString(), "c5");
      assertEquals(r6.getValue("rx:charlie").getString(), "c6");      
   }
   
   /**
    * Make sure that something is coming back that seems correct for the nodes.
    * A better test would check the nodes themselves.
    * 
    * @throws Exception
    */
   public void testNodesWithoutTraversing() throws Exception
   {
      PSQueryResult r = buildResult();
      
      NodeIterator n = r.getNodes();
      
      assertEquals(n.getSize(), 6);
      assertEquals(n.getPosition(), 0);
   }
   
   /**
    * Make sure we get the expected number of columns back.
    * 
    * @throws Exception
    */
   public void testGetColumns() throws Exception
   {
      PSQueryResult r = buildResult();
      String c[] = r.getColumnNames();
      //FB: EC_BAD_ARRAY_COMPARE NC 1-17-16
      assert(Arrays.equals(columns, c));
   }
}
