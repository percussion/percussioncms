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

package com.percussion.assetmanagement.service;

import static com.percussion.webservices.PSWebserviceUtils.getItemSummary;
import static java.util.Arrays.asList;

import com.percussion.assetmanagement.dao.IPSAssetDao;
import com.percussion.assetmanagement.data.PSAsset;
import com.percussion.assetmanagement.data.PSAssetFolderRelationship;
import com.percussion.assetmanagement.data.PSAssetWidgetRelationship;
import com.percussion.assetmanagement.data.PSBinaryAssetRequest;
import com.percussion.assetmanagement.data.PSExtractedAssetRequest;
import com.percussion.assetmanagement.data.PSImageAssetReportLine;
import com.percussion.assetmanagement.data.PSReportFailedToRunException;
import com.percussion.assetmanagement.data.PSAbstractAssetRequest.AssetType;
import com.percussion.assetmanagement.data.PSAssetWidgetRelationship.PSAssetResourceType;
import com.percussion.assetmanagement.service.impl.PSWidgetAssetRelationshipService;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSFolder;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.itemmanagement.service.IPSItemWorkflowService;
import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.data.PSRegion;
import com.percussion.pagemanagement.data.PSRegionBranches;
import com.percussion.pagemanagement.data.PSRegionWidgets;
import com.percussion.pagemanagement.data.PSWidgetItem;
import com.percussion.pagemanagement.data.PSRegionNode.PSRegionOwnerType;
import com.percussion.pagemanagement.service.IPSPageService;
import com.percussion.pagemanagement.service.IPSTemplateService;
import com.percussion.pagemanagement.service.IPSWidgetService;
import com.percussion.pagemanagement.service.PSSiteDataServletTestCaseFixture;
import com.percussion.pathmanagement.data.PSFolderProperties;
import com.percussion.pathmanagement.service.impl.PSAssetPathItemService;
import com.percussion.pathmanagement.service.impl.PSPathUtils;
import com.percussion.services.content.data.PSItemSummary;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.share.dao.IPSFolderHelper;
import com.percussion.share.data.IPSItemSummary;
import com.percussion.share.service.IPSIdMapper;
import com.percussion.share.service.IPSDataService.DataServiceNotFoundException;
import com.percussion.share.spring.PSSpringWebApplicationContextUtils;
import com.percussion.test.PSServletTestCase;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.content.IPSContentDesignWs;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.security.IPSSecurityWs;
import com.percussion.webservices.system.IPSSystemWs;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Ignore;
import org.junit.experimental.categories.Category;

@Category(IntegrationTest.class)
public class PSAssetServiceTest extends PSServletTestCase
{

    private static final Logger log = LogManager.getLogger(PSAssetServiceTest.class);

    private static final String tempPrefix = "TemplateTest";
    private PSSiteDataServletTestCaseFixture fixture;
    private String templateId;
    
    @Override
    public void setUp() throws Exception
    {
        PSSpringWebApplicationContextUtils.injectDependencies(this);
        fixture = new PSSiteDataServletTestCaseFixture(request, response);
        fixture.setUp("Admin", "demo", "Default");
        // create a template owner
        templateId = fixture.template1.getId();
        //FB:IJU_SETUP_NO_SUPER NC 1-16-16
        super.setUp();
                    
    }
        
    @Override
    protected void tearDown() throws Exception
    {
        fixture.tearDown();
        fixture.templateCleanUp(tempPrefix);
    }

    public void testNonCompliantImageAssetsReport(){
    	try {
			List<PSImageAssetReportLine> report = assetService.findNonCompliantImageAssets();
			
		} catch (PSReportFailedToRunException e) {
			// TODO Auto-generated catch block
            log.error(e.getMessage());
            log.debug(e.getMessage(), e);
		}
    }
    
    public void testCreateAssetWidgetRelationship() throws Exception
    {
        String assetId = null;
        String awRelId = null;

        try
        {
            
            // create an asset
            PSAsset asset = createAsset("testAsset", "//Folders");
            assetId = asset.getId();
            assertNotNull(assetId);
            
            // asset widget relationship should not be there
            assertTrue(widgetAssetRelationshipService.getRelationshipOwners(assetId).isEmpty());
            
            // create an asset-widget relationship
            PSAssetWidgetRelationship awRel = new PSAssetWidgetRelationship(templateId, 5, "widget5", assetId, 1);
            awRelId = assetService.createAssetWidgetRelationship(awRel);
            assertNotNull(awRelId);
            
            // make sure it was saved correctly
            PSRelationship rel = getLocalRelationship(awRel.getOwnerId(), awRel.getWidgetId(), awRel.getAssetId());
            assertNotNull(rel);
            assertTrue(rel.getProperty(PSRelationshipConfig.PDU_INLINERELATIONSHIP) == null);
            assertEquals(rel.getProperty(PSWidgetAssetRelationshipService.ASSET_ORDER_PROP_NAME), 
                    String.valueOf(awRel.getAssetOrder()));
            
            // test get asset widget relationship owners
            Set<String> relOwners = widgetAssetRelationshipService.getRelationshipOwners(assetId);
            assertEquals(1, relOwners.size());
            IPSGuid ownerGuid = idMapper.getGuid(awRel.getOwnerId());
            ownerGuid = contentDesignWs.getItemGuid(ownerGuid);
            assertTrue(relOwners.contains(idMapper.getString(ownerGuid)));
        }
        finally
        {
            if (awRelId != null)
            {
                systemWs.deleteRelationships(asList(idMapper.getGuid(awRelId)));
            }
            
            if (assetId != null)
            {                
                assetDao.delete(assetId);
            }
            
        }
    }
    
    public void testShareLocalAsset() throws Exception
    {
        String assetId = null;
        String awRelId = null;
        String sharedAssetId = null;
        IPSGuid sharedRelGuid  = null;
        PSRelationship rel = null;

        try
        {
            
            // create an asset
            PSAsset localAsset = createAsset("testAsset", "//Folders");
            assetId = localAsset.getId();
            assertNotNull(assetId);
            
            // asset widget relationship should not be there
            assertTrue(widgetAssetRelationshipService.getRelationshipOwners(assetId).isEmpty());
            
            // create an asset-widget relationship
            PSAssetWidgetRelationship awRel = new PSAssetWidgetRelationship(templateId, 5, "widget5", assetId, 1);
            awRelId = assetService.createAssetWidgetRelationship(awRel);
            assertNotNull(awRelId);
            
            // make sure it was saved correctly
            rel = getLocalRelationship(awRel.getOwnerId(), awRel.getWidgetId(), awRel.getAssetId());
            assertNotNull(rel);
            
            // make sure not shared
            assertTrue(widgetAssetRelationshipService.getSharedAssets(awRel.getOwnerId()).isEmpty());
            
            // share it
            String name = "sharedAsset";
            String path = "Assets/uploads";
            String sharedRelId = assetService.shareLocalContent(name, path, awRel);
            assertNotNull(sharedRelId);
            
            // assert local relationship is deleted
            assertNull(getLocalRelationship(awRel.getOwnerId(), awRel.getWidgetId(), assetId));
            rel = null;
            
            // assert local content is deleted
            assertNull(assetDao.find(assetId));
            assetId = null;
            
            // assert new relationship is shared
            List<PSRelationship> rels = getSharedRelationships(awRel.getOwnerId());
            assertEquals(1, rels.size());
            // compare dependent of relationship with guid of shared item
            PSLocator dependentLocator = rels.get(0).getDependent();
            // Relationship dependent of shared is -1.  We exepct the revision to still be 1
            dependentLocator.setRevision(1);
            
            sharedAssetId = idMapper.getString(dependentLocator);
            assertEquals(sharedAssetId,sharedRelId);
            
            Set<String> sharedAssets = widgetAssetRelationshipService.getSharedAssets(awRel.getOwnerId()); 
            assertEquals(1, sharedAssets.size());
            sharedAssetId = sharedAssets.iterator().next();
            
            // assert shared asset is created 
            PSAsset sharedAsset = assetDao.find(sharedAssetId);
            assertNotNull(sharedAsset);
            
            // has the correct name
            assertEquals(name, sharedAsset.getName());
            
            // matches source, 
            assertEquals(localAsset.getLabel(), sharedAsset.getLabel());
            assertEquals(localAsset.getType(), sharedAsset.getType());
                        
            // is in the correct folder
            String folderPath = PSPathUtils.getFolderPath("/" + path);
            String fullPath = folderPath + "/" + name;
            boolean didThrow = false;
            IPSItemSummary assetItem = null;
            try
            {
                assetItem = folderHelper.findItem(fullPath);
            }
            catch (Exception e)
            {
                didThrow = true;
            }
            
            assertFalse("Failed to locate item with path: " + fullPath, didThrow);
            assertNotNull(assetItem);
            
            // and in the correct workflow
            IPSItemSummary folderSummary = folderHelper.findFolder(folderPath);
            PSFolderProperties folderProperties = folderHelper.findFolderProperties(folderSummary.getId());
            String wfId = String.valueOf(folderHelper.getValidWorkflowId(folderProperties));
            assertEquals(wfId, sharedAsset.getFields().get("sys_workflowid"));
            
        }
        finally
        {
            if (rel != null)
            {
                systemWs.deleteRelationships(asList(rel.getGuid()));
            }
            
            if (sharedRelGuid != null)
            {
                systemWs.deleteRelationships(asList(sharedRelGuid));
            }
            
            if (assetId != null)
            {                
                assetDao.delete(assetId);
            }
            
            if (sharedAssetId != null)
            {
                assetDao.delete(sharedAssetId);
            }
        }
    }

