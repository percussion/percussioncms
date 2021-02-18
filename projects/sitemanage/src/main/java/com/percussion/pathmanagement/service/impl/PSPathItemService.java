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
package com.percussion.pathmanagement.service.impl;

import com.percussion.assetmanagement.service.IPSAssetService;
import com.percussion.assetmanagement.service.IPSWidgetAssetRelationshipService;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.designmanagement.service.IPSFileSystemService.PSInvalidCharacterInFolderNameException;
import com.percussion.itemmanagement.service.IPSItemWorkflowService;
import com.percussion.itemmanagement.service.IPSItemWorkflowService.PSItemWorkflowServiceException;
import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.service.IPSPageService;
import com.percussion.pathmanagement.data.PSDeleteFolderCriteria;
import com.percussion.pathmanagement.data.PSDeleteFolderCriteria.SkipItemsType;
import com.percussion.pathmanagement.data.PSFolderPermission;
import com.percussion.pathmanagement.data.PSItemByWfStateRequest;
import com.percussion.pathmanagement.data.PSMoveFolderItem;
import com.percussion.pathmanagement.data.PSPathItem;
import com.percussion.pathmanagement.data.PSRenameFolderItem;
import com.percussion.pathmanagement.service.IPSPathService;
import com.percussion.services.contentmgr.IPSContentMgr;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.workflow.IPSWorkflowService;
import com.percussion.share.IPSSitemanageConstants;
import com.percussion.share.dao.IPSFolderHelper;
import com.percussion.share.dao.impl.PSFolderHelper;
import com.percussion.share.data.IPSItemSummary;
import com.percussion.share.data.PSItemProperties;
import com.percussion.share.data.PSItemSummaryUtils;
import com.percussion.share.data.PSNoContent;
import com.percussion.share.service.IPSDataService;
import com.percussion.share.service.IPSIdMapper;
import com.percussion.share.service.exception.PSBeanValidationException;
import com.percussion.share.service.exception.PSBeanValidationUtils;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.ui.service.IPSListViewHelper;
import com.percussion.user.data.PSAccessLevel;
import com.percussion.user.data.PSAccessLevelRequest;
import com.percussion.user.data.PSCurrentUser;
import com.percussion.user.service.IPSUserService;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.thread.PSThreadUtils;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.PSErrorResultsException;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static com.percussion.share.dao.PSFolderPathUtils.concatPath;
import static com.percussion.share.dao.PSFolderPathUtils.pathSeparator;
import static com.percussion.sitemanage.service.IPSSiteSectionMetaDataService.PAGE_CATALOG;
import static com.percussion.sitemanage.service.IPSSiteSectionMetaDataService.SECTION_SYSTEM_FOLDER_NAME;
import static com.percussion.webservices.PSWebserviceUtils.getItemSummary;
import static com.percussion.webservices.PSWebserviceUtils.isItemCheckedOutToUser;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.apache.commons.lang.StringUtils.removeEnd;
import static org.apache.commons.lang.StringUtils.substringBeforeLast;
import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;

/**
 * Base class for all path item services. Each path item service must implement
 * {@link #getFullFolderPath(String)}. The {@link #findItem(String)} and
 * {@link #findItems(String)} methods should also be overridden when necessary
 * in order to modify the item discovery algorithms.
 */
public abstract class PSPathItemService implements IPSPathService
{
    public PSPathItemService(IPSFolderHelper folderHelper, IPSIdMapper idMapper,
            IPSItemWorkflowService itemWorkflowService, IPSAssetService assetService,
            IPSWidgetAssetRelationshipService widgetAssetRelationshipService, IPSContentMgr contentMgr,
            IPSWorkflowService workflowService, IPSPageService pageService,
            IPSListViewHelper listViewHelper, IPSUserService userService)
    {
        super();
        this.folderHelper = folderHelper;
        this.idMapper = idMapper;
        this.itemWorkflowService = itemWorkflowService;
        this.assetService = assetService;
        this.widgetAssetRelationshipService = widgetAssetRelationshipService;
        this.contentMgr = contentMgr;
        this.workflowService = workflowService;
        this.pageService = pageService;
        this.listViewHelper = listViewHelper;
        this.userService = userService;
    }
    
    public IPSListViewHelper getListViewHelper()
    {
        return listViewHelper;
    }
    
    public void setListViewHelper(IPSListViewHelper listViewHelper)
    {
        this.listViewHelper = listViewHelper;
    }

    public List<String> getRolesAllowed()
    {
        return rolesAllowed;
    }
    
    public void setRolesAllowed(List<String> rolesAllowed)
    {
        this.rolesAllowed = rolesAllowed;
    }
    
