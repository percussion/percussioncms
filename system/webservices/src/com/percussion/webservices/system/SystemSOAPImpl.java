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
package com.percussion.webservices.system;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.services.guidmgr.PSGuidUtils;
import com.percussion.services.guidmgr.data.PSDesignGuid;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.system.data.PSContentStatusHistory;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.IPSWebserviceErrors;
import com.percussion.webservices.PSBaseSOAPImpl;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.PSErrorsException;
import com.percussion.webservices.PSInvalidLocaleException;
import com.percussion.webservices.PSUserNotMemberOfCommunityException;
import com.percussion.webservices.PSWebserviceErrors;
import com.percussion.webservices.PSWebserviceUtils;
import com.percussion.webservices.faults.PSContractViolationFault;
import com.percussion.webservices.faults.PSErrorResultsFault;
import com.percussion.webservices.faults.PSErrorsFault;
import com.percussion.webservices.faults.PSInvalidLocaleFault;
import com.percussion.webservices.faults.PSInvalidSessionFault;
import com.percussion.webservices.faults.PSNotAuthorizedFault;
import com.percussion.webservices.faults.PSUnknownRelationshipTypeFault;
import com.percussion.webservices.faults.PSUseSpecificMethodsFault;
import com.percussion.webservices.faults.PSUserNotMemberOfCommunityFault;

import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

/**
 * Server side implementations for web services defined in
 * <code>rhythmyx.wsdl</code> for operations defined in the
 * <code>systemSOAP</code> bindings.
 */
