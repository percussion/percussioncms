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

package com.percussion.recycle.service.impl;

import com.percussion.auditlog.PSActionOutcome;
import com.percussion.auditlog.PSAuditLogService;
import com.percussion.auditlog.PSContentEvent;
import com.percussion.cms.PSCmsException;
import com.percussion.cms.handlers.PSRelationshipCommandHandler;
import com.percussion.cms.objectstore.*;
import com.percussion.cms.objectstore.server.PSRelationshipProcessor;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.design.objectstore.PSRelationshipSet;
import com.percussion.fastforward.managednav.IPSManagedNavService;
import com.percussion.itemmanagement.data.PSItemStateTransition;
import com.percussion.itemmanagement.service.IPSItemWorkflowService;
import com.percussion.itemmanagement.service.IPSWorkflowHelper;
import com.percussion.recycle.service.IPSRecycleService;
import com.percussion.server.cache.PSFolderRelationshipCache;
import com.percussion.services.content.data.PSItemStatus;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.legacy.IPSCmsContentSummaries;
import com.percussion.services.legacy.PSCmsContentSummariesLocator;
import com.percussion.servlets.PSSecurityFilter;
import com.percussion.share.data.IPSItemSummary;
import com.percussion.share.data.PSDataItemSummary;
import com.percussion.share.service.IPSDataItemSummaryService;
import com.percussion.share.service.IPSDataService;
import com.percussion.share.service.IPSIdMapper;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.PSErrorResultsException;
import com.percussion.webservices.PSErrorsException;
import com.percussion.webservices.PSWebserviceUtils;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.system.IPSSystemWs;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.*;

import static org.apache.commons.lang.Validate.notEmpty;

/**
 * @author chriswright
 */
@Component("recycleService")
@Lazy
public class PSRecycleService implements IPSRecycleService {
    private PSAuditLogService psAuditLogService=PSAuditLogService.getInstance();
    private PSContentEvent psContentEvent;


    /**
     * Logger for this class.
     */
    private static final Logger log = LogManager.getLogger(PSRecycleService.class);

    /**
     * Constant to represent the recycled relationship name.
     * {@link PSRelationshipConfig.SysConfigEnum}
     */
    private static final String RECYCLED_TYPE = PSRelationshipConfig.TYPE_RECYCLED_CONTENT;

    /**
     * Constant to represent the folder category name.
     * {@link PSRelationshipConfig.SysConfigEnum}
     */
    private static final String FOLDER_CATEGORY = PSRelationshipConfig.CATEGORY_FOLDER;



    /**
     * Constant to represent the folder relationship name.
     * {@link PSRelationshipConfig.SysConfigEnum}
     */
    private static final String FOLDER_TYPE = PSRelationshipConfig.TYPE_FOLDER_CONTENT;

    private static final String FOLDERS = "//Folders";
    private static final String SYSTEM = "$System$";
    private static final String SITES = "//Sites";
    private static final String ASSETS = "Assets";
    private static final String NAVON_FIELD_DISPLAYTITLE = "displaytitle";

    /**
     * Static constant to represent assets root.
     */
    public static final String ASSETS_ROOT = FOLDERS + "/" + SYSTEM + "/" + ASSETS;

    /**
     * Static constant to represent the Recycling root.
     */
    public static final String RECYCLING_ROOT = FOLDERS + "/" + SYSTEM + "/" + "Recycling";

    /**
     * Error when calculating a unique item name and path.
     */
    public static final String UNIQUE_ID_ERROR = "Error finding if item is unique with id: ";

    /**
     * System Web Service.
     */
    private IPSSystemWs systemWs;

    /**
     * Data Item Summary Service.
     */
    private IPSDataItemSummaryService itemSummaryService;

    /**
     * Guid Manager.
     */
    private IPSIdMapper idMapper;

    /**
     * Content Web Service.
     */
    private IPSContentWs contentWs;

    /**
     * Workflow Helper.
     */
    private IPSWorkflowHelper workflowHelper;

    private IPSManagedNavService navService;

    @Autowired
    public PSRecycleService(IPSSystemWs systemWs, IPSIdMapper idMapper,
                            IPSDataItemSummaryService itemSummaryService, IPSContentWs contentWs,
                            IPSWorkflowHelper workflowHelper, IPSManagedNavService navService) {
        this.systemWs = systemWs;
        this.idMapper = idMapper;
        this.itemSummaryService = itemSummaryService;
        this.contentWs = contentWs;
        this.workflowHelper = workflowHelper;
        this.navService = navService;
    }

