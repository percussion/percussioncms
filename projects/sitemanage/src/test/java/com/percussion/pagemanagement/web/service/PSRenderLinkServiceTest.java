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
package com.percussion.pagemanagement.web.service;

import static com.percussion.share.test.PSMatchers.containsRegEx;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import com.percussion.assetmanagement.data.PSAsset;
import com.percussion.pagemanagement.data.PSInlineLinkRequest;
import com.percussion.pagemanagement.data.PSRenderLink;
import com.percussion.pathmanagement.service.impl.PSAssetPathItemService;
import com.percussion.share.test.PSRestTestCase;
import com.percussion.share.test.PSTestUtils;

import java.io.InputStream;

import com.percussion.utils.testing.IntegrationTest;
import net.sf.oval.constraint.AssertFalse;
import net.sf.oval.constraint.AssertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Tests generating links.
 * @author adamgent
 *
 */
@Category(IntegrationTest.class)
public class PSRenderLinkServiceTest
{
    private static PSRenderLinkServiceClient renderClient;

    private static PSTestSiteData testSiteData;
    private static String pageId;
    private static PSAsset asset;
    private static String file;
    
    @BeforeClass
    public static void setUp() throws Exception
    {
        testSiteData = new PSTestSiteData();
        testSiteData.setUp();
        renderClient = new PSRenderLinkServiceClient();
        PSRestTestCase.setupClient(renderClient);
        pageId = testSiteData.createPage("TestLinkToPage", testSiteData.site1.getFolderPath(), testSiteData.template1.getId());
        file = PSTestUtils.resourceToBase64(PSRenderLinkServiceTest.class, "inline-link.txt");
        
        asset = new PSAsset();
        asset.getFields().put("sys_title", "MyFile");
        asset.getFields().put("displaytitle", "MyFile Displaytitle");
        asset.getFields().put("filename", "inline-link.txt");
        asset.getFields().put("item_file_attachment", file);
        asset.getFields().put("item_file_attachment_filename", "inline-link.txt");
        asset.getFields().put("item_file_attachment_type", "text/plain");
        asset.setType("percFileAsset");
        asset.setFolderPaths(asList(PSAssetPathItemService.ASSET_ROOT));
        asset = testSiteData.saveAsset(asset);
    }

    @AfterClass
    public static void tearDown() throws Exception
    {
        testSiteData.tearDown();
    }

    @Test
    public void testPreviewPageLink() throws Exception
    {
        
        PSRenderLink actual = renderClient.getPreviewPageLink(pageId);
        assertNotNull(actual.getUrl());
        assertFalse(actual.getUrl().isEmpty());
        assertThat(actual.getUrl(),  containsRegEx("sys_siteid=[0-9]+"));
        assertThat(actual.getUrl(),  not(containsString("sys_siteid=0")));
        
    }
    
    
    @Test
    public void testPostFileLink() throws Exception
    {
        PSInlineLinkRequest link = new PSInlineLinkRequest();
        link.setTargetId(asset.getId());

        PSRenderLink actual = renderClient.previewPostLinkRequest(link);
        assertNotNull(actual.getUrl());
        assertFalse(actual.getUrl().isEmpty());
    }
    
    @Test
    public void testGetFileLinkForResource() throws Exception
    {
        PSRenderLink actual = renderClient.getPreviewLink(asset.getId(), "percSystem.fileBinary");
        assertNotNull(actual.getUrl());
        assertFalse(actual.getUrl().isEmpty());
    }
    
    @Test
    public void testFollowFileLink() throws Exception
    {
        PSRenderLink actual = renderClient.getPreviewLink(asset.getId(), "percSystem.fileBinary");
        InputStream content = renderClient.followLink(actual.getUrl());
        String linkContent = PSTestUtils.resourceToBase64(content);
        assertNotNull(linkContent);
        assertEquals("File uploaded should be the same as the render link followed",
                file, linkContent);
        
    }
    
    @SuppressWarnings("deprecation")
    @Ignore("Cannot create image asset with rest yet.")
    @Test
    public void testImagePreviewLink() throws Exception
    {
        PSInlineLinkRequest link = new PSInlineLinkRequest();
        link.setTargetId(asset.getId());

        PSRenderLink actual = renderClient.previewPostLinkRequest(link);
        assertNotNull(actual);
    }
    
}
