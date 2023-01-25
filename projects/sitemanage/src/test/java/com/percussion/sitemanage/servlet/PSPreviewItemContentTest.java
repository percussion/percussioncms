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
package com.percussion.sitemanage.servlet;

import static java.util.Arrays.asList;

import com.percussion.assetmanagement.data.PSAsset;
import com.percussion.assetmanagement.service.IPSAssetService;
import com.percussion.assetmanagement.service.IPSWidgetAssetRelationshipService;
import com.percussion.itemmanagement.service.IPSItemService;
import com.percussion.pagemanagement.dao.IPSPageDao;
import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.service.IPSPageService;
import com.percussion.pagemanagement.service.PSSiteDataServletTestCaseFixture;
import com.percussion.server.PSServer;
import com.percussion.share.service.IPSIdMapper;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.share.spring.PSSpringWebApplicationContextUtils;
import com.percussion.test.PSServletTestCase;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.system.IPSSystemWs;

import java.net.URL;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.junit.Assert;
import org.junit.experimental.categories.Category;

//@Ignore("If you want to run these unit tests, adjust the SERVER_URL constant and start your CMS " + "server.")
@Category(IntegrationTest.class)
public class PSPreviewItemContentTest extends PSServletTestCase
{
    private PSSiteDataServletTestCaseFixture fixture;

    private String pageFinderPath = "";

    private String assetFinderPath = "";

    private static final String ASSET_FOLDER = "//Folders/$System$/Assets/uploads";

    /**
     * Used for all tests
     */
    private static HttpClient conn;

    @Override	
    public void setUp() throws Exception
    {
        PSSpringWebApplicationContextUtils.injectDependencies(this);
        fixture = new PSSiteDataServletTestCaseFixture(request, response);
        fixture.setUp();

        URL url = new URL(getUrlRoot());
        int port = url.getPort();
        String host = url.getHost();

        conn = new HttpClient();
        conn.getState().setCredentials(new AuthScope(host, port), new UsernamePasswordCredentials("admin1", "demo"));
        //FB:IJU_SETUP_NO_SUPER NC 1-16-16
        super.setUp();
    }

    private String getUrlRoot()
    {
        return "http://" + PSServer.getHostName() + ":" + PSServer.getListenerPort();
    }

    @Override
    protected void tearDown() throws Exception
    {
        fixture.tearDown();
    }

    public void testPageFriendlyUrl() throws Exception
    {
        // create a page
        String name = "Page1";
        String title = "Page1";
        String folderPath = fixture.site1.getFolderPath();
        String linkTitle = "TestLink";
        String url = "testurl.file";
        String pageId = createPage(name, title, fixture.template1.getId(), folderPath, linkTitle, url, "true",
                "This is Page 1.");

        // Check pageId is not null
        assertNotNull(pageId);

        pageFinderPath = fixture.site1.getFolderPath().substring(1) + "/Page1";
        String path = getUrlRoot() + pageFinderPath;

        System.out.println("pageFinderPath " + pageFinderPath);
        GetMethod req = new GetMethod(path);
        try
        {
            int status = conn.executeMethod(req);

            Assert.assertTrue("Request to get page " + req.getPath() + " succeed with http status code " + status,
                    status == 200);
            String src = req.getResponseBodyAsString();
            System.out.println(status + "\n" + src);
        }
        finally
        {
            req.releaseConnection();
        }
    }

    public void testUpperCasePageFriendlyUrl() throws Exception
    {
        // create a page
        String name = "pageStory399";
        String title = "Page1";
        String folderPath = fixture.site1.getFolderPath();
        String linkTitle = "TestLink";
        String url = "testurl.file";
        String pageId = createPage(name, title, fixture.template1.getId(), folderPath, linkTitle, url, "true",
                "This is Page 1.");

        // Check pageId is not null
        assertNotNull(pageId);

        fixture.assetCleaner.remove(pageId);

        pageFinderPath = fixture.site1.getFolderPath().substring(1) + "/Pagestory399";

        String path = getUrlRoot() + pageFinderPath;
        System.out.println("pageFinderPath " + pageFinderPath);

        GetMethod req = new GetMethod(path);
        try
        {
            int status = conn.executeMethod(req);
            Assert.assertTrue("Request to get asset " + req.getPath() + " succeed with http status code " + status,
                    status == 200);
            String src = req.getResponseBodyAsString();
            System.out.println(status + "\n" + src);
        }
        finally
        {
            req.releaseConnection();
        }
    }

