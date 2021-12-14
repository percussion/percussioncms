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
package com.percussion.foldermanagement.service;

import com.percussion.foldermanagement.data.PSFolderItem;
import com.percussion.foldermanagement.data.PSGetAssignedFoldersJobStatus;
import com.percussion.foldermanagement.data.PSWorkflowAssignment;
import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.data.PSRegion;
import com.percussion.pagemanagement.data.PSRegionBranches;
import com.percussion.pagemanagement.data.PSRegionNode.PSRegionOwnerType;
import com.percussion.pathmanagement.data.PSDeleteFolderCriteria;
import com.percussion.pathmanagement.data.PSPathItem;
import com.percussion.pathmanagement.web.service.PSPathServiceRestClient;
import com.percussion.share.IPSSitemanageConstants;
import com.percussion.share.test.PSRestClient.RestClientException;
import com.percussion.share.test.PSRestTestCase;
import com.percussion.share.test.PSTestDataCleaner;
import com.percussion.sitemanage.data.PSSite;
import com.percussion.sitemanage.web.service.PSSiteRestClient;
import com.percussion.sitemanage.web.service.PSSiteTemplateRestClient;
import com.percussion.workflow.data.PSUiWorkflow;
import com.percussion.workflow.web.service.PSSteppedWorkflowRestServiceClient;
import org.apache.commons.lang.StringUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author miltonpividori
 * 
 */
public class PSFolderServiceTest extends PSRestTestCase<PSFolderServiceRestClient>
{
    private static final String STANDARD_WORKFLOW = "Standard Workflow";
    
    private static final String DEFAULT_WORKFLOW = "Default Workflow";
    
    static PSSite site;
    
    static PSPathItem pathSite; 
    
    static PSUiWorkflow workflow1, workflow2;
    
    static PSSiteRestClient siteRestClient;
    
    static PSFolderServiceRestClient restClient;
    
    static PSSteppedWorkflowRestServiceClient workflowRestClient;
    
    static PSSiteTemplateRestClient siteTemplateRestClient;
    
    static PSPathServiceRestClient pathRestClient;

    static PSTestDataCleaner<String> siteCleaner = new PSTestDataCleaner<String>()
    {
        @Override
        protected void clean(String name) throws Exception
        {
            siteRestClient.delete(name);
        }
    };
    
    static PSTestDataCleaner<String> folderCleaner = new PSTestDataCleaner<String>()
    {
        @Override
        protected void clean(String path) throws Exception
        {
            PSDeleteFolderCriteria criteria = new PSDeleteFolderCriteria();
            criteria.setPath(path);
            pathRestClient.deleteFolder(criteria);
        }
    };

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.percussion.share.test.PSRestTestCase#getRestClient(java.lang.String)
     */
    @Override
    protected PSFolderServiceRestClient getRestClient(String baseUrl)
    {
        restClient = new PSFolderServiceRestClient(baseUrl);
        return restClient;
    }
    
    private static PSSite createSite()
    {
        String siteName = "Site" + System.currentTimeMillis();
        siteCleaner.add(siteName);
        PSSite site = new PSSite();
        site.setName(siteName);
        site.setLabel("My test site");
        site.setHomePageTitle("homePageTitle");
        site.setNavigationTitle("navigationTitle");
        site.setBaseTemplateName(IPSSitemanageConstants.PLAIN_BASE_TEMPLATE_NAME);
        site.setTemplateName("templateName");
        return siteRestClient.save(site);
    }
    
    /**
     * @param defaultWorkflow
     * @param folder1_1
     * @param folder1_1_1
     * @param folder2_1
     */
    private void saveWorkflow(String defaultWorkflow, PSPathItem... folders)
    {
        saveWorkflow(defaultWorkflow, folders, new PSPathItem[0]);
    }
    
    private void saveWorkflow(String defaultWorkflow, PSPathItem[] assignedFolders, PSPathItem[] unassignedFolders)
    {
        PSWorkflowAssignment workflowAssignment = new PSWorkflowAssignment();
        workflowAssignment.setWorkflowName(defaultWorkflow);
        
        List<String> assigned = new ArrayList<String>();
        if (assignedFolders != null)
        {
            for (PSPathItem folder : assignedFolders)
            {
                assigned.add(folder.getId());
            }
        }
        
        List<String> unassigned = new ArrayList<String>();
        if (unassignedFolders != null)
        {
            for (PSPathItem folder : unassignedFolders)
            {
                unassigned.add(folder.getId());
            }
        }
        
        workflowAssignment.setAssignedFolders(assigned.toArray(new String[0]));
        workflowAssignment.setUnassignedFolders(unassigned.toArray(new String[0]));
        
        restClient.save(workflowAssignment);
    }
    
