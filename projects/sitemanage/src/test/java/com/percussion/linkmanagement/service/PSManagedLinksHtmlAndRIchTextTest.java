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

package com.percussion.linkmanagement.service;

import static java.util.Arrays.asList;

import com.percussion.assetmanagement.data.PSAbstractAssetRequest;
import com.percussion.assetmanagement.data.PSAbstractAssetRequest.AssetType;
import com.percussion.assetmanagement.data.PSAsset;
import com.percussion.assetmanagement.data.PSBinaryAssetRequest;
import com.percussion.assetmanagement.service.IPSAssetService;
import com.percussion.linkmanagement.service.impl.PSManagedLinksConverter;
import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.service.PSSiteDataServletTestCaseFixture;
import com.percussion.pagemanagement.service.PSSiteDataServletTestCaseFixture.PSAssetCleaner;
import com.percussion.pathmanagement.service.impl.PSAssetPathItemService;
import com.percussion.server.PSServer;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.share.spring.PSSpringWebApplicationContextUtils;
import com.percussion.share.test.PSTestUtils;
import com.percussion.test.PSServletTestCase;

import java.io.InputStream;
import java.util.Properties;

import com.percussion.utils.testing.IntegrationTest;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.experimental.categories.Category;

/**
 * Tests whether a new style managed link/image in rich text asset is converted to old style link or not. 
 * When we support new style managed link/image in rich text asset then this code needs to be refactored. 
 */
@Category(IntegrationTest.class)
public class PSManagedLinksHtmlAndRIchTextTest extends PSServletTestCase 
{
    private static final String MANAGED_LINK_TEST = "/ManagedLinkTest";
	private static final String TEXT = "text";
    private static final String HTML = "html";
	private static final String SYS_RELATIONSHIPID = "sys_relationshipid";
	private PSSiteDataServletTestCaseFixture fixture;
    private PSAssetCleaner assetCleaner;
    private IPSAssetService assetService;
    PSPage page = null;
    PSAsset fileAsset = null;
    PSAsset imgAsset = null;
    private String propOriginal = "false";
    
    public IPSAssetService getAssetService() {
		return assetService;
	}
	public void setAssetService(IPSAssetService assetService) {
		this.assetService = assetService;
	}

	private String TEST_PREFIX = "ManagedLinksConverterTest";
    
    @Override
    public void setUp() throws Exception
    {
        PSSpringWebApplicationContextUtils.injectDependencies(this);
        
        fixture = new PSSiteDataServletTestCaseFixture(request, response);
        fixture.setUp();
        assetCleaner = fixture.assetCleaner;
    	page = fixture.createPage(TEST_PREFIX + "Page");
    	fileAsset = createFileAsset();
    	imgAsset = createImgAsset();
    	 //FB:IJU_SETUP_NO_SUPER NC 1-16-16
        super.setUp();
    }
    @Override
    public void tearDown() throws Exception
    {
    	fixture.tearDown();
    }
    
    
    public void testManagedPathsInRichText() throws Exception
    {
    	//Make sure converter code doesn't throw exception with empty content for rich text
    	PSAsset richAsset = createRichTextAsset("");
    	//No links case doesn't fail
    	updateRichTextAsset(richAsset, "Test Rich Text");
    	 
    	String content = generateTestContent();
    	
    	richAsset = createRichTextAsset(content);
    	String result = getRichTextContent(richAsset);

    	Document doc = Jsoup.parseBodyFragment(result);
    	
    	//Verify link with perc-managed attr
    	verifyManagedLink(doc, LINK_WITH_PERC_MANAGED, true);

        //Verify link without perc-managed attr
    	verifyManagedLink(doc, LINK_WITHOUT_PERC_MANAGED, true);

    	//Verify img with perc-managed attr
    	verifyManagedImg(doc, IMG_WITH_PERC_MANAGED, true);
    	
        //Verify img without perc-managed attr
    	verifyManagedImg(doc, IMG_WITHOUT_PERC_MANAGED, true);

        //Verify link to resource with perc-managed attr
    	verifyManagedLink(doc, LINK_WITH_PERC_MANAGED_TO_RESOURCE, true);

        //Verify link to resource without perc-managed attr
    	verifyManagedLink(doc, LINK_WITHOUT_PERC_MANAGED_TO_RESOURCE, true);
        
        //Verify external link is not damaged
    	verifyExternalLink(doc, EXTERNAL_LINK);
        
        //Recreate the asset
    	richAsset = createRichTextAsset(content);
    	result = getRichTextContent(richAsset);

    	doc = Jsoup.parseBodyFragment(result);
        
        //Re-test all the cases again
    	
    	//Verify link with perc-managed attr still works
    	verifyManagedLink(doc, LINK_WITH_PERC_MANAGED, true);

        //Verify link without perc-managed attr has managed links now
    	verifyManagedLink(doc, LINK_WITH_PERC_MANAGED, true);

    	//Verify img with perc-managed attr still works
    	verifyManagedImg(doc, IMG_WITH_PERC_MANAGED, true);
    	
        //Verify img without perc-managed attr has managed image now
    	verifyManagedImg(doc, IMG_WITHOUT_PERC_MANAGED, true);

        //Verify link to resource with perc-managed attr still works
    	verifyManagedLink(doc, LINK_WITH_PERC_MANAGED_TO_RESOURCE, true);

        //Verify link to resource without perc-managed attr has managed links now
    	verifyManagedLink(doc, LINK_WITHOUT_PERC_MANAGED_TO_RESOURCE, true);
        
        //Verify external link is not damaged
    	verifyExternalLink(doc, EXTERNAL_LINK);
        
    }
    
