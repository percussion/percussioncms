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
package com.percussion.membership.services.impl;

import com.percussion.delivery.services.PSAbstractRestService;
import com.percussion.membership.data.PSAccountCreateResult;
import com.percussion.membership.data.PSAccountSummary;
import com.percussion.membership.data.PSGetUserResult;
import com.percussion.membership.data.PSLoginRequest;
import com.percussion.membership.data.PSLoginResult;
import com.percussion.membership.data.PSMembershipAccount;
import com.percussion.membership.data.PSMembershipResult;
import com.percussion.membership.data.PSMembershipResult.STATUS;
import com.percussion.membership.data.PSResetRequest;
import com.percussion.membership.data.PSUserGroup;
import com.percussion.membership.data.PSUserSession;
import com.percussion.membership.data.PSUserSummary;
import com.percussion.membership.services.IPSMembershipRestService;
import com.percussion.membership.services.IPSMembershipService;
import com.percussion.membership.services.PSAuthenticationFailedException;
import com.percussion.membership.services.PSMemberExistsException;
import com.percussion.membership.services.PSResetPwdException;
import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 *  REST/Webservice layer used to access the membership service.
 *  
 * @author jayseletz
 *
 */
@Path("/membership")
@Component
@Scope("singleton")
public class PSMembershipRestService extends PSAbstractRestService implements IPSMembershipRestService
{
    @Autowired
    private IPSMembershipService membershipService;

    private  static final Logger log = LogManager.getLogger(PSMembershipRestService.class);

    public PSMembershipRestService(){}
    /**
     * Ctor, autowired by spring.
     * 
     * @param service The service to use, may not be <code>null</code>.
     */
    public PSMembershipRestService(IPSMembershipService service)
    {
        Validate.notNull(service);
        membershipService = service;
    }

    @HEAD
    @Path("/csrf")
    public void csrf(@Context HttpServletRequest request, @Context HttpServletResponse response)  {
        CsrfToken csrfToken = new HttpSessionCsrfTokenRepository().loadToken(request);

        response.setHeader("X-CSRF-HEADER", csrfToken.getHeaderName());
        response.setHeader("X-CSRF-PARAM", csrfToken.getParameterName());
        response.setHeader("X-CSRF-TOKEN", csrfToken.getToken());
    }

    /* (non-Javadoc)
	 * @see com.percussion.membership.services.impl.IPSMembershipRestService#createUser(com.percussion.membership.data.PSMembershipAccount, javax.ws.rs.core.HttpHeaders)
	 */
    @Override
	@POST
    @Path("/user")
    @Produces("application/json")
    public PSMembershipResult createUser(PSMembershipAccount membership, @Context HttpHeaders header)
    {
        Validate.notNull(membership);
        
        if (!validateNotEmpty(membership.getEmail())) {
        	log.error("Email may not be empty.");
            return new PSAccountCreateResult(STATUS.INVALID_PARAM, "Email may not be empty", "");
        }
        
        if (!validateNotEmpty(membership.getPassword())) {
        	log.error("Password may not be empty.");
        	return new PSAccountCreateResult(STATUS.INVALID_PARAM, "Password may not be empty", "");
        }
        
        if(log.isDebugEnabled()){
    		log.debug("Http Header in the service is :" + header.getRequestHeaders());
    	}
        
        PSAccountCreateResult result;
        try
        {
            MultivaluedMap<String, String> headerParams = header.getRequestHeaders();
            String[] host = headerParams.getFirst("host").split(":");
            String customerSite = host[0].toUpperCase();
            String sessionId = membershipService.createAccount(membership.getEmail(), membership.getPassword(), 
                    membership.isConfirmationRequired(), membership.getConfirmationPage(), customerSite);
            result = new PSAccountCreateResult(STATUS.SUCCESS, "", sessionId);
        }
        catch (PSMemberExistsException e)
        {
       		log.error("Membership exists exception : " + e.getLocalizedMessage());
            result = new PSAccountCreateResult(STATUS.MEMBER_EXISTS, e.getLocalizedMessage(), "");
        }
        catch (PSAuthenticationFailedException e)
        {
       		log.error("Authentication failed exception : " + e.getLocalizedMessage());
            result = new PSAccountCreateResult(STATUS.AUTH_FAILED, e.getLocalizedMessage(), "");
        }
        catch (Exception e)
        {
       		log.error("Exception during create user : " + e.getLocalizedMessage());
            result = new PSAccountCreateResult(STATUS.UNEXPECTED_ERROR, e.getLocalizedMessage(), "");
        }
        
        return result;
    }
    
