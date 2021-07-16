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
package com.percussion.foldermanagement.service.impl;

import com.percussion.foldermanagement.data.PSFolderItem;
import com.percussion.foldermanagement.data.PSGetAssignedFoldersJobStatus;
import com.percussion.foldermanagement.data.PSWorkflowAssignment;
import com.percussion.foldermanagement.service.IPSFolderService;
import com.percussion.foldermanagement.service.IPSFolderService.PSWorkflowAssignmentInProgressException;
import com.percussion.foldermanagement.service.IPSFolderService.PSWorkflowNotFoundException;
import com.percussion.pathmanagement.service.IPSPathService.PSPathNotFoundServiceException;
import com.percussion.share.dao.IPSGenericDao.LoadException;
import com.percussion.share.data.PSLightWeightObject;
import com.percussion.share.data.PSLightWeightObjectList;
import com.percussion.share.service.exception.PSValidationException;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.List;

/**
 * Exposes the services provided by {@link PSFolderService} through a REST API,
 * and handles exceptions and HTTP error codes accordingly.
 * 
 * @author miltonpividori
 *
 */
@Path("/folders")
@Component("folderRestService")
public class PSFolderRestService
{
    private static final Logger log = LogManager.getLogger(PSFolderRestService.class);
    
    private IPSFolderService folderService;
    
    @Autowired
    public PSFolderRestService(IPSFolderService folderService)
    {
        this.folderService = folderService;
    }
    
    /**
     * Start the async job to get associated folders.
     * 
     * @param workflowName The name of the workflow to get folders for, not <code>null<code/> or empty, must be a valid workflow name.
     * @param path The root path to get folders from, not <code>null</code>.
     * 
     * @return The job id to use to get the status/result.
     */
    @GET
    @Path("/GetAssociatedFoldersJob/start/{workflowName}/{path:.*}")
    @Produces(MediaType.TEXT_PLAIN)
    public String startGetAssociatedFoldersJob(@PathParam("workflowName") String workflowName,
            @PathParam("path") String path,
            @QueryParam("includeFoldersWithDifferentWorkflow") @DefaultValue("false") Boolean includeFoldersWithDifferentWorkflow)
    {
        try {
            return folderService.startGetAssignedFoldersJob(workflowName, path, includeFoldersWithDifferentWorkflow);
        } catch (PSWorkflowNotFoundException e) {
            throw new WebApplicationException(e.getMessage());
        }
    }
    
