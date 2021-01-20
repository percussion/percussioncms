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

package com.percussion.apibridge;

import com.percussion.assetmanagement.dao.IPSAssetDao;
import com.percussion.assetmanagement.service.IPSAssetService;
import com.percussion.assetmanagement.service.IPSWidgetAssetRelationshipService;
import com.percussion.itemmanagement.service.IPSItemWorkflowService;
import com.percussion.pagemanagement.service.IPSPageService;
import com.percussion.pagemanagement.service.IPSTemplateService;
import com.percussion.pagemanagement.service.IPSWidgetService;
import com.percussion.pagemanagement.service.PSSiteDataServletTestCaseFixture;
import com.percussion.rest.folders.Folder;
import com.percussion.rest.folders.SectionInfo;
import com.percussion.rest.pages.Page;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.share.dao.IPSFolderHelper;
import com.percussion.share.service.IPSIdMapper;
import com.percussion.share.spring.PSSpringWebApplicationContextUtils;
import com.percussion.test.PSServletTestCase;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.webservices.content.IPSContentDesignWs;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.security.IPSSecurityWs;
import com.percussion.webservices.system.IPSSystemWs;
import org.junit.experimental.categories.Category;

import java.net.URI;

@Category(IntegrationTest.class)
public class FolderAdaptorTest extends PSServletTestCase
{
   private static final String tempPrefix = "FolderAdaptorTest";
   private static final String FIXTURE_SITE_NAME = "PSSiteDataServletTestCaseFixtureSite";
   
   private static final String API_BASE_URL = "http://localhost:9992/Rhythmyx/rest";
   private URI baseUri;
   
   private PSSiteDataServletTestCaseFixture fixture;
   private String templateId;

   @Override
   public void setUp() throws Exception
   {

       baseUri = new URI(API_BASE_URL);
       PSSpringWebApplicationContextUtils.injectDependencies(this);
       fixture = new PSSiteDataServletTestCaseFixture(request, response);
     
       fixture.setUp("Admin", "demo", "Default");
       fixture.pageCleaner.add(fixture.site1.getFolderPath() + "/index.html");
       super.setUp();
  
   }
       
   @Override
   protected void tearDown() throws Exception
   {
       fixture.tearDown();
   }

   public void testGetSite() throws Exception
   {
    
       Folder folder = folderAdaptor.getFolder(baseUri, fixture.site1.getName(), "", "");
 
       assertNotNull(folder.getId());
   }

   private static String RENAME_FOLDER_TEST="renameFolder";
   
   /**
    * Test renaming folders.  This test should be self contained and clean up after itself.
    */
   public void testRenameFolder(){
	   Folder folder = new Folder();
	   Folder mFolder = new Folder();
	   Folder testRoot = new Folder();
	   
  		SectionInfo rootSec = new SectionInfo();
  		rootSec.setTemplateName(fixture.template1.getName());
  		rootSec.setDisplayTitle(RENAME_FOLDER_TEST);
  		testRoot.setSiteName(FIXTURE_SITE_NAME);
  		testRoot.setName(RENAME_FOLDER_TEST);
  		testRoot.setSectionInfo(rootSec);
  		testRoot.setPath("");
  		testRoot = this.folderAdaptor.updateFolder(baseUri, testRoot);
  		assertEquals("Expected name", testRoot.getName(),RENAME_FOLDER_TEST);
  		fixture.pageCleaner.add("/Sites/" + FIXTURE_SITE_NAME + "/" + RENAME_FOLDER_TEST + "/index.html" );
  		
	   mFolder.setName(RENAME_FOLDER_TEST);
	   mFolder.setPath("");
	   mFolder.setSiteName(FIXTURE_SITE_NAME);
	   mFolder = folderAdaptor.updateFolder(baseUri, mFolder);
	   

		   folder.setName("folder1");
		   folder.setPath(RENAME_FOLDER_TEST);
		   folder.setSiteName(FIXTURE_SITE_NAME);
		   folder = folderAdaptor.updateFolder(baseUri, folder);
	   
		   Folder returnedRenamedFolder = folderAdaptor.renameFolder(baseUri, folder.getSiteName(), folder.getPath(), folder.getName(), "folder2");
		   Folder renamedFolder = folderAdaptor.getFolder(baseUri, folder.getSiteName(), folder.getPath(), "folder2");
		 
		   assertEquals("Folder names should match", returnedRenamedFolder.getName(), renamedFolder.getName());
		   assertEquals("Folder paths should match", returnedRenamedFolder.getPath(), renamedFolder.getPath());
		   assertEquals("Folder sites should match", returnedRenamedFolder.getSiteName(), renamedFolder.getSiteName());
			   
   }
   
