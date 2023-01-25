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
package com.percussion.integritymanagement.data;

import com.percussion.share.data.PSAbstractDataObject;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "PSIntegrityTaskProperty")
@Table(name = "PSX_INTEGRITY_TASK_PROPERTIES")
public class PSIntegrityTaskProperty extends PSAbstractDataObject
{

    /**
    * 
    */
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "TASKRPROPERTYID")
    private long taskPropertyId = -1L;

    @Basic
    @Column(name = "TASKID", insertable = false,updatable = false)
    private long taskId;

    @Basic
    @Column(name = "PROPERTYNAME")
    private String name;

    @Basic
    @Column(name = "PROPERTYVALUE")
    private String value;

    /**
     * The default constructor.
     */
    public PSIntegrityTaskProperty()
    {
    }
    
    public PSIntegrityTaskProperty(String name, String value)
    {
        setName(name);
        setValue(value);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof PSIntegrityTaskProperty)) {
            return false;
        }

        // use "name" & "value" should be enough to avoid same pair more than
        // once to make sure the property names are unique within a PSPubServer
        PSIntegrityTaskProperty b = (PSIntegrityTaskProperty) obj;
        return new EqualsBuilder().append(name, b.name).append(value, b.value).isEquals();
    }

    @Override
    public int hashCode()
    {
        // use "name" should be enough to avoid same pair more than once to make
        // sure the property names are unique within a PSPubServer
        return new HashCodeBuilder().append(name).toHashCode();
    }

    /**
     * Get the property name
     * 
     * @return Returns the property name, never <code>null</code> or empty
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name The name to set, never <code>null</code> or empty
     */
    public void setName(String name)
    {
        if (StringUtils.isBlank(name))
        {
            throw new IllegalArgumentException("name may not be null or empty");
        }
        this.name = name;
    }

    /**
     * Get the value
     * 
     * @return Returns the value, may be <code>null</code> or empty.
     */
    public String getValue()
    {
        return value;
    }

    /**
     * @param value The value to set.
     */
    public void setValue(String value)
    {
        this.value = value;
    }

    public long getTaskPropertyId()
    {
        return taskPropertyId;
    }

    public void setTaskPropertyId(long taskPropertyId)
    {
        this.taskPropertyId = taskPropertyId;
    }

    public long getTaskId()
    {
        return taskId;
    }

    public void setTaskId(long taskId)
    {
        this.taskId = taskId;
    }

}
