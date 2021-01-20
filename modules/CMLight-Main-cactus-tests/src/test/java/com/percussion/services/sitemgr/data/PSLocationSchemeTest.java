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
 
package com.percussion.services.sitemgr.data;

import com.percussion.services.guidmgr.data.PSGuid;

import java.util.List;

import com.percussion.utils.testing.IntegrationTest;
import org.apache.cactus.ServletTestCase;

import junit.framework.TestCase;
import org.junit.experimental.categories.Category;

/**
 * Unit test for the {@link PSLocationScheme} object.
 */
@Category(IntegrationTest.class)
public class PSLocationSchemeTest extends ServletTestCase
{
   /**
    * Test equals and hashcode
    * 
    * @throws Exception if the test fails.
    */
   public void testEquals() throws Exception
   {
      // testing with no parameters
      PSLocationScheme scheme1 = createScheme(0);
      PSLocationScheme scheme2 = new PSLocationScheme();
      assertTrue(!scheme1.equals(scheme2));
      scheme2 = (PSLocationScheme) scheme1.clone();
      assertEquals(scheme1, scheme2);
      assertEquals(scheme1.hashCode(), scheme2.hashCode());
      
      // testing with one parameter
      scheme1.setParameter("name", "type", "value");
      scheme2 = (PSLocationScheme) scheme1.clone();
      assertEquals(scheme1, scheme2);
      //compareSchemes(scheme1, scheme2);
      assertEquals(scheme1.hashCode(), scheme2.hashCode());
      assertTrue(scheme2.getParameterNames().size() == 1);

      scheme2.setParameter("name", "type", "value");
      assertTrue(scheme2.getParameterNames().size() == 1);
      
      // testing with multiple parameters
      scheme1.setParameter("name2", "type2", "value2");
      scheme2 = (PSLocationScheme) scheme1.clone();
      assertEquals(scheme1, scheme2);
      //compareSchemes(scheme1, scheme2);
      assertEquals(scheme1.hashCode(), scheme2.hashCode());
      assertTrue(scheme2.getParameterNames().size() == 2);

      scheme2.setParameter("name3", "type3", "value3");
      assertTrue(!scheme1.equals(scheme2));
      assertTrue(scheme2.getParameterNames().size() == 3);
      
      scheme2.setParameter("name", "type", "value");
      assertTrue(scheme2.getParameterNames().size() == 3);      
   }
   
   /**
    * Test the xml serialization
    * 
    * @throws Exception if there are any errors.
    */
   public void testXml() throws Exception
   {
      // testing with no parameters
      PSLocationScheme scheme1 = createScheme(0);
            
      String str = scheme1.toXML();
      PSLocationScheme scheme2 = new PSLocationScheme();
      assertTrue(!scheme1.equals(scheme2));
      scheme2.fromXML(str);
      compareSchemes(scheme1, scheme2);
      
      // testing with multiple parameters
      scheme1 = createScheme(2);
      scheme2 = new PSLocationScheme();
      assertTrue(!scheme1.equals(scheme2));
      str = scheme1.toXML();
      scheme2.fromXML(str);
      compareSchemes(scheme1, scheme2);
   }
   
   /**
    * Creates a location scheme with dummy values.
    * 
    * @param params The number of location scheme parameters to add to the
    * newly created scheme.
    * 
    * @return a new {@link PSLocationScheme} object initialized with dummy
    * values.
    */
   private PSLocationScheme createScheme(int params)
   {
      PSLocationScheme scheme = new PSLocationScheme();
      scheme.setContentTypeId(new Long(311));
      scheme.setContextId(new PSGuid("0-100-501"));
      scheme.setDescription("This is a test description");
      scheme.setGenerator("Java/com/percussion/extension/general/test");
      scheme.setGUID(new PSGuid("0-113-1"));
      scheme.setName("scheme1");
      scheme.setTemplateId(new Long(312));
      scheme.setVersion(new Integer(0));
      
      for (int i = 0; i < params; i++)
      {
         scheme.addParameter("param" + i, i, "type" + i, "value" + i);
      }
      
      return scheme;
   }
   
   /**
    * Compares two location schemes.  If this method returns, then the two
    * schemes are equal.  This method is used in place of 
    * {@link PSLocationScheme#equals(Object)} to confirm the equality of two
    * schemes in {@link #testXml()} due to the fact that
    * {@link PSLocationScheme#toXML()} and
    * {@link PSLocationScheme#fromXML(String)} do not account for location
    * scheme parameter id's, which can't be set because they are created each
    * time a new parameter is added.
    * 
    * @param scheme1 the first location scheme, assumed not <code>null</code>.
    * @param scheme2 the second location scheme, assumed not <code>null</code>.
    */
   private void compareSchemes(PSLocationScheme scheme1, 
         PSLocationScheme scheme2)
   {
      assertEquals(scheme1.getContentTypeId(), scheme2.getContentTypeId());
      assertEquals(scheme1.getContextId(), scheme2.getContextId());
      assertEquals(scheme1.getDescription(), scheme2.getDescription());
      assertEquals(scheme1.getGenerator(), scheme2.getGenerator());
      assertEquals(scheme1.getGUID(), scheme2.getGUID());
      assertEquals(scheme1.getName(), scheme2.getName());
      assertEquals(scheme1.getTemplateId(), scheme2.getTemplateId());
            
      assertTrue(scheme1.getParameterNames().size() == 
         scheme2.getParameterNames().size());
      List<String> params1 = scheme1.getParameterNames();
      for (String param : params1)
      {
         String type = scheme1.getParameterType(param);
         String value = scheme1.getParameterValue(param);
         Integer sequence = scheme1.getParameterSequence(param);
         assertEquals(type, scheme2.getParameterType(param));
         assertEquals(value, scheme2.getParameterValue(param));
         assertEquals(sequence, scheme2.getParameterSequence(param));
      }
   }
}
