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
package com.percussion.workflow.service;

import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.workflow.data.PSState;
import com.percussion.share.dao.IPSGenericDao;
import com.percussion.share.data.PSEnumVals;
import com.percussion.workflow.data.PSUiWorkflow;

import java.util.List;

/**
 * The workflow service is responsible for exposing the workflow information
 * 
 * 
 * @author leonardohildt
 * @author rafaelsalis
 * 
 */
public interface IPSSteppedWorkflowService
{
    
    
    /**
     * Constant for the metadata entry key prefix for workflow staging roles.
     */
    public static final String METADATA_STAGING_ROLES_KEY_PREFIX = "psx.workflow.staging.roles.";

    /**
     * Constant for workflow staging roles separator
     */
    public static final String METADATA_STAGING_ROLES_VALUE_SEPARATOR = ";";

    /**
     * Load the workflow information based on the workflow name as the parameter, and 
     * builds a <code>PSUiWorkflow</code> object with it.
     * 
     * @param workflowName the name of the workflow of which it is going to retrieve
     * the information from. Cannot be empty or <code>null</code>
     * @return a <code><PSUiWorkflow></code> object maybe empty never <code>null</code>
     * @throws PSWorkflowEditorServiceException, if the supplied object is invalid.
     */
    public PSUiWorkflow getWorkflow(String workflowName) throws PSWorkflowEditorServiceException;
    
    /**
     * Retrieves a list of all workflow names existing.
     * 
     * @return a <code>PSEnumVals</code> containing the list of workflow names, never 
     * empty or <code>null</code>
     * @throws PSWorkflowEditorServiceException, if the supplied object is invalid.
     */
    public PSEnumVals getWorkflowList() throws PSWorkflowEditorServiceException;

    /**
     * Retrieves a list of all workflow existing including their metadata information.
     * 
     * @return a list of <code>PSUiWorkflow</code>, never empty or <code>null</code>
     * @throws PSWorkflowEditorServiceException, if the supplied object is invalid.
     */
    public List<PSUiWorkflow> getWorkflowMetadataList() throws PSWorkflowEditorServiceException;
    
    /**
     * Retrieves the id and name information for the current default workflow.
     * 
     * @return a <code>PSEnumVals</code> containing the workflow name, never 
     * empty or <code>null</code>
     */
    public PSEnumVals getDefaultWorkflowMetadata();
    
    /**
     * Creates a new workflow with the name provided.
     * 
     * @param workflowName the name of the workflow to be created
     * @param uiWorkflow the <code>PSUiWorkflow</code> object containing the workflow information to create
     * that workflow. Must not be emtpy or <code>null</code>
     * @return a <code>PSUiWorkflow</code> object maybe empty never <code>null</code>
     * @throws PSWorkflowEditorServiceException, if the supplied object is invalid.
     */
    public PSUiWorkflow createWorkflow(String workflowName, PSUiWorkflow uiWorkflow) throws PSWorkflowEditorServiceException;
    
    /**
     * Updates a workflow with the name provided.
     * 
     * @param workflowName the name of the workflow to be updated
     * @param uiWorkflow the <code>PSUiWorkflow</code> object containing the workflow information to update
     * that workflow (previous and actual workflow name). Must not be emtpy or <code>null</code>
     * @return a <code>PSUiWorkflow</code> object never empty or <code>null</code>
     * @throws PSWorkflowEditorServiceException, if the supplied object is invalid.
     */
    public PSUiWorkflow updateWorkflow(String workflowName, PSUiWorkflow uiWorkflow) throws PSWorkflowEditorServiceException, PSNotFoundException, IPSGenericDao.LoadException, IPSGenericDao.SaveException;
    
    /**
     * Deletes a workflow with the name provided.
     * 
     * @param workflowName the name of the workflow to be deleted. Cannot be empty or <code>null</code>
     * @throws PSWorkflowEditorServiceException, if the supplied workflow name is not found or contains associated items.
     */
    public void deleteWorkflow(String workflowName) throws PSWorkflowEditorServiceException;

    /**
     * Creates a new step with the information provided in the supplied workflow object.
     * 
     * @param the name of the workflow
     * @param stepName the name of the step to be created. Must be identical to 
     * the attribute <code>stepName</code> within the <code>uiWorkflow</code> param
     * @param uiWorkflow the <code>PSUiWorkflow</code> object containing the state to add to
     * that workflow. Must not be empty or <code>null</code>
     * @return a <code>PSUiWorkflow</code> object never empty or <code>null</code>
     * @throws PSWorkflowEditorServiceException, if the supplied object is invalid.
     */
    public PSUiWorkflow createStep(String workflowName, String stepName, PSUiWorkflow uiWorkflow) throws PSWorkflowEditorServiceException;
    
    /**
     * Updates the step with the information provided in the supplied workflow object.
     * @param uiWorkflow the <code>PSUiWorkflow</code> object containing the state to add to
     * that workflow. Must not be <code>null</code>
     * 
     * @param workflowName the name of the workflow
     * @param stepName the name of the step to be updated. Must be identical to 
     * the attribute <code>previousStepName</code> within the <code>uiWorkflow</code> param
     * @param uiWorkflow the <code>PSUiWorkflow</code> object containing the state to update
     * that step (previous and actual step name). Must not be empty or <code>null</code>
     * @return PSUiWorkflow The updated workflow, never empty or <code>null</code>.
     * @throws PSWorkflowEditorServiceException, if the supplied object is invalid.
     */
    public PSUiWorkflow updateStep(String workflowName, String stepName, PSUiWorkflow uiWorkflow) throws PSWorkflowEditorServiceException;
    
    /**
     * Deletes the given step from the given workflow, re-points the submit transition of the previous step to next step and 
     * reject transitions of next step to the previous step.  
     * 
     * @param workflowName the name of the workflow
     * @param stepName the name of the step to be deleted
     * @return PSUiWorkflow The updated workflow, never empty or <code>null</code>.
     * @throws PSWorkflowEditorServiceException if the supplied workflow or step are not found in the system or the supplied step
     * name is a reserved step name or if items exist in that workflow.
     */
    public PSUiWorkflow deleteStep(String workflowName, String stepName) throws PSWorkflowEditorServiceException;
    

    /**
     * Finds all the states that belong to the workflow named as the parameter. 
     * If no workflow with that name is found, it returns <code>null</code> 
     * 
     * @param workflowName the name of the workflow of which we are going to
     * retrieve the states from. Maybe empty or <code>null</code>
     * @return a <code>List<PSState><code> object. Maybe empty or <code>null</code>
     */
    public List<PSState> getStates(String workflowName);

    /**
     * Thrown when an error is encountered in the workflow service.
     * 
     * @author leonardohildt
     * 
     */
    public static class PSWorkflowEditorServiceException extends RuntimeException
    {
        private static final long serialVersionUID = 1L;

        public PSWorkflowEditorServiceException()
        {
            super();
        }

        public PSWorkflowEditorServiceException(String message)
        {
            super(message);
        }

        public PSWorkflowEditorServiceException(String message, Throwable cause)
        {
            super(message, cause);
        }

        public PSWorkflowEditorServiceException(Throwable cause)
        {
            super(cause);
        }
    }

}
