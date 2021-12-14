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

package com.percussion.pagemanagement.service;

import com.percussion.assetmanagement.data.PSAsset;
import com.percussion.assetmanagement.service.IPSAssetService;
import com.percussion.assetmanagement.service.IPSWidgetAssetRelationshipService;
import com.percussion.itemmanagement.service.impl.PSItemWorkflowService;
import com.percussion.pagemanagement.dao.IPSPageDao;
import com.percussion.pagemanagement.dao.IPSPageDaoHelper;
import com.percussion.pagemanagement.dao.impl.PSTemplateDao;
import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.data.PSTemplate;
import com.percussion.pagemanagement.data.PSTemplateSummary;
import com.percussion.pagemanagement.data.PSWidgetItem;
import com.percussion.pagemanagement.service.impl.PSPageCatalogService;
import com.percussion.pagemanagement.service.impl.PSPageManagementUtils;
import com.percussion.pagemanagement.service.impl.PSTemplateService;
import com.percussion.share.service.IPSIdMapper;
import com.percussion.share.service.IPSNameGenerator;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.share.spring.PSSpringWebApplicationContextUtils;
import com.percussion.sitemanage.dao.impl.PSSiteDao;
import com.percussion.sitemanage.data.PSSiteSummary;
import com.percussion.sitemanage.service.PSPageToTemplatePair;
import com.percussion.sitemanage.service.impl.PSSiteTemplateService;
import com.percussion.test.PSServletTestCase;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.utils.types.PSPair;
import com.percussion.webservices.system.IPSSystemWs;
import org.junit.experimental.categories.Category;

import static java.util.Arrays.asList;

@Category(IntegrationTest.class)
public class PSPageToTemplateTest extends PSServletTestCase
{

    private PSSiteDataServletTestCaseFixture fixture;

    private IPSPageService pageService;

    private IPSIdMapper idMapper;

    private IPSSystemWs systemWs;

    private IPSPageDao pageDao;

    private IPSPageDaoHelper pageDaoHelper;

    private IPSWidgetAssetRelationshipService widgetService;

    private IPSAssetService assetService;

    private PSItemWorkflowService itemWorkflowService;

    private IPSNameGenerator nameGenerator;

    private PSSiteTemplateService siteTemplateService;

    private PSSiteDao siteDao;
    
    private PSTemplateService templateService;
    
    private PSTemplateDao templateDao;
    
    private PSPageCatalogService pageCatalogService;
    
    

    public IPSNameGenerator getNameGenerator()
    {
        return nameGenerator;
    }

    public void setNameGenerator(IPSNameGenerator nameGenerator)
    {
        this.nameGenerator = nameGenerator;
    }