    public void testClearAssetWidgetRelationship() throws Exception
    {

        String assetId = null;
        List<IPSGuid> awRelIds = new ArrayList<IPSGuid>();
        
        try
        {
            
            // create an asset
            PSAsset asset = createAsset("testAsset", "//Folders");
            assetId = asset.getId();
            assertNotNull(assetId);
            
            // create a local asset-widget relationship
            PSAssetWidgetRelationship localRel = new PSAssetWidgetRelationship(templateId, 5, "widget5", assetId, 1);
            String localRelId = assetService.createAssetWidgetRelationship(localRel);
            assertNotNull(localRelId);
            awRelIds.add(idMapper.getGuid(localRelId));
            
            // make sure it was saved correctly
            String ownerId = localRel.getOwnerId();
            long widgetId = localRel.getWidgetId();
            String asstId = localRel.getAssetId();
            assertNotNull(getLocalRelationship(ownerId, widgetId, asstId));
                      
            // create a shared asset-widget relationship
            PSAssetWidgetRelationship sharedRel = new PSAssetWidgetRelationship(ownerId, 6, "widget6", asstId, 1);
            sharedRel.setResourceType(PSAssetResourceType.shared);
            String sharedRelId = assetService.createAssetWidgetRelationship(sharedRel);
            awRelIds.add(idMapper.getGuid(sharedRelId));
            
            // delete the shared relationship
            assetService.clearAssetWidgetRelationship(sharedRel);
            assertNull(getSharedRelationship(ownerId, widgetId, asstId));
            
            // asset item should still exist
            try
            {
                assetService.find(assetId);
            }
            catch (Exception e)
            {
                fail("Asset: " + assetId + " should exist.");
            }
            
            // delete the local relationship
            assetService.clearAssetWidgetRelationship(localRel);
            assertNull(getLocalRelationship(ownerId, widgetId, asstId));
            
            // asset item should also have been deleted
            try
            {
                assetService.find(assetId);
                fail("Asset: " + assetId + " should have been deleted.");
            }
            catch (Exception e)
            {
                // expected
            }
        }
        finally
        {
            try
            {
                systemWs.deleteRelationships(awRelIds);
            }
            catch (Exception e)
            {
                // already deleted
            }
                       
            if (assetId != null)
            {                
                try
                {
                    assetDao.delete(assetId);
                }
                catch (Exception e)
                {
                    // already deleted
                }
            }
        }
    }
       
    public void testAddAssetToFolder() throws Exception
    {

        String assetId = null;
        String folderPath;
        IPSGuid folderId = null;

        try
        {
            
            // create a page item which will act as the asset
            PSPage pageAsset = createPage("testPageAsset", templateId);
            assetId = pageService.save(pageAsset).getId();
            assertNotNull(assetId);
            
            // create a folder
            List<PSFolder> folders = contentWs.addFolderTree(PSAssetPathItemService.ASSET_ROOT + "/Test");
            assertEquals(1, folders.size());
            folderPath = folders.get(0).getFolderPath();
            folderId = folders.get(0).getGuid();
            
            // add asset to folder
            PSAssetFolderRelationship fr = new PSAssetFolderRelationship();
            fr.setAssetId(assetId);
            fr.setFolderPath(folderPath);
            assetService.addAssetToFolder(fr);
            
            // make sure it was added correctly
            List<PSItemSummary> items = contentWs.findFolderChildren(folderId, false);
            assertEquals(1, items.size());
            assertEquals(assetId, idMapper.getString(items.get(0).getGUID()));
        }
        finally
        {
            if (assetId != null)
            {
                // wait before deleting (to allow for search index queue processing)
                Thread.sleep(1000);
                
                pageService.delete(assetId);
            }
            
            if (folderId != null)
            {
                contentWs.deleteFolders(asList(folderId), false);
            }
            
        }
    }
    
    public void testRemoveAssetFromFolder() throws Exception
    {
        String assetId = null;
        String folderPath;
        IPSGuid folderId = null;

        try
        {
            // create a page item which will act as the asset
            PSPage pageAsset = createPage("testPageAsset", templateId);
            assetId = pageService.save(pageAsset).getId();
            assertNotNull(assetId);
            
            // create a folder
            List<PSFolder> folders = contentWs.addFolderTree(PSAssetPathItemService.ASSET_ROOT + "/Test");
            assertEquals(1, folders.size());
            folderPath = folders.get(0).getFolderPath();
            folderId = folders.get(0).getGuid();
            
            // add asset to folder
            PSAssetFolderRelationship fr = new PSAssetFolderRelationship();
            fr.setAssetId(assetId);
            fr.setFolderPath(folderPath);
            assetService.addAssetToFolder(fr);
            
            // make sure it was added correctly
            List<PSItemSummary> items = contentWs.findFolderChildren(folderId, false);
            assertEquals(1, items.size());
            assertEquals(assetId, idMapper.getString(items.get(0).getGUID()));
            
            // remove the asset
            assetService.removeAssetFromFolder(fr);
            
            // make sure it was removed from folder
            items = contentWs.findFolderChildren(folderId, false);
            assertTrue(items.isEmpty());
            
            // make sure the asset still exists
            try
            {
                pageService.find(assetId);
            }
            catch (DataServiceNotFoundException e)
            {
                fail("Asset: " + assetId + " should still exist");
            }
        }
        finally
        {
            if (assetId != null)
            {
                // wait before deleting (to allow for search index queue processing)
                Thread.sleep(1000);
                
                pageService.delete(assetId);
            }
            
            if (folderId != null)
            {
                contentWs.deleteFolders(asList(folderId), false);
            }
            
        }
    }
    
    public void testDeleteLocalAssets() throws Exception
    {
        List<String> assetIds = new ArrayList<String>();
        List<IPSGuid> awRelIds = new ArrayList<IPSGuid>();
        
        try
        {
            // delete local assets (shouldn't be any)
            widgetAssetRelationshipService.deleteLocalAssets(templateId);
            
            // create some local assets
            String asset1Id = createAsset("LocalAsset1", null).getId();
            assertNotNull(asset1Id);
            assetIds.add(asset1Id);
            String asset2Id = createAsset("LocalAsset2", null).getId();
            assertNotNull(asset2Id);
            assetIds.add(asset2Id);
            
            // create a shared asset
            String asset3Id = createAsset("SharedAsset1", "//Folders").getId();
            assertNotNull(asset3Id);
            assetIds.add(asset3Id);
            
            // add the assets to the template
            PSAssetWidgetRelationship awRel1 = new PSAssetWidgetRelationship(templateId, 5, "widget5", asset1Id, 1);
            String awRelId1 = assetService.createAssetWidgetRelationship(awRel1);
            assertNotNull(awRelId1);
            awRelIds.add(idMapper.getGuid(awRelId1));
            PSAssetWidgetRelationship awRel2 = new PSAssetWidgetRelationship(templateId, 6, "widget6", asset2Id, 2);
            String awRelId2 = assetService.createAssetWidgetRelationship(awRel2);
            assertNotNull(awRelId2);
            awRelIds.add(idMapper.getGuid(awRelId2));
            PSAssetWidgetRelationship awRel3 = new PSAssetWidgetRelationship(templateId, 7, "widget7", asset3Id, 3);
            awRel3.setResourceType(PSAssetResourceType.shared);
            String awRelId3 = assetService.createAssetWidgetRelationship(awRel3);
            assertNotNull(awRelId3);
            awRelIds.add(idMapper.getGuid(awRelId3));
            
            // delete the local assets
            widgetAssetRelationshipService.deleteLocalAssets(templateId);
            
            try
            {
                assetService.find(asset1Id);
                fail("Asset: " + asset1Id + " should have been deleted.");
            }
            catch (Exception e)
            {
                // expected
            }
                    
            try
            {
                assetService.find(asset2Id);
                fail("Asset: " + asset2Id + " should have been deleted.");
            }
            catch (Exception e)
            {
                // expected
            }
            
            try
            {
                assetService.find(asset3Id);
            }
            catch (Exception e)
            {
                fail("Asset: " + asset3Id + " should not have been deleted.");
            }
        }
        finally
        {
            try
            {
                systemWs.deleteRelationships(awRelIds);
            }
            catch (Exception e)
            {
                // already deleted
            }
                       
            for (String assetId : assetIds)
            {                
                try
                {
                    assetDao.delete(assetId);
                }
                catch (Exception e)
                {
                    // already deleted
                }
            }
        }
    }
    
