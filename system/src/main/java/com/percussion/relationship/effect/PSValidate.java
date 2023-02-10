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
