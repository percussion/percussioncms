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

import static java.text.MessageFormat.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.Validate.notNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.assetmanagement.service.IPSWidgetAssetRelationshipService;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSInvalidContentTypeException;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.extension.IPSExtension;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSWorkFlowContext;
import com.percussion.itemmanagement.service.IPSWorkflowHelper;
import com.percussion.itemmanagement.service.impl.PSAbstractWorkflowExtension.WorkflowItem.AssetType;
import com.percussion.itemmanagement.service.impl.PSAbstractWorkflowExtension.WorkflowItem.ItemStatus;
import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.service.IPSPageService;
import com.percussion.pubserver.IPSPubServerService;
import com.percussion.rx.publisher.IPSEditionTaskStatusCallback;
import com.percussion.services.contentchange.IPSContentChangeService;
import com.percussion.services.contentchange.PSContentChangeServiceLocator;
import com.percussion.services.contentchange.data.PSContentChangeEvent;
import com.percussion.services.contentchange.data.PSContentChangeType;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.publisher.IPSEdition;
import com.percussion.services.publisher.IPSPubItemStatus;
import com.percussion.services.publisher.IPSSiteItem.Operation;
import com.percussion.services.publisher.IPSSiteItem.Status;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.workflow.data.PSState;
import com.percussion.services.workflow.data.PSWorkflow;
import com.percussion.share.dao.IPSFolderHelper;
import com.percussion.share.service.IPSIdMapper;
import com.percussion.share.spring.PSSpringWebApplicationContextUtils;
import com.percussion.sitemanage.task.impl.PSWorkflowEditionTask;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSWebserviceUtils;
import com.percussion.webservices.publishing.IPSPublishingWs;
import com.percussion.webservices.system.IPSSystemWs;

/**
 * 
 * An abstract extension that allows you to workflow items
 * into the live state on publish (post edition task extension) and lock local content
 * when the page is moved to pending state (workflow action extension). 
 * 
 * @author adamgent
 *
 */
public abstract class PSAbstractWorkflowExtension implements IPSExtension
{

    private IPSGuidManager guidManager;
    private IPSSystemWs systemWs;
    private IPSIdMapper idMapper;
    private IPSWidgetAssetRelationshipService widgetAssetRelationshipService;
    private IPSPageService pageService;
    private PSItemDefManager itemDefManager;
    private IPSCmsObjectMgr cmsObjectManager;
    private IPSWorkflowHelper workflowHelper;
    private IPSFolderHelper folderHelper;
    private IPSPublishingWs publishingWs;
    private IPSContentChangeService contentChangeService;
    private IPSPubServerService pubServerService;
    
    /**
     * Parameter name for what state to workflow from.
     * Parameters come from the extension manager.
     */
    public static final String STATE_PARAMETER = "state";
    
    /**
     * Parameter name for what trigger to execute.
     */
    public static final String TRIGGER_PARAMETER = "trigger";

    
    protected WorkflowItemWorker getWorker(Map<String, String> params) {
        return new WorkflowItemWorker(params);
    }
    
    protected void setSecurity() {
        PSWebserviceUtils.setUserName("rxserver");
    }
    
    protected void setSecurity(String user){
        PSWebserviceUtils.setUserName(user);
    }
    
    protected String getUser(){
        return PSWebserviceUtils.getUserName();
    }
        
    
    /**
     * 
     * Hold the item and workflow state.
     * 
     * @author adamgent
     *
     */
    public static class WorkflowItem {
        
        public IPSGuid guid;
        public String state;
        public String workflow;
        public String checkedOutUserName;
        public Integer publicRevision = -1;
        public boolean publishable = false;
        public PSComponentSummary itemSummary;
        public boolean revisionLock;
        public Exception error;
        public String type;
        public AssetType assetType = null;
        public ItemStatus  status = ItemStatus.STARTED;
        
        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
        }
        
        public static enum AssetType {
            PAGE,RESOURCE,LOCAL,SHARED;
        }
        
        /**
         * 
         * Status of the workflow item.
         * @author adamgent
         *
         */
        public static enum ItemStatus {
            /**
             * Started workflowing.
             */
            STARTED, 
            /**
             * Finished workflowing successfully.
             */
            PROCESSED,
            /**
             * Item has already been workflowed.
             */
            IGNORED, 
            /**
             * Failed to workflow.
             */
            FAILED;
        }
        