    public void testCopyAssetWidgetRelationships() throws Exception
    {

        List<String> assetIds = new ArrayList<String>();
        List<IPSGuid> awRelIds = new ArrayList<IPSGuid>();
        
        try
        {
            
            // create a destination template
            String tempDestId = fixture.createTemplate(tempPrefix + '2').getId();
                        
            // copy relationships (shouldn't be any)
            widgetAssetRelationshipService.copyAssetWidgetRelationships(templateId, tempDestId);
            assertTrue(getLocalRelationships(tempDestId).isEmpty());
            assertTrue(getSharedRelationships(tempDestId).isEmpty());
            
            // create a local asset
            String asset1Id = createAsset("LocalAsset1", null).getId();
            assertNotNull(asset1Id);
            assetIds.add(asset1Id);
                        
            // create a shared asset
            String asset2Id = createAsset("SharedAsset1", "//Folders").getId();
            assertNotNull(asset2Id);
            assetIds.add(asset2Id);
            
            // add the assets to the source template
            PSAssetWidgetRelationship awRel1 = new PSAssetWidgetRelationship(templateId, 5, "widget5", asset1Id, 1);
            String awRelId1 = assetService.createAssetWidgetRelationship(awRel1);
            assertNotNull(awRelId1);
            awRelIds.add(idMapper.getGuid(awRelId1));
            PSAssetWidgetRelationship awRel2 = new PSAssetWidgetRelationship(templateId, 6, "widget6", asset2Id, 2);
            awRel2.setResourceType(PSAssetResourceType.shared);
            String awRelId2 = assetService.createAssetWidgetRelationship(awRel2);
            assertNotNull(awRelId2);
            awRelIds.add(idMapper.getGuid(awRelId2));
                        
            // no relationships should exist for the destination template
            assertTrue(getLocalRelationships(tempDestId).isEmpty());
            assertTrue(getSharedRelationships(tempDestId).isEmpty());
            
            // copy the asset relationships
            widgetAssetRelationshipService.copyAssetWidgetRelationships(templateId, tempDestId);
            
            // check the relationships
            List<PSRelationship> rels = getSharedRelationships(tempDestId); 
            assertEquals(1, rels.size());
            awRelIds.add(rels.get(0).getGuid());
            rels.addAll(getLocalRelationships(tempDestId));
            assertEquals(2, rels.size());
            awRelIds.add(rels.get(1).getGuid());  
            String localAssetId = idMapper.getString(rels.get(1).getDependent());
            assetIds.add(localAssetId);
            PSAsset asset1 = assetDao.find(asset1Id);
            PSAsset localAsset = assetDao.find(localAssetId);
            assertTrue(!localAssetId.equals(asset1Id));
            assertEquals(asset1.getType(), localAsset.getType());
            assertEquals(asset1.getFields().get("html"), localAsset.getFields().get("html"));
            
            // make sure source relationships still exist
            assertEquals(1, getLocalRelationships(templateId).size());
            assertEquals(1, getSharedRelationships(templateId).size());
        }
        finally
        {
            try
            {
                systemWs.deleteRelationships(awRelIds);
            }
            catch (Exception e)
            {
                // already deleted
            }
                       
            for (String assetId : assetIds)
            {                
                try
                {
                    assetDao.delete(assetId);
                }
                catch (Exception e)
                {
                    // already deleted
                }
            }
        }
    }
    
    public void testRemoveAssetWidgetRelationships() throws Exception
    {

        List<String> assetIds = new ArrayList<String>();
        List<IPSGuid> awRelIds = new ArrayList<IPSGuid>();
        
        try
        {

            // create some local assets
            String asset1Id = createAsset("LocalAsset1", null).getId();
            assertNotNull(asset1Id);
            assetIds.add(asset1Id);
            String asset2Id = createAsset("LocalAsset2", null).getId();
            assertNotNull(asset2Id);
            assetIds.add(asset2Id);
            
            // create a shared asset
            String asset3Id = createAsset("SharedAsset1", "//Folders").getId();
            assertNotNull(asset3Id);
            assetIds.add(asset3Id);
            
            // add the assets to the template
            PSAssetWidgetRelationship awRel1 = new PSAssetWidgetRelationship(templateId, 5, "widget5", asset1Id, 1);
            String awRelId1 = assetService.createAssetWidgetRelationship(awRel1);
            assertNotNull(awRelId1);
            awRelIds.add(idMapper.getGuid(awRelId1));
            PSAssetWidgetRelationship awRel2 = new PSAssetWidgetRelationship(templateId, 6, "widget6", asset2Id, 2);
            String awRelId2 = assetService.createAssetWidgetRelationship(awRel2);
            assertNotNull(awRelId2);
            awRelIds.add(idMapper.getGuid(awRelId2));
            PSAssetWidgetRelationship awRel3 = new PSAssetWidgetRelationship(templateId, 7, "widget7", asset3Id, 3);
            awRel3.setResourceType(PSAssetResourceType.shared);
            String awRelId3 = assetService.createAssetWidgetRelationship(awRel3);
            assertNotNull(awRelId3);
            awRelIds.add(idMapper.getGuid(awRelId3));
            
            // relationships should exist
            assertTrue(!getLocalRelationships(templateId).isEmpty());
            assertTrue(!getSharedRelationships(templateId).isEmpty());
            
            PSWidgetItem widget7 = new PSWidgetItem();
            widget7.setId(String.valueOf(awRel3.getWidgetId()));
            List<PSWidgetItem> widgets = new ArrayList<PSWidgetItem>();
            widgets.add(widget7);
            
            // remove the relationships
            widgetAssetRelationshipService.removeAssetWidgetRelationships(templateId, widgets);
            
            // local relationships should not exist
            assertTrue(getLocalRelationships(templateId).isEmpty());
            
            // shared relationship should still exist
            assertTrue(!getSharedRelationships(templateId).isEmpty());
            
            try
            {
                assetService.find(asset1Id);
                fail("Asset: " + asset1Id + " should have been deleted.");
            }
            catch (Exception e)
            {
                // expected
            }
                    
            try
            {
                assetService.find(asset2Id);
                fail("Asset: " + asset2Id + " should have been deleted.");
            }
            catch (Exception e)
            {
                // expected
            }
            
            widgets.clear();
            widgetAssetRelationshipService.removeAssetWidgetRelationships(templateId, widgets);
            
            // shared relationship should not exist
            assertTrue(getSharedRelationships(templateId).isEmpty());
            
            try
            {
                assetService.find(asset3Id);
            }
            catch (Exception e)
            {
                fail("Asset: " + asset3Id + " should not have been deleted.");
            }
        }
        finally
        {
            try
            {
                systemWs.deleteRelationships(awRelIds);
            }
            catch (Exception e)
            {
                // already deleted
            }
                       
            for (String assetId : assetIds)
            {                
                try
                {
                    assetDao.delete(assetId);
                }
                catch (Exception e)
                {
                    // already deleted
                }
            }
        }
    }
    
