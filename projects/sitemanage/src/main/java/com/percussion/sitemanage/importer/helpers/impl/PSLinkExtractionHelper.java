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

package com.percussion.sitemanage.importer.helpers.impl;

import static com.percussion.share.dao.PSFolderPathUtils.concatPath;
import static com.percussion.share.dao.PSFolderPathUtils.pathSeparator;
import static com.percussion.share.spring.PSSpringWebApplicationContextUtils.getWebApplicationContext;
import static com.percussion.sitemanage.service.IPSSiteSectionMetaDataService.PAGE_CATALOG;
import static com.percussion.sitemanage.service.IPSSiteSectionMetaDataService.SECTION_SYSTEM_FOLDER_NAME;

import com.percussion.itemmanagement.service.IPSItemWorkflowService;
import com.percussion.pagemanagement.dao.IPSPageDao;
import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.service.IPSPageCatalogService;
import com.percussion.pathmanagement.service.impl.PSPathUtils;
import com.percussion.queue.IPSPageImportQueue;
import com.percussion.queue.impl.PSSiteQueue;
import com.percussion.server.IPSHttpErrors;
import com.percussion.services.assembly.impl.PSReplacementFilter;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.share.dao.IPSFolderHelper;
import com.percussion.share.dao.IPSGenericDao;
import com.percussion.share.service.IPSIdMapper;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.sitemanage.dao.IPSiteDao;
import com.percussion.sitemanage.data.PSPageContent;
import com.percussion.sitemanage.data.PSSiteImportCtx;
import com.percussion.sitemanage.data.PSSiteSummary;
import com.percussion.sitemanage.error.PSSiteImportException;
import com.percussion.sitemanage.importer.IPSConnectivity;
import com.percussion.sitemanage.importer.IPSSiteImportLogger;
import com.percussion.sitemanage.importer.IPSSiteImportLogger.PSLogEntryType;
import com.percussion.sitemanage.importer.PSLink;
import com.percussion.sitemanage.importer.PSSiteImporter;
import com.percussion.sitemanage.importer.theme.PSFileDownloader;
import com.percussion.sitemanage.importer.theme.PSURLConverter;
import com.percussion.sitemanage.importer.utils.PSHtmlRetriever;
import com.percussion.sitemanage.importer.utils.PSLinkExtractor;
import com.percussion.sitesummaryservice.service.IPSSiteImportSummaryService;
import com.percussion.theme.service.IPSThemeService;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

   
@Component("linkExtractionHelper")
@Lazy
public class PSLinkExtractionHelper extends PSImportHelper
{

    private static final String HREF = "href";

    private static final String PERC_MANAGED_ATTR = "perc-managed";

    private static String CATALOG_FOLDERS = pathSeparator() + concatPath(SECTION_SYSTEM_FOLDER_NAME, PAGE_CATALOG);

    
    /**
     * The default connectivity implementation. A wrapper around Jsoup
     * 
     */
    protected class PSLinkExtractionHelperConnectivity implements IPSConnectivity
    {
        Connection mi_conn;

        public PSLinkExtractionHelperConnectivity(String url, boolean ignoreContentType, boolean followRedirects,
                String userAgent)
        {
            mi_conn = PSSiteImporter.buildJsoupConnection(url, ignoreContentType, followRedirects, userAgent);
        }

        public Document get() throws IOException
        {
            PSSiteImporter.URLConnectionProperties properties = null;
            
            try {
                properties = PSSiteImporter.overrideConnectionProperties();
                return mi_conn.get();
            }
            catch (IOException e) {
                throw e;
            }
            finally {
                PSSiteImporter.restoreConnectionProperties(properties);
            }
        }

        public int getResponseStatusCode()
        {
            return mi_conn.response().statusCode();
        }

        public String getResponseUrl()
        {
            return mi_conn.response().url().toString();
        }
    }

    /**
     * Prefix of directory for linked files
     */
    public static final String ASSETS_DIR_PREFIX = "/Assets/uploads/";

