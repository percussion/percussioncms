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

import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.search.objectstore.PSWSSearchField;
import com.percussion.search.objectstore.PSWSSearchParams;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.webservices.transformation.converter.PSSearchParamsConverter;
import org.junit.experimental.categories.Category;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Unit tests for the {@link PSSearchParamsConverter} class.
 */
@Category(IntegrationTest.class)
public class PSSearchParamsConverterTest extends PSConverterTestBase
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
      PSWSSearchParams source = createSearchParams(3);
      
      PSWSSearchParams target = (PSWSSearchParams) roundTripConversion(
         PSWSSearchParams.class, 
         com.percussion.webservices.content.PSSearchParams.class, 
         source);
      
      // verify the the round-trip object is equal to the source object
      assertTrue(source.equals(target));
      
      // create the source array
      PSWSSearchParams[] sourceArray = new PSWSSearchParams[1];
      sourceArray[0] = source;
      
      PSWSSearchParams[] targetArray = (PSWSSearchParams[]) roundTripConversion(
         PSWSSearchParams[].class, 
         com.percussion.webservices.content.PSSearchParams[].class, 
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
      List<PSWSSearchParams> sourceList = new ArrayList<PSWSSearchParams>();
      sourceList.add(createSearchParams(2));
      sourceList.add(createSearchParams(4));
      
      List<PSWSSearchParams> targetList = roundTripListConversion(
            com.percussion.webservices.content.PSSearchParams[].class, 
            sourceList);

      assertTrue(sourceList.equals(targetList));
   }

   /**
    * Create the search parameters for testing.
    * 
    * @param count the number of properties, result and search fields to 
    *    create, must be >= 0.
    * @return the new search parameters, never <code>null</code>.
    * @throws Exception for any error.
    */
   public static PSWSSearchParams createSearchParams(int count) 
      throws Exception
   {
      if (count < 0)
         throw new IllegalArgumentException("count must be >= 0");
      
      // register a test content type definition
      PSItemDefManager mgr = PSItemConverterTest.getTestItemDefManager();
      PSItemDefinition def = mgr.getItemDef(316, -1);
      
      PSWSSearchParams searchParams = new PSWSSearchParams();
      searchParams.setContentTypeId(def.getContentEditor().getContentType());
      searchParams.setTitle("title", 
         PSWSSearchField.PSOperatorEnum.LESSTHAN.getOrdinal(), 
         PSWSSearchField.PSConnectorEnum.AND.getOrdinal());
      searchParams.setSearchForFolders(true);
      searchParams.setFolderPathFilter("folderPathFilter", false);
      searchParams.setFTSQuery("fullTextQuery");
      Map<String, String> properties = new HashMap<String, String>();
      Collection<String> resultFields = new ArrayList<String>();
      List<PSWSSearchField> searchFields = new ArrayList<PSWSSearchField>();
      for (int i=0; i<count; i++)
      {
         properties.put("propertyName_" + i, "value_" + i);
         resultFields.add("resultField_" + i);
         
         PSWSSearchField searchField = new PSWSSearchField("searchField_" + i, 
            PSWSSearchField.PSOperatorEnum.EQUAL.getOrdinal(), "value_" + i, 
            PSWSSearchField.PSConnectorEnum.AND.getOrdinal());
         searchFields.add(searchField);
      }
      searchParams.setProperties(properties);
      searchParams.setResultFields(resultFields);
      searchParams.setSearchFields(searchFields);
      
      
      return searchParams;
   }
}
