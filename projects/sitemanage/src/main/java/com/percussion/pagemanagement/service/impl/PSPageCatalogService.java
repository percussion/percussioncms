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
package com.percussion.pagemanagement.service.impl;

import com.percussion.fastforward.managednav.IPSManagedNavService;
import com.percussion.itemmanagement.service.IPSItemWorkflowService;
import com.percussion.pagemanagement.dao.IPSPageDao;
import com.percussion.pagemanagement.dao.IPSPageDaoHelper;
import com.percussion.pagemanagement.dao.IPSTemplateDao;
import com.percussion.pagemanagement.data.PSCatalogPageSummary;
import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.data.PSTemplate;
import com.percussion.pagemanagement.data.PSTemplate.PSTemplateTypeEnum;
import com.percussion.pagemanagement.data.PSTemplateSummary;
import com.percussion.pagemanagement.service.IPSPageCatalogService;
import com.percussion.pagemanagement.service.IPSPageService.PSPageException;
import com.percussion.pagemanagement.service.IPSTemplateService;
import com.percussion.pathmanagement.service.impl.PSPathUtils;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.IPSItemEntry;
import com.percussion.services.notification.IPSNotificationListener;
import com.percussion.services.notification.IPSNotificationService;
import com.percussion.services.notification.PSNotificationEvent;
import com.percussion.services.notification.PSNotificationEvent.EventType;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.services.workflow.IPSWorkflowService;
import com.percussion.services.workflow.PSWorkflowServiceLocator;
import com.percussion.share.IPSSitemanageConstants;
import com.percussion.share.dao.IPSFolderHelper;
import com.percussion.share.service.IPSIdMapper;
import com.percussion.share.service.IPSSystemProperties;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.sitemanage.dao.IPSiteDao;
import com.percussion.sitemanage.data.PSSiteSummary;
import com.percussion.sitemanage.error.PSSiteImportException;
import com.percussion.sitemanage.service.IPSSiteTemplateService;
import com.percussion.utils.guid.IPSGuid;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static com.percussion.pathmanagement.service.impl.PSPathUtils.doesItemExist;
import static com.percussion.share.dao.PSFolderPathUtils.concatPath;
import static com.percussion.share.dao.PSFolderPathUtils.pathSeparator;
import static com.percussion.share.service.IPSSystemProperties.CATALOG_PAGE_MAX;
import static com.percussion.sitemanage.service.IPSSiteSectionMetaDataService.PAGE_CATALOG;
import static com.percussion.sitemanage.service.IPSSiteSectionMetaDataService.SECTION_SYSTEM_FOLDER_NAME;

/**
 * @author JaySeletz
 *
 */
@Component("pageCatalogService")
@Lazy
@Transactional(noRollbackFor = {Exception.class,PSPageException.class})
public class PSPageCatalogService implements IPSPageCatalogService
{
    private IPSFolderHelper folderHelper;
    private IPSPageDao pageDao;
    private IPSiteDao siteDao;
    private IPSTemplateService templateService;
    private IPSSiteTemplateService siteTemplateService;
    private IPSItemWorkflowService itemWorkflowService;
    private IPSTemplateDao templateDao;
    private IPSIdMapper idMapper;
    private IPSSystemProperties systemProps;
    private IPSPageDaoHelper pageDaoHelper;
    private IPSManagedNavService navService;
    private IPSCmsObjectMgr cmsMgr;
    
    private PSSiteCache siteCache = new PSSiteCache();
    
    private Integer defaultWorkflowId = null;
    
    private static String CATALOG_FOLDERS = pathSeparator() + concatPath(SECTION_SYSTEM_FOLDER_NAME, PAGE_CATALOG);
    private static Log log = LogFactory.getLog(PSPageCatalogService.class);
    
