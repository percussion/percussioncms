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

import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.design.objectstore.PSRelationshipConfigSet;
import com.percussion.design.objectstore.PSRelationshipConfigTest;
import com.percussion.services.guidmgr.data.PSDesignGuid;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.webservices.system.RelationshipConfigSummary;
import com.percussion.webservices.transformation.converter.PSRelationshipConfigConverter;
import com.percussion.webservices.transformation.impl.PSTransformerFactory;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.beanutils.Converter;
import org.junit.experimental.categories.Category;

/**
 * Unit tests for the {@link PSRelationshipConfigConverter} class.
 */
@Category(IntegrationTest.class)
public class PSRelationshipConfigConverterTest extends PSConverterTestBase
{
   /**
    * Tests the conversion from a server to a client object and vice versa.
    */
   public void testConversion() throws Exception
   {      
      // test with simple relationship
      PSRelationshipConfig simpleConfig = getSimpleConfig("simpleConfig");
      roundTripConvertion(simpleConfig);

      PSRelationshipConfigSet cset = PSRelationshipConfigTest.getConfigs();
      Iterator configs = cset.iterator();
      while (configs.hasNext())
      {
         roundTripConvertion((PSRelationshipConfig)configs.next());
      }
   }

   /**
    * Round trip the supplied source.
    * 
    * @param source the source to round trip, assumed not <code>null</code>.
    * @throws Exception for any error.
    */
   private void roundTripConvertion(PSRelationshipConfig source)
      throws Exception
   {
      PSRelationshipConfig target = (PSRelationshipConfig) roundTripConversion(
         PSRelationshipConfig.class, 
         com.percussion.webservices.system.PSRelationshipConfig.class, source);

      // verify the the round-trip object is equal to the source object
      assertTrue(source.equals(target));
   }

   /**
    * Test a list of server object convert to (client) search array,
    * and vice versa.
    *
    * @throws Exception if an error occurs.
    */
   @SuppressWarnings("unchecked")
   public void testConfigListToArray() throws Exception
   {
      PSRelationshipConfigSet cset = PSRelationshipConfigTest.getConfigs();
      List<PSRelationshipConfig> srcList = cset.getConfigList();

      // test simple search objects
      List<PSRelationshipConfig> tgtList = roundTripListConversion(
         com.percussion.webservices.system.PSRelationshipConfig[].class, 
         srcList);
      
      assertTrue(srcList.equals(tgtList));
   }
   
   /**
    * Test the relationship config summary converter.
    * 
    * @throws Exception for any error.
    */
   public void testRelationshipConfigSummary() throws Exception
   {
      PSRelationshipConfigSet cset = PSRelationshipConfigTest.getConfigs();
      List<PSRelationshipConfig> srcList = cset.getConfigList();

      PSTransformerFactory factory = PSTransformerFactory.getInstance();
      
      // convert from list to array
      Converter converter = factory
            .getConverter(RelationshipConfigSummary[].class);
      RelationshipConfigSummary[] tgtArray = 
         (RelationshipConfigSummary[]) converter.convert(
            RelationshipConfigSummary[].class, srcList);
      
      for (int i=0; i < tgtArray.length; i++)
      {
         PSRelationshipConfig src = srcList.get(i);
         long id = (new PSDesignGuid(tgtArray[i].getId())).longValue();
         assertTrue(id == src.getId());
         assertTrue(tgtArray[i].getName().equals(src.getName()));
         assertTrue(tgtArray[i].getLabel().equals(src.getLabel()));
         assertTrue(tgtArray[i].getDescription().equals(src.getDescription()));      
      }
   }

   /**
    * Creates an search with the given name.
    *
    * @param name the name of the new action, assumed not <code>null</code>.
    *
    * @return the created action, never <code>null</code>.
    */
   private PSRelationshipConfig getSimpleConfig(String name) throws Exception
   {
      PSRelationshipConfig target = new PSRelationshipConfig(name, 
         PSRelationshipConfig.RS_TYPE_USER, PSRelationshipConfig.CATEGORY_COPY);
      target.setId(1000);
      
      return target;
   }
}

