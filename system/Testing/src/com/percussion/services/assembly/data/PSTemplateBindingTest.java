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
package com.percussion.services.assembly.data;


import com.percussion.utils.testing.UnitTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

/**
 * @author dougrand
 *
 */
@Category(UnitTest.class)
public class PSTemplateBindingTest
{
   /**
    * 
    */
   @Test
   public void testBasics()
   {
      final PSTemplateBinding binding = new PSTemplateBinding();
      
      // variable can be null or empty.
      final String VALUE = "Value !";
      binding.setVariable(VALUE);
      assertEquals(VALUE, binding.getVariable());
      binding.setVariable("");
      assertEquals("", binding.getVariable());
      binding.setVariable(null);
      assertNull(binding.getVariable());
   }
   
   /**
    * 
    */
   public void testEqualsHash()
   {
      final PSTemplateBinding binding1 = new PSTemplateBinding();
      final PSTemplateBinding binding2 = new PSTemplateBinding();
      
      checkEqual(binding1, binding2);
      
      final String expression = "Expression";
      binding1.setExpression(expression);
      assertFalse(binding1.equals(binding2));
      
      binding2.setExpression(expression);
      checkEqual(binding1, binding2);

      assertFalse(binding1.equals(null));
      assertFalse(binding1.equals(new Object()));
   }

   private void checkEqual(final Object o1, final Object o2)
   {
      assertEquals(o1, o2);
      assertEquals(o2, o1);
      assertEquals(o1.hashCode(), o2.hashCode());
   }
   
   /**
    * 
    */
   public void testClone()
   {
      PSTemplateBinding binding = new PSTemplateBinding(101, "variable 1", "expression 1");
      assertEquals(binding, binding.clone());
      assertEquals(binding.clone(), binding);
      assertEquals(binding.hashCode(), binding.clone().hashCode());
   }
}
