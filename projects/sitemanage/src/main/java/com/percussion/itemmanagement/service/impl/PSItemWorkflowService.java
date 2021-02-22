/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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

import com.percussion.assetmanagement.dao.IPSAssetDao;
import com.percussion.assetmanagement.data.PSAsset;
import com.percussion.assetmanagement.service.IPSWidgetAssetRelationshipService;
import com.percussion.assetmanagement.service.impl.PSWidgetAssetRelationshipService;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.foldermanagement.service.IPSFolderService;
import com.percussion.itemmanagement.data.PSApprovableItems;
import com.percussion.itemmanagement.data.PSBulkApprovalJobStatus;
import com.percussion.itemmanagement.data.PSItemStateTransition;
import com.percussion.itemmanagement.data.PSItemTransitionResults;
import com.percussion.itemmanagement.data.PSItemUserInfo;
import com.percussion.itemmanagement.service.IPSItemWorkflowService;
import com.percussion.itemmanagement.service.IPSWorkflowHelper;
import com.percussion.pagemanagement.dao.IPSPageDao;
import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.service.IPSPageService;
import com.percussion.pathmanagement.data.PSFolderPermission;
import com.percussion.recycle.service.IPSRecycleService;
import com.percussion.security.PSThreadRequestUtils;
import com.percussion.server.IPSRequestContext;
import com.percussion.services.catalog.data.PSObjectSummary;
import com.percussion.services.content.data.PSItemStatus;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.services.system.IPSSystemService;
import com.percussion.services.system.PSSystemException;
import com.percussion.services.workflow.IPSWorkflowService;
import com.percussion.services.workflow.data.PSAssignmentTypeEnum;
import com.percussion.services.workflow.data.PSState;
import com.percussion.services.workflow.data.PSTransition;
import com.percussion.services.workflow.data.PSTransitionRole;
import com.percussion.services.workflow.data.PSWorkflow;
import com.percussion.share.async.IPSAsyncJob;
import com.percussion.share.async.IPSAsyncJobService;
import com.percussion.share.async.PSAsyncJobStatus;
import com.percussion.share.dao.IPSFolderHelper;
import com.percussion.share.dao.IPSGenericDao;
import com.percussion.share.data.PSDataItemSummary;
import com.percussion.share.data.PSNoContent;
import com.percussion.share.service.IPSDataItemSummaryService;
import com.percussion.share.service.IPSIdMapper;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.sitemanage.dao.IPSiteDao;
import com.percussion.sitemanage.data.PSSiteSummary;
import com.percussion.util.PSSiteManageBean;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.request.PSRequestInfo;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.PSErrorsException;
import com.percussion.webservices.PSWebserviceUtils;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.security.IPSSecurityWs;
import com.percussion.webservices.system.IPSSystemWs;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.MediaType;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static com.percussion.share.service.exception.PSParameterValidationUtils.rejectIfBlank;
import static com.percussion.share.service.exception.PSParameterValidationUtils.rejectIfNull;
import static com.percussion.webservices.PSWebserviceUtils.getItemSummary;
import static com.percussion.webservices.PSWebserviceUtils.getUserName;
import static com.percussion.webservices.PSWebserviceUtils.getUserRoles;
import static com.percussion.webservices.PSWebserviceUtils.getWorkflow;
import static com.percussion.webservices.PSWebserviceUtils.isItemCheckedOutToUser;
import static com.percussion.webservices.PSWebserviceUtils.transitionItem;
import static java.util.Arrays.asList;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;

/**
 * @author peterfrontiero
 *
 */
@Path("/workflow")
//@Component("workflowRestService")
@PSSiteManageBean("workflowRestService")
@Lazy
public class PSItemWorkflowService implements IPSItemWorkflowService
{
    IPSContentWs contentWs;
    IPSIdMapper idMapper;
    IPSSecurityWs securityWs;
    IPSWidgetAssetRelationshipService widgetAssetRelationshipService;
    IPSPageDao pageDao;
    IPSSystemService systemService;
    IPSWorkflowHelper workflowHelper;
    IPSAssetDao assetDao;
    IPSDataItemSummaryService dataItemSummaryService;
    IPSFolderHelper folderHelper;
    IPSWorkflowService workflowService;
    IPSiteDao siteDao;
    IPSPageService pageService;
    IPSSiteManager siteMgr;
    IPSSystemWs systemWs;
    private IPSRecycleService recycleService;
    private IPSAsyncJobService asyncJobService;
    private static final String BULK_APPROVAL_JOB_BEAN = "bulkApprovalJob";
        
