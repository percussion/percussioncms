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

package com.percussion.itemmanagement.service;

import static com.percussion.webservices.PSWebserviceUtils.getItemSummary;
import static java.util.Arrays.asList;

import com.percussion.assetmanagement.data.PSAsset;
import com.percussion.assetmanagement.data.PSAssetWidgetRelationship;
import com.percussion.assetmanagement.data.PSAssetWidgetRelationship.PSAssetResourceType;
import com.percussion.assetmanagement.service.impl.PSWidgetAssetRelationshipService;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.itemmanagement.data.PSItemTransitionResults;
import com.percussion.itemmanagement.data.PSItemUserInfo;
import com.percussion.itemmanagement.service.IPSItemWorkflowService.PSItemWorkflowServiceException;
import com.percussion.itemmanagement.service.impl.PSWorkflowHelper;
import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.service.IPSPageService;
import com.percussion.pathmanagement.data.PSItemByWfStateRequest;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.legacy.IPSItemEntry;
import com.percussion.share.data.IPSItemSummary;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSWebserviceUtils;

import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.security.auth.login.LoginException;

public class PSItemWorkflowServiceTest extends PSItemWorkflowServiceTestBase
{
    public void testCheckIn() throws Exception
    {
        securityWs.login("admin1", "demo", "Enterprise_Investments", null);
        
        // create test page
        PSPage page = createPage("testPage", templateId);
        String pageId = pageService.save(page).getId();
        pageCleaner.add(pageId);
                
        // multiple check-ins, same user
        itemWorkflowService.checkIn(pageId);
        itemWorkflowService.checkIn(pageId);
        
        assertTrue(isCheckedIn(pageId));
        
        // create some local assets
        PSAsset asset = createAsset("localAsset1", null);
        String localAssetId1 = asset.getId();
        PSItemUserInfo info = itemWorkflowService.checkOut(localAssetId1);
        assertTrue(!StringUtils.isBlank(info.getCheckOutUser()));

        asset = createAsset("localAsset2", null);
        String localAssetId2 = asset.getId();
        itemWorkflowService.checkOut(localAssetId2);
        assertTrue(!StringUtils.isBlank(info.getCheckOutUser()));

        // create a shared asset
        asset = createAsset("sharedAsset", null);
        String sharedAssetId = asset.getId();
        assetCleaner.add(sharedAssetId);
        itemWorkflowService.checkOut(sharedAssetId);
        assertTrue(!StringUtils.isBlank(info.getCheckOutUser()));

        // check out page
        info = itemWorkflowService.checkOut(pageId);
        assertTrue(!StringUtils.isBlank(info.getCheckOutUser()));

        // add assets to page
        PSAssetWidgetRelationship awRel1 = new PSAssetWidgetRelationship(pageId, 5, "widget5", localAssetId1, 1);
        assertNotNull(assetService.createAssetWidgetRelationship(awRel1));
        
        PSAssetWidgetRelationship awRel2 = new PSAssetWidgetRelationship(pageId, 6, "widget6", localAssetId2, 1);
        assertNotNull(assetService.createAssetWidgetRelationship(awRel2));
        
        PSAssetWidgetRelationship awRel3 = new PSAssetWidgetRelationship(pageId, 7, "widget7", sharedAssetId, 1);
        awRel3.setResourceType(PSAssetResourceType.shared);
        assertNotNull(assetService.createAssetWidgetRelationship(awRel3));
        
        // check in page
        itemWorkflowService.checkIn(pageId);

        // page and local content should be checked in
        assertTrue(isCheckedIn(pageId));
        assertTrue(isCheckedIn(localAssetId1));
        assertTrue(isCheckedIn(localAssetId2));

        // shared asset should still be checked out
        assertTrue(!isCheckedIn(sharedAssetId));
        
        checkinPageWithOutOfSyncRelationship(pageId, localAssetId1);
    }
    
