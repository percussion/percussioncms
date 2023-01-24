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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PSGenericKey)) return false;

        PSGenericKey that = (PSGenericKey) o;

        if (id != that.id) return false;
        if (!getGenericKey().equals(that.getGenericKey())) return false;
        return getExpirationDate().equals(that.getExpirationDate());
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + getGenericKey().hashCode();
        result = 31 * result + getExpirationDate().hashCode();
        return result;
    }
}
