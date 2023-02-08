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
