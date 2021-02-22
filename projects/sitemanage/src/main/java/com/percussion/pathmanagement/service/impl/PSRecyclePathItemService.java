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
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.fastforward.managednav.IPSManagedNavService;
import com.percussion.itemmanagement.service.IPSItemWorkflowService;
import com.percussion.pagemanagement.service.IPSPageService;
import com.percussion.pathmanagement.data.PSDeleteFolderCriteria;
import com.percussion.pathmanagement.data.PSPathItem;
import com.percussion.recycle.service.IPSRecycleService;
import com.percussion.services.contentmgr.IPSContentMgr;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.workflow.IPSWorkflowService;
import com.percussion.share.dao.IPSFolderHelper;
import com.percussion.share.data.IPSItemSummary;
import com.percussion.share.data.PSItemProperties;
import com.percussion.share.service.IPSDataService;
import com.percussion.share.service.IPSIdMapper;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.ui.service.IPSListViewHelper;
import com.percussion.user.service.IPSUserService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.apache.commons.lang.Validate.notEmpty;

@Component("recyclePathItemService")
@Lazy
public class PSRecyclePathItemService extends PSPathItemService {

    private IPSRecycleService recycleService;

    private IPSManagedNavService navService;

    private String navTreeType;

    private String navonType;

    @Autowired
    public PSRecyclePathItemService(IPSFolderHelper folderHelper,
                                    IPSIdMapper idMapper, IPSPageService pageService,
                                    IPSItemWorkflowService itemWorkflowService, IPSAssetService assetService,
                                    IPSWidgetAssetRelationshipService widgetAssetRelationshipService, IPSContentMgr contentMgr,
                                    IPSWorkflowService workflowService, @Qualifier("cm1ListViewHelper") IPSListViewHelper listViewHelper,
                                    IPSUserService userService, IPSRecycleService recycleService, IPSManagedNavService navService) {
        super(folderHelper, idMapper, itemWorkflowService, assetService, widgetAssetRelationshipService,
                contentMgr, workflowService, pageService, listViewHelper, userService);
        this.recycleService = recycleService;
        this.navService = navService;
        this.setRootName("Recycling");
    }

    @Override
    protected String getFullFolderPath(String path) {
        notEmpty(path, "path");
        log.debug("Getting full folder path for path: " + path);

        String fullFolderPath = RECYCLING_ROOT;
        if (!path.equals("/")) {
            fullFolderPath = folderHelper.concatPath(fullFolderPath, path);
        }

        return fullFolderPath;
    }

    @Override
    public PSItemProperties findItemProperties(String path) throws PSPathNotFoundServiceException {
        notEmpty(path, "path");
        if (log.isDebugEnabled())
            log.debug("find item properties: " + path);
        String fullFolderPath = getFullFolderPath(path);
        PSItemProperties props;
        try
        {
            props = folderHelper.findItemProperties(fullFolderPath, RECYCLED_RELATE_TYPE);
        }
        catch (Exception e)
        {
            throw new PSPathNotFoundServiceException("Path not found: " + path);
        }
        return props;
    }

    @Override
    protected List<PSPathItem> findItems(String path) {
        String fullPath = getFullFolderPath(path);
        log.debug("findItems path: " + fullPath);
        List<PSPathItem> items = new ArrayList<>();
        List<IPSItemSummary> summaries = recycleService.findChildren(fullPath);
        for (IPSItemSummary summ : summaries) {
            PSPathItem pathItem = new PSPathItem();
            convert(summ, pathItem);
            pathItem.setPath(path + summ.getName());
            pathItem.setFolderPath(fullPath + summ.getName());
            pathItem.setFolderPaths(asList(fullPath));
            if (!shouldFilterItem(pathItem)) {
                items.add(pathItem);
            }
        }
        return items;
    }

