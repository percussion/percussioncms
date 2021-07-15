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

package com.percussion.sitemanage.service;

import static java.util.Arrays.asList;

import com.percussion.assetmanagement.data.PSAsset;
import com.percussion.assetmanagement.data.PSAssetWidgetRelationship;
import com.percussion.assetmanagement.data.PSAssetWidgetRelationship.PSAssetResourceType;
import com.percussion.assetmanagement.service.IPSAssetService;
import com.percussion.assetmanagement.service.impl.PSWidgetAssetRelationshipService;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.pagemanagement.data.PSTemplate;
import com.percussion.pagemanagement.data.PSTemplateSummary;
import com.percussion.pagemanagement.data.PSTemplate.PSTemplateTypeEnum;
import com.percussion.pagemanagement.service.IPSTemplateService;
import com.percussion.pagemanagement.service.PSSiteDataServletTestCaseFixture;
import com.percussion.share.IPSSitemanageConstants;
import com.percussion.share.service.IPSIdMapper;
import com.percussion.share.spring.PSSpringWebApplicationContextUtils;
import com.percussion.sitemanage.data.PSSite;
import com.percussion.test.PSServletTestCase;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.webservices.system.IPSSystemWs;
import org.junit.experimental.categories.Category;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Category(IntegrationTest.class)
public class PSSiteTemplateServiceTest extends PSServletTestCase
{
    private IPSTemplateService templateService;

    private PSSiteDataServletTestCaseFixture fixture;

    private IPSAssetService assetService;

    private IPSSystemWs systemWs;

    private IPSIdMapper idMapper;

    private IPSSiteTemplateService siteTemplateService;
    
    private IPSSiteDataService siteDataService;

    public IPSSiteDataService getSiteDataService()
    {
        return siteDataService;
    }

    public void setSiteDataService(IPSSiteDataService siteDataService)
    {
        this.siteDataService = siteDataService;
    }

    public IPSSiteTemplateService getSiteTemplateService()
    {
        return siteTemplateService;
    }