    private void checkinPageWithOutOfSyncRelationship(String pageId, String localAssetId) throws PSItemWorkflowServiceException {
        validateRevisions(pageId, localAssetId, 1, 1);
        validateLocalAssetRelationship(localAssetId, 1, pageId);
        
        itemWorkflowService.transition(pageId, IPSItemWorkflowService.TRANSITION_TRIGGER_APPROVE);
        
        validateRevisions(pageId, localAssetId, 1, 1);
        validateLocalAssetRelationship(localAssetId, 1, pageId);

        itemWorkflowService.checkOut(pageId);
        itemWorkflowService.checkOut(localAssetId);
        
        validateRevisions(pageId, localAssetId, 1, 2);
        PSRelationship rel = validateLocalAssetRelationship(localAssetId, 2, pageId);
        
        // prepare the relationship out of sync on purpose
        PSLocator dep = rel.getDependent();
        dep.setRevision(1);
        systemWs.saveRelationships(Collections.singletonList(rel));
        
        // should be able to check in with "out of sync" relationships
        itemWorkflowService.checkIn(pageId);
        
        validateRevisions(pageId, localAssetId, 2, 2);
        validateLocalAssetRelationship(localAssetId, 2, pageId);
    }

    private void validateRevisions(String pageId, String localAssetId, int curRevision, int tipRevision)
    {
        PSComponentSummary pageSummary;
        PSComponentSummary summary;
        pageSummary = getSummary(pageId);
        summary = getSummary(localAssetId);
        
        assertTrue(pageSummary.getCurrentLocator().getRevision() == curRevision);
        assertTrue(pageSummary.getTipLocator().getRevision() == tipRevision);
        assertTrue(summary.getCurrentLocator().getRevision() == curRevision);
        assertTrue(summary.getTipLocator().getRevision() == tipRevision);
    }
    
    public void testCheckOut() throws Exception
    {
        securityWs.login("admin1", "demo", "Enterprise_Investments", null);
        
        // create an asset
        PSAsset testAsset = createAsset("testAsset", null);
        String assetId = testAsset.getId();
        assetCleaner.add(assetId);
        
        // set revision lock
        PSComponentSummary summary = getSummary(assetId);
        summary.setRevisionLock(true);
        cmsObjectMgr.saveComponentSummaries(Collections.singletonList(summary));

        // create a page
        PSPage testPage = pageService.save(createPage("testPage", templateId));
        String pageId = testPage.getId();
        pageCleaner.add(pageId);
        
        // add the asset to the page as local content
        PSAssetWidgetRelationship awRel1 = new PSAssetWidgetRelationship(pageId, 5, "widget5", assetId, 1);
        String awRelId1 = assetService.createAssetWidgetRelationship(awRel1);
        relationshipCleaner.add(idMapper.getGuid(awRelId1));
        
        // check in the page (and asset)
        itemWorkflowService.checkIn(pageId);
        
        // check out the asset
        itemWorkflowService.checkOut(assetId);
        
        // check local content relationship revisions
        validateLocalAssetRelationship(assetId, 2, pageId);
    }

    private PSRelationship validateLocalAssetRelationship(String assetId, int assetRevision, String pageId)
    {
        PSRelationship rel = getLocalAssetRelationship(assetId, pageId);
        assertEquals(assetRevision, rel.getDependent().getRevision());
        return rel;
    }

    private PSRelationship getLocalAssetRelationship(String assetId, String pageId)
    {
        PSRelationshipFilter filter = new PSRelationshipFilter();
        filter.setName(PSWidgetAssetRelationshipService.LOCAL_ASSET_WIDGET_REL_FILTER);
        filter.setOwner(idMapper.getLocator(pageId));
        filter.limitToOwnerRevision(true);
        filter.setDependentId( (new PSLegacyGuid(assetId)).getContentId() );
        List<PSRelationship> rels = systemWs.loadRelationships(filter);
        PSRelationship lcRel = rels.get(0);
        return lcRel;
    }

    private PSComponentSummary getSummary(String assetId)
    {
        IPSGuid asset1Guid = idMapper.getGuid(assetId);
        int contentId = ((PSLegacyGuid) asset1Guid).getContentId();
        PSComponentSummary summary = getItemSummary(contentId);
        return summary;
    }
    
    public void testIsModifiableByUser() throws Exception
    {
        securityWs.login("admin1", "demo", "Enterprise_Investments", null);
        
        // create test page
        PSPage page = createPage("testPage", templateId);
        String pageId = pageService.save(page).getId();
        pageCleaner.add(pageId);
                
        // current user is Assignee, should have access to item
        assertTrue(itemWorkflowService.isModifiableByUser(pageId));
        
        // transition item to pending state
        itemWorkflowService.transition(pageId, IPSItemWorkflowService.TRANSITION_TRIGGER_APPROVE);
        
        // switch to unauthorized user
        securityWs.login("author1", "demo", "Enterprise_Investments", null);
        
        // should not have access to item
        assertFalse(itemWorkflowService.isModifiableByUser(pageId));
        
        // switch to Admin
        securityWs.login("admin1", "demo", "Enterprise_Investments", null);
        
        // should have access to item
        assertTrue(itemWorkflowService.isModifiableByUser(pageId));
    }
    
