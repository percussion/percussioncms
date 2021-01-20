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

package com.percussion.pagemanagement.service;

import static java.util.Arrays.asList;

import com.percussion.assetmanagement.data.PSAsset;
import com.percussion.assetmanagement.data.PSAssetWidgetRelationship;
import com.percussion.assetmanagement.data.PSAssetWidgetRelationship.PSAssetResourceType;
import com.percussion.assetmanagement.service.IPSAssetService;
import com.percussion.assetmanagement.service.impl.PSWidgetAssetRelationshipService;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.itemmanagement.service.IPSItemWorkflowService;
import com.percussion.pagemanagement.dao.IPSPageDaoHelper;
import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.data.PSRegion;
import com.percussion.pagemanagement.data.PSRegionTree;
import com.percussion.pagemanagement.data.PSTemplate;
import com.percussion.pagemanagement.data.PSTemplateSummary;
import com.percussion.pagemanagement.data.PSWidgetItem;
import com.percussion.pagemanagement.data.PSTemplate.PSTemplateTypeEnum;
import com.percussion.share.service.IPSDataService.DataServiceSaveException;
import com.percussion.share.service.IPSIdMapper;
import com.percussion.share.service.exception.PSBeanValidationException;
import com.percussion.share.spring.PSSpringWebApplicationContextUtils;
import com.percussion.test.PSServletTestCase;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.webservices.content.IPSContentDesignWs;
import com.percussion.webservices.system.IPSSystemWs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.junit.experimental.categories.Category;

@Category(IntegrationTest.class)
public class PSTemplateServiceTest extends PSServletTestCase
{

    private IPSTemplateService templateService;

    private PSSiteDataServletTestCaseFixture fixture;

    private IPSAssetService assetService;

    private IPSSystemWs systemWs;

    private IPSIdMapper idMapper;
    
    private IPSPageDaoHelper pageDaoHelper;

    private IPSContentDesignWs contentDesignWs;
    
    private IPSItemWorkflowService itemWorkflowService;
    

    public IPSItemWorkflowService getItemWorkflowService()
    {
        return itemWorkflowService;
    }

    public void setItemWorkflowService(IPSItemWorkflowService itemWorkflowService)
    {
        this.itemWorkflowService = itemWorkflowService;
    }

    public IPSContentDesignWs getContentDesignWs()
    {
        return contentDesignWs;
    }

    public void setContentDesignWs(IPSContentDesignWs contentDesignWs)
    {
        this.contentDesignWs = contentDesignWs;
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

    public IPSSystemWs getSystemWs()
    {
        return systemWs;
    }

    public void setSystemWs(IPSSystemWs systemWs)
    {
        this.systemWs = systemWs;
    }
    
    /**
     * @param pageDaoHelper the pageDaoHelper to set
     */
    public void setPageDaoHelper(IPSPageDaoHelper pageDaoHelper)
    {
        this.pageDaoHelper = pageDaoHelper;
    }

    /**
     * @return the pageDaoHelper
     */
    public IPSPageDaoHelper getPageDaoHelper()
    {
        if(pageDaoHelper == null)
        {
            pageDaoHelper = (IPSPageDaoHelper) getBean("pageDaoHelper");
        }        
        return pageDaoHelper;
    }

    @Override
    protected void setUp() throws Exception
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
        fixture.tearDown();
    }

    public void testFindTemplates() throws Exception
    {
        IPSTemplateService srv = getTemplateService();

        int preAllBaseNum = srv.findBaseTemplates("base").size();

        List<PSTemplateSummary> summaris = srv.findBaseTemplates("base");
        assertTrue("This is no Base Template in current community", summaris.size() == preAllBaseNum);

        fixture.createBasePageTemplate(TEMPLATE_NAME_PREFIX + "_1");

        summaris = srv.findBaseTemplates("base");
        assertTrue(summaris.size() == 1 + preAllBaseNum);

        fixture.createBasePageTemplate(TEMPLATE_NAME_PREFIX + "_2");

        summaris = srv.findBaseTemplates("base");
        assertTrue(summaris.size() == 2 + preAllBaseNum);
    }
    
    /**
     * Tests saving a template with type unassigned using new create method and checking the result 
     * @throws Exception
     */
    public void testCreateTypedTemplate () throws Exception
    {
        IPSTemplateService srv = getTemplateService();
        String templateName = "templateUnassigned";
        
        PSTemplateSummary templateSummary = srv.createTemplate(templateName,
                fixture.baseTemplateId,
                fixture.site1.getId(),
                PSTemplateTypeEnum.UNASSIGNED);
        assertNotNull(templateSummary);
        
        PSTemplate template = templateService.load(templateSummary.getId());
        assertEquals(template.getName(), templateName);
        assertEquals(template.getType(), PSTemplateTypeEnum.UNASSIGNED.getLabel());
    }
    
