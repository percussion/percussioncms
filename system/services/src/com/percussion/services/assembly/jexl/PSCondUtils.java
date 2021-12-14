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
