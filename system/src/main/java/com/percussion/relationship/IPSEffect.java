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
package com.percussion.relationship;

import com.percussion.extension.IPSExtension;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.server.IPSRequestContext;

/**
 * Relationships can have effects attached. All relationship effects must
 * implement this interface. They can be executed from different execution
 * contexts. See <code>com.percussion.design.objectstore.PSRelationship</code>
 * and PSXRelationshipSet.dtd for a description of relationships.
 * <strong>NOTE:</strong> As all server extensions, all implementations of this
 * interface must be  thread safe!
 */
public interface IPSEffect extends IPSExtension
{
   /**
    * This method is called by the relationship processor first in the
    * processing of a relationship effect thing before the
    * {@link #attempt(Object[], IPSRequestContext, IPSExecutionContext, PSEffectResult)}
    * method to test whether or not the effect can be executed. Implementations
    * can test if all parameters needed are available, etc. If the test
    * succeeds, the method should set success on the provided result object. If
    * the test fails for any reason this method must return set error on the
    * provided result object. If there is a non critical error the effect could
    * set a warning on the result object. If the test needs to be recursed for
    * the dependents this method must set the recurse dependents flag to
    * <code>true</code> using {@link PSEffectResult#setRecurseDependents}
    * method. The relationship processor calls this method until either the
    * results status is STATUS_ERROR or recurse dependent flag is false. If the
    * final result of test method is success, then the processor will call the
    * attempt method.
    * 
    * @param params
    *           and array of effect specific parameters. The parameters used in
    *           each effect are specified in the effect definition. Might be
    *           <code>null</code> or empty.
    * @param request
    *           the request context in which this effect is being executed,
    *           migth be <code>null</code>.
    * @param context
    *           the execution context in which this effect is being executed,
    *           might be <code>null</code>.
    * @param result
    *           effect result object that effect must use to communicate the
    *           result of the operation requested, never <code>null</code>.
    * @throws PSExtensionProcessingException
    *            if anything goes wrong executing the test and the implementer
    *            chooses so.
    * @throws PSParameterMismatchException
    *            if parameter validation for the effect fails.
    */
   public void test(Object[] params, IPSRequestContext request,
      IPSExecutionContext context, PSEffectResult result)
      throws PSExtensionProcessingException, PSParameterMismatchException;
   
   /**
    * Executes this effect for the provided request and execution context.
    *
    * @param    params    and array of effect specific parameters. The parameters
    * used in each effect are specified in the effect definition.
    * Might be <code>null</code> or empty.
    * @param    request    the request context in which this effect is being
    * executed, migth be <code>null</code>.
    * @param    context    the execution context in which this effect is being
    * executed, might be <code>null</code>.
    * @exception PSParameterMismatchException if the supplied parameters are
    * not correct.
    * @param  result effect result object that effect must use to communicate
    * the result of the operation requested, never <code>null</code>.
    *
    * @throws PSExtensionProcessingException if anything goes wrong executing
    * the attempt and the implementer chooses so. For this case the default
    * implementation calls the recovery method.
    * @throws PSParameterMismatchException if parameter validation for the
    * effect fails.
    */
   public void attempt(Object[] params, IPSRequestContext request,
      IPSExecutionContext context, PSEffectResult result)
      throws PSExtensionProcessingException, PSParameterMismatchException;

   /**
    * If anything fails making an attempt to execute this effect, this method
    * will be called to recover from that error. Things such as rollbacks can be
    * implemented in this method.
    *
    * @param    params    and array of effect specific parameters. The parameters
    * used in each effect are specified in the effect definition. Might be
    * <code>null</code> or empty.
    * @param    request    the request context in which this effect is being
    * executed, migth be <code>null</code>.
    * @param    context    the execution context in which this effect is being
    * executed, might be <code>null</code>.
    * @param    e    the exception that caused a recovery to be required. This
    * can  be rethrown once the recovery is executed. Not <code>null</code>.
    * @param  result effect result object that effect must use to communicate
    * the result of the operation requested, never <code>null</code>.
    *
    * @throws PSExtensionProcessingException if anything goes wrong recovering
    * from an error in the atempt.
    */
   public void recover(Object[] params, IPSRequestContext request,
      IPSExecutionContext context, PSExtensionProcessingException e,
      PSEffectResult result) throws PSExtensionProcessingException;
}
