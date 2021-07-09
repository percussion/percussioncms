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

import static com.percussion.share.test.PSRestTestCase.baseUrl;
import static com.percussion.share.test.PSRestTestCase.setupClient;
import static java.util.Arrays.asList;
import static org.apache.commons.lang.Validate.notEmpty;
import static junit.framework.Assert.assertEquals;

import com.percussion.assetmanagement.data.PSAsset;
import com.percussion.assetmanagement.web.service.PSAssetServiceRestClient;
import com.percussion.itemmanagement.web.service.PSItemWorkflowServiceRestClient;
import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.data.PSTemplateSummary;
import com.percussion.pagemanagement.data.PSWidgetItem;
import com.percussion.pathmanagement.web.service.PSPathServiceRestClient;
import com.percussion.share.service.PSAsyncJobStatusRestClient;
import com.percussion.share.test.PSTestDataCleaner;
import com.percussion.sitemanage.data.PSCreateSiteSection;
import com.percussion.sitemanage.data.PSSite;
import com.percussion.sitemanage.data.PSSiteSection;
import com.percussion.sitemanage.data.PSUpdateSectionLink;
import com.percussion.sitemanage.service.PSSiteTemplates;
import com.percussion.sitemanage.service.AssignTemplate;
import com.percussion.sitemanage.service.PSSiteTemplates.CreateTemplate;
import com.percussion.sitemanage.web.service.PSSiteRestClient;
import com.percussion.sitemanage.web.service.PSSiteSectionRestClient;
import com.percussion.sitemanage.web.service.PSSiteTemplateRestClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 
 * Sets up two test sites and two test template items.
 * 
 * @author adamgent
 *
 */
public class PSTestSiteData
{

    private PSSiteTemplateRestClient siteTemplateRestClient;
    private PSTemplateServiceClient templateServiceClient;
    private PSPageRestClient pageRestClient;
    private PSSiteRestClient siteRestClient;
    private PSAssetServiceRestClient assetRestClient;
    private PSSiteSectionRestClient sectionClient;
    private PSPathServiceRestClient pathClient;
    private PSItemWorkflowServiceRestClient workflowClient;
    private List<PSTemplateSummary> siteTemplates;
    private PSRenderServiceClient renderServiceClient;
    private PSAsyncJobStatusRestClient asyncJobStatusRestClient;
    
    public PSSite site1;
    public PSSite site2;
    
    public String baseTemplateId;
    
    public PSTemplateSummary template1;
    public PSTemplateSummary template2;
    
    PSTestDataCleaner<String> siteCleaner = new SiteCleaner();
    
    public class SiteCleaner extends PSTestDataCleaner<String> {
        
        
        @Override
        protected void clean(String name) throws Exception
        {
            siteRestClient.delete(name);
        }    
    }
    
    PSTestDataCleaner<String> templateCleaner = new TemplateCleaner();
    
    public class TemplateCleaner extends  PSTestDataCleaner<String> {

        @Override
        protected void clean(String name) throws Exception
        {
            String id = templateNameToId(name);
            templateServiceClient.deleteTemplate(id); 
        }
        
        private String templateNameToId(String name) {
            List<PSTemplateSummary> templates = templateServiceClient.findAll();
            for(PSTemplateSummary sum : templates) {
                if (ObjectUtils.equals(sum.getName(), name)) {
                    return sum.getId();
                }
            }
            return null;
        }
    
    }

    
    PSTestDataCleaner<String> pageCleaner = new PageCleaner();
        
    public class PageCleaner extends PSTestDataCleaner<String>
    {
        @Override
        protected void clean(String folderPath) throws Exception
        {
            PSPage page = pageRestClient.findPageByFullFolderPath(folderPath);
            pageRestClient.delete(page.getId());
        }
    }

    
    PSTestDataCleaner<String> assetCleaner = new AssetCleaner();
    
    public class AssetCleaner extends PSTestDataCleaner<String>
    {
        @Override
        protected void clean(String id) throws Exception
        {
            assetRestClient.delete(id);
        }
    }
    
    PSTestDataCleaner<String> sectionCleaner = new SectionCleaner();
    
    public class SectionCleaner extends PSTestDataCleaner<String>
    {
        @Override
        protected void clean(String sectionId) throws Exception
        {
            sectionClient.delete(sectionId);
        }
        
        @Override
        protected List<String> getDataIds()
        {
            // the order of the list is parent object followed by its children
            // need to reverse the order, so that we can delete the child folder
            // first, then delete the parent folder in the clean() method.
            
            List<String> ids = super.getDataIds();
            Collections.reverse(ids);
            return ids;
        }
        
    }
    
