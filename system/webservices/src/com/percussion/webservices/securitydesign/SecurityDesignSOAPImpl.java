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
package com.percussion.webservices.securitydesign;

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.PSGuidUtils;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.security.PSSecurityCatalogException;
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

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.exception.ExceptionUtils;

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
         throw new RemoteException(e.getLocalizedMessage());
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