    private void saveWorkflow(String defaultWorkflow, String... folderIds)
    {
        PSWorkflowAssignment workflowAssignment = new PSWorkflowAssignment();
        workflowAssignment.setWorkflowName(defaultWorkflow);
        
        List<String> assigned = new ArrayList<String>();
        for (String folderId : folderIds)
        {
            assigned.add(folderId);
        }
        
        workflowAssignment.setAssignedFolders(assigned.toArray(new String[0]));
        
        restClient.save(workflowAssignment);
    }
    
    private PSPage createPage(String folderPath, String templateId)
    {
        PSPage pageNew = new PSPage();
        // the .xml was added to test for bug cml-2262
        pageNew.setName(String.valueOf(System.currentTimeMillis()) + ".xml");
        pageNew.setTitle("test new page title");
        pageNew.setFolderPath(folderPath);
        pageNew.setTemplateId(templateId);
        pageNew.setLinkTitle("dummy");

        PSRegionBranches br = new PSRegionBranches();
        PSRegion region = new PSRegion();
        region.setRegionId("Test");
        region.setOwnerType(PSRegionOwnerType.PAGE);
        br.setRegions(asList(region));
        pageNew.setRegionBranches(br);

        return pageNew;
    }

    private void testGetAssignedFoldersShouldFail(String workflowName, String path, int expectedErrorCode)
    {
        try
        {
            restClient.getAssociatedFolders(workflowName, path, false);

            fail("Should have thrown an exception");
        }
        catch (RestClientException e)
        {
            assertEquals("http error code", expectedErrorCode, e.getStatus());
        }
        catch (Exception e)
        {
            fail("Should have thrown another type of exception");
        }
    }
    
    private void saveWorkflowShouldFail(String workflowName, int expectedErrorCode, PSPathItem... folders)
    {
        try
        {
            saveWorkflow(workflowName, folders);

            fail("Should have thrown an exception");
        }
        catch (RestClientException e)
        {
            assertEquals("http error code", expectedErrorCode, e.getStatus());
        }
        catch (Exception e)
        {
            fail("Should have thrown another type of exception");
        }
    }
    
    @Before
    public void testSetup()
    {
        folderCleaner.clean();
    }
    
    @BeforeClass
    public static void setup() throws Exception
    {
        siteRestClient = new PSSiteRestClient(baseUrl);
        setupClient(siteRestClient);
        
        workflowRestClient = new PSSteppedWorkflowRestServiceClient(baseUrl);
        setupClient(workflowRestClient);
        
        siteTemplateRestClient = new PSSiteTemplateRestClient();
        setupClient(siteTemplateRestClient);
        
        pathRestClient = new PSPathServiceRestClient(baseUrl);
        setupClient(pathRestClient);
        
        site = createSite();
        pathSite = pathRestClient.find(site.getFolderPath().substring(1));
        
        workflow1 = workflowRestClient.getWorkflow(DEFAULT_WORKFLOW);
        workflow2 = workflowRestClient.getWorkflow(STANDARD_WORKFLOW);
    }
    
    @AfterClass
    public static void tearDown()
    {
        restClient.login("admin1", "demo");

        siteCleaner.clean();
    }
    
    private void assertFolderItem(PSFolderItem item, PSPathItem expectedItem, String expectedWorkflowName,
            boolean expectedAllChildren, int expectedChildrenSize)
    {
        if (!PSPathItem.TYPE_SITE.equals(expectedItem.getType()))
            assertEquals(expectedItem.getPath() + " : id", expectedItem.getId(), item.getId());
        assertEquals(expectedItem.getPath() + " : name", expectedItem.getName(), item.getName());
        assertEquals(expectedItem.getPath() + " : workflow name", expectedWorkflowName, item.getWorkflowName());
        assertEquals(expectedItem.getPath() + " : allChildrenAssociatedWithWorkflow", expectedAllChildren, item.getAllChildrenAssociatedWithWorkflow());
        assertNotNull(expectedItem.getPath() + " : children not null", item.getChildren());
        assertEquals(expectedItem.getPath() + " : children size", expectedChildrenSize, item.getChildren().size());
    }
    
