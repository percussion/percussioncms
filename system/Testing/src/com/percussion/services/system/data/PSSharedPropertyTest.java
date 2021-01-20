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
package com.percussion.services.system.data;

import com.percussion.services.system.IPSSystemService;
import com.percussion.services.system.PSSystemServiceLocator;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.webservices.PSErrorsException;
import com.percussion.webservices.system.IPSSystemDesignWs;
import com.percussion.webservices.system.PSSystemWsLocator;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Unit tests for the {@link PSSharedProperty} class.
 */
@Category(IntegrationTest.class)
public class PSSharedPropertyTest extends TestCase
{
   /**
    * Test all object contracts.
    */
   @Test
   public void testContracts()
   {
      PSSharedProperty property = new PSSharedProperty();
      
      String name = "name";
      String value = "value";
      
      try
      {
         property = new PSSharedProperty(null, value);
         assertFalse("Should have thrown exception", false);
      }
      catch (IllegalArgumentException e)
      {
         // expected exception
         assertTrue(true);
      }
      
      try
      {
         property = new PSSharedProperty(" ", value);
         assertFalse("Should have thrown exception", false);
      }
      catch (IllegalArgumentException e)
      {
         // expected exception
         assertTrue(true);
      }

      property = new PSSharedProperty(name, null);
      
      property = new PSSharedProperty(name, " ");
      
      property = new PSSharedProperty(name, value);
      
      try
      {
         property.setVersion(-1);
         assertFalse("Should have thrown exception", false);
      }
      catch (IllegalArgumentException e)
      {
         // expected exception
         assertTrue(true);
      }
      
      property.setVersion(0);
      
      try
      {
         property.setVersion(1);
         assertFalse("Should have thrown exception", false);
      }
      catch (IllegalStateException e)
      {
         // expected exception
         assertTrue(true);
      }
   }
   
   /**
    * Test all CRUD services.
    */
   @Test
   public void testCRUD() throws Exception
   {
      IPSSystemService service = PSSystemServiceLocator.getSystemService();
      
      // find all properties
      List<PSSharedProperty> properties = 
         service.findSharedPropertiesByName(null);
      int count = properties.size();
      
      // create and save property
      PSSharedProperty property = new PSSharedProperty("name", "value");
      service.saveSharedProperty(property);
      
      // find properties
      properties = service.findSharedPropertiesByName(null);
      assertTrue(properties != null && properties.size() == count+1);
      
      // save the property
      property.setValue("new value");
      service.saveSharedProperty(property);
      PSSharedProperty property2 = service.loadSharedProperty(
         property.getGUID());
      assertTrue(property.equals(property2));
      
      // delete the property
      service.deleteSharedProperty(property.getGUID());
      properties = service.findSharedPropertiesByName(null);
      assertTrue(properties != null && properties.size() == count);
   }
   
   /**
    * Create a new shared property for the supplied parameters and save it to 
    * the repository.
    * 
    * @param name the property name, not <code>null</code> or empty.
    * @param value the property value, may be <code>null</code> or empty.
    * @param session the session used to create the property, not 
    *    <code>null</code> or empty.
    * @param user the user used to create the property, not <code>null</code>
    *    or empty.
    * @return the new shared property, saved in the repository, never 
    *    <code>null</code>.
    * @throws PSErrorsException if the property could not be created.
    */
   public static PSSharedProperty createProperty(String name, String value, 
      String session, String user) throws PSErrorsException
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name cannot be null or empty");
      
      if (StringUtils.isBlank(session))
         throw new IllegalArgumentException("session cannot be null or empty");
      
      if (StringUtils.isBlank(user))
         throw new IllegalArgumentException("user cannot be null or empty");
      
      IPSSystemDesignWs service = PSSystemWsLocator.getSystemDesignWebservice();
      
      List<PSSharedProperty> properties = new ArrayList<PSSharedProperty>();
      properties.add(new PSSharedProperty(name, value));
      service.saveSharedProperties(properties, true, session, user);
      
      return properties.get(0);
   }
}

