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
package com.percussion.itemmanagement.service;

import com.percussion.itemmanagement.data.PSApprovableItems;
import com.percussion.itemmanagement.data.PSBulkApprovalJobStatus;
import com.percussion.itemmanagement.data.PSItemStateTransition;
import com.percussion.itemmanagement.data.PSItemTransitionResults;
import com.percussion.itemmanagement.data.PSItemUserInfo;
import com.percussion.share.data.PSNoContent;

import java.util.Set;

/**
 * This interface handles all workflow operations, i.e., check-in, check-out, for content items.
 * 
 * @author peterfrontiero
 *
 */
public interface IPSItemWorkflowService
{
    /**
     * Performs a check-in of the item identified by the specified id.  All local content items associated with the item
     * will also be checked in.  A forced check-in will be performed if the item is currently checked out by a
     * different user and the current user has admin privileges.  If the item does not exist, the method silently
     * returns successfully.
     * 
     * @param id never blank.
     * 
     * @return a no-content response as a workaround for an issue in jQuery/ajax, 
     * which always expecting something in the response; otherwise it flags 
     * JavaScript error for the jQuery/ajax operation. 
     * 
     * @throws PSItemWorkflowServiceException if an error occurs.
     */
    public PSNoContent checkIn(String id) throws PSItemWorkflowServiceException;
    
    
    /**
     * Checks in the content, if ignoreRevisionCheck is <code>true</code> the underlying server code
     * doesn't check whether the content'e revision is checked out to the current user or not.
     * Pass <code>true</code> for this method only in case of force checkin.
     * @param id item id assumed to be a valid guid.
     * @param ignoreRevisionCheck flag to ignore revisions while checking in or not.
     * @return PSNoContent
     */
    public PSNoContent checkIn(String id, boolean ignoreRevisionCheck);
    
    /**
     * Performs a check-out of the item identified by the specified id to the current user.  The item will be
     * transitioned to the quick edit state prior to check-out if necessary.
     * 
     * @param id never blank.
     * 
     * @return the user info for the item, never <code>null</code>.
     * @throws PSItemWorkflowServiceException if the request context for the current user thread is invalid. 
     */
    public PSItemUserInfo checkOut(String id) throws PSItemWorkflowServiceException;
    
    /**
     * Performs a forced check-out of the item identified by the specified id to the current user.  This includes a
     * check-in of the item immediately followed by a check-out.
     * 
     * @param id never blank.
     * 
     * @return the user info for the item, never <code>null</code>.
     * @throws PSItemWorkflowServiceException if the request context for the current user thread is invalid or any other
     * error occurs.
     */
    public PSItemUserInfo forceCheckOut(String id) throws PSItemWorkflowServiceException;
    
    /**
     * Gets all possible workflow transitions for the specified item.
     * 
     * @param id the ID of the item in question, not blank.
     * 
     * @return the transition info.
     */
    public PSItemStateTransition getTransitions(String id);
    
    /**
     * calls {@link #transitionWithComments(String, String, String)} with <code>null</code> for comment.
     */
    public PSItemTransitionResults transition(String id, String trigger);
    
    /**
     * Transition a specified item according to the specified trigger name. If the trigger is {@link #TRANSITION_TRIGGER_APPROVE} calls
     * {@link #performApproveTransition(String, boolean)} method to handle it.
     * 
     * @param id the ID of the item, not blank.
     * @param trigger the trigger name of the transition, not blank.  A trigger of 'Publish' will also result in the
     * transition of all shared content items associated with the item if 'Publish' is an available transition trigger.
     * @param comment transition comment may be <code>null</code> or empty.
     * 
     * @return the transition results for the item.  This includes shared assets which failed to transition. 
     */
    public PSItemTransitionResults transitionWithComments(String id, String trigger, String comment);
    
    /**
     * Performs approve transition of the supplied item.  The item will only be transitioned if all
     * shared assets which can be transitioned are also successfully transitioned. If it is index page, associated navon is transitioned.
     * If the supplied preventIfStartDate flag is true, then doesn't transition if the user doesn't have access to the publish transition
     * from the current state of the item and the start date is set on the item.
     * 
     * @param id the ID of the item, not blank.
     * @param preventIfStartDate flag to indicate whether to prevent approve transition if the start date is set.
     * @param comment transition comment may be <code>null</code> or empty.
     * 
     * @return the transition results for the item.  This includes shared assets which failed to transition. 
     */
    public PSItemTransitionResults performApproveTransition(String id, boolean preventIfStartDate, String comment);
    
