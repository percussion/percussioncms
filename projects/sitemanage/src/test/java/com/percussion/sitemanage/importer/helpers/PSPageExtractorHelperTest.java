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

import static com.percussion.share.spring.PSSpringWebApplicationContextUtils.getWebApplicationContext;

import static org.springframework.util.StringUtils.countOccurrencesOf;

import com.percussion.assetmanagement.data.PSAsset;
import com.percussion.assetmanagement.data.PSAssetSummary;
import com.percussion.assetmanagement.service.IPSAssetService;
import com.percussion.assetmanagement.service.IPSWidgetAssetRelationshipService;
import com.percussion.itemmanagement.service.IPSItemWorkflowService;
import com.percussion.pagemanagement.dao.IPSPageDao;
import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.data.PSTemplate;
import com.percussion.pagemanagement.service.IPSPageCatalogService;
import com.percussion.pagemanagement.service.IPSPageService;
import com.percussion.pagemanagement.service.IPSTemplateService;
import com.percussion.pagemanagement.service.PSSiteDataServletTestCaseFixture;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.share.service.IPSIdMapper;
import com.percussion.share.service.IPSNameGenerator;
import com.percussion.share.spring.PSSpringWebApplicationContextUtils;
import com.percussion.sitemanage.dao.IPSiteDao;
import com.percussion.sitemanage.dao.impl.PSSiteContentDao;
import com.percussion.sitemanage.data.PSPageContent;
import com.percussion.sitemanage.data.PSSite;
import com.percussion.sitemanage.data.PSSiteImportCtx;
import com.percussion.sitemanage.importer.IPSSiteImportLogger;
import com.percussion.sitemanage.importer.IPSSiteImportLogger.PSLogEntryType;
import com.percussion.sitemanage.importer.IPSSiteImportLogger.PSLogObjectType;
import com.percussion.sitemanage.importer.PSSiteImportLogger;
import com.percussion.sitemanage.importer.PSSiteImporter;
import com.percussion.sitemanage.importer.helpers.impl.PSImportHelper;
import com.percussion.sitemanage.importer.helpers.impl.PSPageExtractorHelper;
import com.percussion.sitemanage.importer.helpers.impl.PSSiteCreationHelper;
import com.percussion.sitemanage.importer.helpers.impl.PSTemplateCreationHelper;
import com.percussion.sitemanage.importer.helpers.impl.PSTemplateExtractorHelper;
import com.percussion.sitemanage.service.IPSSiteTemplateService;
import com.percussion.sitesummaryservice.service.IPSSiteImportSummaryService;
import com.percussion.theme.data.PSThemeSummary;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.webservices.security.IPSSecurityWs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

import org.apache.cactus.ServletTestCase;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runners.MethodSorters;