        public static boolean passedStartPublishDate(PSComponentSummary summary)
        {
            if (summary.getContentStartDate() == null)
                return true;
            
            Date currentDate = new Date();
            return (summary.getContentStartDate().getTime() <= currentDate.getTime());
        }
    }
    
    /**
     * Holds transient data that only applies to a single
     * call to the method "perform".
     * <p>
     * This is needed because extensions are singletons.
     * 
     * @author adamgent
     * @see PSWorkflowEditionTask#perform(IPSEdition, IPSSite, Date, Date, long, long, boolean, Map, IPSEditionTaskStatusCallback)
     *
     */
    public class WorkflowItemWorker
    {
        private Map<String, String> params;
        // used to keep track of template ids that have already been searched for shared assets
        private final HashSet<String> seenIds = new HashSet<String>();
        
        /**
         * @see #isAlreadyWorkflowed(WorkflowItem)
         */
        private Set<IPSGuid> successfullyWorkflowedIds = new HashSet<IPSGuid>();
        
        public void processItem(IPSPubItemStatus item)
        {
            processItem(item, null, false);
        }
        
        /**
         * Process the published item.
         * @param item never <code>null</code>.
         */
        public boolean processItem(IPSPubItemStatus item, IPSSite site, boolean isDefaultPubServer)
        {
            notNull(item, "item cannot be null");
            boolean skipped = false;
            Status s = item.getStatus();
            Operation o = item.getOperation();
            if (log.isDebugEnabled())
                log.debug(format("Item id: {2}, Operation:{0}, Status:{1}", o, s, item.getContentId()));
            if (o == Operation.PUBLISH && s == Status.SUCCESS)
            {
                skipped = workflowItemIfPossible(item, site, isDefaultPubServer);
            }
            else
            {
                    // Only set on debug confusing warning to user otherwise.
                    if (s != Status.SUCCESS)
                        log.warn(format("Not workflowing item:{0} because Message:{3} Operation:{1}, Status{2}", 
                            item.getContentId(), o,s, item.getMessage()));
                    else
                        log.debug(format("Not workflowing item:{0} because Message:{3} Operation:{1}, Status{2}", 
                            item.getContentId(), o,s, item.getMessage()));
            }
            
            return skipped;
        }
        
        
        /**
         * Process the workflow context.
         * Called from a workflow action.
         * @param workflowContext never <code>null</code>.
         */
        public void processItem(IPSWorkFlowContext workflowContext)
        {
            notNull(workflowContext, "workflowContext cannot be null");
            IPSGuid guid = getId(workflowContext.getContentID(), null);
            WorkflowItem wfItem = getWorkflowItem(guid);
            if (wfItem.assetType == AssetType.PAGE) {
                List<WorkflowItem> localAssets = getLocalAssetWorkflowItems(wfItem);
                lockContentIfPossible(localAssets);
            }
        }
        
        
        
        public WorkflowItemWorker(Map<String, String> params)
        {
            super();
            this.params = params;
            notNull(params);
        }

        /**
         * Workflows the item if possible. Exceptions will be logged but not
         * thrown. An exception should never be thrown from this method or else
         * the other items in this publish job will not be workflowed.
         * 
         * @param item never <code>null</code>.
         */
        protected boolean workflowItemIfPossible(IPSPubItemStatus item, IPSSite site, boolean isDefaultPubServer)
        {
            notNull(item, "item");

            try
            {
                WorkflowItem wfItem = getWorkflowItem(item);
                
                //Skip the workflow transition if the item published does not match the current revision to cover scheduled publish case.
                if(item.getRevisionId() != wfItem.itemSummary.getCurrentLocator().getRevision()){
                    if (log.isDebugEnabled())
                        log.debug("Item is most likely scheduled for a future publish: " + wfItem);
                    wfItem.status = ItemStatus.IGNORED;
                    return true;
                }
                
                if (isAlreadyWorkflowed(wfItem)) { 
                    if (log.isDebugEnabled())
                        log.debug("Item is already workflowed: " + wfItem);
                    wfItem.status = ItemStatus.IGNORED;
                    return true;
                }

                
                /*
                 * Workflow resources or pages only.
                 */
                boolean success = false;
                if (wfItem.assetType == AssetType.PAGE || 
                        wfItem.assetType == AssetType.RESOURCE) {
                    success = transitionIfPossible(singletonList(wfItem), null, site, isDefaultPubServer);
                }
                
                /*
                 * If the item is a page we need to workflow its
                 * related shared assets and lock its local content.
                 */
                if (success && wfItem.assetType == AssetType.PAGE) {
                    List<WorkflowItem> localAssets = getLocalAssetWorkflowItems(wfItem);
                    List<WorkflowItem> sharedAssets = getSharedAssetWorkflowItems(wfItem);

                    /*
                     * Revision lock the local content
                     */
                    success = lockContentIfPossible(localAssets);
                    /*
                     * Transition the shared content.
                     */
                    success = transitionIfPossible(sharedAssets, wfItem, site, isDefaultPubServer) && success;
                }
                if (success) {
                    addToSuccessfulItems(wfItem);
                    wfItem.status = ItemStatus.PROCESSED;
                }
                else
                    wfItem.status = ItemStatus.FAILED;
            }
            catch (Exception e)
            {
                log.debug("Failed to workflow item (may have been updated by a publish now): " + item, e);
            }
            
            return false;
        }
        
        /**
         * Adds the item to the successfully workflowed items.
         * @param wfItem never <code>null</code>.
         * @see #isAlreadyWorkflowed(WorkflowItem)
         */
        protected void addToSuccessfulItems(WorkflowItem wfItem) {
            if (wfItem.guid != null) {
                successfullyWorkflowedIds.add(wfItem.guid);
            }
        
        }
        
        /**
         * Sees  if the item has already been successfully workflowed.
         * @param wfItem never <code>null</code>.
         * @return never <code>null</code>.
         * @see #addToSuccessfulItems(WorkflowItem)
         */
        protected boolean isAlreadyWorkflowed(WorkflowItem wfItem) {
            return wfItem.guid != null && successfullyWorkflowedIds.contains(wfItem.guid);
        }
        
        protected boolean lockContentIfPossible(List<WorkflowItem> items) {
            boolean success = true;
            for(WorkflowItem wfItem : items) {
                try {
                    if( ! wfItem.revisionLock ) {
                        wfItem.itemSummary.setRevisionLock(true);
                        getCmsObjectManager().saveComponentSummaries(singletonList(wfItem.itemSummary));
                        wfItem.revisionLock = true;
                        wfItem.status = ItemStatus.PROCESSED;
                    }
                }
                catch (Exception e) {
                    handleError(wfItem, "lock content", e);
                    wfItem.status = ItemStatus.FAILED;
                    success = false;
                }
            }
            return success;
        }
        
        protected void handleError(WorkflowItem wfItem, String action, Exception e) {
            log.error("Failed to " + action + " item: " + wfItem, e);
            wfItem.error = e;
            wfItem.status = ItemStatus.FAILED;
        }
        
        /**
         * Tries to transition the workflow items passed in the parameters  
         * 
         * @param items - the list of items. Never <code> null </code>. May be empty.
         * @param pageWfItem - the workflow item for the page. This is used to perform the allowed sites check.
         * May be <code> null </code> if the workflow item passed is the page itself and not its shared assets. 
         * @return true if all the assets where correctly transitioned, false if at least
         * one of the was not correctly transitioned.  
         */
        protected boolean transitionIfPossible(List<WorkflowItem> items, WorkflowItem pageWfItem, IPSSite site, boolean isDefaultPubServer) {
            boolean success = true;
            for (WorkflowItem wfItem : items) {
                try
                {
                    if (isAlreadyWorkflowed(wfItem)) {
                        wfItem.status = ItemStatus.IGNORED;
                        if (log.isDebugEnabled())
                            log.debug("Did not workflow item: " + wfItem + " because its already been workflowed.");
                        return success;
                    }
                    
                    if (isValidWorkflowState(wfItem))
                    {
                        if (pageWfItem != null && !isAllowedSitePublishable(wfItem, pageWfItem))
                        {
                            wfItem.status = ItemStatus.IGNORED;
                            if (log.isDebugEnabled())
                                log.debug("Did not workflow item: " + wfItem + " because its not allowed for the current site.");
                            
                        }
                        else
                        {
                            if (log.isDebugEnabled())
                                log.debug("Attempting to transition item: " + wfItem);
                            transition(wfItem);
                            addToSuccessfulItems(wfItem);
                        }
                    }
                    else
                    {
                        log.debug("Did not workflow item: " + wfItem.guid + " because its not in a valid state");
                        wfItem.status = ItemStatus.FAILED;
                        success = false;
                    }
                    
                    if (site != null && isDefaultPubServer)
                    {
                        contentChangeService.deleteChangeEvents(site.getSiteId(), wfItem.itemSummary.getContentId(), PSContentChangeType.PENDING_LIVE);
                    }
                }
                catch (Exception e)
                {
                    handleError(wfItem, "workflow", e);
                    success = false;
                }
            }
            return success;
            
        }
        
        /**
         * Checks if the asset workflow item's root level folder has the current site allowed to publish.
         * If it's not then the asset shouldn't be transitioned to live (since it was not included in the page's code) 
         * 
         * @author federicoromanelli
         * @param item - the asset workflow item to perform the check. Never <code> null </code>
         * @param pageWfItem - the page workflow item used to retrieve the site id. Never <code> null </code>
         * @return true if the asset could be transitioned (meaning its root level folder has the site included as allowed sites)
         */
        private boolean isAllowedSitePublishable (WorkflowItem item, WorkflowItem pageWfItem)
        {
            Long siteId = getPageSiteLegacyId(pageWfItem);
            String rootLevelFolderAllowedSites = folderHelper.getRootLevelFolderAllowedSitesPropertyValue(item.guid.toString());
            if (rootLevelFolderAllowedSites != null)
            {
                if (rootLevelFolderAllowedSites.contains(String.valueOf(siteId)))
                {
                    return true;
                }
                else
                {
                    return false;
                }
            }
            return true;
        }
        
        /**
         * Create a workflow item.
         * @param item never <code>null</code>.
         * @return never <code>null</code>.
         */
        protected WorkflowItem getWorkflowItem(IPSPubItemStatus item) {
            notNull(item, "item cannot be null");
            IPSGuid id = getId(item.getContentId(), item.getRevisionId());
            WorkflowItem wfItem = getWorkflowItem(id);
            if ( wfItem.assetType == null || wfItem.assetType == AssetType.SHARED) {
                wfItem.assetType = AssetType.RESOURCE;
            }
            return wfItem;
        }
        
        public IPSGuid makeGuidFromRevision(IPSGuid guid, Integer revision) {
            notNull(revision);
            Integer contentId = guid.getUUID();
            PSLocator locator = new PSLocator(contentId, revision);
            return guidManager.makeGuid(locator);
        }
        
        public WorkflowItem getWorkflowItem(IPSGuid contentId) {
        	return getWorkflowItem(contentId,false);
        }
        public WorkflowItem getWorkflowItem(IPSGuid contentId, boolean isStaging) {
            PSLocator locator = getGuidManager().makeLocator(contentId);
            PSComponentSummary summary = PSWebserviceUtils.getItemSummary(locator.getId());

            //If this is a folder or an item with an invalid workflow, it is not publishable.
            if(!(summary.getWorkflowAppId() > 0)){
                log.warn("Item " + summary.getContentId() + " is not Workflowable - it will be skipped.");
                return null;
            }

            PSWorkflow workflow = PSWebserviceUtils.getWorkflow(summary.getWorkflowAppId());
            PSState state = PSWebserviceUtils.getStateById(workflow, summary.getContentStateId());
            WorkflowItem wfItem = new WorkflowItem();
            wfItem.guid = contentId;
            wfItem.state = state.getName();
            wfItem.workflow = workflow.getName();
            wfItem.checkedOutUserName = summary.getCheckoutUserName();
            wfItem.revisionLock = summary.isRevisionLock();
            wfItem.itemSummary = summary;
            wfItem.publishable = isPublishable(summary, isStaging);
            wfItem.publicRevision = getPublishRevision(summary, wfItem.publishable, isStaging);
            try
            {
                wfItem.type = itemDefManager.contentTypeIdToName(summary.getContentTypeId());
            }
            catch (PSInvalidContentTypeException e)
            {
                throw new RuntimeException(e);
            }
            if ( IPSPageService.PAGE_CONTENT_TYPE.equals(wfItem.type)) {
                wfItem.assetType = AssetType.PAGE;
            }
            else if (PSWorkflowHelper.LOCAL_WORKFLOW_NAME.equals(wfItem.workflow)) {
                wfItem.assetType = AssetType.LOCAL;
            }
            else {
                wfItem.assetType = AssetType.SHARED;
            }
            
            return wfItem;
        }
        
        private int getPublishRevision(PSComponentSummary summary, boolean isPublishable, boolean isStaging)
        {
            if(isStaging){
            	return summary.getCurrentLocator().getRevision();
            }
            else if (workflowHelper.isItemInApproveState(summary.getContentId())
                    && WorkflowItem.passedStartPublishDate(summary))
            {
                // publish current revision, this is because the
                // last-public-revision may be < current-revision
                return summary.getCurrentLocator().getRevision();
            }

            return summary.getPublicOrCurrentRevision();
        }

        private boolean isPublishable(PSComponentSummary summary, boolean isStaging)
        {
            boolean result = false;
            if(isStaging){
            	result = workflowHelper.isItemInStagingState(summary.getContentId());
            }
            else{
            	if (summary.getPublicRevision() != -1)
            		return true;
            	result = workflowHelper.isItemInApproveState(summary.getContentId());
            }

            return result;
        }
        
        /**
         * Create a workflow items for the local assets of a page.
         * @param page never <code>null</code>.
         * @return never <code>null</code>.
         */
        protected List<WorkflowItem> getLocalAssetWorkflowItems(WorkflowItem page) {
            IPSGuid guid = page.guid;
            String newId = idMapper.getString(guid);
            Set<String> ids =  widgetAssetRelationshipService.getLocalAssets(newId);
            return loadWorkflowItems(ids, AssetType.LOCAL);
        }
        
        /**
         * Helper method to get the site's legacy id giving a page workflow id
         * 
         * @author federicoromanelli
         * @param page - the page's workflow item to retrieve the guid. Never <code> null </code>
         * @return - the legacy id of the site the page is contained 
         */
        private Long getPageSiteLegacyId (WorkflowItem page)
        {
            IPSGuid guid = page.guid;
            IPSSite site = publishingWs.getItemSites(guid).get(0);
            return site.getSiteId();
        }
        
        private List<WorkflowItem> loadWorkflowItems(Collection<String> ids, AssetType assetType) {
            List<WorkflowItem> items = new ArrayList<WorkflowItem>();
            for (String id : ids) {
                IPSGuid guid = getIdMapper().getGuid(id);
                WorkflowItem item = getWorkflowItem(guid);
                item.assetType = assetType;
                items.add(item);
            }
            return items;
        }
        
        /**
         * Create a list of workflow items that correspond to the shared assets.
         * @param page never <code>null</code>.
         * @return never <code>null</code>.
         */
        protected List<WorkflowItem> getSharedAssetWorkflowItems(WorkflowItem page) {
            IPSGuid guid = page.guid;
            String newId = getIdMapper().getString(guid);
            Set<String> ids =  getWidgetAssetRelationshipService().getSharedAssets(newId);
            
            PSPage pageItem = pageService.find(newId);
            String templateId = pageItem.getTemplateId();
            
            // if the templateId hasn't been seen, we check to see if the template is an owner of any shared assets
            if (!seenIds.contains(templateId)) {
                 Set<String> ids_temp = getWidgetAssetRelationshipService().getSharedAssets(templateId);
                 ids.addAll(ids_temp);
                 seenIds.add(templateId);
            }

            // remove already processed IDs and add those to seenIds so they don't get processed again
            ids.removeAll(seenIds);
            seenIds.addAll(ids);
            return loadWorkflowItems(ids, AssetType.SHARED);
        }

        /**
         * Workflow the item to live.
         * <p>
         * This method is safe to override by subclasses.
         * @param item Never <code>null</code>
         */
        protected void transition(WorkflowItem item) {
            notNull(item, "item should not be null");
            notNull(item.guid, "Item should have a guid");
            notNull(getTrigger(), "trigger not set in parameters");
            notNull(item.itemSummary, "Item summary should be set");
            getSystemWs().transitionItems(asList(item.guid), getTrigger());
            getWorkflowHelper().transitionRelatedNavigationItem(item.guid, getTrigger());
        }
        

        /**
         * 
         * @return <code>true</code> if the item valid to transition.
         */
        protected boolean isValidWorkflowState(WorkflowItem workflowItem)
        {

            notNull(getState(), "state was not set in parameters");
            return (getState().equals(workflowItem.state) && isBlank(workflowItem.checkedOutUserName));
        }

        protected IPSGuid getId(Integer contentId, Integer revision) {
            revision = revision == null ? -1 : revision;
            return getGuidManager().makeGuid(new PSLocator(contentId, revision));
        }

        /**
         * The name of the state that the item must be in if it is to be
         * workflowed.
         * 
         * @return never <code>null</code>.
         */
        public String getState()
        {
            return params.get(STATE_PARAMETER);
        }


        /**
         * The trigger name used to transition the item if its in the right
         * workflow state.
         * 
         * @return never <code>null</code>.
         */
        public String getTrigger()
        {
            return params.get(TRIGGER_PARAMETER);
        }

    }
    
    @Override
    public void init(@SuppressWarnings("unused") IPSExtensionDef def, @SuppressWarnings("unused") File file)
    {
        PSSpringWebApplicationContextUtils.injectDependencies(this);
    }

    public IPSSystemWs getSystemWs()
    {
        return systemWs;
    }

    public void setSystemWs(IPSSystemWs systemWs)
    {
        this.systemWs = systemWs;
    }
    

    public IPSGuidManager getGuidManager()
    {
        return guidManager;
    }

    public void setGuidManager(IPSGuidManager guidManager)
    {
        this.guidManager = guidManager;
    }

    public IPSIdMapper getIdMapper()
    {
        return idMapper;
    }

    public void setIdMapper(IPSIdMapper idMapper)
    {
        this.idMapper = idMapper;
    }

    public IPSWidgetAssetRelationshipService getWidgetAssetRelationshipService()
    {
        return widgetAssetRelationshipService;
    }

    public void setWidgetAssetRelationshipService(IPSWidgetAssetRelationshipService widgetAssetRelationshipService)
    {
        this.widgetAssetRelationshipService = widgetAssetRelationshipService;
    }

    public IPSPageService getPageService()
    {
        return pageService;
    }

    public void setPageService(IPSPageService pageService)
    {
        this.pageService = pageService;
    }

    public PSItemDefManager getItemDefManager()
    {
        return itemDefManager;
    }

    public void setItemDefManager(PSItemDefManager itemDefManager)
    {
        this.itemDefManager = itemDefManager;
    }

    public IPSCmsObjectMgr getCmsObjectManager()
    {
        return cmsObjectManager;
    }

    public void setCmsObjectManager(IPSCmsObjectMgr cmsObjectManager)
    {
        this.cmsObjectManager = cmsObjectManager;
    }

    
    protected IPSWorkflowHelper getWorkflowHelper()
    {
        return workflowHelper;
    }

    public void setWorkflowHelper(IPSWorkflowHelper workflowHelper)
    {
        this.workflowHelper = workflowHelper;
    }

    public void setFolderHelper(IPSFolderHelper folderHelper)
    {
        this.folderHelper = folderHelper;
    }

    public IPSFolderHelper getFolderHelper()
    {
        return folderHelper;
    }
    
    public IPSPublishingWs getPublishingWs()
    {
        return publishingWs;
    }

    public void setPublishingWs(IPSPublishingWs publishingWs)
    {
        this.publishingWs = publishingWs;
    }    
    

    public void setContentChangeService(IPSContentChangeService contentChangeService)
    {
        this.contentChangeService = contentChangeService;
    }


    public IPSPubServerService getPubServerService()
    {
        return pubServerService;
    }

    public void setPubServerService(IPSPubServerService pubServerService)
    {
        this.pubServerService = pubServerService;
    }    
    

    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    protected final Log log = LogFactory.getLog(getClass());

}

