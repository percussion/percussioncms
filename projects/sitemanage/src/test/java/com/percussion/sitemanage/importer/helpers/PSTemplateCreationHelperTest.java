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

import com.percussion.pagemanagement.dao.IPSPageDao;
import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.data.PSTemplateSummary;
import com.percussion.pagemanagement.service.IPSPageService;
import com.percussion.pagemanagement.service.IPSTemplateService;
import com.percussion.pagemanagement.service.impl.PSPageManagementUtils;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.share.IPSSitemanageConstants;
import com.percussion.share.service.IPSDataService.DataServiceLoadException;
import com.percussion.share.service.IPSIdMapper;
import com.percussion.share.spring.PSSpringWebApplicationContextUtils;
import com.percussion.sitemanage.dao.IPSiteDao;
import com.percussion.sitemanage.data.PSSite;
import com.percussion.sitemanage.data.PSSiteImportCtx;
import com.percussion.sitemanage.error.PSSiteImportException;
import com.percussion.sitemanage.importer.IPSSiteImportLogger.PSLogObjectType;
import com.percussion.sitemanage.importer.PSSiteImportLogger;
import com.percussion.sitemanage.importer.helpers.impl.PSTemplateCreationHelper;
import com.percussion.sitemanage.service.IPSSiteTemplateService;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.webservices.security.IPSSecurityWs;

import java.util.ArrayList;
import java.util.List;

import org.apache.cactus.ServletTestCase;
import org.apache.commons.lang.StringUtils;
import org.junit.experimental.categories.Category;

/**
 * @author LucasPiccoli
 * 
 */
@Category(IntegrationTest.class)
public class PSTemplateCreationHelperTest extends ServletTestCase
{

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        PSSpringWebApplicationContextUtils.injectDependencies(this);

        // Login is needed to create folder
        securityWs.login("Admin", "demo", "Default", null);