    public void setSiteTemplateService(IPSSiteTemplateService siteTemplateService)
    {
        this.siteTemplateService = siteTemplateService;
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

    public void testFindTemplatesBySiteAndType() throws Exception
    {
        PSTemplateSummary sum1 = null;
        PSTemplateSummary sum2 = null;
        PSTemplateSummary sum3 = null;
         
        String site1Id = fixture.site1.getId();
        int nonTypedAmount = siteTemplateService.findTemplatesBySite(site1Id).size();
        
        sum1 = fixture.createTemplateWithSite("testTemplateItem_1", site1Id);
        PSTemplate item = templateService.load(sum1.getId());
        item.setType(PSTemplateTypeEnum.NORMAL.getLabel());
        templateService.save(item);

        sum2 = fixture.createTemplateWithSite("testTemplateItem_2", site1Id);
        PSTemplate item2 = templateService.load(sum2.getId());
        item2.setType(PSTemplateTypeEnum.NORMAL.getLabel());
        templateService.save(item2);
        
        sum3 = fixture.createTemplateWithSite("testTemplateItem_3", site1Id);
        PSTemplate item3 = templateService.load(sum3.getId());
        item3.setType(PSTemplateTypeEnum.UNASSIGNED.getLabel());
        templateService.save(item3);
     
        assertEquals(nonTypedAmount + 2, siteTemplateService.findTypedTemplatesBySite(site1Id, PSTemplateTypeEnum.NORMAL).size());
        assertEquals(1, siteTemplateService.findTypedTemplatesBySite(site1Id, PSTemplateTypeEnum.UNASSIGNED).size());
    }
    
    public void testCopyTemplates() throws Exception
    {
        PSTemplateSummary origSum1 = null;
        PSTemplateSummary origSum2 = null;
        PSTemplateSummary copySum1 = null;
                
        // add two templates to the fixture site
        String site1Id = fixture.site1.getId();
        origSum1 = fixture.createTemplateWithSite("testTemplateItem_1", site1Id);
        origSum2 = fixture.createTemplateWithSite("testTemplateItem_2", site1Id);

        // create an asset
        PSAsset asset = new PSAsset();
        asset.getFields().put("sys_title", "SharedAsset");
        asset.setFolderPaths(asList("//Folders/Assets"));
        asset.setType("percRawHtmlAsset");
        asset.getFields().put("html", "TestHTML");
        asset = assetService.save(asset);
        fixture.assetCleaner.add(asset.getId());
        String assetId = asset.getId();

        // add the asset to the templates as a shared resource
        PSAssetWidgetRelationship awRel = new PSAssetWidgetRelationship(origSum1.getId(), 5, "widget5", assetId, 1);
        awRel.setResourceType(PSAssetResourceType.shared);
        assetService.createAssetWidgetRelationship(awRel);

        awRel = new PSAssetWidgetRelationship(origSum2.getId(), 5, "widget5", assetId, 1);
        awRel.setResourceType(PSAssetResourceType.shared);
        assetService.createAssetWidgetRelationship(awRel);

        // create another site
        PSSite site2 = new PSSite();
        site2.setName(this.getName() + "Site");
        site2.setLabel(site2.getName());
        site2.setHomePageTitle("Home");
        site2.setNavigationTitle("Home");
        site2.setDescription("This is " + site2.getName());
        site2.setBaseTemplateName(IPSSitemanageConstants.PLAIN_BASE_TEMPLATE_NAME);
        site2.setTemplateName(site2.getName() + "PageTemplate");
        site2 = siteDataService.save(site2);
        String site2Id = site2.getId();
        fixture.siteCleaner.add(site2Id);

        // add template to the new site with same name as original site template and one additional template
        copySum1 = fixture.createTemplateWithSite(origSum1.getName(), site2Id);
        fixture.createTemplateWithSite("testTemplateItem_3", site2Id);
        
        // new site should now have three templates
        assertEquals(3, siteTemplateService.findTemplatesBySite(site2Id).size());

        // copy templates, all but one of the original site's templates should be copied
        List<PSTemplateSummary> site1Templates = siteTemplateService.findTemplatesBySite(site1Id);
        Map<String, String> tempMap = siteTemplateService.copyTemplates(site1Id, site2Id);
        assertNotNull(tempMap);
        assertEquals(site1Templates.size(), tempMap.size());
        for (String tempId : tempMap.keySet())
        {
            assertFalse(tempId.equals(tempMap.get(tempId)));
        }
        
        // site 2 should now have two more templates than site 1
        List<PSTemplateSummary> site2Templates = siteTemplateService.findTemplatesBySite(site2Id);
        assertEquals(site1Templates.size() + 2, site2Templates.size());
        Map<String, String> site2TempMap = new HashMap<String, String>();
        for (PSTemplateSummary site2Temp : site2Templates)
        {
            site2TempMap.put(site2Temp.getName(), site2Temp.getId());
        }
        
        // site 2 should now have all site 1 templates by name
        for (PSTemplateSummary site1Temp : site1Templates)
        {
            assertTrue(site2TempMap.containsKey(site1Temp.getName()));
            assertFalse(site2TempMap.get(site1Temp.getName()).equals(site1Temp.getId()));
        }
                
        // make sure the asset was copied to the second template
        PSRelationshipFilter filter = new PSRelationshipFilter();
        filter.limitToOwnerRevision(true);
        filter.setName(PSWidgetAssetRelationshipService.SHARED_ASSET_WIDGET_REL_FILTER);
        filter.setOwner(idMapper.getLocator(site2TempMap.get(origSum2.getName())));
        assertEquals(1, systemWs.loadRelationships(filter).size());
        
        // the existing template should not have been updated
        filter.setOwner(idMapper.getLocator(site2TempMap.get(copySum1.getName())));
        assertTrue(systemWs.loadRelationships(filter).isEmpty());
    }
    
}
