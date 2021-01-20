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
package com.percussion.webservices.systemdesign;

import com.percussion.services.catalog.IPSCatalogSummary;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.PSGuidUtils;
import com.percussion.services.guidmgr.data.PSDesignGuid;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.security.PSPermissions;
import com.percussion.services.security.data.PSUserAccessLevel;
import com.percussion.services.system.IPSSystemService;
import com.percussion.services.system.PSSystemServiceLocator;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.IPSWebserviceErrors;
import com.percussion.webservices.PSBaseSOAPImpl;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.PSErrorResultsException;
import com.percussion.webservices.PSErrorsException;
import com.percussion.webservices.PSLockErrorException;
import com.percussion.webservices.PSWebserviceErrors;
import com.percussion.webservices.PSWebserviceUtils;
import com.percussion.webservices.common.PSObjectSummary;
import com.percussion.webservices.faults.PSContractViolationFault;
import com.percussion.webservices.faults.PSErrorResultsFault;
import com.percussion.webservices.faults.PSErrorsFault;
import com.percussion.webservices.faults.PSInvalidSessionFault;
import com.percussion.webservices.faults.PSLockFault;
import com.percussion.webservices.faults.PSNotAuthorizedFault;
import com.percussion.webservices.faults.PSUnknownConfigurationFault;
import com.percussion.webservices.system.IPSSystemDesignWs;
import com.percussion.webservices.system.PSAclImpl;
import com.percussion.webservices.system.PSDependency;
import com.percussion.webservices.system.PSItemFilter;
import com.percussion.webservices.system.PSMimeContentAdapter;
import com.percussion.webservices.system.PSRelationshipConfig;
import com.percussion.webservices.system.PSSharedProperty;
import com.percussion.webservices.system.PSSystemWsLocator;
import com.percussion.webservices.system.RelationshipCategory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.exception.ExceptionUtils;

/**
 * Server side implementations for web services defined in
 * <code>rhythmyxDesign.wsdl</code> for operations defined in the
 * <code>systemDesignSOAP</code> bindings.
 */
public class SystemDesignSOAPImpl extends PSBaseSOAPImpl implements SystemDesign
{
   /*
    * (non-Javadoc)
    * 
    * @see SystemDesign#createAcls(long[])
    */
   public PSAclImpl[] createAcls(long[] ids)
      throws RemoteException, PSInvalidSessionFault, PSContractViolationFault,
      PSNotAuthorizedFault
   {
      String session = authenticate();
      String user = getRemoteUser();

      PSAclImpl[] result = null;
      String service = "createAcls";
      try
      {
         if (ids == null || ids.length == 0)
            throw new IllegalArgumentException("Ids may not be null or empty");
         
         IPSSystemDesignWs svce = PSSystemWsLocator.getSystemDesignWebservice();
         List<com.percussion.services.security.data.PSAclImpl> aclList = 
            new ArrayList<com.percussion.services.security.data.PSAclImpl>(
               ids.length);
         for (long id : ids)
         {
            aclList.add(svce.createAcl(new PSDesignGuid(id), session, user));
         }

         result = (PSAclImpl[]) convert(PSAclImpl[].class, aclList);
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, service);
      }
      catch (PSLockErrorException e)
      {
         handleLockError(e);
      }
      catch (RuntimeException e)
      {
         handleRuntimeException(e, service);
      }

