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
package com.percussion.sitemanage.importer;

import static com.percussion.sitemanage.importer.helpers.PSHelperTestUtils.USER_AGENT;

import com.percussion.pagemanagement.dao.IPSPageDao;
import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.service.IPSPageCatalogService;
import com.percussion.pagemanagement.service.IPSPageService;
import com.percussion.pagemanagement.service.PSSiteDataServletTestCaseFixture;
import com.percussion.pagemanagement.service.impl.PSPageCatalogService;
import com.percussion.pathmanagement.service.impl.PSPathUtils;
import com.percussion.share.dao.IPSFolderHelper;
import com.percussion.share.dao.PSFolderPathUtils;
import com.percussion.share.data.IPSItemSummary;
import com.percussion.share.service.IPSSystemProperties;
import com.percussion.sitemanage.dao.IPSiteDao;
import com.percussion.sitemanage.data.PSSite;
import com.percussion.sitemanage.data.PSSiteImportCtx;
import com.percussion.sitemanage.error.PSSiteImportException;
import com.percussion.sitemanage.importer.helpers.impl.PSImportHelper;
import com.percussion.sitemanage.importer.helpers.impl.PSSiteCreationHelper;
import com.percussion.sitemanage.service.IPSSiteImportService;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.webservices.security.IPSSecurityWs;

import java.util.List;
import java.util.Properties;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * @author LucasPiccoli
 * 
 */
@Category(IntegrationTest.class)
public class PSSiteImportServiceTest extends PSSiteImportTestBase
{

    /**
     * Placeholder test to keep junit happy, all other tests ignored as tech debt
     */
    @Test
    public void testNothing()
    {
        
    }    

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        
        // Inject dependencies
        setSecurityWs((IPSSecurityWs) getBean("sys_securityWs"));
        setSiteImportService((PSSiteImportService) getBean("siteImportService"));
        setSiteDao((IPSiteDao) getBean("siteDao"));
        setPageService((IPSPageService) getBean("pageService"));
        setPageDao((IPSPageDao) getBean("pageDao"));
        setTemplateImportService((IPSSiteImportService) getBean("templateImportService"));
        setPageImportService((IPSSiteImportService) getBean("pageImportService"));
        setPageCatalogService((IPSPageCatalogService) getBean("pageCatalogService"));
        setFolderHelper((IPSFolderHelper) getBean("folderHelper"));
        
        
        
        // Login is needed to create folder for the new site.
        securityWs.login("Admin", "demo", "Default", null);

        initData();
        