    public void setUpClients() throws Exception {
        siteTemplateRestClient = new PSSiteTemplateRestClient();
        templateServiceClient = new PSTemplateServiceClient(baseUrl);
        siteRestClient = new PSSiteRestClient(baseUrl);
        pageRestClient = new PSPageRestClient(baseUrl);
        assetRestClient = new PSAssetServiceRestClient(baseUrl);
        sectionClient = new PSSiteSectionRestClient(baseUrl);
        pathClient = new PSPathServiceRestClient(baseUrl);
        workflowClient = new PSItemWorkflowServiceRestClient(baseUrl);
        renderServiceClient = new PSRenderServiceClient();
        asyncJobStatusRestClient = new PSAsyncJobStatusRestClient();
        
        setupClient(siteTemplateRestClient);
        setupClient(templateServiceClient);
        setupClient(siteRestClient);
        setupClient(pageRestClient);
        setupClient(assetRestClient);
        setupClient(sectionClient);
        setupClient(pathClient);
        setupClient(workflowClient);
        setupClient(renderServiceClient);
        setupClient(asyncJobStatusRestClient);
    }
    
    public void setUp() throws Exception {
        log.info("!!!!!!!!!!!!!!  Started Setup  !!!!!!!!!!!!!");
        setUpClients();
        PSTemplateSummary sum = templateServiceClient.findAllReadOnly().get(0);
        baseTemplateId = sum.getId();
        String t1 = "SiteTemplateServiceTest1" + System.currentTimeMillis();
        String t2 = "SiteTemplateServiceTest2" + System.currentTimeMillis();
        
        site1 = createSite(t1, sum.getName());
        template1 = siteTemplateRestClient.findTemplatesBySite(site1.getId()).get(0);
        log.debug("Created template: " + template1);
        site2 = createSite(t2, sum.getName());
        template2 = siteTemplateRestClient.findTemplatesBySite(site2.getId()).get(0);
        log.debug("Created template: " + template2);    

        PSSiteTemplates s = new PSSiteTemplates();
        AssignTemplate a1 = new AssignTemplate();
        AssignTemplate a2 = new AssignTemplate();
        
        a1.setTemplateId(template1.getId());
        a1.setSiteIds(asList(site1.getName()));
        a2.setTemplateId(template2.getId());
        a2.setSiteIds(asList(site2.getName()));
        
        CreateTemplate c1 = new CreateTemplate();
        c1.setName("SiteTemplateServiceCreated1");
                
        c1.setSiteIds(asList(site1.getName()));
        c1.setSourceTemplateId(sum.getId());
        
        s.setAssignTemplates(asList(a1,a2));
        s.setCreateTemplates(asList(c1));
        
        List<PSTemplateSummary> templates = siteTemplateRestClient.save(s);
        
        String json = siteTemplateRestClient.objectToJson(s);
        log.debug("JSON of site templates: " + json);
        log.debug("Saved templates: " + templates);
        assertEquals("Number of templates", 3, templates.size());
        
        this.siteTemplates = templates;
        log.info("!!!!!!!!!!!!!!  Finished Setup  !!!!!!!!!!!!!");
    }
    
    public void tearDown() throws Exception {
        log.info("!!!!!!!!!!!!!!  Started Tear Down  !!!!!!!!!!!!!");
        setUpClients();
        PSTestDataCleaner.runCleaners(sectionCleaner,pageCleaner,siteCleaner,templateCleaner,assetCleaner);
        log.info("!!!!!!!!!!!!!!  Finished Tear Down  !!!!!!!!!!!!!");
    }
    
    public static final String TEST_WIDGET_DEFINITION = "PSWidget_TestProperties";
    
    public PSTemplateSummary createTemplate(String name) {
        templateCleaner.add(name);
        return templateServiceClient.createTemplate(name, baseTemplateId);
    }
    public PSWidgetItem createWidgetItem(String name, String definition) {
        PSWidgetItem widgetItem = new PSWidgetItem();
        widgetItem.setName(name);
        widgetItem.setDefinitionId(definition);
        return widgetItem;
    }
    
    public List<PSTemplateSummary> assignTemplatesToSite(String siteId, String ... templateIds) {
        PSSiteTemplates s = new PSSiteTemplates();
        List<AssignTemplate> assigns = new ArrayList<AssignTemplate>();
        for (String tid : templateIds) {
            AssignTemplate a = new AssignTemplate();
            a.setSiteIds(asList(siteId));
            a.setTemplateId(tid);
            assigns.add(a);
        }
        s.setAssignTemplates(assigns);
        return siteTemplateRestClient.save(s);
    }
    
    public PSSite createSite(String name, String baseTemplateName)
    {
        siteCleaner.add(name);
        PSSite site = new PSSite();
        site.setName(name);
        site.setLabel("My test site");
        site.setHomePageTitle("homePageTitle");
        site.setNavigationTitle("navigationTitle");
        site.setBaseTemplateName(baseTemplateName);
        site.setTemplateName(baseTemplateName + System.currentTimeMillis());
        
        PSSite actual = siteRestClient.save(site);
        // assertEquals(site, actual);
        return actual;
    }
    
    
    public String createPage(String name, String folderPath, String templateId) throws Exception
    {
        notEmpty(templateId);
        String pageId = null;

        PSPage pageNew = new PSPage();
        pageNew.setName(name);
        pageNew.setTitle(name);
        pageNew.setFolderPath(folderPath);
        pageNew.setTemplateId(templateId);
        pageNew.setLinkTitle("dummy");
        PSPage r = pageRestClient.save(pageNew);
        pageId = r.getId();
        String fullPath = folderPath + "/" + name;
        pageCleaner.add(fullPath);
        
        return pageId;

    }
    