      // will never get here
      return result;
   }

   /*
    * (non-Javadoc)
    * 
    * @see SystemDesign#createRelationshipTypes(CreateRelationshipTypesRequest)
    */
   public PSRelationshipConfig[] createRelationshipTypes(
      CreateRelationshipTypesRequest req)
      throws RemoteException, PSInvalidSessionFault, PSContractViolationFault,
      PSNotAuthorizedFault
   {
      String session = authenticate();
      String user = getRemoteUser();

      PSRelationshipConfig[] result = null;
      String service = "createRelationshipTypes";
      try
      {
         IPSSystemDesignWs svce = PSSystemWsLocator.getSystemDesignWebservice();
         
         List<String> categories = getRelationshipCategories(req.getCategory());
         List<String> names = Arrays.asList(req.getName());
         List<com.percussion.design.objectstore.PSRelationshipConfig> configs;
         configs = svce.createRelationshipTypes(names, categories, session, user);
         result = (PSRelationshipConfig[]) convert(
               PSRelationshipConfig[].class, configs);
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, service);
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
         handleRuntimeException(e, service);
      }
      
      // will never get here
      return result;
   }
   
   /**
    * Convertes relationship categories from webservice to objectstore format.
    * 
    * @param cats the to be converted categories, assumed not <code>null</code>.
    *    
    * @return the converted categories, never <code>null</code>.
    */
   private List<String> getRelationshipCategories(RelationshipCategory[] cats)
   {
      List<String> categories = new ArrayList<String>();
      for (RelationshipCategory cat : cats)
      {
         if (cat == null)
            throw new IllegalArgumentException("Relationship Category must not be null.");
         categories.add(getRelationshipCategory(cat));
      }
      return categories;
   }

   /*
    * (non-Javadoc)
    * 
    * @see SystemDesign#deleteAcls(DeleteAclsRequest)
    */
   public void deleteAcls(DeleteAclsRequest deleteAclsRequest)
      throws RemoteException, PSInvalidSessionFault, PSErrorsFault,
      PSContractViolationFault, PSNotAuthorizedFault
   {
      final String service = "deleteAcls";
      String session = authenticate();
      String user = getRemoteUser();

      IPSSystemDesignWs ws = PSSystemWsLocator.getSystemDesignWebservice();
      try
      {
         long[] idarr = deleteAclsRequest.getId();
         if (idarr == null || idarr.length == 0)
            throw new IllegalArgumentException("Ids may not be null");
         
         // convert ids to a list of GUIDs
         List<IPSGuid> ids = new ArrayList<IPSGuid>();
         for (long id : idarr)
            ids.add(new PSGuid(PSTypeEnum.ACL, id));
         
         boolean ignoreDep = extractBooleanValue(
            deleteAclsRequest.getIgnoreDependencies(), false);
         
         ws.deleteAcls(ids, ignoreDep, session, user);
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
    * @see SystemDesign#deleteRelationshipTypes(DeleteRelationshipTypesRequest)
    */
   public void deleteRelationshipTypes(
      DeleteRelationshipTypesRequest req)
      throws RemoteException, PSInvalidSessionFault, PSErrorsFault,
      PSContractViolationFault, PSNotAuthorizedFault
   {
      final String service = "deleteRelationshipTypes";
      String session = authenticate();
      String user = getRemoteUser();

      // convert ids to a list of GUIDs
      List<IPSGuid> ids = new ArrayList<IPSGuid>();
      for (long id : req.getId())
         ids.add(new PSDesignGuid(id));
      
      boolean ignoreDep = extractBooleanValue(req.getIgnoreDependencies(), 
         false);

      IPSSystemDesignWs ws = PSSystemWsLocator.getSystemDesignWebservice();
      try
      {
         ws.deleteRelationshipTypes(ids, ignoreDep, session, user);
      }
      catch (PSErrorsException e)
      {
         handleErrorsException(e, service);
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, service);
      }
      catch (PSErrorException e)
      {
         throw new RemoteException(e.getLocalizedMessage());
      }
      catch (RuntimeException e)
      {
         handleRuntimeException(e, service);
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see SystemDesign#deleteSharedProperties(DeleteSharedPropertiesRequest)
    */
   @SuppressWarnings("unchecked")
   public void deleteSharedProperties(
      DeleteSharedPropertiesRequest deleteSharedPropertiesRequest)
      throws RemoteException, PSInvalidSessionFault, PSErrorsFault,
      PSContractViolationFault, PSNotAuthorizedFault
   {
      final String serviceName = "deleteSharedProperties";
      try
      {
         String session = authenticate();
         String user = getRemoteUser();

         IPSSystemDesignWs webService = 
            PSSystemWsLocator.getSystemDesignWebservice();
         
         IPSSystemService service = PSSystemServiceLocator.getSystemService();
         
         // convert from client to server object
         List<com.percussion.services.system.data.PSSharedProperty> properties = 
            (List<com.percussion.services.system.data.PSSharedProperty>) convert(
               List.class, 
               deleteSharedPropertiesRequest.getPSSharedProperty());

         // get the correct guid / version for existing properties
         for (com.percussion.services.system.data.PSSharedProperty property : 
            properties)
         {
            List<com.percussion.services.system.data.PSSharedProperty> existingProperties = 
               service.findSharedPropertiesByName(property.getName());
            if (!existingProperties.isEmpty())
            {
               com.percussion.services.system.data.PSSharedProperty existingProperty = 
                  existingProperties.get(0);
               property.setGUID(existingProperty.getGUID());
               property.setVersion(existingProperty.getVersion());
            }
         }

         boolean ignoreDependencies = extractBooleanValue(
            deleteSharedPropertiesRequest.getIgnoreDependencies(), false);
         
         webService.deleteSharedProperties(properties, ignoreDependencies, session, 
            user);
      }
      catch (IllegalArgumentException e)
      {
         int code = IPSWebserviceErrors.INVALID_CONTRACT;
         throw new PSContractViolationFault(code, 
            PSWebserviceErrors.createErrorMessage(code, serviceName, 
               e.getLocalizedMessage()), ExceptionUtils.getFullStackTrace(e));
      }
      catch (PSErrorsException e)
      {
         PSErrorsFault fault = (PSErrorsFault) convert(
            PSErrorsFault.class, e);
         fault.setService(serviceName);
         
         throw fault;
      }
      catch (RuntimeException e)
      {
         handleRuntimeException(e, serviceName);
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see SystemDesign#extendLocks(long[])
    */
   public void extendLocks(long[] extendLockRequest) throws RemoteException,
      PSInvalidSessionFault, PSErrorsFault, PSContractViolationFault
   {
      final String serviceName = "extendLocks";
      try
      {
         if (extendLockRequest == null)
            throw new IllegalArgumentException(
               "extendLockRequest cannot be null");
         
         if (extendLockRequest.length == 0)
            throw new IllegalArgumentException(
               "extendLockRequest cannot be empty");
         
         String session = authenticate();
         String user = getRemoteUser();

         IPSSystemDesignWs service = 
            PSSystemWsLocator.getSystemDesignWebservice();
         
         service.extendLocks(PSGuidUtils.toGuidList(extendLockRequest), session, 
            user);
      }
      catch (IllegalArgumentException e)
      {
         int code = IPSWebserviceErrors.INVALID_CONTRACT;
         throw new PSContractViolationFault(code, 
            PSWebserviceErrors.createErrorMessage(code, serviceName, 
               e.getLocalizedMessage()), ExceptionUtils.getFullStackTrace(e));
      }
      catch (PSErrorsException e)
      {
         PSErrorsFault fault = (PSErrorsFault) convert(
            PSErrorsFault.class, e);
         fault.setService(serviceName);
         
         throw fault;
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see SystemDesign#findDependencies(long[])
    */
   public PSDependency[] findDependencies(long[] findDependenciesRequest)
      throws RemoteException, PSInvalidSessionFault, PSContractViolationFault
   {
      try
      {
         authenticate();

         if (findDependenciesRequest == null || 
            findDependenciesRequest.length == 0)
         {
            throw new IllegalArgumentException(
               "findDependenciesRequest may not be null or empty");
         }
         IPSSystemDesignWs service = 
            PSSystemWsLocator.getSystemDesignWebservice();
         
         List<IPSGuid> ids = new ArrayList<IPSGuid>(
            findDependenciesRequest.length);
         for (int i = 0; i < findDependenciesRequest.length; i++)
         {
            ids.add(new PSDesignGuid(findDependenciesRequest[i]));
         }
         List deps = service.findDependencies(ids);
         
         return (PSDependency[]) convert(PSDependency[].class, deps);
      }
      catch (IllegalArgumentException e)
      {
         int code = IPSWebserviceErrors.INVALID_CONTRACT;
         throw new PSContractViolationFault(code, 
            PSWebserviceErrors.createErrorMessage(code, "findDependencies", 
               e.getLocalizedMessage()), ExceptionUtils.getFullStackTrace(e));
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see SystemDesign#findRelationshipTypes(FindRelationshipTypesRequest)
    */
   public PSObjectSummary[] findRelationshipTypes(
      FindRelationshipTypesRequest req)
      throws RemoteException, PSInvalidSessionFault, PSContractViolationFault
   {
      PSObjectSummary[] result = null;
      String service = "findRelationshipTypes";
      try
      {
         authenticate();

         IPSSystemDesignWs svce = PSSystemWsLocator.getSystemDesignWebservice();
         
         List<IPSCatalogSummary> configs;
         configs = svce.findRelationshipTypes(req.getName(), 
               getRelationshipCategory(req.getCategory()));
         result = (PSObjectSummary[]) convert(PSObjectSummary[].class, configs);
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, service);
      }
      catch (PSErrorException e)
      {
         throw new RemoteException(e.getLocalizedMessage());
      }
      return result;
   }

   /*
    * (non-Javadoc)
    * 
    * @see SystemDesign#findWorkflows(FindWorkflowsRequest)
    */
   public PSObjectSummary[] findWorkflows(
      FindWorkflowsRequest findWorkflowsRequest) throws RemoteException,
      PSInvalidSessionFault, PSContractViolationFault
   {
      authenticate();

      IPSSystemDesignWs sysws = PSSystemWsLocator.getSystemDesignWebservice();

      List<IPSCatalogSummary> objects = sysws.findWorkflows(
         findWorkflowsRequest.getName());
      objects = PSWebserviceUtils.toObjectSummaries(objects);

      List<com.percussion.services.catalog.data.PSObjectSummary> summaries = 
         new ArrayList<com.percussion.services.catalog.data.PSObjectSummary>(
            objects.size());
      for (IPSCatalogSummary object : objects)
         summaries
               .add((com.percussion.services.catalog.data.PSObjectSummary) object);
      return (PSObjectSummary[]) convert(PSObjectSummary[].class, summaries);
   }

   /*
    * (non-Javadoc)
    * 
    * @see SystemDesign#loadAcls(LoadAclsRequest)
    */
   public PSAclImpl[] loadAcls(LoadAclsRequest req)
      throws RemoteException, PSErrorResultsFault, PSInvalidSessionFault,
      PSContractViolationFault, PSNotAuthorizedFault
   {
      String service = "loadAcls";
      try
      {
         String session = authenticate();
         String user = getRemoteUser();

         IPSSystemDesignWs svce = PSSystemWsLocator.getSystemDesignWebservice();
         
         // convert long[] to List<IPSGuid>
         List<IPSGuid> guids = new ArrayList<IPSGuid>();
         long[] ids = req.getId();
         if (ids == null || ids.length == 0)
         {
            throw new IllegalArgumentException("Ids may not be null or empty");
         }
         
         for (long id : ids)
            guids.add(new PSGuid(id));
         
         boolean lock = extractBooleanValue(req.getLock(), false);
         boolean overrideLock = extractBooleanValue(req.getOverrideLock(), 
            false);
         
         List<com.percussion.services.security.data.PSAclImpl> aclList = svce
               .loadAcls(guids, lock, overrideLock, session, user);
         
         return (PSAclImpl[]) convert(PSAclImpl[].class, aclList);
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

      // will never get here
      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see SystemDesign#loadConfiguration(LoadConfigurationRequest)
    */
   public LoadConfigurationResponse loadConfiguration(
      LoadConfigurationRequest loadConfigurationRequest)
      throws RemoteException, PSInvalidSessionFault, PSContractViolationFault,
      PSUnknownConfigurationFault, PSNotAuthorizedFault
   {
      final String serviceName = "loadConfiguration";
      String session = authenticate();
      String user = getRemoteUser();

      try
      {
         IPSSystemDesignWs service = 
            PSSystemWsLocator.getSystemDesignWebservice();

         boolean lock = extractBooleanValue(
            loadConfigurationRequest.getLock(), false);
         boolean overrideLock = extractBooleanValue(
            loadConfigurationRequest.getOverrideLock(), false);
         
         com.percussion.services.system.data.PSMimeContentAdapter config = 
            service
            .loadConfiguration(loadConfigurationRequest.getName(), lock,
               overrideLock, session, user);
   
         LoadConfigurationResponse response = new LoadConfigurationResponse();
         response.setPSMimeContentAdapter((PSMimeContentAdapter) convert(
            PSMimeContentAdapter.class, config));
         
         return response;
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, serviceName);
      }
      catch (PSLockErrorException e)
      {
         PSLockFault fault = (PSLockFault) convert(
            PSLockFault.class, e);
         
         throw fault;
      }
      catch (FileNotFoundException e)
      {
         e.printStackTrace();
         throw new RemoteException(e.getLocalizedMessage());
      }
      catch (RuntimeException e)
      {
         handleRuntimeException(e, serviceName);
      }
      
      // will never get here
      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see SystemDesign#loadRelationshipTypes(LoadRelationshipTypesRequest)
    */
   public PSRelationshipConfig[] loadRelationshipTypes(
      LoadRelationshipTypesRequest req)
      throws RemoteException, PSErrorResultsFault, PSInvalidSessionFault,
      PSContractViolationFault, PSNotAuthorizedFault
   {
      String service = "loadRelationshipTypes";
      try
      {
         String session = authenticate();
         String user = getRemoteUser();

         IPSSystemDesignWs svce = PSSystemWsLocator.getSystemDesignWebservice();
         
         // convert long[] to List<IPSGuid>
         List<IPSGuid> guids = new ArrayList<IPSGuid>();
         long[] ids = req.getId();
         if (ids != null)
         {
            for (long id : ids)
               guids.add(new PSDesignGuid(id));
         }
         List<com.percussion.design.objectstore.PSRelationshipConfig> configs = 
            svce.loadRelationshipTypes(guids, req.getLock(), 
               req.getOverrideLock(), session, user);
         
         return (PSRelationshipConfig[]) convert(
               PSRelationshipConfig[].class, configs);
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

      // will never get here
      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see SystemDesign#loadSharedProperties(LoadSharedPropertiesRequest)
    */
   @SuppressWarnings("unchecked")
   public PSSharedProperty[] loadSharedProperties(
      LoadSharedPropertiesRequest loadSharedPropertiesRequest)
      throws RemoteException, PSErrorResultsFault, PSInvalidSessionFault,
      PSContractViolationFault, PSNotAuthorizedFault
   {
      final String serviceName = "loadSharedProperties";
      try
      {
         String session = authenticate();
         String user = getRemoteUser();

         IPSSystemDesignWs service = 
            PSSystemWsLocator.getSystemDesignWebservice();

         boolean lock = extractBooleanValue(
            loadSharedPropertiesRequest.getLock(), false);
         boolean overrideLock = extractBooleanValue(
            loadSharedPropertiesRequest.getOverrideLock(), false);
         
         List properties = service.loadSharedProperties(
            loadSharedPropertiesRequest.getName(), lock, overrideLock, 
            session, user);
   
         return (PSSharedProperty[]) convert(PSSharedProperty[].class, 
            properties);
      }
      catch (IllegalArgumentException e)
      {
         int code = IPSWebserviceErrors.INVALID_CONTRACT;
         throw new PSContractViolationFault(code, 
            PSWebserviceErrors.createErrorMessage(code, "loadSharedProperties", 
               e.getLocalizedMessage()), ExceptionUtils.getFullStackTrace(e));
      }
      catch (RuntimeException e)
      {
         handleRuntimeException(e, serviceName);
      }
      catch (PSErrorResultsException e)
      {
         PSErrorResultsFault fault = (PSErrorResultsFault) convert(
            PSErrorResultsFault.class, e);
         fault.setService(serviceName);
         
         throw fault;
      }
      
      // will never get here
      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see SystemDesign#releaseLocks(long[])
    */
   public void releaseLocks(long[] releaseLockRequest) throws RemoteException,
      PSInvalidSessionFault, PSContractViolationFault
   {
      final String serviceName = "releaseLocks";
      try
      {
         if (releaseLockRequest == null)
            throw new IllegalArgumentException(
               "releaseLockRequest cannot be null");
         
         if (releaseLockRequest.length == 0)
            throw new IllegalArgumentException(
               "releaseLockRequest cannot be empty");
         
         String session = authenticate();
         String user = getRemoteUser();

         IPSSystemDesignWs service = 
            PSSystemWsLocator.getSystemDesignWebservice();
         
         service.releaseLocks(PSGuidUtils.toGuidList(releaseLockRequest), 
            session, user);
      }
      catch (IllegalArgumentException e)
      {
         int code = IPSWebserviceErrors.INVALID_CONTRACT;
         throw new PSContractViolationFault(code, 
            PSWebserviceErrors.createErrorMessage(code, serviceName, 
               e.getLocalizedMessage()), ExceptionUtils.getFullStackTrace(e));
      }
   }

   /* (non-Javadoc)
    * @see SystemDesign#getLockedSummaries()
    */
   public PSObjectSummary[] getLockedSummaries() 
      throws RemoteException
   {
      final String serviceName = "getLockedSummaries";
      try
      {
         String session = authenticate();
         String user = getRemoteUser();

         IPSSystemDesignWs service = 
            PSSystemWsLocator.getSystemDesignWebservice();
         
         List summaries = service.getLockedSummaries(session, user);
         return (PSObjectSummary[]) convert(PSObjectSummary[].class, summaries);
      }
      catch (IllegalArgumentException e)
      {
         int code = IPSWebserviceErrors.INVALID_CONTRACT;
         throw new RemoteException(
            PSWebserviceErrors.createErrorMessage(code, serviceName, 
               e.getLocalizedMessage()));
      }
      catch (PSErrorResultsException e)
      {
         // this should never happen
         throw new RemoteException(e.getLocalizedMessage());
      }
   }

   /* (non-Javadoc)
    * @see SystemDesign#createLocks(CreateLocksRequest)
    */
   public void createLocks(CreateLocksRequest createLocksRequest) 
      throws RemoteException, PSInvalidSessionFault, PSErrorsFault, 
         PSContractViolationFault
   {
      final String serviceName = "createLocks";
      try
      {
         String session = authenticate();
         String user = getRemoteUser();

         IPSSystemDesignWs service = PSSystemWsLocator
            .getSystemDesignWebservice();

         List<IPSGuid> ids = PSGuidUtils.toGuidList(createLocksRequest.getId());
         boolean overrideLock = extractBooleanValue(createLocksRequest
            .isOverrideLock(), false);

         service.createLocks(ids, overrideLock, session, user);
      }
      catch (IllegalArgumentException e)
      {
         int code = IPSWebserviceErrors.INVALID_CONTRACT;
         throw new PSContractViolationFault(code, PSWebserviceErrors
            .createErrorMessage(code, serviceName, e
               .getLocalizedMessage()), ExceptionUtils.getFullStackTrace(e));
      }
      catch (PSErrorsException e)
      {
         handleErrorsException(e, serviceName);
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see SystemDesign#isLocked(long[])
    */
   public PSObjectSummary[] isLocked(long[] isLockedRequest) 
      throws RemoteException, PSInvalidSessionFault, PSContractViolationFault
   {
      final String serviceName = "isLocked";
      try
      {
         if (isLockedRequest == null || isLockedRequest.length == 0)
            throw new IllegalArgumentException("ids cannot be null or empty");
         
         authenticate();
         String user = getRemoteUser();

         IPSSystemDesignWs service = 
            PSSystemWsLocator.getSystemDesignWebservice();
         
         List<IPSGuid> ids = PSGuidUtils.toGuidList(isLockedRequest);

         List<com.percussion.services.catalog.data.PSObjectSummary> summaries = 
            service.isLocked(ids, user);
         
         PSObjectSummary[] results = new PSObjectSummary[summaries.size()];
         for (int i=0; i<summaries.size(); i++)
         {
            com.percussion.services.catalog.data.PSObjectSummary summary =
               summaries.get(i);
            if (summary == null)
               results[i] = null;
            else
               results[i] = (PSObjectSummary) convert(PSObjectSummary.class, 
                  summary);
         }

         return results;
      }
      catch (IllegalArgumentException e)
      {
         int code = IPSWebserviceErrors.INVALID_CONTRACT;
         throw new PSContractViolationFault(code, 
            PSWebserviceErrors.createErrorMessage(code, serviceName, 
               e.getLocalizedMessage()), ExceptionUtils.getFullStackTrace(e));
      }
      catch (PSErrorResultsException e)
      {
         // this should never happen
         throw new RemoteException(e.getLocalizedMessage());
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see SystemDesign#saveAcls(SaveAclsRequest)
    */
   @SuppressWarnings(value={"unchecked"})
   public SaveAclsResponsePermissions[] saveAcls(SaveAclsRequest req)
      throws RemoteException, PSErrorResultsFault, PSInvalidSessionFault,
         PSContractViolationFault, PSNotAuthorizedFault
   {
      SaveAclsResponsePermissions[] results = null;

      String service = "saveAcls";
      try
      {
         String session = authenticate();
         String user = getRemoteUser();

         PSAclImpl[] aclarr = req.getPSAclImpl();
         if (aclarr == null || aclarr.length == 0)
            throw new IllegalArgumentException(
               "PSAclImpl may not be null or empty");
         
         List<com.percussion.services.security.data.PSAclImpl> aclList = 
            (List<com.percussion.services.security.data.PSAclImpl>) convert(
               List.class, req.getPSAclImpl());
         
         IPSSystemDesignWs webService = 
            PSSystemWsLocator.getSystemDesignWebservice();   
         boolean release = extractBooleanValue(req.getRelease(), false);
         List<PSUserAccessLevel> accessLevels = webService.saveAcls(aclList, 
            release, session, user);
         
         results = new SaveAclsResponsePermissions[aclList.size()];
         for (int i=0; i<aclList.size(); i++)
         {
            SaveAclsResponsePermissions result = 
               new SaveAclsResponsePermissions();
            result.setId(aclList.get(i).getId());

            PSUserAccessLevel accessLevel = accessLevels.get(i);
            Set<PSPermissions> permset = accessLevel.getPermissions();
            int[] perms = new int[permset.size()];
            int index = 0;
            for (PSPermissions permission : permset)
               perms[index++] = permission.getOrdinal();
            result.setPermission(perms);
            
            results[i] = result;
         }
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
      
      return results;
   }

   /*
    * (non-Javadoc)
    * 
    * @see SystemDesign#saveConfiguration(SaveConfigurationRequest)
    */
   public void saveConfiguration(
      SaveConfigurationRequest saveConfigurationRequest)
      throws RemoteException, PSInvalidSessionFault, PSContractViolationFault,
      PSNotAuthorizedFault
   {
      final String serviceName = "saveConfiguration";
      String session = authenticate();
      String user = getRemoteUser();
      
      try
      {
         IPSSystemDesignWs service = 
            PSSystemWsLocator.getSystemDesignWebservice();

         com.percussion.services.system.data.PSMimeContentAdapter config = 
            (com.percussion.services.system.data.PSMimeContentAdapter) convert(
            com.percussion.services.system.data.PSMimeContentAdapter.class, 
            saveConfigurationRequest.getPSMimeContentAdapter());
         
         Boolean release = saveConfigurationRequest.getRelease() == null ? 
            Boolean.TRUE : saveConfigurationRequest.getRelease().booleanValue();
         
         service.saveConfiguration(config, release, session, user);
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, serviceName);
      }
      catch (PSLockErrorException e)
      {
         PSLockFault fault = (PSLockFault) convert(
            PSLockFault.class, e);
         
         throw fault;
      }
      catch (IOException e)
      {
         e.printStackTrace();
         throw new RemoteException(e.getLocalizedMessage());
      }
      catch (RuntimeException e)
      {
         handleRuntimeException(e, serviceName);
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see SystemDesign#saveRelationshipTypes(SaveRelationshipTypesRequest)
    */
   @SuppressWarnings("unchecked")
   public void saveRelationshipTypes(
      SaveRelationshipTypesRequest req)
      throws RemoteException, PSErrorsFault, PSInvalidSessionFault,
      PSContractViolationFault, PSNotAuthorizedFault
   {
      final String serviceName = "saveRelationshipTypes";
      
      String session = authenticate();
      String user = getRemoteUser();
      try
      {
         List<com.percussion.design.objectstore.PSRelationshipConfig> configs = 
            (List<com.percussion.design.objectstore.PSRelationshipConfig>) convert(
               List.class, req.getPSRelationshipConfig());
         
         IPSSystemDesignWs webService = 
            PSSystemWsLocator.getSystemDesignWebservice();         
         webService.saveRelationshipTypes(configs, req.getRelease(), session, 
               user);
      }
      catch (PSErrorsException e)
      {
         handleErrorsException(e, serviceName);
      }
      catch (PSErrorException e)
      {
         throw new RemoteException(e.getLocalizedMessage());
      }
      catch (RuntimeException e)
      {
         handleRuntimeException(e, serviceName);
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see SystemDesign#saveSharedProperties(SaveSharedPropertiesRequest)
    */
   @SuppressWarnings("unchecked")
   public void saveSharedProperties(
      SaveSharedPropertiesRequest saveSharedPropertiesRequest)
      throws RemoteException, PSErrorsFault, PSInvalidSessionFault,
      PSContractViolationFault, PSNotAuthorizedFault
   {
      final String serviceName = "saveSharedProperties";
      try
      {
         String session = authenticate();
         String user = getRemoteUser();
         
         IPSSystemDesignWs webService = 
            PSSystemWsLocator.getSystemDesignWebservice();
         
         IPSSystemService service = PSSystemServiceLocator.getSystemService();

         // convert from client to server object
         List<com.percussion.services.system.data.PSSharedProperty> properties = 
            (List<com.percussion.services.system.data.PSSharedProperty>) convert(
               List.class, 
               saveSharedPropertiesRequest.getPSSharedProperty());

         // get the correct guid / version for existing properties
         for (com.percussion.services.system.data.PSSharedProperty property : 
            properties)
         {
            List<com.percussion.services.system.data.PSSharedProperty> existingProperties = 
               service.findSharedPropertiesByName(property.getName());
            if (!existingProperties.isEmpty())
            {
               com.percussion.services.system.data.PSSharedProperty existingProperty = 
                  existingProperties.get(0);
               property.setGUID(existingProperty.getGUID());
               property.setVersion(existingProperty.getVersion());
            }
         }
         
         boolean release = extractBooleanValue(
            saveSharedPropertiesRequest.getRelease(), true);
         
         webService.saveSharedProperties(properties, release, session, user);
      }
      catch (IllegalArgumentException e)
      {
         int code = IPSWebserviceErrors.INVALID_CONTRACT;
         throw new PSContractViolationFault(code, 
            PSWebserviceErrors.createErrorMessage(code, serviceName, 
               e.getLocalizedMessage()), ExceptionUtils.getFullStackTrace(e));
      }
      catch (PSErrorsException e)
      {
         PSErrorsFault fault = (PSErrorsFault) convert(
            PSErrorsFault.class, e);
         fault.setService(serviceName);
         
         throw fault;
      }
      catch (RuntimeException e)
      {
         handleRuntimeException(e, serviceName);
      }
   }

   /* (non-Javadoc)
    * @see SystemDesign#createGuids(CreateGuidsRequest)
    */
   public long[] createGuids(CreateGuidsRequest createGuidsRequest) 
      throws RemoteException, PSInvalidSessionFault, PSContractViolationFault
   {
      final String serviceName = "createGuids";
      try
      {
         if (createGuidsRequest == null)
            throw new IllegalArgumentException(
               "extendLockRequest cannot be null");
         
         PSTypeEnum type = PSTypeEnum.valueOf(createGuidsRequest.getType());
         if (type == null)
            throw new IllegalArgumentException(
               "an unknown type was specified with the supplied request");
         
         int count = createGuidsRequest.getCount() == null ? 
            1 : createGuidsRequest.getCount().intValue();
         
         authenticate();

         IPSSystemDesignWs service = 
            PSSystemWsLocator.getSystemDesignWebservice();
         
         List<IPSGuid> guids = service.createGuids(type, count);
         return PSGuidUtils.toLongArray(guids);
      }
      catch (IllegalArgumentException e)
      {
         int code = IPSWebserviceErrors.INVALID_CONTRACT;
         throw new PSContractViolationFault(code, 
            PSWebserviceErrors.createErrorMessage(code, serviceName, 
               e.getLocalizedMessage()), ExceptionUtils.getFullStackTrace(e));
      }
   }

   /* (non-Javadoc)
    * @see SystemDesign#createItemFilters(String[])
    */
   public PSItemFilter[] createItemFilters(String[] createItemFiltersRequest)
      throws RemoteException, PSInvalidSessionFault, PSContractViolationFault,
      PSNotAuthorizedFault
   {
      final String serviceName = "createItemFilters";
      try
      {
         String session = authenticate();
         String user = getRemoteUser();

         IPSSystemDesignWs service = 
            PSSystemWsLocator.getSystemDesignWebservice();
         
         return (PSItemFilter[]) convert(PSItemFilter[].class, 
            service.createItemFilters(Arrays.asList(createItemFiltersRequest), 
               session, user));
      }
      catch (IllegalArgumentException e)
      {
         int code = IPSWebserviceErrors.INVALID_CONTRACT;
         throw new PSContractViolationFault(code, 
            PSWebserviceErrors.createErrorMessage(code, serviceName, 
               e.getLocalizedMessage()), ExceptionUtils.getFullStackTrace(e));
      }
      catch (RuntimeException e)
      {
         handleRuntimeException(e, serviceName);
      }
      
      // will never get here
      return null;
   }

   /* (non-Javadoc)
    * @see SystemDesign#deleteItemFilters(DeleteItemFiltersRequest)
    */
   public void deleteItemFilters(
      DeleteItemFiltersRequest deleteItemFiltersRequest)
      throws RemoteException, PSInvalidSessionFault, PSErrorsFault,
      PSContractViolationFault, PSNotAuthorizedFault
   {
      final String serviceName = "deleteItemFilters";
      try
      {
         String session = authenticate();
         String user = getRemoteUser();

         IPSSystemDesignWs service = 
            PSSystemWsLocator.getSystemDesignWebservice();
         
         List<IPSGuid> ids = PSGuidUtils.toGuidList(
            deleteItemFiltersRequest.getId());
         boolean ignoreDependencies = extractBooleanValue(
            deleteItemFiltersRequest.getIgnoreDependencies(), false);
         service.deleteItemFilters(ids, ignoreDependencies, session, user);
      }
      catch (IllegalArgumentException e)
      {
         int code = IPSWebserviceErrors.INVALID_CONTRACT;
         throw new PSContractViolationFault(code, 
            PSWebserviceErrors.createErrorMessage(code, serviceName, 
               e.getLocalizedMessage()), ExceptionUtils.getFullStackTrace(e));
      }
      catch (PSErrorsException e)
      {
         PSErrorsFault fault = (PSErrorsFault) convert(
            PSErrorsFault.class, e);
         fault.setService(serviceName);
         
         throw fault;
      }
      catch (RuntimeException e)
      {
         handleRuntimeException(e, serviceName);
      }
   }

   /* (non-Javadoc)
    * @see SystemDesign#findItemFilters(FindItemFiltersRequest)
    */
   public PSObjectSummary[] findItemFilters(
      FindItemFiltersRequest findItemFiltersRequest) throws RemoteException,
      PSInvalidSessionFault
   {
      authenticate();

      IPSSystemDesignWs service = 
         PSSystemWsLocator.getSystemDesignWebservice();
      
      List summaries = service.findItemFilters(
         findItemFiltersRequest.getName());

      return (PSObjectSummary[]) convert(PSObjectSummary[].class, summaries);
   }

   /* (non-Javadoc)
    * @see SystemDesign#loadItemFilters(LoadItemFiltersRequest)
    */
   public PSItemFilter[] loadItemFilters(
      LoadItemFiltersRequest loadItemFiltersRequest) throws RemoteException,
      PSErrorResultsFault, PSInvalidSessionFault, PSContractViolationFault,
      PSNotAuthorizedFault
   {
      final String serviceName = "loadItemFilters";
      try
      {
         String session = authenticate();
         String user = getRemoteUser();

         IPSSystemDesignWs service = 
            PSSystemWsLocator.getSystemDesignWebservice();
         
         List<IPSGuid> ids = PSGuidUtils.toGuidList(
            loadItemFiltersRequest.getId());
         boolean lock = extractBooleanValue(
            loadItemFiltersRequest.getLock(), false);
         boolean overrideLock = extractBooleanValue(
            loadItemFiltersRequest.getOverrideLock(), false);
         List filters = service.loadItemFilters(ids, lock, overrideLock, 
            session, user);

         return (PSItemFilter[]) convert(PSItemFilter[].class, filters);
      }
      catch (IllegalArgumentException e)
      {
         int code = IPSWebserviceErrors.INVALID_CONTRACT;
         throw new PSContractViolationFault(code, 
            PSWebserviceErrors.createErrorMessage(code, serviceName, 
               e.getLocalizedMessage()), ExceptionUtils.getFullStackTrace(e));
      }
      catch (RuntimeException e)
      {
         handleRuntimeException(e, serviceName);
      }
      catch (PSErrorResultsException e)
      {
         PSErrorResultsFault fault = (PSErrorResultsFault) convert(
            PSErrorResultsFault.class, e);
         fault.setService(serviceName);
         
         throw fault;
      }
      
      // will never get here
      return null;
   }

   /* (non-Javadoc)
    * @see SystemDesign#saveItemFilters(SaveItemFiltersRequest)
    */
   @SuppressWarnings("unchecked")
   public void saveItemFilters(SaveItemFiltersRequest saveItemFiltersRequest)
      throws RemoteException, PSInvalidSessionFault, PSErrorsFault,
      PSContractViolationFault, PSNotAuthorizedFault
   {
      final String serviceName = "saveItemFilters";
      try
      {
         String session = authenticate();
         String user = getRemoteUser();

         IPSSystemDesignWs service = 
            PSSystemWsLocator.getSystemDesignWebservice();
         
         List filters = (List) convert(List.class, 
            saveItemFiltersRequest.getPSItemFilter());
         boolean release = extractBooleanValue(
            saveItemFiltersRequest.getRelease(), true);
         service.saveItemFilters(filters, release, session, user);
      }
      catch (IllegalArgumentException e)
      {
         int code = IPSWebserviceErrors.INVALID_CONTRACT;
         throw new PSContractViolationFault(code, 
            PSWebserviceErrors.createErrorMessage(code, serviceName, 
               e.getLocalizedMessage()), ExceptionUtils.getFullStackTrace(e));
      }
      catch (PSErrorsException e)
      {
         PSErrorsFault fault = (PSErrorsFault) convert(
            PSErrorsFault.class, e);
         fault.setService(serviceName);
         
         throw fault;
      }
      catch (RuntimeException e)
      {
         handleRuntimeException(e, serviceName);
      }
   }
}
