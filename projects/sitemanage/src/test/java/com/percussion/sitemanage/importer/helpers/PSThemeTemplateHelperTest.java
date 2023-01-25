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
