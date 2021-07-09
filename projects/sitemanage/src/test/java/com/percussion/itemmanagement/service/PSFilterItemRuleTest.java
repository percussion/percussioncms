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

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.pagemanagement.data.PSPage;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.filter.IPSFilterItem;
import com.percussion.services.filter.PSFilterException;
import com.percussion.services.filter.data.PSFilterItem;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.share.dao.IPSFolderHelper;
import com.percussion.sitemanage.service.impl.PSPublicAssetItemFilterRule;
import com.percussion.sitemanage.service.impl.PSStartDateFilterRule;
import com.percussion.utils.exceptions.PSORMException;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.publishing.IPSPublishingWs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tests the item filter rules that are used by CM1 
 */
public class PSFilterItemRuleTest extends PSItemWorkflowServiceTestBase
{
    public void testFilterItem() throws Exception
    {
        PSPage page = createPage("testPage", templateId);
        String pageId = getPageService().save(page).getId();
        pageCleaner.add(pageId);

        IPSGuid pid = getIdMapper().getGuid(pageId);
        IPSGuid siteId = new PSGuid(PSTypeEnum.SITE, fixture.site1.getSiteId());
        IPSFilterItem item = new PSFilterItem(pid, null, siteId);
     
        List<IPSFilterItem> items = new ArrayList<IPSFilterItem>();
        items.add(item);

        validateStartDateFilterRule(pid, items);
        
        validatePublicAssetItemRule(pageId, items);
    }


    private void validatePublicAssetItemRule(String pageId, List<IPSFilterItem> items) throws Exception
    {
        Map<String, String> params = new HashMap<String, String>();
        
        List<IPSFilterItem> filtered;
        PSPublicAssetItemFilterRule pubRule = createPublicItemFilterRule();
        filtered = pubRule.filter(items, params);
        assertTrue("Should filter out the item", filtered.size() == 0);
        
        PSComponentSummary summary = getSummary(pageId);
        assertTrue(summary.getPublicRevision() == -1);

        summary.setContentStartDate(null);
        saveSummary(summary);
        
        getItemWorkflowService().transition(pageId, IPSItemWorkflowService.TRANSITION_TRIGGER_APPROVE);
        filtered = pubRule.filter(items, params);
        assertTrue("Should filter out the item", filtered.size() == 1);
        
        summary = getSummary(pageId);
        int lastPublicRev = summary.getPublicRevision();
        assertTrue(lastPublicRev != -1);
        
        
        Date futureDate = new Date(System.currentTimeMillis() + 24*60*60);
        summary = saveSummaryStartDate(pageId, futureDate);
        
        int tipRev = summary.getTipLocator().getRevision();
        
        getItemWorkflowService().checkOut(pageId);
        // check in and transition back to pending
        getItemWorkflowService().checkIn(pageId);

        summary = getSummary(pageId);
        int tipRev2 = summary.getTipLocator().getRevision();
        assertTrue("Always advanced tip revision", tipRev < tipRev2);
        
        int lastPublicRev2 = summary.getPublicRevision();
        assertTrue("Last public revision should not change, as the starting date in the future", lastPublicRev == lastPublicRev2);
    }


    private PSComponentSummary saveSummaryStartDate(String pageId, Date futureDate) throws Exception
    {
        return saveSummaryStartDate(getIdMapper().getGuid(pageId), futureDate);
    }

    private PSComponentSummary saveSummaryStartDate(IPSGuid pageId, Date futureDate) throws Exception
    {
        PSComponentSummary summary = getSummary(pageId);
        summary.setContentStartDate(futureDate);
        saveSummary(summary);
        return summary;
    }


    private Map<String, String> validateStartDateFilterRule(IPSGuid pid, List<IPSFilterItem> items)
            throws Exception
    {
        Map<String, String> params = new HashMap<String, String>();
        PSStartDateFilterRule rule = new PSStartDateFilterRule();
        
        List<IPSFilterItem> filtered = rule.filter(items, params);
        assertTrue("Should pass the start date filter", filtered.size() == 1);
        
        Date futureDate = new Date(System.currentTimeMillis() + 24*60*60);
        saveSummaryStartDate(pid, futureDate);
        
        filtered = rule.filter(items, params);
        assertTrue("Should filter out the item with starting date in the future", filtered.size() == 0);
        return params;
    }

    private void saveSummary(PSComponentSummary summary) throws Exception
    {
        getCmsObjectMgr().saveComponentSummaries(Collections.singletonList(summary));
    }
    
    private PSComponentSummary getSummary(String pageId)
    {
        return getCmsObjectMgr().loadComponentSummary(getIdMapper().getContentId(pageId));
    }

    private PSComponentSummary getSummary(IPSGuid pageId)
    {
        return getCmsObjectMgr().loadComponentSummary(getIdMapper().getContentId(pageId));
    }

