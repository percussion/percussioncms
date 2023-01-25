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
import net.sf.oval.constraint.NotBlank;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Cache (usage=CacheConcurrencyStrategy.READ_WRITE, 
      region = "PSIntegrityStatus")
@Table(name = "PSX_INTEGRITYSTATUS")
@XmlRootElement(name = "integritystatus")
public class PSIntegrityStatus extends PSAbstractDataObject
{
    private static final long serialVersionUID = 1L;
    public static enum Status {
        RUNNING, SUCCESS, FAILED, CANCELLED;
    }
    @Id
    @NotBlank    
    @Column(name = "TOKEN")
    private String token;
    
    @Basic
    @Column(name = "STATUS")
    @Enumerated(EnumType.STRING)
    private Status status;

    @Basic
    @Column(name = "START_TIME")
    private Date startTime;

    @Basic
    @Column(name = "END_TIME")
    private Date endTime;

    @OneToMany(targetEntity = PSIntegrityTask.class, fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "TOKEN", nullable = false, insertable = false, updatable = false)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = "PSIntegrityTask")
    @Fetch(FetchMode. SUBSELECT)
    private Set<PSIntegrityTask> tasks = new HashSet<>();
    
    @Transient
    private long elapsedTime;

    public String getToken()
    {
        return token;
    }

    public void setToken(String token)
    {
        this.token = token;
    }

    public Status getStatus()
    {
        return status;
    }

    public void setStatus(Status status)
    {
        this.status = status;
    }

    public Date getStartTime()
    {
        return startTime;
    }

    public void setStartTime(Date startTime)
    {
        this.startTime = startTime;
    }

    public Date getEndTime()
    {
        return endTime;
    }

    public void setEndTime(Date endTime)
    {
        this.endTime = endTime;
    }

    @Transient
    public long getElapsedTime()
    {
        long elapsed = -1;
        if(endTime == null && startTime != null){
            elapsed = new Date().getTime() - startTime.getTime();
        }
        else if(endTime != null && startTime != null){
            elapsed = endTime.getTime() - startTime.getTime();
        }
        return elapsed;
    }

    public Set<PSIntegrityTask> getTasks()
    {
        return tasks;
    }

    public void setTasks(Set<PSIntegrityTask> tasks)
    {
        this.tasks = tasks;
    }
    

    
}
