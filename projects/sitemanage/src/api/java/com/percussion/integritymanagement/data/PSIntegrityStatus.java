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
    private Set<PSIntegrityTask> tasks = new HashSet<PSIntegrityTask>();  
    
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