    @Autowired
    public PSItemWorkflowService(IPSContentWs contentWs, IPSIdMapper idMapper, IPSSecurityWs securityWs,
            IPSWidgetAssetRelationshipService widgetAssetRelationshipService, IPSPageDao pageDao,
            IPSSystemService systemService, IPSWorkflowHelper workflowHelper, IPSAssetDao assetDao,
            IPSDataItemSummaryService dataItemSummaryService, IPSFolderHelper folderHelper, 
            IPSWorkflowService workflowService, IPSiteDao siteDao, IPSSiteManager siteMgr, IPSSystemWs systemWs, IPSAsyncJobService asyncJobService, IPSRecycleService recycleService)
    {
        super();
        this.contentWs = contentWs;
        this.idMapper = idMapper;
        this.securityWs = securityWs;
        this.widgetAssetRelationshipService = widgetAssetRelationshipService;
        this.pageDao = pageDao;
        this.systemService = systemService;
        this.workflowHelper = workflowHelper;
        this.assetDao = assetDao;
        this.dataItemSummaryService = dataItemSummaryService;
        this.folderHelper = folderHelper;
        this.workflowService = workflowService;
        this.siteDao = siteDao;
        this.siteMgr = siteMgr;
        this.systemWs = systemWs;
        this.asyncJobService = asyncJobService;
        this.recycleService=recycleService;
    }
            
    @Override
    @GET
    @Path("checkIn/{id}")
    public PSNoContent checkIn(@PathParam("id") String id)
    {
        try {
            rejectIfBlank("checkIn", "id", id);

            return checkIn(id, false);
        } catch (PSItemWorkflowServiceException | PSDataServiceException e) {
            throw new WebApplicationException(e.getMessage());
        }
    }

    /**
     * Checks in the content, if ignoreRevisionCheck is <code>true</code> the underlying server code
     * doesn't check whether the content'e revision is checked out to the current user or not.
     * Pass <code>true</code> for this method only in case of force checkin.
     * @param id item id assumed to be a valid guid.
     * @param ignoreRevisionCheck flag to ignore revisions while checking in or not.
     * @return PSNoContent
     */
    public PSNoContent checkIn(String id, boolean ignoreRevisionCheck) throws PSItemWorkflowServiceException, PSDataServiceException {
            List<String> ids = new ArrayList<>();
            ids.add(id);

            PSDataItemSummary sum = dataItemSummaryService.find(id);
            if (sum == null)
                return new PSNoContent("checkIn");

            if (sum.getType().equals(IPSPageService.PAGE_CONTENT_TYPE)) {
                ids.addAll(getLocalAssetIdsForCheckin(id));
            }

            try {
                contentWs.checkinItems(idMapper.getGuids(ids), null, ignoreRevisionCheck);
                return new PSNoContent("checkIn");
            } catch (PSErrorsException e) {
                PSErrorException ex = (PSErrorException) e.getErrors().get(
                        ids.get(0));
                throw new PSItemWorkflowServiceException("Failed to check-in item: " + sum != null ? sum.getName() : "no summary " + "id= " + id + " , Error: " + ex.getErrorMessage(), ex);
            }

    }
    
   

    private List<String> getLocalAssetIdsForCheckin(String pageId)
    {
        List<String> ids = new ArrayList<>();
        
        List<PSRelationship> rels = widgetAssetRelationshipService.getLocalAssetRelationships(pageId, null, null);
        for (PSRelationship rel : rels)
        {
            PSLocator dep = rel.getDependent();
            PSComponentSummary summary = PSWebserviceUtils.getItemSummary(dep.getId());
            int tipRev = summary.getTipLocator().getRevision();
            String currentUser = (String)PSRequestInfo.getRequestInfo(PSRequestInfo.KEY_USER);
            
            if (currentUser != null && StringUtils.isNotEmpty(summary.getCheckoutUserName())
                    && !currentUser.equals(summary.getCheckoutUserName())) {
                forceCheckInLocalContent(dep, currentUser);
            }
            
            if (dep.getRevision() != tipRev)
            {
                // the revision of local asset must by in tip revision before check-in.
                // the relationship is out of sync, fix up the relationship.
                dep.setRevision(tipRev);
                systemWs.saveRelationships(Collections.singletonList(rel));
            }
            IPSGuid guid = new PSLegacyGuid(dep);
            ids.add(guid.toString());
        }
        return ids;
    }
    
    private void forceCheckInLocalContent(PSLocator dep, String currentUser) {
        try {
            log.debug("Attempting to force checkin for local content with content id: " + dep.getId()
                    + " and current user: " + currentUser);
            PSThreadRequestUtils.changeToInternalRequest(true);
            IPSGuid guid = idMapper.getGuid(dep);
            contentWs.checkinItems(Collections.singletonList(guid), null, true);
        }
        catch (PSErrorsException e) {
            PSErrorException ex = (PSErrorException) e.getErrors().get(
                    dep.getId());
            log.error("Error checking in local content with id: " + dep.getId() + " and error: " + ex.getErrorMessage(), e);
        }
        finally {
            PSThreadRequestUtils.restoreOriginalRequest();
        }
    }