    public void testGetLocalAssets() throws Exception
    {
        List<String> assetIds = new ArrayList<String>();
        List<IPSGuid> awRelIds = new ArrayList<IPSGuid>();
        
        try
        {
            // get local assets (shouldn't be any)
            Set<String> localAssets = widgetAssetRelationshipService.getLocalAssets(templateId);
            assertTrue(localAssets.isEmpty());
            
            // create some local assets
            String asset1Id = createAsset("LocalAsset1", null).getId();
            assertNotNull(asset1Id);
            assetIds.add(asset1Id);
            String asset2Id = createAsset("LocalAsset2", null).getId();
            assertNotNull(asset2Id);
            assetIds.add(asset2Id);
            
            // create a shared asset
            String asset3Id = createAsset("SharedAsset1", "//Folders").getId();
            assertNotNull(asset3Id);
            assetIds.add(asset3Id);
            
            // add the assets to the template
            PSAssetWidgetRelationship awRel1 = new PSAssetWidgetRelationship(templateId, 5, "widget5", asset1Id, 1);
            String awRelId1 = assetService.createAssetWidgetRelationship(awRel1);
            assertNotNull(awRelId1);
            awRelIds.add(idMapper.getGuid(awRelId1));
            PSAssetWidgetRelationship awRel2 = new PSAssetWidgetRelationship(templateId, 6, "widget6", asset2Id, 2);
            String awRelId2 = assetService.createAssetWidgetRelationship(awRel2);
            assertNotNull(awRelId2);
            awRelIds.add(idMapper.getGuid(awRelId2));
            PSAssetWidgetRelationship awRel3 = new PSAssetWidgetRelationship(templateId, 7, "widget7", asset3Id, 3);
            awRel3.setResourceType(PSAssetResourceType.shared);
            String awRelId3 = assetService.createAssetWidgetRelationship(awRel3);
            assertNotNull(awRelId3);
            awRelIds.add(idMapper.getGuid(awRelId3));
            
            // get the local assets
            localAssets = widgetAssetRelationshipService.getLocalAssets(templateId);
            assertEquals(2, localAssets.size());
            assertTrue(localAssets.contains(getUpdatedRevisionId(asset1Id)));
            assertTrue(localAssets.contains(getUpdatedRevisionId(asset2Id)));
        }
        finally
        {
            try
            {
                systemWs.deleteRelationships(awRelIds);
            }
            catch (Exception e)
            {
                // already deleted
            }
                       
            for (String assetId : assetIds)
            {                
                try
                {
                    assetDao.delete(assetId);
                }
                catch (Exception e)
                {
                    // already deleted
                }
            }
        }
    }
    
    public void testGetSharedAssets() throws Exception
    {
        List<String> assetIds = new ArrayList<String>();
        List<IPSGuid> awRelIds = new ArrayList<IPSGuid>();
        
        try
        {
            // get shared assets (shouldn't be any)
            Set<String> sharedAssets = widgetAssetRelationshipService.getSharedAssets(templateId);
            assertTrue(sharedAssets.isEmpty());
            
            // create some shared assets
            String asset1Id = createAsset("SharedAsset1", "//Folders").getId();
            assertNotNull(asset1Id);
            assetIds.add(asset1Id);
            
            String asset2Id = createAsset("SharedAsset2", "//Folders").getId();
            assertNotNull(asset2Id);
            assetIds.add(asset2Id);
            
            // create a local asset
            String asset3Id = createAsset("LocalAsset1", null).getId();
            assertNotNull(asset3Id);
            assetIds.add(asset3Id);
                         
            // add the assets to the template
            PSAssetWidgetRelationship awRel1 = new PSAssetWidgetRelationship(templateId, 5, "widget5", asset1Id, 1);
            awRel1.setResourceType(PSAssetResourceType.shared);
            String awRelId1 = assetService.createAssetWidgetRelationship(awRel1);
            assertNotNull(awRelId1);
            awRelIds.add(idMapper.getGuid(awRelId1));
            PSAssetWidgetRelationship awRel2 = new PSAssetWidgetRelationship(templateId, 6, "widget6", asset2Id, 2);
            awRel2.setResourceType(PSAssetResourceType.shared);
            String awRelId2 = assetService.createAssetWidgetRelationship(awRel2);
            assertNotNull(awRelId2);
            awRelIds.add(idMapper.getGuid(awRelId2));
            PSAssetWidgetRelationship awRel3 = new PSAssetWidgetRelationship(templateId, 7, "widget7", asset3Id, 3);
            String awRelId3 = assetService.createAssetWidgetRelationship(awRel3);
            assertNotNull(awRelId3);
            awRelIds.add(idMapper.getGuid(awRelId3));
            
            // get the shared assets
            sharedAssets = widgetAssetRelationshipService.getSharedAssets(templateId);
            assertEquals(2, sharedAssets.size());
            assertTrue(sharedAssets.contains(asset1Id));
            assertTrue(sharedAssets.contains(asset2Id));
        }
        finally
        {
            try
            {
                systemWs.deleteRelationships(awRelIds);
            }
            catch (Exception e)
            {
                // already deleted
            }
                       
            for (String assetId : assetIds)
            {                
                try
                {
                    assetDao.delete(assetId);
                }
                catch (Exception e)
                {
                    // already deleted
                }
            }
        }
    }
    
    public void testGetLinkedAssets() throws Exception
    {
        List<String> assetIds = new ArrayList<String>();
        List<IPSGuid> awRelIds = new ArrayList<IPSGuid>();
        
        try
        {
            // get linked assets (shouldn't be any)
            Set<String> linkedAssets = widgetAssetRelationshipService.getLinkedAssets(templateId);
            assertTrue(linkedAssets.isEmpty());
            
            // create a shared asset
            String asset1Id = createAsset("SharedAsset1", "//Folders").getId();
            assertNotNull(asset1Id);
            assetIds.add(asset1Id);
                        
            // create a local asset
            String asset2Id = createAsset("LocalAsset1", null).getId();
            assertNotNull(asset2Id);
            assetIds.add(asset2Id);
            
            // create some linked assets
            String asset3Id = createAsset("LinkedAsset1", "//Folders").getId();
            assertNotNull(asset3Id);
            assetIds.add(asset3Id);
            
            String asset4Id = createAsset("LinkedAsset2", "//Folders").getId();
            assertNotNull(asset4Id);
            assetIds.add(asset4Id);
            
            // add the assets to the template
            PSAssetWidgetRelationship awRel1 = new PSAssetWidgetRelationship(templateId, 5, "widget5", asset1Id, 1);
            awRel1.setResourceType(PSAssetResourceType.shared);
            String awRelId1 = assetService.createAssetWidgetRelationship(awRel1);
            assertNotNull(awRelId1);
            awRelIds.add(idMapper.getGuid(awRelId1));
            PSAssetWidgetRelationship awRel2 = new PSAssetWidgetRelationship(templateId, 6, "widget6", asset2Id, 2);
            String awRelId2 = assetService.createAssetWidgetRelationship(awRel2);
            assertNotNull(awRelId2);
            awRelIds.add(idMapper.getGuid(awRelId2));
            
            // add the linked assets
            PSRelationship rel = systemWs.createRelationship(
                    PSWidgetAssetRelationshipService.SHARED_ASSET_WIDGET_REL_TYPE, idMapper.getGuid(asset1Id), 
                    idMapper.getGuid(asset3Id));
            systemWs.saveRelationships(Collections.singletonList(rel));
            rel = systemWs.createRelationship(
                    PSWidgetAssetRelationshipService.SHARED_ASSET_WIDGET_REL_TYPE, idMapper.getGuid(asset2Id), 
                    idMapper.getGuid(asset4Id));
            systemWs.saveRelationships(Collections.singletonList(rel));
            
            // get the linked assets
            linkedAssets = widgetAssetRelationshipService.getLinkedAssets(templateId);
            assertEquals(2, linkedAssets.size());
            assertTrue(linkedAssets.contains(asset3Id));
            assertTrue(linkedAssets.contains(asset4Id));
        }
        finally
        {
            try
            {
                systemWs.deleteRelationships(awRelIds);
            }
            catch (Exception e)
            {
                // already deleted
            }
                       
            for (String assetId : assetIds)
            {                
                try
                {
                    assetDao.delete(assetId);
                }
                catch (Exception e)
                {
                    // already deleted
                }
            }
        }
    }
    
