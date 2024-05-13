/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.percussion.sitemanage.service.impl;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.percussion.assetmanagement.dao.IPSAssetDao;
import com.percussion.assetmanagement.data.PSAsset;
import com.percussion.assetmanagement.service.IPSWidgetAssetRelationshipService;
import com.percussion.cms.objectstore.PSFolder;
import com.percussion.designmanagement.service.IPSFileSystemService;
import com.percussion.error.PSExceptionUtils;
import com.percussion.fastforward.managednav.IPSManagedNavService;
import com.percussion.foldermanagement.service.IPSFolderService;
import com.percussion.itemmanagement.service.IPSItemService;
import com.percussion.itemmanagement.service.IPSItemWorkflowService;
import com.percussion.linkmanagement.service.IPSManagedLinkService;
import com.percussion.monitor.process.PSSiteCopyProcessMonitor;
import com.percussion.pagemanagement.dao.IPSPageDao;
import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.data.PSTemplateSummary;
import com.percussion.pagemanagement.service.IPSPageService;
import com.percussion.pagemanagement.service.IPSTemplateService;
import com.percussion.pagemanagement.service.impl.PSPageService;
import com.percussion.pathmanagement.data.PSDeleteFolderCriteria;
import com.percussion.pathmanagement.data.PSFolderPermission;
import com.percussion.pathmanagement.data.PSGenerateSiteMapOptions;
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
import com.percussion.services.error.PSNotFoundException;
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
import com.percussion.share.dao.IPSGenericDao;
import com.percussion.share.dao.IPSGenericDao.DeleteException;
import com.percussion.share.dao.PSFolderPathUtils;
import com.percussion.share.data.IPSItemSummary;
import com.percussion.share.data.PSEnumVals;
import com.percussion.share.data.PSMapWrapper;
import com.percussion.share.service.IPSIdMapper;
import com.percussion.share.service.PSAbstractDataService;
import com.percussion.share.service.PSSiteCopyUtils;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.share.service.exception.PSParameterValidationUtils;
import com.percussion.share.service.exception.PSParametersValidationException;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.share.spring.PSSpringWebApplicationContextUtils;
import com.percussion.share.validation.PSValidationErrors;
import com.percussion.share.validation.PSValidationErrorsBuilder;
import com.percussion.sitemanage.dao.IPSSitePublishDao;
import com.percussion.sitemanage.dao.IPSiteDao;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.percussion.share.dao.PSFolderPermissionUtils.getFolderPermission;
import static com.percussion.share.service.exception.PSParameterValidationUtils.validateParameters;
import static com.percussion.share.spring.PSSpringWebApplicationContextUtils.getWebApplicationContext;
import static com.percussion.utils.service.impl.PSSiteConfigUtils.copySecureSiteConfiguration;
import static com.percussion.utils.service.impl.PSSiteConfigUtils.removeSiteConfigurationAndTouchedFile;
import static com.percussion.utils.service.impl.PSSiteConfigUtils.removeTouchedFile;
import static com.percussion.utils.service.impl.PSSiteConfigUtils.updateSiteConfiguration;
import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;