    public void testIsApproved() throws Exception
    {
        securityWs.login("admin1", "demo", "Enterprise_Investments", null);
        
        // create test page
        PSPage page = createPage("testPage", templateId);
        String pageId = pageService.save(page).getId();
        pageCleaner.add(pageId);
                
        // should not be approved
        assertFalse(workflowHelper.isApproved(pageId));
        
        // transition item to pending state
        itemWorkflowService.transition(pageId, IPSItemWorkflowService.TRANSITION_TRIGGER_APPROVE);
        
        // should be in approved state
        assertTrue(workflowHelper.isApproved(pageId));
        
        // transition item to live state        
        itemWorkflowService.transition(pageId, IPSItemWorkflowService.TRANSITION_TRIGGER_LIVE);
        
        // should be in approved state
        assertTrue(workflowHelper.isApproved(pageId));
        
        // transition item to quick edit state
        itemWorkflowService.checkOut(pageId);
                
        // should be in approved state
        assertTrue(workflowHelper.isApproved(pageId));
    }
    
    public void testIsPending() throws Exception
    {
        securityWs.login("admin1", "demo", "Enterprise_Investments", null);
        
        // create test page
        PSPage page = createPage("testPage", templateId);
        String pageId = pageService.save(page).getId();
        pageCleaner.add(pageId);
                
        // should not be pending
        assertFalse(workflowHelper.isPending(pageId));
        
        // transition item to pending state
        itemWorkflowService.transition(pageId, IPSItemWorkflowService.TRANSITION_TRIGGER_APPROVE);
        
        // should be pending
        assertTrue(workflowHelper.isPending(pageId));
    }
    
    public void testIsLive() throws Exception
    {
        securityWs.login("admin1", "demo", "Enterprise_Investments", null);
        
        // create test page
        PSPage page = createPage("testPage", templateId);
        String pageId = pageService.save(page).getId();
        pageCleaner.add(pageId);
                
        // should not be live
        assertFalse(workflowHelper.isLive(pageId));
        
        // transition item to pending state
        itemWorkflowService.transition(pageId, IPSItemWorkflowService.TRANSITION_TRIGGER_APPROVE);
        
        // should not be live
        assertFalse(workflowHelper.isLive(pageId));
        
        // transition item to live state
        itemWorkflowService.transition(pageId, IPSItemWorkflowService.TRANSITION_TRIGGER_LIVE);
        
        // should be live
        assertTrue(workflowHelper.isLive(pageId));
    }
    
    public void testIsQuickEdit() throws Exception
    {
        securityWs.login("admin1", "demo", "Enterprise_Investments", null);
        
        // create test page
        PSPage page = createPage("testPage", templateId);
        String pageId = pageService.save(page).getId();
        pageCleaner.add(pageId);
                
        // should not be quick edit
        assertFalse(workflowHelper.isQuickEdit(pageId));
        
        // transition item to pending state
        itemWorkflowService.transition(pageId, IPSItemWorkflowService.TRANSITION_TRIGGER_APPROVE);

        assertTrue(workflowHelper.isPending(pageId));

        // should not be quick edit
        assertFalse(workflowHelper.isQuickEdit(pageId));
        
        itemWorkflowService.transition(pageId, IPSItemWorkflowService.TRANSITION_TRIGGER_LIVE);
        
        assertTrue(workflowHelper.isLive(pageId));
        
        itemWorkflowService.checkOut(pageId);
        
        // should be quick edit
        assertTrue(workflowHelper.isQuickEdit(pageId));
    }
    
