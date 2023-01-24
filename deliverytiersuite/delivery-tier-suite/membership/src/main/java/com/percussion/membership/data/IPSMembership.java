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
package com.percussion.membership.data;

import java.util.Date;

/**
 * Data object representing a member managed by the membership service.
 * 
 * @author JaySeletz
 *
 */
public interface IPSMembership
{
    /**
     * The possible values for the status column in the table.
     * 
     * @author rafaelsalis
     */
    public static enum PSMemberStatus {
        Blocked,
        Active,
        Unconfirmed;
    }
    
    /**
     * Get the account id of this membership.
     * 
     * @return The account id, never <code>null</code> or empty, "0" if this 
     * membership has not been persisted.
     */
    public String getId();
    
    /**
     * Get the user id of this membership.
     * 
     * @return The user id, never <code>null</code> or empty.
     */
    public String getUserId();
    
    /**
     * Get the email address of this membership.
     * 
     * @return The email, never empty, may be <code>null</code> if not set.
     */
    public String getEmailAddress();
    
    /**
     * Get the password value stored with this membership.
     * 
     * @return The password, never <code>null</code> or empty.
     */
    public String getPassword();
    
    /**
     * Get the date and time when this membership account was last accessed,
     * may be used for determining if a session has timed out.
     *  
     * @return The date-time when last accessed, never <code>null</code>.
     */
    public Date getLastAccessed();

    
    /**
     * Get the last session id used by this account.
     * 
     * @return The session id, never empty, may be <code>null</code> if no session has been created
     * or if the previously used session has been expired and thus cleared.
     */
    public String getSessionId();
    
    /**
     * Set the account id.
     * 
     * @param accountId The id, may not be <code>null</code> or empty.
     */
    public void setId(String accountId);
    
    /**
     * Set the user Id of this membership account.
     * 
     * @param userId The id, may not be <code>null</code> or empty.
     */
    public void setUserId(String userId);
    
    /**
     * Set the email address of this membership account.
     * 
     * @param email The email address, may not be <code>null</code> or empty.
     */
    public void setEmailAddress(String email);
    
    /**
     * Set the password for this membership account.
     * 
     * @param password The password to set, may not be <code>null</code> or empty.
     */
    public void setPassword(String password);
    
    /**
     * Set the last accessed date for this membership account.
     * 
     * @param lastAccessed The last accessed date, may not be <code>null</code>.
     */
    public void setLastAccessed(Date lastAccessed);
    
    /**
     * Set the session id for this membership account.
     * 
     * @param sessionId The session id to set, may be empty, never <code>null</code>.
     */    
    public void setSessionId(String sessionId);

    /**
     * Set the key used to identify a password reset request for this membership account.
     * 
     * @param pwdResetKey The key, never empty, may be <code>null</code> to clear the key.
     */
    public void setPwdResetKey(String pwdResetKey);

    /**
     * Get the key used to identify a password reset request for this membership account.
     * 
     * @return The key, never empty, may be <code>null</code>.
     */
    public String getPwdResetKey();

    /**
     * Set the date this membership account was created.
     * 
     * @param createdDate The date, never <code>null</code>.
     */
    public void setCreatedDate(Date createdDate);

    /**
     * Get the date this membership account was created. 
     * 
     * @return The date, may be <code>null</code> if never set.
     */
    public Date getCreatedDate();
    
    /**
     * Get the status of the membership account. 
     * 
     * @return The status, an {@link PSMemberStatus} object, 
	 * never empty or <code>null</code>.
     */
    public PSMemberStatus getStatus();
    
    /**
     * Set the status of the membership account.
     * 
     * @param status The status, an {@link PSMemberStatus} object, 
	 * never empty or <code>null</code>.
     */
    public void setStatus(PSMemberStatus status);
    
    /**
     * Get the groups of the membership account. 
     * 
     * @return The groups, may be empty but <code>null</code>.
     */    
    public String getGroups();
    
    /**
     * Set the groups of the membership account.
     * 
     * @param groups The groups, may be empty but never <code>null</code>.
     */
    public void setGroups(String groups);
}
