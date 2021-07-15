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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.pagemanagement.service.impl;

import com.percussion.assetmanagement.data.PSReportFailedToRunException;
import com.percussion.assetmanagement.service.IPSWidgetAssetRelationshipService;
import com.percussion.auditlog.PSActionOutcome;
import com.percussion.auditlog.PSAuditLogService;
import com.percussion.auditlog.PSContentEvent;
import com.percussion.cms.IPSConstants;
import com.percussion.cms.objectstore.PSCoreItem;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.itemmanagement.service.IPSItemWorkflowService;
import com.percussion.itemmanagement.service.IPSItemWorkflowService.PSItemWorkflowServiceException;
import com.percussion.pagemanagement.dao.IPSPageDao;
import com.percussion.pagemanagement.dao.IPSPageDaoHelper;
import com.percussion.pagemanagement.dao.IPSTemplateDao;
import com.percussion.pagemanagement.data.PSNonSEOPagesRequest;
import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.data.PSPageChangeEvent;
import com.percussion.pagemanagement.data.PSPageChangeEvent.PSPageChangeEventType;
import com.percussion.pagemanagement.data.PSPageReportLine;
import com.percussion.pagemanagement.data.PSPageSummary;
import com.percussion.pagemanagement.data.PSRegion;
import com.percussion.pagemanagement.data.PSRegionTreeUtils;
import com.percussion.pagemanagement.data.PSRegionWidgetAssociations;
import com.percussion.pagemanagement.data.PSSEOStatistics;
import com.percussion.pagemanagement.data.PSTemplate;
import com.percussion.pagemanagement.service.IPSPageCatalogService;
import com.percussion.pagemanagement.service.IPSPageChangeListener;
import com.percussion.pagemanagement.service.IPSPageService;
import com.percussion.pagemanagement.service.IPSPageTemplateService;
import com.percussion.pagemanagement.service.IPSWidgetService;
import com.percussion.pathmanagement.data.PSPathItem;
import com.percussion.pathmanagement.service.IPSPathService;
import com.percussion.pathmanagement.service.impl.PSPathUtils;
import com.percussion.queue.IPSPageImportQueue;
import com.percussion.queue.impl.PSSiteQueue;
import com.percussion.recent.service.rest.IPSRecentService;
import com.percussion.recycle.service.IPSRecycleService;
import com.percussion.recycle.service.impl.PSRecycleService;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.guidmgr.PSGuidUtils;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.IPSItemEntry;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.servlets.PSSecurityFilter;
import com.percussion.share.dao.IPSContentItemDao;
import com.percussion.share.dao.IPSFolderHelper;
import com.percussion.share.dao.PSFolderPathUtils;
import com.percussion.share.data.IPSItemSummary;
import com.percussion.share.data.PSNoContent;
import com.percussion.share.data.PSPagedItemList;
import com.percussion.share.data.PSPagedObjectList;
import com.percussion.share.data.PSUnassignedResults;
import com.percussion.share.data.PSUnassignedResults.ImportStatus;
import com.percussion.share.data.PSUnassignedResults.ItemStatus;
import com.percussion.share.data.PSUnassignedResults.UnassignedItem;
import com.percussion.share.data.PSUnassignedResults.UnassignedItemList;
import com.percussion.share.service.IPSDataItemSummaryService;
import com.percussion.share.service.IPSIdMapper;
import com.percussion.share.service.PSAbstractDataService;
import com.percussion.share.service.PSSiteCopyUtils;
import com.percussion.share.service.exception.PSBeanValidationException;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.share.service.exception.PSParameterValidationUtils;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.share.validation.PSAbstractBeanValidator;
import com.percussion.share.validation.PSValidationErrors;
import com.percussion.share.validation.PSValidationErrorsBuilder;
import com.percussion.sitemanage.dao.IPSiteDao;
import com.percussion.sitemanage.data.PSSiteSummary;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.request.PSRequestInfo;
import com.percussion.webservices.PSErrorResultsException;
import com.percussion.webservices.content.IPSContentDesignWs;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.publishing.IPSPublishingWs;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.percussion.pagemanagement.data.PSTemplate.PSTemplateTypeEnum.UNASSIGNED;
import static com.percussion.pathmanagement.service.impl.PSSitePathItemService.SITE_ROOT;
import static com.percussion.share.service.exception.PSParameterValidationUtils.validateParameters;
import static com.percussion.share.spring.PSSpringWebApplicationContextUtils.getWebApplicationContext;
import static org.apache.commons.lang.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.removeStart;
import static org.apache.commons.lang.Validate.isTrue;
import static org.apache.commons.lang.Validate.notNull;

/**
 * CRUDS pages.
 * 
 * @author adamgent
 * @author yubingchen
 */
@Component("pageService")
@Lazy
public class PSPageService extends PSAbstractDataService<PSPage, PSPage, String> implements IPSPageService
{
    
    private IPSPageDao pageDao;
    
    /**
     * The page dao helper. Initialized by constructor, never
     * <code>null</code> after that.
     */
    private IPSPageDaoHelper pageDaoHelper;
    
    /**
     * The content web-service. Initialized by constructor, never
     * <code>null</code> after that.
     */
    private IPSContentWs contentWs;

    /**
     * Used for folder item operations. Initialized in ctor, never
     * <code>null</code> after that.
     */
    private IPSFolderHelper folderHelperWs;

    /**
     * The content design web-service. Initialized by constructor, never
     * <code>null</code> after that.
     */
    private IPSContentDesignWs contentDesignWs;

    /**
     * The id mapper, initialized by constructor, never <code>null</code> after
     * that.
     */
    private IPSIdMapper idMapper;
    
    /**
     * The publishing webservice, initialized by constructor, never <code>null</code> after
     * that.
     */
    
    private IPSPublishingWs publishingWs;
    
    private List<IPSPageChangeListener> pageChangeListeners = new ArrayList<>();
    /**
     * The widget asset relationship service. Initialized by constructor, never <code>null</code> after that.
     */
    private IPSWidgetAssetRelationshipService widgetAssetRelationshipService;
    
