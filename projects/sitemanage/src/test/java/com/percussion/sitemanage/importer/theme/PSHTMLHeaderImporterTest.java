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
package com.percussion.sitemanage.importer.theme;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.percussion.sitemanage.importer.IPSSiteImportLogger;
import com.percussion.sitemanage.importer.IPSSiteImportLogger.PSLogObjectType;
import com.percussion.sitemanage.importer.PSSiteImportLogger;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Unit test to cover the use cases when importing the header
 * 
 * @author Leonardo M. Hildt
 * 
 */
@Ignore("Uses P.com")
public class PSHTMLHeaderImporterTest
{
    private IPSSiteImportLogger logger = new PSSiteImportLogger(PSLogObjectType.SITE);
    
    private Document sourceDoc = null;
    
    private Element docHeader = null;
    
    private Element docBody = null;

    
    private String installDir = "C:/DevEnv/Installs/dev/";
    
    private String absoluteThemePath = "web_resources/themes/www.percussion.com";

    private static final String siteName = "www.percussion.com";
    
    private static final String themeName = "www.percussion.com";
    
    private static final String themePath = "/web_resources/themes/" + themeName;

    private static final String siteUrl = "http://www.percussion.com";
    
    private PSHTMLHeaderImporter headerImporter;
    
    @Before
    public void setup() throws IOException
    {
    	if(System.getProperty("install.dir")!=null){
    		installDir = System.getProperty("install.dir");
    		//FB: NS_DANGEROUS_NON_SHORT_CIRCUIT NC 1-16-16
    		if(!(installDir.endsWith("/") || installDir.endsWith("\\")) ){
    			installDir = installDir.concat("/");
    		}
    	}
    	
    	absoluteThemePath = installDir + absoluteThemePath;
    	    	
        sourceDoc = Jsoup.parse(new File("src/test/resources/importer/CM1905-SamplePage.html"), "UTF-8");
        docHeader = sourceDoc.head();
        docBody = sourceDoc.body();
        headerImporter = new PSHTMLHeaderImporter(sourceDoc, siteUrl, siteName, absoluteThemePath, themePath, logger);
    }

    @Test
    public void testGetLinks()
    {
        try
        {
            // Get the links before importing the CSS link paths
            Elements originalLinks = this.docHeader.select("link");
            originalLinks.addAll(this.docBody.select("link"));
            
            assertTrue(this.docHeader.toString().contains("http://www.functravel.com/css/0.1/screen/common/masthead.css"));
            
            //Relative URL to site
            assertEquals("/Rhythmyx/web_resources/cm/css/perc_decoration.css", originalLinks.get(0).attr("href"));
            
            //Relative URL to page
            assertEquals("perc_theme.css", originalLinks.get(2).attr("href"));
            
            //Absolute URL with the same host as the base URL used to create the site
            assertEquals("http://www.percussion.com/css/0.1/screen/common/masthead.css", originalLinks.get(3).attr("href"));
            
            //Absolute URL with the different host as the base URL used to create the site
            assertEquals("http://www.functravel.com/css/0.1/screen/common/masthead.css", originalLinks.get(4).attr("href"));
            
            // Call the HTML Header importer to process the CSS link paths. Check the CSS link paths after processing
            Map<String, String> linkPaths = headerImporter.getLinkPaths();
            assertNotNull(linkPaths);
            assertFalse(linkPaths.isEmpty());
            assertTrue(linkPaths.size() == 12);
            
            // Get the links from the updated header
            Elements links = docHeader.select("link");
            links.addAll(docBody.select("link"));

            // Assert for link with relative URL to the site
            String newLinkExpected = "/web_resources/themes/www.percussion.com/import/www.percussion.com/Rhythmyx/web_resources/cm/css/perc_decoration.css";
            assertEquals(newLinkExpected, links.get(0).attr("href"));
            
            // Assert for link with relative URL to the page
            newLinkExpected = "/web_resources/themes/www.percussion.com/import/www.percussion.com/perc_theme.css";
            assertEquals(newLinkExpected, links.get(2).attr("href"));
            
            // Assert for link with absolute URL path, with the same host as the base URL used to create the site
            newLinkExpected = "/web_resources/themes/www.percussion.com/import/www.percussion.com/css/0.1/screen/common/masthead.css";
            assertEquals(newLinkExpected, links.get(3).attr("href"));
            
            // Assert for shortcut icon link with relative URL path
            newLinkExpected = "/web_resources/themes/www.percussion.com/import/www.percussion.com/s/en_US-j7rwzw/649/favicon.ico";
            assertEquals(newLinkExpected, links.get(6).attr("href"));
           
            // Assert for icon link with relative URL path
            newLinkExpected = "/web_resources/themes/www.percussion.com/import/www.percussion.com/s/en_US-j7rwzw/649/icons/favicon.png";
            assertEquals(newLinkExpected, links.get(7).attr("href"));
            
            // Assert for icon link with relative URL path
            newLinkExpected = "/web_resources/themes/www.percussion.com/import/www.percussion.com/favicon.ico";
            assertEquals(newLinkExpected, links.get(8).attr("href"));
            
            // Assert for link with absolute URL to external CSS file, with different host as the base URL used to create the site
            newLinkExpected = "/web_resources/themes/www.percussion.com/import/cache.boston.com/universal/newsprojects/widgets/slider/slider.css";
            assertEquals(newLinkExpected, links.get(9).attr("href"));
            
            // Assert for dynamic CSS content
            newLinkExpected = "/web_resources/themes/www.percussion.com/import/www.percussion.com/CSS/homepage.cfm.css";
            assertEquals(newLinkExpected, links.get(10).attr("href"));
            
            // Assert for link with relative URL to the site that is in document body
            newLinkExpected = "/web_resources/themes/www.percussion.com/import/www.percussion.com/NewHome/engine3/style.css";
            assertEquals(newLinkExpected, links.get(11).attr("href"));
        }
        catch (Exception e)
        {
            fail("No exception should have been thrown.");
        }
    }

