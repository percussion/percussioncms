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
package com.percussion.webservices.security;

import com.percussion.data.PSInternalRequestCallException;
import com.percussion.services.guidmgr.PSGuidUtils;
import com.percussion.services.security.data.PSLogin;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.IPSWebserviceErrors;
import com.percussion.webservices.PSBaseSOAPImpl;
import com.percussion.webservices.PSWebserviceErrors;
import com.percussion.webservices.faults.PSContractViolationFault;
import com.percussion.webservices.faults.PSInvalidSessionFault;
import com.percussion.webservices.faults.PSNotAuthenticatedFault;
import com.percussion.webservices.faults.PSNotAuthorizedFault;
import com.percussion.webservices.security.data.PSCommunity;
import com.percussion.webservices.security.data.PSRole;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.List;

import javax.security.auth.login.LoginException;
import javax.servlet.ServletException;

import org.apache.commons.lang.exception.ExceptionUtils;

/**
 * Server side implementations for web services defined in
 * <code>rhythmyx.wsdl</code> for operations defined in the
 * <code>securitySOAP</code> bindings.
 */
public class SecuritySOAPImpl extends PSBaseSOAPImpl implements Security
{
   /*
    * (non-Javadoc)
    * 
    * @see Security#loadCommunities(LoadCommunitiesRequest)
    */
   public PSCommunity[] loadCommunities(
      LoadCommunitiesRequest loadCommunitiesRequest) throws RemoteException,
      PSInvalidSessionFault, PSContractViolationFault, PSNotAuthorizedFault
   {
      String serviceName = "loadCommunities";
      try
      {
         authenticate();

         IPSSecurityWs service = PSSecurityWsLocator.getSecurityWebservice();
         
         List communities = service.loadCommunities(
            loadCommunitiesRequest.getName());

         return (PSCommunity[]) convert(PSCommunity[].class, communities);
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
    * @see Security#loadRoles(LoadRolesRequest)
    */
   public PSRole[] loadRoles(LoadRolesRequest loadRolesRequest)
      throws RemoteException, PSInvalidSessionFault, PSContractViolationFault,
      PSNotAuthorizedFault
   {
      String serviceName = "loadRoles";
      try
      {
         authenticate();

         IPSSecurityWs service = PSSecurityWsLocator.getSecurityWebservice();
         
         List roles = service.loadRoles(loadRolesRequest.getName());

         return (PSRole[]) convert(PSRole[].class, roles);
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
    * @see Security#login(LoginRequest)
    */
   public LoginResponse login(LoginRequest loginRequest)
      throws RemoteException, PSNotAuthenticatedFault, PSContractViolationFault
   {
      try
      {
         IPSSecurityWs service = PSSecurityWsLocator.getSecurityWebservice();
         PSLogin login = service.login(getServletRequest(), getServletResponse(), 
            loginRequest.getUsername(), loginRequest.getPassword(), 
            loginRequest.getClientId(), loginRequest.getCommunity(), 
            loginRequest.getLocaleCode());
         
         return new LoginResponse(
            (com.percussion.webservices.security.data.PSLogin) 
               convert(com.percussion.webservices.security.data.PSLogin.class, 
                  login));
      }
      catch (IllegalArgumentException e)
      {
         int code = IPSWebserviceErrors.INVALID_CONTRACT;
         throw new PSContractViolationFault(code, 
            PSWebserviceErrors.createErrorMessage(code, "login", 
               e.getLocalizedMessage()), ExceptionUtils.getFullStackTrace(e));
      }
      catch (IOException e)
      {
         throw new PSNotAuthenticatedFault(0, e.getLocalizedMessage(), 
            ExceptionUtils.getFullStackTrace(e));
      }
      catch (ServletException e)
      {
         throw new PSNotAuthenticatedFault(0, e.getLocalizedMessage(), 
            ExceptionUtils.getFullStackTrace(e));
      }
      catch (LoginException e)
      {
         throw new PSNotAuthenticatedFault(0, e.getLocalizedMessage(), 
            ExceptionUtils.getFullStackTrace(e));
      }
      catch (PSInternalRequestCallException e)
      {
         throw new PSNotAuthenticatedFault(0, e.getLocalizedMessage(), 
            ExceptionUtils.getFullStackTrace(e));
      }
      catch (Exception e)
      {
         throw new PSNotAuthenticatedFault(0, e.getLocalizedMessage(), 
            ExceptionUtils.getFullStackTrace(e));
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see Security#logout(LogoutRequest)
    */
   public void logout(LogoutRequest logoutRequest) throws RemoteException,
      PSInvalidSessionFault, PSContractViolationFault
   {
      try
      {
         IPSSecurityWs service = PSSecurityWsLocator.getSecurityWebservice();
         
         String sessionId = logoutRequest.getSessionId();
         service.logout(getServletRequest(), sessionId);
      }
      catch (IllegalArgumentException e)
      {
         int code = IPSWebserviceErrors.INVALID_CONTRACT;
         throw new PSContractViolationFault(code, 
            PSWebserviceErrors.createErrorMessage(code, "logout", 
               e.getLocalizedMessage()), ExceptionUtils.getFullStackTrace(e));
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see Security#refreshSession(RefreshSessionRequest)
    */
   public void refreshSession(RefreshSessionRequest refreshSessionRequest)
      throws RemoteException, PSInvalidSessionFault, PSContractViolationFault
   {
      try
      {
         IPSSecurityWs service = PSSecurityWsLocator.getSecurityWebservice();
         
         String sessionId = refreshSessionRequest.getSessionId();
         service.refreshSession(getServletRequest(), sessionId);
      }
      catch (IllegalArgumentException e)
      {
         int code = IPSWebserviceErrors.INVALID_CONTRACT;
         throw new PSContractViolationFault(code, 
            PSWebserviceErrors.createErrorMessage(code, "refreshSession", 
               e.getLocalizedMessage()), ExceptionUtils.getFullStackTrace(e));
      }
      catch (LoginException e)
      {
         throw new PSInvalidSessionFault(0, e.getLocalizedMessage(), 
            ExceptionUtils.getFullStackTrace(e));
      }
   }

   /* (non-Javadoc)
    * @see com.percussion.webservices.security.Security
    * #filterByRuntimeVisibility(long[])
    */
   public FilterByRuntimeVisibilityResponse filterByRuntimeVisibility(long[] ids) 
      throws RemoteException, PSInvalidSessionFault, PSContractViolationFault
   {
      String serviceName = "filterByRuntimeVisibility";
      try
      {
         authenticate();

         IPSSecurityWs service = PSSecurityWsLocator.getSecurityWebservice();
         
         List<IPSGuid> filtered = service.filterByRuntimeVisibility(
            PSGuidUtils.toGuidList(ids));

         return new FilterByRuntimeVisibilityResponse(
            PSGuidUtils.toLongArray(filtered));
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