    /**
     * Test for bug CM-7876 
     * @throws Exception
     */
    public void testForceCheckinLocalAsset() throws Exception
    {
        securityWs.login("Editor", "demo", "Default", null);
        
        PSAsset localAsset = createAsset("localAsset", null);
        String localAssetId = localAsset.getId();
        assetCleaner.add(localAssetId);
                    
        // create a page
        PSPage page1 = createPage("page1", templateId);
        String page1Id = pageService.save(page1).getId();
        pageCleaner.add(page1Id);
        
        PSAssetWidgetRelationship awRel3 = new PSAssetWidgetRelationship(page1Id, 7, "widget7", localAssetId, 1);
        String awRelId3 = assetService.createAssetWidgetRelationship(awRel3);
        relationshipCleaner.add(idMapper.getGuid(awRelId3));
        
        // transition the page to Pending
        itemWorkflowService.transition(page1Id,IPSItemWorkflowService.TRANSITION_TRIGGER_APPROVE);
        
        // move the page to Quick Edit
        itemWorkflowService.transition(page1Id, IPSItemWorkflowService.TRANSITION_TRIGGER_EDIT);
                    
        //Check out the local asset        
        PSItemUserInfo info = itemWorkflowService.checkOut(page1Id);
        assertEquals("Editor", info.getCheckOutUser());
        info = itemWorkflowService.checkOut(localAssetId);
        assertEquals("Editor", info.getCheckOutUser());
        
        securityWs.login("Admin", "demo", "Default", null);

        info = itemWorkflowService.forceCheckOut(page1Id);
        assertEquals("Admin", info.getCheckOutUser());
        assertTrue(isCheckedIn(localAssetId));
    }
    
    public void testIsTipRevision() throws Exception
    {
        securityWs.login("admin1", "demo", "Enterprise_Investments", null);
        
        // create test page
        PSPage page = createPage("testPage", templateId);
        String pageId = pageService.save(page).getId();
        pageCleaner.add(pageId);
        
        // get the current revision guid
        IPSGuid pageGuid = contentDesignWs.getItemGuid(idMapper.getGuid(pageId));
        String pageGuidStr = idMapper.getString(pageGuid);
                
        // should be tip
        assertTrue(workflowHelper.isTipRevision(pageGuidStr));
        
        // transition item to pending
        itemWorkflowService.transition(pageId, IPSItemWorkflowService.TRANSITION_TRIGGER_APPROVE);

        // transition item to live
        itemWorkflowService.transition(pageId, IPSItemWorkflowService.TRANSITION_TRIGGER_LIVE);
        
        // check the item out
        itemWorkflowService.checkOut(pageId);
        
        // should not be tip
        assertFalse(workflowHelper.isTipRevision(pageGuidStr));
    }
    
    public void testGetApprovedPages() throws Exception
    {
        String assetId = null;
        
        String site1Path = fixture.site1.getFolderPath();
                    
        // create an asset under a site
        PSAsset asset = createAsset("asset", site1Path);
        assetId = asset.getId();
        assetCleaner.add(assetId);
                 
        // should not be used by approved page
        assertTrue(itemWorkflowService.getApprovedPages(assetId).isEmpty());
        assertTrue(itemWorkflowService.getApprovedPages(assetId, site1Path).isEmpty());
        
        // create a page
        PSPage page = createPage("page1", templateId);
        String pageId1 = pageService.save(page).getId();
        pageCleaner.add(pageId1);
                   
        // add the asset to the page
        PSAssetWidgetRelationship awRel1 = new PSAssetWidgetRelationship(pageId1, 5, "widget5", assetId, 1);
        awRel1.setResourceType(PSAssetResourceType.shared);
        String awRelId1 = assetService.createAssetWidgetRelationship(awRel1);
        relationshipCleaner.add(idMapper.getGuid(awRelId1));
        
        // should still not be used by approved page
        assertTrue(itemWorkflowService.getApprovedPages(assetId).isEmpty());
        assertTrue(itemWorkflowService.getApprovedPages(assetId, site1Path).isEmpty());
        
        // approve the page
        itemWorkflowService.transition(pageId1, IPSItemWorkflowService.TRANSITION_TRIGGER_APPROVE);
        
        // should be used by approved page
        assertEquals(1, itemWorkflowService.getApprovedPages(assetId).size());
        
        // should be used by approved page and page under site
        Set<String> pages = itemWorkflowService.getApprovedPages(assetId);
        IPSGuid pageGuid = contentDesignWs.getItemGuid(idMapper.getGuid(pageId1));
        String pageGuidStr = idMapper.getString(pageGuid);
        assertTrue(pages.contains(pageGuidStr));
        assertTrue(itemWorkflowService.getApprovedPages(assetId, site1Path).contains(pageGuidStr));
    }
    
