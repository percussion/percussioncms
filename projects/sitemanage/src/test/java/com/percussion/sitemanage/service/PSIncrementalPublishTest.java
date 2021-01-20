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
package com.percussion.sitemanage.service;

import com.percussion.assetmanagement.data.PSAbstractAssetRequest;
import com.percussion.assetmanagement.data.PSAbstractAssetRequest.AssetType;
import com.percussion.assetmanagement.data.PSAsset;
import com.percussion.assetmanagement.data.PSBinaryAssetRequest;
import com.percussion.assetmanagement.service.IPSAssetService;
import com.percussion.cms.objectstore.PSFolder;
import com.percussion.itemmanagement.service.IPSItemWorkflowService;
import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.service.IPSPageService;
import com.percussion.pagemanagement.service.PSSiteDataServletTestCaseFixture;
import com.percussion.pathmanagement.data.PSPathItem;
import com.percussion.pathmanagement.service.impl.PSAssetPathItemService;
import com.percussion.pathmanagement.service.impl.PSPathUtils;
import com.percussion.services.contentchange.IPSContentChangeService;
import com.percussion.services.contentchange.PSContentChangeServiceLocator;
import com.percussion.share.data.PSPagedItemList;
import com.percussion.share.spring.PSSpringWebApplicationContextUtils;
import com.percussion.share.test.PSTestDataCleaner;
import com.percussion.test.PSServletTestCase;
import com.percussion.ui.service.IPSListViewHelper;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.security.IPSSecurityWs;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.experimental.categories.Category;

/**
 * @author JaySeletz
 *
 */
@Category(IntegrationTest.class)
public class PSIncrementalPublishTest extends PSServletTestCase
{
    private PSSiteDataServletTestCaseFixture fixture;
    private IPSSecurityWs securityWs;
    private IPSPageService pageService;
    private IPSAssetService assetService;
    private IPSItemWorkflowService itemWorkflowService;
    private IPSSitePublishService sitePublishService;

    
    
    @Override
    public void setUp() throws Exception
    {
        PSSpringWebApplicationContextUtils.injectDependencies(this);
        fixture = new PSSiteDataServletTestCaseFixture(request, response);
        fixture.setUp();
        //FB:IJU_SETUP_NO_SUPER NC 1-16-16
        super.setUp();
    }
        
    @Override
    protected void tearDown() throws Exception
    {
        securityWs.login("admin1", "demo", "Enterprise_Investments", null);
        pageCleaner.clean();
        assetCleaner.clean();

        fixture.tearDown();
        
        List<PSFolder> folders = contentWs.loadFolders(new String[]{PSAssetPathItemService.ASSET_ROOT
                + "/Test"});
        if (!folders.isEmpty())
            contentWs.deleteFolders(Arrays.asList(folders.get(0).getGuid()), true);
    }
    
    protected PSTestDataCleaner<String> pageCleaner = new PSTestDataCleaner<String>()
    {
        @Override
        protected void clean(String id) throws Exception
        {
            pageService.delete(id, true);
        }
    };

    protected PSTestDataCleaner<String> assetCleaner = new PSTestDataCleaner<String>()
    {
        @Override
        protected void clean(String id) throws Exception
        {
            assetService.delete(id);
        }
    };
    private IPSContentWs contentWs;
    
    public void test() throws Exception
    {
        securityWs.login("admin1", "demo", "Enterprise_Investments", null);

        IPSContentChangeService changeService = PSContentChangeServiceLocator.getContentChangeService();
        
        // clear the changes from the service (ensure clean start)
        changeService.deleteChangeEventsForSite(fixture.site1.getSiteId());
        
        // create and approve some pages
        Map<String, PSPage> pages = new HashMap<String, PSPage>();        
        for (int i = 0; i < 5; i++)
        {
            PSPage page = createAndApprovePage("page" + i);
            pages.put(page.getId(), page);
        }
        
        // create and approve some assets
        Map<String, PSAsset> assets = new HashMap<String, PSAsset>();
        for (int i = 0; i < 5; i++)
        {
            PSAsset asset = createAndApproveAsset("asset" + i); 
            assets.put(asset.getId(), asset);
        }
        
        // Get preview, ensure we get them all w/correct data
        PSPagedItemList pagedItems = sitePublishService.getQueuedIncrementalContent(fixture.site1.getName(), fixture.site1.getName(), 1, 5);
        assertNotNull(pagedItems);
        assertEquals(10, pagedItems.getChildrenCount().intValue());
        List<PSPathItem> pathItems = pagedItems.getChildrenInPage();
        assertEquals(5, pathItems.size());
        validatePathItems(pages, assets, pathItems);
        
        pagedItems = sitePublishService.getQueuedIncrementalContent(fixture.site1.getName(), fixture.site1.getName(), 6, 6);
        assertNotNull(pagedItems);
        assertEquals(10, pagedItems.getChildrenCount().intValue());
        pathItems = pagedItems.getChildrenInPage();
        assertEquals(5, pathItems.size());
        validatePathItems(pages, assets, pathItems);  
        
        assertTrue(pages.isEmpty());
        assertTrue(assets.isEmpty());
        
        // clear the changes from the service
        changeService.deleteChangeEventsForSite(fixture.site1.getSiteId());
        
    }

