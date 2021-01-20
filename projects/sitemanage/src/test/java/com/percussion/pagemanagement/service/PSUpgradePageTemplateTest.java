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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.percussion.pagemanagement.dao.IPSPageDao;
import com.percussion.pagemanagement.dao.IPSTemplateDao;
import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.data.PSRegionBranches;
import com.percussion.pagemanagement.data.PSRegionWidgets;
import com.percussion.pagemanagement.data.PSTemplate;
import com.percussion.pagemanagement.data.PSWidgetItem;
import com.percussion.services.content.data.PSItemStatus;
import com.percussion.share.service.IPSIdMapper;
import com.percussion.share.spring.PSSpringWebApplicationContextUtils;
import com.percussion.test.PSServletTestCase;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.security.IPSSecurityWs;
import com.percussion.webservices.system.IPSSystemWs;
import org.junit.experimental.categories.Category;

/**
 * This is used to upgrade widget IDs (or names) in the CLOB field of pages and tempaltes.
 * This is done in addition of updating database through various SQL statements, rename
 * table name, update relationship database table, ...etc. (see detail in change list 42191)
 * 
 * @author YuBingChen
 */
@Category(IntegrationTest.class)
public class PSUpgradePageTemplateTest extends PSServletTestCase
{

    @Override
    public void setUp() throws Exception
    {
        PSSpringWebApplicationContextUtils.injectDependencies(this);
        securityWs.login("Admin", "demo", "Default", null);

        widgetMap.put("event", "percEvent");
        widgetMap.put("file", "percFile");
        widgetMap.put("flash", "percFlash");
        widgetMap.put("image", "percImage");
        widgetMap.put("navBar", "percNavBar");
        widgetMap.put("navBreadcrumb", "percNavBreadcrumb");
        widgetMap.put("PageAutoList", "percPageAutoList");
        widgetMap.put("PSWidget_RawHtml", "percRawHtml");
        widgetMap.put("PSWidget_RichText", "percRichText");
        widgetMap.put("PSWidget_SimpleText", "percSimpleText");
        //FB:IJU_SETUP_NO_SUPER NC 1-16-16
        super.setUp();
    }

    private Map<String, String> widgetMap = new HashMap<String, String>();

    public void testUpdateAllTemplates() throws Exception
    {
        List<PSTemplate> updateTempates = new ArrayList<PSTemplate>();
        for (PSTemplate template : templateDao.findAll())
        {
            Set<PSRegionWidgets> widgets = template.getRegionTree().getRegionWidgetAssociations();
            boolean updateTemplate = needToResetWidgets(widgets);
            if (updateTemplate)
                updateTempates.add(template);
        }

        for (PSTemplate template : updateTempates)
        {
            templateDao.save(template);
            System.out.println("Updated page \"" + template.getName() + "\"");
        }
    }

    public void testUpdateAllPages() throws Exception
    {
        List<PSPage> updatePages = new ArrayList<PSPage>();
        for (PSPage page : pageDao.findAll())
        {
            PSRegionBranches branches = page.getRegionBranches();
            Set<PSRegionWidgets> widgets = branches.getRegionWidgetAssociations();
            boolean updatePage = needToResetWidgets(widgets);
            if (updatePage)
                updatePages.add(page);
        }

        for (PSPage page : updatePages)
        {
            IPSGuid id = idMapper.getGuid(page.getId());
            PSItemStatus status = contentWs.prepareForEdit(id);

            pageDao.save(page);

            contentWs.releaseFromEdit(status, false);
            System.out.println("Updated page \"" + page.getName() + "\"");
        }
    }

    private boolean needToResetWidgets(Set<PSRegionWidgets> widgets)
    {
        boolean isUpdated = false;
        for (PSRegionWidgets ws : widgets)
        {
            for (PSWidgetItem w : ws.getWidgetItems())
            {
                String oldName = w.getDefinitionId();
                String newName = widgetMap.get(oldName);
                if (newName != null)
                {
                    isUpdated = true;
                    w.setDefinitionId(newName);
                    System.out.println("Change widget definition: " + oldName + " -> " + newName);
                }
            }
        }
        return isUpdated;
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

    public IPSTemplateDao getTemplateDao()
    {
        return templateDao;
    }

    public void setTemplateDao(IPSTemplateDao templateDao)
    {
        this.templateDao = templateDao;
    }

    public IPSPageDao getPageDao()
    {
        return pageDao;
    }

    public void setPageDao(IPSPageDao pageDao)
    {
        this.pageDao = pageDao;
    }


    public IPSSecurityWs getSecurityWs()
    {
        return securityWs;
    }

    public void setSecurityWs(IPSSecurityWs securityWs)
    {
        this.securityWs = securityWs;
    }

    public IPSContentWs getContentWs()
    {
        return contentWs;
    }

    public void setContentWs(IPSContentWs contentWs)
    {
        this.contentWs = contentWs;
    }

    private IPSPageService pageService;

    private IPSIdMapper idMapper;

    private IPSSystemWs systemWs;

    private IPSPageDao pageDao;

    private IPSTemplateDao templateDao;

    private IPSSecurityWs securityWs;

    private IPSContentWs contentWs;
}