    /**
     * Tests loading a recently saved template with type UNASSIGNED 
     * @throws Exception
     */
    public void testLoadUnassignedTemplate() throws Exception
    {
        IPSTemplateService srv = getTemplateService();
        
        String templateName = "Template1";
        PSTemplateSummary templateSummary = fixture.createTemplate(templateName);
        PSTemplate item = templateService.load(templateSummary.getId());
        item.setType(PSTemplateTypeEnum.UNASSIGNED.getLabel());
        srv.save(item);
        
        item = templateService.load(templateSummary.getId());
        assertEquals(item.getName(), templateName);
        assertEquals(item.getType(), PSTemplateTypeEnum.UNASSIGNED.getLabel());
    }

    /**
     * Tests retrieving the user templates by type (any untyped templates will also be returned) 
     * @throws Exception
     */
    public void testFindUserTemplatesByType() throws Exception
    {
        IPSTemplateService srv = getTemplateService();
        
        int preAllNum = srv.findAll().size();
        int preUserNum = srv.findAllUserTemplates().size();
        
        String templateName = "Template1";
        String template2Name = "Template2";
        String template3Name = "Template3";
        String template4Name = "Template4";
        String template5Name = "Template5";
        
        PSTemplateSummary templateSummary = fixture.createTemplate(templateName);
        PSTemplate item = templateService.load(templateSummary.getId());
        item.setType(PSTemplateTypeEnum.NORMAL.getLabel());
        srv.save(item);

        PSTemplateSummary templateSummary2 = fixture.createTemplate(template2Name);
        PSTemplate item2 = srv.load(templateSummary2.getId());
        item2.setType(PSTemplateTypeEnum.NORMAL.getLabel());
        srv.save(item2);
        
        PSTemplateSummary templateSummary3 = fixture.createTemplate(template3Name);
        PSTemplate item3 = templateService.load(templateSummary3.getId());
        item3.setType(PSTemplateTypeEnum.UNASSIGNED.getLabel());
        srv.save(item3);
        
        PSTemplateSummary templateSummary4 = fixture.createTemplate(template4Name);
        PSTemplate item4 = templateService.load(templateSummary4.getId());
        item4.setType(PSTemplateTypeEnum.NORMAL.getLabel());
        srv.save(item4);
        
        PSTemplateSummary templateSummary5 = fixture.createTemplate(template5Name);
        
        List<PSTemplateSummary> userTemplates = srv.findAllUserTemplates();
        List<PSTemplateSummary> allTemplates = srv.findAll();
        
        assertEquals("4 new user templates (1 untyped, 3 types normal): ", preUserNum + 4, userTemplates.size());
        assertEquals("5 new total templates (1 untyped, 3 types normal, 1 unassigned): ", preAllNum + 4, allTemplates.size());
    }
    
    /**
     * This test creates a list of fake ids to test the processing of it on
     * oracle databases, cause there is an implicit limit of 1000 items on an IN
     * clause.
     */
    public void testFindPageIdsByTemplateInRecentRevision_usingPagination() 
    {
        List<Integer> fakeIds = new ArrayList<Integer>();
        for(int i = 0; i < 1500; i++)
        {
            fakeIds.add(i);
        }
        try 
        {
            Map<String, String> pageToTemplates = getPageDaoHelper().findTemplateUsedByCurrentRevisionOfPages(fakeIds);
        } 
        catch (Exception e)
        {
            fail("The query failed for 1500 ids.");
        }
    }

