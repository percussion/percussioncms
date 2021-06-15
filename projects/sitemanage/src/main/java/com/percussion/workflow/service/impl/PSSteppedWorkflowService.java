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

import com.percussion.foldermanagement.service.IPSFolderService;
import com.percussion.maintenance.service.IPSMaintenanceManager;
import com.percussion.metadata.data.PSMetadata;
import com.percussion.metadata.service.IPSMetadataService;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.catalog.data.PSObjectSummary;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.services.notification.IPSNotificationListener;
import com.percussion.services.notification.IPSNotificationService;
import com.percussion.services.notification.PSNotificationEvent;
import com.percussion.services.notification.PSNotificationEvent.EventType;
import com.percussion.services.workflow.IPSWorkflowService;
import com.percussion.services.workflow.data.PSAgingTransition;
import com.percussion.services.workflow.data.PSAssignedRole;
import com.percussion.services.workflow.data.PSAssignmentTypeEnum;
import com.percussion.services.workflow.data.PSNotification;
import com.percussion.services.workflow.data.PSNotificationDef;
import com.percussion.services.workflow.data.PSState;
import com.percussion.services.workflow.data.PSTransition;
import com.percussion.services.workflow.data.PSTransitionRole;
import com.percussion.services.workflow.data.PSWorkflow;
import com.percussion.services.workflow.data.PSWorkflowRole;
import com.percussion.share.dao.IPSGenericDao;
import com.percussion.share.data.PSEnumVals;
import com.percussion.share.data.PSEnumVals.EnumVal;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.user.data.PSRoleList;
import com.percussion.user.service.IPSUserService;
import com.percussion.util.PSSiteManageBean;
import com.percussion.utils.PSNamedLockManager;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.workflow.PSWorkFlowUtils;
import com.percussion.workflow.data.PSUiWorkflow;
import com.percussion.workflow.data.PSUiWorkflowStep;
import com.percussion.workflow.data.PSUiWorkflowStepRole;
import com.percussion.workflow.data.PSUiWorkflowStepRoleTransition;
import com.percussion.workflow.service.IPSSteppedWorkflowService;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.percussion.workflow.service.impl.PSSteppedWorkflowMetadata.ARCHIVE_STATE;
import static com.percussion.workflow.service.impl.PSSteppedWorkflowMetadata.LIVE_STATE;
import static com.percussion.workflow.service.impl.PSSteppedWorkflowMetadata.PENDING_STATE;
import static com.percussion.workflow.service.impl.PSSteppedWorkflowMetadata.QUICK_EDIT_STATE;
import static com.percussion.workflow.service.impl.PSSteppedWorkflowMetadata.TRANSITION_NAME_APPROVE;
import static com.percussion.workflow.service.impl.PSSteppedWorkflowMetadata.TRANSITION_NAME_EDIT;
import static com.percussion.workflow.service.impl.PSSteppedWorkflowMetadata.TRANSITION_NAME_LIVE;
import static com.percussion.workflow.service.impl.PSSteppedWorkflowMetadata.TRANSITION_NAME_PUBLISH;
import static com.percussion.workflow.service.impl.PSSteppedWorkflowMetadata.TRANSITION_NAME_REJECT;
import static com.percussion.workflow.service.impl.PSSteppedWorkflowMetadata.TRANSITION_NAME_REMOVE;
import static com.percussion.workflow.service.impl.PSSteppedWorkflowMetadata.TRANSITION_NAME_SUBMIT;
import static com.percussion.workflow.service.impl.PSSteppedWorkflowMetadata.defaultTransitions;
import static com.percussion.workflow.service.impl.PSSteppedWorkflowMetadata.excludedStates;
import static com.percussion.workflow.service.impl.PSSteppedWorkflowMetadata.excludedWorkflows;
import static com.percussion.workflow.service.impl.PSSteppedWorkflowMetadata.orderedTransitions;

/**
 * See the interface for documentation.
 * 
 * 
 * @author leonardohildt
 * @author rafaelsalis
 * 
 */