    public PSPathItem find(String path) throws PSPathServiceException, IPSDataService.DataServiceNotFoundException, PSValidationException {
        log.debug("Find root of path: {}", path);
        if ("/".equals(path))
            return findRoot();

        return findItem(path);
    }

    public PSItemProperties findItemProperties(String path) throws PSPathNotFoundServiceException, IPSDataService.DataServiceNotFoundException, PSValidationException {
        notEmpty(path, "path");
        if (log.isDebugEnabled())
            log.debug("find item properties: {}" , path);
        String fullFolderPath = getFullFolderPath(path);
        PSItemProperties props;
        try
        {
            props = folderHelper.findItemProperties(fullFolderPath);
        }
        catch (Exception e)
        {
        	throw new PSPathNotFoundServiceException("Path not found: " + path);
        }
        return props;
    }

    public List<PSItemProperties> findItemProperties(PSItemByWfStateRequest request)
            throws PSPathServiceException, IPSDataService.DataServiceNotFoundException, PSValidationException {
        notNull(request, "request");
        String path = request.getPath();
        String workflowName = request.getWorkflow();
        String stateName = request.getState();
        if (log.isDebugEnabled())
            log.debug("find item properties: {}, {}, {}",path, workflowName ,stateName);

        // find the workflow, state id's
        int workflowId = -1;
        int stateId = -1;
        try
        {
            workflowId = itemWorkflowService.getWorkflowId(request.getWorkflow());
            stateId = itemWorkflowService.getStateId(request.getWorkflow(), request.getState());
        }
        catch (PSItemWorkflowServiceException | PSValidationException e)
        {
            throw new PSPathServiceException(e);
        }

        // build the jcr query
        String jcrQuery = "select rx:sys_title, jcr:path from " + getQuerySelectFrom() + " where jcr:path like '"
                + getFullFolderPath(path) + "/%' and rx:sys_workflowid = " + workflowId;

        if (stateId != -1)
        {
            jcrQuery += " and rx:sys_contentstateid = " + stateId;
        }

        // get the properties for all items which satisfy the jcr query
        List<PSItemProperties> props = new ArrayList<>();
        try
        {
            Query query = contentMgr.createQuery(jcrQuery, Query.SQL);
            QueryResult queryResult = contentMgr.executeQuery(query, -1, new HashMap<String, Object>(), null);
            RowIterator rowIter = queryResult.getRows();
            while (rowIter.hasNext())
            {
                Row r = rowIter.nextRow();
                String itemName = r.getValue("rx:sys_title").getString();
                String itemPath = r.getValue("jcr:path").getString();
                String fullItemPath = folderHelper.concatPath(itemPath, itemName);

                if (!StringUtils.containsIgnoreCase(fullItemPath, CATALOG_FOLDERS))
                {
                    PSItemProperties itemProps = folderHelper.findItemProperties(fullItemPath);
                    itemProps.setPath(PSPathUtils.getFinderPath(fullItemPath));
                    PSPage page = null;
                    try
                    {
                        page = pageService.findPageByPath(fullItemPath);
                    }
                    catch (Exception e)
                    {
                        log.warn("Error occurred while finding the page by path : " + fullItemPath, e);
                    }

                    // check if the page was found or not
                    if (page == null)
                    {
                        throw new PSPathNotFoundServiceException("The page: '" + fullItemPath + "' could not be found.");
                    }

                    String summary = page.getSummary();
                    itemProps.setSummary(summary);

                    props.add(itemProps);
                }
            }
        }
        catch (Exception e)
        {
            throw new PSPathServiceException("Failed to get item properties for path: " + path, e);
        }

        return props;
    }

    /*
     * //see base interface method for details
     */
    public PSNoContent moveItem(PSMoveFolderItem request) throws PSDataServiceException, PSPathServiceException, PSItemWorkflowServiceException {
        String path = request.getTargetFolderPath();
        String relativePath = path.substring(path.indexOf('/'));
        if (relativePath.charAt(relativePath.length() - 1) != '/')
            relativePath += "/";
        String targetPath = getFullFolderPath(relativePath);

        path = request.getItemPath();
        relativePath = path.substring(path.indexOf('/'));
        if (relativePath.charAt(relativePath.length() - 1) != '/')
            relativePath += "/";
        String itemPath = getFullFolderPath(relativePath);
        validateUserAccessBeforeMove(itemPath, targetPath);
        folderHelper.moveItem(targetPath, itemPath, true);

        return new PSNoContent("moveItem");
    }
    
