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
package com.percussion.workflow.web.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.percussion.share.data.PSEnumVals;
import com.percussion.share.test.PSObjectRestClient.DataRestClientException;
import com.percussion.share.test.PSRestClient.RestClientException;
import com.percussion.share.test.PSObjectRestClient;
import com.percussion.share.test.PSRestTestCase;
import com.percussion.workflow.data.PSUiWorkflow;
import com.percussion.workflow.data.PSUiWorkflowStep;
import com.percussion.workflow.data.PSUiWorkflowStepRole;
import com.percussion.workflow.data.PSUiWorkflowStepRoleTransition;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author leonardohildt
 * @author rafaelsalis
 * 
 */
public class PSSteppedWorkflowRestServiceTest extends PSRestTestCase<PSSteppedWorkflowRestServiceClient>
{
    private static PSSteppedWorkflowRestServiceClient workflowEditorRestServiceClient;
    
    private Map<String, PSEnumVals> mapWorkflowStates;
    
    // Maximum name lenght when creating/updating a step/workflow
    private static final Integer NAME_MAX_LENGHT = 50;
    
    // Workflow error messages
    private static final String WORKFLOW_NAME_IS_EMPTY = "Workflow name cannot be blank.";
    private static final String WORKFLOW_NAME_IS_INVALID = "Invalid character in workflow name. Characters allowed are: a-z, 0-9, -, _ and [space].";
    private static final String WORKFLOW_NAME_IS_TOO_LONG = "Workflow name cannot have more than " + NAME_MAX_LENGHT + " characters.";
    // Workflow creating error messages
    private static final String WORKFLOW_NAME_CREATE_IS_A_SYSTEM_WORKFLOW = "Cannot create workflow ''{0}'' because it is a restricted workflow name.";
    private static String WORKFLOW_NAME_CREATE_IS_NOT_UNIQUE = "Cannot create workflow ''{0}'' because a workflow named ''{1}'' already exists.";
    // Workflow deleting error messages
    private static final String WORKFLOW_IS_A_SYSTEM_WORKFLOW = "The workflow ''{0}'' cannot be deleted because is a system workflow.";
    private static final String WORKFLOW_HAS_ITEMS = "Cannot delete: ''{0}'' because this workflow has items assigned to it.";
    private static final String WORKFLOW_IS_DEFAULT_WORKFLOW = "The workflow ''{0}'' cannot be deleted because is the default workflow.";

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
    
    @Override
    protected PSSteppedWorkflowRestServiceClient getRestClient(String baseUrl)
    {
        return workflowEditorRestServiceClient;
    }
    
    @BeforeClass
    public static void setupSuite() throws Exception
    {
        workflowEditorRestServiceClient = new PSSteppedWorkflowRestServiceClient(baseUrl);
        setupClient(workflowEditorRestServiceClient);
    }
    
    @Before
    public void setupClient() throws Exception {
        restClient = getRestClient(baseUrl);
        setupClient(restClient, "Admin", 10);
    }
    
    public static void setupClient(PSObjectRestClient restClient) throws Exception {        
        setupClient(restClient, "Admin", 10);
    }
    
    /**
     * Method to test getting a workflow with an invalid workflow name.
     * 
     * @param workflowName
     * @param errorMessage
     */
    @SuppressWarnings("unused")
    private void testGetWorkflowWrongWorkflowName(String workflowName, long expectedErrorCode)
    {
        try
        {
            // call the service
            PSUiWorkflow workflowReturned = workflowEditorRestServiceClient.getWorkflow(workflowName);
            fail("Should have thrown an exception");
        }
        catch (RestClientException e)
        {
            assertEquals("http error code", expectedErrorCode, e.getStatus());
        }
    }
    
    /**
     * Method to test creating a workflow with an invalid workflow name.
     * 
     * @param workflowName
     * @param workflow
     * @param expectedMessageException
     */
    private void testCreateWorkflowWrongWorkflowName(String workflowName, PSUiWorkflow workflow, String expectedMessageException)
    {
        try
        {
            // call the service
            @SuppressWarnings("unused")
            PSUiWorkflow workflowReturned = workflowEditorRestServiceClient.createWorkflow(workflowName, workflow);
            fail("Should have thrown an exception");
        }
        catch (Exception e)
        {
            assertEquals(expectedMessageException, e.getMessage());
        }
    }
    
    /**
     * Method to test updating a workflow with an invalid workflow name.
     * 
     * @param workflowName
     * @param workflow
     * @param expectedMessageException
     */
    private void testUpdateWorkflowWrongWorkflowName(String workflowName, PSUiWorkflow workflow, String expectedMessageException)
    {
        try
        {
            // call the service
            @SuppressWarnings("unused")
            PSUiWorkflow workflowReturned = workflowEditorRestServiceClient.updateWorkflow(workflowName, workflow);
            fail("Should have thrown an exception");
        }
        catch (Exception e)
        {
            assertEquals(expectedMessageException, e.getMessage());
        }
    }
    
    /**
     * Method to test deleting a workflow with an invalid workflow name.
     * 
     * @param workflowName
     * @param expectedMessageException One or more acceptable messages
     */
    private void testDeleteWorkflowWrongWorkflow(String workflowName, String... expectedMessageException)
    {
        try
        {
            // call the service
            workflowEditorRestServiceClient.deleteWorkflow(workflowName);
            fail("Should have thrown an exception");
        }
        catch (Exception e)
        {
            for (int i = 0; i < expectedMessageException.length; i++)
            {
                if (e.getMessage().contains(expectedMessageException[i]))
                    return;
            }
            fail("Not expected message: " + e.getMessage());
        }
    }
    
    /**
     * Method to test creating a state with an invalid state name.
     * 
     * @param workflowName
     * @param stepName
     * @param workflow
     * @param expectedMessageException
     */
    private void testCreateStateWrongStateName(String workflowName, String stepName, PSUiWorkflow workflow, String expectedMessageException)
    {
        try
        {
            // call the service
            @SuppressWarnings("unused")
            PSUiWorkflow workflowReturned = workflowEditorRestServiceClient.createStep(workflowName, stepName, workflow);
            fail("Should have thrown an exception");
        }
        catch (Exception e)
        {
            assertEquals(expectedMessageException, e.getMessage());
        }
    }
    
    /**
     * Method to test updating a state with an invalid state name.
     * 
     * @param workflowName
     * @param previousStepName
     * @param worlkflow
     * @param expectedMessageException
     */
    private void testUpdateStateWrongStateName(String worlkflowName, String previousStepName, PSUiWorkflow workflow, String expectedMessageException)
    {
        try
        {
            // call the service
            @SuppressWarnings("unused")
            PSUiWorkflow workflowReturned = workflowEditorRestServiceClient.updateStep(worlkflowName, previousStepName, workflow);
            fail("Should have thrown an exception");
        }
        catch (Exception e)
        {
            assertEquals(expectedMessageException, e.getMessage());
        }
    }

    /**
     * Method to test deleting a state with an invalid state name.
     * 
     * @param workflowName
     * @param stateName
     * @param expectedMessageException
     */
    private void testDeleteStateWrongState(String workflowName, String stateName, String expectedMessageException)
    {
        try
        {
            // call the service
            @SuppressWarnings("unused")
            PSUiWorkflow workflowReturned = workflowEditorRestServiceClient.deleteState(workflowName, stateName);
            fail("Should have thrown an exception");
        }
        catch (Exception e)
        {
            assertTrue(e.getMessage().contains(expectedMessageException));
        }
    }

    @Test
    public void testGetWorkflowList()
    {
        // Returned workflow list, the call to the service
        PSEnumVals returnedWorkflowList = workflowEditorRestServiceClient.getWorkflowList();

        // Asserts
        assertNotNull(returnedWorkflowList);
        assertFalse(returnedWorkflowList.getEntries().isEmpty());
        assertTrue(returnedWorkflowList.hasValue("Default Workflow"));
    }
    
    @Test
    public void testGetWorkflowMetadataList()
    {
        // Expected workflow list
        PSUiWorkflow uiWorkflow = new PSUiWorkflow();
        uiWorkflow.setWorkflowName("Default Workflow");
        uiWorkflow.setWorkflowDescription(
        		"This workflow requires two approvals before content is published.  It is the default for most content types and is assigned to all communities.");
        uiWorkflow.setDefaultWorkflow(true);

        // Returned workflow list, the call to the service
        List<PSUiWorkflow> returnedWorkflowList = workflowEditorRestServiceClient.getWorkflowMetadataList();

        // Asserts
        assertEquals(returnedWorkflowList.get(0).getWorkflowName(), uiWorkflow.getWorkflowName());
        assertEquals(returnedWorkflowList.get(0).getWorkflowDescription(), uiWorkflow.getWorkflowDescription());
        assertEquals(returnedWorkflowList.get(0).isDefaultWorkflow(), uiWorkflow.isDefaultWorkflow());
    }
    
