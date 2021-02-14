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

import static com.percussion.share.dao.PSDateUtils.getDateFromString;
import static com.percussion.share.dao.PSDateUtils.getDateToString;

import java.text.ParseException;
import java.util.Date;

import org.apache.commons.lang.Validate;

import com.percussion.share.service.IPSDataService.DataServiceLoadException;

/**
 * Object to hold summary data about a registered user.
 * 
 * @author JaySeletz
 */
public class PSUserSummary
{
    private String email;
    private Date createdDate;
    private String status;
    private String groups;
    
    /**
     * Default ctor required by jax-b
     */
    public PSUserSummary()
    {
        
    }

    public String getEmail()
    {
        return email;
    }

    public String getCreatedDate()
    {
        return getDateToString(this.createdDate);
    }
    
    public void setEmail(String email)
    {
        Validate.notEmpty(email);
        this.email = email;
    }
    
    public void setCreatedDate(String createdDate)
    {
        Validate.notNull(createdDate);
        Date formattedDate;
        try {
            formattedDate = getDateFromString(createdDate);
        } catch (ParseException e) {
            throw new DataServiceLoadException("Error parsing date in setCreatedDate(String createdDate)"
                    + "in com.percussion.membership.data.PSUserSummary", e);
        }
        this.createdDate = formattedDate;
    }
    
    /**
     * 
     * @return the status of the account, never empty or <code>null</code>.
     */
    public String getStatus()
    {
        return status;
    }
    
    /**
     * 
     * @param status set the status of the account, never empty or <code>null</code>.
     */
    public void setStatus(String status)
    {
        Validate.notEmpty(status);
        this.status = status;
    }

    /**
     * @param groups the groups to set, may be empty or <code>null</code>.
     */
    public void setGroups(String groups)
    {
        this.groups = groups;
    }

    /**
     * @return the groups, may be empty or <code>null</code>.
     */
    public String getGroups()
    {
        return groups;
    }
}