    /**
     * Validates whether user has access to source item and target folder.
     * If the user is designer or admin or if the source is folder then skips the check.
     * For the source item checks whether item is modifiable by the user, for target folder
     * check whether user has access level of more than reader.  
     * @param srcPath assumed not blank.
     * @param targetPath assume not blank.
     */
    private void validateUserAccessBeforeMove(String srcPath, String targetPath) throws PSDataServiceException, PSPathServiceException, PSItemWorkflowServiceException {
        PSCurrentUser curUser = userService.getCurrentUser();
        //If the logged in user is not an admin or designer, check access
        if(!(curUser.isAdminUser() || curUser.isDesignerUser())){
            IPSItemSummary sum = null;
            try
            {
                sum = folderHelper.findItem(srcPath);
            }
            catch (Exception e)
            {
                throw new PSPathServiceException("Failed to move, could not find the selected item.");
            }
            if(!sum.isFolder()){
                if(!itemWorkflowService.isModifyAllowed(sum.getId()))
                    throw new PSPathServiceException("You are not authorized to move the selected item.");
            }
            PSAccessLevelRequest acLevelReq = new PSAccessLevelRequest();
            acLevelReq.setParentFolderPath(targetPath);
            PSAccessLevel accLevel = userService.getAccessLevel(acLevelReq);
            if(accLevel.getAccessLevel().equals("READER") || accLevel.getAccessLevel().equals("NONE")){
                throw new PSPathServiceException("You are not authorized to move this item into this folder.");
            }
            
        }
    }
    
    public List<PSPathItem> findChildren(String path) throws PSPathServiceException, IPSDataService.DataServiceNotFoundException, PSValidationException {
        log.debug("Find children of path: {}",  path);

        return findItems(path);
    }

    public PSPathItem addFolder(String path) throws PSPathServiceException, IPSDataService.DataServiceNotFoundException, PSValidationException {
        notEmpty(path, "path");

        log.debug("Add folder: {}", path);

        try
        {
            folderHelper.createFolder(getFullFolderPath(path));
        }
        catch (Exception e)
        {
            throw new PSPathServiceException("Failed to add folder: " + path, e);
        }

        return find(path);
    }

    public PSPathItem addNewFolder(String path) throws PSPathNotFoundServiceException, PSPathServiceException, IPSDataService.DataServiceNotFoundException, PSValidationException {
        notEmpty(path, "path");

        String folderPath = path;
        PSPathItem item = findItem(path);
        if (item.isLeaf())
        {
            // item is not a folder, so get the parent folder path
            folderPath = relativeParentPath(path);
        }

        log.debug("Add new folder to: {}" , folderPath);

        String folderName = folderHelper.getUniqueFolderName(
                getFullFolderPath(folderHelper.concatPath(folderPath, "/")), "New-Folder");
        return addFolder(folderHelper.concatPath(folderPath, folderName, "/"));
    }

    public String relativeParentPath(String path) throws PSPathNotFoundServiceException {

        PSPathUtils.validatePath(path);
        path = removeEnd(path, "/");
        String parentPath = substringBeforeLast(path, "/");
        return parentPath + "/";

    }

    public PSPathItem renameFolder(PSRenameFolderItem item) throws PSValidationException, PSPathServiceException, IPSDataService.DataServiceNotFoundException {
        PSBeanValidationException errors = PSBeanValidationUtils.validate(item);
        errors.throwIfInvalid();

        String path = item.getPath();
        if ("/".equals(path))
        {
            throw new PSPathServiceException("Root folder may not be renamed");
        }

        log.debug("Rename folder: {}" , path);

        String name = item.getName();
        
        if (name.length() > 50)
        {
            log.debug("Cannot rename folder because name exceeds 50 characters: {}",name );
            errors.rejectValue("name", "renameFolderItem.longName", "Cannot rename folder '<old_name>' to '<new_name>' because that name exceeds character limit.");
            throw errors;        
        }
        
        try
        {
            folderHelper.renameFolder(getFullFolderPath(path), name);
        }
        catch (PSReservedNameServiceException e)
        {
            errors.rejectValue("name", "renameFolderItem.reservedName", "Cannot rename folder '<old_name>' to '<new_name>' because that name is a reserved folder name");
            throw errors;
        }
        catch (PSErrorResultsException e)
        {
            String msg = "Failed to rename folder.";
            Iterator<Entry<IPSGuid, Object>> iter = e.getErrors().entrySet().iterator();
            if (iter.hasNext())
            {
                Entry<IPSGuid, Object> obj = iter.next();
                msg = ((PSErrorException) (obj.getValue())).getLocalizedMessage();
                if (msg.contains("This field cannot be empty and must be unique within the folder"))
                    msg = "An asset or folder with the name you specified already exists. Specify a different name.";
            }
            errors.rejectValue("name", "renameFolderItem.name", msg);
            throw errors;
        }
        catch (PSInvalidCharacterInFolderNameException e)
        {
            errors.rejectValue("name", "renameFolderItem.invalidCharInName",
                    "Cannot rename folder '<old_name>' to '<new_name>' because folder names cannot contain the following characters: "
                            + e.getInvalidChars());
            throw errors;
        }
        catch (Exception e)
        {
            errors.rejectValue("name", "renameFolderItem.name", "Failed to rename folder");
            throw errors;
        }

        return find(folderHelper.concatPath(relativeParentPath(path), name, "/"));
    }