    /**
     * @param string
     * @return
     */
    private PSPathItem addFolder(String string)
    {
        PSPathItem folder = pathRestClient.addFolder(site.getFolderPath().substring(1) + "/" + string);
        folderCleaner.add(folder.getPath());
        return folder;
    }

    @Test
    public void testGetAssignedFolders_WorkflowDoesNotExist() throws Exception
    {
        testGetAssignedFoldersShouldFail("non-existent workflow", "/Sites", 404);
    }

    @Test
    public void testGetAssignedFolders_PathDoesNotExist() throws Exception
    {
        testGetAssignedFoldersShouldFail(DEFAULT_WORKFLOW, "/Sites/DoesNoExist876", 500);
    }

    @Test
    public void testGetAssignedFolders_AllChildrenInTheSameWorkflow() throws Exception
    {
        PSPathItem folder1 =     addFolder("folder1");
        PSPathItem folder1_1 =   addFolder("folder1/folder1-1");
        PSPathItem folder1_1_1 = addFolder("folder1/folder1-1/folder1-1-1");
        PSPathItem folder2 =     addFolder("folder2");
        PSPathItem folder2_1 =   addFolder("folder2/folder-1");
        PSPathItem folder2_2 =   addFolder("folder2/folder-2");
        
        // Save workflow
        saveWorkflow(DEFAULT_WORKFLOW, folder1_1, folder1_1_1, folder2_1, folder2_2);
        
        List<PSFolderItem> folderItems =
                restClient.getAssociatedFolders(DEFAULT_WORKFLOW,
                        site.getFolderPath().substring(1),
                        false);
        
        assertNotNull("folderItems not null", folderItems);
        
        // level 1
        assertEquals("folderItems.size()", 2, folderItems.size());
        PSFolderItem folderItem1 = folderItems.get(0);
        PSFolderItem folderItem2 = folderItems.get(1);
        assertFolderItem(folderItem1, folder1, StringUtils.EMPTY, true, 1);
        assertFolderItem(folderItem2, folder2, StringUtils.EMPTY, true, 2);
        
        // level 2
        PSFolderItem folderItem1_1 = folderItem1.getChildren().get(0);
        PSFolderItem folderItem2_1 = folderItem2.getChildren().get(0);
        PSFolderItem folderItem2_2 = folderItem2.getChildren().get(1);
        assertFolderItem(folderItem1_1, folder1_1, DEFAULT_WORKFLOW, true, 1);
        assertFolderItem(folderItem2_1, folder2_1, DEFAULT_WORKFLOW, true, 0);
        assertFolderItem(folderItem2_2, folder2_2, DEFAULT_WORKFLOW, true, 0);
        
        // level 3
        PSFolderItem folderItem1_1_1 = folderItem1_1.getChildren().get(0);
        assertFolderItem(folderItem1_1_1, folder1_1_1, DEFAULT_WORKFLOW, true, 0);
    }

    @Test
    public void testGetAssignedFolders_SubfoldersDoNotBelongToWorkflow() throws Exception
    {
        PSPathItem folder1 =     addFolder("folder1");
        PSPathItem folder1_1 =   addFolder("folder1/folder1-1");
        PSPathItem folder1_1_1 = addFolder("folder1/folder1-1/folder1-1-1");
        
        // Save workflow
        saveWorkflow(STANDARD_WORKFLOW, folder1_1_1);
        
        List<PSFolderItem> folderItems =
                restClient.getAssociatedFolders(DEFAULT_WORKFLOW,
                        site.getFolderPath().substring(1),
                        false);
        
        assertNotNull("folderItems not null", folderItems);
        
        // level 1
        assertEquals("folderItems.size()", 0, folderItems.size());
    }
    
