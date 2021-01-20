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

package com.percussion.sitemanage.service.impl;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.percussion.assetmanagement.dao.IPSAssetDao;
import com.percussion.assetmanagement.data.PSAsset;
import com.percussion.assetmanagement.service.IPSWidgetAssetRelationshipService;
import com.percussion.cms.objectstore.PSFolder;
import com.percussion.designmanagement.service.IPSFileSystemService;
import com.percussion.fastforward.managednav.IPSManagedNavService;
import com.percussion.itemmanagement.service.IPSItemService;
import com.percussion.itemmanagement.service.IPSItemWorkflowService;
import com.percussion.linkmanagement.service.IPSManagedLinkService;
import com.percussion.monitor.process.PSSiteCopyProcessMonitor;
import com.percussion.pagemanagement.dao.IPSPageDao;
import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.data.PSTemplateSummary;
import com.percussion.pagemanagement.service.IPSTemplateService;
import com.percussion.pagemanagement.service.impl.PSPageService;
import com.percussion.pathmanagement.data.PSDeleteFolderCriteria;
import com.percussion.pathmanagement.data.PSFolderPermission;
import com.percussion.pathmanagement.data.PSPathItem;
import com.percussion.pathmanagement.service.impl.PSAssetPathItemService;
import com.percussion.pathmanagement.service.impl.PSPathService;
import com.percussion.pathmanagement.service.impl.PSPathUtils;
import com.percussion.pubserver.IPSPubServerService;
import com.percussion.queue.IPSPageImportQueue;
import com.percussion.recent.service.rest.IPSRecentService;
import com.percussion.search.PSSearchIndexEventQueue;
import com.percussion.server.PSServer;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.contentchange.IPSContentChangeService;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.pubserver.IPSPubServerDao;
import com.percussion.services.pubserver.PSPubServerDaoLocator;
import com.percussion.services.pubserver.data.PSPubServer;
import com.percussion.services.siteimportsummary.data.PSSiteImportSummary;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.share.async.IPSAsyncJob;
import com.percussion.share.async.IPSAsyncJobService;
import com.percussion.share.async.PSAsyncJobStatus;
import com.percussion.share.dao.IPSFolderHelper;
import com.percussion.share.dao.IPSGenericDao.DeleteException;
import com.percussion.share.dao.PSFolderPathUtils;
import com.percussion.share.data.IPSItemSummary;
import com.percussion.share.data.PSEnumVals;
import com.percussion.share.data.PSMapWrapper;
import com.percussion.share.service.IPSIdMapper;
import com.percussion.share.service.PSAbstractDataService;
import com.percussion.share.service.PSSiteCopyUtils;
import com.percussion.share.service.exception.PSParameterValidationUtils;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.share.spring.PSSpringWebApplicationContextUtils;
import com.percussion.share.validation.PSValidationErrors;
import com.percussion.share.validation.PSValidationErrorsBuilder;
import com.percussion.sitemanage.dao.IPSiteDao;
import com.percussion.sitemanage.dao.impl.PSSitePublishDao;
import com.percussion.sitemanage.data.*;
import com.percussion.sitemanage.error.PSSiteImportException;
import com.percussion.sitemanage.importer.IPSSiteImportLogger.PSLogObjectType;
import com.percussion.sitemanage.importer.dao.IPSImportLogDao;
import com.percussion.sitemanage.importer.data.PSImportLogEntry;
import com.percussion.sitemanage.service.IPSSiteDataService;
import com.percussion.sitemanage.service.IPSSiteImportService;
import com.percussion.sitemanage.service.IPSSiteSectionService;
import com.percussion.sitemanage.service.IPSSiteTemplateService;
import com.percussion.sitesummaryservice.service.IPSSiteImportSummaryService;
import com.percussion.user.service.IPSUserService;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSSiteManageBean;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.service.impl.PSSiteConfigUtils;
import com.percussion.utils.types.PSPair;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.publishing.IPSPublishingWs;
import com.percussion.webservices.security.IPSSecurityWs;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

import static com.percussion.share.dao.PSFolderPermissionUtils.getFolderPermission;
import static com.percussion.share.service.exception.PSParameterValidationUtils.validateParameters;
import static com.percussion.share.spring.PSSpringWebApplicationContextUtils.getWebApplicationContext;
import static com.percussion.utils.service.impl.PSSiteConfigUtils.*;
import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;

