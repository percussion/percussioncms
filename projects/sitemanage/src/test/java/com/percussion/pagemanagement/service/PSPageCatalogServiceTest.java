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
package com.percussion.pagemanagement.service;

import com.percussion.pagemanagement.data.PSCatalogPageSummary;
import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pathmanagement.service.impl.PSPathUtils;
import com.percussion.share.dao.IPSFolderHelper;
import com.percussion.share.dao.PSFolderPathUtils;
import com.percussion.share.data.IPSItemSummary;
import com.percussion.pagemanagement.service.impl.PSPageCatalogService;
import com.percussion.share.service.IPSSystemProperties;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.share.spring.PSSpringWebApplicationContextUtils;
import com.percussion.sitemanage.error.PSSiteImportException;
import com.percussion.test.PSServletTestCase;
import com.percussion.utils.testing.IntegrationTest;
import org.junit.experimental.categories.Category;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author JaySeletz
 *
 */
@Category(IntegrationTest.class)
public class PSPageCatalogServiceTest extends PSServletTestCase
{
    private PSSiteDataServletTestCaseFixture fixture;
    private IPSPageCatalogService pageCatalogService;
    private IPSFolderHelper folderHelper;
    private IPSPageService pageService;
    private IPSSystemProperties serviceProps;
    private PSMockSystemProps testProps;
    
    @Override
    public void setUp() throws Exception
    {
        PSSpringWebApplicationContextUtils.injectDependencies(this);
        
        fixture = new PSSiteDataServletTestCaseFixture(request, response);
        fixture.setUp();
        
        PSPageCatalogService svcImpl = (PSPageCatalogService)pageCatalogService;
        serviceProps = svcImpl.getSystemProps();

        testProps = new PSMockSystemProps();
        svcImpl.setSystemProps(testProps);
        testProps.setMax("-1");
        //FB:IJU_SETUP_NO_SUPER NC 1-16-16
        super.setUp();
    }

    @Override
    public void tearDown() throws Exception
    {
        fixture.tearDown();
        PSPageCatalogService svcImpl = (PSPageCatalogService)pageCatalogService;
        svcImpl.setSystemProps(serviceProps);
    }
    
    public void testPageCatalog() throws Exception
    {
        testProps.setMax("-1");
        
        String siteName = fixture.site1.getName();
        List<String> ids = pageCatalogService.findCatalogPages(siteName);
        assertNotNull(ids);
        assertTrue(ids.isEmpty());
        
        String href1 = "http://www.test.com/folder1/page1.htm";
        String pageName1 = "page1.htm";
        String folderPath1 = "/folder1";
        String linkText1 = "page1";
        
        PSPage page1 = catalogPage(siteName, href1, pageName1, folderPath1, linkText1);
        PSPage dupe = addCatalogPage(siteName, href1, pageName1, folderPath1, linkText1);

        // Duplicated page is not added to the catalog
        assertNull(dupe);      
        
        ids = pageCatalogService.findCatalogPages(siteName);
        assertNotNull(ids);
        assertTrue(ids.size() == 1);
        assertEquals(page1.getId(), ids.get(0));
        checkSummary(page1.getId(), folderPath1 + "/" + pageName1, linkText1);
        
        String href2 = "http://www.test.com/folder1/subfolder1/page2.htm";
        String pageName2 = "page2.htm";
        String folderPath2 = "/folder1/subfolder1";
        String linkText2 = "page2";
        
        PSPage page2 = catalogPage(siteName, href2, pageName2, folderPath2, linkText2);

        // cannot add a cataloged page where its folder has already been occupied by a page (above)
        PSPage pageNull = pageCatalogService.addCatalogPage(siteName, "pageNull", linkText2, folderPath2 + "/" + pageName2, href2);
        assertNull(pageNull);
        
        ids = pageCatalogService.findCatalogPages(siteName);
        assertNotNull(ids);
        assertTrue(ids.size() == 2);
        assertTrue(ids.contains(page1.getId()));
        assertTrue(ids.contains(page2.getId()));
        checkSummary(page1.getId(), folderPath1 + "/" + pageName1, linkText1);
        checkSummary(page2.getId(), folderPath2 + "/" + pageName2, linkText2);        
        checkTemplate(siteName);
        checkCreateImportedPage(page1);
        
        // create real page
        String realpageName = "realPage.htm";
        String realPath = "/folder2/subfolder2";
        String realhref = "http://www.test.com" + realPath + "/" + realpageName;
        createAndSavePage(realpageName, realPath);
        PSPage dupeRealPage = addCatalogPage(siteName, realhref, realpageName, realPath, realpageName);
        // the page is already exist under the real site folder
        assertNull(dupeRealPage);
    }
    