    @Override
    public int deleteFolder(PSDeleteFolderCriteria criteria) throws PSPathServiceException, PSValidationException, IPSDataService.DataServiceNotFoundException, IPSDataService.DataServiceLoadException, PSNotFoundException {
        PSPathItem folder = findItem(criteria.getPath());
        if (folder.getCategory() == IPSItemSummary.Category.SECTION_FOLDER)
        {
            log.debug("Detected section folder being purged. Id is:" + criteria.getPath());
            String folderPath = folder.getFolderPath();
            try
            {
                // here we need to purge the navon item
                // then call super delete folder to handle the rest.
                folderHelper.removeItem(folderPath, idMapper.getString(
                        navService.findNavigationIdFromFolder(folderPath, RECYCLED_RELATE_TYPE)), true);
            }
            catch (IllegalArgumentException e) {
                throw new PSPathServiceException(e.getMessage());
            }
            catch (Exception e)
            {
                throw new PSPathServiceException("Failed to delete navon from section: " + criteria.getPath(), e);
            }
        }
        return super.deleteFolder(criteria);
    }

    @Override
    protected PSPathItem findItem(String path) throws IPSDataService.DataServiceLoadException {
        String fullPath = getFullFolderPath(path);
        log.debug("findItem path: " + fullPath);
        IPSItemSummary summary = recycleService.findItem(fullPath);
        PSPathItem pathItem = new PSPathItem();
        convert(summary, pathItem);
        pathItem.setPath(path);
        pathItem.setFolderPath(fullPath);
        pathItem.setFolderPaths(asList(fullPath));
        return pathItem;
    }

    @Override
    protected void convert(IPSItemSummary dataItem, PSPathItem newPathtem) {
        super.convert(dataItem, newPathtem);
        newPathtem.setId(dataItem.getId());
        newPathtem.setName(dataItem.getName());
        newPathtem.setType(dataItem.getType());
    }

    @Override
    protected boolean shouldFilterItem(IPSItemSummary item)
    {
        if (navonType == null) {
            navonType = navService.getNavonContentTypeName();
        }

        if (navTreeType == null) {
            navTreeType = navService.getNavtreeContentTypeName();
        }

        if (item == null || (".system".equals(item.getName())) || (item.getCategory().equals(IPSItemSummary.Category.EXTERNAL_SECTION_FOLDER) ||
                (navonType != null && item.getType().equals(navonType)) ||
                (navTreeType != null && item.getType().equals(navTreeType))))
        {
            return true;
        }

        return false;
    }

    @Override
    protected String getInUsePagesResult() {
        return PAGES_IN_USE_PAGES;
    }

    @Override
    protected String getNotAuthorizedResult() {
        return PAGES_NOT_AUTHORIZED;
    }

    @Override
    protected String getInUseTemplatesResult() {
        return PAGES_IN_USE_TEMPLATES;
    }

    @Override
    protected String getFolderRoot() {
        return RECYCLING_ROOT;
    }

    /**
     * Constant for the recycling root folder path.
     */
    public static final String RECYCLING_ROOT_SUB = "//Folders/$System$";
    /*
     * Constant for the Recycling Folder.
     */
    public static final String RECYCLING_ROOT = RECYCLING_ROOT_SUB + "/Recycling";

    /**
     * Constant for the response given when a folder contains in use pages.
     */
    public static final String PAGES_IN_USE_PAGES = "PagesInUsePages";

    /**
     * Constant for the response given when a user is not authorized to remove pages in a folder.
     */
    public static final String PAGES_NOT_AUTHORIZED = "PagesNotAuthorized";

    /**
     * Constant for the response given when a folder contains pages linked by templates.
     */
    public static final String PAGES_IN_USE_TEMPLATES = "PagesInUseTemplates";

    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Log log = LogFactory.getLog(PSRecyclePathItemService.class);

    /**
     * Static constant to represent the {@link PSRelationshipConfig} constant for recycled content
     * relationships.
     */
    private static final String RECYCLED_RELATE_TYPE = PSRelationshipConfig.TYPE_RECYCLED_CONTENT;

}
