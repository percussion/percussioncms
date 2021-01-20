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
package com.percussion.itemmanagement;

import com.percussion.itemmanagement.data.PSApprovableItem;
import com.percussion.itemmanagement.data.PSApprovableItems;
import com.percussion.itemmanagement.data.PSItemTransitionResults;
import com.percussion.itemmanagement.service.IPSItemWorkflowService.PSItemWorkflowServiceException;
import com.percussion.itemmanagement.service.IPSWorkflowHelper;
import com.percussion.itemmanagement.service.impl.PSItemWorkflowService;
import com.percussion.pathmanagement.data.PSFolderPermission;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.workflow.data.PSState;
import com.percussion.share.async.impl.PSAsyncJob;
import com.percussion.share.dao.IPSFolderHelper;
import com.percussion.share.service.IPSIdMapper;
import com.percussion.user.service.IPSUserService;
import com.percussion.user.service.impl.PSUserService;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSWebserviceUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Class that implements job to approve a list of items.
 * 
 * @author leonardohildt
 * 
 */
public class PSBulkApprovalJob extends PSAsyncJob
{

    private static final Log log = LogFactory.getLog(PSBulkApprovalJob.class);

    private static final String APPROVED_ITEM = "Approved";

    private static final String FAILED_APPROVAL_ITEM = "Failed";

    private PSItemWorkflowService itemWorkflowService;

    private IPSWorkflowHelper workflowHelper;
    
    private IPSFolderHelper folderHelper;
    
    private IPSIdMapper idMapper;
    
    private IPSUserService userService;
    
    private boolean isAdmin = false;

    PSApprovableItems items;
    
    List<PSApprovableItem> processedItems = new ArrayList<PSApprovableItem>();
    
    private Map<String, String> approvalErrors = new HashMap<String, String>();

    @Override
    protected void doInit(Object config)
    {
        items = (PSApprovableItems) config;
        items.setProcessedItems(processedItems);
        setResult(items);
        setStatusMessage("Initializing");
    }

    @Override
    public void doRun()
    {
        isAdmin = userService.isAdminUser(PSWebserviceUtils.getUserName());
        for (PSApprovableItem item : items.getApprovableItems())
        {
            if (item.isApprove())
            {
                approvePage(item);
            }
        }
        items.setErrors(approvalErrors);
        setStatus(COMPLETE_STATUS);
        setCompleted();
    }

    /**
     * Approves a single page.
     * @param item Details of the page assumed not <code>null</code>.
     */
    private void approvePage(PSApprovableItem item)
    {
        try
        {

            if(isInApprovedState(item) || isCheckedOutByOthers(item) || !hasFolderAccess(item) || !hasApproveTransition(item))
            {
                processedItems.add(item);
                return;
            }
            
            PSItemTransitionResults transitionResult = itemWorkflowService.performApproveTransition(
                    item.getId(), false, null);

            if (transitionResult.getFailedAssets().isEmpty())
            {
                item.setApprovalStatus(APPROVED_ITEM);
                PSState state = workflowHelper.getState(item.getId());
                item.setStatus(state.getName());
                processedItems.add(item);
            }
            else
            {
                String msg = "You cannot approve this page as one or more related asset(s) could not be approved.";
                handleError(item, msg);
            }
                
            
        }
        catch(PSItemWorkflowServiceException iwe)
        {
            handleError(item, iwe.getLocalizedMessage());
        }
        catch(Exception e)
        {
            log.error(e);
            String msg = "Unexpected error occurred while approving the item.";
            handleError(item, msg);
        }
    }

    /**
     * Helper method to check whether the item is checked out by someone else or not.
     * @param item assumed not <code>null</code>
     * @return true if it is checked out by someone else.
     */
    private boolean isCheckedOutByOthers(PSApprovableItem item)
    {
        boolean result = false;
        if(workflowHelper.isCheckedOutToSomeoneElse((item.getId())))
        {
            String msg = "You cannot approve this page as it is being edited by someone else.";
            handleError(item, msg);
            result = true;            
        }
        return result;
    }

    /**
     * Helper method to check whether user has permission to approve transition of the item 
     * @param item assumed not <code>null</code>
     * @return true if the user has approve transition.
     */
    private boolean hasApproveTransition(PSApprovableItem item)
    {
        if(isAdmin)
            return true;
        boolean result = true;
        if(!workflowHelper.isApproveAvailableToCurrentUser(item.getId()))
        {
            String msg = "You do not have permission to approve this page.";
            handleError(item, msg);
            result = false;            
        }
        return result;
    }

    
    /**
     * Helper method to check whether user has at least write permission to the parent folder. 
     * If user doesn't have the access then updates the item status and error message.
     * @param itemId assumed not <code>null</code>.
     * @return true if user has at least write access or false.
     */
    private boolean hasFolderAccess(PSApprovableItem item)
    {
        if(isAdmin)
            return true;
        //check folder permission
        IPSGuid itemGuid = idMapper.getGuid(item.getId());
        IPSGuid parentFolder = folderHelper.getParentFolderId(itemGuid);
        PSFolderPermission.Access access = folderHelper.getFolderAccessLevel(parentFolder.toString());
        boolean result = false;
        if(access.equals(PSFolderPermission.Access.ADMIN) || access.equals(PSFolderPermission.Access.WRITE))
        {
            result = true;
        }
        else
        {
            String msg = "You do not have permission to approve this page.";
            handleError(item, msg);
            result = false;
        }
        return result;
    }
    
    /**
     * Helper method to check whether the item is already in an approved state or not
     * if it is in, then updates the supplied item and returns true otherwise false.
     * @param item assumed not <code>null</code>
     * @return true if the item is already in approved state.
     */
    private boolean isInApprovedState(PSApprovableItem item)
    {
        boolean result = false;
        if(workflowHelper.isLive(item.getId()) || workflowHelper.isPending(item.getId()))
        {
            item.setApprovalStatus(APPROVED_ITEM);
            PSState state = workflowHelper.getState(item.getId());
            item.setStatus(state.getName());
            processedItems.add(item);
            result = true;
        }
        return result;
    }
    
    /**
     * Helper method to handle error for an item.
     * @param item assumed not <code>null</code>.
     * @param msg assumed not <code>null</code>.
     */
    private void handleError(PSApprovableItem item, String msg)
    {
        item.setApprovalStatus(FAILED_APPROVAL_ITEM);
        item.setApprovalMessage(msg);
        approvalErrors.put(item.getName(), msg);
        processedItems.add(item);
    }
    
    public PSItemWorkflowService getItemWorkflowService()
    {
        return itemWorkflowService;
    }

    public void setItemWorkflowService(PSItemWorkflowService itemWorkflowService)
    {
        this.itemWorkflowService = itemWorkflowService;
    }

    public IPSWorkflowHelper getWorkflowHelper()
    {
        return workflowHelper;
    }

    public void setWorkflowHelper(IPSWorkflowHelper workflowHelper)
    {
        this.workflowHelper = workflowHelper;
    }

    public IPSFolderHelper getFolderHelper()
    {
        return folderHelper;
    }

    public void setFolderHelper(IPSFolderHelper folderHelper)
    {
        this.folderHelper = folderHelper;
    }

    public IPSIdMapper getIdMapper()
    {
        return idMapper;
    }

    public void setIdMapper(IPSIdMapper idMapper)
    {
        this.idMapper = idMapper;
    }

    public IPSUserService getUserService()
    {
        return userService;
    }

    public void setUserService(IPSUserService userService)
    {
        this.userService = userService;
    }


}