    public void testTransition() throws Exception
    {
        securityWs.login("editor1", "demo", "Enterprise_Investments", null);
        
        // create some assets
        PSAsset sharedAsset1 = createAsset("sharedAsset1", null);
        String sharedAssetId1 = sharedAsset1.getId();
        assetCleaner.add(sharedAssetId1);
        itemWorkflowService.checkIn(sharedAssetId1);
        
        PSAsset sharedAsset2 = createAsset("sharedAsset2", null);
        String sharedAssetId2 = sharedAsset2.getId();
        assetCleaner.add(sharedAssetId2);
        itemWorkflowService.checkIn(sharedAssetId2);
        
        PSAsset localAsset = createAsset("localAsset", null);
        String localAssetId = localAsset.getId();
        assetCleaner.add(localAssetId);
                    
        PSAsset linkedAsset = createAsset("linkedAsset", null);
        String linkedAssetId = linkedAsset.getId();
        assetCleaner.add(linkedAssetId);
        itemWorkflowService.checkIn(linkedAssetId);
        
        PSAsset linkedAsset2 = createAsset("linkedAsset2", null);
        String linkedAssetId2 = linkedAsset2.getId();
        assetCleaner.add(linkedAssetId2);
        itemWorkflowService.checkIn(linkedAssetId2);
        
        // create a page
        PSPage page1 = createPage("page1", templateId);
        String page1Id = pageService.save(page1).getId();
        pageCleaner.add(page1Id);
        
        // create a linked page
        PSPage linkedPage = createPage("linkedPage", templateId);
        String linkedPageId = pageService.save(linkedPage).getId();
        pageCleaner.add(linkedPageId);
                   
        // add the assets to the page
        PSAssetWidgetRelationship awRel1 = new PSAssetWidgetRelationship(page1Id, 5, "widget5", sharedAssetId1, 1);
        awRel1.setResourceType(PSAssetResourceType.shared);
        String awRelId1 = assetService.createAssetWidgetRelationship(awRel1);
        relationshipCleaner.add(idMapper.getGuid(awRelId1));
        
        PSAssetWidgetRelationship awRel2 = new PSAssetWidgetRelationship(page1Id, 6, "widget6", sharedAssetId2, 1);
        awRel2.setResourceType(PSAssetResourceType.shared);
        String awRelId2 = assetService.createAssetWidgetRelationship(awRel2);
        relationshipCleaner.add(idMapper.getGuid(awRelId2));
        
        PSAssetWidgetRelationship awRel3 = new PSAssetWidgetRelationship(page1Id, 7, "widget7", localAssetId, 1);
        String awRelId3 = assetService.createAssetWidgetRelationship(awRel3);
        relationshipCleaner.add(idMapper.getGuid(awRelId3));
        
        // add the linked assets/page
        PSRelationship rel1 = systemWs.createRelationship(
                PSWidgetAssetRelationshipService.SHARED_ASSET_WIDGET_REL_TYPE, idMapper.getGuid(sharedAssetId1),
                idMapper.getGuid(linkedAssetId));
        systemWs.saveRelationships(Collections.singletonList(rel1));
        relationshipCleaner.add(rel1.getGuid());
        
        PSRelationship rel2 = systemWs.createRelationship(
                PSWidgetAssetRelationshipService.SHARED_ASSET_WIDGET_REL_TYPE, idMapper.getGuid(sharedAssetId2),
                idMapper.getGuid(linkedPageId));
        systemWs.saveRelationships(Collections.singletonList(rel2));
        relationshipCleaner.add(rel2.getGuid());
        
        PSRelationship rel3 = systemWs.createRelationship(
                PSWidgetAssetRelationshipService.SHARED_ASSET_WIDGET_REL_TYPE, idMapper.getGuid(localAssetId),
                idMapper.getGuid(linkedAssetId2));
        systemWs.saveRelationships(Collections.singletonList(rel3));
        relationshipCleaner.add(rel3.getGuid());
        
        // all assets should not be Pending
        assertFalse(workflowHelper.isPending(sharedAssetId1));
        assertFalse(workflowHelper.isPending(sharedAssetId2));
        assertFalse(workflowHelper.isPending(localAssetId));
        assertFalse(workflowHelper.isPending(linkedAssetId));
        assertFalse(workflowHelper.isPending(linkedAssetId2));
        
        // linked page should not be Pending
        assertFalse(workflowHelper.isPending(linkedPageId));
        
        // transition the page to Pending
        PSItemTransitionResults results = itemWorkflowService.transition(page1Id,
                IPSItemWorkflowService.TRANSITION_TRIGGER_APPROVE);
        assertTrue(results != null);
        assertTrue(results.getFailedAssets().isEmpty());
        
        // page should now be Pending
        assertTrue(workflowHelper.isPending(page1Id));
        
        // shared assets should now be Pending
        assertTrue(workflowHelper.isPending(sharedAssetId1));
        assertTrue(workflowHelper.isPending(sharedAssetId2));
        assertTrue(workflowHelper.isPending(linkedAssetId));
        assertTrue(workflowHelper.isPending(linkedAssetId2));
        
        // local asset should not be Pending
        assertFalse(workflowHelper.isPending(localAssetId));
        
        // linked page should not be Pending
        assertFalse(workflowHelper.isPending(linkedPageId));
        
        // move the page to Quick Edit
        results = itemWorkflowService.transition(page1Id, IPSItemWorkflowService.TRANSITION_TRIGGER_EDIT);
                    
        // page should no longer be Pending
        assertFalse(workflowHelper.isPending(page1Id));
        
        // checkout shared asset by different user
        securityWs.login("admin1", "demo", "Enterprise_Investments", null);
        PSItemUserInfo info = itemWorkflowService.checkOut(sharedAssetId2);
        assertEquals("admin1", info.getCheckOutUser());
        
        // transition page again
        securityWs.login("editor1", "demo", "Enterprise_Investments", null);
        results = itemWorkflowService.transition(page1Id, IPSItemWorkflowService.TRANSITION_TRIGGER_APPROVE);
        List<? extends IPSItemSummary> failedAssets = results.getFailedAssets();
        assertEquals(1, failedAssets.size());
        assertEquals(sharedAssetId2, failedAssets.get(0).getId());
                    
        // page should not be Pending
        assertFalse(workflowHelper.isPending(page1Id));
        
        // some shared assets should now be Pending
        assertTrue(workflowHelper.isPending(sharedAssetId1));
        assertTrue(workflowHelper.isPending(linkedAssetId));
        assertTrue(workflowHelper.isPending(linkedAssetId2));
        
        // local asset should not be Pending
        assertFalse(workflowHelper.isPending(localAssetId));
        
        // checked out asset should not be Pending
        assertFalse(workflowHelper.isPending(sharedAssetId2));
        
        // check in asset so it can be cleaned up
        securityWs.login("admin1", "demo", "Enterprise_Investments", null);
        itemWorkflowService.checkIn(sharedAssetId2);
    }
    