    /* (non-Javadoc)
	 * @see com.percussion.membership.services.impl.IPSMembershipRestService#changeStateAccount(com.percussion.membership.data.PSAccountSummary)
	 */
    @Override
	@PUT
    @Path("/admin/account")
    @RolesAllowed("deliverymanager")
    public void changeStateAccount(PSAccountSummary account)
    {
    	if(account == null) {
			log.error("Illegal argument passed. account cannot be null.");
			return;
	    }
        try
        {
            membershipService.changeStateAccount(account);
        }
        catch (Exception e)
        {
       		log.error("Could not change state account : " + e.getLocalizedMessage());
        	
            throw new WebApplicationException(e, Response.serverError().build());
        }
    }
    
    /* (non-Javadoc)
	 * @see com.percussion.membership.services.impl.IPSMembershipRestService#deleteAccount(java.lang.String)
	 */
    @Override
	@DELETE
    @Path("/admin/account/{email:.*}")
    @RolesAllowed("deliverymanager")
    public void deleteAccount(@PathParam("email") String email)
    {
        try
        {
            membershipService.deleteAccount(email);
        }
        catch (Exception e)
        {
       		log.error("Exception while deleting account : " + e.getLocalizedMessage());
        	
            throw new WebApplicationException(e, Response.serverError().build());
        }
    }
    
    /* (non-Javadoc)
	 * @see com.percussion.membership.services.impl.IPSMembershipRestService#getUser(java.lang.String)
	 */
    @Override
	@POST
    @Path("/session")
    @Produces(MediaType.APPLICATION_JSON)
    public PSGetUserResult getUser(PSUserSession psUserSession)
    {
        String sessionId = psUserSession.getSessionId();
        if (!validateNotEmpty(sessionId)) {
        	log.error("Illegal argument passed. session id cannot be empty.");
            return new PSGetUserResult(STATUS.INVALID_PARAM, "No session Id supplied", null);
        }
        
        PSGetUserResult result;
        try
        {
            PSUserSummary userSum = membershipService.getUser(sessionId);
            
            if(log.isDebugEnabled()){
        		log.debug("The user email is " + userSum.getEmail() +" and the user status is : " + userSum.getStatus());
        	}
            result = new PSGetUserResult(STATUS.SUCCESS, "", userSum);            
        }
        catch (Exception e)
        {
       		log.error("Exception while getting user : " + e.getLocalizedMessage());

            result = new PSGetUserResult(STATUS.UNEXPECTED_ERROR, e.getLocalizedMessage(), null);
        }
        
        return result;
    }
    
    /* (non-Javadoc)
	 * @see com.percussion.membership.services.impl.IPSMembershipRestService#login(com.percussion.membership.data.PSLoginRequest)
	 */
    @Override
	@POST
    @Path("/login")
    @Produces(MediaType.APPLICATION_JSON)    
    public PSLoginResult login(PSLoginRequest loginRequest)
    {
        Validate.notNull(loginRequest);
        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();
        
        if (!validateNotEmpty(email)){
        	log.error("Email may not be empty.");
            return new PSLoginResult(STATUS.INVALID_PARAM, "Email may not be empty", "");
        }
        
        if (!validateNotEmpty(password)){
        	log.error("Password may not be empty.");
            return new PSLoginResult(STATUS.INVALID_PARAM, "Password may not be empty", "");
        }
        
        if(log.isDebugEnabled()){
    		log.debug("Email in the login request is :" + loginRequest.getEmail());
    	}
        
        PSLoginResult result;
        try
        {
            String sessionId = membershipService.login(email, password);
            result = new PSLoginResult(STATUS.SUCCESS, "", sessionId);
        }
        catch (PSAuthenticationFailedException e)
        {
       		log.error("Could not log in! Authentication failed : " + e.getLocalizedMessage());
            result = new PSLoginResult(STATUS.AUTH_FAILED, e.getLocalizedMessage(), "");
        }
        catch (Exception e)
        {
       		log.error("Exception occurred while login : " + e.getLocalizedMessage());
            result = new PSLoginResult(STATUS.UNEXPECTED_ERROR, e.getLocalizedMessage(), "");
        }
        
        return result;
    }
    
    /* (non-Javadoc)
	 * @see com.percussion.membership.services.impl.IPSMembershipRestService#logout(java.lang.String)
	 */
    @Override
	@POST
    @Path("/logout")
    @Produces(MediaType.APPLICATION_JSON)
    public PSMembershipResult logout(PSUserSession psUserSession)
    {
        String sessionId = psUserSession.getSessionId();
        if (!validateNotEmpty(sessionId)){
        	log.error("Illegal argument passed. session id cannot be empty.");
            return new PSMembershipResult(STATUS.INVALID_PARAM, "No session Id supplied");
        }
        
        PSMembershipResult result;
        
        try
        {
            membershipService.logout(sessionId);
            result = new PSMembershipResult(STATUS.SUCCESS, "");
        }
        catch (Exception e)
        {
       		log.error("Exception occurred while logout : " + e.getLocalizedMessage());
            result = new PSMembershipResult(STATUS.UNEXPECTED_ERROR, e.getLocalizedMessage());
        }
        
        return result;
    }
    
