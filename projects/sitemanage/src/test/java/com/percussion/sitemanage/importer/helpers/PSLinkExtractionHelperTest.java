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

package com.percussion.sitemanage.importer.helpers;

import static org.junit.Assert.*;
import static com.percussion.share.dao.PSFolderPathUtils.concatPath;
import static com.percussion.share.dao.PSFolderPathUtils.pathSeparator;
import static com.percussion.sitemanage.service.IPSSiteSectionMetaDataService.PAGE_CATALOG;
import static com.percussion.sitemanage.service.IPSSiteSectionMetaDataService.SECTION_SYSTEM_FOLDER_NAME;

import com.percussion.itemmanagement.service.IPSItemWorkflowService;
import com.percussion.pagemanagement.dao.IPSPageDao;
import com.percussion.pagemanagement.service.IPSPageCatalogService;
import com.percussion.share.dao.IPSFolderHelper;
import com.percussion.share.dao.IPSGenericDao;
import com.percussion.sitemanage.data.PSPageContent;
import com.percussion.sitemanage.data.PSSite;
import com.percussion.sitemanage.data.PSSiteImportCtx;
import com.percussion.sitemanage.error.PSSiteImportException;
import com.percussion.sitemanage.importer.IPSConnectivity;
import com.percussion.sitemanage.importer.IPSSiteImportLogger;
import com.percussion.sitemanage.importer.PSLink;
import com.percussion.sitemanage.importer.PSSiteImportLogger;
import com.percussion.sitemanage.importer.IPSSiteImportLogger.PSLogObjectType;
import com.percussion.sitemanage.importer.helpers.impl.PSLinkExtractionHelper;
import com.percussion.sitemanage.importer.theme.PSURLConverter;
import com.percussion.sitemanage.importer.utils.PSLinkExtractor;
import com.percussion.sitemanage.importer.helpers.PSLinkExtractionHelperTest.TestablePSLinkExtractionHelper.PSLinkExtractionTestConnectivity;
import com.percussion.theme.service.IPSThemeService;

