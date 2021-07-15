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
package com.percussion.sitemanage.web.service;


import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.percussion.itemmanagement.service.IPSItemWorkflowService;
import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.data.PSTemplate;
import com.percussion.pagemanagement.data.PSTemplateSummary;
import com.percussion.pagemanagement.web.service.PSTemplateServiceClient;
import com.percussion.pagemanagement.web.service.PSTestSiteData;
import com.percussion.pathmanagement.data.PSFolderPermission;
import com.percussion.pathmanagement.data.PSFolderPermission.Principal;
import com.percussion.pathmanagement.data.PSFolderPermission.PrincipalType;
import com.percussion.pathmanagement.service.impl.PSPathUtils;
import com.percussion.share.data.PSItemProperties;
import com.percussion.share.test.PSObjectRestClient.DataRestClientException;
import com.percussion.share.test.PSRestTestCase;
import com.percussion.sitemanage.data.PSCreateSiteSection;
import com.percussion.sitemanage.data.PSMoveSiteSection;
import com.percussion.sitemanage.data.PSReplaceLandingPage;
import com.percussion.sitemanage.data.PSSectionNode;
import com.percussion.sitemanage.data.PSSite;
import com.percussion.sitemanage.data.PSSiteBlogProperties;
import com.percussion.sitemanage.data.PSSiteSection;
import com.percussion.sitemanage.data.PSSiteSection.PSSectionTypeEnum;
import com.percussion.sitemanage.data.PSSiteSectionProperties;
import com.percussion.sitemanage.data.PSUpdateSectionLink;
import com.percussion.sitemanage.service.impl.PSSiteSectionService;

import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.runners.MethodSorters;
import org.junit.FixMethodOrder;
import org.junit.Test;

