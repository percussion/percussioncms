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
package com.percussion.server.clone;

import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.server.IPSRequestContext;
import com.percussion.util.IPSHtmlParameters;
import org.w3c.dom.Document;

import java.util.HashMap;
import java.util.Map;

/**
 * This exit clones children of the source workflow (states,roles, transitions
 * etc.) and adds them to the new workflow clone. If the parameter
 * <code>clonesourceid</code> is not provided the exit does nothing. This exit
 * also needs the newly created workflow id in the form of HTML parameter named
 * "workflowid" if it is missing, the exit skips creating workflow child items
 * such as states, roles etc.
 */
public class PSCloneWorkflowExit extends PSCloneBase
{

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.extension.IPSResultDocumentProcessor#
    *       processResultDocument(java.lang.Object[],
    *         com.percussion.server.IPSRequestContext, org.w3c.dom.Document)
    */
   public Document processResultDocument(Object[] params,
         IPSRequestContext request, Document resultDoc)
         throws PSParameterMismatchException, PSExtensionProcessingException
   {

      int sourceWorkflowId = getCloneSourceId(request);
      String targetWorkflowId = request.getParameter("workflowid","").trim();
      if(targetWorkflowId.length()<0)
      {
         request
         .printTraceMessage("Error: Missing workflowid html parameter, " + 
               "skipped copying the workflow relationships."
               + "Check the resource on which this exit is placed and make sure"
               + "it supplies workflowid html parameter.");
         return resultDoc;
      }
      
      if (sourceWorkflowId > 0)
      {

         Map qrParams = new HashMap();
         qrParams.put(IPSHtmlParameters.SYS_WORKFLOWID, Integer
               .toString(sourceWorkflowId));
         Map upParams = new HashMap();
         upParams.put("DBActionType", "INSERT");

         //Call the base class cloneChildObjects method to clone the states and
         // others.
         cloneChildObjects(request, "WorkflowId", targetWorkflowId,
               ms_queryResources, ms_updateResources, qrParams, upParams);
      }
      return resultDoc;
   }

   /**
    * Array of query resource names of workflow child relations.
    */
   private static final String[] ms_queryResources =
   {"sys_wfCloning/QueryStates", "sys_wfCloning/QueryRoles",
         "sys_wfCloning/QueryNotifications", "sys_wfCloning/QueryTransitions",
         "sys_wfCloning/QueryStateRoles", "sys_wfCloning/QueryTransitionRoles",
         "sys_wfCloning/QueryTransitionNotifications"};

   /**
    * Array of update resource names of workflow child relations.
    */
   private static final String[] ms_updateResources =
   {"sys_wfCloning/UpdateStates", "sys_wfCloning/UpdateRoles",
         "sys_wfCloning/UpdateNotifications",
         "sys_wfCloning/UpdateTransitions", "sys_wfCloning/UpdateStateRoles",
         "sys_wfCloning/UpdateTransitionRoles",
         "sys_wfCloning/UpdateTransitionNotifications"};
}
