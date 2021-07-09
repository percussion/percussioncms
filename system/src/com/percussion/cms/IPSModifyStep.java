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
