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
package com.percussion.itemmanagement;

import static java.util.Arrays.asList;

import com.percussion.assetmanagement.data.PSAbstractAssetRequest;
import com.percussion.assetmanagement.data.PSAbstractAssetRequest.AssetType;
import com.percussion.assetmanagement.data.PSAsset;
import com.percussion.assetmanagement.data.PSAssetWidgetRelationship;
import com.percussion.assetmanagement.data.PSBinaryAssetRequest;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSFolder;
import com.percussion.itemmanagement.data.PSItemTransitionResults;
import com.percussion.itemmanagement.service.IPSItemWorkflowService;
import com.percussion.itemmanagement.service.PSItemWorkflowServiceTestBase;
import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.data.PSTemplate;
import com.percussion.pagemanagement.service.IPSTemplateService;
import com.percussion.pathmanagement.service.impl.PSPathUtils;
import com.percussion.pubserver.IPSPubServerService;
import com.percussion.pubserver.data.PSPublishServerInfo;
import com.percussion.pubserver.data.PSPublishServerProperty;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.contentchange.IPSContentChangeService;
import com.percussion.services.contentchange.PSContentChangeServiceLocator;
import com.percussion.services.contentchange.data.PSContentChangeEvent;
import com.percussion.services.contentchange.data.PSContentChangeType;
import com.percussion.services.guidmgr.PSGuidUtils;
import com.percussion.services.pubserver.data.PSPubServer;
import com.percussion.sitemanage.data.PSSiteSummary;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.content.PSContentWsLocator;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.io.IOUtils;

public class PSLivePublishChangeHandlerTest extends PSItemWorkflowServiceTestBase
{

    private static final String ASSETS_TEST_FOLDER = "/Assets/Test";
    private IPSContentChangeService changeService;
    private IPSPubServerService pubServerService;
    

    public void setUp() throws Exception
    {
        super.setUp();
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        IPSContentWs contentWs = PSContentWsLocator.getContentWebservice();
        List<PSFolder> folders = contentWs.loadFolders(new String[]{PSPathUtils.getFolderPath(ASSETS_TEST_FOLDER)});
        if (!folders.isEmpty())
            contentWs.deleteFolders(Arrays.asList(folders.get(0).getGuid()), true);
    }