    public void testTransitionToPending() throws Exception
    {
        Set<String> assetIds = new HashSet<String>();
        
        try
        {
            // create some assets
            PSAsset asset1 = createAsset("asset1", null);
            String assetId1 = asset1.getId();
            assetIds.add(assetId1);
            
            PSAsset asset2 = createAsset("asset2", null);
            String assetId2 = asset2.getId();
            assetIds.add(assetId2);
                                
            // all assets should not be Pending
            assertFalse(workflowHelper.isPending(assetId1));
            assertFalse(workflowHelper.isPending(assetId2));
                        
            // transition the assets to Pending
            workflowHelper.transitionToPending(assetIds);
            
            // assets should now be Pending
            assertTrue(workflowHelper.isPending(assetId1));
            assertTrue(workflowHelper.isPending(assetId2));
        }
        finally
        {
            for (String assetId : assetIds)
            {                
                assetService.delete(assetId);
            }
        }
    }
    
    public void testIsPage() throws Exception
    {
        securityWs.login("admin1", "demo", "Enterprise_Investments", null);
        
        // create test page
        PSPage page = createPage("testPage", templateId);
        String pageId = pageService.save(page).getId();
        pageCleaner.add(pageId);

        // should be a page
        assertTrue(workflowHelper.isPage(pageId));

        // template should not be a page
        assertFalse(workflowHelper.isPage(templateId));
        
        // create test asset
        PSAsset asset = createAsset("asset", null);
        String assetId = asset.getId();
        assetCleaner.add(assetId);
        
        // should not be a page
        assertFalse(workflowHelper.isPage(assetId));
    }
    
    public void testIsTemplate() throws Exception
    {
        securityWs.login("admin1", "demo", "Enterprise_Investments", null);
        
        // create test page
        PSPage page = createPage("testPage", templateId);
        String pageId = pageService.save(page).getId();
        pageCleaner.add(pageId);

        // should not be a template
        assertFalse(workflowHelper.isTemplate(pageId));

        // should be a template
        assertTrue(workflowHelper.isTemplate(templateId));
        
        // create test asset
        PSAsset asset = createAsset("asset", null);
        String assetId = asset.getId();
        assetCleaner.add(assetId);
        
        // should not be a template
        assertFalse(workflowHelper.isTemplate(assetId));
    }
    