    //Create Root Relationship between Site-SiteName for Recycle Folder, if not created yet
    private void createRootSiteDeleteRelationship(String path) throws PSCmsException {

        if(path == null ){
            return;
        }
        List<IPSGuid> pathids = contentWs.findPathIds(path);
        if(pathids == null || pathids.size() <2 ){
            return;
        }

        IPSGuid siteId = null;
        PSLocator owner = null;

        PSRelationshipConfig config = PSRelationshipCommandHandler.getRelationshipConfig(RECYCLED_TYPE);
        PSRelationshipProcessor m_relProc = PSRelationshipProcessor.getInstance();
        PSRelationshipFilter filter = new PSRelationshipFilter();
        List<PSRelationship> relSet = null;
        HashMap<IPSGuid,Boolean> matchedFolders = new HashMap<>();

        if(path.startsWith(PSRecycleService.SITES)) {
            owner = new PSLocator(PSFolder.SYS_SITES_ID, -1);
            siteId = pathids.get(1);

            filter.setOwner(owner);
            filter.setCategory(config.getCategory());

            relSet = m_relProc.getRelationshipList(filter);
            for (PSRelationship rel:relSet) {
                if(rel.getDependent().getId() == siteId.getUUID() && rel.getConfig().equals(config)){
                    matchedFolders.putIfAbsent(siteId,true);
                    owner = new PSLocator(siteId.getUUID(),-1);
                }
            }

        }else if(path.startsWith(PSRecycleService.ASSETS_ROOT)){
            owner = new PSLocator(PSFolder.SYS_ASSETS_ID, -1);
            if(pathids.size()>3)
            siteId = pathids.get(3);
        }else if(path.startsWith(PSRecycleService.RECYCLING_ROOT)){
            return;
        }



        PSLocator dependent=null;
        //Loop through folders
        for(int i = 0;i<pathids.size();i++) {
            int id = pathids.get(i).getUUID();
            if (id != PSFolder.SYS_SITES_ID &&
                    id != PSFolder.SYS_RECYCLING_ID &&
                    id != PSFolder.SYS_SYSTEM_FOLDER_ID &&
                    id != PSFolder.SYS_SYSTEM_TEMPLATES_FOLDER_ID &&
                    id != PSFolder.SYS_FOLDERS_ID &&
                    id != PSFolder.SYS_SYSTEM_USERPROFILES_FOLDER_ID
            ) {
                IPSGuid g = pathids.get(i);

                dependent = new PSLocator(g.getUUID(), -1);
                //Can't depend on your self.
                if (dependent.getId() != owner.getId()) {

                    filter.setOwner(owner);
                    filter.setDependent(dependent);
                    relSet = m_relProc.getRelationshipList(filter);
                    for (PSRelationship rel : relSet) {
                        int idj = rel.getDependent().getId();
                        if (idj != PSFolder.SYS_SITES_ID &&
                                idj != PSFolder.SYS_RECYCLING_ID &&
                                idj != PSFolder.SYS_SYSTEM_FOLDER_ID &&
                                idj != PSFolder.SYS_SYSTEM_TEMPLATES_FOLDER_ID &&
                                idj != PSFolder.SYS_FOLDERS_ID &&
                                idj != PSFolder.SYS_SYSTEM_USERPROFILES_FOLDER_ID
                        ) {
                            if (idj == g.getUUID() && rel.getConfig().equals(config)) {
                                matchedFolders.putIfAbsent(g, true);
                            }
                        }
                    }

                    if (null == matchedFolders.get(g)) {
                        if (owner.getId() != dependent.getId()) {
                            PSRelationship rel = new PSRelationship(-1, owner, dependent, config);
                            systemWs.saveRelationships(Collections.singletonList(rel));
                        } else {
                            log.debug("Tried to link recycled item to itself.");
                        }
                    }
                }
                if (owner == null) {
                    owner = new PSLocator(siteId.getUUID(), -1);
                } else {
                    if (dependent == null) {
                        dependent = new PSLocator(g.getUUID(), -1);
                    }
                    owner = dependent;
                }
            }
        }

    }

    @Override
    public void recycleItem(int dependentId) {

        PSRelationshipFilter filter = generateDependentFilter(dependentId, FOLDER_TYPE);
        List<PSRelationship> rels;
        rels = systemWs.loadRelationships(filter);
        PSRelationship folderRel = rels.get(0);
        IPSGuid itemGuid = idMapper.getGuid(folderRel.getDependent());
        String path="";
        try {

            path= itemSummaryService.find(itemGuid.toString(), FOLDER_TYPE).getFolderPaths().get(0);
            log.debug("Recycling item with relationship ID: {} and item id: {}",
                    folderRel.getId(),
                    dependentId);

            createRootSiteDeleteRelationship(path);
            // transition the item to archive state if necessary
            transitionWorkflowItem(folderRel, dependentId);
            PSFolderRelationshipCache cache = PSFolderRelationshipCache.getInstance();
            renameIfRequired(itemGuid, FOLDER_TYPE,false);
            updateRelationshipConfigId(RECYCLED_TYPE, folderRel);
            if (cache != null) {
                log.debug("The parent locators for the item are: {}", folderRel.getDependent());
                updateParentFolders(  idMapper.getGuid(folderRel.getDependent()), FOLDER_TYPE, RECYCLED_TYPE);
            }
            psContentEvent=new PSContentEvent(itemGuid.toString(),String.valueOf(dependentId),path, PSContentEvent.ContentEventActions.recycle, PSSecurityFilter.getCurrentRequest().getServletRequest(), PSActionOutcome.SUCCESS);
            psAuditLogService.logContentEvent(psContentEvent);
        } catch (PSErrorsException | PSErrorException | PSErrorResultsException | PSCmsException | IPSDataService.DataServiceLoadException e) {
            psContentEvent=new PSContentEvent(itemGuid.toString(),String.valueOf(dependentId),path, PSContentEvent.ContentEventActions.recycle,PSSecurityFilter.getCurrentRequest().getServletRequest(), PSActionOutcome.FAILURE);
            psAuditLogService.logContentEvent(psContentEvent);
            log.error("Unable to recycle item with dependent id: {} Error: {}",
                    dependentId, e.getMessage());
            log.debug(e.getMessage(),e);
        }
    }

