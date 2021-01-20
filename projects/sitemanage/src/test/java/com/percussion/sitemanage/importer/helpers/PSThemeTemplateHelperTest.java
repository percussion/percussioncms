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
package com.percussion.sitemanage.importer.helpers;

import com.percussion.itemmanagement.service.IPSItemWorkflowService;
import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.data.PSTemplate;
import com.percussion.pagemanagement.service.IPSPageService;
import com.percussion.pagemanagement.service.IPSTemplateService;
import com.percussion.pagemanagement.service.PSSiteDataServletTestCaseFixture;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.share.spring.PSSpringWebApplicationContextUtils;
import com.percussion.sitemanage.dao.impl.PSSiteContentDao;
import com.percussion.sitemanage.data.PSPageContent;
import com.percussion.sitemanage.data.PSSite;
import com.percussion.sitemanage.data.PSSiteImportCtx;
import com.percussion.sitemanage.importer.IPSSiteImportLogger;
import com.percussion.sitemanage.importer.IPSSiteImportLogger.PSLogObjectType;
import com.percussion.sitemanage.importer.PSSiteImportLogger;
import com.percussion.sitemanage.importer.helpers.impl.PSThemeTemplateHelper;
import com.percussion.test.PSServletTestCase;
import com.percussion.theme.service.IPSThemeService;
import com.percussion.utils.testing.IntegrationTest;
import org.junit.experimental.categories.Category;

/**
 * @author federicoromanelli
 *
 */
@Category(IntegrationTest.class)
public class PSThemeTemplateHelperTest extends PSServletTestCase
{
    IPSSiteImportLogger logger = new PSSiteImportLogger(PSLogObjectType.SITE);
    
    private PSSiteDataServletTestCaseFixture fixture;
    private String themeName = "";  
    private String renamedLandingPage = "renamedIndex";
    
    // Services
    private IPSThemeService themeService;
    private IPSTemplateService templateService;
    private PSThemeTemplateHelper themeTemplateHelper;
    private IPSSiteManager siteMgr;
    private IPSPageService pageService;
    private IPSItemWorkflowService itemWorkflowService;
    
    public void setUp() throws Exception
    {
        PSSpringWebApplicationContextUtils.injectDependencies(this);
        fixture = new PSSiteDataServletTestCaseFixture(request, response);
        fixture.setUp();
        fixture.pageCleaner.add(fixture.site1.getFolderPath() + "/Page1");
        
        PSTemplate templateObj = templateService.load(fixture.baseTemplate.getId());
        // Get the name of the theme for the created base template to compare later 
        themeName = templateObj.getTheme();
        //FB:IJU_SETUP_NO_SUPER NC 1-16-16
        super.setUp();
    }

    public void tearDown() throws Exception
    {
        //fixture.siteCleaner.clean();
        fixture.tearDown();
    }
    
    public void testProcess() throws Exception
    {
        // Create basic context objects
        PSPageContent pageContent = new PSPageContent();
        PSSiteImportCtx importContext = new PSSiteImportCtx();
        
        PSSite site = new PSSite();
        site.setName(fixture.site1.getName());
        site.setSiteId(fixture.site1.getSiteId());
        
        importContext.setSite(site);
        importContext.setLogger(logger);
        
        themeTemplateHelper.process(pageContent, importContext);
        // test the themeSummary object returned by the helper and name of the new theme
        assertEquals(importContext.getThemeSummary().getName(), themeName);
                
        // Change the name for index page and make sure the system is still able to process the page
        PSPage landingPage = pageService.findPage(PSSiteContentDao.HOME_PAGE_NAME, fixture.site1.getFolderPath());

        if (landingPage != null)
        {
            itemWorkflowService.checkOut(landingPage.getId());
            landingPage.setName(renamedLandingPage);
            pageService.save(landingPage);
        }
        
        landingPage = pageService.findPage(renamedLandingPage, fixture.site1.getFolderPath());
        if (landingPage != null)
        {
            assertEquals(landingPage.getName(), renamedLandingPage);
        }
        
        themeTemplateHelper.process(pageContent, importContext);
        // test the themeSummary object returned by the helper and name of the new theme
        assertEquals(importContext.getThemeSummary().getName(), themeName);
    }

    public IPSThemeService getThemeService()
    {
        return themeService;
    }

    public void setThemeService(IPSThemeService themeService)
    {
        this.themeService = themeService;
    }

    public IPSTemplateService getTemplateService()
    {
        return templateService;
    }

    public void setTemplateService(IPSTemplateService templateService)
    {
        this.templateService = templateService;
    }

    public PSThemeTemplateHelper getThemeTemplateHelper()
    {
        return themeTemplateHelper;
    }

    public void setThemeTemplateHelper(PSThemeTemplateHelper themeTemplateHelper)
    {
        this.themeTemplateHelper = themeTemplateHelper;
    }

    public IPSSiteManager getSiteMgr()
    {
        return siteMgr;
    }

    public void setSiteMgr(IPSSiteManager siteMgr)
    {
        this.siteMgr = siteMgr;
    }
    
    public IPSPageService getPageService()
    {
        return pageService;
    }
    
    public void setPageService(IPSPageService pageService)
    {
        this.pageService = pageService;
    }
        
    public IPSItemWorkflowService getItemWorkflowService()
    {
        return itemWorkflowService;
    }

    public void setItemWorkflowService(IPSItemWorkflowService itemWorkflowService)
    {
        this.itemWorkflowService = itemWorkflowService;
    }

}