    public int deleteFolder(PSDeleteFolderCriteria criteria) throws PSPathServiceException, IPSDataService.DataServiceNotFoundException, PSValidationException {
        notNull(criteria, "criteria");

        String path = criteria.getPath();
        if ("/".equals(path))
        {
            throw new PSPathServiceException("Root folder may not be deleted");
        }

        log.debug("Delete folder: {}" , path);



        List<PSPathItem> items = findAllPurgeableLeafItems(path);
        return deleteFolder(path, getInUseItems(items), criteria);
    }

    public String validateFolderDelete(String path) throws PSPathServiceException, IPSDataService.DataServiceNotFoundException, PSValidationException, PSItemWorkflowServiceException {
        notEmpty(path, "path");

        String response = "";

        PSPathItem folder = find(path);
        if (!folderHelper.validateFolderPermissionForDelete(folder.getId()))
        {
            response += FOLDER_HAS_NO_ADMIN_PERMISSION;
        }

        List<PSPathItem> items = findAllPurgeableLeafItems(path);

        if (!validateFolderDeleteAuthorization(items))
        {
            response += getNotAuthorizedResult();
        }

        if (!validateFolderDeleteTemplates(items))
        {
            response += getInUseTemplatesResult();
        }

        if (!getInUseItems(items).isEmpty())
        {
            if (!StringUtils.isEmpty(response))
            {
                response += ',';
            }

            response += getInUsePagesResult();
        }

        return !StringUtils.isEmpty(response) ? response : VALIDATE_SUCCESS;
    }

    public String findLastExistingPath(String path)
    {
        notEmpty(path);

        String tmpPath = path;
        PSPathItem item = null;
        do
        {
            try
            {
                item = find(tmpPath);
            }
            catch (Exception e)
            {
                tmpPath = folderHelper.concatPath(
                        PSPathUtils.getFinderPath(folderHelper.parentPath(PSPathUtils.getFolderPath(tmpPath))), "/");
            }
        }
        while (item == null);

        return StringUtils.removeStart(StringUtils.removeEnd(tmpPath, "/"), "/");
    }

    public String getRootName()
    {
        return rootName;
    }

    public void setRootName(String rootName)
    {
        this.rootName = rootName;
    }

    /**
     * Attempts to find the item identified by the specified path.
     * 
     * @param path the item path. This is a relative path.
     * 
     * @return {@link PSPathItem} which represents the item located at the
     *         specified path. Never <code>null</code>.
     */
    protected PSPathItem findItem(String path) throws PSPathNotFoundServiceException, IPSDataService.DataServiceNotFoundException, PSValidationException {
        String fullFolderPath = getFullFolderPath(path);
        IPSItemSummary dataItemSummary = getItemFromPath(path, fullFolderPath);
        PSPathItem item = createPathItem();
        convert(dataItemSummary, item);
        item.setPath(path);
        item.setFolderPath(fullFolderPath);
        return item;
    }

    /**
     * Attempts to find the items which are children of the location identified
     * by the specified path.
     * 
     * @param path the parent item path. This is a relative path.
     * 
     * @return list of {@link PSPathItem} objects which represent the child
     *         items of the specified path. Never <code>null</code>.
     */
    protected List<PSPathItem> findItems(String path) throws PSPathNotFoundServiceException, IPSDataService.DataServiceNotFoundException, PSValidationException {
        String fullFolderPath = getFullFolderPath(path);
        List<IPSItemSummary> sums;
        try
        {
            sums = folderHelper.findItems(fullFolderPath, PSPathOptions.folderChildrenOnly());
        }
        catch (Exception e)
        {
            throw new PSPathNotFoundServiceException("Path not found: " + path, e);
        }

        List<PSPathItem> pathFolderItems = new ArrayList<PSPathItem>();
        List<PSPathItem> pathItems = new ArrayList<PSPathItem>();

        for (IPSItemSummary data : sums)
        {
            PSThreadUtils.checkForInterrupt();
            if (shouldFilterItem(data))
            {
                continue;
            }

            PSPathItem item = createPathItem();
            convert(data, item);
            item.setPath(path + item.getName());
            item.setFolderPath(folderHelper.concatPath(fullFolderPath, item.getName()));
            if (data.isFolder())
            {
                pathFolderItems.add(item);
            }
            else
            {
                pathItems.add(item);
            }
        }
        
        Collections.sort(pathFolderItems, PSPathItemComparator.getInstance());
        Collections.sort(pathItems, PSPathItemComparator.getInstance());
        pathFolderItems.addAll(pathItems);

        return pathFolderItems;
    }

