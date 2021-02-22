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

import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.data.PSTemplate;
import com.percussion.pagemanagement.data.PSWidgetItem;
import com.percussion.pagemanagement.service.IPSPageService;
import com.percussion.pagemanagement.service.IPSTemplateService;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.share.spring.PSSpringWebApplicationContextUtils;
import com.percussion.sitemanage.dao.IPSiteDao;
import com.percussion.sitemanage.dao.impl.PSSiteContentDao;
import com.percussion.sitemanage.data.PSPageContent;
import com.percussion.sitemanage.data.PSSite;
import com.percussion.sitemanage.data.PSSiteImportCtx;
import com.percussion.sitemanage.error.PSSiteImportException;
import com.percussion.sitemanage.importer.IPSSiteImportLogger;
import com.percussion.sitemanage.importer.IPSSiteImportLogger.PSLogEntryType;
import com.percussion.sitemanage.importer.IPSSiteImportLogger.PSLogObjectType;
import com.percussion.sitemanage.importer.PSSiteImportLogger;
import com.percussion.sitemanage.importer.PSSiteImporter;
import com.percussion.sitemanage.importer.helpers.impl.PSImportHelper;
import com.percussion.sitemanage.importer.helpers.impl.PSSiteCreationHelper;
import com.percussion.sitemanage.importer.helpers.impl.PSTemplateExtractorHelper;
import com.percussion.sitesummaryservice.service.IPSSiteImportSummaryService;
import com.percussion.theme.data.PSThemeSummary;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.webservices.security.IPSSecurityWs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

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
public class PSTemplateExtractorHelperTest extends ServletTestCase
{
    //TODO Add Javadocs/comments
    @Override
    protected void setUp() throws Exception
    {
        PSSpringWebApplicationContextUtils.injectDependencies(this);
        super.setUp();

        // Login is needed to create folder for the new site.
        securityWs.login("Admin", "demo", "Default", null);

        siteCreationHelper = new PSSiteCreationHelper(siteDao, pageService);
        templateExtractorHelper = new PSTemplateExtractorHelper(templateService);
        
        initData();
        
        createSite();        
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        deleteSite();
    }

