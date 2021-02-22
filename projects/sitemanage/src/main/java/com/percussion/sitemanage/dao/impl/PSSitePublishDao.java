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
package com.percussion.sitemanage.dao.impl;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.percussion.pathmanagement.service.impl.PSAssetPathItemService;
import com.percussion.pubserver.IPSPubServerService;
import com.percussion.pubserver.impl.PSPubServerService;
import com.percussion.rx.publisher.IPSEditionTask;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.publisher.*;
import com.percussion.services.publisher.data.PSEditionContentList;
import com.percussion.services.publisher.data.PSEditionContentListPK;
import com.percussion.services.publisher.data.PSEditionType;
import com.percussion.services.pubserver.IPSPubServerDao;
import com.percussion.services.pubserver.data.PSPubServer;
import com.percussion.services.sitemgr.IPSPublishingContext;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.share.dao.IPSGenericDao;
import com.percussion.share.data.PSDataItemSummary;
import com.percussion.share.service.IPSIdMapper;
import com.percussion.sitemanage.data.PSSite;
import com.percussion.sitemanage.data.PSSitePublishProperties;
import com.percussion.sitemanage.data.PSSiteSummary;
import com.percussion.sitemanage.impl.PSSitePublishDaoHelper;
import com.percussion.sitemanage.service.IPSSiteDataService.PublishType;
import com.percussion.sitemanage.service.IPSSitePublishService.PubType;
import com.percussion.sitemanage.service.IPSSiteSectionMetaDataService;
import com.percussion.sitemanage.task.impl.PSUpdateTablesEditionTask;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSPathUtil;
import com.percussion.util.PSUrlUtils;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.service.impl.PSSiteConfigUtils;
import com.percussion.webservices.IPSWebserviceErrors;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.publishing.IPSPublishingWs;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.derby.database.Database;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

import static com.percussion.share.spring.PSSpringWebApplicationContextUtils.getWebApplicationContext;
import static com.percussion.utils.service.impl.PSSiteConfigUtils.removeServerEntry;
import static java.util.Arrays.asList;
import static org.apache.commons.lang.StringUtils.endsWith;
import static org.apache.commons.lang.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;

/**
 * 
 * CRUDS publishing specific data for a site.
 * @author adamgent
 *
 */
@Component("sitePublishDao")
@Lazy
@Transactional(noRollbackFor = Exception.class)
public class PSSitePublishDao
{

    /**
     * The publishing ws. Initialized in ctor, never <code>null</code> after
     * that.
     */
    private IPSPublishingWs publishWs;
    
    /**
     * The publisher service, initialized by constructor, never 
     * <code>null</code> after that.
     * <p>
     * Note, the "publishWs" is an unnecessary layer, should make direct call to
     * this service directly.
     */
    private IPSPublisherService publisherService;
    
    private IPSIdMapper idMapper;   
    private IPSSiteSectionMetaDataService siteSectionMetaDataService;
    private IPSSiteManager siteMgr;
    
    private PSPubServerService pubServerService;
    
    private static final String UPDATE_TABLES_EDITION_TASK_TEMPLATE_PARAM = "template";
    private static final String UPDATE_TABLES_EDITION_TASK_TEMPLATE_VALUE = "perc.pageDatabase";
    private static final String UPDATE_TABLES_EDITION_TASK_NAME = "Java/global/percussion/task/perc_UpdateTablesEditionTask";
    
    private static final String PUBLISH_NOW_SUFFIX = "_PUBLISH_NOW";
    private static final String UNPUBLISH_NOW_SUFFIX = "_UNPUBLISH_NOW";
    
    @Value("${sitePublish.webServerPort:80}")
    private String webServerPort = "80";
    @Value("${rxdeploydir}/Deployment/Server")
    private String webServerFileSystemRoot;
    
    @Autowired
    public PSSitePublishDao(IPSIdMapper idMapper, IPSPublishingWs publishWs, IPSPublisherService publisherService,
            IPSSiteSectionMetaDataService siteSectionMetaDataService, IPSSiteManager siteMgr)
    {
        super();
        this.idMapper = idMapper;
        this.publishWs = publishWs;
        this.publisherService = publisherService;
        this.siteSectionMetaDataService = siteSectionMetaDataService;
        this.siteMgr = siteMgr;
    }

    public List<PSSiteSummary> findAllSummaries() {
        List<IPSSite> sites = publishWs.findAllSites();

        List<PSSiteSummary> sums = new ArrayList<>();
        for(IPSSite site : sites) {
            PSSiteSummary summary = new PSSiteSummary();
            convertToSummary(site, summary);
            //Flag the site as a CM1 site
            summary.setCM1Site(isValidPubSite(summary));

                sums.add(summary);
        }
        return sums;
    }
    
    /**
     * Finds the site summary by the legacy ID.
     * 
     * @param id the legacy ID of the site, not <code>null</code>.
     * @param isValidate it is <code>true</code> if wants to validate the site that contains "category" folder;
     * otherwise don't validate the returned site object. The returned object should not to be validated if it is
     * used for assembly process, such as previewing or publishing. 
     * 
     * @return the site with the specified ID. It may be <code>null</code> if cannot find the site.
     */
    public PSSiteSummary findByLegacySiteId(String id, boolean isValidate)
    {
        IPSGuid siteGuid = idMapper.getGuid(id);
        IPSSite site = publishWs.findSiteById(siteGuid);
        return convertToSummary(site, isValidate);
    }
    
    private boolean isValidPubSite(PSSiteSummary siteSummary) {
        boolean containCategoryFolder = siteSectionMetaDataService.containCategoryFolder(siteSummary);
        if (containCategoryFolder && log.isDebugEnabled()) {
            log.debug("Valid Site: " + siteSummary);
        }
        else if( log.isDebugEnabled()) {
            log.debug("Invalid Site: " + siteSummary);
        }
        return containCategoryFolder;
    }
    
    
    public PSSiteSummary findSummary(String name) throws IPSGenericDao.LoadException {
        try
        {
            notEmpty(name, "name");
            IPSSite site = publishWs.findSite(name);
            if (site == null) return null;
            PSSiteSummary summary = new PSSiteSummary();
            convertToSummary(site, summary);
           // if (isValidPubSite(summary)) - Don't exclude sites without a pub server
                return summary;
        }
        catch (PSErrorException e)
        {
            if (e.getCode() == IPSWebserviceErrors.OBJECT_NOT_FOUND_BY_NAME)
                return null;
            
            throw new IPSGenericDao.LoadException("Failed to find item for id: " + name, e);
        }
    }

    private PSSiteSummary convertToSummary(IPSSite site, boolean isValidate)
    {
        PSSiteSummary summary = new PSSiteSummary();
        convertToSummary(site, summary);
        if (!isValidate || isValidPubSite(summary))
        {
            return summary;
        }

        return null;
    }
    
    /**
     * Updates the specified site and its related edition/content-list with the
     * new name and description.
     * 
     * @param site the existing site, not <code>null</code>.
     * @param newName the new name of the site, not blank.
     * @param newDescrption the new description of the site, may be blank.
     * 
     * @return <code>true</code> if the update resulted in a change to a pubserver definition, <code>false</code>
     * if not.
     */
    public boolean updateSite(IPSSite site, String newName, String newDescrption) throws PSNotFoundException {
        notNull(site, "site");
        notEmpty(newName, "newName");
        
        String oldName = site.getName();
        if (!oldName.equals(newName)) {
            site.setPreviousName(oldName);
        }
        boolean serverChanged = setSiteNameRelatedProperties(site, newName);
        site.setDescription(newDescrption);
      
        siteMgr.saveSite(site);
        
        List<IPSEdition> editions = publishWs.findAllEditionsBySite(site.getGUID());
        
        updateFullEdition(editions, site, oldName);
        
        return serverChanged;
    }
    
    /**
     * Updates the specified site with passed in publishing properties and updates 
     * Content-list with user passed in delivery type and this is part of the 
     * publish properties. 
     * @param site the existing site, not <code>null</code>.
     * @param publishProps publishing properties to be updated on the site. not <code>null</code>.
     */
    public void updateSitePublishProperties(IPSSite site, PSSitePublishProperties publishProps) throws PSNotFoundException {
        notNull(site, "site");
        notNull(publishProps, "publishProps");
        
        List<IPSEdition> editions = publishWs.findAllEditionsBySite(site.getGUID());
        for (IPSEdition edition : editions)
        {
            List<IPSEditionContentList> editionContentLists = publishWs.loadEditionContentLists(edition.getGUID()); 
            for (IPSEditionContentList editionContentList : editionContentLists)
            {
                IPSContentList contentList = publisherService.loadContentList(editionContentList.getContentListId());
                String currentUrl = contentList.getUrl();
                contentList.setUrl(PSUrlUtils.replaceUrlParameterValue(currentUrl,
                        IPSHtmlParameters.SYS_DELIVERYTYPE, publishProps.getPublishType().toString()));
                publisherService.saveContentList(contentList);
            } 
        }
        
        site.setIpAddress(publishProps.getFtpServerName());
        site.setUserId(publishProps.getFtpUserName());
        site.setPassword(publishProps.getFtpPassword());
        site.setPort(publishProps.getFtpServerPort());
        String siteName = site.getName();
        site.setRoot((!publishProps.getPublishType().equals(PublishType.filesystem)) ? 
                getPublishingRoot(publishProps.getDeliveryRootPath(), siteName) : getLocalPublishingRoot(siteName));
        site.setPrivateKey(publishProps.getPrivateKey());

        publishWs.saveSite(site);
    }
    
    protected boolean saveSite(PSSite site) throws PSErrorException, IPSPubServerService.PSPubServerServiceException, PSNotFoundException {
        notNull(site,"site may not be null");
        

        IPSSite tmpSite;
        PSPubServer pubServer = null;
        boolean isNew = false;
        String siteName = site.getName();

        // save or load (if delete) the site object first
        tmpSite = siteMgr.findSite(siteName);
        
        if (tmpSite == null)
        {
            isNew = true;

            // No problem, create new instance
            tmpSite = createSite(siteName);
            
            // create a new publishing server for the new site           
            pubServer = getPubServerService().createDefaultPubServer(tmpSite, siteName);
            
            // update the site to set the default publish server
            tmpSite.setDefaultPubServer(pubServer.getServerId());
        }
        else
        {
            // has to get the site from loadSite(); otherwise cannot save
            // the site object from find.
            tmpSite = siteMgr.loadSiteModifiable(siteName);            
        }
        tmpSite.setDescription(site.getDescription());
        tmpSite.setDefaultFileExtention(site.getDefaultFileExtention());
        tmpSite.setCanonical(site.isCanonical());
        tmpSite.setSiteProtocol(site.getSiteProtocol());
        tmpSite.setDefaultDocument(site.getDefaultDocument());
        tmpSite.setCanonicalDist(site.getCanonicalDist());
        tmpSite.setCanonicalReplace(site.isCanonicalReplace());
        site.setSiteId(tmpSite.getSiteId());

        publishWs.saveSite(tmpSite);
        site.setFolderPath(tmpSite.getFolderRoot());
        if (isNew)
        {
            createPublishingItems(tmpSite, pubServer, true);
        }
        
        return isNew;
    }
    
