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
package com.percussion.webservices.transformation.converter;

import com.percussion.search.objectstore.PSWSSearchField;

import java.util.ArrayList;
import java.util.List;

import com.percussion.utils.testing.IntegrationTest;
import com.percussion.webservices.transformation.converter.PSSearchFieldConverter;
import org.apache.commons.lang.StringUtils;
import org.junit.experimental.categories.Category;

/**
 * Unit tests for the {@link PSSearchFieldConverter} class.
 */
@Category(IntegrationTest.class)
public class PSSearchFieldConverterTest extends PSConverterTestBase
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
      PSWSSearchField source = createSearchField("field_1", "value_1", null);
      
      PSWSSearchField target = (PSWSSearchField) roundTripConversion(
         PSWSSearchField.class, 
         com.percussion.webservices.content.PSSearchField.class, 
         source);
      
      // verify the the round-trip object is equal to the source object
      assertTrue(source.equals(target));
      
      // create the source array
      PSWSSearchField[] sourceArray = new PSWSSearchField[1];
      sourceArray[0] = source;
      
      PSWSSearchField[] targetArray = (PSWSSearchField[]) roundTripConversion(
         PSWSSearchField[].class, 
         com.percussion.webservices.content.PSSearchField[].class, 
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
      List<PSWSSearchField> sourceList = new ArrayList<PSWSSearchField>();
      sourceList.add(createSearchField("field_1", "value_1", null));
      sourceList.add(createSearchField("field_1", "value_1", "externalOp"));
      
      List<PSWSSearchField> targetList = roundTripListConversion(
            com.percussion.webservices.content.PSSearchField[].class, 
            sourceList);

      assertTrue(sourceList.equals(targetList));
   }

   /**
    * Create a search field for testing.
    * 
    * @param name the field name, assumed not <code>null</code> or empty.
    * @param value the field value, assumed not <code>null</code> or empty.
    * @param externalOperator the external operator, may be <code>null</code>
    *    or empty to create a search field with an internal operator.
    * @return the new search field, never <code>null</code>.
    */
   private PSWSSearchField createSearchField(String name, String value, 
      String externalOperator)
   {
      PSWSSearchField searchField = null;
      
      if (StringUtils.isBlank(externalOperator))
         searchField = new PSWSSearchField(name, 
            PSWSSearchField.PSOperatorEnum.ISNOTNULL.getOrdinal(), value, 
            PSWSSearchField.PSConnectorEnum.OR.getOrdinal());
      else
         searchField = new PSWSSearchField(name, externalOperator, value, 
            PSWSSearchField.PSConnectorEnum.OR.getOrdinal());
      
      return searchField;
   }
}
