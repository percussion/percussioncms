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
package com.percussion.sitemanage.web.service;

import static java.util.Arrays.asList;

import static junit.framework.Assert.assertEquals;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.data.PSRegionTree;
import com.percussion.pagemanagement.data.PSTemplate;
import com.percussion.pagemanagement.data.PSTemplateSummary;
import com.percussion.pagemanagement.data.PSWidgetItem;
import com.percussion.pagemanagement.web.service.PSTemplateServiceClient;
import com.percussion.pagemanagement.web.service.PSTestSiteData;
import com.percussion.share.async.IPSAsyncJob;
import com.percussion.share.async.PSAsyncJobStatus;
import com.percussion.share.data.PSItemProperties;
import com.percussion.share.test.PSRestTestCase;
import com.percussion.sitemanage.data.PSCreateSiteSection;
import com.percussion.sitemanage.data.PSSite;
import com.percussion.sitemanage.data.PSSiteBlogPosts;
import com.percussion.sitemanage.data.PSSiteBlogProperties;
import com.percussion.sitemanage.data.PSSiteSection;
import com.percussion.sitemanage.data.PSSiteSection.PSSectionTypeEnum;
import com.percussion.sitemanage.data.PSSiteSectionProperties;
import com.percussion.sitemanage.data.PSSiteSummary;
import com.percussion.sitemanage.service.PSSiteTemplates;
import com.percussion.sitemanage.service.AssignTemplate;
import com.percussion.sitemanage.service.PSSiteTemplates.ImportTemplate;

import java.util.ArrayList;
import java.util.List;

import com.percussion.utils.testing.IntegrationTest;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runners.MethodSorters;

/**
 * Test saving site to template association.
 * @author adamgent
 *
 */
