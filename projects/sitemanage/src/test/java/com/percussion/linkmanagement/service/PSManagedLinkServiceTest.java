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
package com.percussion.linkmanagement.service;

import static com.percussion.linkmanagement.service.IPSManagedLinkService.HREF_ATTR;
import static com.percussion.linkmanagement.service.IPSManagedLinkService.PERC_LINKID_ATTR;
import static com.percussion.linkmanagement.service.IPSManagedLinkService.PERC_MANAGED_ATTR;
import static com.percussion.linkmanagement.service.IPSManagedLinkService.SRC_ATTR;
import static com.percussion.linkmanagement.service.IPSManagedLinkService.TRUE_VAL;
import static com.percussion.util.IPSHtmlParameters.SYS_OVERWRITE_PREVIEW_URL_GEN;
import static java.util.Arrays.asList;

import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.percussion.utils.testing.IntegrationTest;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;

import com.percussion.assetmanagement.data.PSAbstractAssetRequest;
import com.percussion.assetmanagement.data.PSAbstractAssetRequest.AssetType;
import com.percussion.assetmanagement.data.PSAsset;
import com.percussion.assetmanagement.data.PSBinaryAssetRequest;
import com.percussion.assetmanagement.service.IPSAssetService;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.itemmanagement.service.IPSItemWorkflowService;
import com.percussion.itemmanagement.service.IPSWorkflowHelper;
import com.percussion.linkmanagement.service.impl.PSManagedLinkService;
import com.percussion.pagemanagement.assembler.IPSRenderLinkContextFactory;
import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.data.PSRenderLinkContext;
import com.percussion.pagemanagement.service.PSSiteDataServletTestCaseFixture;
import com.percussion.pagemanagement.service.PSSiteDataServletTestCaseFixture.PSAssetCleaner;
import com.percussion.pathmanagement.service.impl.PSAssetPathItemService;
import com.percussion.pathmanagement.service.impl.PSPathUtils;
import com.percussion.server.PSRequest;
import com.percussion.services.linkmanagement.IPSManagedLinkDao;
import com.percussion.services.linkmanagement.data.PSManagedLink;
import com.percussion.share.service.IPSIdMapper;
import com.percussion.share.spring.PSSpringWebApplicationContextUtils;
import com.percussion.share.test.PSTestUtils;
import com.percussion.sitemanage.data.PSSiteSummary;
import com.percussion.test.PSServletTestCase;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.request.PSRequestInfo;
import com.percussion.webservices.content.IPSContentWs;
import org.junit.experimental.categories.Category;

/**
 * @author JaySeletz
 *
 */
@Category(IntegrationTest.class)
public class PSManagedLinkServiceTest extends PSServletTestCase
{
    private PSSiteDataServletTestCaseFixture fixture;
    private PSAssetCleaner assetCleaner;
    private IPSManagedLinkService service;
    private IPSContentWs contentWs;
    private IPSIdMapper idMapper;
    private List<Integer> parentLinkIds;
    private IPSAssetService assetService;
    private IPSRenderLinkContextFactory renderLinkContextFactory;
    private IPSWorkflowHelper workflowHelper;
    private IPSItemWorkflowService itemWorkflowService;
    private int unassignedParentId;

    private IPSManagedLinkDao dao;
    
    /**
     * Single managed link anchor, w/params: 0 - The ID of the anchor element, 1 - the href, 2 - The link ID, 3 - The anchor text
     */
    private static final String MANAGED_LINK = "<a id=\"{0}\" href=\"{1}\" " + PERC_MANAGED_ATTR + "=\"true\" " + PERC_LINKID_ATTR + "=\"{2}\" >{3}</a>";
    
    private static final String MANAGED_IMG_LINK = "<img alt=\"{3}\" id=\"{0}\" src=\"{1}\" " + PERC_MANAGED_ATTR + "=\"true\" " + PERC_LINKID_ATTR + "=\"{2}\" />";    

    /**
     * Single managed link anchor, w/params: 0 - The ID of the anchor element, 1 - the href, 2 - The anchor text
     */
    private static final String UNMANAGED_LINK = "<a id=\"{0}\" href=\"{1}\" " + PERC_MANAGED_ATTR + "=\"true\" >{2}</a>";
    
    private static final String UNMANAGED_IMG_LINK = "<img alt=\"{2}\" id=\"{0}\" src=\"{1}\" " + PERC_MANAGED_ATTR + "=\"true\" />";
    
    /**
     * @param dao the dao to set
     */
    public void setDao(IPSManagedLinkDao dao)
    {
        this.dao = dao;
    }

    @Override
    public void setUp() throws Exception
    {
        PSSpringWebApplicationContextUtils.injectDependencies(this);
        
        fixture = new PSSiteDataServletTestCaseFixture(request, response);
        fixture.setUp();
        assetCleaner = fixture.assetCleaner;
        parentLinkIds = new ArrayList<Integer>();
        parentLinkIds.add(unassignedParentId);
        
        //FB:IJU_SETUP_NO_SUPER NC 1-16-16
        super.setUp();
    }

    @Override
    public void tearDown() throws Exception
    {
        fixture.tearDown();
        for (Integer parentId : parentLinkIds)
        {
            List<PSManagedLink> links = dao.findLinksByParentId(parentId);
            for (PSManagedLink link : links)
            {
                dao.deleteLink(link);
            }
        }
    }
    
   
    
    /**
     * Test html with no links or invalid links is not modified
     * 
     * @throws Exception
     */
    