    public IPSSite createSite(String siteName) throws PSNotFoundException {

        IPSSite tmpSite = publishWs.createSite();
        setSiteNameRelatedProperties(tmpSite, siteName);
        return tmpSite;
    }

    /**
     * Find the specified edition
     * @param pubServerId The pub server guid, not <code>null</code>.
     * @param pubType The publication type, not <code>null</code>.
     * 
     * @return The edition, or <code>null</code> if not found.
     */
    public IPSEdition findEdition(IPSGuid pubServerId, PubType pubType)
    {
        Validate.notNull(pubServerId);
        Validate.notNull(pubType);
        
        List<IPSEdition> editions = publishWs.findAllEditionsByPubServer(pubServerId);
        for (IPSEdition edition : editions)
        {
            if (edition.getName().endsWith(pubType.name()))
                return edition;
        }
        
        return null;
    }
    
    /**
     * Sets the properties related to site name for the specified site.
     * 
     * @param site the updated site, assumed not <code>null</code>.
     * @param siteName the new site name, assumed not blank.
     * 
     * @return <code>true</code> if a pubserver was modified, <code>false</code> if not
     */
    private boolean setSiteNameRelatedProperties(IPSSite site, String siteName) throws PSNotFoundException {
        String oldName = site.getName();
       
        site.setName(siteName);
        site.setFolderRoot(FOLDER_ROOT_BASE + siteName);
        
        String deliveryServerPath = getWebServerFileSystemRoot();
        notEmpty(deliveryServerPath, "webServerFileSystemRoot");
        String port = getWebServerPort();
        port = "80".equals(port) ? "" : ":" + port;
        site.setBaseUrl("http://" + siteName + port + '/');

        // default to local server publishing
        String root = getLocalPublishingRoot(siteName);
        if (oldName != null && PublishType.valueOf(getSiteDeliveryType(site)).isFtpType())
        {
           // this is an existing site configured for ftp publishing
           String oldRoot = getPubServerService().getDefaultPubServer(site.getGUID()).getPropertyValue(IPSPubServerDao.PUBLISH_FOLDER_PROPERTY);
           String pubBase = getPublishingBase(oldRoot, oldName);
           root = getPublishingRoot(pubBase, siteName);
        }
        
        /*
         * For Tomcat virtual hosting we do
         * $CATALINA_HOME/SiteName/ROOT
         * See http://tomcat.apache.org/tomcat-6.0-doc/virtual-hosting-howto.html
         */
        site.setRoot(root);
        return getPubServerService().updateDefaultFolderLocation(site, root, oldName);
    }
    
    /**
     * Generates the publishing root location for local filesystem publishing for the specified site.
     * 
     * @param siteName
     * 
     * @return the absolute path of the root publishing location, never blank.
     */
    private String getLocalPublishingRoot(String siteName)
    {
        return getPublishingRoot(getWebServerFileSystemRoot(), siteName);
    }
    
    /**
     * Generates the publishing root location for the specified site.  This location is tomcat specific, which means
     * that the resulting path will end in {siteName}apps/ROOT.  See {@link #makePublishingDir(String)}.
     * 
     * @param basePath assumed to be the main tomcat server directory.  May not be <code>null</code>.
     * @param siteName never blank.
     * 
     * @return the absolute path of the root publishing location, never blank.
     */
    public String getPublishingRoot(String basePath, String siteName)
    {
        notNull(basePath);
        notEmpty(siteName);
        
        if (!basePath.endsWith("/") && !basePath.endsWith("\\") && basePath.length() > 0)
        {
            basePath += '/';
        }
        
        return basePath + makePublishingDir(siteName);
    }
    
    /**
     * Extracts the base path from the specified publishing root location.  The base path represents the location of the
     * tomcat server.
     * 
     * @param siteRoot the publishing root location, never blank.
     * @param siteName never blank.
     * 
     * @return the base path, never <code>null</code>.
     */
    public String getPublishingBase(String siteRoot, String siteName)
    {
        notEmpty(siteRoot);
        notEmpty(siteName);
        
        int index = siteRoot.lastIndexOf(makePublishingDir(siteName));
        if (index == -1)
        {
           // path must have been generated using windows file separator
           index = siteRoot.lastIndexOf(makePublishingDir(siteName).replace('/', '\\'));
        }
        
        if (index == 0)
        {
            return "";
        }
        else if (index == -1)
        {
            return siteRoot;
        }
        else
        {
            return siteRoot.substring(0, (index == 1) ? index : index - 1);
        }
    }
    
    /**
     * Generates the directory which will be used as the root publishing location for the
     * specified site.
     * 
     * @param siteName never blank.
     * @param publishServerType never blank
     * @param deliveryRootPath never blank
     * 
     * @return the relative directory of the publishing path for the site, never blank.  Forward slash is used as the
     * file separator.
     */
    public String getPublishingDeliveryRoot(String siteName, String publishServerType, String deliveryRootPath)
    {
        String folderPath = StringUtils.EMPTY;
        if (publishServerType.equalsIgnoreCase(PublishType.filesystem.toString())
                || publishServerType.equalsIgnoreCase(PublishType.filesystem_only.toString()))
        {
            folderPath = getLocalPublishingRoot(siteName);
        }
        else if (publishServerType.equalsIgnoreCase(PublishType.ftp.toString())
                || publishServerType.equalsIgnoreCase(PublishType.sftp.toString())
                || publishServerType.equalsIgnoreCase(PublishType.ftp_only.toString())
                || publishServerType.equalsIgnoreCase(PublishType.ftps.toString())
                || publishServerType.equalsIgnoreCase(PublishType.ftps_only.toString())
                || publishServerType.equalsIgnoreCase(PublishType.sftp_only.toString()))
        {
            folderPath = getPublishingRoot(deliveryRootPath, siteName);
        }
        return folderPath;
    }
    
    /**
     * Generates the directory under a tomcat server which will be used as the root publishing location for the
     * specified site.
     * 
     * @param siteName never blank.
     * 
     * @return the relative directory of the publishing path for the site, never blank.  Forward slash is used as the
     * file separator.
     */
    public String makePublishingDir(String siteName)
    {
        notEmpty(siteName);
        
        return siteName + "apps/ROOT";
    }
    
    protected String getFQDN() {
        try
        {
            return InetAddress.getLocalHost().getCanonicalHostName();
        }
        catch (UnknownHostException e)
        {
            log.warn("Could not get FQDN for localhost using localhost for baseurl");
            return "localhost";
        }
    }
        
    public void convertToSummary(IPSSite site, PSSiteSummary summary) {

        summary.setGuid(new PSGuid(PSTypeEnum.SITE, site.getSiteId()).toString());
        summary.setFolderPath(site.getFolderRoot());
        summary.setFolderPaths(asList(site.getFolderRoot()));
        summary.setName(site.getName());
        summary.setType(PSDataItemSummary.TYPE_SITE);
        summary.setDescription(site.getDescription());
        summary.setBaseUrl(site.getBaseUrl());
        summary.setSiteId(site.getSiteId());
        summary.setDefaultFileExtention(site.getDefaultFileExtention());
        summary.setCanonical(site.isCanonical());
        summary.setSiteProtocol(site.getSiteProtocol());
        summary.setDefaultDocument(site.getDefaultDocument());
        summary.setCanonicalDist(site.getCanonicalDist());
        summary.setCanonicalReplace(site.isCanonicalReplace());
        summary.setOverrideSystemJQuery(site.isOverrideSystemJQuery());
        summary.setSiteAfterBodyOpenContent(site.getSiteAfterBodyOpenContent());
        summary.setSiteBeforeBodyCloseContent(site.getSiteBeforeBodyCloseContent());
        summary.setOverrideSystemFoundation(site.isOverrideSystemFoundation());
        summary.setOverrideSystemJQueryUI(site.isOverrideSystemJQueryUI());
        summary.setSiteAdditionalHeadContent(site.getSiteAdditionalHeadContent());
        summary.setLabel(site.getLabel());
    }
    
    /**
     * Creates (and saves) the publishing infrastructure for the specified site.
     * This includes all content lists and editions.
     * 
     * @param site The site, may not be <code>null</code>.
     * @param pubServer The default publishing server associated with the new site, may not be <code>null</code>.
     * @param isDefaultServer boolean flag that indicates whether the server is the default <code>null</code>.
     * 
     * @throws PSErrorException If an error occurs creating the required content
     *             lists.
     */
    protected void createPublishingItems(IPSSite site, PSPubServer pubServer, boolean isDefaultServer) throws PSErrorException, PSNotFoundException {
        notNull(site, "site");

        // Create content lists
        createContentLists(site, pubServer, isDefaultServer);

        // Create editions
        createEditions(site, pubServer, isDefaultServer);
        
        // Setup publish now
        addPublishNow(site);
        
        addUnpublishNow(site);
    }