    /**
     * Generates the full internal folder path for the specified relative path.
     * This path is used for all item lookup operations.
     * 
     * @param path the path identifying a relative location of an item or folder
     *            in the system.
     * 
     * @return the complete folder path used for item lookup. Never
     *         <code>null</code> or empty.
     */
    protected abstract String getFullFolderPath(String path) throws IPSDataService.DataServiceNotFoundException, PSPathNotFoundServiceException, PSValidationException;

    /**
     * Gets the root of the folder path used for all item lookup operations. See
     * {@link #getFullFolderPath(String)}.
     * 
     * @return the internal folder root. Never <code>null</code> or empty.
     */
    protected abstract String getFolderRoot() throws PSPathServiceException;

    /**
     * Gets the from portion of the jcr query select statement used in
     * {@link #findItemProperties(PSItemByWfStateRequest)}.
     * 
     * @return the jcr query select from component.
     */
    protected String getQuerySelectFrom()
    {
        return "rx:" + IPSPageService.PAGE_CONTENT_TYPE;
    }

    protected PSPathItem createPathItem()
    {
        return new PSPathItem();
    }

    protected void convert(IPSItemSummary itemSummary, PSPathItem item)
    {
        PSItemSummaryUtils.copyProperties(itemSummary, item);
        if (itemSummary.isFolder())
        {
            item.setLeaf(false);
        }        
        
        try
        {
            if (PSPathOptions.shouldCheckChildTypes() && (itemSummary.isFolder() || IPSSitemanageConstants.SITE_CONTENTTYPE.equalsIgnoreCase(itemSummary.getType())))
            {
                List<IPSItemSummary> sums;
                boolean foldersOnly = PSPathOptions.folderChildrenOnly();
                if (IPSSitemanageConstants.SITE_CONTENTTYPE.equalsIgnoreCase(itemSummary.getType()))
                    sums = folderHelper.findItems(itemSummary.getFolderPaths().get(0), foldersOnly);
                else
                    sums = folderHelper.findItems(folderHelper.concatPath(itemSummary.getFolderPaths().get(0),
                            itemSummary.getName()), foldersOnly);

                for (IPSItemSummary sum : sums)
                {
                    if (IPSItemSummary.Category.SECTION_FOLDER.equals(sum.getCategory())
                            || IPSItemSummary.Category.EXTERNAL_SECTION_FOLDER.equals(sum.getCategory()))
                    {
                        item.setHasSectionChildren(true);
                    }
                    else if (sum.isFolder() && !(".system".equals(sum.getName())))
                    {
                        item.setHasFolderChildren(true);
                    }
                    else
                    {
                        item.setHasItemChildren(true);
                    }
                    if (item.hasItemChildren() && item.hasFolderChildren() && item.hasSectionChildren())
                        break;
                }
                    
            }
            else
            {
                item.setHasItemChildren(false);
                item.setHasFolderChildren(false);
                item.setHasSectionChildren(false);
            }
        }
        catch (Exception e)
        {
            // The path wasn't found, set the properties to false
            item.setHasItemChildren(false);
            item.setHasFolderChildren(false);
            item.setHasSectionChildren(false);
        }
        
    }

    protected PSPathItem findRoot() throws PSPathNotFoundServiceException, IPSDataService.DataServiceNotFoundException, PSValidationException {
        PSPathItem root = new PSPathItem();
        root.setName(getRootName());
        root.setPath("/");
        root.setLeaf(false);
        root.setHasItemChildren(true);
        root.setHasFolderChildren(true);
        root.setHasSectionChildren(true);
        root.setFolderPath(getFullFolderPath("/"));
        return root;
    }

    /**
     * Finds all (purgeable) leaf items for the specified folder path,
     * recursive. This includes leaf items in sub-folders, etc. The purgeable
     * items are the items under the folders that current user has ADMIN or
     * WRITE access to the folders.
     * 
     * @param path the folder.
     * 
     * @return list of all items included under the folder.
     */
    private List<PSPathItem> findAllPurgeableLeafItems(String path) throws PSPathNotFoundServiceException, IPSDataService.DataServiceNotFoundException, PSValidationException {

        PSPathUtils.validatePath(path);

        List<PSPathItem> items = new ArrayList<PSPathItem>();

        for (PSPathItem item : findItems(path))
        {
            if (item.isLeaf())
            {
                items.add(item);
            }
            else if (folderHelper.hasFolderPermission(item.getId(), PSFolderPermission.Access.WRITE))
            {
                items.addAll(findAllPurgeableLeafItems(folderHelper.concatPath(item.getPath(), "/")));
            }
        }

        return items;
    }