    @Override
    public void recycleFolder(IPSGuid guid) {
        log.debug("Received guid for deleting a folder: {}", guid);
        PSFolderRelationshipCache cache = PSFolderRelationshipCache.getInstance();
        int folderId = idMapper.getContentId(guid);
        String path;

        try{
            path= itemSummaryService.find(guid.toString(), FOLDER_TYPE).getFolderPaths().get(0);

            createRootSiteDeleteRelationship(path);
            // create a filter to load the folder to be recycled
            PSRelationshipFilter filter = new PSRelationshipFilter();
            filter.setName(FOLDER_TYPE);
            filter.setCategory(FOLDER_CATEGORY);

            path= itemSummaryService.find(guid.toString(), FOLDER_TYPE).getFolderPaths().get(0);

            // check to see if there are any remaining items in the folder.
            // if there are, we leave the original folder.
            // do a check here to see if any folder relationships exist under the folder path.
            // active assembly relationships are allowed

            List<PSRelationship> childRels = null;
            boolean foundNavonType = false;
            if (cache != null) {
                PSLocator folderLocator = new PSLocator(folderId, -1);
                childRels = cache.getChildren(folderLocator, filter);
                for (PSRelationship rel : childRels) {
                    IPSCmsContentSummaries summaries = PSCmsContentSummariesLocator.getObjectManager();
                    PSComponentSummary summ = summaries.loadComponentSummary(rel.getDependent().getId());
                    if (summ.getContentTypeId() == navService.getNavonContentTypeId()) {
                        recycleItem(rel.getDependent().getId());
                        foundNavonType = true;
                    }
                }
            }
            if ((childRels != null && childRels.isEmpty()) || (foundNavonType && childRels.size() == 1)) {
                PSRelationshipFilter dependentFilter = generateDependentFilter(folderId, FOLDER_TYPE);
                List<PSRelationship> rels = systemWs.loadRelationships(dependentFilter);
                FoundTypeAndFolderIndex obj = checkForExistingRelType(rels, RECYCLED_TYPE, FOLDER_TYPE);
                PSRelationship rel = rels.get(obj.index);
                // create a new recycled relationship for the item when recycled
                // only create if there is not an existing recycle relationship for the item
                // because one may already exist if {@link PSRecycleService#updateParentRelationships}
                // created one first when deleting a child item.

                renameIfRequired(guid, FOLDER_TYPE, true);
                //We need to delete this relationship, not just update config id
                // as we already created a new realtionship for recycle folder
                systemWs.deleteRelationships(Collections.singletonList(rel.getGuid()));

            }
        } catch (PSErrorsException | PSErrorException | PSErrorResultsException | PSCmsException | IPSDataService.DataServiceLoadException e) {
            log.error("Error recycling folder with guid: {} Error:" ,guid, e.getMessage());
            log.debug(e.getMessage(),e);
        }
    }

    @Override
    public void restoreItem(String guid) {
        log.debug("Received guid for item restore: {}" , guid);
        int iGuid = idMapper.getContentId(guid);
        PSLocator depLocator = new PSLocator(iGuid, -1);
        // first set the item to be a folder rel instead of recycled rel.
        // this should bring back the relationship of the deleted item.
        // when restoring all types should be of recycled type.
        PSRelationshipFilter filter = generateDependentFilter(iGuid, RECYCLED_TYPE);
        try {
            List<PSRelationship> rels = systemWs.loadRelationships(filter);
            PSRelationship recycledRel = rels.get(0);
            IPSGuid itemGuid = idMapper.getGuid(recycledRel.getDependent());
            // second get parents of item.
            PSFolderRelationshipCache cache = PSFolderRelationshipCache.getInstance();
            List<PSLocator> parentLocators = cache.getParentLocators(depLocator);
            IPSGuid parentGuid = idMapper.getGuid(recycledRel.getOwner());
            renameIfRequired(itemGuid, RECYCLED_TYPE,false);
            updateRelationshipConfigId(FOLDER_TYPE, recycledRel);
            updateParentFolders(parentLocators, RECYCLED_TYPE,FOLDER_TYPE,cache );

        } catch (PSErrorsException | PSErrorException | PSErrorResultsException | PSCmsException e) {
            log.error("Error restoring item with guid: {} Error: {}" , guid, e.getMessage());
            log.debug(e.getMessage(),e);
        }
    }