    @Test
    public void testGetWorkflow_DefaultWorkflow()
    {
        String workflowName = "Default Workflow";

        // Expected workflow
        PSUiWorkflow expectedWorkflow = new PSUiWorkflow();
        expectedWorkflow.setWorkflowName(workflowName);

        List<PSUiWorkflowStep> steps = new ArrayList<PSUiWorkflowStep>();

        PSUiWorkflowStep draftStep = new PSUiWorkflowStep();
        draftStep.setStepName("Draft");

        List<PSUiWorkflowStepRole> draftStepRoles = new ArrayList<PSUiWorkflowStepRole>();
        PSUiWorkflowStepRoleTransition draftApprove = new PSUiWorkflowStepRoleTransition();
        draftApprove.setTransitionPermission("Approve");
        PSUiWorkflowStepRoleTransition draftSubmit = new PSUiWorkflowStepRoleTransition();
        draftSubmit.setTransitionPermission("Submit");

        PSUiWorkflowStepRole draftStepAdminRole = new PSUiWorkflowStepRole("Admin", 1);
        List<PSUiWorkflowStepRoleTransition> draftAdminTransitions = new ArrayList<PSUiWorkflowStepRoleTransition>();
        draftAdminTransitions.add(draftSubmit);
        draftAdminTransitions.add(draftApprove);
        draftStepAdminRole.setRoleTransitions(draftAdminTransitions);
        draftStepRoles.add(draftStepAdminRole);

        PSUiWorkflowStepRole draftStepContributorRole = new PSUiWorkflowStepRole("Contributor", 2);
        List<PSUiWorkflowStepRoleTransition> draftContributorTransitions = new ArrayList<PSUiWorkflowStepRoleTransition>();
        draftContributorTransitions.add(draftSubmit);
        draftStepContributorRole.setRoleTransitions(draftContributorTransitions);
        draftStepRoles.add(draftStepContributorRole);

        PSUiWorkflowStepRole draftStepEditorRole = new PSUiWorkflowStepRole("Editor", 3);
        List<PSUiWorkflowStepRoleTransition> draftEditorTransitions = new ArrayList<PSUiWorkflowStepRoleTransition>();
        draftEditorTransitions.add(draftSubmit);
        draftEditorTransitions.add(draftApprove);
        draftStepEditorRole.setRoleTransitions(draftEditorTransitions);
        draftStepRoles.add(draftStepEditorRole);

        draftStep.setStepRoles(draftStepRoles);
        steps.add(draftStep);

        PSUiWorkflowStep reviewStep = new PSUiWorkflowStep();
        reviewStep.setStepName("Review");

        List<PSUiWorkflowStepRole> reviewStepRoles = new ArrayList<PSUiWorkflowStepRole>();
        PSUiWorkflowStepRoleTransition reviewApprove = new PSUiWorkflowStepRoleTransition();
        reviewApprove.setTransitionPermission("Approve");
        PSUiWorkflowStepRoleTransition reviewReject = new PSUiWorkflowStepRoleTransition();
        reviewReject.setTransitionPermission("Reject");

        PSUiWorkflowStepRole reviewStepAdminRole = new PSUiWorkflowStepRole("Admin", 1);
        List<PSUiWorkflowStepRoleTransition> reviewAdminTransitions = new ArrayList<PSUiWorkflowStepRoleTransition>();
        reviewAdminTransitions.add(reviewReject);
        reviewAdminTransitions.add(reviewApprove);
        reviewStepAdminRole.setRoleTransitions(reviewAdminTransitions);
        reviewStepRoles.add(reviewStepAdminRole);

        PSUiWorkflowStepRole reviewStepEditorRole = new PSUiWorkflowStepRole("Editor", 3);
        List<PSUiWorkflowStepRoleTransition> reviewEditorTransitions = new ArrayList<PSUiWorkflowStepRoleTransition>();
        reviewEditorTransitions.add(reviewReject);
        reviewEditorTransitions.add(reviewApprove);
        reviewStepEditorRole.setRoleTransitions(reviewEditorTransitions);
        reviewStepRoles.add(reviewStepEditorRole);

        reviewStep.setStepRoles(reviewStepRoles);
        steps.add(reviewStep);

        PSUiWorkflowStep approvedStep = new PSUiWorkflowStep();
        approvedStep.setStepName("Approved");

        List<PSUiWorkflowStepRole> approvedStepRoles = new ArrayList<PSUiWorkflowStepRole>();
        PSUiWorkflowStepRoleTransition approvedReject = new PSUiWorkflowStepRoleTransition();
        approvedReject.setTransitionPermission("Reject");
        PSUiWorkflowStepRoleTransition approvedSubmit = new PSUiWorkflowStepRoleTransition();
        approvedSubmit.setTransitionPermission("Submit");
        PSUiWorkflowStepRoleTransition approvedApprove = new PSUiWorkflowStepRoleTransition();
        approvedApprove.setTransitionPermission("Approve");

        PSUiWorkflowStepRole approvedStepAdminRole = new PSUiWorkflowStepRole("Admin", 1);
        List<PSUiWorkflowStepRoleTransition> approvedAdminTransitions = new ArrayList<PSUiWorkflowStepRoleTransition>();
        approvedAdminTransitions.add(approvedSubmit);
        approvedAdminTransitions.add(approvedReject);
        approvedAdminTransitions.add(approvedApprove);
        approvedStepAdminRole.setRoleTransitions(approvedAdminTransitions);
        approvedStepRoles.add(approvedStepAdminRole);

        PSUiWorkflowStepRole approvedStepEditorRole = new PSUiWorkflowStepRole("Editor", 3);
        List<PSUiWorkflowStepRoleTransition> approvedEditorTransitions = new ArrayList<PSUiWorkflowStepRoleTransition>();
        approvedEditorTransitions.add(approvedSubmit);
        approvedEditorTransitions.add(approvedReject);
        approvedEditorTransitions.add(approvedApprove);
        approvedStepEditorRole.setRoleTransitions(approvedEditorTransitions);
        approvedStepRoles.add(approvedStepEditorRole);

        approvedStep.setStepRoles(approvedStepRoles);
        steps.add(approvedStep);

        PSUiWorkflowStep archieveStep = new PSUiWorkflowStep();
        archieveStep.setStepName("Archive");

        List<PSUiWorkflowStepRole> archieveStepRoles = new ArrayList<PSUiWorkflowStepRole>();
        PSUiWorkflowStepRoleTransition archieveApprove = new PSUiWorkflowStepRoleTransition();
        archieveApprove.setTransitionPermission("Approve");

        PSUiWorkflowStepRole archieveStepAdminRole = new PSUiWorkflowStepRole("Admin", 1);
        List<PSUiWorkflowStepRoleTransition> archieveAdminTransitions = new ArrayList<PSUiWorkflowStepRoleTransition>();
        archieveAdminTransitions.add(archieveApprove);
        archieveStepAdminRole.setRoleTransitions(archieveAdminTransitions);
        archieveStepRoles.add(archieveStepAdminRole);

        PSUiWorkflowStepRole archieveStepEditorRole = new PSUiWorkflowStepRole("Editor", 3);
        List<PSUiWorkflowStepRoleTransition> archieveEditorTransitions = new ArrayList<PSUiWorkflowStepRoleTransition>();
        archieveEditorTransitions.add(archieveApprove);
        archieveStepEditorRole.setRoleTransitions(archieveEditorTransitions);
        archieveStepRoles.add(archieveStepEditorRole);

        archieveStep.setStepRoles(archieveStepRoles);
        steps.add(archieveStep);

        expectedWorkflow.setWorkflowSteps(steps);

        // Returned workflow, the call to the service
        PSUiWorkflow returnedWorkflow = workflowEditorRestServiceClient.getWorkflow(workflowName);

        // Asserts
        assertEquals(expectedWorkflow.getWorkflowName(), returnedWorkflow.getWorkflowName());
        assertTrue(returnedWorkflow.getWorkflowSteps().get(0).getStepName().equalsIgnoreCase(
                expectedWorkflow.getWorkflowSteps().get(0).getStepName()));
        assertTrue(returnedWorkflow.getWorkflowSteps().get(1).getStepName().equalsIgnoreCase(
                expectedWorkflow.getWorkflowSteps().get(1).getStepName()));
        assertTrue(returnedWorkflow.getWorkflowSteps().get(2).getStepName().equalsIgnoreCase(
                expectedWorkflow.getWorkflowSteps().get(2).getStepName()));
    }

    @Test
    public void testGetWorkflow_NonExistentWorkflow()
    {
        testGetWorkflowWrongWorkflowName("Non Existent Workflow", 500);
    }

    @Test
    public void testGetStatesChoices_NoWorkflowName()
    {
        try
        {
            @SuppressWarnings("unused")
            PSEnumVals vals = workflowEditorRestServiceClient.getStatesChoices("");
            fail("The workflow name can not be empty. An exception should have been thrown.");
        }
        catch (DataRestClientException e)
        {
            assertTrue(true);
        }
    }

    @Test
    public void testGetStatesChoices_NoExistingWorkflow()
    {
        // build the map of workflowNames -> PSEnumVals
        buildStatesMap();

        String notExistingWorkflow = getNotExistingWorkflowName();

        PSEnumVals emptyVals = workflowEditorRestServiceClient.getStatesChoices(notExistingWorkflow);

        assertNotNull("The returned PSEnumVals object should never be null", emptyVals);
        assertNotNull("The entries should not be null.", emptyVals.getEntries());
        assertTrue("At least must return the Default Workflow.", emptyVals.getEntries().size() > 0);
    }

    @Test
    public void testGetStatesChoices_AllWorkflows()
    {
        // build the map of workflowNames -> PSEnumVals
        buildStatesMap();

        for (String workflowName : mapWorkflowStates.keySet())
        {
            PSEnumVals vals = workflowEditorRestServiceClient.getStatesChoices(workflowName);
            assertTrue("The PSEnumVals object returned should have been the same.", mapWorkflowStates.get(workflowName)
                    .hasSameValues(vals));
        }
    }

    @Test
    public void testCreateWorkflow_EmptyWorkflowName()
    {
        String workflowName = "";
        PSUiWorkflow workflow = new PSUiWorkflow();
        workflow.setWorkflowName(workflowName);
        String expectedMessageException = WORKFLOW_NAME_IS_EMPTY;
        
        testCreateWorkflowWrongWorkflowName(workflowName, workflow, expectedMessageException);
    }
    
    @Test
    public void testCreateWorkflow_VeryLongWorkflowName()
    {
        String workflowName = "This is a very long workflow which have more tan 50 characters";
        PSUiWorkflow workflow = new PSUiWorkflow();
        workflow.setWorkflowName(workflowName);
        String expectedMessageException = WORKFLOW_NAME_IS_TOO_LONG;
        
        testCreateWorkflowWrongWorkflowName(workflowName, workflow, expectedMessageException);
    }
    
