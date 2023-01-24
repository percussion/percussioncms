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
package com.percussion.webservices.securitydesign;

import com.percussion.error.PSExceptionUtils;
import com.percussion.security.PSSecurityCatalogException;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.PSGuidUtils;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.IPSWebserviceErrors;
import com.percussion.webservices.PSBaseSOAPImpl;
import com.percussion.webservices.PSErrorResultsException;
import com.percussion.webservices.PSErrorsException;
import com.percussion.webservices.PSWebserviceErrors;
import com.percussion.webservices.common.PSObjectSummary;
import com.percussion.webservices.faults.PSContractViolationFault;
import com.percussion.webservices.faults.PSErrorResultsFault;
import com.percussion.webservices.faults.PSErrorsFault;
import com.percussion.webservices.faults.PSInvalidSessionFault;
import com.percussion.webservices.faults.PSNotAuthorizedFault;
import com.percussion.webservices.security.IPSSecurityDesignWs;
import com.percussion.webservices.security.PSSecurityWsLocator;
import com.percussion.webservices.security.data.PSCommunity;
import com.percussion.webservices.security.data.PSCommunityVisibility;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;

/**
 * Server side implementations for web services defined in
 * <code>rhythmyxDesign.wsdl</code> for operations defined in the
 * <code>securityDesignSOAP</code> bindings.
 */
