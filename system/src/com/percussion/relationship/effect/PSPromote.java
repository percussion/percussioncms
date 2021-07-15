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
package com.percussion.relationship.effect;

import com.percussion.cms.IPSCmsErrors;
import com.percussion.cms.IPSConstants;
import com.percussion.cms.PSCmsException;
import com.percussion.cms.handlers.PSWorkflowCommandHandler;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.cms.objectstore.server.PSRelationshipDbProcessor;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSRelationshipSet;
import com.percussion.error.PSException;
import com.percussion.extension.IPSExtensionErrors;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.log.PSLogManager;
import com.percussion.log.PSLogServerWarning;
import com.percussion.relationship.IPSEffect;
import com.percussion.relationship.IPSExecutionContext;
import com.percussion.relationship.PSEffect;
import com.percussion.relationship.PSEffectResult;
import com.percussion.relationship.PSRelationshipProcessorException;
import com.percussion.server.IPSInternalRequest;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.IPSServerErrors;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSCms;
import com.percussion.util.PSRelationshipUtils;
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
 * If the item being processed is a Promotable item that is entering a public
 * state for the first time, then the following actions will be performed:
 * <pre>
 * 1. Transition the original item using a default or a specified workflow
 *    transition, which, for example, moves original item from public to
 *    archive state.
 * 2. Checkout then checkin orig. item to bump up the revision 
 * 3. Set dependent of all inbound relations of the original item to point
 *    to the new PV item.
 * 4. Set the new PV item as the owner of all outbound relations except
 *    for clonable relationships, which are removed.
 * </pre>
 *
 * If the above sequence fails at any stage, then the error is returned.
 *
 * The effect will return immediately for any context except RS_POST_WORKFLOW.
 *
 * <p>This effect takes one parameter as described below:
 * <p>
 * params[0] is the internal name of the transition to use. If not supplied,
 * the first transition with the 'default' property (in alpha order) is used.
 *
 */
public class PSPromote extends PSEffect
{
   /**
    * Override the method in the base class. This effect is meant to be run
    * during RS_POST_WORKFLOW context and hence will return <code>false</code>
    * for all other contexts.
    */
   public void test(Object[] params, IPSRequestContext request,
      IPSExecutionContext context, PSEffectResult result)
   {
      if (!context.isPostWorkflow())
      {
         String[] args = {m_name, "post workflow"};
         result.setWarning(request.getUserLocale(),
            IPSExtensionErrors.ILLEGAL_EXECUTION_CONTEXT, args);
         return;
      }

      PSRelationship relationship = context.getCurrentRelationship();

      if (!relationship.getConfig().isPromotable())
      {
         String[] args = {m_name, relationship.getConfig().getName()};
         result.setWarning(request.getUserLocale(),
            IPSExtensionErrors.NONPROMOTABLE_RELATIONSHIP, args);
         return;
      }

      PSLocator depLocator = relationship.getDependent();
      PSLocator ownerLocator = relationship.getOwner();

      try
      {
         Object wfid = request.getParameter(IPSHtmlParameters.SYS_WORKFLOWID);

         if (wfid == null)
         {
            String[] args = {m_name};
            result.setWarning(request.getUserLocale(),
               IPSExtensionErrors.WORKFLOWID_IN_REQUEST_ISNULL, args);
            return;
         }

         String wfAction = request.getParameter(
            IPSConstants.DEFAULT_ACTION_TRIGGER_NAME, "").trim();

         // ignore invalid actions
         if (wfAction == null || wfAction.length() == 0)
         {
            wfAction = (wfAction==null)?"null":wfAction;
            String[] args = {m_name, wfAction};
            result.setWarning(request.getUserLocale(),
               IPSExtensionErrors.INVALID_WORKFLOW_ACTION, args);
            return;
         }

         // ignore checkins
         if (wfAction.equalsIgnoreCase(IPSConstants.TRIGGER_CHECKIN))
         {
            String[] args = {m_name, wfAction};
            result.setWarning(request.getUserLocale(),
               IPSExtensionErrors.INVALID_WORKFLOW_ACTION, args);
            return;
         }

         // ignore checkouts
         if (wfAction.equalsIgnoreCase(IPSConstants.TRIGGER_CHECKOUT))
         {
            String[] args = {m_name, wfAction};
            result.setWarning(request.getUserLocale(),
               IPSExtensionErrors.INVALID_WORKFLOW_ACTION, args);
            return;
         }

         // see if both original and dependent items are in a public state
         if (!PSWorkFlowUtils.isInPublicState(request, depLocator.getId()) ||
             !PSWorkFlowUtils.isInPublicState(request, ownerLocator.getId()))
         {
            String[] args = {m_name};
            result.setWarning(request.getUserLocale(),
               IPSExtensionErrors.ITEM_NOT_IN_PUBLIC_STATE, args);
            return;
         }

         result.setSuccess();
      }
      catch (PSException e)
      {
         result.setError(e);
      }
   }