   private static String MOVE_FOLDER_TEST="moveFolder";
   /**
    * Test moving a folder.  This test should be self contained and clean up after itself. 
    */
   public void testMoveFolder(){
	   Folder folder = new Folder();
	   Folder folder2 = new Folder();
	   Folder testRoot = new Folder();
	   
  		SectionInfo rootSec = new SectionInfo();
  		rootSec.setTemplateName(fixture.template1.getName());
  		rootSec.setDisplayTitle(MOVE_FOLDER_TEST);
  		testRoot.setSiteName(FIXTURE_SITE_NAME);
  		testRoot.setName(MOVE_FOLDER_TEST);
  		testRoot.setSectionInfo(rootSec);
  		testRoot.setPath("");
  		testRoot = this.folderAdaptor.updateFolder(baseUri, testRoot);
  		assertEquals("Expected name", testRoot.getName(),MOVE_FOLDER_TEST);
  		fixture.pageCleaner.add("/Sites/" + FIXTURE_SITE_NAME + "/" + MOVE_FOLDER_TEST + "/index.html" );
  		
	 
		   folder.setName("folder1");
		   folder.setPath(MOVE_FOLDER_TEST);
		   folder.setSiteName(FIXTURE_SITE_NAME);
		   folder = folderAdaptor.updateFolder(baseUri, folder);
	   
		   folder2.setName("folder2");
		   folder2.setPath(MOVE_FOLDER_TEST);
		   folder2.setSiteName(FIXTURE_SITE_NAME);
		   folder2 = folderAdaptor.updateFolder(baseUri, folder2);
	   
		   
		   folderAdaptor.moveFolder(baseUri, "/Sites/" + folder.getSiteName() + "/" + folder.getPath() + "/" + folder.getName(),
				   "/Sites/" + folder.getSiteName() + "/" + folder2.getPath()+"/"+folder2.getName());
		   Folder renamedFolder = folderAdaptor.getFolder(baseUri, folder2.getSiteName(), folder2.getPath() + "/" + folder2.getName(), "folder1");
		 
		   assertEquals("Folder names should match", renamedFolder.getName(), "folder1");

		   try{
		   Folder oldFolder = folderAdaptor.getFolder(baseUri, folder.getSiteName(), folder.getPath(), folder.getName());
		   assertEquals("Old folder should be gone.",oldFolder, null);
		   }catch(Exception e){
			   //We Expect this to error or return null
		   }
		   
		   
	   
   }
   
   
   private static String MOVE_FOLDER_ITEM_TEST = "moveFolderItem";
   /***
    * Tests moving an item from one folder to the other. Test should be self contained and cleanup after itself. 
    */
   public void testMoveFolderItem(){
	
	   Folder folder = new Folder();
	   Folder folder2 = new Folder();
	   Folder testRoot = new Folder();
	   
	   		SectionInfo rootSec = new SectionInfo();
	   		rootSec.setTemplateName(fixture.template1.getName());
	   		rootSec.setDisplayTitle(MOVE_FOLDER_ITEM_TEST);
	   		testRoot.setSiteName(FIXTURE_SITE_NAME);
	   		testRoot.setName(MOVE_FOLDER_ITEM_TEST);
	   		testRoot.setSectionInfo(rootSec);
	   		testRoot.setPath("");
	   		testRoot = this.folderAdaptor.updateFolder(baseUri, testRoot);
	   		assertEquals("Expected name", testRoot.getName(),MOVE_FOLDER_ITEM_TEST);
	   		fixture.pageCleaner.add("/Sites/" + FIXTURE_SITE_NAME + "/" + MOVE_FOLDER_ITEM_TEST + "/index.html" );
		   
	   		folder.setName("folder1");
		   folder.setPath(MOVE_FOLDER_ITEM_TEST);
		   folder.setSiteName(FIXTURE_SITE_NAME);
		   SectionInfo sec = new SectionInfo();
		   sec.setDisplayTitle("folder1");
		   sec.setTemplateName(fixture.template1.getName());
		   folder.setSectionInfo(sec);
		   folder = folderAdaptor.updateFolder(baseUri, folder);
	   
		   folder2.setName("folder2");
		   folder2.setPath(MOVE_FOLDER_ITEM_TEST);
		   folder2.setSiteName(FIXTURE_SITE_NAME);
		   folder2 = folderAdaptor.updateFolder(baseUri, folder2);
		   
		   
		   folderAdaptor.moveFolderItem(baseUri, "/Sites/" + folder.getSiteName() + "/" + folder.getPath() + "/" + folder.getName() + "/index.html", 
				   "/Sites/" + folder2.getSiteName() + "/" + folder2.getPath() + "/" + folder2.getName());
		  
		   Page p = pageAdaptor.getPage(baseUri, folder2.getSiteName(), folder2.getPath() +"/"+folder2.getName(),"index.html");
		   fixture.pageCleaner.add("/Sites/" + FIXTURE_SITE_NAME + "/" + MOVE_FOLDER_ITEM_TEST + "/"+folder2.getName() + "/index.html" );
		   
		   assertEquals("Page name should match", p.getName(), "index.html");
		   assertEquals("Folder paths should match", p.getFolderPath(), folder2.getPath() +"/"+folder2.getName());
		   
		   
	   
	   
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
  
   public FolderAdaptor getFolderAdaptor()
   {
       return folderAdaptor;
   }

   public void setFolderAdaptor(FolderAdaptor folderAdaptor)
   {
       this.folderAdaptor = folderAdaptor;
   }

   public PageAdaptor getPageAdaptor() {
		return pageAdaptor;
	}

	public void setPageAdaptor(PageAdaptor pageAdaptor) {
		this.pageAdaptor = pageAdaptor;
	}

	public AssetAdaptor getAssetAdaptor() {
		return assetAdaptor;
	}

	public void setAssetAdaptor(AssetAdaptor assetAdaptor) {
		this.assetAdaptor = assetAdaptor;
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
   private FolderAdaptor folderAdaptor;    
   private PageAdaptor pageAdaptor;
  private AssetAdaptor assetAdaptor;
   
   
}