    @Test
    public void testCreateWorkflow_InvalidWorkflowName()
    {
        String workflowName;
        PSUiWorkflow workflow;
        String expectedMessageException = WORKFLOW_NAME_IS_INVALID;

        workflowName = "New ! workflow";
        workflow = new PSUiWorkflow();
        workflow.setWorkflowName(workflowName);
        testCreateWorkflowWrongWorkflowName(workflowName, workflow, expectedMessageException);

        workflowName = "New $ workflow";
        workflow = new PSUiWorkflow();
        workflow.setWorkflowName(workflowName);
        testCreateWorkflowWrongWorkflowName(workflowName, workflow, expectedMessageException);

        workflowName = "New%25workflow";
        workflow = new PSUiWorkflow();
        workflow.setWorkflowName(workflowName);
        testCreateWorkflowWrongWorkflowName(workflowName, workflow, expectedMessageException);

        workflowName = "New & workflow";
        workflow = new PSUiWorkflow();
        workflow.setWorkflowName(workflowName);
        testCreateWorkflowWrongWorkflowName(workflowName, workflow, expectedMessageException);

        workflowName = "New / workflow";
        workflow = new PSUiWorkflow();
        workflow.setWorkflowName(workflowName);
        testCreateWorkflowWrongWorkflowName(workflowName, workflow, expectedMessageException);

        workflowName = "New ( workflow";
        workflow = new PSUiWorkflow();
        workflow.setWorkflowName(workflowName);
        testCreateWorkflowWrongWorkflowName(workflowName, workflow, expectedMessageException);

        workflowName = "New ) workflow";
        workflow = new PSUiWorkflow();
        workflow.setWorkflowName(workflowName);
        testCreateWorkflowWrongWorkflowName(workflowName, workflow, expectedMessageException);

        workflowName = "New = workflow";
        workflow = new PSUiWorkflow();
        workflow.setWorkflowName(workflowName);
        testCreateWorkflowWrongWorkflowName(workflowName, workflow, expectedMessageException);

        workflowName = "New%3Fworkflow";
        workflow = new PSUiWorkflow();
        workflow.setWorkflowName(workflowName);
        testCreateWorkflowWrongWorkflowName(workflowName, workflow, expectedMessageException);

        workflowName = "New ' workflow";
        workflow = new PSUiWorkflow();
        workflow.setWorkflowName(workflowName);
        testCreateWorkflowWrongWorkflowName(workflowName, workflow, expectedMessageException);

        workflowName = "New ` workflow";
        workflow = new PSUiWorkflow();
        workflow.setWorkflowName(workflowName);
        testCreateWorkflowWrongWorkflowName(workflowName, workflow, expectedMessageException);

        workflowName = "New + workflow";
        workflow = new PSUiWorkflow();
        workflow.setWorkflowName(workflowName);
        testCreateWorkflowWrongWorkflowName(workflowName, workflow, expectedMessageException);

        workflowName = "New , workflow";
        workflow = new PSUiWorkflow();
        workflow.setWorkflowName(workflowName);
        testCreateWorkflowWrongWorkflowName(workflowName, workflow, expectedMessageException);

        workflowName = "New . workflow";
        workflow = new PSUiWorkflow();
        workflow.setWorkflowName(workflowName);
        testCreateWorkflowWrongWorkflowName(workflowName, workflow, expectedMessageException);

        workflowName = "New < workflow";
        workflow = new PSUiWorkflow();
        workflow.setWorkflowName(workflowName);
        testCreateWorkflowWrongWorkflowName(workflowName, workflow, expectedMessageException);

        workflowName = "New > workflow";
        workflow = new PSUiWorkflow();
        workflow.setWorkflowName(workflowName);
        testCreateWorkflowWrongWorkflowName(workflowName, workflow, expectedMessageException);

        workflowName = "New | workflow";
        workflow = new PSUiWorkflow();
        workflow.setWorkflowName(workflowName);
        testCreateWorkflowWrongWorkflowName(workflowName, workflow, expectedMessageException);

        workflowName = "New @ workflow";
        workflow = new PSUiWorkflow();
        workflow.setWorkflowName(workflowName);
        testCreateWorkflowWrongWorkflowName(workflowName, workflow, expectedMessageException);

        workflowName = "New%23workflow";
        workflow = new PSUiWorkflow();
        workflow.setWorkflowName(workflowName);
        testCreateWorkflowWrongWorkflowName(workflowName, workflow, expectedMessageException);

        workflowName = "New ~ workflow";
        workflow = new PSUiWorkflow();
        workflow.setWorkflowName(workflowName);
        testCreateWorkflowWrongWorkflowName(workflowName, workflow, expectedMessageException);
    }
    
    @Test
    public void testCreateWorkflow_SystemWorkflowName()
    {
        String workflowName;
        PSUiWorkflow workflow;
        String expectedMessageException;

        workflowName = "LocalContent";
        workflow = new PSUiWorkflow();
        workflow.setWorkflowName(workflowName);
        expectedMessageException = MessageFormat.format(
                WORKFLOW_NAME_CREATE_IS_A_SYSTEM_WORKFLOW, workflowName);
        testCreateWorkflowWrongWorkflowName(workflowName, workflow, expectedMessageException);
        
        workflowName = "Local Content";
        workflow = new PSUiWorkflow();
        workflow.setWorkflowName(workflowName);
        expectedMessageException = MessageFormat.format(
                WORKFLOW_NAME_CREATE_IS_A_SYSTEM_WORKFLOW, workflowName);
        testCreateWorkflowWrongWorkflowName(workflowName, workflow, expectedMessageException);
    }
    
    @Test
    public void testCreateWorkflow_NotUniqueWorkflowName()
    {
        String workflowName = "testCreateWorkflow_NotUniqueWorkflowName";
        PSUiWorkflow workflow = new PSUiWorkflow();
        workflow.setWorkflowName(workflowName);
        String expectedMessageException = MessageFormat.format(
                WORKFLOW_NAME_CREATE_IS_NOT_UNIQUE, workflowName, workflowName);

        // Create a workflow
        restClient.createWorkflow(workflowName, workflow);
        
        try
        {
            testCreateWorkflowWrongWorkflowName(workflowName, workflow, expectedMessageException);
        }
        finally
        {
            // Delete the created workflow
            restClient.deleteWorkflow(workflowName);
        }
    }
    
    @Test
    public void testCreateWorkflow()
    {
        String workflowName = "testCreateWorkflow";
        PSUiWorkflow workflow = new PSUiWorkflow();
        workflow.setWorkflowName(workflowName);
        
        // Call the service
        PSUiWorkflow returnedWorkflow = workflowEditorRestServiceClient.createWorkflow(workflowName, workflow);

        // Expected workflow
        PSUiWorkflow expectedWorkflow = getExpectedNewWorkflow(workflowName, false);
        
        try
        {
            validateNewWorkflow(returnedWorkflow, expectedWorkflow);
        }
        finally
        {
            // Delete the created workflow
            restClient.deleteWorkflow(workflowName);
        }
    }

    private PSUiWorkflow getExpectedNewWorkflow(String workflowName, boolean isDefault)
    {
        PSUiWorkflow expectedWorkflow = new PSUiWorkflow();
        expectedWorkflow.setWorkflowName(workflowName);
        if (isDefault)
            expectedWorkflow.setDefaultWorkflow(true);
        List<PSUiWorkflowStep> steps = new ArrayList<PSUiWorkflowStep>();
        PSUiWorkflowStep draftStep = new PSUiWorkflowStep();
        draftStep.setStepName("Draft");
        steps.add(draftStep);
        PSUiWorkflowStep reviewStep = new PSUiWorkflowStep();
        reviewStep.setStepName("Review");
        steps.add(reviewStep);
        PSUiWorkflowStep approvedStep = new PSUiWorkflowStep();
        approvedStep.setStepName("Approved");
        steps.add(approvedStep);
        PSUiWorkflowStep archieveStep = new PSUiWorkflowStep();
        archieveStep.setStepName("Archive");
        steps.add(archieveStep);
        expectedWorkflow.setWorkflowSteps(steps);
        return expectedWorkflow;
    }
    
    @Test
    public void testCreateWorkflow_DefaultWorkflow()
    {
        String workflowName = "Test Default";
        PSUiWorkflow workflow = new PSUiWorkflow();
        workflow.setWorkflowName(workflowName);
        workflow.setDefaultWorkflow(true);
        
        // Call the service
        PSUiWorkflow returnedWorkflow = workflowEditorRestServiceClient.createWorkflow(workflowName, workflow);
        
        // need to wait for content to be reassigned
        try
        {
            Thread.sleep(2000);
        }
        catch (InterruptedException e)
        {
            // okay, we tried
        }

        try
        {
            // Asserts
            validateNewWorkflow(returnedWorkflow, getExpectedNewWorkflow(workflowName, true));
        }
        finally
        {
            restoreDefaultWorkflow();
            
            // Delete the created workflow
            restClient.deleteWorkflow(workflowName);
        }
    }

    private void validateNewWorkflow(PSUiWorkflow returnedWorkflow, PSUiWorkflow expectedWorkflow)
    {
        assertEquals(expectedWorkflow.getWorkflowName(), returnedWorkflow.getWorkflowName());
        assertEquals(expectedWorkflow.isDefaultWorkflow(), returnedWorkflow.isDefaultWorkflow());
        assertTrue(returnedWorkflow.getWorkflowSteps().get(0).getStepName().equalsIgnoreCase(
                expectedWorkflow.getWorkflowSteps().get(0).getStepName()));
        assertTrue(returnedWorkflow.getWorkflowSteps().get(1).getStepName().equalsIgnoreCase(
                expectedWorkflow.getWorkflowSteps().get(1).getStepName()));
        assertTrue(returnedWorkflow.getWorkflowSteps().get(2).getStepName().equalsIgnoreCase(
                expectedWorkflow.getWorkflowSteps().get(2).getStepName()));
        assertTrue(returnedWorkflow.getWorkflowSteps().get(3).getStepName().equalsIgnoreCase(
                expectedWorkflow.getWorkflowSteps().get(3).getStepName()));
    }
    
    @Test
    public void testUpdateWorkflow_InconsistentParams()
    {
        PSUiWorkflow workflow = new PSUiWorkflow();
        String workflowNameUrl = "url";
        String workflowNameObject = "object";
        workflow.setPreviousWorkflowName(workflowNameObject);
        String expectedMessageException = "Parameters values are inconsistent with the values passed in the object";
        
        testUpdateWorkflowWrongWorkflowName(workflowNameUrl, workflow, expectedMessageException);
    }
    
    @Test
    public void testUpdateWorkflow()
    {
        String workflowNameAfterUpdating = "testUpdateWorkflow modified";
        String workflowNameBeforeUpdating = "testUpdateWorkflow";
        String toDelete = workflowNameBeforeUpdating;
        PSUiWorkflow workflow = new PSUiWorkflow();
        workflow.setWorkflowName(workflowNameBeforeUpdating);
        
        // Call the service
        PSUiWorkflow expectedWorkflow = workflowEditorRestServiceClient.createWorkflow(workflowNameBeforeUpdating, workflow);
        
        // Rename the workflow
        expectedWorkflow.setWorkflowName(workflowNameAfterUpdating);
        expectedWorkflow.setPreviousWorkflowName(workflowNameBeforeUpdating);
        
        try
        {
            // Call the service
            PSUiWorkflow returnedWorkflow = workflowEditorRestServiceClient.updateWorkflow(workflowNameBeforeUpdating, expectedWorkflow);
            toDelete = workflowNameAfterUpdating;
            
            // Asserts
            validateNewWorkflow(returnedWorkflow, expectedWorkflow);
        }
        finally
        {
            // Delete the created workflow
            restClient.deleteWorkflow(toDelete);
        }
    }
    
    @Test
    public void testUpdateWorkflow_DefaultWorkflow()
    {
        String workflowNameAfterUpdating = "testUpdateWorkflow_DefaultWorkflow modified";
        String workflowNameBeforeUpdating = "testUpdateWorkflow_DefaultWorkflow";
        String workflowToDelete = workflowNameBeforeUpdating;
        PSUiWorkflow workflow = new PSUiWorkflow();
        workflow.setWorkflowName(workflowNameBeforeUpdating);
        workflow.setDefaultWorkflow(true);
        
        // Call the service
        PSUiWorkflow expectedWorkflow = workflowEditorRestServiceClient.createWorkflow(workflowNameBeforeUpdating, workflow);
        
        // Rename the workflow
        expectedWorkflow.setWorkflowName(workflowNameAfterUpdating);
        expectedWorkflow.setPreviousWorkflowName(workflowNameBeforeUpdating);
        expectedWorkflow.setDefaultWorkflow(true);
        
        // need to wait for content to be reassigned
        try
        {
            Thread.sleep(2000);
        }
        catch (InterruptedException e)
        {
            // okay, we tried
        }
        
        try
        {
            // Call the service
            PSUiWorkflow returnedWorkflow = workflowEditorRestServiceClient.updateWorkflow(workflowNameBeforeUpdating, expectedWorkflow);
            workflowToDelete = workflowNameAfterUpdating;
            
            // Asserts
            validateNewWorkflow(returnedWorkflow, getExpectedNewWorkflow(workflowNameAfterUpdating, true));
        }
        finally
        {
            restoreDefaultWorkflow();
            
            // Delete the created workflow
            restClient.deleteWorkflow(workflowToDelete);
        }
    }
    
