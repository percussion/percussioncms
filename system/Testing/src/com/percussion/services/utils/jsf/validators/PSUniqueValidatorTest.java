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
package com.percussion.services.utils.jsf.validators;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.faces.validator.ValidatorException;
import junit.framework.TestCase;

/**
 * @author Andriy Palamarchuk
 */
public class PSUniqueValidatorTest extends TestCase
{
   public void testConstructor()
   {
      // not yet specified
      final PSUniqueValidator v = new PSUniqueValidator();
      try
      {
         v.validate(null, null, new Object());
         fail();
      }
      catch (IllegalStateException expected) {}

      // specified
      v.setValueProvider(new TestValueProvider());
      v.validate(null, null, new Object());
   }
   
   public void testSetValueProvider()
   {
      final PSUniqueValidator v = new PSUniqueValidator();
      try
      {
         v.setValueProvider(null);
         fail();
      }
      catch (IllegalArgumentException expected) {}
      
      v.setValueProvider(new TestValueProvider());
   }
   
   public void testValidate()
   {
      final String value1 = "Value 1";
      final String value2 = "Value 2";

      final PSUniqueValidator v = new PSUniqueValidator();

      // does not exist yet
      final TestValueProvider valueProvider = new TestValueProvider();
      valueProvider.mi_values.add(value2);
      v.setValueProvider(valueProvider);
      v.validate(null, null, value1);
      v.validate(null, null, new Object());

      // already exists
      try
      {
         v.validate(null, null, value2);
         fail();
      }
      catch (ValidatorException expected) {}
      
      //case insensitive
      try
      {
         v.validate(null, null, value2.toLowerCase());
         fail();
      }
      catch (ValidatorException expected) {}
   }
   
   /**
    * The value provider used for testing.
    */
   private static class TestValueProvider
         implements IPSUniqueValidatorValueProvider
   {
      // see base
      public Collection<Object> getAllValues()
      {
         return mi_values;
      }
      
      private final Set<Object> mi_values = new HashSet<Object>();
   }
}