    @Test
    public void testGetAssignedFolders_SomeSubfoldersDoNotBelongToWorkflow() throws Exception
    {
        PSPathItem folder1 =     addFolder("folder1");
        PSPathItem folder1_1 =   addFolder("folder1/folder1-1");
        PSPathItem folder1_1_1 = addFolder("folder1/folder1-1/folder1-1-1");
        PSPathItem folder1_1_2 = addFolder("folder1/folder1-1/folder1-1-2");
        
        // Save workflow
        saveWorkflow(DEFAULT_WORKFLOW, folder1_1_1);
        
        List<PSFolderItem> folderItems =
                restClient.getAssociatedFolders(DEFAULT_WORKFLOW,
                        site.getFolderPath().substring(1),
                        false);
        
        assertNotNull("folderItems not null", folderItems);
        
        // level 1
        assertEquals("folderItems.size()", 1, folderItems.size());
        PSFolderItem folderItem1 = folderItems.get(0);
        assertFolderItem(folderItem1, folder1, StringUtils.EMPTY, false, 1);
        
        // level 2
        PSFolderItem folderItem1_1 = folderItem1.getChildren().get(0);
        assertFolderItem(folderItem1_1, folder1_1, StringUtils.EMPTY, false, 1);
        
        // level 3
        PSFolderItem folderItem1_1_1 = folderItem1_1.getChildren().get(0);
        assertFolderItem(folderItem1_1_1, folder1_1_1, DEFAULT_WORKFLOW, true, 0);
    }
    
    @Test
    public void testGetAssignedFolders_SiteIsAssigned() throws Exception
    {
        List<PSFolderItem> folderItems =
                restClient.getAssociatedFolders(DEFAULT_WORKFLOW,
                        "/Sites",
                        true);
        assertNotNull("folderItems not null", folderItems);
        
        PSFolderItem siteItem = null;
        for (PSFolderItem item : folderItems)
        {
            if (item.getName().equals(site.getName()))
            {
                assertFolderItem(item, pathSite, "", false, 0);
                siteItem = item;
                break;
            }
        }
        
        assertNotNull(siteItem);
        
        PSPathItem sitePathItem = new PSPathItem();
        sitePathItem.setId(siteItem.getId());
        sitePathItem.setName(siteItem.getName());
        
        // Save workflow
        saveWorkflow(DEFAULT_WORKFLOW, sitePathItem);
        
        // requery to ensure saved properly
        folderItems =
                restClient.getAssociatedFolders(DEFAULT_WORKFLOW,
                        "/Sites",
                        false);
        
        // level 1
        assertTrue("folderItems.size()", folderItems.size() >= 1);
        boolean siteExists = false;
        
        for (PSFolderItem item : folderItems)
        {
            if (item.getName().equals(site.getName()))
            {
                assertFolderItem(item, pathSite, DEFAULT_WORKFLOW, true, 0);
                siteExists = true;
            }
        }
        
        assertTrue(siteExists);
        
        saveWorkflow(DEFAULT_WORKFLOW, new PSPathItem[] {}, new PSPathItem[] { sitePathItem });
    }
    
    @Test
    public void testGetAssignedFolders_IncludeFoldersWithDifferentWorkflow() throws Exception
    {
        PSPathItem folder1 =     addFolder("folder1");
        PSPathItem folder1_1 =   addFolder("folder1/folder1-1");
        PSPathItem folder1_1_1 = addFolder("folder1/folder1-1/folder1-1-1");
        PSPathItem folder1_1_2 = addFolder("folder1/folder1-1/folder1-1-2");
        
        // Save workflow
        saveWorkflow(DEFAULT_WORKFLOW, folder1_1_1);
        
        List<PSFolderItem> folderItems =
                restClient.getAssociatedFolders(DEFAULT_WORKFLOW,
                        site.getFolderPath().substring(1),
                        true);
        
        assertNotNull("folderItems not null", folderItems);
        
        // level 1
        assertEquals("folderItems.size()", 1, folderItems.size());
        PSFolderItem folderItem1 = folderItems.get(0);
        assertFolderItem(folderItem1, folder1, StringUtils.EMPTY, false, 1);
        
        // level 2
        PSFolderItem folderItem1_1 = folderItem1.getChildren().get(0);
        assertFolderItem(folderItem1_1, folder1_1, StringUtils.EMPTY, false, 2);
        
        // level 3
        PSFolderItem folderItem1_1_1 = folderItem1_1.getChildren().get(0);
        assertFolderItem(folderItem1_1_1, folder1_1_1, DEFAULT_WORKFLOW, true, 0);
        PSFolderItem folderItem1_1_2 = folderItem1_1.getChildren().get(1);
        assertFolderItem(folderItem1_1_2, folder1_1_2, StringUtils.EMPTY, false, 0);
    }
    