    @Test
    public void testDeleteWorkflow_SystemWorkflow()
    {
        String workflowName;
        String expectedMessageException;

        workflowName = "Local Content";
        expectedMessageException = MessageFormat.format(WORKFLOW_IS_A_SYSTEM_WORKFLOW, "Local Content");
        // Call the service
        testDeleteWorkflowWrongWorkflow(workflowName, expectedMessageException);
        
        workflowName = "LocalContent";
        expectedMessageException = MessageFormat.format(WORKFLOW_IS_A_SYSTEM_WORKFLOW, "LocalContent");
        // Call the service
        testDeleteWorkflowWrongWorkflow(workflowName, expectedMessageException);
        
        workflowName = " local content ";
        expectedMessageException = MessageFormat.format(WORKFLOW_IS_A_SYSTEM_WORKFLOW, "local content");
        // Call the service
        testDeleteWorkflowWrongWorkflow(workflowName, expectedMessageException);
    }
    
    @Test
    public void testDeleteWorkflow_DefaultWorkflow()
    {
        String newDefaultWorkflowName = "testDeleteWorkflow_DefaultWorkflow";
        String newWorkflowName;
        PSUiWorkflow newWorkflow = new PSUiWorkflow();
        newWorkflow.setWorkflowName(newDefaultWorkflowName);
        newWorkflow.setDefaultWorkflow(true);
        
        String[] expectedMessageException = new String[2];
        
        // Create a workflow
        restClient.createWorkflow(newDefaultWorkflowName, newWorkflow);
        
        try
        {
            newWorkflowName = "testDeleteWorkflow_DefaultWorkflow";
            expectedMessageException[0] = MessageFormat.format(WORKFLOW_IS_DEFAULT_WORKFLOW, "testDeleteWorkflow_DefaultWorkflow");
            expectedMessageException[1] = MessageFormat.format(WORKFLOW_HAS_ITEMS, "testDeleteWorkflow_DefaultWorkflow");
            // Call the service
            testDeleteWorkflowWrongWorkflow(newWorkflowName, expectedMessageException);
        }
        finally
        {
            restoreDefaultWorkflow();
            
            // Delete the created workflow
            restClient.deleteWorkflow(newDefaultWorkflowName);
        }
    }
    
    @Test
    public void testDeleteWorkflow()
    {
        String workflowName = "testDeleteWorkflow";
        PSUiWorkflow workflow = new PSUiWorkflow();
        workflow.setWorkflowName(workflowName);
        workflow.setDefaultWorkflow(false);
        
        // Get the workflow
        workflowEditorRestServiceClient.createWorkflow(workflowName, workflow);

        List<PSUiWorkflow> expectedWorkflowList;
        try
        {
            expectedWorkflowList = workflowEditorRestServiceClient.getWorkflowMetadataList();
            assertTrue(isWorkflowInList(workflowName, expectedWorkflowList));
        }
        finally
        {
            // Call the service to delete
            workflowEditorRestServiceClient.deleteWorkflow(workflowName);
        }
                
        expectedWorkflowList = workflowEditorRestServiceClient.getWorkflowMetadataList();
        assertFalse(isWorkflowInList(workflowName, expectedWorkflowList));
    }

    private boolean isWorkflowInList(String workflowName, List<PSUiWorkflow> expectedWorkflowList)
    {
        boolean found = false;
        for (PSUiWorkflow wf : expectedWorkflowList)
        {
            if (wf.getWorkflowName().equals(workflowName))
            {
                found = true;
            }
        }
        
        return found;
    }
    
    @Test
    public void testCreateState_InconsistentParams()
    {
        PSUiWorkflow workflow;
        List<PSUiWorkflowStep> workflowSteps;
        PSUiWorkflowStep workflowStep;
        String workflowNameUrl;
        String workflowNameObject;
        String stepNameUrl;
        String stepNameObject;
        String expectedMessageException = "Parameters values are inconsistent with the values passed in the object";
        
        workflow = new PSUiWorkflow();
        workflowStep = new PSUiWorkflowStep();
        workflowNameUrl = "workflow url";
        workflowNameObject = "workflow object";
        stepNameUrl = "step url";
        stepNameObject = "step object";
        workflow.setWorkflowName(workflowNameObject);
                workflowStep.setStepName(stepNameObject);
        workflowSteps = new ArrayList<PSUiWorkflowStep>();
        workflowSteps.add(workflowStep);
        workflow.setWorkflowSteps(workflowSteps);
        testCreateStateWrongStateName(workflowNameUrl, stepNameUrl, workflow, expectedMessageException);
        
        workflow = new PSUiWorkflow();
        workflowStep = new PSUiWorkflowStep();
        workflowNameUrl = "workflow url";
        workflowNameObject = "workflow object";
        stepNameUrl = "step object";
        stepNameObject = "step object";
        workflow.setWorkflowName(workflowNameObject);
                workflowStep.setStepName(stepNameObject);
        workflowSteps = new ArrayList<PSUiWorkflowStep>();
        workflowSteps.add(workflowStep);
        workflow.setWorkflowSteps(workflowSteps);
        testCreateStateWrongStateName(workflowNameUrl, stepNameUrl, workflow, expectedMessageException);
        
        workflow = new PSUiWorkflow();
        workflowStep = new PSUiWorkflowStep();
        workflowNameUrl = "workflow object";
        workflowNameObject = "workflow object";
        stepNameUrl = "step url";
        stepNameObject = "step object";
        workflow.setWorkflowName(workflowNameObject);
                workflowStep.setStepName(stepNameObject);
        workflowSteps = new ArrayList<PSUiWorkflowStep>();
        workflowSteps.add(workflowStep);
        workflow.setWorkflowSteps(workflowSteps);
        testCreateStateWrongStateName(workflowNameUrl, stepNameUrl, workflow, expectedMessageException);
    }
    
    @Test
    public void testCreateState_EmptyStateName() throws Exception
    {
        String workflowName = "testCreateStateEmptyName";
        PSUiWorkflow workflow = new PSUiWorkflow();
        workflow.setWorkflowName(workflowName);
        workflow.setDefaultWorkflow(false);
        
        // Get the workflow
        workflowEditorRestServiceClient.createWorkflow(workflowName, workflow);
        List<PSUiWorkflowStep> workflowSteps;
        PSUiWorkflowStep workflowStep;
        String newStateName;
        String expectedMessageException = STATE_NAME_IS_EMPTY;

        workflowStep = new PSUiWorkflowStep();

        newStateName = "";
        workflowStep.setStepName(newStateName);
        workflowSteps = new ArrayList<PSUiWorkflowStep>();
        workflowSteps.add(workflowStep);
        workflow.setWorkflowSteps(workflowSteps);
        workflow.setPreviousStepName("Draft");
        try
        {
            testCreateStateWrongStateName(workflowName, newStateName, workflow, expectedMessageException);
        }
        finally
        {
            workflowEditorRestServiceClient.deleteWorkflow(workflowName);
        }
    }
    
    @Test
    public void testCreateState_VeryLongStateName()
    {
        String workflowName = "testCreateStateLongName";
        PSUiWorkflow workflow = new PSUiWorkflow();
        workflow.setWorkflowName(workflowName);
        workflow.setDefaultWorkflow(false);
        workflowEditorRestServiceClient.createWorkflow(workflowName, workflow);
        
        List<PSUiWorkflowStep> workflowSteps;
        PSUiWorkflowStep workflowStep;
        String newStateName = "This is a very long step which have more tan 50 characters";
        String expectedMessageException = STATE_NAME_IS_TOO_LONG;
        
        workflowStep = new PSUiWorkflowStep();
        workflowStep.setStepName(newStateName);
        workflowSteps = new ArrayList<PSUiWorkflowStep>();
        workflowSteps.add(workflowStep);
        workflow.setWorkflowSteps(workflowSteps);
        workflow.setPreviousStepName("Draft");
        
        try
        {
            testCreateStateWrongStateName(workflowName, newStateName, workflow, expectedMessageException);
        }
        finally
        {
            workflowEditorRestServiceClient.deleteWorkflow(workflowName);
        }
    }

