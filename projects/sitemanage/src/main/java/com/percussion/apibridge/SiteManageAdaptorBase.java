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

package com.percussion.apibridge;

import com.percussion.itemmanagement.data.PSItemStateTransition;
import com.percussion.itemmanagement.service.IPSItemWorkflowService;
import com.percussion.rest.errors.BackendException;
import com.percussion.rest.errors.NotAuthorizedException;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.workflow.IPSWorkflowService;
import com.percussion.services.workflow.PSWorkflowServiceLocator;
import com.percussion.services.workflow.data.PSWorkflow;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.user.service.IPSUserService;
import com.percussion.utils.guid.IPSGuid;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class SiteManageAdaptorBase
{
   
	protected final IPSUserService userService;
 
	protected final IPSItemWorkflowService itemWorkflowService;
	
    @Autowired
	public SiteManageAdaptorBase(IPSUserService userService, IPSItemWorkflowService itemWorkflowService)
	{
		this.userService = userService;
		this.itemWorkflowService = itemWorkflowService;
	}
	
    protected void checkAPIPermission() throws BackendException {
        try {
            if (!userService.isAdminUser(userService.getCurrentUser().getName())) {
                throw new NotAuthorizedException();
            }
        } catch (PSDataServiceException e) {
            throw new BackendException(e.getMessage(),e);
        }
    }
    
    protected PSWorkflow loadWorkflow(int workflowId)
    {
        IPSGuid id = new PSGuid(PSTypeEnum.WORKFLOW, workflowId);
        IPSWorkflowService srv = PSWorkflowServiceLocator.getWorkflowService();
        return srv.loadWorkflow(id);
    }
    
    protected String setWorkflowState(String itemId, String requestedWorkflowState, List<String> stateChanges)
    {
        PSItemStateTransition transitions = itemWorkflowService.getTransitions(itemId);

        String currentState = transitions.getStateName();
        String testCurrentState = currentState;

        if (APPROVED_STATES.contains(testCurrentState)) {
			testCurrentState = "Live";
		}

        String testWorkflowState = requestedWorkflowState;

        if (APPROVED_STATES.contains(testWorkflowState)) {
			testWorkflowState = "Live";
		}

        if (stateChanges.contains(currentState)) {
			throw new RuntimeException("Loop detected trying to get item into state " + testWorkflowState
					+ ". Tried changing through following states " + stateChanges);
		}

        stateChanges.add(currentState);

        String requiredTrigger = null;
        if (!testWorkflowState.equals(testCurrentState))
        {
            List<String> availableTriggers = transitions.getTransitionTriggers();
            String[] testTriggers = TRANSITION_MAP.get(testWorkflowState);
            if (testTriggers == null) {
				// Default trigger list for custom states
				testTriggers = TRANSITION_MAP.get(null);
			}

            for (String trigger : testTriggers)
            {
                if (availableTriggers.contains(trigger))
                {
                    requiredTrigger = trigger;
                    break;
                }
            }
            if (requiredTrigger == null)
            {
                throw new RuntimeException("Cannot find trigger to get to " + testWorkflowState + " tried "
                        + Arrays.toString(testTriggers) + " Transitioned through : " + stateChanges
                        + " available transitions " + availableTriggers);
            }
            itemWorkflowService.transition(itemId, requiredTrigger);
            // recurse to check if we are now in the correct state.
            return setWorkflowState(itemId, requestedWorkflowState, stateChanges);
        }

        return currentState;
    }
    
    protected static HashMap<String, String[]> TRANSITION_MAP = new HashMap<>();
    static
    {
        // Will get to live through approve if available, or through submit steps if not.
        TRANSITION_MAP.put(DefaultWorkflowStates.live, new String[]
        		{IPSItemWorkflowService.TRANSITION_TRIGGER_APPROVE, IPSItemWorkflowService.TRANSITION_TRIGGER_SUBMIT});
        
        // Will get to Quick Edit through Submit and approve
        TRANSITION_MAP.put(DefaultWorkflowStates.quickEdit, new String[]
        		{IPSItemWorkflowService.TRANSITION_TRIGGER_EDIT, IPSItemWorkflowService.TRANSITION_TRIGGER_APPROVE, 
        			IPSItemWorkflowService.TRANSITION_TRIGGER_SUBMIT});
        
        // resubmit back to draft if already approved, or will reject back if not
        TRANSITION_MAP.put(DefaultWorkflowStates.draft, new String[]
        		{IPSItemWorkflowService.TRANSITION_TRIGGER_RESUBMIT, IPSItemWorkflowService.TRANSITION_TRIGGER_REJECT});
        
        // Will get to archive through Approve and Quick Edit.
        TRANSITION_MAP.put(DefaultWorkflowStates.archive, new String[]
        		{IPSItemWorkflowService.TRANSITION_TRIGGER_ARCHIVE, IPSItemWorkflowService.TRANSITION_TRIGGER_EDIT,
                	IPSItemWorkflowService.TRANSITION_TRIGGER_APPROVE, IPSItemWorkflowService.TRANSITION_TRIGGER_SUBMIT});

        // Will submit through custom transitions if any other transition requested, 
        // if Approve is reached it will resubmit back to Draft to get to possible previous state. 
        // May not be quickest path, reject may be quicker. Loop check will throw error if state cannot be found
        TRANSITION_MAP.put(DefaultWorkflowStates.review, new String[]
        		{IPSItemWorkflowService.TRANSITION_TRIGGER_RESUBMIT, IPSItemWorkflowService.TRANSITION_TRIGGER_SUBMIT,
                	IPSItemWorkflowService.TRANSITION_TRIGGER_APPROVE});
        
        // Any custom states are handled the same as Review
        TRANSITION_MAP.put(null, new String[]
        		{IPSItemWorkflowService.TRANSITION_TRIGGER_RESUBMIT, IPSItemWorkflowService.TRANSITION_TRIGGER_SUBMIT,
                	IPSItemWorkflowService.TRANSITION_TRIGGER_APPROVE});
    }

	protected static final ArrayList<String> APPROVED_STATES = new ArrayList<>(Arrays.asList(new String[] { DefaultWorkflowStates.live, DefaultWorkflowStates.pending }));
}