    public void testInvalidLinks() throws Exception
    {
        String parentId = getParentId(1, 1);
        
        // test html w/no links
        String source = "<div><p>This is some text</p></div>";
        String result = service.manageLinks(parentId, source);
        assertEquals(source, result);
        
        
        
        // test plain link
        source = "<a href=\"/index.html\">home</a>";
        result = service.manageLinks(parentId, source);
        assertEquals(source, result);
        
        source ="<PRESERVE><?php ?></PRESERVE>perc-managed";
        result = service.manageLinks(parentId, source);
        assertTrue(result.contains("<?php ?>"));
        
        
        // testInvalidLinkTargetIsUnManaged
        PSManagedLink link = dao.createLink(1, 1, 99999999, null);
        dao.saveLink(link);
        
        String anchorId = "test";
        source = MessageFormat.format(MANAGED_LINK, new Object[] {anchorId, "/index.html", String.valueOf(link.getLinkId())});
        result = service.manageLinks(parentId, source);
        
        Document doc = Jsoup.parseBodyFragment(result);
        Element el = doc.getElementById(anchorId);
        assertNotNull(el);
        assertEquals(TRUE_VAL, el.attr(PERC_MANAGED_ATTR));
        assertEquals("", el.attr(PERC_LINKID_ATTR));
        
        // test both w/link to folder or non-resource asset
        source = MessageFormat.format(UNMANAGED_LINK, new Object[] {anchorId, PSPathUtils.getFinderPath(fixture.site1.getFolderPath()), anchorId});
        result = service.manageLinks(parentId, source);
        doc = Jsoup.parseBodyFragment(result);
        el = doc.getElementById(anchorId);
        assertNotNull(el);
        assertEquals(TRUE_VAL, el.attr(PERC_MANAGED_ATTR));
        
        
        PSAsset htmlAsset = createHtmlAsset("Test", null, true);
        source = MessageFormat.format(UNMANAGED_LINK, new Object[] {anchorId, PSPathUtils.getFinderPath(htmlAsset.getFolderPaths().get(0)), anchorId});
        result = service.manageLinks(parentId, source);
        doc = Jsoup.parseBodyFragment(result);
        el = doc.getElementById(anchorId);
        assertNotNull(el);
        assertEquals(TRUE_VAL, el.attr(PERC_MANAGED_ATTR));
        assertEquals("", el.attr(PERC_LINKID_ATTR));
    }
    

    private String getParentId(int cid, int rev)
    {
        IPSGuid guid = idMapper.getGuid(new PSLocator(cid, rev));
        parentLinkIds.add(cid);
        return idMapper.getString(guid);
    }