    /**
     * Gets in use items from a list of (leaf) items, where the items are used
     * by other approved pages (pages are in "Pending" or "Live" state).
     * 
     * @param items a list of leaf items in question, not <code>null</code>.
     * 
     * @return list of paths representing the in use children of the path. Never
     *         <code>null</code>, may be empty.
     */
    private List<String> getInUseItems(List<PSPathItem> items) throws PSValidationException {
        List<String> inUseItemPaths = new ArrayList<>();

        Map<String, Set<String>> assetRelOwnersMap = new HashMap<>();
        List<Integer> deletePageIds = new ArrayList<>();

        for (PSPathItem item : items) {
            PSThreadUtils.checkForInterrupt();

                Set<String> relOwners = getApprovedPages(item);
                if (!relOwners.isEmpty()) {
                    assetRelOwnersMap.put(item.getPath(), relOwners);
                }

                PSLegacyGuid id = (PSLegacyGuid) idMapper.getGuid(item.getId());
                deletePageIds.add(id.getContentId());

        }

        Iterator<String> iter = assetRelOwnersMap.keySet().iterator();
        while (iter.hasNext())
        {
            PSThreadUtils.checkForInterrupt();

            String itemPath = iter.next();
            Set<String> relOwners = assetRelOwnersMap.get(itemPath);
            for (String owner : relOwners)
            {
                PSLegacyGuid id = (PSLegacyGuid) idMapper.getGuid(owner);
                if (!deletePageIds.contains(id.getContentId()))
                {
                    // asset is being used by page which is not going to be
                    // deleted
                    inUseItemPaths.add(itemPath);
                    break;
                }
            }
        }

        return inUseItemPaths;
    }

    /**
     * Validates that the current user is authorized to delete the specified
     * items
     * 
     * @param items a list of leaf items in question, not <code>null</code>.
     * 
     * @return <code>true</code> if the user is authorized, <code>false</code>
     *         otherwise.
     */
    private boolean validateFolderDeleteAuthorization(List<PSPathItem> items) throws PSItemWorkflowServiceException, PSValidationException {
        for (PSPathItem item : items)
        {
            if (!itemWorkflowService.isModifyAllowed(item.getId()))
            {
                return false;
            }
        }

        return true;
    }

    /**
     * Validates that all items (assets, pages) under the specified folder are
     * not in use by one or more templates.
     * 
     * @param items a list of leaf items in question, not <code>null</code>.
     * 
     * @return <code>true</code> if all assets are not in use by templates,
     *         <code>false</code> otherwise.
     */
    private boolean validateFolderDeleteTemplates(List<PSPathItem> items)
    {
        for (PSPathItem item : items)
        {
            try {
                if (widgetAssetRelationshipService.isUsedByTemplate(item.getId())) {
                    return false;
                }
            } catch (PSValidationException e) {
               log.warn("Validation error preparing for delete action, id: {} Error: {}",
                       item.getId(),
                       e.getMessage());
            }
        }

        return true;
    }

    /**
     * Result to be returned when items used by approved pages are found by
     * {@link #validateFolderDelete(String)}.
     * 
     * @return string response indicating that items are in use by approved
     *         pages.
     */
    protected abstract String getInUsePagesResult() throws PSPathServiceException;

    /**
     * Result to be returned when unauthorized items are found by
     * {@link #validateFolderDelete(String)}.
     * 
     * @return string response indicating that the user is not authorized to
     *         delete items.
     */
    protected abstract String getNotAuthorizedResult() throws PSPathServiceException;

    /**
     * Result to be returned when items linked by templates are found by
     * {@link #validateFolderDelete(String)}.
     * 
     * @return string response indicating that items are linked by templates.
     */
    protected abstract String getInUseTemplatesResult() throws PSPathServiceException;

    /**
     * Used to determine if an item should be filtered in
     * {@link #findItems(String)}.
     * 
     * @param item the summary that will be checked for filtering.
     * 
     * @return <code>true</code> if the item should be filtered,
     *         <code>false<code> otherwise.
     */
    protected boolean shouldFilterItem(IPSItemSummary item)
    {
        return false;
    }