    /**
     * Creates (and saves) content lists for the specified {@link PSPubServer
     * publish server}.
     * 
     * @param site The {@link IPSSite site}, assumed not <code>null</code>.
     * @param pubServer the {@link PSPubServer publish server}, assumed not
     *            <code>null</code>.
     * @param isDefault <code>true</code> if this is the default pub server, <code>false</code> otherwise.
     */
    private void createContentListsForPubServer(IPSSite site, PSPubServer pubServer, boolean isDefault)
    {
        String siteName = site.getName();
        String siteRoot = site.getFolderRoot();

        Map<String, String> searchGenParams = new HashMap<>();
        Map<String, String> assetSearchGenParams = new HashMap<>();

        String queryKey = "query";
        searchGenParams.put(queryKey, PSSitePublishDaoHelper.makeJcrSearchQuery(siteRoot));
        assetSearchGenParams.put(queryKey, PSSitePublishDaoHelper.makeJcrSearchQuery(PSAssetPathItemService.ASSET_ROOT));

        IPSGuid filterId = PSSitePublishDaoHelper.getPublicItemFilterGuid();
        
        String suffix = PSSitePublishDaoHelper.createSiteSuffix(site);
        boolean isStaging = (PSPubServer.STAGING.equalsIgnoreCase(pubServer.getServerType()));
        
        if (pubServer.isDatabaseType() || pubServer.isXmlFormat())
        {
            if(isStaging){
            	filterId = PSSitePublishDaoHelper.getStagingItemFilterGuid();
            }
        	// only create one content list if we are publishing to database or xml format
            PSSitePublishDaoHelper.createPubServerContentList(siteName, suffix + FULL_SITE, IPSContentList.Type.NORMAL, "Site Root Full",
                    PSEditionType.AUTOMATIC, SEARCH_GENERATOR, searchGenParams, filterId, true, pubServer);
        }
        else if (isStaging)
        {
            // only create one content list if we are publishing to database or xml format
        	filterId = PSSitePublishDaoHelper.getStagingItemFilterGuid();
            PSSitePublishDaoHelper.createPubServerContentList(siteName, suffix + STAGING_SITE, IPSContentList.Type.NORMAL, "Site Root Staging",
                    PSEditionType.AUTOMATIC, SEARCH_GENERATOR, searchGenParams, filterId, true, pubServer);

            PSSitePublishDaoHelper.createPubServerContentList(siteName, suffix + STAGING_ASSET, IPSContentList.Type.NORMAL, "Asset Root Staging",
                    PSEditionType.AUTOMATIC, SEARCH_GENERATOR, assetSearchGenParams, filterId, true, pubServer);
            //Create incremental content list
        	PSSitePublishDaoHelper.createIncrementalContentList(site, pubServer, filterId);
        }
        else
        {
            // Full Non-Binary
            PSSitePublishDaoHelper.createPubServerContentList(siteName, suffix + FULL_SITE, IPSContentList.Type.NORMAL, "Site Root Full",
                    PSEditionType.AUTOMATIC, SEARCH_GENERATOR, searchGenParams, filterId, true, pubServer);

            PSSitePublishDaoHelper.createPubServerContentList(siteName, suffix + FULL_ASSET, IPSContentList.Type.NORMAL, "Asset Root Full",
                    PSEditionType.AUTOMATIC, SEARCH_GENERATOR, assetSearchGenParams, filterId, true, pubServer);
        } 
        
    }

    /**
     * Creates the content lists and the publishing editions for the given
     * {@link PSPubServer publish server}. It creates one content list only
     * (for pages) if the server is {@link PublishType} Database or it is for
     * xml publishing, otherwise it creates two content lists, one for pages and
     * one for assets.
     * 
     * @param site The {@link IPSSite site}, must not be <code>null</code>.
     * @param pubServer the {@link PSPubServer publish server}, must not be
     *            <code>null</code>.
     */
    public void createPublishingItemsForPubServer(IPSSite site, PSPubServer pubServer, boolean isDefaultServer) throws PSNotFoundException {
        notNull(site, "site");
        notNull(pubServer, "pubServer");

        createContentListsForPubServer(site, pubServer, isDefaultServer);
        createEditionForPubServer(site, pubServer, isDefaultServer);
    }
    
    /**
     * Creates (and saves) content lists for the specified site.
     * 
     * @param site The site, may not be <code>null</code>.
     * @param isDefaultServer <code>true</code> if this is the default server, <code>false</code> if not.
     * 
     * @throws PSErrorException If a required item filter could not be found.
     */
    private void createContentLists(IPSSite site, PSPubServer pubServer, boolean isDefaultServer) throws PSErrorException
    {
        notNull(site, "site");

        String siteName = site.getName();
        String siteRoot = site.getFolderRoot();
        
        String suffix = PSSitePublishDaoHelper.createSiteSuffix(site);
        String fullSiteName = PSSitePublishDaoHelper.createName(pubServer.getName(), suffix + FULL_SITE);
        String assetsName = PSSitePublishDaoHelper.createName(pubServer.getName(), suffix + FULL_ASSET);

        Map<String, String> searchGenParams = new HashMap<>();
        Map<String, String> assetSearchGenParams = new HashMap<>();
        
        String queryKey = "query";
        searchGenParams.put(queryKey, PSSitePublishDaoHelper.makeJcrSearchQuery(siteRoot));
        assetSearchGenParams.put(queryKey, PSSitePublishDaoHelper.makeJcrSearchQuery(PSAssetPathItemService.ASSET_ROOT));
    
        IPSGuid filterId = PSSitePublishDaoHelper.getPublicItemFilterGuid();

        // Full Non-Binary
        createContentList(siteName, fullSiteName, IPSContentList.Type.NORMAL,
                "Site Root Full", PSEditionType.AUTOMATIC, SEARCH_GENERATOR,
                searchGenParams, filterId, pubServer.getPublishType(), true);

        createContentList(siteName, assetsName, IPSContentList.Type.NORMAL,
                "Asset Root Full", PSEditionType.AUTOMATIC, SEARCH_GENERATOR,
                assetSearchGenParams, filterId, pubServer.getPublishType(), true);
        
        if (isDefaultServer)
            PSSitePublishDaoHelper.createIncrementalContentList(site, pubServer, filterId);
    }
    
        
    /**
     * Convenience method which creates a content list for the specified site with a delivery type of file system.
     */
    protected void createContentList(String siteName, String name, IPSContentList.Type type, String description,
            PSEditionType edtnType, String generator, Map<String, String> genParams, IPSGuid filterId)
    {
        createContentList(siteName, name, type, description, edtnType, generator, genParams, filterId, "filesystem", true, false);
    }
    /**
     * Convenience method which creates a content list for the specified site with a delivery type of file system.
     */
    protected void createContentList(String siteName, String name, IPSContentList.Type type, String description,
            PSEditionType edtnType, String generator, Map<String, String> genParams, IPSGuid filterId,  
            String deliveryType, boolean isPublish)
    {
        createContentList(siteName, name, type, description, edtnType, generator, genParams, filterId, deliveryType, isPublish, false);
    }
    
    /**
     * Creates a content list for the specified site.
     * 
     * @param siteName The site name, may not be blank.
     * @param name The name of the new content list, may not be blank.
     * @param type The type of the new content list which determines how it will
     *            be processed, may not be <code>null</code>.
     * @param description The new content list description, may not be
     *            <code>null</code>.
     * @param edtnType The edition type of the new content list, may not be
     *            <code>null</code>.
     * @param generator The content list generator, may not be blank.
     * @param genParams The content list generator parameters.
     * @param filterId The id of the filter to be used with the content list,
     *            may not be <code>null</code>.
     * @param deliveryType The delivery type of the new content list which determines how it will be published, may not
     *            be blank.
     * @param isPublish determines if the content list is used for publishing or unpublishing. It is <code>true</code>
     *            if it is used for publishing.
     */
    protected void createContentList(String siteName, String name, IPSContentList.Type type, String description,
            PSEditionType edtnType, String generator, Map<String, String> genParams, IPSGuid filterId, 
            String deliveryType, boolean isPublish, boolean isStaging)
    {
        notEmpty(siteName,"siteName may not be blank");
        notEmpty(name, "name");
        notNull(description, "description");
        notNull(type, "type");
        notNull(edtnType, "edtnType");
        notEmpty(generator, "generator");
        notNull(filterId, "filterId");
        notEmpty(deliveryType, "deliveryType");

        IPSContentList cList = publishWs.createContentList(name);
        cList.setType(type);
        cList.setEditionType(edtnType);
        cList.setDescription(description + " - " + siteName);
        String url = PSSitePublishDaoHelper.makeContentListUrl(name, deliveryType);
        if (!isPublish)
            url += "&" + IPSHtmlParameters.SYS_PUBLISH + "=unpublish";
        cList.setUrl(url);
        cList.setGenerator(generator);
        if (genParams != null)
        {
            cList.setGeneratorParams(genParams);
        }
        cList.setExpander(PSSitePublishDaoHelper.CONTENT_LIST_TEMPLATE_EXPANDER);
        cList.setFilterId(filterId);

        publishWs.saveContentList(cList);
    }

    /**
     * Location of where the files get published on the filesystem
     * if one is not provided by the user.
     * @return should be never <code>null</code>, but not guaranteed.
     */
    public String getWebServerFileSystemRoot()
    {
        return webServerFileSystemRoot;
    }

    public void setWebServerFileSystemRoot(String fileSystemRoot)
    {
        this.webServerFileSystemRoot = fileSystemRoot;
    }
    
    /**
     * The default server port of the webserver.
     * @return never <code>null</code>.
     */
    public String getWebServerPort()
    {
        return webServerPort;
    }

    public void setWebServerPort(String webServerPort)
    {
        this.webServerPort = webServerPort;
    }


    
    /**
     * Updates the specified list of full editions for the specified site. When
     * the site name is changed, just need to update the query for each content
     * list associated to the full edition, in order to match the new site name.
     * Others updates regarding the full editions and content lists are done as
     * part of the update process when a publish server is modified.
     * 
     * @param editions it contains the updated edition, assumed not
     * <code>null</code>, may be empty (do nothing in this case).
     * @param site the site of the edition, assumed not <code>null</code>.
     * @param oldSiteName the original name of the site, assumed not blank.
     */
    private void updateFullEdition(List<IPSEdition> editions, IPSSite site, String oldSiteName) throws PSNotFoundException {
        IPSEdition tgtEdition = null;
        for (IPSEdition edition : editions)
        {
            if (edition.getEditionType() != PSEditionType.AUTOMATIC)
            {
                tgtEdition = edition;

                updateContentListForEditionFull(tgtEdition, site, oldSiteName);
            }
        }
    }
    
    /**
     * Updates the content lists contains for the specified "full" edition. Only
     * the content list which name ends with "FULL_SITE" is updated.
     * 
     * @param edition the "full" edition, assumed not <code>null</code>.
     * @param site the site of the edition, assumed not <code>null</code>. it
     * also contains the updated site name.
     * @param oldSiteName the original site name, assumed not blank.
     */
    private void updateContentListForEditionFull(IPSEdition edition, IPSSite site, String oldSiteName) throws PSNotFoundException {
        List<IPSEditionContentList> editionCLists = publishWs.loadEditionContentLists(edition.getGUID());

        for (IPSEditionContentList editionClist : editionCLists)
        {
            IPSContentList clist = publisherService.loadContentList(editionClist.getContentListId());
            if (clist.getName().endsWith(FULL_SITE) || clist.getName().endsWith(STAGING_SITE))
            {
                updateFullSiteContentList(clist, site);
            }
        }
    }
    