    private void validatePathItems(Map<String, PSPage> pages, Map<String, PSAsset> assets, List<PSPathItem> pathItems)
    {
        for (PSPathItem pathItem : pathItems)
        {
            PSPage page = pages.remove(pathItem.getId());
            if (page != null)
                validatePage(page, pathItem);
            else
            {
                PSAsset asset = assets.remove(pathItem.getId());
                assertNotNull("Got unexpected path item", asset);
                validateAsset(asset, pathItem);
            }
        }
    }

    /**
     * @param asset
     * @param pathItem
     */
    private void validateAsset(PSAsset asset, PSPathItem pathItem)
    {
        assertEquals(asset.getName(), pathItem.getName());
        Map<String, String> props = pathItem.getDisplayProperties();
        
        List<String> paths = asset.getFolderPaths();
        assertNotNull(paths);
        assertTrue(!paths.isEmpty());
        assertEquals(PSPathUtils.getFinderPath(paths.get(0) + "/" + asset.getName()), pathItem.getPath());
        
        assertEquals(asset.getName(), props.get(IPSListViewHelper.TITLE_NAME));
        assertEquals("admin1", props.get(IPSListViewHelper.CONTENT_LAST_MODIFIER_NAME));
        assertTrue(!StringUtils.isBlank(props.get(IPSListViewHelper.CONTENT_LAST_MODIFIED_DATE_NAME)));
        assertEquals(asset.getType(), pathItem.getType());
    }

    /**
     * @param page
     * @param pathItem
     */
    private void validatePage(PSPage page, PSPathItem pathItem)
    {
        assertEquals(page.getLinkTitle(), pathItem.getName());
        Map<String, String> props = pathItem.getDisplayProperties();

        List<String> paths = page.getFolderPaths();
        assertNotNull(paths);
        assertTrue(!paths.isEmpty());
        assertEquals(PSPathUtils.getFinderPath(paths.get(0) + "/" + page.getName()), pathItem.getPath());
        
        assertEquals(page.getName(), props.get(IPSListViewHelper.TITLE_NAME));
        assertEquals(page.getType(), pathItem.getType());

        assertEquals("admin1", props.get(IPSListViewHelper.CONTENT_LAST_MODIFIER_NAME));
        assertTrue(!StringUtils.isBlank(props.get(IPSListViewHelper.CONTENT_LAST_MODIFIED_DATE_NAME)));
    }


    private PSAsset createAndApproveAsset(String name)
    {
        InputStream in = IOUtils.toInputStream("This is my file content");
        PSAbstractAssetRequest ar = new PSBinaryAssetRequest("/Assets/Test", AssetType.FILE, "test.txt", "text/plain", in);
        PSAsset resource = assetService.createAsset(ar);
        String resourceId = resource.getId();
        assetCleaner.add(resourceId);        
        itemWorkflowService.performApproveTransition(resourceId, false, null);
        return resource;
    }

    private PSPage createAndApprovePage(String name)
    {
        PSPage page = createPage(name, fixture.template1.getId());
        String pageId = pageService.save(page).getId();
        pageCleaner.add(pageId);
        itemWorkflowService.performApproveTransition(pageId, false, null);
        return page;
    }
    
    protected PSPage createPage(String name, String templateId)
    {
        PSPage pageNew = new PSPage();
        pageNew.setName(name);
        pageNew.setTitle(name);
        pageNew.setFolderPath(fixture.site1.getFolderPath());
        pageNew.setTemplateId(templateId);
        pageNew.setLinkTitle(name + "Link");
        
        return pageNew;
    }
    
    public void setPageService(IPSPageService pageService)
    {
        this.pageService = pageService;
    }

    public void setAssetService(IPSAssetService assetService)
    {
        this.assetService = assetService;
    }
    
    public void setSecurityWs(IPSSecurityWs securityWs)
    {
        this.securityWs = securityWs;
    }

    public void setItemWorkflowService(IPSItemWorkflowService itemWorkflowService)
    {
        this.itemWorkflowService = itemWorkflowService;
    }

    public void setSiteDataService(IPSSitePublishService sitePublishService)
    {
        this.sitePublishService = sitePublishService;
    }

    public void setContentWs(IPSContentWs contentWs)
    {
        this.contentWs = contentWs;
    }
    
}
