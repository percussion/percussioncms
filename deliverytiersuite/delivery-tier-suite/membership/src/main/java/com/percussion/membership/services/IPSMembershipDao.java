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

import com.percussion.membership.data.IPSMembership;
import com.percussion.membership.data.PSAccountSummary;
import com.percussion.membership.data.IPSMembership.PSMemberStatus;

import java.util.List;

/**
 * DAO service for the membership service.
 * 
 * @author JaySeletz
 */
public interface IPSMembershipDao
{
    /**
     * Search for a member with a session matching the supplied id.  
     * 
     * @param sessionId The session id to use, may not be <code>null</code> or empty.
     * 
     * @return The member, or <code>null</code> if not found. 
     * @throws Exception if there are any errors.
     */
    public IPSMembership findMemberBySessionId(String sessionId) throws Exception;
    
    /**
     * Search for a member matching the supplied user id.
     * 
     * @param userId The id to use, may not be <code>null</code> or empty.
     * 
     * @return The member, or <code>null</code> if not found. 
     */
    public IPSMembership findMemberByUserId(String userId);
    
    /**
     * Search for a member matching the supplied password reset key.
     * 
     * @param pwdResetKey The key to use, may not be <code>null</code> or empty.
     * 
     * @return The member, or <code>null</code> if not found. 
     */
    public IPSMembership findMemberByPwdResetKey(String pwdResetKey);
    
    /**
     * Create an instance of a member.  The member is not yet persisted.
     * 
     * @param userId The member's user id, may not be <code>null</code> or empty.
     * @param password The member's password, may not be <code>null</code> or empty.
     * 
     * @return The member, never <code>null</code>.
     * 
     * @throws PSMemberExistsException if a member with that user name already exists.
     * @throws Exception if there are any errors.
     */
    public IPSMembership createMember(String userId, String password, PSMemberStatus status) throws PSMemberExistsException, Exception;
    
    /**
     * Save the supplied member.  
     * 
     * @param member The member to save, may not be <code>null</code>.
     * 
     * @throws PSMemberExistsException if a member with that user name already exists.
     * @throws Exception if there are any errors.
     */
    public void saveMember(IPSMembership member) throws Exception;
    
    /**
     * Get all membership accounts
     * 
     * Deprecated for performance reasons:  @see {@link IPSMembershipDao.findMembers(PSDefaultRangedPage pager)}
     * @return A list of all members, sorted ascending by userId, never <code>null</code>, may be empty.
     * 
     * @throws Exception if there are any unexpected errors.
     */
    /* TODO Commented until paging is done 
     * @Deprecated  
     */
    public List<IPSMembership> findMembers() throws Exception;    

    /*
     * Pages 
     * @param pager
     * @return
     * @throws Exception
    
     TODO Commented until paging is done.
     *    public List<IPSMembership> findMembers(PSDefaultRangedPage pager) throws Exception;
     */
    
    /**
     * Changes the state of an account.
     * 
     * @param account a {@link PSAccountSummary} object with the data
     * to process.
     * @throws Exception if there are any unexpected errors.
     */
    public void changeStatusAccount(PSAccountSummary account) throws Exception;
    
    /**
     * Deletes an account.
     * 
     * @param email the email relative to the account to delete, 
     * never empty or <code>null</code>.
     * @throws Exception if there are any unexpected errors.
     */
    public void deleteAccount(String email) throws Exception;
    
}