    public void testTemplateItem() throws Exception
    {
        

        PSTemplateSummary sum1 = null;
        PSTemplateSummary sum2 = null;

        int preAllNum = templateService.findAll().size();
        int preSystemNum = templateService.findBaseTemplates("base").size();
        int preUserNum = templateService.findAllUserTemplates().size();

        sum1 = fixture.createTemplateWithSite("testTemplateItem_1", fixture.site1.getId());
        PSTemplate item = templateService.load(sum1.getId());
        assertTrue("Find created item", item != null);
        assertTrue(StringUtils.isNotBlank(item.getCssRegion()));

        // create an asset
        PSAsset asset = new PSAsset();
        asset.getFields().put("sys_title", "SharedAsset");
        asset.setFolderPaths(asList("//Folders/Assets"));
        asset.setType("percRawHtmlAsset");
        asset.getFields().put("html", "TestHTML");
        asset = assetService.save(asset);
        fixture.assetCleaner.add(asset.getId());
        assertNotNull(asset);
        String assetId = asset.getId();
        assertNotNull(assetId);

        // add the asset to the template as a shared resource
        PSAssetWidgetRelationship awRel = new PSAssetWidgetRelationship(sum1.getId(), 5, "widget5", assetId, 1);
        awRel.setResourceType(PSAssetResourceType.shared);
        assetService.createAssetWidgetRelationship(awRel);

        // create user template from another user template
        sum2 = fixture.createTemplateFromTemplate("testTemplateItem_2", sum1.getId());

        // the item should be created in the template folder
        assertTrue("should have one more template", (preAllNum + 2) == templateService.findAll().size());
        assertTrue("should have one more template", (preUserNum + 2) == templateService.findAllUserTemplates().size());
        assertTrue("no change on number of system templates", preSystemNum == templateService.findBaseTemplates("base")
                .size());
        
        // make sure the asset was copied
        PSRelationshipFilter filter = new PSRelationshipFilter();
        filter.limitToOwnerRevision(true);
        filter.setName(PSWidgetAssetRelationshipService.SHARED_ASSET_WIDGET_REL_FILTER);
        filter.setOwner(idMapper.getLocator(sum2.getId()));
        assertEquals(1, systemWs.loadRelationships(filter).size());

        // update the template
        PSTemplate temp2 = templateService.load(sum2.getId());
        templateService.save(temp2);

        // make sure the asset relationship was removed
        assertTrue(systemWs.loadRelationships(filter).isEmpty());

        PSTemplateSummary itemSum = templateService.find(sum1.getId());
        assertNotNull(itemSum);

        templateService.delete(sum1.getId());
        PSTemplateSummary sum = templateService.find(sum1.getId());
        assertTrue("Should not find deleted item", sum == null);
        fixture.templateCleaner.remove(sum1.getName());

        templateService.delete(sum2.getId());
        sum = templateService.find(sum2.getId());
        assertTrue("Should not find deleted item_2", sum == null);
        fixture.templateCleaner.remove(sum2.getName());

        // total number of templates should be the name as BEFORE
        assertTrue("should have one more template", preAllNum == templateService.findAll().size());
        assertTrue("should have one more template", preUserNum == templateService.findAllUserTemplates().size());
    }

    public void testExportTemplate() throws Exception
    {
        PSTemplate templateToExport = null;
        PSTemplateSummary sum1 = null;

        //Create a template for testing purpose
        sum1 = fixture.createTemplateWithSite("testTemplateItem_1", fixture.site1.getId());
        PSTemplate item = templateService.load(sum1.getId());
        assertTrue("Find created item", item != null);
        assertTrue(StringUtils.isNotBlank(item.getName()));
        assertTrue(StringUtils.isNotBlank(item.getCssRegion()));
        
        //validate the given template to export 
        templateToExport = templateService.exportTemplate(item.getId(), item.getName());
        assertTrue("Template to export", templateToExport != null);
        assertTrue(StringUtils.isNotBlank(templateToExport.getName()));
        assertTrue(StringUtils.isNotBlank(templateToExport.getCssRegion()));
        
        //validate the ID not exists in the template 
        assertTrue("Should not find template ID", templateToExport.getId() == null);
        //remove created items
        templateService.delete(sum1.getId());
        PSTemplateSummary sum = templateService.find(sum1.getId());
        assertTrue("Should not find deleted item", sum == null);
        fixture.templateCleaner.remove(sum1.getName());     
    }

    public void fixme_testImportTemplate() throws Exception
    {
        PSTemplate importedTemplate = null;
        PSTemplateSummary sum1 = null;
        //Create a template for testing purpose
        sum1 = fixture.createTemplateWithSite("testTemplateItem_2", fixture.site1.getId());
        PSTemplate item = templateService.load(sum1.getId());
        assertTrue("Find created item", item != null);
        assertTrue(StringUtils.isNotBlank(item.getId())); 
        assertTrue(StringUtils.isNotBlank(item.getName()));
        assertTrue(StringUtils.isNotBlank(item.getCssRegion()));
        
        //validate the given template to export 
        importedTemplate = templateService.importTemplate(item, fixture.site1.getSiteId().toString());
        assertTrue(StringUtils.isNotBlank(importedTemplate.getId()));
        assertTrue(StringUtils.isNotBlank(importedTemplate.getName()));
                      
        //remove created items
        templateService.delete(importedTemplate.getId());
        PSTemplateSummary sum = templateService.find(sum1.getId());
        assertTrue("Should not find deleted item", sum == null);
        fixture.templateCleaner.remove(sum1.getName()); 
    }