    /**
     * The IPSItemWorkflowService. Initialized by constructor, never <code>null</code> after that.
     */  
    private IPSItemWorkflowService itemWorkflowService;

    private IPSContentItemDao contentItemDao;
    
    private IPSTemplateDao templateDao;
    
    private IPSPageCatalogService catalogService;
    
    private IPSPageImportQueue pageImportQueue;
    
    private IPSiteDao siteDao;
    
    private IPSPageTemplateService pageTemplateService;

    private IPSRecentService recentService;

    private IPSDataItemSummaryService dataItemSummaryService;
    
    private PSUnassignedPagesDataHelper unassignedPagesHelper = new PSUnassignedPagesDataHelper();

    private IPSRecycleService recycleService;
    
    private final String PATH_SEPARATOR = "/";

    private static final String RECYCLED_TYPE = PSRelationshipConfig.TYPE_RECYCLED_CONTENT;

    private static final String FOLDER_TYPE = PSRelationshipConfig.TYPE_FOLDER_CONTENT;

    private static final String RECYCLING_ROOT = PSRecycleService.RECYCLING_ROOT;
    private PSAuditLogService psAuditLogService=PSAuditLogService.getInstance();
    private PSContentEvent psContentEvent;

    @Autowired
    public PSPageService(IPSFolderHelper folderHelperWs, IPSContentDesignWs contentDesignWs, IPSContentWs contentWs,
                         IPSIdMapper idMapper, IPSPageDao pageDao, IPSWidgetAssetRelationshipService widgetAssetRelationshipService,
                         IPSWidgetService widgetService, IPSItemWorkflowService itemWorkflowService, IPSPublishingWs publishingWs,
                         IPSPageDaoHelper pageDaoHelper, IPSContentItemDao contentItemDao, @Qualifier("templateDao") IPSTemplateDao templateDao,
                         IPSiteDao siteDao, IPSPageTemplateService pageTemplateService, IPSRecycleService recycleService,
                         IPSDataItemSummaryService dataItemSummaryService)
             
    {
        super(pageDao);
        this.pageDaoHelper = pageDaoHelper;
        this.folderHelperWs = folderHelperWs;
        this.contentDesignWs = contentDesignWs;
        this.contentWs = contentWs;
        this.idMapper = idMapper;
        this.pageDao = pageDao;
        this.widgetAssetRelationshipService = widgetAssetRelationshipService;
        this.itemWorkflowService = itemWorkflowService;
        this.publishingWs  = publishingWs;
        this.contentItemDao = contentItemDao;
        this.templateDao = templateDao;
        this.siteDao = siteDao;
        this.pageTemplateService = pageTemplateService;
        this.recycleService = recycleService;
        this.dataItemSummaryService = dataItemSummaryService;

        pageValidator = new PSPageValidator();
        regionWidgetAssocationsValidator = new PSPageRegionWidgetValidator(widgetService);
        addPageChangeListener(new PSPageChangeHandler());
    }

    /**
     * 
     * @{inheritDoc}
     */
    @Override
    public void delete(String id) throws PSValidationException {
       // List<IPSSite> sites = folderHelper.getItemSites(id);  
        List<IPSSite> sites = publishingWs.getItemSites(idMapper.getGuid(id));
        if((sites != null) && (sites.size() > 1))
        { 
           PSSiteCopyUtils.throwCopySiteMessageIfNotAllowed(sites.get(0).getName(), "delete", 
                                                          PSSiteCopyUtils.CAN_NOT_DELETE_PAGE);
        }
        delete(id, false);
    }

    public String copy(String id, boolean addToRecent) throws PSDataServiceException, IPSPathService.PSPathNotFoundServiceException {
        return copy(id, null, addToRecent);
    }

