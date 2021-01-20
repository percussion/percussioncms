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
package com.percussion.itemmanagement.web.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.percussion.assetmanagement.data.PSAsset;
import com.percussion.assetmanagement.data.PSAssetWidgetRelationship;
import com.percussion.assetmanagement.data.PSAssetWidgetRelationship.PSAssetResourceType;
import com.percussion.assetmanagement.web.service.PSAssetServiceRestClient;
import com.percussion.itemmanagement.data.PSItemStateTransition;
import com.percussion.itemmanagement.data.PSItemTransitionResults;
import com.percussion.itemmanagement.data.PSItemUserInfo;
import com.percussion.pagemanagement.web.service.PSTestSiteData;
import com.percussion.services.workflow.data.PSAssignmentTypeEnum;
import com.percussion.share.test.PSRestTestCase;
import com.percussion.share.test.PSTestDataCleaner;
import com.percussion.share.test.PSRestClient.RestClientException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


public class PSItemWorkflowRestServiceTest extends PSRestTestCase<PSItemWorkflowServiceRestClient> {
    
    static PSItemWorkflowServiceRestClient restClient;
    private static PSTestSiteData testSiteData;
    
    @BeforeClass
    public static void setUp() throws Exception
    {
        testSiteData = new PSTestSiteData();
        testSiteData.setUp();
    }
    
    @Override
    protected PSItemWorkflowServiceRestClient getRestClient(String baseUrl)
    {
        restClient = new PSItemWorkflowServiceRestClient(baseUrl);
        return restClient;
    }
    
    @Test
    public void testCheckIn() throws Exception
    {
        // create test item
        PSAsset testAsset = getAssetClient().createAsset("testAsset", null);
        String id = testAsset.getId();
        assetCleaner.add(id);
        
        // multiple check-ins, same user (admin1)
        restClient.checkIn(id);
        restClient.checkIn(id);
        
        // check out
        restClient.checkOut(id);
        
        // switch to different admin user
        restClient.login("admin2", "demo");
        
        // force check in 
        restClient.checkIn(id);
        
        // switch to non-admin user
        restClient.login("editor1", "demo");
        
        // check out
        restClient.checkOut(id);
        
        // check in
        restClient.checkIn(id);
        
        // check out again
        restClient.checkOut(id);
        
        // switch to different non-admin user
        restClient.login("editor2", "demo");
        
        // try to check in, should fail
        try
        {
            restClient.checkIn(id);
            fail("Forced check-in should not be allowed for non-admin users");
        }
        catch (RestClientException e)
        {
            // expected
        }
        
        // switch to admin user
        restClient.login("admin1", "demo");
        
        // force check in
        restClient.checkIn(id);
    }
    
    @Test
    public void testCheckOut() throws Exception
    {
        PSAsset testAsset = getAssetClient().createAsset("testAsset", null);
        String id = testAsset.getId();
        assetCleaner.add(id);
        
        // check-in
        restClient.checkIn(id);
               
        // multiple check-outs, same user
        PSItemUserInfo info = restClient.checkOut(id);
        assertEquals(info, restClient.checkOut(id));
        String ADMIN = PSAssignmentTypeEnum.ADMIN.getLabel();
        assertTrue(info.getAssignmentType().equals(ADMIN));
               
        String origUser = info.getCheckOutUser();
        String newUser = "admin2";
        String password = "demo";
        
        // switch to different user
        restClient.login(newUser, password);
        
        // try to check-out item, should still be checked out by original user
        info = restClient.checkOut(id);
        assertEquals(origUser, info.getCheckOutUser());
                
        // switch back to original user
        restClient.login(origUser, password);
        
        // check-in item
        restClient.checkIn(id);
        
        // switch users again
        restClient.login(newUser, password);
        
        // should now be able to check-out item
        info = restClient.checkOut(id);
        assertEquals(newUser, info.getCheckOutUser());
        restClient.checkIn(id);
        
        // switch to non-admin user
        restClient.login("qa1", "demo");
        
        // item user info should be non-admin
        String assignmentType = restClient.checkOut(id).getAssignmentType();
        assertFalse(assignmentType.equals(ADMIN));
    }
       
    @Test
    public void testForceCheckOut() throws Exception
    {
        PSAsset testAsset = getAssetClient().createAsset("testAsset", null);
        String id = testAsset.getId();
        assetCleaner.add(id);
        
        // check out
        PSItemUserInfo info = restClient.checkOut(id);
        info.getCheckOutUser();
        
        // switch to different admin user
        String newUser = "admin2";
        restClient.login(newUser, "demo");
        
        // force check-out
        info = restClient.forceCheckOut(id);
        assertEquals(newUser, info.getCheckOutUser());
        
        // switch to different non-admin user
        restClient.login("editor1", "demo");
        
        // force check-out should fail
        try
        {
            restClient.forceCheckOut(id);
            fail("Forced check-out should not be allowed for non-admin users");
        }
        catch (RestClientException e)
        {
            // expected
        }
    }
    
    @Test
    public void testTransition() throws Exception
    {
        // create a page
        String id = testSiteData.createPage("testPage", testSiteData.site1.getFolderPath(),
                testSiteData.template1.getId());

        // create some shared assets
        PSAsset sharedAsset1 = getAssetClient().createAsset("sharedAsset1", "//Folders");
        String sharedAsset1Id = sharedAsset1.getId();
        assetCleaner.add(sharedAsset1Id);

        PSAsset sharedAsset2 = getAssetClient().createAsset("sharedAsset2", "//Folders");
        String sharedAsset2Id = sharedAsset2.getId();
        assetCleaner.add(sharedAsset2Id);

        // create a local asset
        PSAsset localAsset = getAssetClient().createAsset("localAsset", null);
        String localAssetId = localAsset.getId();
        assetCleaner.add(localAssetId);

        // add the assets to the page
        PSAssetWidgetRelationship awRel = new PSAssetWidgetRelationship(id, 5, "widget5", sharedAsset1Id, 1);
        awRel.setResourceType(PSAssetResourceType.shared);
        getAssetClient().createAssetWidgetRelationship(awRel);

        awRel = new PSAssetWidgetRelationship(id, 6, "widget6", sharedAsset2Id, 1);
        awRel.setResourceType(PSAssetResourceType.shared);
        getAssetClient().createAssetWidgetRelationship(awRel);

        awRel = new PSAssetWidgetRelationship(id, 7, "widget7", localAssetId, 1);
        getAssetClient().createAssetWidgetRelationship(awRel);

        PSItemStateTransition trans = restClient.getTransitions(id);
        assertTrue(trans != null);
        assertTrue(!trans.getTransitionTriggers().isEmpty());

        PSItemTransitionResults results = restClient.transition(id, trans.getTransitionTriggers().get(0));
        assertTrue(results != null);
        assertTrue(results.getItemId().equals(id));
        assertTrue(results.getFailedAssets().isEmpty());
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
    
    static PSTestDataCleaner<String> assetCleaner = new PSTestDataCleaner<String>()
    {
        @Override
        protected void clean(String id) throws Exception
        {
            getAssetClient().delete(id);
        }
    };
}
