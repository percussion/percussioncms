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