    public void testIsAsset() throws Exception
    {
        securityWs.login("admin1", "demo", "Enterprise_Investments", null);
        
        String assetId = null;
        
        // create test asset
        PSAsset asset = createAsset("testAsset", null);
        assetId = asset.getId();
        assetCleaner.add(assetId);
        
        // should be an asset
        assertTrue(workflowHelper.isAsset(assetId));
        
        // create test page
        PSPage page = createPage("page", templateId);
        String pageId = pageService.save(page).getId();
        pageCleaner.add(pageId);

        // should not be an asset
        assertFalse(workflowHelper.isAsset(pageId));

        // template should not be an asset
        assertFalse(workflowHelper.isAsset(templateId));
    }
    
    public void testTransitionRelatedNavigationItem() throws LoginException, IPSPageService.PSPageException, PSValidationException {
        securityWs.login("admin1", "demo", "Enterprise_Investments", null);
        
        PSPage homePage = fixture.getPageService().findPageByPath(fixture.site1.getFolderPath() + "/index.html");
        IPSGuid homePageId = idMapper.getGuid(homePage.getId());
        
        IPSGuid navId = navService.findRelatedNavigationNodeId(homePageId);
        int navContentId = ((PSLegacyGuid)navId).getContentId();
        int pageId = ((PSLegacyGuid) homePageId).getContentId();

        transitionPageAndNav(homePageId, IPSItemWorkflowService.TRANSITION_TRIGGER_APPROVE);
        assertPageNavItemState(pageId, navContentId, PSWorkflowHelper.WF_STATE_PENDING);

        transitionPageAndNav(homePageId, IPSItemWorkflowService.TRANSITION_TRIGGER_LIVE);
        assertPageNavItemState(pageId, navContentId, PSWorkflowHelper.WF_STATE_LIVE);

        // It should be fine to transition the related nav-item while the nav-item is already in "live" state
        try
        {
            workflowHelper.transitionRelatedNavigationItem(homePageId, IPSItemWorkflowService.TRANSITION_TRIGGER_LIVE);
            assertPageNavItemState(pageId, navContentId, PSWorkflowHelper.WF_STATE_LIVE);
        }
        catch (Exception ex)
        {
            fail("No exception should have been thrown");
        }
    }

    private void transitionPageAndNav(IPSGuid homePageId, String trigger)
    {
        systemWs.transitionItems(asList(homePageId), trigger);
        workflowHelper.transitionRelatedNavigationItem(homePageId, trigger);        
    }
    
    private void assertPageNavItemState(int pageId, int navContentId, String expectedState)
    {
        IPSItemEntry page = cmsObjectMgr.findItemEntry(pageId);
        IPSItemEntry nav = cmsObjectMgr.findItemEntry(navContentId);
        assertTrue(StringUtils.equalsIgnoreCase(page.getStateName(), expectedState));
        assertTrue(StringUtils.equalsIgnoreCase(nav.getStateName(), expectedState));
    }
    
    public void testGetWorkflowId() throws Exception
    {
        securityWs.login("admin1", "demo", "Enterprise_Investments", null);
        
        PSItemByWfStateRequest req = new PSItemByWfStateRequest();
        req.setWorkflow("Default Workflow");
        int workflowId = itemWorkflowService.getWorkflowId(req.getWorkflow());
        assertTrue(workflowId > 0);
        
        req.setWorkflow("Foo Workflow");
        
        try
        {
            itemWorkflowService.getWorkflowId(req.getWorkflow());
            fail("Workflow id was found for invalid workflow : " + req.getWorkflow());
        }
        catch (PSItemWorkflowServiceException e)
        {
            // expected
        }
    }
    
    public void testGetStateId() throws Exception
    {
        securityWs.login("admin1", "demo", "Enterprise_Investments", null);
        
        PSItemByWfStateRequest req = new PSItemByWfStateRequest();
        req.setWorkflow("Default Workflow");
        req.setState("Pending");
        int pendingStateId = itemWorkflowService.getStateId(req.getWorkflow(),req.getState());
        assertTrue(pendingStateId > 0);
        
        req.setState("Draft");
        int draftStateId = itemWorkflowService.getStateId(req.getWorkflow(),req.getState()); 
        assertTrue(draftStateId > 0 && draftStateId != pendingStateId);
        
        req.setState("Foo State");
        assertTrue(itemWorkflowService.getStateId(req.getWorkflow(),req.getState()) == -1);
    }
    