    /**
     * Get the status/result for a running async job to get associated folders.
     * 
     * @param jobId The id of the job, must be a valid job.  
     * @return The status, {@link PSGetAssignedFoldersJobStatus#getStatus()} will return the resulting folder items once the job is complete, 
     * otherwise it will be <code>null</code>.  
     * If the job has failed, {@link PSGetAssignedFoldersJobStatus#getStatus()} 
     * will return -1, if completed it will return 100, and if running it will return 1.     * 
     */
    @GET
    @Path("/GetAssociatedFoldersJob/status/{jobId}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSGetAssignedFoldersJobStatus getAssociatedFoldersJobStatus(@PathParam("jobId") String jobId)
    {
        return folderService.getAssignedFoldersJobStatus(jobId);
    }
    
    /**
     * Cancel a running async job to get associated folders.  Method will block util the job 
     * is actually stopped.
     * 
     * @param jobId The id of the job, must be a valid job.  
     * 
     * @return The status, indicating the job has been aborted.
     */
    @GET
    @Path("/GetAssociatedFoldersJob/cancel/{jobId}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSGetAssignedFoldersJobStatus cancelAssociatedFoldersJob(@PathParam("jobId") String jobId)
    {
        return folderService.cancelAssignedFoldersJob(jobId);
    }
    
    
    @GET
    @Path("/{workflowName}/{path:.*}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getAssociatedFolders(@PathParam("workflowName") String workflowName,
            @PathParam("path") String path,
            @QueryParam("includeFoldersWithDifferentWorkflow") @DefaultValue("false") Boolean includeFoldersWithDifferentWorkflow)
    {
        String message = "There was an error getting the assosiated folders to workflow '" + workflowName
                + "', from the path '" + path + "'. ";
        try
        {
            GenericEntity<List<PSFolderItem>> folderItems =
                    new GenericEntity<List<PSFolderItem>>(folderService.getAssignedFolders(workflowName, "/" + path, includeFoldersWithDifferentWorkflow)) {}; 
            return Response.ok(folderItems).build();
        }
        catch(PSWorkflowNotFoundException e)
        {
            log.error("{}, Error: {}", message, e.getMessage());
            log.debug(e.getMessage(), e);
            return Response.status(Status.NOT_FOUND).entity(message + e.getMessage()).build();
        }
        catch(IllegalArgumentException e)
        {   
            // This means that either the workflow name or the path are empty.
            log.error("{}, Error: {}", message, e.getMessage());
            log.debug(e.getMessage(), e);
            return Response.status(Status.BAD_REQUEST).entity(message + e.getLocalizedMessage()).build();
        }
        catch(PSPathNotFoundServiceException | LoadException e)
        {
            // This means that the required path could not be found.
            log.error("{}, Error: {}", message, e.getMessage());
            log.debug(e.getMessage(), e);
            return Response.status(Status.NOT_FOUND).entity(message + e.getLocalizedMessage()).build();
        } catch (Exception e)
        {
            log.error("{}, Error: {}", message, e.getMessage());
            log.debug(e.getMessage(), e);
            return Response.serverError().entity(message + e.getLocalizedMessage()).build();
        }
    }
    
    @GET
    @Path("/workflowassignment/isInProgress")
    @Produces(MediaType.TEXT_PLAIN)
    public Boolean isContentWorkflowAssignmentInProgress()
    {
        return folderService.isContentWorkflowAssignmentInProgress();
    }
    
    @POST
    @Path("/workflowassignment")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response assignFoldersToWorkflow(PSWorkflowAssignment workflowAssignment)
    {
        String message = "There was an error associating the workflow '" + workflowAssignment.getWorkflowName()
        + "' to the folders: [" + StringUtils.join(workflowAssignment.getAssignedFolders(), ",") + "]. ";
        try
        {
            folderService.assignFoldersToWorkflow(workflowAssignment);
            return Response.noContent().build();
        }
        catch(PSWorkflowNotFoundException e)
        {
            log.error("{}, Error: {}", message, e.getMessage());
            log.debug(e.getMessage(), e);
            return Response.status(Status.NOT_FOUND).entity(message + e.getMessage()).build();
        }
        catch (PSWorkflowAssignmentInProgressException e)
        {
            log.error("{}, Error: {}", message, e.getMessage());
            log.debug(e.getMessage(), e);
            return Response.status(Status.CONFLICT).entity(e.getLocalizedMessage()).build();
        }
        catch(IllegalArgumentException e)
        {   
            // This means that is empty or does not exists.
            log.error("{}, Error: {}", message, e.getMessage());
            log.debug(e.getMessage(), e);
            return Response.status(Status.BAD_REQUEST).entity(message + e.getLocalizedMessage()).build();
        }
        catch (Exception e)
        {
            log.error("{}, Error: {}", message, e.getMessage());
            log.debug(e.getMessage(), e);
            return Response.serverError().entity(message + e.getLocalizedMessage()).build();
        }
    }
    
    @GET
    @Path("/folderpages/id/{id}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public List<PSLightWeightObject> getFolderPagesById(@PathParam("id") String id){
        try {
            return new PSLightWeightObjectList(folderService.getPagesFromFolder(id));
        } catch (IPSFolderService.PSFolderNotFoundException | IPSFolderService.PSPagesNotFoundException | PSValidationException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
            throw new WebApplicationException(e.getMessage());
        }
    }
    
}
