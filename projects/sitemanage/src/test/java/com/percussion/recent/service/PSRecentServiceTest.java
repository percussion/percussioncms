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

package com.percussion.recent.service;

import com.percussion.assetmanagement.data.PSAsset;
import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.data.PSTemplateSummary;
import com.percussion.pagemanagement.data.PSWidgetContentType;
import com.percussion.pathmanagement.data.PSPathItem;
import com.percussion.recent.service.rest.IPSRecentService;
import com.percussion.share.data.PSItemProperties;
import com.percussion.share.spring.PSSpringWebApplicationContextUtils;
import com.percussion.test.PSServletTestCase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.percussion.utils.testing.IntegrationTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(IntegrationTest.class)
public class PSRecentServiceTest extends PSServletTestCase
{

    private static final int MAX_TEST_ADD = 22;
    private static final String PAGE_PREFIX = "RECENT_PAGE_";
    private static final String ASSET_PREFIX = "RECENT_ASSET_";
    private static final String TEMPLATE_PREFIX = "RECENT_TEMPLATE_";
    private static final String PAGE_FOLDER_PREFIX = "RECENT_PAGE_FOLDER_";
    private static final String ASSET_FOLDER_PREFIX = "RECENT_ASSET_FOLDER_";
    private static final String ASSET_FOLDER = "//Folders/$System$/Assets";
    
    
    private IPSRecentService recentService;
    private PSRecentServiceFixture fixture;
  
    private List<PSPage> pageList = new ArrayList<PSPage>();
    private List<PSAsset> assetList = new ArrayList<PSAsset>();
    private List<PSWidgetContentType> widgetTypes = new ArrayList<PSWidgetContentType>();
    private List<PSTemplateSummary> templateList = new ArrayList<PSTemplateSummary>();
    private List<String> assetsFolders = new ArrayList<String>();
    private List<String> siteFolders = new ArrayList<String>();
     
    @Override
    protected void setUp() throws Exception
    { 
       
        PSSpringWebApplicationContextUtils.injectDependencies(this);
   
        fixture = new PSRecentServiceFixture(request, response);
        
        fixture.setUp("Admin", "demo", "Default");
        
        for (int i=0; i<MAX_TEST_ADD; i++)
        {
            pageList.add(fixture.createPage(PAGE_PREFIX+i));
        }
        
        for (int i=0; i<MAX_TEST_ADD; i++)
        {
            siteFolders.add(fixture.createSiteFolder(PAGE_FOLDER_PREFIX+i));
        }
        for (int i=0; i<MAX_TEST_ADD; i++)
        {
            assetsFolders.add(fixture.createAssetFolder(ASSET_FOLDER_PREFIX+i));
        }
        
        for (int i=0; i<MAX_TEST_ADD; i++)
        {
            PSAsset assetCreated = new PSAsset();
            String assetTitle = ASSET_PREFIX+i;
            assetCreated.getFields().put( "sys_title" , assetTitle);
            assetCreated.setType( "percRawHtmlAsset" );
            assetCreated.getFields().put( "html" , "TestHTML" );
            assetCreated.setFolderPaths(Collections.singletonList( ASSET_FOLDER+"/"+fixture.site1.getName()));
            String localAssetId = fixture.saveAsset(assetCreated).getId();
            assetCreated.setId(localAssetId);
            assetList.add(assetCreated);
        }
        
        for (int i=0; i<MAX_TEST_ADD; i++)
        {
            templateList.add(fixture.createTemplate(TEMPLATE_PREFIX+i));
        }
        widgetTypes = fixture.getWidgetTypes();
        //FB:IJU_SETUP_NO_SUPER NC 1-16-16
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception
    {
        fixture.tearDown();
    }
    
    @Test
    public void testUpdateRecentPages()
    {
     
        for (PSPage page : pageList)
        {
            recentService.addRecentItem(page.getId());
        }
        
        List<PSItemProperties> recentList = recentService.findRecentItem(false);
        
        assertEquals(20,recentList.size());

    }

    @Test
    public void testUpdateRecentAssets()
    {
     
        for (PSAsset asset : assetList)
        {
            recentService.addRecentItem(asset.getId());
        }
        
        List<PSItemProperties> recentList = recentService.findRecentItem(false);
        
        assertEquals(20,recentList.size());
        
    }
    
    @Test
    public void testUpdateRecentTemplates()
    {
     
        for (PSTemplateSummary template : templateList)
        {
            recentService.addRecentTemplate(fixture.site1.getName(), template.getId());
        }
        
        List<PSTemplateSummary> recentList = recentService.findRecentTemplate(fixture.site1.getName());
        
        assertEquals(6,recentList.size());
        
    }
    
    @Test
    public void testUpdateRecentAssetTypes()
    {
     
        for (PSWidgetContentType type : widgetTypes)
        {
            recentService.addRecentAssetType(type.getWidgetId());
        }
        
        List<PSWidgetContentType> recentList = recentService.findRecentAssetType();
        
        assertEquals(6,recentList.size());
        
    }

    @Test
    public void testUpdateRecentSiteFolder()
    {
     
        for (String folderName : siteFolders)
        {
            recentService.addRecentSiteFolder(folderName);
        }
        
        List<PSPathItem> recentList = recentService.findRecentSiteFolder(fixture.site1.getName());
        
        assertEquals(10,recentList.size());
        
    }
    
    @Test
    public void testUpdateRecentAssetFolder()
    {
     
        for (String folderName : assetsFolders)
        {
            recentService.addRecentAssetFolder(folderName);
        }
        
        List<PSPathItem> recentList = recentService.findRecentAssetFolder();
        
        assertEquals(10,recentList.size());
        
    }
    
    public IPSRecentService getRecentService()
    {
        return recentService;
    }

    public void setRecentService(IPSRecentService recentService)
    {
        this.recentService = recentService;
    }

    
}