    @Test
    public void testGetAssignedFolders_FolderDoesNotBelongToWorkflow_AllChildrenDo() throws Exception
    {
        PSPathItem folder1 =     addFolder("folder1");
        PSPathItem folder1_1 =   addFolder("folder1/folder1-1");
        PSPathItem folder1_1_1 = addFolder("folder1/folder1-1/folder1-1-1");
        
        // Save workflow
        saveWorkflow(DEFAULT_WORKFLOW, folder1, folder1_1_1);
        saveWorkflow(STANDARD_WORKFLOW, folder1_1);
        
        List<PSFolderItem> folderItems =
                restClient.getAssociatedFolders(DEFAULT_WORKFLOW,
                        site.getFolderPath().substring(1),
                        false);
        
        assertNotNull("folderItems not null", folderItems);
        
        // level 1
        assertEquals("folderItems.size()", 1, folderItems.size());
        PSFolderItem folderItem1 = folderItems.get(0);
        assertFolderItem(folderItem1, folder1, DEFAULT_WORKFLOW, false, 1);
        
        // level 2
        PSFolderItem folderItem1_1 = folderItem1.getChildren().get(0);
        assertFolderItem(folderItem1_1, folder1_1, STANDARD_WORKFLOW, true, 1);
        
        // level 3
        PSFolderItem folderItem1_1_1 = folderItem1_1.getChildren().get(0);
        assertFolderItem(folderItem1_1_1, folder1_1_1, DEFAULT_WORKFLOW, true, 0);
    }

    @Test
    public void testAssignFoldersToWorkflow_WorkflowNameIsNullOrEmptyOrDoesNotExist() throws Exception
    {
        PSPathItem folder1 = addFolder("folder1");
        
        saveWorkflowShouldFail(null, 400, folder1);
        
        saveWorkflowShouldFail(StringUtils.EMPTY, 400, folder1);
        
        saveWorkflowShouldFail("non-existent ", 404, folder1);
    }

    @Test
    public void testAssignFoldersToWorkflow_SomePathIsEmpty() throws Exception
    {
        PSPathItem folder1 =     addFolder("folder1");
        PSPathItem folder1_1 =   addFolder("folder1/folder1-1");
        PSPathItem folder1_1_1 = addFolder("folder1/folder1-1/folder1-1-1");
        PSPathItem folder1_1_2 = addFolder("folder1/folder1-1/folder1-1-2");
        
        // Save workflow
        saveWorkflow(DEFAULT_WORKFLOW, folder1_1_1.getId(), StringUtils.EMPTY);
        
        List<PSFolderItem> folderItems =
                restClient.getAssociatedFolders(DEFAULT_WORKFLOW,
                        site.getFolderPath().substring(1),
                        false);
        
        assertNotNull("folderItems not null", folderItems);
        
        // level 1
        assertEquals("folderItems.size()", 1, folderItems.size());
        PSFolderItem folderItem1 = folderItems.get(0);
        assertFolderItem(folderItem1, folder1, StringUtils.EMPTY, false, 1);
        
        // level 2
        PSFolderItem folderItem1_1 = folderItem1.getChildren().get(0);
        assertFolderItem(folderItem1_1, folder1_1, StringUtils.EMPTY, false, 1);
        
        // level 3
        PSFolderItem folderItem1_1_1 = folderItem1_1.getChildren().get(0);
        assertFolderItem(folderItem1_1_1, folder1_1_1, DEFAULT_WORKFLOW, true, 0);
    }