    /**
     * Test that links are properly managed
     * 
     * @throws Exception
     */
    public void testManageLinks() throws Exception
    {
        String result;

        String path = fixture.site1.getFolderPath() + "/index.html";
        String href = PSPathUtils.getFinderPath(path);
        String text = "text";
        
        IPSGuid itemGuid = contentWs.getIdByPath(path);
        String itemId = String.valueOf(itemGuid.getUUID());
        String source = MessageFormat.format(UNMANAGED_LINK, new Object[] {itemId, href, text});
        int parentContentId = 1;
        String parentId = getParentId(parentContentId, 1);
        result = service.manageLinks(parentId, source);
        List<Long> linkIds = validateManagedLinks(source, result, Arrays.asList(itemId));
        assertEquals(1, linkIds.size());
        Document doc = Jsoup.parseBodyFragment(result);
        Element el = doc.getElementById(itemId);
        assertNotNull(el);
        assertEquals(text, el.text());
        
        el.removeAttr(PERC_LINKID_ATTR);
        // perc-managed="true" should be re-added
        result = service.manageLinks(parentId, source);
        validateManagedLinks(source, result, Arrays.asList(itemId));
        assertNotNull(el);
        assertEquals(href, el.attr(HREF_ATTR));
        assertEquals(text, el.text());
        

        // test managed link w/wrong href
        String badPath = fixture.site1.getFolderPath() + "/home";
        source = MessageFormat.format(MANAGED_LINK, new Object[] {itemId, badPath, String.valueOf(linkIds.get(0)), text});
        result = service.manageLinks(parentId, source);
        validateManagedLinks(source, result, Arrays.asList(itemId));
        doc = Jsoup.parseBodyFragment(result);
        el = doc.getElementById(itemId);
        assertNotNull(el);
        assertEquals(href, el.attr(HREF_ATTR));
        assertEquals(text, el.text());

        
        // test img asset managed link
        PSAsset imgAsset = createImgAsset();
        
        String imgPathAsset = StringUtils.join(imgAsset.getFolderPaths(), '/');
        href = PSPathUtils.getFinderPath(imgPathAsset) + "/" + imgAsset.getName();
        itemGuid = contentWs.getIdByPath(imgPathAsset + "/" + imgAsset.getName());
        itemId = String.valueOf(itemGuid.getUUID());
        source = MessageFormat.format(UNMANAGED_IMG_LINK, new Object[] {itemId, href, text});
        parentContentId = 1;
        parentId = getParentId(parentContentId, 1);
        result = service.manageLinks(parentId, source);
        linkIds = validateManagedLinks(source, result, Arrays.asList(itemId));
        assertEquals(1, linkIds.size());
        doc = Jsoup.parseBodyFragment(result);
        el = doc.getElementById(itemId);
        assertNotNull(el);
        assertEquals(el.attributes().get("alt"), text);
        
        // test image managed link w/wrong src
        String imgBadPathAsset = StringUtils.join(imgAsset.getFolderPaths(), "/");
        String badSrc = PSPathUtils.getFinderPath(imgBadPathAsset).replace("ManagedLinkTest", "ManagedLinkTest1") + "/" + imgAsset.getName();
        source = MessageFormat.format(MANAGED_IMG_LINK, new Object[] {itemId, badSrc, String.valueOf(linkIds.get(0)), text});
        result = service.manageLinks(parentId, source);
        validateManagedLinks(source, result, Arrays.asList(itemId));
        doc = Jsoup.parseBodyFragment(result);
        el = doc.getElementById(itemId);
        assertNotNull(el);
        assertEquals(href, el.attr(SRC_ATTR));
        assertEquals(el.attributes().get("alt"), text);
        
        // test several links
        PSAsset asset = null;
        try
        {
            List<String> itemIds = new ArrayList<String>();
            itemIds.add(itemId);
            asset = createHtmlAsset("test", "//Folders", false);
            parentId = asset.getId();
            parentContentId = idMapper.getLocator(parentId).getId();
            parentLinkIds.add(parentContentId);

            href = PSPathUtils.getFinderPath(imgPathAsset) + "/" + imgAsset.getName();
            itemGuid = contentWs.getIdByPath(imgPathAsset + "/" + imgAsset.getName());
            itemId = String.valueOf(itemGuid.getUUID());
            itemIds.add(itemId);
            source += MessageFormat.format(UNMANAGED_IMG_LINK, new Object[] {itemId, href, text});
            
            String pageId = null;
            for (int i = 0; i < 5; i++)
            {
                String name = "page" + i;
                path = fixture.site1.getFolderPath() + "/folder" + i;
                pageId = createPage(path, name);
                itemId = String.valueOf(idMapper.getContentId(pageId));
                itemIds.add(itemId);
                href = PSPathUtils.getFinderPath(path) + "/" + name;
                source += MessageFormat.format(UNMANAGED_LINK, new Object[] {itemId, href});            
            }
            

            
            // note: following tests work with last page created above
            
            result = service.manageLinks(parentId, source);
            List<Long> validated = validateManagedLinks(source, result, itemIds);
            assertEquals(validated.size(), itemIds.size());
            
            // test new revision of parent
            Set<String> idSet = new HashSet<String>();
            idSet.add(parentId);
            workflowHelper.transitionToPending(idSet);
            itemWorkflowService.checkOut(parentId);
            itemWorkflowService.checkIn(parentId);
            PSComponentSummary sum = workflowHelper.getComponentSummary(parentId);
            parentId = idMapper.getGuid(sum.getCurrentLocator()).toString();
            long linkId = validated.get(validated.size() - 1);
            source = MessageFormat.format(MANAGED_LINK, new Object[] {itemId, href, String.valueOf(linkId), text});
            result = service.manageLinks(parentId, source);
            validated = validateManagedLinks(source, result, Arrays.asList(itemId));
            assertEquals(1, validated.size());
            assertTrue(validated.get(0) == linkId);
            PSManagedLink link = dao.findLinkByLinkId(linkId);
            assertNotNull(link);
            assertEquals(2, link.getParentRevision());
            
           // test wrong parent id for link (manually copied html) - should act as newly managed link
            int parentContentid2 = 2;
            String parentId2 = getParentId(parentContentid2, 1);
            source = result;
            result = service.manageLinks(parentId2, source);
            validated = validateManagedLinks(source, result, Arrays.asList(itemId));
            assertEquals(1, validated.size());
            assertTrue(linkId != validated.get(0));
            link = dao.findLinkByLinkId(linkId);
            assertNotNull(link);
            assertEquals(parentContentId, link.getParentId());
            link = dao.findLinkByLinkId(validated.get(0));
            assertNotNull(link);
            assertEquals(parentContentid2, link.getParentId());

            // test delete child
            fixture.getPageService().delete(pageId);
            result = service.manageLinks(parentId, source);
            doc = Jsoup.parseBodyFragment(result);
            el = doc.getElementById(itemId);
            assertNotNull(el);
            assertEquals("", el.attr(PERC_LINKID_ATTR));
            
            // test delete parent
            assertFalse(dao.findLinksByParentId(parentContentId).isEmpty());
            assetService.delete(parentId);
            asset = null;
            assertTrue("Links were not deleted", dao.findLinksByParentId(parentContentId).isEmpty());       
            
        }
        finally
        {
            if (asset != null)
                assetService.delete(asset.getId());
        }        
    }

    private String createPage(String path, String name)
    {
        PSPage page = new PSPage();

        String pageId;
        page.setFolderPath(path);
        page.setName(name);
        page.setTitle(name);
        page.setTemplateId(fixture.template1.getId());
        page.setLinkTitle(name);
        page.setNoindex("true");
        page.setDescription("");
        
        pageId = fixture.createPage(page).getId();
        return pageId;
    }
    
    /**
     * Test {@link IPSManagedLinkService#updateCopyAssetsLinks(java.util.Collection, String, String, Map)}
     * 
     * @throws Exception
     */
    public void testUpdateAssetsLinks() throws Exception
    {
        // Prepare data
        PSSiteSummary origSite = fixture.site1;
        PSSiteSummary copySite = fixture.createSite(getClass().getSimpleName(), "CopySite");
        int origHomeId = getContentId(origSite);
        int copyHomeId = getContentId(copySite);

        PSAsset htmlAsset = createHtmlAsset("Test", null, true);
        PSAsset fileAsset1 = createFileAsset(1);
        PSAsset fileAsset2 = createFileAsset(2);
        
        Collection<String> assetIds = Collections.singleton(htmlAsset.getId());
        int assetContentId1 = getContentId(fileAsset1.getId());
        int assetContentId2 = getContentId(fileAsset2.getId());

        int crossSitePageId = getItemId("//Sites/EnterpriseInvestments/EI Home Page");
        
        // prepare link to an asset
        int parentId = getContentId(htmlAsset.getId());
        createManagedLink(parentId, 1, assetContentId1);
        createManagedLink(parentId, 1, crossSitePageId);
        
        // validate 
        validateLink(parentId, assetContentId1, crossSitePageId);
        
        Map<String, String> assetMap = new HashMap<String, String>();
        
        service.updateCopyAssetsLinks(assetIds, origSite.getFolderPath(), copySite.getFolderPath(), assetMap);
        validateLink(parentId, assetContentId1, crossSitePageId);
        
        // prepare link to home page
        createManagedLink(parentId, 1, origHomeId);
        validateLink(parentId, assetContentId1, crossSitePageId, origHomeId);

        assetMap.put(fileAsset1.getId(), fileAsset2.getId());
        
        service.updateCopyAssetsLinks(assetIds, origSite.getFolderPath(), copySite.getFolderPath(), assetMap);
        validateLink(parentId, assetContentId2, crossSitePageId, copyHomeId);
    }

