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
package com.percussion.itemmanagement.service.impl;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSFolder;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.fastforward.managednav.IPSManagedNavService;
import com.percussion.itemmanagement.data.PSItemStateTransition;
import com.percussion.itemmanagement.service.IPSItemWorkflowService;
import com.percussion.itemmanagement.service.IPSWorkflowHelper;
import com.percussion.metadata.data.PSMetadata;
import com.percussion.metadata.service.IPSMetadataService;
import com.percussion.pagemanagement.service.IPSPageService;
import com.percussion.pagemanagement.service.IPSTemplateService;
import com.percussion.rx.design.PSDesignModelUtils;
import com.percussion.security.PSSecurityProvider;
import com.percussion.server.PSRequest;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.IPSItemEntry;
import com.percussion.services.workflow.data.PSState;
import com.percussion.services.workflow.data.PSTransition;
import com.percussion.services.workflow.data.PSTransitionRole;
import com.percussion.services.workflow.data.PSWorkflow;
import com.percussion.share.service.IPSIdMapper;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.request.PSRequestInfo;
import com.percussion.webservices.PSWebserviceUtils;
import com.percussion.workflow.service.IPSSteppedWorkflowService;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.percussion.itemmanagement.service.IPSItemWorkflowService.TRANSITION_TRIGGER_APPROVE;
import static com.percussion.itemmanagement.service.IPSItemWorkflowService.TRANSITION_TRIGGER_LIVE;
import static com.percussion.share.service.exception.PSParameterValidationUtils.rejectIfBlank;
import static com.percussion.share.service.exception.PSParameterValidationUtils.rejectIfNull;
import static com.percussion.webservices.PSWebserviceUtils.getItemSummary;
import static com.percussion.webservices.PSWebserviceUtils.getStateById;
import static com.percussion.webservices.PSWebserviceUtils.getUserRoles;
import static com.percussion.webservices.PSWebserviceUtils.getWorkflow;
import static com.percussion.webservices.PSWebserviceUtils.isItemCheckedOutToSomeoneElse;
import static com.percussion.webservices.PSWebserviceUtils.isItemCheckedOutToUser;
import static com.percussion.webservices.PSWebserviceUtils.transitionItem;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableCollection;
import static org.apache.commons.lang.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang.Validate.notEmpty;

/**
 * Implements {@link IPSWorkflowHelper}
 *
 * @author yubingchen
 */
@Component("workflowHelper")
@Lazy
public class PSWorkflowHelper implements IPSWorkflowHelper
{
    /**
     * The navigation service, initialized by constructor.
     */
    private IPSManagedNavService navService;

    /**
     * The item definition manager, initialized by constructor.
     */
    private PSItemDefManager itemDefManager;

    /**
     * Used for id to guid translation, initialized by constructor.
     */
    private IPSIdMapper idMapper;
    
    private IPSCmsObjectMgr cmsObjectMgr;
    
    private IPSMetadataService metadataService;
    
    /**
     * Create an instance of the workflow helper.
     *
     * @param navService navigation service, not <code>null</code>.
     * @param itemDefManager the item definition manager, not <code>null</code>.
     * @param idMapper the id to guid mapper, not <code>null</code>.
     */
    @Autowired
    public PSWorkflowHelper(IPSManagedNavService navService, PSItemDefManager itemDefManager, IPSIdMapper idMapper, 
    		IPSCmsObjectMgr cmsObjectMgr, IPSMetadataService metadataService)
    {
        this.navService = navService;
        this.itemDefManager = itemDefManager;
        this.idMapper = idMapper;
        this.cmsObjectMgr = cmsObjectMgr;
        this.metadataService = metadataService;
    }