    public static final String ASSETS_DIR_SUFFIX = "/import/";

    /**
     * Message shown while links are being extracted
     */
    private static final String STATUS_MESSAGE = "extracting links";

    /**
     * The page catalog service
     */
    private transient IPSPageCatalogService m_pageCatalogService;

    /**
     * The them service
     */
    private IPSThemeService themeService;

    private IPSPageImportQueue m_importQueue;

    private IPSIdMapper m_idMapper;

    private IPSiteDao siteDao;

    private IPSPageImportQueue pageImportQueue;

	private IPSPageDao pageDao;
	
	private IPSItemWorkflowService itemWorkflowService;
	
	private IPSFolderHelper folderHelper;

    /**
     * Constructor per super-class contract
     * 
     * @param pageCatalogService a page Catalog Service
     */
	@Autowired
    public PSLinkExtractionHelper(final IPSPageCatalogService pageCatalogService, IPSThemeService themeService, IPSPageDao pageDao, IPSItemWorkflowService itemWorkflowService, IPSFolderHelper folderHelper)
    {
        super();
        this.m_pageCatalogService = pageCatalogService;
        this.themeService = themeService;
        this.pageDao = pageDao;
        this.itemWorkflowService = itemWorkflowService;
        this.folderHelper = folderHelper;
    }

    /**
     * Wrapped getConnectivity, makes testing easier, override this for unit
     * test without mockery.
     * 
     * @param url The URL to connect to
     * @param ignoreContentType see Jsoup interface, indicates if an IOException
     *            should be thrown on binary parsing
     * @param followRedirects see Jsoup interface, indicates if 301 or 302
     *            responses should be followed
     * @param userAgent the user agent string
     * @return a IPSConnectivity object
     */
    protected IPSConnectivity getConnectivity(String url, boolean ignoreContentType, boolean followRedirects,
            String userAgent)
    {
        return new PSLinkExtractionHelperConnectivity(url, ignoreContentType, followRedirects, userAgent);
    }

    public PSSiteQueue getSiteQueue(long siteId)
    {

        if (pageImportQueue == null)
        {
            pageImportQueue =  (IPSPageImportQueue) getWebApplicationContext().getBean("pageImportQueue");
        }

        return pageImportQueue.getPageIds(siteId);
    }