    private PSPublicAssetItemFilterRule createPublicItemFilterRule()
    {
        PSPublicAssetItemFilterRule rule = new PSPublicAssetItemFilterRule();
        rule.setCmsObjectManager(getCmsObjectMgr());
        rule.setFolderHelper(getFolderHelper());
        rule.setGuidManager(getGuidManager());
        rule.setIdMapper(getIdMapper());
        rule.setItemDefManager(getItemDefManager());
        rule.setPublishingWs(getPublishingWs());
        rule.setSystemWs(getSystemWs());
        rule.setWidgetAssetRelationshipService(getWidgetAssetRelationshipService());
        rule.setWorkflowHelper(getWorkflowHelper());
        
        return rule;
    }
    
    IPSPublishingWs publishingWs;
    
    public IPSPublishingWs getPublishingWs()
    {
        return publishingWs;
    }

    public void setPublishingWs(IPSPublishingWs publishingWs)
    {
        this.publishingWs = publishingWs;
    }    


    private PSItemDefManager itemDefManager;
    
    public PSItemDefManager getItemDefManager()
    {
        return itemDefManager;
    }

    public void setItemDefManager(PSItemDefManager itemDefManager)
    {
        this.itemDefManager = itemDefManager;
    }


    IPSGuidManager guidManager;
    
    public IPSGuidManager getGuidManager()
    {
        return guidManager;
    }

    public void setGuidManager(IPSGuidManager guidManager)
    {
        this.guidManager = guidManager;
    }


    private IPSFolderHelper folderHelper;
    

    public void setFolderHelper(IPSFolderHelper folderHelper)
    {
        this.folderHelper = folderHelper;
    }

    public IPSFolderHelper getFolderHelper()
    {
        return folderHelper;
    }

//    public IPSPageService getPageService()
//    {
//        return pageService;
//    }
//    
//    public void setPageService(IPSPageService pageService)
//    {
//        this.pageService = pageService;
//    }
//
//    public IPSAssetService getAssetService()
//    {
//        return assetService;
//    }
//
//    public void setAssetService(IPSAssetService assetService)
//    {
//        this.assetService = assetService;
//    }
//    
//    public IPSIdMapper getIdMapper()
//    {
//        return idMapper;
//    }
//
//    public void setIdMapper(IPSIdMapper idMapper)
//    {
//        this.idMapper = idMapper;
//    }
//    
//    public IPSWidgetAssetRelationshipService getWidgetAssetRelationshipService()
//    {
//        return widgetAssetRelationshipService;
//    }
//
//    public void setWidgetAssetRelationshipServiceao(IPSWidgetAssetRelationshipService widgetAssetRelationshipService)
//    {
//        this.widgetAssetRelationshipService = widgetAssetRelationshipService;
//    }
//
//    public IPSItemWorkflowService getItemWorkflowService()
//    {
//        return itemWorkflowService;
//    }
//
//    public void setItemWorkflowService(IPSItemWorkflowService itemWorkflowService)
//    {
//        this.itemWorkflowService = itemWorkflowService;
//    }
//    
//    public IPSSecurityWs getSecurityWs()
//    {
//        return securityWs;
//    }
//
//    public void setSecurityWs(IPSSecurityWs securityWs)
//    {
//        this.securityWs = securityWs;
//    }
//    
//    public IPSSystemWs getSystemWs()
//    {
//        return systemWs;
//    }
//
//    public void setSystemWs(IPSSystemWs systemWs)
//    {
//        this.systemWs = systemWs;
//    }
//    
//    public IPSCmsObjectMgr getCmsObjectMgr()
//    {
//        return cmsObjectMgr;
//    }
//
//    public void setCmsObjectMgr(IPSCmsObjectMgr cmsObjectMgr)
//    {
//        this.cmsObjectMgr = cmsObjectMgr;
//    }
//    
//    public IPSWorkflowHelper getWorkflowHelper()
//    {
//        return workflowHelper;
//    }
//
//    public void setWorkflowHelper(IPSWorkflowHelper workflowHelper)
//    {
//        this.workflowHelper = workflowHelper;
//    }
//    
//    public IPSContentDesignWs getContentDesignWs()
//    {
//        return contentDesignWs;
//    }
//
//    public void setContentDesignWs(IPSContentDesignWs contentDesignWs)
//    {
//        this.contentDesignWs = contentDesignWs;
//    }
//    
//    /**
//     * @return the workflowService
//     */
//    public IPSWorkflowService getWorkflowService()
//    {
//        return workflowService;
//    }
//
//    /**
//     * @param workflowService the workflowService to set
//     */
//    public void setWorkflowService(IPSWorkflowService workflowService)
//    {
//        this.workflowService = workflowService;
//    }
//    
//    public IPSManagedNavService getNavService()
//    {
//        return navService;
//    }
//    
//    public void setNavService(IPSManagedNavService navService)
//    {
//        this.navService = navService;
//    }
//    
//    private IPSPageService pageService;
//    private IPSAssetService assetService;
//    private IPSIdMapper idMapper;
//    private IPSWidgetAssetRelationshipService widgetAssetRelationshipService;
//    private IPSItemWorkflowService itemWorkflowService;
//    private IPSSecurityWs securityWs;
//    private IPSSystemWs systemWs;
//    private IPSCmsObjectMgr cmsObjectMgr;
//    private IPSWorkflowHelper workflowHelper;
//    private IPSContentDesignWs contentDesignWs;
//    private IPSWorkflowService workflowService;
//    private IPSManagedNavService navService;


}
