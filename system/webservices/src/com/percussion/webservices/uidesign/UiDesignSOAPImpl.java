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
package com.percussion.webservices.uidesign;

import com.percussion.cms.objectstore.PSAction;
import com.percussion.cms.objectstore.PSSearch;
import com.percussion.services.catalog.IPSCatalogSummary;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.PSGuidUtils;
import com.percussion.services.guidmgr.data.PSDesignGuid;
import com.percussion.services.ui.PSUiException;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSBaseSOAPImpl;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.PSErrorResultsException;
import com.percussion.webservices.PSErrorsException;
import com.percussion.webservices.PSLockErrorException;
import com.percussion.webservices.common.PSObjectSummary;
import com.percussion.webservices.faults.PSContractViolationFault;
import com.percussion.webservices.faults.PSErrorResultsFault;
import com.percussion.webservices.faults.PSErrorsFault;
import com.percussion.webservices.faults.PSInvalidSessionFault;
import com.percussion.webservices.faults.PSNotAuthorizedFault;
import com.percussion.webservices.ui.IPSUiDesignWs;
import com.percussion.webservices.ui.PSUiWsLocator;
import com.percussion.webservices.ui.data.ActionType;
import com.percussion.webservices.ui.data.PSDisplayFormat;
import com.percussion.webservices.ui.data.PSHierarchyNode;
import com.percussion.webservices.ui.data.PSSearchDef;
import com.percussion.webservices.ui.data.PSViewDef;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Server side implementations for web services defined in
 * <code>rhythmyxDesign.wsdl</code> for operations defined in the
 * <code>uiDesignSOAP</code> bindings.
 */
public class UiDesignSOAPImpl extends PSBaseSOAPImpl implements UiDesign
{
   /*
    * (non-Javadoc)
    * 
    * @see UiDesign#createActions(CreateActionsRequest)
    */
   public com.percussion.webservices.ui.data.PSAction[] createActions(
      CreateActionsRequest req) throws RemoteException,
      PSInvalidSessionFault, PSContractViolationFault, PSNotAuthorizedFault
   {
      final String serviceName = "createActions";
      
      String session = authenticate();
      String user = getRemoteUser();

      // get list of names ane types from the request
      List<String> names = Arrays.asList( req.getName());
      List<ActionType> types = new ArrayList<ActionType>();
      for (com.percussion.webservices.uidesign.CreateActionsRequestType type :
         req.getType())
      {
         if (type.getValue().equals(
            com.percussion.webservices.uidesign.CreateActionsRequestType._item))
         {
            types.add(ActionType.item);
         }
         else if (type.getValue().equals(
            com.percussion.webservices.uidesign.CreateActionsRequestType._cascading))
         {
            types.add(ActionType.cascading);
         }
         else
         {
            types.add(ActionType.dynamic);
         }
      }
      
      // create the actions
      IPSUiDesignWs uiws = PSUiWsLocator.getUiDesignWebservice();
      com.percussion.webservices.ui.data.PSAction[] result = null;
      try
      {
         List<PSAction> actions = uiws.createActions(names, types, session, user);
         // convert the actions
         result = (com.percussion.webservices.ui.data.PSAction[]) convert(
            com.percussion.webservices.ui.data.PSAction[].class, actions);
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, serviceName);
      }
      catch (PSLockErrorException e)
      {
         handleLockError(e);
      }
      catch (PSErrorException e)
      {
         throw new RemoteException(e.getLocalizedMessage());
      }
      catch (RuntimeException e)
      {
         handleRuntimeException(e, serviceName);
      }