    public void testManagedPathsInHtml() throws Exception
    {
        //Make sure converter code doesn't throw exception with empty content for rich text
    	PSAsset htmlAsset = createHtmlAsset("");
    	//No links case doesn't fail
    	updateHtmlAsset(htmlAsset, "Test HTML Text");
    	
    	String content = generateTestContent();
    	
    	htmlAsset = createHtmlAsset(content);
    	String result = getHtmlContent(htmlAsset);

    	Document doc = Jsoup.parseBodyFragment(result);
    	
    	//Verify link with perc-managed attr
    	verifyManagedLink(doc, LINK_WITH_PERC_MANAGED, false);

        //Verify link without perc-managed attr
    	verifyManagedLink(doc, LINK_WITHOUT_PERC_MANAGED, false);

    	//Verify img with perc-managed attr
    	verifyManagedImg(doc, IMG_WITH_PERC_MANAGED, false);
    	
        //Verify img without perc-managed attr
    	verifyManagedImg(doc, IMG_WITHOUT_PERC_MANAGED, false);

        //Verify link to resource with perc-managed attr
    	verifyManagedLink(doc, LINK_WITH_PERC_MANAGED_TO_RESOURCE, false);

        //Verify link to resource without perc-managed attr
    	verifyManagedLink(doc, LINK_WITHOUT_PERC_MANAGED_TO_RESOURCE, false);
        
        //Verify external link is not damaged
    	verifyExternalLink(doc, EXTERNAL_LINK);
        
        //Recreate the asset
    	htmlAsset = createHtmlAsset(content);
    	result = getHtmlContent(htmlAsset);

    	doc = Jsoup.parseBodyFragment(result);
        
        //Re-test all the cases again
    	
    	//Verify link with perc-managed attr still works
    	verifyManagedLink(doc, LINK_WITH_PERC_MANAGED, false);

        //Verify link without perc-managed attr has managed links now
    	verifyManagedLink(doc, LINK_WITHOUT_PERC_MANAGED, false);

    	//Verify img with perc-managed attr still works
    	verifyManagedImg(doc, IMG_WITH_PERC_MANAGED, false);
    	
        //Verify img without perc-managed attr has managed image now
    	verifyManagedImg(doc, IMG_WITHOUT_PERC_MANAGED, false);

        //Verify link to resource with perc-managed attr still works
    	verifyManagedLink(doc, LINK_WITH_PERC_MANAGED_TO_RESOURCE, false);

        //Verify link to resource without perc-managed attr has managed links now
    	verifyManagedLink(doc, LINK_WITHOUT_PERC_MANAGED_TO_RESOURCE, false);
        
        //Verify external link is not damaged
    	verifyExternalLink(doc, EXTERNAL_LINK);
        
    }
    