    @Test
    public void testAssignFoldersToWorkflow_SomePathDoesNotExist() throws Exception
    {
        PSPathItem folder1 =     addFolder("folder1");
        PSPathItem folder1_1 =   addFolder("folder1/folder1-1");
        PSPathItem folder1_1_1 = addFolder("folder1/folder1-1/folder1-1-1");
        PSPathItem folder1_1_2 = addFolder("folder1/folder1-1/folder1-1-2");
        
        // Save workflow
        saveWorkflow(DEFAULT_WORKFLOW, folder1_1_1.getId(), folder1_1_2.getId() + "876iuy");
        
        List<PSFolderItem> folderItems =
                restClient.getAssociatedFolders(DEFAULT_WORKFLOW,
                        site.getFolderPath().substring(1),
                        false);
        
        assertNotNull("folderItems not null", folderItems);
        
        // level 1
        assertEquals("folderItems.size()", 1, folderItems.size());
        PSFolderItem folderItem1 = folderItems.get(0);
        assertFolderItem(folderItem1, folder1, StringUtils.EMPTY, false, 1);
        
        // level 2
        PSFolderItem folderItem1_1 = folderItem1.getChildren().get(0);
        assertFolderItem(folderItem1_1, folder1_1, StringUtils.EMPTY, false, 1);
        
        // level 3
        PSFolderItem folderItem1_1_1 = folderItem1_1.getChildren().get(0);
        assertFolderItem(folderItem1_1_1, folder1_1_1, DEFAULT_WORKFLOW, true, 0);
    }
    
    @Test
    public void testAssignFolders_Unassingment_Successful() throws Exception
    {
        PSPathItem folder1 =     addFolder("folder1");
        PSPathItem folder1_1 =   addFolder("folder1/folder1-1");
        PSPathItem folder1_1_1 = addFolder("folder1/folder1-1/folder1-1-1");
        PSPathItem folder1_1_2 = addFolder("folder1/folder1-1/folder1-1-2");
        
        // Save workflow
        saveWorkflow(DEFAULT_WORKFLOW, folder1, folder1_1, folder1_1_1, folder1_1_2);
        
        List<PSFolderItem> folderItems =
                restClient.getAssociatedFolders(DEFAULT_WORKFLOW,
                        site.getFolderPath().substring(1),
                        false);
        
        // level 1
        assertEquals("folderItems.size()", 1, folderItems.size());
        PSFolderItem folderItem1 = folderItems.get(0);
        assertFolderItem(folderItem1, folder1, DEFAULT_WORKFLOW, true, 1);
        
        // level 2
        PSFolderItem folderItem1_1 = folderItem1.getChildren().get(0);
        assertFolderItem(folderItem1_1, folder1_1, DEFAULT_WORKFLOW, true, 2);
        
        // level 3
        PSFolderItem folderItem1_1_1 = folderItem1_1.getChildren().get(0);
        assertFolderItem(folderItem1_1_1, folder1_1_1, DEFAULT_WORKFLOW, true, 0);
        PSFolderItem folderItem1_1_2 = folderItem1_1.getChildren().get(1);
        assertFolderItem(folderItem1_1_2, folder1_1_2, DEFAULT_WORKFLOW, true, 0);
        
        
        
        // Unassign
        saveWorkflow(DEFAULT_WORKFLOW, null, new PSPathItem[] { folder1_1, folder1_1_1 });
        
        folderItems =
                restClient.getAssociatedFolders(DEFAULT_WORKFLOW,
                        site.getFolderPath().substring(1),
                        false);
        
        // level 1
        assertEquals("folderItems.size()", 1, folderItems.size());
        folderItem1 = folderItems.get(0);
        assertFolderItem(folderItem1, folder1, DEFAULT_WORKFLOW, false, 1);
        
        // level 2
        folderItem1_1 = folderItem1.getChildren().get(0);
        assertFolderItem(folderItem1_1, folder1_1, StringUtils.EMPTY, false, 1);
        
        // level 3
        folderItem1_1_2 = folderItem1_1.getChildren().get(0);
        assertFolderItem(folderItem1_1_2, folder1_1_2, DEFAULT_WORKFLOW, true, 0);
    }
    
