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
import com.percussion.fastforward.managednav.IPSManagedNavService;
import com.percussion.itemmanagement.service.IPSItemWorkflowService;
import com.percussion.pagemanagement.service.IPSPageService;
import com.percussion.pathmanagement.data.PSDeleteFolderCriteria;
import com.percussion.pathmanagement.data.PSPathItem;
import com.percussion.pathmanagement.data.PSRenameFolderItem;
import com.percussion.services.contentmgr.IPSContentMgr;
import com.percussion.services.workflow.IPSWorkflowService;
import com.percussion.share.dao.IPSFolderHelper;
import com.percussion.share.dao.PSFolderPathUtils;
import com.percussion.share.data.IPSItemSummary;
import com.percussion.share.data.PSDataItemSummary;
import com.percussion.share.service.IPSDataService;
import com.percussion.share.service.IPSDataService.DataServiceLoadException;
import com.percussion.share.service.IPSIdMapper;
import com.percussion.share.service.exception.PSSpringValidationException;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.sitemanage.data.PSSiteSummary;
import com.percussion.sitemanage.service.IPSSiteDataService;
import com.percussion.ui.service.IPSListViewHelper;
import com.percussion.user.service.IPSUserService;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;
@Component("sitePathItemService")
@Lazy
public class PSSitePathItemService extends PSPathItemService
{

    private IPSSiteDataService siteDataService;
    private IPSPageService pageService;
           
    private Pattern sitePathPattern = Pattern
        .compile("^/([^/]*?)(/.*)$");
    
    
    @Autowired
    public PSSitePathItemService(IPSSiteDataService siteDataService, IPSFolderHelper folderHelper,
            IPSIdMapper idMapper, IPSManagedNavService navService, IPSPageService pageService,
            IPSItemWorkflowService itemWorkflowService, IPSAssetService assetService,
            IPSWidgetAssetRelationshipService widgetAssetRelationshipService, IPSContentMgr contentMgr,
            IPSWorkflowService workflowService, @Qualifier("cm1ListViewHelper") IPSListViewHelper listViewHelper, IPSUserService userService)
    {
        super(folderHelper, idMapper, itemWorkflowService, assetService, widgetAssetRelationshipService,
                contentMgr, workflowService, pageService, listViewHelper, userService);
        this.siteDataService = siteDataService;
        this.navService = navService;
        this.pageService = pageService;
        this.setRootName("Sites");
    }

    @Override
    protected PSPathItem findItem(String path) throws PSPathNotFoundServiceException, IPSDataService.DataServiceNotFoundException, PSValidationException {
        SiteIdAndFolderPath sfp = getSiteIdAndFolderPath(path);
        PSSiteSummary site = null;
        try
        {
            site = siteDataService.find(sfp.getSiteId());
        }
        catch (DataServiceLoadException | PSValidationException e)
        {
            try {
                site = siteDataService.findByPath(("/Sites/" + path).replace("//","/"));
            }catch (IPSDataService.DataServiceNotFoundException | PSValidationException e1){
                // Site not found, if we have assume we have a valid path and the path item is orphaned
                String msg = sfp.isOnlySiteId() ? "Oops.  We can't find the site " + sfp.getSiteId() + ".  It may have been deleted." : "Oops. We're sorry. This page should have been deleted when its site was deleted. Please contact Customer Success for assistance.";
                throw new PSPathNotFoundServiceException(msg);
            }

        }
        
        //Only the site id.
        if (sfp.isOnlySiteId()) {
            PSPathItem item = createPathItem();
            convert(site, item);
            return item;
        }
        
        return super.findItem(path);
    }
    
    protected void convert(PSSiteSummary site, PSPathItem item) 
    {
        super.convert(site, item);
        item.setId(site.getId());
        item.setPath("/" + site.getId() + "/");
        item.setFolderPath(site.getFolderPath());
        item.setName(site.getName());
        item.setLeaf(false);
        item.setType(PSDataItemSummary.TYPE_SITE);
    }
    