/**
 * @author LucasPiccoli
 *
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Category(IntegrationTest.class)
public class PSPageExtractorHelperTest extends ServletTestCase
{

  

    @Override
    protected void setUp() throws Exception
    {
        PSSpringWebApplicationContextUtils.injectDependencies(this);
        super.setUp();

        // Login is needed to create folder for the new site.
        securityWs.login("Admin", "demo", "Default", null);

        createHelpers();
        initData();
    }
    
    @Test
    public void test010AddBodyContentToPageWhenImportingSite()
    {
        PSSite site = new PSSite();
        site.setBaseUrl(TEST_SITE_URL);
        site.setName(TEST_SITE_NAME);
        importContext.setSite(site);
        
        //Process previous necessary helpers first, so that site is first imported and template already created.
        siteCreationHelper.process(pageContent, importContext);
        siteCreationHelperExecuted = true;
        templateExtractorHelper.process(pageContent, importContext);
        
        //Process page extractor helper
        pageExtractorHelper.process(pageContent, importContext);

        //Test if page and template are found for the imported site.
        PSPage homePage = pageService
                .findPage(PSSiteContentDao.HOME_PAGE_NAME, importContext.getSite().getFolderPath());
        assertNotNull(homePage);
        PSTemplate template = templateService.load(homePage.getTemplateId());
        assertNotNull(template);
        
        //Test if local content was added to page
        Set<String> assetIds = widgetAssetRelationshipService.getLocalAssets(homePage.getId());
        assertFalse(assetIds.isEmpty());
        String assetId = assetIds.iterator().next();
        PSAssetSummary assetSummary = assetService.find(assetId);
        assertEquals("percRawHtmlAsset", assetSummary.getType());  
        
        //Test if body was correctly inserted into local asset
        PSAsset asset = assetService.load(assetSummary.getId());
        assertTrue(asset.getFields().containsKey("html"));
        String bodyContent = (String)asset.getFields().get("html");
        assertNotNull(bodyContent);
        assertEquals(normalizeHtml(pageContent.getBodyContent()), normalizeHtml(bodyContent));

        // validate the commented out tags
        validateJSReferencesTags(bodyContent);
        validateLogsForJSTags(importContext.getLogger());
    }
    
    @Test
    public void test020AddBodyContentToPageWhenImportingTemplate() throws Exception
    {
        createFixture();
        importContext.setSite((PSSite) fixture.site1);

        // Process previous necessary helpers first, so that site is first
        // imported and template already created.
        templateCreationHelper.process(pageContent, importContext);
        templateCreationHelperExecuted = true;
        templateExtractorHelper.process(pageContent, importContext);

        // Process page extractor helper
        pageExtractorHelper.process(pageContent, importContext);

        // Test if page and template are found for the imported site.
        PSPage homePage = pageService.findPage(importContext.getPageName(), importContext.getSite().getFolderPath());
        assertNotNull(homePage);
        PSTemplate template = templateService.load(importContext.getTemplateId());
        assertNotNull(template);

        // Test if local content was added to page
        Set<String> assetIds = widgetAssetRelationshipService.getLocalAssets(homePage.getId());
        assertFalse(assetIds.isEmpty());
        String assetId = assetIds.iterator().next();
        PSAssetSummary assetSummary = assetService.find(assetId);
        assertEquals("percRawHtmlAsset", assetSummary.getType());

        // Test if body was correctly inserted into local asset
        PSAsset asset = assetService.load(assetSummary.getId());
        assertTrue(asset.getFields().containsKey("html"));
        String bodyContent = (String) asset.getFields().get("html");
        assertNotNull(bodyContent);
        assertEquals(normalizeHtml(pageContent.getBodyContent()), normalizeHtml(bodyContent));
        
        // validate the commented out tags
        validateJSReferencesTags(bodyContent);
        validateLogsForJSTags(importContext.getLogger());
    }

    /**
     * Normalized HTML content for comparison purposes
     * 
     * @param html
     * 
     * @return The normalized html
     */
    private String normalizeHtml(String html)
    {
        Document doc = Jsoup.parseBodyFragment(html);
        return doc.body().html();
    }

    private void createHelpers()
    {
        siteCreationHelper = new PSSiteCreationHelper(siteDao, pageService);
        templateCreationHelper = new PSTemplateCreationHelper(templateService, pageDao, assemblyService, idMapper, siteTemplateService, pageService);
        templateExtractorHelper = new PSTemplateExtractorHelper(templateService);
        pageExtractorHelper = new PSPageExtractorHelper(pageService, assetService, itemWorkflowService,
                templateService, nameGenerator, idMapper);
        pageExtractorHelper.setRunSaveSyncronously(true);
    }
    
    private void initData() throws Exception
    {
        importContext = new PSSiteImportCtx();
        importContext.setLogger(new PSSiteImportLogger(PSLogObjectType.SITE));
        IPSSiteImportSummaryService summaryService = (IPSSiteImportSummaryService) getWebApplicationContext().getBean("siteImportSummaryService");
        importContext.setSummaryService(summaryService);
        
        // create initial content
        File pageSampleFile = createTempConfigFileBasedOn(getClass().getResourceAsStream("CM1094-SamplePage.html"));
        Document doc = Jsoup.parse(pageSampleFile, "UTF-8");
        pageContent = PSSiteImporter.createPageContent(doc, importContext.getLogger());

        PSThemeSummary themeSummary = new PSThemeSummary();
        themeSummary.setName(TEST_THEME_NAME);
        importContext.setThemeSummary(themeSummary);
    }

    private File createTempConfigFileBasedOn(InputStream baseConfigFile) throws Exception
    {
        // Copy mixed passwords to temp directory
        File tempConfigFile = File.createTempFile("samplePage", ".html");
        OutputStream out = new FileOutputStream(tempConfigFile);
        InputStream in = baseConfigFile;
        
        IOUtils.copy(in, out);
        
        return tempConfigFile;
    }
    
    private void deleteSite()
    {
        siteCreationHelper.rollback(pageContent, importContext);
    }

    private void createFixture() throws Exception
    {
        fixture = new PSSiteDataServletTestCaseFixture(request, response);
        fixture.setUp();
        fixture.pageCleaner.add(fixture.site1.getFolderPath() + "/Page1");
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        if(siteCreationHelperExecuted)
        {
            deleteSite();  
            siteCreationHelperExecuted = false;
        }
        if(templateCreationHelperExecuted)
        {
            fixture.tearDown();
            templateCreationHelperExecuted = false;
        }
    }    
    
    public void setSecurityWs(IPSSecurityWs securityWs)
    {
        this.securityWs = securityWs;
    }
    
    public void setPageService(IPSPageService pageService)
    {
        this.pageService = pageService;
    }
    
    public void setTemplateService(IPSTemplateService templateService)
    {
        this.templateService = templateService;
    }

    public void setSiteDao(IPSiteDao siteDao)
    {
        this.siteDao = siteDao;
    }
    
    public void setWidgetAssetRelationshipService(IPSWidgetAssetRelationshipService widgetAssetRelationshipService)
    {
        this.widgetAssetRelationshipService = widgetAssetRelationshipService;
    }
    
    public void setAssetService(IPSAssetService assetService)
    {
        this.assetService = assetService;
    }

    public IPSPageCatalogService getPageCatalogService()
    {
        return pageCatalogService;
    }

    public void setPageCatalogService(IPSPageCatalogService pageCatalogService)
    {
        this.pageCatalogService = pageCatalogService;
    }

    public void setItemWorkflowService(IPSItemWorkflowService itemWorkflowService)
    {
        this.itemWorkflowService = itemWorkflowService;
    }

    public void setNameGenerator(IPSNameGenerator nameGenerator)
    {
        this.nameGenerator = nameGenerator;
    }

    /**
     * @return the templateCreationHelper
     */
    public PSTemplateCreationHelper getTemplateCreationHelper()
    {
        return templateCreationHelper;
    }

    /**
     * @param templateCreationHelper the templateCreationHelper to set
     */
    public void setTemplateCreationHelper(PSTemplateCreationHelper templateCreationHelper)
    {
        this.templateCreationHelper = templateCreationHelper;
    }
    
    /**
     * @return the siteDao
     */
    public IPSiteDao getSiteDao()
    {
        return siteDao;
    }

    /**
     * @param pageDao the pageDao to set
     */
    public void setPageDao(IPSPageDao pageDao)
    {
        this.pageDao = pageDao;
    }

    /**
     * @param assemblyService the assemblyService to set
     */
    public void setAssemblyService(IPSAssemblyService assemblyService)
    {
        this.assemblyService = assemblyService;
    }

    /**
     * @param idMapper the idMapper to set
     */
    public void setIdMapper(IPSIdMapper idMapper)
    {
        this.idMapper = idMapper;
    }

    /**
     * @param siteTemplateService the siteTemplateService to set
     */
    public void setSiteTemplateService(IPSSiteTemplateService siteTemplateService)
    {
        this.siteTemplateService = siteTemplateService;
    }

    /**
     * Verifies that the correct amount of elements has been commented out.
     * 
     * @param logger {@link IPSSiteImportLogger} to use for the tests. Assumed
     *            not <code>null</code>.
     */
    private void validateLogsForJSTags(IPSSiteImportLogger logger)
    {
        String log = logger.getLog(); 
        
        for (String percussionTag : PERCUSSION_TAGS)
        {
            String logLine = buildLogLineForCommentedJSElement(percussionTag);
            assertTrue("The LOG should contain the following line:  '" + logLine + "'.",
                    log.contains(logLine));
        }
        
        for (String percussionTag : NOT_MANAGED_TAGS)
        {
            String logLine = buildLogLineForCommentedJSElement(percussionTag);
            assertFalse("The LOG should not contain the following line:  '" + logLine + "'.",
                    log.contains(logLine));
        }

        /*
         * As the managed js are also in the "after body start" and
         * "before body close" content, in the body we will have 3 times each
         * tag commented.
         */
        int lines = countOccurrencesOf(log, PSImportHelper.COMMENTED_JS_REFERENCE_FROM_BODY);
        assertTrue("The log entries for commented tags should have been " + PERCUSSION_TAGS.length + ", but they were "
                + lines, lines == PERCUSSION_TAGS.length * 3);
    }

    /**
     * Builds a line equals to the ones that appear in the loggind data, for
     * those commented js referenced tags. An example:
     * 
     * <pre>
     * STATUS: Commented out managed jquery reference from &lt;body&gt; element: &lt;script src="jquery.js" type="text/javascript"&gt; &lt;/script&gt; from &lt;body&gt; element.
     * </pre>
     * 
     * @param tag {@link String} with the whole tag to build the line. Assumed
     *            not <code>null</code> nor empty.
     * @return {@link String}, never <code>null</code> or empty.
     */
    private String buildLogLineForCommentedJSElement(String percussionTag)
    {
        // Example: STATUS: Commented out managed element: <title>Home</title>
        String logLine = PSLogEntryType.STATUS.name();
        logLine += ": ";
        logLine += PSImportHelper.COMMENTED_JS_REFERENCE_FROM_BODY;
        logLine += ": ";
        logLine += percussionTag;        
        return logLine;
    }

    /**
     * Checks that the js references that are generated by CM1 gets commented on the
     * given additional head content.
     * 
     * @param bodyContent {@link String} with the html asset
     *            content. Assumed not <code>null</code>.
     */
    private void validateJSReferencesTags(String bodyContent)
    {
        // affirmative cases
        for (String percussionTag : PERCUSSION_TAGS)
        {
            assertTrue("The tag '" + percussionTag + "' should have been commented.",
                    bodyContent.contains(COMMENT_START + percussionTag + COMMENT_END));
        }
        
        // negative cases
        for (String notManagedTag : NOT_MANAGED_TAGS)
        {
            assertFalse("The tag '" + notManagedTag + "' should not have been commented.",
                    bodyContent.contains(COMMENT_START + notManagedTag + COMMENT_END));
        }
    }

    private final String TEST_SITE_NAME = "TestImportedSite";

    private final String TEST_SITE_URL = "http://www.test.com";
    
    private final String TEST_THEME_NAME = "ThemeName";
    
    private boolean siteCreationHelperExecuted = false;
    
    private boolean templateCreationHelperExecuted = false;
    
    private PSSiteDataServletTestCaseFixture fixture;
    
    private PSPageContent pageContent;

    private PSSiteImportCtx importContext;
    
    private PSTemplateExtractorHelper templateExtractorHelper;
    
    private PSSiteCreationHelper siteCreationHelper;
    
    private PSTemplateCreationHelper templateCreationHelper;
    
    private PSPageExtractorHelper pageExtractorHelper;

    private IPSSecurityWs securityWs;

    private IPSPageService pageService;
    
    private IPSAssetService assetService;
    
    private IPSWidgetAssetRelationshipService widgetAssetRelationshipService;
    
    private IPSItemWorkflowService itemWorkflowService;
    
    private IPSTemplateService templateService;
    
    private IPSNameGenerator nameGenerator;
    
    private IPSiteDao siteDao;
    
    private IPSPageDao pageDao;
    
    private IPSAssemblyService assemblyService;
    
    private IPSIdMapper idMapper;
    
    private IPSSiteTemplateService siteTemplateService;
    
    private IPSPageCatalogService pageCatalogService;
    
    
    private static final String[] PERCUSSION_TAGS = new String[]
    {
            "<script src=\"http://ajax.googleapis.com/ajax/libs/jquery/1.8/jquery.js\" type=\"text/javascript\"></script>",
            "<script src=\"http://ajax.googleapis.com/ajax/libs/jquery/1.4/jquery.min.js\" type=\"text/javascript\"></script>",
            "<script src=\"http://ajax.googleapis.com/ajax/libs/jquery/1.4.4/jquery.min.js\" type=\"text/javascript\"></script>",
            "<script src=\"jquery.js\" type=\"text/javascript\"></script>",
            "<script src=\"jquery.ui.core.js\" type=\"text/javascript\"></script>",
            "<script src=\"/scripts/js/jquery.ui.js\" type=\"text/javascript\"></script>",
            "<script src=\"jquery.tools.min.js\" type=\"text/javascript\"></script>",
            "<script src=\"/scripts/custom/js/jquery-latest.js\" type=\"text/javascript\"></script>",
            "<script src=\"http://ajax.aspnetcdn.com/ajax/jQuery/jquery-1.7.1.min.js\" type=\"text/javascript\"></script>",
            "<script src=\"http://ajax.aspnetcdn.com/ajax/jquery.ui/1.8.18/jquery-ui.min.js\" type=\"text/javascript\"></script>"};

    private static final String COMMENT_START = "<!--";
    private static final String COMMENT_END = "-->";

    private static final String[] NOT_MANAGED_TAGS = new String[]
    {
            "<script src=\"http://ajax.googleapis.com/ajax/libs/jquery/1.4.4/jquery.dialog.min.js\" type=\"text/javascript\"></script>",
            "<script src=\"jquery.carrousel-1.4.2.min.js\" type=\"text/javascript\"></script>",
            "<script src=\"/scripts/js/jquery.ui.carrousel-1.1.js\" type=\"text/javascript\"></script>",
            "<script src=\"http://ajax.aspnetcdn.com/ajax/jQuery/jquery-animation.1.7.1.min.js\" type=\"text/javascript\"></script>",};

}
