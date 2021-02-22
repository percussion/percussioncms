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
package com.percussion.integritymanagement.data;

import com.percussion.share.data.PSAbstractDataObject;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

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
    @Column(name = "TASKID")
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
        if (!(obj instanceof PSIntegrityTaskProperty))
            return false;

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