    public void testGetLinkedAssetsForAsset() throws Exception
    {
        List<String> assetIds = new ArrayList<String>();
        String pageId = null;
        
        try
        {
            // create a local asset
            String assetId = createAsset("LocalAsset", null).getId();
            assertNotNull(assetId);
            assetIds.add(assetId);
            
            // get linked assets (shouldn't be any)
            Set<String> linkedAssets = widgetAssetRelationshipService.getLinkedAssetsForAsset(assetId);
            assertTrue(linkedAssets.isEmpty());
            
            // create a shared asset
            String asset1Id = createAsset("SharedAsset1", "//Folders").getId();
            assertNotNull(asset1Id);
            assetIds.add(asset1Id);
                        
            // create a local asset
            String asset2Id = createAsset("LocalAsset1", null).getId();
            assertNotNull(asset2Id);
            assetIds.add(asset2Id);
               
            // add the assets to the asset
            PSRelationship rel = systemWs.createRelationship(
                    PSWidgetAssetRelationshipService.SHARED_ASSET_WIDGET_REL_TYPE, idMapper.getGuid(assetId), 
                    idMapper.getGuid(asset1Id));
            systemWs.saveRelationships(Collections.singletonList(rel));
            rel = systemWs.createRelationship(
                    PSWidgetAssetRelationshipService.LOCAL_ASSET_WIDGET_REL_TYPE, idMapper.getGuid(assetId), 
                    idMapper.getGuid(asset2Id));
            systemWs.saveRelationships(Collections.singletonList(rel));
                        
            // get linked assets (shouldn't be any)
            linkedAssets = widgetAssetRelationshipService.getLinkedAssetsForAsset(assetId);
            assertTrue(linkedAssets.isEmpty());
            
            // create some linked assets
            String asset3Id = createAsset("LinkedAsset1", "//Folders").getId();
            assertNotNull(asset3Id);
            assetIds.add(asset3Id);
            
            String asset4Id = createAsset("LinkedAsset2", "//Folders").getId();
            assertNotNull(asset4Id);
            assetIds.add(asset4Id);
            
            // add the linked assets
            rel = systemWs.createRelationship(
                    PSWidgetAssetRelationshipService.SHARED_ASSET_WIDGET_REL_TYPE, idMapper.getGuid(assetId), 
                    idMapper.getGuid(asset3Id));
            rel.setProperty(PSRelationshipConfig.PDU_INLINERELATIONSHIP, "inline");
            systemWs.saveRelationships(Collections.singletonList(rel));
            rel = systemWs.createRelationship(
                    PSWidgetAssetRelationshipService.SHARED_ASSET_WIDGET_REL_TYPE, idMapper.getGuid(assetId), 
                    idMapper.getGuid(asset4Id));
            rel.setProperty(PSRelationshipConfig.PDU_INLINERELATIONSHIP, "inline");
            systemWs.saveRelationships(Collections.singletonList(rel));
            
            // get the linked assets
            linkedAssets = widgetAssetRelationshipService.getLinkedAssetsForAsset(assetId);
            assertEquals(2, linkedAssets.size());
            assertTrue(linkedAssets.contains(asset3Id));
            assertTrue(linkedAssets.contains(asset4Id));
            
            // create a linked page
            PSPage linkedPage = createPage("LinkedPage", templateId);
            pageId = pageService.save(linkedPage).getId();
            assertNotNull(pageId);
            
            // add the linked page
            rel = systemWs.createRelationship(
                    PSWidgetAssetRelationshipService.SHARED_ASSET_WIDGET_REL_TYPE, idMapper.getGuid(assetId), 
                    idMapper.getGuid(pageId));
            rel.setProperty(PSRelationshipConfig.PDU_INLINERELATIONSHIP, "inline");
            systemWs.saveRelationships(Collections.singletonList(rel));
            
            // get the linked assets, page should not be included
            linkedAssets = widgetAssetRelationshipService.getLinkedAssetsForAsset(assetId);
            assertEquals(2, linkedAssets.size());
            assertTrue(!linkedAssets.contains(pageId));
        }
        finally
        {
            for (String assetId : assetIds)
            {                
                try
                {
                    assetDao.delete(assetId);
                }
                catch (Exception e)
                {
                    // already deleted
                }
            }
            
            if (pageId != null)
            {
                pageService.delete(pageId);
            }
        }
    }
    
    public void testGetResourceAssets() throws Exception
    {
        List<String> assetIds = new ArrayList<String>();
        List<IPSGuid> awRelIds = new ArrayList<IPSGuid>();
        InputStream in = null;
        
        try
        {
            // get resource assets (shouldn't be any)
            Set<String> resourceAssets = widgetAssetRelationshipService.getResourceAssets(templateId);
            assertTrue(resourceAssets.isEmpty());
            
            // create a non-resource asset
            String asset1Id = createAsset("NonResourceAsset", "//Folders").getId();
            assertNotNull(asset1Id);
            assetIds.add(asset1Id);
            
            // create a file resource asset
            String folderPath = "/Assets/file";
            String fileName = "file.txt";
            AssetType type = AssetType.FILE;
            String mimeType = "text/plain";
            in = getClass().getResourceAsStream(fileName);
            PSBinaryAssetRequest req = new PSBinaryAssetRequest(folderPath, type, fileName, mimeType, in);
            String asset2Id = assetService.createAsset(req).getId();
            assertNotNull(asset2Id);
            assetIds.add(asset2Id);
            
            in.close();
            
            // create an image resource asset
            folderPath = "/Assets/image";
            fileName = "image.JPG";
            type = AssetType.IMAGE;
            mimeType = "image/jpeg";
            in = getClass().getResourceAsStream(fileName);
            req = new PSBinaryAssetRequest(folderPath, type, fileName, mimeType, in);
            String asset3Id = assetService.createAsset(req).getId();
            assertNotNull(asset3Id);
            assetIds.add(asset3Id);
            
            in.close();
                         
            // add the non-resource asset to the template
            PSAssetWidgetRelationship awRel1 = new PSAssetWidgetRelationship(templateId, 5, "widget5", asset1Id, 1);
            awRel1.setResourceType(PSAssetResourceType.shared);
            String awRelId1 = assetService.createAssetWidgetRelationship(awRel1);
            assertNotNull(awRelId1);
            awRelIds.add(idMapper.getGuid(awRelId1));
            
            // should not be any resource assets
            assertTrue(widgetAssetRelationshipService.getResourceAssets(templateId).isEmpty());
            
            // add the resource assets to the template
            PSAssetWidgetRelationship awRel2 = new PSAssetWidgetRelationship(templateId, 6, "widget6", asset2Id, 2);
            awRel2.setResourceType(PSAssetResourceType.shared);
            String awRelId2 = assetService.createAssetWidgetRelationship(awRel2);
            assertNotNull(awRelId2);
            awRelIds.add(idMapper.getGuid(awRelId2));
            PSAssetWidgetRelationship awRel3 = new PSAssetWidgetRelationship(templateId, 7, "widget7", asset3Id, 3);
            awRel3.setResourceType(PSAssetResourceType.shared);
            String awRelId3 = assetService.createAssetWidgetRelationship(awRel3);
            assertNotNull(awRelId3);
            awRelIds.add(idMapper.getGuid(awRelId3));
            
            // get the resource assets
            resourceAssets = widgetAssetRelationshipService.getResourceAssets(templateId);
            assertEquals(2, resourceAssets.size());
            assertTrue(resourceAssets.contains(asset2Id));
            assertTrue(resourceAssets.contains(asset3Id));
        }
        finally
        {
            try
            {
                systemWs.deleteRelationships(awRelIds);
            }
            catch (Exception e)
            {
                // already deleted
            }
                       
            for (String assetId : assetIds)
            {                
                try
                {
                    assetDao.delete(assetId);
                }
                catch (Exception e)
                {
                    // already deleted
                }
            }
            
            if (in != null)
            {
                in.close();
            }
        }
    }
    
    public void testIsUsedByTemplate() throws Exception
    {
        String assetId = null;
        List<IPSGuid> awRelIds = new ArrayList<IPSGuid>();
        String pageId = null;
        
        try
        {
            // create an asset
            assetId = createAsset("asset", "//Folders").getId();
                     
            // should not be used by template
            assertFalse(widgetAssetRelationshipService.isUsedByTemplate(assetId));
            
            // create a page
            PSPage page = createPage("page", templateId);
            pageId = pageService.save(page).getId();
                       
            // add the asset to the page
            PSAssetWidgetRelationship awRel1 = new PSAssetWidgetRelationship(pageId, 5, "widget5", assetId, 1);
            awRel1.setResourceType(PSAssetResourceType.shared);
            String awRelId1 = assetService.createAssetWidgetRelationship(awRel1);
            awRelIds.add(idMapper.getGuid(awRelId1));
            
            // should still not be used by template
            assertFalse(widgetAssetRelationshipService.isUsedByTemplate(assetId));
            
            // add the asset to a template            
            PSAssetWidgetRelationship awRel2 = new PSAssetWidgetRelationship(templateId, 6, "widget6", assetId, 2);
            awRel2.setResourceType(PSAssetResourceType.shared);
            String awRelId2 = assetService.createAssetWidgetRelationship(awRel2);
            awRelIds.add(idMapper.getGuid(awRelId2));
            
            // should be used by template
            assertTrue(widgetAssetRelationshipService.isUsedByTemplate(assetId));
        }
        finally
        {
            try
            {
                systemWs.deleteRelationships(awRelIds);
            }
            catch (Exception e)
            {
                // already deleted
            }
                       
            if (assetId != null)
            {                
                assetDao.delete(assetId);
            }
            
            if (pageId != null)
            {
                pageService.delete(pageId);
            }
        }
    }
    
