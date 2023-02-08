/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
import com.percussion.rest.errors.BackendException;
import com.percussion.rest.pages.Page;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.share.dao.IPSFolderHelper;
import com.percussion.share.service.IPSIdMapper;
import com.percussion.share.service.exception.PSDataServiceException;
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
public class PageAdaptorTest extends PSServletTestCase
{
   private static final String tempPrefix = "TemplateTest";
   private static final String FIXTURE_SITE_NAME = "PSSiteDataServletTestCaseFixtureSite";
   
   private static final String API_BASE_URL = "http://localhost:9992/Rhythmyx/rest";
   private URI baseUri;
   
   private PSSiteDataServletTestCaseFixture fixture;


   @Override
   public void setUp() throws Exception
   {

       baseUri = new URI(API_BASE_URL);
       PSSpringWebApplicationContextUtils.injectDependencies(this);
       fixture = new PSSiteDataServletTestCaseFixture(request, response);  
       
       fixture.setUp("Admin", "demo", "Default");
       
       fixture.pageCleaner.add("/" + FIXTURE_SITE_NAME + "/index.html");
       super.setUp();
   
   }
       
   @Override
   protected void tearDown() throws Exception
   {
         fixture.tearDown();
         fixture.templateCleanUp(tempPrefix);

   }

   public void testGetPage() throws Exception
   {
       Page page = pageAdaptor.getPage(baseUri, FIXTURE_SITE_NAME, "", "index.html");

       assertNotNull(page.getId());
   }

   public void testRenamePage() throws PSDataServiceException, BackendException {
	   Page p = pageAdaptor.renamePage(baseUri,FIXTURE_SITE_NAME , "", "index.html", "renamed-index.html");
	   
	   assertEquals("Page should have been renamed.","renamed-index.html", p.getName());
	   fixture.pageCleaner.add("/"+ FIXTURE_SITE_NAME + "/renamed-index.html");
   
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
  
   public PageAdaptor getPageAdaptor()
   {
       return pageAdaptor;
   }

   public void setPageAdaptor(PageAdaptor pageAdaptor)
   {
       this.pageAdaptor = pageAdaptor;
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
   private PageAdaptor pageAdaptor;    

}
