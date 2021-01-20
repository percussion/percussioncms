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
