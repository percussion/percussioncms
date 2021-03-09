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
package com.percussion.relationship.effect;

import com.percussion.cms.IPSCmsErrors;
import com.percussion.cms.IPSConstants;
import com.percussion.cms.PSCmsException;
import com.percussion.cms.handlers.PSWorkflowCommandHandler;
import com.percussion.data.PSInternalRequestCallException;
import com.percussion.data.macro.PSMacroUtils;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.error.PSException;
import com.percussion.extension.IPSExtensionErrors;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.relationship.IPSExecutionContext;
import com.percussion.relationship.PSEffect;
import com.percussion.relationship.PSEffectResult;
import com.percussion.server.IPSInternalRequest;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.IPSServerErrors;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSCms;
import com.percussion.workflow.PSWorkFlowUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
/**
 * This effect is to get the following behavior:
 * <p>
 * The current item cannot be transitioned to an unpublish (ContentValid != y
 * or i) state from a public state unless the other end of this relationship is
 * already unpublished. If the forceTransition parameter is yes, and a
 * transition by the supplied name is found or a default transition to an
 * unpublished state is present, the item will be transitioned with this item.
 * If the other item cannot be put into an unpublished state, an exception is
 * thrown and the item being processed is not allowed to transition. The effect
 * will return immediately for any context except RS_PRE_WORKFLOW.
 * <p>This effect takes three parameters as described below:
 * <p>
 * params[0] is a boolean flag (either "yes" or "no" value) that controls
 * whether the item at the other end of the relationship is forced to
 * transition if it is possible. If "no" and the item is already in a public
 * state, the operation will fail.
 * <p>
 * params[1] is the internal name of the transition to use if the owner needs
 * to be transitioned. If not supplied, the first transition with the "default"
 * property (in alpha order) is used.
 * <p>
 * params[2] is the internal name of the transition to use if the dependent
 * needs to be transitioned. If not supplied, the first transition with the
 * "default" property (in alpha order) is used.
 *
 * @author Ram
 * @version 1.0
 */
public abstract class PSPublishUnpublishMandatory extends PSEffect
{
   /**
    * Abstract method that retuns a mode name.
    * @return the name of the effect mode: "publish" or "unpublish",
    * never <code>null</code>.
    */
   protected abstract String getModeName();

   /**
    * Derived class determines is the given WF state is a desired
    * state or not.
    * @param elem element with a WF state, may be <code>null</code>.
    * @return <code>true</code> if the item is in desired WF state
    * <code>false</code> otherwise.
    */
   protected abstract boolean isItemInDesiredWFState(Element elem);
   
   /**
    * This method is used to determine whether the item is
    * transitioning into a WF state which should trigger
    * the relationship engine to execute attempt on this
    * effect. It is up to the derived class to decide if so.
    *
    * @param request request context mainly for I18n of the message.
    * @param isCurrentlyPublic <code>true</code> indicates
    * that the item is in the public state, <code>false</code>
    * otherwise.
    * @param isToPublic <code>true</code> indicates that this
    * item is transitioning into a public state from a non
    * public state, <code>false</code> otherwise.
    * @param isToOutOfPublic <code>true</code> indicates that
    * this item is transitioning out of a public state from
    * a public state, <code>false</code> otherwise.
    * @param result result to set, never <code>null</code>.
    *
    * @return <code>true</code> indicates that the trigger
    * condition has been met, <code>false</code> otherwise.
    */
   protected abstract boolean isTransitioningIntoTriggerState(
      IPSRequestContext request,
      boolean isCurrentlyPublic,
      boolean isToPublic,
      boolean isToOutOfPublic,
      PSEffectResult result);
   