    /**
     * Determines if the current user is authorized to modify (check-out, delete, etc.) the specified item in its
     * current state.
     *  
     * @param id the ID of the item, not blank.
     * 
     * @return <code>true</code> if the item can be modified by the current user, <code>false</code> otherwise.
     */
    public boolean isModifiableByUser(String id);
    
    /**
     * Gets all approved pages which use the specified asset.  An approved page is a page which is the tip revision and
     * is in an approved state (Pending, Live, Quick Edit) or which is not the tip revision and is in the Quick Edit
     * state.
     * 
     * @param id the ID of the asset, never blank.
     * 
     * @return set of page id's.  Never <code>null</code>, may be empty.
     */
    public Set<String> getApprovedPages(String id);
    
    /**
     * Gets all approved pages on the site specified by the given folder path which use the specified asset.  See
     * {@link #getApprovedPages(String)} for a description of an approved page.
     * 
     * @param id the ID of the asset, never blank.
     * @param folderPath the asset's folder path, never blank.  Must represent a valid site folder path.
     * 
     * @return set of page id's.  Never <code>null</code>, may be empty.
     */
    public Set<String> getApprovedPages(String id, String folderPath);
    
    /**
     * Checks whether the item with the supplied id is checked out to the current user or not.
     * @param id the ID of the item, never blank.
     * @return <code>true</code> if the supplied item is still checked out to the current user, otherwise
     * <code>false</code>.
     */
    public boolean isCheckedOutToCurrentUser(String id);
    
    /**
     * Checks whether the item with the supplied id is checked out to the
     * someone else user or not.
     * 
     * @param id the ID of the item, never blank.
     * @return <code>true</code> if the supplied item is still checked out to
     *         someone else user, otherwise <code>false</code>.
     */
    public boolean isCheckedOutToSomeoneElse(String id);
    
    /**
     * Checks whether the currently logged in user has previleges to do approve on the items those are 
     * in draft status.
     * 
     * @param path The path to use to determine which workflow to check, if not supplied then default workflow is used.
     * 
     * @return <code>true</code> if user can have approve, otherwise <code>false</code>.
     */
    public boolean isApproveAvailableToCurrentUser(String path);
    
    /**
     * Determines the id of the workflow in the specified request.
     * 
     * @param  workflowName never <code>null</code>.
     * 
     * @return the workflow id for the specified request.
     * @throws PSItemWorkflowServiceException if the workflow could not be found.
     */
    public int getWorkflowId(String workflowName) throws PSItemWorkflowServiceException;
    
    /**
     * Determines the id of the workflow state for the given workflow and state.
     * 
     * @param workflowName never <code>null</code>.
     * @param stateName never <code>null</code>.
     * 
     * @return the workflow state id for the specified request or -1 if a matching state could not be found.
     * @throws PSItemWorkflowServiceException if the workflow could not be found.
     */
    public int getStateId(String workflowName, String stateName) throws PSItemWorkflowServiceException;
    
    /**
     * Determines if the given trigger is available for the current user for the given item in its current state, which
     * means, can the current user use this trigger to transition the item?
     * 
     * @param id of the item, never blank.
     * @param trigger the transition trigger, never blank.
     * 
     * @return <code>true</code> if the item can be transitioned using the trigger, <code>false</code> otherwise.
     */
    public boolean isTriggerAvailable(String id, String trigger);
    
    /**
     * Checks whether staging option is available or not for the supplied user for the supplied item.
     * If the item is in one of the staging publishable states and if the user is in a role that
     * has staging permission then returns <code>true</code> otherwise returns <code>false</code>.
     * @param id of the item,  never blank.
     * @return <code>true</code> if available otherwise <code>false</code>.
     */
    public boolean isStagingOptionAvailable(String id);
    
