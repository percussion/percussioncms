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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.membership.data;

import com.percussion.delivery.services.PSCustomDateSerializer;
import com.percussion.membership.data.IPSMembership.PSMemberStatus;

import java.util.Date;

import org.apache.commons.lang.Validate;
import org.codehaus.jackson.map.annotate.JsonSerialize;

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