    private void verifyManagedLink(Document doc, String className, boolean isOldStyle)
    {
        Elements elems = doc.getElementsByClass(className);
        assertEquals(1, elems.size());
        Element elem = elems.get(0);
        if(isOldStyle){
	        assertEquals(PSManagedLinksConverter.RXHYPERLINK, elem.attr(PSManagedLinksConverter.INLINETYPE));
	        assertFalse(elem.attr(SYS_RELATIONSHIPID).equals(""));
        }
        else{
	        assertFalse(elem.attr(IPSManagedLinkService.PERC_LINKID_ATTR).equals(""));
        }
    }
    
    private void verifyNonManagedLink(Document doc, String className, boolean isOldStyle)
    {
        Elements elems = doc.getElementsByClass(className);
        assertEquals(1, elems.size());
        Element elem = elems.get(0);
        if(isOldStyle){
        	assertTrue(elem.attr(PSManagedLinksConverter.INLINETYPE).equals(""));
        	assertTrue(elem.attr(SYS_RELATIONSHIPID).equals(""));
        }
        else{
	        assertTrue(elem.attr(IPSManagedLinkService.PERC_LINKID_ATTR).equals(""));
        }
    }
   
    private void verifyManagedImg(Document doc, String className, boolean isOldStyle)
    {
    	Elements elems = doc.getElementsByClass(className);
        assertEquals(1, elems.size());
        Element elem = elems.get(0);
        if(isOldStyle){
	        assertEquals(PSManagedLinksConverter.RXIMAGE, elem.attr(PSManagedLinksConverter.INLINETYPE));
	        assertFalse(elem.attr(SYS_RELATIONSHIPID).equals(""));
        }
        else{
	        assertFalse(elem.attr(IPSManagedLinkService.PERC_LINKID_ATTR).equals(""));
        }
    }

    private void verifyNonManagedImg(Document doc, String className, boolean isOldStyle)
    {
        Elements elems = doc.getElementsByClass(className);
        assertEquals(1, elems.size());
        Element elem = elems.get(0);
        if(isOldStyle){
	        assertTrue(elem.attr(PSManagedLinksConverter.INLINETYPE).equals(""));
	        assertTrue(elem.attr(SYS_RELATIONSHIPID).equals(""));
        }
        else{
	        assertTrue(elem.attr(IPSManagedLinkService.PERC_LINKID_ATTR).equals(""));
        }
    }

    private void verifyExternalLink(Document doc, String className)
    {
        Elements elems = doc.getElementsByClass(className);
        assertEquals(1, elems.size());
        Element elem = elems.get(0);
        assertTrue(elem.attr(PSManagedLinksConverter.INLINETYPE).equals(""));
        assertTrue(elem.attr(SYS_RELATIONSHIPID).equals(""));
        assertTrue(elem.attr("href").equals(EXTERNAL_LINK_URL));
    }

    private PSAsset createRichTextAsset(String content) throws Exception
    {
        String name = TEST_PREFIX + "RichTextAsset";
        String folder = PSAssetPathItemService.ASSET_ROOT + MANAGED_LINK_TEST;
        
        PSAsset asset = new PSAsset();
        asset.getFields().put("sys_title", name + System.currentTimeMillis());
        asset.setType("percRichTextAsset");
        asset.getFields().put(TEXT, content);
        if (folder != null)
        {
            asset.setFolderPaths(asList(folder));
        }
             
        asset = assetService.save(asset);
        assetCleaner.add(asset.getId());
        
        return asset;
    }
    
    private PSAsset createHtmlAsset(String content) throws Exception
    {
        String name = TEST_PREFIX + "HtmlAsset";
        String folder = PSAssetPathItemService.ASSET_ROOT + MANAGED_LINK_TEST;
        
        PSAsset asset = new PSAsset();
        asset.getFields().put("sys_title", name + System.currentTimeMillis());
        asset.setType("percRawHtmlAsset");
        asset.getFields().put(HTML, content);
        if (folder != null)
        {
            asset.setFolderPaths(asList(folder));
        }
             
        asset = assetService.save(asset);
        assetCleaner.add(asset.getId());
        
        return asset;
    }
    
    private PSAsset updateRichTextAsset(PSAsset richAsset, String content) throws Exception
    {
    	richAsset.getFields().put(TEXT, content);
    	richAsset = assetService.save(richAsset);
        return richAsset;
    }

    private String getRichTextContent(PSAsset richAsset)
    {
    	return richAsset.getFields().get(TEXT).toString();
    }
    