    /**
     * Removes the specified path item from the given folder. The item may also
     * be purged depending on the value of @param shouldPurge.
     * 
     * @param fullFolderPath path to the folder which contains the item, never
     *            blank.
     * @param item never <code>null</code>.
     * @param shouldPurge true if the item should be permanently deleted
     *
     */
    protected void removeItem(String fullFolderPath, PSPathItem item, boolean shouldPurge) throws Exception {
        notEmpty(fullFolderPath);
        notNull(item);

        folderHelper.removeItem(fullFolderPath, item.getId(), shouldPurge);
    }

    /**
     * Gets all approved pages which use the specified item.
     * 
     * @param item never <code>null</code>.
     * 
     * @return set of id's for the pages. Never <code>null</code>, may be empty.
     */
    protected Set<String> getApprovedPages(PSPathItem item) throws PSValidationException {
        notNull(item);

        return itemWorkflowService.getApprovedPages(item.getId());
    }

    private IPSItemSummary getItemFromPath(String path, String fullFolderPath) throws PSPathNotFoundServiceException {
        String error = "Path not found: " + path;
        if (fullFolderPath == null)
            throw new PSPathNotFoundServiceException(error);
        IPSItemSummary item;
        try
        {
            item = folderHelper.findItem(fullFolderPath);
            if (item == null)
                throw new PSPathNotFoundServiceException(error);
        }
        catch (Exception e)
        {
            throw new PSPathNotFoundServiceException(error, e);
        }
        return item;
    }

    /**
     * Deletes the given folder recursively.
     * 
     * @param path the folder path, assumed not <code>null</code>.
     * @param inUseItems list of in use item paths, assumed not
     *            <code>null</code>.
     *            {@link SkipItemsType#EMPTY} or <code>null</code>, in use items
     *            will be skipped.
     * 
     * @return number of undeleted items;
     */
    private int deleteFolder(String path, List<String> inUseItems, PSDeleteFolderCriteria criteria) throws IPSDataService.DataServiceNotFoundException, PSPathServiceException, PSValidationException {
        PSPathItem folderItem = findItem(path);
        String fullFolderPath = getFullFolderPath(path);
        int undeletedItems = 0;
        String pathToCheck = PSFolderHelper.getOppositePath(fullFolderPath);
//        if (!criteria.getShouldPurge()) {
//            if (!folderHelper.isFolderValidForRecycleOrRestore(pathToCheck, fullFolderPath, TYPE_RECYCLED_CONTENT, TYPE_FOLDER_CONTENT)) {
//                throw new PSPathServiceException("This folder already exists under the recycle bin. Please rename" +
//                        " the folder prior to recycling or purge the folder under the recycle bin.");
//            }
//        }

        // attempt to purge the child items if possible
        if (folderHelper.hasFolderPermission(folderItem.getId(), PSFolderPermission.Access.WRITE))
        {
            undeletedItems = purgeFolderChildren(path, inUseItems, criteria);
        }

        // attempt to purge the folder itself if possible
        if (findItems(path).isEmpty())
        {
            try
            {
                if (folderHelper.validateFolderPermissionForDelete(folderItem.getId())) {
                    folderHelper.deleteFolder(fullFolderPath, !criteria.getShouldPurge());
                }
                else
                    undeletedItems++;
            }
            catch (Exception e)
            {
                throw new PSPathServiceException("Failed to delete folder: " + path, e);
            }
        }

        return undeletedItems;
    }

    /**
     * Purge the child items for the specified folder.
     * 
     * @param path the path of the specified folder, assumed not blank.
     * @param inUseItems list of in use item paths, assumed not
     *            <code>null</code>.
     * 
     * @return number of undeleted items;
     */
    private int purgeFolderChildren(String path, List<String> inUseItems, PSDeleteFolderCriteria criteria) throws IPSDataService.DataServiceNotFoundException, PSPathServiceException, PSValidationException {
        int undeletedItems = 0;

        String fullFolderPath = getFullFolderPath(path);
        SkipItemsType skipItems = criteria.getSkipItems();

        int ord = -1;
        if (skipItems != null)
        {
            ord = skipItems.ordinal();
        }

        boolean shouldSkip = skipItems == null || ord == SkipItemsType.EMPTY.ordinal()
                || ord == SkipItemsType.YES.ordinal();
        List<PSPathItem> childItems = findItems(path);
        for (PSPathItem child : childItems)
        {
            String childPath = child.getPath();
            if (child.isLeaf())
            {
                if (inUseItems.contains(childPath) && shouldSkip)
                {
                    undeletedItems++;
                }
                else
                {
                    try
                    {
                        if (hasDeletePermission(child.getId()))
                        {
                            // remove the item
                            // shouldPurge is false so the item is not purged, it is recycled.
                            removeItem(fullFolderPath, child, criteria.getShouldPurge());
                        }
                        else
                        {
                            undeletedItems++;
                        }
                    }
                    catch (Exception e)
                    {
                        if (skipItems != null)
                        {
                            log.warn("Error deleting item.", e);
                            // return failed item path
                            undeletedItems++;
                        }
                        else
                        {
                            throw new PSPathServiceException("Failed to delete item: " + childPath, e);
                        }
                    }
                }
            }
            else
            {
                // recurse into sub-folder
                undeletedItems += deleteFolder(folderHelper.concatPath(childPath, "/"), inUseItems, criteria);
            }
        }

        return undeletedItems;
    }