    private int getItemId(String path)
    {
        IPSGuid itemId = contentWs.getIdByPath(path);
        return idMapper.getContentId(itemId);
    }
    
    private int getContentId(PSSiteSummary site)
    {
        return getItemId(site.getFolderPath() + "/index.html");
    }

    private void createManagedLink(int parentId, int revision, int childId)
    {
        PSManagedLink link = dao.createLink(parentId, revision, childId, null);
        dao.saveLink(link);        
    }
    
    private void validateLink(int parentId, int ... ids)
    {
        List<Integer> childIds = new ArrayList<Integer>();
        for (int i=0; i < ids.length; i++)
        {
            childIds.add(ids[i]);
        }
        List<PSManagedLink> links = dao.findLinksByParentId(parentId);
        assertTrue(links.size() == ids.length);
        
        for (PSManagedLink link : links)
        {
            assertTrue(childIds.contains(link.getChildId()));
        }
    }
    
    private int getContentId(String guid)
    {
        return idMapper.getContentId(guid);
    }
    
    /**
     * Test that managed links are rendered as expected.
     * 
     * @throws Exception
     */
    public void testRenderLinks() throws Exception
    {
        PSAsset htmlAsset = null;
        PSAsset fileAsset = null;

        // create parent html asset
        htmlAsset = createHtmlAsset("Test", null, true);
        String parentId = htmlAsset.getId();
        int parentContentId = idMapper.getLocator(parentId).getId();
        parentLinkIds.add(parentContentId);
        
        String result;

        String path = fixture.site1.getFolderPath() + "/index.html";
        String href = PSPathUtils.getFinderPath(path);
        String text = "text";
        
        // create link to index page
        IPSGuid itemGuid = contentWs.getIdByPath(path);
        String itemId = String.valueOf(itemGuid.getUUID());
        String source = MessageFormat.format(UNMANAGED_LINK, new Object[] {itemId, path, text});
        result = service.manageLinks(parentId, source);
        List<Long> linkIds = validateManagedLinks(source, result, Arrays.asList(itemId));
        assertEquals(1, linkIds.size());
        
        //test render (ensure attrs removed on publish, not on preview:
        /*
         * HACK ALERT!!!: 
         * see PSConcurrentRegionsAssembler.RegionResultsCallable.setPreviewUrlGenerator() - must set
         * IPSHtmlParameters.SYS_OVERWRITE_PREVIEW_URL_GEN so the assembler will use the friendly generator even if not
         * using the override - somewhat backwards implementation in
         * PSGeneratePubLocation.getCustomUrlGenerator() and PSGeneratePubLocation.processUdf() - if no override is 
         * specified, always uses default rather than friendly generator 
         */
        PSRequest req = (PSRequest) PSRequestInfo.getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
        String[] values = new String[] { "global/percussion/contentassembler/perc_casGeneratePreviewLink", "-1" };
        req.setPrivateObject(SYS_OVERWRITE_PREVIEW_URL_GEN,  values);
        
        // test valid page link preview
        PSRenderLinkContext previewPageLinkContext = renderLinkContextFactory.createPreview(fixture.getPageService().find(itemGuid.toString()));
        String rendered = service.renderLinks(previewPageLinkContext, result, parentContentId);
        validateRenderedLink(href, text, itemId, rendered, linkIds.get(0).toString());
        
        // test edit
        rendered = service.renderLinks(null, result, parentContentId);
        validateRenderedLink(href, text, itemId, rendered, linkIds.get(0).toString());

        
        // test valid but not live target page link publish
        PSRenderLinkContext publicLinkContext = new PSPublicLinkContext(fixture.site1);
        rendered = service.renderLinks(publicLinkContext, result,parentContentId);
        validateRenderedLink("#", text, itemId, rendered, null);
        
        // test valid page link publish
        // approve the page
        Set<String> itemIds = new HashSet<String>();
        itemIds.add(itemGuid.toString());
        workflowHelper.transitionToPending(itemIds);
        href = "/index.html";
        rendered = service.renderLinks(publicLinkContext, result, parentContentId);
        validateRenderedLink(href, text, itemId, rendered, null);
        
        // test w/file asset
        fileAsset = createFileAsset(0);
        path = fileAsset.getFolderPaths().get(0);
        href = PSPathUtils.getFinderPath(path) + "/" + fileAsset.getName();
        text = "file";
        
        // create link to asset
        itemGuid = idMapper.getGuid(fileAsset.getId());
        itemId = String.valueOf(itemGuid.getUUID());
        source = MessageFormat.format(UNMANAGED_LINK, new Object[] {itemId, href, text});
        result = service.manageLinks(parentId, source);
        linkIds = validateManagedLinks(source, result, Arrays.asList(itemId));
        assertEquals(1, linkIds.size());
        
        // test file link preview 
        PSRenderLinkContext previewFileLinkContext = renderLinkContextFactory.createAssetPreview(fileAsset.getFolderPaths().get(0), fileAsset);
        rendered = service.renderLinks(previewFileLinkContext, result,parentContentId);
        validateRenderedLink(href, text, itemId, rendered, linkIds.get(0).toString());
        
        // test edit
        rendered = service.renderLinks(null, result,parentContentId);
        validateRenderedLink(href, text, itemId, rendered, linkIds.get(0).toString());
        
        // test valid but not live file link publish
        rendered = service.renderLinks(publicLinkContext, result,parentContentId);
        validateRenderedLink("#", text, itemId, rendered, null);
        
        // test valid page link publish
        // approve the page
        itemIds.clear();
        itemIds.add(itemGuid.toString());
        workflowHelper.transitionToPending(itemIds);
        rendered = service.renderLinks(publicLinkContext, result,parentContentId);
        validateRenderedLink(href, text, itemId, rendered, null);
        
        // test invalid link preview (invalid=bad linkid or missing target)
        PSManagedLink link = dao.createLink(parentContentId, 1, 99999999, null);
        dao.saveLink(link);
        
        String anchorId = "test";
        href = "/bad.html";
        text = String.valueOf(link.getLinkId());
        source = MessageFormat.format(MANAGED_LINK, new Object[] {anchorId, href, text, text});
        rendered = service.renderLinks(previewFileLinkContext, source,parentContentId);
        validateRenderedLink(href, text, anchorId, rendered, null);
        rendered = service.renderLinks(null, source,parentContentId);
        validateRenderedLink(href, text, anchorId, rendered, null);
        
        // test invalid link publish
        rendered = service.renderLinks(publicLinkContext, source,parentContentId);
        validateRenderedLink(href, text, anchorId, rendered, null);
        
        // invalid link id
        text = "999999";
        source = MessageFormat.format(MANAGED_LINK, new Object[] {anchorId, href, text, text});
        rendered = service.renderLinks(previewFileLinkContext, source,parentContentId);
        validateRenderedLink(href, text, anchorId, rendered, null);
        rendered = service.renderLinks(null, source,parentContentId);
        validateRenderedLink(href, text, anchorId, rendered, null);
        rendered = service.renderLinks(publicLinkContext, source,parentContentId);
        validateRenderedLink(href, text, anchorId, rendered, null);
    }
    