    private PSAsset updateHtmlAsset(PSAsset htmlAsset, String content) throws Exception
    {
    	htmlAsset.getFields().put(HTML, content);
    	htmlAsset = assetService.save(htmlAsset);
        return htmlAsset;
    }

    private String getHtmlContent(PSAsset htmlAsset)
    {
    	return htmlAsset.getFields().get(HTML).toString();
    }

    private PSAsset createFileAsset() throws PSDataServiceException {
        String fileName = "managed-link.txt";
        String file = PSTestUtils.resourceToBase64(PSManagedLinkServiceTest.class, fileName);
        
        PSAsset asset = new PSAsset();
        asset.getFields().put("sys_title", fileName);
        asset.getFields().put("displaytitle", "MyFile Displaytitle");
        asset.getFields().put("filename", fileName);
        asset.getFields().put("item_file_attachment", file);
        asset.getFields().put("item_file_attachment_filename", fileName);
        asset.getFields().put("item_file_attachment_type", "text/plain");
        asset.setType("percFileAsset");
        asset.setFolderPaths(asList(PSAssetPathItemService.ASSET_ROOT + MANAGED_LINK_TEST));
        asset = assetService.save(asset);
        
        assetCleaner.add(asset.getId());
        return asset;
    }

    private PSAsset createImgAsset() throws IPSAssetService.PSAssetServiceException, PSValidationException {
        
        String fileName = "managed-image.jpg";
        InputStream in = getClass().getResourceAsStream(fileName);
        PSAbstractAssetRequest ar = new PSBinaryAssetRequest(PSAssetPathItemService.ASSET_ROOT + MANAGED_LINK_TEST,
                AssetType.IMAGE,
                fileName, "image/jpeg", in);

        PSAsset newAsset = assetService.createAsset(ar);
        assetCleaner.add(newAsset.getId());
        return newAsset;
    }
    
    private String generateTestContent()
    {
    	String pagePath = page.getFolderPath() + "/" + page.getName();
    	String imgPath = "/Assets" + MANAGED_LINK_TEST + "/" + imgAsset.getName();
    	String filePath = "/Assets" + MANAGED_LINK_TEST + "/" + fileAsset.getName();
    	
    	//Note that managed links now look at the path of the link to see if it is qualified
    	String text = "<div>"
				+ "<a perc-managed=\"true\" class=\"" + LINK_WITH_PERC_MANAGED + "\" href=\"" + pagePath + "\">Link With Perc Managed</a>"
				+ "<a class=\"" + LINK_WITHOUT_PERC_MANAGED + "\" href=\"" + pagePath + "\">Link Without Perc Managed</a>"
				+ "<img perc-managed=\"true\" class=\"" + IMG_WITH_PERC_MANAGED + "\" src=\"" + imgPath + "\"/>"
				+ "<img class=\"" + IMG_WITHOUT_PERC_MANAGED + "\" src=\"" + imgPath + "\"/>"
				+ "<a perc-managed=\"true\" class=\"" + LINK_WITH_PERC_MANAGED_TO_RESOURCE + "\" href=\"" + filePath + "\">Link With Perc Managed To A Resource</a>"
				+ "<a class=\"" + LINK_WITHOUT_PERC_MANAGED_TO_RESOURCE + "\" href=\"" + imgPath + "\">Link Without Perc Managed To A Resource</a>"
				+ "<a class=\"" + EXTERNAL_LINK + "\" href=\"" + EXTERNAL_LINK_URL + "\">External Link</a>"
				+ "</div>";
    					
    	return text;
    }
    
    private static final String LINK_WITH_PERC_MANAGED = "link-with-perc-managed";
    private static final String LINK_WITHOUT_PERC_MANAGED = "link-without-perc-managed";
    private static final String IMG_WITH_PERC_MANAGED = "img-with-perc-managed";
    private static final String IMG_WITHOUT_PERC_MANAGED = "img-without-perc-managed";
    private static final String LINK_WITH_PERC_MANAGED_TO_RESOURCE = "link-with-perc-managed-to-resource";
    private static final String LINK_WITHOUT_PERC_MANAGED_TO_RESOURCE = "link-without-perc-managed-to-resource";
    private static final String EXTERNAL_LINK = "external-link";
    private static final String EXTERNAL_LINK_URL = "http://www.msn.com/";
}