    protected SiteIdAndFolderPath getSiteIdAndFolderPath(String path) throws PSPathNotFoundServiceException
    {
        PSPathUtils.validatePath(path);
        Matcher matcher = sitePathPattern.matcher(path);
        if (matcher.find()) {
            String siteId = matcher.group(1).trim();
            String preFp = matcher.group(2).trim();
            String folderPath = preFp; 
            SiteIdAndFolderPath sfp = new SiteIdAndFolderPath(siteId, folderPath);
            if("/".equals(preFp)) {
                sfp.onlySiteId = true;
            }
            else {
                sfp.onlySiteId = false;
            }
            return sfp;
        }
        throw new PSPathNotFoundServiceException("Could not extract site id or folder path from: " + path);
    }
    
    protected List<PSPathItem> findRootChildren() {
        List<PSSiteSummary> sites = siteDataService.findAll();
        List<PSPathItem> items = new ArrayList<>();
        for(PSSiteSummary site : sites) {
            PSPathItem item = createPathItem();
            convert(site, item);
            items.add(item);
        }
        return items;
    }
    
    @Override
    protected List<PSPathItem> findItems(String path) throws IPSDataService.DataServiceNotFoundException, PSPathNotFoundServiceException, PSValidationException {
        if ("/".equals(path)) return findRootChildren();
               
        return super.findItems(path);
    }
    
    @Override
    protected String getFullFolderPath(String path) throws IPSDataService.DataServiceNotFoundException, PSPathNotFoundServiceException, PSValidationException {
        notEmpty(path, "path");
        
        String fullFolderPath = SITE_ROOT;
        if (!path.equals("/"))
        {
            SiteIdAndFolderPath sfp = getSiteIdAndFolderPath(path);
            PSSiteSummary site = siteDataService.findByPath(SITE_ROOT + path);
            fullFolderPath = sfp.getFullFolderPath(site.getFolderPath());        
        }
        
        return fullFolderPath;
    }
    
    @Override
    public PSPathItem addNewFolder(String path) throws PSPathServiceException, IPSDataService.DataServiceNotFoundException, PSValidationException {
        PSPathUtils.validatePath(path);
        
        if ("/".equals(path))
        {
            throw new PSPathServiceException("New folders may not be added as sites");
        }
                
        return super.addNewFolder(path);
    }
    
    @Override
    public PSPathItem renameFolder(PSRenameFolderItem item) throws PSValidationException, PSPathServiceException, IPSDataService.DataServiceNotFoundException {
        String path = item.getPath();
        if (getSiteIdAndFolderPath(path).isOnlySiteId())
        {
            throw new PSPathServiceException("Site folders may not be renamed");
        }
                
        return super.renameFolder(item);
    }

    @Override
    public int deleteFolder(PSDeleteFolderCriteria criteria) throws PSPathServiceException, IPSDataService.DataServiceNotFoundException, PSValidationException {
        String path = criteria.getPath();
        if (getSiteIdAndFolderPath(path).isOnlySiteId())
        {
            throw new PSPathServiceException("Site folders may not be deleted");
        }

        PSPathItem folder = findItem(path);
        if (folder.getCategory() == IPSItemSummary.Category.SECTION_FOLDER)
        {
            // this is a section folder, so delete the navon now because it's a filtered type
            String folderPath = folder.getFolderPath();

            try
            {
                // purgeItem is false so item is recycled.
                folderHelper.removeItem(folderPath, idMapper.getString(
                        navService.findNavigationIdFromFolder(folderPath)), false);

            }
            catch (IllegalArgumentException e) {
                throw new PSPathServiceException(e.getMessage());
            }
            catch (Exception e)
            {
                throw new PSPathServiceException("Failed to delete navon from section: " + path, e);
            }
        }

        return super.deleteFolder(criteria);

    }

    @Override
    protected String getInUsePagesResult()
    {
        return PAGES_IN_USE_PAGES;
    }
    
    @Override
    protected String getNotAuthorizedResult()
    {
        return PAGES_NOT_AUTHORIZED;
    }
    
    @Override
    protected String getInUseTemplatesResult()
    {
        return PAGES_IN_USE_TEMPLATES;
    }
    
