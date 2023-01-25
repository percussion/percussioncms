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
