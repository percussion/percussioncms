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
package com.percussion.membership.service;

import com.percussion.membership.data.PSAccountSummary;
import com.percussion.membership.data.PSUserGroup;
import com.percussion.membership.data.PSUserSummaries;
import com.percussion.membership.data.PSUserSummary;

/**
 * Service to proxy calls to the delivery tier membership services
 * 
 * @author JaySeletz
 */
public interface IPSMembershipService
{
    public static final String MEMBERSHIP = "/membership";
    public static final String ADMIN_USERS = "/admin/users";
    public static final String ADMIN_ACCOUNT = "/admin/account";
    public static final String ADMIN_USER_GROUP = "/admin/user/group";

    /**
     * Get the list of registered users.
     * 
     * @return The list of summaries, maybe empty but never <code>null</code>
     */
    public PSUserSummaries getUsers(String site);
    
    /**
     * Changes the state of an account.
     * 
     * @param account a {@link PSAccountSummary} object with the data
     * to process.
     * @return The list of summaries, maybe empty but never <code>null</code>
     */
    public PSUserSummaries changeStateAccount(PSAccountSummary account,String site);
    
    /**
     * Deletes an account.
     * 
     * @param email the email relative to the account to delete, 
     * never empty or <code>null</code>.
     * @return The list of summaries, maybe empty but never <code>null</code>
     */
    public PSUserSummaries deleteAccount(String email,String site);
    
    /**
     * Updates the groups of an account.
     * 
    * @param userSummary a {@link PSUserSummary} object with the data
     * to process.
     * @return The list of summaries, maybe empty but never <code>null</code>
     */
    public PSUserSummaries updateGroupAccount(PSUserGroup userGroup,String site);
}
