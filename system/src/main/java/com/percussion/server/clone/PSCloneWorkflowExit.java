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