    /**
     * Test that managed links are rendered as expected.
     * 
     * @throws Exception
     */
    public void testRenderItemPathLinks() throws Exception
    {
        PSAsset htmlAsset = null;
        PSAsset fileAsset = null;

        // create parent html asset
        htmlAsset = createHtmlAsset("Test", null, true);
        String parentId = htmlAsset.getId();
        int parentContentId = idMapper.getLocator(parentId).getId();
        parentLinkIds.add(parentContentId);
        
        String result;

        String path = fixture.site1.getFolderPath() + "/index.html";
        String href = PSPathUtils.getFinderPath(path);
        
        // create link to index page
        IPSGuid itemGuid = contentWs.getIdByPath(path);
        String itemId = String.valueOf(itemGuid.getUUID());
        result = service.manageItemPath(parentId, href, null);
        validateItemPathLink(result, itemId);
        
        //test render (ensure attrs removed on publish, not on preview:
        /*
         * HACK ALERT!!!: 
         * see PSConcurrentRegionsAssembler.RegionResultsCallable.setPreviewUrlGenerator() - must set
         * IPSHtmlParameters.SYS_OVERWRITE_PREVIEW_URL_GEN so the assembler will use the friendly generator even if not
         * using the override - somewhat backwards implementation in
         * PSGeneratePubLocation.getCustomUrlGenerator() and PSGeneratePubLocation.processUdf() - if no override is 
         * specified, always uses default rather than friendly generator 
         */
        PSRequest req = (PSRequest) PSRequestInfo.getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
        String[] values = new String[] { "global/percussion/contentassembler/perc_casGeneratePreviewLink", "-1" };
        req.setPrivateObject(SYS_OVERWRITE_PREVIEW_URL_GEN,  values);
        
        // test valid page link preview
        PSRenderLinkContext previewPageLinkContext = renderLinkContextFactory.createPreview(fixture.getPageService().find(itemGuid.toString()));
        String rendered = service.renderItemPath(previewPageLinkContext, result);
        assertEquals(href, rendered);
        
        // test edit
        rendered = service.renderItemPath(null, result);
        assertEquals(href, rendered);
        
        
        // test valid but not live target page link publish
        PSRenderLinkContext publicLinkContext = new PSPublicLinkContext(fixture.site1);
        rendered = service.renderItemPath(publicLinkContext, result);
        assertEquals("#", rendered);
        
        // test valid page link publish
        // approve the page and html asset
        Set<String> itemIds = new HashSet<String>();
        itemIds.add(itemGuid.toString());
        itemIds.add(htmlAsset.getId());
        
        workflowHelper.transitionToPending(itemIds);
        href = "/index.html";
        rendered = service.renderItemPath(publicLinkContext, result);
        assertEquals(href, rendered);
        
        // test w/file asset
        fileAsset = createFileAsset(0);
        path = fileAsset.getFolderPaths().get(0);
        href = PSPathUtils.getFinderPath(path) + "/" + fileAsset.getName();
        
        // create link to asset
        itemGuid = idMapper.getGuid(fileAsset.getId());
        itemId = String.valueOf(itemGuid.getUUID());
        result = service.manageItemPath(parentId, href, null);
        validateItemPathLink(result, itemId);
        
        // test file link preview 
        PSRenderLinkContext previewFileLinkContext = renderLinkContextFactory.createAssetPreview(fileAsset.getFolderPaths().get(0), fileAsset);
        rendered = service.renderItemPath(previewFileLinkContext, result);
        assertEquals(href, rendered);
        
        // test edit
        rendered = service.renderItemPath(null, result);
        assertEquals(href, rendered);
        
        // test valid but not live file link publish
        rendered = service.renderItemPath(publicLinkContext, result);
        assertEquals("#", rendered);
        
        // test valid page link publish
        // approve the page
        itemIds.clear();
        itemIds.add(itemGuid.toString());
        workflowHelper.transitionToPending(itemIds);
        rendered = service.renderItemPath(publicLinkContext, result);
        assertEquals(href, rendered);
        
        // test invalid link preview (invalid=bad linkid or missing target)
        PSManagedLink link = dao.createLink(parentContentId, 1, 99999999, null);
        dao.saveLink(link);
        String linkIdVal = String.valueOf(link.getLinkId());
        rendered = service.renderItemPath(previewFileLinkContext, linkIdVal);
        assertEquals("#", rendered);
        rendered = service.renderItemPath(null, linkIdVal);
        assertEquals("#", rendered);
        
        // test invalid link publish
        rendered = service.renderItemPath(publicLinkContext, linkIdVal);
        assertEquals("#", rendered);
        
        // invalid link id
        linkIdVal = "999999";
        rendered = service.renderItemPath(previewFileLinkContext, linkIdVal);
        assertEquals("#", rendered);
        rendered = service.renderItemPath(null, linkIdVal);
        assertEquals("#", rendered);
        rendered = service.renderItemPath(publicLinkContext, linkIdVal);
        assertEquals("#", rendered);
    }

