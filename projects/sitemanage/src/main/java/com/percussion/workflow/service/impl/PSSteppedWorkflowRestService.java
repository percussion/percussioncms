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
package com.percussion.workflow.service.impl;

import com.percussion.services.workflow.data.PSState;
import com.percussion.share.data.PSEnumVals;
import com.percussion.util.PSStringComparator;
import com.percussion.workflow.data.PSUiWorkflow;
import com.percussion.workflow.data.PSUiWorkflowList;
import com.percussion.workflow.service.IPSSteppedWorkflowService;
import com.percussion.workflow.service.IPSSteppedWorkflowService.PSWorkflowEditorServiceException;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.percussion.share.service.exception.PSParameterValidationUtils.rejectIfBlank;

/**
 * 
 * 
 * @author leonardohildt
 * @author rafaelsalis
 * 
 */
@Path("/workflows")
@Component("steppedWorkflowRestService")
@Lazy
public class PSSteppedWorkflowRestService
{
    private IPSSteppedWorkflowService service;

    /**
     * The comparator to use when ordering the list of states.
     */
    private static final PSStringComparator stringComparator = 
        new PSStringComparator(PSStringComparator.SORT_CASE_INSENSITIVE_ASC);
    
    @Autowired
    public PSSteppedWorkflowRestService(IPSSteppedWorkflowService service)
    {
        this.service = service;
    }