    public void testAssignFolders_AssignmentAndUnassingment_Successful() throws Exception
    {
        PSPathItem folder1 =     addFolder("folder1");
        PSPathItem folder1_1 =   addFolder("folder1/folder1-1");
        PSPathItem folder1_1_1 = addFolder("folder1/folder1-1/folder1-1-1");
        PSPathItem folder1_1_2 = addFolder("folder1/folder1-1/folder1-1-2");
        
        // Save workflow
        saveWorkflow(DEFAULT_WORKFLOW, folder1_1, folder1_1_1);
        
        List<PSFolderItem> folderItems =
                restClient.getAssociatedFolders(DEFAULT_WORKFLOW,
                        site.getFolderPath().substring(1),
                        false);
        
        // level 1
        assertEquals("folderItems.size()", 1, folderItems.size());
        PSFolderItem folderItem1 = folderItems.get(0);
        assertFolderItem(folderItem1, folder1, StringUtils.EMPTY, false, 1);
        
        // level 2
        PSFolderItem folderItem1_1 = folderItem1.getChildren().get(0);
        assertFolderItem(folderItem1_1, folder1_1, DEFAULT_WORKFLOW, false, 1);
        
        // level 3
        PSFolderItem folderItem1_1_1 = folderItem1_1.getChildren().get(0);
        assertFolderItem(folderItem1_1_1, folder1_1_1, DEFAULT_WORKFLOW, true, 0);
        
        
        
        // Unassign
        saveWorkflow(STANDARD_WORKFLOW,
                new PSPathItem[] { folder1, folder1_1_2 },
                new PSPathItem[] { folder1_1, folder1_1_1 });
        
        folderItems =
                restClient.getAssociatedFolders(STANDARD_WORKFLOW,
                        site.getFolderPath().substring(1),
                        false);
        
        // level 1
        assertEquals("folderItems.size()", 1, folderItems.size());
        folderItem1 = folderItems.get(0);
        assertFolderItem(folderItem1, folder1, STANDARD_WORKFLOW, false, 1);
        
        // level 2
        folderItem1_1 = folderItem1.getChildren().get(0);
        assertFolderItem(folderItem1_1, folder1_1, StringUtils.EMPTY, false, 1);
        
        // level 3
        PSFolderItem folderItem1_1_2 = folderItem1_1.getChildren().get(0);
        assertFolderItem(folderItem1_1_2, folder1_1_2, STANDARD_WORKFLOW, true, 0);
    }
    
    @Test
    public void testJobGetAssignedFoldersBadWorkflow()
    {
        testJobGetAssignedFoldersShouldFail("non-existent workflow", "/Sites");
    }
    
    @Test
    public void testJobGetAssignedFolders()
    {
        List<PSFolderItem> folderItems =
                restClient.getAssociatedFolders(DEFAULT_WORKFLOW,
                        "/Sites",
                        true);
        assertNotNull("folderItems not null", folderItems);
        
        PSFolderItem siteItem = null;
        for (PSFolderItem item : folderItems)
        {
            if (item.getName().equals(site.getName()))
            {
                assertFolderItem(item, pathSite, "", false, 0);
                siteItem = item;
                break;
            }
        }
        
        assertNotNull(siteItem);
        
        PSPathItem sitePathItem = new PSPathItem();
        sitePathItem.setId(siteItem.getId());
        sitePathItem.setName(siteItem.getName());
        
        // Save workflow
        saveWorkflow(DEFAULT_WORKFLOW, sitePathItem);
        
        // requery to ensure saved properly using job        
        String jobId = restClient.startGetAssociatedFoldersJob(DEFAULT_WORKFLOW, "/Sites", true);
        PSGetAssignedFoldersJobStatus jobStatus = null;
        
        while (true)
        {
            jobStatus = restClient.getAssociatedFoldersJobResults(jobId);
            assertNotNull(jobStatus);
            assertEquals(jobId, String.valueOf(jobStatus.getJobId()));
            
            String status = jobStatus.getStatus();
            
            if (status.equals("-1"))
                fail("job aborted");
            else if (status.equals("100"))
                break;
            try
            {
                System.out.println("Sleeping...");

                Thread.sleep(1000);
            }
            catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        assertEquals("100", jobStatus.getStatus());
        
        folderItems = jobStatus.getFolderItems();
        assertNotNull("folderItems not null", folderItems);
        
        // level 1
        assertTrue("folderItems.size()", folderItems.size() >= 1);
        boolean siteExists = false;
        
        for (PSFolderItem item : folderItems)
        {
            if (item.getName().equals(site.getName()))
            {
                assertFolderItem(item, pathSite, DEFAULT_WORKFLOW, true, 0);
                siteExists = true;
            }
        }
        
        assertTrue(siteExists);
        
        saveWorkflow(DEFAULT_WORKFLOW, new PSPathItem[] {}, new PSPathItem[] { sitePathItem });
    }
    
    private void testJobGetAssignedFoldersShouldFail(String workflowName, String path)
    {
        try
        {
            restClient.startGetAssociatedFoldersJob(workflowName, path, false);

            fail("Should have thrown an exception");
        }
        catch (Exception e)
        {
            // noop
        }
    }
}