    /**
     * Validate the rendered link is correct
     * 
     * @param href The expected href
     * @param text The text of the link
     * @param elementId The element id used to find the link
     * @param rendered The html containing the rendered link
     * @param linkId The expected linkid, if <code>null</code> then link should not have any managed attributes.
     */
    private void validateRenderedLink(String href, String text, String elementId, String rendered, String linkId)
    {
        Document doc;
        Element el;
        doc = Jsoup.parseBodyFragment(rendered);
        el = doc.getElementById(elementId);
        assertNotNull(el);
        assertEquals(href, el.attr(HREF_ATTR));
        assertEquals(text, el.text());
        
        if (linkId == null)
        {
            assertFalse(el.hasAttr(PERC_LINKID_ATTR));
            assertFalse(el.hasAttr(PERC_MANAGED_ATTR));
        }
        else
        {
            assertEquals(linkId, el.attr(PERC_LINKID_ATTR));
            // managed attribute is added if it did not exist and link id exists
            // or is created
            assertTrue(el.hasAttr(PERC_MANAGED_ATTR));
        }        
    }

    /**
     * Validate the link w/the specified element id was removed and it's text remains in the doc. 
     * @param text The text of the original link
     * @param elementId The element id of the original link
     * @param rendered The html containing the rendered link
     */
    private void validateNotRenderedLink(String text, String elementId, String rendered)
    {
        Document doc;
        Element el;
        doc = Jsoup.parseBodyFragment(rendered);
        el = doc.getElementById(elementId);
        assertNull(el);
        Elements els = doc.getElementsContainingText(text);
        assertNotNull(els);
        assertFalse(els.isEmpty());
    }

    /**
     * Test creating links for new items w/dummy parent id and then updating them
     * 
     * @throws Exception
     */
    public void testManageNewItemLinks() throws Exception
    {
        String source = "";
        List<String> itemIds = new ArrayList<String>();
        for (int i = 0; i < 3; i++)
        {
            PSPage page = new PSPage();
            String name = "page" + i;
            String path = fixture.site1.getFolderPath() + "/folder" + i;
            page.setFolderPath(path);
            page.setName(name);
            page.setTitle(name);
            page.setTemplateId(fixture.template1.getId());
            page.setLinkTitle(name);
            page.setNoindex("true");
            page.setDescription("");
            
            String pageId = fixture.createPage(page).getId();
            String itemId = String.valueOf(idMapper.getGuid(pageId).getUUID());
            itemIds.add(itemId);
            String href = PSPathUtils.getFinderPath(path) + "/" + name;
            source += MessageFormat.format(UNMANAGED_LINK, new Object[] {itemId, href});              
        }

        String result = service.manageNewItemLinks(source);
        List<Long> linkIds = validateManagedLinks(source, result, itemIds);
        assertEquals(linkIds.size(), itemIds.size());
        
        // validate links have no w/parent
        for (Long linkId : linkIds)
        {
            PSManagedLink link = dao.findLinkByLinkId(linkId);
            assertEquals(unassignedParentId, link.getParentId());
        }
        
        // now call update w/parent id
        int cid = 1;
        String parentId = getParentId(cid, 1);
        service.updateNewItemLinks(parentId);
        
        // validate links updated w/parent
        for (Long linkId : linkIds)
        {
            PSManagedLink link = dao.findLinkByLinkId(linkId);
            assertEquals(cid, link.getParentId());
        }
    }
    
    /**
     * Test creating links for new items w/dummy parent id and then updating them
     * 
     * @throws Exception
     */
    public void testManageNewItemPaths() throws Exception
    {
        List<String> itemIds = new ArrayList<String>();
        List<Long> linkIds = new ArrayList<Long>();
        service.initNewItemLinks();
        for (int i = 0; i < 3; i++)
        {
            PSPage page = new PSPage();
            String name = "page" + i;
            String path = fixture.site1.getFolderPath() + "/folder" + i;
            page.setFolderPath(path);
            page.setName(name);
            page.setTitle(name);
            page.setTemplateId(fixture.template1.getId());
            page.setLinkTitle(name);
            page.setNoindex("true");
            page.setDescription("");
            
            String pageId = fixture.createPage(page).getId();
            String itemId = String.valueOf(idMapper.getGuid(pageId).getUUID());
            itemIds.add(itemId);
            String href = PSPathUtils.getFinderPath(path) + "/" + name;
            String result = service.manageItemPath(null, href, null);
            validateItemPathLink(result, itemId);
            linkIds.add(Long.valueOf(result));
        }

        
        // validate links have no w/parent
        for (Long linkId : linkIds)
        {
            PSManagedLink link = dao.findLinkByLinkId(linkId);
            assertEquals(unassignedParentId, link.getParentId());
        }
        
        // now call update w/parent id
        int cid = 1;
        String parentId = getParentId(cid, 1);
        service.updateNewItemLinks(parentId);
        
        // validate links updated w/parent
        for (Long linkId : linkIds)
        {
            PSManagedLink link = dao.findLinkByLinkId(linkId);
            assertEquals(cid, link.getParentId());
        }
    }
    