    public PSAsset saveAsset(PSAsset asset) {
        PSAsset rvalue = assetRestClient.save(asset);
        assetCleaner.add(rvalue.getId());
        return rvalue;
    }
    
    
    public PSSiteSection createSection(PSCreateSiteSection req)
    {
        PSSiteSection section = sectionClient.create(req);
        sectionCleaner.add(section.getId());        
        return section;
    }
    
    public PSSiteSection createSectionLink(String targetSectionGuid, String parentSectionGuid)
    {
        PSSiteSection section = sectionClient.createSectionLink(targetSectionGuid, parentSectionGuid);
        return section;
    }

    /**
     * This is reverse of {@link #createSection(PSCreateSiteSection)}.
     * Remove the site section from the clean up list where it has already 
     * been deleted by the unit test.
     * 
     * @param section the section that does not need to be cleaned up,
     * not <code>null</code>.
     */
    public void removeSectionFromCleaner(PSSiteSection section)
    {
        sectionCleaner.remove(section.getId());
    }
    
    public PSSiteSectionRestClient getSectionClient()
    {
        return sectionClient;
    }
    
    public PSItemWorkflowServiceRestClient getWorkflowClient()
    {
        return workflowClient;
    }
    
    public PSPathServiceRestClient getPathRestClient()
    {
        return pathClient;
    }
    
    public PSAssetServiceRestClient getAssetRestClient()
    {
        return assetRestClient;
    }

    public void setAssetRestClient(PSAssetServiceRestClient assetRestClient)
    {
        this.assetRestClient = assetRestClient;
    }

    public PSSiteTemplateRestClient getSiteTemplateRestClient()
    {
        return siteTemplateRestClient;
    }

    public void setSiteTemplateRestClient(PSSiteTemplateRestClient siteTemplateRestClient)
    {
        this.siteTemplateRestClient = siteTemplateRestClient;
    }

    public PSTemplateServiceClient getTemplateServiceClient()
    {
        return templateServiceClient;
    }

    public void setTemplateServiceClient(PSTemplateServiceClient templateServiceClient)
    {
        this.templateServiceClient = templateServiceClient;
    }

    public PSSiteRestClient getSiteRestClient()
    {
        return siteRestClient;
    }

    public void setSiteRestClient(PSSiteRestClient siteRestClient)
    {
        this.siteRestClient = siteRestClient;
    }
    
    public PSRenderServiceClient getRenderServiceClient()
    {
        return renderServiceClient;
    }
    
    public void setRenderServiceClient(PSRenderServiceClient renderServiceClient)
    {
        this.renderServiceClient = renderServiceClient;
    }

    public List<PSTemplateSummary> getSiteTemplates()
    {
        return siteTemplates;
    }

    public void setSiteTemplates(List<PSTemplateSummary> siteTemplates)
    {
        this.siteTemplates = siteTemplates;
    }
    
    public PSAsyncJobStatusRestClient getAsyncJobStatusRestClient()
    {
        return asyncJobStatusRestClient;
    }

    public void setAsyncJobStatusRestClient(PSAsyncJobStatusRestClient asyncJobStatusRestClient)
    {
        this.asyncJobStatusRestClient = asyncJobStatusRestClient;
    }

    public PSPageRestClient getPageRestClient()
    {
        return pageRestClient;
    }

    public void setPageRestClient(PSPageRestClient pageRestClient)
    {
        this.pageRestClient = pageRestClient;
    }

    public PSTestDataCleaner<String> getSiteCleaner()
    {
        return siteCleaner;
    }

    public PSTestDataCleaner<String> getTemplateCleaner()
    {
        return templateCleaner;
    }
    
    public PSTestDataCleaner<String> getPageCleaner()
    {
        return pageCleaner;
    }
    
    public PSTestDataCleaner<String> getSectionCleaner()
    {
        return sectionCleaner;
    }

    public PSTestDataCleaner<String> getAssetCleaner()
    {
        return assetCleaner;
    }

    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Logger log = LogManager.getLogger(PSTestSiteData.class);

    /**
     * Updates a section link and returns the result of the update.
     * 
     * @param updateRequest {@link PSUpdateSectionLink} request, assumed not
     *            <code>null</code>.
     * @return {@link PSSiteSection} never <code>null</code>.
     */
    public PSSiteSection updateSectionLink(PSUpdateSectionLink updateRequest)
    {
        PSSiteSection section = sectionClient.updateSectionLink(updateRequest);
        return section;        
    }
    
    
}