   /**
    * Helper method to validate the parameters supplied to this effect.
    * @param request request context, assumed not <code>null</code>.
    * @param params Must not be null and must have at least two parameters
    * [0]. the required transition value must be "yes" or "no".
    */
   private void validateParams(IPSRequestContext request, Object[] params)
      throws PSParameterMismatchException, PSExtensionProcessingException
   {
      int suppliedParams = 0;
      if(params != null)
         suppliedParams = params.length;
      if(suppliedParams==0)
      {
         throw new PSParameterMismatchException(request.getUserLocale(), 2,
            suppliedParams);
      }

      if(!params[0].toString().equalsIgnoreCase("yes") &&
         !params[0].toString().equalsIgnoreCase("no"))
      {
         String args[] = {m_name, params[0].toString()};
         throw new PSExtensionProcessingException(request.getUserLocale(),
            IPSExtensionErrors.INVALID_OPTION_FOR_FORCETRANSITION, args);
      }
   }

   /**
    * Override the method in the base class. This effect is meant to be run
    * pre-workflow context and hence will return <code>false</code> for all
    * other contexts.
    * <p>
    */
   public void test(Object[] params, IPSRequestContext request,
      IPSExecutionContext context, PSEffectResult result)
      throws PSExtensionProcessingException, PSParameterMismatchException
   {
      if(!context.isPreWorkflow())
      {
         String[] args = {m_name, "pre-workflow"};
         result.setWarning(request.getUserLocale(),
            IPSExtensionErrors.ILLEGAL_EXECUTION_CONTEXT, args);
         return;
      }

      PSRelationship relationship = context.getCurrentRelationship();
      /* this check is required to handle force correctly - if this relationship
       * is between the same 2 items that an already processed relationship is
       * between, and the relationship types are the same, then skip
       */
      if (context.getProcessedRelationships() != null)
      {
         Iterator rels = context.getProcessedRelationships().iterator();
         while (rels.hasNext())
         {
            PSRelationship rel = (PSRelationship) rels.next();
            PSLocator o1 = relationship.getOwner();
            PSLocator o2 = rel.getOwner();
            PSLocator d1 = relationship.getDependent();
            PSLocator d2 = rel.getDependent();
            if (((o1.getId() == o2.getId()) && (d1.getId() == d2.getId()))
                  || ((o1.getId() == d2.getId()) && (d1.getId() == o2.getId())))
            {
               if (relationship.getConfig().getType() == rel.getConfig().getType())
               {
                  //these msgs aren't seen by the user, so I didn't i18n them
                  result.setWarning(request.getUserLocale(),
                        IPSServerErrors.RAW_DUMP, new String[] {
                        "Skip: already processed same owner/dependent."});
                  return;
               }
            }
         }
      }
      
      validateParams(request, params);

      //Always recurse.
      result.setRecurseDependents(true);

      // ignore events caused by forcing dependents to public
      String forceDependent = request.getParameter(
         IPSHtmlParameters.SYS_FORCEDEPENDENT, "");

      if (forceDependent.equals(String.valueOf(true)))
      {
         String[] args = {m_name};
         result.setWarning(request.getUserLocale(),
            IPSExtensionErrors.EFFECT_SELF_TRIGGERED, args);
         return;
      }

      boolean forceTransition = params[0].toString().equalsIgnoreCase("yes");

      String trNameOwner = "";
      String trNameDependent = "";

      if(params.length > 1 && params[1] != null)
         trNameOwner = params[1].toString();
      if(params.length > 2 && params[2] != null)
         trNameDependent = params[2].toString();

      String wfAction = request.getParameter(
         IPSConstants.DEFAULT_ACTION_TRIGGER_NAME, "").trim();

      // ignore invalid actions
      if (wfAction == null || wfAction.length() == 0)
      {
         wfAction = (wfAction==null) ? "null" : wfAction;
         String[] args = {m_name, wfAction};
         result.setWarning(request.getUserLocale(),
            IPSExtensionErrors.INVALID_WORKFLOW_ACTION, args);
         return;
      }

      boolean isOwner =
         (context.getActivationEndPoint()==IPSExecutionContext.RS_ENDPOINT_OWNER);

      String transitionName = isOwner ? trNameOwner : trNameDependent;

      PSLocator locator = getOtherEnd(relationship, context);

      try
      {
          /* (This comment applies to the 2nd condition)
           * If in "unpublish" mode and the direction of the effect is down 
           * (which means this would not be an owner) then we need to check
           * the owner to be sure that it matches the last public revision for
           * this its contentid. If it does not then  return success so that the
           * effect does not stop the transition. 
           */
          if (!isCorrectStateContext(request, result)
                || !isEndpointValidRevision(locator, isOwner))   
          {
             if (!result.hasWarning())
             {
                //these msgs aren't seen by the user, so I didn't i18n them
                result.setWarning(request.getUserLocale(),
                      IPSServerErrors.RAW_DUMP, new String[] {
                      "Skip: invalid endpoint rev"});
                result.setRecurseDependents(false);
             }
             return; // not interested            
          }
         
         Document workflowStatus = PSEffectUtils.getWorkflowState(request,
               locator.getId(), m_name);
         
         if (isItemInDesiredWFState(workflowStatus.getDocumentElement()))
         {
            //return with success if the other end is already there
            result.setSuccess();
            return;
         }

         /*
          * The dependent item is not in the desired state; if the force
          * transition is set to 'no' we must return with an error.
          */
         if(!forceTransition)
         {
            String[] args = { m_name, "" + locator.getId(), getModeName()};
            result.setError(request.getUserLocale(),
               IPSExtensionErrors.DEPENDENT_ITEM_NOT_IN_DESIRED_STATE, args);
            return;
         }

         String realTransitionName = getNamedTransition(request,
            workflowStatus, transitionName);

         if(realTransitionName == null)
         {
            String[] args = {m_name, getModeName()};
            result.setError(request.getUserLocale(),
               IPSExtensionErrors.DEPENDENT_ITEM_CANNOT_GOTO_DESIRED_STATE, args);
            return;
         }

         result.setSuccess();
      }
      catch(PSNotFoundException nfe)
      {
         result.setError(request.getUserLocale(),
            IPSExtensionErrors.MISSING_INTERNAL_REQUEST_RESOURCE,
            nfe.getErrorArguments());
      }
      catch(PSInternalRequestCallException irce)
      {
         /* Rare possibilty, I18n? */
         result.setError(irce);
      }
   }