    public void testMaxCatalogSetting() throws Exception
    {
        String siteName = fixture.site1.getName();
        
        List<String> ids = pageCatalogService.findCatalogPages(siteName);
        assertNotNull(ids);
        assertTrue(ids.isEmpty());
        
        // test w/no value
        PSPageCatalogService svcImpl = (PSPageCatalogService)pageCatalogService;
        PSMockSystemProps props = new PSMockSystemProps();
        svcImpl.setSystemProps(props);
        
        String href1 = "http://www.test.com/folder1/page1.htm";
        String pageName1 = "page1.htm";
        String folderPath1 = "/folder1";
        String linkText1 = "page1";

        
        // test w/0
        props.setMax("0");
        svcImpl.setSystemProps(props);
        PSPage page1 = addCatalogPage(siteName, href1, pageName1, folderPath1, linkText1);
        assertNull(page1);
        
               
        // test 1
        props.setMax("1");
        svcImpl.setSystemProps(props);
        page1 = addCatalogPage(siteName, href1, pageName1, folderPath1, linkText1);
        assertTrue(page1 != null);
        
        String href2 = "http://www.test.com/folder1/subfolder1/page2.htm";
        String pageName2 = "page2.htm";
        String folderPath2 = "/folder1/subfolder1";
        String linkText2 = "page2";
        PSPage page2 = addCatalogPage(siteName, href2, pageName2, folderPath2, linkText2);
        assertNull(page2);
        
        
        // test explicit number
        int max = 5;
        props.setMax(String.valueOf(max));
        svcImpl.setSystemProps(props);
        for (int i = 2; i <= (max+1); i++)
        {
            String pageName = "page" + i;
            String folderPath = "/folder" + i + "/subfolder" + i;
            String href = "http://www.test.com" + folderPath + "/" + pageName;            
            String linkText = pageName;
            
            PSPage page = addCatalogPage(siteName, href, pageName, folderPath, linkText);
            if (i <= max)
                assertTrue(page != null);
            else
                assertNull(page);
        }

        // unlim (go past last number)
        props.setMax("-1");
        svcImpl.setSystemProps(props);
        for (int i = max; i < (max + 5); i++)
        {
            String pageName = "page" + i;
            String folderPath = "/folder" + i + "/subfolder-" + i;
            String href = "http://www.test.com" + folderPath + "/" + pageName;            
            String linkText = pageName;
            
            PSPage page = addCatalogPage(siteName, href, pageName, folderPath, linkText);
            if (i <= max)
                assertTrue(page != null);
        }
    }
    
    public void testImportedPages() throws Exception
    {
        String siteName = fixture.site1.getName();
        
        List<String> ids = pageCatalogService.findImportedPageIds(siteName);
        assertNotNull(ids);
        assertTrue(ids.isEmpty());
        
        // create imported pages
        int max = 5;
        List<String> createdPageIds = new ArrayList<String>();
        
        for (int i = 1; i <= max; i++)
        {
            String pageName = "page" + i;
            String folderPath = "/importedPages" + "/subfolder" + i;
            String href = "http://www.test.com" + folderPath + "/" + pageName;
            String linkText = pageName;

            PSPage page = addCatalogPage(siteName, href, pageName, folderPath, linkText);
            assertNotNull(page);
            createdPageIds.add(page.getId());
            assertFalse(pageCatalogService.doesImportedPageExist(page));

            pageCatalogService.createImportedPage(page.getId());
            assertTrue(pageCatalogService.doesImportedPageExist(page));
            
            assertNull(addCatalogPage(siteName, href, pageName, folderPath, linkText));
        }

        // find the imported pages
        ids = pageCatalogService.findImportedPageIds(siteName);
        assertNotNull(ids);

        for (int i = 0; i < max; i++)
        {
            assertTrue(ids.contains(createdPageIds.get(i)));

        }
    }
    