    /**
     * Collect all the links, normalize, handle redirects, handle files, send to
     * page catalog service
     */
    @Override
    public void process(final PSPageContent pageContent, final PSSiteImportCtx context) throws PSSiteImportException, IPSGenericDao.SaveException {
        startTimer();
        final IPSSiteImportLogger log = context.getLogger();
        

        String themeRootDirectory = getThemeRootDirectory(context);
        String themeRootUrl = getThemeRootUrl(context);
        String siteName = context.getSite().getName();
        PSSiteQueue siteQueue = getSiteQueue(context.getSite().getSiteId());
        final List<PSLink> links = PSLinkExtractor.getLinksForDocument(pageContent.getSourceDocument(), log, siteQueue, context.getSite().getBaseUrl());
        final List<PSLink> imageLinks = PSLinkExtractor.getImagesForDocument(pageContent.getSourceDocument(), log);
        
        
        Map<IPSSiteImportSummaryService.SiteImportSummaryTypeEnum, Integer> summaryStats = new HashMap<IPSSiteImportSummaryService.SiteImportSummaryTypeEnum, Integer>();

        for (PSLink imageLink : imageLinks)
        {
            imageLink.getElement().attr(PERC_MANAGED_ATTR, "true");
        }
        int catalogedCount = 0;
        

        Map<String, String> filesForDownload = new HashMap<String, String>();
        PSFileDownloader downloader = new PSFileDownloader();
        for (PSLink link : links)
        {

            String resolvedUrlTarget = link.getAbsoluteLink();
            try
            {
                // Don't process links more than once
                if (!siteQueue.hasLinkBeenProcessed(resolvedUrlTarget))
                {
                	PSLink linkForCache = PSLink.createLinkWithoutElementReference(link.getLinkPath(), link.getLinkText(), link.getAbsoluteLink(), link.getPageName());
                    siteQueue.setProcessedLink(resolvedUrlTarget, linkForCache);
                    
                    IPSConnectivity conn = getConnectivity(link.getAbsoluteLink(), false, true, context.getUserAgent());
                    PSHtmlRetriever ret = new PSHtmlRetriever(conn);
                    // Q: Is this downloading twice? for binaries?
                    // A: No, fulfills binary contract per JSoup
                    Document doc = ret.getHtmlDocument();
                    if (doc != null)
                    {
                        if (link.getLinkText() != null
                                && link.getLinkText().equals(PSLinkExtractor.QUERY_STRING_LINK_TEXT_TOKEN))
                        {
                            link.setLinkText(doc.title());
                        }
                        resolvedUrlTarget = conn.getResponseUrl();

                        String pathToTargetItem = getPathForTargetItem(siteName, link);

                        link.getElement().attr(HREF, pathToTargetItem);
                        link.getElement().attr(PERC_MANAGED_ATTR, "true");

                        catalogPage(context, log, link, conn.getResponseStatusCode(),
                                resolvedUrlTarget);
                            catalogedCount++;
                    }
                    else
                    {
                        PSURLConverter urlConverter = getURLConverter(context, log, themeRootDirectory, themeRootUrl,
                                siteName);
                        // Get the paths
                        String remoteUrl = getRemoteUrlConverted(link, urlConverter);
                        String fullThemePath = getCmsFolderPathForImageAssetsSiteName(siteName, urlConverter, remoteUrl);
                        filesForDownload.put(resolvedUrlTarget, fullThemePath);

                        if (link.getLinkText() != null
                                && link.getLinkText().equals(PSLinkExtractor.QUERY_STRING_LINK_TEXT_TOKEN))
                        {
                            link.setLinkText(link.getPageName());
                        }

                        link.setLinkPath(fullThemePath);
                        link.getElement().attr(HREF, fullThemePath);
                        link.getElement().attr(PERC_MANAGED_ATTR, "true");
                    }
                }
                else
                {
                    String pathToTargetItem = getPathForTargetItem(siteName, link);
                    link.getElement().attr(HREF, pathToTargetItem);
                    link.getElement().attr(PERC_MANAGED_ATTR, "true");
                }
            }
            catch (IOException | PSDataServiceException e)
            {
                log.appendLogMessage(PSLogEntryType.ERROR, "Link Extractor", link.getAbsoluteLink()
                        + " could not be retrieved.");
            }
        }
        downloader.downloadFiles(filesForDownload, context, true);

        if (catalogedCount > 0 || links.size() > 0)
        {
            summaryStats.put(IPSSiteImportSummaryService.SiteImportSummaryTypeEnum.PAGES, new Integer(catalogedCount));
            summaryStats.put(IPSSiteImportSummaryService.SiteImportSummaryTypeEnum.INTERNALLINKS,
                    new Integer(links.size()));
            if (context.getSummaryService() != null && context.getSite().getSiteId() != null)
                context.getSummaryService().update(context.getSite().getSiteId().intValue(), summaryStats);
        }
        log.appendLogMessage(PSLogEntryType.STATUS, "Link Extractor", "Finished cataloging links for Site: "
                + context.getSite().getName());
        endTimer();
    }

    /**
     * Extracted for test
     * 
     * @param context
     * @return the theme root URL
     */
    protected String getThemeRootUrl(final PSSiteImportCtx context)
    {
        String themeRootUrl = themeService.getThemeRootUrl(context.getThemeSummary().getName());
        return themeRootUrl;
    }

    /**
     * Extracted for test
     * 
     * @param context
     * @return the theme root directory
     */
    protected String getThemeRootDirectory(final PSSiteImportCtx context)
    {
        String themeRootDirectory = themeService.getThemeRootDirectory(context.getThemeSummary().getName());
        return themeRootDirectory;
    }