        fixture = new PSSiteDataServletTestCaseFixture(request, response);
        fixture.setUp();
        fixtureCreated = true;
        
    }

    /**
     * Tests if the service can gather all the helpers from the context.
     */
    @Ignore
    public void ignore_testHelperAvailability()
    {
        assertNotNull(siteImportService);
        List<PSImportHelper> mandatoryHelpers = siteImportService.getMandatoryHelpers();
        assertNotNull(mandatoryHelpers);
        assertTrue(!mandatoryHelpers.isEmpty());
        List<PSImportHelper> optionalHelpers = siteImportService.getOptionalHelpers();
        assertNotNull(optionalHelpers);
        assertTrue(!optionalHelpers.isEmpty());
    }

    /**
     * Tests if the whole process of importing a site is successful.
     */
    @Ignore
    public void ignore_testImportSite()
    {
        importedSite = new PSSite();
        importedSite.setName("Test");
        importedSite.setBaseUrl(IMPORT_SITE_URL);
        try
        {
            importedSite = siteImportService.importSiteFromUrl(importedSite, USER_AGENT).getSite();
            siteCreated = true;
        }
        catch (RuntimeException | PSSiteImportException e)
        {
            fail();
        }
    }

    /**
     * Test if the correct helpers configured for templateImportService bean are
     * executed correctly and without errors.
     * @throws Exception 
     * 
     */
    @Ignore
    public void ignore_testImportTemplateFromUrl() throws Exception
    {
        fixture.site1.setBaseUrl(IMPORT_SITE_URL);
        try
        {
            templateImportService.importSiteFromUrl((PSSite) fixture.site1, USER_AGENT);
        }
        catch (RuntimeException e)
        {
            fail();
        }
    }
    
    @Ignore
    public void ignore_testImportPage() throws Exception
    {
        PSPage page1 = addCatalogedPage();
        
        fixture.site1.setBaseUrl(IMPORT_SITE_URL);
        try
        {
            PSSiteImportCtx context = new PSSiteImportCtx();
            pageImportService.importCatalogedPage((PSSite) fixture.site1, page1.getId(), USER_AGENT, context);
            
            checkCreateImportedPage(page1);
        }
        catch (Exception e)
        {
            fail();
        }
    }

    /**
     * Test hardness to import one or more specified web site.
     * @throws Exception
     */
    public void runme_importSite() throws Exception
    {
        importSite("samples.percussion.com." + System.currentTimeMillis(), "samples.percussion.com");
    }
    
    /**
     * Test hardness to create a site and import the cataloged pages identified from the home page.
     */
    private void importSite(String name, String url) throws Exception
    {
        importedSite = new PSSite();
        importedSite.setName(name);
        importedSite.setBaseUrl(url);
        try
        {
            importedSite = siteImportService.importSiteFromUrl(importedSite, USER_AGENT).getSite();
            siteCreated = true;
        }
        catch (RuntimeException e)
        {
            fail();
        }
        
        importCatagedPagesForSite(importedSite);
    }

    private void importCatagedPagesForSite(PSSite site) throws Exception
    {
        List<String> pageIds = pageCatalogService.findCatalogPages(site.getName());
        for (String id : pageIds)
        {
            importPage(site, id);
        }
    }
    
    private void importPage(PSSite site, String pageId)
    {
        try
        {
            PSSiteImportCtx context = new PSSiteImportCtx();
            pageImportService.importCatalogedPage(site, pageId, USER_AGENT, context);
        }
        catch (Exception e)
        {
            fail();
        }
    }

    @Override
    protected void tearDown() throws Exception
    {
        if (siteCreated)
        {
            deleteSite();
            siteCreated = false;
        }
        if (fixtureCreated)
        {
            fixture.tearDown();
            fixtureCreated = false;
        }
        
        ((PSPageCatalogService) pageCatalogService).setSystemProps(systemProperties);
        super.tearDown();
    }

    private void initData() throws Exception
    {
        createHelpers();
    }

    private void deleteSite()
    {
        if (importedSite != null)
        {
            PSSiteImportCtx importCtx = new PSSiteImportCtx();
            importCtx.setSite(importedSite);
            siteCreationHelper.rollback(null, importCtx);
            importedSite = null;
        }
    }

    private void createHelpers()
    {
        siteCreationHelper = new PSSiteCreationHelper(siteDao, pageService);
    }

    private PSPage addCatalogedPage() throws Exception
    {
        increaseCatalogLimit(1);
        
        PSPage catalogedPage = pageCatalogService.addCatalogPage(fixture.site1.getName(), CATALOGED_PAGE_NAME, CATALOGED_PAGE_NAME, 
                PSFolderPathUtils.concatPath(CATALOGED_PAGE_FOLDER, CATALOGED_PAGE_NAME), 
                CATALOGED_PAGE_URL);
        
        increaseCatalogLimit(0);
        
        if (catalogedPage != null)
            fixture.pageCatalogCleaner.add(catalogedPage.getId());
        
        return catalogedPage;
    }

    private void increaseCatalogLimit(int max)
    {
        PSPageCatalogService svcImpl = (PSPageCatalogService)pageCatalogService;
        PSMockSystemProps props = new PSMockSystemProps();
        props.setMax(String.valueOf(max));
        svcImpl.setSystemProps(props);
    }
    
    private void checkCreateImportedPage(PSPage page) throws Exception
    {
        String pageId = page.getId();
        String expectedFolderPath = "//Sites/PSSiteDataServletTestCaseFixtureSite/folder/CatalogedPage";
        String expectedPath = PSFolderPathUtils.concatPath(expectedFolderPath, page.getName());
        
        IPSItemSummary item = folderHelper.findItemById(pageId);
        
        // check the item is not null
        assertNotNull(item);
    }

    /**
     * @return the securityWs
     */
    public IPSSecurityWs getSecurityWs()
    {
        return securityWs;
    }

    /**
     * @param securityWs the securityWs to set
     */
    public void setSecurityWs(IPSSecurityWs securityWs)
    {
        this.securityWs = securityWs;
    }

    /**
     * @return the siteImportService
     */
    public PSSiteImportService getSiteImportService()
    {
        return siteImportService;
    }

    /**
     * @param siteImportService the siteImportService to set
     */
    public void setSiteImportService(PSSiteImportService siteImportService)
    {
        this.siteImportService = siteImportService;
    }

    /**
     * @return the siteDao
     */
    public IPSiteDao getSiteDao()
    {
        return siteDao;
    }

    /**
     * @param siteDao the siteDao to set
     */
    public void setSiteDao(IPSiteDao siteDao)
    {
        this.siteDao = siteDao;
    }

    /**
     * @return the pageService
     */
    public IPSPageService getPageService()
    {
        return pageService;
    }

    /**
     * @param pageService the pageService to set
     */
    public void setPageService(IPSPageService pageService)
    {
        this.pageService = pageService;
    }

    /** 
     * @return the pageDao
     */
    public IPSPageDao getPageDao()
    {
        return this.pageDao;
    }
    
    /**
     * @param pageDao the pageDao to set
     */
    public void setPageDao(IPSPageDao pageDao)
    {
        this.pageDao = pageDao;
    }

    /**
     * @return the templateImportService
     */
    public IPSSiteImportService getTemplateImportService()
    {
        return templateImportService;
    }

    /**
     * @param templateImportService the templateImportService to set
     */
    public void setTemplateImportService(IPSSiteImportService templateImportService)
    {
        this.templateImportService = templateImportService;
    }
    
    /**
     * @return the pageImportService
     */
    public IPSSiteImportService getPageImportService()
    {
        return pageImportService;
    }
    
    /**
     * @param pageImportService the pageImportService to set
     */
    public void setPageImportService(IPSSiteImportService pageImportService)
    {
        this.pageImportService = pageImportService;
    }
    
    /**
     * @return the pageCatalogService
     */
    public IPSPageCatalogService getPageCatalogService()
    {
        return this.pageCatalogService;
    }
    
    /**
     * @param pageCatalogService the pageCatalogService to set
     */
    public void setPageCatalogService(IPSPageCatalogService pageCatalogService)
    {
        this.pageCatalogService = pageCatalogService;
        
        systemProperties = ((PSPageCatalogService) pageCatalogService).getSystemProps();
    }
    
    /**
     * @return the folderHelper
     */
    public IPSFolderHelper getFolderHelper()
    {
        return folderHelper;
    }

    /**
     * @param folderHelper the folderHelper to set
     */
    public void setFolderHelper(IPSFolderHelper folderHelper)
    {
        this.folderHelper = folderHelper;
    }

    private class PSMockSystemProps extends Properties implements IPSSystemProperties
    {
        public void setMax(String value)
        {
            setProperty(CATALOG_PAGE_MAX, value);
        }
    }
    
    private PSSiteDataServletTestCaseFixture fixture;

    private PSSiteCreationHelper siteCreationHelper;

    private PSSite importedSite;

    private IPSSecurityWs securityWs;

    private PSSiteImportService siteImportService;

    private IPSiteDao siteDao;

    private IPSPageService pageService;
    
    private IPSPageDao pageDao;

    private IPSSiteImportService templateImportService;
    
    private IPSSiteImportService pageImportService;
    
    private IPSPageCatalogService pageCatalogService;
    
    private IPSSystemProperties systemProperties;
    
    private IPSFolderHelper folderHelper;
    
    private boolean siteCreated = false;
    
    private boolean fixtureCreated = false;
    
    private final String CATALOGED_PAGE_NAME = "CatalogedPage";
    
    private final String CATALOGED_PAGE_FOLDER = "/folder";
    
    private final String CATALOGED_PAGE_URL = "http://samples.percussion.com/products/index.html";
    
    private static final String IMPORT_SITE_URL = "http://samples.percussion.com/products/index.html";
}
