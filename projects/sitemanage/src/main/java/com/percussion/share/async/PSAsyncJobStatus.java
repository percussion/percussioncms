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
package com.percussion.share.async;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.fasterxml.jackson.annotation.JsonRootName;
import org.apache.commons.lang.Validate;

/**
 * Represents the current status of a running asynchronous job. Includes the status as
 * a value between <code>1-100</code> to indicate the % done and a corresponding
 * message. <code>100</code> indicates that the job has completed successfully.
 * If the job has terminated abnormally, the status will be {@link #ABORT_STATUS} and the
 * message will contain the error message.
 * 
 * @author JaySeletz
 */
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
@XmlType(name = "", propOrder = {"jobId", "status", "message"})
@XmlRootElement(name = "asyncJobStatus")
@JsonRootName("asyncJobStatus")
public class PSAsyncJobStatus
{
    private Long jobId;
    private Integer status;
    private String message;
    
    // Default Constructor needed for JAXB to unmarshall
    public PSAsyncJobStatus()
    {
    }
    
    public PSAsyncJobStatus(long jobId, int status, String message)
    {
        Validate.notNull(message);
        this.jobId = jobId;
        this.status = status;
        this.message = message;
    }

    public Long getJobId()
    {
        return jobId;
    }

    public void setJobId(Long jobId)
    {
        this.jobId = jobId;
    }

    public Integer getStatus()
    {
        return status;
    }

    public void setStatus(Integer status)
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
}