/**
 * Junit test case to test service {@link PSSiteSectionService}.
 * 
 * @author Santiago M. Murchio
 * 
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PSSiteSectionServiceTest extends PSRestTestCase<PSSiteSectionRestClient> 
{
    private static PSTestSiteData testSiteData;
    private static PSTemplateSummary renameTemplate;

    /**
     * counter used to create unique section names
     */
    private static int section_counter = 0;


    static PSSiteTemplateRestClient siteTemplateRestClient;
    static PSTemplateServiceClient templateRestClient;
    
 
    @BeforeClass
    public static void setUp() throws Exception {
        testSiteData = new PSTestSiteData();
        testSiteData.setUp();
        renameTemplate = testSiteData.createTemplate("TestTemplateRename");
        testSiteData.assignTemplatesToSite(testSiteData.site1.getId(), renameTemplate.getId());
        // template will be cleaned with site
        testSiteData.getTemplateCleaner().remove("TestTemplateRename");
        
        siteTemplateRestClient = new PSSiteTemplateRestClient();
        templateRestClient = new PSTemplateServiceClient(baseUrl);
        setupClient(siteTemplateRestClient);
        setupClient(templateRestClient);
    }
    
    @AfterClass
    public static void tearDown() throws Exception {
        testSiteData.tearDown();
    }
    
    /**
     * Creates a site with a large number of site sections.  Can be tweaked to adjust number of sections at first level and then 
     * number of sub sections.  
     * 
     * BEWARE that the {@link #tearDown()} method will delete the site, so comment that out
     * before running this test.  Also, the test is ignored, so you need to switch the annotations.  
     * 
     * DO NOT checkin these changes!! (feel free to make improvements)
     */
    @Ignore @Test
    public void test10CreateLargeNavigationTree()
    {
        PSSite site = testSiteData.site1;
        String templateId = testSiteData.template1.getId();
        
        PSSiteSectionRestClient client = testSiteData.getSectionClient();
        PSSiteSection root = client.loadRoot(site.getName());
        assertTrue(root != null);
        assertTrue("There is no section yet", root.getChildIds().isEmpty());
        
        List<PSSiteSection> children = createSubSections(site.getFolderPath(), templateId, 30, 0);
        
        for (PSSiteSection child : children)
        {
            createSubSections(child.getFolderPath(), templateId, 5, 1);
        }
        
    }
    
    private List<PSSiteSection> createSubSections(String parentPath, String templateId, int numSections, int numLevels)
    {
        List<PSSiteSection> childSections = new ArrayList<PSSiteSection>();
        for (int i = 0; i < numSections; i++)
        {
            PSSiteSection childSection = createSection(parentPath, templateId, null);
            childSections.add(childSection);
            System.out.println("Created section " + childSection.getTitle());
        }
        
        if (numLevels > 0)
        {
            numLevels--;
            for (PSSiteSection section : childSections)
            {
                createSubSections(section.getFolderPath(), templateId, numSections, numLevels);
            }
        }
        
        return childSections;
        
        
    }
    
    @Test
    public void test20SiteSection() throws Exception 
    {
        PSSite site = testSiteData.site1;
        String templateId = testSiteData.template1.getId();
        
        PSSiteSectionRestClient client = testSiteData.getSectionClient();
        PSSiteSection root = client.loadRoot(site.getName());
        assertTrue(root != null);
        assertTrue("There is no section yet", root.getChildIds().isEmpty());
        
        // testing create()
        PSSiteSection section = createSection(site.getFolderPath(), templateId, null);
        assertTrue("A section is created", section != null);
        
        // testing loadRoot()
        root = client.loadRoot(site.getName());
        assertTrue("There is one child section under root", root.getChildIds().size() == 1);

        // test loadChildSections()
        String linkTitle = section.getTitle();
        List<PSSiteSection> sections = client.loadChildSections(root);
        assertTrue("Loaded 1 child section", sections.size() == 1);
        assertTrue(sections.get(0).getTitle().equals(linkTitle));

        PSSiteSection section2 = createSection(site.getFolderPath(), templateId, null);
        root = client.loadRoot(site.getName());
        assertTrue("There is two child section under root", root.getChildIds().size() == 2);
        sections = client.loadChildSections(root);
        assertTrue("Loaded 2 child section", sections.size() == 2);

        // test loadTree()
        PSSectionNode tree = client.loadTree(site.getName());
        assertEquals("There is a root under the tree", tree.getTitle(), root.getTitle());
        assertTrue("There is 2 child sections under tree", tree.getChildNodes().size() == 2);

        // test move()
        moveSectionTest(site.getName(), client, templateId);
        
        // test update()
        updateSectionTest(section, client);
        
        // test delete section
        client.delete(sections.get(0).getId());
        testSiteData.removeSectionFromCleaner(sections.get(0));
        
        root = client.loadRoot(site.getName());
        sections = client.loadChildSections(root);
        assertTrue("Loaded 1 child section", sections.size() == 1);
        
        // create a child section
        PSSiteSection childSection = createSection(section2.getFolderPath(), templateId, null);
        childSection = client.get(childSection.getId());
        // delete both section2 and childSection
        client.delete(section2.getId());
        testSiteData.removeSectionFromCleaner(childSection);
        testSiteData.removeSectionFromCleaner(section2);
        
        try
        {
            client.get(childSection.getId());
            fail("Child section should have deleted by now.");
        }
        catch (Exception e)
        {
            // child section is deleted.
        }
        
        // test replace landing page
        replaceLandingPageTest(site.getFolderPath(), client, templateId);
    }

    @SuppressWarnings("unused")
    @Test
    public void test30MoveSectionLinkTest()
    {
        PSSite site = testSiteData.site1;
        String templateId = testSiteData.template1.getId();

        PSSiteSectionRestClient client = testSiteData.getSectionClient();
        PSSiteSection root = client.loadRoot(site.getName());

        PSSiteSection section1 = createSection(site.getFolderPath(), templateId, null);
        PSSiteSection section2 = createSection(site.getFolderPath(), templateId, null);
        PSSiteSection section3 = createSection(site.getFolderPath(), templateId, null);

        PSSiteSection section = testSiteData.createSectionLink(section2.getId(), section2.getId());

        // validate the display title path
        assertTrue(section2.getDisplayTitlePath() == null);
        
        String expectedDisplayPath = "/" + root.getTitle() + "/" + section2.getTitle();
        assertTrue(section.getDisplayTitlePath().equals(expectedDisplayPath));
        
        // try to move the section link same level as actual section
        PSMoveSiteSection req1 = new PSMoveSiteSection();
        req1.setSourceId(section.getId());
        req1.setTargetId(root.getId());
        String expectedMessage = "Section and a link to it or duplicate section links at the same level are not allowed";
        try
        {

            PSSiteSection root1 = client.move(req1);
            assertTrue(root1.getChildIds().size() == 1);
            fail("Should have gotten an exception");
        }
        catch (DataRestClientException rest)
        {
            assertEquals(expectedMessage, rest.getMessage());
        }

        // move section link to the different section
        root = client.loadRoot(site.getName());
        PSMoveSiteSection req = new PSMoveSiteSection();
        req.setSourceId(section.getId());
        req.setTargetId(section3.getId());
        req.setTargetIndex(-1);
        PSSiteSection newRoot = client.move(req);
        assertTrue(newRoot.getChildIds().size() == 1);
    }
 
    @Test
    public void test40CreateSectionLink_navTreeElement()
    {
        PSSite site = testSiteData.site1;
        String templateId = testSiteData.template1.getId();

        PSSiteSectionRestClient client = testSiteData.getSectionClient();
        PSSiteSection root = client.loadRoot(site.getName());
        PSSiteSection section2 = createSection(site.getFolderPath(), templateId, null);

        try 
        {
            testSiteData.createSectionLink(root.getId(), section2.getId());
            fail("PSSiteSectionException should have been thrown.");
        }
        catch(DataRestClientException e)
        {
            assertTrue(true);
        }
        catch(Exception e)
        {
            fail("A 500 response should have been thrown.");
        }
    }
    
    @Test
    @SuppressWarnings("unused")
    public void test50UpdateSectionLink_navTreeElement()
    {
        PSSite site = testSiteData.site1;
        String templateId = testSiteData.template1.getId();

        PSSiteSectionRestClient client = testSiteData.getSectionClient();
        PSSiteSection root = client.loadRoot(site.getName());
        PSSiteSection section2 = createSection(site.getFolderPath(), templateId, null);

        PSSiteSection section = testSiteData.createSectionLink(section2.getId(), section2.getId());

        PSUpdateSectionLink updateRequest = new PSUpdateSectionLink();
        updateRequest.setNewSectionId(root.getId());
        updateRequest.setOldSectionId(section2.getId());
        updateRequest.setParentSectionId(section2.getId());        
        
        try 
        {
            testSiteData.updateSectionLink(updateRequest);
            fail("PSSiteSectionException should have been thrown.");
        }
        catch(DataRestClientException e)
        {
            assertTrue(true);
        }
        catch(Exception e)
        {
            fail("A 500 response should have been thrown.");
        }
    }
    
    @Test
    public void test60BlogSection()
    {
        PSSite site = testSiteData.site1;
        String templateId = testSiteData.template1.getId();

        PSSiteSectionRestClient client = testSiteData.getSectionClient();
        PSSiteSection root = client.loadRoot(site.getName());
        
        assertTrue(root != null);
        
        // blog templates IDs
        String blogIndexTempId = null;
        
        try 
        {   
            // testing create()
            PSSiteSection section = createSection(site.getFolderPath(), templateId, null);
            
            // create a second child section at level 1
            PSSiteSection sectionLevel1 = createSection(site.getFolderPath(), templateId, null);
            
            // create a child section at level 2
            PSSiteSection sectionLevel2 = createSection(section.getFolderPath(), templateId, null);
              
            // create a child section at level 3
            PSSiteSection sectionLevel3 = createSection(sectionLevel2.getFolderPath(), templateId, null);
   
            // create a blog template
            blogIndexTempId = createBlogTemplate("test", templateId, site.getId());
            
            // create a blog section
            PSSiteSection blogSection = createSection(sectionLevel3.getFolderPath(), templateId, blogIndexTempId);
            
            // check if it is a blog section
            assertTrue(blogSection.getSectionType() == PSSectionTypeEnum.blog);
                                  
            // check blogs per site
            List<PSSiteBlogProperties> blogs = client.getBlogsForSite(site.getName());
            assertTrue("Loaded 1 blog", blogs.size() == 1);
            
            // create a child section at level 4
            PSSiteSection sectionLevel41 = createSection(sectionLevel3.getFolderPath(), templateId, null);
            
            // create another child section at level 4
            PSSiteSection sectionLevel42 = createSection(sectionLevel3.getFolderPath(), templateId, null);
            
            // create a blog section
            PSSiteSection blogSection2 = createSection(sectionLevel42.getFolderPath(), templateId, blogIndexTempId);
            
            // check if it is a blog section
            assertTrue(blogSection.getSectionType() == PSSectionTypeEnum.blog);
                                  
            // check blogs per site
            blogs = client.getBlogsForSite(site.getName());
            assertTrue("Loaded 2 blogs", blogs.size() == 2);
            
            // delete all created sections
            client.delete(blogSection2.getId());
            client.delete(sectionLevel42.getId());
            client.delete(sectionLevel41.getId());
            client.delete(blogSection.getId());
            
            client.delete(sectionLevel3.getId());
            client.delete(sectionLevel2.getId());
            client.delete(sectionLevel1.getId());
            client.delete(section.getId());
           
            testSiteData.removeSectionFromCleaner(blogSection2);
            testSiteData.removeSectionFromCleaner(sectionLevel42);
            testSiteData.removeSectionFromCleaner(sectionLevel41);
            testSiteData.removeSectionFromCleaner(blogSection);
            
            testSiteData.removeSectionFromCleaner(sectionLevel3);
            testSiteData.removeSectionFromCleaner(sectionLevel2);
            testSiteData.removeSectionFromCleaner(sectionLevel1);
            testSiteData.removeSectionFromCleaner(section);
        }
        catch(DataRestClientException e)
        {
            assertTrue(true);
        }
        catch(Exception e)
        {
            fail("A 500 response should have been thrown.");
        }
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
        String name = "Section_" + section_counter++ + "_" + System.currentTimeMillis() / 1000;
        String linkTitle = name + " navon title";

        PSCreateSiteSection req = new PSCreateSiteSection();
        req.setFolderPath(parentFolder);
        req.setPageName(name);
        req.setPageTitle(name + " title");
        req.setTemplateId(templateId);
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

    /* (non-Javadoc)
     * @see com.percussion.share.test.PSRestTestCase#getRestClient(java.lang.String)
     */
    @Override
    protected PSSiteSectionRestClient getRestClient(String baseUrl)
    {
        if (restClient == null)
        {
            restClient = new PSSiteSectionRestClient(baseUrl);
        }
        return restClient;
    }

    /**
     * Tests move sections to different location.
     * 
     * @param siteName the site name, assumed not blank.
     * @param client the REST client, assumed not <code>null</code>.
     * @param templateId the template to use for creating sections.
     */
    private void  moveSectionTest(String siteName, PSSiteSectionRestClient client, String templateId)
    {
        PSSiteSection root = client.loadRoot(siteName);
        List<String> childIds = root.getChildIds();

        assertTrue(childIds.size() == 2);
        
        String id_0 = childIds.get(0);
        String id_1 = childIds.get(1);
        
        // re-order child nodes
        PSMoveSiteSection req = new PSMoveSiteSection();
        req.setSourceId(id_0);
        req.setSourceParentId(root.getId());
        req.setTargetId(root.getId());
        req.setTargetIndex(-1);

        PSSiteSection newRoot = client.move(req);
        
        childIds = newRoot.getChildIds();
        assertEquals(id_0, childIds.get(1));
        assertEquals(id_1, childIds.get(0));
     
        // add another node
        PSSiteSection section_3 = createSection(root.getFolderPath(), templateId, null);
        root = client.loadRoot(siteName);
        assertTrue(root.getChildIds().size() == 3);
        
        // re-order again
        req.setSourceId(section_3.getId());
        req.setTargetIndex(1);
        newRoot = client.move(req);
        childIds = newRoot.getChildIds();
        assertEquals(section_3.getId(), childIds.get(1));
        
        // move node to different parent
        req.setSourceId(section_3.getId());
        req.setTargetId(id_0);
        req.setTargetIndex(-1);
        
        PSSiteSection section_0 = client.move(req);

        // don't need to clean up the moved section
        // because it will be removed/cleaned up by its parent
        testSiteData.removeSectionFromCleaner(section_3);

        // validating the move
        assertTrue(section_0.getChildIds().size() == 1);
        
        section_0 = client.get(id_0);
        assertTrue(section_0.getChildIds().size() == 1);
        
        root = client.loadRoot(siteName);
        assertTrue(root.getChildIds().size() == 2);
    }
    
    /**
     * Tests update section operation.
     * 
     * @param section to be updated section, assumed not <code>null</code>.
     * @param client the REST client, assumed not <code>null</code>.
     */
    private void updateSectionTest(PSSiteSection section, PSSiteSectionRestClient client)
    {
        PSSiteSectionProperties properties = client.getSectionProperties(section.getId());
        assertTrue(properties.getFolderPermission().getAccessLevel() == PSFolderPermission.Access.WRITE);
        
        String title = section.getTitle();
        String newTitle = title + "-New" + System.currentTimeMillis();
        String folderName = getFolderNameFromPath(section.getFolderPath());
        String newFolderName = folderName + "-New" + System.currentTimeMillis();
        
        // update section's title and folder name
        PSSiteSectionProperties updateReq = new PSSiteSectionProperties();
        updateReq.setId(section.getId());
        updateReq.setTitle(newTitle);
        updateReq.setFolderName(newFolderName);
        PSFolderPermission permission = new PSFolderPermission();
        permission.setAccessLevel(PSFolderPermission.Access.READ);
        List<Principal> writeUsers = new ArrayList<Principal>();
        Principal writer = new Principal();
        writer.setName("writer");
        writer.setType(PrincipalType.USER);
        writeUsers.add(writer);
        permission.setWritePrincipals(writeUsers);
        List<Principal> adminUsers = new ArrayList<Principal>();
        Principal admin = new Principal();
        admin.setName("admin");
        admin.setType(PrincipalType.USER);
        adminUsers.add(admin);
        permission.setAdminPrincipals(adminUsers);
        List<Principal> readUsers = new ArrayList<Principal>();
        Principal reader = new Principal();
        reader.setName("reader");
        reader.setType(PrincipalType.USER);
        readUsers.add(reader);
        permission.setReadPrincipals(readUsers);
        updateReq.setFolderPermission(permission);
        
        section = client.update(updateReq);
    
        assertEquals(newTitle, section.getTitle());
        assertEquals(newFolderName, getFolderNameFromPath(section.getFolderPath()));

        properties = client.getSectionProperties(section.getId());
        PSFolderPermission propsPerm = properties.getFolderPermission();
        assertTrue(propsPerm.getAccessLevel() == PSFolderPermission.Access.READ);
        assertTrue(propsPerm.getAdminPrincipals().size() == 1 
                && propsPerm.getAdminPrincipals().get(0).equals(admin));
        assertTrue(propsPerm.getWritePrincipals().size() == 1 
                && propsPerm.getWritePrincipals().get(0).equals(writer));
        assertTrue(propsPerm.getReadPrincipals().size() == 1 
                && propsPerm.getReadPrincipals().get(0).equals(reader));

        // undo above changes
        updateReq.setTitle(title);
        updateReq.setFolderName(folderName);
        updateReq.setFolderPermission(new PSFolderPermission());
        client.update(updateReq);
    }

    private String getFolderNameFromPath(String path)
    {
        int i = path.lastIndexOf("/");
        return path.substring(i+1); //, folderName.length());
    }


    /**
     * Tests replacing a landing page.
     * 
     * @param siteFolder the site folder path, assumed not blank.
     * @param client the REST client, assumed not <code>null</code>.
     * @param templateId the template to use for creating sections and pages.
     * 
     * @throws Exception if an error occurs creating a page.
     */
    private void replaceLandingPageTest(String siteFolder, PSSiteSectionRestClient client, String templateId)
        throws Exception
    {
        replaceLandingPageSameFolderTest(siteFolder, client, templateId);
        replaceLandingPageDifferentFolderTest(siteFolder, client, templateId);
    }
    
    /**
     * Tests replacing a landing page with a page in the same folder.
     * 
     * @param siteFolder the site folder path, assumed not blank.
     * @param client the REST client, assumed not <code>null</code>.
     * @param templateId the template to use for creating sections and pages.
     * 
     * @throws Exception if an error occurs creating a page.
     */
    private void replaceLandingPageSameFolderTest(String siteFolder, PSSiteSectionRestClient client, String templateId)
        throws Exception
    {
        // create a section
        PSSiteSection section = createSection(siteFolder, templateId, null);
        String sectionFolderPath = section.getFolderPath();
                       
        // create new landing page
        String newLandingPageId = testSiteData.createPage("New-Landing-Page", sectionFolderPath, templateId);
        testSiteData.getPageCleaner().remove(sectionFolderPath + "/New-Landing-Page");
        
        // replace landing page
        testReplaceLandingPage(section, newLandingPageId, client);
    }
    
    /**
     * Tests replacing a landing page with a page in a different folder.
     * 
     * @param siteFolder the site folder path, assumed not blank.
     * @param client the REST client, assumed not <code>null</code>.
     * @param templateId the template to use for creating sections and pages.
     * 
     * @throws Exception if an error occurs creating the page.
     */
    private void replaceLandingPageDifferentFolderTest(String siteFolder, PSSiteSectionRestClient client,
            String templateId) throws Exception
    {
        // create new landing page
        String newLandingPageId = testSiteData.createPage("New-Landing-Page", siteFolder, templateId);
        testSiteData.getPageCleaner().remove(siteFolder + "/New-Landing-Page");
        
        // workflow to PENDING
        testSiteData.getWorkflowClient().transition(newLandingPageId, IPSItemWorkflowService.TRANSITION_TRIGGER_APPROVE);
        
        // create a section
        PSSiteSection section = createSection(siteFolder, templateId, null);
        
        // figure out landing page path
        int index = section.getFolderPath().lastIndexOf('/');
        String ldPageName = section.getFolderPath().substring(index+1);
        String landingPagePath = section.getFolderPath().replaceAll("//Sites", PSPathUtils.SITES_FINDER_ROOT) + "/"
              + ldPageName;
        
        // transition old landing page to "Pending"
        PSItemProperties itemProps = testSiteData.getPathRestClient().findItemProperties(landingPagePath);
        assertTrue(itemProps.getStatus().equals("Draft"));
        testSiteData.getWorkflowClient().transition(itemProps.getId(), IPSItemWorkflowService.TRANSITION_TRIGGER_APPROVE);
        itemProps = testSiteData.getPathRestClient().findItemProperties(landingPagePath);
        assertTrue(itemProps.getStatus().equals("Pending"));
        
        // replace landing page
        testReplaceLandingPage(section, newLandingPageId, client);
        
        // After replace landing page, the state of new landing page should be from "Pending" to "Quick Edit"
        itemProps = testSiteData.getPathRestClient().findItemProperties(landingPagePath);
        assertTrue(itemProps.getStatus().equals("Quick Edit"));
        
        // After replace landing page, the state of old landing page should be from "Pending" to "Quick Edit"
        itemProps = testSiteData.getPathRestClient().findItemProperties(landingPagePath + "-1");
        assertTrue(itemProps.getStatus().equals("Quick Edit"));
    }
    
    /**
     * Tests replacing a landing page with the specified page.
     * 
     * @param section assumed not null.
     * @param newLandingPageId assumed not blank.
     * @param client the REST client, assumed not <code>null</code>.
     */
    private void testReplaceLandingPage(PSSiteSection section, String newLandingPageId, 
            PSSiteSectionRestClient client)
    {
        // get the section properties
        PSSiteSectionProperties sectionProps = client.getSectionProperties(section.getId());
        String sectionFolderName = sectionProps.getFolderName();
        
        // replace the landing page
        PSReplaceLandingPage req = new PSReplaceLandingPage();
        req.setNewLandingPageId(newLandingPageId);
        req.setSectionId(section.getId());
        PSReplaceLandingPage resp = client.replaceLandingPage(req);
        
        // check the response
        assertEquals(sectionFolderName, resp.getNewLandingPageName());
        assertEquals(sectionFolderName + "-1", resp.getOldLandingPageName());
        
        // check the page
        PSPage nlp = testSiteData.getPageRestClient().get(newLandingPageId);
        assertEquals(sectionFolderName, nlp.getName());
        assertEquals(section.getTitle(), nlp.getLinkTitle());
        assertEquals(section.getFolderPath(), nlp.getFolderPath());
    }
    
    /**
     * Creates and saves a template based on the specified blog name and source template.  The name of the template will
     * use the following convention: {blog name} - {source template name} - (n).  The increment will be added if
     * necessary to ensure a unique name under the specified site.  It will start at 2.
     * 
     * @param name of the blog.
     * @param srcId source template.
     * @param siteId destination site.
     * 
     * @return id of the new template.
     * @throws Exception 
     */
    private String createBlogTemplate(String name, String srcId, String siteId) throws Exception
    {
       PSTemplate tempId = null;
       
       PSTemplateSummary tempSrc = templateRestClient.loadTemplate(srcId);
       if (tempSrc != null)
       {
          String templateName = name.replaceAll("[\\\\\\\\|/<>?\":*#;% ]", "");
          String tempBaseName = templateName + "-" + tempSrc.getName();
          String tempName = tempBaseName;

          boolean tempExists = false;
          int i = 2;

          List<PSTemplateSummary> siteTemps = siteTemplateRestClient.findTemplatesBySite(siteId);

          while (!tempExists)
          {
             for (PSTemplateSummary siteTempSum : siteTemps)
             {
                if (siteTempSum.getName().equals(tempName))
                {
                   tempExists = true;
                   break;
                }
             }

             if (tempExists)
             {
                tempName = tempBaseName + "-" + i++;
                tempExists = false;
             }
             else
             {
                break;
             }
          }

          tempId = templateRestClient.createTemplate(tempName, srcId);
       }
      
       //FB: NP_NULL_ON_SOME_PATH NC 1-17-16
       if(tempId != null)
    	   return tempId.getId();
       
       return null;
    }
    
}