    /**
     * Determines if current user has privilege (or permission) to delete the
     * specified item.
     * 
     * @param itemId the ID of the item in question, not empty.
     * @return <code>true</code> if current user has privilege to delete the
     *         item; otherwise current user does not have privilege to delete
     *         the item.
     */
    private boolean hasDeletePermission(String itemId) throws PSValidationException, PSItemWorkflowServiceException {
        IPSGuid id = idMapper.getGuid(itemId);

        PSComponentSummary sum = getItemSummary(((PSLegacyGuid) id).getContentId());
        if (sum.isFolder())
            return folderHelper.hasFolderPermission(itemId, PSFolderPermission.Access.ADMIN);

        if (!isEmpty(sum.getCheckoutUserName()) && !isItemCheckedOutToUser(sum))
            return false;

        return itemWorkflowService.isModifiableByUser(itemId)
                && !widgetAssetRelationshipService.isUsedByTemplate(itemId);
    }

    /**
     * Determines if a path item represents a Page.
     * 
     * @param item the path item, may not be <code>null</code>.
     * 
     * @return <code>true</code> if the item is a Page, <code>false</code>
     *         otherwise.
     */
    protected boolean isPage(PSPathItem item)
    {
        notNull(item);

        return item.getType().equals(IPSPageService.PAGE_CONTENT_TYPE);
    }

    /**
     * Constant for the response given when validation of a folder for delete is
     * successful.
     */
    public static final String VALIDATE_SUCCESS = "Success";

    /**
     * Part of the response from {@link #validateFolderDelete(String)} if the
     * current user does not ADMIN access to the specified folder or one of its
     * descendant folders
     */
    public static String FOLDER_HAS_NO_ADMIN_PERMISSION = "FolderHasNoAdminPermission";

    /**
     * Used for folder item operations. Initialized in ctor, never
     * <code>null</code> after that.
     */
    protected IPSFolderHelper folderHelper;

    /**
     * The id mapper, initialized by constructor, never <code>null</code> after
     * that.
     */
    protected IPSIdMapper idMapper;

    /**
     * Used for item workflow operations. Initialized in ctor, never
     * <code>null</code> after that.
     */
    protected IPSItemWorkflowService itemWorkflowService;

    /**
     * Used for checking current user and access level
     */
    protected IPSUserService userService;

    /**
     * Used for asset operations. Initialized in ctor, never <code>null</code>
     * after that.
     */
    protected IPSAssetService assetService;

    /**
     * Used for relationship operations. Initialized in ctor, never
     * <code>null</code> after that.
     */
    protected IPSWidgetAssetRelationshipService widgetAssetRelationshipService;

    /**
     * Used for jcr query execution. Initialized in ctor, never
     * <code>null</code> after that.
     */
    protected IPSContentMgr contentMgr;

    /**
     * Used for server workflow operations. Initialized in ctor, never
     * <code>null</code> after that.
     */
    protected IPSWorkflowService workflowService;

    protected IPSPageService pageService;
    
    private IPSListViewHelper listViewHelper;

    /**
     * The root name seen in the ui as the base for all items located by this
     * service. Initialized in the spring configuration.
     */
    private String rootName;
    
    /**
     * A comma separated list of user roles that are allowed to access this service.
     * If it null or empty, all roles are allowed to access it.
     */
    private List<String> rolesAllowed;
    
    /**
     * Constant for catalog path folder
     */
    private static String CATALOG_FOLDERS = pathSeparator() + concatPath(SECTION_SYSTEM_FOLDER_NAME, PAGE_CATALOG);
    
    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Logger log = LogManager.getLogger(PSPathItemService.class);

    /**
     * The constant to represent the recycled content relationship type.
     */
    private static final String TYPE_RECYCLED_CONTENT = PSRelationshipConfig.TYPE_RECYCLED_CONTENT;

    /**
     * The constant to represent the recycled content relationship type.
     */
    private static final String TYPE_FOLDER_CONTENT = PSRelationshipConfig.TYPE_FOLDER_CONTENT;

    /**
     * The static constant to represent the root recycling folder.
     */
    private static final String RECYCLING_ROOT = "//Folders/$System$/Recycling";
}