    @Test
    public void testCreateState_InvalidStateName()
    {
        String workflowName = "testCreateStateInvalidName";
        PSUiWorkflow workflow = new PSUiWorkflow();
        workflow.setWorkflowName(workflowName);
        workflow.setDefaultWorkflow(false);
        workflowEditorRestServiceClient.createWorkflow(workflowName, workflow);
        
        List<PSUiWorkflowStep> workflowSteps;
        PSUiWorkflowStep workflowStep;
        
        String newStateName;
        String expectedMessageException = STATE_NAME_IS_INVALID;

        workflow.setPreviousStepName("Draft");
        workflowStep = new PSUiWorkflowStep();

        try
        {
            newStateName = "New ! state";
            workflowStep.setStepName(newStateName);
            workflowSteps = new ArrayList<PSUiWorkflowStep>();
            workflowSteps.add(workflowStep);
            workflow.setWorkflowSteps(workflowSteps);
            testCreateStateWrongStateName(workflowName, newStateName, workflow, expectedMessageException);

            newStateName = "New $ state";
            workflowStep.setStepName(newStateName);
            workflowSteps = new ArrayList<PSUiWorkflowStep>();
            workflowSteps.add(workflowStep);
            workflow.setWorkflowSteps(workflowSteps);
            testCreateStateWrongStateName(workflowName, newStateName, workflow, expectedMessageException);

            newStateName = "New % state";
            workflowStep.setStepName(newStateName);
            workflowSteps = new ArrayList<PSUiWorkflowStep>();
            workflowSteps.add(workflowStep);
            workflow.setWorkflowSteps(workflowSteps);
            testCreateStateWrongStateName(workflowName, newStateName, workflow, expectedMessageException);

            newStateName = "New & state";
            workflowStep.setStepName(newStateName);
            workflowSteps = new ArrayList<PSUiWorkflowStep>();
            workflowSteps.add(workflowStep);
            workflow.setWorkflowSteps(workflowSteps);
            testCreateStateWrongStateName(workflowName, newStateName, workflow, expectedMessageException);

            newStateName = "New ( state";
            workflowStep.setStepName(newStateName);
            workflowSteps = new ArrayList<PSUiWorkflowStep>();
            workflowSteps.add(workflowStep);
            workflow.setWorkflowSteps(workflowSteps);
            testCreateStateWrongStateName(workflowName, newStateName, workflow, expectedMessageException);

            newStateName = "New ) state";
            workflowStep.setStepName(newStateName);
            workflowSteps = new ArrayList<PSUiWorkflowStep>();
            workflowSteps.add(workflowStep);
            workflow.setWorkflowSteps(workflowSteps);
            testCreateStateWrongStateName(workflowName, newStateName, workflow, expectedMessageException);

            newStateName = "New = state";
            workflowStep.setStepName(newStateName);
            workflowSteps = new ArrayList<PSUiWorkflowStep>();
            workflowSteps.add(workflowStep);
            workflow.setWorkflowSteps(workflowSteps);
            testCreateStateWrongStateName(workflowName, newStateName, workflow, expectedMessageException);

            newStateName = "New ' state";
            workflowStep.setStepName(newStateName);
            workflowSteps = new ArrayList<PSUiWorkflowStep>();
            workflowSteps.add(workflowStep);
            workflow.setWorkflowSteps(workflowSteps);
            testCreateStateWrongStateName(workflowName, newStateName, workflow, expectedMessageException);

            newStateName = "New ` state";
            workflowStep.setStepName(newStateName);
            workflowSteps = new ArrayList<PSUiWorkflowStep>();
            workflowSteps.add(workflowStep);
            workflow.setWorkflowSteps(workflowSteps);
            testCreateStateWrongStateName(workflowName, newStateName, workflow, expectedMessageException);

            newStateName = "New + state";
            workflowStep.setStepName(newStateName);
            workflowSteps = new ArrayList<PSUiWorkflowStep>();
            workflowSteps.add(workflowStep);
            workflow.setWorkflowSteps(workflowSteps);
            testCreateStateWrongStateName(workflowName, newStateName, workflow, expectedMessageException);

            newStateName = "New , state";
            workflowStep.setStepName(newStateName);
            workflowSteps = new ArrayList<PSUiWorkflowStep>();
            workflowSteps.add(workflowStep);
            workflow.setWorkflowSteps(workflowSteps);
            testCreateStateWrongStateName(workflowName, newStateName, workflow, expectedMessageException);

            newStateName = "New . state";
            workflowStep.setStepName(newStateName);
            workflowSteps = new ArrayList<PSUiWorkflowStep>();
            workflowSteps.add(workflowStep);
            workflow.setWorkflowSteps(workflowSteps);
            testCreateStateWrongStateName(workflowName, newStateName, workflow, expectedMessageException);

            newStateName = "New < state";
            workflowStep.setStepName(newStateName);
            workflowSteps = new ArrayList<PSUiWorkflowStep>();
            workflowSteps.add(workflowStep);
            workflow.setWorkflowSteps(workflowSteps);
            testCreateStateWrongStateName(workflowName, newStateName, workflow, expectedMessageException);

            newStateName = "New > state";
            workflowStep.setStepName(newStateName);
            workflowSteps = new ArrayList<PSUiWorkflowStep>();
            workflowSteps.add(workflowStep);
            workflow.setWorkflowSteps(workflowSteps);
            testCreateStateWrongStateName(workflowName, newStateName, workflow, expectedMessageException);

            newStateName = "New | state";
            workflowStep.setStepName(newStateName);
            workflowSteps = new ArrayList<PSUiWorkflowStep>();
            workflowSteps.add(workflowStep);
            workflow.setWorkflowSteps(workflowSteps);
            testCreateStateWrongStateName(workflowName, newStateName, workflow, expectedMessageException);

            newStateName = "New @ state";
            workflowStep.setStepName(newStateName);
            workflowSteps = new ArrayList<PSUiWorkflowStep>();
            workflowSteps.add(workflowStep);
            workflow.setWorkflowSteps(workflowSteps);
            testCreateStateWrongStateName(workflowName, newStateName, workflow, expectedMessageException);

            newStateName = "New ~ state";
            workflowStep.setStepName(newStateName);
            workflowSteps = new ArrayList<PSUiWorkflowStep>();
            workflowSteps.add(workflowStep);
            workflow.setWorkflowSteps(workflowSteps);
            testCreateStateWrongStateName(workflowName, newStateName, workflow, expectedMessageException);
        }
        finally
        {
            workflowEditorRestServiceClient.deleteWorkflow(workflowName);
        }
    }

    @Test
    public void testCreateState_SystemStateName()
    {
        
        String workflowName = "testCreateStateSystemName";
        PSUiWorkflow workflow = new PSUiWorkflow();
        workflow.setWorkflowName(workflowName);
        workflow.setDefaultWorkflow(false);
        workflowEditorRestServiceClient.createWorkflow(workflowName, workflow);
        
        List<PSUiWorkflowStep> workflowSteps;
        PSUiWorkflowStep workflowStep;
        String newStateName;
        String expectedMessageException;

        workflow.setPreviousStepName("Draft");
        workflowStep = new PSUiWorkflowStep();

        try
        {
            newStateName = "pending";
            workflowStep.setStepName(newStateName);
            workflowSteps = new ArrayList<PSUiWorkflowStep>();
            workflowSteps.add(workflowStep);
            workflow.setWorkflowSteps(workflowSteps);
            expectedMessageException = MessageFormat.format(STATE_NAME_CREATE_IS_A_SYSTEM_STATE, newStateName);
            testCreateStateWrongStateName(workflowName, newStateName, workflow, expectedMessageException);

            newStateName = "LiVe";
            workflowStep.setStepName(newStateName);
            workflowSteps = new ArrayList<PSUiWorkflowStep>();
            workflowSteps.add(workflowStep);
            workflow.setWorkflowSteps(workflowSteps);
            expectedMessageException = MessageFormat.format(STATE_NAME_CREATE_IS_A_SYSTEM_STATE, newStateName);
            testCreateStateWrongStateName(workflowName, newStateName, workflow, expectedMessageException);

            newStateName = "EdITLive";
            workflowStep.setStepName(newStateName);
            workflowSteps = new ArrayList<PSUiWorkflowStep>();
            workflowSteps.add(workflowStep);
            workflow.setWorkflowSteps(workflowSteps);
            expectedMessageException = MessageFormat.format(STATE_NAME_CREATE_IS_A_SYSTEM_STATE, newStateName);
            testCreateStateWrongStateName(workflowName, newStateName, workflow, expectedMessageException);

            newStateName = "ApproveD";
            workflowStep.setStepName(newStateName);
            workflowSteps = new ArrayList<PSUiWorkflowStep>();
            workflowSteps.add(workflowStep);
            workflow.setWorkflowSteps(workflowSteps);
            expectedMessageException = MessageFormat.format(STATE_NAME_CREATE_IS_A_SYSTEM_STATE, newStateName);
            testCreateStateWrongStateName(workflowName, newStateName, workflow, expectedMessageException);
        }
        finally
        {
            workflowEditorRestServiceClient.deleteWorkflow(workflowName);
        }
    }

    @Test
    public void testCreateState_NotUniqueStateName()
    {
        
        String workflowName = "testCreateStateDupeName";
        PSUiWorkflow workflow = new PSUiWorkflow();
        workflow.setWorkflowName(workflowName);
        workflow.setDefaultWorkflow(false);
        workflowEditorRestServiceClient.createWorkflow(workflowName, workflow);
        
        List<PSUiWorkflowStep> workflowSteps;
        PSUiWorkflowStep workflowStep;
        String newStateName;
        String expectedMessageException;

        workflowStep = new PSUiWorkflowStep();

        try
        {
            newStateName = "draft";
            workflowStep.setStepName(newStateName);
            workflowSteps = new ArrayList<PSUiWorkflowStep>();
            workflowSteps.add(workflowStep);
            workflow.setWorkflowSteps(workflowSteps);
            expectedMessageException = MessageFormat.format(STATE_NAME_CREATE_IS_NOT_UNIQUE, newStateName, "Draft");
            testCreateStateWrongStateName(workflowName, newStateName, workflow, expectedMessageException);

            newStateName = "Draft";
            workflowStep.setStepName(newStateName);
            workflowSteps = new ArrayList<PSUiWorkflowStep>();
            workflowSteps.add(workflowStep);
            workflow.setWorkflowSteps(workflowSteps);
            expectedMessageException = MessageFormat.format(STATE_NAME_CREATE_IS_NOT_UNIQUE, newStateName, "Draft");
            testCreateStateWrongStateName(workflowName, newStateName, workflow, expectedMessageException);
        }
        finally
        {
            workflowEditorRestServiceClient.deleteWorkflow(workflowName);
        }
    }
    
    @Test
    public void testCreateState()
    {
        String workflowName = "testCreateState";
        PSUiWorkflow workflow = new PSUiWorkflow();
        workflow.setWorkflowName(workflowName);
        workflow.setDefaultWorkflow(false);
        workflowEditorRestServiceClient.createWorkflow(workflowName, workflow);
        
        
        try
        {
            // Create the state
            workflow = createState(workflowName, "Test", "Draft");
            
            // Call the service
            PSUiWorkflow returnedWorkflow = workflowEditorRestServiceClient.createStep(workflow.getWorkflowName(), workflow.getWorkflowSteps().get(0).getStepName(), workflow);

            // Expected workflow
            PSUiWorkflow expectedWorkflow = new PSUiWorkflow();
            expectedWorkflow.setWorkflowName(workflow.getWorkflowName());
            List<PSUiWorkflowStep> steps = new ArrayList<PSUiWorkflowStep>();
            PSUiWorkflowStep draftStep = new PSUiWorkflowStep();
            draftStep.setStepName("Draft");
            steps.add(draftStep);
            steps.add(workflow.getWorkflowSteps().get(0));
            PSUiWorkflowStep reviewStep = new PSUiWorkflowStep();
            reviewStep.setStepName("Review");
            steps.add(reviewStep);
            PSUiWorkflowStep approvedStep = new PSUiWorkflowStep();
            approvedStep.setStepName("Approved");
            steps.add(approvedStep);
            PSUiWorkflowStep archieveStep = new PSUiWorkflowStep();
            archieveStep.setStepName("Archive");
            steps.add(archieveStep);
            expectedWorkflow.setWorkflowSteps(steps);
            
            // Asserts
            assertEquals(expectedWorkflow.getWorkflowName(), returnedWorkflow.getWorkflowName());
            assertTrue(returnedWorkflow.getWorkflowSteps().get(0).getStepName().equalsIgnoreCase(
                    expectedWorkflow.getWorkflowSteps().get(0).getStepName()));
            // The created state
            assertTrue(returnedWorkflow.getWorkflowSteps().get(2).getStepName().equalsIgnoreCase(
                    expectedWorkflow.getWorkflowSteps().get(2).getStepName()));
            assertTrue(returnedWorkflow.getWorkflowSteps().get(3).getStepName().equalsIgnoreCase(
                    expectedWorkflow.getWorkflowSteps().get(3).getStepName()));
            assertTrue(returnedWorkflow.getWorkflowSteps().get(4).getStepName().equalsIgnoreCase(
                    expectedWorkflow.getWorkflowSteps().get(4).getStepName()));
        }
        finally
        {
            workflowEditorRestServiceClient.deleteWorkflow(workflowName);
        }
    }
    
