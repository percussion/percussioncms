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