    /*
     * //see base interface method for details
     */
    public void transitionRelatedNavigationItem(IPSGuid id, String trigger)
    {
        notEmpty(trigger);
        
        try
        {
        if (!isPage(idMapper.getString(id)))
            return;  // do nothing if not a page
        }
        catch (PSNotFoundException | PSValidationException e)
        {
            // if we have caught this exception, the item has
            // no content type and we can do nothing.
            log.error("Error transitioning landing/navigation page with id: {} Error: {}", id, e.getMessage());
            return;
        }
        
         /*
          * Make request as the internal user to ensure permissions regardless of what workflow the item may be in
         */
        PSRequest req = (PSRequest) PSRequestInfo
                .getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
        String userName = (String) PSRequestInfo.getRequestInfo(PSRequestInfo.KEY_USER);
        
        try
        {
            PSDesignModelUtils.setRequestToInternalUser(req);
            PSRequestInfo.setRequestInfo(PSRequestInfo.KEY_USER, PSSecurityProvider.INTERNAL_USER_NAME);

            IPSGuid navId = navService.findRelatedNavigationNodeId(id);
            if (navId == null)
                return;  // do nothing if is not a landing page

            int navContentId = ((PSLegacyGuid)navId).getContentId();

            // Perform transition only if item is not already in target state
            if(!isItemInPendingOrLiveTargetState(navContentId, trigger))
            {
                PSWebserviceUtils.transitionItem(navContentId, trigger, null, null);
            }
        }
        finally
        {
            PSDesignModelUtils.resetRequestToOriginal(req, userName);
        }
    }

    /**
     * Determines if the item is already in "pending" or "live" target state, according to the given trigger.
     * 
     * @param contentId the id of the content. Assumed a valid id.
     * @param trigger {@link String} with the trigger name. Assumed not <code>null</code>.
     * @return <code>true</code> if the given item is in the state implied by
     *         the trigger. <code>false</code> otherwise.
     */
    private boolean isItemInPendingOrLiveTargetState(int contentId, String trigger)
    {
        String stateName = getStateName(contentId);
        
        if (equalsIgnoreCase(TRANSITION_TRIGGER_APPROVE, trigger) && isInApproveState(stateName))
        {
            return true;
        }

        if (equalsIgnoreCase(TRANSITION_TRIGGER_LIVE, trigger) && isInLiveState(stateName))
        {
            return true;
        }

        return false;
    }

    /**
     * Checks if the given state name is the LIVE workflow state.
     * 
     * @param stateName {@link String} with the name of the workflow state.  Assumed not blank.
     * 
     * @return <code>true</code> if the state is Live state. <code>false</code> otherwise.
     */
    private boolean isInLiveState(String stateName)
    {
        return equalsIgnoreCase(WF_STATE_LIVE, stateName);
    }

    @Override
    public void transitionToPending(Set<String> ids) throws PSValidationException {
        rejectIfNull("transitionToPending", "ids", ids);
        
        Set<String> tids = new HashSet<>();
        
        for (String id : ids)
        {
            // the item will be transitioned if the trigger is available
            PSItemStateTransition trans = getTransitions(id);
            if (trans.getTransitionTriggers().contains(WF_TRIGGER_APPROVE))
            {
                tids.add(id);
            }
        }
        
        for (String tid : tids)
        {
        	//Process as much of the set as we can, log any errors. 
        	try{
	            PSLegacyGuid guid = (PSLegacyGuid)idMapper.getGuid(tid);
	            transitionItem(guid.getContentId(), WF_TRIGGER_APPROVE, null, null);
        	}catch(Exception e){
        		log.error("An error occurred while transitioning item id: {} to Pending.  Error: {}",tid , e.getMessage());
        		log.debug(e.getMessage(),e);
        	}
        }
    }

    /***
     *
     * @param id the ID of the item in question, not blank.
     *
     * @return The transitions or null if no transitions found.
     */
    @Override
    public PSItemStateTransition getTransitions(String id) throws PSValidationException {
        rejectIfBlank("getTransitions", "id", id);
      
        IPSItemEntry item = getItemEntry(id);
        int wfId = item.getWorkflowAppId();

        // -1 can be used for a workflow on certain types of items that are not workflowed - like cm1 style templates.
        if(wfId > 0) {
            List<String> userRoles = getUserRoles();
            PSWorkflow wf = getWorkflow(wfId);
            Set<Integer> userRoleIds = wf.getRoleIds(userRoles);

            int stateId = item.getContentStateId();
            List<String> triggers = new ArrayList<>();
            PSState state = getState(id);
            for (PSTransition t : state.getTransitions()) {
                if (isAllowedTransition(t, userRoleIds)) {
                    triggers.add(t.getTrigger());
                }
            }

            PSItemStateTransition trans = new PSItemStateTransition();
            trans.setItemId(id);
            trans.setStateId("" + stateId);
            trans.setStateName(state.getName());
            trans.setWorkflowId("" + wfId);
            trans.setTransitionTriggers(triggers);

            return trans;
        }else{
            return null;
        }
    }
    