public class SystemSOAPImpl extends PSBaseSOAPImpl 
   implements com.percussion.webservices.system.System
{
   /*
    * (non-Javadoc)
    * 
    * @see System#createRelationship(CreateRelationshipRequest)
    */
   public CreateRelationshipResponse createRelationship(
      CreateRelationshipRequest req)
      throws RemoteException, PSUseSpecificMethodsFault, PSInvalidSessionFault,
      PSUnknownRelationshipTypeFault, PSContractViolationFault
   {
      try
      {
         authenticate();

         // get data from request
         PSLegacyGuid ownerId = new PSLegacyGuid(req.getOwnerId());
         PSLegacyGuid dependentId = new PSLegacyGuid(req.getDependentId());
         String configName = req.getName();

         // create and save relationships
         IPSSystemWs service = PSSystemWsLocator.getSystemWebservice();
         com.percussion.design.objectstore.PSRelationship relationship = 
            service.createRelationship(configName, ownerId, dependentId);
         
         // convert the saved relationships
         PSRelationship result = (PSRelationship) convert(
               PSRelationship.class, relationship);
         
         return new CreateRelationshipResponse(result);
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, "createRelationship");
      }
      catch (PSErrorException e)
      {
         throw new RemoteException(e.getLocalizedMessage());
      }

      return null;   
   }

   /*
    * (non-Javadoc)
    * 
    * @see System#deleteRelationships(long[])
    */
   public void deleteRelationships(long[] ids)
      throws RemoteException, PSInvalidSessionFault, PSErrorsFault,
      PSContractViolationFault
   {
      String serviceName = "deleteRelationships";
      try
      {
         authenticate();

         List<IPSGuid> idList = PSGuidUtils.toGuidList(ids);
         IPSSystemWs service = PSSystemWsLocator.getSystemWebservice();
         service.deleteRelationships(idList);
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, serviceName);
      }
      catch (PSErrorsException e)
      {
         handleErrorsException(e, serviceName);
      }      
      catch (PSErrorException e)
      {
         throw new RemoteException(e.getLocalizedMessage());
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see System#findChildren(FindChildrenRequest)
    */
   public FindDependentsResponse findDependents(FindDependentsRequest request)
      throws RemoteException, PSInvalidSessionFault, PSContractViolationFault
   {
      authenticate();
      
      try
      {
         // converts the request data
         PSRelationshipFilter filter;
         PSLegacyGuid ownerId;
         if (request.getPSRelationshipFilter() != null)
         {
            request.getPSRelationshipFilter().setOwner(request.getId());
            filter = PSWebserviceUtils.getRelationshipFilter(
               request.getPSRelationshipFilter());
            // the revision of the owner may have been modified if it was -1
            // get the owner id from the filter
            ownerId = new PSLegacyGuid(filter.getOwner());
         }
         else
         {
            filter = null;
            ownerId = new PSLegacyGuid(request.getId());
         }

         // find the dependents
         IPSSystemWs service = PSSystemWsLocator.getSystemWebservice();
         List<IPSGuid> ids = service.findDependents(ownerId, filter);

         return new FindDependentsResponse(PSWebserviceUtils
               .getLongsFromGuids(ids));
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, "findDependents");
      }
      catch (PSErrorException e)
      {
         throw new RemoteException(e.getLocalizedMessage());
      }
      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see System#findParents(FindParentsRequest)
    */
   public FindOwnersResponse findOwners(FindOwnersRequest request)
      throws RemoteException, PSInvalidSessionFault, PSContractViolationFault
   {
      authenticate();
      
      try
      {
         // converts the request data
         PSRelationshipFilter filter = PSWebserviceUtils.getRelationshipFilter(
               request.getPSRelationshipFilter());
         PSLegacyGuid dependentId = new PSLegacyGuid(request.getId());

         // finds the owners
         IPSSystemWs service = PSSystemWsLocator.getSystemWebservice();
         List<IPSGuid> ids = service.findOwners(dependentId, filter);

         return new FindOwnersResponse(PSWebserviceUtils.getLongsFromGuids(ids));
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, "findOwners");
      }
      catch (PSErrorException e)
      {
         throw new RemoteException(e.getLocalizedMessage());
      }
      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see System#loadAuditTrails(long[])
    */
   public PSAuditTrail[] loadAuditTrails(long[] ids)
      throws RemoteException, PSErrorResultsFault, PSInvalidSessionFault,
      PSContractViolationFault
   {
      authenticate();
      
      try
      {
         List<IPSGuid> idList = PSWebserviceUtils.getLegacyGuidFromLong(ids);

         IPSSystemWs service = PSSystemWsLocator.getSystemWebservice();
         Map<IPSGuid, List<PSContentStatusHistory>> auditTrails = 
            service.loadAuditTrails(idList);

         return convertAuditTrails(idList, auditTrails);
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, "loadAuditTrails");
      }
      catch (PSErrorException e)
      {
         throw new RemoteException(e.getLocalizedMessage());
      }
      return null;
   }

   /**
    * Converts a list of content histories to audit trail objects.
    * 
    * @param idList the id list, assumed matches map key and not 
    *   <code>null</code> or empty. 
    * @param auditTrails the to be converted content histories, assumed not
    *    <code>null</code> or empty.
    * 
    * @return the converted list in the order of id list, never 
    *   <code>null</code> or empty.
    */
   private PSAuditTrail[] convertAuditTrails(List<IPSGuid> idList,
      Map<IPSGuid, List<PSContentStatusHistory>> auditTrails) 
   {
      PSAuditTrail[] result = new PSAuditTrail[auditTrails.size()];
      for (int i=0; i < result.length; i++)
      {
         IPSGuid id = idList.get(i);
         List<PSContentStatusHistory> hists = auditTrails.get(id);
         PSAudit[] audits = new PSAudit[hists.size()];
         for (int j=0; j<audits.length; j++)
         {
            audits[j] = getAudit(hists.get(j));
         }
         result[i] = new PSAuditTrail(audits, new PSDesignGuid(id).getValue());
      }
      return result;
   }

   /**
    * Creates an audit instance from a specified content status history.
    * Note, the the transition name will be reseted if the transition id is 
    * <code>0</code>, where the transition name will be set {@link #CHECKIN} 
    * when there is no check out user name; otherwise it will be set to 
    * {@link #CHECKOUT}. 
    * 
    * @param hist the source data, assumed not <code>null</code>.
    * 
    * @return the created audit object, never <code>null</code>.
    */
   private PSAudit getAudit(PSContentStatusHistory hist)
   {
      PSDesignGuid id = new PSDesignGuid(new PSLegacyGuid(hist.getContentId(), 
         hist.getRevision()));
      Calendar eventTime = Calendar.getInstance();
      eventTime.setTime(hist.getEventTime());
      
      String label = hist.getTransitionLabel();
      if (hist.getTransitionId() == 0)
      {
         if (StringUtils.isBlank(hist.getCheckoutUserName()))
            label = CHECKIN;
         else
            label = CHECKOUT;
      }
      return new PSAudit(
            id.getValue(),
            hist.getRevision(),
            eventTime,
            hist.getActor(),
            hist.getStateId(),
            hist.getStateName(),
            hist.getTransitionId(),
            label,
            hist.getTransitionComment(),
            hist.isValid());
   }
   
   /**
    * The interpreted string for the transition name of the {@link PSAudit}
    * when the transition id is <code>0</code> and the checkout user of the
    * related {@link PSComponentSummary} is <code>null</code> or empty.
    */
   private static String CHECKIN = "Checked in";

   /**
    * The interpreted string for the transition name of the {@link PSAudit}
    * when the transition id is <code>0</code> and the checkout user of the
    * related {@link PSComponentSummary} is not empty.
    */
   private static String CHECKOUT = "Checked out";
   
   /*
    * (non-Javadoc)
    * 
    * @see System#loadRelationships(LoadRelationshipsRequest)
    */
   public PSRelationship[] loadRelationships(
      LoadRelationshipsRequest request)
      throws RemoteException, PSInvalidSessionFault, PSContractViolationFault,
      PSNotAuthorizedFault
   {
      authenticate();
      
      try
      {
         PSRelationshipFilter filter = PSWebserviceUtils.getRelationshipFilter(
               request.getPSRelationshipFilter());

         IPSSystemWs service = PSSystemWsLocator.getSystemWebservice();
         List<com.percussion.design.objectstore.PSRelationship> relationships = 
            service.loadRelationships(filter);
         
         // convert the saved relationships
         PSRelationship[] result = (PSRelationship[]) convert(
               PSRelationship[].class, relationships);
         
         return result;
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, "loadRelationships");
      }
      catch (PSErrorException e)
      {
         throw new RemoteException(e.getLocalizedMessage());
      }

      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see System#loadRelationshipTypes(LoadRelationshipTypesRequest)
    */
   public RelationshipConfigSummary[] loadRelationshipTypes(
      LoadRelationshipTypesRequest req)
      throws RemoteException, PSInvalidSessionFault, PSContractViolationFault,
      PSNotAuthorizedFault
   {
      RelationshipConfigSummary[] result = null;
      try
      {
         authenticate();

         IPSSystemWs service = PSSystemWsLocator.getSystemWebservice();
         
         List<com.percussion.design.objectstore.PSRelationshipConfig> configs = service
               .loadRelationshipTypes(req.getName(),
                     getRelationshipCategory(req.getCategory()));
         
         result = (RelationshipConfigSummary[]) convert(
               RelationshipConfigSummary[].class, configs);
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, "loadRelationshipTypes");
      }
      catch (PSErrorException e)
      {
         // unknown error
         throw new RemoteException(e.getLocalizedMessage());
      }
      
      return result;
   }

   /*
    * (non-Javadoc)
    * 
    * @see System#loadWorkflows(LoadWorkflowsRequest)
    */
   @SuppressWarnings("unused")
   public PSWorkflow[] loadWorkflows(LoadWorkflowsRequest loadWorkflowsRequest)
      throws RemoteException, PSInvalidSessionFault, PSContractViolationFault,
      PSNotAuthorizedFault
   {
      authenticate();

      IPSSystemWs service = 
         PSSystemWsLocator.getSystemWebservice();
      
      List<com.percussion.services.workflow.data.PSWorkflow> wfs = 
         service.loadWorkflows(loadWorkflowsRequest.getName());
      
      PSWorkflow[] result = (PSWorkflow[]) convert(
         PSWorkflow[].class, wfs);
      
      return result;
   }

   /*
    * (non-Javadoc)
    * 
    * @see System#saveRelationships(com.percussion.services.system.data.PSRelationship[])
    */
   @SuppressWarnings("unchecked")
   public void saveRelationships(PSRelationship[] srcRelationships)
      throws RemoteException, PSInvalidSessionFault, PSErrorsFault,
      PSContractViolationFault
   {
      String serviceName = "saveRelationships";
      authenticate();
      
      try
      {
         List<com.percussion.design.objectstore.PSRelationship> relationships = 
            (List<com.percussion.design.objectstore.PSRelationship>) convert(
                  List.class, srcRelationships);
         
         IPSSystemWs service = PSSystemWsLocator.getSystemWebservice();
         
         service.saveRelationships(relationships);
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, serviceName);
      }
      catch (PSErrorsException e)
      {
         handleErrorsException(e, serviceName);
      }            
   }


   /*
    * (non-Javadoc)
    * 
    * @see System#switchCommunity(SwitchCommunityRequest)
    */
   public void switchCommunity(SwitchCommunityRequest switchCommunityRequest)
      throws RemoteException, PSInvalidSessionFault, PSContractViolationFault,
      PSUserNotMemberOfCommunityFault
   {
      String serviceName = "switchCommunity";
      try
      {
         authenticate();

         IPSSystemWs sysSvc = PSSystemWsLocator.getSystemWebservice();
         sysSvc.switchCommunity(switchCommunityRequest.getName());
      }
      catch (IllegalArgumentException e)
      {
         int code = IPSWebserviceErrors.INVALID_CONTRACT;
         throw new PSContractViolationFault(code, 
            PSWebserviceErrors.createErrorMessage(code, serviceName, 
               e.getLocalizedMessage()), ExceptionUtils.getFullStackTrace(e));
      }
      catch (PSUserNotMemberOfCommunityException e)
      {
         throw new PSUserNotMemberOfCommunityFault(e.getCode(), 
            e.getErrorMessage(), ExceptionUtils.getFullStackTrace(e));
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see System#switchLocale(SwitchLocaleRequest)
    */
   public void switchLocale(SwitchLocaleRequest switchLocaleRequest)
      throws RemoteException, PSInvalidSessionFault, PSInvalidLocaleFault,
      PSContractViolationFault
   {
      String serviceName = "switchLocale";
      try
      {
         authenticate();

         IPSSystemWs sysSvc = PSSystemWsLocator.getSystemWebservice();
         sysSvc.switchLocale(switchLocaleRequest.getCode());
      }
      catch (IllegalArgumentException e)
      {
         int code = IPSWebserviceErrors.INVALID_CONTRACT;
         throw new PSContractViolationFault(code, 
            PSWebserviceErrors.createErrorMessage(code, serviceName, 
               e.getLocalizedMessage()), ExceptionUtils.getFullStackTrace(e));
      }
      catch (PSInvalidLocaleException e)
      {
         throw new PSInvalidLocaleFault(e.getCode(), 
            e.getErrorMessage(), ExceptionUtils.getFullStackTrace(e));
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see System#transitionItems(TransitionItemsRequest)
    */
   public TransitionItemsResponse transitionItems(TransitionItemsRequest request)
      throws RemoteException, PSErrorResultsFault, PSInvalidSessionFault,
      PSContractViolationFault
   {
      try
      {
         authenticate();
         
         List<IPSGuid> guids = PSGuidUtils.toLegacyGuidList(request.getId());
         
         IPSSystemWs sysSvc = PSSystemWsLocator.getSystemWebservice();
         List<String> states = sysSvc.transitionItems(guids, request
               .getTransition());
         
         String[] result = new String[states.size()];
         return new TransitionItemsResponse(states.toArray(result));
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, "transitionItems");
      }
      catch (PSErrorsException e)
      {
         handleErrorsException(e, "transitionItems");
      }
      catch (PSErrorException ex)
      {
         throw new RemoteException(ex.getLocalizedMessage());
      }
      return null;
   }

   /* (non-Javadoc)
    * @see com.percussion.webservices.system.System#getAllowedTransitions(long[])
    */
   public GetAllowedTransitionsResponse getAllowedTransitions(long[] ids) 
      throws RemoteException, PSInvalidSessionFault, PSContractViolationFault
   {
      String serviceName = "getAllowedTransitions";
      try
      {
         authenticate();

         IPSSystemWs sysSvc = PSSystemWsLocator.getSystemWebservice();
         Map<String, String> transitions = sysSvc.getAllowedTransitions(
            PSWebserviceUtils.getLegacyGuidFromLong(ids));
         
         GetAllowedTransitionsResponse resp = 
            new GetAllowedTransitionsResponse();
         String[] triggers = new String[transitions.size()];
         String[] labels = new String[transitions.size()];
         int i = 0;
         for (Entry<String, String> entry : transitions.entrySet())
         {
            triggers[i] = entry.getKey();
            labels[i] = entry.getValue();
            i++;
         }
         resp.setTransition(triggers);
         resp.setLabel(labels);
         
         return resp;
      }
      catch (IllegalArgumentException e)
      {
         int code = IPSWebserviceErrors.INVALID_CONTRACT;
         throw new PSContractViolationFault(code, 
            PSWebserviceErrors.createErrorMessage(code, serviceName, 
               e.getLocalizedMessage()), ExceptionUtils.getFullStackTrace(e));
      }
   }
}
