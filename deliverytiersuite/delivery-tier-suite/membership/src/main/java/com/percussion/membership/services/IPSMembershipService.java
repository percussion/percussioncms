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
package com.percussion.membership.services;

import com.percussion.membership.data.PSAccountSummary;
import com.percussion.membership.data.PSUserSummary;
import java.util.List;

import javax.ws.rs.Path;

/**
 * The membership service is used to store, retrieve and locate members. 
 *
 * @author JaySeletz
 */
public interface IPSMembershipService
{
    /**
     * Locate a matching session for the supplied session id and if found return the user's name
     * 
     * @param sessionId The session id to use, may not be <code>null</code> or empty.
     * 
     * @return The user summary, <code>null</code> if no valid session is found.
     * 
     * @throws Exception If there are any unexpected errors.
     */
    public PSUserSummary getUser(String sessionId) throws Exception;
    
    /**
     * Creates a membership account for the specified user, and also creates a valid session for
     * the user by calling the authentication provider.  
     * 
     * @param email Used as the user id for the account, may not be <code>null</code> or empty.
     * @param password The password for the account, may not be <code>null</code> or empty.
     * <code>null</code> or empty if they were not registering while trying to view a protected resource.
     * @param confirmationRequired Indicates if the activation through email is required for the account to create. 
     * May not be <code>null</code> or empty.
     * @param confirmationPage The confirmation page to include in the email sent to the user, if the confirmation
     * is required. May not be <code>null</code> or empty.
     * @param customerSite The customer website host address, never empty or <code>null</code>.
     * 
     * @return The session id for the user, not <code>null</code> or empty.
     * 
     * @throws PSMemberExistsException if a member with that user name already exists.
     * @throws PSAuthenticationFailedException if the member cannot be authenticated.
     * @throws Exception If there are any unexpected errors.
     */
    public String createAccount(String email, String password, boolean confirmationRequired, String confirmationPage, String customerSite) throws PSMemberExistsException, PSAuthenticationFailedException, Exception;
    
    /**
     * Authenticates the supplied credentials and creates a session.
     * 
     * @param email Used as the user id for the account, may not be <code>null</code> or empty.
     * @param password The password for the account, may not be <code>null</code> or empty.
     * 
     * @return The session id for the user, not <code>null</code> or empty.
     * 
     * @throws PSMemberExistsException if a member with that user name already exists.
     * @throws PSAuthenticationFailedException if the member cannot be authenticated.
     * @throws Exception If there are any unexpected errors.
     */
    public String login(String email, String password) throws PSAuthenticationFailedException, Exception;
    
    /**
     * Destroys the session for the supplied session id
     * 
     * @param sessionId The id, if not valid then method silently returns
     */
    public void logout(String sessionId) throws Exception;
    
    /**
     * Finds all users registered and returns summary info about them.
     * 
     * @return The list of summaries, never <code>null</code>, may be empty.
     * 
     * @throws Exception if there are any unexpected errors.
     */
    public List<PSUserSummary> findUsers() throws Exception;
    
    /**
     * Set a reset key for the supplied email account.
     * The reset key is only valid for 24 hours.
     * 
     * @param email Used as the user id for the account, may not be <code>null</code> or empty.
     * @param linkUrl The URL link to the reset page to be included in the email sent to the 
     *      user, not <code>null</code> or empty.
     * 
     * @return The email for the user, not <code>null</code> or empty.
     * 
     * @throws PSAuthenticationFailedException if the member cannot be authenticated.
     * @throws Exception If there are any unexpected errors.
     */
    public String setResetKey(String email, String linkUrl) throws PSAuthenticationFailedException, Exception;
    
    /**
     * Validate the reset key for the supplied email account.
     * 
     * @param resetKey The reset key to validate, not <code>null</code> or empty.
     * 
     * @return The summary for the user, not <code>null</code>.
     * 
     * @throws PSResetPwdException if the reset key is invalid.
     * @throws Exception If there are any unexpected errors.
     */
    public PSUserSummary validatePwdResetKey(String resetKey) throws PSAuthenticationFailedException, Exception;
    
    /** 
     * Find the user account given the supplied parameters, and set the new password. 
     * 
     * @param resetKey String containing the token key, may not be <code>null</code>.
     * @param email Used as the user id for the account, may not be <code>null</code> or empty.
     * @param password The password for the account, may not be <code>null</code> or empty.
     * 
     * @return The session id for the user, not <code>null</code> or empty.
     * 
     * @throws PSMemberExistsException if a member with that user name already exists.
     * @throws PSAuthenticationFailedException if the member cannot be authenticated.
     * @throws Exceptions If there are any unexpected errors.
     */
    public String resetPwd(String resetKey, String email, String password) throws PSAuthenticationFailedException, Exception;
    
    /**
     * Changes the state of an account.
     * 
     * @param account a {@link PSAccountSummary} object with the data
     * to process.
     */
    public void changeStateAccount(PSAccountSummary account) throws Exception;
    
    /**
     * Deletes an account.
     * 
     * @param email the email relative to the account to delete, 
     * never empty or <code>null</code>.
     */
    public void deleteAccount(String email) throws Exception;
	
	/** 
     * Confirm an existent account by finding the member and changing the account status to Enabled. 
     * 
     * @param confirmKey String containing the token key, may not be <code>null</code>.
     * 
     * @return The id for the user, not <code>null</code> may be empty.
     * 
     * @throws PSAuthenticationFailedException if the member cannot be authenticated.
     * @throws Exceptions If there are any unexpected errors.
     */
	public String confirmAccount(String confirmKey) throws PSAuthenticationFailedException, Exception;
	
	 /**
     * Set the groups for a given user
     * 
     * @param email the user email to update.
     * @param groups the user groups to set.
     * 
     */
	public void setUserGroups(String email, String groups) throws PSAuthenticationFailedException, Exception;

}
