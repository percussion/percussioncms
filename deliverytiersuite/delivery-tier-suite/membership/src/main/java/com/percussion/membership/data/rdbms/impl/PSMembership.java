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
package com.percussion.membership.data.rdbms.impl;

import com.percussion.membership.data.IPSMembership;
import com.percussion.membership.data.IPSMembership.PSMemberStatus;

import java.util.Calendar;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.time.DateUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * @author jayseletz
 *
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "PSMembership")
@Table(name = "PERC_MEMBERSHIP")
public class PSMembership implements IPSMembership
{
    @TableGenerator(
            name="membershipId", 
            table="PERC_ID_GEN", 
            pkColumnName="GEN_KEY", 
            valueColumnName="GEN_VALUE", 
            pkColumnValue="membershipId", 
            allocationSize=1)
    
    @Id
    @GeneratedValue(strategy=GenerationType.TABLE, generator="membershipId")
    private long id;
    
    @Basic
    @Column(length = 254, unique = true)
    private String userId;
    
    @Basic
    @Column(length = 254)
    private String emailAddress;
    
    @Basic
    @Column(length = 4000)
    private String password;
    
    @Basic    
    private Date lastAccessed;
    
    @Basic
    @Column(length = 4000)
    private String sessionId;
    
    @Basic
    @Column(length = 4000)
    private String pwdResetKey;
       
    @Basic    
    private Date createdDate;
    
    @Basic
    @Column(length = 4000)
    private PSMemberStatus status = PSMemberStatus.Unconfirmed;
    
    @Basic
    @Column(length = 4000)
    private String groups;

    public PSMembership()
    {
        
    }
    
    /**
     * Creates a new membership with the same values as the given one,
     * except for the id.
     * 
     * @param membership A membership to create a copy from, not <code>null</code>.
     */
    public PSMembership(IPSMembership membership)
    {
        Validate.notNull(membership, "membership may not be null");
        
        this.userId = membership.getUserId();
        this.emailAddress = membership.getEmailAddress();
        this.password = membership.getPassword();
        this.lastAccessed = membership.getLastAccessed();
        this.sessionId = membership.getSessionId();
        this.pwdResetKey = membership.getPwdResetKey();
        this.createdDate = membership.getCreatedDate();
        this.status = membership.getStatus();
        this.groups = membership.getGroups();
    }

    @Override
    public String getId()
    {
        return String.valueOf(id);
    }

    @Override
    public void setId(String accountId)
    {
        Validate.notEmpty(accountId, "accountId may not be null or empty");
        this.id = Long.valueOf(accountId);
    }
    
    @Override
    public String getUserId()
    {
        return userId;
    }
    
    @Override
    public void setUserId(String userId)
    {
        Validate.notEmpty(userId, "userId may not be null or empty");
        this.userId = userId;
    }

    @Override
    public String getEmailAddress()
    {
        return emailAddress;
    }

    @Override
    public void setEmailAddress(String email)
    {
        Validate.notEmpty(email, "The email may not be null or empty");
        this.emailAddress = email;
    }
    
    @Override
    public String getPassword()
    {
        return password;
    }
    
    @Override
    public void setPassword(String password)
    {
        Validate.notEmpty(password, "The password may not be null or empty");
        this.password = password;
    }

    @Override
    public Date getLastAccessed()
    {
        return lastAccessed;
    }
    
    @Override
    public void setLastAccessed(Date lastAccessed)
    {
        Validate.notNull(lastAccessed, "The last accessed date may not be null");
        this.lastAccessed = lastAccessed;
    }
    
    @Override
    public String getSessionId()
    {
        return sessionId;
    }
    
    @Override
    public void setSessionId(String sessionId)
    {
        Validate.notNull(sessionId);
        this.sessionId = sessionId;
    }

    @Override
    public String getPwdResetKey()
    {
        return pwdResetKey;
    }

    @Override
    public void setPwdResetKey(String pwdResetKey)
    {
        if (pwdResetKey != null)
            Validate.notEmpty(pwdResetKey, "The pwdResetKey may not be empty");
        
        this.pwdResetKey = pwdResetKey;
    }

    @Override
    public Date getCreatedDate()
    {
        return createdDate;
    }

    @Override
    public void setCreatedDate(Date createdDate)
    {
        Validate.notNull(createdDate);
        this.createdDate = createdDate;
    }
    
    /**
     * @return the status of the account.
     */
    @Override
    public PSMemberStatus getStatus()
    {
        if(status != null)
            return this.status;
         return PSMemberStatus.Unconfirmed;
    }

    /**
     * @param status the status to set to the account
     */
    @Override
    public void setStatus(PSMemberStatus status)
    {
        this.status = status;
    }
    
    /**
     * @return the group of the account.
     */
    @Override
    public String getGroups()
    {
        return groups;
    }

    /**
     * @param group the group to set to the account
     */
    @Override
    public void setGroups(String group)
    {
        this.groups = group;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof IPSMembership))
            return false;
        IPSMembership other = (IPSMembership) obj;
        EqualsBuilder bldr = new EqualsBuilder();
        bldr.append(this.emailAddress, other.getEmailAddress());
        bldr.append(this.groups, other.getGroups());
        bldr.append(this.password, other.getPassword());
        bldr.append(this.pwdResetKey, other.getPwdResetKey());
        bldr.append(this.sessionId, other.getSessionId());
        bldr.append(this.status, other.getStatus());
        bldr.append(this.userId, other.getUserId());
        
        if (this.createdDate == null || other.getCreatedDate() == null)
            bldr.append(this.createdDate, other.getCreatedDate());
        else
            bldr.append(DateUtils.truncate(this.createdDate, Calendar.SECOND), DateUtils.truncate(other.getCreatedDate(), Calendar.SECOND));
        
        if (this.lastAccessed == null || other.getLastAccessed() == null)
            bldr.append(this.lastAccessed, other.getLastAccessed());
        else
            bldr.append(DateUtils.truncate(this.lastAccessed, Calendar.SECOND), DateUtils.truncate(other.getLastAccessed(), Calendar.SECOND));
                
        return bldr.isEquals();
    }
    
    @Override
    public int hashCode()
    {
        return String.valueOf(userId).hashCode() + String.valueOf(password).hashCode();
    }

}
