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
package com.percussion.foldermanagement.service.impl;

import com.percussion.cms.objectstore.PSInvalidContentTypeException;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.foldermanagement.data.PSFolderItem;
import com.percussion.foldermanagement.data.PSGetAssignedFoldersJobStatus;
import com.percussion.foldermanagement.data.PSWorkflowAssignment;
import com.percussion.foldermanagement.service.IPSFolderService;
import com.percussion.pagemanagement.service.IPSPageService;
import com.percussion.pathmanagement.data.PSFolderProperties;
import com.percussion.pathmanagement.data.PSPathItem;
import com.percussion.pathmanagement.service.IPSPathService;
import com.percussion.server.PSRequest;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.content.data.PSItemSummary;
import com.percussion.services.content.data.PSItemSummary.ObjectTypeEnum;
import com.percussion.services.guidmgr.PSGuidUtils;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.services.workflow.IPSWorkflowService;
import com.percussion.services.workflow.data.PSWorkflow;
import com.percussion.share.async.IPSAsyncJobService;
import com.percussion.share.async.PSAsyncJobStatus;
import com.percussion.share.dao.IPSFolderHelper;
import com.percussion.share.data.PSLightWeightObject;
import com.percussion.share.service.IPSIdMapper;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.sitemanage.service.IPSSiteSectionMetaDataService;
import com.percussion.utils.request.PSRequestInfo;
import com.percussion.utils.thread.PSThreadUtils;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.workflow.service.IPSSteppedWorkflowMetadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * Service class to handle the associations of sites or assets folders and
 * workflows. It provides methods to query and to modify the workflow associated
 * with sites and folders.
 * 
 * @author miltonpividori
 * 
 */
@Component("folderService")
public class PSFolderService implements IPSFolderService
{
    private static final String ASSIGNMENT_IN_PROGRESS = "We are unable to process your request at this time.  A similar request has already been submitted and is still processing in the background.  Please try again later.";
    
    private static final Log log = LogFactory.getLog(PSFolderService.class);
    
    private static AtomicInteger assignmentOperationCount = new AtomicInteger();

    private IPSFolderHelper folderHelper;

    private IPSPathService pathService;

    private IPSSiteManager siteMgr;

    private IPSWorkflowService workflowService;
    
    private IPSCmsObjectMgr cmsObjectManager;
    
    private IPSContentWs contentWs;
    
    private IPSSteppedWorkflowMetadata steppedWfMetadata;
    
    private IPSIdMapper idMapper;
    
    private IPSAsyncJobService asyncJobService;
    
    /**
     * The item definition manager, initialized by constructor.
     */
    private PSItemDefManager itemDefManager;
    
