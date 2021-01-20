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

import com.percussion.data.IPSInternalRequestHandler;
import com.percussion.data.PSExecutionData;
import com.percussion.data.PSInternalRequestCallException;
import com.percussion.design.objectstore.PSValidationException;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;

/**
 * Base class for all modify steps that are part of a modify plan.  To process a
 * modify plan, each step of the plan is executed.  When executing
 * a step, a request is made against an IPSInternalRequestHandler, which is
 * essentially a dataset in the modify handler's update application.
 */
public abstract class PSModifyStep implements IPSModifyStep
{
   /**
    * Constructs a step.  The request handler must be set on the step after
    * construction by a call to {@link
    * #setHandler(IPSInternalRequestHandler) setHandler}.
    *
    * @param requestMame The request name that is used to retrieve the resource
    * handler.  May not be <code>null</code>.
    *
    * @param dbActionType The dbaction type that must be set in the request
    * params before making a request against this handler.  May not be
    * <code>null</code>.
    *
    * @throws IllegalArgumentException if requestName, dbActionTypeParam, or
    * dbActionType is <code>null</code>.
    *
    */
   public PSModifyStep(String requestName, String dbActionTypeParam,
      String dbActionType)
   {
      if (requestName == null || dbActionTypeParam == null ||
         dbActionType == null)
         throw new IllegalArgumentException(
            "requestName, dbActionTypeParam and dbActionType may not be null");

      m_reqName = requestName;
      m_dbActionParam = dbActionTypeParam;
      m_dbActionType = dbActionType;
   }

   /**
    * Sets the handler on this step.
    *
    * @param handler The resource handler for this type.  May not be <code>
    * null</code>.
    *
    * @throws IllegalArgumentException if handler is <code>null</code>, or if a
    * handler has already been set on this step.
    */
   public void setHandler(IPSInternalRequestHandler handler)
   {
      if (handler == null)
         throw new IllegalArgumentException("handler may not be null");

      if (m_handler != null)
         throw new IllegalArgumentException("a handler has already been set");

      m_handler = handler;
   }

   /**
    * Gets the handler that has been set on this step.
    *
    * @return The handler, or <code>null</code> if one has not been set.
    */
   public IPSInternalRequestHandler getHandler()
   {
      return m_handler;   
   }

   /**
    * Returns the request name.
    *
    * @return The request name associated with the handler used by this step.
    * Never <code>null</code>.
    */
   public String getName()
   {
      return m_reqName;
   }

   /**
    * Executes the request against the resource handler.
    *
    * @param data The execution data.  May not be <code>null</code>.
    *
    * @throws PSAuthorizationException if the user is not authorized to
    * perform the step.
    * @throws PSAuthenticationFailedException if the user cannot be
    * authenticated.
    * @throws PSValidationException if the step does any validation and the
    * validation fails.
    * @throws PSInternalRequestCallException if there are any other errors.
    * @throws IllegalArgumentException if data is <code>null</code>.
    * @throws IllegalStateException if a handler has not been set.
    */
   public abstract void execute(PSExecutionData data)
      throws PSInternalRequestCallException, PSAuthorizationException,
      PSAuthenticationFailedException, PSValidationException;


   /**
    * The request Name of the dataset this step will execute against.
    * Initialized in the constructor, never <code>null</code> after that.
    */
   private String m_reqName = null;

   /**
    * The DBActionType value to set in the request param before making
    * the request.
    * Initialized in the constructor, never <code>null</code> after that.
    */
   protected String m_dbActionType = null;

   /**
    * The DBActionType param to set in the request params before making
    * the request.
    * Initialized in the constructor, never <code>null</code> after that.
    */
   protected String m_dbActionParam = null;

   /**
    * The handler to make requests against when executing this step.
    * Initialized by a call to {@link #setHandler(IPSInternalRequestHandler)
    * setHandler()}.
    */
   private IPSInternalRequestHandler m_handler = null;
}