@Component("siteDataService")
@PSSiteManageBean
@Lazy
 public class PSSiteDataService extends PSAbstractDataService<PSSite, PSSiteSummary, String>
        implements
            IPSSiteDataService
{
 	private static final Logger log = LogManager.getLogger(PSSiteDataService.class);

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

    private IPSSitePublishDao sitePublishDao;

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
            IPSContentWs contentWs, IPSUserService userService, IPSSitePublishDao sitePublishDao,
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
    public PSSite load(String id) throws DataServiceLoadException, DataServiceNotFoundException, PSValidationException {
        return super.load(id);
    }

    public PSSiteSummary find(String id) throws com.percussion.share.service.IPSDataService.DataServiceLoadException, PSValidationException, IPSGenericDao.LoadException {
        return find(id, false);
    }

    public PSSiteSummary findByName(String name) throws DataServiceLoadException, PSValidationException {
        PSParameterValidationUtils.rejectIfBlank("findByName", "name", name);

        PSSiteSummary sum = siteDao.findByName(name);

        if (sum == null)
            throw new DataServiceLoadException("Could not find site for name: " + name);

        return sum;
    }

    public PSSiteSummary find(String id, boolean includePubInfo) throws com.percussion.share.service.IPSDataService.DataServiceLoadException, PSValidationException, IPSGenericDao.LoadException {
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

    
    public PSSiteProperties getSiteProperties(String siteName) throws IPSSiteSectionService.PSSiteSectionException, PSValidationException, PSNotFoundException {
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
        	log.error("Can't find a root NavOn for site {} in Root Folder {} " ,
                    siteName, site.getFolderRoot());
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
        props.setDefaultFileExtention(site.getDefaultFileExtension());
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
        props.setGenerateSiteMap(site.isGenerateSitemap());

        ObjectMapper mapper = new ObjectMapper();
        String jsonString = site.getGenerateSiteMapOptions();
        PSGenerateSiteMapOptions psGenerateSiteMapOptions = null;
        try {
            psGenerateSiteMapOptions = mapper.readValue(jsonString, PSGenerateSiteMapOptions.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        props.setGenerateSiteMapOptions(psGenerateSiteMapOptions);
    }

    public PSSitePublishProperties getSitePublishProperties(String siteName) throws PSValidationException, PSNotFoundException {
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
    private PSFolderPermission getSiteRootPermission(IPSSite site) throws PSValidationException {
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

    public PSSiteProperties updateSiteProperties(PSSiteProperties props) throws PSDataServiceException, PSNotFoundException {
        notNull(props, "Properties cannot be null");
        IPSSite site = siteMgr.loadSiteModifiable(idMapper.getGuid(props.getId()));
        String newSiteName = props.getName();
        String oldSiteName = site.getName();
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

        updateThumbnailCache(oldSiteName, newSiteName);

        return props;
    }

    public static final String PAGE_IMAGE_CACHE_DIR= File.separator + "rx_resources" + File.separator + "images" + File.separator + "TemplateImages";

    private void updateThumbnailCache(String oldSiteName, String newSiteName) {
        log.info("Updating Page and Template thumbnail cache for site: {} to use new site name: {}...",
                oldSiteName, newSiteName);

        File sourceCacheDir = new File(PSServer.getRxDir().getAbsolutePath() +  PAGE_IMAGE_CACHE_DIR + File.separator + oldSiteName );
        File destCacheDir = new File(PSServer.getRxDir().getAbsolutePath() + PAGE_IMAGE_CACHE_DIR + File.separator + newSiteName );
        if(sourceCacheDir.renameTo(destCacheDir))
            log.info("Page and Template image cache folder moved to to: {}", destCacheDir.getAbsolutePath());
        else
            log.error("Unable to automatically move: {} to {}.  An adminstrator may need to stop the service and rename / move the folder to resolve the issue.",
                    sourceCacheDir.getAbsolutePath(),
                    destCacheDir.getAbsolutePath());
    }

    private void updateSiteFromProps(IPSSite site, PSSiteProperties props) {
        site.setLoginPage(props.getLoginPage());
        site.setRegistrationPage(props.getRegistrationPage());
        site.setRegistrationConfirmationPage(props.getRegistrationConfirmationPage());
        site.setResetPage(props.getResetPage());
        site.setResetRequestPasswordPage(props.getResetRequestPasswordPage());
        site.setSecure(props.isSecure());
        site.setDefaultFileExtension(props.getDefaultFileExtention());
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
        site.setGenerateSitemap(props.isGenerateSiteMap());
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = null;
        try {
            json = ow.writeValueAsString(props.getGenerateSiteMapOptions());

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        site.setGenerateSiteMapOptions(json);
        site.setBaseUrl(props.getSiteProtocol() + "://" + site.getName());
    }

    private void updatePSRecentEntries(IPSSite site, PSSiteProperties props) {
        recentService.updateSiteNameRecent(site.getName(), props.getName());
    }

    /**
     * Updates the site's auto  list widgets when a site is renamed.
     * @param site the original site information.
     * @param props the updated site information.
     */
    private void updateAutoListAssetsForSite(IPSSite site, PSSiteProperties props) {
        log.debug("Updating the auto list entries for the site: {} ", site.getName());
        if (!site.getName().equals(props.getName()))
        {
            Collection<PSAsset> autoListAssets = findAutoListWidgets();

            for (PSAsset asset : autoListAssets) {
                updateSiteListAsset(asset, props.getName(), site.getName(), new HashMap<>());
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
                log.error("Unable to find assets with type: {} Error: {}",  contentTypeName,
                        PSExceptionUtils.getMessageForLog(e));
                log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            }
        }

        return assets;
    }


    public PSSitePublishProperties updateSitePublishProperties(PSSitePublishProperties publishProps) throws DataServiceSaveException, PSNotFoundException {
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
     * {@link PSPubServer#setSiteRenamed(boolean)} flag.
     *
     * @param site the site object
     * @param props
     */
    private void updatePubServers(IPSSite site, PSSiteProperties props) {
        log.info("Updating publishing server for site: {}" , site.getName());
            try {
                PSPubServer pubServer = getPubServerService().getDefaultPubServer(site
                        .getGUID());

                if (!site.getName().equals(props.getName())) {
                    pubServer.setSiteRenamed(true);
                }

                IPSPubServerDao pubServerDao = PSPubServerDaoLocator.getPubServerManager();
                pubServerDao.savePubServer(pubServer);
            } catch (Exception e) {
                log.error("Error updating PSPubServer flag setSiteRenamed while renaming site: {}. Error: {}",
                        site.getName(),
                        PSExceptionUtils.getMessageForLog(e));
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
    public String isSiteBeingImported(String sitename) throws PSDataServiceException {
        PSSite site = siteDao.find(sitename);
        List<Integer> importingPages = null;
        if (site!=null)
        {
            importingPages = pageImportQueue.getImportingPageIds(site.getSiteId());
        }
        return (site!=null && !importingPages.isEmpty()) ? Boolean.TRUE.toString() : Boolean.FALSE.toString();
    }

    /**
     * Validates the new properties against the original / source site.
     * 
     * @param site the source site, assumed not <code>null</code>.
     * @param props the new site properties, assumed not <code>null</code>.
     */
    private void validateSiteProperties(IPSSite site, PSSiteProperties props) throws PSValidationException {
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
    public PSSiteSummary findByLegacySiteId(String id, boolean isValidate) throws DataServiceLoadException, PSValidationException {
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
            return new ArrayList<>();

        List<PSSiteSummary> summaries = new ArrayList<>();

        // Filter out sites that are currently getting copied
        Map<String, String> entries = getCopySiteInfo().getEntries();
        String newSiteName = null;
        if (!entries.isEmpty())
        {
            newSiteName = entries.get("Target");
        }

        for (PSSiteSummary site : sums)
        {
            if (!StringUtils.equals(site.getName(), newSiteName))
            {
                if(site.isPageBased()) {
                    if(includePubInfo) {
                        try {
                            site.setPubInfo(getPubServerService().getS3PubInfo(new PSGuid(PSTypeEnum.SITE, site.getSiteId())));
                        }catch (Exception e)
                        {
                            log.error("Error adding the publishing info to the site. Error:{}",
                                    PSExceptionUtils.getMessageForLog(e));
                        }
                    }
                    summaries.add(site);
                }
            }
        }

        return summaries;

        //Filter out sites that shouldn't be visible
        //TODO: Re-enable when site permissions are fixed
        /**
        ArrayList<IPSGuid> guids = new ArrayList<>();
        for(PSSiteSummary s: sums){
            if(s.isPageBased()) {
                guids.add(new PSGuid(PSTypeEnum.SITE, s.getSiteId()));
            }
        }

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
    public void delete(String siteName) throws PSDataServiceException {
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
                log.error("{} Error: {}",
                        msg,
                        PSExceptionUtils.getMessageForLog(e));
                throw new PSDataServiceException("Unable to delete site as Failed to Move Site Items to Recycle Folder: " + siteName);
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
                ArrayList<String> listOfSites = new ArrayList<>(Arrays.asList(folder
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
            log.error("{} Error: {}",
                    msg,
                    PSExceptionUtils.getMessageForLog(e));
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
        List<IPSItemSummary> rootAssetChildren = new ArrayList<>();
        try
        {
            rootAssetChildren = folderHelper.findItems(folderPath);
        }
        catch (Exception e)
        {
            // The folderPath comes from a constant so there's no way the method
            // throws the exception
            String msg = "The folderPath comes from a constant so there's no way the method throws the exception";
            log.error("{} Error: {}",
                    msg,
                    PSExceptionUtils.getMessageForLog(e));
        }

        for (IPSItemSummary rootAssetChild : rootAssetChildren) {
            updateAllowedSitesProperty(rootAssetChild, siteId);
        }
    }

    public PSSite save(PSSite site) throws PSDataServiceException, PSParametersValidationException {
        PSSiteCopyUtils.throwCopySiteMessageIfSameTargetName(site.getName(), "save",
                PSSiteCopyUtils.CAN_NOT_CREATE_SAME_COPIED_SITE_NAME);
        validateNewSite(site);
        return super.save(site);
    }

 
    public PSSite createSiteFromUrl(HttpServletRequest request, PSSite site) throws PSSiteImportException, PSValidationException {
        validateNewSite(site);

        // Get the user agent
        String userAgent = request.getHeader("User-Agent");

        PSSiteImportCtx importContext = siteImportService.importSiteFromUrl(site, userAgent);
        return importContext.getSite();
    }

  
    public Long createSiteFromUrlAsync(HttpServletRequest request, PSSiteImportConfiguration config) throws PSValidationException, IPSFolderService.PSWorkflowNotFoundException {
        PSSite site = config.getSite();
        validateNewSite(site);

        // Get the user agent
        String userAgent = request.getHeader("User-Agent");

        // Create and setup import context
        PSSiteImportCtx importContext = new PSSiteImportCtx();
        importContext.setSite(site);
        importContext.setSiteUrl(site.getBaseUrl());
        importContext.setStatusMessagePrefix(IMPORT_STATUS_MESSAGE_PREFIX);
        importContext.setUserAgent(userAgent);
        importContext.setImportConfiguration(config);

        // Execute import job
        return asyncJobService.startJob(SITE_IMPORT_JOB_BEAN, importContext);

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
    
    public void validateFolders(PSValidateCopyFoldersRequest req) throws PSValidationException {
        notNull(req, "req cannot be null");

        String srcAssetFolder = PSFolderPathUtils.concatPath(PSAssetPathItemService.ASSET_ROOT, req.getSrcFolder());
        String destAssetFolder = PSFolderPathUtils.concatPath(PSAssetPathItemService.ASSET_ROOT, req.getDestFolder());

        validateCopyFolders(StringUtils.removeEnd(srcAssetFolder, "/"), StringUtils.removeEnd(destAssetFolder, "/"));
    }

    public PSSite copy(PSSiteCopyRequest req) throws IPSItemService.PSItemServiceException, PSDataServiceException {
        boolean paused = false;
        notNull(req, "req cannot be null");
        Collection<String> createdLocalAssets = new ArrayList<>();
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

            log.debug("Starting Process Monitor Site Copy of ....{}" ,newName);
            PSSiteCopyProcessMonitor.startSiteCopy(newName);

            assetMap = new HashMap<>();

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
            
            log.debug("Create Site With Content origId: {} newName: {}",origId ,newName);

            copy = siteDao.createSiteWithContent(origId, newName);
            if(copy==null){
                return null;
            }
            copiedFolderPath = copy.getFolderPath();
            // copy the templates
            try{
	            log.info("Copying Site Template...");
	            PSSiteCopyProcessMonitor.copyingTemplates();
	            tempMap = siteTemplateService.copyTemplates(origId, copy.getId());
            }catch(Exception e){
            	log.error("An error occurred while Copying Site Templates for Site id: {} Error: {}" ,
                        origId,
                        PSExceptionUtils.getMessageForLog(e));
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
            	log.error("An exception occurred while Updating Copied Site Assets for Site: {} Error: {}",
                        newName,
                        PSExceptionUtils.getMessageForLog(e));
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
            	log.error("Error updating Copied Site Templates for Site {}. Error: {}" ,
                        newName,
                        PSExceptionUtils.getMessageForLog(e));
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
            	log.error("Error occurred while Copying Site Confiuration for {}. Error: {}" ,
                        newName,
                        PSExceptionUtils.getMessageForLog(e));
            	throw (e);
            }
            
            // If option "Copy assets from the selected folder." was selected,
            // then add copy site to the allowed sites list of the copied root
            // level asset folder.
            if (assetFolder != null)
            {
            	try{
            		log.info("Updating Allowed Sites...");            		
            		String copySiteId = String.valueOf(copySite.getSiteId());
            		addNewSiteToAssetFolderAllowedSites(newName, copySiteId);
            	}catch(Exception e){
            		log.error("An error occurred in Copy Site Updating Allowed Sites for {} Error: {}" ,newName,
                            PSExceptionUtils.getMessageForLog(e));
            		log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            		throw (e);
            	}
            }

        }
        catch (Exception e)
        {

            log.error("Error Copying site, attempting to roll back. Error: {}",
                    PSExceptionUtils.getMessageForLog(e));
            if (assetMap != null)
            {
                for (String asset : createdLocalAssets)
                {
                    // add to map to help cleanup.
                    assetMap.put(asset, asset);
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
                    catch (DeleteException | IPSGenericDao.LoadException e1)
                    {
                        log.error("Cannot delete all site resources for site {} Error:  {}" , siteName,
                                PSExceptionUtils.getMessageForLog(e1));
                    }
                }
            }

            log.error("An error occurred copying site {}. Error: {}",
                    siteName,
                    PSExceptionUtils.getMessageForLog(e));
            throw new PSDataServiceException("There was an error copying the site " + siteName
                    + ", review the logs for details", e);
        }
        finally
        {

            PSSiteCopyProcessMonitor.siteCopyCompleted();
            PSSiteCopyUtils.clearCopySite();
            // resume regardless of pre state, just in case another process has incorrectly left it paused.  e.g. fix
            // issue with site import.
            indexer.resume();
            log.info("PSSiteDataService.Copy: Site Copy Ended - {} to {}", siteName, newName);
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
            ArrayList<String> listOfSites = new ArrayList<>(Arrays.asList(newAssetFolder
                    .getProperty(IPSHtmlParameters.SYS_ALLOWEDSITES).getValue().split(",")));
            listOfSites.add(copySiteId);
            newAssetFolder
                    .setProperty(IPSHtmlParameters.SYS_ALLOWEDSITES, StringUtils.join(listOfSites.toArray(), ","));
        }
    }

    public PSValidationErrors validate(PSSite site) throws PSValidationException {
        return super.validate(site);
    }

 
    public PSSiteStatisticsSummary getSiteStatistics(String siteId) throws PSDataServiceException {
        notNull(siteId, "siteId cannot be null");

        PSSiteSummary site = siteDao.findSummary(siteId);
        if (site == null)
        {
            throw new PSDataServiceException("Unable to get site statistics, failed to find site: " + siteId);
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

    
 
    public PSMapWrapper getSaaSSiteNames(boolean filterUsedSites) throws DataServiceLoadException {
        Map<String, String> resultMap = new HashMap<>();
        File saasDir = new File(PSServer.getRxDir().getAbsolutePath() + SAAS_SITE_CONFIG_FOLDER_PATH);
        if(!saasDir.exists() || !saasDir.isDirectory()){
            if(!saasDir.mkdirs()){
                log.error("Error creating SaaS configuration folder: {}",
                        PSServer.getRxDir().getAbsolutePath() + SAAS_SITE_CONFIG_FOLDER_PATH);
            }
        }
        FileFilter filter = new SuffixFileFilter(".json",IOCase.INSENSITIVE);
        File[] fileList = saasDir.listFiles(filter);
        List<String> siteNames = new ArrayList<>();
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
    public PSSaasSiteConfig getSaasSiteConfig(String siteName) throws DataServiceLoadException {
        PSMapWrapper mapWrapper = getSaaSSiteNames(true);
        Map<String, String> map = mapWrapper.getEntries();
        if(!map.containsKey(siteName)){
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
            log.error("The site config file {} is not a valid json file. Error: {}",
                    file.getName() ,
                    PSExceptionUtils.getMessageForLog(e));
        }
        catch(JsonMappingException  e){
            log.error("The site config file {} does not map to the java class. Error: {}",
                    file.getName() , PSExceptionUtils.getMessageForLog(e));
        }
        catch (IOException e)
        {
            log.error("Exception occurred while reading saas site configuration file {}. Error: {}",
                    file.getName(),
                    PSExceptionUtils.getMessageForLog(e));
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
        List<IPSItemSummary> pages = new ArrayList<>();

        List<String> dirs = new ArrayList<>();
        dirs.add(site.getFolderPath());

        boolean directoryFound = true;
        while (directoryFound)
        {
            List<String> newDirs = new ArrayList<>();
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
        List<PSSiteIssueSummary> issues = new ArrayList<>();

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

            List<String> dirs = new ArrayList<>();
            dirs.add(path);

            boolean directoryFound = true;
            while (directoryFound)
            {
                List<String> newDirs = new ArrayList<>();
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
        List<String> resourceAssets = new ArrayList<>();
        List<String> pageIds = new ArrayList<>();
        Set<String> files = new HashSet<>();

        long linksCount = 0;
        long binaryCount = 0;
        PSPair<Long, Long> pair = new PSPair<>();
        pair.setFirst(linksCount);
        pair.setSecond(binaryCount);
        return pair;
    }

    @Override
    public PSSiteSummary findByPath(String path) throws DataServiceNotFoundException, PSValidationException {
        PSParameterValidationUtils.rejectIfNull("findByPath", "path", path);
        PSSiteSummary sum = siteDao.findByPath(path);
        if (sum != null)
        {
            return sum;
        }

        throw new DataServiceNotFoundException(String.format("Site cannot be found for path: %s", path));

    }

    @Override
    public void createPublishingItemsForPubServer(IPSSite site, PSPubServer pubServer, boolean isDefaultServer) throws PSNotFoundException {
        sitePublishDao.createPublishingItemsForPubServer(site, pubServer, isDefaultServer);
    }

    @Override
    public void setPublishServerAsDefault(IPSSite site, PSPubServer pubServer) throws PSNotFoundException {
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
    public void deletePublishingItemsByPubServer(PSPubServer pubServer) throws PSNotFoundException {
        sitePublishDao.deletePublishingItemsByPubServer(pubServer);
    }

    @Override
    public void updateServerEditions(IPSSite site, PSPubServer oldServer, PSPubServer pubServer, boolean isDefaultServer) throws PSNotFoundException {
        sitePublishDao.updateServerEditions(site, oldServer, pubServer, isDefaultServer);
    }

    /**
     * Validates the specified new site.
     * 
     * @param site the site in question, not <code>null</code>.
     */
    private void validateNewSite(PSSite site) throws PSValidationException {
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
        siteName = StringUtils.defaultString(siteName);
        return siteName.matches("^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\\-]*[A-Za-z0-9])$");
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

        try {
            List<IPSItemSummary> items = folderHelper.findItems(folder);
            if (!items.isEmpty()) {
                builder.reject("siteCopy.folder.exists",
                        "Unable to copy assets to existing folder '" + PSPathUtils.getFinderPath(folder)
                                + "'.  Please rename the folder or choose a different " + "name for your site.");
                builder.throwIfInvalid();
            }
        } catch (Exception e) {
            log.error(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
        }

    }

    /**
     * Validates the specified folder. A folder is not valid if it doesn't
     * exist.
     * 
     * @param folder the path of the folder in question, not <code>null</code>.
     */
    private void validateFolder(String folder) throws PSValidationException {
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
        List<String> currentRoles;
        try {
            currentRoles = userService.getCurrentUser().getRoles();
        } catch (PSDataServiceException e) {
            log.warn(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            return false;
        }
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
            String assetFolder, String id, Map<String, String> tempMap) throws IPSWidgetAssetRelationshipService.PSWidgetAssetRelationshipServiceException {
        // get all the local content items related to this item
        Set<String> contentIds = widgetAssetRelationshipService.getLocalAssets(id);

        for (String contentId : contentIds)
        {
            try {
                updateListAsset(contentId, originalFolderPath, copiedFolderPath, assetFolder, newName, tempMap);
            } catch (PSDataServiceException e) {
                //log the error and continue to that 1 bad asset doesn't prevent all assets from getting updated.
                log.warn("Error updating list asset with ID: {} Error: {}",contentId,
                        PSExceptionUtils.getMessageForLog(e));
            }
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
            Map<String, String> tempMap){
        String type = asset.getType();
        if (siteListAssetMap.containsKey(type))
        {
            log.debug("Updating asset: {} ", asset.getName());
            Map<String, String> replaceMappings = new HashMap<>();
            replaceMappings.put(originalPath, replacementPath);
            replaceMappings.putAll(tempMap);

            try {
                updateListAsset(asset, replaceMappings, siteListAssetMap.get(type));
            } catch (IPSItemWorkflowService.PSItemWorkflowServiceException | PSDataServiceException e) {
                log.warn(PSExceptionUtils.getMessageForLog(e));
                log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            }
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
            Map<String, String> replaceMappings = new HashMap<>();
            replaceMappings.put(originalPath, replacementPath);

            try {
                updateListAsset(asset, replaceMappings, assetListAssetMap.get(type));
            } catch (IPSItemWorkflowService.PSItemWorkflowServiceException | PSDataServiceException e) {
                log.warn("Error updating Asset: {} at Path: {} with New Path: {} Error: {}",
                asset.getId(),originalPath,replacementPath,PSExceptionUtils.getMessageForLog(e));
                log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            }
        }
    }

    /**
     * Updates the given auto list asset. For each field specified in the list
     * of replacement fields, the string value of the field will be modified
     * according to the values specified in the replacement mappings.
     * 
     * @param asset the asset
     * @param replaceMappings map of source to replacement string.
     * @param replaceFields list of asset field names to be updated.
     */
    private void updateListAsset(PSAsset asset, Map<String, String> replaceMappings, List<String> replaceFields) throws PSDataServiceException, IPSItemWorkflowService.PSItemWorkflowServiceException {
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
            String copiedAssetPath, Map<String, String> tempMap) throws PSDataServiceException {
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
    private Collection<String> updatePage(PSPage page, PSSiteSummary copySite, PSSiteSummary origSite, Map<String, String> assetMap) throws DataServiceSaveException {
        try {
            String pageFolderPath = page.getFolderPath();
            String pagePath = PSFolderPathUtils.concatPath(pageFolderPath, page.getName());
            String copySiteName = copySite.getName();
            String origSiteName = origSite.getName();

            String origPagePath = pagePath.replaceFirst(copySiteName, origSiteName);
            PSPage origPage = pageDao.findPageByPath(origPagePath);

            Collection<String> assetIds = null;
            if (origPage != null) {
                String pageId = page.getId();
                Set<String> localIds = widgetAssetRelationshipService.getLocalAssets(origPage.getId());
                if (!localIds.isEmpty()) {
                    // update the copied page's assets
                    widgetAssetRelationshipService.removeAssetWidgetRelationships(pageId, Collections.EMPTY_LIST);
                    assetIds = widgetAssetRelationshipService.copyAssetWidgetRelationships(origPage.getId(), pageId);

                    updateLinks(pageId, origSite, copySiteName, assetMap);
                }

                updateSharedAssets(pageId, assetMap);
                if (assetIds != null && assetIds.size() > 0)
                    updateLinksForLocalAssets(assetIds, origSite, copySite, assetMap);

                // Copy the workflow id from the original page
                page.setWorkflowId(origPage.getWorkflowId());
            }
            return assetIds;
        } catch (IPSPageService.PSPageException | IPSWidgetAssetRelationshipService.PSWidgetAssetRelationshipServiceException | PSValidationException | PSNotFoundException e) {
            log.error("Error updating Page. Error: {}",PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            throw new DataServiceSaveException(e.getMessage(),e);
        }
    }

    private void updateLinksForLocalAssets(Collection<String> localIds, PSSiteSummary origSite, PSSiteSummary copySite,
            Map<String, String> assetMap)
    {
        Set<String> assetIds = new HashSet<>();
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
    private void updateTemplate(String id, PSSiteSummary origSite, String copySiteName, Map<String, String> assetMap) throws IPSWidgetAssetRelationshipService.PSWidgetAssetRelationshipServiceException, PSNotFoundException, PSValidationException {
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
    private void updateLinks(String id, PSSiteSummary origSite, String copySiteName, Map<String, String> assetMap) throws IPSWidgetAssetRelationshipService.PSWidgetAssetRelationshipServiceException, PSValidationException, PSNotFoundException {
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
    private void updateLinkedPages(String assetId, PSSiteSummary origSite, String copySiteName) throws PSNotFoundException, PSValidationException {
        Set<String> linkedPageIds = widgetAssetRelationshipService.getLinkedPages(assetId);
        for (String linkedPageId : linkedPageIds)
        {
            try {
                PSPage linkedPage = pageDao.find(linkedPageId);
                if (linkedPage != null) {
                    String linkedPagePath = PSFolderPathUtils.concatPath(linkedPage.getFolderPath(), linkedPage.getName());
                    PSSiteSummary linkedSite = siteDao.findByPath(linkedPagePath);
                    if (linkedSite != null && linkedSite.getId().equals(origSite.getId())) {
                        // page is under the original site, need to update the link
                        // to point to copied page
                        String copyPagePath = linkedPagePath.replaceFirst(origSite.getName(), copySiteName);
                        PSPage copyPage = pageDao.findPageByPath(copyPagePath);
                        if (copyPage != null) {
                            widgetAssetRelationshipService.updateSharedRelationshipDependent(assetId, linkedPageId,
                                    copyPage.getId());

                        }
                    }
                }
            } catch (PSDataServiceException e) {
                log.warn("Error while processing linked pages. Linked Page ID: {} Error: {}",linkedPageId,
                        PSExceptionUtils.getMessageForLog(e));
                log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            }
        }
    }
/**
 * Default case of updating local linked assets. No need to checkout
 * @param assetId
 * @param assetMap
 */
    private void updateLinkedAssets(String assetId, Map<String, String> assetMap) throws PSValidationException, PSNotFoundException {
    	updateLinkedAssets(assetId,assetMap, false);
    }
    
    /**
     * Updates all links to assets contained in the specified asset.
     * 
     * @param assetId
     * @param checkInOut specify whether or not an item should be checkedout before updating it
     * @param assetMap map of original asset id (string) to copied asset id.
     */
    private void updateLinkedAssets(String assetId, Map<String, String> assetMap, boolean checkInOut) throws PSNotFoundException, PSValidationException {
        updateAssets(assetId, widgetAssetRelationshipService.getLinkedAssetsForAsset(assetId), assetMap, checkInOut);
    }

    /**
     * Updates all shared assets for the specified item. 
     * 
     * @param id of the page/template.
     * @param assetMap map of original asset id (string) to copied asset id.
     */
    private void updateSharedAssets(String id, Map<String, String> assetMap) throws IPSWidgetAssetRelationshipService.PSWidgetAssetRelationshipServiceException, PSValidationException {
    	//Shared assets should be checked out
        
    	boolean checkInOut = true;
    	updateAssets(id, widgetAssetRelationshipService.getSharedAssets(id), assetMap, checkInOut);
     }

    private void updateAssets(String id, Set<String> assetIds, Map<String, String> assetMap) throws PSValidationException {
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
    private void updateAssets(String id, Set<String> assetIds, Map<String, String> assetMap, boolean checkInOut) throws PSValidationException {
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
    private void validateCopyFolders(String src, String dest) throws PSValidationException {
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
    private void handleRemoveSiteAndTouchedConfiguration(String sitename) throws DataServiceSaveException {
        try
        {
            removeSiteConfigurationAndTouchedFile(sitename);
        }
        catch (IOException e)
        {
            String errorMsg = "Failed to remove the site configuration folder/files for site: " + sitename;
            log.error("{} Error: {}", errorMsg,
                    PSExceptionUtils.getMessageForLog(e));
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
    private void handleCopySiteConfiguration(String sourceName, String destinationName) throws DataServiceSaveException {
        try
        {
            copySecureSiteConfiguration(sourceName, destinationName);
        }
        catch (IOException e)
        {
            String errorMsg = "Failed copying the secure configuration from '" + sourceName + "' to '"
                    + destinationName + "'";
            log.error("{} Error: {}",errorMsg,
                    PSExceptionUtils.getMessageForLog(e));
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
    private void handleRemoveTouchedFile(String sitename) throws DataServiceSaveException {
        try
        {
            removeTouchedFile(sitename);
        }
        catch (IOException e)
        {
            String errorMsg = "Failed removing the tch file for site '" + sitename + "'";
            log.error("{} Error: {}", errorMsg, PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
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
    private void updateSiteConfigurationFiles(IPSSite site, PSSiteProperties props) throws DataServiceSaveException {
        try
        {
            updateSiteConfiguration(site, props);
        }
        catch (IOException e)
        {
            String errorMsg = "Failed updating the configuration for site '" + site.getName() + "'";
            log.error("{} Error: {}",
                    errorMsg,
                    PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            throw new DataServiceSaveException(errorMsg, e);
        }
    }

    private List<String> getAssetsByItem(String itemId) throws IPSWidgetAssetRelationshipService.PSWidgetAssetRelationshipServiceException {
        List<String> resourceAssets = new ArrayList<>();

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
    private static Map<String, List<String>> siteListAssetMap = new HashMap<>();

    /**
     * Map whose key is the type of asset auto list asset and value is a list of
     * field names which should be updated when a site is copied.
     */
    private static Map<String, List<String>> assetListAssetMap = new HashMap<>();

    static
    {
        List<String> pal = new ArrayList<>();
        pal.add("query");
        pal.add("site_path");
        pal.add("page_templates_list");
        siteListAssetMap.put("percPageAutoList", pal);

        List<String> cat = new ArrayList<>();
        cat.add("query");
        cat.add("site_path");
        cat.add("page_templates_list");
        siteListAssetMap.put("percCategoryList", cat);

        List<String> tag = new ArrayList<>();
        tag.add("query");
        tag.add("site_path");
        tag.add("page_templates_list");
        siteListAssetMap.put("percTagList", tag);

        List<String> arc = new ArrayList<>();
        arc.add("query_string");
        arc.add("query_site_path");
        arc.add("query_template_list");
        siteListAssetMap.put("percArchiveList", arc);

        List<String> ial = new ArrayList<>();
        ial.add("query");
        ial.add("asset_library_path");
        assetListAssetMap.put("percImageAutoList", ial);
    }
    /**
     * All lower case reserved names for the site name. Make sure to lower case before comparing.
     */
    private static Set<String> reservedSiteNames = new HashSet<>();
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
              log.error("Error getting S3 Publishing Server information for Site: {}. Error: {}",
                      siteId, PSExceptionUtils.getMessageForLog(e));
          }
		return ret;
	}
}