    protected PSURLConverter getURLConverter(final PSSiteImportCtx context, final IPSSiteImportLogger log,
            String themeRootDirectory, String themeRootUrl, String siteName)
    {
        PSURLConverter urlConverter = new PSURLConverter(context.getSiteUrl(), siteName, themeRootDirectory,
                themeRootUrl, log);
        return urlConverter;
    }

    protected String getCmsFolderPathForImageAssetsSiteName(String siteName, PSURLConverter urlConverter,
            String remoteUrl) throws MalformedURLException
    {
        String fullThemePath = urlConverter.getCmsFolderPathForImageAsset(remoteUrl, siteName);
        return fullThemePath;
    }

    protected String getRemoteUrlConverted(PSLink link, PSURLConverter urlConverter)
    {
        String remoteUrl = urlConverter.getFullUrl(link.getAbsoluteLink());
        return remoteUrl;
    }

    protected String getPathForTargetItem(String siteName, PSLink link) throws PSDataServiceException {
        // Determine the path
        String pathToTargetItem = getFinderPathForTargetItem(siteName, link.getRelativePathWithFileName());

        if (!PSPathUtils.doesItemExist(pathToTargetItem))
        {
            pathToTargetItem = getCatalogedItemPath(siteName, link.getLinkPath(), link.getPageName());
        }

        return pathToTargetItem;
    }

    protected String getCatalogedItemPath(String siteName, String folderPath, String pageName) throws PSDataServiceException {
        // find the site
        PSSiteSummary site = getSiteDao().findSummary(siteName);
        if (site == null)
        {
            throw new PSDataServiceException("Unable to find cataloged pages, the specified site was not found: " + siteName);
        }

        // determine paths
        String catalogRoot = getCatalogFolderPath(site);
        String fullFolderPath = concatPath(catalogRoot, folderPath);
        String catItemPath = concatPath(fullFolderPath, pageName);

        return catItemPath;
    }

    protected String getFinderPathForTargetItem(String siteName, String targetPathUrl)
    {
        String targetPath = targetPathUrl;
        if (!targetPath.startsWith("/"))
        {
            targetPath = "/" + targetPathUrl;
        }

        return PSPathUtils.SITES_FINDER_ROOT + "/" + siteName + targetPath;
    }

    /**
     * Get the full path to the catalog folder for the supplied site
     * 
     * @param site The site, assumed not <code>null</code>
     * 
     * @return The catalog folder path for the site
     */
    private String getCatalogFolderPath(PSSiteSummary site)
    {
        return concatPath(site.getFolderPath(), CATALOG_FOLDERS);
    }

    /**
     * Handle a page: send to page catalog service
     * 
     * @param context
     * @param log
     * @param link
     * @param responseStatusCode
     * @param resolvedUrlTarget
     */
    private boolean catalogPage(final PSSiteImportCtx context, final IPSSiteImportLogger log, PSLink link,
            int responseStatusCode, String resolvedUrlTarget)
    {

        boolean isCataloged = false;
        if (responseStatusCode == IPSHttpErrors.HTTP_OK)
        {
            try
            {
                if (context.isCanceled())
                {
                    return false;
                }
                
                String linkUrlWithoutAnchor = resolvedUrlTarget;
                String potentialUrlAnchor = PSReplacementFilter.getAnchor(linkUrlWithoutAnchor);
                if (potentialUrlAnchor != null && !potentialUrlAnchor.isEmpty())
                	linkUrlWithoutAnchor = linkUrlWithoutAnchor.replace(potentialUrlAnchor, "");
                
                String linkPathWithoutAnchor = link.getLinkPath();
                String potentialPathAnchor = PSReplacementFilter.getAnchor(linkPathWithoutAnchor);
                
                if (potentialPathAnchor != null && !potentialPathAnchor.isEmpty())
                	linkPathWithoutAnchor = linkPathWithoutAnchor.replace(potentialPathAnchor, "");
                
               evaluateForIndexPage(context, linkPathWithoutAnchor);
                
                PSPage page = m_pageCatalogService.addCatalogPage(context.getSite().getName(), link.getPageName(),
                        link.getLinkText(), linkPathWithoutAnchor, linkUrlWithoutAnchor);
                if (page != null)
                {
                    isCataloged = true;
                    if (context.isCanceled())
                    {
                        return true;
                    }

                    
                    
                    
                    
                    int id = ((PSLegacyGuid) getIdMapper().getGuid(page.getId())).getContentId();
                    getImportQueue().addCatalogedPageIds(context.getSite(), context.getUserAgent(),
                            Arrays.asList(id));
                }
            }
            catch (Exception e)
            {
                log.appendLogMessage(PSLogEntryType.ERROR, "Link Extractor",
                        "Failed to catalog page: " + link.getAbsoluteLink());
                log.appendLogMessage(
                        PSLogEntryType.STATUS,
                        "Link Extractor",
                        "Failed to catalog page: " + link.getAbsoluteLink() + ", error was: "
                                + e.getLocalizedMessage());
            }
        }
        return isCataloged;
    }