    @Override
    @GET
    @Path("getTransitions/{id}")
    public PSItemStateTransition getTransitions(@PathParam("id") String id)
    {
        try {
            rejectIfBlank("getTransitions", "id", id);

            PSComponentSummary sum = workflowHelper.getComponentSummary(id);
            int wfId = sum.getWorkflowAppId();

            List<String> userRoles = getUserRoles();
            PSWorkflow wf = getWorkflow(wfId);
            Set<Integer> userRoleIds = wf.getRoleIds(userRoles);

            int stateId = sum.getContentStateId();
            List<String> triggers = new ArrayList<>();
            String defTrigger = null;
            PSState state = workflowHelper.getState(id);
            for (PSTransition t : state.getTransitions()) {
                if (isAllowedTransition(t, userRoleIds)) {
                    if (t.isDefaultTransition()) {
                        defTrigger = t.getTrigger();
                    } else {
                        triggers.add(t.getTrigger());
                    }
                }
            }
            if (defTrigger != null) {
                triggers.add(0, defTrigger);
            }
            PSItemStateTransition trans = new PSItemStateTransition();
            trans.setItemId(id);
            trans.setStateId("" + stateId);
            trans.setStateName(state.getName());
            trans.setWorkflowId("" + wfId);
            trans.setTransitionTriggers(triggers);

            return trans;
        } catch (PSValidationException e) {
            throw new WebApplicationException(e.getMessage());
        }
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
    
    @Override
    @GET
    @Path("transition/{id}/{trigger}")
    public PSItemTransitionResults transition(@PathParam("id") String id, @PathParam("trigger") String trigger)
    {
        return transitionWithComments(id, trigger, null);
    }
    
    @Override
    @GET
    @Path("transitionWithComments/{id}/{trigger}")
    public PSItemTransitionResults transitionWithComments(@PathParam("id") String id, @PathParam("trigger") String trigger, @QueryParam("comment") String comment)
    {
        try {
            rejectIfBlank("transition", "id", id);
            PSItemTransitionResults results = new PSItemTransitionResults();
            //Make sure user has permission for publish transition while he is approving the content
            //When the scheduled date is on
            if (trigger.equalsIgnoreCase(TRANSITION_TRIGGER_APPROVE)) {
                results = performApproveTransition(id, true, comment);
            } else {
                IPSGuid guid = idMapper.getGuid(id);
                // transition the item
                checkIn(id);
                transitionItem(((PSLegacyGuid) guid).getContentId(), trigger, comment, null);
                results.setItemId(id);
            }
            return results;
        } catch (PSItemWorkflowServiceException | PSDataServiceException | PSNotFoundException e) {
            throw new WebApplicationException(e.getMessage());
        }
    }
    
    /**
     * Checks whether the page has shared assets or managed links to resources, that are included in a folder
     * with the current site as not allowed. If so, an error message will display to the user stating he cannot
     * approve the page.
     * 
     * @author federicoromanelli
     * @param id - the ID of the page to check the information. Never <code> null </code>
     * @return true if no assets or managed links are found that are unallowed for the current site, false otherwise.
     */
    private boolean isPublishablePage(String id) throws PSDataServiceException, PSNotFoundException {
        rejectIfBlank("isPublishablePage", "id", id);
        
        PSPage page = pageDao.find(id);
        if (page == null)
            return true;
        IPSGuid guid = idMapper.getGuid(id);
        List<IPSSite> sites = siteMgr.getItemSites(guid);
        if (sites == null || sites.size() == 0)
            return true;
        Long siteId = sites.get(0).getSiteId();
        
        // Create a set with shared and linked assets
        Set<String> sharedAssets = widgetAssetRelationshipService.getSharedAssets(id);
        log.debug("The shared assets to check for approval are: {}", sharedAssets);
        Set<String> linkedAssets = widgetAssetRelationshipService.getLinkedAssets(id);
        log.debug("The linked assets to check for approval are: {}", linkedAssets);
        Set<String> allAssets = new TreeSet<>(sharedAssets);
        allAssets.addAll(linkedAssets);

        //Iterate over the shared and linked assets
        Iterator<String> assetsIterator = allAssets.iterator();
        while (assetsIterator.hasNext())
        {
            String currentAssetId = assetsIterator.next();
            log.debug("The asset being transitioned is: " + currentAssetId);
            String rootLevelFolderAllowedSites = folderHelper.getRootLevelFolderAllowedSitesPropertyValue(currentAssetId);
            log.debug("The root level folder being checked is: " + rootLevelFolderAllowedSites);
            if (rootLevelFolderAllowedSites != null)
            {
                if (!rootLevelFolderAllowedSites.contains(String.valueOf(siteId)))
                {
                    return false;
                }
            }
        }

        return true;
    }
    
    /*
     * (non-Javadoc)
     * @see com.percussion.itemmanagement.service.IPSItemWorkflowService#performApproveTransition(java.lang.String, boolean)
     */
    public PSItemTransitionResults performApproveTransition(String id, boolean preventIfStartDate, String comment) throws PSItemWorkflowServiceException, PSDataServiceException, PSNotFoundException {
        rejectIfBlank("transition", "id", id);
        
        //Make sure user has permission for publish transition while he is approving the content
        //When the scheduled date is on
        if(!isTriggerAvailable(id, TRANSITION_TRIGGER_PUBLISH) && preventIfStartDate) 
        {
            PSComponentSummary sum = workflowHelper.getComponentSummary(id);
            if(sum.getContentStartDate() != null)
            {
                throw new PSItemWorkflowServiceException("When there is a publish date, you must have publishing permissions to Approve an item.");
            }
        }
        
        if (!isPublishablePage(id))
        {
            throw new PSItemWorkflowServiceException("This page contains assets that are under another site. " +
                    "Before this page can be approved or published, the assets which are no longer appropriate must be removed.");
        }
        
        //Transition shared assets if the trigger is approve, collect all the assets that are not transitionable.
        List<PSDataItemSummary> failedAssets = new ArrayList<>();
       	failedAssets = approveSharedAssets(id);
        
        //If there are any failed assets then don't transition the page prepare PSItemTransitionResults and send it back. 
        if (failedAssets.isEmpty())
        {
            IPSGuid guid = idMapper.getGuid(id);
            workflowHelper.transitionRelatedNavigationItem(guid, IPSItemWorkflowService.TRANSITION_TRIGGER_APPROVE);
            // transition the item
            checkIn(id);
            transitionItem(((PSLegacyGuid) guid).getContentId(), IPSItemWorkflowService.TRANSITION_TRIGGER_APPROVE, comment, null);
        }
        
        PSItemTransitionResults results = new PSItemTransitionResults();
        results.setItemId(id);
        results.setFailedAssets(failedAssets);
        
        return results;
    }
    


    @Override
    @GET
    @Path("checkOut/{id}")
    public PSItemUserInfo checkOut(@PathParam("id") String id)
    {
        try {
            rejectIfBlank("checkOut", "id", id);

            IPSGuid guid = idMapper.getGuid(id);

            try {
                //Check out the item only if the user has assignee access or admin access.
                if (isModifiableByUser(id)) {
                    PSComponentSummary summ = workflowHelper.getComponentSummary(id);
                    PSLocator loc = idMapper.getLocator(guid);
                    String currentUser = (String) PSRequestInfo.getRequestInfo(PSRequestInfo.KEY_USER);

                    if (StringUtils.isNotEmpty(summ.getCheckoutUserName())
                            && !summ.getCheckoutUserName().equals(currentUser)
                            && isLocalContent(id)) {
                        // need to force check in here. CMS-773.
                        forceCheckInLocalContent(loc, currentUser);
                    }

                    PSItemStatus status = contentWs.prepareForEdit(guid);
                    if (status.isDidCheckout()) {
                        //Need to update the parent relationship's dependent revision for the asset
                        widgetAssetRelationshipService.updateLocalRelationshipAsset(id);
                    }
                }
            } catch (PSErrorException e) {
                PSDataItemSummary sum = dataItemSummaryService.find(id);

                //It's ok if the check-out failed.  The check-out user name of the returned item user info will be different
                //than the current user, which will indicate that the current user cannot check the item out.
                log.warn("Failed to check-out item: " + sum.getName(), e);
            }

            IPSRequestContext ctx = securityWs.getRequestContext();
            if (ctx == null) {
                throw new PSItemWorkflowServiceException("Invalid request context for current user thread");
            }


            String currentUser = getUserName();
            int contentId = ((PSLegacyGuid) guid).getContentId();
            PSComponentSummary summary = getItemSummary(contentId);

            return new PSItemUserInfo(summary.getName(), summary.getCheckoutUserName(), currentUser,
                    getAssignmentType(id).getLabel());
        } catch (PSItemWorkflowServiceException | PSDataServiceException e) {
            throw new WebApplicationException(e.getMessage());
        }
    }
    
    @Override
    @GET
    @Path("forceCheckOut/{id}")
    public PSItemUserInfo forceCheckOut(@PathParam("id") String id)
    {
        try {
            rejectIfBlank("forceCheckOut", "id", id);

            checkIn(id, true);
            return checkOut(id);
        } catch (PSItemWorkflowServiceException | PSDataServiceException e) {
            throw new WebApplicationException(e.getMessage());
        }
    }
    
    @Override
    public boolean isModifiableByUser(String id) throws PSValidationException, PSItemWorkflowServiceException {
        rejectIfBlank("isModifiableByUser", "id", id);
        
        PSAssignmentTypeEnum asmtType = getAssignmentType(id);
        return asmtType == PSAssignmentTypeEnum.ASSIGNEE || asmtType == PSAssignmentTypeEnum.ADMIN;
    }
    
    @Override
    public Set<String> getApprovedPages(String id) throws PSValidationException, PSNotFoundException {
        rejectIfBlank("getApprovedPages", "id", id);
        
        return getApprovedPages(id, null);
    }
    
    @Override
    public Set<String> getApprovedPages(String id, String folderPath) throws PSValidationException, PSNotFoundException {
        rejectIfBlank("getApprovedPages", "id", id);
        
        PSSiteSummary resourceSiteSum = null;
        if (folderPath != null)
        {
            notEmpty(folderPath, "folderPath must not be empty");
            
            resourceSiteSum = siteDao.findByPath(folderPath);
            if (resourceSiteSum == null)
            {
                throw new IllegalArgumentException("folderPath must represent a valid site folder path");
            }
        }
        
        Set<String> pages = new HashSet<>();
        Set<String> owners = widgetAssetRelationshipService.getRelationshipOwners(id);
        for (String owner : owners)
        {
            if (workflowHelper.isPage(owner) && workflowHelper.isApproved(owner))
            {
                  if (workflowHelper.isTipRevision(owner) || workflowHelper.isQuickEdit(owner))
                {
                    if (folderPath != null)
                    {
                        try {
                            PSPage page = pageDao.find(owner);

                            // check to see if the page is in the same site as the resource
                            PSSiteSummary pageSiteSum = siteDao.findByPath(page.getFolderPath());
                            if (pageSiteSum.equals(resourceSiteSum)) {
                                pages.add(owner);
                            }
                        } catch (PSDataServiceException e) {
                            log.error(e.getMessage());
                            log.debug(e.getMessage(),e);
                            //continue loop so one bad entry doesn't stop all processing
                        }

                    }
                    else
                    {
                        pages.add(owner);
                    }
                }
            }
            else if (workflowHelper.isAsset(owner))
            {    
                pages.addAll(getApprovedPages(owner, folderPath));                
            }
        }
        
        return pages;
    }

    @GET
    @Path("isCheckedOutToCurrentUser/{id}")
    @Produces(MediaType.TEXT_PLAIN_VALUE)
    public boolean isCheckedOutToCurrentUser(@PathParam("id") String id)
    {
        try {
            rejectIfBlank("isCheckedOutToCurrentUser", "id", id);

            return workflowHelper.isCheckedOutToCurrentUser(id);
        } catch (PSValidationException e) {
            throw new WebApplicationException(e.getMessage());
        }
    }
    
    @GET
    @Path("isApproveAvailableToCurrentUser/{path:.*}")
    public boolean isApproveAvailableToCurrentUser(@PathParam("path") String path)
    {
        try {
            int workflowId;
            if (StringUtils.isEmpty(path))
                workflowId = getWorkflowId(workflowService.getDefaultWorkflowName());
            else {
                if (!StringUtils.startsWith(path, "/"))
                    path = "/" + path;
                try {
                    String folderPath = folderHelper.getFolderPath(path);
                    workflowId = folderHelper.getValidWorkflowId(folderHelper.findFolderProperties(folderHelper.findFolder(folderPath).getId()));
                } catch (Exception e) {
                    throw new PSItemWorkflowServiceException("Cannot determine workflow for folder: " + path, e);
                }
            }
            return workflowHelper.isApproveAvailableToCurrentUser(workflowId);
        } catch (PSItemWorkflowServiceException | PSValidationException e) {
            throw new WebApplicationException(e.getMessage());
        }
    }
    
    public int getWorkflowId(String workflowName) throws PSItemWorkflowServiceException, PSValidationException {
        rejectIfNull("getWorkflowId", "workflowName", workflowName);
        
        PSWorkflow workflow = getWorkflowFromName(workflowName);
        if (workflow == null)
        {
            throw new PSItemWorkflowServiceException("Invalid workflow: " + workflowName);
        }
        
        return workflow.getGUID().getUUID();
    }
    
    public int getStateId(String workflowName, String stateName) throws PSItemWorkflowServiceException
    {
        PSWorkflow workflow = getWorkflowFromName(workflowName);
        if (workflow == null)
        {
            throw new PSItemWorkflowServiceException("Invalid workflow: " + workflowName);
        }
        
        long stateId = -1;
        if (!StringUtils.isBlank(stateName))
        {
           for (PSState state : workflow.getStates())
           {
               if (state.getName().equalsIgnoreCase(stateName))
               {
                   stateId = state.getStateId();
                   break;
               }
           }
        }
        
        return (int) stateId;
    }
    
    public boolean isTriggerAvailable(String id, String trigger) throws PSValidationException {
        rejectIfBlank("isTriggerAvailable", "id", id);
        rejectIfBlank("isTriggerAvailable", "trigger", trigger);
        
        return getTransitions(id).getTransitionTriggers().contains(trigger);
    }
    
    @Override
    public int getLocalContentWorkflowId() throws PSItemWorkflowServiceException {
        if(localContentWorkflowId > 0)
            return localContentWorkflowId;
        
        List<PSObjectSummary> wfs = workflowService.findWorkflowSummariesByName("LocalContent");
        if (wfs.isEmpty())
        {
            throw new PSItemWorkflowServiceException("Failed to find the workflow with name LocalContent");
        }
        return (int) wfs.get(0).getGUID().longValue();
     }
    
    /* (non-Javadoc)
     * @see com.percussion.itemmanagement.service.IPSWorkflowHelper#isModifyAllowed(java.lang.String)
     */
    @Override
    public boolean isModifyAllowed(String id) throws PSValidationException, PSItemWorkflowServiceException {
        notNull(id);    

        PSComponentSummary sum = workflowHelper.getComponentSummary(id);

        if (isEmpty(sum.getCheckoutUserName()))
        {
            return isModifiableByUser(id);
        }

        return isItemCheckedOutToUser(sum);
    }
    
    /* (non-Javadoc)
     * @see com.percussion.itemmanagement.service.IPSItemWorkflowService#isCheckedOutToSomeoneElse(java.lang.String)
     */
    @Override
    public boolean isCheckedOutToSomeoneElse(String id) throws PSValidationException {
        rejectIfBlank("isCheckedOutToSomeoneElse", "id", id);
        
        return workflowHelper.isCheckedOutToSomeoneElse(id);
    }

    /**
     * Gets the workflow for the specified workflow name.
     * 
     * @param workflowName assumed not <code>null</code>.
     * 
     * @return the workflow object, may be <code>null</code> if the workflow could not be found.
     */
    private PSWorkflow getWorkflowFromName(String workflowName)
    {
        PSWorkflow workflow = null;
        
        List<PSWorkflow> workflows = workflowService.findWorkflowsByName(workflowName);
        if (!workflows.isEmpty())
        {
            workflow = workflows.get(0);
        }
        
        return workflow;
    }
    
    /**
     * Gets the highest assignment type of the current user for the specified item in its current state.
     * 
     * @param id the ID of the item, assumed not blank.
     * 
     * @return the assignment type value, never <code>null</code>.
     */
    private PSAssignmentTypeEnum getAssignmentType(String id) throws PSItemWorkflowServiceException {
        PSAssignmentTypeEnum psAssignmentTypeEnum = null;
        try
        {
            List<PSAssignmentTypeEnum> atypes = systemService.getContentAssignmentTypes(asList(idMapper.getGuid(id)));
            if (!atypes.isEmpty()) {
                psAssignmentTypeEnum = atypes.get(0);
            }
        }
        catch (PSSystemException e)
        {
            String msg = "Failed to get assignment type for item id = " + id;
            log.error(msg, e);
            throw new PSItemWorkflowServiceException(msg, e);
        }
        return psAssignmentTypeEnum;
    }
    
   /**
    * Checks if the current item is local content to a page.
    * @param id the ID of the item to check.
    * @return <code>true</code> if item is local content.
    */
    private boolean isLocalContent(String id)
    {
        try {
            log.debug("Determing if item is local content with id: {}", id);
            PSRelationshipFilter filter = new PSRelationshipFilter();
            filter.setName(PSWidgetAssetRelationshipService.LOCAL_ASSET_WIDGET_REL_FILTER);
            filter.limitToOwnerRevision(true);
            filter.setDependentId( (new PSLegacyGuid(id)).getContentId());
            List<PSRelationship> rels = systemWs.loadRelationships(filter);
            return !rels.isEmpty();
        }
        catch (PSErrorException e) {
            // if there is an error determining if content is local, we return false
            // and fall back to previous behavior prior to force checking in local content.
            log.error("Error checking if content with id: " + id + " is local content.", e);
            return false;
        }
    }
    
    /**
     * Transitions all shared, shared linked assets of the specified item, according to the specified trigger.
     * 
     * @param id assumed not blank.
     * 
     * @return list of {@link PSAsset} objects representing assets which could not be transitioned either due to an
     * error or if the items are checked out to a different user.  Never <code>null</code>, may be empty.
     */
    private List<PSDataItemSummary> approveSharedAssets(String id) throws PSDataServiceException, PSNotFoundException {
        Set<String> sharedAssetIds = new HashSet<>();
        sharedAssetIds.addAll(widgetAssetRelationshipService.getSharedAssets(id));
        sharedAssetIds.addAll(widgetAssetRelationshipService.getLinkedAssets(id));
        // build a list of assets which could not be transitioned
        List<PSDataItemSummary> failedAssets = new ArrayList<>();
        Map<String, List<String>> wfStatesMap = getApprovalWorkflowStates();
        
        // transition the shared items
        for (String tid : sharedAssetIds)
        {
            PSDataItemSummary asset = null;
            PSComponentSummary sum = workflowHelper.getComponentSummary(tid);
            List<String> stateIds = wfStatesMap.get(sum.getWorkflowAppId()+"");
            //Skip the assets that don't have approve transition from the state they are in.
            if (!stateIds.contains(sum.getContentStateId() + "")) {
                continue;
            }

             if(recycleService.isInRecycler(tid)){
                 continue;
             }


            if (isAssetTransitionable(tid))
            {
                try
                {
                    PSLegacyGuid guid = (PSLegacyGuid)idMapper.getGuid(tid);
                    transitionItem(guid.getContentId(), TRANSITION_TRIGGER_APPROVE, null, null);
                }
                catch (PSErrorException e)
                {
                    asset = dataItemSummaryService.find(tid);
                }
            }
            else
            {
                asset = dataItemSummaryService.find(tid);
            }
            
            if (asset != null)
            {
                failedAssets.add(asset);
            }
        }

        return failedAssets;
    }
    
    /**
     * Determines if the specified asset can be transitioned or not. 
     * Returns false if user doesn't have the write permissions to the folder.
     * Returns false if the user does not have permission to approval transition from content's current state.
     * 
     * @param id the ID of the asset, assumed not blank
     * @return <code>false</code> if it can be transitioned.
     */
    private boolean isAssetTransitionable(String id) throws PSValidationException {
        // does current user have at least WRITE access to its parent folder?
        IPSGuid folderId = folderHelper.getParentFolderId(idMapper.getGuid(id), false);
        if (folderId != null)
        {
            PSFolderPermission.Access access = folderHelper.getFolderAccessLevel(idMapper.getString(folderId));
            if (access == PSFolderPermission.Access.READ)
                return false;
        }
        
        PSComponentSummary sum = workflowHelper.getComponentSummary(id);

        //Check whether the user through his roles has permission to approve the asset
        if (!isTriggerAvailable(id, IPSItemWorkflowService.TRANSITION_TRIGGER_APPROVE))
        	return false;
        	
        // is the item checked out?
        if (isEmpty(sum.getCheckoutUserName()))
            return true;
        
        // check in the item before transition (next call) if item is checked out by current user
        if (isItemCheckedOutToUser(sum))
        {
            try
            {
                checkIn(id);
                return true;
            }
            catch (Exception e)
            {
                log.warn("Cannot checkin item "+id);
            }
        }
        // the item must be checked out by other user or failed to checkin
        return false;
    }
    
    /**
     * Helper method to get a map of workflow ids and list of state ids that have approve transition. 
     * @return Map workflow id and list of state id map nver <code>null</code> may be empty.
     */
    private Map<String, List<String>> getApprovalWorkflowStates()
    {
    	Map<String, List<String>> wfMap = new HashMap<>();
    	List<PSWorkflow> workflows = workflowService.findWorkflowsByName("");
        for (PSWorkflow psWorkflow : workflows) 
    	{
			List<String> statesList = new ArrayList<>();
			wfMap.put(psWorkflow.getGUID().getUUID() + "", statesList);
			for (PSState psState : psWorkflow.getStates()) 
			{
				List<PSTransition> trans = psState.getTransitions();
				for (PSTransition psTransition : trans) 
				{
					if(psTransition.getName().equalsIgnoreCase(TRANSITION_TRIGGER_APPROVE))
					{
						statesList.add(psState.getGUID().getUUID() + "");
						break;
					}
				}
			}
		}
    	return wfMap;
    }
    

    /**
     * Logger for this service.
     */
    public static final Logger log = LogManager.getLogger(PSItemWorkflowService.class);
    
    /**
     * Member variable that stores the local content workflow id initialized in the first call to method getLocalContentWorkflowId. 
     */
    private int localContentWorkflowId = -1;

    @Override
    @POST
    @Consumes(MediaType.APPLICATION_JSON_VALUE)
    @Produces(MediaType.APPLICATION_JSON_VALUE)
    @Path("/bulkApprove")
    public String bulkApprove(PSApprovableItems items)
    {
        try {
            // Execute import job
            long jobId = asyncJobService.startJob(BULK_APPROVAL_JOB_BEAN, items);
            return "" + jobId;
        } catch (IPSFolderService.PSWorkflowNotFoundException e) {
            throw new WebApplicationException(e.getMessage());
        }
    }

    @Override
    @GET
    @Consumes(MediaType.APPLICATION_JSON_VALUE)
    @Produces(MediaType.APPLICATION_JSON_VALUE)
    @Path("/bulkApprove/status/full/{jobId}")
    public PSBulkApprovalJobStatus getApprovalStatusFull(@PathParam("jobId") String jobId)
    {
        return getBulkApproveStatus(jobId, true);
    }
    
    @Override
    @GET
    @Consumes(MediaType.APPLICATION_JSON_VALUE)
    @Produces(MediaType.APPLICATION_JSON_VALUE)
    @Path("/bulkApprove/status/processed/{jobId}")
    public PSBulkApprovalJobStatus getApprovalStatusProcessed(@PathParam("jobId") String jobId)
    {
        return getBulkApproveStatus(jobId, false);
    }

    /**
     * Gets the status of the bulk approval job, recognized by the supplied jobId.
     * @param jobId must not be <code>null</code>
     * @param isFull whether returned result requires all items or only processed items, if <code>true</code>
     * returned object will have all items set, if false only processed items set.
     * @return approval job status never <code>null</code>
     */
    public PSBulkApprovalJobStatus getBulkApproveStatus(String jobId, boolean isFull)
    {
        PSBulkApprovalJobStatus approvalJob = new PSBulkApprovalJobStatus();
        if(!StringUtils.isNumeric(jobId))
            throw new IllegalArgumentException("jobId must be a number.");
        long job = Long.parseLong(jobId);
        approvalJob.setJobId(job);  
        
        PSAsyncJobStatus jobStatus = asyncJobService.getJobStatus(job);
        if (jobStatus == null)
        {
            approvalJob.setStatus("JOBNOTFOUND");
        }
        else
        {
            PSApprovableItems jobResult = (PSApprovableItems) asyncJobService.getJobResult(job);
            
            PSApprovableItems items = new PSApprovableItems();
            
            if(isFull)
                items.setApprovableItems(jobResult.getApprovableItems());
            else
                items.setProcessedItems(jobResult.getProcessedItems());
            
            items.setErrors(jobResult.getErrors());
            
            approvalJob.setItems(items);
            approvalJob.setJobId(job);
            
            if (jobStatus.getStatus().equals(IPSAsyncJob.COMPLETE_STATUS))
            {
                approvalJob.setStatus("COMPLETED");
            }
            else if (jobStatus.getStatus().equals(IPSAsyncJob.ABORT_STATUS))
            {
                approvalJob.setStatus("FAILED");
            }
            else
            {
                approvalJob.setStatus("PROCESSING");
            }
        }
        return approvalJob;
    }

    
    @Override
    public boolean isStagingOptionAvailable(String id) throws PSValidationException, IPSGenericDao.LoadException {
        boolean result = false;
        PSComponentSummary sum = workflowHelper.getComponentSummary(id);
        if(workflowHelper.isItemInStagingState(sum.getContentId()))
        {
            int wfId = sum.getWorkflowAppId();
            List<String> stgRoles = workflowHelper.getStagingRoles(wfId);
            List<String> userRoles = getUserRoles();
            stgRoles.retainAll(userRoles);
            result = !stgRoles.isEmpty();
        }
        return result;
    }
    
    @Override
    public boolean isRemoveFromStagingOptionAvailable(String id) throws PSValidationException, IPSGenericDao.LoadException {
        boolean result = false;
        PSComponentSummary sum = workflowHelper.getComponentSummary(id);
        if(workflowHelper.isArchived(id))
        {
            int wfId = sum.getWorkflowAppId();
            List<String> stgRoles = workflowHelper.getStagingRoles(wfId);
            List<String> userRoles = getUserRoles();
            stgRoles.retainAll(userRoles);
            result = !stgRoles.isEmpty();
        }
        return result;
    }
    
    
}