    public void testUpdateLocalRelationship() throws Exception
    {
        List<String> assetIds = new ArrayList<String>();
        List<IPSGuid> awRelIds = new ArrayList<IPSGuid>();
        
        try
        {
            // create a local asset
            String asset1Id = createAsset("LocalAsset1", null).getId();
            assertNotNull(asset1Id);
            assetIds.add(asset1Id);
                        
            // set revision lock
            IPSGuid asset1Guid = idMapper.getGuid(asset1Id);
            int contentId = ((PSLegacyGuid) asset1Guid).getContentId();
            PSComponentSummary summary = getItemSummary(contentId);
            summary.setRevisionLock(true);
            cmsObjectMgr.saveComponentSummaries(Collections.singletonList(summary));
            
            // add the asset to the template
            PSAssetWidgetRelationship awRel1 = new PSAssetWidgetRelationship(templateId, 5, "widget5", asset1Id, 1);
            String awRelId1 = assetService.createAssetWidgetRelationship(awRel1);
            assertNotNull(awRelId1);
            awRelIds.add(idMapper.getGuid(awRelId1));
                        
            // get current locator
            PSLocator assetLocator = idMapper.getLocator(asset1Id);
                        
            // set the local relationship dependent
            widgetAssetRelationshipService.updateLocalRelationshipAsset(asset1Id);
            
            // should be equal
            assertEquals(assetLocator, getLocalRelationships(templateId).get(0).getDependent());
            
            // change the revision
            itemWorkflowService.checkIn(asset1Id);
            contentWs.prepareForEdit(asset1Guid);
            
            // update
            widgetAssetRelationshipService.updateLocalRelationshipAsset(asset1Id);
            
            // should be equal
            assertEquals(idMapper.getLocator(asset1Id), getLocalRelationships(templateId).get(0).getDependent());
        }
        finally
        {
            try
            {
                systemWs.deleteRelationships(awRelIds);
            }
            catch (Exception e)
            {
                // already deleted
            }
                       
            for (String assetId : assetIds)
            {                
                try
                {
                    assetDao.delete(assetId);
                }
                catch (Exception e)
                {
                    // already deleted
                }
            }
        }
    }
    
    public void testCreateBinaryAssets() throws Exception
    {
        List<String> assetIds = new ArrayList<String>();
        List<IPSGuid> folderIds = new ArrayList<IPSGuid>();
        InputStream in = null;
        try
        {
            // test file assets
            String folderPath = "/Assets/file";
            String fileName = "file.txt";
            AssetType type = AssetType.FILE;
            String mimeType = "text/plain";
            in = getClass().getResourceAsStream(fileName);
            PSBinaryAssetRequest req = new PSBinaryAssetRequest(folderPath, type, fileName, mimeType, in);

            // create the asset
            PSAsset newAsset = assetService.createAsset(req);
            in.close();
            assetIds.add(newAsset.getId());
            assertEquals("percFileAsset", newAsset.getType());
            assertEquals(fileName, newAsset.getName());
            Map<String, Object> fields = newAsset.getFields();
            assertEquals(mimeType, fields.get("item_file_attachment_type"));
            String newAssetFolderPath = newAsset.getFolderPaths().get(0);
            folderIds.add(contentWs.getIdByPath(newAssetFolderPath));
            assertEquals(folderPath, PSPathUtils.getFinderPath(newAssetFolderPath));

            // create asset with same file (should be renamed)
            in = getClass().getResourceAsStream(fileName);
            req = new PSBinaryAssetRequest(folderPath, type, fileName, mimeType, in);
            newAsset = assetService.createAsset(req);
            in.close();
            assetIds.add(newAsset.getId());
            assertEquals("file-1.txt", newAsset.getName());
            
            // create asset with same file (should be renamed again)
            in = getClass().getResourceAsStream(fileName);
            req = new PSBinaryAssetRequest(folderPath, type, fileName, mimeType, in);
            newAsset = assetService.createAsset(req);
            in.close();
            assetIds.add(newAsset.getId());
            assertEquals("file-2.txt", newAsset.getName());
            
            // test flash assets
            folderPath = "/Assets/flash";
            fileName = "flash.swf";
            type = AssetType.FLASH;
            mimeType = "application/x-shockwave-flash";
            in = getClass().getResourceAsStream(fileName);
            req = new PSBinaryAssetRequest(folderPath, type, fileName, mimeType, in);

            // create the asset
            newAsset = assetService.createAsset(req);
            in.close();
            assetIds.add(newAsset.getId());
            assertEquals("percFlashAsset", newAsset.getType());
            assertEquals(fileName, newAsset.getName());
            fields = newAsset.getFields();
            assertEquals(mimeType, fields.get("item_file_attachment_type"));
            newAssetFolderPath = newAsset.getFolderPaths().get(0);
            folderIds.add(contentWs.getIdByPath(newAssetFolderPath));
            assertEquals(folderPath, PSPathUtils.getFinderPath(newAssetFolderPath));

            // create asset with same file (should be renamed)
            in = getClass().getResourceAsStream(fileName);
            req = new PSBinaryAssetRequest(folderPath, type, fileName, mimeType, in);
            newAsset = assetService.createAsset(req);
            in.close();
            assetIds.add(newAsset.getId());
            assertEquals("flash-1.swf", newAsset.getName());
            
            // test image assets
            folderPath = "/Assets/image";
            fileName = "image.JPG";
            type = AssetType.IMAGE;
            mimeType = "image/jpeg";
            in = getClass().getResourceAsStream(fileName);
            req = new PSBinaryAssetRequest(folderPath, type, fileName, mimeType, in);

            // create the asset
            newAsset = assetService.createAsset(req);
            in.close();
            assetIds.add(newAsset.getId());
            assertEquals("percImageAsset", newAsset.getType());
            assertEquals(fileName, newAsset.getName());
            fields = newAsset.getFields();
            assertEquals(mimeType, fields.get("img_type"));
            newAssetFolderPath = newAsset.getFolderPaths().get(0);
            folderIds.add(contentWs.getIdByPath(newAssetFolderPath));
            assertEquals(folderPath, PSPathUtils.getFinderPath(newAssetFolderPath));

            // create asset with same file (should be renamed)
            in = getClass().getResourceAsStream(fileName);
            req = new PSBinaryAssetRequest(folderPath, type, fileName, mimeType, in);
            newAsset = assetService.createAsset(req);
            in.close();
            assetIds.add(newAsset.getId());
            assertEquals("image-1.JPG", newAsset.getName());
        }
        finally
        {
            for (String assetId : assetIds)
            {
                assetService.delete(assetId);
            }
            
            if (!folderIds.isEmpty())
            {
                contentWs.deleteFolders(folderIds, false);
            }
            
            if (in != null)
            {
                in.close();
            }
        }
    }
    
    public void testUpdateBinaryAssets() throws Exception
    {
        List<String> assetIds = new ArrayList<String>();
        List<IPSGuid> folderIds = new ArrayList<IPSGuid>();
        InputStream in = null;
        try
        {
            // test image assets
            String folderPath = "/Assets/uploads/images ";
            String fileName = "image.JPG";
            AssetType type = AssetType.IMAGE;
            String mimeType = "image/jpeg";
            in = getClass().getResourceAsStream(fileName);
            PSBinaryAssetRequest req = new PSBinaryAssetRequest(folderPath, type, fileName, mimeType, in);

            // create the asset
            PSAsset newAsset = assetService.createAsset(req);
            in.close();
            String itemId = newAsset.getId();
            assetIds.add(newAsset.getId());
            assertEquals("percImageAsset", newAsset.getType());
            assertEquals(fileName, newAsset.getName());
            Map<String, Object>  fields = newAsset.getFields();
            assertEquals(mimeType, fields.get("img_type"));
            String newAssetFolderPath = newAsset.getFolderPaths().get(0);
            folderIds.add(contentWs.getIdByPath(newAssetFolderPath));
            assertEquals(folderPath, PSPathUtils.getFinderPath(newAssetFolderPath));

            // update the asset with same name and new file
            in = getClass().getResourceAsStream(fileName);
            req = new PSBinaryAssetRequest(folderPath, type, fileName, mimeType, in);
            newAsset = assetService.updateAsset(itemId, req, false);
            assertEquals(itemId, newAsset.getId());
            assertEquals("image.JPG", newAsset.getName());
        }
        finally
        {
            for (String assetId : assetIds)
            {
                assetService.delete(assetId);
            }
            
            if (!folderIds.isEmpty())
            {
                contentWs.deleteFolders(folderIds, false);
            }
            
            if (in != null)
            {
                in.close();
            }
        }
    }
    
