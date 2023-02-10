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

import com.percussion.services.content.data.PSItemSummary;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.utils.testing.IntegrationTest;
import org.junit.experimental.categories.Category;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Unit tests for the {@link PSItemSummaryConverter} class.
 */
@Category(IntegrationTest.class)
public class PSItemSummaryConverterTest extends PSConverterTestBase
{
   /**
    * Tests the conversion from a server to a client object as well as a
    * server array of objects to a client array of objects and back.
    * 
    * @throws Exception if an error occurs.
    */
   public void testConversion() throws Exception
   {
      // create the source object
      PSItemSummary source = createItemSummary();
      
      PSItemSummary target = (PSItemSummary) roundTripConversion(
         PSItemSummary.class, 
         com.percussion.webservices.content.PSItemSummary.class, 
         source);
      
      // verify the the round-trip object is equal to the source object
      assertTrue(source.equals(target));
      
      // create the source array
      PSItemSummary[] sourceArray = new PSItemSummary[1];
      sourceArray[0] = source;
      
      PSItemSummary[] targetArray = (PSItemSummary[]) roundTripConversion(
         PSItemSummary[].class, 
         com.percussion.webservices.content.PSItemSummary[].class, 
         sourceArray);
      
      // verify the the round-trip array is equal to the source array
      assertTrue(sourceArray.length == targetArray.length);
      assertTrue(sourceArray[0].equals(targetArray[0]));
   }
   
   /**
    * Test a list of server object conversion to client array, and vice versa.
    * 
    * @throws Exception if an error occurs.
    */
   @SuppressWarnings("unchecked")
   public void testListToArray() throws Exception
   {
      List<PSItemSummary> sourceList = new ArrayList<PSItemSummary>();
      sourceList.add(createItemSummary());
      sourceList.add(createItemSummary());
      
      List<PSItemSummary> targetList = roundTripListConversion(
         com.percussion.webservices.content.PSItemSummary[].class, 
         sourceList);

      assertTrue(sourceList.equals(targetList));
   }

   /**
    * Create an item summary for testing.
    * 
    * @return the new item summary, never <code>null</code>.
    */
   public static PSItemSummary createItemSummary() 
   {
      PSItemSummary summary = new PSItemSummary();
      summary.setGUID(new PSLegacyGuid(1001, 2));
      summary.setName("title");
      summary.setContentTypeId(101);
      summary.setContentTypeName("contentType");
      summary.setObjectType(PSItemSummary.ObjectTypeEnum.ITEM);
      
      Collection<PSItemSummary.OperationEnum> operations = 
         new ArrayList<PSItemSummary.OperationEnum>();
      operations.add(PSItemSummary.OperationEnum.READ);
      operations.add(PSItemSummary.OperationEnum.WRITE);
      operations.add(PSItemSummary.OperationEnum.TRANSITION);
      summary.setOperations(operations);
      
      return summary;
   }
}