    /**
     * Validate the links in the supplied result are managed as expected.
     * 
     * @param source The source html, with the unmanaged links
     * @param result The resulting html, with the managed links
     * @param ids The dependent ids, also used as the id of the anchor element to validate the match.
     * 
     * @return The list of link ids for all managed links found
     */
    private List<Long> validateManagedLinks(String source, String result, List<String> ids)
    {
        List<Long> linkIds = new ArrayList<Long>();
        
        Document doc = Jsoup.parseBodyFragment(result);
        for (String id : ids)
        {
            Element el = doc.getElementById(id);
            assertNotNull(el);
            String linkIdVal = el.attr(PERC_LINKID_ATTR);
            assertNotNull(linkIdVal);
            String managedAttr = el.attr(PERC_MANAGED_ATTR);
            assertEquals("true",managedAttr);
            assertTrue(NumberUtils.isNumber(linkIdVal));
            long linkId = Long.parseLong(linkIdVal);
            PSManagedLink link = dao.findLinkByLinkId(linkId);
            assertNotNull(link);
            assertEquals(id, String.valueOf(link.getChildId()));
            linkIds.add(linkId);
        }
        
        return linkIds;
    }
    
    private void validateItemPathLink(String linkIdVal, String childId)
    {
        assertNotNull(linkIdVal);
        assertTrue(NumberUtils.isNumber(linkIdVal));
        long linkId = Long.parseLong(linkIdVal);
        PSManagedLink link = dao.findLinkByLinkId(linkId);
        assertNotNull(link);
        assertEquals(childId, String.valueOf(link.getChildId()));        
    }
    
    private PSAsset createHtmlAsset(String name, String folder, boolean addToCleaner) throws Exception
    {
        if (folder == null)
            folder = PSAssetPathItemService.ASSET_ROOT + "/ManagedLinkTest";
        
        PSAsset asset = new PSAsset();
        asset.getFields().put("sys_title", name + System.currentTimeMillis());
        asset.setType("percRawHtmlAsset");
        asset.getFields().put("html", "TestHTML");
        if (folder != null)
        {
            asset.setFolderPaths(asList(folder));
        }
             
        asset = assetService.save(asset);
        if (addToCleaner)
            assetCleaner.add(asset.getId());
        
        return asset;
    }
    
    private PSAsset createFileAsset(int count)
    {
        String fileName = "managed-link.txt";
        String file = PSTestUtils.resourceToBase64(PSManagedLinkServiceTest.class, fileName);
        
        fileName = "managed-link_" + count + ".txt";
        
        PSAsset asset = new PSAsset();
        asset.getFields().put("sys_title", fileName);
        asset.getFields().put("displaytitle", "MyFile Displaytitle");
        asset.getFields().put("filename", fileName);
        asset.getFields().put("item_file_attachment", file);
        asset.getFields().put("item_file_attachment_filename", fileName);
        asset.getFields().put("item_file_attachment_type", "text/plain");
        asset.setType("percFileAsset");
        asset.setFolderPaths(asList(PSAssetPathItemService.ASSET_ROOT + "/ManagedLinkTest"));
        asset = assetService.save(asset);
        
        assetCleaner.add(asset.getId());
        return asset;
    }

    private PSAsset createImgAsset()
    {
        
        String fileName = "managed-image.jpg";
        /*
        String file = PSTestUtils.resourceToBase64(PSManagedLinkServiceTest.class, fileName);
        
        PSAsset asset = new PSAsset();
        asset.getFields().put("sys_title", fileName);
        asset.getFields().put("displaytitle", "MyFile Displaytitle");
        asset.getFields().put("filename", fileName);
        asset.getFields().put("item_file_attachment", file);
        asset.getFields().put("item_file_attachment_filename", fileName);
        asset.getFields().put("item_file_attachment_type", "text/plain");
        asset.setType("percImageAsset");
        asset.setFolderPaths(asList(PSAssetPathItemService.ASSET_ROOT + "/ManagedLinkTest"));
        asset = assetService.save(asset);
        
        assetCleaner.add(asset.getId());
        */
        InputStream in = getClass().getResourceAsStream(fileName);
        PSAbstractAssetRequest ar = new PSBinaryAssetRequest(PSAssetPathItemService.ASSET_ROOT + "/ManagedLinkTest",
                AssetType.IMAGE,
                fileName, "image/jpeg", in);

        PSAsset newAsset = assetService.createAsset(ar);
        return newAsset;
    }

    /**
     * @param service the service to set
     */
    public void setService(IPSManagedLinkService service)
    {
        this.service = service;
        unassignedParentId = PSManagedLinkService.UNASSIGNED_PARENT_ID;
    }
    
    /**
     * @param contentWs the contentWs to set
     */
    public void setContentWs(IPSContentWs contentWs)
    {
        this.contentWs = contentWs;
    }

    /**
     * @param idMapper the idMapper to set
     */
    public void setIdMapper(IPSIdMapper idMapper)
    {
        this.idMapper = idMapper;
    }

    /**
     * @param assetService the assetService to set
     */
    public void setAssetService(IPSAssetService assetService)
    {
        this.assetService = assetService;
    }

    /**
     * @param renderLinkContextFactory the renderLinkContextFactory to set
     */
    public void setRenderLinkContextFactory(IPSRenderLinkContextFactory renderLinkContextFactory)
    {
        this.renderLinkContextFactory = renderLinkContextFactory;
    }

    /**
     * @param workflowHelper the workflowHelper to set
     */
    public void setWorkflowHelper(IPSWorkflowHelper workflowHelper)
    {
        this.workflowHelper = workflowHelper;
    }

    /**
     * @param itemWorkflowService the itemWorkflowService to set
     */
    public void setItemWorkflowService(IPSItemWorkflowService itemWorkflowService)
    {
        this.itemWorkflowService = itemWorkflowService;
    }