    /* (non-Javadoc)
	 * @see com.percussion.membership.services.impl.IPSMembershipRestService#requestPwdReset(com.percussion.membership.data.PSResetRequest, javax.ws.rs.core.HttpHeaders)
	 */
    @Override
	@POST
    @Path("/pwd/requestReset")
    @Produces(MediaType.APPLICATION_JSON)    
    public PSMembershipResult requestPwdReset(PSResetRequest resetRequest, @Context HttpHeaders header)
    {
        Validate.notNull(resetRequest);
        String email = resetRequest.getEmail();
        String resetLinkUrl = resetRequest.getRedirectPage();
        
        if (!validateNotEmpty(email)){
        	log.error("Email may not be empty.");
            return new PSLoginResult(STATUS.INVALID_PARAM, "Email may not be empty", "");
        }
        
        if(log.isDebugEnabled()){
    		log.debug("Http Header in the service is :" + header.getRequestHeaders());
    	}
        
        PSMembershipResult result = null;
        try
        {
            String resetKey = membershipService.setResetKey(email, resetLinkUrl);
            result = new PSAccountCreateResult(STATUS.SUCCESS, "", resetKey);
        }
        catch (PSAuthenticationFailedException e)
        {
       		log.error("Authentication Failed! : " + e.getLocalizedMessage());
            result = new PSAccountCreateResult(STATUS.AUTH_FAILED, e.getLocalizedMessage(), "");
        }
        catch (Exception e)
        {
       		log.error("Exception occurred while requesting password reset : " + e.getLocalizedMessage());
            result = new PSAccountCreateResult(STATUS.UNEXPECTED_ERROR, e.getLocalizedMessage(), "");
        }
        
        return result;
    }
    
    /* (non-Javadoc)
	 * @see com.percussion.membership.services.impl.IPSMembershipRestService#validatePwdResetKey(java.lang.String)
	 */
    @Override
	@POST
    @Path("/pwd/validate/{resetKey:.*}")
    @Produces("application/json")
    public PSGetUserResult validatePwdResetKey( @PathParam("resetKey") String resetKey)
    {
        Validate.notNull(resetKey);
                
        PSGetUserResult result;
        
        if (!validateNotEmpty(resetKey))
        {
        	log.error("Parameters may not be empty.");
            result = new PSGetUserResult(STATUS.INVALID_PARAM, "Parameters may not be empty", null);
        }
        
        try
        {
            PSUserSummary sum = membershipService.validatePwdResetKey(resetKey);
            result = new PSGetUserResult(STATUS.SUCCESS, "", sum);
        }
        catch (PSResetPwdException e)
        {
       		log.error("Reset Password Exception! : " + e.getLocalizedMessage());
            result = new PSGetUserResult(STATUS.INVALID_RESET_KEY, e.getLocalizedMessage(), null);
        }
        catch (PSAuthenticationFailedException e)
        {
       		log.error("Authentication Failed! : " + e.getLocalizedMessage());
            result = new PSGetUserResult(STATUS.AUTH_FAILED, e.getLocalizedMessage(), null);
        }
        catch (Exception e)
        {
       		log.error("Exception occurred while validating password reset : " + e.getLocalizedMessage());
            result = new PSGetUserResult(STATUS.UNEXPECTED_ERROR, e.getLocalizedMessage(), null);
        }
       
        return result;
    }
    
    /* (non-Javadoc)
	 * @see com.percussion.membership.services.impl.IPSMembershipRestService#resetPwd(java.lang.String, com.percussion.membership.data.PSMembershipAccount)
	 */
    @Override
	@POST
    @Path("/pwd/reset/{resetKey:.*}")
    @Produces("application/json")
    public PSLoginResult resetPwd( @PathParam("resetKey") String resetKey, 
            PSMembershipAccount resetRequest)
    {
        Validate.notNull(resetRequest);
        String email = resetRequest.getEmail();
        String password = resetRequest.getPassword();
        
        if (!validateNotEmpty(resetKey) || !validateNotEmpty(email) || !validateNotEmpty(password))
        {
        	log.error("Parameters may not be empty.");
            return new PSLoginResult(STATUS.INVALID_PARAM, "Parameters may not be empty", "");
        }
        
        PSLoginResult result;
        try
        {
            String sessionId = membershipService.resetPwd(resetKey, email, password);
            result = new PSLoginResult(STATUS.SUCCESS, "", sessionId);
        }
        catch (PSResetPwdException e)
        {
       		log.error("Reset Password Exception! : " + e.getLocalizedMessage());
            result = new PSLoginResult(STATUS.INVALID_RESET_KEY, e.getLocalizedMessage(), "");
        }
        catch (PSAuthenticationFailedException e)
        {
       		log.error("Authentication Failed! : " + e.getLocalizedMessage());
            result = new PSLoginResult(STATUS.AUTH_FAILED, e.getLocalizedMessage(), "");
        }
        catch (Exception e)
        {
       		log.error("Exception occurred while resetting password : " + e.getLocalizedMessage());
            result = new PSLoginResult(STATUS.UNEXPECTED_ERROR, e.getLocalizedMessage(), "");
        }
        
        return result;
    }
    
