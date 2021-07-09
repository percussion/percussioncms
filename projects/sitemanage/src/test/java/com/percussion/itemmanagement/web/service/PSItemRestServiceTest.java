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
package com.percussion.itemmanagement.web.service;

import static org.junit.Assert.assertEquals;

import com.percussion.assetmanagement.web.service.PSAssetServiceRestClient;
import com.percussion.itemmanagement.data.PSRevision;
import com.percussion.itemmanagement.data.PSRevisionsSummary;
import com.percussion.itemmanagement.service.IPSItemWorkflowService;
import com.percussion.pagemanagement.web.service.PSTestSiteData;
import com.percussion.share.test.PSRestTestCase;
import com.percussion.share.test.PSTestDataCleaner;

import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


public class PSItemRestServiceTest extends PSRestTestCase<PSItemServiceRestClient> {
    
    static PSItemServiceRestClient restClient;
    private static PSTestSiteData testSiteData;
    
    @BeforeClass
    public static void setUp() throws Exception
    {
        testSiteData = new PSTestSiteData();
        testSiteData.setUp();
    }
    
    @Override
    protected PSItemServiceRestClient getRestClient(String baseUrl)
    {
        restClient = new PSItemServiceRestClient(baseUrl);
        return restClient;
    }
    
    @Test
    public void testGetRevisions() throws Exception
    {
        // create a page
        String id = testSiteData.createPage("testPage", testSiteData.site1.getFolderPath(),
                testSiteData.template1.getId());

        // get revisions, should be one in "Draft"
        PSRevisionsSummary revSummary = restClient.getRevisions(id);
        List<PSRevision> revisions = revSummary.getRevisions();
        assertEquals(1, revisions.size());
        assertEquals("Draft", revisions.get(0).getStatus());
        // transition the page so that revisions are generated
        PSItemWorkflowServiceRestClient wfClient = getWorkflowClient();
        wfClient.transition(id, IPSItemWorkflowService.TRANSITION_TRIGGER_APPROVE);
        
        // get revisions, should be one in "Pending"
        revSummary = restClient.getRevisions(id);
        revisions = revSummary.getRevisions();
        assertEquals(1, revisions.size());
        assertEquals("Pending", revisions.get(0).getStatus());
        
        
        // transition to "Quick Edit"
        wfClient.checkOut(id);
       
        // check in to create a new revision
        wfClient.checkIn(id);
        
        // get revisions, should be two: "Pending," "Quick Edit"
        revSummary = restClient.getRevisions(id);
        revisions = revSummary.getRevisions();
        assertEquals(2, revisions.size());
        assertEquals("Pending", revisions.get(0).getStatus());
        assertEquals("Quick Edit", revisions.get(1).getStatus());
    }
    
    @Test
    public void testRestoreRevision() throws Exception
    {
        // create a page
        String id = testSiteData.createPage("testPage1", testSiteData.site1.getFolderPath(),
                testSiteData.template1.getId());

        // get revisions, should be one in "Draft"
        PSRevisionsSummary revSummary = restClient.getRevisions(id);
        List<PSRevision> revisions = revSummary.getRevisions();
        assertEquals(1, revisions.size());
        assertEquals("Draft", revisions.get(0).getStatus());
        
        // transition the page so that revisions are generated
        PSItemWorkflowServiceRestClient wfClient = getWorkflowClient();
        wfClient.transition(id, IPSItemWorkflowService.TRANSITION_TRIGGER_APPROVE);
        // create three more revisions
        wfClient.checkOut(id);
        wfClient.checkIn(id);
        wfClient.checkOut(id);
        wfClient.checkIn(id);
        wfClient.checkOut(id);
        wfClient.checkIn(id);
        //make sure we have 4 revisions now
        revSummary = restClient.getRevisions(id);
        revisions = revSummary.getRevisions();
        assertEquals(4, revisions.size());
        assertEquals(true,revSummary.isRestorable());
        
        //Reset the id to older revision and restore that revision
        String newId = "1-" + id.split("-")[1] + "-" + id.split("-")[2];
        restClient.restoreRevision(newId);
        
        //Make sure we have 5 revisions now
        revSummary = restClient.getRevisions(id);
        revisions = revSummary.getRevisions();
        assertEquals(5, revisions.size());
    }
    
    @AfterClass
    public static void tearDown() throws Exception
    {
        assetCleaner.clean();
        testSiteData.tearDown();
    }
    
    private static PSAssetServiceRestClient getAssetClient() throws Exception
    {
        PSAssetServiceRestClient client = new PSAssetServiceRestClient(baseUrl);
        setupClient(client);
        return client;
    }
    
    private static PSItemWorkflowServiceRestClient getWorkflowClient() throws Exception
    {
        PSItemWorkflowServiceRestClient client = new PSItemWorkflowServiceRestClient(baseUrl);
        setupClient(client);
        return client;
    }
    
    static PSTestDataCleaner<String> assetCleaner = new PSTestDataCleaner<String>()
    {
        @Override
        protected void clean(String id) throws Exception
        {
            getAssetClient().delete(id);
        }
    };
}