   /**
    * Implements {@link IPSEffect.attempt(Object[], IPSRequestContext,
    *  IPSExecutionContext, PSEffectResult)}.
    * <pre>
    * See class descr. for more details.
    * </pre>
    *
    * The result is communicated back to the engine by setting success or error
    * on the supplied result object.
    *
    * @exception PSExtensionProcessingException if anything goes wrong executing
    * the attempt and the implementer chooses so. For this case the default
    * implementation calls the recovery method.
    *
    * @throws PSParameterMismatchException never.
    */
   public void attempt(Object[] params, IPSRequestContext request,
      IPSExecutionContext context, PSEffectResult result)
      throws PSExtensionProcessingException, PSParameterMismatchException
   {
      //First parameter, if present is the transition name to take the item from
      //public to a non public state.
      String transitionName = "";
      if (params != null && params.length > 0)
         transitionName = params[0].toString();
      PSRelationship relationship = context.getCurrentRelationship();
      try
      {
         PSLocator ownerLocator = relationship.getOwner();

         String resource = PSCms.getNewRequestResource(request, ownerLocator);

         /*
          * 1. Set dependent of all inbound relations of the original item to point
          *    to the new PV item.
          * 2. Set the new PV item as the owner of all outbound relations except
          *    for clonable relationships, which are removed.
          */
         repointRelationships(request, relationship);
         
         /* transition the original item using the specified transition;
          * ie: transitions original item to the archive state.
          */
         transitionItem(request, ownerLocator, resource,
            getTransition(request, transitionName, ownerLocator), false);

         //tell the engine that we did OK
         result.setSuccess();
      }
      catch (PSException e)
      {
         throw new PSExtensionProcessingException(e.getErrorCode(),
            e.getErrorArguments());
      }
   }

   /**
    * Always returns success. For more info see {@link IPSEffect}.
    *
    * @returns always returns success, never <code>null</code>.
    *
    * @throws PSExtensionProcessingException never.
    */
   public void recover(Object[] params, IPSRequestContext request,
      IPSExecutionContext context, PSExtensionProcessingException e,
      PSEffectResult result)
      throws PSExtensionProcessingException
   {
      result.setSuccess();
   }

   /**
    * Performs the supplied workflow transition.
    *
    * @param request the request context to operate with,
    *    assumed not <code>null</code>.
    * @param item the item that needs to be transitioned,
    *    assumed not <code>null</code>.
    * @param resource the content editor resource,
    *    assumed not <code>null</code> or empty.
    * @param transition the transition trigger,
    *    assumed not <code>null</code> or empty.
    * @param forceDependent <code>true</code> if this is a transition that
    *    forces a dependent to public, <code>false</code> otherwise.
    *
    * @throws PSExtensionProcessingException if anything goes wrong.
    */
   private void transitionItem(IPSRequestContext request, PSLocator item,
      String resource, String transition, boolean forceDependent)
      throws PSExtensionProcessingException
   {
      try
      {
         Map params = new HashMap();
         params.put(IPSHtmlParameters.SYS_COMMAND,
            PSWorkflowCommandHandler.COMMAND_NAME);
         params.put(IPSConstants.DEFAULT_ACTION_TRIGGER_NAME, transition);
         params.put(IPSHtmlParameters.SYS_CONTENTID,
            Integer.toString(item.getId()));
         params.put(IPSHtmlParameters.SYS_REVISION,
            Integer.toString(item.getRevision()));

         if (forceDependent)
            params.put(IPSHtmlParameters.SYS_FORCEDEPENDENT,
               String.valueOf(forceDependent));

         IPSInternalRequest ir = request.getInternalRequest(
            resource, params, false);

         if (ir == null)
         {
            Object[] args =
            {
               resource,
               "No request handler found."
            };

            throw new PSExtensionProcessingException(
               IPSServerErrors.MISSING_INTERNAL_REQUEST_RESOURCE, args);
         }

         ir.performUpdate();
         String errMsg = ir.getRequestContext().getParameter(
               IPSHtmlParameters.SYS_VALIDATION_ERROR);
         if (null != errMsg && errMsg.trim().length() > 0)
         {
            //validation failed, print message
            String[] args = 
            {
               "" + item.getId() + ":" + item.getRevision(),
               transition,
               errMsg
            };
            PSLogManager.write(new PSLogServerWarning(
                  IPSExtensionErrors.PROMOTE_TRANSITION_FAILED, args, true, 
                  null));
         }        
      }
      catch (PSException e)
      {
         throw new PSExtensionProcessingException(e.getErrorCode(),
            e.getErrorArguments());
      }
   }

