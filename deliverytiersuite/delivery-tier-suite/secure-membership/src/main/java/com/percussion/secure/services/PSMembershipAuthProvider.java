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
package com.percussion.secure.services;

import com.percussion.secure.data.PSMembershipConfiguration;
import com.percussion.utils.string.PSStringUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Works to provide authentication for Active Directory users and CM1 registered members
 * using Spring Security
 *
 */
public class PSMembershipAuthProvider extends  AbstractUserDetailsAuthenticationProvider
    {
        /**
         * Stores the current thread session id so that it can be obtained elsewhere to set the membership session cookie.
         */
    private static final ThreadLocal<String> SESSION_ID = new ThreadLocal<>();

    private static Client ms_client = ClientBuilder.newClient();
    
    private PSMembershipConfiguration membershipConfig;
    private PSLdapMembershipAuthProvider ldapMembershipAuthProvider;
    private String accessGroupFileName;
    private GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();
    

    public void setMembershipConfig(PSMembershipConfiguration membershipConfig)
    {
        this.membershipConfig = membershipConfig;
    }

	public void setLdapMembershipAuthProvider(
			PSLdapMembershipAuthProvider ldapMembershipAuthProvider) {
		this.ldapMembershipAuthProvider = ldapMembershipAuthProvider;
	}

	public void setAccessGroupFileName(String accessGroupFileName) {
		this.accessGroupFileName = accessGroupFileName;
	}

	/**
     * Get the current thread's session id for the authenticated subject, calling this method clears the current
     * session id.
     *
     * @return The session id, may be <code>null</code> if there is no authenticated session.
     */
    public static String getAuthenticatedSessionId()
    {
        String sessionid = SESSION_ID.get();
        SESSION_ID.set(null);
        return sessionid;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    	
    	// Check what kind of request is this. Membership or ldap secure membership? Then proceed accordingly.
    	
    	if(membershipConfig != null && membershipConfig.getUseLdap() != null && membershipConfig.getUseLdap().equalsIgnoreCase("yes")) {
    		return ldapMembershipAuthProvider.authenticate(authentication);
    	}

    	return super.authenticate(authentication);
    }

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails,
            UsernamePasswordAuthenticationToken authentication) throws AuthenticationException
    {
        // no-op for our impl
    }

    @Override
    protected UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken authentication)
            throws AuthenticationException
    {
        String groups = login(username, authentication.getCredentials().toString());
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        
        if(StringUtils.isEmpty(groups))
        	return new User(username, "", authorities);
        
        String[] groupList = PSStringUtils.getAllowedGroups(groups);
        List<String> groupsFromFile = PSMembershipAuthUtils.getAccessGroupsFromXML(accessGroupFileName);

        for (int i = 0; i < groupList.length; i++)
        {
            String group = StringUtils.strip(groupList[i]);
            
            if(groupsFromFile != null && !groupsFromFile.isEmpty() && groupsFromFile.contains("'"+group.toUpperCase()+"'"))
            	authorities.add(new SimpleGrantedAuthority(group));
        }
        
        if(!authorities.isEmpty())
        	return new User(username, "", authorities);
        
        logger.error("User Not Authorized - PSMembershipAuthProvider.createSuccessfulAuthentication()", new AuthorizationServiceException("User Not Authorized"));
		throw new AuthorizationServiceException("User Not Authorized");
    }

    /**
     * Login the user and return the groups the user is a member of.
     * 
     * @param userId Assumed not <code>null</code>
     * @param password Assumed not <code>null</code>
     * 
     * @return A comma-delimited list of group names, never <code>null</code>, may be empty.
     * 
     * @throws BadCredentialsException if the authentication fails for any reason.
     */
    private String login(String userId, String password) throws BadCredentialsException
    {
        try
        {
            String sessionId = authenticateMember(userId, password);
            
            String groups = getMemberGroups(sessionId, userId);
            
            SESSION_ID.set(sessionId);
            
            return groups;
        }
        catch (JSONException e)
        {
            // log an error and then auth should fail
            logger.error("Error authenticating user " + userId, e);
            logger.debug(e.getMessage(), e);
            throw new BadCredentialsException("");
        }
    }

    /**
     * Authenticate the supplied credentials
     * 
     * @return The session id if successful
     * 
     * @throws JSONException If there is an error parsing a REST response
     */
    private String authenticateMember(String userId, String password) throws JSONException
    {


        WebTarget webTarget = ms_client.target(membershipConfig.getBaseUrl() + "/perc-membership-services/membership/login");

        String request = "{\"email\": \"" + userId + "\", \"password\": \"" + password + "\"}";

        Invocation.Builder invocationBuilder =  webTarget.request(MediaType.APPLICATION_JSON_TYPE);
        Response response = invocationBuilder.post(Entity.entity(request, MediaType.APPLICATION_JSON));

        JSONObject resultObj = getJSONResult(response);
        String sessionId = resultObj.getString("sessionId");
        String status = resultObj.getString("status");
        String message = resultObj.getString("message");

        if (!"SUCCESS".equals(status))
        {
            if ("AUTH_FAILED".equals(status))
                throw new BadCredentialsException("");

            throw new BadCredentialsException(message);
        }
        
        return sessionId;
    }


    /**
     * Get the groups for the supplied session id
     *
     * @param sessionId The session id to use
     * @param userId Used for any error messages
     * 
     * @return The list of groups as a comma-delimited string, upper-cased to support case-insensitivity.
     * 
     * @throws JSONException If there is an error parsing a REST response
     */
    private String getMemberGroups( String sessionId, String userId) throws JSONException
    {
        String groups;

        WebTarget webTarget = ms_client.target(membershipConfig.getBaseUrl() + "/perc-membership-services/membership/session");

        String request = "{\"sessionId\": \"" + sessionId + "\"}";

        Invocation.Builder invocationBuilder =  webTarget.request(MediaType.APPLICATION_JSON_TYPE);
        Response response = invocationBuilder.post(Entity.entity(request, MediaType.APPLICATION_JSON));

        JSONObject resultObj = getJSONResult(response);
        JSONObject summaryObj = resultObj.getJSONObject("userSummary");

        String email = summaryObj.getString("email");

        if (email == null || email.isEmpty())
        {
            // should have worked since we just logged in
            logger.error("Unable to retrieve session info for user: " + userId);
            throw new BadCredentialsException("");
        }
        groups = summaryObj.getString("groups");
        if (groups == null)
            groups = "";

        return groups.toUpperCase();
    }
    


   
    /**
     * Check the response status and if successful return the response as a JSON object.
     * 
     * @param response The response to check, assumed not <code>null</code>
     * 
     * @return The JSON Object, never <code>null</code>.
     * 
     * @throws JSONException If the reponse cannot be parsed as a JSON object.
     * 
     * @throws BadCredentialsException If the response does not have a 200 status.
     */
    private JSONObject getJSONResult(Response response) throws JSONException, BadCredentialsException
    {
        if (response.getStatus() != 200)
        {
            throw new BadCredentialsException("Failed : HTTP error code : " + response.getStatus());
        }

        String jsonString =  response.readEntity(String.class);
        JSONObject resultObj = new JSONObject(jsonString);

        return resultObj;
    }
    
    /***
     * Default ctor
     */
    public PSMembershipAuthProvider(){
    	super();
    }
    
    
}
