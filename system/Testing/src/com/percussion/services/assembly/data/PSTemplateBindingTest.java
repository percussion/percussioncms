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
