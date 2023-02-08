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
package com.percussion.webservices.transformation.converter;

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.ui.data.PSHierarchyNode;
import com.percussion.utils.testing.IntegrationTest;
import org.junit.experimental.categories.Category;

/**
 * Unit tests for the {@link PSHierarchyNodeConverter} class.
 */
@Category(IntegrationTest.class)
public class PSHierarchyNodeConverterTest extends PSConverterTestBase
{
   /**
    * Tests the conversion from a server to a client object. 
    */
   public void testConversion() throws Exception
   {
      // create source object
      PSHierarchyNode source = new PSHierarchyNode("name", new PSGuid(
         PSTypeEnum.HIERARCHY_NODE, 1001), PSHierarchyNode.NodeType.FOLDER);
      
      // convert
      PSHierarchyNode target = (PSHierarchyNode) roundTripConversion(
         PSHierarchyNode.class, 
         com.percussion.webservices.ui.data.PSHierarchyNode.class, source);
      
      assertEquals(source, target);

      // create source object
      source = new PSHierarchyNode("name", new PSGuid(
         PSTypeEnum.HIERARCHY_NODE, 1001), PSHierarchyNode.NodeType.FOLDER);
      source.addProperty("property_1", "value_1");
      source.addProperty("property_2", " ");
      source.addProperty("property_3", null);
      
      // convert
      target = (PSHierarchyNode) roundTripConversion(
         PSHierarchyNode.class, 
         com.percussion.webservices.ui.data.PSHierarchyNode.class, source);
      
      assertEquals(source, target);
      
      // create the source array
      PSHierarchyNode[] sourceArray = new PSHierarchyNode[1];
      sourceArray[0] = source;
      
      PSHierarchyNode[] targetArray = (PSHierarchyNode[]) roundTripConversion(
         PSHierarchyNode[].class, 
         com.percussion.webservices.ui.data.PSHierarchyNode[].class, 
         sourceArray);
      
      // verify the the round-trip array is equal to the source array
      assertTrue(sourceArray.length == targetArray.length);
      assertTrue(sourceArray[0].equals(targetArray[0]));
   }
}
