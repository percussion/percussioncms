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