    public void test() throws Exception
    {
        securityWs.login("admin1", "demo", "Enterprise_Investments", null);

        changeService = PSContentChangeServiceLocator.getContentChangeService();
        long siteId = fixture.site1.getSiteId().longValue();
        
        try
        {
            // create page
            PSPage page = createPage("testPage", this.templateId);
            String pageId = this.pageService.save(page).getId();
            pageCleaner.add(pageId);
            
            
            
            // should not be published yet
            assertNothingToPublish(siteId);
      
            // Approve page
            PSItemTransitionResults results = itemWorkflowService.performApproveTransition(pageId, false, null);
            assertTrue(results.getFailedAssets().isEmpty());
            
            // Should be published
            assertSomethingToPublish(siteId, pageId);
            
            // quick edit, add schedule, and approve
            itemWorkflowService.transition(pageId, IPSItemWorkflowService.TRANSITION_TRIGGER_EDIT);
            PSComponentSummary sum = workflowHelper.getComponentSummary(pageId);
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, 1);
            sum.setContentStartDate(cal.getTime());
            cmsObjectMgr.saveComponentSummaries(Arrays.asList(sum));
            itemWorkflowService.performApproveTransition(pageId, false, null);
            
            // Should not be published
            assertNothingToPublish(siteId);
            
            // clear queue, reapprove, should still not be published
            clearChanges(siteId);
            itemWorkflowService.transition(pageId, IPSItemWorkflowService.TRANSITION_TRIGGER_EDIT);
            itemWorkflowService.performApproveTransition(pageId, false, null);
            assertNothingToPublish(siteId);
                    
            // edit and remove schedule, approve, should be published
            itemWorkflowService.transition(pageId, IPSItemWorkflowService.TRANSITION_TRIGGER_EDIT);
            sum = workflowHelper.getComponentSummary(pageId);
            sum.setContentStartDate(null);
            cmsObjectMgr.saveComponentSummaries(Arrays.asList(sum));
            itemWorkflowService.performApproveTransition(pageId, false, null);
            assertSomethingToPublish(siteId, pageId);
            
            // Archive page
            itemWorkflowService.transition(pageId, IPSItemWorkflowService.TRANSITION_TRIGGER_EDIT);
            itemWorkflowService.transition(pageId, IPSItemWorkflowService.TRANSITION_TRIGGER_ARCHIVE);
            
            // Should not be published
            assertNothingToPublish(siteId);
            
            // Revive and reapprove - should be published
            results = itemWorkflowService.transition(pageId, IPSItemWorkflowService.TRANSITION_TRIGGER_APPROVE);
            assertSomethingToPublish(siteId, pageId);
            
            // Delete page, should not be published
            pageService.delete(pageId, true);
            assertNothingToPublish(siteId);
            
            InputStream in = IOUtils.toInputStream("This is my file content");
            
            // Create resource
            PSAbstractAssetRequest ar = new PSBinaryAssetRequest(ASSETS_TEST_FOLDER, AssetType.FILE, "test.txt", "text/plain", in);
            PSAsset resource = assetService.createAsset(ar);
            String resourceId = resource.getId();
            
            
            // Should not be published
            assertNothingToPublish(siteId);
            
            // Approve resource
            results = itemWorkflowService.performApproveTransition(resourceId, false, null);
            
            // Should be published to each site, or each allowed site
            assertSomethingToPublish(siteId, resourceId);
            
            // Archive - resource not published
            itemWorkflowService.transition(resourceId, IPSItemWorkflowService.TRANSITION_TRIGGER_EDIT);
            itemWorkflowService.transition(resourceId, IPSItemWorkflowService.TRANSITION_TRIGGER_ARCHIVE);
            
            assertNothingToPublish(siteId);
            
            // Revive and approve - published
            itemWorkflowService.transition(resourceId, IPSItemWorkflowService.TRANSITION_TRIGGER_APPROVE);
            assertSomethingToPublish(siteId, resourceId);
            
            // Delete - not published
            assetService.delete(resourceId);
            assertNothingToPublish(siteId);
            
            // Create shared asset, add to new page
            page = createPage("testPage", this.templateId);
            pageId = this.pageService.save(page).getId();
            pageCleaner.add(pageId);
            int pageContentId = idMapper.getContentId(pageId);
            
            PSAsset htmlAsset = createAsset("htmltest", PSPathUtils.getFolderPath(ASSETS_TEST_FOLDER));
            String assetId = htmlAsset.getId();
            PSAssetWidgetRelationship awRel = new PSAssetWidgetRelationship(pageId, 5, "widget5", assetId, 1);
            String awRelId = assetService.createAssetWidgetRelationship(awRel);
            assertNotNull(awRelId);
            
            // Approve page, page should publish
            itemWorkflowService.performApproveTransition(pageId, false, null);
            assertSomethingToPublish(siteId, pageId);
            
            clearChanges(siteId);
            
            // transition page to live
            itemWorkflowService.transition(pageId, IPSItemWorkflowService.TRANSITION_TRIGGER_LIVE);
            assertNothingToPublish(siteId);
            
            // Approve asset - page should be published
            itemWorkflowService.performApproveTransition(assetId, false, null);
            assertSomethingToPublish(siteId, pageId);
            
            clearChanges(siteId);
            
            // Archive asset - page should be published
            itemWorkflowService.transition(assetId, IPSItemWorkflowService.TRANSITION_TRIGGER_EDIT);
            itemWorkflowService.transition(assetId, IPSItemWorkflowService.TRANSITION_TRIGGER_ARCHIVE);
            assertSomethingToPublish(siteId, pageId);
            
            clearChanges(siteId);
            
            // delete asset - page should be published
            assetService.delete(assetId);
            assertSomethingToPublish(siteId, pageId);
            
            // move page back to live
            itemWorkflowService.transition(pageId, IPSItemWorkflowService.TRANSITION_TRIGGER_EDIT);
            itemWorkflowService.performApproveTransition(pageId, false, null);
            assertSomethingToPublish(siteId, pageId);
            itemWorkflowService.transition(pageId, IPSItemWorkflowService.TRANSITION_TRIGGER_LIVE);
            clearChanges(siteId);
            
            // create and add asset to template - approve asset, live pages should be added
            htmlAsset = createAsset("htmltest", PSPathUtils.getFolderPath(ASSETS_TEST_FOLDER));
            assetId = htmlAsset.getId();
            assetCleaner.add(assetId);
            awRel = new PSAssetWidgetRelationship(templateId, 5, "widget5", assetId, 1);
            awRelId = assetService.createAssetWidgetRelationship(awRel);
            assertNotNull(awRelId);
            itemWorkflowService.performApproveTransition(assetId, false, null);
            assertSomethingToPublish(siteId, pageId);

            clearChanges(siteId);
            
            // archive asset, live pages should be published
            itemWorkflowService.transition(assetId, IPSItemWorkflowService.TRANSITION_TRIGGER_EDIT);
            itemWorkflowService.transition(assetId, IPSItemWorkflowService.TRANSITION_TRIGGER_ARCHIVE);
            assertSomethingToPublish(siteId, pageId);

            clearChanges(siteId);
            // resubmit asset, delete it, live pages should be published
            itemWorkflowService.transition(assetId, IPSItemWorkflowService.TRANSITION_TRIGGER_APPROVE);
            assertSomethingToPublish(siteId, pageId);
            clearChanges(siteId);
            assetService.delete(assetId);
            assertSomethingToPublish(siteId, pageId);

            clearChanges(siteId);
            
            // template change
            IPSTemplateService templateService = fixture.getTemplateService();
            PSTemplate template = templateService.load(templateId);
            templateService.save(template);
            assertNothingToPublish(siteId);
            
            // change pubserver props should clear for site
            createContentChange(siteId, pageContentId);
            assertSomethingToPublish(siteId, pageId);
            
            IPSGuid siteGuid = PSGuidUtils.makeGuid(siteId, PSTypeEnum.SITE);
            PSPubServer pubServer = pubServerService.getDefaultPubServer(siteGuid);
            PSPublishServerInfo pubServerInfo = pubServerService.getPubServer(siteGuid.toString(), pubServer.getGUID().toString());
            
            List<PSPublishServerProperty> props = pubServerInfo.getProperties();
            for (PSPublishServerProperty property : props)
            {
                if (property.getKey().equals("HTML"))
                    property.setValue("false");
                else if (property.getKey().equals("XML"))
                    property.setValue("true");
            }
            pubServerService.updatePubServer(siteGuid.toString(), pubServer.getGUID().toString(), pubServerInfo);
            assertNothingToPublish(siteId);
            
            // Create resource
            in = IOUtils.toInputStream("This is my file content");
            ar = new PSBinaryAssetRequest(ASSETS_TEST_FOLDER, AssetType.FILE, "test.txt", "text/plain", in);
            resource = assetService.createAsset(ar);
            resourceId = resource.getId();
            
            // Should not be published
            assertNothingToPublish(siteId);
            
            // Approve resource
            results = itemWorkflowService.performApproveTransition(resourceId, false, null);
            
            // should still not be published, since XML pub server
            assertNothingToPublish(siteId);            
            
            // site delete
            PSSiteSummary siteSum = fixture.createSite("LivePubChangeSiteDelTest", "Site");
            siteId = siteSum.getSiteId();
            createContentChange(siteId, 99998);
            createContentChange(siteId, 99999);
            List<Integer> changes = changeService.getChangedContent(siteId, PSContentChangeType.PENDING_LIVE);
            assertNotNull(changes);
            assertEquals(2, changes.size());
            
            fixture.getSiteDataService().delete(siteSum.getName());
            fixture.siteCleaner.remove(siteSum.getName());
            assertNothingToPublish(siteId);
        }
        finally
        {
            changeService.deleteChangeEventsForSite(siteId);
        }
        
    }

    private void createContentChange(long siteId, int contentId)
    {
        PSContentChangeEvent changeEvent;
        changeEvent = new PSContentChangeEvent();
        changeEvent.setChangeType(PSContentChangeType.PENDING_LIVE);
        changeEvent.setContentId(contentId);
        changeEvent.setSiteId(siteId);
        changeService.contentChanged(changeEvent);
    }

    private void clearChanges(long siteId)
    {
        changeService.deleteChangeEventsForSite(siteId);
        assertNothingToPublish(siteId);
    }

    private void assertNothingToPublish(long siteId)
    {
        List<Integer> changes = changeService.getChangedContent(siteId,
                PSContentChangeType.PENDING_LIVE);
        assertNotNull(changes);
        assertEquals(0, changes.size());
    }
    
    private void assertSomethingToPublish(long siteId, String itemId)
    {
        List<Integer> changes = changeService.getChangedContent(siteId,
                PSContentChangeType.PENDING_LIVE);
        assertNotNull(changes);
        assertEquals(1, changes.size());
        assertEquals(Integer.valueOf(idMapper.getContentId(itemId)), changes.get(0));
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
        asset.getFields().put("sys_title", name + System.currentTimeMillis());
        asset.setType("percRawHtmlAsset");
        asset.getFields().put("html", "TestHTML");
        if (folder != null)
        {
            asset.setFolderPaths(asList(folder));
        }
             
        return assetService.save(asset);
    }

    public void setPubServerService(IPSPubServerService pubServerService)
    {
        this.pubServerService = pubServerService;
    }
}
