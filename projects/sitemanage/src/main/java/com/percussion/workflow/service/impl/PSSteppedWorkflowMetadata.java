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
        new HashSet<String>(Arrays.asList(excludedWorkflowsArray));

    // Steps names to exclude from the list to show
    static String[] excludedStatesArray =
        {"Pending", "Live", "Quick Edit"};
    static final Set<String> excludedStates = 
        new HashSet<String>(Arrays.asList(excludedStatesArray));
    
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
        {TRANSITION_NAME_SUBMIT, TRANSITION_NAME_REJECT, TRANSITION_NAME_APPROVE, TRANSITION_NAME_PUBLISH};
    static List<String> defaultTransitions = 
        new ArrayList<String>(Arrays.asList(defaultTransitionsValues));
    
    //Ordered list of transitions names
    static String[] orderedTransitionsValues =
        {TRANSITION_NAME_SUBMIT, TRANSITION_NAME_RESUBMIT, TRANSITION_NAME_REJECT, TRANSITION_NAME_APPROVE, 
        TRANSITION_NAME_PUBLISH, TRANSITION_NAME_ARCHIVE};
    
    static List<String> orderedTransitions = 
        new ArrayList<String>(Arrays.asList(orderedTransitionsValues));
    
    // Steps names that are locked down to the system
    static String[] systemStatesArray =
        {DRAFT_STATE, QUICK_EDIT_STATE, REVIEW_STATE, PENDING_STATE, LIVE_STATE, ARCHIVE_STATE};
    static final List<String> systemStatesList = Arrays.asList(systemStatesArray);
}