   /**
    * Get the transition to be used to move the original item of a promotable
    * relationship out of its public state.
    *
    * @param request the request to operate with, assumed not <code>null</code>.
    * @param relationship the relationship for which to get the transition,
    *    this assumes that the promotable flag is enabled.
    * @param locator the locator of the item used to get the current state and 
    *    then the default transition from that state. Assumed not 
    *    <code>null</code>.
    * @return the transition to used, never <code>null</code> or empty.
    * @throws PSRelationshipProcessorException if anything goes wrong.
    */
   private String getTransition(IPSRequestContext request,
      String suppliedTransitionName, PSLocator locator)
      throws PSExtensionProcessingException, PSNotFoundException
   {
      if (request == null)
         throw new IllegalArgumentException("request cannot be null");

      try
      {
         String result = suppliedTransitionName;
         
         //Default transitio name is supplied, return that.
         if (result != null && result.length() > 0)
            return result;
            
         //Else walk through all transitions out of current state to locate the 
         //default one
         
         //Let us get the current workflow and state ids for the item
         String resource = PSRelationshipUtils.SYS_PSXRELATIONSHIPSUPPORT
            + "/" + PSRelationshipUtils.GET_CURRENTSTATE;

         Map params = new HashMap();
         params.put(IPSHtmlParameters.SYS_CONTENTID, "" + locator.getId());
         
         IPSInternalRequest ir = request.getInternalRequest(
            resource, params, false);
         String curWfId = "";
         String curStateId = "";
         if (ir != null)
         {
            Document doc = ir.getResultDoc();
            if (doc != null)
            {
               curWfId = doc.getDocumentElement().getAttribute("workflowId");
               curStateId = doc.getDocumentElement().getAttribute("stateId");
            }
         }
         else
         {
            Object[] args =
            {
               resource,
               "No request handler found."
            };
            throw new PSNotFoundException(
               IPSServerErrors.MISSING_INTERNAL_REQUEST_RESOURCE, args);
         }
         //Current workflow and state ids cannot be empty unless there is a 
         //serious problem with the item

         //Now let us get the transition and locate the default one.
         resource = PSRelationshipUtils.SYS_PSXRELATIONSHIPSUPPORT
            + "/" + PSRelationshipUtils.GET_TRANSITIONS;

         params.clear();
         params.put(IPSHtmlParameters.SYS_WORKFLOWID, curWfId);
         params.put(IPSConstants.DEFAULT_NEWSTATEID_NAME, curStateId);

         ir = request.getInternalRequest(resource, params, false);
         if (ir != null)
         {
            boolean found = false;
            Document doc = ir.getResultDoc();
            NodeList transitions = doc.getElementsByTagName("Transition");

            for (int i=0; transitions!=null && !found &&
                  (transitions.item(i) != null); i++)
            {
               Element transition = (Element) transitions.item(i);
               if (transition.getAttribute("isDefault").equalsIgnoreCase("y"))
               {
                  result = transition.getAttribute("name");
                  found = true;
                  break;
               }
            }

            if (!found)
            {
               Object[] args =
               {
                  request.getParameter(
                     IPSHtmlParameters.SYS_WORKFLOWID,
                     curWfId),
                     request.getParameter(
                        IPSConstants.DEFAULT_NEWSTATEID_NAME,
                        curStateId)
               };
               throw new PSExtensionProcessingException(
                  IPSCmsErrors.UNDEFINED_DEFAULT_TRANSITION, args);
            }
         }
         else
         {
            Object[] args =
            {
               resource,
               "No request handler found."
            };
            throw new PSNotFoundException(
               IPSServerErrors.MISSING_INTERNAL_REQUEST_RESOURCE, args);
         }

         return result;
      }
      catch (PSException e)
      {
         throw new PSExtensionProcessingException(e.getErrorCode(),
            e.getErrorArguments());
      }
   }

