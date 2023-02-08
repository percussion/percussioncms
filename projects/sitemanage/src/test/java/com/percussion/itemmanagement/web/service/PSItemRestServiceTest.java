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