    /**
     * Updates the full site content list for the specified site. When the site
     * name changes, the query as part of the parameters of the content list, is
     * updated to match the new site name.
     * 
     * @param fullSiteCList the content list, assumed not <code>null</code>.
     * @param site the site of the content list, assumed not <code>null</code>.
     */
    private void updateFullSiteContentList(IPSContentList fullSiteCList, IPSSite site)
    {
        // update the content list with the new site name
        Map<String, String> searchGenParams = new HashMap<>();
        
        String queryKey = "query";
        searchGenParams.put(queryKey, PSSitePublishDaoHelper.makeJcrSearchQuery(site.getFolderRoot()));
        fullSiteCList.setGeneratorParams(searchGenParams);
        
        publisherService.saveContentList(fullSiteCList);
    }
    
    /**
     * Updates the specified edition for the specified site and publish server 
     * to set the default publish server id in PUBLISH NOW and UNPUBLISH NOW editions.
     *  
     * @param editions it contains the updated edition, assumed not 
     * <code>null</code>, may be empty (do nothing in this case).
     * @param site the site of the edition, assumed not <code>null</code>.
     * @param oldSiteName the original name of the site, assumed not blank.
     * @param pubServer The publish server, may not be <code>null</code>.
     * 
     */
    private void updateOnDemandEditionsByPubServer(List<IPSEdition> editions, IPSSite site, PSPubServer pubServer, String oldSiteName, boolean isDefaultServer) throws PSNotFoundException {
        for (IPSEdition edition : editions)
        {
            if (edition.getSiteId().longValue() == site.getGUID().longValue()
                    && edition.getEditionType() == PSEditionType.AUTOMATIC
                    && edition.getName().endsWith(PSSitePublishDaoHelper.PUBLISH_NOW))
            {
                //if we are a staging publishing or unpublish now skip updating these editions
                if(edition.getName().contains("_STAGING_"))
                    continue;
                
                edition.setPubServerId(pubServer.getGUID());
                publishWs.saveEdition(edition);

                /*
                 * Handle the edition tasks associated to the edition
                 */
                List<IPSEditionTaskDef> taskDefs = publishWs.loadEditionTaskByEdition(edition.getGUID());
                for(IPSEditionTaskDef taskDef : taskDefs)
                {
                    publishWs.deleteEditionTask(taskDef);
                }
                
                addTaskDefsToPubServerEdition(edition, pubServer, isDefaultServer);

                updateContentListForOnDemandEditionsByPubServer(edition, pubServer);
            }
        }
    }
    
    private void updateContentListForOnDemandEditionsByPubServer(IPSEdition edition, PSPubServer pubServer) throws PSNotFoundException {
        notNull(edition, "edition");
        notNull(pubServer, "pubServer");
        
        List<IPSEditionContentList> editionCLists = publishWs.loadEditionContentLists(edition.getGUID());

        for (IPSEditionContentList editionClist : editionCLists)
        {
            IPSContentList cList = publisherService.loadContentList(editionClist.getContentListId());
            
            String name = cList.getName();
            
            String url = PSSitePublishDaoHelper.makeContentListUrl(name, pubServer.getPublishType());
            if (edition.getDisplayTitle().endsWith(PSSitePublishDaoHelper.UNPUBLISH_NOW))
                url += "&" + IPSHtmlParameters.SYS_PUBLISH + "=unpublish";
            
            cList.setUrl(url);
            
            cList.setExpander(PSSitePublishDaoHelper.CONTENT_LIST_TEMPLATE_EXPANDER);
            cList.setExpanderParams(PSSitePublishDaoHelper.getExpanderParams(pubServer));
            cList.setGenerator(SELECTED_ITEMS_GENERATOR);
            
            publisherService.saveContentList(cList);
        }
    }
    
    /**
     * Creates the editions required for publishing the specified site.
     * 
     * @param site The site, may not be <code>null</code>.
     * @param pubServer The publish server, may not be <code>null</code>.
     * 
     * @throws PSErrorException If a required content list does not exist.
     */
    protected void createEditions(IPSSite site, PSPubServer pubServer, boolean isDefaultServer) throws PSErrorException, PSNotFoundException {
        notNull(site, "site");

        String suffix = PSSitePublishDaoHelper.createSiteSuffix(site);
        String fullSiteName = PSSitePublishDaoHelper.createName(pubServer.getName(), suffix + FULL_SITE);
        String fullAssetName = PSSitePublishDaoHelper.createName(pubServer.getName(), suffix + FULL_ASSET);
        
        IPSContentList siteList = publishWs.loadContentList(fullSiteName);
        IPSContentList assetList = publishWs.loadContentList(fullAssetName);
        
        IPSContentList[] cLists = new IPSContentList[]
        {assetList, siteList};
       
        // Full publish for server
        PSSitePublishDaoHelper.createEdition(site, FULL, "Full publish for publish server", PSEditionType.NORMAL,
                IPSEdition.Priority.LOWEST, cLists, pubServer, true, isDefaultServer);
        
        if (isDefaultServer)
        {
            PSSitePublishDaoHelper.createIncrementalEdition(site, pubServer, isDefaultServer);
        }
    }

    /**
     * Creates the full publishing edition for {@link PSPubServer servers} that
     * should publish to Database or in xml format. For database servers, it
     * sets a pre-edition task to update the tables (See
     * {@link PSUpdateTablesEditionTask})
     * 
     * @param site {@link IPSSite}, assumed not <code>null</code>.
     * @param suffix {@link String}, the suffix of the new edition name, assumed
     *            not blank.
     * @param description {@link String}, the description of the new edition,
     *            assumed not blank.
     * @param type The {@link PSEditionType} of the new edition, assumed not
     *            <code>null</code>.
     * @param priority The new edition {@link IPSEdition.Priority priority},
     *            assumed not <code>null</code>.
     * @param cLists {@link IPSContentList}[] with the content lists to
     *            associate with the edition. The content lists should be
     *            ordered by sequence. Assumed not <code>null</code>.
     * @param pubServer {@link PSPubServer} to create the edition for, assumed
     *            not <code>null</code>.
     * @throws {@link PSErrorException} if an error occurs.
     */
    private void createEditionForDatabaseOrXml(IPSSite site, String suffix, String description, PSEditionType type,
            IPSEdition.Priority priority, IPSContentList[] cLists, PSPubServer pubServer)
            throws PSErrorException
    {
        IPSEdition edtn = publishWs.createEdition();
        String edtnName = PSSitePublishDaoHelper.createName(pubServer.getName(), suffix);
        edtn.setName(edtnName);
        edtn.setDisplayTitle(edtnName);
        edtn.setComment(description);
        edtn.setEditionType(type);
        edtn.setSiteId(site.getGUID());
        edtn.setPriority(priority);
        edtn.setPubServerId(pubServer.getGUID());

        IPSGuid edtnGuid = edtn.getGUID();
        long edtnId = edtnGuid.longValue();

        IPSPublishingContext locationContext = publishWs.loadContext(PSSitePublishDaoHelper.LOCATION_CONTEXT);
        IPSGuid locationContextId = locationContext.getGUID();
        IPSPublishingContext linkContext = publishWs.loadContext(PSSitePublishDaoHelper.LINK_CONTEXT);
        IPSGuid linkContextId = linkContext.getGUID();

        for (int i = 0; i < cLists.length; i++)
        {
            IPSEditionContentList ecl = publishWs.createEditionContentList();
            PSEditionContentListPK eclPk = ((PSEditionContentList) ecl).getEditionContentListPK();
            eclPk.setEditionid(edtnId);
            eclPk.setContentlistid(cLists[i].getGUID().longValue());
            ecl.setSequence(i + 1);
            ecl.setDeliveryContextId(locationContextId);
            ecl.setAssemblyContextId(linkContextId);
            publishWs.saveEditionContentList(ecl);
        }

        publishWs.saveEdition(edtn);

        /*
         * If the server is database type, we need to create the pre-task for
         * this edition to update database tables.
         */
        if(pubServer.isDatabaseType())
        {
            createUpdateTablesTask(edtnGuid);
        }
        
        /*
         * Add the workflow post edition task here.
         */
        if (!suffix.equals(PSSitePublishDaoHelper.UNPUBLISH_NOW))
        {
            IPSEditionTaskDef workflowTask = publishWs.createEditionTask();
            workflowTask.setContinueOnFailure(false);
            workflowTask.setEditionId(edtnGuid);
            workflowTask.setParam("state", PSSitePublishDaoHelper.PENDING_WORKFLOW_STATE);
            workflowTask.setParam("trigger", PSSitePublishDaoHelper.LIVE_WORKFLOW_TRANSITION);
            workflowTask.setSequence(1);
            workflowTask.setExtensionName(PSSitePublishDaoHelper.WF_EDITION_TASK_EXT_NAME);        
            publishWs.saveEditionTask(workflowTask);
        }
    }

    /**
     * Creates and saves an instance of {@link IPSEditionTask} for the
     * datababase update tables pre-task.
     * 
     * @param edtnGuid {@link IPSGuid}, assumed not <code>null</code>.
     */
    private void createUpdateTablesTask(IPSGuid edtnGuid)
    {
        IPSEditionTaskDef updateTablesTask = publishWs.createEditionTask();
        updateTablesTask.setEditionId(edtnGuid);
        updateTablesTask.setContinueOnFailure(false);
        updateTablesTask.setParam(UPDATE_TABLES_EDITION_TASK_TEMPLATE_PARAM, UPDATE_TABLES_EDITION_TASK_TEMPLATE_VALUE);
        updateTablesTask.setSequence(-1);
        updateTablesTask.setExtensionName(UPDATE_TABLES_EDITION_TASK_NAME);
        publishWs.saveEditionTask(updateTablesTask);
    }
    
    /**
     * Update the editions associated with a publish server being updated. 
     * 
     * @param site 
     * @param oldServer
     * @param server
     */
    public void updateServerEditions(IPSSite site, PSPubServer oldServer, PSPubServer server, boolean isDefaultServer) throws PSNotFoundException {
        notNull(oldServer);
        notNull(server);

        if (!serverNeedsEditionUpdates(oldServer, server))
        {
            return;
        }

        // Get the editions
        List<IPSEdition> editions = publishWs.findAllEditionsByPubServer(server.getGUID());

        // Update the full Edition
        updateServerFullEdition(editions, oldServer, server, site, isDefaultServer);
        
        // Update the Publish now and Unpublish now editions
        updateOnDemandEditionsByPubServer(editions, site, server, oldServer.getName(), isDefaultServer);
        
        // Update the incremental editions
        updateServerIncrementalEdition(editions, server, site, isDefaultServer);
    }