    public void testCreateExtractedAssets() throws Exception
    {
        List<String> assetIds = new ArrayList<String>();
        List<IPSGuid> folderIds = new ArrayList<IPSGuid>();
        InputStream in = null;
        try
        {
            // test html assets
            String folderPath = "/Assets/html";
            String fileName = "html.html";
            AssetType type = AssetType.HTML;
            String selector = "body";
            boolean includeOuterHtml = false;
            in = getClass().getResourceAsStream(fileName);
            PSExtractedAssetRequest req = new PSExtractedAssetRequest(folderPath, type, fileName, in, selector,
                    includeOuterHtml);

            // create the asset
            PSAsset newAsset = assetService.createAsset(req);
            in.close();
            assetIds.add(newAsset.getId());
            assertEquals("percRawHtmlAsset", newAsset.getType());
            assertEquals(fileName, newAsset.getName());
            Map<String, Object> fields = newAsset.getFields();
            assertNotNull(fields.get("html"));
            String newAssetFolderPath = newAsset.getFolderPaths().get(0);
            folderIds.add(contentWs.getIdByPath(newAssetFolderPath));
            assertEquals(folderPath, PSPathUtils.getFinderPath(newAssetFolderPath));

            // create asset with same file (should be renamed)
            in = getClass().getResourceAsStream(fileName);
            req = new PSExtractedAssetRequest(folderPath, type, fileName, in, selector, includeOuterHtml);
            newAsset = assetService.createAsset(req);
            in.close();
            assetIds.add(newAsset.getId());
            assertEquals("html-1.html", newAsset.getName());
            
            // create asset with same file (should be renamed again)
            in = getClass().getResourceAsStream(fileName);
            req = new PSExtractedAssetRequest(folderPath, type, fileName, in, selector, includeOuterHtml);
            newAsset = assetService.createAsset(req);
            in.close();
            assetIds.add(newAsset.getId());
            assertEquals("html-2.html", newAsset.getName());
            
            // test rich text assets
            folderPath = "/Assets/richtext";
            fileName = "richtext.html";
            type = AssetType.RICH_TEXT;
            in = getClass().getResourceAsStream(fileName);
            req = new PSExtractedAssetRequest(folderPath, type, fileName, in, selector, includeOuterHtml);

            // create the asset
            newAsset = assetService.createAsset(req);
            in.close();
            assetIds.add(newAsset.getId());
            assertEquals("percRichTextAsset", newAsset.getType());
            assertEquals(fileName, newAsset.getName());
            fields = newAsset.getFields();
            assertNotNull(fields.get("text"));
            newAssetFolderPath = newAsset.getFolderPaths().get(0);
            folderIds.add(contentWs.getIdByPath(newAssetFolderPath));
            assertEquals(folderPath, PSPathUtils.getFinderPath(newAssetFolderPath));

            // create asset with same file (should be renamed)
            in = getClass().getResourceAsStream(fileName);
            req = new PSExtractedAssetRequest(folderPath, type, fileName, in, selector, includeOuterHtml);
            newAsset = assetService.createAsset(req);
            in.close();
            assetIds.add(newAsset.getId());
            assertEquals("richtext-1.html", newAsset.getName());
            
            // test simple text assets
            folderPath = "/Assets/simpletext";
            fileName = "simpletext.html";
            type = AssetType.SIMPLE_TEXT;
            in = getClass().getResourceAsStream(fileName);
            req = new PSExtractedAssetRequest(folderPath, type, fileName, in, selector, includeOuterHtml);

            // create the asset
            newAsset = assetService.createAsset(req);
            in.close();
            assetIds.add(newAsset.getId());
            assertEquals("percSimpleTextAsset", newAsset.getType());
            assertEquals(fileName, newAsset.getName());
            fields = newAsset.getFields();
            assertNotNull(fields.get("text"));
            newAssetFolderPath = newAsset.getFolderPaths().get(0);
            folderIds.add(contentWs.getIdByPath(newAssetFolderPath));
            assertEquals(folderPath, PSPathUtils.getFinderPath(newAssetFolderPath));

            // create asset with same file (should be renamed)
            in = getClass().getResourceAsStream(fileName);
            req = new PSExtractedAssetRequest(folderPath, type, fileName, in, selector, includeOuterHtml);
            newAsset = assetService.createAsset(req);
            in.close();
            assetIds.add(newAsset.getId());
            assertEquals("simpletext-1.html", newAsset.getName());
        }
        finally
        {
            for (String assetId : assetIds)
            {
                assetService.delete(assetId);
            }
            
            if (!folderIds.isEmpty())
            {
                contentWs.deleteFolders(folderIds, false);
            }
            
            if (in != null)
            {
                in.close();
            }
        }
    }
    
    public void testFindByTypeAndWf() throws Exception
    {
        List<String> assetIds = new ArrayList<String>();
        String defaultWf = "Default Workflow";        
        
        try
        {
            // create a shared asset
            PSAsset asset = createAsset("SharedAsset1", "//Folders");
            String assetId = asset.getId();
            assertNotNull(assetId);
            assetIds.add(assetId);
            String assetType = asset.getType();
         
            Collection<PSAsset> assets = assetService.findByTypeAndWf(assetType, defaultWf, "Draft");
            assertTrue(idExists(assets, assetId));
            
            assets = assetService.findByTypeAndWf(assetType, defaultWf, null);
            assertTrue(idExists(assets, assetId));
            
            assets = assetService.findByTypeAndWf(assetType, defaultWf, "Pending");
            assertFalse(idExists(assets, assetId));
            
            // transition the asset to a different state
            itemWorkflowService.transition(assetId, IPSItemWorkflowService.TRANSITION_TRIGGER_APPROVE);
            
            assets = assetService.findByTypeAndWf(assetType, defaultWf, "Pending");
            assertTrue(idExists(assets, assetId));
            
            assets = assetService.findByTypeAndWf(assetType, defaultWf, null);
            assertTrue(idExists(assets, assetId));
            
            assets = assetService.findByTypeAndWf(assetType, defaultWf, "Draft");
            assertFalse(idExists(assets, assetId));
        }
        finally
        {
            for (String assetId : assetIds)
            {                
                try
                {
                    assetDao.delete(assetId);
                }
                catch (Exception e)
                {
                    // already deleted
                }
            }
        }
    }
    
    public void testFindByTypeAndName() throws Exception
    {
        List<String> assetIds = new ArrayList<String>();
        
        try
        {
            // create a shared asset
            PSAsset asset = createAsset("SharedAsset1", "//Folders");
            String assetId = asset.getId();
            assertNotNull(assetId);
            assetIds.add(assetId);
            String assetType = asset.getType();
            String assetName = asset.getName();
                     
            Collection<PSAsset> assets = assetDao.findByTypeAndName(assetType, assetName);
            assertEquals(1, assets.size());
            assertTrue(idExists(assets, assetId));
            
            assetDao.delete(assetId);
            
            assets = assetDao.findByTypeAndName(assetType, assetName);
            assertTrue(assets.isEmpty());
        }
        finally
        {
            for (String assetId : assetIds)
            {                
                try
                {
                    assetDao.delete(assetId);
                }
                catch (Exception e)
                {
                    // already deleted
                }
            }
        }
    }
    
    /**
     * Creates a new page for the specified name and template.
     */
    private PSPage createPage(String name, String templateId)
    {
        PSPage pageNew = new PSPage();
        pageNew.setName(name);
        pageNew.setTitle(name);
        pageNew.setFolderPath(fixture.site1.getFolderPath());
        pageNew.setTemplateId(templateId);
        pageNew.setLinkTitle("dummy");
        
        PSRegion region = new PSRegion();
        region.setOwnerType(PSRegionOwnerType.PAGE);
        region.setRegionId("Test");
        
        PSWidgetItem wi = new PSWidgetItem();
        wi.setDefinitionId("percRawHtml");

        PSRegionBranches br = new PSRegionBranches();
        br.setRegionWidgets("Test", asList(wi));
        br.setRegions(asList(region));
        
        pageNew.setRegionBranches(br);
        
        return pageNew;
    }

    
    
