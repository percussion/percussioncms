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

package com.percussion.share.service.impl;

import com.percussion.error.PSExceptionUtils;
import com.percussion.foldermanagement.service.IPSFolderService;
import com.percussion.share.async.IPSAsyncJobService;
import com.percussion.share.async.PSAsyncJobStatus;
import com.percussion.share.service.IPSAsyncJobStatusRestService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

/**
 * Implementation of {@link IPSAsyncJobStatusRestService}.
 *
 * @author federicoromanelli
 */
@Path("/jobstatus")
@Component("asyncJobStatusRestService")
@Lazy
public class PSAsyncJobStatusRestService implements IPSAsyncJobStatusRestService
{
    private IPSAsyncJobService asyncJobService;
    
    
    public PSAsyncJobStatusRestService()
    {
       
    }
    
    @Autowired
    public PSAsyncJobStatusRestService(IPSAsyncJobService asyncJobService)
    {
        this.asyncJobService = asyncJobService;
    }
    
    @Override
    @GET
    @Path("/{jobId}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSAsyncJobStatus getStatus(@PathParam("jobId") Long jobId)
    {
        PSAsyncJobStatus jobStatus = asyncJobService.getJobStatus(jobId);
        if (jobStatus == null)
        {
            return new PSAsyncJobStatus();
        }
        return jobStatus;
    }
    
    /**
     * Dummy method used to create an async job, used for unit testing purposes only 
     * @author federicoromanelli
     * 
     * @return Long - the id of the job created
     */
    @GET
    @Path("/startTestJob")
    @Produces(MediaType.TEXT_PLAIN)
    public Long startTestJob()
    {
        try {
            long jobId = asyncJobService.startJob("asyncJobTest", 1);
            log.info("Created dummy async job with id: " + jobId);
            return jobId;
        } catch (IPSFolderService.PSWorkflowNotFoundException e) {
            log.error(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
           throw new WebApplicationException(e);
        }
    }
    
    /**
     * Logger for this service.
     */
    public static final Logger log = LogManager.getLogger(PSAsyncJobStatusRestService.class);

}