    @Autowired
    public PSPageCatalogService(IPSFolderHelper folderHelper, IPSPageDao pageDao, IPSiteDao siteDao,
            IPSSiteTemplateService siteTemplateService, IPSItemWorkflowService itemWorkflowService,
            IPSTemplateDao templateDao, IPSTemplateService templateService, IPSIdMapper idMapper,
            IPSNotificationService notifications, IPSPageDaoHelper pageDaoHelper, IPSManagedNavService navService,
            IPSSiteManager siteManager, IPSCmsObjectMgr cmsMgr)
    {
        this.folderHelper = folderHelper;
        this.pageDao = pageDao;
        this.siteDao = siteDao;
        this.siteTemplateService = siteTemplateService;
        this.itemWorkflowService = itemWorkflowService;
        this.templateDao = templateDao;
        this.templateService = templateService;
        this.idMapper = idMapper;
        this.pageDaoHelper = pageDaoHelper;
        this.cmsMgr = cmsMgr;
        this.navService = navService;
        
        //this.notifications = notifications;
        setNotifications(notifications);
    }
    
    @Override
    public List<String> findCatalogPages(String siteName) throws Exception
    {
        // find the site
        PSSiteSummary site = siteDao.findSummary(siteName);
        if (site == null)
        {
            // this would be a strange bug
            throw new RuntimeException("Unable to find cataloged pages, the specified site was not found: " + siteName);
        }
        
        // determine paths
        String catalogRoot = getCatalogFolderPath(site);
        
        return folderHelper.findItemIdsByPath(catalogRoot);
    }
    
    @Override
    public PSCatalogPageSummary getCatalogPageSummary(String id) throws PSDataServiceException {
        PSCatalogPageSummary sum = null;
        PSPage page = pageDao.find(id);
        if (page != null)
        {
            sum = new PSCatalogPageSummary();
            sum.setId(page.getId());
            sum.setName(page.getLinkTitle());
            String fullPath = concatPath(page.getFolderPath(), page.getName());
            sum.setPath(getPath(fullPath));
        }
        
        return sum;
    }    

    @Override
    public PSPage addCatalogPage(String siteName, String pageName, String linkText, String folderPath, String href) throws Exception
    {
        // validate inputs
        Validate.notEmpty(siteName);
        Validate.notEmpty(pageName);
        Validate.notEmpty(linkText);
        Validate.notEmpty(folderPath);
        Validate.notEmpty(href);
        
        
        log.debug("Catalog page: \"" + pageName + "\", path: \"" + folderPath + "\"");
        
        // find the site
        PSSiteSummary site = siteDao.findSummary(siteName);
        if (site == null)
        {
            // this would be a strange bug
            throw new RuntimeException("Unable to add cataloged page, the specified site was not found: " + siteName);
        }
        
        if (isMaxCountReached(site))
            return null;
        
        // determine paths
        String fullFolderPath = this.getFullFolderPath(folderPath, site);
        
        fullFolderPath = concatPath(getCatalogFolderPath(site), folderPath);
        
        String siteItemPath = concatPath(site.getFolderPath(), folderPath, pageName);
        String catItemPath = concatPath(fullFolderPath, pageName);
        
        PSPage page;
        
        // see if item exists and return null if found
        page = pageDao.findPageByPath(catItemPath);
        if (page != null)
            return null;
        
        // also check to see if real page exists
        page = pageDao.findPageByPath(siteItemPath);
        if (page != null)
            return null;

        
        if (pageWithFolderPathExists(fullFolderPath))
        {
            log.warn("Skip catalog page: \"" + catItemPath + "\". Because a page has been created at: \"" + fullFolderPath + "\"\n");
            return null;
        }
        
        return createPageStub(pageName, linkText, href, site, fullFolderPath, catItemPath);
    }

    private PSPage createPageStub(String pageName, String linkText, String href, PSSiteSummary site,
            String fullFolderPath, String catItemPath) throws IPSItemWorkflowService.PSItemWorkflowServiceException, PSDataServiceException, PSSiteImportException {
        PSPage page;
        page = new PSPage();
        
        page.setName(pageName);
        page.setTitle(linkText);
        page.setLinkTitle(linkText);
        page.setTemplateId(getCatalogTemplateId(site));
        page.setFolderPath(fullFolderPath);
        page.setDescription(href);
        page.setWorkflowId(getDefaultWorkflowId());
       
        // save it
        pageDao.save(page);
        incrementCatalogCount(site);
        
        // checkin the page
        itemWorkflowService.checkIn(page.getId());
        
        return pageDao.findPageByPath(catItemPath);
    }

