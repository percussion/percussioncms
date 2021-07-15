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

import com.percussion.share.service.IPSDataService;
import com.percussion.share.service.exception.PSValidationException;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.percussion.assetmanagement.service.IPSAssetService;
import com.percussion.assetmanagement.service.IPSWidgetAssetRelationshipService;
import com.percussion.itemmanagement.service.IPSItemWorkflowService;
import com.percussion.itemmanagement.service.IPSWorkflowHelper;
import com.percussion.pagemanagement.dao.IPSPageDao;
import com.percussion.pagemanagement.dao.IPSPageDaoHelper;
import com.percussion.pagemanagement.dao.IPSTemplateDao;
import com.percussion.pagemanagement.dao.IPSWidgetDao;
import com.percussion.pagemanagement.data.PSRegionTree;
import com.percussion.pagemanagement.data.PSTemplate;
import com.percussion.pagemanagement.service.impl.PSTemplateService;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.share.service.IPSIdMapper;
import com.percussion.share.service.exception.PSBeanValidationException;
import com.percussion.sitemanage.service.IPSSiteTemplateService;


/**
 * Scenario description: Test template validation.
 * @author adamgent, Nov 13, 2009
 */
@RunWith(JMock.class)
@Ignore("Incompatible with JEXL Uberspect")
public class PSTemplateServiceValidationTest
{

    Mockery context = new JUnit4Mockery();

    private IPSTemplateService sut;

    private IPSTemplateDao templateDao;
    private IPSWidgetAssetRelationshipService widgetAssetRelationshipService;
    private IPSWidgetService widgetService;
    private IPSPageDao pageDao;
    private IPSPageDaoHelper pageDaoHelper;
    private IPSWorkflowHelper workflowHelper;
    private PSTemplate template;
    private IPSWidgetDao widgetDao;
    private IPSIdMapper idMapper;
    private IPSAssemblyService assemblyService;
    private IPSSiteTemplateService siteTemplateService;
    private IPSAssetService assetService;
    private IPSPageService pageService;
    private IPSItemWorkflowService itemWorkflowService;
    
    @SuppressWarnings("deprecation")
    @Before
    public void setUp() throws Exception
    {
     
        template = new PSTemplate();
        template.setName("name");
        template.setId("id");
        templateDao = context.mock(IPSTemplateDao.class);
        widgetService = context.mock(IPSWidgetService.class);
        widgetAssetRelationshipService = context.mock(IPSWidgetAssetRelationshipService.class);
        pageDao = context.mock(IPSPageDao.class);
        widgetDao = context.mock(IPSWidgetDao.class);
        workflowHelper = context.mock(IPSWorkflowHelper.class);
        idMapper = context.mock(IPSIdMapper.class);
        assemblyService = context.mock(IPSAssemblyService.class);
        
        
        sut = new PSTemplateService(templateDao, 
                widgetAssetRelationshipService, pageDao, pageDaoHelper, widgetService, 
                workflowHelper, widgetDao, assemblyService, idMapper);
        
        context.checking(new Expectations()
        {
            {
                allowing(templateDao).findUserTemplateByName_UsedByUnitTestOnly("name");
                will(returnValue(null));
            }
        });

    }
    
    @Test
    public void shouldNotFail() throws IPSDataService.DataServiceSaveException, PSValidationException {
        sut.validate(template);
    }
    
    @Test(expected=PSBeanValidationException.class)
    public void shouldFailWithBadEmptyName() throws IPSDataService.DataServiceSaveException, PSValidationException {
        template.setName("");
        sut.validate(template);
    }
    
    @Test(expected=PSBeanValidationException.class)
    public void shouldFailWithBadRegionTree() throws IPSDataService.DataServiceSaveException, PSValidationException {
        template.setRegionTree(new PSRegionTree());
        sut.validate(template);
    }
    
    
}