    @Override
    public void setUp() throws Exception
    {
        
        PSSpringWebApplicationContextUtils.injectDependencies(this);
        fixture = new PSSiteDataServletTestCaseFixture(request, response);
     
        fixture.setUp();
        
        fixture.pageCleaner.add(fixture.site1.getFolderPath() + "/Page1");
        //FB:IJU_SETUP_NO_SUPER NC 1-16-16
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception
    {
        fixture.tearDown();
    }

    public PSSiteDao getSiteDao()
    {
        return siteDao;
    }

    public void setSiteDao(PSSiteDao siteDao)
    {
        this.siteDao = siteDao;
    }

    public PSPageCatalogService getPageCatalogservice()
    {
        return pageCatalogService;
    }

    public void setPageCatalogservice(PSPageCatalogService pageCatalogService)
    {
        this.pageCatalogService = pageCatalogService;
    }

    public PSTemplateDao getTemplateDao()
    {
        return templateDao;
    }

    public void setTemplateDao(PSTemplateDao templateDao)
    {
        this.templateDao = templateDao;
    }

    public PSTemplateService getTemplateService()
    {
        return templateService;
    }

    public void setTemplateService(PSTemplateService templateService)
    {
        this.templateService = templateService;
    }

    public PSSiteTemplateService getPSSiteTemplateService()
    {
        return siteTemplateService;
    }

    public void setSiteTemplateService(PSSiteTemplateService siteTemplateService)
    {
        this.siteTemplateService = siteTemplateService;
    }

    public PSItemWorkflowService getItemWorkflowService()
    {
        return itemWorkflowService;
    }

    public void setItemWorkflowService(PSItemWorkflowService itemWorkflowService)
    {
        this.itemWorkflowService = itemWorkflowService;
    }

    public IPSAssetService getAssetService()
    {
        return assetService;
    }

    public void setAssetService(IPSAssetService assetService)
    {
        this.assetService = assetService;
    }

    public IPSPageService getPageService()
    {
        return pageService;
    }

    public void setPageService(IPSPageService pageService)
    {
        this.pageService = pageService;
    }

    public IPSIdMapper getIdMapper()
    {
        return idMapper;
    }

    public void setIdMapper(IPSIdMapper idMapper)
    {
        this.idMapper = idMapper;
    }

    public IPSSystemWs getSystemWs()
    {
        return systemWs;
    }

    public void setSystemWs(IPSSystemWs systemWs)
    {
        this.systemWs = systemWs;
    }

    public IPSPageDao getPageDao()
    {
        return pageDao;
    }

    public IPSPageDaoHelper getPageDaoHelper()
    {
        return pageDaoHelper;
    }

    public void setPageDao(IPSPageDao pageDao)
    {
        this.pageDao = pageDao;
    }

    public void setPageDaoHelper(IPSPageDaoHelper pageDaoHelper)
    {
        this.pageDaoHelper = pageDaoHelper;
    }

    public IPSWidgetAssetRelationshipService getWidgetService()
    {
        return widgetService;
    }

    public void setWidgetService(IPSWidgetAssetRelationshipService widgetService)
    {
        this.widgetService = widgetService;
    }

    public void testTemplateToPage() throws Exception
    {


            String name = "Sinister-Plans";
            String title = "The Plans of Fu Manchu";
            String folderPath = fixture.site1.getFolderPath();
            String linkTitle = "The link of Fu Manchu";
            
            PSSiteSummary siteSummary = siteDao.findSummary(fixture.site1.getId());
            assertNotNull(siteSummary);
            String templateId = pageCatalogService.getCatalogTemplateId(siteSummary);
            String pageId = createPage(name, title, templateId, folderPath, linkTitle,
                    fixture.site1.getBaseUrl() + "/Plans_Of_Fu_Manchu.html", "true", "This is the plan of Fu Manchu");
            
            PSPage page = pageDao.find(pageId);  
            
            PSPageToTemplatePair pair = new PSPageToTemplatePair();
            pair.setPageId(page.getId());
            pair.setSiteId(fixture.site1.getId());
            
            PSTemplateSummary summary = siteTemplateService.createTemplateFromPage(pair);
            PSTemplate template = templateService.load(summary.getId());
            
            assertTrue(template.getAdditionalHeadContent().equals(page.getAdditionalHeadContent()));
            assertTrue(template.getAfterBodyStartContent().equals(page.getAfterBodyStartContent()));
            assertTrue(template.getBeforeBodyCloseContent().equals(page.getBeforeBodyCloseContent()));
            assertTrue(template.getDescription().equals(page.getDescription()));
    }

    /**
     * Creates and saves a page using the testcase fixture
     * {@link PSSiteDataServletTestCaseFixture}.
     * 
     * @param name assumed not <code>null</code>.
     * @param title assumed not <code>null</code>.
     * @param templateId may be <code>null</code>.
     * @param folderPath assumed not <code>null</code>.
     * @param linkTitle assumed not <code>null</code>.
     * @param url assumed not <code>null</code>.
     * @param noindex assumed not <code>null</code>.
     * @param description assumed not <code>null</code>.
     * 
     * @return the id of the created page, never blank.
     */
    private String createPage(String name, String title, String templateId, String folderPath, String linkTitle,
            String url, String noindex, String description) throws PSDataServiceException {
        PSPage page = new PSPage();
        page.setFolderPath(folderPath);
        page.setName(name);
        page.setTitle(title);
        page.setTemplateId(templateId);
        if (templateId != null)
        {
            page.setTemplateId(templateId);
        }

        page.setFolderPath(folderPath);
        page.setLinkTitle(linkTitle);
        page.setNoindex(noindex);
        
        
        String bodyStartContent = "The Body of Fu Manchu";
        page.setAfterBodyStartContent(bodyStartContent);
        
        String closeContent = "The End of Fu Manchu";
        page.setBeforeBodyCloseContent(closeContent);
                
        String header = "The Mustached head of Fu Manchu";
        page.setAdditionalHeadContent(header);
        
        description = "Fu Manchu is a sinister criminal mastermind";
        page.setDescription(description);
        
        page = fixture.createPage(page);
        
        
       
        String extractedBodyHtml = "This is the extracted body of Fu Manchu";
        PSAsset localAsset = createHTMLLocalContent(extractedBodyHtml);
        PSWidgetItem rawHtmlWidget = PSPageManagementUtils.createRawHtmlWidgetItem("1");
        PSPair<PSWidgetItem, PSAsset> widgetAssetPair = new PSPair<PSWidgetItem, PSAsset>(rawHtmlWidget, localAsset);
        widgetService.createRelationship(localAsset.getId(), page.getId(), rawHtmlWidget.getId(), 
                rawHtmlWidget.getName(), false);
        //Set the widget
        page.getRegionBranches().setRegionWidgets(PSSiteTemplateService.REGION_CONTENT, asList(rawHtmlWidget));
        page = pageService.save(page);
        return page.getId();
        // Add asset to widget on the page
        //itemWorkflowService.checkOut(page.getId());
        
        //itemWorkflowService.checkIn(page.getId());
        //return page.getId();

    }

    private PSAsset createHTMLLocalContent(String htmlContent) throws PSDataServiceException {
        PSAsset asset = new PSAsset();
        String assetName = nameGenerator.generateLocalContentName();
        asset.setName(assetName);
        asset.setType("percRawHtmlAsset");
        asset.getFields().put(IPSHtmlParameters.SYS_TITLE, assetName);
        asset.getFields().put("html", htmlContent);
        asset = assetService.save(asset);
        return asset;
    }

    
}