    /**
     * @param editions
     * @param server
     * @param site
     * @param isDefaultServer
     */
    private void updateServerIncrementalEdition(List<IPSEdition> editions, PSPubServer server,
            IPSSite site, boolean isDefaultServer) throws PSNotFoundException {
        boolean isStaging = PSPubServer.STAGING.equalsIgnoreCase(server.getServerType());
        String suffix = isStaging?PSSitePublishDaoHelper.STAGING_INCREMENTAL:PSSitePublishDaoHelper.INCREMENTAL;
    	IPSEdition tgtEdition = null;
        for (IPSEdition edition : editions)
        {
            if (edition.getSiteId().longValue() == site.getGUID().longValue()
                    && edition.getName().endsWith(suffix))
            {
                tgtEdition = edition;
                break;
            }
        }
        if (tgtEdition == null)
            return; // do nothing if cannot find the edition
        
        String editionName = PSSitePublishDaoHelper.createName(server.getName(), suffix);
        tgtEdition.setName(editionName);
        tgtEdition.setPubServerId(server.getGUID());
        publishWs.saveEdition(tgtEdition);

        /*
         * Handle the edition tasks associated to the edition
         */
        List<IPSEditionTaskDef> taskDefs = publishWs.loadEditionTaskByEdition(tgtEdition.getGUID());
        for(IPSEditionTaskDef taskDef : taskDefs)
        {
            publishWs.deleteEditionTask(taskDef);
        }
        addTaskDefsToPubServerEdition(tgtEdition, server, isDefaultServer);
                
        updateServerContentListForEditionIncremental(tgtEdition, server, site);
    }

    private void updateServerFullEdition(List<IPSEdition> editions, PSPubServer oldServer, PSPubServer server,
            IPSSite site, boolean isDefaultServer) throws PSNotFoundException {
        String editionName = PSSitePublishDaoHelper.createName(oldServer.getName(), FULL);
        IPSEdition tgtEdition = null;
        for (IPSEdition edition : editions)
        {
            if (edition.getPubServerId().longValue() == server.getServerId()
                    && edition.getEditionType() != PSEditionType.AUTOMATIC)
            {
                tgtEdition = edition;
                break;
            }
        }
        if (tgtEdition == null)
            return; // do nothing if cannot find the edition
        
        editionName = PSSitePublishDaoHelper.createName(server.getName(), FULL);
        tgtEdition.setName(editionName);
        publishWs.saveEdition(tgtEdition);

        /*
         * Handle the edition tasks associated to the edition
         */
        List<IPSEditionTaskDef> taskDefs = publishWs.loadEditionTaskByEdition(tgtEdition.getGUID());
        for(IPSEditionTaskDef taskDef : taskDefs)
        {
            publishWs.deleteEditionTask(taskDef);
        }
        addTaskDefsToPubServerEdition(tgtEdition, server, isDefaultServer);
                
        updateServerContentListForEditionFull(tgtEdition, oldServer, server, site);
    }
    
    /**
     * Add the task definitions for the specified edition associated with a given publishing server.
     * 
     * @param tgtEdition the edition, assumed not <code>null</code>.
     * @param pubServer the publishing server, assumed not <code>null</code>.
     */
    private void addTaskDefsToPubServerEdition(IPSEdition tgtEdition, PSPubServer pubServer,
            boolean isDefaultServer)
    {
        IPSGuid editionGuid = tgtEdition.getGUID();

        if (pubServer.isDatabaseType() || pubServer.isXmlFormat())
        {
            if (pubServer.isDatabaseType())
            {
                createUpdateTablesTask(editionGuid);
            }

            
            if (isStagingServer(pubServer))
            {
                /*
                 * Add the staging post edition task here.
                 */
                IPSEditionTaskDef stagingPostEdTask = publishWs.createEditionTask();
                stagingPostEdTask.setContinueOnFailure(false);
                stagingPostEdTask.setEditionId(editionGuid);
                stagingPostEdTask.setSequence(1);
                stagingPostEdTask.setExtensionName(PSSitePublishDaoHelper.STAGING_EDITION_TASK_EXT_NAME);
                publishWs.saveEditionTask(stagingPostEdTask);
            }
            else
            {
                /*
                 * Add the workflow post edition task here.
                 */
                IPSEditionTaskDef workflowTask = publishWs.createEditionTask();
                workflowTask.setContinueOnFailure(false);
                workflowTask.setEditionId(editionGuid);
                workflowTask.setParam("state", PSSitePublishDaoHelper.PENDING_WORKFLOW_STATE);
                workflowTask.setParam("trigger", PSSitePublishDaoHelper.LIVE_WORKFLOW_TRANSITION);
                workflowTask.setSequence(1);
                workflowTask.setExtensionName(PSSitePublishDaoHelper.WF_EDITION_TASK_EXT_NAME);
                publishWs.saveEditionTask(workflowTask);
            }
            
            
            return;
        }
        
        if (pubServer.getPropertyValue(IPSPubServerDao.PUBLISH_DRIVER_PROPERTY).equalsIgnoreCase("AMAZONS3"))
        {
            //Add amazon post edition task here
            IPSEditionTaskDef preTask = publishWs.createEditionTask();
            preTask.setContinueOnFailure(false);
            preTask.setEditionId(editionGuid);
            preTask.setParam("bucket_name", pubServer.getPropertyValue(IPSPubServerDao.PUBLISH_AS3_BUCKET_PROPERTY, ""));
            preTask.setParam("access_key", pubServer.getPropertyValue(IPSPubServerDao.PUBLISH_AS3_ACCESSKEY_PROPERTY, ""));
            preTask.setParam("secret_key", pubServer.getPropertyValue(IPSPubServerDao.PUBLISH_AS3_SECURITYKEY_PROPERTY, ""));
            Region defaultRegion = null;
            try {
                defaultRegion = Regions.getCurrentRegion();
            }catch(Exception e){
               //Do nothing
            }
            preTask.setParam("region", pubServer.getPropertyValue(IPSPubServerDao.PUBLISH_EC2_REGION, (defaultRegion != null? defaultRegion.getName():"")));
            preTask.setSequence(0);
            preTask.setExtensionName(PSSitePublishDaoHelper.AMAZONS3_EDITION_TASK_EXT_NAME);
            publishWs.saveEditionTask(preTask);
        }
        else if(!isOnDemandEdition(tgtEdition))
        {
            /*
             * Add the ant post-edition task here.
             */
            IPSEditionTaskDef preTask = publishWs.createEditionTask();
            preTask.setContinueOnFailure(false);
            preTask.setEditionId(editionGuid);
            preTask.setParam("ant_file", "copy-resources.xml");
            preTask.setSequence(0);
            preTask.setExtensionName(PSSitePublishDaoHelper.EDITION_TASK_EXT_NAME);
            publishWs.saveEditionTask(preTask);
        }
        
        /*
         * Add the workflow post edition task here.
         */
        if(!tgtEdition.getName().contains(PSSitePublishDaoHelper.UNPUBLISH_NOW) && !isStagingServer(pubServer))
        {
            IPSEditionTaskDef workflowTask = publishWs.createEditionTask();
            workflowTask.setContinueOnFailure(false);
            workflowTask.setEditionId(editionGuid);
            workflowTask.setParam("state", PSSitePublishDaoHelper.PENDING_WORKFLOW_STATE);
            workflowTask.setParam("trigger", PSSitePublishDaoHelper.LIVE_WORKFLOW_TRANSITION);
            workflowTask.setSequence(1);
            workflowTask.setExtensionName(PSSitePublishDaoHelper.WF_EDITION_TASK_EXT_NAME);
            publishWs.saveEditionTask(workflowTask);
        }
        
        /*
         * Add the staging post edition task here.
         */
        if (isStagingServer(pubServer))
        {
            IPSEditionTaskDef stagingPostEdTask = publishWs.createEditionTask();
            stagingPostEdTask.setContinueOnFailure(false);
            stagingPostEdTask.setEditionId(editionGuid);
            stagingPostEdTask.setSequence(1);
            stagingPostEdTask.setExtensionName(PSSitePublishDaoHelper.STAGING_EDITION_TASK_EXT_NAME);
            publishWs.saveEditionTask(stagingPostEdTask);
        }
        
        if((isDefaultServer || isStagingServer(pubServer)) && !isOnDemandEdition(tgtEdition))
        {
            /*
             * Add the push feeds edition task
             */
            IPSEditionTaskDef pushFeedsTask = publishWs.createEditionTask();
            pushFeedsTask.setContinueOnFailure(false);
            pushFeedsTask.setEditionId(editionGuid);
            pushFeedsTask.setSequence(2);
            pushFeedsTask.setExtensionName("Java/global/percussion/task/perc_PushFeedDescriptorTask");
            publishWs.saveEditionTask(pushFeedsTask);

            /*
             * Add the flush publication cache post edition task
             */
            IPSEditionTaskDef flushPublicationCacheTask = publishWs.createEditionTask();
            flushPublicationCacheTask.setContinueOnFailure(false);
            flushPublicationCacheTask.setEditionId(editionGuid);
            flushPublicationCacheTask.setSequence(3);
            flushPublicationCacheTask.setExtensionName("Java/global/percussion/task/sys_flushPublicationCache");
            publishWs.saveEditionTask(flushPublicationCacheTask);
        }
    }

    /**
     * Checks if the given edition is on demand (publish now or unpublish now).
     * 
     * @param tgtEdition {@link IPSEdition}, assumed not <code>null</code>.
     * @return <code>true</code> if the edition is publish now or unpublish now.
     *         <code>false</code> otherwise.
     */
    private boolean isOnDemandEdition(IPSEdition tgtEdition)
    {
        if (endsWith(tgtEdition.getName(), PSSitePublishDaoHelper.PUBLISH_NOW) || endsWith(tgtEdition.getName(), UNPUBLISH_NOW_SUFFIX))
        {
            return true;
        }
        return false;
    }
    
    private boolean isStagingServer(PSPubServer pubServer)
    {
       return PSPubServer.STAGING.equalsIgnoreCase( pubServer.getServerType());
    }

    private void updateServerContentListForEditionIncremental(IPSEdition edition, PSPubServer server,
            IPSSite site) throws PSNotFoundException {
        notNull(edition, "edition");
        notNull(server, "server");
        notNull(site, "Site");
        
        // the server is not XML, delete asset content list as it is not needed
        List<IPSEditionContentList> editionCLists = publishWs.loadEditionContentLists(edition.getGUID());
        
        for (IPSEditionContentList editionClist : editionCLists)
        {
            IPSContentList cList = publisherService.loadContentList(editionClist.getContentListId());
            updateIncrementalPubServerContentList(cList, server, site);
        }

    }
    
