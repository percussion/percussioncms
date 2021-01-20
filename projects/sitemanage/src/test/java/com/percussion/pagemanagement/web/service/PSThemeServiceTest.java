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

package com.percussion.pagemanagement.web.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import com.percussion.utils.testing.IntegrationTest;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.percussion.pathmanagement.data.PSDeleteFolderCriteria;
import com.percussion.pathmanagement.data.PSDeleteFolderCriteria.SkipItemsType;
import com.percussion.pathmanagement.web.service.PSPathServiceRestClient;
import com.percussion.share.test.PSRestClient.RestClientException;
import com.percussion.share.test.PSRestTestCase;
import com.percussion.share.test.PSTestDataCleaner;
import com.percussion.theme.data.PSRegionCSS;
import com.percussion.theme.data.PSRegionCSS.Property;
import com.percussion.theme.data.PSRegionCssList;
import com.percussion.theme.data.PSTheme;
import com.percussion.theme.data.PSThemeSummary;
import org.junit.experimental.categories.Category;

@Category(IntegrationTest.class)
public class PSThemeServiceTest extends PSRestTestCase<PSThemeRestClient>
{
    private static PSTestSiteData testSiteData;
    
    @BeforeClass
    public static void setUpFixture() throws Exception
    {
        testSiteData = new PSTestSiteData();
        testSiteData.setUp();
    }

    @Before
    public void setUp() throws Exception
    {
        restClient.prepareForEditRegionCSS(PERC_THEME, TEMPLATE);
    }
    
    @After
    public void tearDown()
    {
        restClient.clearCacheRegionCSS(PERC_THEME, TEMPLATE);
    }
    

    @AfterClass
    public static void cleanup() throws Exception
    {
        testSiteData.tearDown();
    }    
    
    @Override
    protected PSThemeRestClient getRestClient(String baseUrl)
    {
        return new PSThemeRestClient();
    }

    protected PSTemplateServiceClient getTemplateRestClient(String baseUrl)
    {
        return new PSTemplateServiceClient(baseUrl);
    }
    
    @Test
    public void testFindAll() throws Exception
    {
       List<PSThemeSummary> themes = restClient.findAll();
       assertTrue(themes != null);
       
       assertTrue(themes.size() > 0);
       
       boolean findPercussion = false;
       for (PSThemeSummary sum : themes)
       {
          if (sum.getName().equals(PERC_THEME))
             findPercussion = true;
       }
       assertTrue("Must have a \"" + PERC_THEME + "\" theme", findPercussion);
    }
    
    @Test
    public void testLoadCSS() throws Exception
    {
        List<PSThemeSummary> themes = restClient.findAll();
        for (PSThemeSummary theme : themes)
        {
            String name = theme.getName();
            PSTheme themeCSS = restClient.loadCSS(name);
            assertNotNull(themeCSS);
            String css = themeCSS.getCSS();
            assertNotNull(css);
            
            if (name.equalsIgnoreCase(PERC_THEME))
            {
                assertTrue(css.toLowerCase().indexOf(PERC_CSS_CONTENT.toLowerCase()) != -1);
            }
        }
    }
    
    @Test
    public void testCreateDelete() throws Exception
    {
        PSThemeSummary newSum = restClient.create("mynewtheme", PERC_THEME);
        assertNotNull(newSum);
        
        restClient.delete(newSum.getName());
        
        try
        {
            restClient.loadCSS(newSum.getName());
            fail("Theme '" + newSum.getName() + "' should have been deleted");
        }
        catch (RestClientException e)
        {
            assertEquals(e.getStatus(), 500);
        }
    }
      
    @Test
    public void testRegionCSS_CRUD() throws Exception
    {
        // test with prepareForEditRegionCSS
        validateRegionCSS_CRUD();
        
        // test without prepareForEditRegionCSS
        restClient.clearCacheRegionCSS(PERC_THEME, TEMPLATE);
        validateRegionCSS_CRUD();
    }

    private void validateRegionCSS_CRUD()
    {
        restClient.deleteRegionCSS(PERC_THEME, TEMPLATE, "container", "header");
        validateEmptyRegionCSS("container", "header");
        
        PSRegionCSS css = new PSRegionCSS("container", "header");
        List<Property> props = new ArrayList<Property>();
        Property prop = new Property("border", "12px");
        props.add(prop);
        css.setProperties(props);
        
        restClient.saveRegionCSS(PERC_THEME, TEMPLATE, css);
        
        css = restClient.getRegionCSS(PERC_THEME, TEMPLATE, "container", "header");
        assertNotNull(css);
        
        restClient.deleteRegionCSS(PERC_THEME, TEMPLATE, "container", "header");
        validateEmptyRegionCSS("container", "header");
    }

    private void validateEmptyRegionCSS(String outer, String region)
    {
        PSRegionCSS regionCSS = restClient.getRegionCSS(PERC_THEME, TEMPLATE, outer, region);
        assertNull(regionCSS.getOuterRegionName());
        assertNull(regionCSS.getRegionName());
        assertTrue(regionCSS.getProperties().isEmpty());
    }
    
    @Test
    public void testMergeRegionCSS() throws Exception
    {
        PSRegionCssList regions = new PSRegionCssList();
        PSRegionCSS css = new PSRegionCSS("container", "header");
        regions.getRegions().add(css);
        
        String templateId = testSiteData.template1.getId();
        // disable this for now, as the server need to load a real template to merge from
        // the merge feature has been extensively tested by its specific component test.
        restClient.mergeRegionCSS(PERC_THEME, templateId, regions);
    }
    
    @Test
    public void testPrepareForEditRegionCSS() throws Exception
    {
        restClient.prepareForEditRegionCSS(PERC_THEME, TEMPLATE);
    }
    
    @Test
    public void testClearCacheRegionCSS() throws Exception
    {
        restClient.clearCacheRegionCSS(PERC_THEME, TEMPLATE);
    }
    
    /**
     * Constant for the percussion theme name.
     */
    private static final String PERC_THEME = "percussion";
    
    private static final String TEMPLATE = "home";
    
    /**
     * A block of text that is expected to appear in the percussion theme css
     * file. This should be chosen so that changes to the file won't break the
     * test. The test is case-insensitive.
     */    
    private static final String PERC_CSS_CONTENT = "container";
}
