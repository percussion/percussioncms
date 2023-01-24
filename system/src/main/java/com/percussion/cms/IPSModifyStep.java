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

import com.percussion.data.IPSInternalRequestHandler;
import com.percussion.data.PSExecutionData;
import com.percussion.data.PSInternalRequestCallException;
import com.percussion.design.objectstore.PSSystemValidationException;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;

/**
 * Interface for executing modify steps as part of a {@link PSModifyPlan}.
 * Steps are executed by making requests to an internal request handler.  
 */
public interface IPSModifyStep
{
   /**
    * Executes the request against an internal resource handler.
    *
    * @param data The execution data.  May not be <code>null</code>.
    *
    * @throws PSAuthorizationException if the user is not authorized to
    * perform the step.
    * @throws PSAuthenticationFailedException if the user cannot be
    * authenticated.
    * @throws PSSystemValidationException if the step does any validation and the
    * validation fails.
    * @throws PSInternalRequestCallException if there are any other errors.
    * @throws IllegalArgumentException if data is <code>null</code>.
    * @throws IllegalStateException if a handler has not been set.
    */
   public void execute(PSExecutionData data)
      throws PSInternalRequestCallException, PSAuthorizationException,
      PSAuthenticationFailedException, PSSystemValidationException;


   /**
    * Sets the handler on this step.
    *
    * @param handler The resource handler for this type.  May not be <code>
    * null</code>.
    *
    * @throws IllegalArgumentException if handler is <code>null</code>, or if a
    * handler has already been set on this step.
    */
   public void setHandler(IPSInternalRequestHandler handler);

   /**
    * Gets the handler that has been set on this step.
    *
    * @return The handler, or <code>null</code> if one has not been set.
    */
   public IPSInternalRequestHandler getHandler();


   /**
    * Returns the request name used to retrieve the handler.
    *
    * @return The request name associated with the handler used by this step.
    * Never <code>null</code>.
    */
   public String getName();


}