    /**
     *    Copies a page given its ID. Returns ID of copied page
     */
    public String copy(String id, String targetFolder, boolean addToRecent) throws PSDataServiceException, IPSPathService.PSPathNotFoundServiceException {
        PSPage page = null;
        page = load(id);
        String path = page.getFolderPath();

        //Allow for copying to new folder
        if(targetFolder != null)
            path = targetFolder;

        String base = page.getName();
        IPSGuid guid = idMapper.getGuid(id);
        String suffix = "copy";

        String newName = folderHelperWs.getUniqueNameInFolder(path, base, suffix, 1, true);
        
        ArrayList<IPSGuid> guids = new ArrayList<>();
        guids.add(guid);
        ArrayList<String> paths = new ArrayList<>();
        paths.add(path);
        
        List<PSCoreItem> items;
        try
        {
            // TODO: Change NewCopy to its constant
            items = contentWs.newCopies(guids, paths, "NewCopy",false, true);
        }
        catch (Exception ae)
        {
            String msg = "Failed to copy page  \"" + base + "\".";
            log.error(msg, ae);
            return null;
        }

        PSCoreItem newPageCoreItem = items.get(0);
        
        PSLocator locator = (PSLocator)newPageCoreItem.getLocator();
        String newPageId = idMapper.getString(locator);

        try {
            itemWorkflowService.checkOut(newPageId);
        } catch (PSItemWorkflowServiceException e) {
            log.warn(e.getMessage());
            log.debug(e.getMessage(),e);
        }
        PSPage newPage = load(newPageId);
        newPage.setName(newName);
        newPage.setWorkflowId(page.getWorkflowId());
        save(newPage);
        if(addToRecent)
            recentService.addRecentItem(newPage.getId());

        try {
            itemWorkflowService.checkIn(newPageId);
        } catch (PSItemWorkflowServiceException e) {
            log.warn(e.getMessage());
            log.debug(e.getMessage(),e);
        }
        log.info("newPageId: {}",newPageId);
        
        String pagePath = newPage.getFolderPath();
        pagePath += "/" + newName;
        
        //second parameter is a list of widgets not to remove.
        widgetAssetRelationshipService.removeAssetWidgetRelationships(newPageId, Collections.EMPTY_LIST);
        widgetAssetRelationshipService.copyAssetWidgetRelationships(id, newPageId);
        
        PSTemplate pageTemplate = templateDao.find(page.getTemplateId());
        if (pageTemplate.getType() != null && pageTemplate.getType().equals(UNASSIGNED.toString())){
            IPSPageImportQueue pageQueue = getPageImportQueue(); 
            PSSiteSummary siteSummary = siteDao.findByPath(pagePath);
            pageQueue.dirtySiteQueue(siteSummary.getSiteId());
        }
        return pagePath;
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public void delete(String id, boolean force) throws PSValidationException {
        // last pararm is false because of recycle feature.  not purging is recycling.
        delete(id, force, false);
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public void delete(String id, boolean force, boolean purgeItem) throws PSValidationException {
        PSValidationErrorsBuilder builder = validateParameters("delete").rejectIfBlank("id", id).throwIfInvalid();
        List<IPSSite> sites = publishingWs.getItemSites(idMapper.getGuid(id));
        if((sites != null) && (sites.size() > 1))
        {
            PSSiteCopyUtils.throwCopySiteMessageIfNotAllowed(sites.get(0).getName(), "delete",
                    PSSiteCopyUtils.CAN_NOT_DELETE_PAGE);
        }
        try
        {
            String currentUser = (String) PSRequestInfo.getRequestInfo(PSRequestInfo.KEY_USER);
            StringBuilder purgeItemPaths = new StringBuilder();
            if (purgeItem) {
                IPSItemSummary summ = dataItemSummaryService.find(id, RECYCLED_TYPE);
                purgeItemPaths.append(summ.getFolderPaths().get(0).replaceFirst("//Folders/\\$System\\$", ""));
                purgeItemPaths.append("/");
                purgeItemPaths.append(summ.getName());
            }

            PSPage page = super.load(id);

            // when an item is being purged the page object won't be loaded properly
            // at the moment as it is in the recycle bin and is not being looked up
            // via a RecycledContent relationship.  Skip for this use case.
            if (!force && !purgeItem)
            {
                validateForDelete(page, builder);
            }

            if (purgeItem) {
                pageDao.delete(id, force);
                log.info("Page: '{}' has been deleted by: {}",purgeItemPaths,  currentUser);

            } else {
                IPSGuid guid = idMapper.getGuid(page.getId());
                int dependentId = idMapper.getContentId(guid);
                recycleService.recycleItem(dependentId);
                log.info("Page: '{}' has been recycled by: {}",page.getFolderPath() + '/' + page.getName(),  currentUser);
            }
        }
        catch (PSItemWorkflowServiceException | PSNotFoundException | PSDataServiceException e)
        {
            log.error("Page: {} not found for delete. Error: {}",id,e.getMessage());
            log.debug(e.getMessage(), e);
        }
    }
    
    /**
     * 
     * @{inheritDoc}
     */
    public PSPage find(String id) throws DataServiceLoadException, DataServiceNotFoundException, PSValidationException {
        return super.load(id);
    }

    public PSPage findPageByPath(String fullPath) throws PSPageException, PSValidationException {
        PSParameterValidationUtils.validateParameters("findPageByFullFolderPath")
            .rejectIfNull("fullPath", fullPath)
            .throwIfInvalid();
        fullPath = "//" + removeStart(fullPath, "//");
        return pageDao.findPageByPath(fullPath);
    }

 

    public boolean isPageItem(String id) throws PSPageException {
        if (isBlank(id))
            return false;
        
        PSLocator loc = new PSLocator(idMapper.getGuid(id).getUUID());
        PSItemDefManager defMgr = PSItemDefManager.getInstance();
        long ctTypeId = defMgr.getItemContentType(loc);
        return ctTypeId == pageDao.getPageContentTypeId();
    }
    

    @Override
    public void updateTemplateMigrationVersion(String pageId) throws PSDataServiceException {
        PSPage page = find(pageId);
        PSTemplate template = templateDao.find(page.getTemplateId());
        if (template == null)
            throw new PSPageException("Failed to locate template with id: " + page.getTemplateId());
        
        page.setTemplateContentMigrationVersion(template.getContentMigrationVersion());
        super.save(page);        
    }
    
    /**
     * 
     * @{inheritDoc}
     */
    @Override
    public PSPagedItemList findPagesByTemplate(String templateId,
            Integer startIndex, Integer maxResults,
            String sortColumn, String sortOrder,
            String pageId)
            throws PSDataServiceException {
        //Check the required parammeters are not null
        notNull(templateId, "templateId is required");
        
        // Get the page ids for all the pages using the template.
        
        List<Integer> pageIds = pageTemplateService.findPageIdsByTemplate(templateId);       
        
        // Result list sorter.
        CompareItemEntry compare = null;
        // Set the default values for the sorter in case parammeters are null.
        String sortingColumn = sortColumn;
        String sortingOrder = sortOrder;
        if (sortingColumn == null || sortingColumn.isEmpty())
        {
            sortingColumn = "name";
        }
        if (sortingOrder == null || sortingOrder.isEmpty())
        {
            sortingOrder = "asc";
        }
        compare = new CompareItemEntry(sortingColumn, sortingOrder);

        // PSPathUtils.sort(items, sortColumn, sortOrder);
        // Get all the pages (sorted) using the template from the object cache.
        IPSCmsObjectMgr cmsObjectMgr = PSCmsObjectMgrLocator.getObjectManager();
        List<IPSItemEntry> allPageEntries = cmsObjectMgr.findItemEntries(pageIds, compare);
        
        PSPagedObjectList<IPSItemEntry> pageGroup = null;
        Integer itemStartIndex = null;
        if (pageId != null)
        {
            IPSGuidManager mgr = PSGuidManagerLocator.getGuidMgr();
            int pageContentId = mgr.makeLocator(idMapper.getGuid(pageId)).getId();
            
            itemStartIndex = getStartIndexForItem(allPageEntries, maxResults, pageContentId);
        }

        // Get the page of pages.
        if (itemStartIndex == null)
        {
            pageGroup = PSPagedObjectList.getPage(allPageEntries, startIndex, maxResults);
        }
        else
        {
            pageGroup = PSPagedObjectList.getPage(allPageEntries, itemStartIndex, maxResults);
        }

        // Convert items to PSPathItem objects
        List<IPSItemEntry> pagedItemEntries = pageGroup.getChildrenInPage();
        Integer resultingStartIndex = pageGroup.getStartIndex();

        // Get page of Path Items
        List<PSPathItem> itemsInPage = new ArrayList<>();
        for (IPSItemEntry pageEntry : pagedItemEntries)
        {
            // Get path for each page entry.
            IPSGuid myGuid = PSGuidUtils.makeGuid(Long.valueOf(pageEntry.getContentId()), PSTypeEnum.LEGACY_CONTENT);
            String[] pagePaths = contentWs.findFolderPaths(myGuid);
            String path = "";
            if (pagePaths != null && pagePaths.length > 0)
            {
                path = pagePaths[0] + "/" + pageEntry.getName();
            }

            // Create path item and set name and path to it.
            PSPathItem pathItem = new PSPathItem();
            PSPage page = pageDao.find(myGuid.toString());
            pathItem.setName(page.getName());
            pathItem.setPath(path);
            pathItem.setRelatedObject(pageEntry);
            pathItem.setId(myGuid.toString());
            pathItem.setTypeProperty("contentMigrationVersion", page.getTemplateContentMigrationVersion());
            String temp =  page.isMigrationEmptyWidgetFlag()?"yes":"no";
            pathItem.setTypeProperty("migrationEmptyWidgetFlag", temp);
            itemsInPage.add(pathItem);
        }
        
        // Create Paged Item List
        PSPagedItemList pagesByTemplatePagedList = new PSPagedItemList(itemsInPage, allPageEntries.size(),
                resultingStartIndex);
        
        if (allPageEntries.size() > 0)
        {
            IPSGuid myGuid = PSGuidUtils.makeGuid(Long.valueOf(allPageEntries.get(0).getContentId()), PSTypeEnum.LEGACY_CONTENT);
                    
            pagesByTemplatePagedList.setFirstItemId(myGuid.toString());
        }
        return pagesByTemplatePagedList;
    }


    /**
     * REST API to get the import status for cataloged pages.
     * 
     * @param sitename {@link String} with the name of the site. Must not be
     *            <code>null</code> nor empty.
     * @param startIndex {@link Integer} with the start index. The first item is
     *            1. If the value is <code>null</code>, or less than 1, it will
     *            be changed to 1.
     * @param maxResults {@link Integer} indicating the maximum amount of
     *            results to return. May be <code>null</code>, but if it isn't
     *            <code>null</code>, it must be greater than 0.
     * @return {@link PSUnassignedResults} with the results, never
     *         <code>null</code>.
     * @throws Exception 
     */
    @Override
    public PSUnassignedResults getUnassignedPagesBySite(String sitename,
            Integer startIndex, Integer maxResults) throws PSPageException {
        try
        {
            return unassignedPagesHelper.getUnassignedResults(sitename, startIndex, maxResults);
        }
        
        catch (Exception e)
        {
            String errMsg = "Failed to get the import status for cataloged pages.";
            log.error(errMsg);
            log.debug(e);
            throw new PSPageException(errMsg, e); 
        }
    }

    public PSNoContent clearMigrationEmptyFlag(String pageid) throws PSDataServiceException {
        updateMigrationEmptyWidgetFlag(pageid, false);
        return new PSNoContent("Successfully cleared migration flag");
    }
    
    public Boolean getMigrationEmptyFlag(String pageid) throws DataServiceLoadException, DataServiceNotFoundException, PSValidationException {
        return getMigrationEmptyWidgetFlag(pageid);
    }
    
    /**
     * Finds the startIndex (first index of the paging group) that contains the page
     * with the id sent
     * 
     * @param allItems The list of items to search the corresponding item
     *            never <code>null</code>.
     * @param maxResults The results page size
     *            , never <code>null</code> and equal or greater than 1.
     * @param childPageId The page item id to find in the list to get the correct page,
     *          never <code>null</code>.
     *          
     * @return Integer the start index of the page group that contains the item
     */
    private Integer getStartIndexForItem(List<IPSItemEntry> allItems, Integer maxResults, Integer childPageId)
    {
        Validate.notNull(allItems, "allItems list cannot be null");
        Validate.notNull(maxResults, "maxResults cannot be null nor lesser than 1");
        Validate.isTrue(maxResults >= 1, "maxResults cannot be lesser than 1");
        Validate.notNull(childPageId, "childPageId cannot be null nor empty");
        
        Integer childIndex = getChildIndex(childPageId, allItems);

        // Didn't find the item, return null
        if (childIndex == null)
            return null;
        
        Integer startIndex = childIndex - (childIndex % maxResults) + 1;
        
        return startIndex;
    }

    /**
     * Finds the index of the item in the list.
     * It compares using the contentId for the page
     * 
     * @param childPageId The page item id to find,
     *          never <code>null</code>.
     * @param list The list of items to search the corresponding item,
     *            never <code>null</code>.
     *          
     * @return Integer the index of the item in the list
     */
    private Integer getChildIndex(int childPageId, List<IPSItemEntry> list)
    {
        IPSItemEntry item;
        int index = 0;
        
        for (index=0; index<list.size(); index++)
        {
            item = list.get(index);
            
            if (item.getContentId() == childPageId)
                break;
        }
        
        return index < list.size() ? index : null;
    }
    
    public PSPage findPage(String name, String folderPath) throws PSDataServiceException {
        return pageDao.findPage(name, folderPath);
    }
    
    @Override
    public PSPage save(PSPage page) throws PSDataServiceException, PSBeanValidationException {
        validate(page);
        
        boolean isExistingPage = page.getId() != null;
        PSPage previousPage = null;
        PSTemplate previousTemplate = null;
        
        PSTemplate template = templateDao.find(page.getTemplateId());
        if (template == null)
        	throw new RuntimeException("The template you have selected doesn't exist in the system. Please refresh and try again.");
        
        //Set the summary if exists
        String currentSummary = null;
        if (isExistingPage)
        {
           previousPage = load(page.getId());
           currentSummary = previousPage.getSummary();
           if (currentSummary != null)
           {
              page.setSummary(currentSummary);
           }
           
           previousTemplate = templateDao.find(previousPage.getTemplateId());
        }
        
        // Set the parent folder workflow only if it's a new page.
        if (!isExistingPage)
        {
            page.setTemplateContentMigrationVersion(template.getContentMigrationVersion());
            pageDaoHelper.setWorkflowAccordingToParentFolder(page);
        }
        
        //Save the page
        PSPage savedPage = super.save(page);
        if(page.isAddToRecent()){
            recentService.addRecentItem(page.getId());
            recentService.addRecentSiteFolder(page.getFolderPath());
            String siteName = PSPathUtils.getSiteFromPath(page.getFolderPath());
            if(StringUtils.isNotBlank(siteName))
                recentService.addRecentTemplate(siteName, page.getTemplateId());
        }

        //Set the page Id and the Event type 
        PSPageChangeEvent pageChangeEvent = new PSPageChangeEvent();
        pageChangeEvent.setPageId(page.getId());
        pageChangeEvent.setType(PSPageChangeEventType.PAGE_SAVED);
        notifyPageChange(pageChangeEvent);
try {
    psContentEvent = new PSContentEvent(page.getId(), page.getId().substring(page.getId().lastIndexOf("-") + 1, page.getId().length()), page.getFolderPath(), PSContentEvent.ContentEventActions.create, PSSecurityFilter.getCurrentRequest().getServletRequest(), PSActionOutcome.SUCCESS);
    psAuditLogService.logContentEvent(psContentEvent);
}catch (Exception e){
    //Handling exception
}
        return savedPage;
    }
    
    public PSNoContent savePageMetadata(String pageId)
    {
        if(pageId != null)
        {
            PSPageChangeEvent pageChangeEvent = new PSPageChangeEvent();
            pageChangeEvent.setPageId(pageId);
            pageChangeEvent.setType(PSPageChangeEventType.PAGE_META_DATA_SAVED);
            notifyPageChange(pageChangeEvent);
        }
        return new PSNoContent();
    }


    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
 
    public PSNoContent changeTemplate(String pageId, String templateId) throws PSDataServiceException {
        isTrue(isNotBlank(pageId), "pageId may not be blank");
        isTrue(isNotBlank(templateId), "templateId may not be blank");
        
        pageTemplateService.changeTemplate(pageId, templateId);
        
        return new PSNoContent("Changed template for page.");
    }
    
    
    public String getPageEditUrl(String id)
    {
        isTrue(isNotBlank(id), "id may not be blank");
        
        String url = contentDesignWs.getItemEditUrl(idMapper.getGuid(id), PAGE_CONTENT_TYPE,
                IPSConstants.SYS_HIDDEN_FIELDS_VIEW_NAME);
        return fixUrl(url);
    }
    
    
    public String getPageViewUrl(String id)
    {
        isTrue(isNotBlank(id), "id may not be blank");
        
        String url = contentDesignWs.getItemViewUrl(idMapper.getGuid(id), PAGE_CONTENT_TYPE,
                IPSConstants.SYS_HIDDEN_FIELDS_VIEW_NAME);

        return fixUrl(url);
    }
    
    @Override
    public PSValidationErrors validate(PSPage object) throws PSValidationException {
        return pageValidator.validate(object).throwIfInvalid().getValidationErrors();
    }

    public PSNoContent validateDelete(String id) throws PSValidationException {
        String opName = "validateDelete";
        PSValidationErrorsBuilder builder = 
            validateParameters(opName).rejectIfBlank("id", id).throwIfInvalid();
        
        try
        {
            PSPage page = super.load(id);
            validateForDelete(page, builder);  
        }
        catch (DataServiceLoadException | DataServiceNotFoundException | PSValidationException | PSItemWorkflowServiceException | PSNotFoundException e)
        {
            log.debug("Page: " + id + " not found for delete validation.", e);
        }
        
        return new PSNoContent(opName);
    }
    
    public List<PSPage> findAll() throws com.percussion.share.service.IPSDataService.DataServiceLoadException,
            com.percussion.share.service.IPSDataService.DataServiceNotFoundException
    {
        // TODO Auto-generated method stub
        // return null;
        throw new UnsupportedOperationException("findAll is not yet supported");
    }
    
    public List<PSSEOStatistics> findNonSEOPages(PSNonSEOPagesRequest request) throws PSDataServiceException {
        List<PSSEOStatistics> nonSEOStats = new ArrayList<>();
        List<PSPageSummary> sums = new ArrayList<>();
        
        // find the workflow, state id's
        int workflowId = -1;
        int stateId = -1;
        try
        {
            workflowId = itemWorkflowService.getWorkflowId(request.getWorkflow());
            stateId = itemWorkflowService.getStateId(request.getWorkflow(),request.getState());
        }
        catch (PSItemWorkflowServiceException | PSValidationException e)
        {
            throw new PSPageException(e);
        }
        
        String keyword = request.getKeyword();
        if (isNotBlank(keyword))
        {
            keyword = trimKeyword(keyword);
        }
        sums = pageDao.findPagesBySiteAndWf(PSPathUtils.getFolderPath(request.getPath()), workflowId, stateId);
        for (PSPageSummary sum : sums)
        {
            PSSEOStatistics stats = new PSSEOStatistics(sum, 
                    PSPathUtils.getFinderPath(sum.getFolderPath()) + '/' + sum.getName(), keyword);
            
            stats.setSummary(((PSPage)sum).getSummary());
            
            // check for keyword/severity
            if ((isBlank(keyword) || stats.isKeywordPresent()) && 
                    stats.getSeverityLevel().compareTo(request.getSeverity()) >= 0)
            {
                nonSEOStats.add(stats);
            }
        }
        
        return nonSEOStats;
    }
    
    /**
     * Trims the specified keyword such that multiple spaces within the keyword are condensed into one.
     * 
     * @param keyword assumed not blank.
     * 
     * @return the trimmed keyword.
     */
    private String trimKeyword(String keyword)
    {
        String trimmedKeyword = "";
        String[] split = keyword.trim().split(" ");
        for (String key : split)
        {
            if (key.length() == 0)
            {
                continue;
            }
            
            if (trimmedKeyword.length() > 0)
            {
                trimmedKeyword += " ";
            }
            
            trimmedKeyword += key;
        }
        
        return trimmedKeyword;
    }
   
    
    /**
     * This is used to validate a specified {@link PSPage} object to make sure
     * the object can be used to create a page if the ID is <code>null</code>
     * or such page exists in the specified folder if the ID is not <code>null</code>.
     */
    private  class PSPageValidator extends PSAbstractBeanValidator<PSPage> {
        
        private List<String> reservedPageNames = Arrays.asList(
                ".htdocs",
                ".htaccess",
                "pspagemetadatascanner.sfv");
        
        private List<String> invalidPageNames = Arrays.asList(
                ".",
                "..");
                
        private boolean validatePageReservedName(String name)
        {
            if (reservedPageNames.contains(name.toLowerCase()))
            {
                log.info("Page name uses a reserved word of the system: " + name);
                return false;
            }
            
            return true;
        }
        
        private boolean validatePageName(String name)
        {           
            for(String invalidName : invalidPageNames)
            {
                if(invalidName.equalsIgnoreCase(name))
                {
                    log.info("Page name uses an invalid word: " + name);
                    return false;
                }
            }    
            
            return true;
        }
        

        @Override
        protected void doValidation(PSPage obj, PSBeanValidationException e)
        {
            String name = obj.getName();
            String path = obj.getFolderPath();
            String fullPath = path + "/" + name;
            
            regionWidgetAssocationsValidator.validate(obj, e);
            regionsValidator.validate(obj, e);
            if (!validatePageReservedName(name))
            {
                String msg = "Cannot create or rename page \"" + name + "\" to \"" + name + "\" because that name is a reserved page name.";
                log.debug(msg);
                e.rejectValue("name", "page.reservedName", msg);
            }
            
            if (!validatePageName(name))
            {
                String msg = "Cannot create or rename page \"" + name + "\" to \"" + name + "\" because that name is an invalid page name.";
                log.debug(msg);
                e.rejectValue("name", "page.invalidName", msg);
            }
            
            if (obj.getId() != null) return;

            // make sure there is no existing item with the same "fullPath"
            IPSGuid existingId;
            try
            {
                existingId = contentWs.getIdByPath(fullPath);
            }
            catch (Exception ae) {
                String msg = "Failed to verify path \"" + fullPath + "\".";
                log.error(msg, ae);
                e.rejectValue("folderPath", "page.badFolderPath", msg);
                return;
            }

            if (existingId != null) {
                String msg = "Cannot create a page with name \"" + name + "\" because a page or folder named \"" + name + "\" already exists in the folder.";
                log.debug(msg);
                e.rejectValue("folderPath", "page.alreadyExists", msg);
            }
        }
        
    }

    /**
     * Validates that the specified page may be deleted.
     * 
     * @param page the page to validate, assumed not <code>null</code>.
     * @param builder used to capture and throw validation errors, assumed not <code>null</code>.
     */
    private void validateForDelete(PSPage page, PSValidationErrorsBuilder builder) throws PSValidationException, PSItemWorkflowServiceException, PSNotFoundException {
        String id = page.getId();
        
        if (!itemWorkflowService.isModifiableByUser(id))
        {
            builder.reject("page.deleteNotAuthorized", "The current user is not authorized to delete this "
                    + "page");
            builder.throwIfInvalid();
        }

        if (widgetAssetRelationshipService.isUsedByTemplate(id))
        {
            builder.reject("page.deleteTemplates",
                    "The page is targeted by links on one or more templates and cannot be deleted");
            builder.throwIfInvalid();
        }

        if (!itemWorkflowService.getApprovedPages(id, page.getFolderPath()).isEmpty())
        {
            builder.reject("page.deleteApprovedPages",
                    "The page is linked to from one or more approved pages and cannot " + "be deleted");
            builder.throwIfInvalid();
        }
    }
    
    /**
     * Changes url from beginning with ../ to /Rhythmyx/
     * This is need because ../ now puts you in /cm/ and some requests still 
     * need to go to /Rhythmyx/
     * 
     * @param url to convert, assumed not <code>null</code>.
     * @return fixed url
     **/
    private String fixUrl(String url)
    {
        isTrue(isNotBlank(url), "url may not be blank");

        if (url.startsWith("../"))
            url = "/Rhythmyx/" + url.substring(3);
        
        return url;
    }
    
   
    /**
     * Logger for this service.
     */
    public static final Logger log = LogManager.getLogger(PSPageService.class);
    
    
    /**
     * Validator to validate the page before {@link #save(PSPage)}
     * and on {@link #validate(PSPage)},
     * never <code>null</code>.
     */
    private PSAbstractBeanValidator<PSPage> pageValidator;
    
    private PSRegionWidgetAssociationsValidator<PSPage> regionWidgetAssocationsValidator; 
    
    
    private PSRegionsValidator<PSPage> regionsValidator = new PSRegionsValidator<PSPage>() {

        @Override
        public String getField()
        {
            return "regionBranches.regions";
        }

        @Override
        public Iterator<PSRegion> getRegions(PSPage wa, PSBeanValidationException e)
        {
            
            List<PSRegion> regions = new ArrayList<>();
            if (wa.getRegionBranches() != null && wa.getRegionBranches().getRegions() != null) {
                
                List<PSRegion> br = wa.getRegionBranches().getRegions();
                for (PSRegion r : br) {
                    CollectionUtils.addAll(regions, PSRegionTreeUtils.iterateRegions(r));
                }
            }
            
            return regions.iterator();
             
        }
        
    };
    
    
    
    public static class PSPageRegionWidgetValidator extends PSRegionWidgetAssociationsValidator<PSPage> {

        public PSPageRegionWidgetValidator(IPSWidgetService widgetService)
        {
            super(widgetService);
        }

        @Override
        public String getField()
        {
            return "regionBranches";
        }

        @Override
        public PSRegionWidgetAssociations getWidgetAssocations(PSPage wa, PSBeanValidationException e)
        {
            return wa.getRegionBranches();
        }
    
    }

   @Override
   /**
    * {@link #addPageChangeListener(IPSPageService)}
    */
   public void addPageChangeListener(IPSPageChangeListener pageChangeListener)
   {
      pageChangeListeners.add(pageChangeListener);
   }

   @Override
   /**
    * {@link #notifyPageChange(IPSPageService)}
    */
   public void notifyPageChange(PSPageChangeEvent pageChangeEvent)
   {
      for(IPSPageChangeListener listener:pageChangeListeners)
      {
         listener.pageChanged(pageChangeEvent);
      }
   }
   
   private class CompareItemEntry implements Comparator<IPSItemEntry>
   {
       String sortColumn;
       int sortOrderNumber;
       
       private CompareItemEntry(String sortColumn, String sortOrder)
       {
           this.sortColumn = sortColumn;
           this.sortOrderNumber =  sortOrder.equalsIgnoreCase("desc") ? -1 : 1;
       }
       
       @Override
       public int compare(IPSItemEntry o1, IPSItemEntry o2)
       {
          Object prop1 = null, prop2 = null;

          if (StringUtils.equals(sortColumn, "name"))
          {
             prop1 = o1.getName();
             prop2 = o2.getName();
          }
          else
          {
             throw new IllegalArgumentException("The specified sort column is not supported");
          }
          
          int compareResult =
                new CompareToBuilder()
                   .append(prop1, prop2)
                   .toComparison();

          return sortOrderNumber * compareResult;
       }
   }
   
   public IPSPageCatalogService getCatalogService()
   {
       if (catalogService == null)
       {
           catalogService = (IPSPageCatalogService) getWebApplicationContext().getBean("pageCatalogService");
       }

       return catalogService;
   }
   
   public IPSPageImportQueue getPageImportQueue()
   {
       
       if (pageImportQueue == null)
       {
           pageImportQueue = (IPSPageImportQueue) getWebApplicationContext().getBean("pageImportQueue");
       }

       return pageImportQueue;
   }

    /**
     * 
     * @author Santiago M. Murchio
     * 
     */
    private class PSUnassignedPagesDataHelper
    {
        private Map<String, PSMockDataForUnassignedPages> mockDataMap = new HashMap<>();
        
        public PSUnassignedResults getUnassignedResults(String siteName, int startIndex, int maxResults) throws Exception
        {
            // get mock data
            if (equalsIgnoreCase(siteName, "perc-mock-site-complete"))
            {
                PSMockDataForUnassignedPages siteData = new PSMockDataForUnassignedPages();
                siteData.importCompleted();
                return siteData.getUnassignedResults(startIndex, maxResults);
            }
            else if (equalsIgnoreCase(siteName, "perc-mock-site-in-progress"))
            {
                return createMockResponse(siteName, startIndex, maxResults);
            }

            // for any other site to get the real data
            PSSiteSummary site = siteDao.findSummary(siteName);
            if (site == null)
            {
                log.warn("Unable to locate Site: " + siteName);
                return new PSUnassignedResults();
            }
            PSSiteQueue siteQueue =  getPageImportQueue().getPageIds(site.getSiteId());
//TODO Fix null pointer here
            List<Integer> catalogedIds = siteQueue.getCatalogedIds();
            List<Integer> importedIds = siteQueue.getImportedIds();
            List<Integer> importingIds = siteQueue.getImportingIds();

            return buildUnassignedResults(catalogedIds, importedIds, importingIds, startIndex, maxResults);
        }
        
        private PSUnassignedResults createMockResponse(String sitename, int startIndex, int maxResults)
        {
            PSMockDataForUnassignedPages siteData = mockDataMap.get(sitename);
            if (siteData == null)
            {
                siteData = new PSMockDataForUnassignedPages();
                mockDataMap.put(sitename, siteData);
            }
            else
            {
                siteData.update();
            }
            return siteData.getUnassignedResults(startIndex, maxResults);
        }
        
        /**
         * Builds the result with the given list of ids. Paginates the results
         * according to the given start index, and max results parameters. 
         * 
         * @param catalogedIds {@link List}<{@link Integer}> list of the cataloged
         *            pages ids. Assumed not <code>null</code>.
         * @param importedIds {@link List}<{@link Integer}> list of the imported
         *            pages ids. Assumed not <code>null</code>.
         * @param importingIds {@link Integer} may be <code>null</code>.
         * @param startIndex {@link Integer} assumed not <code>null</code> and
         *            greater than 0.
         * @param maxResults {@link Integer} assumed not <code>null</code> and
         *            greater than 0.
         * @return {@link PSUnassignedResults} with the results, never <code>null</code>
         */
        private PSUnassignedResults buildUnassignedResults(List<Integer> catalogedIds, List<Integer> importedIds, List<Integer> importingIds, Integer startIndex, Integer maxResults) throws PSPageException {
            // Build a unique list
            List<Integer> pageIds = new ArrayList<>();
            pageIds.addAll(catalogedIds);
            pageIds.addAll(importedIds);
            pageIds.addAll(importingIds);
            Collections.sort(pageIds);
           
            int catalogCount = importingIds.size() != 0 ? catalogedIds.size() + 1 : catalogedIds.size();
            
            // Build import status
            ImportStatus importStatus = new ImportStatus(catalogCount, importedIds.size());

            // build Item list
            PSPagedObjectList<Integer> pageGroup = PSPagedObjectList.getPage(pageIds, startIndex, maxResults);

            List<UnassignedItem> items = new ArrayList<>();
            for (Integer id : pageGroup.getChildrenInPage())
            {
                items.add(getUnassignedPage(id, importingIds, importedIds));
            }

            UnassignedItemList itemList = new UnassignedItemList(pageGroup.getStartIndex(), pageGroup.getChildrenCount(), items);

            return new PSUnassignedResults(itemList, importStatus);
        }

    }
    
    /**
     * Get the unassigned item with the item id.
     * 
     * @param id {@link Integer} assumed not <code>null</code> and greater than
     *            0.
     * @param importingIds {@link Integer} may be <code>null</code>.
     * @param importedIds {@link List}<{@link Integer}> list of the imported
     *            pages ids. Assumed not <code>null</code>.
     * 
     * @return {@link UnassignedItem} the unassigned item. Never
     *         <code>null</code>, or empty.
     */
    private UnassignedItem getUnassignedPage(Integer id, List<Integer> importingIds, List<Integer> importedIds) throws PSPageException {
        try
        {
            boolean isImported = importedIds.contains(id);
            String pageId = idMapper.getString(new PSLegacyGuid(id));
            
            String path = folderHelperWs.findPathFromLegacyFolderId(id);
            
            String name = getLinkTitleForUnassignedPage(path, isImported);
            
            if (!isImported)
                path = path == null ? "" : getCatalogService().convertToImportedFolderPath(path);
             
            if (path.startsWith(SITE_ROOT))
            {
                path = path.substring(1);
            }
                        
            ItemStatus currentStatus = getItemStatus(id, importingIds, isImported);
            
            return new UnassignedItem(pageId, name, path, currentStatus);
        }
        catch(Exception e)
        {
            String msg = "An error occurred when getting cataloged page's summary.";
            throw new PSPageException(msg, e);
        }
    }
    
    /**
     * Get the link title for the given item.
     * 
     * @param path {@link String} the path to the target item, the assumed not
     *            <code>null</code> and greater than 0.
     * @param isImported {@link Boolean} flag to indicate if the item was
     *            already imported.
     * @return {@link String} the link title for the item. Never
     *         <code>null</code>, or empty.
     */
    private String getLinkTitleForUnassignedPage(String path, boolean isImported) throws PSPageException {
        String pagePath = path;

        if (isImported)
            pagePath = getCatalogService().convertToImportedFolderPath(path);

        PSPage page = pageDao.findPageByPath(pagePath);

        if (page != null)
        {
            return page.getLinkTitle();
        }
        else
        {
            pagePath = PSPathUtils.stripSitesRoot(path);
            return PSFolderPathUtils.getName(path);
        }
    }
    
    /**
     * Get the status for the given item.
     * 
     * @param id {@link String} assumed not <code>null</code> and
     *            greater than 0.    
     * @param importingIds {@link String} assumed not <code>null</code> and
     *            greater than 0.    
     * @param isImported {@link Boolean} flag to indicate if the item was already imported.          
     * @return {@link ItemStatus} the item status for the item. Never
     *         <code>null</code>, or empty.
     */
    private ItemStatus getItemStatus(Integer id, List<Integer> importingIds, boolean isImported)
    {
        ItemStatus currentStatus = ItemStatus.Cataloged;
        
        if(importingIds.size() != 0 && importingIds.contains(id))
            return ItemStatus.Importing;
        
        if(isImported)
            return ItemStatus.Imported;
        
        return currentStatus;
    }
    
    /**
     * Generates a name for a new page, using site-wide naming conventions. It
     * avoids collision with existing pages appending a numerical value to the
     * end if necessary (Example: New-Page-2)
     * 
     * @param pageName The initial name for the page. It will be kept unchanged
     *            if no such page exists in the folder with folderPath.
     *            Otherwise, a numerical suffix will be added to avoid naming
     *            collision.
     * @param folderPath The path of the folder where the page will be created.
     *            Used to check for existing pages that might have the same
     *            name.
     * @return String The automatically generated name for the page. Never null
     *         or empty.
     * 
     */
    public String generateNewPageName(String pageName, String folderPath) throws PSPageException {
        int count = 0;

        String fullPath = folderPath + PATH_SEPARATOR + pageName;

        String generatedPageName = pageName;

        while (pageDao.findPageByPath(fullPath) != null)
        {
            count++;
            generatedPageName = PSPageManagementUtils.getNameForCount(pageName, count);
            fullPath = folderPath + PATH_SEPARATOR + generatedPageName;
        }

        return generatedPageName;
    }

    @Override
    public void updateMigrationEmptyWidgetFlag(String pageId, boolean flag) throws PSDataServiceException {
        PSPage page = find(pageId);
        page.setMigrationEmptyWidgetFlag(flag);
        super.save(page);          
    }

    @Override
    public boolean getMigrationEmptyWidgetFlag(String pageId) throws DataServiceLoadException, DataServiceNotFoundException, PSValidationException {
        PSPage page = find(pageId);
        return  page.isMigrationEmptyWidgetFlag();
    }

    public IPSRecentService getRecentService() {
        return recentService;
    }

    @Autowired
    @Lazy
    public void setRecentService(IPSRecentService recentService) {
        this.recentService = recentService;
    }
	@Override
	public List<PSPageReportLine> findAllPages(String sitePath) throws PSReportFailedToRunException, PSDataServiceException {
		List<PSPageReportLine> ret = new ArrayList<>();
		List<PSPage> pages = pageDao.findAllPagesBySite(sitePath);
		
		List<PSCoreItem> nodes = null;
		for(PSPage p : pages){
			PSPageReportLine line = new PSPageReportLine();
			try {
				nodes = this.contentWs.loadItems(new ArrayList<>(), false, true, false, true);
			} catch (PSErrorResultsException e) {
				log.error("Error retrieving");
			}
			line.setBulkImportAction("UPDATE");
		
		}
		
		
		return ret;
	}
}