    /**
     * Handles the update of the content lists for a given server. It deletes
     * the full asset edition if needed (when changing from non xml to xml for
     * example).
     * 
     * @param edition {@link IPSEdition} object, must not be <code>null</code>.
     * @param oldServer {@link PSPubServer} representing the old publishing
     *            server object, must not be <code>null</code>.
     * @param server {@link PSPubServer} representing the new publishing server
     *            object, must not be <code>null</code>.
     * @param site {@link IPSSite} object with the site of the server. Must not
     *            be <code>null</code>.
     */
    private void updateServerContentListForEditionFull(IPSEdition edition, PSPubServer oldServer, PSPubServer server,
            IPSSite site) throws PSNotFoundException {
        notNull(edition, "edition");
        notNull(server, "server");
        notNull(oldServer, "oldServer");
        notNull(site, "Site");
        
        // the server is not XML, delete asset content list as it is not needed
        if ((changedFromOtherToXml(oldServer, server) && !oldServer.isDatabaseType())
                || (changedFromOtherToDatabase(oldServer, server) && !oldServer.isXmlFormat()))
        {
            deleteFullAssetContentList(edition);
        }
        
        List<IPSEditionContentList> editionCLists = publishWs.loadEditionContentLists(edition.getGUID());
        
        int nextSeq = 1;
        for (IPSEditionContentList editionClist : editionCLists)
        {
            IPSContentList cList = publisherService.loadContentList(editionClist.getContentListId());
            updateFullPubServerContentList(cList, server, site);
            
            if (editionClist.getSequence() >= nextSeq)
                nextSeq = editionClist.getSequence() + 1;
        }
        createAssetContentListIfNeeded(oldServer, server, site, nextSeq);
    }
    
    /**
     * Update the full site content list for the specified publishing server.
     * 
     * @param fullSiteCList the content list, assumed not <code>null</code>.
     * @param pubServer the publishing server of the content list, assumed not
     *            <code>null</code>.
     * @param site {@link IPSSite} object with the site of the server. Assumed
     *            not be <code>null</code>.
     */
    private void updateFullPubServerContentList(IPSContentList fullSiteCList, PSPubServer pubServer, IPSSite site)
    {
        // update the content list with the new server name
        String appendSiteId = PSSitePublishDaoHelper.createSiteSuffix(site);
        boolean isStaging = PSPubServer.STAGING.equalsIgnoreCase( pubServer.getServerType());
        String siteSuffix = isStaging?STAGING_SITE:FULL_SITE;
        String assetSuffix = isStaging?STAGING_ASSET:FULL_ASSET;
        
        String fullSiteName = PSSitePublishDaoHelper.createName(pubServer.getName(), appendSiteId + siteSuffix);
        String assetsName = PSSitePublishDaoHelper.createName(pubServer.getName(), appendSiteId + assetSuffix);
        
        String cListName = PSSitePublishDaoHelper.createName(pubServer.getName(), siteSuffix);
        
        if (fullSiteCList.getName().endsWith(siteSuffix))
        {
            cListName = fullSiteName;
        }
        if (fullSiteCList.getName().endsWith(assetSuffix))
        {
            cListName = assetsName;
        }
        
        fullSiteCList.setName(cListName);
        fullSiteCList.setDescription("Full edition - " + pubServer.getName());
        fullSiteCList.setUrl(PSSitePublishDaoHelper.makeContentListUrl(cListName, pubServer.getPublishType()));
        fullSiteCList.setExpander(PSSitePublishDaoHelper.CONTENT_LIST_TEMPLATE_EXPANDER);
        fullSiteCList.setExpanderParams(PSSitePublishDaoHelper.getExpanderParams(pubServer));
        publisherService.saveContentList(fullSiteCList);
    }
    
    private void updateIncrementalPubServerContentList(IPSContentList incrementalSiteCList, PSPubServer pubServer, IPSSite site)
    {
        // update the content list with the new server name
        String appendSiteId = PSSitePublishDaoHelper.createSiteSuffix(site);
        boolean isStaging = PSPubServer.STAGING.equalsIgnoreCase( pubServer.getServerType());
        String incSuffix = isStaging?PSSitePublishDaoHelper.STAGING_INCREMENTAL:PSSitePublishDaoHelper.INCREMENTAL;
        String cListName = PSSitePublishDaoHelper.createName(pubServer.getName(), appendSiteId + incSuffix);
        
        incrementalSiteCList.setName(cListName);
        incrementalSiteCList.setDescription("Incremental edition - " + pubServer.getName());
        incrementalSiteCList.setUrl(PSSitePublishDaoHelper.makeContentListUrl(cListName, pubServer.getPublishType()));
        incrementalSiteCList.setExpander(PSSitePublishDaoHelper.CONTENT_LIST_TEMPLATE_EXPANDER);
        incrementalSiteCList.setExpanderParams(PSSitePublishDaoHelper.getExpanderParams(pubServer));
        publisherService.saveContentList(incrementalSiteCList);
    }

    /**
     * If the server changed from database or xml to other, we need to add a new
     * content list for Assets. We need to double check it, cause we may have
     * the case of a server that was database, but now it's not, but still be
     * xml format, so we don't need to add the new content list. The same
     * happens for the case of a server that was xml and now it's not, but it is
     * a database type.
     * 
     * @param oldServer {@link PSPubServer} object, assumed not <code>null</code>.
     * @param pubServer {@link PSPubServer} object, assumed not
     *            <code>null</code>.
     * @param site {@link IPSSite} object, assumed not <code>null</code>.
     * @param nextSeq the next sequence number for the new edition content list,
     *            assumed not <code>null</code>.
     */
    private void createAssetContentListIfNeeded(PSPubServer oldServer, PSPubServer pubServer, IPSSite site, int nextSeq)
    {
        boolean createContentList = false;

        // the server is not database, create asset content list if it is
        // not xml
        if(changedFromDatabaseToOther(oldServer, pubServer) && !pubServer.isXmlFormat())
        {
            createContentList = true;
        }
        
        // the server is not xml, create asset content list only if it is
        // not database
        if(changedFromXmlToOther(oldServer, pubServer) && !pubServer.isDatabaseType())
        {
            createContentList = true;
        }

        if(createContentList)
        {
            createFullAssetContentList(pubServer, site, nextSeq);
        }
    }
    
    /**
     * Creates a full asset content list for the updated publishing server. It
     * also links the created content list with the corresponding edition for
     * the given server.
     * 
     * @param pubServer {@link PSPubServer} the updated server, assumed not
     *            <code>null</code>.
     * @param site {@link IPSSite} the site for the server, assumed not
     *            <code>null</code>.
     * @param nextSeq the next sequence number for the new edition content list,
     *            assumed not <code>null</code>.
     */
    private void createFullAssetContentList(PSPubServer pubServer, IPSSite site, int nextSeq)
    {
        String siteName = site.getName();
        String siteRoot = site.getFolderRoot();
        String suffix = site.getSiteId() + "_" + FULL_ASSET;

        Map<String, String> searchGenParams = new HashMap<>();
        Map<String, String> assetSearchGenParams = new HashMap<>();

        String queryKey = "query";
        searchGenParams.put(queryKey, PSSitePublishDaoHelper.makeJcrSearchQuery(siteRoot));
        assetSearchGenParams.put(queryKey, PSSitePublishDaoHelper.makeJcrSearchQuery(PSAssetPathItemService.ASSET_ROOT));

        IPSGuid filterId = PSSitePublishDaoHelper.getPublicItemFilterGuid();

        IPSContentList cList = PSSitePublishDaoHelper.createPubServerContentList(siteName, suffix, IPSContentList.Type.NORMAL,
                "Asset Root Full", PSEditionType.AUTOMATIC, SEARCH_GENERATOR, assetSearchGenParams, filterId, true,
                pubServer);

        String edtnName = PSSitePublishDaoHelper.createName(pubServer.getName(), FULL);
        IPSEdition edtn = publishWs.findEditionByName(edtnName);

        updateEditionWithContentList(cList, edtn, nextSeq);
    }

    /**
     * Deletes a full asset content list for the updated publishing server. Try to find
     * the full asset content list with the old name, in case it was updated.
     * 
     * @param edition {@link IPSEdition} the edition, assumed not
     *            <code>null</code>.        
     *            
     */
    private void deleteFullAssetContentList(IPSEdition edition)
    {
        Set<IPSContentList> cLists = new HashSet<>();

        List<IPSEditionContentList> ecls = publishWs.loadEditionContentLists(edition.getGUID());
        for (IPSEditionContentList ecl : ecls)
        {
            IPSContentList cList = publishWs.findContentListById(ecl.getContentListId());
            if (cList != null && cList.getName().endsWith(FULL_ASSET))
            {
                cLists.add(cList);

                publishWs.deleteEditionContentList(ecl);
                publishWs.deleteContentLists(new ArrayList<>(cLists));
                break;
            }
        }
    }

    /**
     * Links the {@link IPSContentList} with the corresponding
     * {@link IPSEdition}. It assumes that both objects are already saved into
     * the system.
     * 
     * @param cList {@link IPSContentList} to link, assumed not
     *            <code>null</code>.
     * @param edtn {@link IPSContentList} to link the content list to, assumed
     *            not <code>null</code>.
     * @param nextSeq the next sequence number for the new edition content list,
     *            assumed not <code>null</code>.
     */
    private void updateEditionWithContentList(IPSContentList cList, IPSEdition edtn, int nextSeq)
    {
        IPSGuid edtnGuid = edtn.getGUID();
        long edtnId = edtnGuid.longValue();

        IPSPublishingContext locationContext = publishWs.loadContext(PSSitePublishDaoHelper.LOCATION_CONTEXT);
        IPSGuid locationContextId = locationContext.getGUID();
        IPSPublishingContext linkContext = publishWs.loadContext(PSSitePublishDaoHelper.LINK_CONTEXT);
        IPSGuid linkContextId = linkContext.getGUID();

        IPSEditionContentList ecl = publishWs.createEditionContentList();
        PSEditionContentListPK eclPk = ((PSEditionContentList) ecl).getEditionContentListPK();
        eclPk.setEditionid(edtnId);
        eclPk.setContentlistid(cList.getGUID().longValue());
        ecl.setSequence(nextSeq);
        ecl.setDeliveryContextId(locationContextId);
        ecl.setAssemblyContextId(linkContextId);
        
        publishWs.saveEditionContentList(ecl);
    }