    /**
     * Load the workflow information based on the workflow name as the parameter, and 
     * builds a <code>PSUiWorkflow</code> object with it.
     * 
     * @param workflowName the name of the workflow of which it is going to retrieve
     * the information from. Cannot be empty or <code>null</code>
     * @return a <code><PSUiWorkflow></code> object maybe empty never <code>null</code>
     * @throws PSWorkflowEditorServiceException, if the supplied object is invalid.
     */
    @GET
    @Path("/{workflowName}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSUiWorkflow getWorflow(@PathParam("workflowName") String workflowName) 
            throws PSWorkflowEditorServiceException
    {
        return service.getWorkflow(workflowName);
    }
    
    /**
     * Retrieves a list of all workflow names existing.
     * 
     * @return a <code>PSEnumVals</code> containing the list of workflow names, never 
     * empty or <code>null</code>
     * @throws PSWorkflowEditorServiceException, if the supplied object is invalid.
     */
    @GET
    @Path("/")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSEnumVals getWorflowList() throws PSWorkflowEditorServiceException
    {
        return service.getWorkflowList();
    }
    
    /**
     * Retrieves a list of all workflow existing including their metadata information.
     * 
     * @return a list of <code>PSUiWorkflow</code>, never empty or <code>null</code>
     * @throws PSWorkflowEditorServiceException, if the supplied object is invalid.
     */
    @GET
    @Path("/metadata")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public List<PSUiWorkflow> getWorflowMetadataList() throws PSWorkflowEditorServiceException
    {
        return new PSUiWorkflowList(service.getWorkflowMetadataList());
    }

    /**
     * Retrieves the information of the default workflow (including id).
     * 
     * @return a <code>PSEnumVals</code> containing the information, never empty or <code>null</code>
     */
    @GET
    @Path("/metadata/default")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSEnumVals getDefaultWorkflowMetadata()
    {
        return service.getDefaultWorkflowMetadata();
    }
    
    /**
     * Creates a new workflow with the name provided.
     * 
     * @param workflowName the name of the workflow to be created
     * @param uiWorkflow the <code>PSUiWorkflow</code> object containing the workflow information to add to
     * that workflow. Must not be emtpy or <code>null</code>
     * @return a <code>PSUiWorkflow</code> object maybe empty never <code>null</code>
     * @throws PSWorkflowEditorServiceException, if the supplied object is invalid.
     */
    @POST
    @Path("/{workflowName:.*}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSUiWorkflow createWorflow(@PathParam("workflowName") String workflowName, PSUiWorkflow uiWorkflow) 
            throws PSWorkflowEditorServiceException
    {
        return service.createWorkflow(getReadableName(workflowName), uiWorkflow);
    }
    
    /**
     * Updates a workflow with the name provided.
     * 
     * @param workflowName the name of the workflow to be updated
     * @param uiWorkflow the <code>PSUiWorkflow</code> object containing the workflow information to update
     * that workflow (previous and actual workflow name). Must not be emtpy or <code>null</code>
     * @return a <code>PSUiWorkflow</code> object never empty or <code>null</code>
     * @throws PSWorkflowEditorServiceException, if the supplied object is invalid.
     */
    @PUT
    @Path("/{workflowName:.*}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSUiWorkflow updateWorkflow(@PathParam("workflowName") String workflowName, PSUiWorkflow uiWorkflow) 
            throws PSWorkflowEditorServiceException
    {
        return service.updateWorkflow(getReadableName(workflowName), uiWorkflow);
    }
    
    /**
     * Deletes a workflow with the name provided.
     * 
     * @param workflowName the name of the workflow to be deleted. Cannot be empty or <code>null</code>
     * @throws PSWorkflowEditorServiceException if the supplied workflow is not found in the system or there are items
     * associated with the workflow.
     */
    @DELETE
    @Path("/{workflowName:.*}")
    public void deleteWorkflow(@PathParam("workflowName") String workflowName) 
            throws PSWorkflowEditorServiceException
    {
        service.deleteWorkflow(workflowName);
    }
    
    /**
     * Creates a new step with the information provided in the supplied workflow object.
     * 
     * @param the name of the workflow
     * @param stepName the name of the step to be created
     * @param uiWorkflow the <code>PSUiWorkflow</code> object containing the state to add to
     * that workflow. Must not be empty or <code>null</code>
     * @return a <code>PSUiWorkflow</code> object never empty or <code>null</code>
     * @throws PSWorkflowEditorServiceException, if the supplied object is invalid.
     */
    @POST
    @Path("/{workflowName}/steps/{stepName:.*}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSUiWorkflow createStep(@PathParam("workflowName") String workflowName, @PathParam("stepName") String stepName, PSUiWorkflow uiWorkflow) 
            throws PSWorkflowEditorServiceException
    {
        return service.createStep(getReadableName(workflowName), getReadableName(stepName), uiWorkflow);
    }
    
    /**
     * Updates the step with the information provided in the supplied workflow object.
     * @param uiWorkflow the <code>PSUiWorkflow</code> object containing the state to add to
     * that workflow. Must not be <code>null</code>
     * 
     * @param workflowName the name of the workflow
     * @param stepName the name of the step to be updated
     * @param uiWorkflow the <code>PSUiWorkflow</code> object containing the state to update
     * that step (previous and actual step name). Must not be empty or <code>null</code>
     * @return PSUiWorkflow The updated workflow, never empty or <code>null</code>.
     * @throws PSWorkflowEditorServiceException, if the supplied object is invalid.
     */
    @PUT
    @Path("/{workflowName}/steps/{stepName:.*}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSUiWorkflow updateStep(@PathParam("workflowName") String workflowName, @PathParam("stepName") String stepName, PSUiWorkflow uiWorkflow) 
            throws PSWorkflowEditorServiceException
    {
        return service.updateStep(getReadableName(workflowName), getReadableName(stepName), uiWorkflow);
    }

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
    @DELETE
    @Path("/{workflowName}/steps/{stepName}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSUiWorkflow deleteStep(@PathParam("workflowName") String workflowName, @PathParam("stepName") String stepName) 
            throws PSWorkflowEditorServiceException
    {
        return service.deleteStep(workflowName, stepName);
    }
    
    /**
     * Gets the list of states that belong to the workflow passed as a parameter, and 
     * builds a <code>PSEnumVals</code> object with them, so they can be presented as 
     * choices in the screen.
     * If the workflow passed as parameter doesn't exist, the system selects the first
     * workflow from the list returned from service.getWorkflowList().
     * 
     * @param workflowName the name of the workflow from where we want to retrieve the states
     * @return a <code>PSEnumVals</code> object, may be empty but never <code>null</code>
     */
    @GET
    @Path("/{workflowName}/states/choices")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSEnumVals getStatesChoices(@PathParam("workflowName") String workflowName)
    {
        rejectIfBlank("getStateChoices", "workflowName", workflowName);
        
        // get the states and build the PSEnumVals object
        List<PSState> states = service.getStates(workflowName);
        
        if (states == null){
        	PSEnumVals workflows = service.getWorkflowList();
        	states = service.getStates(workflows.getEntries().get(0).getValue());
        }

        PSEnumVals choices = new PSEnumVals();
        
        if(states != null)
        {
            //List<String> orderedNames = orderStateNames(states);
            
            for(PSState state : states)
            {
                choices.addEntry(state.getName(), String.valueOf(state.getGUID().longValue()));
            }
        }
        
        return choices;
    }

    /**
     * Gets a list of workflow states, builds another list with their names,
     * and then returns that list ordered alphabetically in ascending order.
     * 
     * @param states the list of states. Assumed not <code>null</code>
     * @return 
     *  an ordered list of strings.
     */
    @SuppressWarnings("unchecked")
    private List<String> orderStateNames(List<PSState> states)
    {
        List<String> names = new ArrayList<String>();
        
        for(PSState state : states)
        {
            names.add(state.getName());
        }
        
        Collections.sort(names, stringComparator);
        
        return names;
    }
    
    
    /**
     * Cleans up a given name, in order to make it readable
     * and then returns it to the caller.
     * 
     * @param encodedName. The encoded name to be cleaned up. Assumed not <code>null</code>
     * @return the readable name without double quotes
     */
    private String getReadableName(String encodedName)
    {
        String decodedName = StringUtils.EMPTY;
        
        try
        {
            decodedName = encodedName.replaceAll("\"", "");
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to decode name = " + encodedName, e);
        }
        return decodedName;
    }
    
}