      return result;
   }

   /*
    * (non-Javadoc)
    * 
    * @see UiDesign#createDisplayFormats(String[])
    */
   public PSDisplayFormat[] createDisplayFormats(String[] names)
      throws RemoteException, PSInvalidSessionFault, PSContractViolationFault,
      PSNotAuthorizedFault
   {
      final String serviceName = "createDisplayFormats";
      
      String session = authenticate();
      String user = getRemoteUser();

      IPSUiDesignWs uiws = PSUiWsLocator.getUiDesignWebservice();
      PSDisplayFormat[] result = null;
      try
      {
         List<com.percussion.cms.objectstore.PSDisplayFormat> dspFormats = 
            uiws.createDisplayFormats(Arrays.asList(names), session, user);
         result = (PSDisplayFormat[]) convert(PSDisplayFormat[].class, dspFormats);
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, serviceName);
      }
      catch (PSLockErrorException e)
      {
         handleLockError(e);
      }
      catch (PSErrorException e)
      {
         throw new RemoteException(e.getLocalizedMessage());
      }
      catch (RuntimeException e)
      {
         handleRuntimeException(e, serviceName);
      }
      
      return result;
   }

   /*
    * (non-Javadoc)
    * 
    * @see UiDesign#createSearches(CreateSearchesRequest)
    */
   public PSSearchDef[] createSearches(
      CreateSearchesRequest req) throws RemoteException,
      PSInvalidSessionFault, PSContractViolationFault, PSNotAuthorizedFault
   {
      final String serviceName = "createSearches";
      
      String session = authenticate();
      String user = getRemoteUser();

      // get list of names ane types from the request
      List<String> names = Arrays.asList( req.getName());
      List<String> types = new ArrayList<String>();
      for (CreateSearchesRequestType type :req.getType())
      {
         if (type.getValue().equals(CreateSearchesRequestType._custom))
            types.add(PSSearch.TYPE_CUSTOMSEARCH);
         else if (type.getValue().equals(CreateSearchesRequestType._standard))
            types.add(PSSearch.TYPE_STANDARDSEARCH);
         else
            types.add(PSSearch.TYPE_USERSEARCH);
      }
      
      // create the searches
      IPSUiDesignWs uiws = PSUiWsLocator.getUiDesignWebservice();
      PSSearchDef[] result = null;
      try
      {
         List<PSSearch> searches = uiws.createSearches(names, types, session, user);
         result = (PSSearchDef[]) convert(PSSearchDef[].class, searches);
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, serviceName);
      }
      catch (PSLockErrorException e)
      {
         handleLockError(e);
      }
      catch (PSErrorException e)
      {
         throw new RemoteException(e.getLocalizedMessage());
      }
      catch (RuntimeException e)
      {
         handleRuntimeException(e, serviceName);
      }
      return result;
   }

   /*
    * (non-Javadoc)
    * 
    * @see UiDesign#createViews(String[])
    */
   public PSViewDef[] createViews(String[] names)
      throws RemoteException, PSInvalidSessionFault, PSContractViolationFault,
      PSNotAuthorizedFault
   {
      final String serviceName = "createViews";
      
      String session = authenticate();
      String user = getRemoteUser();

      // get list of names ane types from the request
      List<String> nameList = Arrays.asList( names);
      
      // create the searches
      IPSUiDesignWs uiws = PSUiWsLocator.getUiDesignWebservice();
      PSViewDef[] result = null;
      try
      {
         List<PSSearch> searches = uiws.createViews(nameList, session, user);
         result = (PSViewDef[]) convert(PSViewDef[].class, searches);
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, serviceName);
      }
      catch (PSLockErrorException e)
      {
         handleLockError(e);
      }
      catch (PSErrorException e)
      {
         throw new RemoteException(e.getLocalizedMessage());
      }
      catch (RuntimeException e)
      {
         handleRuntimeException(e, serviceName);
      }
      
      return result;
   }

   /*
    * (non-Javadoc)
    * 
    * @see UiDesign#deleteActions(DeleteActionsRequest)
    */
   public void deleteActions(DeleteActionsRequest req)
      throws RemoteException, PSInvalidSessionFault, PSErrorsFault,
      PSContractViolationFault, PSNotAuthorizedFault
   {
      final String service = "deleteActions";
      String session = authenticate();
      String user = getRemoteUser();

      // convert ids to a list of GUIDs
      List<IPSGuid> ids = new ArrayList<IPSGuid>();
      for (long id : req.getId())
         ids.add(new PSDesignGuid(id));
      
      boolean ignoreDep = extractBooleanValue(req.getIgnoreDependencies(), 
         false);
      
      IPSUiDesignWs uiws = PSUiWsLocator.getUiDesignWebservice();
      try
      {
         uiws.deleteActions(ids, ignoreDep, session, user);
      }
      catch (PSErrorsException e)
      {
         handleErrorsException(e, service);
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, service);
      }
      catch (RuntimeException e)
      {
         handleRuntimeException(e, service);
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see UiDesign#deleteDisplayFormats(DeleteDisplayFormatsRequest)
    */
   public void deleteDisplayFormats(
      DeleteDisplayFormatsRequest req)
      throws RemoteException, PSInvalidSessionFault, PSErrorsFault,
      PSContractViolationFault, PSNotAuthorizedFault
   {
      final String service = "deleteDisplayFormats";
      String session = authenticate();
      String user = getRemoteUser();

      // convert ids to a list of GUIDs
      List<IPSGuid> ids = new ArrayList<IPSGuid>();
      for (long id : req.getId())
         ids.add(new PSDesignGuid(id));
      boolean ignoreDep = extractBooleanValue(req.getIgnoreDependencies(), 
         false);

      IPSUiDesignWs uiws = PSUiWsLocator.getUiDesignWebservice();
      try
      {
         uiws.deleteDisplayFormats(ids, ignoreDep, session, user);
      }
      catch (PSErrorsException e)
      {
         handleErrorsException(e, service);
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, service);
      }
      catch (RuntimeException e)
      {
         handleRuntimeException(e, service);
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see UiDesign#deleteSearches(DeleteSearchesRequest)
    */
   public void deleteSearches(DeleteSearchesRequest req)
      throws RemoteException, PSInvalidSessionFault, PSErrorsFault,
      PSContractViolationFault, PSNotAuthorizedFault
   {
      final String service = "deleteSearches";
      String session = authenticate();
      String user = getRemoteUser();

      // convert ids to a list of GUIDs
      List<IPSGuid> ids = new ArrayList<IPSGuid>();
      for (long id : req.getId())
         ids.add(new PSDesignGuid(id));
      
      boolean ignoreDep = extractBooleanValue(req.getIgnoreDependencies(), 
         false);

      IPSUiDesignWs uiws = PSUiWsLocator.getUiDesignWebservice();
      try
      {
         uiws.deleteSearches(ids, ignoreDep, session, user);
      }
      catch (PSErrorsException e)
      {
         handleErrorsException(e, service);
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, service);
      }
      catch (RuntimeException e)
      {
         handleRuntimeException(e, service);
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see UiDesign#deleteViews(DeleteViewsRequest)
    */
   public void deleteViews(DeleteViewsRequest req)
      throws RemoteException, PSInvalidSessionFault, PSErrorsFault,
      PSContractViolationFault, PSNotAuthorizedFault
   {
      final String service = "deleteViews";
      String session = authenticate();
      String user = getRemoteUser();

      // convert ids to a list of GUIDs
      List<IPSGuid> ids = new ArrayList<IPSGuid>();
      for (long id : req.getId())
         ids.add(new PSDesignGuid(id));
      
      boolean ignoreDep = extractBooleanValue(req.getIgnoreDependencies(), 
         false);

      IPSUiDesignWs uiws = PSUiWsLocator.getUiDesignWebservice();
      try
      {
         uiws.deleteViews(ids, ignoreDep, session, user);
      }
      catch (PSErrorsException e)
      {
         handleErrorsException(e, service);
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, service);
      }
      catch (RuntimeException e)
      {
         handleRuntimeException(e, service);
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see UiDesign#findActions(FindActionsRequest)
    */
   public PSObjectSummary[] findActions(FindActionsRequest request)
      throws RemoteException, PSInvalidSessionFault, PSContractViolationFault
   {
      authenticate();

      IPSUiDesignWs uiws = PSUiWsLocator.getUiDesignWebservice();
      try
      {
         List<ActionType> types = request.getType() == null ? null : Arrays
               .asList(request.getType());
         
         List<IPSCatalogSummary> objects = uiws.findActions(request.getName(),
               request.getLabel(), types);
         return (PSObjectSummary[]) convert(PSObjectSummary[].class, objects);
      }
      catch (PSErrorException e)
      {
         // unknown error
         throw new RemoteException(e.getLocalizedMessage());
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see UiDesign#findDisplayFormats(FindDisplayFormatsRequest)
    */
   public PSObjectSummary[] findDisplayFormats(
      FindDisplayFormatsRequest request)
      throws RemoteException, PSInvalidSessionFault, PSContractViolationFault
   {
      authenticate();

      IPSUiDesignWs uiws = PSUiWsLocator.getUiDesignWebservice();
      try
      {
         List<IPSCatalogSummary> objects = uiws.findDisplayFormats(request
               .getName(), request.getLabel());
         return (PSObjectSummary[]) convert(PSObjectSummary[].class, objects);
      }
      catch (PSErrorException e)
      {
         // unknown error
         throw new RemoteException(e.getLocalizedMessage());
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see UiDesign#findSearches(FindSearchesRequest)
    */
   public PSObjectSummary[] findSearches(FindSearchesRequest request)
      throws RemoteException, PSInvalidSessionFault, PSContractViolationFault
   {
      authenticate();

      IPSUiDesignWs uiws = PSUiWsLocator.getUiDesignWebservice();
      try
      {
         List<IPSCatalogSummary> objects = uiws.findSearches(request.getName(),
               request.getLabel());
         return (PSObjectSummary[]) convert(PSObjectSummary[].class, objects);
      }
      catch (PSErrorException e)
      {
         // unknown error
         throw new RemoteException(e.getLocalizedMessage());
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see UiDesign#findViews(FindViewsRequest)
    */
   public PSObjectSummary[] findViews(FindViewsRequest request)
      throws RemoteException, PSInvalidSessionFault, PSContractViolationFault
   {
      authenticate();

      IPSUiDesignWs uiws = PSUiWsLocator.getUiDesignWebservice();
      try
      {
         List<IPSCatalogSummary> objects = uiws.findViews(request.getName(),
               request.getLabel());
         return (PSObjectSummary[]) convert(PSObjectSummary[].class, objects);
      }
      catch (PSErrorException e)
      {
         // unknown error
         throw new RemoteException(e.getLocalizedMessage());
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see UiDesign#loadActions(LoadActionsRequest)
    */
   public com.percussion.webservices.ui.data.PSAction[] loadActions(
         LoadActionsRequest loadActionsRequest)
      throws RemoteException, PSErrorResultsFault, PSInvalidSessionFault,
      PSContractViolationFault, PSNotAuthorizedFault
   {
      final String service = "loadActions";
      String session = authenticate();
      String user = getRemoteUser();

      // convert ids to a list of GUIDs
      List<IPSGuid> ids = new ArrayList<IPSGuid>();
      for (long id : loadActionsRequest.getId())
         ids.add(new PSDesignGuid(id));
      
      IPSUiDesignWs uiws = PSUiWsLocator.getUiDesignWebservice();
      com.percussion.webservices.ui.data.PSAction[] result = null;
      try
      {
         List<PSAction> actions = uiws.loadActions(ids, loadActionsRequest
               .getLock(), loadActionsRequest.getOverrideLock(), session, user);
         
         result = (com.percussion.webservices.ui.data.PSAction[]) convert(
               com.percussion.webservices.ui.data.PSAction[].class, actions);
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, service);
      }
      catch (RuntimeException e)
      {
         handleRuntimeException(e, service);
      }
      catch (PSErrorResultsException e)
      {
         handleErrorResultsException(e, service);
      }
      
      return result;
   }

   /*
    * (non-Javadoc)
    * 
    * @see UiDesign#loadDisplayFormats(LoadDisplayFormatsRequest)
    */
   public PSDisplayFormat[] loadDisplayFormats(
      LoadDisplayFormatsRequest request)
      throws RemoteException, PSErrorResultsFault, PSInvalidSessionFault,
      PSContractViolationFault, PSNotAuthorizedFault
   {
      final String service = "loadDisplayFormats";
      String session = authenticate();
      String user = getRemoteUser();

      // convert ids to a list of GUIDs
      List<IPSGuid> ids = new ArrayList<IPSGuid>();
      for (long id : request.getId())
         ids.add(new PSDesignGuid(id));
      
      IPSUiDesignWs uiws = PSUiWsLocator.getUiDesignWebservice();
      PSDisplayFormat[] result = null;
      try
      {
         List<com.percussion.cms.objectstore.PSDisplayFormat> dspFormats = uiws
               .loadDisplayFormats(ids, request.getLock(), request
                     .getOverrideLock(), session, user);
         
         result = (PSDisplayFormat[]) convert(PSDisplayFormat[].class, dspFormats);
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, service);
      }
      catch (RuntimeException e)
      {
         handleRuntimeException(e, service);
      }
      catch (PSErrorResultsException e)
      {
         handleErrorResultsException(e, service);
      }
      
      return result;
   }

   /*
    * (non-Javadoc)
    * 
    * @see UiDesign#loadSearches(LoadSearchesRequest)
    */
   public PSSearchDef[] loadSearches(LoadSearchesRequest request)
      throws RemoteException, PSErrorResultsFault, PSInvalidSessionFault,
      PSContractViolationFault, PSNotAuthorizedFault
   {
      final String service = "loadSearches";
      String session = authenticate();
      String user = getRemoteUser();

      // convert ids to a list of GUIDs
      List<IPSGuid> ids = new ArrayList<IPSGuid>();
      for (long id : request.getId())
         ids.add(new PSDesignGuid(id));
      
      IPSUiDesignWs uiws = PSUiWsLocator.getUiDesignWebservice();
      PSSearchDef[] result = null;
      try
      {
         List<com.percussion.cms.objectstore.PSSearch> searches = uiws
               .loadSearches(ids, request.getLock(), request
                     .getOverrideLock(), session, user);
         
         result = (PSSearchDef[]) convert(PSSearchDef[].class, searches);
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, service);
      }
      catch (RuntimeException e)
      {
         handleRuntimeException(e, service);
      }
      catch (PSErrorResultsException e)
      {
         handleErrorResultsException(e, service);
      }
      
      return result;
   }

   /*
    * (non-Javadoc)
    * 
    * @see UiDesign#loadViews(LoadViewsRequest)
    */
   public PSViewDef[] loadViews(LoadViewsRequest request)
      throws RemoteException, PSErrorResultsFault, PSInvalidSessionFault,
      PSContractViolationFault, PSNotAuthorizedFault
   {
      final String service = "loadViews";
      String session = authenticate();
      String user = getRemoteUser();

      // convert ids to a list of GUIDs
      List<IPSGuid> ids = new ArrayList<IPSGuid>();
      for (long id : request.getId())
         ids.add(new PSDesignGuid(id));
      
      IPSUiDesignWs uiws = PSUiWsLocator.getUiDesignWebservice();
      PSViewDef[] result = null;
      try
      {
         List<com.percussion.cms.objectstore.PSSearch> views = uiws
               .loadViews(ids, request.getLock(), request
                     .getOverrideLock(), session, user);
         
         result = (PSViewDef[]) convert(PSViewDef[].class, views);
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, service);
      }
      catch (RuntimeException e)
      {
         handleRuntimeException(e, service);
      }
      catch (PSErrorResultsException e)
      {
         handleErrorResultsException(e, service);
      }
      
      return result;
   }

   /*
    * (non-Javadoc)
    * 
    * @see UiDesign#saveActions(SaveActionsRequest)
    */
   @SuppressWarnings("unchecked")
   public void saveActions(SaveActionsRequest saveActionsRequest)
      throws RemoteException, PSErrorsFault, PSInvalidSessionFault,
      PSContractViolationFault, PSNotAuthorizedFault
   {
      final String serviceName = "saveActions";
      
      String session = authenticate();
      String user = getRemoteUser();

      try
      {
         List<PSAction> actions = (List<PSAction>) convert(List.class, 
               saveActionsRequest.getPSAction());
         
         IPSUiDesignWs uiws = PSUiWsLocator.getUiDesignWebservice();
         uiws.saveActions(actions, saveActionsRequest.getRelease()
               .booleanValue(), session, user);
      }
      catch (PSErrorsException e)
      {
         handleErrorsException(e, serviceName);
      }
      catch (RuntimeException e)
      {
         handleRuntimeException(e, serviceName);
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see UiDesign#saveDisplayFormats(SaveDisplayFormatsRequest)
    */
   @SuppressWarnings("unchecked")
   public void saveDisplayFormats(SaveDisplayFormatsRequest request)
      throws RemoteException, PSErrorsFault, PSInvalidSessionFault,
      PSContractViolationFault, PSNotAuthorizedFault
   {
      final String serviceName = "saveDisplayFormats";
      
      String session = authenticate();
      String user = getRemoteUser();

      try
      {
         List<com.percussion.cms.objectstore.PSDisplayFormat> dspFormats = 
            (List<com.percussion.cms.objectstore.PSDisplayFormat>) convert(
               List.class, request.getPSDisplayFormat());
         
         IPSUiDesignWs uiws = PSUiWsLocator.getUiDesignWebservice();
         uiws.saveDisplayFormats(dspFormats, request.getRelease().booleanValue(),
               session, user);
      }
      catch (PSErrorsException e)
      {
         handleErrorsException(e, serviceName);
      }
      catch (RuntimeException e)
      {
         handleRuntimeException(e, serviceName);
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see UiDesign#saveSearches(SaveSearchesRequest)
    */
   @SuppressWarnings("unchecked")
   public void saveSearches(SaveSearchesRequest saveSearchesRequest)
      throws RemoteException, PSErrorsFault, PSInvalidSessionFault,
      PSContractViolationFault, PSNotAuthorizedFault
   {
      final String serviceName = "saveSearches";
      
      String session = authenticate();
      String user = getRemoteUser();

      try
      {
         List<PSSearch> searches = (List<PSSearch>) convert(List.class, 
               saveSearchesRequest.getPSSearchDef());
         
         IPSUiDesignWs uiws = PSUiWsLocator.getUiDesignWebservice();
         uiws.saveSearches(searches, saveSearchesRequest.getRelease()
               .booleanValue(), session, user);
      }
      catch (PSErrorsException e)
      {
         handleErrorsException(e, serviceName);
      }
      catch (RuntimeException e)
      {
         handleRuntimeException(e, serviceName);
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see UiDesign#saveViews(SaveViewsRequest)
    */
   @SuppressWarnings("unchecked")
   public void saveViews(SaveViewsRequest saveViewsRequest)
      throws RemoteException, PSErrorsFault, PSInvalidSessionFault,
      PSContractViolationFault, PSNotAuthorizedFault
   {
      final String serviceName = "saveViews";
      
      String session = authenticate();
      String user = getRemoteUser();

      try
      {
         List<PSSearch> searches = (List<PSSearch>) convert(List.class, 
               saveViewsRequest.getPSViewDef());
         
         IPSUiDesignWs uiws = PSUiWsLocator.getUiDesignWebservice();
         uiws.saveViews(searches, saveViewsRequest.getRelease()
               .booleanValue(), session, user);
      }
      catch (PSErrorsException e)
      {
         handleErrorsException(e, serviceName);
      }
      catch (RuntimeException e)
      {
         handleRuntimeException(e, serviceName);
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see UiDesign#createHierarchyNodes(CreateHierarchyNodesRequest)
    */
   @SuppressWarnings("unchecked")
   public PSHierarchyNode[] createHierarchyNodes(
      CreateHierarchyNodesRequest createHierarchyNodesRequest)
      throws RemoteException, PSInvalidSessionFault, PSContractViolationFault,
      PSNotAuthorizedFault
   {
      final String serviceName = "createHierarchyNodes";
      try
      {
         String session = authenticate();
         String user = getRemoteUser();

         IPSUiDesignWs service = PSUiWsLocator.getUiDesignWebservice();
         
         String[] names = createHierarchyNodesRequest.getName();
         
         List<IPSGuid> parents = new ArrayList<IPSGuid>();
         for (long parent : createHierarchyNodesRequest.getParentId())
            parents.add(parent == 0 ? null : new PSDesignGuid(parent));
         
         List<com.percussion.services.ui.data.PSHierarchyNode.NodeType> types = 
            new ArrayList<com.percussion.services.ui.data.PSHierarchyNode.NodeType>();
         for (CreateHierarchyNodesRequestType type : createHierarchyNodesRequest.getType())
            types.add(
               (com.percussion.services.ui.data.PSHierarchyNode.NodeType) convert(
               com.percussion.services.ui.data.PSHierarchyNode.NodeType.class, 
               type));

         return (PSHierarchyNode[]) convert(PSHierarchyNode[].class, 
            service.createHierarchyNodes(Arrays.asList(names), parents, types, 
               session, user));
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, serviceName);
      }
      catch (RuntimeException e)
      {
         handleRuntimeException(e, serviceName);
      }
      catch (PSUiException e)
      {
         // this should never happen
         throw new RemoteException(e.getLocalizedMessage());
      }
      
      // will never get here
      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see UiDesign#deleteHierarchyNodes(DeleteHierarchyNodesRequest)
    */
   public void deleteHierarchyNodes(
      DeleteHierarchyNodesRequest deleteHierarchyNodesRequest)
      throws RemoteException, PSInvalidSessionFault, PSErrorsFault,
      PSContractViolationFault, PSNotAuthorizedFault
   {
      final String serviceName = "deleteHierarchyNodes";
      try
      {
         String session = authenticate();
         String user = getRemoteUser();

         IPSUiDesignWs service = PSUiWsLocator.getUiDesignWebservice();
         
         List<IPSGuid> ids = PSGuidUtils.toGuidList(
            deleteHierarchyNodesRequest.getId(), PSTypeEnum.HIERARCHY_NODE);
         boolean ignoreDependencies = extractBooleanValue(
            deleteHierarchyNodesRequest.getIgnoreDependencies(), false);
         service.deleteHierarchyNodes(ids, ignoreDependencies, session, user);
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, serviceName);
      }
      catch (PSErrorsException e)
      {
         handleErrorsException(e, serviceName);
      }
      catch (RuntimeException e)
      {
         handleRuntimeException(e, serviceName);
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see UiDesign#findHierarchyNodes(FindHierarchyNodesRequest)
    */
   public PSObjectSummary[] findHierarchyNodes(
      FindHierarchyNodesRequest findHierarchyNodesRequest)
      throws RemoteException, PSInvalidSessionFault, PSContractViolationFault
   {
      final String serviceName = "findHierarchyNodes";
      try
      {
         authenticate();

         IPSUiDesignWs service = PSUiWsLocator.getUiDesignWebservice();
         
         com.percussion.services.ui.data.PSHierarchyNode.NodeType type = null;
         if (findHierarchyNodesRequest.getType() != null)
         {
            type = 
               (com.percussion.services.ui.data.PSHierarchyNode.NodeType) convert(
                  com.percussion.services.ui.data.PSHierarchyNode.NodeType.class, 
                  findHierarchyNodesRequest.getType());
         }
         
         List summaries = service.findHierarchyNodes(
            findHierarchyNodesRequest.getPath(), type);

         return (PSObjectSummary[]) convert(PSObjectSummary[].class, summaries);
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, serviceName);
         return null; // never here, used to turn off compiling error
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see UiDesign#getChildren(GetChildrenRequest)
    */
   public long[] getChildren(GetChildrenRequest getChildrenRequest)
      throws RemoteException, PSInvalidSessionFault, PSContractViolationFault
   {
      final String serviceName = "getChildren";
      try
      {
         authenticate();

         IPSUiDesignWs service = PSUiWsLocator.getUiDesignWebservice();
         
         IPSGuid parentId = null;
         if (getChildrenRequest.getId() != 0)
            parentId = new PSDesignGuid(getChildrenRequest.getId());
         List<IPSGuid> children = service.getChildren(parentId);

         return PSGuidUtils.toLongArray(children);
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, serviceName);
         return null; // never here, used to turn off compiling error
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see UiDesign#idsToPaths(long[])
    */
   public String[] idsToPaths(long[] idsToPathsRequest) throws RemoteException,
      PSInvalidSessionFault, PSContractViolationFault, PSErrorResultsFault
   {
      final String serviceName = "idsToPaths";
      try
      {
         authenticate();

         IPSUiDesignWs service = PSUiWsLocator.getUiDesignWebservice();
         
         if (idsToPathsRequest == null)
            throw new IllegalArgumentException("ids cannot be null");
         
         List<String> paths = service.idsToPaths(
            PSGuidUtils.toGuidList(idsToPathsRequest));

         return paths.toArray(new String[paths.size()]);
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, serviceName);
      }
      catch (PSErrorResultsException e)
      {
         handleErrorResultsException(e, serviceName);
      }
      
      return null; // never here, used to turn off compiling error
   }

   /*
    * (non-Javadoc)
    * 
    * @see UiDesign#loadHierarchyNodes(LoadHierarchyNodesRequest)
    */
   public PSHierarchyNode[] loadHierarchyNodes(
      LoadHierarchyNodesRequest loadHierarchyNodesRequest)
      throws RemoteException, PSErrorResultsFault, PSInvalidSessionFault,
      PSContractViolationFault, PSNotAuthorizedFault
   {
      final String serviceName = "loadHierarchyNodes";
      try
      {
         String session = authenticate();
         String user = getRemoteUser();

         IPSUiDesignWs service = PSUiWsLocator.getUiDesignWebservice();
         
         List<IPSGuid> ids = PSGuidUtils.toGuidList(
            loadHierarchyNodesRequest.getId(), PSTypeEnum.HIERARCHY_NODE);
         boolean lock = extractBooleanValue(
            loadHierarchyNodesRequest.getLock(), false);
         boolean overrideLock = extractBooleanValue(
            loadHierarchyNodesRequest.getOverrideLock(), false);
         List nodes = service.loadHierachyNodes(ids, lock, overrideLock, 
            session, user);

         return (PSHierarchyNode[]) convert(PSHierarchyNode[].class, nodes);
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, serviceName);
      }
      catch (RuntimeException e)
      {
         handleRuntimeException(e, serviceName);
      }
      catch (PSErrorResultsException e)
      {
         handleErrorResultsException(e, serviceName);
      }
      
      return null; // never here, used to turn off compiling error
   }

   /*
    * (non-Javadoc)
    * 
    * @see UiDesign#moveChildren(MoveChildrenRequest)
    */
   public void moveChildren(MoveChildrenRequest moveChildrenRequest)
      throws RemoteException, PSInvalidSessionFault, PSContractViolationFault
   {
      final String serviceName = "moveChildren";
      try
      {
         authenticate();
   
         IPSUiDesignWs service = PSUiWsLocator.getUiDesignWebservice();
         
         IPSGuid sourceId = new PSDesignGuid(
            moveChildrenRequest.getSourceId());
         IPSGuid targetId = new PSDesignGuid(
            moveChildrenRequest.getTargetId());
         List<IPSGuid> children = PSGuidUtils.toGuidList(
            moveChildrenRequest.getId());
         service.moveChildren(sourceId, targetId, children);
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, serviceName);
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see UiDesign#pathsToIds(String[])
    */
   public long[][] pathsToIds(String[] pathsToIdsRequest) throws RemoteException,
      PSInvalidSessionFault, PSContractViolationFault, PSErrorResultsFault
   {
      final String serviceName = "pathsToIds";
      try
      {
         authenticate();

         IPSUiDesignWs service = PSUiWsLocator.getUiDesignWebservice();
         
         if (pathsToIdsRequest == null)
            throw new IllegalArgumentException("paths cannot be null");
         
         List<List<IPSGuid>> idsList = service.pathsToIds(
            Arrays.asList(pathsToIdsRequest));

         long[][] idsArray = new long[idsList.size()][0];
         int index = 0;
         for (List<IPSGuid> ids : idsList)
            idsArray[index++] = PSGuidUtils.toLongArray(ids);
         
         return idsArray;
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, serviceName);
      }
      catch (PSErrorException e)
      {
         throw new RemoteException(e.getLocalizedMessage());
      }
      
      return null; // never here, used to turn off compiling error
   }

   /*
    * (non-Javadoc)
    * 
    * @see UiDesign#removeChildren(RemoveChildrenRequest)
    */
   public void removeChildren(RemoveChildrenRequest removeChildrenRequest)
      throws RemoteException, PSInvalidSessionFault, PSContractViolationFault
   {
      final String serviceName = "removeChildren";
      try
      {
         authenticate();

         IPSUiDesignWs service = PSUiWsLocator.getUiDesignWebservice();
         
         IPSGuid parentId = null;
         if (removeChildrenRequest.getParentId() != 0)
            parentId = new PSDesignGuid(removeChildrenRequest.getParentId());
         List<IPSGuid> children = PSGuidUtils.toGuidList(
            removeChildrenRequest.getId());
         service.removeChildren(parentId, children);
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, serviceName);
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see UiDesign#saveHierarchyNodes(SaveHierarchyNodesRequest)
    */
   @SuppressWarnings("unchecked")
   public void saveHierarchyNodes(
      SaveHierarchyNodesRequest saveHierarchyNodesRequest)
      throws RemoteException, PSErrorsFault, PSInvalidSessionFault,
      PSContractViolationFault, PSNotAuthorizedFault
   {
      final String serviceName = "saveHierarchyNodes";
      try
      {
         String session = authenticate();
         String user = getRemoteUser();

         IPSUiDesignWs service = PSUiWsLocator.getUiDesignWebservice();
         
         List nodes = (List) convert(List.class,
            saveHierarchyNodesRequest.getPSHierarchyNode());
         boolean release = extractBooleanValue(
            saveHierarchyNodesRequest.getRelease(), true);
         service.saveHierarchyNodes(nodes, release, session, user);
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, serviceName);
      }
      catch (PSErrorsException e)
      {
         handleErrorsException(e, serviceName);
      }
      catch (RuntimeException e)
      {
         handleRuntimeException(e, serviceName);
      }
   }
}