   /**
    * Queries the derived class by calling
    * {@link #isTransitioningIntoTriggerState(IPSRequestContext, boolean, 
    * boolean, boolean, PSEffectResult)} to determine if the target state of the
    * current transition is appropriate for the derived class.
    * 
    * @param request The ctx supplied to the main methods. Assumed not
    * <code>null</code>.
    * @param result Passed on to the aforementioned method. Assumed not
    * <code>null</code>.
    * @return <code>true</code> if the current effect should continue with its
    * processing, given the supplied state, <code>false</code> means the
    * current effect does not care.
    * 
    * @throws PSNotFoundException If required resources (apps) cannot be found.
    * @throws PSInternalRequestCallException If any failures while making
    * requests to obtain needed meta data.
    */
   private boolean isCorrectStateContext(IPSRequestContext request,
         PSEffectResult result)
      throws PSNotFoundException, PSInternalRequestCallException
   {
      //are we moving into public state or out of the public state?
      boolean isCurrentlyPublic =
         PSWorkFlowUtils.isInPublicState(request);

      boolean isToPublic = PSWorkFlowUtils.toPublicState(request);

      boolean isToOutOfPublic = (isCurrentlyPublic && !isToPublic);

      return isTransitioningIntoTriggerState(request, isCurrentlyPublic,
         isToPublic, isToOutOfPublic, result);
   }
   
   /**
    * Helper method to get the non-active end point locator for the relationship.
    * @param relationship relationship for which the non-active locator is to be
    * extracted, assumed not <code>null</code>.
    * @param context execution context, assumed not <code>unll</code>.
    * @return locator for the non-active end point of the relationship,
    * never <code>null</code>.
    */
   private PSLocator getOtherEnd(PSRelationship relationship,
      IPSExecutionContext context)
   {
      if(context.getActivationEndPoint()==IPSExecutionContext.RS_ENDPOINT_OWNER)
         return relationship.getDependent();
      return relationship.getOwner();
   }
   
