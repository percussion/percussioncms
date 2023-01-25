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

import com.percussion.search.objectstore.PSWSSearchParams;
import com.percussion.search.objectstore.PSWSSearchRequest;
import com.percussion.utils.testing.IntegrationTest;
import org.junit.experimental.categories.Category;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for the {@link PSSearchConverter} class.
 */
@Category(IntegrationTest.class)
public class PSSearchConverterTest extends PSConverterTestBase
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
      PSWSSearchRequest source = createSearch(3);
      
      PSWSSearchRequest target = (PSWSSearchRequest) roundTripConversion(
         PSWSSearchRequest.class, 
         com.percussion.webservices.content.PSSearch.class, 
         source);
      
      // verify the the round-trip object is equal to the source object
      assertTrue(source.equals(target));
      
      // create the source array
      PSWSSearchRequest[] sourceArray = new PSWSSearchRequest[1];
      sourceArray[0] = source;
      
      PSWSSearchRequest[] targetArray = (PSWSSearchRequest[]) roundTripConversion(
         PSWSSearchRequest[].class, 
         com.percussion.webservices.content.PSSearch[].class, 
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
      List<PSWSSearchRequest> sourceList = new ArrayList<PSWSSearchRequest>();
      sourceList.add(createSearch(2));
      sourceList.add(createSearch(4));
      
      List<PSWSSearchRequest> targetList = roundTripListConversion(
         com.percussion.webservices.content.PSSearch[].class, sourceList);

      assertTrue(sourceList.equals(targetList));
   }

   /**
    * Create the search request for testing.
    * 
    * @param count the number of search parameters to create, assumed > 0.
    * @return the new search parameters, never <code>null</code>.
    * @throws Exception for any error.
    */
   private PSWSSearchRequest createSearch(int count) throws Exception
   {
      PSWSSearchParams searchParams = 
         PSSearchParamsConverterTest.createSearchParams(count);
      
      PSWSSearchRequest search = new PSWSSearchRequest(searchParams);
      search.setCaseInsensitiveSearch(false);
      search.setUseExternalSearchEngine(false);
      
      return search;
   }
}
