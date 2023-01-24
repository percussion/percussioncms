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
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
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
    public enum TaskStatus {
        SUCCESS, FAILED;
    }

    @Id
    @Column(name = "TASKID")
    private long taskId = -1L;
    
    @Basic
    @Column(name = "TOKEN" ,nullable = false, insertable = false, updatable = false)
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
       if ( !(obj instanceof PSIntegrityTask) ) {
           return false;
       }
       
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