    /**
     * Checks whether remove from staging option is available or not for the supplied user for the supplied item.
     * If the item is in archive state and if the user is in a role that has staging permission then returns 
     * <code>true</code> otherwise returns <code>false</code>.
     * @param id of the item,  never blank.
     * @return <code>true</code> if available otherwise <code>false</code>.
     */
    public boolean isRemoveFromStagingOptionAvailable(String id);

    /**
     * Returns the id of the local content workflow recognized by the name "LocalContent", throws RunTimeException if the workflow is not found.
     * @return The id of the local content workflow.
     */
    public int getLocalContentWorkflowId();
    
    /**
     * Checks if a modification to the given item is allowed. The modifications
     * are allowed if:
     * <p>
     * <li>if the item is checked out to the current user
     * <li>the item is not checked out to current user
     * 
     * @param id {@link String} with the id of the item. Must not be <code>null
     *            </code>.
     * @return <code>true</code> if the modifications are allowed. <code>false
     *         </code> otherwise.
     */
    boolean isModifyAllowed(String id);
    
    /**
     * 
     * @param items
     * @return
     */
    public String bulkApprove(PSApprovableItems items);
    
    /**
     * 
     * @param jobId
     * @return
     */
    public PSBulkApprovalJobStatus getApprovalStatusFull(String jobId);
    
    /**
     * 
     * @param jobId
     * @return
     */
    public PSBulkApprovalJobStatus getApprovalStatusProcessed(String jobId);

    /**
     * Thrown when an error is encountered in the item workflow service.
     * 
     * @author peterfrontiero
     *
     */
    public static class PSItemWorkflowServiceException extends RuntimeException
    {
        private static final long serialVersionUID = 1L;
        
        public PSItemWorkflowServiceException()
        {
            super();
        }

        public PSItemWorkflowServiceException(String message)
        {
            super(message);
        }
        
        public PSItemWorkflowServiceException(String message, Throwable cause)
        {        
            super(message, cause);
        }
        
        public PSItemWorkflowServiceException(Throwable cause)
        {
            super(cause);
        }     
    }
    
    /**
     * Constant for the approve transition trigger.
     */
    public static final String TRANSITION_TRIGGER_APPROVE = "Approve";
    
    /**
     * Constant for the archive transition trigger.
     */
    public static final String TRANSITION_TRIGGER_ARCHIVE = "Archive";
    
    /**
     * Constant for the resubmit transition trigger.
     */
    public static final String TRANSITION_TRIGGER_RESUBMIT = "Resubmit";
    
    /**
     * Constant for the take down transition trigger.
     */
    public static final String TRANSITION_TRIGGER_TAKEDOWN = "Take Down";
    
    /**
     * Constant for the live transition trigger.
     */
    public static final String TRANSITION_TRIGGER_LIVE = "forcetolive";
    
    /**
     * Constant for the submit transition trigger.
     */
    public static final String TRANSITION_TRIGGER_SUBMIT = "Submit";
    
    /**
     * Constant for the reject transition trigger.
     */
    public static final String TRANSITION_TRIGGER_REJECT = "Reject";
    
    /**
     * Constant for the edit transition trigger.
     */
    public static final String TRANSITION_TRIGGER_EDIT = "Quick Edit";
    
    /**
     * Constant for the publish transition trigger.
     */
    public static final String TRANSITION_TRIGGER_PUBLISH = "Publish";
    
    /**
     * Constant for the remove transition trigger.
     */
    public static final String TRANSITION_TRIGGER_REMOVE = "Remove";

    /**
     * Constant for the remove current state : Pending.
     */
    public static final String CURRENT_STATE_PENDING = "Pending";

    /**
     * Constant for the remove current state : Live.
     */
    public static final String CURRENT_STATE_LIVE = "Live";

    /**
     * Determines if the quick edit trigger is available for the current user for the given item in its current state (pending or live), which
     * means, can the current user use this trigger to transition the item?
     *
     * @param id of the item, never blank.
     * @param trigger the transition trigger, never blank.
     * @param currentState "Pending" or "Live", never blank.
     *
     * @return <code>true</code> if the item can be transitioned using the trigger, <code>false</code> otherwise.
     */
    public boolean isQuickEditTriggerAvailableForPendingOrLivePage(String id, String trigger, String currentState);
    
}
