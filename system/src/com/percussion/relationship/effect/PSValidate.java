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
package com.percussion.relationship.effect;

import com.percussion.extension.IPSExtensionErrors;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.relationship.IPSExecutionContext;
import com.percussion.relationship.PSEffect;
import com.percussion.relationship.PSEffectResult;
import com.percussion.server.IPSRequestContext;

/**
 * This effect is to get the following behavior:
 * <p>
 * This effect immediately throws an exception if the context is construction
 * RS_CONSTRUCTION, otherwise, it returns immediately. It can be used to perform
 * validation using conditionals. The conditions must be written such that they
 * are the inverse of the desired validation. For example, to implement an item
 * validation (request fails if the owner is not an item), this effect with the
 * following conditional would be placed as the first effect in the
 * relationship: PSXContentItemStatus/CONTENTSTATUS.OBJECTTYPE != 1 (Note that
 * '1' is the id for objects of type of item.)
 * <p>This effect takes one parameter as described below:
 * <p>
 * params[0] the text to return in the exception. This text can be
 * internationalized.
 * <p>
 * This is typically used for validation purpose. For example, one would 
 * attach this efefct to a relationship to throw an exception with a given 
 * message (parameter) whenever a condition succeeds and to not continue 
 * processing the effect. 
 *
 */
public class PSValidate extends PSEffect
{
   /**
    * Override the methode in the base class. This effect is meant to be run
    * any context other than {@link IPSExecutionContext#RS_PRE_CONSTRUCTION}}
    * and hence will return <code>true</code> for all other contexts.
    */
   public void test(
      Object[] params,
      IPSRequestContext request,
      IPSExecutionContext context,
      PSEffectResult result)
   {
      if (context.isPreConstruction())
      {
         //Some default message
         String msg = "Validation fails";
         if (params.length > 0)
         {
            Object obj = params[0];
            if (obj != null)
               msg = obj.toString();
         }
         
         
         String[] args = {m_name, msg};
         
         result.setError(request.getUserLocale(),
            IPSExtensionErrors.EFFECT_VALIDATE_MESSAGE, args);
      }
      else
      {
         result.setSuccess();
      }
   }

   //Implementation of the base class method
   public void attempt(
      Object[] params,
      IPSRequestContext request,
      IPSExecutionContext context,
      PSEffectResult result)
      throws PSExtensionProcessingException, PSParameterMismatchException
   {
      //Validation effect does not need to process anything
      result.setSuccess();
   }

   //Implementation of the base class method
   public void recover(
      Object[] params,
      IPSRequestContext request,
      IPSExecutionContext context,
      PSExtensionProcessingException e,
      PSEffectResult result)
      throws PSExtensionProcessingException
   {
      //Validation effect does not need to recover anything
      result.setSuccess();
   }
}