    @Test
    public void testGetScripts()
    {
        // Get the links before importing the CSS link paths
        Elements originalLinks = this.docHeader.select("script");
        originalLinks.addAll(this.docBody.select("script"));
        
        // Check the header contains the absolute URL
        assertTrue(this.docHeader.toString().contains("http://www.functravel.com/js/utils/myscript.js"));
        
        //Relative URL
        assertEquals("/Rhythmyx/web_resources/cm/jslib/jquery.js", originalLinks.get(0).attr("src"));
        
        //Absolute URL with the same host as the base URL used to create the site
        assertEquals("http://www.percussion.com/js/scriptaculous.js", originalLinks.get(4).attr("src"));
        
        //Absolute URL with the different host as the base URL used to create the site
        assertEquals("http://www.functravel.com/js/utils/myscript.js", originalLinks.get(5).attr("src"));
        
        //Relative URL referenced in the body
        assertEquals("/templates/percussion/js/jquery.hoverIntent.minified.js", originalLinks.get(9).attr("src"));
        
        // Call the HTML Header importer to process the script paths and check if the paths were updated as expected
        Map<String, String> scriptsPaths;
        scriptsPaths = headerImporter.getScriptPaths();

        assertNotNull(scriptsPaths);
        assertFalse(scriptsPaths.isEmpty());
        assertTrue(scriptsPaths.size() == 9);

        // Check if the paths for header scripts are updated as expected
        Elements headerScripts = docHeader.select("script");
        
        // The relative URL should be changed as expected
        String newSrcExpected = "/web_resources/themes/www.percussion.com/import/www.percussion.com/Rhythmyx/web_resources/cm/jslib/jquery.js";
        assertEquals(newSrcExpected, headerScripts.get(0).attr("src"));
        
        //Absolute URL with the same host as the base URL used to create the site should be changed as expected
        assertEquals("/web_resources/themes/www.percussion.com/import/www.percussion.com/js/scriptaculous.js", headerScripts.get(4).attr("src"));
        
        // Check if the paths for body scripts are updated as expected
        Elements bodyScripts = docBody.select("script");
        
        // The relative URL should be changed as expected
        newSrcExpected = "/web_resources/themes/www.percussion.com/import/www.percussion.com/templates/percussion/js/jquery.min.js";
        assertEquals(newSrcExpected, bodyScripts.get(0).attr("src"));
        
        //Absolute URL with the same host as the base URL used to create the site should be changed as expected
        assertEquals("/web_resources/themes/www.percussion.com/import/www.percussion.com/templates/percussion/js/jquery.hoverIntent.minified.js", bodyScripts.get(1).attr("src"));
    }
    
    @Test
    public void testProcessInlineImages()
    {
        Map<String, String> inlineImages;
        inlineImages = headerImporter.processInlineStyles();

        // Asserts for the returned map of images
        assertNotNull(inlineImages);
        assertFalse(inlineImages.isEmpty());
        assertTrue(inlineImages.size() == 5);
        
        // Asserts to check if the returned map contains the following keys for absolute URLs
        assertTrue(inlineImages.containsKey("http://www.percussion.com/images/ui-bg_highlight-soft_100_eeeeee_1x100.png"));
        assertTrue(inlineImages.containsKey("http://www.percussion.com/images/bullet.png"));
        assertTrue(inlineImages.containsKey("http://www.percussion.com/bullet.png"));
        
        // Asserts to check if the returned map contains the following values for paths
        assertTrue(inlineImages.containsValue("C:/DevEnv/Installs/dev/web_resources/themes/www.percussion.com/import/www.percussion.com/images/ui-bg_highlight-soft_100_eeeeee_1x100.png"));  
        assertTrue(inlineImages.containsValue("C:/DevEnv/Installs/dev/web_resources/themes/www.percussion.com/import/www.percussion.com/images/bullet.png"));
        assertTrue(inlineImages.containsValue("C:/DevEnv/Installs/dev/web_resources/themes/www.percussion.com/import/www.percussion.com/bullet.png"));
       
        // Asserts for the updated header
        assertTrue(StringUtils.contains(docHeader.toString(), "/web_resources/themes/www.percussion.com/import/www.percussion.com/images/ui-bg_highlight-soft_100_eeeeee_1x100.png"));
        assertTrue(StringUtils.contains(docHeader.toString(), "/web_resources/themes/www.percussion.com/import/www.percussion.com/images/bullet.png"));
        
        // Inline imports in head 
        //assertTrue(StringUtils.contains(docHeader.toString(), "@import '/web_resources/themes/www.percussion.com/import/www.percussion.com/styles1.css';"));
        //assertTrue(StringUtils.contains(docHeader.toString(), "@import '/web_resources/themes/www.percussion.com/import/www.percussion.com/styles2.css';"));
        
        // Asserts for the updated body       
        assertTrue(StringUtils.contains(docBody.toString(), "/web_resources/themes/www.percussion.com/import/www.percussion.com/images/grad.gif"));
        assertTrue(StringUtils.contains(docBody.toString(), "images/grad3.gif"));
    }
    