	protected void evaluateForIndexPage(final PSSiteImportCtx context,
			String linkPathWithoutAnchor) throws Exception {
		if (m_pageCatalogService.pageWithFolderPathExists(m_pageCatalogService.getFullFolderPath(linkPathWithoutAnchor, context.getSite())))
		   {
			
				PSPage pageForMove = pageDao.findPageByPath(m_pageCatalogService.getFullFolderPath(linkPathWithoutAnchor, context.getSite()));
				itemWorkflowService.checkOut(pageForMove.getId());
				String folderForMove = concatPath(pageForMove.getFolderPath(), pageForMove.getName());
				pageForMove.setName("index-" + pageForMove.getName());
				pageDao.save(pageForMove);
				itemWorkflowService.checkIn(pageForMove.getId());
				
		        String fullPath = concatPath(pageForMove.getFolderPath(), pageForMove.getName());
		        String newPageFolderPath = concatPath(folderForMove, pageForMove.getName());
		        // try to create the target folder if it doesn't exist
		        if (!PSPathUtils.doesItemExist(folderForMove))
		            folderHelper.createFolder(folderForMove);
		        // move the item into the local location
		        folderHelper.moveItem(folderForMove, fullPath, false);
		        
		       pageForMove = pageDao.findPageByPath(newPageFolderPath);
		       itemWorkflowService.checkOut(pageForMove.getId());
		       pageForMove.setName("index.html");
		       pageDao.save(pageForMove);
		       itemWorkflowService.checkIn(pageForMove.getId());
		    }
	}

    private IPSIdMapper getIdMapper()
    {
        if (m_idMapper == null)
        {
            m_idMapper = (IPSIdMapper) getWebApplicationContext().getBean("sys_idMapper");
        }
        return m_idMapper;
    }

    private IPSPageImportQueue getImportQueue()
    {
        if (m_importQueue == null)
        {
            m_importQueue = (IPSPageImportQueue) getWebApplicationContext().getBean("pageImportQueue");
        }
        return m_importQueue;
    }

    private IPSiteDao getSiteDao()
    {
        if (siteDao == null)
        {
            siteDao = (IPSiteDao) getWebApplicationContext().getBean("siteDao");
        }
        return siteDao;
    }

    @Override
    /**
     * NOOP - this is an optional helper
     */
    public void rollback(final PSPageContent pageContent, final PSSiteImportCtx context)
    {
        // NOOP - this is an optional helper
    }

    @Override
    /**
     * Returns the status message appropriate to this helper
     */
    public String getHelperMessage()
    {
        return STATUS_MESSAGE;
    }

    public void setPageCatalogService(final IPSPageCatalogService pageCatalogService)
    {
        this.m_pageCatalogService = pageCatalogService;
    }
}
