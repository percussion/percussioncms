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

import com.percussion.services.filter.IPSFilterService;
import com.percussion.services.filter.IPSItemFilter;
import com.percussion.services.filter.IPSItemFilterRuleDef;
import com.percussion.services.filter.PSFilterServiceLocator;
import com.percussion.services.filter.data.PSItemFilter;
import com.percussion.services.filter.data.PSItemFilterRuleDef;
import com.percussion.utils.testing.IntegrationTest;
import org.junit.experimental.categories.Category;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Unit tests for the {@link PSItemFilterConverter} class.
 */
@Category(IntegrationTest.class)
public class PSItemFilterConverterTest extends PSConverterTestBase
{
   /**
    * Tests the conversion from a server to a client object as well as a
    * server array of objects to a client array of objects and back.
    * 
    * @throws Exception if an error occurs.
    */
   public void testConversion() throws Exception
   {
      IPSItemFilter parent = null;
      IPSItemFilter source = null;

      try
      {
         // create the source object
         parent = createItemFilter("parent", null, Integer.valueOf(1), 2, 3);
         source = createItemFilter("child", parent, null, 2, 3);
         
         PSItemFilter target = (PSItemFilter) roundTripConversion(
            PSItemFilter.class, 
            com.percussion.webservices.system.PSItemFilter.class, 
            source);
         
         // verify the the round-trip object is equal to the source object
         assertTrue(source.equals(target));
         
         // create the source array
         IPSItemFilter[] sourceArray = new PSItemFilter[1];
         sourceArray[0] = source;
         
         PSItemFilter[] targetArray = (PSItemFilter[]) roundTripConversion(
            PSItemFilter[].class, 
            com.percussion.webservices.system.PSItemFilter[].class, 
            sourceArray);
         
         // verify the the round-trip array is equal to the source array
         assertTrue(sourceArray.length == targetArray.length);
         assertTrue(sourceArray[0].equals(targetArray[0]));
      }
      finally
      {
         removeItemFilter(source);
         removeItemFilter(parent);
      }
   }
   
   /**
    * Test a list of server object conversion to client array, and vice versa.
    * 
    * @throws Exception if an error occurs.
    */
   @SuppressWarnings("unchecked")
   public void testListToArray() throws Exception
   {
      IPSItemFilter parent = null;
      IPSItemFilter source = null;

      try
      {
         parent = createItemFilter("parent", null, Integer.valueOf(1), 2, 3);
         source = createItemFilter("child", parent, null, 2, 3);
   
         List<IPSItemFilter> sourceList = new ArrayList<IPSItemFilter>();
         sourceList.add(parent);
         sourceList.add(source);
         
         List<PSItemFilter> targetList = roundTripListConversion(
            com.percussion.webservices.system.PSItemFilter[].class, 
            sourceList);
   
         assertTrue(sourceList.equals(targetList));
      }
      finally
      {
         removeItemFilter(source);
         removeItemFilter(parent);
      }
   }

   /**
    * Create a filter for testing and save it to the repository.
    * 
    * @param name the filter name, assumed not <code>null</code> or empty.
    * @param parent the parent filter, may be <code>null</code>.
    * @param authtype the authtype for legacy filters, may be <code>null</code>.
    * @param ruleCount the number of rules to create, assumed >= 0;
    * @param paramCount the number of rule parameters to create, assumed >= 0.
    * @return the new filter, never <code>null</code>.
    */
   private IPSItemFilter createItemFilter(String name, IPSItemFilter parent, 
      Integer authtype, int ruleCount, int paramCount) throws Exception
   {
      IPSFilterService service = PSFilterServiceLocator.getFilterService();
      IPSItemFilter filter = service.createFilter(name, "desctiption");
      filter.setParentFilter(parent);
      filter.setLegacyAuthtypeId(authtype);

      for (int i=0; i<ruleCount; i++)
      {
         Map<String, String> params = new HashMap<String, String>();
         for (int j=0; j<paramCount; j++)
            params.put(name + "_param_" + i + "." + j, 
               "value_" + i + "." + j);
         
         filter.addRuleDef(createFilterRule(
            PSItemFilterRuleDef.TEST_RULE_NAME, params));
      }
      
      service.saveFilter(filter);

      return filter;
   }
   
   /**
    * Create a new rule definition for the supplied parameters.
    * 
    * @param rule the rule string, assumed not <code>null</code>, may be empty.
    * @param params the rule parameters, assumed not <code>null</code>, may 
    *    be empty.
    * @return the new rule definition created, never <code>null</code>.
    * @throws Exception for any error creating the new rule.
    */
   private IPSItemFilterRuleDef createFilterRule(String rule, 
      Map<String, String> params) throws Exception
   {
      IPSFilterService service = PSFilterServiceLocator.getFilterService();
      
      return service.createRuleDef(rule, params);
   }
   
   /**
    * Remove the supplied filter from the repository.
    * 
    * @param filter the filter to remove, may be <code>null</code>.
    * @throws Exception for any error.
    */
   private void removeItemFilter(IPSItemFilter filter) throws Exception
   {
      IPSFilterService service = PSFilterServiceLocator.getFilterService();
      
      if (filter != null)
         service.deleteFilter(filter);
   }
}