    private void updateParentFolders(List<PSLocator> parentLocators, String originalType, String newType,PSFolderRelationshipCache cache) throws  PSErrorException {
        if (parentLocators == null) {
            return;
        }
        for (PSLocator parentLocator : parentLocators) {
            // < 300 are system level folders that would not need
            // to be updated
            if (parentLocator.getId() < 300) {
                continue;
            }

            PSRelationshipFilter filter = new PSRelationshipFilter();
            filter.setDependentId(parentLocator.getId());
            List<PSRelationship> rels = systemWs.loadRelationships(filter);
            PSRelationship folderRel = null;
            FoundTypeAndFolderIndex obj = checkForExistingRelType(rels, newType, originalType);
            while (-1 != obj.index){
                folderRel = rels.get(obj.index);

                if (!obj.foundType && folderRel != null) {
                    log.debug("Found a folder with id: " + parentLocator.getId() +
                            " that needs to be updated to new relationship of type: " + newType);
                    //If folder has more items, then create new relationship
                    //else update existing one.
                    PSRelationshipFilter childFilter = generateOwnerFilter(folderRel.getDependent().getId(), RECYCLED_TYPE);
                     saveNewRelationship(newType, folderRel);
                }
                List<PSLocator> pl = cache.getParentLocators(folderRel.getOwner());
                filter.setDependentId(folderRel.getOwner().getId());
                rels = systemWs.loadRelationships(filter);
                obj = checkForExistingRelType(rels, newType, originalType);
            }
        }
    }


    @Override
    public void restoreFolder(String guid) {
        log.debug("Received guid for delete folder: {}" , guid);
        PSFolderRelationshipCache cache = PSFolderRelationshipCache.getInstance();

        // load relationships for the folder being restored
        PSRelationshipFilter filter = generateDependentFilter(idMapper.getContentId(guid), RECYCLED_TYPE);
        try {
            List<PSRelationship> folderRels = systemWs.loadRelationships(filter);
            FoundTypeAndFolderIndex obj = checkForExistingRelType(folderRels, FOLDER_TYPE, RECYCLED_TYPE);
            PSRelationship folderRel = folderRels.get(obj.index);
            IPSGuid itemGuid = idMapper.getGuid(folderRel.getDependent());
            List<PSLocator> parentLocators = cache.getParentLocators(folderRel.getDependent());
            PSRelationshipFilter childFilter = generateOwnerFilter(idMapper.getContentId(guid), RECYCLED_TYPE);
            List<PSRelationship> childRels = systemWs.loadRelationships(childFilter);
            renameIfRequired(itemGuid, RECYCLED_TYPE,true);
            //We do this to make sure new folder is created if required under site
            updateRelationshipConfigId(FOLDER_TYPE, folderRel);
            updateParentFolders(parentLocators,RECYCLED_TYPE,FOLDER_TYPE,cache);
            restoreChildItems(childRels,cache);



        } catch (PSErrorsException | PSErrorException | PSErrorResultsException | PSCmsException e) {
            log.error("Error restoring folder with guid: {} Error: {}", guid, e.getMessage());
        }
    }

    /**
     * Creates a new relationship out of an existing relationship but of a new
     * relationship type.  This is the relationship type parameter supplied via the method.
     *
     * @param relationshipType the relationship type the new relationship will have.
     * @param newRel           the existing relationship which will be saved with the new relationship type.
     */
    private void saveNewRelationship(String relationshipType, PSRelationship newRel) throws PSErrorsException {
        log.debug("Saving a new relationship of type: " + relationshipType + " and RID of: " + newRel.getId());
        PSRelationshipConfig config = PSRelationshipCommandHandler.getRelationshipConfig(relationshipType);
        PSRelationship folderRel = new PSRelationship(-1, newRel.getOwner(), newRel.getDependent(), config);
        systemWs.saveRelationships(Collections.singletonList(folderRel));
    }


