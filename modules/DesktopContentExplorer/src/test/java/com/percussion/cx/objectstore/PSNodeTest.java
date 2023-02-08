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
package com.percussion.cx.objectstore;

import com.percussion.util.PSEntrySet;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

/**
 * Test case for the {@link PSNode} class.
 */
public class PSNodeTest extends TestCase
{
   /**
    * Test the clone method to ensure child collections are properly supported.
    * 
    * @throws Exception if there are any errors 
    */
   public void testClone() throws Exception
   {
      PSNode node1 = new PSNode("test1", "test 1", PSNode.TYPE_FOLDER, "url", 
         "iconKey", true, 1);
      
      PSNode node2 = new PSNode("test1", "test 1", PSNode.TYPE_FOLDER, "url", 
         "iconKey", true, 1);

      PSNode child1 = new PSNode("child1", "child 1", PSNode.TYPE_FOLDER,
         "childurl", "iconKey", true, 1);
      PSNode child2 = new PSNode("child1", "child 1", PSNode.TYPE_FOLDER,
         "childurl", "iconKey", true, 1);
      
      assertEquals(node1, node2);
      
      PSNode clone = (PSNode)node1.clone();      
      assertEquals(node1, clone);
      // ensure node1 not changed
      assertEquals(node1, node2);
      
      node1.addChild(child1);
      node2.addChild(child2);
      
      List<PSEntrySet> columnDefs = new ArrayList<PSEntrySet>();
      columnDefs.add(new PSEntrySet("col1", PSNode.DATA_TYPE_TEXT));
      columnDefs.add(new PSEntrySet("col2", PSNode.DATA_TYPE_NUMBER));
      columnDefs.add(new PSEntrySet("col3", PSNode.DATA_TYPE_DATE));
      node1.setChildrenDisplayFormat(columnDefs.iterator());
      node2.setChildrenDisplayFormat(columnDefs.iterator());
      
      Map<String, String> rowData = new HashMap<String, String>();
      rowData.put("col1", "val1");
      rowData.put("col2", "301");
      rowData.put("col3", (new Date()).toString());
      node1.setRowData(rowData);
      node2.setRowData(new HashMap<String, String>(rowData));
      assertEquals(node1, node2);
      
      assertFalse(node1.equals(clone));
      clone = (PSNode)node1.clone();
      assertEquals(node1, clone);
      // ensure node1 not changed
      assertEquals(node1, node2);
   }
}