import java.io.IOException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class PSLinkExtractionHelperTest {

    protected class TestablePSLinkExtractionHelper extends
            PSLinkExtractionHelper {
        /**
         * 
         * Test IPSConnectivity implementation
         *
         */
        protected class PSLinkExtractionTestConnectivity implements
                IPSConnectivity {

            private int mi_StatusCode = 200;

            private String mi_ResponseUrl = "http://local/";
            private Document mi_Document = null;

            public Document get() throws IOException {
                if (mi_Document != null)
                    return mi_Document;
                else
                    throw new IOException("Unhandled content type \"application/pdf\" on URL");
            }

            public void setDocument(Document document) {
                mi_Document = document;
            }

            public int getResponseStatusCode() {
                return mi_StatusCode;
            }

            public void setResponseStatusCode(int statusCode) {
                mi_StatusCode = statusCode;
            }

            public String getResponseUrl() {
                return mi_ResponseUrl;
            }

            public void setResponseUrl(String responseUrl) {
                mi_ResponseUrl = responseUrl;
            }

        }

        private IPSConnectivity m_Connectivity = new PSLinkExtractionTestConnectivity();

        public TestablePSLinkExtractionHelper(
                IPSPageCatalogService pageCatalogService, IPSThemeService themeService, IPSPageDao pageDao,  IPSItemWorkflowService itemWorkflowService, IPSFolderHelper folderHelper) {
            super(pageCatalogService, themeService, pageDao, itemWorkflowService, folderHelper);
        }

        protected IPSConnectivity getConnectivity(String url,
                boolean ignoreContentType, boolean followRedirects,
                String userAgent) {
            return m_Connectivity;
        }
        
        protected void downloadAsset(String remoteUrl, String fullThemePath) {
            
        }

       
        protected String getCmsFolderPathForImageAssetsSiteName(String siteName,
                PSURLConverter urlConverter, String remoteUrl) {
           return  PSLinkExtractionHelper.ASSETS_DIR_PREFIX;
        }

        protected String getRemoteUrlConverted(PSLink link,
                PSURLConverter urlConverter) {
            return link.getAbsoluteLink();
        }
        
        protected PSURLConverter getURLConverter(final PSSiteImportCtx context,
                final IPSSiteImportLogger log, String themeRootDirectory,
                String themeRootUrl, String siteName) {
           return null;
        }
        
        protected String getPathForTargetItem(String siteName, PSLink link)
        {
            // Determine the path
            String pathToTargetItem = getFinderPathForTargetItem(siteName, link.getRelativePathWithFileName());
            
            pathToTargetItem = getCatalogedItemPath(siteName,link.getLinkPath(), link.getPageName());

            return pathToTargetItem;
        }
        
        protected String getFinderPathForTargetItem(String siteName, String targetPathUrl)
        {
            String targetPath = targetPathUrl;
            if (!targetPath.startsWith("/"))
            {
                targetPath = "/" + targetPathUrl;
            }
            
            return "/Sites"+ "/" + siteName + targetPath;
        }
        
        protected String getCatalogedItemPath(String siteName, String folderPath, String pageName)
        {
            // determine paths
            String catalogRoot = concatPath("/Sites/", siteName, pathSeparator() + concatPath(SECTION_SYSTEM_FOLDER_NAME, PAGE_CATALOG));
            String fullFolderPath = concatPath(catalogRoot, folderPath);
            String catItemPath = concatPath(fullFolderPath, pageName);
            
            return catItemPath;
        }

        public IPSConnectivity getConnectivity() {
            return m_Connectivity;
        }
        
        /**
         * Extracted for test
         * @param context
         * @return the theme root URL
         */
        protected String getThemeRootUrl(final PSSiteImportCtx context) {
            return "";
        }

        /**
         * Extracted for test
         * @param context
         * @return the theme root directory
         */
        protected String getThemeRootDirectory(final PSSiteImportCtx context) {
            return "";
        }

    }

    PSSiteImportCtx mContext;
    PSPageContent mPageContent;
    static final String PERC_MANAGED_ATTR = "perc-managed";

    @Before
    public void setUp() {
        mContext = new PSSiteImportCtx();
        mContext.setLogger(new PSSiteImportLogger(PSLogObjectType.SITE));
        PSSite site = new PSSite();
        site.setName("Test Site");
        mContext.setSite(site);
        try {
            PSHelperTestUtils testHelper = new PSHelperTestUtils();
            mPageContent = testHelper.createTempPageBasedOnResource(
                    "PSLinkExtractionHelperTest.html",
                    PSLinkExtractionHelperTest.class, mContext);
        } catch (Exception e) {
            fail("Couldn'create page content #tragic");
        }
    }

    @Ignore("Test relied on broken error handling that has been fixed")
    public void testFileUpload() throws PSSiteImportException, IPSGenericDao.SaveException {
        PSLinkExtractionHelper linkExtractionHelper = new TestablePSLinkExtractionHelper(
                null, null, null, null, null);
        // IOException catalog files - Document is null - Mock throws an
        // IOException per the Jsoup contract for binaries
        linkExtractionHelper.process(mPageContent, mContext);
        Document doc = mPageContent.getSourceDocument();
        Elements links = doc.select(PSLinkExtractor.A_HREF);
        assertTrue(links.size() == 5);
        for (Element link : links) {
            assertTrue(link.attr(PSLinkExtractor.HREF).contains(
                    PSLinkExtractionHelper.ASSETS_DIR_PREFIX));
        }
    }

    @Ignore("Awkward Coupling")
    public void testStandardPath() throws PSSiteImportException, IPSGenericDao.SaveException {
        PSLinkExtractionHelper linkExtractionHelper = new TestablePSLinkExtractionHelper(
                null, null, null, null, null);
        PSLinkExtractionTestConnectivity testConn = (PSLinkExtractionTestConnectivity) ((TestablePSLinkExtractionHelper) linkExtractionHelper)
                .getConnectivity();

        testConn.setDocument(mPageContent.getSourceDocument());
        // IOException catalog files - Document is null - Mock throws an
        // IOException per the Jsoup contract for binaries
        linkExtractionHelper.process(mPageContent, mContext);
        Document doc = mPageContent.getSourceDocument();
        Elements links = doc.select(PSLinkExtractor.A_HREF);
        assertTrue(links.size() == 5);
        for (Element link : links)
        {
            assertFalse(link.attr(PSLinkExtractor.HREF).contains(PSLinkExtractionHelper.ASSETS_DIR_PREFIX));
        }
        for (Element link : links)
        {
            assertTrue(link.attr(PERC_MANAGED_ATTR).contains("true"));
        }
    }

    @Test
    public void testDummy()
    {
    }
}