    private void renameFolder(PSCoreItem item, IPSGuid itemGuid, String originalRelTypeName) throws PSErrorException, PSCmsException {
        String folderName = item.getFieldByName("sys_title").getValue().getValueAsString();
        log.debug("Renaming folder with id: " + itemGuid + " and name: " + folderName);
        PSFolder folder = contentWs.loadFolder(itemGuid, false);
        folder.setName(folderName);
        folder.setFolderPath(item.getFolderPaths().get(0));
        contentWs.saveFolder(folder);
        // rename section title if required
        renameSectionTitle(itemGuid,originalRelTypeName,folderName);

    }
    private  void renameSectionTitle(IPSGuid itemGuid, String originalRelTypeName,String fenamedSectionName) throws PSCmsException {
        String tempguid = itemGuid.toString();
        PSRelationshipConfig config = PSRelationshipCommandHandler.getRelationshipConfig(originalRelTypeName);
        PSRelationshipProcessor m_relProc = PSRelationshipProcessor.getInstance();
        PSRelationshipFilter filter = new PSRelationshipFilter();
        List<PSRelationship> relSet = null;
        HashMap<IPSGuid,Boolean> matchedFolders = new HashMap<>();
        int folderId  = itemGuid.getUUID();
        filter.setOwnerId(folderId);
        filter.setCategory(config.getCategory());

        relSet = m_relProc.getRelationshipList(filter);

        for(PSRelationship relationship : relSet){
            PSLocator pathLocator = new PSLocator(relationship.getDependent().getId(), -1);
            IPSGuid sectionGuid =  idMapper.getGuid(pathLocator);
            IPSCmsContentSummaries summaries = PSCmsContentSummariesLocator.getObjectManager();
            PSComponentSummary summ = summaries.loadComponentSummary(relationship.getDependent().getId());
            if (summ.getContentTypeId() == navService.getNavonContentTypeId()) {
                List<String> temp = new ArrayList<>();
                temp.add(NAVON_FIELD_DISPLAYTITLE);
                Map<String, String> navProps = navService.getNavonProperties(sectionGuid, temp);
                navProps.put(NAVON_FIELD_DISPLAYTITLE,fenamedSectionName );
                navService.setNavonProperties(sectionGuid, navProps);
            }
        }

    }