    @Test
    public void testProcessHeaderAndBodyImages()
    {
        Elements imgElements = docBody.getElementsByTag("img");

        assertNotNull(imgElements);
        assertFalse(imgElements.isEmpty());
        assertTrue(imgElements.size() == 12);
        
        //Relative URL
        assertEquals("/homepage/2011/047.jpg", imgElements.get(2).attr("src"));
        
        //Absolute URL with the same host as the base URL used to create the site
        assertEquals("http://www.percussion.com/homepage/giveToCentral.gif", imgElements.get(9).attr("src"));
        
        //Absolute URL with the different host as the base URL used to create the site
        assertEquals("http://img.centralcollege.info/homepage/2011/049.jpg", imgElements.get(4).attr("src"));
        
        // Get the map of img tags 
        Map<String, String> bodyImages;
        bodyImages = headerImporter.processHeaderAndBodyImages();

        assertNotNull(bodyImages);
        assertFalse(bodyImages.isEmpty());
        assertTrue(bodyImages.size() == 13);
        
        // The returned map should have the following keys
        assertTrue(bodyImages.containsKey("http://www.percussion.com/homepage/2011/047.jpg"));
        assertTrue(bodyImages.containsKey("http://www.percussion.com/homepage/studentProfiles/2011/KatieTokle.jpg"));
        
        // The returned map should have the following values
        assertTrue(bodyImages.containsValue("/Assets/uploads/www.percussion.com/import/www.percussion.com/homepage/2011/047.jpg"));
        assertTrue(bodyImages.containsValue("/Assets/uploads/www.percussion.com/import/www.percussion.com/homepage/studentProfiles/2011/KatieTokle.jpg"));
        
        // Asserts for the updated body
        assertTrue(StringUtils.contains(docBody.toString(), "/Assets/uploads/www.percussion.com/import/www.percussion.com/images/menu/goteal.gif"));
        assertTrue(StringUtils.contains(docBody.toString(), "/Assets/uploads/www.percussion.com/import/www.percussion.com/homepage/2011/047.jpg"));
        assertTrue(StringUtils.contains(docBody.toString(), "/Assets/uploads/www.percussion.com/import/www.percussion.com/homepage/studentProfiles/2011/KatieTokle.jpg"));
        assertTrue(StringUtils.contains(docBody.toString(), "/Assets/uploads/www.percussion.com/import/www.google.com.ar/images/srpr/logo3w.png"));
    }
    
    @Test
    public void testGetFlashFiles()
    {
        // Get the links before importing the CSS link paths
        Elements originalFlashFiles = this.docHeader.select("embed");
        originalFlashFiles.addAll(this.docBody.select("embed"));
        
        // Check the header contains the absolute URL
        assertTrue(this.docBody.toString().contains("images/media/your_flash_file.swf"));
        
        //Relative URL
        assertEquals("images/media/your_flash_file.swf", originalFlashFiles.get(0).attr("src"));
        
        // Call the HTML Header importer to process the script paths and check if the paths were updated as expected
        Map<String, String> flashPaths;
        flashPaths = headerImporter.processFlashFiles(themeName);

        assertNotNull(flashPaths);
        assertFalse(flashPaths.isEmpty());
        assertTrue(flashPaths.size() == 3);

        // Check if the paths for body flash files are updated as expected
        Elements objectFlash = docBody.select("object");
        Elements bodyFlashFiles = docBody.select("embed");
        Elements movieFlashFiles = docBody.select("param[name=movie]");
        
        // Check that first flash object was updated
        String newSrcExpected = "/Assets/uploads/www.percussion.com/import/www.percussion.com/images/media/your_flash_file.swf";
        assertEquals(newSrcExpected, bodyFlashFiles.get(0).attr("src"));
        assertEquals(newSrcExpected, movieFlashFiles.get(0).attr("value"));
        
        // Assert equals for data atrribute
        assertEquals(newSrcExpected, objectFlash.get(0).attr("data"));
        
        // Check that second flash object was updated
        newSrcExpected = "/Assets/uploads/www.percussion.com/import/www.percussion.com/flash_slider/slider.swf";
        assertEquals(newSrcExpected, bodyFlashFiles.get(1).attr("src"));
        assertEquals(newSrcExpected, movieFlashFiles.get(1).attr("value"));
    }
}