   /**
    * Checks if the end point has a valid revision, meaning its revision is
    * the last public revision of this content item.
    * If in "unpublish" mode and the direction of the effect is down 
    * (which means this end point is the owner and not the active end point of 
    * this relationship)
    * then we need to check the end point to be sure that it matches the last 
    * public revision for this its contentid. If it does not then the owner
    * is not considered valid. 
    * 
    * @param endpoint the locator for the end point, assumed not <code>null</code>
    * @param isEndpointActive flag indicating if the end point is the active end 
    * of the relationship
    * @return <code>true</code> if this is a valid end point
    */
   private boolean isEndpointValidRevision(
      PSLocator endpoint,
      boolean isEndpointActive)
   {
      if(!isEndpointActive && getModeName().equals(MODE_UNPUBLISH))
      {
         String contentid = endpoint.getPart(PSLocator.KEY_ID);
         String revision = endpoint.getPart(PSLocator.KEY_REVISION);
         String lastPublicRev =
            PSMacroUtils.getLastPublicRevision(contentid);
         if(lastPublicRev.equals("-1") || !lastPublicRev.equals(revision))
         {
            return false;
         }
      }
      return true;
   }

   /**
    * Attempts to move dependent or owner item to the desired WF state, which
    * depending on the derived class could be publish or unpublish.
    * 
    * @see PSEffect#attempt(Object[], IPSRequestContext, IPSExecutionContext,
    *      PSEffectResult) for more information.
    */
   @SuppressWarnings("unused")
   public void attempt(Object[] params, IPSRequestContext request,
      IPSExecutionContext context, PSEffectResult result)
      throws PSExtensionProcessingException, PSParameterMismatchException
   {
      String forceDependent = request.getParameter(
         IPSHtmlParameters.SYS_FORCEDEPENDENT, "");
      if (forceDependent.equals(String.valueOf(true)))
      {
         String[] args = {m_name};
         result.setWarning(request.getUserLocale(),
            IPSExtensionErrors.ITEM_NOT_IN_PUBLIC_STATE, args);
         return;
      }

      //params are already validated in test(). no need to do again.
      boolean forceTransition = params[0].toString().equalsIgnoreCase("yes");

      String trNameOwner = "";
      String trNameDependent = "";

      if(params.length > 1 && params[1] != null)
         trNameOwner = params[1].toString();
      if(params.length > 2 && params[2] != null)
         trNameDependent = params[2].toString();

      boolean isOwner =
         (context.getActivationEndPoint()==IPSExecutionContext.RS_ENDPOINT_OWNER);

      String transitionName = isOwner ? trNameOwner : trNameDependent;
      
      PSRelationship relationship = context.getCurrentRelationship();
      PSLocator locator = getOtherEnd(relationship, context);
      
      try
      {
         /* (This comment applies to the 2nd condition)
          * If in "unpublish" mode and the direction of the effect is down 
          * (which means this would not be an owner) then we need to check
          * the owner to be sure that it matches the last public revision for
          * this its contentid. If it does not then  return success so that the
          * effect does not stop the transition. 
          */
         if (!isCorrectStateContext(request, result)
               || !isEndpointValidRevision(locator, isOwner))   
         {
            result.setSuccess();
            return;            
         }
         
         Document workflowStatus = PSEffectUtils.getWorkflowState(request,
               locator.getId(), m_name);
         
         if (isItemInDesiredWFState(workflowStatus.getDocumentElement()))
         {
            //return with success if the active end is already where we want it
            result.setSuccess();
            return;
         }


         /*
          * The dependent item is not in the desired state; if the force
          * transition is set to 'no' we must return with an error.
          */
         if(!forceTransition)
         {
            String[] args = { m_name, "" + locator.getId(), getModeName()};
            result.setError(request.getUserLocale(),
               IPSExtensionErrors.DEPENDENT_ITEM_NOT_IN_DESIRED_STATE, args);
            return;
         }

         String realTransitionName =
            getNamedTransition(request, workflowStatus, transitionName);

         if(realTransitionName == null)
         {
            String[] args = {m_name, getModeName()};
            result.setError(request.getUserLocale(),
               IPSExtensionErrors.DEPENDENT_ITEM_CANNOT_GOTO_DESIRED_STATE, args);
            return;
         }

         String resource = PSCms.getNewRequestResource(request, locator);

         transit(result, request, locator, resource, realTransitionName, true);
      }
      catch (PSException e)
      {
         throw new PSExtensionProcessingException(
            IPSCmsErrors.UNEXPECTED_ERROR, e.getLocalizedMessage());
      }
   }

