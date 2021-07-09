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

package com.percussion.share.service.impl;

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
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
           throw new WebApplicationException(e);
        }
    }
    
    /**
     * Logger for this service.
     */
    public static final Logger log = LogManager.getLogger(PSAsyncJobStatusRestService.class);

}