    @Autowired
    public PSFolderService(IPSFolderHelper folderHelper, @Qualifier("pathService") IPSPathService pathService, IPSSiteManager siteMgr,
            IPSWorkflowService workflowService, IPSCmsObjectMgr cmsObjectManager, IPSContentWs contentWs, IPSSteppedWorkflowMetadata steppedWfMetadata, IPSIdMapper idMapper, 
            IPSAsyncJobService asyncJobService, PSItemDefManager itemDefManager)
    {
        this.folderHelper = folderHelper;
        this.pathService = pathService;
        this.siteMgr = siteMgr;
        this.workflowService = workflowService;
        this.cmsObjectManager = cmsObjectManager;
        this.contentWs = contentWs;
        this.steppedWfMetadata = steppedWfMetadata;
        this.idMapper = idMapper;
        this.asyncJobService = asyncJobService;
        this.itemDefManager = itemDefManager;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.percussion.foldermanagement.service.IPSFolderService#getAssignedFolders
     * (java.lang.String, java.lang.String, boolean)
     */
    @Override
    public List<PSFolderItem> getAssignedFolders(String workflowName, String path,
            boolean includeFoldersWithDifferentWorkflow) throws Exception
    {
        Validate.notEmpty(workflowName, "workflowName cannot be empty");
        Validate.notEmpty(path, "path cannot be empty");
        validateWorkflow(workflowName);

        List<PSFolderItem> folderItems = new ArrayList<>();

        List<PSPathItem> foldersProperties;
        PSFolderTreeInfo fullFolderTreeItem;
        
        PSThreadUtils.checkForInterrupt();
        
        // Get the properties for the given path first
        foldersProperties = getSubfolders(path);

        for (PSPathItem folderProperties : foldersProperties)
        {
            PSThreadUtils.checkForInterrupt();
            fullFolderTreeItem = getFullFolderTree(getFolderItem(folderProperties), workflowName,
                    includeFoldersWithDifferentWorkflow);

            if (!includeFoldersWithDifferentWorkflow)
            {
                // add the child only if it has any children with the given
                // workflow
                if (fullFolderTreeItem.branchAssociatedWithWorkflow)
                {
                    folderItems.add(fullFolderTreeItem.folderItem);
                }
            }
            else
            {
                folderItems.add(fullFolderTreeItem.folderItem);
            }
        }

        return folderItems;
    }
    
    
    
    @Override
    public String startGetAssignedFoldersJob(String workflowName, String path, boolean includeFoldersWithDifferentWorkflow) throws PSWorkflowNotFoundException {
        long jobId = asyncJobService.startJob("getAssignedFoldersJob", new Object[] {workflowName, path, includeFoldersWithDifferentWorkflow});
        return String.valueOf(jobId);
    }

    @Override
    public PSGetAssignedFoldersJobStatus getAssignedFoldersJobStatus(String jobId)
    {
        if(!StringUtils.isNumeric(jobId))
            throw new IllegalArgumentException("jobId must be a number.");
        Long lJob = Long.parseLong(jobId);
        
        PSAsyncJobStatus status = asyncJobService.getJobStatus(lJob);

        // If result is an exception, throw it
        Object result = asyncJobService.getJobResult(lJob);


        // if we have a result, return it
        if (result instanceof PSGetAssignedFoldersJobStatus)
        {
            PSGetAssignedFoldersJobStatus jobStatus = (PSGetAssignedFoldersJobStatus)result;
            jobStatus.setJobId(lJob);
            if(jobStatus.getStatus().equals("-1" )){
                asyncJobService.cancelJob(lJob);
                jobStatus.setMessage("terminated");
            }else{
                jobStatus.setMessage(status.getMessage());
            }
            return jobStatus;
        }

        // return current status
        PSGetAssignedFoldersJobStatus jobStatus = new PSGetAssignedFoldersJobStatus();
        jobStatus.setJobId(lJob);
        jobStatus.setStatus(String.valueOf(status.getStatus()));
        jobStatus.setMessage(status.getMessage());
        return jobStatus;
    }

    

    @Override
    public PSGetAssignedFoldersJobStatus cancelAssignedFoldersJob(String jobId)
    {
        if(!StringUtils.isNumeric(jobId))
            throw new IllegalArgumentException("jobId must be a number.");
        Long lJob = Long.parseLong(jobId);
        
       asyncJobService.cancelJob(lJob);
       
       return getAssignedFoldersJobStatus(jobId);
    }

    public void assignFoldersToWorkflow(PSWorkflowAssignment workflowAssignment) throws PSWorkflowNotFoundException, PSWorkflowAssignmentInProgressException {
        Validate.notNull(workflowAssignment, "workflowAssignment cannot be null");
        Validate.notEmpty(workflowAssignment.getWorkflowName(), "workflowAssignment.workflowName cannot be null");

        // validate and get the workflow
        PSWorkflow workflow = validateWorkflow(workflowAssignment.getWorkflowName());
        
        boolean didSetInProgress = false;
        boolean didStartThread = false;
        try
        {
            // Ensure no other operation is in progress
            if (!incrementAssignmentOperationCount(false))
            {
                throw new PSWorkflowAssignmentInProgressException(ASSIGNMENT_IN_PROGRESS);
            }

            didSetInProgress = true;
            
            assignFoldersToWorkflow(workflowAssignment.getAssignedFolders(), workflowAssignment.getUnassignedFolders(), workflow);

            didStartThread = applyWorkflowToContent(workflowAssignment.getAppliedFolders());
        }
        finally
        {
            if (didSetInProgress && !didStartThread)
            {
                decrementAssignmentOperationCount();
            }
        }
        
    }

    private void decrementAssignmentOperationCount()
    {
        int count = assignmentOperationCount.decrementAndGet();
        if (count < 0)
            assignmentOperationCount.compareAndSet(count, 0);
    }
    
    private boolean incrementAssignmentOperationCount(boolean allowMultiple)
    {
        if (assignmentOperationCount.getAndIncrement() > 0 && !allowMultiple)
        {
            assignmentOperationCount.decrementAndGet();
            return false;
        }
        
        return true;
    }
    

    private void assignFoldersToWorkflow(String[] assignedFolderIds, String[] unassignedFolderIds, PSWorkflow workflow)
    {
        // Assignment
        for (String id : assignedFolderIds)
        {
            try
            {
                // retrieve the folder properties given the path
                PSFolderProperties folderProperties = folderHelper.findFolderProperties(id);

                // set the workflow id to the given folder
                folderProperties.setWorkflowId(workflow.getGUID().getUUID());

                // save the folder properties into the system
                folderHelper.saveFolderProperties(folderProperties);
            }
            catch (Exception e)
            {
                log.error("There was an error assigning the workflow '" + workflow.getName() + "' to the folder with id '"
                        + id + "'. The underlying error was: " + e.getMessage());
            }
        }

        // Unassignment
        for (String id : unassignedFolderIds)
        {
            try
            {
                // retrieve the folder properties given the path
                PSFolderProperties folderProperties = folderHelper.findFolderProperties(id);

                folderProperties.setWorkflowId(Integer.MIN_VALUE);

                // save the folder properties into the system
                folderHelper.saveFolderProperties(folderProperties);
            }
            catch (Exception e)
            {
                log.error("There was an error unassigning the workflow '" + workflow.getName() + "' to the folder '"
                        + id + "'. The underlying error was: " + e.getMessage());
            }
        }
    }
    

    @Override
    public boolean isContentWorkflowAssignmentInProgress()
    {
        return assignmentOperationCount.get() > 0;
    }

    @Override
    public boolean applyWorkflowToContent(String[] folderIds)
    {
        boolean didStartThread = false;
        boolean didIncrementCount = false;
        
        if (folderIds != null && folderIds.length == 0)
            return didStartThread;
        
        final Map<Integer,List<Integer>> workflowsMap = new HashMap<>();
        
        // if no ids supplied, get the list of ids using the default workflow implicitly
        if (folderIds == null)
        {
            List<String> defaultfolderIds = getDefaultWorkflowFolderIds();
            if (defaultfolderIds.isEmpty())
            {
                return didStartThread;
            }
            
            // increment assignment operation count, as it's not managed by caller if folderIds is null
            didIncrementCount = incrementAssignmentOperationCount(true);
            folderIds = defaultfolderIds.toArray(new String[defaultfolderIds.size()]);            
        }
        
        try
        {
            for (String folderId : folderIds)
            {
                try
                {
                    PSFolderProperties folderProperties = folderHelper.findFolderProperties(folderId);
                    int folderContentId = idMapper.getContentId(folderId);
                    int folderWorkflowId = folderHelper.getValidWorkflowId(folderProperties);
                    
                    List<Integer> workflowsList = workflowsMap.get(folderWorkflowId);
                    if (workflowsList == null)
                    {
                        workflowsList = new ArrayList<>();
                        workflowsMap.put(folderWorkflowId,workflowsList);
                    }
                       
                    workflowsList.add(folderContentId);
                }
                catch (Exception e)
                {
                    log.error("There was an error applying the assigned workflow to content in the folder '"
                            + folderId + "'. The underlying error was: " + e.getMessage());
                }
            }

            
            // apply the changes to content asynchronously
            final List<String> systemStateNames = steppedWfMetadata.getSystemStatesList();
            final Map<String, Object> requestInfoMap = PSRequestInfo.copyRequestInfoMap();
            PSRequest request = (PSRequest) requestInfoMap.get(PSRequestInfo.KEY_PSREQUEST);
            requestInfoMap.put(PSRequestInfo.KEY_PSREQUEST, request.cloneRequest());
            Runnable worker = new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        if (PSRequestInfo.isInited()) {
                            PSRequestInfo.resetRequestInfo();
                        }
                        
                        PSRequestInfo.initRequestInfo(requestInfoMap);
                        
                        Iterator<Map.Entry<Integer,List<Integer>>> it = workflowsMap.entrySet().iterator();
                        while (it.hasNext()) 
                        {
                            Map.Entry<Integer, List<Integer>> pairs = it.next();
                            cmsObjectManager.changeWorfklowForItems(pairs.getValue(), pairs.getKey(), systemStateNames);
                        }                        
                    }
                    catch(Exception e)
                    {
                        log.error("There was an error applying the assigned workflow to content in the specified folders. " +
                                "The underlying error was: " + e.getMessage(), e);
                    }
                    finally
                    {
                        assignmentOperationCount.decrementAndGet();
                    }
                }
                
            };
            
