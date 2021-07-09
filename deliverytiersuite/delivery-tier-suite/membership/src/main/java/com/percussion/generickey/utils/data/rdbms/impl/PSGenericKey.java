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
package com.percussion.generickey.utils.data.rdbms.impl;

import com.percussion.generickey.data.IPSGenericKey;

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
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * @author leonardohildt
 *
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "PSGenericKey")
@Table(name = "PERC_GENERIC_KEY")
public class PSGenericKey implements IPSGenericKey
{
    @TableGenerator(
            name="genericKeyId", 
            table="PERC_ID_GEN", 
            pkColumnName="GEN_KEY", 
            valueColumnName="GEN_VALUE", 
            pkColumnValue="genericKeyId", 
            allocationSize=1)
    
    @Id
    @GeneratedValue(strategy=GenerationType.TABLE, generator="genericKeyId")
    private long id;
    
    @Basic
    @Column(length = 4000)
    private String genericKey;
    
    @Basic    
    private Date expirationDate; 
    
    public PSGenericKey()
    {
        
    }
    
    /**
     * Creates a new genericKey with the same values as the given one,
     * except for the id.
     * 
     * @param genericKey A generic key to create a copy from, not <code>null</code>.
     */
    public PSGenericKey(IPSGenericKey genericKey)
    {
        Validate.notNull(genericKey, "genericKey may not be null");
        
        this.expirationDate = genericKey.getExpirationDate();
        this.genericKey = genericKey.getGenericKey();
    }
   
    @Override
    public String getResetKeyId()
    {
        return String.valueOf(id);
    }

    @Override
    public void setResetKeyId(String genericKeyId)
    {
        Validate.notEmpty(genericKeyId, "genericKeyId may not be null or empty");
        this.id = Long.valueOf(genericKeyId);
    }

    @Override
    public void setExpirationDate(Date expirationDate)
    {
        this.expirationDate = expirationDate;
    }

    @Override
    public Date getExpirationDate()
    {
        return expirationDate;
    }
    
    @Override
    public String getGenericKey()
    {
        return genericKey;
    }

    @Override
    public void setGenericKey(String genericKey)
    {
        this.genericKey = genericKey;
    }

    @Override
    public boolean equals(Object obj)
    {
        return EqualsBuilder.reflectionEquals(this, obj);
    }
    
    @Override
    public int hashCode()
    {
        return HashCodeBuilder.reflectionHashCode(this);
    }
    
}