    /**
     * Gets the local asset relationship for the specified owner, widget, and asset.
     * 
     * @param ownerId the owner id, assumed not blank. This is the
     *            {@link IPSGuid} representation in string format.  
     * @param widgetId           
     * @param assetId the asset id, assumed not blank. This is the
     *            {@link IPSGuid} representation in string format.
     *
     * @return the relationship or <code>null</code> if it does not exist.
     * 
     * @throws PSErrorException
     */
    private PSRelationship getLocalRelationship(String ownerId, long widgetId, String assetId) throws PSErrorException
    {
        return getRelationship(PSWidgetAssetRelationshipService.LOCAL_ASSET_WIDGET_REL_FILTER, ownerId, widgetId, assetId);
    }

    /**
     * Gets the shared asset relationship for the specified owner, widget, and asset.
     * 
     * @param ownerId the owner id, assumed not blank. This is the
     *            {@link IPSGuid} representation in string format.  
     * @param widgetId           
     * @param assetId the asset id, assumed not blank. This is the
     *            {@link IPSGuid} representation in string format.
     *
     * @return the relationship or <code>null</code> if it does not exist.
     * 
     * @throws PSErrorException
     */
    private PSRelationship getSharedRelationship(String ownerId, long widgetId, String assetId) throws PSErrorException
    {
        return getRelationship(PSWidgetAssetRelationshipService.SHARED_ASSET_WIDGET_REL_FILTER, ownerId, widgetId, assetId);
    }
    
    /**
     * Gets the local asset relationship for the specified owner, widget, and asset.  Owner-revision specific.
     * 
     * @param name of the relationship filter, assumed not blank.
     * @param ownerId the owner id, assumed not <code>null</code>. This is the
     *            {@link IPSGuid} representation in string format.  
     * @param widgetId    
     * @param assetId the asset id, assumed not <code>null</code>. This is the
     *            {@link IPSGuid} representation in string format.
     *
     * @return the relationship or <code>null</code> if it does not exist.
     * 
     * @throws PSErrorException
     */
    private PSRelationship getRelationship(String name, String ownerId, long widgetId, String assetId)
    throws PSErrorException
    {
        PSRelationship rel = null;
        
        PSRelationshipFilter filter = getRelationshipFilter(name);
        filter.setOwner(idMapper.getLocator(ownerId));
        filter.setDependentId(idMapper.getGuid(assetId).getUUID());
        filter.setProperty(PSWidgetAssetRelationshipService.WIDGET_ID_PROP_NAME, String.valueOf(widgetId));
        List<PSRelationship> rels = systemWs.loadRelationships(filter);
        if (!rels.isEmpty())
        {
            rel = rels.get(0);
        }
        
        return rel;
    }
    
    /**
     * Gets all local content relationships for the specified owner.
     * 
     * @param ownerId assumed not blank.
     * 
     * @return list of local relationships, never <code>null</code>, may be empty.
     */
    private List<PSRelationship> getLocalRelationships(String ownerId)
    {
        return getRelationships(PSWidgetAssetRelationshipService.LOCAL_ASSET_WIDGET_REL_FILTER, ownerId);
    }
    
    /**
     * Gets all shared relationships for the specified owner.
     * 
     * @param ownerId assumed not blank.
     * 
     * @return list of shared relationships, never <code>null</code>, may be empty.
     */
    private List<PSRelationship> getSharedRelationships(String ownerId)
    {
        return getRelationships(PSWidgetAssetRelationshipService.SHARED_ASSET_WIDGET_REL_FILTER, ownerId);
    }
    
    /**
     * Get all relationships for the specified type and owner.
     * 
     * @param name the relationship filter name, assumed not blank.
     * @param ownerId assumed not blank.
     * 
     * @return list of relationships, never <code>null</code>, may be empty.
     */
    private List<PSRelationship> getRelationships(String name, String ownerId)
    {
        PSRelationshipFilter filter = getRelationshipFilter(name);
        filter.setOwner(idMapper.getLocator(ownerId));
        
        return systemWs.loadRelationships(filter);
    }
    
    /**
     * Get the relationship filter for the specified type.  Limited to owner revision.
     * 
     * @param name the relationship filter name, assumed not blank.
     * 
     * @return relationship filter, never <code>null</code>.
     */
    private PSRelationshipFilter getRelationshipFilter(String name)
    {
        PSRelationshipFilter filter = new PSRelationshipFilter();
        filter.limitToOwnerRevision(true);
        filter.setName(name);
        
        return filter;
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

    /**
     * Determines if the specified asset is present in a collection of assets.
     * 
     * @param assets collection of assets, assumed not <code>null</code>.
     * @param id of the asset, assumed not blank.
     * 
     * @return <code>true</code> if the asset is contained in the collection, <code>false</code> otherwise.
     */
    private boolean idExists(Collection<PSAsset> assets, String id)
    {
        Iterator<PSAsset> iter = assets.iterator();
        while (iter.hasNext())
        {
            if (iter.next().getId().equals(id))
            {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Gets an id which includes the updated revision for the specified id.
     * 
     * @param id assumed not blank.
     * 
     * @return id with current revision.  Never blank.
     */
    private String getUpdatedRevisionId(String id)
    {
        return idMapper.getString(contentDesignWs.getItemGuid(idMapper.getGuid(id)));
    }
    
    public IPSPageService getPageService()
    {
        return pageService;
    }
    
    public void setPageService(IPSPageService pageService)
    {
        this.pageService = pageService;
    }

    public IPSTemplateService getTemplateService()
    {
        return templateService;
    }
    
    public void setTemplateService(IPSTemplateService templateService)
    {
        this.templateService = templateService;
    }

    public IPSAssetService getAssetService()
    {
        return assetService;
    }

    public void setAssetService(IPSAssetService assetService)
    {
        this.assetService = assetService;
    }
    
    public IPSIdMapper getIdMapper()
    {
        return idMapper;
    }

    public void setIdMapper(IPSIdMapper idMapper)
    {
        this.idMapper = idMapper;
    }
    
    public IPSSecurityWs getSecurityWs()
    {
        return securityWs;
    }

    public void setSecurityWs(IPSSecurityWs securityWs)
    {
        this.securityWs = securityWs;
    }

    public IPSSystemWs getSystemWs()
    {
        return systemWs;
    }
    
    public void setSystemWs(IPSSystemWs systemWs)
    {
        this.systemWs = systemWs;
    }
    
    public IPSWidgetService getWidgetService()
    {
        return widgetService;
    }

    public void setWidgetService(IPSWidgetService widgetService)
    {
        this.widgetService = widgetService;
    }
    
    public IPSContentWs getContentWs()
    {
        return contentWs;
    }

    public void setContentWs(IPSContentWs contentWs)
    {
        this.contentWs = contentWs;
    }
    
    public IPSAssetDao getAssetDao()
    {
        return assetDao;
    }

    public void setAssetDao(IPSAssetDao assetDao)
    {
        this.assetDao = assetDao;
    }

    public IPSWidgetAssetRelationshipService getWidgetAssetRelationshipService()
    {
        return widgetAssetRelationshipService;
    }

    public void setWidgetAssetRelationshipServiceao(IPSWidgetAssetRelationshipService widgetAssetRelationshipService)
    {
        this.widgetAssetRelationshipService = widgetAssetRelationshipService;
    }

    public IPSContentDesignWs getContentDesignWs()
    {
        return contentDesignWs;
    }

    public void setContentDesignWs(IPSContentDesignWs contentDesignWs)
    {
        this.contentDesignWs = contentDesignWs;
    }

    public IPSItemWorkflowService getItemWorkflowService()
    {
        return itemWorkflowService;
    }

    public void setItemWorkflowService(IPSItemWorkflowService itemWorkflowService)
    {
        this.itemWorkflowService = itemWorkflowService;
    }

    public IPSCmsObjectMgr getCmsObjectMgr()
    {
        return cmsObjectMgr;
    }

    public void setCmsObjectMgr(IPSCmsObjectMgr cmsObjectMgr)
    {
        this.cmsObjectMgr = cmsObjectMgr;
    }
    
    public void setFolderHelper(IPSFolderHelper folderHelper)
    {
        this.folderHelper = folderHelper;
    }
    
    private IPSPageService pageService;
    private IPSTemplateService templateService;
    private IPSAssetService assetService;
    private IPSIdMapper idMapper;
    private IPSSecurityWs securityWs;
    private IPSSystemWs systemWs;
    private IPSWidgetService widgetService;
    private IPSContentWs contentWs;
    private IPSAssetDao assetDao;
    private IPSWidgetAssetRelationshipService widgetAssetRelationshipService;
    private IPSContentDesignWs contentDesignWs;
    private IPSItemWorkflowService itemWorkflowService;
    private IPSCmsObjectMgr cmsObjectMgr;
    private IPSFolderHelper folderHelper;
            
}
