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
package com.percussion.pagemanagement.service;

import com.percussion.share.service.IPSDataService;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.webservices.content.IPSContentDesignWs;
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
    private IPSContentDesignWs contentDesignWs;

    public PSTemplateServiceValidationTest() {
    }

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
                workflowHelper, widgetDao, assemblyService, idMapper,contentDesignWs);
        
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