    private PSPage addCatalogPage(String siteName, String href, String pageName, String folderPath, String linkText) throws Exception
    {
        PSPage page = pageCatalogService.addCatalogPage(siteName, pageName, linkText, folderPath, href);
        if (page != null)
            fixture.pageCatalogCleaner.add(page.getId()); 
        
        return page;
    }

    private PSPage catalogPage(String siteName, String href, String pageName, String folderPath, String linkText) throws Exception
    {
        PSPage page = addCatalogPage(siteName, href, pageName, folderPath, linkText);
        assertNotNull(page);
        assertEquals(pageName, page.getName());
        assertTrue(page.getFolderPath().endsWith(folderPath));
        assertEquals(linkText, page.getLinkTitle());
        assertEquals(href, page.getDescription());
        return page;
    }

    private void checkSummary(String id, String folderPath, String linkText) throws Exception
    {
        PSCatalogPageSummary sum = pageCatalogService.getCatalogPageSummary(id);
        assertNotNull(sum);
        assertEquals(id, sum.getId());
        assertEquals(linkText, sum.getName());
        assertEquals(folderPath, sum.getPath());        
    }
    
    private void checkTemplate(String siteName) throws PSDataServiceException, PSSiteImportException {
        String templateId = pageCatalogService.getCatalogTemplateIdBySite(siteName);
        assertNotNull(templateId);
    }
    
    private void checkCreateImportedPage(PSPage page) throws Exception
    {
        String pageId = page.getId();
        pageCatalogService.createImportedPage(pageId);
        String expectedFolderPath = "//Sites/PSSiteDataServletTestCaseFixtureSite/folder1";
        String expectedPath = PSFolderPathUtils.concatPath(expectedFolderPath, page.getName());
        
        IPSItemSummary item = folderHelper.findItemById(pageId);
        
        // check the item is not null
        assertNotNull(item);
        
        // check the item exists in the location
        String newPageFolderPath = item.getFolderPaths().get(0);
        String expectedFullPath = PSFolderPathUtils.concatPath(newPageFolderPath, page.getName());
        assertEquals(expectedPath, expectedFullPath);
        assertTrue(PSPathUtils.doesItemExist(expectedFullPath));
    }
    
    private PSPage createAndSavePage(String pageName, String folderPath) throws PSDataServiceException {
        String templateId = fixture.template1.getId();
        String siteFolderPath = fixture.site1.getFolderPath() + folderPath;
        String linkTitle = "TestLink";
        String noindex = "true";
        String description = "This is a page";

        String pageId = createPage(pageName, pageName, templateId, siteFolderPath, linkTitle, 
                noindex, description);
        assertNotNull(pageId);
        
        PSPage page = pageService.findPage(pageName, siteFolderPath);
        assertNotNull(page);

        return page;
    }
    
    private String createPage(String name, String title, String templateId, String folderPath, String linkTitle,
            String noindex, String description) throws PSDataServiceException {
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
    
    
    public void setPageCatalogService(IPSPageCatalogService pageCatalogService)
    {
        this.pageCatalogService = pageCatalogService;
    }
    
    public IPSFolderHelper getFolderHelper()
    {
        return folderHelper;
    }

    public void setFolderHelper(IPSFolderHelper folderHelper)
    {
        this.folderHelper = folderHelper;
    }
    public void setPageService(IPSPageService pageService)
    {
        this.pageService = pageService;
    }
    
    private class PSMockSystemProps extends Properties implements IPSSystemProperties
    {
        public void setMax(String value)
        {
            setProperty(CATALOG_PAGE_MAX, value);
        }
    }
}
