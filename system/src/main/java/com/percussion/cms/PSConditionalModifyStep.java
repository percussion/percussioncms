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

package com.percussion.cms;

import com.percussion.data.PSConditionalEvaluator;
import com.percussion.data.PSExecutionData;
import com.percussion.data.PSInternalRequestCallException;
import com.percussion.design.objectstore.PSSystemValidationException;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.util.PSCollection;

/**
 * A conditional version of {@link PSUpdateStep}.  Supplied conditions are
 * evaluated at runtime and the step is only executed if the conditions evaluate
 * to <code>true</code>.
 */
public class PSConditionalModifyStep extends PSUpdateStep
{
   /**
    * Constructs a conditional step.  Step is only executed if the conditions
    * evaluate to <code>true</code>.  See {@link PSUpdateStep#PSUpdateStep(
    * String, String, String, boolean) PSUpdateStep ctor} for a description of the
    * common parameters and exceptions.
    *
    * @param conditions A collection of PSConditional objects to evaluate at
    * runtime. If the conditions evalutate to <code>true</code>, the step is
    * executed, if <code>false</code>, then it is skipped.  May not be <code>
    * null</code>.
    */
   public PSConditionalModifyStep(String requestName, String dbActionTypeParam,
      String dbActionType,  boolean allowMultiple, PSCollection conditions)
   {
      super(requestName, dbActionTypeParam, dbActionType, allowMultiple);

      if (conditions == null)
         throw new IllegalArgumentException("conditions may not be null");

      try
      {
         m_conditions = new PSConditionalEvaluator(conditions);
      }
      catch (IllegalArgumentException e)
      {
         throw new IllegalArgumentException(e.getLocalizedMessage());
      }
   }

   /**
    * Tests the conditions to see if the step should be executed.  If not, it
    * simply returns.  If so, executes the request against the resource
    * handler.  See {@link PSModifyStep#execute(PSExecutionData)
    * super.execute()} for a description of the parameters and exceptions.
    *
    */
   public void execute(PSExecutionData data)
      throws PSInternalRequestCallException, PSAuthorizationException,
      PSAuthenticationFailedException, PSSystemValidationException
   {
      if (m_conditions.isMatch(data))
         super.execute(data);
   }

   /**
    * A conditional evalutator used to determine if this step is to be executed.
    * Initialized in the constructor.
    */
   private PSConditionalEvaluator m_conditions = null;
}
