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

import com.percussion.error.PSException;
import com.percussion.extension.IPSExtensionErrors;
import com.percussion.extension.IPSWorkFlowContext;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.relationship.IPSExecutionContext;
import com.percussion.relationship.PSEffect;
import com.percussion.relationship.PSEffectResult;
import com.percussion.server.IPSRequestContext;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.workflow.IWorkflowRoleInfo;
import com.percussion.workflow.PSExitNotifyAssignees;
import com.percussion.workflow.PSWorkFlowContext;
import com.percussion.workflow.PSWorkflowRoleInfo;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This effect notifies all assignees for the supplied workflowid, stateid
 * and transitionid if a new relationship is created.
 */
public class PSNotifyEffect extends PSEffect
{
   /**
    * This effect is only executed if the execution context indicates that
    * this relationship was just created.
    * See base class for additional information.
    */
   public void test(Object[] params, IPSRequestContext request,
      IPSExecutionContext context, PSEffectResult result)
   {
      // no-op
   }

   /**
    * Notifies all assignees for the supplied stateid and transitionid in the
    * provided workflow.
    *
    * @param params this effect takes 4 parameters:
    *    params[0] the workflowid of the workflow from which this should
    *       notify all assignees, not <code>null</code> or empty.
    *    params[1] the stateid for which this should notify all assignees,
    *       not <code>null</code> or empty.
    *    params[2] the transitionid for which this should notify all assignees,
    *       not <code>null</code> or empty.
    *    params[3] the username for which this should notify all assignees,
    *       not <code>null</code> or empty.
    * @param request the request for which to send the notifications, not
    *    <code>null</code>.
    * @param context the execution context for which to process this effect,
    *    not used, can be <code>null</code>.
    * @param result - effect result that is used to communicate effect result
    *    back to the engine, assumed never <code>null</code>.
    * @return <code>true</code> if the notifications are sent successfully.
    * @throws PSExtensionProcessingException for any error catched while
    *    notifying the assignees.
    */
   public void attempt(Object[] params, IPSRequestContext request,
      IPSExecutionContext context, PSEffectResult result)
      throws PSExtensionProcessingException, PSParameterMismatchException
   {
      /**
       * Build up a list with all parameters, with the parameter name as
       * key and the parameter value as <code>String</code>.
       */
      Map<String,Object> parameters = Collections.synchronizedMap(
              new HashMap<String,Object>());

      // get all effect parameters and add them to our parameters map
      for (int i=0; i<REQUIRED_PARAMS.length; i++)
      {
         if (params.length < i+1)
         {
            Object[] args = {REQUIRED_PARAMS[i], "missing"};
            throw new PSExtensionProcessingException(
               IPSExtensionErrors.EXT_MISSING_REQUIRED_PARAMETER_ERROR,
                  args);
         }

         String parameter = params[i].toString().trim();
         if (parameter.length() == 0)
         {
            Object[] args = {REQUIRED_PARAMS[i], "empty"};
            throw new PSExtensionProcessingException(
               IPSExtensionErrors.EXT_MISSING_REQUIRED_PARAMETER_ERROR,
                  args);
         }

         parameters.put(REQUIRED_PARAMS[i], parameter);
      }

      // get all HTML parameters and add them to our parameters map
      for (int i=0; i<REQUIRED_HTML_PARAMS.length; i++)
      {
         String parameter = request.getParameter(
            REQUIRED_HTML_PARAMS[i], "").trim();
         if (parameter == null || parameter.length() == 0)
         {
            Object[] args = {REQUIRED_HTML_PARAMS[i], "null or empty"};
            throw new PSExtensionProcessingException(
               IPSExtensionErrors.EXT_MISSING_HTML_PARAMETER_ERROR,
                  args);
         }

         parameters.put(REQUIRED_HTML_PARAMS[i], parameter);
      }

      // create workflow context
      PSWorkFlowContext wfContext = new PSWorkFlowContext(
         getIntParameter("workflowid",
            (String) parameters.get("workflowid")),
         getIntParameter(IPSHtmlParameters.SYS_CONTENTID,
            (String) parameters.get(IPSHtmlParameters.SYS_CONTENTID)),
         getIntParameter(IPSHtmlParameters.SYS_REVISION,
            (String) parameters.get(IPSHtmlParameters.SYS_REVISION)),
         getIntParameter("transitionid",
            (String) parameters.get("transitionid")),
         getIntParameter("stateid",
            (String) parameters.get("stateid")));
      request.setPrivateObject(
         IPSWorkFlowContext.WORKFLOW_CONTEXT_PRIVATE_OBJECT, wfContext);

      // create workflow role info
     IWorkflowRoleInfo wfRoleInfo = new PSWorkflowRoleInfo();
      request.setPrivateObject(
         PSWorkflowRoleInfo.WORKFLOW_ROLE_INFO_PRIVATE_OBJECT, wfRoleInfo);

      // call the notify assignees exit with an empty document
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Object[] notifyParams =
      {
         parameters.get(IPSHtmlParameters.SYS_CONTENTID),
         parameters.get("username")
      };
      try
      {
         PSExitNotifyAssignees notify = new PSExitNotifyAssignees();
         notify.processResultDocument(notifyParams, request, doc);
      }
      catch (PSException e)
      {
         throw new PSExtensionProcessingException(e.getErrorCode(),
            e.getErrorArguments());
      }

      result.setSuccess();
   }

   /**
    * We don't want to throw an exeption if notify fails, but we want to
    * log that problem.
    *
    * See <code>IPSEffect</code> for additional description.
    */
   public void recover(Object[] params, IPSRequestContext request,
      IPSExecutionContext context, PSExtensionProcessingException e,
      PSEffectResult result)
         throws PSExtensionProcessingException
   {

      result.setSuccess();
   }

   /**
    * Parses tha integer value from the supplied <code>String</code> value.
    *
    * @param name the parameter name used for error messages only, assumed not
    *    <code>null</code>.
    * @param value the value to be parsed int an integer, assumed not
    *    <code>null</code>.
    * @return the integer value.
    * @throws PSExtensionProcessingException if the supplied value is not
    *    a parsable integer.
    */
   private int getIntParameter(String name, String value) throws
      PSExtensionProcessingException
   {
      try
      {
         return Integer.parseInt(value);
      }
      catch (NumberFormatException e)
      {
         Object[] args = {name, e.getLocalizedMessage()};
         throw new PSExtensionProcessingException(
            IPSExtensionErrors.EXT_PARAM_VALUE_INVALID, args);
      }
   }

   /**
    * A list of names for all required parameters. The parameters are in the
    * order as the are expected.
    */
   private static final String[] REQUIRED_PARAMS =
   {
      "workflowid",
      "stateid",
      "transitionid",
      "username"
   };

   /**
    * A list of required HTML parameters.
    */
   private static final String[] REQUIRED_HTML_PARAMS =
   {
      IPSHtmlParameters.SYS_CONTENTID,
      IPSHtmlParameters.SYS_REVISION
   };
}