    @Override
    protected boolean shouldFilterItem(IPSItemSummary item)
    {
        if (item == null || getFilteredItemTypes().contains(item.getType()) || getFilteredItemNames().contains(
                item.getName()) || item.getCategory().equals(IPSItemSummary.Category.EXTERNAL_SECTION_FOLDER))
        {
            return true;
        }
        
        return false;        
    }
    
    @Override
    protected void removeItem(String fullFolderPath, PSPathItem item, boolean purgeItem) throws Exception
    {
        notEmpty(fullFolderPath);
        notNull(item);
        
        if (isPage(item))
        {
            pageService.delete(item.getId(), true, purgeItem);
        }
        else
        {
            super.removeItem(fullFolderPath, item, purgeItem);
        }
    }
    
    @Override
    protected Set<String> getApprovedPages(PSPathItem item) throws PSValidationException {
        notNull(item);
        
        return itemWorkflowService.getApprovedPages(item.getId(), PSFolderPathUtils.parentPath(item.getFolderPath()));
    }
  
    @Override
    protected String getFolderRoot()
    {
        return SITE_ROOT;
    }
    
    /**
     * Used to determine items to be filtered by type.
     * 
     * @return list of item types that should not be displayed.  Never <code>null</code>.
     */
    private List<String> getFilteredItemTypes()
    {
        if (navService.isManagedNavUsed())
        {
            if (navTreeType == null)
            {
                navTreeType = navService.getNavtreeContentTypeName();
            }

            if (navonType == null)
            {
                navonType = navService.getNavonContentTypeName();
            }
        }

        List<String> types = new ArrayList<String>();

        if (StringUtils.isNotBlank(navTreeType))
        {
            types.add(navTreeType);
        }

        if (StringUtils.isNotBlank(navonType))
        {
            types.add(navonType);
        }
               
        return types;
    }
    
    /**
     * Used to determine items to be filtered by name.
     * 
     * @return list of item names that should not be displayed.  Never <code>null</code>.
     */
    private List<String> getFilteredItemNames()
    {
        if (filteredItemNames == null)
        {
            filteredItemNames = new ArrayList<>();
            filteredItemNames.add(".system");
        }
                
        return filteredItemNames;
    }
    
    private PSSiteSummary getSite(String id) throws PSPathNotFoundServiceException, DataServiceLoadException, PSValidationException {
        PSSiteSummary site = siteDataService.find(id);
        if(log.isDebugEnabled())
            log.debug("Loaded site: " + site);
        if(site == null) throw new PSPathNotFoundServiceException("Site could not be found for id");
        return site;
    }
    
    public static class SiteIdAndFolderPath {
        private String siteId;
        private String folderPath;
        private boolean onlySiteId = false;
        public SiteIdAndFolderPath(String siteId, String sitePath)
        {
            super();
            this.siteId = siteId;
            this.folderPath = sitePath;
        }
        
        
        public String getSiteId()
        {
            return siteId;
        }


        public boolean isOnlySiteId()
        {
            return onlySiteId;
        }


        public String getFullFolderPath(String siteFolderPath) {
            if(siteFolderPath == null) throw new IllegalArgumentException("site folder path cannot be null");
            return siteFolderPath + folderPath;
        }
        
        
    }

    /**
     * Constant for the site root folder path.
     */
    public static final String SITE_ROOT_SUB = "/Sites";
    public static final String SITE_ROOT = "/" + SITE_ROOT_SUB;

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
     * The managed nav service.  Initialized in ctor, never <code>null</code> after that.
     */
    private IPSManagedNavService navService;
    
    /**
     * Name of the navigation tree content type.  Initialized in {@link #getFilteredItemTypes()}.  Should not be
     * <code>null</code> after that.
     */
    private String navTreeType = null;
    
    /**
     * Name of the navigation node content type.  Initialized in {@link #getFilteredItemTypes()}.  Should not be
     * <code>null</code> after that.
     */
    private String navonType = null;
    
    /**
     * List of item names which will be not be displayed.  Initialized in {@link #getFilteredItemNames()}.  Never
     * <code>null</code> after that.
     */    
    private List<String> filteredItemNames = null;
        
    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Log log = LogFactory.getLog(PSSitePathItemService.class);
    
}
