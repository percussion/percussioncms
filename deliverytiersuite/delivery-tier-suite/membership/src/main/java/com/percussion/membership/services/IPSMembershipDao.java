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
