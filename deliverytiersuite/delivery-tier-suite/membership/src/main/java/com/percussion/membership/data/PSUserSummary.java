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

import com.percussion.delivery.services.PSCustomDateSerializer;
import com.percussion.membership.data.IPSMembership.PSMemberStatus;

import java.util.Date;

import org.apache.commons.lang.Validate;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Object to hold summary data about a registered user.
 * 
 * @author JaySeletz
 */
public class PSUserSummary
{
    private String email;
    private Date createdDate;
    private PSMemberStatus status;
    private String groups;
    
    public PSUserSummary(IPSMembership member)
    {
        Validate.notNull(member);
        
        this.email = member.getEmailAddress();
        this.createdDate = member.getCreatedDate();
        this.status = member.getStatus();
        this.groups = member.getGroups() != null ? member.getGroups() : "";
    }

    /**
     * Get the user's email.
     * 
     * @return The email, not <code>null</code> or empty.
     */
    public String getEmail()
    {
        return email;
    }

    @JsonSerialize(using = PSCustomDateSerializer.class)
    public Date getCreatedDate()
    {
        return createdDate;
    }

    /**
     * @return The status, a {@link PSMemberStatus} object, never or 
	 * <code>null</code>.
     */
    public PSMemberStatus getStatus()
    {
        return status;
    }
    
    /**
     * @return a comma separated list of groups
     */
    public String getGroups()
    {
        return groups;
    }
}