    /**
     * Checks if the server needs its editions to be updates.
     * <p>
     * The editions need to be updated if:
     * <li>the publishing format changed.
     * <li>the publishing type changed.
     * <li>the server name changed.
     * 
     * @param server {@link PSPubServer} object, assumed not <code>null</code>.
     * @param oldServer {@link PSPubServer} object, assumed not <code>null</code>.
     * @return <code>true</code> if the server needs is editions to be updated.
     *         <code>false</code> otherwise.
     */
    private boolean serverNeedsEditionUpdates(PSPubServer oldServer, PSPubServer server)
    {
        // was xml format and now its not?
        if (oldServer.isXmlFormat() && !server.isXmlFormat())
        {
            return true;
        }

        // was not xml and it is?
        if (!oldServer.isXmlFormat() && server.isXmlFormat())
        {
            return true;
        }

        // did the publish type change?
        if (!equalsIgnoreCase(oldServer.getPublishType(), server.getPublishType()))
        {
            return true;
        }

        // did the server name changed?
        if (changedServerName(oldServer, server))
        {
            return true;
        }

        return false;
    }


    /**
     * Verifies if the server's {@link PublishType publish type} was Database
     * and was changed to other.
     * 
     * @param oldServer {@link PSPubServer} object with the old values, assumed
     *            not <code>null</code>
     * @param pubServer {@link PSPubServer} object with the new values, assumed
     *            not <code>null</code>
     * @return <code>true</code> if the server was database publishing type, and
     *         now its not. <code>false</code> otherwise.
     */
    private boolean changedFromDatabaseToOther(PSPubServer oldServer, PSPubServer pubServer)
    {
        return oldServer.isDatabaseType() && !pubServer.isDatabaseType();
    }

    /**
     * Verifies if the server's {@link PublishType publish type} was not Database
     * and was changed to be Database.
     * 
     * @param oldServer {@link PSPubServer} object with the old values, assumed
     *            not <code>null</code>
     * @param pubServer {@link PSPubServer} object with the new values, assumed
     *            not <code>null</code>
     * @return <code>true</code> if the server was not database publishing type, and
     *         now it is. <code>false</code> otherwise.
     */
    private boolean changedFromOtherToDatabase(PSPubServer oldServer, PSPubServer pubServer)
    {
        return !oldServer.isDatabaseType() && pubServer.isDatabaseType();
    }

    /**
     * Verifies if the server's publishing format was XML and was changed to
     * other.
     * 
     * @param oldServer {@link PSPubServer} object with the old values, assumed
     *            not <code>null</code>
     * @param pubServer {@link PSPubServer} object with the new values, assumed
     *            not <code>null</code>
     * @return <code>true</code> if the server was xml publishing format, and
     *         now its not. <code>false</code> otherwise.
     */
    private boolean changedFromXmlToOther(PSPubServer oldServer, PSPubServer pubServer)
    {
        return oldServer.isXmlFormat() && !pubServer.isXmlFormat();
    }

    /**
     * Verifies if the server's publishing format was not XML and was changed to
     * be XML.
     * 
     * @param oldServer {@link PSPubServer} object with the old values, assumed
     *            not <code>null</code>
     * @param pubServer {@link PSPubServer} object with the new values, assumed
     *            not <code>null</code>
     * @return <code>true</code> if the server was not xml publishing format,
     *         and now it is. <code>false</code> otherwise.
     */
    private boolean changedFromOtherToXml(PSPubServer oldServer, PSPubServer pubServer)
    {
        return !oldServer.isXmlFormat() && pubServer.isXmlFormat();
    }
    
    /**
     * Verifies if the server name was changed or not.
     * 
     * @param oldServer {@link PSPubServer} object with the old values, assumed
     *            not <code>null</code>
     * @param pubServer {@link PSPubServer} object with the new values, assumed
     *            not <code>null</code>
     * @return <code>true</code> if the server name changed. <code>false</code>
     *         otherwise.
     */
    private boolean changedServerName(PSPubServer oldServer, PSPubServer pubServer)
    {
        return !equalsIgnoreCase(oldServer.getName(), pubServer.getName());
    }

    public void deleteSite(String name)
    {
        notEmpty(name, "name may not be blank");

        IPSSite site = null;
        try
        {
            site = publishWs.findSite(name);
            getPubServerService().deletePubServersBySite(site.getGUID());
            deletePublishingItems(site);
            publishWs.deleteSite(site);
        }
        catch (Exception e)
        {
            throw new PSErrorException("Failed to delete all site configuration", e);
        }
    }
    
    /**
     * Lazy loads the publish server service to avoid circular reference.
     * @return the publish server service, never <code>null</code>.
     */
    private PSPubServerService getPubServerService()
    {
        if (pubServerService == null)
            pubServerService = (PSPubServerService) getWebApplicationContext().getBean("pubServerService");
        
        return pubServerService;
    }
    
    /**
     * Deletes all publishing items associated with the specified site.
     * 
     * @param site The site, may not be <code>null</code>.
     */
    protected void deletePublishingItems(IPSSite site)
    {

        notNull(site, "site");
        
        IPSGuid siteId = site.getGUID();

        // delete pub items
        publishWs.deleteSiteItems(siteId);

        // delete pub logs
        List<IPSPubStatus> pubStatusEntries = publishWs.findPubStatusBySite(siteId);
        for (IPSPubStatus pubStatus : pubStatusEntries)
        {
            publishWs.purgeJobLog(pubStatus.getStatusId());
        }

        Set<IPSContentList> cLists = new HashSet<>();

        // delete editions
        List<IPSEdition> edtns = publishWs.findAllEditionsBySite(site.getGUID());
        for (IPSEdition edtn : edtns)
        {
            List<IPSEditionContentList> ecls = publishWs.loadEditionContentLists(edtn.getGUID());
            for (IPSEditionContentList ecl : ecls)
            {
                IPSContentList cList = publishWs.findContentListById(ecl.getContentListId());
                if (cList != null)
                {
                    cLists.add(cList);
                }
            }

            try
            {
                publishWs.deleteEdition(edtn);
            }
            catch (Exception e)
            {
                throw new PSErrorException("Failed to delete edition for site: " + site + " edition " + edtn, e);
            }
        }

        /*
         * Try to delete content lists. It may be empty since the process to delete the publishing servers already
         * deletes content lists associated to editions for publishing servers.
         */
        try
        {
            if (!cLists.isEmpty()) {
                publishWs.deleteContentLists(new ArrayList<>(cLists));
            }
        }
        catch (Exception e)
        {
            throw new PSErrorException("Failed to delete content lists: " + new ArrayList<IPSContentList>(cLists) + " for site:"
                    + site, e);
        }

    }
    
    /**
     * Gets delivery type for this site based on the default publish server.
     * Doesn't iterate all editions and Contentlists. Just get the first Edition
     * and content list and find delivery type from there.
     * 
     * @param site the existing site, not <code>null</code>.
     * @return the delivery type for the given site
     * @throws PSErrorException if no edition is found for the default publish
     *             server.
     */
    public String getSiteDeliveryType(IPSSite site) throws PSNotFoundException {
        // Get the default publish server for the site
        PSPubServer defaultPubServer = getPubServerService().getDefaultPubServer(site.getGUID());
        List<IPSEdition>  editions = publishWs.findAllEditionsByPubServer(defaultPubServer.getGUID());
        if (editions.isEmpty())
        {
            log.error("Cannot find Edition for the default publish server " + defaultPubServer);
            throw new PSErrorException("Cannot find an edition for default publish server " + defaultPubServer);
        }
        
        IPSEdition edition = editions.get(0);
        List<IPSEditionContentList> editionContentLists = publishWs.loadEditionContentLists(edition.getGUID());   
        IPSEditionContentList list = editionContentLists.get(0);
        IPSContentList contentList =  publisherService.findContentListById(list.getContentListId());
        String currentUrl = contentList.getUrl();
        return PSUrlUtils.getUrlParameterValue(currentUrl, IPSHtmlParameters.SYS_DELIVERYTYPE);
    }

    /**
     * Gets delivery type for this site based on the staging server.
     * Doesn't iterate all editions and Contentlists. Just get the first Edition
     * and content list and find delivery type from there.
     * 
     * @param site the existing site, not <code>null</code>.
     * @return the delivery type for the given site
     * @throws PSErrorException if no edition is found for the staging publish
     *             server.
     */
    public String getStagingDeliveryType(IPSSite site) throws PSNotFoundException {
        // Get the default publish server for the site
        PSPubServer stagingPubServer = PSSitePublishDaoHelper.getStagingPubServer(site.getGUID());
        if(stagingPubServer == null)
        {
            log.error("Cannot find staging pub server for site " + site);
            throw new PSErrorException("Cannot find staging pub server for site " + stagingPubServer);
        }
        List<IPSEdition>  editions = publishWs.findAllEditionsByPubServer(stagingPubServer.getGUID());
        if (editions.isEmpty())
        {
            log.error("Cannot find Edition for the staging publish server " + stagingPubServer);
            throw new PSErrorException("Cannot find an edition for staging publish server " + stagingPubServer);
        }
        
        IPSEdition edition = editions.get(0);
        List<IPSEditionContentList> editionContentLists = publishWs.loadEditionContentLists(edition.getGUID());   
        IPSEditionContentList list = editionContentLists.get(0);
        IPSContentList contentList =  publisherService.findContentListById(list.getContentListId());
        String currentUrl = contentList.getUrl();
        return PSUrlUtils.getUrlParameterValue(currentUrl, IPSHtmlParameters.SYS_DELIVERYTYPE);
    }      
    /**
     * Creates the publish now infrastructure for the specified site.  This includes a new content list and edition.
     * 
     * @param site may not be <code>null</code>.
     */
    public void addPublishNow(IPSSite site) throws PSNotFoundException {
        addOnDemandEdition(site, PSSitePublishDaoHelper.PUBLISH_NOW);
    }

    /**
     * Creates the publish now infrastructure for the specified site.  This includes a new content list and edition.
     * 
     * @param site may not be <code>null</code>.
     */
    public void addUnpublishNow(IPSSite site) throws PSNotFoundException {
        addOnDemandEdition(site, PSSitePublishDaoHelper.UNPUBLISH_NOW);
    }

    /**
     * Creates the publish now infrastructure for the specified site.  This includes a new content list and edition.
     * 
     * @param site may not be <code>null</code>.
     */
    public void addStagingPublishNow(IPSSite site) throws PSNotFoundException {
        addOnDemandEdition(site, PSSitePublishDaoHelper.STAGING_PUBLISH_NOW);
    }

    /**
     * Creates the publish now infrastructure for the specified site.  This includes a new content list and edition.
     * 
     * @param site may not be <code>null</code>.
     */
    public void addStagingUnpublishNow(IPSSite site) throws PSNotFoundException {
        addOnDemandEdition(site, PSSitePublishDaoHelper.STAGING_UNPUBLISH_NOW);
    }
    