    @Override
    public boolean isPending(String id) throws PSValidationException {
        rejectIfBlank("isPending", "id", id);
        
        return getState(id).getName().equals(WF_STATE_PENDING);
    }
    
    @Override
    public boolean isLive(String id) throws PSValidationException {
        rejectIfBlank("isLive", "id", id);
        
        return getState(id).getName().equals(WF_STATE_LIVE);
    }
    
    @Override
    public boolean isQuickEdit(String id) throws PSValidationException {
        rejectIfBlank("isQuickEdit", "id", id);
        
        return getState(id).getName().equals(WF_STATE_QUICKEDIT);
    }
    
    
    @Override
    public boolean isArchived(String id) throws PSValidationException {
        rejectIfBlank("isArchive", "id", id);
        
        return getState(id).getName().equals(WF_STATE_ARCHIVE);
    }

    @Override
    public boolean isApproved(String id) throws PSValidationException {
        rejectIfBlank("isApproved", "id", id);
        
        return isPending(id) || isLive(id) || isQuickEdit(id);
    }
    
    @Override
    public boolean isCheckedOutToCurrentUser(String id) throws PSValidationException {
        rejectIfBlank("isCheckedOutToCurrentUser", "id", id);
        PSComponentSummary summary = getComponentSummary(id);
        return isItemCheckedOutToUser(summary);
    }
    
    public boolean isApproveAvailableToCurrentUser(int workflowId)
    {
       List<String> userRoles = getUserRoles();
       PSWorkflow wf = getWorkflow(workflowId);
       PSState state = wf.getInitialState();
       Set<Integer> userRoleIds = wf.getRoleIds(userRoles);
       boolean isAvailable = false;
       for (PSTransition t : state.getTransitions())
       {
           if (isAllowedApprovalTransition(t, userRoleIds))
           {
               isAvailable = true;
               break;
           }
       }
       return isAvailable; 
    }

    @Override
    public boolean isTipRevision(String id) throws PSValidationException {
        rejectIfBlank("isTipRevision", "id", id);
        
        PSLocator locator = idMapper.getLocator(id);
        int rev = locator.getRevision();
        PSComponentSummary sum = getComponentSummary(id);
        PSLocator tipLocator = sum.getTipLocator();
        
        return rev == tipLocator.getRevision();
    }
    
    @Override
    public PSComponentSummary getComponentSummary(String id) throws PSValidationException {
        rejectIfBlank("getComponentSummary", "id", id);
        PSLegacyGuid guid = (PSLegacyGuid) idMapper.getGuid(id);
        return getItemSummary(guid.getContentId());
    }
    
    private IPSItemEntry getItemEntry(String id)
    {
        int contentId = idMapper.getContentId(id);
        return cmsObjectMgr.findItemEntry(contentId);
    }
    
    @Override
    public PSState getState(String id) throws PSValidationException {
        rejectIfBlank("getState", "id", id);
        
        IPSItemEntry item = getItemEntry(id);
        
        PSWorkflow wf = getWorkflow(item.getWorkflowAppId());
        return getStateById(wf, item.getContentStateId());
    }
    
    @Override
    public boolean isCheckedOutToSomeoneElse(String id) throws PSValidationException {
        rejectIfBlank("isCheckedOutToSomeoneElse", "id", id);
        PSComponentSummary summary = getComponentSummary(id);
        return isItemCheckedOutToSomeoneElse(summary);
    }
    
