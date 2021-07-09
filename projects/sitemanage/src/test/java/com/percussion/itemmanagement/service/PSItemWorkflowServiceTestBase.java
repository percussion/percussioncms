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

package com.percussion.itemmanagement.service;

import static java.util.Arrays.asList;

import com.percussion.assetmanagement.service.IPSAssetService;
import com.percussion.assetmanagement.service.IPSWidgetAssetRelationshipService;
import com.percussion.fastforward.managednav.IPSManagedNavService;
import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.data.PSRegion;
import com.percussion.pagemanagement.data.PSRegionBranches;
import com.percussion.pagemanagement.data.PSWidgetItem;
import com.percussion.pagemanagement.data.PSRegionNode.PSRegionOwnerType;
import com.percussion.pagemanagement.service.IPSPageService;
import com.percussion.pagemanagement.service.PSSiteDataServletTestCaseFixture;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.workflow.IPSWorkflowService;
import com.percussion.share.service.IPSIdMapper;
import com.percussion.share.spring.PSSpringWebApplicationContextUtils;
import com.percussion.share.test.PSTestDataCleaner;
import com.percussion.test.PSServletTestCase;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.webservices.content.IPSContentDesignWs;
import com.percussion.webservices.security.IPSSecurityWs;
import com.percussion.webservices.system.IPSSystemWs;
import org.junit.experimental.categories.Category;

import java.util.Collections;

@Category(IntegrationTest.class)
public class PSItemWorkflowServiceTestBase extends PSServletTestCase
{
    protected PSSiteDataServletTestCaseFixture fixture;
    protected String templateId;
    
    @Override
    public void setUp() throws Exception
    {
        PSSpringWebApplicationContextUtils.injectDependencies(this);
        fixture = new PSSiteDataServletTestCaseFixture(request, response);
        fixture.setUp();
        // create a template owner
        templateId = fixture.template1.getId();
        //FB:IJU_SETUP_NO_SUPER NC 1-16-16
        super.setUp();
    }
        
    @Override
    protected void tearDown() throws Exception
    {
        securityWs.login("admin1", "demo", "Enterprise_Investments", null);
        pageCleaner.clean();
        assetCleaner.clean();
        relationshipCleaner.clean();
        fixture.tearDown();
    }
    
    protected PSTestDataCleaner<String> pageCleaner = new PSTestDataCleaner<String>()
    {
        @Override
        protected void clean(String id) throws Exception
        {
            pageService.delete(id, true);
        }
    };

    protected PSTestDataCleaner<String> assetCleaner = new PSTestDataCleaner<String>()
    {
        @Override
        protected void clean(String id) throws Exception
        {
            assetService.delete(id);
        }
    };

    protected PSTestDataCleaner<IPSGuid> relationshipCleaner = new PSTestDataCleaner<IPSGuid>()
    {
        @Override
        protected void clean(IPSGuid id) throws Exception
        {
            systemWs.deleteRelationships(Collections.singletonList(id));
        }
    };
    
    /**
     * Creates a new page for the specified name and template.
     */
    protected PSPage createPage(String name, String templateId)
    {
        PSPage pageNew = new PSPage();
        pageNew.setName(name);
        pageNew.setTitle(name);
        pageNew.setFolderPath(fixture.site1.getFolderPath());
        pageNew.setTemplateId(templateId);
        pageNew.setLinkTitle("dummy");
        
        PSRegion region = new PSRegion();
        region.setOwnerType(PSRegionOwnerType.PAGE);
        region.setRegionId("Test");
        
        PSWidgetItem wi = new PSWidgetItem();
        wi.setDefinitionId("percRawHtml");

        PSRegionBranches br = new PSRegionBranches();
        br.setRegionWidgets("Test", asList(wi));
        br.setRegions(asList(region));
        
        pageNew.setRegionBranches(br);
        
        return pageNew;
    }
    
    public IPSPageService getPageService()
    {
        return pageService;
    }
    
    public void setPageService(IPSPageService pageService)
    {
        this.pageService = pageService;
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
    
    public IPSWidgetAssetRelationshipService getWidgetAssetRelationshipService()
    {
        return widgetAssetRelationshipService;
    }

    public void setWidgetAssetRelationshipServiceao(IPSWidgetAssetRelationshipService widgetAssetRelationshipService)
    {
        this.widgetAssetRelationshipService = widgetAssetRelationshipService;
    }

    public IPSItemWorkflowService getItemWorkflowService()
    {
        return itemWorkflowService;
    }

    public void setItemWorkflowService(IPSItemWorkflowService itemWorkflowService)
    {
        this.itemWorkflowService = itemWorkflowService;
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
    
    public IPSCmsObjectMgr getCmsObjectMgr()
    {
        return cmsObjectMgr;
    }

    public void setCmsObjectMgr(IPSCmsObjectMgr cmsObjectMgr)
    {
        this.cmsObjectMgr = cmsObjectMgr;
    }
    
    public IPSWorkflowHelper getWorkflowHelper()
    {
        return workflowHelper;
    }

    public void setWorkflowHelper(IPSWorkflowHelper workflowHelper)
    {
        this.workflowHelper = workflowHelper;
    }
    
    public IPSContentDesignWs getContentDesignWs()
    {
        return contentDesignWs;
    }

    public void setContentDesignWs(IPSContentDesignWs contentDesignWs)
    {
        this.contentDesignWs = contentDesignWs;
    }
    
    /**
     * @return the workflowService
     */
    public IPSWorkflowService getWorkflowService()
    {
        return workflowService;
    }

    /**
     * @param workflowService the workflowService to set
     */
    public void setWorkflowService(IPSWorkflowService workflowService)
    {
        this.workflowService = workflowService;
    }
    
    public IPSManagedNavService getNavService()
    {
        return navService;
    }
    
    public void setNavService(IPSManagedNavService navService)
    {
        this.navService = navService;
    }
    
    protected IPSPageService pageService;
    protected IPSAssetService assetService;
    protected IPSIdMapper idMapper;
    protected IPSWidgetAssetRelationshipService widgetAssetRelationshipService;
    protected IPSItemWorkflowService itemWorkflowService;
    protected IPSSecurityWs securityWs;
    protected IPSSystemWs systemWs;
    protected IPSCmsObjectMgr cmsObjectMgr;
    protected IPSWorkflowHelper workflowHelper;
    protected IPSContentDesignWs contentDesignWs;
    protected IPSWorkflowService workflowService;
    protected IPSManagedNavService navService;


}