    public void testGetWidgets() throws Exception
    {
        PSTemplateSummary sum1 = fixture.createTemplateWithSite("testTemplateItem_1", fixture.site1.getId());
        PSTemplate item = templateService.load(sum1.getId());
        
        assertEquals(0, item.getWidgets().size());
        
        PSRegion region = new PSRegion();
        region.setRegionId("region");
        
        List<PSWidgetItem> widgets = new ArrayList<PSWidgetItem>();       
        PSWidgetItem widget1 = new PSWidgetItem();
        widget1.setDefinitionId("widget1");
        widgets.add(widget1);
        
        PSWidgetItem widget2 = new PSWidgetItem();
        widget2.setDefinitionId("widget2");
        widgets.add(widget2);
        
        PSRegionTree regTree = new PSRegionTree();
        regTree.setRegionWidgets(region.getRegionId(), widgets);
        
        item.setRegionTree(regTree);
        
        assertEquals(2, item.getWidgets().size());
    }

    public void testHasWidget() throws Exception
    {
        PSTemplateSummary sum1 = fixture.createTemplateWithSite("testTemplateItem_1", fixture.site1.getId());
        PSTemplate item = templateService.load(sum1.getId());
        
        assertFalse(item.hasWidget("widget1"));
        assertFalse(item.hasWidget("widget2"));
        
        PSRegion region = new PSRegion();
        region.setRegionId("region");
        
        List<PSWidgetItem> widgets = new ArrayList<PSWidgetItem>();       
        PSWidgetItem widget1 = new PSWidgetItem();
        widget1.setDefinitionId("widget1");
        widgets.add(widget1);
        
        PSWidgetItem widget2 = new PSWidgetItem();
        widget2.setDefinitionId("widget2");
        widgets.add(widget2);
        
        PSRegionTree regTree = new PSRegionTree();
        regTree.setRegionWidgets(region.getRegionId(), widgets);
        
        item.setRegionTree(regTree);
        
        assertTrue(item.hasWidget("widget1"));
        assertTrue(item.hasWidget("widget2"));
    }
    
    
    public void testUpdateTemplateVersion() throws Exception
    {
        // create a template, assert version is 0
        PSTemplateSummary sum1 = fixture.createTemplateWithSite("testTemplateVersion", fixture.site1.getId());
        assertEquals("0", sum1.getContentMigrationVersion());
        PSTemplate item = templateService.load(sum1.getId());
        assertEquals("0", item.getContentMigrationVersion());
        
        // create a page w/it
        String name = "page1";
        PSPage page1 = createPage(sum1, name);
        assertNotNull(page1);
        fixture.pageCleaner.add(page1.getId());
        
        // save the template w/out the page id
        item = templateService.save(item);
        
        // assert template version still 0
        assertEquals("0", item.getContentMigrationVersion());
        
        // todo: assert the page version is 0
        
        // todo: create another page w/the template
        
        // todo: assert 2nd page version is 0
        
        // checkin the page and try to save template w/page id, should fail
        itemWorkflowService.checkIn(page1.getId());
        boolean didThrow = false;
        
        try
        {
            templateService.save(item, null, page1.getId());
        }
        catch (DataServiceSaveException e)
        {
            didThrow = true;
        }
        
        assertTrue(didThrow);
        
        // checkout the page and save the template w/page id, should succeed
        itemWorkflowService.checkOut(page1.getId());
        item = templateService.save(item, null, page1.getId());

        // assert the template version is incremented
        assertEquals("1", item.getContentMigrationVersion());
        
        // do it again
        item = templateService.save(item);
        assertEquals("1", item.getContentMigrationVersion());
        item = templateService.save(item, null, page1.getId());
        assertEquals("2", item.getContentMigrationVersion());
        
        // tood: assert the 1 page version is incremented, 2nd is not
        
        // delete the page and try to save the template with the page id, should fail nicely
        fixture.getPageService().delete(page1.getId());
        try
        {
            templateService.save(item, null, page1.getId());
        }
        catch (DataServiceSaveException e)
        {
            didThrow = true;
        }
    }

    private PSPage createPage(PSTemplateSummary sum1, String name)
    {
        PSPage page = new PSPage();
        page.setFolderPath(fixture.site1.getFolderPath());
        page.setName(name);
        page.setTitle(name);
        page.setTemplateId(sum1.getId());
        page.setLinkTitle(name);
        page.setNoindex("true");
        page.setDescription(name);
        PSPage page1 = fixture.createPage(page);
        return page1;
    }

    public static final String TEMPLATE_NAME_PREFIX = "perc.base.testPageTemplate";
}