public class SecurityDesignSOAPImpl extends PSBaseSOAPImpl implements SecurityDesign
{
   /*
    * (non-Javadoc)
    * 
    * @see SecurityDesign#createCommunities(String[])
    */
   @SuppressWarnings("unused")
   public PSCommunity[] createCommunities(String[] names) 
      throws RemoteException, PSInvalidSessionFault, PSContractViolationFault, 
      PSNotAuthorizedFault
   {
      final String serviceName = "createCommunities";
      try
      {
         String session = authenticate();
         String user = getRemoteUser();

         IPSSecurityDesignWs service = 
            PSSecurityWsLocator.getSecurityDesignWebservice();
         
         return (PSCommunity[]) convert(PSCommunity[].class, 
            service.createCommunities(Arrays.asList(names), session, user));
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

   /*
    * (non-Javadoc)
    * 
    * @see SecurityDesign#deleteCommunities(DeleteCommunitiesRequest)
    */
   @SuppressWarnings("unused")
   public void deleteCommunities(
      DeleteCommunitiesRequest deleteCommunitiesRequest)
      throws RemoteException, PSInvalidSessionFault, PSErrorsFault,
      PSContractViolationFault, PSNotAuthorizedFault
   {
      final String serviceName = "deleteCommunities";
      try
      {
         String session = authenticate();
         String user = getRemoteUser();

         IPSSecurityDesignWs service = 
            PSSecurityWsLocator.getSecurityDesignWebservice();
         
         List<IPSGuid> ids = PSGuidUtils.toGuidList(
            deleteCommunitiesRequest.getId(), PSTypeEnum.COMMUNITY_DEF);
         boolean ignoreDependencies = extractBooleanValue(
            deleteCommunitiesRequest.getIgnoreDependencies(), false);
         service.deleteCommunities(ids, ignoreDependencies, session, user);
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
    * @see SecurityDesign#findCommunities(FindCommunitiesRequest)
    */
   public PSObjectSummary[] findCommunities(
      FindCommunitiesRequest findCommunitiesRequest) throws RemoteException,
      PSInvalidSessionFault, PSContractViolationFault
   {
      final String serviceName = "findCommunities";
      try
      {
         authenticate();

         IPSSecurityDesignWs service = 
            PSSecurityWsLocator.getSecurityDesignWebservice();
         
         List summaries = service.findCommunities(
            findCommunitiesRequest.getName());

         return (PSObjectSummary[]) convert(PSObjectSummary[].class, summaries);
      }
      catch (IllegalArgumentException e)
      {
         int code = IPSWebserviceErrors.INVALID_CONTRACT;
         throw new PSContractViolationFault(code, 
            PSWebserviceErrors.createErrorMessage(code, serviceName, 
               e.getLocalizedMessage()), ExceptionUtils.getFullStackTrace(e));
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see SecurityDesign#findRoles(FindRolesRequest)
    */
   public PSObjectSummary[] findRoles(FindRolesRequest findRolesRequest)
      throws RemoteException, PSInvalidSessionFault, PSContractViolationFault
   {
      final String serviceName = "findRoles";
      try
      {
         authenticate();

         IPSSecurityDesignWs service = 
            PSSecurityWsLocator.getSecurityDesignWebservice();
         
         List summaries = service.findRoles(findRolesRequest.getName());

         return (PSObjectSummary[]) convert(PSObjectSummary[].class, summaries);
      }
      catch (IllegalArgumentException e)
      {
         int code = IPSWebserviceErrors.INVALID_CONTRACT;
         throw new PSContractViolationFault(code, 
            PSWebserviceErrors.createErrorMessage(code, serviceName, 
               e.getLocalizedMessage()), ExceptionUtils.getFullStackTrace(e));
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see SecurityDesign#loadCommunities(LoadCommunitiesRequest)
    */
   @SuppressWarnings("unused")
   public PSCommunity[] loadCommunities(
      LoadCommunitiesRequest loadCommunitiesRequest) throws RemoteException,
      PSErrorResultsFault, PSInvalidSessionFault, PSContractViolationFault,
      PSNotAuthorizedFault
   {
      final String serviceName = "loadCommunities";
      try
      {
         String session = authenticate();
         String user = getRemoteUser();

         IPSSecurityDesignWs service = 
            PSSecurityWsLocator.getSecurityDesignWebservice();
         
         List<IPSGuid> ids = PSGuidUtils.toGuidList(
            loadCommunitiesRequest.getId(), PSTypeEnum.COMMUNITY_DEF);
         boolean lock = extractBooleanValue(
            loadCommunitiesRequest.getLock(), false);
         boolean overrideLock = extractBooleanValue(
            loadCommunitiesRequest.getOverrideLock(), false);
         List communities = service.loadCommunities(ids, lock, overrideLock, 
            session, user);

         return (PSCommunity[]) convert(PSCommunity[].class, communities);
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

   /*
    * (non-Javadoc)
    * 
    * @see SecurityDesign#saveCommunities(SaveCommunitiesRequest)
    */
   @SuppressWarnings({"unchecked","unused"})
   public void saveCommunities(
      SaveCommunitiesRequest saveCommunitiesRequest) throws RemoteException,
      PSErrorsFault, PSInvalidSessionFault, PSContractViolationFault,
      PSNotAuthorizedFault
   {
      final String serviceName = "saveCommunities";
      try
      {
         String session = authenticate();
         String user = getRemoteUser();

         IPSSecurityDesignWs service = 
            PSSecurityWsLocator.getSecurityDesignWebservice();
         
         List communities = (List) convert(List.class,
            saveCommunitiesRequest.getPSCommunity());
         boolean release = extractBooleanValue(
            saveCommunitiesRequest.getRelease(), true);
         service.saveCommunities(communities, release, session, user);
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
    * @see SecurityDesign#isValidRhythmyxUser(IsValidRhythmyxUserRequest)
    */
   public IsValidRhythmyxUserResponse isValidRhythmyxUser(
      IsValidRhythmyxUserRequest isValidRhythmyxUserRequest)
      throws RemoteException, PSInvalidSessionFault, PSContractViolationFault
   {
      final String serviceName = "isValidRhythmyxUser";
      try
      {
         authenticate();

         IPSSecurityDesignWs service = 
            PSSecurityWsLocator.getSecurityDesignWebservice();
         
         boolean isValidUser = service.isValidRhythmyxUser(
            isValidRhythmyxUserRequest.getUsername());

         return new IsValidRhythmyxUserResponse(isValidUser);
      }
      catch (IllegalArgumentException e)
      {
         int code = IPSWebserviceErrors.INVALID_CONTRACT;
         throw new PSContractViolationFault(code, 
            PSWebserviceErrors.createErrorMessage(code, serviceName, 
               e.getLocalizedMessage()), ExceptionUtils.getFullStackTrace(e));
      }
      catch (PSSecurityCatalogException e)
      {
         // this should never happen
         throw new RemoteException(PSExceptionUtils.getMessageForLog(e));
      }
   }

   /* (non-Javadoc)
    * @see SecurityDesign#getVisibilityByCommunity(
    *    GetVisibilityByCommunityRequest)
    */
   public PSCommunityVisibility[] getVisibilityByCommunity(
      GetVisibilityByCommunityRequest getVisibilityByCommunityRequest) 
      throws RemoteException, PSErrorResultsFault, PSInvalidSessionFault, 
         PSContractViolationFault, PSNotAuthorizedFault
   {
      final String serviceName = "getVisibilityByCommunity";
      try
      {
         String session = authenticate();
         String user = getRemoteUser();

         IPSSecurityDesignWs service = 
            PSSecurityWsLocator.getSecurityDesignWebservice();
         
         List<IPSGuid> ids = PSGuidUtils.toGuidList(
            getVisibilityByCommunityRequest.getId());
         PSTypeEnum type = null;
         if (getVisibilityByCommunityRequest.getType() != null)
         {
            type = PSTypeEnum.valueOf(
               getVisibilityByCommunityRequest.getType());
            if (type == null)
               throw new IllegalArgumentException("unknown type was supplied");
         }
         List visibilities = service.getVisibilityByCommunity(ids, type, 
            session, user);

         return (PSCommunityVisibility[]) convert(PSCommunityVisibility[].class, 
            visibilities);
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
         PSErrorResultsFault fault = (PSErrorResultsFault) convert(
            PSErrorResultsFault.class, e);
         fault.setService(serviceName);
         
         throw fault;
      }
   }
}