@Component("siteDataService")
@PSSiteManageBean
@Lazy
 public class PSSiteDataService extends PSAbstractDataService<PSSite, PSSiteSummary, String>
        implements
            IPSSiteDataService
{
 	private static final Log log = LogFactory.getLog(PSSiteDataService.class);

    private static final String SITE_IMPORT_JOB_BEAN = "siteImportJob";

    private static final String IMPORT_STATUS_MESSAGE_PREFIX = "Importing site:";

    private static final int MAX_ERROR_MESSAGES = 100;

    private static final String ABRIDGED_ERROR_MESSAGE = "Displaying the first 100 import issues. Please download the Import Report Log for the full list of missing files and other import issues.";

    @Autowired
    private IPSSecurityWs securityWs;

    @Autowired
    private PSPageService psPageService;

    @Autowired
    private PSPathService psPathService;

    private IPSiteDao siteDao;

    private IPSPublishingWs publishingWs;

    private IPSSiteManager siteMgr;

    private IPSManagedNavService navService;

    private IPSIdMapper idMapper;

    private IPSSiteSectionService sectionService;

    private IPSContentWs contentWs;

    private IPSUserService userService;

    private PSSitePublishDao sitePublishDao;

    private IPSSiteTemplateService siteTemplateService;

    private IPSPageDao pageDao;

    private IPSItemWorkflowService itemWorkflowService;

    private IPSWidgetAssetRelationshipService widgetAssetRelationshipService;

    private IPSAssetDao assetDao;

    private IPSItemService itemService;

    private IPSFolderHelper folderHelper;

    private IPSSiteImportService siteImportService;

    private IPSAsyncJobService asyncJobService;

    private IPSPageImportQueue pageImportQueue;

    private IPSManagedLinkService managedLinkService;

    private IPSTemplateService templateService;

    private IPSFileSystemService fileSystemService;

    private IPSImportLogDao importLogDao;

    private IPSSiteImportSummaryService summaryService;

    private IPSContentChangeService contentChangeService;
    
    private IPSPubServerService pubserverService;

    private IPSRecentService recentService;

    @Autowired
    public PSSiteDataService(IPSiteDao dao, IPSPublishingWs publishingWs, IPSSiteManager siteMgr,
            IPSManagedNavService navService, IPSIdMapper idMapper, IPSSiteSectionService sectionService,
            IPSContentWs contentWs, IPSUserService userService, PSSitePublishDao sitePublishDao,
            IPSSiteTemplateService siteTemplateService, IPSPageDao pageDao, IPSItemWorkflowService itemWorkflowService,
            IPSWidgetAssetRelationshipService widgetAssetRelationshipService, IPSAssetDao assetDao,
            IPSItemService itemService, IPSFolderHelper folderHelper,@Qualifier("siteImportService") IPSSiteImportService siteImportService,
            IPSAsyncJobService asyncJobService, IPSPageImportQueue pageImportQueue, IPSImportLogDao importLogDao,
            IPSSiteImportSummaryService summaryService, IPSContentChangeService contentChangeService, IPSRecentService recentService)
    {
        super(dao);
        siteDao = dao;
        this.publishingWs = publishingWs;
        this.siteMgr = siteMgr;
        this.navService = navService;
        this.idMapper = idMapper;
        this.sectionService = sectionService;
        this.contentWs = contentWs;
        this.userService = userService;
        this.sitePublishDao = sitePublishDao;
        this.siteTemplateService = siteTemplateService;
        this.pageDao = pageDao;
        this.itemWorkflowService = itemWorkflowService;
        this.widgetAssetRelationshipService = widgetAssetRelationshipService;
        this.assetDao = assetDao;
        this.itemService = itemService;
        this.folderHelper = folderHelper;
        this.siteImportService = siteImportService;
        this.asyncJobService = asyncJobService;
        this.pageImportQueue = pageImportQueue;
        this.importLogDao = importLogDao;
        this.summaryService = summaryService;
        this.contentChangeService = contentChangeService;
        this.recentService = recentService;
    }
    public PSSite load(String id) throws DataServiceLoadException
    {
        return super.load(id);
    }

    public PSSiteSummary find(String id) throws com.percussion.share.service.IPSDataService.DataServiceLoadException
    {
        return find(id, false);
    }

    public PSSiteSummary findByName(String name)throws com.percussion.share.service.IPSDataService.DataServiceLoadException{
        PSParameterValidationUtils.rejectIfBlank("findByName", "name", name);

        PSSiteSummary sum = siteDao.findByName(name);

        if (sum == null)
            throw new DataServiceLoadException("Could not find site for name: " + name);

        return sum;
    }

    public PSSiteSummary find(String id, boolean includePubInfo) throws com.percussion.share.service.IPSDataService.DataServiceLoadException
    {
        PSParameterValidationUtils.rejectIfBlank("find", "id", id);
        PSSiteSummary sum = siteDao.findSummary(id);
        if (sum == null)
            throw new DataServiceLoadException("Could not find site for id: " + id);
        if(includePubInfo){
            try
            {
                sum.setPubInfo(getPubServerService().getS3PubInfo(new PSGuid(PSTypeEnum.SITE, sum.getSiteId())));
            }
            catch (Exception e)
            {
                throw new DataServiceLoadException(e);
            }
        }
        return sum;
    }

    
    public PSSiteProperties getSiteProperties(String siteName)
    {
        IPSSite site = siteMgr.findSite(siteName);
        if (site == null)
        {
            PSValidationErrorsBuilder builder = validateParameters("getSiteProperties");
            String msg = "Cannot find site with name: \"" + siteName + "\"";
            builder.reject("siteName", msg).throwIfInvalid();
            return null;
        }
        IPSGuid treeId = navService.findNavigationIdFromFolder(site.getFolderRoot());
        String linkTitle = "";
        
        if(treeId!=null){
        	linkTitle = navService.getNavTitle(treeId);
        }else{
        	log.error("Can't find a root NavOn for site " + siteName + " in Root Folder " + site.getFolderRoot());
        }
        
        PSSiteProperties props = new PSSiteProperties();
        props.setId(idMapper.getString(site.getGUID()));
        props.setName(site.getName());
        props.setHomePageLinkText(linkTitle);
        props.setDescription(site.getDescription());
        props.setFolderPermission(getSiteRootPermission(site));
        updatePropsFromSite(site, props);
        // Get the site's class names using PSSiteSection object
        PSSiteSection siteSection = sectionService.loadRoot(site.getName());
        props.setCssClassNames(siteSection.getCssClassNames());

        return props;
    }

    private void updatePropsFromSite(IPSSite site, PSSiteProperties props) {
        props.setLoginPage(site.getLoginPage());
        props.setRegistrationPage(site.getRegistrationPage());
        props.setRegistrationConfirmationPage(site.getRegistrationConfirmationPage());
        props.setResetPage(site.getResetPage());
        props.setResetRequestPasswordPage(site.getResetRequestPasswordPage());
        props.setSecure(site.isSecure());
        props.setDefaultFileExtention(site.getDefaultFileExtention());
        props.setCanonical(site.isCanonical());
        props.setSiteProtocol(site.getSiteProtocol());
        props.setDefaultDocument(site.getDefaultDocument());
        props.setCanonicalDist(site.getCanonicalDist());
        props.setCanonicalReplace(site.isCanonicalReplace());
        props.setOverrideSystemJQuery(site.isOverrideSystemJQuery());
        props.setOverrideSystemJQueryUI(site.isOverrideSystemJQueryUI());
        props.setOverrideSystemFoundation(site.isOverrideSystemFoundation());
        props.setSiteAdditionalHeadContent(site.getSiteAdditionalHeadContent());
        props.setSiteAfterBodyOpenContent(site.getSiteAfterBodyOpenContent());
        props.setSiteBeforeBodyCloseContent(site.getSiteBeforeBodyCloseContent());
        props.setMobilePreviewEnabled(site.isMobilePreviewEnabled());
    }

    public PSSitePublishProperties getSitePublishProperties(String siteName)
    {
        IPSSite site = siteMgr.findSite(siteName);
        if (site == null)
        {
            PSValidationErrorsBuilder builder = validateParameters("getSitePublishProperties");
            String msg = "Cannot find site with name: \"" + siteName + "\"";
            builder.reject("siteName", msg).throwIfInvalid();
            return null;
        }
        PSSitePublishProperties publishProps = new PSSitePublishProperties();
        publishProps.setId(idMapper.getString(site.getGUID()));
        publishProps.setSiteName(site.getName());
        publishProps.setFtpServerName(site.getIpAddress());
        publishProps.setFtpUserName(site.getUserId());
        publishProps.setFtpPassword(site.getPassword());
        publishProps.setFtpServerPort(site.getPort());
        String siteRoot = site.getRoot();
        if (StringUtils.isBlank(siteRoot))
            publishProps.setDeliveryRootPath("");
        else
            publishProps.setDeliveryRootPath(sitePublishDao.getPublishingBase(site.getRoot(), site.getName()));
        publishProps.setPublishType(PublishType.valueOf(siteDao.getSiteDeliveryType(site)));
        publishProps.setPrivateKey(site.getPrivateKey());
        publishProps.setSecure(site.isSecure());
        return publishProps;
    }

    /**
     * Gets the folder permission for the specified site root.
     * 
     * @param site the site, assumed not <code>null</code>.
     * @return the folder permission, never <code>null</code>.
     */
    private PSFolderPermission getSiteRootPermission(IPSSite site)
    {
        IPSGuid folderId = contentWs.getIdByPath(site.getFolderRoot());
        if (folderId == null)
        {
            PSValidationErrorsBuilder builder = validateParameters("getSiteProperties");
            String msg = "Cannot find site root folder for site name: \"" + site.getName() + "\"";
            builder.reject("siteRootFolder", msg).throwIfInvalid();
        }
        PSFolder folder = contentWs.loadFolder(folderId, false);
        return getFolderPermission(folder);
    }

    public PSSiteProperties updateSiteProperties(PSSiteProperties props)
    {
        notNull(props, "Properties cannot be null");
        IPSSite site = siteMgr.loadSiteModifiable(idMapper.getGuid(props.getId()));
        String newSiteName = props.getName();
        PSSiteCopyUtils.throwCopySiteMessageForUpdateError(site.getName(), newSiteName, "updateSiteProperties");
        validateSiteProperties(site, props);

        updateSiteConfigurationFiles(site, props);

        // find the list of auto list widgets that are used by old site name.
        updateAutoListAssetsForSite(site, props);

        // update the psx_recent table entries.
        updatePSRecentEntries(site, props);

        updatePubServers(site, props);

        // update navtree, folder and landing page
        PSSiteSectionProperties section = new PSSiteSectionProperties();
        IPSGuid treeId = navService.findNavigationIdFromFolder(site.getFolderRoot());
        section.setId(idMapper.getString(treeId));
        section.setFolderName(newSiteName);
        section.setTitle(props.getHomePageLinkText());
        section.setFolderPermission(props.getFolderPermission());
        section.setSiteRootSection(true);
        section.setCssClassNames(props.getCssClassNames());

        updateSiteFromProps(site, props);

        boolean pubServersChanged = siteDao.updateSite(site, newSiteName, props.getDescription());
        props.setPubServersChanged(pubServersChanged);

        sectionService.update(section);
        return props;
    }

    private void updateSiteFromProps(IPSSite site, PSSiteProperties props) {
        site.setLoginPage(props.getLoginPage());
        site.setRegistrationPage(props.getRegistrationPage());
        site.setRegistrationConfirmationPage(props.getRegistrationConfirmationPage());
        site.setResetPage(props.getResetPage());
        site.setResetRequestPasswordPage(props.getResetRequestPasswordPage());
        site.setSecure(props.isSecure());
        site.setDefaultFileExtention(props.getDefaultFileExtention());
        site.setCanonical(props.isCanonical());
        site.setSiteProtocol(props.getSiteProtocol());
        site.setDefaultDocument(props.getDefaultDocument());
        site.setCanonicalDist(props.getCanonicalDist());
        site.setCanonicalReplace(props.isCanonicalReplace());
        site.setOverrideSystemJQuery(props.isOverrideSystemJQuery());
        site.setOverrideSystemJQueryUI(props.isOverrideSystemJQueryUI());
        site.setOverrideSystemFoundation(props.isOverrideSystemFoundation());
        site.setSiteAdditionalHeadContent(props.getSiteAdditionalHeadContent());
        site.setSiteAfterBodyOpenContent(props.getSiteAfterBodyOpenContent());
        site.setSiteBeforeBodyCloseContent(props.getSiteBeforeBodyCloseContent());
        site.setMobilePreviewEnabled(props.isMobilePreviewEnabled());
    }

    private void updatePSRecentEntries(IPSSite site, PSSiteProperties props) {
        recentService.updateSiteNameRecent(site.getName(), props.getName());
    }

    /**
     * Updates the site's auto  list widgets when a site is renamed.
     * @param site the original site information.
     * @param props the updated site information.
     */
    private void updateAutoListAssetsForSite(IPSSite site, PSSiteProperties props)
    {
        log.debug("Updating the auto list entries for the site: " + site.getName());
        if (!site.getName().equals(props.getName()))
        {
            Collection<PSAsset> autoListAssets = findAutoListWidgets();

            for (PSAsset asset : autoListAssets) {
                updateSiteListAsset(asset, props.getName(), site.getName(), new HashMap<String, String>());
            }
        }
    }

    /**
     * Finds all auto list widgets by content type.
     * I.E. percPageAutoList, percTagList, etc.
     * @return a collection of PSAsset objects based on the auto
     *         list types defined in {@link PSSiteDataService#siteListAssetMap}.
     *         May be empty, never <code>null</code>.
     */
    private Collection<PSAsset> findAutoListWidgets() {
        Collection<PSAsset> assets = new ArrayList<>();

        for (String contentTypeName : siteListAssetMap.keySet()) {
            try {
                Collection<PSAsset> collAssets = assetDao.findByType(contentTypeName);
                assets.addAll(collAssets);
            } catch (Exception e) {
                log.error("Unable to find assets with type: " + contentTypeName, e);
            }
        }

        return assets;
    }


    public PSSitePublishProperties updateSitePublishProperties(PSSitePublishProperties publishProps)
    {
        notNull(publishProps, "Publish Properties cannot be null");
        validateSitePublishProperties(publishProps);
        IPSSite site = siteMgr.loadSiteModifiable(idMapper.getGuid(publishProps.getId()));

        // force the publication of the configuration files
        handleRemoveTouchedFile(site.getName());

        // Update the site object with publish properties
        siteDao.updateSitePublishProperties(site, publishProps);

        return publishProps;
    }

    /**
     * Updates the publishing server to set the
     * {@link PSPubServer#setSiteRenamed()} flag.
     *
     * @param site the site object
     * @param props
     */
    private void updatePubServers(IPSSite site, PSSiteProperties props) {
        log.info("Updating publishing server for site: " + site.getName());
        if (!site.getName().equals(props.getName())) {
            try {
                PSPubServer pubServer = getPubServerService().getDefaultPubServer(site
                        .getGUID());
                pubServer.setSiteRenamed(true);

                IPSPubServerDao pubServerDao = PSPubServerDaoLocator.getPubServerManager();
                pubServerDao.savePubServer(pubServer);
            } catch (Exception e) {
                log.error("Error updating PSPubServer flag setSiteRenamed while renaming site: "
                        + site.getName());
            }
        }
    }

    /**
     * Verifies if the site is being imported or not.
     * 
     * @param sitename {@link String} with the name of the site. Must not be
     *            <code>null</code>.
     * @return <code>true</code> if there is a page for the site currently being
     *         imported. <code>false</code> otherwise.
     */
  
    @Override
    public String isSiteBeingImported(String sitename)
    {
        PSSite site = siteDao.find(sitename);
        List<Integer> importingPages = null;
        if (site!=null)
        {
            importingPages = pageImportQueue.getImportingPageIds(site.getSiteId());
        }
        return (site!=null && importingPages.size() != 0) ? Boolean.TRUE.toString() : Boolean.FALSE.toString();
    }

    /**
     * Validates the new properties against the original / source site.
     * 
     * @param site the source site, assumed not <code>null</code>.
     * @param props the new site properties, assumed not <code>null</code>.
     */
    private void validateSiteProperties(IPSSite site, PSSiteProperties props)
    {
        notNull(props.getName(), "Name cannot be null.");
        notEmpty(props.getName(), "Name cannot be empty.");
        notNull(props.getHomePageLinkText(), "Home page link text cannot be null.");
        notEmpty(props.getHomePageLinkText(), "Home page link text cannot be empty.");

        String name = props.getName();
        String tgtFolderRoot = "//Sites/" + name;
        if (tgtFolderRoot.equalsIgnoreCase(site.getFolderRoot()))
        {
            return;
        }

        PSValidationErrorsBuilder builder = validateParameters("saveSiteProperties");

        if (siteMgr.findSite(name) != null)
        {
            String msg = "Cannot rename site \"" + site.getName() + "\" to an existing site name: \"" + name + "\".";
            builder.rejectField("name", msg, name).throwIfInvalid();
        }
        if (contentWs.isChildExistInFolder("//Sites", name))
        {
            String msg = "Cannot rename site \"" + site.getName() + "\" to an existing site folder: \"" + name + "\".";
            builder.rejectField("name", msg, name).throwIfInvalid();
        }
    }

    /**
     * Checks to see that required incoming publish properties are not null and
     * empty
     * 
     * @param publishProperties the site publish properties, assumed not
     *            <code>null</code>.
     */
    private void validateSitePublishProperties(PSSitePublishProperties publishProperties)
    {
        notNull(publishProperties.getSiteName(), "Name cannot be null.");
        notEmpty(publishProperties.getSiteName(), "Name cannot be empty.");
        if (!publishProperties.getPublishType().equals(PublishType.filesystem))
        {
            notNull(publishProperties.getFtpServerName(), "Ftp Server name cannot be null.");
            notEmpty(publishProperties.getFtpServerName(), "Ftp Server name cannot be empty.");
            notNull(publishProperties.getFtpUserName(), "Ftp user name cannot be null.");
            notEmpty(publishProperties.getFtpUserName(), "Ftp user name cannot be empty.");
        }
    }

    @Override
    public PSSiteSummary findByLegacySiteId(String id, boolean isValidate) throws DataServiceLoadException
    {
        PSParameterValidationUtils.rejectIfBlank("findByLegacySiteId", "id", id);
        PSSiteSummary sum = siteDao.findByLegacySiteId(id, isValidate);
        if (sum == null)
            throw new DataServiceLoadException("Could not find site for legacy id: " + id);
        return sum;
    }

    @Override
    public List<PSSiteSummary> findAll()
    {
        return findAll(false);
    }

    public List<PSSiteSummary> findAll(boolean includePubInfo)
    {
        List<PSSiteSummary> sums = siteDao.findAllSummaries();
        if (sums == null)
            return new ArrayList<PSSiteSummary>();

        ArrayList<IPSGuid> guids = new ArrayList<IPSGuid>();
        for(PSSiteSummary s: sums){
            guids.add(new PSGuid(PSTypeEnum.SITE, s.getSiteId()));
        }

        //Filter out sites that shouldn't be visible
        //TODO: Re-enable when site permissions are fixed
        /**
        List<IPSGuid> filtered_guids = securityWs.filterByRuntimeVisibility(guids);
        Iterator iterator = sums.iterator();
        while(iterator.hasNext()){
           PSSiteSummary s = (PSSiteSummary)iterator.next();
            //Need iterator to safely remove to avoid concurrentmodification exceptions
            boolean matched = false;
            for(IPSGuid g : filtered_guids) {

                if(g.getUUID() == s.getSiteId()) {
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                iterator.remove();
            }
        }
        **/

        // Filter out sites that are currently getting copied
        Map<String, String> entries = getCopySiteInfo().getEntries();

        if (!entries.isEmpty())
        {
            String newSiteName = entries.get("Target");
            for (PSSiteSummary site : sums)
            {
                if (StringUtils.equals(site.getName(), newSiteName))
                {
                    sums.remove(site);
                    break;
                }
            }
        }

        //Filter out Sites that shouldn't be visible

        if(includePubInfo)
        {
            for (PSSiteSummary sum : sums)
            {
                try
                {
                    sum.setPubInfo(getPubServerService().getS3PubInfo(new PSGuid(PSTypeEnum.SITE, sum.getSiteId())));
                }
                catch (Exception e)
                {
                    log.error("Error adding the publishing info to the site.", e);
                }
            }
        }
        return sums;
    }

    public PSEnumVals getChoices()
    {
        PSEnumVals choices = new PSEnumVals();

        List<PSSiteSummary> sums = findAll();
        for (PSSiteSummary sum : sums)
        {
            choices.addEntry(sum.getName(), null);
        }

        return choices;
    }

    public PSMapWrapper getCopySiteInfo()
    {
        return PSSiteCopyUtils.getCopySiteInfo();
    }
    
    @Override
    public void delete(String siteName)
    {
        PSValidationErrorsBuilder builder = validateParameters("delete").rejectIfBlank("id", siteName).throwIfInvalid();
        PSSiteCopyUtils.throwCopySiteMessageIfNotAllowed(siteName, "delete", PSSiteCopyUtils.CAN_NOT_DELETE_SITE);
        if (!isCurrentUserAdmin())
        {
            builder.reject("site.deleteNotAuthorized", "You are not authorized to delete the site");
            builder.throwIfInvalid();
        }

        if (!publishingWs.getInProgressPublishingJobs(siteName).isEmpty())
        {
            builder.reject("site.isPublishing", "Site is publishing");
            builder.throwIfInvalid();
        }

        IPSSite site = siteMgr.findSite(siteName);
        //FB: NP_NULL_ON_SOME_PATH NC 1-16-16
        if (site != null)
        {
             handleRemoveSiteAndTouchedConfiguration(site.getName());
       
	        contentChangeService.deleteChangeEventsForSite(site.getSiteId());
            try{
                String folderPath = site.getFolderRoot();
                IPSItemSummary folder = folderHelper.findFolder(folderPath);
                List<PSPathItem> children = psPathService.findChildren(folderPath);
                if(children != null) {
                    for (PSPathItem cPath:children
                    ) {
                        IPSItemSummary is = folderHelper.findItem(cPath.getFolderPath());
                        if(is.isFolder()) {
                            PSDeleteFolderCriteria dc = new PSDeleteFolderCriteria();
                            dc.setPath(cPath.getPath());
                            dc.setShouldPurge(false);
                            psPathService.deleteFolder(dc);
                        }
                        if(is.isPage()){
                            psPageService.delete(is.getId(),true);
                        }
                    }

                }
            }catch (Exception e) {
                String msg = "Failed to Move Site Items to Recycle Folder";
                log.error(msg, e);
                throw new RuntimeException("Unable to delete site as Failed to Move Site Items to Recycle Folder: " + siteName);
            }

	        super.delete(siteName);
	        handleUpdateAllowedSites(site);
        }else{
        	   builder.reject("site.notFound", "Unable to find site with id of " + siteName);
               builder.throwIfInvalid();
        }
    }

    /**
     * Updated the allowed sites property of the folder passed to the method. It
     * removes the siteId String sent by the second param.
     * 
     * @param childItem - the item summary of the folder to allow the property.
     *            Never <code> null </code> or empty
     * @param siteId - a string representation of the site legacy id. Never
     *            <code> null </code> or empty
     */
    private void updateAllowedSitesProperty(IPSItemSummary childItem, String siteId)
    {
        try
        {
            PSFolder folder = contentWs.loadFolder(idMapper.getGuid(childItem.getId()), false);

            if (folder.getProperty(IPSHtmlParameters.SYS_ALLOWEDSITES) != null)
            {
                ArrayList<String> listOfSites = new ArrayList<String>(Arrays.asList(folder
                        .getProperty(IPSHtmlParameters.SYS_ALLOWEDSITES).getValue().split(",")));
                listOfSites.remove(siteId);
                folder.setProperty(IPSHtmlParameters.SYS_ALLOWEDSITES, StringUtils.join(listOfSites.toArray(), ","));
                //Save the folder. 
                contentWs.saveFolder(folder);
            }
        }
        catch (Exception e)
        {
            String msg = "The folderPath comes from a constant so there's no way the method throws the exception";
            log.error(msg, e);
        }
    }

    /**
     * Helper method to update the allowed sites property for all root level
     * folders when a site is deleted.
     * 
     * @author federicoromanelli
     * @param site - the site object that's being deleted. Never
     *            <code> null </code> or empty
     */
    private void handleUpdateAllowedSites(IPSSite site)
    {
        String siteId = String.valueOf(site.getSiteId());
        String folderPath = PSAssetPathItemService.ASSET_ROOT;
        List<IPSItemSummary> rootAssetChildren = new ArrayList<IPSItemSummary>();
        try
        {
            rootAssetChildren = folderHelper.findItems(folderPath);
        }
        catch (Exception e)
        {
            // The folderPath comes from a constant so there's no way the method
            // throws the exception
            String msg = "The folderPath comes from a constant so there's no way the method throws the exception";
            log.error(msg, e);
        }

        Iterator<IPSItemSummary> iterator = rootAssetChildren.iterator();
        while (iterator.hasNext())
        {
            updateAllowedSitesProperty(iterator.next(), siteId);
        }
    }

    public PSSite save(PSSite site)
    {
        PSSiteCopyUtils.throwCopySiteMessageIfSameTargetName(site.getName(), "save",
                PSSiteCopyUtils.CAN_NOT_CREATE_SAME_COPIED_SITE_NAME);
        validateNewSite(site);
        return super.save(site);
    }

 
    public PSSite createSiteFromUrl(HttpServletRequest request, PSSite site) throws PSSiteImportException
    {
        validateNewSite(site);

        // Get the user agent
        String userAgent = request.getHeader("User-Agent");

        PSSiteImportCtx importContext = siteImportService.importSiteFromUrl(site, userAgent);
        return importContext.getSite();
    }

  
    public Long createSiteFromUrlAsync(HttpServletRequest request, PSSite site)
    {
        validateNewSite(site);

        // Get the user agent
        String userAgent = request.getHeader("User-Agent");

        // Create and setup import context
        PSSiteImportCtx importContext = new PSSiteImportCtx();
        importContext.setSite(site);
        importContext.setSiteUrl(site.getBaseUrl());
        importContext.setStatusMessagePrefix(IMPORT_STATUS_MESSAGE_PREFIX);
        importContext.setUserAgent(userAgent);

        // Execute import job
        long jobId = asyncJobService.startJob(SITE_IMPORT_JOB_BEAN, importContext);

        return new Long(jobId);
    }

  
    public PSSite getImportedSite(Long jobId)
    {
        notNull(jobId);
        PSAsyncJobStatus jobStatus = asyncJobService.getJobStatus(jobId);
        if (jobStatus == null)
            return null;

        if (jobStatus.getStatus().equals(IPSAsyncJob.COMPLETE_STATUS))
        {
            Object jobResult = asyncJobService.getJobResult(jobId);
            if (jobResult != null)
            {
                PSSiteImportCtx importContext = (PSSiteImportCtx) jobResult;
                return importContext.getSite();
            }
        }
        return null;
    }
    
    public void validateFolders(PSValidateCopyFoldersRequest req)
    {
        notNull(req, "req cannot be null");

        String srcAssetFolder = PSFolderPathUtils.concatPath(PSAssetPathItemService.ASSET_ROOT, req.getSrcFolder());
        String destAssetFolder = PSFolderPathUtils.concatPath(PSAssetPathItemService.ASSET_ROOT, req.getDestFolder());

        validateCopyFolders(StringUtils.removeEnd(srcAssetFolder, "/"), StringUtils.removeEnd(destAssetFolder, "/"));
    }

    public PSSite copy(PSSiteCopyRequest req)
    {
        boolean paused = false;
        notNull(req, "req cannot be null");
        Collection<String> createdLocalAssets = new ArrayList<String>();
        String siteName = req.getSrcSite();
        String newName = req.getCopySite();
        String assetFolder = req.getAssetFolder();
       
        log.info("PSSiteDataService.Copy: Site Copy Started - " + siteName + " to " + newName);
        PSSearchIndexEventQueue indexer = PSSearchIndexEventQueue.getInstance();
        
        // remember to resume in finally
        indexer.pause();
               
        
        PSSite copy = new PSSite();
        copy.setName(newName);
        PSSiteSummary orig = siteDao.findSummary(siteName);
        if (orig == null)
        {
            throw new RuntimeException("Unable to copy site, failed to find site with name: " + siteName);
        }
        String origFolderPath = orig.getFolderPath();

        boolean deleteSiteOnRollback = false;
        boolean deleteAssetsOnRollback = false;
        Map<String, String> assetMap = null;
        Map<String, String> tempMap = null;
        String copiedFolderPath = null;

        log.debug("Locking Sites " + orig.getName()  + " " + newName);
        PSSiteCopyUtils.startSiteCopy(orig.getName(), newName);
        try
        {
        	log.debug("Validating New Site for Site Copy....");
            validateNewSite(copy);

            log.debug("Starting Process Monitor Site Copy of ...." + newName);
            PSSiteCopyProcessMonitor.startSiteCopy(newName);

            assetMap = new HashMap<String, String>();

            if (assetFolder != null)
            {

                String srcAssetFolder = PSFolderPathUtils.concatPath(PSAssetPathItemService.ASSET_ROOT, assetFolder);
                
                String newPath = PSFolderPathUtils.concatPath(PSAssetPathItemService.ASSET_ROOT, newName);
                // validate folders
                validateCopyFolders(srcAssetFolder, newPath);

                // copy the assets
                PSSiteCopyProcessMonitor.copyingAssetsFolder();
                log.debug("Copying Asset Folder Source: " + srcAssetFolder + " to "  + newPath );
                deleteAssetsOnRollback = true;
                assetMap = itemService.copyFolder(srcAssetFolder, PSAssetPathItemService.ASSET_ROOT, newName);

            }

            // copy the site
            log.info("Copying site Content...");
            PSSiteCopyProcessMonitor.copyingSiteContent();
            String origId = orig.getId();
            deleteSiteOnRollback = true;
            
            log.debug("Create Site With Content origId:" + origId + " " + newName);
            try{
            	copy = siteDao.createSiteWithContent(origId, newName);
            	copiedFolderPath = copy.getFolderPath();
            }catch(Exception e){
            	log.error("An error occurred while copying Site Content for Site "+ origId, e);
            	throw(e);
            }

            // copy the templates
            try{
	            log.info("Copying Site Template...");
	            PSSiteCopyProcessMonitor.copyingTemplates();
	            tempMap = siteTemplateService.copyTemplates(origId, copy.getId());
            }catch(Exception e){
            	log.error("An error occurred while Copying Site Templates for Site " + origId,e);
            	throw(e);
            }

            // update the copied assets
            try{
 	            log.info("Updating Copied Site Assets...");
	            PSSiteCopyProcessMonitor.updatingAssets();
	            for (String assetId : assetMap.values())
	            {
	                updateSharedAssets(assetId, assetMap);
	                updateListAsset(assetId, origFolderPath, copiedFolderPath, assetFolder, newName, tempMap);
	                updateLinkedPages(assetId, orig, newName);
	               //this is a shared assets copy case, shared assets should be checked out and in
	                boolean checkoutIn = true;
	                updateLinkedAssets(assetId, assetMap, checkoutIn);
	            }
            }catch(Exception e){
            	log.error("An exception occurred while Updating Copied Site Assets for Site " + newName,e);
            	throw(e);
            }

            // update the template id's of the copied pages
            try{
	            log.info("Updating Copied Site Page Templates...");
	            PSSiteCopyProcessMonitor.updatingPageTemplateIds();
	            for (String tempId : tempMap.keySet())
	            {
	                String copiedTempId = tempMap.get(tempId);
	                updateTemplate(copiedTempId, orig, newName, assetMap);
	                updateListAssets(copiedFolderPath, origFolderPath, newName, assetFolder, copiedTempId, tempMap);
	
	                for (PSPage page : pageDao.findPagesBySiteAndTemplate(copiedFolderPath, tempId))
	                {
	                   
	                    // update the local content and shared assets on the page
                        Collection<String> newAssets = updatePage(page, copy, orig, assetMap);
	                    if (newAssets!=null)
	                        createdLocalAssets.addAll(newAssets);

	                    // checkout
                        String pageId = page.getId();
                        itemWorkflowService.forceCheckOut(pageId);
                        
	                    
	                    //TODO: Check and get latest revision id.  checkout may have changed.
	
	                    // Dealing with list assets
	                    updateListAssets(copiedFolderPath, origFolderPath, newName, assetFolder, pageId, tempMap);
	
	                    // set the template id
	                    page.setTemplateId(copiedTempId);
	
	                    // save and checkin
	                    page = pageDao.save(page);
	                    // revision should be ok for checkin as we are getting page from result of save.
	                    itemWorkflowService.checkIn(page.getId(),true);
	                }
	            }
	            
	            log.info("Updating any Blog template ids");
	            sectionService.updateSectionBlogTemplates(newName, tempMap);
	            
            }catch(Exception e){
            	log.error("Error updating Copied Site Templates for Site " + newName, e);
            	throw(e);
            }

            IPSSite copySite  = null;
            try{
            log.info("Copying Site Publishing Configuration...");
            PSSiteCopyProcessMonitor.copyingSiteConfiguration();

            copySite = publishingWs.findSite(newName);
            if (copySite.isSecure())
            {
                handleCopySiteConfiguration(orig.getName(), copySite.getName());
            }
            }catch(Exception e){
            	log.error("Error occurred while Copying Site Confiuration for " + newName,e);
            	throw (e);
            }
            
            // If option "Copy assets from the selected folder." was selected,
            // then add copy site to the allowed sites list of the copied root
            // level asset folder.
            if (assetFolder != null && copySite != null)
            {
            	try{
            		log.info("Updating Allowed Sites...");            		
            		String copySiteId = String.valueOf(copySite.getSiteId());
            		addNewSiteToAssetFolderAllowedSites(newName, copySiteId);
            	}catch(Exception e){
            		log.error("An error occurred in Copy Site Updating Allowed Sites for " + newName,e);
            		throw (e);
            	}
            }

        }
        catch (Exception e)
        {

            log.error("Error Copying site, attempting to roll back",e);
            if (assetMap != null)
            {
                if (createdLocalAssets!=null)
                {
                    for (String asset : createdLocalAssets)
                    {
                        // add to map to help cleanup.
                        assetMap.put(asset, asset);
                    }
                }
                
                log.info("Rolling back copied assets");
                itemService.rollBackCopiedFolder(assetMap,
                        PSFolderPathUtils.concatPath(PSAssetPathItemService.ASSET_ROOT, newName));
            }

            if (deleteSiteOnRollback)
            {
                log.info("Rolling back site");
                PSSiteSummary site = siteDao.findSummary(newName);
                if (site != null)
                {
                    try
                    {
                        siteDao.delete(newName);
                    }
                    catch (DeleteException e1)
                    {
                        log.error("Cannot delete all site resources for site " + siteName, e1);
                    }
                }
            }

            log.error("An error occurred copying site " + siteName,e);
            throw new RuntimeException("There was an error copying the site " + siteName
                    + ", review the logs for details", e);
        }
        finally
        {

            PSSiteCopyProcessMonitor.siteCopyCompleted();
            PSSiteCopyUtils.clearCopySite();
            // resume regardless of pre state, just in case another process has incorrectly left it paused.  e.g. fix
            // issue with site import.
            indexer.resume();
            log.info("PSSiteDataService.Copy: Site Copy Ended - " + siteName + " to " + newName);
        }

        return copy;
    }

    /**
     * Adds the copied site to the allowed sites list for newly created asset
     * folder.
     * 
     * @param copySiteName name of the copied site, never <code>null</code> or empty.
     * @param copySiteId numeric id of the copied site, converted to string, never
     *            <code>null</code> or empty.
     */
    private void addNewSiteToAssetFolderAllowedSites(String copySiteName, String copySiteId)
    {
        // Get the new asset folder that was created to modify allowed sites
        // property
        String newAssetFolderPath = PSFolderPathUtils.concatPath(PSAssetPathItemService.ASSET_ROOT, copySiteName);
        IPSGuid newAssetFolderGuid = contentWs.getIdByPath(newAssetFolderPath);
        PSFolder newAssetFolder = contentWs.loadFolder(newAssetFolderGuid, false);

        // Get new asset folder property allowed sites, and add copied site id
        // to the list.
        if (newAssetFolder.getProperty(IPSHtmlParameters.SYS_ALLOWEDSITES) != null)
        {
            ArrayList<String> listOfSites = new ArrayList<String>(Arrays.asList(newAssetFolder
                    .getProperty(IPSHtmlParameters.SYS_ALLOWEDSITES).getValue().split(",")));
            listOfSites.add(copySiteId);
            newAssetFolder
                    .setProperty(IPSHtmlParameters.SYS_ALLOWEDSITES, StringUtils.join(listOfSites.toArray(), ","));
        }
    }

    public PSValidationErrors validate(PSSite site)
    {
        return super.validate(site);
    }

 
    public PSSiteStatisticsSummary getSiteStatistics(String siteId)
    {
        notNull(siteId, "siteId cannot be null");

        PSSiteSummary site = siteDao.findSummary(siteId);
        if (site == null)
        {
            throw new RuntimeException("Unable to get site statistics, failed to find site: " + siteId);
        }

        PSSiteStatisticsSummary siteStatSummary = new PSSiteStatisticsSummary();
        siteStatSummary.setName(site.getName());
        siteStatSummary.setSiteId(site.getSiteId());

        // Set site stats
        siteStatSummary.setStatistics(getSiteStats(site));
        // Set import issues
        setImportIssues(siteStatSummary);

        // return the object
        return siteStatSummary;
    }

    
 
    public PSMapWrapper getSaaSSiteNames(boolean filterUsedSites)
    {
        Map<String, String> resultMap = new HashMap<String, String>();
        File saasDir = new File(PSServer.getRxDir().getAbsolutePath() + SAAS_SITE_CONFIG_FOLDER_PATH);
        if(!saasDir.exists() || !saasDir.isDirectory()){
            String msg = "Either saas configuration folder does not exist or it is not a folder. Path: " + saasDir.getAbsolutePath();
            log.error(msg);
            throw new DataServiceLoadException(msg);
        }
        FileFilter filter = new SuffixFileFilter(".json",IOCase.INSENSITIVE);
        File[] fileList = saasDir.listFiles(filter);
        List<String> siteNames = new ArrayList<String>();
        List<PSSiteSummary> sums = findAll();
        for (PSSiteSummary siteSum : sums)
        {
            siteNames.add(siteSum.getName());
        }
        ObjectMapper mapper = new ObjectMapper();
        
        for (File file : fileList)
        {
            PSSaasSiteConfig saasSiteConfig = loadSiteConfig(file.getName(), mapper);
            if(saasSiteConfig == null){
                //loadSiteConfig already logs the error we just continue here if it is null.
                continue;
            }
            PSSiteInfo siteInfo = saasSiteConfig.getSiteConfig().getSiteInfo();
            String siteName = siteInfo.getSiteName(); 
            boolean add = filterUsedSites?(siteNames.contains(siteName)?false:true):true;
            if(add && !resultMap.containsKey(siteName)){
                if(!isValidSiteName(siteName)){
                    String msg = "Skipping the configuration file (" + file.getName() + "), the siteName value is not valid. Accepted characters are alphanumeric and periods and dashes.";
                    log.error(msg);
                }
                else if(isReservedSiteName(siteName)){
                    String msg = "Skipping the configuration file (" + file.getName() + "), the siteName value is reserved word.";
                    log.error(msg);
                }
                else{
                    resultMap.put(siteName,file.getName());
                }
            }
        }
        PSMapWrapper mapWrapper = new PSMapWrapper();
        mapWrapper.setEntries(resultMap);
        return mapWrapper;
    }
    
    @Override
    public PSSaasSiteConfig getSaasSiteConfig(String siteName)
    {
        PSMapWrapper mapWrapper = getSaaSSiteNames(true);
        Map<String, String> map = mapWrapper.getEntries();
        if(!map.keySet().contains(siteName)){
            return null;
        }
        ObjectMapper mapper = new ObjectMapper();
        return loadSiteConfig(map.get(siteName), mapper);
    }

    /**
     * Helper method to load the supplied site config json file and convert the json into 
     * {@link PSSaasSiteConfig}.
     * @param fileName assumed not <code>null</code>
     * @return PSSaasSiteConfig may be <code>null</code> if there is an error. The error is logged.
     */
    private PSSaasSiteConfig loadSiteConfig(String fileName, ObjectMapper mapper)
    {
        PSSaasSiteConfig saasSiteConfig = null;
        File file = new File(PSServer.getRxDir().getAbsolutePath() + SAAS_SITE_CONFIG_FOLDER_PATH + File.separator + fileName);
        try
        {
            saasSiteConfig = mapper.readValue(file, PSSaasSiteConfig.class);
        }
        catch(JsonGenerationException  e){
            log.error("The site config file " + file.getName() + " is not a valid json file.", e);
        }
        catch(JsonMappingException  e){
            log.error("The site config file " + file.getName() + " does not map to the java class.", e);
        }
        catch (IOException e)
        {
            log.error("Exception occurred while reading saas site configuration file " + file.getName() + ".", e);
        }
        return saasSiteConfig;
    }
    
     
    /**
     * Helper method to build the site statistics for a given site.
     * 
     * @param site assumed not <code>null</code>.
     * @return PSSiteStatistics object.
     */
    private PSSiteStatistics getSiteStats(PSSiteSummary site)
    {
        PSSiteStatistics statistics = new PSSiteStatistics();
        try
        {
            PSSiteImportSummary impSummary = summaryService.find(site.getSiteId().intValue());
            if (impSummary != null){
                statistics.setPages(impSummary.getPages());
                statistics.setTemplates(impSummary.getTemplates());
                statistics.setLinksInternal(impSummary.getInternallinks());
                statistics.setBinary(impSummary.getFiles());
                statistics.setCss(impSummary.getStylesheets());
            }
        }
        catch (Exception e)
        {
            log.debug("Failed to find the statistics for site, please see log for details.", e);
        }
        return statistics;
    }

    /**
     * Helper method to get the page summaries for the supplied site.
     * 
     * @param site assumed not <code>null</code>
     * @return List of IPSItemSummary never null may be empty.
     * @throws Exception
     */
    private List<IPSItemSummary> getPageSummaries(PSSiteSummary site) throws Exception
    {
        List<IPSItemSummary> pages = new ArrayList<IPSItemSummary>();

        List<String> dirs = new ArrayList<String>();
        dirs.add(site.getFolderPath());

        boolean directoryFound = true;
        while (directoryFound)
        {
            List<String> newDirs = new ArrayList<String>();
            Iterator<String> iter = dirs.iterator();
            List<IPSItemSummary> items = folderHelper.findItems(iter.next());
            iter.remove();

            for (IPSItemSummary itemSummary : items)
            {
                if (itemSummary.isPage())
                {
                    pages.add(itemSummary);
                }
                else if (itemSummary.isFolder())
                {
                    String path = itemSummary.getFolderPaths().get(0) + "/" + itemSummary.getName();
                    String folderPath = PSPathUtils.getFolderPath(path);
                    newDirs.add(folderPath);
                }
            }
            dirs.addAll(newDirs);
            if (dirs.isEmpty())
                directoryFound = false;
        }
        return pages;
    }

    /**
     * Helper method to get the site import issues, will only get the first 100,
     * will enter a message if truncating.
     * 
     * @return List of issues never <code>null</code> may be empty.
     */
    private void setImportIssues(PSSiteStatisticsSummary sum)
    {
        List<PSSiteIssueSummary> issues = new ArrayList<PSSiteIssueSummary>();

        List<PSImportLogEntry> errors = importLogDao.findAll(String.valueOf(sum.getSiteId()),
                PSLogObjectType.SITE_ERROR.name());
        int count = 0;
        int max = MAX_ERROR_MESSAGES;
        boolean truncated = false;
        for (PSImportLogEntry error : errors)
        {
            if (count >= max)
            {
                truncated = true;
                break;
            }

            PSSiteIssueSummary issueSummary = new PSSiteIssueSummary();
            issueSummary.setRefUri(error.getDescription());
            issueSummary.setSuggestion(error.getLogData());
            issueSummary.setType(error.getCategory());

            issues.add(issueSummary);
            count++;
        }

        sum.setIssues(issues);
        if (truncated)
            sum.setAbridgedErrorMessage(ABRIDGED_ERROR_MESSAGE);
    }

    /**
     * Helper method to get the css files from the supplied themes.
     * 
     * @param themeNames assumed not <code>null</code>
     * @return css files count.
     * @throws FileNotFoundException
     */
    private long getCssFilesCount(Set<String> themeNames) throws FileNotFoundException
    {
        int cssCount = 0;

        for (String themeName : themeNames)
        {
            String path = "/themes/" + themeName + "/";

            List<String> dirs = new ArrayList<String>();
            dirs.add(path);

            boolean directoryFound = true;
            while (directoryFound)
            {
                List<String> newDirs = new ArrayList<String>();
                Iterator<String> iter = dirs.iterator();

                List<File> children = getFileSystemService().getChildren(iter.next());
                iter.remove();
                for (File item : children)
                {
                    if (item.isFile() && FilenameUtils.getExtension(item.getName()).equalsIgnoreCase("css"))
                    {
                        cssCount = cssCount + 1;
                    }
                    else if (item.isDirectory())
                    {
                        String dirPath = item.getPath();
                        int index = dirPath.indexOf("themes");
                        dirPath = StringUtils.substring(dirPath, index - 1);
                        newDirs.add(dirPath);
                    }
                }
                dirs.addAll(newDirs);
                if (dirs.isEmpty())
                    directoryFound = false;
            }
        }
        return cssCount;
    }

    /**
     * Helper method to get the internal links and resource assets for the
     * supplied pages and templates.
     * 
     * @param pages assumed not <code>null</code>.
     * @param templates assumed not <code>null</code>.
     * @return PSPair<Long, Long> the first element is count of links and second
     *         element is count of file assets
     */
    private PSPair<Long, Long> getLinksAndResourcesCount(List<IPSItemSummary> pages, List<PSTemplateSummary> templates)
    {
        List<String> resourceAssets = new ArrayList<String>();
        List<String> pageIds = new ArrayList<String>();
        Set<String> files = new HashSet<String>();

        long linksCount = 0;
        long binaryCount = 0;
        PSPair<Long, Long> pair = new PSPair<Long, Long>();
        pair.setFirst(linksCount);
        pair.setSecond(binaryCount);
        return pair;
    }

    @Override
    public PSSiteSummary findByPath(String path) throws DataServiceNotFoundException
    {
        PSParameterValidationUtils.rejectIfNull("findByPath", "path", path);
        PSSiteSummary sum = siteDao.findByPath(path);
        if (sum != null)
        {
            return sum;
        }

        throw new DataServiceNotFoundException("Site cannot be found for path: " + path);

    }

    @Override
    public void createPublishingItemsForPubServer(IPSSite site, PSPubServer pubServer, boolean isDefaultServer)
    {
        sitePublishDao.createPublishingItemsForPubServer(site, pubServer, isDefaultServer);
    }

    @Override
    public void setPublishServerAsDefault(IPSSite site, PSPubServer pubServer)
    {
        sitePublishDao.setPublishServerAsDefault(site, pubServer);
    }

    @Override
    public String getDefaultPublishingRoot(IPSSite site, String publishServerType, String deliveryRootPath)
    {
        return sitePublishDao.getPublishingDeliveryRoot(site.getName(), publishServerType, deliveryRootPath);
    }

    @Override
    public String getBasePublishingRoot(String serverRootPath, String siteName)
    {
        return sitePublishDao.getPublishingBase(serverRootPath, siteName);
    }

    @Override
    public void deletePublishingItemsByPubServer(PSPubServer pubServer)
    {
        sitePublishDao.deletePublishingItemsByPubServer(pubServer);
    }

    @Override
    public void updateServerEditions(IPSSite site, PSPubServer oldServer, PSPubServer pubServer, boolean isDefaultServer)
    {
        sitePublishDao.updateServerEditions(site, oldServer, pubServer, isDefaultServer);
    }

    /**
     * Validates the specified new site.
     * 
     * @param site the site in question, not <code>null</code>.
     */
    private void validateNewSite(PSSite site)
    {
        PSValidationErrorsBuilder builder = validateParameters("save").rejectIfNull("site", site).throwIfInvalid();

        // is the site name valid?
        if (!isValidSiteName(site.getName()))
        {
            builder.reject("site.invalidName", "Cannot create site '" + site.getName()
                    + "' because that name is an invalid site name");
            builder.throwIfInvalid();
        }

        if (isReservedSiteName(site.getName()))
        {
            builder.reject("site.reservedName", "Cannot create site 'web' because that name is a reserved site name");
            builder.throwIfInvalid();
        }

        // does the user have permission
        if (!isCurrentUserAdmin())
        {
            builder.reject("site.saveNotAuthorized", "You are not authorized to create a site");
            builder.throwIfInvalid();
        }

        // is the site name unique?
        IPSSite existingSite = siteMgr.findSite(site.getName());
        if (existingSite != null)
        {
            builder.reject("site.exists", "Cannot create a site with name \"" + site.getName()
                    + "\" because a site named \"" + site.getName() + "\" already exists.");
            builder.throwIfInvalid();
        }
    }
    
    /**
     * Helper method to check whether supplied site name consists of allowed characters (alphanumeric characters
     * or - and .) if yes returns <code>true</code> otherwise <code>false</code>, returns <code>false</code> in case of 
     * just periods and dashes.
     * @param siteName must not be null
     * @return <code>true</code> if valid otherwise <code>false</code>.
     */
    private boolean isValidSiteName(String siteName)
    {
        boolean isValid = true;
        siteName = StringUtils.defaultString(siteName);
        String stripped = siteName.replaceAll("[^a-zA-Z0-9\\-\\.]", "");
        if(!stripped.equals(siteName))
            isValid = false;
        else if(siteName.replaceAll("[\\-\\.]", "").length()==0)
            isValid = false;
        return isValid;
    }

    private boolean isReservedSiteName(String siteName)
    {
        return reservedSiteNames.contains(StringUtils.lowerCase(siteName));
    }
    /**
     * Validates the specified folder. A folder is not valid if it exists and is
     * not empty.
     * 
     * @param folder the path of the folder in question, not <code>null</code>.
     */
    private void validateNonEmptyFolder(String folder) throws PSValidationException
    {
        PSValidationErrorsBuilder builder = validateParameters("copy").rejectIfNull("folder", folder).throwIfInvalid();

        try
        {
            List<IPSItemSummary> items = folderHelper.findItems(folder);
            if (!items.isEmpty())
            {
                builder.reject("siteCopy.folder.exists",
                        "Unable to copy assets to existing folder '" + PSPathUtils.getFinderPath(folder)
                                + "'.  Please rename the folder or choose a different " + "name for your site.");
                builder.throwIfInvalid();
            }
        }
        catch (Exception e)
        {
            if (e instanceof PSValidationException)
            {
                throw (PSValidationException) e;
            }
        }
    }

    /**
     * Validates the specified folder. A folder is not valid if it doesn't
     * exist.
     * 
     * @param folder the path of the folder in question, not <code>null</code>.
     */
    private void validateFolder(String folder)
    {
        PSValidationErrorsBuilder builder = validateParameters("copy").rejectIfNull("folder", folder).throwIfInvalid();

        IPSItemSummary item = null;
        try
        {
            item = folderHelper.findFolder(folder);
        }
        catch (Exception e)
        {
            // folder doesn't exist
        }

        if (item == null)
        {
            builder.reject("siteCopy.folder.missing", "The selected folder '" + PSPathUtils.getFinderPath(folder)
                    + "' no longer exists.  Please choose a different folder.");
            builder.throwIfInvalid();
        }
    }

    /**
     * Determines if the current user has Administrator privileges.
     * 
     * @return <code>true</code> if the user is an Admin, <code>false</code>
     *         otherwise.
     */
    private boolean isCurrentUserAdmin()
    {
        boolean isAdmin = false;
        List<String> currentRoles = userService.getCurrentUser().getRoles();
        for (String role : currentRoles)
        {
            if (role.equals("Admin"))
            {
                isAdmin = true;
                break;
            }
        }
        return isAdmin;
    }

/**
     * Updates all local auto list assets for the specified item.  
     * See {@link #updateListAsset(String, String, String, String, String, Map).
     * 
     * @param copiedFolderPath folder path of the copied site.
     * @param originalFolderPath folder path of the original site.
     * @param newName of the copied site.
     * @param assetFolder the path of the original asset folder relative to the Assets Library.
     * @param id of the page/template.
     * @param tempMap map of original to copied template id's.
     */
    private void updateListAssets(String copiedFolderPath, String originalFolderPath, String newName,
            String assetFolder, String id, Map<String, String> tempMap)
    {
        // get all the local content items related to this item
        Set<String> contentIds = widgetAssetRelationshipService.getLocalAssets(id);

        for (String contentId : contentIds)
        {
            updateListAsset(contentId, originalFolderPath, copiedFolderPath, assetFolder, newName, tempMap);
        }
    }

    /**
     * Updates the given site-specific auto list asset by replacing the
     * specified path value with the specified replacement path value. Template
     * id's will also be updated according to the specified map.
     * 
     * @param asset
     * @param replacementPath the replacement path value.
     * @param originalPath the source path value.
     * @param tempMap map of original to copied template id's.
     */
    private void updateSiteListAsset(PSAsset asset, String replacementPath, String originalPath,
            Map<String, String> tempMap)
    {
        String type = asset.getType();
        if (siteListAssetMap.containsKey(type))
        {
            log.debug("Updating asset: " + asset.getName());
            Map<String, String> replaceMappings = new HashMap<String, String>();
            replaceMappings.put(originalPath, replacementPath);
            replaceMappings.putAll(tempMap);

            updateListAsset(asset, replaceMappings, siteListAssetMap.get(type));
        }
    }

    /**
     * Updates the given asset-specific auto list asset by replacing the
     * specified path value with the specified replacement path value.
     * 
     * @param asset
     * @param replacementPath the replacement path value.
     * @param originalPath the source path value.
     */
    private void updateAssetListAsset(PSAsset asset, String replacementPath, String originalPath)
    {
        String type = asset.getType();
        if (assetListAssetMap.containsKey(type))
        {
            Map<String, String> replaceMappings = new HashMap<String, String>();
            replaceMappings.put(originalPath, replacementPath);

            updateListAsset(asset, replaceMappings, assetListAssetMap.get(type));
        }
    }

    /**
     * Updates the given auto list asset. For each field specified in the list
     * of replacement fields, the string value of the field will be modified
     * according to the values specified in the replacement mappings.
     * 
     * @param asset
     * @param replaceMappings map of source to replacement string.
     * @param replaceFields list of asset field names to be updated.
     */
    private void updateListAsset(PSAsset asset, Map<String, String> replaceMappings, List<String> replaceFields)
    {
        if (replaceFields != null)
        {
            Map<String, Object> fields = asset.getFields();
            boolean modified = false;

            for (String replaceField : replaceFields)
            {
                String replaceVal = (String) fields.get(replaceField);
                if (replaceVal != null)
                {
                    for (String original : replaceMappings.keySet())
                    {
                        String replacement = replaceMappings.get(original);
                        if (replaceVal.contains(original + '/'))
                        {
                            replaceVal = replaceVal.replaceFirst(original + '/', replacement + '/');
                        }
                        else if (replaceVal.contains(original + '%'))
                        {
                            replaceVal = replaceVal.replaceFirst(original + '%', replacement + '%');
                        }
                        else
                        {
                            replaceVal = replaceVal.replaceFirst(original, replacement);
                        }
                    }

                    if (!replaceVal.equals((String) fields.get(replaceField)))
                    {
                        fields.put(replaceField, replaceVal);
                        modified = true;
                    }
                }
            }

            if (modified)
            {
                String assetId = asset.getId();

                itemWorkflowService.checkOut(assetId);
                assetDao.save(asset);
                itemWorkflowService.checkIn(assetId);
            }
        }
    }

    /**
     * Updates the specified auto list asset such that it points to the copied
     * site, asset folder, and templates.
     * 
     * @param assetId
     * @param origSitePath path of the original site.
     * @param copiedSitePath path of the copied site.
     * @param origAssetPath path of the original asset folder relative to the
     *            Assets Library.
     * @param copiedAssetPath path of the copied asset folder relative to the
     *            Assets Library.
     * @param tempMap map of original to copied template id's.
     */
    private void updateListAsset(String assetId, String origSitePath, String copiedSitePath, String origAssetPath,
            String copiedAssetPath, Map<String, String> tempMap)
    {
        PSAsset asset = assetDao.find(assetId);
        String type = asset.getType();
        if (siteListAssetMap.containsKey(type))
        {
            updateSiteListAsset(asset, copiedSitePath, origSitePath, tempMap);
        }

        if (origAssetPath != null && assetListAssetMap.containsKey(type))
        {
            String origAssetFolder = PSFolderPathUtils.concatPath(PSPathUtils.ASSETS_FINDER_ROOT, origAssetPath);
            String copyAssetFolder = PSFolderPathUtils.concatPath(PSPathUtils.ASSETS_FINDER_ROOT, copiedAssetPath);
            updateAssetListAsset(asset, copyAssetFolder, StringUtils.removeEnd(origAssetFolder, "/"));
        }
    }

    /**
     * Updates all local content and shared assets for the specified page. This
     * includes finding the original page from which the page was copied,
     * removing the current assets, then copying all assets from the original.
     * All local inline links to copied pages/assets are also updated
     * accordingly for the page.
     * 
     * @param page
     * @param copySite the copied site under which the page exists
     * @param origSite the original site from which the page was copied
     */
    private Collection<String> updatePage(PSPage page, PSSiteSummary copySite, PSSiteSummary origSite, Map<String, String> assetMap)
    {
        String pageFolderPath = page.getFolderPath();
        String pagePath = PSFolderPathUtils.concatPath(pageFolderPath, page.getName());
        String copySiteName = copySite.getName();
        String origSiteName = origSite.getName();

        String origPagePath = pagePath.replaceFirst(copySiteName, origSiteName);
        PSPage origPage = pageDao.findPageByPath(origPagePath);
       
        Collection<String> assetIds = null;
        if (origPage != null)
        {
            String pageId = page.getId();
            Set<String> localIds = widgetAssetRelationshipService.getLocalAssets(origPage.getId());
            if (!localIds.isEmpty())
            {
                // update the copied page's assets
                widgetAssetRelationshipService.removeAssetWidgetRelationships(pageId, Collections.EMPTY_LIST);
                assetIds = widgetAssetRelationshipService.copyAssetWidgetRelationships(origPage.getId(), pageId);

                updateLinks(pageId, origSite, copySiteName, assetMap);
            }

            updateSharedAssets(pageId, assetMap);
            if (assetIds!= null && assetIds.size()>0)
                updateLinksForLocalAssets(assetIds, origSite, copySite, assetMap);

            // Copy the workflow id from the original page
            page.setWorkflowId(origPage.getWorkflowId());
        }
        return assetIds;
    }

    private void updateLinksForLocalAssets(Collection<String> localIds, PSSiteSummary origSite, PSSiteSummary copySite,
            Map<String, String> assetMap)
    {
        Set<String> assetIds = new HashSet<String>();
        if (localIds != null)
            assetIds.addAll(localIds);

        assetIds.addAll(assetMap.values());
        if (assetIds.isEmpty())
            return;

        getManagedLinkService().updateCopyAssetsLinks(assetIds, origSite.getFolderPath(), copySite.getFolderPath(),
                assetMap);
    }

    private IPSManagedLinkService getManagedLinkService()
    {
        if (managedLinkService != null)
            return managedLinkService;

        managedLinkService = (IPSManagedLinkService) getWebApplicationContext().getBean("managedLinkService");
        return managedLinkService;
    }

    private IPSFileSystemService getFileSystemService()
    {
        if (fileSystemService != null)
            return fileSystemService;

        fileSystemService = (IPSFileSystemService) getWebApplicationContext().getBean("webResourcesService");
        return fileSystemService;
    }

    private IPSTemplateService getTemplateService()
    {
        if (templateService != null)
            return templateService;

        templateService = (IPSTemplateService) getWebApplicationContext().getBean("sys_templateService");
        return templateService;
    }

    /**
     * Updates all local asset links and shared assets for the specified
     * template.
     * 
     * @param id of the template.
     * @param origSite the original site which was copied.
     * @param copySiteName the name of the copied site.
     * @param assetMap map of original asset id (string) to copied asset id.
     */
    private void updateTemplate(String id, PSSiteSummary origSite, String copySiteName, Map<String, String> assetMap)
    {
        updateLinks(id, origSite, copySiteName, assetMap);
        updateSharedAssets(id, assetMap);
    }

    /**
     * Updates links to pages and assets contained in all local assets for the
     * specified item.
     * 
     * @param id of the page/template.
     * @param origSite the original site which was copied.
     * @param copySiteName the name of the copied site.
     * @param assetMap map of original asset id (string) to copied asset id.
     */
    private void updateLinks(String id, PSSiteSummary origSite, String copySiteName, Map<String, String> assetMap)
    {
        for (String assetId : widgetAssetRelationshipService.getLocalAssets(id))
        {
            updateLinkedPages(assetId, origSite, copySiteName);
            updateLinkedAssets(assetId, assetMap);
        }
    }

    /**
     * Updates all links to pages contained in the specified asset.
     * 
     * @param assetId
     * @param origSite the original site which was copied.
     * @param copySiteName the name of the copied site.
     */
    private void updateLinkedPages(String assetId, PSSiteSummary origSite, String copySiteName)
    {
        Set<String> linkedPageIds = widgetAssetRelationshipService.getLinkedPages(assetId);
        for (String linkedPageId : linkedPageIds)
        {
            PSPage linkedPage = pageDao.find(linkedPageId);
            if (linkedPage != null)
            {
                String linkedPagePath = PSFolderPathUtils.concatPath(linkedPage.getFolderPath(), linkedPage.getName());
                PSSiteSummary linkedSite = siteDao.findByPath(linkedPagePath);
                if (linkedSite != null && linkedSite.getId().equals(origSite.getId()))
                {
                    // page is under the original site, need to update the link
                    // to point to copied page
                    String copyPagePath = linkedPagePath.replaceFirst(origSite.getName(), copySiteName);
                    PSPage copyPage = pageDao.findPageByPath(copyPagePath);
                    if (copyPage != null)
                    {
                        	widgetAssetRelationshipService.updateSharedRelationshipDependent(assetId, linkedPageId,
							        copyPage.getId());
						
                    }
                }
            }
        }
    }
/**
 * Default case of updating local linked assets. No need to checkout
 * @param assetId
 * @param assetMap
 */
    private void updateLinkedAssets(String assetId, Map<String, String> assetMap)
    {
    	updateLinkedAssets(assetId,assetMap, false);
    }
    
    /**
     * Updates all links to assets contained in the specified asset.
     * 
     * @param assetId
     * @param checkInOut specify whether or not an item should be checkedout before updating it
     * @param assetMap map of original asset id (string) to copied asset id.
     */
    private void updateLinkedAssets(String assetId, Map<String, String> assetMap, boolean checkInOut)
    {
        updateAssets(assetId, widgetAssetRelationshipService.getLinkedAssetsForAsset(assetId), assetMap, checkInOut);
    }

    /**
     * Updates all shared assets for the specified item. 
     * 
     * @param id of the page/template.
     * @param assetMap map of original asset id (string) to copied asset id.
     */
    private void updateSharedAssets(String id, Map<String, String> assetMap)
    {
    	//Shared assets should be checked out
        
    	boolean checkInOut = true;
    	updateAssets(id, widgetAssetRelationshipService.getSharedAssets(id), assetMap, checkInOut);
     }

    private void updateAssets(String id, Set<String> assetIds, Map<String, String> assetMap)
    {
    	updateAssets(id, assetIds, assetMap, false);
    }
    /**
     * Updates all assets for the specified item.
     * 
     * @param id of the page/template.
     * @param assetIds set of asset id's which will be updated.
     * @param assetMap map of original asset id (string) to copied asset id.
     * @param checkInOut specifies whether or not an asset should be checked out before updating.
     * 
     
     */
    private void updateAssets(String id, Set<String> assetIds, Map<String, String> assetMap, boolean checkInOut)
    {
        if (assetMap.isEmpty())
        {
            return;
        }

        for (String assetId : assetIds)
        {
        	
        	idMapper.getLocator(assetId);
        	
            if (assetMap.containsKey(assetId))
            {
                // re-point to the copied asset
                widgetAssetRelationshipService.updateSharedRelationshipDependent(id, assetId, assetMap.get(assetId), checkInOut);
            }
        }
    }

    /**
     * Validates the specified source and destination folder paths for copy.
     * 
     * @param src the source folder path.
     * @param dest the destination folder path.
     */
    private void validateCopyFolders(String src, String dest)
    {
        // validate source folder
        validateFolder(src);

        // validate destination folder
        validateNonEmptyFolder(dest);
    }

    /**
     * Convenience method that calls
     * {@link PSSiteConfigUtils#removeSiteConfigurationAndTouchedFile} and
     * handles the possible IOException.
     * 
     * @param sitename the name of the site
     */
    private void handleRemoveSiteAndTouchedConfiguration(String sitename)
    {
        try
        {
            removeSiteConfigurationAndTouchedFile(sitename);
        }
        catch (IOException e)
        {
            String errorMsg = "Failed to remove the site configuration folder/files for site: " + sitename;
            log.error(errorMsg, e);
            throw new DataServiceSaveException(errorMsg, e);
        }
    }

    /**
     * Convenience method that calls
     * {@link PSSiteConfigUtils#copySecureSiteConfiguration(String, String)} and
     * handles the possible IOException.
     * 
     * @param sourceName the name of the source site
     * @param destinationName the name of the destination site
     */
    private void handleCopySiteConfiguration(String sourceName, String destinationName)
    {
        try
        {
            copySecureSiteConfiguration(sourceName, destinationName);
        }
        catch (IOException e)
        {
            String errorMsg = "Failed copying the secure configuration from '" + sourceName + "' to '"
                    + destinationName + "'";
            log.error(errorMsg, e);
            throw new DataServiceSaveException(errorMsg, e);
        }
    }

    /**
     * Convenience method that calls
     * {@link PSSiteConfigUtils#removeTouchedFile(String)} and handles the
     * possible IOException.
     * 
     * @param sitename the name of the site
     */
    private void handleRemoveTouchedFile(String sitename)
    {
        try
        {
            removeTouchedFile(sitename);
        }
        catch (IOException e)
        {
            String errorMsg = "Failed removing the tch file for site '" + sitename + "'";
            log.error(errorMsg, e);
            throw new DataServiceSaveException(errorMsg, e);
        }
    }

    /**
     * Handles the update of the configuration files according to the site
     * properties.
     * 
     * @param site the modifiable site object representing the original site
     *            (without applying the update yet)
     * @param props a {@link PSSiteProperties} object, representing the data
     *            coming from the screen
     */
    private void updateSiteConfigurationFiles(IPSSite site, PSSiteProperties props)
    {
        try
        {
            updateSiteConfiguration(site, props);
        }
        catch (IOException e)
        {
            String errorMsg = "Failed updating the configuration for site '" + site.getName() + "'";
            log.error(errorMsg, e);
            throw new DataServiceSaveException(errorMsg, e);
        }
    }

    private List<String> getAssetsByItem(String itemId)
    {
        List<String> resourceAssets = new ArrayList<String>();

        // get all the local content items related to this item
        Set<String> localIds = widgetAssetRelationshipService.getLocalAssets(itemId);
        resourceAssets.addAll(localIds);

        // get all the shared content items related to this item
        Set<String> sharedIds = widgetAssetRelationshipService.getSharedAssets(itemId);
        resourceAssets.addAll(sharedIds);

        return resourceAssets;
    }
    
    private IPSPubServerService getPubServerService()
    {
        if(pubserverService == null)
            pubserverService = (IPSPubServerService)PSSpringWebApplicationContextUtils.getWebApplicationContext().getBean("pubServerService", IPSPubServerService.class);
        return pubserverService;
    }
    
    /**
     * Map whose key is the type of site auto list asset and value is a list of
     * field names which should be updated when a site is copied.
     */
    private static Map<String, List<String>> siteListAssetMap = new HashMap<String, List<String>>();

    /**
     * Map whose key is the type of asset auto list asset and value is a list of
     * field names which should be updated when a site is copied.
     */
    private static Map<String, List<String>> assetListAssetMap = new HashMap<String, List<String>>();

    static
    {
        List<String> pal = new ArrayList<String>();
        pal.add("query");
        pal.add("site_path");
        pal.add("page_templates_list");
        siteListAssetMap.put("percPageAutoList", pal);

        List<String> cat = new ArrayList<String>();
        cat.add("query");
        cat.add("site_path");
        cat.add("page_templates_list");
        siteListAssetMap.put("percCategoryList", cat);

        List<String> tag = new ArrayList<String>();
        tag.add("query");
        tag.add("site_path");
        tag.add("page_templates_list");
        siteListAssetMap.put("percTagList", tag);

        List<String> arc = new ArrayList<String>();
        arc.add("query_string");
        arc.add("query_site_path");
        arc.add("query_template_list");
        siteListAssetMap.put("percArchiveList", arc);

        List<String> ial = new ArrayList<String>();
        ial.add("query");
        ial.add("asset_library_path");
        assetListAssetMap.put("percImageAutoList", ial);
    }
    /**
     * All lower case reserved names for the site name. Make sure to lower case before comparing.
     */
    private static Set<String> reservedSiteNames = new HashSet<String>();
    static
    {
        reservedSiteNames.add("web");
    }
	@Override
	public PSPubInfo getS3PubServerInfo(long siteId) {
		PSPubInfo ret = null;
		try
          {
             ret =  getPubServerService().getS3PubInfo(new PSGuid(PSTypeEnum.SITE, siteId));
          }
          catch (Exception e)
          {
              log.error("Error getting S3 Publishing Server information for Site:" + siteId, e);
          }
		return ret;
	}
}