    @Test
    public void testDeleteState_SystemState()
    {
        String workflowName = "testDeleteSystemStep";
        PSUiWorkflow workflow = new PSUiWorkflow();
        workflow.setWorkflowName(workflowName);
        workflow.setDefaultWorkflow(false);
        workflowEditorRestServiceClient.createWorkflow(workflowName, workflow);
        
        try
        {
            String stateName;
            String expectedMessageException = STATE_IS_A_SYSTEM_STATE;

            stateName = "Draft";
            // Call the service
            testDeleteStateWrongState(workflowName, stateName, expectedMessageException);
            
            stateName = "draft";
            // Call the service
            testDeleteStateWrongState(workflowName, stateName, expectedMessageException);
            
            stateName = " Draft ";
            // Call the service
            testDeleteStateWrongState(workflowName, stateName, expectedMessageException);
        }
        finally
        {
            workflowEditorRestServiceClient.deleteWorkflow(workflowName);
        }
    }
    
    @Test
    public void testDeleteState()
    {
        String workflowName = "testDeleteState";
        PSUiWorkflow workflow = new PSUiWorkflow();
        workflow.setWorkflowName(workflowName);
        workflow.setDefaultWorkflow(false);
        workflowEditorRestServiceClient.createWorkflow(workflowName, workflow);
        
        try
        {
            // Create the state
            workflow = createState(workflowName, "Test", "Draft");
            
            // Call the service
            PSUiWorkflow returnedWorkflow = workflowEditorRestServiceClient.createStep(workflow.getWorkflowName(), workflow.getWorkflowSteps().get(0).getStepName(), workflow);

            String stateName = "Test";
            workflow = workflowEditorRestServiceClient.getWorkflow(workflowName);
            PSUiWorkflowStep deletedStep = workflow.getWorkflowSteps().get(1);

            // Call the service
            returnedWorkflow = workflowEditorRestServiceClient.deleteState(workflowName, stateName);
            
            assertTrue(!returnedWorkflow.getWorkflowSteps().contains(deletedStep));
        }
        finally
        {
            workflowEditorRestServiceClient.deleteWorkflow(workflowName);
        }
        
    }
    
    @Test
    public void testUpdateState_InconsistentParams()
    {
        PSUiWorkflow workflow;
        List<PSUiWorkflowStep> workflowSteps;
        PSUiWorkflowStep workflowStep;
        String workflowNameUrl;
        String workflowNameObject;
        String stepNameUrl;
        String stepNameObject;
        String expectedMessageException = "Parameters values are inconsistent with the values passed in the object";
        
        workflow = new PSUiWorkflow();
        workflowStep = new PSUiWorkflowStep();
        workflowNameUrl = "workflow url";
        workflowNameObject = "workflow object";
        stepNameUrl = "step url";
        stepNameObject = "step object";
        workflow.setWorkflowName(workflowNameObject);
        workflow.setPreviousStepName(stepNameObject);
        workflowSteps = new ArrayList<PSUiWorkflowStep>();
        workflowSteps.add(workflowStep);
        workflow.setWorkflowSteps(workflowSteps);
        testUpdateStateWrongStateName(workflowNameUrl, stepNameUrl, workflow, expectedMessageException);
        
        workflow = new PSUiWorkflow();
        workflowStep = new PSUiWorkflowStep();
        workflowNameUrl = "workflow url";
        workflowNameObject = "workflow object";
        stepNameUrl = "step object";
        stepNameObject = "step object";
        workflow.setWorkflowName(workflowNameObject);
        workflow.setPreviousStepName(stepNameObject);
        workflowSteps = new ArrayList<PSUiWorkflowStep>();
        workflowSteps.add(workflowStep);
        workflow.setWorkflowSteps(workflowSteps);
        testUpdateStateWrongStateName(workflowNameUrl, stepNameUrl, workflow, expectedMessageException);
        
        workflow = new PSUiWorkflow();
        workflowStep = new PSUiWorkflowStep();
        workflowNameUrl = "workflow object";
        workflowNameObject = "workflow object";
        stepNameUrl = "step url";
        stepNameObject = "step object";
        workflow.setWorkflowName(workflowNameObject);
        workflow.setPreviousStepName(stepNameObject);
        workflowSteps = new ArrayList<PSUiWorkflowStep>();
        workflowSteps.add(workflowStep);
        workflow.setWorkflowSteps(workflowSteps);
        testUpdateStateWrongStateName(workflowNameUrl, stepNameUrl, workflow, expectedMessageException);
    }
    
    @Test
    public void testUpdateState_NotUniqueStateName()
    {
        String workflowName = "testUpdateStateNonUnique";
        PSUiWorkflow workflow = new PSUiWorkflow();
        workflow.setWorkflowName(workflowName);
        workflow.setDefaultWorkflow(false);
        workflowEditorRestServiceClient.createWorkflow(workflowName, workflow);
        
        String stepNameBeforeUpdating = "To modify not unique";
        String stepNameAfterUpdating = "draft";
        String previousStepName = stepNameBeforeUpdating;
        String expectedMessageException;
        
        
        try
        {
            // Create a state
            PSUiWorkflow createdWorkflow = createState(workflowName, stepNameBeforeUpdating, "Draft");
            PSUiWorkflow modifiedWorkflow = workflowEditorRestServiceClient.createStep(workflowName, stepNameBeforeUpdating, createdWorkflow);

            // Modify the state
            Iterator<PSUiWorkflowStep> itr = modifiedWorkflow.getWorkflowSteps().iterator(); 
            PSUiWorkflowStep state = modifiedWorkflow.getWorkflowSteps().get(0);
            while(itr.hasNext()) {
                state = itr.next();
                if (!state.getStepName().equalsIgnoreCase(stepNameBeforeUpdating))
                {
                    itr.remove();
                }
            }
            // Rename the state
            modifiedWorkflow.getWorkflowSteps().get(0).setStepName(stepNameAfterUpdating);
            modifiedWorkflow.setPreviousStepName(stepNameBeforeUpdating);
            
            expectedMessageException = MessageFormat.format(
                    STATE_NAME_UPDATE_IS_NOT_UNIQUE, stepNameBeforeUpdating, stepNameAfterUpdating, "Draft");
            
            testUpdateStateWrongStateName(workflowName, previousStepName, modifiedWorkflow, expectedMessageException);
        }
        finally
        {
            workflowEditorRestServiceClient.deleteWorkflow(workflowName);
        }

    }
    
    @Test
    public void testUpdateState_SystemStateName()
    {
        String workflowName = "testUpdateStateSystemState";
        PSUiWorkflow workflow = new PSUiWorkflow();
        workflow.setWorkflowName(workflowName);
        workflow.setDefaultWorkflow(false);
        workflowEditorRestServiceClient.createWorkflow(workflowName, workflow);
        
        String stepNameBeforeUpdating = "To modify system";
        String stepNameAfterUpdating = "Live";
        String previousStepName = stepNameBeforeUpdating;
        String expectedMessageException;
        
        try
        {
            // Create a state
            PSUiWorkflow createdWorkflow = createState(workflowName, stepNameBeforeUpdating, "Draft");
            PSUiWorkflow modifiedWorkflow = workflowEditorRestServiceClient.createStep(workflowName, stepNameBeforeUpdating, createdWorkflow);

            // Modify the state
            Iterator<PSUiWorkflowStep> itr = modifiedWorkflow.getWorkflowSteps().iterator(); 
            PSUiWorkflowStep state = modifiedWorkflow.getWorkflowSteps().get(0);
            while(itr.hasNext()) {
                state = itr.next();
                if (!state.getStepName().equalsIgnoreCase(stepNameBeforeUpdating))
                {
                    itr.remove();
                }
            }
            // Rename the state
            modifiedWorkflow.getWorkflowSteps().get(0).setStepName(stepNameAfterUpdating);
            modifiedWorkflow.setPreviousStepName(stepNameBeforeUpdating);
            
            expectedMessageException = MessageFormat.format(
                    STATE_NAME_UPDATE_IS_A_SYSTEM_STATE, stepNameBeforeUpdating, "Live");
            
            testUpdateStateWrongStateName(workflowName, previousStepName, modifiedWorkflow, expectedMessageException);
        }
        finally
        {
            workflowEditorRestServiceClient.deleteWorkflow(workflowName);
        }
    }
    
    @Test
    public void testUpdateState_OnlyName()
    {
        String workflowName = "testUpdateStateName";
        PSUiWorkflow workflow = new PSUiWorkflow();
        workflow.setWorkflowName(workflowName);
        workflow.setDefaultWorkflow(false);
        workflowEditorRestServiceClient.createWorkflow(workflowName, workflow);
        
        String stepNameAfterUpdating;
        try
        {
            String stepNameBeforeUpdating = "To modify";
            stepNameAfterUpdating = "Modified";
            // Create a state
            PSUiWorkflow createdWorkflow = createState(workflowName, stepNameBeforeUpdating, "Draft");
            PSUiWorkflow modifiedWorkflow = workflowEditorRestServiceClient.createStep(workflowName, stepNameBeforeUpdating, createdWorkflow);

            // Modify the state
            Iterator<PSUiWorkflowStep> itr = modifiedWorkflow.getWorkflowSteps().iterator(); 
            PSUiWorkflowStep state = modifiedWorkflow.getWorkflowSteps().get(0);
            while(itr.hasNext()) {
                state = itr.next();
                if (!state.getStepName().equalsIgnoreCase(stepNameBeforeUpdating))
                {
                    itr.remove();
                }
            }
            // Rename the state
            modifiedWorkflow.getWorkflowSteps().get(0).setStepName(stepNameAfterUpdating);
            modifiedWorkflow.setPreviousStepName(stepNameBeforeUpdating);
            
            // Call the service
            PSUiWorkflow returnedWorkflow = workflowEditorRestServiceClient.updateStep(workflowName, stepNameBeforeUpdating, modifiedWorkflow);
            
            PSUiWorkflow expectedWorkflow = new PSUiWorkflow();
            expectedWorkflow.setWorkflowName(workflowName);
            List<PSUiWorkflowStep> steps = new ArrayList<PSUiWorkflowStep>();
            PSUiWorkflowStep draftStep = new PSUiWorkflowStep();
            draftStep.setStepName("Draft");
            steps.add(draftStep);
            steps.add(modifiedWorkflow.getWorkflowSteps().get(0));
            PSUiWorkflowStep reviewStep = new PSUiWorkflowStep();
            reviewStep.setStepName("Review");
            steps.add(reviewStep);
            PSUiWorkflowStep approvedStep = new PSUiWorkflowStep();
            approvedStep.setStepName("Approved");
            steps.add(approvedStep);
            PSUiWorkflowStep archieveStep = new PSUiWorkflowStep();
            archieveStep.setStepName("Archive");
            steps.add(archieveStep);
            expectedWorkflow.setWorkflowSteps(steps);
            
            // Asserts
            assertEquals(expectedWorkflow.getWorkflowName(), returnedWorkflow.getWorkflowName());
            assertTrue(returnedWorkflow.getWorkflowSteps().get(0).getStepName().equalsIgnoreCase(
                    expectedWorkflow.getWorkflowSteps().get(0).getStepName()));
            // The modified state
            assertEquals(returnedWorkflow.getWorkflowSteps().get(1), expectedWorkflow.getWorkflowSteps().get(1));
            assertTrue(returnedWorkflow.getWorkflowSteps().get(2).getStepName().equalsIgnoreCase(
                    expectedWorkflow.getWorkflowSteps().get(2).getStepName()));
            assertTrue(returnedWorkflow.getWorkflowSteps().get(3).getStepName().equalsIgnoreCase(
                    expectedWorkflow.getWorkflowSteps().get(3).getStepName()));
            assertTrue(returnedWorkflow.getWorkflowSteps().get(4).getStepName().equalsIgnoreCase(
                    expectedWorkflow.getWorkflowSteps().get(4).getStepName()));
            
            
            // Deleted the created and modified state
            workflowEditorRestServiceClient.deleteState(workflowName, stepNameAfterUpdating);
        }
        finally
        {
            workflowEditorRestServiceClient.deleteWorkflow(workflowName);
        }
    }
    
