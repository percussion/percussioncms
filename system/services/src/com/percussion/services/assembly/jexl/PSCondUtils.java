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
package com.percussion.services.assembly.jexl;

import com.percussion.extension.IPSJexlMethod;
import com.percussion.extension.IPSJexlParam;
import com.percussion.extension.PSJexlUtilBase;

/**
 * Functions for conditional value selection
 * 
 * @author dougrand
 */
public class PSCondUtils extends PSJexlUtilBase
{
   /**
    * choose a value based on a boolean expression
    * 
    * @param conditional a value that is evaluated to a true or false that chooses the value
    * @param valuetrue the value to return if the conditional is <code>true</code>
    * @param valuefalse the value to return if the conditional is <code>false</code>
    * @return the <code>true</code> or <code>false</code> value depending on the
    * conditional
    */
   @IPSJexlMethod(description = "choose a value based on a boolean expression", params =
   {
         @IPSJexlParam(name = "conditional", description = "a value that is evaluated to a true or false that chooses the value"),
         @IPSJexlParam(name = "valuetrue", description = "the value to return if the conditional is true"),
         @IPSJexlParam(name = "valuefalse", description = "the value to return if the conditional is false")})
   @Deprecated
   public Object choose(Object conditional, Object valuetrue, Object valuefalse)
   {
      boolean bval = false;

      if (conditional instanceof Boolean)
      {
         bval = (Boolean) conditional;
      }
      else if (conditional instanceof Number)
      {
         Number n = (Number) conditional;

         bval = n.longValue() != 0;
      }
      else
      {
         String sval = conditional.toString().toLowerCase();

         bval = sval.equals("t") || sval.equals("true") || sval.equals("y")
               || sval.equals("yes") || sval.equals("1");
      }
      return bval ? valuetrue : valuefalse;
   }
}