            Thread thread = new Thread(worker, "AssignContentToWorkflow");
            thread.setDaemon(true);
            thread.start();
            didStartThread = true;
            
            return didStartThread;
        }
        finally
        {
            if (!didStartThread && didIncrementCount)
                decrementAssignmentOperationCount();
        }
    }

    /**
     * Recursively get all folder ids using the default workflow implicitly (no workflow assigned)
     * 
     * @return A list of ids, not <code>null</code>, may be empty if none found or if an error occurs
     */
    private List<String> getDefaultWorkflowFolderIds()
    {
        List<String> idList = new ArrayList<>();
        try
        {
            idList.addAll(getDefaultWorfklowIdsFromPath("/Sites"));
            idList.addAll(getDefaultWorfklowIdsFromPath("/Assets"));
            return idList;
        }
        catch (Exception e)
        {
            // log the error and return empty list 
            log.error("Error getting all paths using the default workflow.  The underlying error was: " + e.getMessage());
            idList.clear();
            
            return idList;
        }
    }
    
    /**
     * Recursively get all sub-folder ids using the default workflow implicitly (no workflow assigned) for the supplied path
     * .
     * @param path the path to process, each subfolder of the path is evaluated and possibly added to the results, then
     * the method recurses for each subfolder path.  The supplied path is not evaluated.
     * 
     * @return A list of ids, not <code>null</code>, may be empty.
     * 
     * @throws Exception if the folders cannot be loaded 
     */
    private List<String> getDefaultWorfklowIdsFromPath(String path) throws Exception
    {
        List<String> ids = new ArrayList<>();
        
        List<PSPathItem> subPaths = getSubfolders(path);
        for (PSPathItem pathItem : subPaths)
        {
            PSFolderItem folderItem = getFolderItem(pathItem);
            if (StringUtils.isEmpty(folderItem.getWorkflowName()))
                ids.add(folderItem.getId());
            
            ids.addAll(getDefaultWorfklowIds(folderItem.getId()));
        }
        
        return ids;
    }

    private List<String> getDefaultWorfklowIds(String id) throws Exception
    {
        List<String> paths = new ArrayList<>();
        
        List<PSPathItem> subPaths = getSubFoldersWithoutPath(id);
        for (PSPathItem pathItem : subPaths)
        {
            PSFolderItem folderItem = getFolderItem(pathItem);
            if (StringUtils.isEmpty(folderItem.getWorkflowName()))
                paths.add(pathItem.getId());
            
            paths.addAll(getDefaultWorfklowIds(pathItem.getId()));
        }
        
        return paths;
    }
    
    /**
     * Finds all the folders that are children of the given path.
     * 
     * @param path the path to get the children from. Assumed not
     *            <code>null</code>, or empty.
     * @return a list of {@link PSPathItem} objects. May be empty, but never
     *         <code>null</code>
     */
    private List<PSPathItem> getSubfolders(String path) throws IPSPathService.PSPathServiceException, PSDataServiceException {
        List<PSPathItem> children = pathService.findChildren(path);
        List<PSPathItem> subfolders = new ArrayList<>();

        for (PSPathItem item : children)
        {
            if (item.isFolder() || PSPathItem.TYPE_SITE.equals(item.getType()))
                subfolders.add(item);
        }

        return subfolders;
    }
    
    private List<PSPathItem> getSubFoldersWithoutPath(String id)
    {
        List<PSPathItem> subfolders = new ArrayList<>();
        List<PSItemSummary> itemSums = contentWs.findFolderChildren(idMapper.getGuid(id), false);
        for (PSItemSummary sum : itemSums)
        {
            if (sum.getObjectType().equals(ObjectTypeEnum.ITEM))
                continue;
            if (sum.getName().equals(IPSSiteSectionMetaDataService.SECTION_SYSTEM_FOLDER_NAME))
                continue;
            
            PSPathItem childItem = new PSPathItem();
            childItem.setId(sum.getGUID().toString());
            childItem.setName(sum.getName());
            childItem.setType(sum.getContentTypeName());
            subfolders.add(childItem);
        }
        
        // sort by name
        Collections.sort(subfolders, new Comparator<PSPathItem>()
        {

            @Override
            public int compare(PSPathItem p1, PSPathItem p2)
            {
                return p1.getName().compareToIgnoreCase(p2.getName());
            }
        });
        
        return subfolders;
    }

    /**
     * Given a {@link PSFolderItem} object, this method recursively finds all of
     * the children and adds them into that object, building the hierarchy of
     * folders under the given root object.
     * <p>
     * If the <code>includeFoldersWithDifferentWorkflow</code> is set to
     * <code>true</code>, the hierarchy will hold every child folder, even those
     * associated with another workflow.
     * <p>
     * If set to <code>false</code> the folders that are not associated with the
     * given workflow, and have no descendants associated either will not be
     * included. But it will include those folders that are not associated with
     * the workflow but have at least one descendant associated.
     * 
     * @param folderItem the root item from which the hierarchy will be build.
     * @param workflowName the name of the workflow that is being queried.
     * @param includeFoldersWithDifferentWorkflow include or not folders that
     *            are not associated with the given workflow.
     * @return a {@link PSFolderTreeInfo} object, holding the corresponding
     *         {@link PSFolderItem} object and a boolean value that indicates if
     *         there is a node in this branch of the tree associated with the
     *         given workflow.
     * @throws Exception if an error occurs when getting the childs of the path.
     */
    private PSFolderTreeInfo getFullFolderTree(PSFolderItem folderItem, String workflowName,
            Boolean includeFoldersWithDifferentWorkflow) throws Exception
    {
        PSThreadUtils.checkForInterrupt();
        
        boolean branchAssociatedWithWorkflow;

        List<PSPathItem> foldersProperties = getSubFoldersWithoutPath(folderItem.getId());

        if (foldersProperties == null || foldersProperties.isEmpty())
        {
            if (StringUtils.equalsIgnoreCase(folderItem.getWorkflowName(), workflowName))
            {
                branchAssociatedWithWorkflow = true;
            }
            else
            {
                branchAssociatedWithWorkflow = false;
            }

            folderItem.setChildren(null);
            folderItem.setAllChildrenAssociatedWithWorkflow(branchAssociatedWithWorkflow);
            return new PSFolderTreeInfo(folderItem, branchAssociatedWithWorkflow);
        }

        List<PSFolderTreeInfo> children = new ArrayList<>();
        PSFolderTreeInfo childFolderItemWithChildren;
        boolean allChildrenAssociatedWithWorkflow = true;

        // iterate over the children
        for (PSPathItem folderProperties : foldersProperties)
        {
            childFolderItemWithChildren = getFullFolderTree(getFolderItem(folderProperties), workflowName,
                    includeFoldersWithDifferentWorkflow);

            // add the child only if it has any children with the given workflow
            if (!includeFoldersWithDifferentWorkflow)
            {
                if (childFolderItemWithChildren.branchAssociatedWithWorkflow)
                {
                    children.add(childFolderItemWithChildren);
                }
            }
            else
            {
                children.add(childFolderItemWithChildren);
            }

            // allChildrenAssociatedWithWorkflow is false if the current child
            // folder has a different workflow, or the child folder has this
            // property set to false (which means it has some child in a
            // different workflow)
            if (!StringUtils.equalsIgnoreCase(childFolderItemWithChildren.folderItem.getWorkflowName(), workflowName)
                    || (childFolderItemWithChildren.folderItem.getAllChildrenAssociatedWithWorkflow() != null && Boolean.FALSE
                            .equals(childFolderItemWithChildren.folderItem.getAllChildrenAssociatedWithWorkflow())))
                allChildrenAssociatedWithWorkflow = false;
        }

        folderItem.setChildren(PSFolderTreeInfo.getChildrenElements(children));
        folderItem.setAllChildrenAssociatedWithWorkflow(allChildrenAssociatedWithWorkflow);

        // this folder has childs in the associated workflow if there is a
        // children that has, or this folder is associated with the given
        // workflow.
        branchAssociatedWithWorkflow = StringUtils.equalsIgnoreCase(folderItem.getWorkflowName(), workflowName)
                || (!includeFoldersWithDifferentWorkflow && children.size() > 0);

        return new PSFolderTreeInfo(folderItem, branchAssociatedWithWorkflow);
    }

    /**
     * Builds a {@link PSFolderItem} object from a {@link PSPathItem} object.
     * 
     * @param pathItem the path item from which to create the PSFolderItem
     *            object. Assumed not <code>null<code>
     * @return a {@link PSFolderItem} object. Never <code>null</code>
     * @throws Exception if the path requested cannot be found.
     */
    private PSFolderItem getFolderItem(PSPathItem pathItem) throws Exception
    {
        String id = pathItem.getId();

        PSFolderProperties folderProperties = null;

        if (PSPathItem.TYPE_SITE.equals(pathItem.getType()))
        {
            // The site is not stored as content items, so we need to get the
            // root folder of it
            IPSSite site = siteMgr.loadSite(pathItem.getName());
            id = folderHelper.findFolder(site.getFolderRoot()).getId();
        }
        
        
        PSFolderItem folder = new PSFolderItem();
        folder.setName(pathItem.getName());
        folder.setId(id);

        folderProperties = folderHelper.findFolderProperties(id);
        if (folderProperties.getWorkflowId() > 0)
        {
            PSWorkflow workflow = workflowService.loadWorkflow(PSGuidUtils.makeGuid(folderProperties.getWorkflowId(),
                    PSTypeEnum.WORKFLOW));

            if (workflow != null)
                folder.setWorkflowName(workflow.getName());
            else
                log.debug("The workflow ID associated with the folder '" + pathItem.getPath()
                        + "' is invalid and will be ignored");
        }

        return folder;
    }

    @Override
    public PSWorkflow validateWorkflow(String workflowName) throws PSWorkflowNotFoundException {
        PSWorkflow workflow = null;

        if (!StringUtils.isBlank(workflowName))
        {
            List<PSWorkflow> wfs = workflowService.findWorkflowsByName(workflowName);
            if (!wfs.isEmpty())
            {
                workflow = wfs.get(0);
            }
        }

        if (workflow == null)
        {
            throw new PSWorkflowNotFoundException("The workflow '" + workflowName + "' could not be found.");
        }

        return workflow;
    }

    /**
     * Class used when generating the folder tree. Saves a {@link PSFolderItem}
     * object and a {@link Boolean} value, that indicates that at least one of
     * its descendants is associated with the given workflow.
     * 
     * @author Santiago M. Murchio
     * 
     */
    private static class PSFolderTreeInfo
    {
        private PSFolderItem folderItem;

        private boolean branchAssociatedWithWorkflow;

        /**
         * Constructs and instance using the params.
         *
         * @param folderItem the PSFolderItem object to use.
         * @param branchAssociatedWithWorkflow the boolean value to use in the
         *            instance.
         */
        public PSFolderTreeInfo(PSFolderItem folderItem, boolean branchAssociatedWithWorkflow)
        {
            this.folderItem = folderItem;
            this.branchAssociatedWithWorkflow = branchAssociatedWithWorkflow;
        }

        /**
         * Given a list of PSFolderTreeInfo objects, it iterates over them and
         * builds a list that contains only PSFolderItem objects.
         * 
         * @param children the list of PSFolderTreeInfo objects. May be
         *            <code>null</code> or empty.
         * @return a list of PSFolderItem objects. May be empty but never
         *         <code>null</code>
         */
        public static List<PSFolderItem> getChildrenElements(List<PSFolderTreeInfo> children)
        {
            List<PSFolderItem> items = new ArrayList<>();
            if (children != null)
            {
                for (PSFolderTreeInfo item : children)
                {
                    items.add(item.folderItem);
                }
            }
            return items;
        }
    }

    @Override
    public List<PSLightWeightObject> getPagesFromFolder(String folderId)
            throws PSFolderNotFoundException, PSPagesNotFoundException, PSValidationException {
        if(StringUtils.isBlank(folderId))
            throw new PSFolderNotFoundException("The supplied folder id is blank");
        PSFolderProperties folderProperties = folderHelper.findFolderProperties(folderId);
        List<PSLightWeightObject> results = new ArrayList<>();
        long pageCtypeId;
        try {
            pageCtypeId = itemDefManager.contentTypeNameToId(IPSPageService.PAGE_CONTENT_TYPE);
        } catch (PSInvalidContentTypeException e) {
            log.error(e);
            throw new PSPagesNotFoundException("Error occurred retrieving pages for the supplied folder, please se log for more details.");
        }
        List<PSItemSummary> itemSums = contentWs.findFolderChildren(idMapper.getGuid(folderProperties.getId()), false);
        for (PSItemSummary sum : itemSums)
        {
            if (sum.getContentTypeId() == pageCtypeId)
                results.add(new PSLightWeightObject(sum.getName(),sum.getGUID().toString()));
        }
        return results;
    }

}