    /**
     * Adds the specified on demand edition for the given site.
     * 
     * @param site the site, must not be <code>null</code>.
     * @param nowType the type of the on demand edition, assumed not <code>null</code>.
     */
    private void addOnDemandEdition(IPSSite site, String nowType) throws PSNotFoundException {
        notNull(site, "site");
        
        String siteName = site.getName();
        
        boolean isPublish = PSSitePublishDaoHelper.PUBLISH_NOW.equals(nowType) || PSSitePublishDaoHelper.STAGING_PUBLISH_NOW.equals(nowType) ;
        boolean isStaging = PSSitePublishDaoHelper.STAGING_PUBLISH_NOW.equals(nowType) || PSSitePublishDaoHelper.STAGING_UNPUBLISH_NOW.equals(nowType) ;
        PSPubServer pubServer = isStaging?getPubServerService().getStagingPubServer(site.getGUID()):getPubServerService().getDefaultPubServer(site.getGUID());
        String contentListDesc = descriptionTexts.get("CLIST-" + nowType);
        String editionDesc = descriptionTexts.get("EDITION-" + nowType);
        
        String edtnContentListName = PSSitePublishDaoHelper.createName(site.getName(), nowType);
        IPSGuid filterGuid = isStaging?publishWs.findFilterByName("perc_staging").getGUID(): publishWs.findFilterByName("perc_publishNow").getGUID();
        String delType = isStaging?getStagingDeliveryType(site):getSiteDeliveryType(site);
        createContentList(siteName, edtnContentListName, IPSContentList.Type.NORMAL, contentListDesc,
                PSEditionType.AUTOMATIC, SELECTED_ITEMS_GENERATOR, null,
                filterGuid, delType, isPublish);
        
        IPSContentList pnCList = publishWs.loadContentList(PSSitePublishDaoHelper.createName(siteName, nowType));
        
        PSSitePublishDaoHelper.createEdition(site, nowType, editionDesc + siteName + " site immediately",
                PSEditionType.AUTOMATIC, IPSEdition.Priority.HIGHEST, new IPSContentList[]{pnCList}, pubServer, false, true);
    
    }

    /**
     * Creates the editions required for publishing the specified server. They
     * will be different if the server is {@link Database type} or for xml
     * publishing, as those editions will not publish assets.
     * 
     * @param site The {@link IPSSite site}, must not be <code>null</code>.
     * @param pubServer The {@link IPSSite publish server}, must not be
     *            <code>null</code>.
     * 
     * @throws PSErrorException If a required content list does not exist.
     */
    private void createEditionForPubServer(IPSSite site, PSPubServer pubServer, boolean isDefaultServer) throws PSErrorException, PSNotFoundException {
        notNull(site, "site");
        notNull(pubServer, "pubServer");
        
        String suffix = PSSitePublishDaoHelper.createSiteSuffix(site);
        boolean isStaging = PSPubServer.STAGING.equalsIgnoreCase( pubServer.getServerType());
        
        if (pubServer.isDatabaseType() || pubServer.isXmlFormat())
        {
            IPSContentList cList = publishWs.loadContentList(PSSitePublishDaoHelper.createName(pubServer.getName(), suffix + FULL_SITE));
            IPSContentList[] contentLists = new IPSContentList[]{cList};
            
            createEditionForDatabaseOrXml(site, FULL, "Full publish for publish server", PSEditionType.NORMAL,
                    IPSEdition.Priority.LOWEST, contentLists, pubServer);
        }
        else 
        {
            IPSContentList siteList = null;
            IPSContentList assetList = null;
            if(isStaging){
                siteList = publishWs.loadContentList(PSSitePublishDaoHelper.createName(pubServer.getName(), suffix + STAGING_SITE));
                assetList = publishWs.loadContentList(PSSitePublishDaoHelper.createName(pubServer.getName(), suffix + STAGING_ASSET));
            	PSSitePublishDaoHelper.createIncrementalEdition(site, pubServer, isDefaultServer);
            }
            else{
                siteList = publishWs.loadContentList(PSSitePublishDaoHelper.createName(pubServer.getName(), suffix + FULL_SITE));
                assetList = publishWs.loadContentList(PSSitePublishDaoHelper.createName(pubServer.getName(), suffix + FULL_ASSET));
            }
            IPSContentList[] cLists = new IPSContentList[]
            {assetList, siteList};

            // Full publish for server
            PSSitePublishDaoHelper.createEdition(site, FULL, "Full publish for publish server", PSEditionType.NORMAL,
                    IPSEdition.Priority.LOWEST, cLists, pubServer, true, isDefaultServer);

        }
    }
    
    /**
     * Updates the specified publish server and site to match the new default server id.
     * 
     * @param site the existing site, not <code>null</code>.
     * @param pubServer The publish server, may not be <code>null</code>.
     */
    public void setPublishServerAsDefault(IPSSite site, PSPubServer pubServer) throws PSNotFoundException {
        notNull(site, "site");
        notNull(pubServer, "pubServer");
        
        String oldName = site.getName();
        //Make sure we have the site from hibernate not cache.
        IPSSite siteMod = siteMgr.loadSiteModifiable(site.getGUID());
        siteMod.setDefaultPubServer(pubServer.getServerId());
        siteMod.setRoot(pubServer.getPropertyValue(IPSPubServerDao.PUBLISH_FOLDER_PROPERTY));
       
        siteMgr.saveSite(siteMod);
        
        List<IPSEdition> editions = publishWs.findAllEditionsBySite(site.getGUID());
        
        updateOnDemandEditionsByPubServer(editions, site, pubServer, oldName, true);
        updateServerIncrementalEdition(editions, pubServer, site, true);
    }
    
    /**
     * Deletes all editions associated with the specified publish server id.
     * 
     * @param pubServer The pubServer, may not be <code>null</code>.
     */
    public void deletePublishingItemsByPubServer(PSPubServer pubServer) throws PSNotFoundException {
        // delete server entry in the site's tch file
        handleDeleteServerEntryFromTchFile(pubServer);
        
        Set<IPSContentList> cLists = new HashSet<>();
        // delete editions and status logs
        List<IPSEdition> edtns = publishWs.findAllEditionsByPubServer(pubServer.getGUID());
        for (IPSEdition edtn : edtns)
        {
            List<IPSEditionContentList> ecls = publishWs.loadEditionContentLists(edtn.getGUID());
            for (IPSEditionContentList ecl : ecls)
            {
                IPSContentList cList = publishWs.findContentListById(ecl.getContentListId());
                if (cList != null)
                {
                    cLists.add(cList);
                }
            }
            
            try
            {
                // delete pub logs
                publisherService.updatePubLogHidden(pubServer);//to hide logs generate from Incremental
                List<IPSPubStatus> pubStatusEntries = publishWs.findPubStatusByEdition(edtn.getGUID());
                for (IPSPubStatus pubStatus : pubStatusEntries)
                {
                    publishWs.purgeJobLog(pubStatus.getStatusId());
                }
                
                if(edtn.getPubServerId().longValue() == pubServer.getGUID().longValue())
                {
                    publishWs.deleteEdition(edtn);
                }
            }
            catch (Exception e)
            {
                log.error("Failed to delete publishing items associted to publish server: " + pubServer + " edition " + edtn, e);
            }
        }
        
        try
        {
            if (cLists.isEmpty())
            {
                log.error("No content lists to delete for server: " + pubServer.getName());
            }
            else
            {
                publishWs.deleteContentLists(new ArrayList<>(cLists));
            }
        }
        catch (Exception e)
        {
            log.error("Failed to delete content lists: " + new ArrayList<IPSContentList>(cLists) + " for server:"
                    + pubServer.getName(), e);
        }
    }

    /**
     * Tries to delete the publication server entry from the tch file that
     * belongs to the given site. See
     * {@link PSSiteConfigUtils#removeServerEntry(String, long)}
     * 
     * @param pubServer {@link PSPubServer} server object, assumed not
     *            <code>null</code>.
     */
    private void handleDeleteServerEntryFromTchFile(PSPubServer pubServer) throws PSNotFoundException {
        IPSSite site = siteMgr.loadSite(new PSGuid(PSTypeEnum.SITE, pubServer.getSiteId()));
        try
        {
            removeServerEntry(site.getName(), pubServer.getServerId());
        }
        catch (IOException e)
        {
            String msg = "Failed to remove publication server entry in tch file for site '" + site.getName()
                    + "', server '" + pubServer.getName() + "'.";
            log.error(msg, e);
        }
    }

    /**
     * Constant for the full edition name.
     */
    public static final String FULL = "FULL";

    /**
     * Constant for the full site content list name.
     */
    public static final String FULL_SITE = "FULL_SITE";

    /**
     * Constant for the full asset content list name.
     */
    public static final String FULL_ASSET = "FULL_ASSET";
    
    /**
     * Constant for the staging site content list name.
     */
    public static final String STAGING_SITE = "STAGING_SITE";

    /**
     * Constant for the staging asset content list name.
     */
    public static final String STAGING_ASSET = "STAGING_ASSET";
    
    /**
     * Constant for the default search generator.
     */
    private static final String SEARCH_GENERATOR = "Java/global/percussion/system/sys_SearchGenerator";
    
    /**
     * Constant for the selected items generator.
     */
    private static final String SELECTED_ITEMS_GENERATOR = "Java/global/percussion/system/sys_SelectedItemsGenerator";
    
    /**
     * Constant for the site root folder base.
     */
    private static final String FOLDER_ROOT_BASE = PSPathUtil.SITES_ROOT + "/";
    
    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Log log = LogFactory.getLog(PSSitePublishDao.class);
    
    private static final Map<String, String> descriptionTexts = new HashMap<>();
    static{
    	descriptionTexts.put("CLIST-" + PSSitePublishDaoHelper.PUBLISH_NOW, "Site Publish Now");
    	descriptionTexts.put("CLIST-" + PSSitePublishDaoHelper.UNPUBLISH_NOW, "Site Unpublish Now");
    	descriptionTexts.put("CLIST-" + PSSitePublishDaoHelper.STAGING_PUBLISH_NOW, "Site Staging Publish Now");
    	descriptionTexts.put("CLIST-" + PSSitePublishDaoHelper.STAGING_UNPUBLISH_NOW, "Site Staging Unpublish Now");
    	descriptionTexts.put("EDITION-" + PSSitePublishDaoHelper.PUBLISH_NOW, "Publish selected content to the ");
    	descriptionTexts.put("EDITION-" + PSSitePublishDaoHelper.UNPUBLISH_NOW, "Unpublish selected content from the ");
    	descriptionTexts.put("EDITION-" + PSSitePublishDaoHelper.STAGING_PUBLISH_NOW, "Publish selected content to the Staging ");
    	descriptionTexts.put("EDITION-" + PSSitePublishDaoHelper.STAGING_UNPUBLISH_NOW, "Unpublish selected content from the Staging ");
    }

}