    /**
     * Determines if the supplied state is in approved states.
     * 
     * @param stateName {@link String} with the name of the workflow state.
     *            Assumed not blank.
     * @return <code>true</code> if the item is in approved states;
     *         <code>false</code> otherwise.
     */
    private boolean isInApproveState(String stateName)
    {
        return WF_APPROVE_STATES.contains(stateName);
    }
    
    private boolean isInStagingState(String stateName)
    {
        return WF_STATE_STAGING.equalsIgnoreCase(stateName) || WF_PUBLIC_STATES.contains(stateName);
    }

    public boolean isItemInApproveState(int contentId)
    {
        IPSItemEntry item = cmsObjectMgr.findItemEntry(contentId);
        return item == null ? false : isInApproveState(item.getStateName());
    }

    public boolean isItemInStagingState(int contentId)
    {
        IPSItemEntry item = cmsObjectMgr.findItemEntry(contentId);
        return item == null ? false : isInStagingState(item.getStateName());
    }
    
    /**
     * Gets the workflow state name for the specified item.
     * @param contentId the ID of the item.
     * @return the state name. It may be <code>null</code> if the item does not exist.
     */
    private String getStateName(int contentId)
    {
        IPSItemEntry item = cmsObjectMgr.findItemEntry(contentId);
        return item == null ? null : item.getStateName();
    }
    
    @Override
    public boolean isPage(String id) throws PSNotFoundException, PSValidationException {
        rejectIfBlank("isPage", "id", id);
        
        String ctId = getContentType(id);
        
        if (StringUtils.isBlank(ctId)) {
            throw new PSNotFoundException("No content type was found for id: " + id);
        }
        
        return IPSPageService.PAGE_CONTENT_TYPE.equals(ctId);
    }

    @Override
    public boolean isTemplate(String id) throws PSValidationException {
        rejectIfBlank("isTemplate", "id", id);
        
        return IPSTemplateService.TPL_CONTENT_TYPE.equals(getContentType(id));
    }
    
