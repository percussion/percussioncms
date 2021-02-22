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
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashSet;
import java.util.Set;

@Entity
@Cache (usage=CacheConcurrencyStrategy.READ_WRITE, 
      region = "PSIntegrityTask")
@Table(name = "PSX_INTEGRITYTASK")
@XmlRootElement(name = "task")
public class PSIntegrityTask  extends PSAbstractDataObject
{
    private static final long serialVersionUID = 1L;
    public static enum TaskStatus {
        SUCCESS, FAILED;
    }

    @Id
    @Column(name = "TASKID")
    private long taskId = -1L;
    
    @Basic
    @Column(name = "TOKEN")
    private String token;
    
    @Basic
    @Column(name = "NAME")
    private String name;

    @Basic
    @Column(name = "TYPE")
    private String type;

    @Basic
    @Column(name = "STATUS")
    @Enumerated(EnumType.STRING)
    private TaskStatus status;


    @Basic
    @Column(name = "MESSAGE")
    private String message;

    @OneToMany(targetEntity = PSIntegrityTaskProperty.class, fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "TASKID", nullable = false, insertable = false, updatable = false)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = "PSIntegrityTaskProperty")
    @Fetch(FetchMode. SUBSELECT)
    private Set<PSIntegrityTaskProperty> taskProperties = new HashSet<>();

    public long getTaskId()
    {
        return taskId;
    }


    public void setTaskId(long taskId)
    {
        this.taskId = taskId;
    }


    public String getToken()
    {
        return token;
    }


    public void setToken(String token)
    {
        this.token = token;
    }


    public String getName()
    {
        return name;
    }


    public void setName(String name)
    {
        this.name = name;
    }


    public String getType()
    {
        return type;
    }


    public void setType(String type)
    {
        this.type = type;
    }


    public TaskStatus getStatus()
    {
        return status;
    }


    public void setStatus(TaskStatus status)
    {
        this.status = status;
    }


    public String getMessage()
    {
        return message;
    }


    public void setMessage(String message)
    {
        this.message = message;
    }
    
    public Set<PSIntegrityTaskProperty> getTaskProperties()
    {
        return taskProperties;
    }


    public void setTaskProperties(Set<PSIntegrityTaskProperty> taskProperties)
    {
        this.taskProperties = taskProperties;
    }


    @Override
    public boolean equals(Object obj)
    {
       if ( !(obj instanceof PSIntegrityTask) )
          return false;
       
       // use "name" & "value" should be enough to avoid same pair more than once to make sure the property names are unique for a task
       PSIntegrityTask b = (PSIntegrityTask) obj;
       return new EqualsBuilder().append(name, b.name).append(type, b.type).isEquals();
    }
    
    @Override
    public int hashCode()
    {
       // use "name" should be enough to avoid same pair more than once to make sure the property names are unique for a task
       return new HashCodeBuilder().append(name).append(type).toHashCode();
    }

}