    @Override
    public String getCatalogTemplateIdBySite(String siteName) throws PSPageException, IPSTemplateService.PSTemplateException, PSValidationException, PSSiteImportException {
        PSSiteSummary site = siteDao.findSummary(siteName);
        if (site == null)
        {
            throw new PSPageException("Unable to find the template id, the specified site was not found: " + siteName);
        }
        
        return getCatalogTemplateId(site);
    }
    
    @Override
    public void createImportedPage(String pageId) throws Exception
    {
        PSPage page = pageDao.find(pageId);

        if (page == null)
        {
            throw new PSPageException("Unable to move the cataloged page, the specified page id was not found: "
                    + pageId);
        }

        // determine full path for the item to be moved
        String fullPath = concatPath(page.getFolderPath(), page.getName());

        // determine path to move the item
        String newPageFolderPath = convertToImportedFolderPath(page.getFolderPath());

        // try to create the target folder if it doesn't exist
        if (!PSPathUtils.doesItemExist(newPageFolderPath))
            folderHelper.createFolder(newPageFolderPath);

        // move the item into the local location
        folderHelper.moveItem(newPageFolderPath, fullPath, false);
    }
    
    @Override
    public List<String> findImportedPageIds(String siteName) throws Exception
    {
        // find the site
        PSSiteSummary site = siteDao.findSummary(siteName);
        if (site == null)
        {
            //This happens if a site was never imported or if the site has been deleted.
            log.warn("Unable to find cataloged pages, the specified site was not found: " + siteName);
            return new ArrayList<>();
        }

        // list of imported page ids to return
        List<String> filteredPageIds = new ArrayList<>();
        
        try
        {
            List<String> importedPageIds = new ArrayList<>();
            
            String templateId = getCatalogTemplateIdBySite(siteName);

            importedPageIds = folderHelper.findItemIdsByPath(site.getFolderPath());

            List<String> catalogedPageIds = findCatalogPages(siteName);

            // remove the cataloged pages
            importedPageIds.removeAll(catalogedPageIds);

            // call query service to get the page IDs associated to unassigned template
            List<Integer> intPageIds = new ArrayList<>(importedPageIds.size());
            for (String importedId : importedPageIds)
            {
                int id = (new PSLegacyGuid(importedId)).getContentId();
                intPageIds.add(id);
            }

            Collection<Integer> pageIds = pageDaoHelper.findImportedPageIdsByTemplate(templateId, intPageIds);

            // convert the list to be returned again
            for (Integer pageId : pageIds)
            {    
                PSLegacyGuid guid = new PSLegacyGuid(pageId, -1);
                filteredPageIds.add(idMapper.getString(guid));
            }
            Collections.sort(filteredPageIds);

            return filteredPageIds;
        }
        catch (Exception e)
        {
            String msg = "An error occurred when getting cataloged pages for site: " + siteName;
            throw new PSPageException(msg, e);
        }
    }
    
    /**
     * Set the system properties on this service.  This service will always use the the values provided by
     * the most recently set instance of the properties.
     * 
     * @param systemProps the system properties
     */
    @Autowired
    public void setSystemProps(IPSSystemProperties systemProps)
    {
        this.systemProps = systemProps;
    }
    
    /**
     * Gets the system properties used by this service.
     * 
     * @return The properties
     */
    public IPSSystemProperties getSystemProps()
    {
        return systemProps;
    }

    /* (non-Javadoc)
     * @see com.percussion.pagemanagement.service.IPSPageCatalogService#isImportedPage(com.percussion.sitemanage.data.PSSite, com.percussion.pagemanagement.data.PSPage)
     */
    @Override
    public boolean doesImportedPageExist(PSPage page)
    {
        String targetPath = convertToImportedFolderPath(concatPath(page.getFolderPath(),
                page.getName()));

        if (doesItemExist(targetPath))
        {
            return true;
        }
        return false;
    }
    
    /**
     * Convert the full folder path to the path the catalog page would have once imported.
     * 
     * @param folderPath The path to convert, assumed not <code>null<code/> or empty. 
     * 
     * @return The path, not <code>null</code>, empty if no paths are found.
     */
    private String getPath(String folderPath)
    {
        return StringUtils.substringAfter(folderPath, CATALOG_FOLDERS);
    }    

