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
    
    public void setCreatedDate(String createdDate) throws DataServiceLoadException {
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