   /**
    * Not implemented.
    * @see PSEffect for more information.
    */
   @SuppressWarnings("unused")  //cover all params and ext
   public void recover(Object[] params, IPSRequestContext request,
      IPSExecutionContext context, PSExtensionProcessingException e,
      PSEffectResult result)
      throws PSExtensionProcessingException
   {
      /**@todo: Implement this com.percussion.relationship.IPSEffect method*/
      // no-op
   }

   /**
    * Attempts a forced transition using a given transitionName.
    * @param request current request context, assumed not <code>null</code>.
    * @param wfStatusDoc WF status document, assumed not <code>null</code>.
    * @param transitionName named transition, assumed not <code>null</code>.
    * @return the transition to be used for the force action, <code>null</code>
    * if no default transition or named transition is found.
    */
   private String getNamedTransition(IPSRequestContext request,
      Document wfStatusDoc, String transitionName)
      throws PSNotFoundException, PSInternalRequestCallException
   {
      String transition = null;
      Element elem = wfStatusDoc.getDocumentElement();
      if (elem != null)
      {
         String wfStateId = elem.getAttribute("stateId");
         String workflowId = elem.getAttribute("workflowId");

         transition = getForceTransition(request,
            wfStateId, workflowId, transitionName);

      }

      return transition;
   }

   /**
    * Performs the supplied workflow transition.
    * 
    * @param result The effect result, assumed not <code>null</code>, used to
    * set the status based on the result sof the transition. 
    * @param request the request to operate with, assumed not <code>null</code>.
    * @param item the item that needs to be transitioned, assumed not
    *    <code>null</code>.
    * @param resource the content editor resource, assumed not <code>null</code>
    *    or empty.
    * @param transition the transition trigger, assumed not <code>null</code>
    *    or empty.
    * @param forceDependent <code>true</code> if this is a transition that
    *    forces a dependent to public, <code>false</code> otherwise.
    * @throws PSCmsException if anything goes wrong.
    */
   private void transit(PSEffectResult result, IPSRequestContext request, 
      PSLocator item, String resource, String transition, 
      boolean forceDependent) throws PSCmsException
   {
      try
      {
         Map<String, String> params = new HashMap<>();
         params.put(IPSHtmlParameters.SYS_COMMAND,
            PSWorkflowCommandHandler.COMMAND_NAME);
         params.put(IPSConstants.DEFAULT_ACTION_TRIGGER_NAME, transition);
         params.put(IPSHtmlParameters.SYS_CONTENTID,
            Integer.toString(item.getId()));

         if (forceDependent)
            params.put(IPSHtmlParameters.SYS_FORCEDEPENDENT,
               String.valueOf(forceDependent));

         IPSInternalRequest ir = request.getInternalRequest(
            resource, params, false);
         if (ir == null)
         {
            String[] args = {m_name, resource,};
            throw new PSNotFoundException(request.getUserLocale(),
               IPSExtensionErrors.MISSING_INTERNAL_REQUEST_RESOURCE, args);
         }
         ir.performUpdate();
         String validationErrMsg = ir.getRequestContext().getParameter(
            IPSHtmlParameters.SYS_VALIDATION_ERROR);
         if (validationErrMsg != null && validationErrMsg.trim().length() > 0)
         {
            Object[] args = new Object[] {transition, String.valueOf(
               item.getId())}; 
            result.setError(request.getUserLocale(), 
               IPSExtensionErrors.MANDATORY_TRANSITION_VALIDATION_FAILURE, 
               args);
         }
         else
            result.setSuccess();
      }
      catch (PSException e)
      {
         throw new PSCmsException(e.getErrorCode(), e.getErrorArguments());
      }
   }