   /**
    * This method does the following:
    * 1. Set dependent of all inbound relations of the original item to point
    *    to the new PV item.
    * 2. Set the new PV item as the owner of all outbound relations except
    *    for clonable relationships, which are removed.
    *
    * @param request the request used to perform the changes, assumed not
    *    <code>null</code>.
    * @param relationship the promotable relationship for which to perform the
    *    move, assumed not <code>null</code>.
    * @throws PSCmsException for any errors performing the move.
    */
   private void repointRelationships(IPSRequestContext request,
      PSRelationship relationship) throws PSCmsException
   {
      PSLocator pvOriginalItem = relationship.getOwner();
      PSLocator pvDependentItem = relationship.getDependent();

      //get locator with a current revision from contentstatus table 
      PSComponentSummary depSummary = getItemSummary(request, pvDependentItem);
      pvDependentItem = depSummary.getCurrentLocator();

      PSRelationshipSet relationshipsToModify = new PSRelationshipSet();
      PSRelationshipSet relationshipsToDelete = new PSRelationshipSet();

      PSRelationshipDbProcessor processor = PSRelationshipDbProcessor.getInstance();

      //get all outbound relationships (orig item is the owner item)
      PSRelationshipFilter filter = new PSRelationshipFilter();
      filter.setOwnerId(pvOriginalItem.getId());
      PSRelationshipSet outboundRelationships = processor.getRelationships(filter);

      /*
       * repoint outbound relationships by making the PV item
       * a new owner of each of the found outbound relationships.
       */
      Iterator itOutboundRel = outboundRelationships.iterator();
      while (itOutboundRel.hasNext())
      {
         PSRelationship rs = (PSRelationship) itOutboundRel.next();
                  
         if (rs.getConfig().isPromotable())
            continue; //do not touch promotable relations
         
         if (!rs.getConfig().isCloningAllowed())
         {
            //repoint non clonable (ie: AA)
            rs.setOwner(new PSLocator(pvDependentItem.getId(),
                  pvDependentItem.getRevision()));
            
            relationshipsToModify.add(rs);
         }
         else
         {
            //delete others
            relationshipsToDelete.add(rs);
            continue;
         }   
      }

      //get all inbound relationships (orig item is a dependent item)
      filter = new PSRelationshipFilter();
      filter.setDependentId(pvOriginalItem.getId());
      PSRelationshipSet inboundRelationships = processor.getRelationships(filter);

      /*
       * repoint inbound relationships by making the PV a dependent
       * item of each of the found inbound relationships. 
       */
      Iterator itInboundRel = inboundRelationships.iterator();
      while (itInboundRel.hasNext())
      {
         PSRelationship rs = (PSRelationship) itInboundRel.next();

         if (rs.isPromotable() || rs.isSkipPromotion())
            continue; //do not touch promotable relations
            
         rs.setDependent(new PSLocator(pvDependentItem.getId(),
            pvDependentItem.getRevision()));
            
         relationshipsToModify.add(rs);
      }
      
      PSRelationshipSet removeThese =
         removeRedundentRelationships(processor, pvOriginalItem, inboundRelationships);
      // Remove any relationships that are redundent (and hence will not
      // be moved from the original item to the PV)
      relationshipsToDelete.addAll(removeThese);
     
      //modify relationships if any
      processor.modifyRelationships(relationshipsToModify);
      
      //delete relationships if any
      processor.deleteRelationships(relationshipsToDelete);
   }

   /**
    * Remove any relationships from the set that already exist for the promotable
    * item. This is done by first getting the relationships for the promotable
    * item. These are then removed from the set of relationships to be modified.
    * Note that this depends on the list being passed in already having been
    * modified so that the relationships reference the new promotable item
    * 
    * @param processor relationship processor, assumed never <code>null</code>
    * @param pvDependentItem the dependent item locator, never <code>null</code>
    * @param relationshipsToModify the set of relationships for modification
    * @return any relationships that should be removed from the original
    * item, may return empty but never <code>null</code>
    * @throws PSCmsException
    */
   PSRelationshipSet removeRedundentRelationships(
         PSRelationshipDbProcessor processor, 
         PSLocator pvDependentItem,
         PSRelationshipSet relationshipsToModify) throws PSCmsException
   {
      PSRelationshipSet removeThese = new PSRelationshipSet();
      
      PSRelationshipFilter filter = new PSRelationshipFilter();
      filter.setDependentId(pvDependentItem.getId());
      PSRelationshipSet existing = processor.getRelationships(filter);
      Iterator iter = relationshipsToModify.iterator();
      while(iter.hasNext())
      {
         PSRelationship rel = (PSRelationship) iter.next();
         if (existing.contains(rel))
         {
            removeThese.add(rel);
         }
      }
      // Remove from set
      relationshipsToModify.removeAll(removeThese);
      
      return removeThese;
   }

   /**
    * Returns item summaries given a locator. 
    * @param request request, assumed never <code>null</code>.
    * @param locator locator, assumed never <code>null</code>.
    * @return component summary, never <code>null</code>.
    * @throws PSCmsException if anything goes wrong.
    */
   static public PSComponentSummary getItemSummary(
      IPSRequestContext request,
      PSLocator locator)
      throws PSCmsException
   {
      IPSCmsObjectMgr cms = PSCmsObjectMgrLocator.getObjectManager();
      PSComponentSummary summary = cms.loadComponentSummary(locator.getId());
      
      if (summary == null)
      {
         throw new PSCmsException(0,
            "Could not get locator for contentid= " + locator.getId());
      }

      return summary;
   }
}
