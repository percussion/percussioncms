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
package com.percussion.workflow.service.impl;

import com.percussion.util.PSSiteManageBean;
import com.percussion.workflow.service.IPSSteppedWorkflowMetadata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * @author JaySeletz
 *
 */
@PSSiteManageBean("steppedWorkflowMetadata")
@Lazy
public class PSSteppedWorkflowMetadata implements IPSSteppedWorkflowMetadata
{


    @Override
    public List<String> getSystemStatesList()
    {
        return systemStatesList;
    }


   
    // Workflow names to exclude from the list to show
    static String[] excludedWorkflowsArray =
        {"LocalContent", "Standard Workflow", "Simple Workflow"};
    static final Set<String> excludedWorkflows = 
        new HashSet<>(Arrays.asList(excludedWorkflowsArray));

    // Steps names to exclude from the list to show
    static String[] excludedStatesArray =
        {"Pending", "Live", "Quick Edit"};
    static final Set<String> excludedStates = 
        new HashSet<>(Arrays.asList(excludedStatesArray));
    
    // Constant for Draft step
    static final String DRAFT_STATE = "Draft";
    
    // Constant for Review step
    static final String REVIEW_STATE = "Review";
    
    // Constant for Quick Edit step
    static final String QUICK_EDIT_STATE = "Quick Edit";
    
    // Constant for Pending step
    static final String PENDING_STATE = "Pending";
    
    // Constant for Live step
    static final String LIVE_STATE = "Live";
    
    // Constant for Archive step
    static final String ARCHIVE_STATE = "Archive";
    
    // Constant for Edit transition
    static final String TRANSITION_NAME_EDIT = "Edit";
    
    // Constant for Live transition
    static final String TRANSITION_NAME_LIVE = "Live";
    
    // Constant for Remove transition
    static final String TRANSITION_NAME_REMOVE = "Remove";
    
    // Constant for Archive transition
    static final String TRANSITION_NAME_ARCHIVE = "Archive";
    
    // Constant for Resubmit transition
    static final String TRANSITION_NAME_RESUBMIT = "Resubmit";

    // List of default step transitions
    static final String TRANSITION_NAME_APPROVE = "Approve";
    static final String TRANSITION_NAME_REJECT = "Reject";
    static final String TRANSITION_NAME_SUBMIT = "Submit";
    static final String TRANSITION_NAME_PUBLISH = "Publish";
    static String[] defaultTransitionsValues =
        {TRANSITION_NAME_SUBMIT, TRANSITION_NAME_REJECT, TRANSITION_NAME_APPROVE, TRANSITION_NAME_PUBLISH, TRANSITION_NAME_ARCHIVE};
    static List<String> defaultTransitions = 
        new ArrayList<>(Arrays.asList(defaultTransitionsValues));
    
    //Ordered list of transitions names
    static String[] orderedTransitionsValues =
        {TRANSITION_NAME_SUBMIT, TRANSITION_NAME_RESUBMIT, TRANSITION_NAME_REJECT, TRANSITION_NAME_APPROVE, 
        TRANSITION_NAME_PUBLISH, TRANSITION_NAME_ARCHIVE};
    
    static List<String> orderedTransitions = 
        new ArrayList<>(Arrays.asList(orderedTransitionsValues));
    
    // Steps names that are locked down to the system
    static String[] systemStatesArray =
        {DRAFT_STATE, QUICK_EDIT_STATE, REVIEW_STATE, PENDING_STATE, LIVE_STATE, ARCHIVE_STATE};
    static final List<String> systemStatesList = Arrays.asList(systemStatesArray);
}
