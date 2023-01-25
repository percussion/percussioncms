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
import com.percussion.services.content.data.PSSearchSummary;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.webservices.transformation.converter.PSSearchSummaryConverter;
import org.junit.experimental.categories.Category;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Unit tests for the {@link PSSearchSummaryConverter} class.
 */
@Category(IntegrationTest.class)
public class PSSearchSummaryConverterTest extends PSConverterTestBase
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
      PSSearchSummary source = createSearchSummary();
      
      PSSearchSummary target = (PSSearchSummary) roundTripConversion(
         PSSearchSummary.class, 
         com.percussion.webservices.content.PSSearchResults.class, 
         source);
      
      // verify the the round-trip object is equal to the source object
      assertTrue(source.equals(target));
      
      // create the source array
      PSSearchSummary[] sourceArray = new PSSearchSummary[1];
      sourceArray[0] = source;
      
      PSSearchSummary[] targetArray = (PSSearchSummary[]) roundTripConversion(
         PSSearchSummary[].class, 
         com.percussion.webservices.content.PSSearchResults[].class, 
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
      List<PSSearchSummary> sourceList = new ArrayList<PSSearchSummary>();
      sourceList.add(createSearchSummary());
      sourceList.add(createSearchSummary());
      
      List<PSSearchSummary> targetList = roundTripListConversion(
         com.percussion.webservices.content.PSSearchResults[].class, 
         sourceList);

      assertTrue(sourceList.equals(targetList));
   }

   /**
    * Create an search summary for testing.
    * 
    * @return the new search summary, never <code>null</code>.
    */
   public static PSSearchSummary createSearchSummary() 
   {
      PSSearchSummary summary = new PSSearchSummary();
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
      
      Map<String, String> fields = new HashMap<String, String>();
      fields.put("name_1", "value_1");
      fields.put("name_2", "value_2");
      fields.put("name_3", "value_3");
      summary.setFields(fields);
      
      return summary;
   }
}