    private void initData() throws Exception
    {
        //TODO move test html page to resource folder.
        File pageSampleFile = createTempConfigFileBasedOn(getClass().getResourceAsStream("CM1094-SamplePage.html"));
        Document doc = Jsoup.parse(pageSampleFile, "UTF-8");
        pageContent = PSSiteImporter.createPageContent(doc, new PSSiteImportLogger(PSLogObjectType.TEMPLATE));
        
        importContext = new PSSiteImportCtx();
        importContext.setLogger(new PSSiteImportLogger(PSLogObjectType.SITE));
        IPSSiteImportSummaryService summaryService = (IPSSiteImportSummaryService) getWebApplicationContext().getBean("siteImportSummaryService");
        importContext.setSummaryService(summaryService);
        PSSite site = new PSSite();
        site.setBaseUrl(TEST_SITE_URL);
        site.setName(TEST_SITE_NAME);
        importContext.setSite(site);
        
        //TODO create theme folder in filesystem.
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
    
    private void createSite() throws PSSiteImportException {
        siteCreationHelper.process(pageContent, importContext);
    }

    private void deleteSite()
    {
        siteCreationHelper.rollback(pageContent, importContext);
    }
    
    // Test if metadata was extracted correctly
    @Test
    public void test010ExtractMetadata() throws PSSiteImportException, PSDataServiceException {
        templateExtractorHelper.process(pageContent, importContext);

        PSPage homePage = pageService
                .findPage(PSSiteContentDao.HOME_PAGE_NAME, importContext.getSite().getFolderPath());
        assertNotNull(homePage);
        PSTemplate template = templateService.load(homePage.getTemplateId());
        assertNotNull(template);

        // Additional head content
        assertNotNull(template.getAdditionalHeadContent());
        assertNotSame("", template.getAdditionalHeadContent());
        
        assertEquals(
                pageContent.getHeadContent().replaceAll("(\\r|\\n)", "").replaceAll("<!--", "").replaceAll("-->", ""),
                template.getAdditionalHeadContent().replaceAll("(\\r|\\n)", "").replaceAll("<!--", "")
                        .replaceAll("-->", ""));

        // validate the commented out tags
        validatePercussionGeneratedTags(template.getAdditionalHeadContent());
        validatePercussionJSReferences(template.getAdditionalHeadContent(), "Additional Head Content");
        validatePercussionJSReferences(template.getAfterBodyStartContent(), "After Body Start Content");
        validatePercussionJSReferences(template.getBeforeBodyCloseContent(), "Before Body close Content");
        validateLogsForManagedTags(importContext.getLogger());

        // After body start content
        assertNotNull(template.getAfterBodyStartContent());
        assertNotSame("", template.getAfterBodyStartContent());
        assertEquals(pageContent.getAfterBodyStart(), template.getAfterBodyStartContent());

        // Body before close content
        assertNotNull(template.getBeforeBodyCloseContent());
        assertNotSame("", template.getBeforeBodyCloseContent());
        assertEquals(pageContent.getBeforeBodyClose(), template.getBeforeBodyCloseContent());

        // Theme
        assertNotNull(template.getTheme());
        assertNotSame("", template.getTheme());
        assertEquals(importContext.getThemeSummary().getName(), template.getTheme());
    }
    
    @Test
    public void test020AddHTMLWidgetToTemplate() throws PSDataServiceException, PSSiteImportException {
        templateExtractorHelper.process(pageContent, importContext);

        PSPage homePage = pageService
                .findPage(PSSiteContentDao.HOME_PAGE_NAME, importContext.getSite().getFolderPath());
        assertNotNull(homePage);
        PSTemplate template = templateService.load(homePage.getTemplateId());
        assertNotNull(template);
        
        //Test if the HTML widget was added to the template
        List<PSWidgetItem> templateWidgets = template.getWidgets();
        assertNotNull(templateWidgets);
        assertTrue(templateWidgets.size()>0);
        PSWidgetItem widget = templateWidgets.get(0);
        assertEquals("percRawHtml", widget.getDefinitionId());
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

    /**
     * Checks that the tags that are generated by CMS gets commented on the
     * given additional head content.
     * 
     * @param templateField {@link String} with the additional html head
     *            content. Assumed not <code>null</code>.
     */
    private void validatePercussionGeneratedTags(String templateField)
    {
        // affirmative cases
        for (String percussionTag : PERCUSSION_TAGS)
        {
            assertTrue("The tag '" + percussionTag + "' should have been commented.",
                    templateField.contains(COMMENT_START + percussionTag + COMMENT_END));
        }
        
        // negative cases
        for (String notManagedTag : NOT_MANAGED_TAGS)
        {
            assertFalse("The tag '" + notManagedTag + "' should not have been commented.",
                    templateField.contains(COMMENT_START + notManagedTag + COMMENT_END));
        }
    }
    
    private void validatePercussionJSReferences(String templateField, String place)
    {
        // affirmative cases
        for (String percussionTag : PERCUSSION_JS_REFERENCES)
        {
            assertTrue("The tag '" + percussionTag + "' should have been commented in '" + place + "'.",
                    templateField.contains(COMMENT_START + percussionTag + COMMENT_END));
        }
        
        // negative cases
        for (String notManagedTag : NOT_MANAGED_TAGS)
        {
            assertFalse("The tag '" + notManagedTag + "' should not have been commented in '" + place + "'.",
                    templateField.contains(COMMENT_START + notManagedTag + COMMENT_END));
        }
    }

    /**
     * Checks that the logger data holds the corresponding lines for those
     * managed tags that were commented.
     * 
     * @param logger {@link IPSSiteImportLogger} the logger that was used for
     *            the metadata extraction. Assumed not <code>null</code>.
     * 
     */
    private void validateLogsForManagedTags(IPSSiteImportLogger logger)
    {
        String log = logger.getLog(); 
        
        for (String percussionTag : PERCUSSION_TAGS)
        {
            String logLine = buildLogLineForCommentedElement(percussionTag);
            assertTrue("The LOG should contain the following line:  '" + logLine + "'.",
                    log.contains(logLine));
        }

        for (String jsReference : PERCUSSION_JS_REFERENCES)
        {
            String logLine = buildLogLineForCommentedJSElement(jsReference);
            assertTrue("The LOG should contain the following line:  '" + logLine + "'.",
                    log.contains(logLine));
        }
        
        for (String percussionTag : NOT_MANAGED_TAGS)
        {
            String logLine = buildLogLineForCommentedElement(percussionTag);
            assertFalse("The LOG should not contain the following line:  '" + logLine + "'.",
                    log.contains(logLine));
        }

        int lines = countOccurrencesOf(log, PSImportHelper.COMMENTED_JS_REFERENCE_FROM_HEAD);
        lines += countOccurrencesOf(log, PSImportHelper.COMMENTED_OUT_ELEMENT);
        assertTrue("The log entries for commented tags should have been " + PERCUSSION_TAGS.length + ", but they were "
                + lines, lines == PERCUSSION_TAGS.length + PERCUSSION_JS_REFERENCES.length);
    }

    /**
     * Builds a line equals to the ones that appear in the loggind data, for
     * those commented metadata tags. An example:
     * 
     * <pre>
     * STATUS: Commented out managed element: &lt;title&gt;Home&lt;/title&gt; from &lt;head&gt; element.
     * </pre>
     * 
     * @param tag {@link String} with the whole tag to build the line. Assumed
     *            not <code>null</code> nor empty.
     * @return {@link String}, never <code>null</code> or empty.
     */
    private String buildLogLineForCommentedElement(String tag)
    {
        String logLine = PSLogEntryType.STATUS.name();
        logLine += ": ";
        logLine += PSImportHelper.COMMENTED_OUT_ELEMENT;        
        logLine += ": ";
        logLine += tag;        
        return logLine;
    }

    /**
     * Builds a line equals to the ones that appear in the loggind data, for
     * those commented js referenced tags. An example:
     * 
     * <pre>
     * STATUS: Commented out managed jquery reference from &lt;body&gt; element: &lt;script src="jquery.js" type="text/javascript"&gt; &lt;/script&gt; from &lt;body&gt; element.
     * </pre>
     * 
     * @param percussionTag {@link String} with the whole tag to build the line. Assumed
     *            not <code>null</code> nor empty.
     * @return {@link String}, never <code>null</code> or empty.
     */
    private String buildLogLineForCommentedJSElement(String percussionTag)
    {
        String logLine = PSLogEntryType.STATUS.name();
        logLine += ": ";
        logLine += PSImportHelper.COMMENTED_JS_REFERENCE_FROM_BODY;
        logLine += ": ";
        logLine += percussionTag;        
        return logLine;
    }

    private final String TEST_SITE_NAME = "TestImportedSite";

    private final String TEST_SITE_URL = "http://www.test.com";
    
    private final String TEST_THEME_NAME = "ThemeName";
    
    private PSPageContent pageContent;

    private PSSiteImportCtx importContext;
    
    private PSTemplateExtractorHelper templateExtractorHelper;
    
    private PSSiteCreationHelper siteCreationHelper;

    private IPSSecurityWs securityWs;

    private IPSPageService pageService;
    
    private IPSTemplateService templateService;
    
    private IPSiteDao siteDao;
        
    private static final String[] PERCUSSION_TAGS = new String[]
    {"<title>Web Content Management Software (WCM) | Percussion Software</title>",
            "<meta content=\"text/html; charset=UTF-8\" http-equiv=\"content-type\" />",
            "<meta name=\"generator\" content=\"Percussion\" />", "<meta name=\"robots\" content=\"noindex\" />",
            "<meta name=\"description\" content=\"The description of the page\" />",
            "<meta property=\"dcterms:author\" content=\"author of the page\" />",
            "<meta property=\"dcterms:type\" content=\"page\" />",
            "<meta property=\"dcterms:source\" content=\"perc.template.name\" />",
            "<meta property=\"dcterms:created\" datatype=\"xsd:dateTime\" content=\"2012-10-10\" />",
            "<meta property=\"dcterms:alternative\" content=\"perc.page.linkTitle\" />",
            "<meta property=\"perc:tags\" content=\"tag1.String\" />",
            "<meta property=\"perc:tags\" content=\"tag2.String\" />",
            "<meta property=\"perc:category\" content=\"category.String\" />",
            "<meta property=\"perc:calendar\" content=\"Calendar Name\" />",
            "<meta property=\"perc:start_date\" content=\"10/12/2012\" />",
            "<meta property=\"perc:end_date\" datatype=\"xsd:dateTime\" content=\"10/12/2012\" />",};

    private static final String[] PERCUSSION_JS_REFERENCES = new String[]
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
            "<meta http-equiv=\"refresh\" content=\"600\" />",
            "<meta http-equiv=\"default-style\" content=\"link_element\" />",

            "<script src=\"http://ajax.googleapis.com/ajax/libs/jquery/1.4.4/jquery.dialog.min.js\" type=\"text/javascript\"></script>",
            "<script src=\"jquery.carrousel-1.4.2.min.js\" type=\"text/javascript\"></script>",
            "<script src=\"/scripts/js/jquery.ui.carrousel-1.1.js\" type=\"text/javascript\"></script>",
            "<script src=\"http://ajax.aspnetcdn.com/ajax/jQuery/jquery-animation.1.7.1.min.js\" type=\"text/javascript\"></script>",
            "<script language=\"javascript\" type=\"text/javascript\">llactid=10810</script>",
            "<SCRIPT TYPE=\"text/javascript\" LANGUAGE=\"JavaScript\" SRC=\"/web_resources/themes/perc-web/elqNow/elqCfg.js\"></SCRIPT>",
            "<SCRIPT TYPE=\"text/javascript\" LANGUAGE=\"JavaScript\" SRC=\"/web_resources/themes/perc-web/elqNow/elqImg.js\"></SCRIPT>"};

}