@PSSiteManageBean("steppedWorkflowService")
@Lazy
public class PSSteppedWorkflowService implements IPSSteppedWorkflowService, IPSNotificationListener
{
    @Autowired
    public PSSteppedWorkflowService(IPSWorkflowService workflowService, IPSUserService userService, 
    		IPSFolderService folderService, IPSMaintenanceManager maintenanceManager,
    		IPSMetadataService metadataService)
    {
        this.workflowService = workflowService;
        this.userService = userService;
        this.folderService = folderService;
        this.maintenanceManager = maintenanceManager;
        this.metadataService = metadataService;
        lockMgr = new PSNamedLockManager(5000);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.percussion.user.service.IPSWorkflowEditorService#getWorkflow(java.
     * lang.String)
     */
    @Override
    public PSUiWorkflow getWorkflow(String workflowName) throws PSWorkflowEditorServiceException
    {
        if (StringUtils.isBlank(workflowName)) {
            throw new IllegalArgumentException("workflowName cannot be blank.");
        }

        PSUiWorkflow defaultWorkflow = new PSUiWorkflow();
        try
        {
            PSWorkflow workflow = getWorkflowFromName(workflowName);
            if (workflow == null)
            {
                throw new PSWorkflowEditorServiceException("Invalid workflow: " + workflowName);
            }

            defaultWorkflow = toPSUiWorkflow(workflow);
        }
        catch (Exception ex)
        {
            String msg = "Failed to get workflow for name = " + workflowName + ".";
            log.error("{}, Error: {}", msg, ex.getMessage());
            log.debug(ex.getMessage(),ex);
            throw new PSWorkflowEditorServiceException(msg, ex);

        }
        return defaultWorkflow;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * com.percussion.user.service.IPSWorkflowEditorService#getWorfklowList()
     */
    @Override
    public PSEnumVals getWorkflowList() throws PSWorkflowEditorServiceException
    {
        PSEnumVals workflowList = new PSEnumVals();
        
        try
        {
            List<PSObjectSummary> workflows = workflowService.findWorkflowSummariesByName("");
            
            if (workflows == null)
            {
                throw new PSWorkflowEditorServiceException("No workflows in the system.");
            }
            else
            {
                // Order the list
                WorkFlowNameComparator comp = new WorkFlowNameComparator();
                Collections.sort(workflows, comp);
                
                // Convert to <code>PSEnumVals</code> object filtering special workflows system
                for (PSObjectSummary workflow : workflows)
                {
                    if (!excludedWorkflows.contains(workflow.getLabel()))
                    {
                        workflowList.addEntry(workflow.getLabel(), String.valueOf(workflow.getGUID().longValue()));
                    }
                }               
            }
        }
        catch (Exception ex)
        {
            String msg = "Failed to get the list of workflows";
            log.error("{}, Error: {}", msg, ex.getMessage());
            log.debug(ex.getMessage(),ex);
            throw new PSWorkflowEditorServiceException(msg, ex);

        }
        return workflowList;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * com.percussion.user.service.IPSWorkflowEditorService#getWorkflowMetadataList()
     */
    @Override
    public List<PSUiWorkflow> getWorkflowMetadataList() throws PSWorkflowEditorServiceException
    {
        List<PSUiWorkflow> workflowList = new ArrayList<>();
        
        try
        {
            List<PSObjectSummary> sums = workflowService.findWorkflowSummariesByName(null);
            
            if (sums.isEmpty())
            {
                throw new PSWorkflowEditorServiceException("No workflows found in the system.");
            }
            
            WorkFlowNameComparator nameComp = new WorkFlowNameComparator();
            Collections.sort(sums, nameComp);
            
            for (PSObjectSummary sum : sums)
            {
                if (!excludedWorkflows.contains(sum.getLabel()))
                {
                    PSUiWorkflow uiWorkflow = new PSUiWorkflow();
                    uiWorkflow.setWorkflowName(sum.getLabel());
                    uiWorkflow.setWorkflowDescription(sum.getDescription());
                    if (sum.getLabel().equalsIgnoreCase(workflowService.getDefaultWorkflowName()))
                    {
                        uiWorkflow.setDefaultWorkflow(true);
                    }
                    else
                    {
                        uiWorkflow.setDefaultWorkflow(false);
                    }
                    //Set staging roles 
                    uiWorkflow.setStagingRoleNames(getStagingRoles(sum.getGUID().longValue()));

                    workflowList.add(uiWorkflow);
                }
            }
            
        }
        catch (Exception ex)
        {
            String msg = "Failed to get the list of workflows";
            log.error("{}, Error: {}", msg, ex.getMessage());
            log.debug(ex.getMessage(),ex);
            throw new PSWorkflowEditorServiceException(msg, ex);
        }
        return workflowList;
    }
    /*
     * (non-Javadoc)
     * @see com.percussion.workflow.service.IPSSteppedWorkflowService#getDefaultWorkflowMetadata()
     */
    public PSEnumVals getDefaultWorkflowMetadata()
    {
        PSWorkflow workflow = workflowService.getDefaultWorkflow();
        PSEnumVals workflowList = new PSEnumVals();
        workflowList.addEntry(workflow.getLabel(), String.valueOf(workflow.getGUID().longValue()));
        return workflowList;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.percussion.user.service.IPSWorkflowEditorService#createWorkflow(java.lang.String, com.percussion.workflowmanagement.data.PSUiWorkflow))
     * 
     * This method is synchronized until the entire process to create a new workflow is completed, as the validation regarding the existent workflows
     * on the system needs to be performed using an updated list of workflow names.
     *  
     */
    @Override
    public synchronized PSUiWorkflow createWorkflow(String workflowName, PSUiWorkflow uiWorkflow) throws PSWorkflowEditorServiceException
    {
        if(!uiWorkflow.getWorkflowName().equalsIgnoreCase(workflowName))
        {
            throw new PSWorkflowEditorServiceException("Parameters values are inconsistent with the values passed in the object");
        }
        
        String name = uiWorkflow.getWorkflowName();
        name = (name != null) ? name.trim() : "";

        boolean locked = tryToLockWorkflow(name);
        
        try
        {
            // Validate the workflow name
            validateWorkflowName(name, "");

            IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
            IPSGuid wfguid = gmgr.createGuid(PSTypeEnum.WORKFLOW);
            PSWorkflow newWorkflow = new PSWorkflow();
            
            // Get the base workflow definition from the xml file
            String validStringXml = loadBaseWorkflow();
            
            // Restore the workflow definition into the new workflow
            newWorkflow.fromXML(validStringXml);
            newWorkflow.setGUID(wfguid);
            long wfId = wfguid.getUUID();
            newWorkflow.setName(name);
            
            correctWorkflowRoles(newWorkflow);
            
            List<PSState> states = newWorkflow.getStates();
            for (PSState psState : states) {
                psState.setWorkflowId(wfId);
                if(psState.getName().equals(QUICK_EDIT_STATE))
                {
                    psState.setContentValidValue("i");
                }
                else if(psState.getName().equals(ARCHIVE_STATE))
                {
                    psState.setContentValidValue("u");
                }
                //Set the new workflow ID to the transitions for the current state
                List<PSTransition> trans = psState.getTransitions();
                for (PSTransition psTransition : trans) 
                {
                    psTransition.setWorkflowId(wfId);
                    List<PSNotification> transNotifs = psTransition.getNotifications();
                    for (PSNotification psNotification : transNotifs) 
                    {
                        psNotification.setWorkflowId(wfId);
                    }
                    
                    // ensure all transitions have the notification message set on them
                    List<PSNotification> notifications = psTransition.getNotifications();
                    if (notifications.isEmpty())
                    {
                        long notificationId = getNotificationId(newWorkflow);
                        PSNotification notif = createNotification(psTransition.getGUID(), wfguid, notificationId);
                        notifications.add(notif);
                        psTransition.setNotifications(notifications);                        
                    }

                    
                    List<PSTransitionRole> transRoles = psTransition.getTransitionRoles();
                    for (PSTransitionRole psTransitionRole : transRoles) 
                    {
                        psTransitionRole.setWorkflowId(wfId);
                    }
                }
                // Assign the list of modified transitions for the current state 
                // This is important to avoid issues when saving
                psState.setTransitions(trans);
                
                // Set the new workflow ID to the aging transitions for the current state
                List<PSAgingTransition> agtrans = psState.getAgingTransitions();
                for (PSAgingTransition psAgingTransition : agtrans) {
                    psAgingTransition.setWorkflowId(wfId);
                    List<PSNotification> transNotifs = psAgingTransition.getNotifications();
                    for (PSNotification psNotification : transNotifs) {
                        psNotification.setWorkflowId(wfId);
                    }
                }
                psState.setAgingTransitions(agtrans);
                                
                // Set the new workflow ID to the assigned roles for the current state
                List<PSAssignedRole> assignedRoles = psState.getAssignedRoles();
                List<PSWorkflowRole> wfRoles = newWorkflow.getRoles();
                int roleAdminId = getRoleIdByName(wfRoles, "Admin");
                int roleDesignerId = getRoleIdByName(wfRoles, "Designer");
                
                for (PSAssignedRole psAssignedRole : assignedRoles) 
                {
                    psAssignedRole.setWorkflowId(wfId);
                    int roleId = psAssignedRole.getGUID().getUUID();
                    PSAssignmentTypeEnum asType = (roleId == roleAdminId || roleId == roleDesignerId) ? PSAssignmentTypeEnum.ASSIGNEE : PSAssignmentTypeEnum.READER;
                    psAssignedRole.setAssignmentType(asType);
                }
            }

            // Set the new workflow ID to all workflow roles 
            List<PSWorkflowRole> wfRoles = newWorkflow.getRoles();
            for (PSWorkflowRole psWorkflowRole : wfRoles) {
                psWorkflowRole.setWorkflowId(wfId);
            }
           
            // Set the new workflow ID to all workflow notification definitions
            List<PSNotificationDef> wfNotifs = newWorkflow.getNotificationDefs();
            for (PSNotificationDef psNotificationDef : wfNotifs) {
                psNotificationDef.setWorkflowId(wfId);
            }
            
            // Save the workflow
            workflowService.saveWorkflow(newWorkflow);
            // Save the staging roles
            saveStagingRoles(wfId, uiWorkflow.getStagingRoleNames());
            if (uiWorkflow.isDefaultWorkflow())
            {
                PSWorkFlowUtils.setDefaultWorkflowName(name);
                folderService.applyWorkflowToContent(null);
            }
            
            // Return the workflow from the DB
            return getWorkflow(name);
        }
        catch (Exception ex)
        {
            log.error(ex.getMessage());
            log.debug(ex.getMessage(),ex);
            throw new PSWorkflowEditorServiceException(ex.getMessage(), ex);
        }
        finally
        {
            if (locked)
                tryToUnlockWorkflow(name);
        }
    }
    
    /**
     * Ensures the workflow has all system roles as workflow roles
     * 
     * @param workflow
     */
    private void correctWorkflowRoles(PSWorkflow workflow) throws PSDataServiceException {
        List<PSWorkflowRole> workflowRoles = workflow.getRoles();
        PSRoleList roleList = userService.getRoles();
        for (String roleName : roleList.getRoles())
        {
            int roleId = getRoleIdByName(workflowRoles, roleName);
            if (roleId == 0)
            {
                workflowService.addRoleToWorkflow(null, roleName, workflow);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.percussion.user.service.IPSWorkflowEditorService#updateWorkflow(java.lang.String, com.percussion.workflowmanagement.data.PSUiWorkflow)
     */
    @Override
    public PSUiWorkflow updateWorkflow(String workflowName, PSUiWorkflow uiWorkflow)
            throws PSWorkflowEditorServiceException, PSNotFoundException, IPSGenericDao.LoadException, IPSGenericDao.SaveException {
        if(!uiWorkflow.getPreviousWorkflowName().equalsIgnoreCase(workflowName))
        {
            throw new PSWorkflowEditorServiceException("Parameters values are inconsistent with the values passed in the object");
        }

        String workflowEditedName = uiWorkflow.getWorkflowName();
        workflowEditedName = (workflowEditedName != null) ? workflowEditedName.trim() : "";

        String previousWorkflowName = uiWorkflow.getPreviousWorkflowName();
        previousWorkflowName = (previousWorkflowName != null) ? previousWorkflowName.trim() : "";

        // Do the workflow name validation
        try
        {
            // As is a modify step, have previous workflow name
            validateWorkflowName(workflowEditedName, previousWorkflowName);
        }
        catch (PSWorkflowEditorServiceException e)
        {
            log.error(e.getMessage());
            throw e;
        }
        
        boolean locked = tryToLockWorkflow(previousWorkflowName);
        try
        {
            // Obtain the workflow from the DB
            PSWorkflow workflow = getWorkflowFromName(previousWorkflowName);
            if (workflow == null)
            {
                throw new PSWorkflowEditorServiceException("Invalid workflow: " + previousWorkflowName);
            }
            
            workflow = workflowService.loadWorkflow(workflow.getGUID());
            // Update the workflow name
            workflow.setName(workflowEditedName);
            // Save the workflow
            workflowService.saveWorkflow(workflow);
            
            //Save the staging roles
            saveStagingRoles(workflow.getGUID().longValue(), uiWorkflow.getStagingRoleNames());
            
            // Set the default workflow if correspond
            if (uiWorkflow.isDefaultWorkflow())
            {
                String previousDefault = PSWorkFlowUtils.getDefaultWorkflowProperty();
                PSWorkFlowUtils.setDefaultWorkflowName(workflowEditedName);
                if (!previousWorkflowName.equals(previousDefault))
                    folderService.applyWorkflowToContent(null);
            }
   
            // Return the workflow from the DB
            return getWorkflow(workflowEditedName);
        }
        finally
        {
            if (locked)
                tryToUnlockWorkflow(previousWorkflowName);
        }
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.percussion.user.service.IPSWorkflowEditorService#deletWorkflow(java.lang.String)
     */
    public void deleteWorkflow(String workflowName) throws PSWorkflowEditorServiceException
    {
        String workflowEditedName = workflowName.trim();
        
        boolean locked = tryToLockWorkflow(workflowEditedName);
        try
        {
            // Check if it is a system workflow name 
            List<String> systemWorkflows = Arrays.asList("LocalContent", "Local Content");
            String returnedName = findName(systemWorkflows, workflowEditedName);
            if (!StringUtils.isBlank(returnedName))
            {
                throw new PSWorkflowEditorServiceException(MessageFormat.format(WORKFLOW_IS_A_SYSTEM_WORKFLOW,
                        workflowEditedName));
            }
            // Obtain the workflow from the DB
            PSWorkflow workflow = getWorkflowFromName(workflowEditedName);
            if (workflow == null)
                throw new PSWorkflowEditorServiceException("Can't find the workflow by given name '" + workflowName
                        + "'.");
            // Check whether there are any items in this workflow
            Collection<Integer> cids = null;
            try
            {
                cids = cmsObjectMgr.findContentIdsByWorkflow(workflow.getGUID().getUUID());
            }
            catch (Exception e)
            {
                log.debug("Failed to find the items by workflow while deleting the workflow:", e);
                throw new PSWorkflowEditorServiceException("Unexpected error occurred while deleting the workflow. "
                        + "Please see the log for more details.");
            }
            if (cids.size() > 0)
            {
                throw new PSWorkflowEditorServiceException(MessageFormat.format(
                        PSSteppedWorkflowService.WORKFLOW_HAVE_ITEMS, workflowEditedName));
            }
            try
            {
                // Delete the workflow
                workflowService.deleteWorkflow(workflow.getGUID());
                //Delete staging roles
                deleteStagingRoles(workflow.getGUID().longValue());
            }
            catch (Exception ex)
            {
                String msg = ex.getMessage();
                log.error("Error: {}", ex.getMessage());
                log.debug(ex.getMessage(),ex);
                throw new PSWorkflowEditorServiceException(msg, ex);
            }
        }
        finally
        {
            if (locked) {
                tryToUnlockWorkflow(workflowEditedName);
            }
        }
    }

    /* 
     * (non-Javadoc)
     * 
     * @see com.percussion.user.service.IPSWorkflowEditorService#createStep(java.lang.String, java.lang.String, com.percussion.workflowmanagement.data.PSUiWorkflow)
     * 
     * This method is synchronized until the entire process to create a new step is completed, as the validation regarding the existent steps
     * on the system for the edited worklfow, needs to be performed using an updated list of step names.
     * 
     */
    @Override
    public synchronized PSUiWorkflow createStep(String workflowName, String stepName, PSUiWorkflow uiWorkflow) throws PSWorkflowEditorServiceException
    {        
        if(!uiWorkflow.getWorkflowName().equalsIgnoreCase(workflowName) ||
                !uiWorkflow.getWorkflowSteps().get(0).getStepName().equalsIgnoreCase(stepName))
        {
            throw new PSWorkflowEditorServiceException("Parameters values are inconsistent with the values passed in the object");
        }
        
        String name = uiWorkflow.getWorkflowSteps().get(0).getStepName();
        name = (name != null) ? name.trim() : "";

        try
        {
            // As is a new step, don't have previous step name
            validateStateName(workflowName, name, "");
        }
        catch (PSWorkflowEditorServiceException e)
        {
            log.error(e.getMessage());
            throw e;
        }

        PSWorkflow workflow;
        boolean locked = tryToLockWorkflow(workflowName);
        try
        {
            workflow = createStepInWorkflow(workflowName, uiWorkflow);
        }
        finally
        {
            if (locked) {
                tryToUnlockWorkflow(workflowName);
            }
        }

        // Return the workflow from the DB
        return getWorkflow(workflow.getName());
    }

    private boolean tryToLockWorkflow(String workflowName)
    {
        if (StringUtils.isBlank(workflowName)) {
            return false;
        }
        
        boolean locked = lockMgr.getLock(workflowName);
        if (!locked)
        {
            log.error("Timeout attempting to lock workflow for modification: " + workflowName);
            throw new RuntimeException("Timeout attempting to lock workflow for modification, please try again later.");
        }
        return locked;
    }
    
    private void tryToUnlockWorkflow(String workflowName)
    {
        boolean unlocked = lockMgr.releaseLock(workflowName);
        if (!unlocked)
        {
            log.warn("Unabled to release lock for workflow: " + workflowName);
        }
    }

    private PSWorkflow createStepInWorkflow(String workflowName, PSUiWorkflow uiWorkflow)
    {
        // Obtain the workflow from the DB
        PSWorkflow workflow = getWorkflowFromName(workflowName);
        if (workflow == null)
        {
            throw new PSWorkflowEditorServiceException("Invalid workflow: " + workflowName);
        }

        workflow = workflowService.loadWorkflow(workflow.getGUID());

        String previousStateName = uiWorkflow.getPreviousStepName();

        // Load the previous workflow state by name
        PSState previousState = null;
        for (PSState psState : workflow.getStates())
        {
            if (psState.getLabel().equalsIgnoreCase(previousStateName))
            {
                previousState = psState;
                break;
            }
        }

        if (previousState == null) {
            throw new IllegalArgumentException(MessageFormat.format(PSSteppedWorkflowService.STATE_NOT_FOUND, previousStateName));
        }

        PSState nextState = getToStateByTransition(workflow, previousState, TRANSITION_NAME_SUBMIT);

        if (nextState == null) {
            throw new IllegalArgumentException("The target step for " + previousStateName
                    + " was not found in the workflow.");
        }

        PSState pendingState = workflowService.loadWorkflowStateByName("Pending", workflow.getGUID());


        // Using the object passed from the client to create the new step
        for (PSUiWorkflowStep workflowUiStep : uiWorkflow.getWorkflowSteps())
        {
            List<PSState> states = workflow.getStates();

        	// Create the new state
            IPSGuid guid = workflow.getGUID();
            PSState newState = workflowService.createState(guid);
            newState.setName(workflowUiStep.getStepName());
            newState.setDescription(workflowUiStep.getStepName());
            newState.setSortOrder(previousState.getSortOrder() + 10);

            // Update the sort order for other states
            for (PSState stateUpdate : states)
            {
                if (stateUpdate.getSortOrder() > previousState.getSortOrder())
                {
                    stateUpdate.setSortOrder(stateUpdate.getSortOrder() + 10);
                }
            }

            //Add roles to the state
        	List<String> stepRoleNames = new ArrayList<>();
        	List<String> stepRoleNamesWithEnableNotification = new ArrayList<>();
            for (PSUiWorkflowStepRole stepRole : workflowUiStep.getStepRoles()) 
            {
            	stepRoleNames.add(stepRole.getRoleName());
            	if (stepRole.isEnableNotification()) {
                    stepRoleNamesWithEnableNotification.add(stepRole.getRoleName());
                }
    		}
            addWorkflowRolesToState(workflow, newState, stepRoleNames, stepRoleNamesWithEnableNotification);
            
            
            // Add the new state to current workflow states
            states.add(newState);

            // Repoint the previous and next state submit and reject transitions to point to the newly created state.
            repointTransitions(previousState, nextState, newState.getStateId());
    		
            Map<String,List<String>> transRoles = convertStepRolesToTransitionRoles(workflowUiStep.getStepRoles());
            
            addTransitionsToState(workflow, newState, previousState, nextState, pendingState, transRoles);
        }
            
        // save the workflow
        workflowService.saveWorkflow(workflow);
        return workflow;
    }
	
	/*
	 * (non-Javadoc)
	 * @see com.percussion.workflowmanagement.service.IPSWorkflowEditorService#updateStep(java.lang.String, java.lang.String, com.percussion.workflowmanagement.data.PSUiWorkflow)
	 */
    @Override
	public PSUiWorkflow updateStep(String workflowName, String stepName, PSUiWorkflow uiWorkflow) throws PSWorkflowEditorServiceException 
	{
        if(!uiWorkflow.getWorkflowName().equalsIgnoreCase(workflowName) ||
                !uiWorkflow.getPreviousStepName().equalsIgnoreCase(stepName))
        {
            throw new PSWorkflowEditorServiceException("Parameters values are inconsistent with the values passed in the object");
        }
        
        String stepEditedName = uiWorkflow.getWorkflowSteps().get(0).getStepName();
        stepEditedName = (stepEditedName != null) ? stepEditedName.trim() : "";

    	// Do the step name validation
        String previousStepName = uiWorkflow.getPreviousStepName();
        previousStepName = (previousStepName != null) ? previousStepName.trim() : "";
        
        // If the step name is Approved, change the names to update the Quick Edit state
        // The names for locked steps are not editable 
        if(stepEditedName.equals("Approved"))
        {
            stepEditedName = QUICK_EDIT_STATE;
            previousStepName = QUICK_EDIT_STATE;
        }

        try
        {
            // As is a modify step, have previous step name
            validateStateName(uiWorkflow.getWorkflowName(), stepEditedName, previousStepName);
        }
        catch (PSWorkflowEditorServiceException e)
        {
            log.error(e.getMessage());
            throw e;
        }
        
        boolean locked = tryToLockWorkflow(workflowName);
              
        try
        {
            // Obtain the workflow from the DB
            PSWorkflow workflow = getWorkflowFromName(uiWorkflow.getWorkflowName());
            if(workflow == null) {
                throw new PSWorkflowEditorServiceException("Can't find the workflow by given name '" + uiWorkflow.getWorkflowName() + "'.");
            }
            workflow = workflowService.loadWorkflow(workflow.getGUID());

            // Update the step name
            String stepByPreviousName = previousStepName;
            List<PSState> states = workflow.getStates();
            PSState currentState = null;
            for (PSState state : states) 
            {
                if(state.getName().equals(stepByPreviousName))
                {
                    currentState = state;
                    currentState.setName(stepEditedName);
                    break;
                }
            }
            
            if(currentState == null) {
                throw new PSWorkflowEditorServiceException(MessageFormat.format(PSSteppedWorkflowService.STATE_NOT_FOUND, stepByPreviousName));
            }
            
            //Update the step roles 
            List<String> stepRoleNames = new ArrayList<>();
            List<PSUiWorkflowStepRole> stepRoles = uiWorkflow.getWorkflowSteps().get(0).getStepRoles();
            List<String> stepRoleNamesWithEnableNotification = new ArrayList<>();
            for (PSUiWorkflowStepRole stepRole : stepRoles) 
            {
                stepRoleNames.add(stepRole.getRoleName());
                if (stepRole.isEnableNotification()) {
                    stepRoleNamesWithEnableNotification.add(stepRole.getRoleName());
                }
            }
            currentState.setAssignedRoles(new ArrayList<>());
            addWorkflowRolesToState(workflow, currentState, stepRoleNames, stepRoleNamesWithEnableNotification); 
      
            //Update the transition roles
            Map<String,List<String>> transRoles = convertStepRolesToTransitionRoles(stepRoles);
            List<PSTransition> transitions = new ArrayList<>();
            
            for(Entry<String, List<String>> entry : transRoles.entrySet()) 
            {
                PSTransition transition = getTransitionByName(currentState, entry.getKey());
                if(transition == null) {
                    throw new PSWorkflowEditorServiceException("Can't find the transition by given name " + entry.getKey());
                }
                List<PSTransitionRole> transitionRoles = new ArrayList<>();
                List<PSTransitionRole> tempRoles = createTransitionRoles(workflow, transition, entry.getValue());
                transitionRoles.addAll(tempRoles);
                transition.setTransitionRoles(transitionRoles);
                transitions.add(transition);
                
                if(currentState.getName().equals(QUICK_EDIT_STATE) && entry.getKey().contentEquals(TRANSITION_NAME_PUBLISH))
                {               
                    PSTransition updatedRemoveTransition = updateRemoveTransition(workflow, currentState, entry.getValue(), stepRoleNamesWithEnableNotification);
                    transitions.add(updatedRemoveTransition);
                }
            }
            currentState.setTransitions(transitions);
            
            //Add those roles to Edit transition of Pending and Live states
            if(currentState.getName().equals(QUICK_EDIT_STATE))
            {
                updateApprovedState(workflow, stepRoleNames, stepRoleNamesWithEnableNotification);
            }
            
            workflowService.saveWorkflow(workflow);
            return getWorkflow(workflow.getName());
        }
        finally
        {
            if (locked) {
                tryToUnlockWorkflow(workflowName);
            }
        }
	}

	/*
	 * (non-Javadoc)
	 * @see com.percussion.workflowmanagement.service.IPSWorkflowEditorService#deleteStep(java.lang.String, java.lang.String)
	 */
    @Override
	public PSUiWorkflow deleteStep(String workflowName, String stepName) throws PSWorkflowEditorServiceException 
	{
        boolean locked = tryToLockWorkflow(workflowName);
        try
        {
            String name = stepName.trim();
            PSWorkflow workflow = getWorkflowFromName(workflowName);
            
            if(workflow == null) {
                throw new PSWorkflowEditorServiceException("Can't find the workflow by given name '" + workflowName + "'.");
            }
            workflow = workflowService.loadWorkflow(workflow.getGUID());
            List<PSState> states = workflow.getStates();
            PSState currentState = null;
            for (PSState state : states) 
            {
            	if(state.getName().equalsIgnoreCase(name))
            	{
            		currentState = state;
            		break;
            	}
            }
            if(currentState == null) {
                throw new PSWorkflowEditorServiceException(MessageFormat.format(PSSteppedWorkflowService.STATE_NOT_FOUND, name));
            }
            
            //Check if it is a system state 
            List<String> systemStates = Arrays.asList("Draft", "Review", "Approved", "Archive");
            String returnedName = findName(systemStates, name);
            if (!returnedName.isEmpty())
            {
                throw new PSWorkflowEditorServiceException(STATE_IS_A_SYSTEM_STATE);
            }
            //Check whether there are any items in this state
            Collection<Integer> cids = null;
            try
            {
            	cids = cmsObjectMgr.findContentIdsByWorkflowStatus(workflow.getGUID().getUUID(), 
            			currentState.getGUID().getUUID());
            }
            catch (Exception e) 
            {
                log.error("Failed to find the items by workflow and state while deleteing the step, Error: {}", e.getMessage());
                log.debug(e.getMessage(),e);
            	throw new PSWorkflowEditorServiceException("Unexpected error occurred while deleting the step. " +
            			"Please see the log for more details.");
            }
            if(cids.size()>0)
            {
                throw new PSWorkflowEditorServiceException(MessageFormat.format(
                        PSSteppedWorkflowService.STATE_HAVE_ITEMS, name));
            }
            PSState prevState = getToStateByTransition(workflow, currentState, TRANSITION_NAME_REJECT);
            PSState nextState = getToStateByTransition(workflow, currentState, TRANSITION_NAME_SUBMIT);
            repointTransitions(prevState, nextState);
            
            //Reset the sort order of the steps
            long currStateSortOrder = currentState.getSortOrder();
            for (PSState state : states)
            {
                if (state.getSortOrder() > currStateSortOrder)
                {
                	state.setSortOrder(state.getSortOrder() - 10);
                }
            }
            
            states.remove(currentState);
            workflowService.saveWorkflow(workflow);
            return getWorkflow(workflowName);
        }
        finally
        {
            if (locked) {
                tryToUnlockWorkflow(workflowName);
            }
        }
    	
	}
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * com.percussion.user.service.IPSWorkflowEditorService#getStatesChoices
     * (java.lang.String)
     */
    @Override
    public List<PSState> getStates(String workflowName)
    {
        if (!StringUtils.isEmpty(workflowName))
        {
            PSWorkflow workflow = getWorkflowFromName(workflowName);
            if (workflow != null)
            {
                return workflow.getStates();
            }
        }
        // if the workflow name is null return null
        return null;
    }
    
   

    /**
     * Helper method that will create and add transitions to the supplied currentState. Loops through the transRoles map keys and creates 
     * the transition for each key. See {@link #createTransition(PSWorkflow, IPSGuid, String, long)} for details. The to state id is set based on the transition name.
     * TRANSITION_NAME_REJECT -> previous state id, TRANSITION_NAME_SUBMIT -> next state id and  TRANSITION_NAME_APPROVE or TRANSITION_NAME_PUBLISH -> pending state id.
     * 
     * @param workflow assumed not <code>null</code>
     * @param currentState assumed not <code>null</code>
     * @param previousState assumed not <code>null</code>
     * @param nextState assumed not <code>null</code>
     * @param pendingState assumed not <code>null</code>
     * @param transRoles assumed not <code>null</code>
     */
    private void addTransitionsToState(PSWorkflow workflow, PSState currentState, PSState previousState, PSState nextState, PSState pendingState,
            Map<String,List<String>> transRoles) {
        // Add transitions to state
        List<PSTransition> transitions = currentState.getTransitions();
        
        for (Entry<String, List<String>> entry : transRoles.entrySet()) 
        {
            String transitionName = entry.getKey();
            long toStateId = 0;
            if (transitionName.equalsIgnoreCase(TRANSITION_NAME_APPROVE) || transitionName.equalsIgnoreCase(TRANSITION_NAME_PUBLISH))
            {
                toStateId = pendingState.getStateId();
            }
            else if (transitionName.equalsIgnoreCase(TRANSITION_NAME_SUBMIT))
            {
                toStateId = nextState.getStateId();
            }
            else if (transitionName.equalsIgnoreCase(TRANSITION_NAME_REJECT))
            {
                toStateId = previousState.getStateId();
            }
            
            PSTransition transition = createTransition(workflow, currentState.getGUID(), transitionName, toStateId);
            List<PSTransitionRole> transitionRoles = transition.getTransitionRoles();
            List<PSTransitionRole> tempRoles = createTransitionRoles(workflow, transition, entry.getValue());
            transitionRoles.addAll(tempRoles);
            transition.setTransitionRoles(transitionRoles);           
            
            // Add the new transition to the worflow transitions
            transitions.add(transition);
        }
        currentState.setTransitions(transitions);
    }   

    /**
     * Sets the to state id of the submit transition of the supplied prevState to the state of the supplied nextState
     * and sets the to state id of the reject transition of the supplied prevState to the state of the supplied nextState
     * @param prevState assumed not <code>null</code>
     * @param nextState assumed not <code>null</code>
     */
    private void repointTransitions(PSState prevState, PSState nextState)
    {
        List<PSTransition> prevTrans = prevState.getTransitions();
        for (PSTransition psTransition : prevTrans)
        {
            if (psTransition.getLabel().equalsIgnoreCase(TRANSITION_NAME_SUBMIT))
            {
                psTransition.setToState(nextState.getStateId());
                break;
            }
        }
        prevState.setTransitions(prevTrans);
        List<PSTransition> nextTrans = nextState.getTransitions();
        for (PSTransition psTransition : nextTrans)
        {
            if (psTransition.getLabel().equalsIgnoreCase(TRANSITION_NAME_REJECT))
            {
                psTransition.setToState(prevState.getStateId());
                break;
            }
        }
        nextState.setTransitions(nextTrans);
    }   

    /**
     * Retrieves a list of all workflow names existing.

     * @return a list of workflows name. Maybe empty but never <code>null</code>
     */
    private List<String> getWorkflowArrayList()
    {
        PSEnumVals workflows = getWorkflowList();

        List<String> workflowsList = new ArrayList<>();
        for (EnumVal workflow : workflows.getEntries())
        {
            workflowsList.add(workflow.getValue());
        }

        return workflowsList;
    }
    
    /**
     * Finds all the states that belong to the workflow named as the parameter.
     * If no workflow with that name is found, it returns <code>null</code>
     * 
     * @param workflowName the name of the workflow of which we are going to
     *            retrieve the states from. Maybe empty or <code>null</code>
     * @return a <code>Set<String></code> object. Maybe empty or
     *         <code>null</code>
     */
    private List<String> getStateList(String workflowName)
    {
        List<PSState> workflowStates = getStates(workflowName);

        List<String> workflowStatesArray = new ArrayList<>();
        for (PSState state : workflowStates)
        {
            workflowStatesArray.add(state.getLabel());
        }

        return workflowStatesArray;
    }
    


    /**
     * Helper method to create list of transition role objects for the supplied list of role names.
     * @param workflow The workflow object under which the transition exists. Assumed not <code>null</code>.
     * @param transition The transition object for which the role is needs to be created, assumed not <code>null</code>
     * @param roleNames The list of role names assumed not <code>null</code>.
     * @return List of transition roles, never <code>null</code>, may be empty.
     */
    private List<PSTransitionRole> createTransitionRoles(PSWorkflow workflow, PSTransition transition, List<String> roleNames)
    {
    	List<PSTransitionRole> tranRoles = new ArrayList<>();
    	for (String roleName : roleNames) 
    	{
    		int roleId = getRoleIdByName(workflow.getRoles(), roleName);
    		if(roleId == 0)
    		{
    			//This should not happen as all roles must be in the workflow at this point.
    			log.debug("Skipping adding role " + roleName + " as it is not assigned to the workflow.");
    			continue;
    		}
    		PSTransitionRole transRole = new PSTransitionRole();
    		transRole.setRoleId(roleId);
    		transRole.setTransitionId(transition.getGUID().getUUID());
    		transRole.setWorkflowId(workflow.getGUID().getUUID()); 
    		tranRoles.add(transRole);
		}
    	return tranRoles;
    }   
    
    /**
     * Helper method to add all the workflow roles to the supplied state, if the role is in the list supplied state role names, then
     * the role's assignment type is set to PSAssignmentTypeEnum.ASSIGNEE otherwise PSAssignmentTypeEnum.READER.  For Quick Edit and Live
     * notify setting is ignored (only set on Pending state).
     * @param workflow The workflow object of the supplied state, assumed not <code>null</code>.
     * @param state The state object for which the roles needs to be added, assumed not <code>null</code>
     * @param stateRoleNames The list of state roles names, assumed not <code>null</code>
     */
    private void addWorkflowRolesToState(PSWorkflow workflow, PSState state, List<String> stateRoleNames, List<String> stateRoleNamesWithNotification)
    {
        List<PSAssignedRole> assignedRoles = state.getAssignedRoles();        
    	for (PSWorkflowRole workflowRole : workflow.getRoles())
        {
    		IPSGuid roleGuid = workflowRole.getGUID();
            PSAssignedRole newAssignedRole = new PSAssignedRole();
            newAssignedRole.setGUID(roleGuid);
            newAssignedRole.setStateId(state.getGUID().getUUID());
            newAssignedRole.setWorkflowId(workflow.getGUID().getUUID());            
            PSAssignmentTypeEnum asType = stateRoleNames.contains(workflowRole.getName())?PSAssignmentTypeEnum.ASSIGNEE:PSAssignmentTypeEnum.READER;
            newAssignedRole.setAssignmentType(asType);
            // Check if the notification is enabled for the role
            newAssignedRole.setDoNotify(false);
            if (stateRoleNamesWithNotification.contains(workflowRole.getName()) && !(state.getName().equals(QUICK_EDIT_STATE) || state.getName().equals(LIVE_STATE)))
            {
                newAssignedRole.setDoNotify(true);
            }
            assignedRoles.add(newAssignedRole);
        }    	
    	state.setAssignedRoles(assignedRoles);
    }
    
    /**
     * Helper method that converts the supplied step roles (role name and available transitions) to a map of transition and list of role names.
     * @param stepRoles assumed not <code>null</code>.
     * @return Map never <code>null</code> may be empty. 
     */
    private Map<String,List<String>> convertStepRolesToTransitionRoles(List<PSUiWorkflowStepRole> stepRoles)
    {
    	Map<String,List<String>> transitionAndRoles = new HashMap<>();
        for (PSUiWorkflowStepRole stepRole : stepRoles)
        {
            for (PSUiWorkflowStepRoleTransition stepTrans : stepRole.getRoleTransitions())
            {
            	String transition = stepTrans.getTransitionPermission();
            	List<String> roles = transitionAndRoles.get(transition);
            	if(roles == null)
            	{
            		roles = new ArrayList<>();
            		transitionAndRoles.put(transition, roles);
            	}
            	roles.add(stepRole.getRoleName());
            }
        }    	
    	return transitionAndRoles;
    }
    
    /**
     * Creates a transition object with supplied name and workflow and state guids, set the to state of the transition to the supplied toStateId.
     * @param workflow The new transition is created with this workflow id, assumed not <code>null</code>
     * @param stateGuid used as the from state id of the transition, assumed not <code>null</code>
     * @param transitionName New transiton's label, description and name are set this string, assumed not <code>blank</code>
     * @param toStateId assumed a valid state id, transtions to state id is set to this id.
     */
    private PSTransition createTransition(PSWorkflow workflow, IPSGuid stateGuid, String transitionName, long toStateId)
    {
    	IPSGuid workflowGuid = workflow.getGUID();
    	PSTransition transition = workflowService.createTransition(workflowGuid, stateGuid);
        transition.setAllowAllRoles(false);
        transition.setLabel(transitionName);
        transition.setDescription(transitionName);
        transition.setTrigger(transitionName);
        transition.setToState(toStateId);  
        if (transitionName.equalsIgnoreCase(TRANSITION_NAME_APPROVE))
        {
            transition.setTransitionAction("Java/global/percussion/extensions/general/perc_LockLocalContent");
        }
        if (transitionName.equalsIgnoreCase(TRANSITION_NAME_SUBMIT))
        {
            transition.setDefaultTransition(true);
        }
        long notificationId = getNotificationId(workflow);
        PSNotification notif = createNotification(transition.getGUID(), workflowGuid, notificationId);
        List<PSNotification> notifications = transition.getNotifications();
        notifications.add(notif);
        transition.setNotifications(notifications);
        return transition;
    }
    
    /**
     * Creates a notification object with supplied name and workflow and transition guids.
     * @param transitionGuid used as the transition id of the transition, assumed not <code>null</code>
     * @param workflowGuid The new transition is created with this workflow id, assumed not <code>null</code>
     */
    private PSNotification createNotification(IPSGuid transitionGuid, IPSGuid workflowGuid, long notificationId)
    {
        PSNotification notification = workflowService.createNotification(workflowGuid, transitionGuid);
        notification.setNotificationId(notificationId);
        return notification;
    }
    
    /**
     * Helper method that will repoint the previous state submit transition to the current state and next state 
     * reject transition to current state.
     * 
     * @param prevState assumed not <code>null</code>
     * @param nextState assumed not <code>null</code>
     * @param currentStateId id of the current state.
     */
    private void repointTransitions(PSState prevState, PSState nextState, long currentStateId)
    {
        List<PSTransition> prevTrans = prevState.getTransitions();
       
        for (PSTransition psTransition : prevTrans)
        {
            if (psTransition.getLabel().equalsIgnoreCase(TRANSITION_NAME_SUBMIT))
            {
                psTransition.setToState(currentStateId);
                break;
            }
        }
        prevState.setTransitions(prevTrans);
        List<PSTransition> nextTrans = nextState.getTransitions();
        for (PSTransition psTransition : nextTrans)
        {
            if (psTransition.getLabel().equalsIgnoreCase(TRANSITION_NAME_REJECT))
            {
                psTransition.setToState(currentStateId);
                break;
            }
        }
        nextState.setTransitions(nextTrans);
    }

    /**
     * Helper method to find the to state id of the given transition and return
     * the state object from the supplied workflow.
     * 
     * @param workflow assumed not <code>null</code>.
     * @param fromState assumed not <code>null</code>
     * @param transitionName assumed not <code>null</code>
     * @return PSState object of to state or <code>null</code> if not found.
     */
    private PSState getToStateByTransition(PSWorkflow workflow, PSState fromState, String transitionName)
    {
        PSState toState = null;
        List<PSTransition> prevTrans = fromState.getTransitions();
        long toStateId = -1;
        for (PSTransition psTransition : prevTrans)
        {
            if (psTransition.getLabel().equalsIgnoreCase(transitionName))
            {
                toStateId = psTransition.getToState();
                break;
            }
        }
        for (PSState psState : workflow.getStates())
        {
            if (psState.getGUID().getUUID() == toStateId)
            {
                toState = psState;
                break;
            }
        }
        return toState;
    }


    /**
     * Gets the workflow for the specified workflow name.
     * 
     * @param workflowName assumed not <code>null</code>.
     * 
     * @return the workflow object, may be <code>null</code> if the workflow
     *         could not be found.
     */
    private PSWorkflow getWorkflowFromName(String workflowName)
    {
        PSWorkflow workflow = null;

        List<PSWorkflow> workflows = workflowService.findWorkflowsByName(workflowName);
        if (!workflows.isEmpty())
        {
            workflow = workflows.get(0);

            // Order the states list
            List<PSState> workflowStatesList = new ArrayList<>();
            workflowStatesList = workflow.getStates();
            WorkFlowStatesComparator comp = new WorkFlowStatesComparator();
            Collections.sort(workflowStatesList, comp);
            workflow.setStates(workflowStatesList);
        }

        return workflow;
    }   

    /**
     * Gets the role name for the specified workflow and role id
     * 
     * @param workflow the workflow that contains the roles assumed not
     *            <code>null</code>.
     * @param roleId the role Id assumed not <code>null</code>.
     * 
     * @return the role name, may be empty never<code>null</code> if the role
     *         could not be found.
     */
    private String getRoleName(PSWorkflow workflow, Integer roleId)
    {
        String roleName = "";

        for (PSWorkflowRole role : workflow.getRoles())
        {
            if (roleId == role.getGUID().getUUID())
            {
                roleName = role.getName();
            }
        }
        return roleName;
    }
    
    /**
     * Helper method to get the transition object from a state given the transition name
     * @param currentState the state that contains the transitions assumed not
     *            <code>null</code>.
     * @param transitionName the transition name to find assumed not <code>null</code>.
     * * @return the transition object, may be <code>null</code> if the transition
     *         could not be found.
     */
    private PSTransition getTransitionByName(PSState currentState, String transitionName)
    {
        PSTransition transition = null;
        for(PSTransition trans : currentState.getTransitions())
        {
            if(trans.getName().equalsIgnoreCase(transitionName))
            {
                transition = trans;
                break;
            }
        }
        return transition;
    }

    private long getNotificationId(PSWorkflow workflow)
    {
        long notificationId = 0;
        List<PSNotificationDef> notifsDef = workflow.getNotificationDefs();
        notificationId = notifsDef.get(0).getGUID().getUUID();
        return notificationId;
    }

    /**
     * Converts an <code>PSWorkflow</code> object to <code>PSUiWorkflow</code>
     * 
     * @param workflow The <code>PSWorkflow</code> object to convert
     * @return a <code>PSUiWorkflow</code> object
     */
    private PSUiWorkflow toPSUiWorkflow(PSWorkflow workflow) throws IPSGenericDao.LoadException {
        PSUiWorkflow uiWorkflow = new PSUiWorkflow();

        uiWorkflow.setWorkflowName(workflow.getName());
        if (workflow.getName().equalsIgnoreCase(workflowService.getDefaultWorkflowName()))
        {
            uiWorkflow.setDefaultWorkflow(true);
        }
        else
        {
            uiWorkflow.setDefaultWorkflow(false);
        }        
        List<PSUiWorkflowStep> uiWorkflowSteps = new ArrayList<>();

        // Flag for Approved state
        boolean addApprovedRole = true;

        for (PSState state : workflow.getStates())
        {
            if (!excludedStates.contains(state.getName()))
            {
                PSUiWorkflowStep uiWorkflowStep = new PSUiWorkflowStep();
                uiWorkflowStep.setStepName(state.getName());
                
                // Set the list of permission names for the current step
                List<String> permissionNamesForStep = new ArrayList<>();
                
                for (PSTransition transition : state.getTransitions())
                {
                    // Add the transition name to the list
                    permissionNamesForStep.add(transition.getLabel());
                }
                
                // Add the list of permission names to the step
                uiWorkflowStep.setPermissionNames(permissionNamesForStep);
                
                // Set the list of roles allowed for the current step
                List<PSUiWorkflowStepRole> uiWorkflowStepRoles = new ArrayList<>();
                
                for (PSAssignedRole assignedRole : state.getAssignedRoles())
                {
                    PSAssignmentTypeEnum assignmentRoleType = assignedRole.getAssignmentType();

                    if (assignmentRoleType == PSAssignmentTypeEnum.ADMIN
                            || assignmentRoleType == PSAssignmentTypeEnum.ASSIGNEE)
                    {
                        int roleId = assignedRole.getGUID().getUUID();
                        String roleName = getRoleName(workflow, roleId);
                        boolean isNotified = assignedRole.isDoNotify();
                        PSUiWorkflowStepRole uiWorkflowStepRol = new PSUiWorkflowStepRole(roleName, roleId, isNotified);
                        uiWorkflowStepRoles.add(uiWorkflowStepRol);

                        List<PSUiWorkflowStepRoleTransition> uiWorkflowStepRoleTransitions = new ArrayList<>();

                        for (PSTransition transition : state.getTransitions())
                        {
                            List<PSTransitionRole> transitionRoles = transition.getTransitionRoles();
                            for (PSTransitionRole tranRole : transitionRoles)
                            {
                                if (tranRole.getRoleId() == roleId)
                                {
                                    PSUiWorkflowStepRoleTransition uiWorkflowStepRoleTransition = new PSUiWorkflowStepRoleTransition();
                                    uiWorkflowStepRoleTransition.setTransitionPermission(transition.getLabel());
                                    uiWorkflowStepRoleTransitions.add(uiWorkflowStepRoleTransition);
                                }
                            }
                        }

                        uiWorkflowStepRol.setRoleTransitions(uiWorkflowStepRoleTransitions);
                    }
                }

                // Order the list of roles
                List<PSUiWorkflowStepRole> workflowStepRoleList = uiWorkflowStepRoles;
                WorkFlowRolesComparator comp = new WorkFlowRolesComparator();
                Collections.sort(workflowStepRoleList, comp);
                uiWorkflowStep.setStepRoles(workflowStepRoleList);

                // Add the step to the list of steps
                uiWorkflowSteps.add(uiWorkflowStep);
            }

            if (excludedStates.contains(state.getName()) && addApprovedRole)
            {
                if(state.getName().equals(QUICK_EDIT_STATE))
                {    
                    PSUiWorkflowStep approvedState = getApprovedState(workflow, state);
                    
                    List<String> permissionNamesForStep = new ArrayList<>();
                    for (PSTransition transition : state.getTransitions())
                    {
                        //The Remove transition permissions is handled by Publish transition, so it is not needed
                        if (!transition.getName().equals(TRANSITION_NAME_REMOVE))
                        {
                            // Add the transition name to the list
                            permissionNamesForStep.add(transition.getLabel());
                        }
                    }
                    
                    approvedState.setPermissionNames(permissionNamesForStep);
                    uiWorkflowSteps.add(approvedState);
                    addApprovedRole = false;
                }
            }
        }
        uiWorkflow.setWorkflowSteps(uiWorkflowSteps);
        //Set staging roles
        uiWorkflow.setStagingRoleNames(getStagingRoles(workflow.getGUID().longValue()));
        return orderStepPermissions(uiWorkflow);
    }

    /**
     * Create a workflow state for the Approved state including Admin and Editor
     * roles
     * @param workflow 
     * @param state 
     * 
     * @return the workflow step object, never<code>null</code>
     */
    private PSUiWorkflowStep getApprovedState(PSWorkflow workflow, PSState state)
    {
        // Consolidate Approved state
        PSUiWorkflowStep workflowApprovedStep = new PSUiWorkflowStep();
        workflowApprovedStep.setStepName("Approved");

        List<PSUiWorkflowStepRole> workflowApprovedStepRoles = new ArrayList<>();
        
        // Get the assigned roles from Quick Edit state, but notfications come from the Pending state
        Set<Integer> notificationRoles = getNotificationRoleIds(workflow, PENDING_STATE);
        for (PSAssignedRole assignedRole : state.getAssignedRoles())
        {
            PSAssignmentTypeEnum assignmentRoleType = assignedRole.getAssignmentType();

            if (assignmentRoleType == PSAssignmentTypeEnum.ADMIN
                    || assignmentRoleType == PSAssignmentTypeEnum.ASSIGNEE)
            {
                Integer roleId = assignedRole.getGUID().getUUID();
                String roleName = getRoleName(workflow, roleId);
                boolean isNotified = notificationRoles.contains(roleId);
                PSUiWorkflowStepRole uiWorkflowStepRol = new PSUiWorkflowStepRole(roleName, roleId, isNotified);
                workflowApprovedStepRoles.add(uiWorkflowStepRol);

                List<PSUiWorkflowStepRoleTransition> uiWorkflowStepRoleTransitions = new ArrayList<>();

                for (PSTransition transition : state.getTransitions())
                {
                    //The Remove transition permissions is handled by Publish transition, so it is not needed
                    if (!transition.getName().equals(TRANSITION_NAME_REMOVE))
                    {
                        List<PSTransitionRole> transitionRoles = transition.getTransitionRoles();
                        for (PSTransitionRole tranRole : transitionRoles)
                        {
                            if (tranRole.getRoleId() == roleId)
                            {
                                PSUiWorkflowStepRoleTransition uiWorkflowStepRoleTransition = new PSUiWorkflowStepRoleTransition();
                                uiWorkflowStepRoleTransition.setTransitionPermission(transition.getLabel());
                                uiWorkflowStepRoleTransitions.add(uiWorkflowStepRoleTransition);
                            }
                        }
                    }
                }

                uiWorkflowStepRol.setRoleTransitions(uiWorkflowStepRoleTransitions);
            }
        }

        List<PSUiWorkflowStepRoleTransition> workflowApprovedStepRoleTransitions = new ArrayList<>();
        
        for(String transitionName : defaultTransitions)
        {
            PSUiWorkflowStepRoleTransition workflowStepRoleTransition = new PSUiWorkflowStepRoleTransition(
                    transitionName);
            
            workflowApprovedStepRoleTransitions.add(workflowStepRoleTransition);
        }
        
        // Order the list of roles
        List<PSUiWorkflowStepRole> workflowStepRoleList = workflowApprovedStepRoles;
        WorkFlowRolesComparator comp = new WorkFlowRolesComparator();
        Collections.sort(workflowStepRoleList, comp);       
        workflowApprovedStep.setStepRoles(workflowStepRoleList);
        
        return workflowApprovedStep;
    }
    
    /**
     * Get the roles ids for roles with notify enabled in the specified state.
     * 
     * @param workflow The workflow object containing the state, assumed not <code>null</code>.
     * @param stateName The name of the state to find, assumed not empty and to be in the supplied workflow object.
     * 
     * @return The list of role ids from the specified state that have notify enabled, not <code>null</code>, may be 
     * empty.
     */
    private Set<Integer> getNotificationRoleIds(PSWorkflow workflow, String stateName)
    {
        Set<Integer> result = new HashSet<>();
        PSState pendingState = getWorkflowStateByName(workflow, stateName);
        if (pendingState == null) {
            return result;
        }
        
        List<PSAssignedRole> roles = pendingState.getAssignedRoles();
        for (PSAssignedRole role : roles)
        {
            if (role.isDoNotify()) {
                result.add(role.getGUID().getUUID());
            }
        }
        
        return result;
    }

    /**
     * Find a workflow state by name in the workflow passed as parameter 
     * roles
     * @param workflow the workflow object
     * @param stateName the name of the state to find
     * 
     * @return the workflow state object, can be<code>null</code> if it is not found
     */
    private PSState getWorkflowStateByName(PSWorkflow workflow, String stateName)
    {
        PSState stateFound = null;
        for (PSState state : workflow.getStates()) 
        {
            if(state.getName().equals(stateName))
            {
                stateFound = state;
                break;
            }
        }
        return stateFound;
    }

    /**
     * Validate the workflow name. Cannot be empty, a reserved state {LocalContent,
     * Local Content}, must be unique and only contain valid characters 
     * a-z, -, _ and <space>, strip leading and trailing spaces.
     * 
     * @param workflowName the name for the new workflow, may not <code>null</code>
     * @param previousWorkflowName if is an update the name that has before, can be
     * empty but not <code>null</code> 
     */
    private void validateWorkflowName(String workflowName, String previousWorkflowName)
            throws PSWorkflowEditorServiceException
    {
        // Make sure both workflowName and previousWorkflowName are not blank
        if (StringUtils.isBlank(workflowName))
        {
            throw new PSWorkflowEditorServiceException(WORKFLOW_NAME_IS_EMPTY);
        }
        
        // System workflow name
        List<String> systemWorkflows = Arrays.asList("LocalContent", "Local Content");
        String returnedName = findName(systemWorkflows, workflowName);
        if (!returnedName.isEmpty())
        {
            // If is creating a new workfow the previous name came empty here
            if (previousWorkflowName.isEmpty())
            {
                throw new PSWorkflowEditorServiceException(MessageFormat.format(
                        PSSteppedWorkflowService.WORKFLOW_NAME_CREATE_IS_A_SYSTEM_WORKFLOW, workflowName));
            }
            else
            {
                throw new PSWorkflowEditorServiceException(MessageFormat.format(
                        PSSteppedWorkflowService.WORKFLOW_NAME_UPDATE_IS_A_SYSTEM_WORKFLOW, previousWorkflowName, workflowName));
            }
        }

        // Unique workflow name
        List<String> workflows = getWorkflowArrayList();
        // If is an update and the name did not change, remove the name from the list
        if (previousWorkflowName.equalsIgnoreCase(workflowName))
        {
            workflows.remove(previousWorkflowName);
        }
        String uniqueName = findName(workflows, workflowName);
        if (!uniqueName.isEmpty())
        {
            // If is creating a new workflow the previous name came empty here
            if (previousWorkflowName.isEmpty())
            {
                throw new PSWorkflowEditorServiceException(MessageFormat.format(
                        PSSteppedWorkflowService.WORKFLOW_NAME_CREATE_IS_NOT_UNIQUE, workflowName, uniqueName));
            }
            else
            {
                throw new PSWorkflowEditorServiceException(MessageFormat.format(
                        PSSteppedWorkflowService.WORKFLOW_NAME_UPDATE_IS_NOT_UNIQUE, previousWorkflowName, workflowName, uniqueName));
            }
        }
        
        // Too long state name
        if (workflowName.length() > NAME_MAX_LENGHT)
        {
            throw new PSWorkflowEditorServiceException(WORKFLOW_NAME_IS_TOO_LONG);
        }

        // Validate characters
        Pattern pattern = Pattern.compile("[\\s\\w-]+");
        Matcher matcher = pattern.matcher(workflowName);
        if (!matcher.matches())
        {
            throw new PSWorkflowEditorServiceException(PSSteppedWorkflowService.WORKFLOW_NAME_IS_INVALID);
        }
    }
    
    /**
     * Validate the state name. Cannot be empty, a reserved state {Pending,
     * Live, EditLive, Approved}, must be unique and only contain valid
     * characters a-z, -, _ and <space>, strip leading and trailing spaces.
     * 
     * @param workflowName the name of the workflow, may not <code>null</code>
     * @param stateName the name for the new state, may not <code>null</code>
     * @param previousStateName if is an update the name that has before, can be
     * empty but not <code>null</code> 
     */
    private void validateStateName(String workflowName, String stateName, String previousStateName)
            throws PSWorkflowEditorServiceException
    {
        // Empty state name
        if (stateName.isEmpty())
        {
            throw new PSWorkflowEditorServiceException(STATE_NAME_IS_EMPTY);
        }

        // System state name
        List<String> systemStates = Arrays.asList("Pending", "Live", "EditLive", "Approved");
        String returnedName = findName(systemStates, stateName);
        if (!returnedName.isEmpty())
        {            
            // If is creating a new state the previous name came empty here
            if (previousStateName.isEmpty())
            {
                throw new PSWorkflowEditorServiceException(MessageFormat.format(
                        PSSteppedWorkflowService.STATE_NAME_CREATE_IS_A_SYSTEM_STATE, stateName));
            }
            else
            {
                throw new PSWorkflowEditorServiceException(MessageFormat.format(
                        PSSteppedWorkflowService.STATE_NAME_UPDATE_IS_A_SYSTEM_STATE, previousStateName, stateName));
            }
        }

        // Unique state name
        List<String> workflowStates = getStateList(workflowName);
        // If is an update and the name did not change, remove the name from the list
        if (previousStateName.equalsIgnoreCase(stateName))
        {
            workflowStates.remove(previousStateName);
        }
        String uniqueName = findName(workflowStates, stateName);
        if (!uniqueName.isEmpty())
        {
            // If is creating a new state the previous name came empty here
            if (previousStateName.isEmpty())
            {
                throw new PSWorkflowEditorServiceException(MessageFormat.format(
                        PSSteppedWorkflowService.STATE_NAME_CREATE_IS_NOT_UNIQUE, stateName, uniqueName));
            }
            else
            {
                throw new PSWorkflowEditorServiceException(MessageFormat.format(
                        PSSteppedWorkflowService.STATE_NAME_UPDATE_IS_NOT_UNIQUE, previousStateName, stateName, uniqueName));
            }
        }
        
        // Too long state name
        if (stateName.length() > NAME_MAX_LENGHT)
        {
            throw new PSWorkflowEditorServiceException(STATE_NAME_IS_TOO_LONG);
        }

        // Validate characters
        Pattern pattern = Pattern.compile("[\\s\\w-]+");
        Matcher matcher = pattern.matcher(stateName);
        if (!matcher.matches())
        {
            throw new PSWorkflowEditorServiceException(PSSteppedWorkflowService.STATE_NAME_IS_INVALID);
        }
    }
    
    /**
     * Helper method to update Pending and Live states, when the 'Approved' state is updated. All roles that have
     * at least one permission (Either Approve or Archive) are added to Edit transition of Pending and Live states.  Only
     * sets notification for Pending state, not Live.
     * @param workflow The workflow object, assumed not <code>null</code>.
     * @param stepRoleNames The list of role names to in order to update the roles for each state, assumed not <code>null</code>
     * @param stepRoleNamesWithEnableNotification The list of roles names with the notification enabled, assumed not <code>null</code>
     */
    private void updateApprovedState(PSWorkflow workflow, List<String> stepRoleNames, List<String> stepRoleNamesWithEnableNotification)
    {
        //Assign the same roles for Edit transition of Pending and Live states
        List<PSTransition> editTransitions = new ArrayList<>();
        PSState pendingState = getWorkflowStateByName (workflow, PENDING_STATE);  
        
        //Assign the roles to the step
        pendingState.setAssignedRoles(new ArrayList<>());
        addWorkflowRolesToState(workflow, pendingState, stepRoleNames, stepRoleNamesWithEnableNotification);
         
        //Update the transition roles
        PSTransition liveTransition = getTransitionByName(pendingState, TRANSITION_NAME_LIVE);
        if(liveTransition == null) {
            throw new PSWorkflowEditorServiceException("Can't find the transition by given name " + TRANSITION_NAME_LIVE);
        }
        
        editTransitions.add(liveTransition);
        
        PSTransition transition = getTransitionByName(pendingState, TRANSITION_NAME_EDIT);
        if(transition == null) {
            throw new PSWorkflowEditorServiceException("Can't find the transition by given name " + TRANSITION_NAME_EDIT);
        }
        
        List<PSTransitionRole> transitionRoles = new ArrayList<>();
        List<PSTransitionRole> tempRoles = createTransitionRoles(workflow, transition, stepRoleNames);
        transitionRoles.addAll(tempRoles);
        transition.setTransitionRoles(transitionRoles);
        if(!editTransitions.contains(transition)) {
            editTransitions.add(transition);
        }
        
        pendingState.setTransitions(editTransitions);
        
        //Live state
        List<PSTransition> liveTransitions = new ArrayList<>();
        PSState liveState = getWorkflowStateByName (workflow, LIVE_STATE);
        
        //Assign the roles to the step, do not set notification for Live
        if(liveState!=null) {
            liveState.setAssignedRoles(new ArrayList<>());
        }
        addWorkflowRolesToState(workflow, liveState, stepRoleNames, stepRoleNamesWithEnableNotification);

        //Update the transition roles
        PSTransition editTransition = getTransitionByName(liveState, TRANSITION_NAME_EDIT);
        if(editTransition == null) {
            throw new PSWorkflowEditorServiceException("Can't find the transition by given name " + TRANSITION_NAME_EDIT);
        }
        
        transitionRoles = new ArrayList<>();
        tempRoles = createTransitionRoles(workflow, editTransition, stepRoleNames);
        transitionRoles.addAll(tempRoles);
        editTransition.setTransitionRoles(transitionRoles);
        if(!liveTransitions.contains(editTransition)) {
            liveTransitions.add(editTransition);
        }
                 
        liveState.setTransitions(liveTransitions);
    }
    
    /**
     * Helper method to update Remove transition for Quick Edit state given the permissions set for Publish transition.
     * @param workflow The workflow object, assumed not <code>null</code>.
     * @param currentState The current state being edited, assumed not <code>null</code>.
     * @param stepRoleNames The list of role names to in order to update the roles for each state, assumed not <code>null</code>
     * @param stepRoleNamesWithEnableNotification The list of roles names with the notification enabled, assumed not <code>null</code>
     * 
     * @return <code>PSTransition<code> object for the updated Remove Transition, never <code>null</code>
     * @throws PSWorkflowEditorServiceException if the Remove transition is not found in the current state.
     */
    private PSTransition updateRemoveTransition(PSWorkflow workflow, PSState currentState, List<String> stepRoleNames, List<String> stepRoleNamesWithEnableNotification)
    {
        PSTransition removeTransition = getTransitionByName(currentState, TRANSITION_NAME_REMOVE);
        if(removeTransition == null) {
            throw new PSWorkflowEditorServiceException("Can't find the transition by given name " + TRANSITION_NAME_REMOVE);
        }
                
        List<PSTransitionRole> transitionRoles = new ArrayList<>();
        List<PSTransitionRole> tempRoles = createTransitionRoles(workflow, removeTransition, stepRoleNames);
        transitionRoles.addAll(tempRoles);          
        removeTransition.setTransitionRoles(transitionRoles);
        
        return removeTransition;
    }

    /**
     * Check if a list passed as parameter contains a string ignoring case.
     * 
     * @param aList the list to iterate
     * @param aString the string to find
     * @return <code>true<code> if the string exists in the list
     */
    private String findName(List<String> aList, String aString)
    {
        for (String listElement : aList)
        {
            if (listElement.equalsIgnoreCase(aString))
            {
                return listElement;
            }
        }
        return "";
    }

    /**
     * Get the role Id based on the role name
     * 
     * @param roles the list of roles of the workflow
     * @param roleName the name of the role to find
     * 
     * @return <code>long<code> the state id to be assigned
     */
    private int getRoleIdByName(List<PSWorkflowRole> roles, String roleName)
    {
        int roleId = 0;
        for (PSWorkflowRole workflowRole : roles)
        {
            if (workflowRole.getName().equalsIgnoreCase(roleName))
            {
                roleId = workflowRole.getGUID().getUUID();
                break;
            }
        }
        return roleId;
    }
    
    /**
     * Load the workflow definition from a xml file that contains the serialized workflow object. 
     * 
     * @return the workflow definition, or <code>empty</code> if the file is not found.
     * @throws IOException if the workflow definition file is not found.
     */
    private String loadBaseWorkflow() throws IOException
    {
        String validStringXml = "";

        InputStream in = null;
        BufferedReader br = null;

        in = this.getClass().getClassLoader().getResourceAsStream("com/percussion/services/workflow/data/DefaultWorkflow.xml");
         
        if(in == null) {
            return "";
        }

        br = new BufferedReader(new InputStreamReader(in));
                           
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = br.readLine()) != null) {
            sb.append(line).append("\n");
        }
        br.close();
          
        validStringXml = sb.toString();  
        validStringXml = validStringXml.trim().replaceFirst("^([\\W]+)<","<");
            
        return validStringXml;
    }
    
    /**
     * Helper method to order the list of permission names of each step, so the client
     * will display the permissions in the right order for each step.
     * 
     * @param workflow The <code>PSUiWorkflow</code> object to order
     * @return a <code>PSUiWorkflow</code> object
     */
    private PSUiWorkflow orderStepPermissions(PSUiWorkflow workflow)
    {
        for(PSUiWorkflowStep step : workflow.getWorkflowSteps())
        {
            //Order the permissions for the step
            List<String> orderedPermissionNames = new ArrayList<>();
            
            for(String permissionName : orderedTransitions)
            {
                if(step.getPermissionNames().contains(permissionName))
                {
                    orderedPermissionNames.add(permissionName);    
                }
            }
            step.setPermissionNames(orderedPermissionNames);
        }
        return workflow;
    }
   
    @Autowired
    public void setNotificationService(IPSNotificationService notificationService)
    {
        notificationService.addListener(EventType.SAVE_ASSETS_PROCESS_COMPLETE, this);
    }

    

    @Override
    public void notifyEvent(PSNotificationEvent notification)
    {
        if (!EventType.SAVE_ASSETS_PROCESS_COMPLETE.equals(notification.getType())) {
            return;
        }
        
        // start background process to cache all workflows
        PSWorkflowCacheBuilder cacheBuilder = new PSWorkflowCacheBuilder(workflowService, maintenanceManager);
        cacheBuilder.buildWorkflowCache();
    }

    /**
     * Saves the staging roles to metadata.
     * @param workflowId
     * @param stagingRoles 
     */
    private void saveStagingRoles(long workflowId, String stagingRoles) throws IPSGenericDao.LoadException, IPSGenericDao.SaveException {
    	PSMetadata md = new PSMetadata(METADATA_STAGING_ROLES_KEY_PREFIX + workflowId, StringUtils.defaultString(stagingRoles));
    	metadataService.save(md);
    }
    
    /**
     * Deletes the staging roles from metadata for the supplied workflow id
     * @param workflowId
     */
    private void deleteStagingRoles(long workflowId) throws IPSGenericDao.LoadException, IPSGenericDao.DeleteException {
    	metadataService.delete(METADATA_STAGING_ROLES_KEY_PREFIX + workflowId);
    }
    
    /**
     * Get the staging roles from metadata for the supplied workflow id
     * @param workflowId assumed to be a valid workflow id
     * @return staging role names corresponding to the supplied workflow, may be empty never <code>null</code>.
     */
    private String getStagingRoles(long workflowId) throws IPSGenericDao.LoadException {
        String stagingRoleNames = "";
        PSMetadata md = metadataService.find(METADATA_STAGING_ROLES_KEY_PREFIX + workflowId);
        if(md != null)
        {
            stagingRoleNames = md.getData();
        }
        return stagingRoleNames;
    }
    

    /**
     * Comparator class that implements the states sorting according 
     * the sort order of each state.
     */
    class WorkFlowStatesComparator implements Comparator<PSState>
    {
        public int compare(PSState o1, PSState o2)
        {
            return o1.getSortOrder().compareTo(o2.getSortOrder());
        }
    }

    /**
     * Comparator class that implements the roles sorting according 
     * their name.
     */
    class WorkFlowRolesComparator implements Comparator<PSUiWorkflowStepRole>
    {
        public int compare(PSUiWorkflowStepRole o1, PSUiWorkflowStepRole o2)
        {
            return o1.getRoleName().compareToIgnoreCase(o2.getRoleName());
        }
    }

    /**
     * Comparator class that implements the workflow sorting according 
     * their name.
     */
    class WorkFlowNameComparator implements Comparator<PSObjectSummary>
    {
        public int compare(PSObjectSummary o1, PSObjectSummary o2)
        {
            return o1.getLabel().compareToIgnoreCase(o2.getLabel());
        }
    }
    
    /**
     * Comparator class that implements the workflow sorting according 
     * their name.
     */
    class WorkFlowMetadataNameComparator implements Comparator<PSWorkflow>
    {
        public int compare(PSWorkflow o1, PSWorkflow o2)
        {
            return o1.getLabel().compareToIgnoreCase(o2.getLabel());
        }
    }

    IPSWorkflowService workflowService;
    private IPSUserService userService;
    private PSNamedLockManager lockMgr;
    private IPSFolderService folderService;
    IPSCmsObjectMgr cmsObjectMgr = PSCmsObjectMgrLocator.getObjectManager();
    private IPSMaintenanceManager maintenanceManager;
    private IPSMetadataService metadataService;

    // Logger for this service.
    public static final Logger log = LogManager.getLogger(PSSteppedWorkflowService.class);

    // Maximum name lenght when creating/updating a step/workflow
    private static final Integer NAME_MAX_LENGHT = 50;
    
    // Workflow error messages
    private static final String WORKFLOW_NAME_IS_EMPTY = "Workflow name cannot be blank.";
    private static final String WORKFLOW_NAME_IS_INVALID = "Invalid character in workflow name. Characters allowed are: a-z, 0-9, -, _ and [space].";
    private static final String WORKFLOW_NAME_IS_TOO_LONG = "Workflow name cannot have more than " + NAME_MAX_LENGHT + " characters.";
    
    // Workflow creating error messages
    private static final String WORKFLOW_NAME_CREATE_IS_A_SYSTEM_WORKFLOW = "Cannot create workflow ''{0}'' because it is a restricted workflow name.";
    private static String WORKFLOW_NAME_CREATE_IS_NOT_UNIQUE = "Cannot create workflow ''{0}'' because a workflow named ''{1}'' already exists.";
    
    // Workflow updating error messages
    private static final String WORKFLOW_NAME_UPDATE_IS_A_SYSTEM_WORKFLOW = "Cannot rename workflow ''{0}'' to ''{1}'' because it is a restricted workflow name.";
    private static String WORKFLOW_NAME_UPDATE_IS_NOT_UNIQUE = "Cannot rename workflow ''{0}'' to ''{1}'' because a workflow named ''{2}'' already exists.";
    
    // Workflow deleting error messages
    private static final String WORKFLOW_IS_A_SYSTEM_WORKFLOW = "The workflow ''{0}'' cannot be deleted because is a system workflow.";
    private static String WORKFLOW_HAVE_ITEMS = "Cannot delete: ''{0}'' because this workflow has items assigned to it.";
    
    // Step error messages
    private static final String STATE_NAME_IS_EMPTY = "Step name cannot be blank.";
    private static final String STATE_NAME_IS_INVALID = "Invalid character in step name. Characters allowed are: a-z, 0-9, -, _ and [space].";
    private static final String STATE_NAME_IS_TOO_LONG = "Step name cannot have more than " + NAME_MAX_LENGHT + " characters.";
    
    // Step creating error messages
    private static final String STATE_NAME_CREATE_IS_A_SYSTEM_STATE = "Cannot create step ''{0}'' because it is a special system step.";
    private static String STATE_NAME_CREATE_IS_NOT_UNIQUE = "Cannot create step ''{0}'' because a step named ''{1}'' already exists.";    
    
    // Step updating error messages
    private static final String STATE_NAME_UPDATE_IS_A_SYSTEM_STATE = "Cannot rename step ''{0}'' to ''{1}'' because it is a special system step.";
    private static String STATE_NAME_UPDATE_IS_NOT_UNIQUE = "Cannot rename step ''{0}'' to ''{1}'' because a step named ''{2}'' already exists.";

    //Step deleting error messages
    private static final String STATE_IS_A_SYSTEM_STATE = "The step cannot be deleted because is a system state.";
    private static String STATE_HAVE_ITEMS = "Cannot delete step ''{0}'' because there are items associated to this step.";
    
    //Step not found
    private static String STATE_NOT_FOUND = "The step ''{0}'' has been removed and no longer exists in the workflow. Please refresh and try again.";
    
}