    public void testNotFoundFriendlyUrl() throws Exception
    {
        // generate a fake finder path
        pageFinderPath = fixture.site1.getFolderPath() + "/PageTest";
        String path = getUrlRoot() + pageFinderPath;
        System.out.println("pageFinderPath " + pageFinderPath);

        GetMethod req = new GetMethod(path);
        try
        {
            int status = conn.executeMethod(req);
            Assert.assertTrue("Request to get page " + req.getPath() + " failed with http status code " + status,
                    status == 404);
            String src = req.getResponseBodyAsString();
            System.out.println(status + "\n" + src);
        }
        finally
        {
            req.releaseConnection();
        }
    }

    public void testAssetFriendlyUrl() throws Exception
    {
        PSAsset assetCreated = new PSAsset();
        String assetTitle = "testAssetHtmlSearch" + System.currentTimeMillis();
        assetCreated.getFields().put("sys_title", assetTitle);
        assetCreated.setType("percRawHtmlAsset");
        assetCreated.getFields().put("html", "TestHTML");
        assetCreated.setFolderPaths(asList(ASSET_FOLDER));

        String localAssetId = fixture.saveAsset(assetCreated).getId();

        fixture.assetCleaner.remove(localAssetId);

        assertNotNull(localAssetId);

        assetFinderPath = "/Assets/uploads/" + assetTitle;

        String path = getUrlRoot() + assetFinderPath;
        System.out.println("assetFinderPath " + assetFinderPath);

        GetMethod req = new GetMethod(path);
        try
        {
            int status = conn.executeMethod(req);
            Assert.assertTrue("Request to get asset " + req.getPath() + " succeed with http status code " + status,
                    status == 200);
            String src = req.getResponseBodyAsString();
            System.out.println(status + "\n" + src);
        }
        finally
        {
            req.releaseConnection();
        }
    }

    /**
     * Creates and saves a page using the testcase fixture
     * {@link PSSiteDataServletTestCaseFixture}.
     * 
     * @param name assumed not <code>null</code>.
     * @param title assumed not <code>null</code>.
     * @param templateId assumed not <code>null</code>.
     * @param folderPath assumed not <code>null</code>.
     * @param linkTitle assumed not <code>null</code>.
     * @param url assumed not <code>null</code>.
     * @param noindex assumed not <code>null</code>.
     * @param description assumed not <code>null</code>.
     * 
     * @return the id of the created page, never blank.
     */
    private String createPage(String name, String title, String templateId, String folderPath, String linkTitle,
            String url, String noindex, String description) throws PSDataServiceException {
        PSPage page = new PSPage();
        page.setFolderPath(folderPath);
        page.setName(name);
        page.setTitle(title);
        page.setTemplateId(templateId);
        page.setFolderPath(folderPath);
        page.setLinkTitle(linkTitle);
        page.setNoindex(noindex);
        page.setDescription(description);

        return fixture.createPage(page).getId();
    }

    public IPSPageService getPageService()
    {
        return pageService;
    }

    public void setPageService(IPSPageService pageService)
    {
        this.pageService = pageService;
    }

    public IPSIdMapper getIdMapper()
    {
        return idMapper;
    }

    public void setIdMapper(IPSIdMapper idMapper)
    {
        this.idMapper = idMapper;
    }

    public IPSSystemWs getSystemWs()
    {
        return systemWs;
    }

    public void setSystemWs(IPSSystemWs systemWs)
    {
        this.systemWs = systemWs;
    }

    public IPSPageDao getPageDao()
    {
        return pageDao;
    }

    public void setPageDao(IPSPageDao pageDao)
    {
        this.pageDao = pageDao;
    }

    public IPSWidgetAssetRelationshipService getWidgetService()
    {
        return widgetService;
    }

    public void setWidgetService(IPSWidgetAssetRelationshipService widgetService)
    {
        this.widgetService = widgetService;
    }

    public IPSAssetService getAssetService()
    {
        return assetService;
    }

    public void setAssetService(IPSAssetService assetService)
    {
        this.assetService = assetService;
    }

    /**
     * @return the contentWs
     */
    public IPSContentWs getContentWs()
    {
        return contentWs;
    }

    /**
     * @param contentWs the contentWs to set
     */
    public void setContentWs(IPSContentWs contentWs)
    {
        this.contentWs = contentWs;
    }

    /**
     * @return the itemService
     */
    public IPSItemService getItemService()
    {
        return itemService;
    }

    /**
     * @param itemService the itemService to set
     */
    public void setItemService(IPSItemService itemService)
    {
        this.itemService = itemService;
    }

    private IPSPageService pageService;

    private IPSIdMapper idMapper;

    private IPSSystemWs systemWs;

    private IPSPageDao pageDao;

    private IPSWidgetAssetRelationshipService widgetService;

    private IPSContentWs contentWs;

    private IPSAssetService assetService;

    private IPSItemService itemService;

}
