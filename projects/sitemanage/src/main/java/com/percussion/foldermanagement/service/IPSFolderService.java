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
package com.percussion.foldermanagement.service;

import com.percussion.error.PSException;
import com.percussion.foldermanagement.data.PSFolderItem;
import com.percussion.foldermanagement.data.PSGetAssignedFoldersJobStatus;
import com.percussion.foldermanagement.data.PSWorkflowAssignment;
import com.percussion.services.workflow.data.PSWorkflow;
import com.percussion.share.data.PSLightWeightObject;
import com.percussion.share.service.exception.IPSNotFoundException;
import com.percussion.share.service.exception.PSValidationException;

import java.util.List;

/**
 * Handles folder operations. It has operations to get folders by path and
 * workflow, and the possibility to change the workflow for the specified
 * folders.
 * 
 * @author miltonpividori
 * 
 */
public interface IPSFolderService
{

    /**
     * Gets the list of folders that are assigned to the specified workflow,
     * starting the search on the given path. If specified in the corresponding
     * parameter, it will include in the results those folders that are not
     * assigned to the given workflow.
     * 
     * @param workflowName the name of the workflow we are looking for. It
     *            cannot be <code>null</code> nor empty.
     * @param path The path from where the method will start searching. It
     *            cannot be <code>null</code> nor empty.
     * @param includeFoldersWithDifferentWorkflow if <code>true</code> the
     *            results will include folders that are not assigned to the
     *            given workflow. if <code>false</code>, the results will
     *            include only those folders that are assigned to the workflow
     *            or have at least one child that is.
     * @return a list of {@link PSFolderItem} objects. May be empty, but never
     *         <code>null</code>
     */
    public List<PSFolderItem> getAssignedFolders(String workflowName, String path,
            boolean includeFoldersWithDifferentWorkflow) throws Exception;

    /**
     * Starts a job to gets the list of folders that are assigned to the specified workflow,
     * starting the search on the given path. If specified in the corresponding
     * parameter, it will include in the results those folders that are not
     * assigned to the given workflow.
     * 
     * @param workflowName the name of the workflow we are looking for. It
     *            cannot be <code>null</code> nor empty.
     * @param path The path from where the method will start searching. It
     *            cannot be <code>null</code> nor empty.
     * @param includeFoldersWithDifferentWorkflow if <code>true</code> the
     *            results will include folders that are not assigned to the
     *            given workflow. if <code>false</code>, the results will
     *            include only those folders that are assigned to the workflow
     *            or have at least one child that is.
     *            
     * @return The job id, never <code>null</code>.
     */
    public String startGetAssignedFoldersJob(String workflowName, String path, boolean includeFoldersWithDifferentWorkflow) throws PSWorkflowNotFoundException;

    /**
     * Get the status of the job started by {@link #startGetAssignedFoldersJob(String, String, boolean)}
     * 
     * @param jobId the id of the job, must be a valid job id.
     * 
     * @return The status, never <code>null</code>.  When the job is complete, it will contain the results.
     */
    public PSGetAssignedFoldersJobStatus getAssignedFoldersJobStatus(String jobId); 
    
    /**
     * Determine if a background process to assign folders and/or their content to the correct workflow setting is in progress.
     * 
     * @return <code>true</code> if it is in progress, <code>false</code> if not.
     */
    public boolean isContentWorkflowAssignmentInProgress();
    
    /**
     * Sets the workflow specified in the argument to each folder in the list.
     * <p>
     * The given workflow must exists. If any of the given paths don't exist,
     * then it's silently skipped, and the operation continues normally.
     * 
     * @param workflowAssignment a {@link PSWorkflowAssignment} instance that
     *            cannot be <code>null</code> and with the following conditions
     *            on its fields:
     *            <ul>
     *            <li>workflowName: cannot be <code>null</code> nor empty.</li>
     *            <li>paths: it can be <code>null</code> or empty. In any case
     *            no operation is performed.</li>
     *            </ul>
     * 
     * @throws PSWorkflowNotFoundException If the specified workflow cannot be found.
     * @throws PSWorkflowAssignmentInProgressException if an assignment is currently in progress.
     */
    public void assignFoldersToWorkflow(PSWorkflowAssignment workflowAssignment) throws PSWorkflowNotFoundException, PSWorkflowAssignmentInProgressException;
    
    /**
     * For all content in the supplied folder paths, apply the folder's workflow property to the
     * content by changing the workflow of the content.  Work is done in a separate thread.
     * 
     * @param paths The folder paths to process, or <code>null</code> to process all folders w/no workflow assignement (those using the default workflow).
     * 
     * @return <code>true</code> if the thread was started, <code>false</code> if no work is to be done.
     */
    public boolean applyWorkflowToContent(String[] paths);
    
    /**
     * Validates that the workflow exists in the system. If is does not exists,
     * or the workflow name is empty or null, it throws an
     * {@link PSWorkflowNotFoundException}. If the workflow exists, it returns
     * it.
     * 
     * @param workflowName the name of the given workflow. Not
     *            <code>null</code> nor empty.
     * @return A {@link PSWorkflow} object containing the requested workflow.
     */
    public PSWorkflow validateWorkflow(String workflowName) throws PSWorkflowNotFoundException;

    /**
     * Returns the pages from the specified folder.
     * @param folderId expected to be string format of folder guid, if not throws PSFolderNotFoundException
     * @return List of PSLightWeightObject that hase name and id of the pages from the supplied folder, may be empty, never <code>null</code>.
     * @throws PSFolderNotFoundException if the supplied folderid doesn't correspond to a folder
     */
    public List<PSLightWeightObject> getPagesFromFolder(String folderId) throws PSFolderNotFoundException, PSValidationException, PSPagesNotFoundException;

    /**
     * Exception thrown if a requested workflow for assignment or for listing is
     * not found.
     * 
     * @author Santiago M. Murchio
     * 
     */
    public static class PSWorkflowNotFoundException extends PSException implements IPSNotFoundException
    {
        public PSWorkflowNotFoundException(String message)
        {
            super(message);
        }
    }
    
    /**
     * Exception thrown if a requested workflow for assignment or for listing is
     * not found.
     * 
     */
    public static class PSWorkflowAssignmentInProgressException extends PSException
    {
        public PSWorkflowAssignmentInProgressException(String message)
        {
            super(message);
        }
    }

    /**
     * Exception thrown if the supplied folder path or id doesn't correspond to a folder in system.
     * 
     */
    public static class PSFolderNotFoundException extends PSException implements IPSNotFoundException
    {
        public PSFolderNotFoundException(String message)
        {
            super(message);
        }
    }
    
    /**
     * Exception thrown if the pages from the supplied folder could not be retrieved.
     * 
     */
    public static class PSPagesNotFoundException extends PSException implements IPSNotFoundException
    {
        public PSPagesNotFoundException(String message)
        {
            super(message);
        }
    }

    /**
     * Cancel a running async job to get associated folders.  Method will block util the job 
     * is actually stopped.
     * 
     * @param jobId The id of the job, must be a valid job.  
     * 
     * @return The status, indicating the job has been aborted.
     */
    public PSGetAssignedFoldersJobStatus cancelAssignedFoldersJob(String jobId);

}