    /* (non-Javadoc)
	 * @see com.percussion.membership.services.impl.IPSMembershipRestService#confirmAccount(java.lang.String)
	 */
    @Override
	@POST
    @Path("/registration/confirm/{rvkey:.*}")
    @Produces("application/json")
    public PSLoginResult confirmAccount( @PathParam("rvkey") String confirmKey)
    {
        if (!validateNotEmpty(confirmKey))
        {
        	log.error("Parameters may not be empty.");
            return new PSLoginResult(STATUS.INVALID_PARAM, "Parameters may not be empty", "");
        }
        
        PSLoginResult result;
        try
        {
            String memberId = membershipService.confirmAccount(confirmKey);
            result = new PSLoginResult(STATUS.SUCCESS, "", memberId);
        }
        catch (PSResetPwdException e)
        {
       		log.error("Reset Password Exception! : " + e.getLocalizedMessage());
            result = new PSLoginResult(STATUS.INVALID_RESET_KEY, e.getLocalizedMessage(), "");
        }
        catch (PSAuthenticationFailedException e)
        {
       		log.error("Authentication Failed! : " + e.getLocalizedMessage());
            result = new PSLoginResult(STATUS.AUTH_FAILED, e.getLocalizedMessage(), "");
        }
        catch (Exception e)
        {
       		log.error("Exception occurred while registration confirmation : " + e.getLocalizedMessage());
            result = new PSLoginResult(STATUS.UNEXPECTED_ERROR, e.getLocalizedMessage(), "");
        }
        
        return result;
    }

    /* (non-Javadoc)
	 * @see com.percussion.membership.services.impl.IPSMembershipRestService#findUserGroups()
	 */
    @Override
	@GET
    @Path("/admin/users")
    @RolesAllowed("deliverymanager")
    @Produces(MediaType.APPLICATION_JSON)  
    public List<PSUserSummary> findUserGroups()
    {
        try
        {
            return membershipService.findUsers();
        }
        catch (Exception e)
        {
       		log.error("Exception occurred while finding user groups : " + e.getLocalizedMessage());
            throw new WebApplicationException(e, Response.serverError().build());
        }
    }
    
    /* (non-Javadoc)
	 * @see com.percussion.membership.services.impl.IPSMembershipRestService#updateUserGroups(com.percussion.membership.data.PSUserGroup)
	 */
    @Override
	@PUT
    @Path("/admin/user/group/{siteName}")
    @RolesAllowed("deliverymanager")
    @Produces(MediaType.APPLICATION_JSON)  
    public void updateUserGroups(PSUserGroup userSummary)
    {
        Validate.notNull(userSummary);
        String email = userSummary.getEmail();
        String groups = userSummary.getGroups();
        
        if(log.isDebugEnabled()){
    		log.debug("The user email is " + email + " and the groups are " + groups);
    	}
                
        try
        {
            membershipService.setUserGroups(email, groups);
        }
        catch (Exception e)
        {
       		log.error("Exception occurred while updating user groups : " + e.getLocalizedMessage());
            throw new WebApplicationException(e, Response.serverError().build());
        }
    }
    
    /**
     * Validate the supplied string to be not <code>null</code> and not empty.
     * 
     * @param string The string to validate.
     * 
     * @return <code>true</code> if it is valid, <code>false</code> if not.
     */
    private boolean validateNotEmpty(String string)
    {
        return string != null && string.trim().length() > 0;
    }
    
    public String getVersion() {
    	
    	String version = super.getVersion();
    	
    	log.info("getVersion() from PSMembershipRestService ..." + version);
    	
    	return version;
    }

    @Override
    public Response updateOldSiteEntries(String prevSiteName, String newSiteName) {
        log.debug("Nothing to do for membership service. Prev name is: " + prevSiteName);
        return Response.status(Response.Status.NO_CONTENT).build();
    }
}