    @Test
    public void testUpdateState_OnlyRoles()
    {
        String workflowName = "testUpdateStateRoles";
        PSUiWorkflow workflow = new PSUiWorkflow();
        workflow.setWorkflowName(workflowName);
        workflow.setDefaultWorkflow(false);
        workflowEditorRestServiceClient.createWorkflow(workflowName, workflow);
        
        String stepName= "To modify";
        // Create a state
        PSUiWorkflow createdWorkflow = createState(workflowName, stepName, "Draft");
        PSUiWorkflow modifiedWorkflow = workflowEditorRestServiceClient.createStep(workflowName, stepName, createdWorkflow);

        try
        {
            // Modify the state
            Iterator<PSUiWorkflowStep> itr = modifiedWorkflow.getWorkflowSteps().iterator(); 
            PSUiWorkflowStep state = modifiedWorkflow.getWorkflowSteps().get(0);
            while(itr.hasNext()) {
                state = itr.next();
                if (!state.getStepName().equalsIgnoreCase(stepName))
                {
                    itr.remove();
                }
            }
            // Add some roles
            List<PSUiWorkflowStep> modifiedSteps = modifiedWorkflow.getWorkflowSteps();

            List<PSUiWorkflowStepRole> modifiedStepRoles = modifiedSteps.get(0).getStepRoles();
            PSUiWorkflowStepRoleTransition submitTransition = new PSUiWorkflowStepRoleTransition();
            submitTransition.setTransitionPermission("Submit");

            PSUiWorkflowStepRole editorRole = new PSUiWorkflowStepRole("Editor", 3);
            editorRole.setEnableNotification(true);
            List<PSUiWorkflowStepRoleTransition> editorTransitions = new ArrayList<PSUiWorkflowStepRoleTransition>();
            editorTransitions.add(submitTransition);
            editorRole.setRoleTransitions(editorTransitions);
            modifiedStepRoles.add(editorRole);

            modifiedWorkflow.getWorkflowSteps().get(0).setStepRoles(modifiedStepRoles);
            modifiedSteps.add(modifiedWorkflow.getWorkflowSteps().get(0));
            modifiedWorkflow.setPreviousStepName(stepName);
            
            // Call the service
            PSUiWorkflow returnedWorkflow = workflowEditorRestServiceClient.updateStep(workflowName, stepName, modifiedWorkflow);
            
            PSUiWorkflow expectedWorkflow = new PSUiWorkflow();
            expectedWorkflow.setWorkflowName(workflowName);
            List<PSUiWorkflowStep> steps = new ArrayList<PSUiWorkflowStep>();
            PSUiWorkflowStep draftStep = new PSUiWorkflowStep();
            draftStep.setStepName("Draft");
            steps.add(draftStep);
            steps.add(modifiedWorkflow.getWorkflowSteps().get(0));
            PSUiWorkflowStep reviewStep = new PSUiWorkflowStep();
            reviewStep.setStepName("Review");
            steps.add(reviewStep);
            PSUiWorkflowStep approvedStep = new PSUiWorkflowStep();
            approvedStep.setStepName("Approved");
            steps.add(approvedStep);
            PSUiWorkflowStep archieveStep = new PSUiWorkflowStep();
            archieveStep.setStepName("Archive");
            steps.add(archieveStep);
            expectedWorkflow.setWorkflowSteps(steps);
            
            // Get the editor role
            int indexEditorRole = 0;
            for (PSUiWorkflowStepRole stepRole : returnedWorkflow.getWorkflowSteps().get(1).getStepRoles())
            {
                if (stepRole.getRoleName().equals("Editor"))
                {
                    break;
                }
                indexEditorRole = indexEditorRole + 1;
            }
            
            PSUiWorkflowStepRole editorRoleAfterSaving = returnedWorkflow.getWorkflowSteps().get(1).getStepRoles().get(indexEditorRole);
            
            // Asserts
            assertEquals(expectedWorkflow.getWorkflowName(), returnedWorkflow.getWorkflowName());
            assertTrue(returnedWorkflow.getWorkflowSteps().get(0).getStepName().equalsIgnoreCase(
                    expectedWorkflow.getWorkflowSteps().get(0).getStepName()));
            // The modified state
            assertTrue(returnedWorkflow.getWorkflowSteps().get(1).getStepName().equalsIgnoreCase(
                    expectedWorkflow.getWorkflowSteps().get(1).getStepName()));
            assertTrue(returnedWorkflow.getWorkflowSteps().get(2).getStepName().equalsIgnoreCase(
                    expectedWorkflow.getWorkflowSteps().get(2).getStepName()));
            assertTrue(returnedWorkflow.getWorkflowSteps().get(3).getStepName().equalsIgnoreCase(
                    expectedWorkflow.getWorkflowSteps().get(3).getStepName()));
            assertTrue(returnedWorkflow.getWorkflowSteps().get(4).getStepName().equalsIgnoreCase(
                    expectedWorkflow.getWorkflowSteps().get(4).getStepName()));
            
            //Assert to check if the notify is on for the Editor role in the step "To modify"
            assertTrue(editorRoleAfterSaving.isEnableNotification() == editorRole.isEnableNotification());
            // Turned notification on for the Editor role
            assertTrue(editorRoleAfterSaving.isEnableNotification());
            
            // Deleted the created and modified state
            workflowEditorRestServiceClient.deleteState(workflowName, stepName);
        }
        finally
        {
            workflowEditorRestServiceClient.deleteWorkflow(workflowName);
        }
    } 
    
    @Test
    public void testUpdateState()
    {
        
        String workflowName = "testUpdateState";
        PSUiWorkflow workflow = new PSUiWorkflow();
        workflow.setWorkflowName(workflowName);
        workflow.setDefaultWorkflow(false);
        workflowEditorRestServiceClient.createWorkflow(workflowName, workflow);
        
        String stepNameBeforeUpdating= "To modify";
        String stepNameAfterUpdating = "Modified";
    
        try
        {
            // Create a state
            PSUiWorkflow createdWorkflow = createState(workflowName, stepNameBeforeUpdating, "Draft");
            PSUiWorkflow modifiedWorkflow = workflowEditorRestServiceClient.createStep(workflowName, stepNameBeforeUpdating, createdWorkflow);

            // Modify the state
            Iterator<PSUiWorkflowStep> itr = modifiedWorkflow.getWorkflowSteps().iterator(); 
            PSUiWorkflowStep state = modifiedWorkflow.getWorkflowSteps().get(0);
            while(itr.hasNext()) {
                state = itr.next();
                if (!state.getStepName().equalsIgnoreCase(stepNameBeforeUpdating))
                {
                    itr.remove();
                }
            }
            // Rename the state
            modifiedWorkflow.getWorkflowSteps().get(0).setStepName(stepNameAfterUpdating);
            // Add some roles
            List<PSUiWorkflowStep> modifiedSteps = modifiedWorkflow.getWorkflowSteps();

            List<PSUiWorkflowStepRole> modifiedStepRoles = modifiedSteps.get(0).getStepRoles();
            PSUiWorkflowStepRoleTransition submitTransition = new PSUiWorkflowStepRoleTransition();
            submitTransition.setTransitionPermission("Submit");

            PSUiWorkflowStepRole editorRole = new PSUiWorkflowStepRole("Editor", 3);
            editorRole.setEnableNotification(false);
            
            List<PSUiWorkflowStepRoleTransition> editorTransitions = new ArrayList<PSUiWorkflowStepRoleTransition>();
            editorTransitions.add(submitTransition);
            editorRole.setRoleTransitions(editorTransitions);
            modifiedStepRoles.add(editorRole);

            modifiedWorkflow.getWorkflowSteps().get(0).setStepRoles(modifiedStepRoles);
            modifiedSteps.add(modifiedWorkflow.getWorkflowSteps().get(0));
            modifiedWorkflow.setPreviousStepName(stepNameBeforeUpdating);
            
            // Call the service
            PSUiWorkflow returnedWorkflow = workflowEditorRestServiceClient.updateStep(workflowName, stepNameBeforeUpdating, modifiedWorkflow);
            
            PSUiWorkflow expectedWorkflow = new PSUiWorkflow();
            expectedWorkflow.setWorkflowName(workflowName);
            List<PSUiWorkflowStep> steps = new ArrayList<PSUiWorkflowStep>();
            PSUiWorkflowStep draftStep = new PSUiWorkflowStep();
            draftStep.setStepName("Draft");
            steps.add(draftStep);
            steps.add(modifiedWorkflow.getWorkflowSteps().get(0));
            PSUiWorkflowStep reviewStep = new PSUiWorkflowStep();
            reviewStep.setStepName("Review");
            steps.add(reviewStep);
            PSUiWorkflowStep approvedStep = new PSUiWorkflowStep();
            approvedStep.setStepName("Approved");
            steps.add(approvedStep);
            PSUiWorkflowStep archieveStep = new PSUiWorkflowStep();
            archieveStep.setStepName("Archive");
            steps.add(archieveStep);
            expectedWorkflow.setWorkflowSteps(steps);
            
            // Get the editor role
            int indexEditorRole = 0;
            for (PSUiWorkflowStepRole stepRole : returnedWorkflow.getWorkflowSteps().get(1).getStepRoles())
            {
                if (stepRole.getRoleName().equals("Editor"))
                {
                    break;
                }
                indexEditorRole = indexEditorRole + 1;
            }
            PSUiWorkflowStepRole editorRoleAfterSaving = returnedWorkflow.getWorkflowSteps().get(1).getStepRoles().get(indexEditorRole);
            
            // Asserts
            assertEquals(expectedWorkflow.getWorkflowName(), returnedWorkflow.getWorkflowName());
            assertTrue(returnedWorkflow.getWorkflowSteps().get(0).getStepName().equalsIgnoreCase(
                    expectedWorkflow.getWorkflowSteps().get(0).getStepName()));
            // The modified state
            assertTrue(returnedWorkflow.getWorkflowSteps().get(1).getStepName().equalsIgnoreCase(
                    expectedWorkflow.getWorkflowSteps().get(1).getStepName()));
            assertTrue(returnedWorkflow.getWorkflowSteps().get(2).getStepName().equalsIgnoreCase(
                    expectedWorkflow.getWorkflowSteps().get(2).getStepName()));
            assertTrue(returnedWorkflow.getWorkflowSteps().get(3).getStepName().equalsIgnoreCase(
                    expectedWorkflow.getWorkflowSteps().get(3).getStepName()));
            assertTrue(returnedWorkflow.getWorkflowSteps().get(4).getStepName().equalsIgnoreCase(
                    expectedWorkflow.getWorkflowSteps().get(4).getStepName()));
            
            //Assert to check if the notify is on for the Editor role in the step "To modify"
            assertTrue(editorRoleAfterSaving.isEnableNotification() == editorRole.isEnableNotification());
            // Turned notification off for the Editor role
            assertTrue(!editorRoleAfterSaving.isEnableNotification());

            // Deleted the created and modified state
            workflowEditorRestServiceClient.deleteState(workflowName, stepNameAfterUpdating);
        }
        finally
        {
            workflowEditorRestServiceClient.deleteWorkflow(workflowName);
        }
    }
    