   /**
    * Tests whether or not the state of the supplied workflow has a 'Default'
    * transition with the same name as the provided workflow action.
    *
    * @param request the request to operate on, assumed not <code>null</code>.
    * @param stateId the state id of the state to test, assumed not
    *    <code>null</code>.
    * @param workflowId the workflow id for teh workflow in which to do the
    *    test, assuemd not <code>null</code>.
    * @param wfAction the workflow action name to do the test for, assumed
    *    not <code>null</code>.
    * @return the transition to be used for the force action, <code>null</code>
    *    if no default transition or named transition is found.
    * @throws PSInternalRequestCallException if any error occurs processing
    *    the internal request call.
    * @throws PSNotFoundException if a required resource cannot be found.
    */
   private String getForceTransition(IPSRequestContext request,
      String stateId, String workflowId, String wfAction)
      throws PSInternalRequestCallException, PSNotFoundException
   {
      String resource = SYS_PSXRELATIONSHIPSUPPORT + "/" + GET_TRANSITIONS;

      Map<String, String> params = new HashMap<>();
      params.put(IPSHtmlParameters.SYS_WORKFLOWID, workflowId);
      params.put(IPSConstants.DEFAULT_NEWSTATEID_NAME, stateId);
      IPSInternalRequest ir =
         request.getInternalRequest(resource, params, false);
      if (ir != null)
      {
         Document doc = ir.getResultDoc();
         NodeList transitions = doc.getElementsByTagName("Transition");
         
         boolean lookForNamedTransition = true;
         if(wfAction == null || wfAction.trim().length()<1)
            lookForNamedTransition = false;
         
         for (int i=0; transitions!=null && transitions.item(i) != null; i++)
         {
            Element transition = (Element) transitions.item(i);
            String transitionName = transition.getAttribute("name");

            if(lookForNamedTransition)
            {
               if (transitionName.equals(wfAction))
                  return transitionName;
            }
            else
            {
               if (transition.getAttribute("isDefault").equalsIgnoreCase("y"))
                  return transitionName;
            }
         }
      }
      else
      {
         String[] args = {m_name, resource,};
         throw new PSNotFoundException(request.getUserLocale(),
            IPSExtensionErrors.MISSING_INTERNAL_REQUEST_RESOURCE, args);
      }

      return null;
   }

   /**
    * The name of the application used to query or update relationships in
    * the repository.
    */
   protected static final String SYS_PSXRELATIONSHIPSUPPORT =
      "sys_psxRelationshipSupport";

   /**
    * The name of the translations query resource.
    */
   protected static final String GET_TRANSLATIONS = "getTranslations";

   /**
    * The name of the query resource to get the workflow status of the current
    * item.
    */
   protected static final String GET_WORKFLOWSTATUS = "getWorkflowStatus";

   /**
    * The name of the query resource to get all transitions leaving the
    * current state.
    */
   protected static final String GET_TRANSITIONS = "getTransitions";

   /**
    * The name of the query resource to get the current workflow state of an
    * item.
    */
   protected static final String GET_CURRENTSTATE = "getCurrentState";
   
   /**
    * Publish mode for this effect
    */
   protected static final String MODE_PUBLISH = "publish";
   
   /**
    * Unpublish mode for this effect
    */
   protected static final String MODE_UNPUBLISH = "unpublish";
}