    /**
     * Get the full path to the catalog folder for the supplied site
     * @param site The site, assumed not <code>null</code>
     * 
     * @return The catalog folder path for the site
     */
    private String getCatalogFolderPath(PSSiteSummary site)
    {
        return concatPath(site.getFolderPath(), CATALOG_FOLDERS);
    }

    private Integer getDefaultWorkflowId()
    {
        if (defaultWorkflowId == null)
        {
            IPSWorkflowService workflowService = PSWorkflowServiceLocator.getWorkflowService();
            defaultWorkflowId = workflowService.getDefaultWorkflowId().getUUID();            
        }

        return defaultWorkflowId;
    }

    public String getCatalogTemplateId(PSSiteSummary site) throws PSValidationException, IPSTemplateService.PSTemplateException, PSSiteImportException {
        String templateId = null;
        String siteId = site.getId();
        
        // cache by siteid since most calls will repeatedly be on behalf of a single site
        templateId = siteCache.getSiteTemplateId(site.getSiteId());
        if (templateId == null)
        {
            List<PSTemplateSummary> templates = siteTemplateService.findTypedTemplatesBySite(siteId, PSTemplateTypeEnum.UNASSIGNED);
            for (PSTemplateSummary sum : templates)
            {
                if (sum.getName().equals(IPSSitemanageConstants.UNASSIGNED_TEMPLATE_NAME))
                {
                    templateId = sum.getId();
                    break;
                }
            }
            
            if (templateId == null)
            {
                // none yet, create one
                IPSAssemblyTemplate baseTemplate = templateDao.loadBaseTemplateByName(IPSSitemanageConstants.PLAIN_BASE_TEMPLATE_NAME);
                PSTemplateSummary templateSummary = templateService.createTemplate(IPSSitemanageConstants.UNASSIGNED_TEMPLATE_NAME,
                        idMapper.getString(baseTemplate.getGUID()), siteId, PSTemplateTypeEnum.UNASSIGNED);
                templateId = templateSummary.getId();
                
                // set the right theme
                setSiteThemeInTemplate(templateId, site);
            }
            
            siteCache.setSiteTemplateid(site.getSiteId(), templateId);
        }
        
        return templateId;
    }

    /**
     * Retrieves the theme name from the site's home template and sets it to the
     * unassigned template for that site.
     * 
     * @param templateId {@link String} with the id of the unassigned template.
     *            Assumed not <code>null</code>.
     * @param siteSummary {@link PSSiteSummary} of the site, assumed not
     *            <code>null</code>.
     */
    private void setSiteThemeInTemplate(String templateId, PSSiteSummary siteSummary) throws PSSiteImportException {
        try
        {
            // get the root folder's id
            IPSGuid rootFolder = idMapper.getGuid(folderHelper.findFolder(siteSummary.getFolderPath()).getId());

            // get the home page
            IPSGuid rootNavNode = navService.findNavigationIdFromFolder(rootFolder);
            IPSGuid homePageId = navService.getLandingPageFromNavnode(rootNavNode);
            PSPage homePage = pageDao.find(homePageId.toString());
            
            // get the home page template and the theme
            PSTemplateSummary homeTemplateSummary = templateService.find(homePage.getTemplateId());
            PSTemplate homeTemplate = templateService.load(homeTemplateSummary.getId());
    
            String theme = homeTemplate.getTheme();
            
            // save the changes
            PSTemplate targetTemplate = templateService.load(templateId);
            targetTemplate.setTheme(theme);
            templateService.save(targetTemplate);        
        }
        catch (Exception e)
        {
            // we could not set the right theme, so leave the default theme in
            // the template
            String msg = "Failed to get template from the home page for site name = '" + siteSummary.getName() + "'.";
            log.error(msg, e);
            throw new PSSiteImportException(msg, e);
        }
    }

    /**
     * Register listener whenever an design object has been changed.
     * 
     * @param notifyService the notification service to set, assumed not <code>null</code>
     */
    private void setNotifications(IPSNotificationService notifyService)
    {
       notifyService.addListener(EventType.SITE_DELETED, new ChangeNotificationListener());
    }
    
