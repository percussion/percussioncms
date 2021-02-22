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