    public boolean isAsset(String id) throws PSNotFoundException, PSValidationException {
        rejectIfBlank("isAsset", "id", id);
        
        if (!isPage(id) && !isTemplate(id))
        {
            IPSItemEntry item = getItemEntry(id);
            if (!item.isFolder())
            {            
                // check to see if it is a navon or navtree
                long cTypeId = item.getContentTypeId();
                if (cTypeId != navService.getNavonContentTypeId() && cTypeId != navService.getNavtreeContentTypeId())
                {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    
    
    @Override
    public boolean isLocalAsset(String id) throws PSNotFoundException, PSValidationException {
        boolean isLocalAsset = false;
        if (isAsset(id))
        {
            IPSItemEntry item = getItemEntry(id);
            PSWorkflow wf = getWorkflow(item.getWorkflowAppId());
            if (PSWorkflowHelper.LOCAL_WORKFLOW_NAME.equals(wf.getName()))
            {
                isLocalAsset = true;
            }
        }
        
        return isLocalAsset;
    }

    @Override
    public PSItemTypeEnum getItemType(String id) throws PSValidationException {
        rejectIfBlank("getItemType", "id", id);
        PSItemTypeEnum type = PSItemTypeEnum.UNKNOWN;
        
        IPSItemEntry item = getItemEntry(id);
        if (item.isFolder())
        {            
            type = PSItemTypeEnum.FOLDER;
        } 
        else
        {
            long cTypeId = item.getContentTypeId();
            try
            {
                type = getItemTypeFromCType(cTypeId);
            }
            catch(Exception e)
            {
                String msg = "Failed to get content type for item id = " + id;
                log.error(msg, e);
            }
        }
        return type;
    }
    
    public PSItemTypeEnum getItemTypeFromCType(long cTypeId)
    {
        PSItemTypeEnum type = PSItemTypeEnum.UNKNOWN;
        
        try
        {
            if (PSFolder.FOLDER_CONTENT_TYPE_ID == cTypeId)
                type = PSItemTypeEnum.FOLDER;
            else if (cTypeId == navService.getNavonContentTypeId())
                type = PSItemTypeEnum.NAVON;
            else if(cTypeId == navService.getNavtreeContentTypeId())
                type = PSItemTypeEnum.NAVTREE;
            else if(cTypeId == itemDefManager.contentTypeNameToId(IPSTemplateService.TPL_CONTENT_TYPE))
                type = PSItemTypeEnum.TEMPLATE;
            else if(cTypeId == itemDefManager.contentTypeNameToId(IPSPageService.PAGE_CONTENT_TYPE))
                type = PSItemTypeEnum.PAGE;
            else
                type = PSItemTypeEnum.ASSET;
            
        }
        catch(Exception e)
        {
            String msg = "Failed to get content type for content type id = " + cTypeId;
            log.error(msg, e);
        }
        
        return type;
    }
    
    /**
     * Determines the underlying content type of the specified item.
     * 
     * @param id the ID of the item, assumed not blank.
     * 
     * @return the name of the content type for the given item.  May be <code>null</code> if an error occurs and the
     * content type cannot be determined.
     */
    private String getContentType(String id)
    {
        String ctype = null;
        IPSItemEntry item = getItemEntry(id);
        
        try
        {
            ctype = itemDefManager.contentTypeIdToName(item.getContentTypeId());
        }
        catch (Exception e)
        {
            String msg = "Failed to get content type for item id = " + id;
            log.error(msg, e);
        }
        
        return ctype;
    }
    
    /**
     * Determines if the specified transition is allowed for the given 
     * user roles.
     * 
     * @param t the specified transition, assumed not <code>null</code>.
     * @param userRoleIds a set of (workflow) role IDs the current user is
     * a member of.
     * 
     * @return <code>true</code> if the transition is allowed for the
     * specified roles; otherwise the transition is not allowed for the
     * user that is a member of the specified roles.
     */
    private boolean isAllowedTransition(PSTransition t, Set<Integer> userRoleIds)
    {
        if (t.isAllowAllRoles())
        {
            return true;
        }
        
        for (PSTransitionRole tranRole : t.getTransitionRoles())
        {
            Integer tranRoleId = (int) tranRole.getRoleId();
            if (userRoleIds.contains(tranRoleId))
            {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Determines if the approval transition is allowed for the given 
     * user roles.
     * 
     * @param t the specified transition, assumed not <code>null</code>.
     * @param userRoleIds a set of (workflow) role IDs the current user is
     * a member of.
     * 
     * @return <code>true</code> if the approval transition is allowed for the
     * specified roles, otherwise false
     */
    private boolean isAllowedApprovalTransition(PSTransition t, Set<Integer> userRoleIds)
    {
        boolean isAllowed = false;
        for (PSTransitionRole tranRole : t.getTransitionRoles())
        {
            Integer tranRoleId = (int) tranRole.getRoleId();
            if ((userRoleIds.contains(tranRoleId)) && (t.getTrigger().equals(WF_TRIGGER_APPROVE)))
            {
                isAllowed =  true;
                break;
            }
        }
        return isAllowed;
    }
    
    public boolean isPublicState(String state)
    {
        return WF_PUBLIC_STATES.contains(state);
    }

    @Override
    public boolean isApproveAvailableToCurrentUser(String itemId) throws PSValidationException {
        PSItemStateTransition stateTrans = getTransitions(itemId);
        List<String> trans = stateTrans.getTransitionTriggers();
        for (String tran : trans)
        {
            if(tran.equalsIgnoreCase(WF_TRIGGER_APPROVE))
                return true;
        }
        return false;
    }
    
    @Override
    public List<String> getStagingRoles(int workflowId) 
    {
    	List<String> roleNames = new ArrayList<>();
    	PSMetadata md = metadataService.find(IPSSteppedWorkflowService.METADATA_STAGING_ROLES_KEY_PREFIX + workflowId);
    	if(md != null)
    	{
            String temp = md.getData();
            roleNames.addAll(Arrays.asList(temp.split(IPSSteppedWorkflowService.METADATA_STAGING_ROLES_VALUE_SEPARATOR)));
    	}
    	return roleNames;
    }


    /**
     * Logger for this service.
     */
    public static Logger log = LogManager.getLogger(PSWorkflowHelper.class);

    /**
     * Constant for the name of the pending workflow state.
     */
    public static final String WF_STATE_PENDING = "Pending";
    
    /**
     * Constant for the name of the live workflow state.
     */
    public static final String WF_STATE_LIVE = "Live";
    
    /**
     * Constant for the name of the archive workflow state.
     */
    public static final String WF_STATE_ARCHIVE = "Archive";

    /**
     * Constant for the name of the archive workflow state.
     */
    public static final String WF_STATE_STAGING = "Review";

    /**
     * Constant for the name of the transition when transition an item from live to archive state.
     */
    public static final String WF_TAKE_DOWN_TRANSITION = "Archive";

    /**
     * Constant for the name of the quick edit workflow state.
     */
    public static final String WF_STATE_QUICKEDIT = "Quick Edit";
    
    /**
     * Constant for the name of the trigger used to transition items to the Pending state.
     */
    public static final String WF_TRIGGER_APPROVE = IPSItemWorkflowService.TRANSITION_TRIGGER_APPROVE;
    
    /***
     * Constant for transitioning items to the review state.
     */
    public static final String WF_TRIGGER_REVIEW = IPSItemWorkflowService.TRANSITION_TRIGGER_SUBMIT;
    
    /**
     * Constant for the name of the default workflow
     */
    public static final String DEFAULT_WORKFLOW = "Default Workflow";
    
    /**
     * Constant for the name for the workflow used for local content.
     */
    public static final String LOCAL_WORKFLOW_NAME = "LocalContent";
        
    private static final Collection<String> WF_PUBLIC_STATES = 
        unmodifiableCollection(asList(WF_STATE_LIVE, WF_STATE_PENDING, WF_STATE_QUICKEDIT));
    
    private static final Collection<String> WF_APPROVE_STATES = 
        unmodifiableCollection(asList(WF_STATE_LIVE, WF_STATE_PENDING));

	@Override
	public void transitionToArchive(Set<String> ids) throws PSValidationException {
		 rejectIfNull("transitionToArchive", "ids", ids);
	        
	        Set<String> tids = new HashSet<>();
	        
	        for (String id : ids)
	        {
                // the item will be transitioned if the trigger is available
                PSItemStateTransition trans = getTransitions(id);
                if (trans.getTransitionTriggers().contains(WF_TAKE_DOWN_TRANSITION))
                {
                    tids.add(id);
                }
            }
	        
	        for (String tid : tids)
	        {
	        	//Process as much of the set as we can, log any errors. 
	        	try{
		            PSLegacyGuid guid = (PSLegacyGuid)idMapper.getGuid(tid);
		            transitionItem(guid.getContentId(), WF_TAKE_DOWN_TRANSITION, null, null);
	        	}catch(Exception e){
	        		log.error("An error occurred while transitioning item id:" + tid + " to Pending.",  e);
	        	}
	        }
		
	}

	@Override
	public void transitionToReview(Set<String> ids) throws PSValidationException {
		 rejectIfNull("transitionToReview", "ids", ids);
	        
	        Set<String> tids = new HashSet<>();
	        
	        for (String id : ids)
	        {
                // the item will be transitioned if the trigger is available
                PSItemStateTransition trans = getTransitions(id);
                if (trans.getTransitionTriggers().contains(WF_TRIGGER_REVIEW))
                {
                    tids.add(id);
                }
            }
	        
	        for (String tid : tids)
	        {
	        	//Process as much of the set as we can, log any errors. 
	        	try{
		            PSLegacyGuid guid = (PSLegacyGuid)idMapper.getGuid(tid);
		            transitionItem(guid.getContentId(), WF_TRIGGER_REVIEW, null, null);
	        	}catch(Exception e){
	        		log.error("An error occurred while transitioning item id:" + tid + " to Pending.",  e);
	        	}
	        }
		
	}
    

}