    /**
     * Listener which invalidates locally cached information
     */
    private final class ChangeNotificationListener implements IPSNotificationListener
    {
       public void notifyEvent(PSNotificationEvent notification)
       {
          if (notification.getType() != EventType.SITE_DELETED)
              return;
          
          IPSGuid guid = (IPSGuid) notification.getTarget();
          siteCache.remove(guid.longValue());
       }
    }

    /**
     * Increment the catalog count for the specified site by 1.  Assumes
     * has already been called.
     * 
     * @param site the site to increment the count for, assumed not <code>null<code/> or empty.
     * 
     * @return The new value
     */
    private int incrementCatalogCount(PSSiteSummary site)
    {
        AtomicInteger count = siteCache.getCatalogCount(site.getSiteId());
        if (count == null)
            throw new IllegalStateException("catalogCount for site has not been initialized: " + site.getName());
        
        return count.incrementAndGet();
    }

    /**
     * Determine if the max number of pages have already been cataloged for the specified site.
     *  
     * @param site The site to check for, assumed not <code>null</code>.
     * 
     * @return <code>true</code> if it has, <code>false</code> if more pages can be cataloged.
     */
    private boolean isMaxCountReached(PSSiteSummary site) throws Exception
    {
        String maxVal = systemProps.getProperty(CATALOG_PAGE_MAX);
        int max = NumberUtils.toInt(maxVal, -1);
        
        int count = getCatalogCount(site);
        
        // if value is negative, then no limit
        if (max < 0)
            return false;
        
        return count >= max;
    }
    
    /**
     * Get the current count of cataloged pages for the specified site
     * 
     * @param site The site to check for, assumed not <code>null</code>.
     * 
     * @return The count
     */
    private int getCatalogCount(PSSiteSummary site) throws Exception
    {
        AtomicInteger count = siteCache.getCatalogCount(site.getSiteId());
        if (count == null)
        {
            int catCount = findCatalogPages(site.getName()).size();
            count = new AtomicInteger(catCount);
            siteCache.setCatalogCount(site.getSiteId(), count);
        }
        
        return count.get();
    }
    
    
    public String getFullFolderPath(String folderPath, PSSiteSummary site)
    {
    	String catalogRoot = getCatalogFolderPath(site);
        String fullFolderPath = concatPath(catalogRoot, folderPath);
        return fullFolderPath;
    }
    
    /**
     * Check if any page already exist with exactly the given path.
     * 
     * @param fullFolderPath the folder path.
     * @return <code>true</code> if a page with the given folder path exists.
     *  <code>false</code> otherwise.
     */
    public boolean pageWithFolderPathExists(String fullFolderPath)
    {
    	
        
        int contentId = PSPathUtils.getIdByPath(fullFolderPath);
        if (contentId == -1)
            return false;
        
        IPSItemEntry item = cmsMgr.findItemEntry(contentId);
        return (item != null && (!item.isFolder()));        
    }

    private class PSSiteCache
    {
        private Map<Long, String> templateBySiteCache = new ConcurrentHashMap<>();
        private Map<Long, AtomicInteger> catalogCountBySiteCache = new ConcurrentHashMap<>();
        
        public String getSiteTemplateId(Long siteId)
        {
            return templateBySiteCache.get(siteId);
        }

        /**
         * @param siteId
         */
        public void remove(Long siteId)
        {
            templateBySiteCache.remove(siteId);
            catalogCountBySiteCache.remove(siteId);
        }

        public void setSiteTemplateid(Long siteId, String templateId)
        {
            templateBySiteCache.put(siteId, templateId);
        }

        public AtomicInteger getCatalogCount(Long siteId)
        {
            return catalogCountBySiteCache.get(siteId);
        }
        
        public void setCatalogCount(Long siteId, AtomicInteger count)
        {
            catalogCountBySiteCache.put(siteId, count);
        }
    }

    public String convertToImportedFolderPath(String catalogedPagePath)
    {
        return catalogedPagePath.replaceFirst(CATALOG_FOLDERS, "");
    }
}