    /**
     * Test that links are properly managed
     * 
     * @throws Exception
     */
    public void testManageItemPath() throws Exception
    {
        String result;
    
        String path = fixture.site1.getFolderPath() + "/index.html";
        String href = PSPathUtils.getFinderPath(path);
        
        
        IPSGuid itemGuid = contentWs.getIdByPath(path);
        String itemId = String.valueOf(itemGuid.getUUID());
        int parentContentId = 1;
        String parentId = getParentId(parentContentId, 1);
        result = service.manageItemPath(parentId, href, null);
        validateItemPathLink(result, itemId);
        
        // test managed link w/wrong href
        String badPath = fixture.site1.getFolderPath() + "/home";
        result = service.manageItemPath(parentId, badPath, result);
        validateItemPathLink(result, itemId);
    
        // test img asset managed link
        PSAsset imgAsset = createImgAsset();
        
        String imgPathAsset = StringUtils.join(imgAsset.getFolderPaths(), '/');
        href = PSPathUtils.getFinderPath(imgPathAsset) + "/" + imgAsset.getName();
        itemGuid = contentWs.getIdByPath(imgPathAsset + "/" + imgAsset.getName());
        itemId = String.valueOf(itemGuid.getUUID());
        parentContentId = 1;
        parentId = getParentId(parentContentId, 1);
        result = service.manageItemPath(parentId, href, null);
        validateItemPathLink(result, itemId);
        
        // test image managed link w/wrong src
        String imgBadPathAsset = StringUtils.join(imgAsset.getFolderPaths(), "/");
        String badSrc = PSPathUtils.getFinderPath(imgBadPathAsset).replace("ManagedLinkTest", "ManagedLinkTest1") + "/" + imgAsset.getName();
        result = service.manageItemPath(parentId, badSrc, result);
        validateItemPathLink(result, itemId);
        
        
        PSAsset asset = null;
        try
        {
            asset = createHtmlAsset("test", "//Folders", false);
            parentId = asset.getId();
            parentContentId = idMapper.getLocator(parentId).getId();
            parentLinkIds.add(parentContentId);
    
            href = PSPathUtils.getFinderPath(imgPathAsset) + "/" + imgAsset.getName();
            itemGuid = contentWs.getIdByPath(imgPathAsset + "/" + imgAsset.getName());
            itemId = String.valueOf(itemGuid.getUUID());

            String name = "page1";
            path = fixture.site1.getFolderPath() + "/folder1";
            String pageId = createPage(path, name);
            itemId = String.valueOf(idMapper.getContentId(pageId));
            href = PSPathUtils.getFinderPath(path) + "/" + name;
            
            result = service.manageItemPath(parentId, href, null);
            validateItemPathLink(result, itemId);
            
            // test new revision of parent
            Set<String> idSet = new HashSet<String>();
            idSet.add(parentId);
            workflowHelper.transitionToPending(idSet);
            itemWorkflowService.checkOut(parentId);
            itemWorkflowService.checkIn(parentId);
            PSComponentSummary sum = workflowHelper.getComponentSummary(parentId);
            parentId = idMapper.getGuid(sum.getCurrentLocator()).toString();
            long linkId = Long.parseLong(result);
            result = service.manageItemPath(parentId, href, result);
            validateItemPathLink(result, itemId);
            PSManagedLink link = dao.findLinkByLinkId(linkId);
            assertNotNull(link);
            assertEquals(2, link.getParentRevision());
            
            // test wrong parent id for link (manually copied html) - should act as newly managed link
            int parentContentid2 = 2;
            String parentId2 = getParentId(parentContentid2, 1);
            String previous = result;
            result = service.manageItemPath(parentId2, href, previous);
            validateItemPathLink(result, itemId);
            assertTrue(result != previous);
            
            link = dao.findLinkByLinkId(linkId);
            assertNotNull(link);
            assertEquals(parentContentId, link.getParentId());
            
            linkId = Long.parseLong(result);
            link = dao.findLinkByLinkId(linkId);
            assertNotNull(link);
            assertEquals(parentContentid2, link.getParentId());
    
            // test delete child
            fixture.getPageService().delete(pageId);
            result = service.manageItemPath(parentId, href, previous);
            assertNull(result);
            
            // Managed links now cleaned up in PSSqlPurgeHelper if parent deleted.
            assertTrue(dao.findLinksByParentId(parentContentId).isEmpty());
            // test delete parent
            /*
            assertFalse(dao.findLinksByParentId(parentContentId).isEmpty());
            assetService.delete(parentId);
            asset = null;
            assertTrue("Links were not deleted", dao.findLinksByParentId(parentContentId).isEmpty());  
            */     
            
        }
        finally
        {
            if (asset != null)
                assetService.delete(asset.getId());
        }        
    }
    

    /***
     * Test the getlinkid routine
     */
    public void testGetLinkId(){
    	
    	Element el = new Element(Tag.valueOf("a"), "");
    	assertEquals(0,service.getLinkId(el));
    	
    	el.attr(IPSManagedLinkService.PERC_LINKID_OLD_ATTR, "99");
    	el.attr(IPSManagedLinkService.PERC_MANAGED_OLD_ATTR, "true");
    	assertEquals(99,service.getLinkId(el) );
    	assertEquals("",el.attr(IPSManagedLinkService.PERC_LINKID_OLD_ATTR));
    	assertEquals("",el.attr(IPSManagedLinkService.PERC_MANAGED_OLD_ATTR));
    	assertEquals("true",el.attr(IPSManagedLinkService.PERC_MANAGED_ATTR));
    	assertEquals("99",el.attr(IPSManagedLinkService.PERC_LINKID_ATTR));
      	assertEquals(99,service.getLinkId(el) );
      
    }
}