    @Test
    public void testUpdateLockedState()
    {
        String workflowName = "testUpdateLockedState";
        PSUiWorkflow workflow = new PSUiWorkflow();
        workflow.setWorkflowName(workflowName);
        workflow.setDefaultWorkflow(false);
        workflowEditorRestServiceClient.createWorkflow(workflowName, workflow);
        
        String stepName= "Review";
        PSUiWorkflow createdWorkflow = workflowEditorRestServiceClient.getWorkflow(workflowName);
        
        try
        {
            String[] expectedPermissionNames = {"Reject", "Approve", "Publish"};
            Set<String> reviewPermissionNames = new HashSet<String>(Arrays.asList(expectedPermissionNames));
                
            List<PSUiWorkflowStep> workflowSteps = new ArrayList<PSUiWorkflowStep>();
            PSUiWorkflowStep workflowStep = new PSUiWorkflowStep();
            
            workflow.setWorkflowName(workflowName);
            workflow.setPreviousStepName(stepName);
            
            // Add the Review step from the Default Workflow to the new one to be updated
            for (PSUiWorkflowStep defaultWorkflowStep : createdWorkflow.getWorkflowSteps())
            {
                if (defaultWorkflowStep.getStepName().equalsIgnoreCase(stepName))
                {
                    workflowStep = defaultWorkflowStep;
                    break;
                }
            }
            
            // Add Approve, Reject and Notify permissions to Contributor role
            PSUiWorkflowStepRoleTransition workflowStepRoleTransitionApprove = 
                    new PSUiWorkflowStepRoleTransition();
            workflowStepRoleTransitionApprove.setTransitionPermission("Approve");
            
            PSUiWorkflowStepRoleTransition workflowStepRoleTransitionPublish = 
                new PSUiWorkflowStepRoleTransition();
            workflowStepRoleTransitionPublish.setTransitionPermission("Publish");
   
            PSUiWorkflowStepRoleTransition workflowStepRoleTransitionReject = 
                    new PSUiWorkflowStepRoleTransition();
            workflowStepRoleTransitionReject.setTransitionPermission("Reject");

            PSUiWorkflowStepRole workflowStepRoleContributor = new PSUiWorkflowStepRole("Contributor", 2);
            workflowStepRoleContributor.setEnableNotification(true);
            List<PSUiWorkflowStepRoleTransition> workflowStepContributorTransitions = 
                    new ArrayList<PSUiWorkflowStepRoleTransition>();
            workflowStepContributorTransitions.add(workflowStepRoleTransitionReject);
            workflowStepContributorTransitions.add(workflowStepRoleTransitionPublish);
            workflowStepContributorTransitions.add(workflowStepRoleTransitionApprove);
            workflowStepRoleContributor.setRoleTransitions(workflowStepContributorTransitions);
            workflowStep.getStepRoles().add(1, workflowStepRoleContributor);

            workflowSteps.add(workflowStep);
            workflow.setWorkflowSteps(workflowSteps);
            
            // Call the service
            PSUiWorkflow returnedWorkflow = workflowEditorRestServiceClient.updateStep(workflowName, stepName, workflow);
            
            // Compare the updated step
            PSUiWorkflowStep updatedReviewState = null;
            
            for(PSUiWorkflowStep step : returnedWorkflow.getWorkflowSteps())
            {
                if(step.getStepName().equals(stepName))
                {
                    updatedReviewState = step;
                    break;
                }
            }
            
            //Check if the permission names for the Review state are the expected values
            assertNotNull(updatedReviewState);
            for(String permissionName : updatedReviewState.getPermissionNames())
            {
                assertTrue(reviewPermissionNames.contains(permissionName));
            }
            
            //Check if the Contributor role is assigned to each transition
            PSUiWorkflowStepRole contributorRole = null;
            for(PSUiWorkflowStepRole stepRole: updatedReviewState.getStepRoles())
            {
                if(stepRole.getRoleName().equals("Contributor"))
                {
                    contributorRole = stepRole;
                }
            }
            
            for(PSUiWorkflowStepRoleTransition stepRoleTransition : contributorRole.getRoleTransitions())
            {
                assertTrue(reviewPermissionNames.contains(stepRoleTransition.getTransitionPermission()));
            }
                    
            // Clean the updated step
            workflow.getWorkflowSteps().get(0).getStepRoles().remove(workflowStepRoleContributor);
            
            // Call the service
            returnedWorkflow = workflowEditorRestServiceClient.updateStep(workflowName, stepName, workflow);
            
            // Compare the restored step
            PSUiWorkflowStep returnedWorkflowStep = returnedWorkflow.getWorkflowSteps().get(1);
            assertEquals(returnedWorkflowStep.getStepName(), stepName);
            assertTrue(!returnedWorkflowStep.getStepRoles().contains(workflowStepRoleContributor));
        }
        finally
        {
            workflowEditorRestServiceClient.deleteWorkflow(workflowName);
        }
    }
    
    /**
     * Builds the workflowNames -> PSEnumVals map.
     */
    private void buildStatesMap()
    {
        mapWorkflowStates = new HashMap<String, PSEnumVals>();

        // Default workflow
        mapWorkflowStates.put("Default Workflow", getDefaultWorkflowChoices());
    }

    /**
     * Creates a mockup PSEnumVals to contain the Default workflow choices.
     */
    private PSEnumVals getDefaultWorkflowChoices()
    {
        PSEnumVals defaultChoices = new PSEnumVals();

        defaultChoices.addEntry("Draft", null);
        defaultChoices.addEntry("Review", null);
        defaultChoices.addEntry("Pending", null);
        defaultChoices.addEntry("Live", null);
        defaultChoices.addEntry("Quick Edit", null);
        defaultChoices.addEntry("Archive", null);

        return defaultChoices;
    }

    /**
     * Comes up with a name that is not a workflow name.
     * 
     * @return a String never <code>null</code>
     */
    private String getNotExistingWorkflowName()
    {
        String name = "WorkflowThatWontExists";

        // this will make sure that the name doesn't exists
        assertNotNull(mapWorkflowStates);
        assertTrue(!mapWorkflowStates.keySet().contains(name));

        return name;
    }

    /**
     * Helper method that creates a state in the supplied workflow object, does not save the workflow
     * 
     * @param workflow the name of the workflow, assumed not empty or <code>null<code>
     * @param state the name of the new state, assumed not empty or <code>null<code>
     * @param previousState the name of the previous state, assumed not empty or <code>null<code>
     * @return The modified workflow
     */
    private PSUiWorkflow createState(String workflow, String state, String previousState)
    {
        PSUiWorkflow uiWorkflow = new PSUiWorkflow();
        List<PSUiWorkflowStep> workflowSteps = new ArrayList<PSUiWorkflowStep>();
        PSUiWorkflowStep workflowStep = new PSUiWorkflowStep();
        
        String workflowName = workflow;
        String newStateName = state;
        String previousStateName = previousState;
        
        uiWorkflow.setWorkflowName(workflowName);
        uiWorkflow.setPreviousStepName(previousStateName);
        
        workflowStep.setStepName(newStateName);
        
        List<PSUiWorkflowStepRole> workflowStepRoles = new ArrayList<PSUiWorkflowStepRole>();
        PSUiWorkflowStepRoleTransition workflowStepRoleTransitionApprove = 
                new PSUiWorkflowStepRoleTransition();
        workflowStepRoleTransitionApprove.setTransitionPermission("Approve");
        PSUiWorkflowStepRoleTransition workflowStepRoleTransitionReject = 
                new PSUiWorkflowStepRoleTransition();
        workflowStepRoleTransitionReject.setTransitionPermission("Reject");
        PSUiWorkflowStepRoleTransition workflowStepRoleTransitionSubmit = 
                new PSUiWorkflowStepRoleTransition();
        workflowStepRoleTransitionSubmit.setTransitionPermission("Submit");

        PSUiWorkflowStepRole workflowStepRoleAdmin = new PSUiWorkflowStepRole("Admin", 1);
        List<PSUiWorkflowStepRoleTransition> workflowStepAdminTransitions = 
                new ArrayList<PSUiWorkflowStepRoleTransition>();
        workflowStepAdminTransitions.add(workflowStepRoleTransitionApprove);
        workflowStepAdminTransitions.add(workflowStepRoleTransitionSubmit);
        workflowStepAdminTransitions.add(workflowStepRoleTransitionReject);
        workflowStepRoleAdmin.setRoleTransitions(workflowStepAdminTransitions);
        workflowStepRoles.add(workflowStepRoleAdmin);
        
        workflowStep.setStepRoles(workflowStepRoles);
        workflowSteps.add(workflowStep);
        
        workflowSteps = new ArrayList<PSUiWorkflowStep>();
        workflowSteps.add(workflowStep);
        
        uiWorkflow.setWorkflowSteps(workflowSteps);
        
        return uiWorkflow;
    }
    
    /**
     * Restore the workflow named "Default Workflow" as the default
     */
    private void restoreDefaultWorkflow()
    {
        // Restore the default workflow
        String defaultWorkflowName = "Default Workflow";
        PSUiWorkflow defaultWorkflow = new PSUiWorkflow();
        defaultWorkflow.setWorkflowName(defaultWorkflowName);
        defaultWorkflow.setPreviousWorkflowName(defaultWorkflowName);
        defaultWorkflow.setDefaultWorkflow(true);
        restClient.updateWorkflow(defaultWorkflowName, defaultWorkflow);
    }
}