    private void restoreChildItems(List<PSRelationship> childRels,PSFolderRelationshipCache cache) throws PSErrorException {
        for (PSRelationship child : childRels) {
            log.debug("Child flagged for restore to a folder is: {}" , child.getDependent().getId());
            if (child.getConfig().getName().equalsIgnoreCase(RECYCLED_TYPE)) {
                IPSCmsContentSummaries summaries = PSCmsContentSummariesLocator.getObjectManager();
                PSComponentSummary summ = summaries.loadComponentSummary(child.getDependent().getId());
                if (summ.getContentTypeId() == PSFolder.FOLDER_CONTENT_TYPE_ID) {
                    restoreFolder(idMapper.getGuid(child.getDependent()).toString());
                } else {
                    restoreItem(idMapper.getGuid(child.getDependent()).toString());
                }
            }
            PSRelationshipFilter childFilter = generateOwnerFilter(child.getDependent().getId(), RECYCLED_TYPE);
            List<PSRelationship> furtherChildren = systemWs.loadRelationships(childFilter);
            if (furtherChildren != null) {
                List<PSLocator> parentLoc = cache.getParentLocators(child.getDependent());
                List<PSLocator> childLoc = cache.getChildLocators(child.getDependent());
                updateParentFolders(parentLoc,RECYCLED_TYPE,FOLDER_TYPE,cache);
                updateParentFolders(childLoc,RECYCLED_TYPE,FOLDER_TYPE,cache);
                restoreChildItems(furtherChildren, cache);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<IPSItemSummary> findChildren(String path) {
        int itemId = getItemIdFromPath(path, RECYCLED_TYPE);
        List<IPSItemSummary> summs = new ArrayList<>();
        if (itemId != -1) {
            PSLocator pathLocator = new PSLocator(itemId, -1);
            IPSGuid guid = idMapper.getGuid(pathLocator);
            PSRelationshipFilter filter = new PSRelationshipFilter();
            filter.setCategory(PSRelationshipConfig.CATEGORY_RECYCLED);
            filter.setName(PSRelationshipConfig.TYPE_RECYCLED_CONTENT);
            List<IPSGuid> depGuids = systemWs.findDependents(guid, filter);
            for (IPSGuid depGuid : depGuids) {
                try {
                    String depId = idMapper.getString(depGuid);
                    IPSItemSummary itemSummary = itemSummaryService.find(depId, RECYCLED_TYPE);
                    summs.add(itemSummary);
                } catch (IPSDataService.DataServiceLoadException e) {
                    log.warn(e.getMessage());
                    log.debug(e.getMessage(),e);
                    //continue loop.
                }
            }
        }
        return summs;
    }

    @Override
    public IPSItemSummary findItem(String path) throws IPSDataService.DataServiceLoadException {
        int itemId = getItemIdFromPath(path, RECYCLED_TYPE);
        IPSItemSummary itemSummary = new PSDataItemSummary();
        if (itemId != -1) {
            PSLocator pathLocator = new PSLocator(itemId, -1);
            String guid = idMapper.getString(pathLocator);
            itemSummary = itemSummaryService.find(guid, RECYCLED_TYPE);
        }
        return itemSummary;
    }

    /***
     * Returns a boolean indicating if the specified guid is in the Recycler.
     * @param guid A valid guid to search for, never null
     * @return true if guid is in the recycler, false if not
     */
    @Override
    public boolean isInRecycler(String guid) {
        notEmpty(guid);
        boolean ret = false;
        IPSGuidManager m_gmgr= PSGuidManagerLocator.getGuidMgr();
        PSRelationshipProcessor m_relProc = PSRelationshipProcessor.getInstance();

        try {
            int id=0;
            try{
                id = ((PSLegacyGuid)m_gmgr.makeGuid(guid)).getContentId();
            }catch (Exception e){
                id=Integer.parseInt(guid);//This has been done in case some one case pass content ID directly
            }

            Collection<Integer> ids = new ArrayList<>();
            ids.add(id);

            PSRelationshipFilter filter = new PSRelationshipFilter();

            filter.setDependentIds(ids);
            filter.setCommunityFiltering(false);
            filter.setCategory(PSRelationshipFilter.FILTER_CATEGORY_RECYCLED);

            PSRelationshipSet relSet = m_relProc.getRelationships(filter);

            if(relSet.size()>0){
                ret = true;
            }
        } catch (PSCmsException e) {
            //NOTE: We may want to flip this logic and return true on error depending on behavior we see - NC
            log.warn("Unable to confirm if item: " + guid + " is in Recycler, assuming not. " + e.getMessage());
            log.debug("Recycler check failed with:",e);
        }

        return ret;

    }

    /***
     * Returns a boolean indicating if the specified guid is in the Recycler.
     * @param guid A valid guid to search for, never null
     * @return true if guid is in the recycler, false if not
     */
    @Override
    public boolean isNavInRecycler(String guid){
        return isInRecycler(guid);
    }

    /***
     * Returns a boolean indicating if the specified guid is in the Recycler.
     * @param guid A valid guid to search for, never null
     * @return true if guid is in the recycler, false if not
     */
    @Override
    public boolean isInRecycler(IPSGuid guid) {
        return isInRecycler(guid.toString());
    }

    /**
     * Updates relationship of a new
     * relationship type.  This is the relationship type parameter supplied via the method.
     *
     * @param relationshipType the relationship type the new relationship will have.
     * @param rel           the existing relationship which will be saved with the new relationship type.
     */
    private void updateRelationshipConfigId(String relationshipType, PSRelationship rel) throws PSErrorsException {
        log.debug("Saving a new relationship of type: " + relationshipType + " and RID of: " + rel.getId());
        PSRelationshipConfig config = PSRelationshipCommandHandler.getRelationshipConfig(relationshipType);
        rel.setConfig(config);
        //if relationship already exist, then delete it else update
        PSRelationshipProcessor relationshipProcessor = PSRelationshipProcessor.getInstance();
        PSRelationship existingRel = relationshipProcessor.checkIfRelationshipAlreadyExists(rel);
        if(existingRel == null) {
            systemWs.saveRelationships(Collections.singletonList(rel));
        }
    }

    private int getItemIdFromPath(String path, String relationshipTypeName) {
        PSRelationshipProcessor processor = PSRelationshipProcessor.getInstance();
        int itemId = -1;
        try {
            itemId = processor.getIdByPath(PSRelationshipProcessorProxy.RELATIONSHIP_COMPTYPE, path,
                    relationshipTypeName);
        } catch (PSCmsException e) {
            log.error("Error finding properties for item with path: {} Error: {}" , path, e.getMessage());
            log.debug(e.getMessage(),e);
        }
        return itemId;
    }

    /**
     * For each parent located, it checks to see if there is a relationship
     * of type recycled content.  If there is not, it creates a new relationship
     * of that type so that the {@link com.percussion.pathmanagement.service.impl.PSRecyclePathItemService}
     * can return the items under its path.
     *
     * @param parentGuid the parent locators of the item being deleted.
     */
    private void updateParentFolders(IPSGuid parentGuid, String originalType, String newType) throws PSErrorsException, PSErrorException {
        // < 300 are system level folders that would not need
        // to be updated

        if (parentGuid == null || parentGuid.getUUID() < 300) {
            return;
        }

        PSRelationshipFilter filter = new PSRelationshipFilter();
        filter.setDependentId(parentGuid.getUUID());
        List<PSRelationship> rels = systemWs.loadRelationships(filter);
        PSRelationship folderRel = null;
        FoundTypeAndFolderIndex obj = checkForExistingRelType(rels, newType, originalType);
        if (-1 != obj.index)
            folderRel = rels.get(obj.index);
        if (!obj.foundType && folderRel != null) {
            log.debug("Found a folder with id: " + parentGuid.getUUID() +
                    " that needs to be updated to new relationship of type: " + newType);
            //If folder has more items, then create new relationship
            //else update existing one.
            PSRelationshipFilter childFilter = generateOwnerFilter(folderRel.getDependent().getId(), originalType);
            List<PSRelationship> childRels = systemWs.loadRelationships(childFilter);
            if(childRels.size() > 0){
                saveNewRelationship(newType,folderRel);
            }else {
                updateRelationshipConfigId(newType, folderRel);
            }
        }

    }

    private void transitionWorkflowItem(PSRelationship rel, int dependentId) {
        Set<String> ids = new HashSet<>();
        String id = idMapper.getString(rel.getDependent());
        ids.add(id);

        PSItemStateTransition trans = workflowHelper.getTransitions(id);

        //Could be a non workflowable item with no workflow - just skip in that case.
        if(trans != null) {
            boolean foundQuickEditTrigger = false;
            for (String transition : trans.getTransitionTriggers()) {
                if (transition.equalsIgnoreCase(IPSItemWorkflowService.TRANSITION_TRIGGER_EDIT)) {
                    foundQuickEditTrigger = true;
                }
            }

            if (foundQuickEditTrigger) {
                log.debug("Found an item that needs to be archived when recycled: " + dependentId);
                try {
                    PSItemStatus status = contentWs.prepareForEdit(idMapper.getGuid(rel.getDependent()));
                    PSWebserviceUtils.transitionItem(dependentId, IPSItemWorkflowService.TRANSITION_TRIGGER_ARCHIVE,
                            "Recycle", null);
                    contentWs.releaseFromEdit(status, true);
                } catch (PSErrorException e) {
                    log.error("Error transitioning item to archive with id: " + dependentId, e);
                }
            }
        }
    }

    private FoundTypeAndFolderIndex checkForExistingRelType(List<PSRelationship> rels, String checkType, String convertType) {
        boolean foundExistingRel = false;
        int index = 0;
        int foundConvertType = -1;
        for (PSRelationship relationship : rels) {
            if (relationship.getConfig().getName().equalsIgnoreCase(checkType)) {
                foundExistingRel = true;
            } else if (relationship.getConfig().getName().equalsIgnoreCase(convertType)) {
                foundConvertType = index;
            }
            index++;
        }
        return new FoundTypeAndFolderIndex(foundConvertType, foundExistingRel);
    }

    /**
     * Generates a relationship filter based on dependent id.
     * @param dependentId the id of the dependent item :)
     * @param relationshipTypeName the type of relationship to generate with the filter.
     * @return a filter for filtered relationships.
     */
    private PSRelationshipFilter generateDependentFilter(int dependentId, String relationshipTypeName) {
        PSRelationshipFilter filter = new PSRelationshipFilter();
        filter.setCommunityFiltering(false);
        filter.setName(relationshipTypeName);
        filter.setDependentId(dependentId);
        return filter;
    }

    /**
     * Generates a relationship filter based on ownerId.
     * @param ownerId the id of the dependent item :)
     * @param relationshipTypeName the type of relationship to generate with the filter.
     * @return a filter for filtered relationships.
     */
    private PSRelationshipFilter generateOwnerFilter(int ownerId, String relationshipTypeName) {
        PSRelationshipFilter filter = new PSRelationshipFilter();
        filter.setCommunityFiltering(false);
        filter.setName(relationshipTypeName);
        filter.setOwnerId(ownerId);
        return filter;
    }

    /**
     * Checks if an item exists in another location with the same name.  I.E.
     * it will check if an item in the recycling bin with name 'foo-bar.html'
     * exists under //Sites.  Conversely, can be used to check if an item being
     * deleted from //Sites has an item with a name that already exists under //Recycling
     * bin.  Needed to determine if a 'foo-bar-1.html' is needed, for example, when sending
     * an item to a new location.
     * <p>
     * This method will return a {@link PSCoreItem} or <code>null</code> if there is no change in path or name.
     * If the name is the same, it means that the destination location does not already contain an item with the
     * same name (and path) and no rename action is necessary.  If a different name (or path) is returned than
     * this means a new name is required prior to saving the item to the new location.
     *
     * @param guid                  the guid of the current item to check against.
     * @param originalRelTypeName   the current relationship of the item (i.e. recycled).
     * @return a {@link PSCoreItem} if some information related to the item has changed
     * or <code>null</code> if there has been no change to the item.
     */

    private void renameIfRequired(IPSGuid guid, String originalRelTypeName,boolean folder) throws PSErrorResultsException, PSCmsException {
        List<PSCoreItem> items = contentWs.loadItems(Collections.singletonList(guid), false,
                false, false, true, false, originalRelTypeName);

        if(items == null || items.isEmpty()){
            return ;
        }

        PSCoreItem coreItem =items.get(0);
        List<String> paths = coreItem.getFolderPaths();
        if(paths == null || paths.isEmpty()){
            return ;
        }
        String folderPaths = paths.get(0);
        log.debug("Folder paths for the loaded item are: " + folderPaths);
        if(!checkValidPathPrefix(folderPaths)){
            return;
        }
        StringBuilder sb = new StringBuilder();
        IPSFieldValue itemName = coreItem.getFieldByName("sys_title").getValue();
        boolean isRecycledType = folderPaths.startsWith(RECYCLING_ROOT);

        if (isRecycledType) {
            // we strip off recycling from prefix
            sb.append(folderPaths.replaceFirst("//Folders/\\$System\\$/Recycling", ""));
            sb.insert(0, "/");
            sb = attachPathPrefix(sb);
        } else {
            sb.append(folderPaths);
            sb = attachPathPrefix(sb);
            if (sb.charAt(1) == '/') {
                sb.replace(0, 1, ""); // remove first slash
            }
            sb.insert(0, RECYCLING_ROOT);
        }

        sb.append("/" + itemName.getValueAsString());

        /**
         * This code renames the name of the item being restored/recycled -- appending -1, -2 to the file name, etc.
         * There were issues with this when checking against folders, etc.  For example, if a folder is moved to the bin
         * and a new folder with same path is created.  When this occurs, recycling an item would update the item's parent
         * folders to have a recycled relationship.  Since the new folder being recycled has a different content id
         * it was allowed to create the new recycled relationship for the folder with the same name and caused issues
         * in the UI.  E.G. the path would display '2 items' but only 1 was displayed due to filtering of items with same name.
         * this code would need to be fixed and the logic updated.
         *
         */
        int idToCheck = getItemIdFromPath(sb.toString(), !isRecycledType ? RECYCLED_TYPE : FOLDER_TYPE);

        int counter = 1;
        while (idToCheck != -1 && idToCheck != coreItem.getContentId()) {
            if (counter > 1) {
                int index = sb.lastIndexOf("-");
                sb.replace(index, sb.length(), "");
                sb.append("-" + counter);
            } else {
                sb.append("-" + counter);
            }
            log.debug("Checking for item: " + sb.toString() + " to see if it exists for rename.");
            idToCheck = getItemIdFromPath(sb.toString(), !isRecycledType ? RECYCLED_TYPE : FOLDER_TYPE);
            counter++;
        }
        String relationshipTypeName = originalRelTypeName;
        if(RECYCLED_TYPE.equals(originalRelTypeName)){
            relationshipTypeName = FOLDER_TYPE;
        }else{
            relationshipTypeName = RECYCLED_TYPE;
        }

        int indexOfName = sb.lastIndexOf("/");
        String newName = sb.substring(indexOfName + 1);
        String newPath = sb.substring(0, indexOfName);
        if (!newName.equalsIgnoreCase(itemName.getValueAsString())) {
            log.debug("The item will need to be saved as its path or name have changed.");
            if (!folder) {
                contentWs.prepareForEdit(guid);
                if(items.size()>0 && items.get(0).getItemDefinition().getName().equalsIgnoreCase("percFileAsset")){
                    items = contentWs.loadItems(Collections
                            .singletonList(guid), true, true, false, false);
                }else{
                    items = contentWs.loadItems(Collections
                            .singletonList(guid), false, false, false, false);
                }
                coreItem = items.get(0);
            }
            coreItem.setTextField("sys_title", newName);
            if (folder) {
                renameFolder(coreItem, guid,originalRelTypeName);
            } else {
                contentWs.saveItems(Arrays.asList(coreItem), false, false, (IPSGuid) null, relationshipTypeName);
            }
        }
    }

    public static boolean checkValidPathPrefix(String sb) throws IllegalArgumentException {
        String currentPath = sb.toString();
        log.debug("Looking for the correct path prefix.  The path is: " + currentPath);
        if (currentPath.startsWith(SITES)) {
            return true;
        } else if (currentPath.startsWith("//" + ASSETS)) {

            return true;
        } else if (currentPath.startsWith(ASSETS_ROOT)) {

            return true;
        } else if (currentPath.startsWith(FOLDERS)){
            return true;
        }
        return false;
    }



    public static StringBuilder attachPathPrefix(StringBuilder sb) throws IllegalArgumentException {
        String currentPath = sb.toString();
        log.debug("Looking for the correct path prefix.  The path is: " + currentPath);
        if (currentPath.startsWith(SITES)) {
            return sb;
        } else if (currentPath.startsWith("//" + ASSETS)) {
            sb.replace(0, 1, ""); // remove the first slash
            return sb.insert(0, FOLDERS + "/" + SYSTEM);
        } else if (currentPath.startsWith(ASSETS_ROOT)) {
            int index = currentPath.indexOf("Assets");
            return sb.replace(0, index - 1, "");
        } else if (currentPath.startsWith(FOLDERS)){
            return sb.insert(0, FOLDERS);
        }
        else {
            throw new IllegalArgumentException("Path must start with //Sites, //Folders, or //Recycling.");
        }
    }

    /**
     * Class used to help identify if a particular relationship exists amongst
     * relationships.  For example, if looking for a recycled_type relationship,
     * this class can be used to store whether or not that type exists.
     * <p>
     * The index can be used to return the index of the folder_type relationship.  -1 can
     * be returned if a folder relationship does not exist.
     */
    private class FoundTypeAndFolderIndex {
        private boolean foundType;
        private int index;

        /**
         * CTOR to set the index and whether or not the relationship type was found.
         *
         * @param index     the index of the found folder relationship.  -1 if not found.
         * @param foundType <code>true</code> if the relationship type was found.
         */
        FoundTypeAndFolderIndex(int index, boolean foundType) {
            this.foundType = foundType;
            this.index = index;
        }
    }
}