@Category(IntegrationTest.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PSSiteTemplateServiceTest extends PSRestTestCase<PSSiteTemplateRestClient>
{

    
    private static PSTestSiteData testSiteData;
    private static PSTemplateSummary renameTemplate;
    
    @BeforeClass
    public static void setUp() throws Exception {
        testSiteData = new PSTestSiteData();
        testSiteData.setUp();
        renameTemplate = testSiteData.createTemplate("TestTemplateRename");
        testSiteData.assignTemplatesToSite(testSiteData.site1.getId(), renameTemplate.getId());
        // template will be cleaned with site
        testSiteData.getTemplateCleaner().remove("TestTemplateRename");
    }
    
    @AfterClass
    public static void tearDown() throws Exception {
        testSiteData.tearDown();
    }
    
    @Override
    protected PSSiteTemplateRestClient getRestClient(String baseUrl)
    {
        PSSiteTemplateRestClient restClient = new PSSiteTemplateRestClient();
        restClient.setUrl(baseUrl);
        return restClient;
    }
    
    private PSTemplateServiceClient getTemplateRestClient() throws Exception
    {
        PSTemplateServiceClient client = new PSTemplateServiceClient(baseUrl);
        setupClient(client);
        return client;
    }
    
    @Test
    public void test010FindSitesByTemplate() throws Exception {
        log.debug("testFindSitesByTemplate");
        StopWatch sw = new StopWatch();
        sw.start();
        String templateId = testSiteData.template1.getId();
        List<PSSiteSummary> sites = testSiteData.getSiteTemplateRestClient().findSitesByTemplate(templateId);
        sw.stop();
        assertFalse("Should have sites associated to a template.", sites.isEmpty());
        log.info("testFindSitesByTemplate took: " + sw);
    }
    
    @Test
    public void test020FindTemplatesBySite() throws Exception {
        log.debug("testFindTemplatesBySite");
        String siteId = testSiteData.site1.getId();
        List<PSTemplateSummary> templates = testSiteData.getSiteTemplateRestClient().findTemplatesBySite(siteId);
        assertFalse("Should have templates associated to a site.", templates.isEmpty());
    }
    
    @Test
    public void test030FindTemplatesWithWidget() throws Exception {
        log.debug("testFindTemplatesWithWidget");
        String siteId = testSiteData.site1.getId();
        List<PSTemplateSummary> templates = testSiteData.getSiteTemplateRestClient().findTemplatesBySite(siteId,
                "percRawHtml");
        assertTrue("Should not have templates with 'widget' associated to a site.", templates.isEmpty());
        
        PSTemplate template1 = getTemplateRestClient().loadTemplate(testSiteData.template1.getId());
        
        PSWidgetItem widget = new PSWidgetItem();
        widget.setDefinitionId("percRawHtml");
                       
        PSRegionTree regTree = template1.getRegionTree();
        regTree.setRegionWidgets(regTree.getRootRegion().getRegionId(), asList(widget));
        
        template1.setRegionTree(regTree);
                
        getTemplateRestClient().save(template1);
        
        PSTemplate template2 = getTemplateRestClient().createTemplate("fooTemplate", template1.getId());
        template2 = getTemplateRestClient().save(template2);
        testSiteData.assignTemplatesToSite(siteId, template2.getId());
        
        templates = testSiteData.getSiteTemplateRestClient().findTemplatesBySite(siteId, "percRawHtml");
        assertEquals(2, templates.size());
    
        templates = testSiteData.getSiteTemplateRestClient().findTemplatesBySite(siteId, "foo");
        assertEquals(0, templates.size());
    }
    
    @Test
    public void test040RenameTemplate() throws Exception
    {
        log.debug("testRenameTemplate");
        AssignTemplate assignTemplate = new AssignTemplate();
        String newName = "TestTemplateRenamedNow";
        assignTemplate.setName(newName);
        assignTemplate.setTemplateId(renameTemplate.getId());
        PSSiteTemplates siteTemplates = new PSSiteTemplates();
        siteTemplates.setAssignTemplates(asList(assignTemplate));
        List<PSTemplateSummary> templates = testSiteData.getSiteTemplateRestClient().save(siteTemplates);
        assertTrue("Should only returned one template", templates.size() == 1);
        assertEquals("Template should have new name", newName, templates.get(0).getName());
    }
    
    
    @Test
    public void test050Blog() throws Exception 
    {
        log.debug("testBlog");
        
        PSSite site = testSiteData.site1;
        String template1Id = testSiteData.template1.getId();
        String template2Id = testSiteData.template2.getId();
        
        List<PSTemplateSummary> siteTemplates = 
            testSiteData.getSiteTemplateRestClient().findTemplatesBySite(site.getId());
        int templateCount = siteTemplates.size();
                
        // testing create()
        PSSiteSection section = createSection(site.getFolderPath(), template1Id, template2Id);
        assertTrue("A blog is created", section != null);
        
        PSSiteSectionProperties sectionProps = testSiteData.getSectionClient().getSectionProperties(section.getId());
        String blogTitle = sectionProps.getFolderName() + "title";
        
        // make sure templates were created
        PSTemplateSummary blogIndexTemplate = null;
        PSTemplateSummary blogPostTemplate = null;
        siteTemplates = 
            testSiteData.getSiteTemplateRestClient().findTemplatesBySite(site.getId());
        assertEquals(templateCount + 2, siteTemplates.size());
        for (PSTemplateSummary siteTemplate : siteTemplates)
        {
            String siteTemplateName = siteTemplate.getName();
            if (siteTemplateName.equals(blogTitle + "-" + testSiteData.template1.getName()))
            {
                blogIndexTemplate = siteTemplate;
            }
            else if (siteTemplateName.equals(blogTitle + "-" + testSiteData.template2.getName()))
            {
                blogPostTemplate = siteTemplate;
            }
        }
        assertNotNull(blogIndexTemplate);
        assertNotNull(blogPostTemplate);
        assertEquals(testSiteData.template1.getSourceTemplateName(), blogIndexTemplate.getSourceTemplateName());
        assertEquals(testSiteData.template2.getSourceTemplateName(), blogPostTemplate.getSourceTemplateName());
        
        ////////////////////////////////////////
        // PERFORM TEARDOWN
        ////////////////////////////////////////
        
        // testSiteData.tearDown();
    }
    
    /**
     * This method is for testing blogs for single site and for all sites.
     * @throws Exception
     */
    @SuppressWarnings("unused")
    @Test
    public void test060GetBlogs() throws Exception 
    {
        log.debug("testGetBlogsForSite");


        PSSite site = testSiteData.site2;
        List<PSSiteBlogProperties> blogList = testSiteData.getSectionClient().getBlogsForSite(site.getName());

        //instead of failing the test if there are existing blogs, just delete them
       if(blogList.size() != 0){
    	   		//call the cleaner to remove the sections
    		    testSiteData.getSiteCleaner().runCleaners(testSiteData.getSectionCleaner());  
    		    blogList = testSiteData.getSectionClient().getBlogsForSite(site.getName());
       }
       
       String template1Id = testSiteData.template1.getId();
       String template2Id = testSiteData.template2.getId();
        

        //Create a regular section
        PSSiteSection section1 = createSection(site.getFolderPath(), template1Id, null);
        PSSiteSection section2 = createSection(site.getFolderPath(), template1Id, null);

        //Create a blog
        PSSiteSection blogSection1 = createSection(site.getFolderPath(), template1Id, template2Id);

        blogList = testSiteData.getSectionClient().getBlogsForSite(site.getName());
        assertTrue("There is one child section under root",blogList.size() == 1);

        PSSiteBlogProperties blogProperties = blogList.get(0);
        assertEquals(blogSection1.getTitle(),blogProperties.getTitle());
        //Section path and landingpage path should not be same
        assertTrue(!blogSection1.getFolderPath().equals(blogProperties.getPath()));

        assertTrue("There are no blog posts for this blog",blogProperties.getBlogPostcount()==0);
        assertEquals("",blogProperties.getLastPublishDate());
        //Section Id and blog id should match 
        assertEquals(blogSection1.getId(), blogProperties.getId());

        //Check blog index page id
        PSPage indexPage = testSiteData.getPageRestClient().get(blogProperties.getPageId());
        assertEquals(blogSection1.getFolderPath(), indexPage.getFolderPath());
        
        //Should not have any posts
        assertEquals(0, blogProperties.getBlogPostcount());
        
        //Add a post
        String postId = testSiteData.createPage("blogPost1", blogSection1.getFolderPath(),
                blogProperties.getBlogPostTemplateId());
        PSPage post = testSiteData.getPageRestClient().get(postId);
        testSiteData.getPageCleaner().remove(post.getFolderPath() + '/' + post.getName());
        
        //Should have one post
        assertEquals(1, testSiteData.getSectionClient().getBlogsForSite(site.getName()).get(0).getBlogPostcount());
        
        PSSiteSection blogSection2 = createSection(site.getFolderPath(), template1Id, template2Id);
        
        //Create a section under blogSection2
        PSSiteSection blogSection3 = createSection(blogSection2.getFolderPath(), template1Id, template2Id);
        testSiteData.getSectionCleaner().remove(blogSection3.getId());
        
        //Create a regular section under blogSection3
        PSSiteSection subSection = createSection(blogSection3.getFolderPath(), template1Id, null);
        testSiteData.getSectionCleaner().remove(subSection.getId());

        blogList = testSiteData.getSectionClient().getBlogsForSite(site.getName());
        assertTrue("There are two child sections under root", blogList.size() == 3);

        //Create a section link
        PSSiteSection sectionLink = testSiteData.createSectionLink(blogSection3.getId(), blogSection3.getId());
        
        blogList = testSiteData.getSectionClient().getBlogsForSite(site.getName());
        assertTrue("There are two child sections under root", blogList.size() == 3);
        
        //Now test for get all blogs for all existing sites. With getBlogs method blogs are created for Site1 
        //and with above code blogs are created under the Site2. So getAllBlogs should return all blogs under these
        //two sites. NC. As well as any other sites already on the system. this list needs to be culled down to the two sites
        List<PSSiteBlogProperties> allBlogsList = testSiteData.getSectionClient().getAllBlogs();
        int count = 0;
        for(PSSiteBlogProperties b : allBlogsList){
        	if(b.getPath().startsWith(testSiteData.site1.getFolderPath().substring(1)) || b.getPath().startsWith(testSiteData.site2.getFolderPath().substring(1))){
        		count++;
        	}
        }
        assertTrue("There are four blogs for all sites", count == 4);      
        
        ////////////////////////////////////////
        // PERFORM TEARDOWN
        ////////////////////////////////////////
        
       // testSiteData.tearDown();

    }
    
    @Test
    public void test070GetBlogPosts() throws Exception 
    {
        log.debug("testGetBlogPosts");
        
        PSSite site = testSiteData.site1;
        String template1Id = testSiteData.template1.getId();
        String template2Id = testSiteData.template2.getId();
        
        // testing create()
        PSSiteSection blogSection = createSection(site.getFolderPath(), template1Id, template2Id);
        assertTrue("A blog is created", blogSection != null);
        
        // blog should not have any posts
        PSSiteBlogPosts blogPosts = testSiteData.getSectionClient().getBlogPosts(blogSection.getId());
        assertNull(blogPosts.getPosts());
        assertEquals(blogSection.getTitle(), blogPosts.getBlogTitle());
        assertEquals(blogSection.getFolderPath(), blogPosts.getBlogSectionPath());
        
        // find the blog post template id
        String blogPostTemplateId = null;
        
        List<PSSiteBlogProperties> siteBlogProps = testSiteData.getSectionClient().getBlogsForSite(site.getName());
        for (PSSiteBlogProperties blogProps : siteBlogProps)
        {
            if (blogProps.getId().equals(blogSection.getId()))
            {
                blogPostTemplateId = blogProps.getBlogPostTemplateId();
                break;
            }
        }
        
        assertNotNull(blogPostTemplateId); 
     
        // create some blog posts
        String post1Id = testSiteData.createPage("blogPost1", blogSection.getFolderPath(), blogPostTemplateId);
        PSPage postPage = testSiteData.getPageRestClient().get(post1Id);
        testSiteData.getPageCleaner().remove(postPage.getFolderPath() + '/' + postPage.getName());
        String post2Id = testSiteData.createPage("blogPost2", blogSection.getFolderPath(), blogPostTemplateId);
        postPage = testSiteData.getPageRestClient().get(post2Id);
        testSiteData.getPageCleaner().remove(postPage.getFolderPath() + '/' + postPage.getName());
        
        // create a page under the blog
        String pageId = testSiteData.createPage("page1", blogSection.getFolderPath(), template2Id);
        PSPage page = testSiteData.getPageRestClient().get(pageId);
        testSiteData.getPageCleaner().remove(page.getFolderPath() + '/' + page.getName());
        
        // blog should have two posts
        boolean post1Found = false;
        boolean post2Found = false;
        blogPosts = testSiteData.getSectionClient().getBlogPosts(blogSection.getId());
        List<PSItemProperties> posts = blogPosts.getPosts();
        assertNotNull(posts);
        assertEquals(2, posts.size());
        for (PSItemProperties post : posts)
        {
            if (post.getId().equals(post1Id))
            {
                post1Found = true;
            }
            else if (post.getId().equals(post2Id))
            {
                post2Found = true;
            }
        }
        assertTrue(post1Found);
        assertTrue(post2Found);

        ////////////////////////////////////////
        // PERFORM TEARDOWN
        ////////////////////////////////////////
        
       // testSiteData.tearDown();
       
    }
    
    /**
     * Creates a site section under the root of the specified site.
     * 
     * @param parentFolder the parent folder, assumed not blank.
     * @param templateId the template ID for creating landing page of the section,
     * assumed not <code>null</code>.
     * @param blogPostTemplateId may be <code>null</code> to indicate a regular section.
     * 
     * @return the created site section, never <code>null</code>.
     */
    private PSSiteSection createSection(String parentFolder, String templateId, String blogPostTemplateId)
    {
        PSCreateSiteSection req = new PSCreateSiteSection();
        req.setFolderPath(parentFolder);
        String name = "Section_" + section_counter++ + "_" + System.currentTimeMillis() / 1000;
        req.setPageName(name);
        req.setPageTitle(name + " title");
        req.setTemplateId(templateId);
        String linkTitle = name + " navon title";
        req.setPageLinkTitle(linkTitle);
        req.setPageUrlIdentifier(name);
        req.setCopyTemplates(true);
        
        if (blogPostTemplateId != null)
        {
            // create a blog
            req.setSectionType(PSSectionTypeEnum.blog);
            req.setBlogPostTemplateId(blogPostTemplateId);
        }
        
        PSSiteSection section = testSiteData.createSection(req);

        return section;
    }
   
    @Test
    @Ignore("Broken")
    public void test080FindTemplatesWithNoSite() throws Exception
    {
        log.debug("testFindTemplatesWitNoSite");
        
        /*
         * Test assigning a previously created template to 
         * no sites.
         */
        List<PSTemplateSummary> temps = testSiteData.getSiteTemplates();
        String createdId = null;
        for (PSTemplateSummary ts : temps) {
            StringUtils.equals(ts.getName(), "SiteTemplateServiceCreated1");
            createdId = ts.getId();
        }
        assertNotNull("Created template should exist", createdId);
        PSSiteTemplates s = new PSSiteTemplates();
        AssignTemplate at = new AssignTemplate();
        at.setSiteIds(new ArrayList<String>());
        at.setTemplateId(createdId);
        s.setAssignTemplates(asList(at));
        List<PSTemplateSummary> savedTemplates = restClient.save(s);
        assertEquals("Should have 1 saved template", 1, savedTemplates.size());
        List<PSTemplateSummary> orphans  = restClient.findTemplatesWithNoSite();
        boolean match = false;
        for(PSTemplateSummary ts : orphans) {
            if (StringUtils.equals("SiteTemplateServiceCreated1", ts.getName())) {
                match = true;
            }
        }
        assertTrue("Should have found an orphan template", match);
        
    }
    
    @Test
    public void test090CreateTemplateFromUrl()
    {
        PSSiteTemplates siteTemplates = new PSSiteTemplates();
        ImportTemplate importTemplate = new ImportTemplate();
        importTemplate.setUrl("http://samples.percussion.com/products/index.html");
        List<String> siteNames = new ArrayList<String>();
        siteNames.add(testSiteData.site1.getName());
        importTemplate.setSiteIds(siteNames);
        siteTemplates.setImportTemplate(importTemplate);
        PSTemplateSummary template = testSiteData.getSiteTemplateRestClient().createTemplateFromUrl(siteTemplates);
        assertNotNull(template);
    }
    
    //TODO: Fix me - This test should not be referencing an external web resource like google.
    @Test
    @Ignore("junit.framework.AssertionFailedError:Line 478")
    public void test100CreateTemplateFromUrlAsync()
    {
        // Build siteTemplates object with all necessary parameters
        PSSiteTemplates siteTemplates = new PSSiteTemplates();
        ImportTemplate importTemplate = new ImportTemplate();
        importTemplate.setUrl("http://samples.percussion.com");
        List<String> siteNames = new ArrayList<String>();
        siteNames.add(testSiteData.site1.getName());
        importTemplate.setSiteIds(siteNames);
        siteTemplates.setImportTemplate(importTemplate);

        // Make REST call
        long jobId = testSiteData.getSiteTemplateRestClient().createTemplateFromUrlAsync(siteTemplates);

        assertTrue(jobId > 0);

        // Poll for status until the job is completed or aborted.
        PSAsyncJobStatus jobStatus = null;
        do
        {
            jobStatus = testSiteData.getAsyncJobStatusRestClient().getStatus(Long.toString(jobId));
            assertNotNull(jobStatus);
        }
        while (!jobStatus.getStatus().equals(IPSAsyncJob.COMPLETE_STATUS)
                && !jobStatus.getStatus().equals(IPSAsyncJob.ABORT_STATUS));

        // Check that the import finished succesfully
        assertTrue(jobStatus.getStatus().equals(IPSAsyncJob.COMPLETE_STATUS));

        // Get imported template from job
        PSTemplateSummary importedTemplate = restClient.getImportedTemplate(jobId);

        // Test that the result got from the job is the expected
        assertNotNull(importedTemplate);
    }
    
    /**
     * counter used to create unique section names
     */
    private static int section_counter = 0;
    
    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Log log = LogFactory.getLog(PSSiteTemplateServiceTest.class);

}
