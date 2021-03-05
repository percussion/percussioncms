/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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

import com.percussion.data.PSExecutionData;
import com.percussion.data.PSInternalRequestCallException;
import com.percussion.design.objectstore.PSSystemValidationException;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.PSRequest;
import com.percussion.util.IPSHtmlParameters;

import java.util.Map;

/**
 * Modify step that updates the a content item.
 */
public class PSUpdateStep extends PSModifyStep
{
   /**
    * Constructs a step. See {@link PSModifyStep#PSModifyStep(String, String,
    * String) super()} for more info on params and exceptions. The differences
    * are noted below.
    * 
    * @param allowMultiple Indicates if multiple rows should be inserted or
    * updated if the params map in the request contains ArrayLists. If <code>
    * false</code>
    * and there are lists in the parameter map, then a copy will be used with
    * each parameter truncated to the first value in it's list. If
    * <code>true</code>, then the parameters will be passed unaltered.
    * Special handling is done if psxmldoc param is present. If that is the only
    * multi-value param present, then it is treated as a single update. This
    * special case handling is required when there are multiple 'file' controls
    * on a complex child. 
    * 
    */
   public PSUpdateStep(String requestName, String dbActionTypeParam,
      String dbActionType, boolean allowMultiple)
   {
      super(requestName, dbActionTypeParam, dbActionType);
      m_allowMultiple = allowMultiple;
   }

   /**
    * Convienience ctor that always disallows muliple updates.           
    */
   public PSUpdateStep(String requestName, String dbActionTypeParam,
      String dbActionType)
   {
      this(requestName, dbActionTypeParam, dbActionType, false);
   }

   // see PSModifyStep for javadoc
   @Override
   // unchecked request param map, unused exception
   @SuppressWarnings({ "unchecked", "unused" }) 
   public void execute(PSExecutionData data)
      throws PSInternalRequestCallException, PSAuthorizationException,
      PSAuthenticationFailedException, PSSystemValidationException
   {

      if (data == null)
         throw new IllegalArgumentException(
            "data must be supplied");

      if (getHandler() == null)
         throw new IllegalStateException(
            "cannot execute unless a handler has been set");

      PSRequest request = data.getRequest();
      Map<String, Object> tmpParams = null;
      PSExecutionData intExecData = null;

      try
      {
         /*
          * if not allowing multiple, need to save the params and truncate the
          * the current params, and then set the saved params back on the
          * request once we're done. 
          * If a complex child is submitted, and it has
          * more than 1 'File' control, then it will have the psxmldoc param
          * more than once (i.e. its value will be a list in the params.) This
          * causes problems because the complex-child handling code supports
          * multiple child entries in 1 submission. 
          * 
          */
         if (!m_allowMultiple 
            || request.isOnlyMultiValueParam(IPSHtmlParameters.REQ_XML_DOC_FLAG))
         {
            tmpParams = request.getParameters();
            request.setParameters(request.getTruncatedParameters());
         }
         else if (m_controlParam != null)
         {
            tmpParams = request.getParameters();
            request.setParameters(request.getBalancedParameters(
               m_controlParam));
         }

         // set the DBActionType - it may not match what's in the request
         // get the param map
         request.setParameter(m_dbActionParam, m_dbActionType);
         intExecData = getHandler().makeInternalRequest(request);
      }
      finally
      {
         if (intExecData != null)
            intExecData.release();
         if (tmpParams != null)
            request.setParameters(tmpParams);
      }
   }

   /**
    * Set the name of the parameter to use to control parameter value list
    * length.  If multiple row updates are allowed, all parameters in the
    * parameter map are truncated to this parameter's orginal length before
    * the request is executed.  If multiple row updates are not allowed, setting
    * this value will have no effect.
    *
    * @param paramName Name of the parameter to use.  May be <code>null
    * </code>, parameter with this name must be in the request's paramter map.
    */
   public void setControlParam(String paramName)
   {
      m_controlParam = paramName;
   }

   /**
    * Flag to indicate if multiple rows should be inserted or updated if the
    * params map in the request contains ArrayLists.  Set in the constructor.
    */
   private boolean m_allowMultiple = false;


   /**
    * Name of the parameter to use to control length of parameter lists if
    * allowing multiple rows.  If the params map contains lists and multiple
    * rows are allowed, then the original length of this parameter's list will
    * cause the map to truncate value ArrayLists to that original list's length.
    * Modified by a call to {@link #setControlParam(String) setControlParam},
    * may be <code>null</code>.
    */
   private String m_controlParam = null;

}