        templateCreationHelper = new PSTemplateCreationHelper(templateService, pageDao, assemblyService, idMapper,
                siteTemplateService, pageService);
    }

    @Override
    protected void tearDown() throws Exception
    {
        if(StringUtils.isNotEmpty(siteId))
        {
            siteDao.delete(siteId);
            siteId = "";
        }
    }
    
    /**
     * Tests if name is correctly extracted from different URLs. In case of URLs
     * where name cannot be extracted, tests if it's generated correctly.
     */
    public void testNameExtractionFromUrl()
    {
        String testPageName = "pageName";
        String testURL = "";
        String extractedPageName = "";

        testURL = "www.test.com/" + testPageName;
        extractedPageName = templateCreationHelper.extractPageNameFromUrl(testURL);
        assertEquals(testPageName, extractedPageName);

        testURL = "www.test.com";
        extractedPageName = templateCreationHelper.extractPageNameFromUrl(testURL);
        assertEquals("", extractedPageName);

        testURL = "www.test.com/";
        extractedPageName = templateCreationHelper.extractPageNameFromUrl(testURL);
        assertEquals("", extractedPageName);
        
        testURL = "https://www.test.com/";
        extractedPageName = templateCreationHelper.extractPageNameFromUrl(testURL);
        assertEquals("", extractedPageName);

        testURL = "www.test.com/" + testPageName + ".html";
        extractedPageName = templateCreationHelper.extractPageNameFromUrl(testURL);
        assertEquals(testPageName, extractedPageName);

        testURL = "http://www.test.com/blog/sports/";
        extractedPageName = templateCreationHelper.extractPageNameFromUrl(testURL);
        assertEquals("sports", extractedPageName);
        
        testURL = "http://www.test.com/subpath/subpath/" + testPageName + ".html";
        extractedPageName = templateCreationHelper.extractPageNameFromUrl(testURL);
        assertEquals(testPageName, extractedPageName);

        testURL = "http://www.test.com/subpath/subpath/" + testPageName + ".html";
        extractedPageName = templateCreationHelper.extractPageNameFromUrl(testURL);
        assertEquals(testPageName, extractedPageName);

        testURL = "www.test.com/" + testPageName + ".html?urlParam1=value";
        extractedPageName = templateCreationHelper.extractPageNameFromUrl(testURL);
        assertEquals(testPageName, extractedPageName);

        testURL = "www.test.com/" + testPageName + "?urlParam1=value&urlParam2=value";
        extractedPageName = templateCreationHelper.extractPageNameFromUrl(testURL);
        assertEquals(testPageName, extractedPageName);

        testURL = "www.test.com/" + testPageName + ";query";
        extractedPageName = templateCreationHelper.extractPageNameFromUrl(testURL);
        assertEquals(testPageName, extractedPageName);

        testURL = "www.test.com/" + testPageName + ".html?urlParam=val;query";
        extractedPageName = templateCreationHelper.extractPageNameFromUrl(testURL);
        assertEquals(testPageName, extractedPageName);

        testURL = "www.test.com/" + testPageName + "?urlParam=val;query";
        extractedPageName = templateCreationHelper.extractPageNameFromUrl(testURL);
        assertEquals(testPageName, extractedPageName);
    }

    /**
     * Tests if template and page creation methods work. Checks that the created
     * page uses the template.
     */
    public void testTemplateAndPageCreation()
    {
        // Create a site
        PSSite siteData = initSiteData();
        siteData = siteDao.save(siteData);
        siteId = siteData.getId();
        
        PSTemplateSummary newTemplate = null;
        try
        {
            newTemplate = templateService.createNewTemplate(siteData.getBaseTemplateName(), "Test-Template", siteData.getId());
        }
        catch (PSAssemblyException e)
        {
            fail();
        }
        assertNotNull(newTemplate);
        PSPage newPage = templateCreationHelper.createNewPage("Test-Page", newTemplate.getId(), siteData.getFolderPath());
        assertNotNull(newPage);
        assertEquals(newTemplate.getId(), newPage.getTemplateId());
    }

    /**
     * Tests if page and template name generation works correctly. Name
     * generation has to avoid all possible naming collisions with existing
     * templates and pages.
     */
    public void testPageNameGeneration()
    {
        // Create a site
        PSSite siteData = initSiteData();
        siteData = siteDao.save(siteData);
        // Mark site for deletion on teardown
        siteId = siteData.getId();
        
        String genericNewTemplateName = PSPageManagementUtils.TEMPLATE_NAME;
        String genericNewPageName = PSPageManagementUtils.PAGE_NAME;
        
        String templateName;
        // Template doesn't exist
        templateName = siteTemplateService.generateNewTemplateName(genericNewTemplateName, siteData.getId());
        assertEquals("Template", templateName);

        //Create a template
        PSTemplateSummary newTemplate = null;
        try
        {
            newTemplate = templateService.createNewTemplate(siteData.getBaseTemplateName(), templateName, siteData.getId());
        }
        catch (PSAssemblyException e)
        {
            fail();
        }

        templateName = siteTemplateService.generateNewTemplateName(genericNewTemplateName, siteData.getId());
        assertEquals("Template-1", templateName);

        String pageName;
        // Page doesn't exist
        pageName = pageService.generateNewPageName(genericNewPageName, siteData.getFolderPath());
        assertEquals("Page", pageName);
        // create a page in the root of the site.
        templateCreationHelper.createNewPage(pageName, newTemplate.getId(), siteData.getFolderPath());
        pageName = pageService.generateNewPageName(genericNewPageName, siteData.getFolderPath());
        assertEquals("Page-1", pageName);
        templateCreationHelper.createNewPage(pageName, newTemplate.getId(), siteData.getFolderPath());
        pageName = pageService.generateNewPageName(genericNewPageName, siteData.getFolderPath());
        assertEquals("Page-2", pageName);
    }
    
    /**
     * Tests main process method of templateCreationHelper is executed
     * succesfully and context object is correctly updated afterwards. Tests if
     * rollback method actually deletes everything the helper has created.
     */
    public void testHelperProcessAndRollback()
    {
        try
        {
            PSSite siteData = initSiteData();
            siteData = siteDao.save(siteData);
            siteId = siteData.getId();

            PSSiteImportCtx context = new PSSiteImportCtx();
            context.setSite(siteData);

            context.setLogger(new PSSiteImportLogger(PSLogObjectType.TEMPLATE));

            templateCreationHelper.process(null, context);
            
            // Test if context was updated with created template and page data.
            assertNotNull(context.getTemplateId());
            assertNotNull(context.getPageName());
            assertNotNull(context.getTemplateName());

            // Test if the logger was created and the log messages were
            // appended.
            assertNotNull(context.getLogger());
            assertNotNull(context.getLogger().getLog());
            assertTrue(context.getLogger().getLog().indexOf(PSTemplateCreationHelper.LOG_ENTRY_PREFIX) != -1);

            try
            {
                // Test if page and template exist
                PSTemplateSummary templateSummary = templateService.find(context.getTemplateId());
                assertEquals(context.getTemplateId(), templateSummary.getId());
                assertEquals(IPSSitemanageConstants.PLAIN_BASE_TEMPLATE_NAME, templateSummary.getSourceTemplateName());
            }
            catch (DataServiceLoadException e)
            {
                fail();
            }

            PSPage page = pageService.findPage(context.getPageName(), siteData.getFolderPath());
            assertNotNull(page);
            assertEquals(context.getTemplateId(), page.getTemplateId());

            templateCreationHelper.rollback(null, context);

            // Check that the page and the template doesn't exist.
            List<PSTemplateSummary> siteTemplates = siteTemplateService.findTemplatesBySite(context.getSite().getId());
            List<String> templateNames = new ArrayList<String>();
            for (PSTemplateSummary template : siteTemplates)
            {
                templateNames.add(template.getName());
            }
            assertFalse(templateNames.contains(context.getTemplateName()));
               
            try
            {
                page = pageService.findPage(context.getPageName(), siteData.getFolderPath());
                assertNull(page);                
            }
            catch(RuntimeException e)
            {
                //Test passes, since findPage method throws an exception when not able to resolve the path.
            }
        }
        catch (PSSiteImportException e)
        {
            fail();
        }
        catch (RuntimeException e)
        {
            fail();
        }
    }
    
    private PSSite initSiteData()
    {
        PSSite newSite = new PSSite();
        newSite.setBaseUrl("http://www.percussion.com/");
        newSite.setName(SITE_NAME);
        newSite.setBaseTemplateName(IPSSitemanageConstants.PLAIN_BASE_TEMPLATE_NAME);
        newSite.setTemplateName(SITE_TEMPLATE_NAME);
        newSite.setNavigationTitle(SITE_NAME);
        newSite.setHomePageTitle("Home");
        return newSite;
    }

    /**
     * @return the templateService
     */
    public IPSTemplateService getTemplateService()
    {
        return templateService;
    }

    /**
     * @param templateService the templateService to set
     */
    public void setTemplateService(IPSTemplateService templateService)
    {
        this.templateService = templateService;
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
     * @return the assemblyService
     */
    public IPSAssemblyService getAssemblyService()
    {
        return assemblyService;
    }

    /**
     * @param assemblyService the assemblyService to set
     */
    public void setAssemblyService(IPSAssemblyService assemblyService)
    {
        this.assemblyService = assemblyService;
    }

    /**
     * @return the idMapper
     */
    public IPSIdMapper getIdMapper()
    {
        return idMapper;
    }

    /**
     * @param idMapper the idMapper to set
     */
    public void setIdMapper(IPSIdMapper idMapper)
    {
        this.idMapper = idMapper;
    }

    /**
     * @return the pageDao
     */
    public IPSPageDao getPageDao()
    {
        return pageDao;
    }

    /**
     * @param pageDao the pageDao to set
     */
    public void setPageDao(IPSPageDao pageDao)
    {
        this.pageDao = pageDao;
    }
    
    /**
     * @return the siteTemplateService
     */
    public IPSSiteTemplateService getSiteTemplateService()
    {
        return siteTemplateService;
    }

    /**
     * @param siteTemplateService the siteTemplateService to set
     */
    public void setSiteTemplateService(IPSSiteTemplateService siteTemplateService)
    {
        this.siteTemplateService = siteTemplateService;
    }

    private final String SITE_NAME = "PercussionTest";
    
    private final String SITE_TEMPLATE_NAME = "PercussionTestTemplate";
    
    private String siteId;

    private PSTemplateCreationHelper templateCreationHelper;

    private IPSTemplateService templateService;

    private IPSPageService pageService;

    private IPSSecurityWs securityWs;
    
    private IPSiteDao siteDao;
    
    private IPSPageDao pageDao;
    
    private IPSAssemblyService assemblyService;
    
    private IPSIdMapper idMapper;
    
    private IPSSiteTemplateService siteTemplateService;

}