    public void testIsTriggerAvailable() throws Exception
    {
        securityWs.login("admin1", "demo", "Enterprise_Investments", null);
        
        // create test page
        PSPage page = createPage("testPage", templateId);
        String pageId = pageService.save(page).getId();
        pageCleaner.add(pageId);
                
        // Approve, Submit triggers should be available 
        assertTrue(itemWorkflowService.isTriggerAvailable(pageId, IPSItemWorkflowService.TRANSITION_TRIGGER_APPROVE));
        assertTrue(itemWorkflowService.isTriggerAvailable(pageId, IPSItemWorkflowService.TRANSITION_TRIGGER_SUBMIT));
        
        // all other triggers should not be available
        assertFalse(itemWorkflowService.isTriggerAvailable(pageId, IPSItemWorkflowService.TRANSITION_TRIGGER_REJECT));
        assertFalse(itemWorkflowService.isTriggerAvailable(pageId, IPSItemWorkflowService.TRANSITION_TRIGGER_EDIT));
        assertFalse(itemWorkflowService.isTriggerAvailable(pageId, IPSItemWorkflowService.TRANSITION_TRIGGER_LIVE));
        assertFalse(itemWorkflowService.isTriggerAvailable(pageId, IPSItemWorkflowService.TRANSITION_TRIGGER_ARCHIVE));
        
        // transition item to pending state
        itemWorkflowService.transition(pageId, IPSItemWorkflowService.TRANSITION_TRIGGER_APPROVE);

        // Edit trigger should be available
        assertTrue(itemWorkflowService.isTriggerAvailable(pageId, IPSItemWorkflowService.TRANSITION_TRIGGER_EDIT));
                
        // all other triggers should not be available
        assertFalse(itemWorkflowService.isTriggerAvailable(pageId, IPSItemWorkflowService.TRANSITION_TRIGGER_APPROVE));
        assertFalse(itemWorkflowService.isTriggerAvailable(pageId, IPSItemWorkflowService.TRANSITION_TRIGGER_SUBMIT));
        assertFalse(itemWorkflowService.isTriggerAvailable(pageId, IPSItemWorkflowService.TRANSITION_TRIGGER_ARCHIVE));
        assertFalse(itemWorkflowService.isTriggerAvailable(pageId, IPSItemWorkflowService.TRANSITION_TRIGGER_LIVE));
    }
       
    public void testIsApproveAvailableToCurrentUser() throws Exception
    {
        securityWs.login("admin1", "demo", "Enterprise_Investments", null);
        //Check whether admin1 has privileges to perform approve on the items
        assertTrue(itemWorkflowService.isApproveAvailableToCurrentUser(null));
        
        //Now login as editor1
        securityWs.login("editor1", "demo", "Enterprise_Investments", null);
        assertTrue(itemWorkflowService.isApproveAvailableToCurrentUser(null));
        
        //Now login as contributor
        securityWs.login("Contributor", "demo", "Default", null);
        assertFalse(itemWorkflowService.isApproveAvailableToCurrentUser(null));        
    }
    
    
    /**
     * Creates an asset.
     * 
     * @param name assumed not <code>null</code>.
     * @param folder assumed not <code>null</code>.
     * 
     * @return {@link PSAsset} representation of the asset item, never <code>null</code>.
     * 
     * @throws Exception if an error occurs saving the asset.
     */
    private PSAsset createAsset(String name, String folder) throws Exception
    {
        PSAsset asset = new PSAsset();
        asset.getFields().put("sys_title", name);
        asset.setType("percRawHtmlAsset");
        asset.getFields().put("html", "TestHTML");
        if (folder != null)
        {
            asset.setFolderPaths(asList(folder));
        }
             
        return assetService.save(asset);
    }
    
    private boolean isCheckedIn(String id)
    {
        int contentId = ((PSLegacyGuid) idMapper.getGuid(id)).getContentId();
        PSComponentSummary summary = PSWebserviceUtils.getItemSummary(contentId);
        return StringUtils.isEmpty(summary.getCheckoutUserName());
    }
    
}
