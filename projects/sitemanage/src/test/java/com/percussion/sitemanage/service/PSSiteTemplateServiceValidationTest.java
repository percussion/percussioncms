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
package com.percussion.sitemanage.service;

import com.percussion.assetmanagement.service.IPSAssetService;
import com.percussion.assetmanagement.service.IPSWidgetAssetRelationshipService;
import com.percussion.itemmanagement.service.IPSItemWorkflowService;
import com.percussion.pagemanagement.service.IPSPageService;
import com.percussion.pagemanagement.service.IPSPageTemplateService;
import com.percussion.pagemanagement.service.IPSTemplateService;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.share.async.IPSAsyncJobService;
import com.percussion.share.dao.IPSFolderHelper;
import com.percussion.share.service.exception.PSBeanValidationException;
import com.percussion.sitemanage.dao.IPSiteDao;
import com.percussion.sitemanage.service.PSSiteTemplates.CreateTemplate;
import com.percussion.sitemanage.service.impl.PSSiteTemplateService;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

//import static java.util.Arrays.*;
//import static org.hamcrest.CoreMatchers.*;
//import static org.junit.matchers.JUnitMatchers.*;

/**
 * Scenario description: 
 * @author adamgent, Oct 14, 2009
 */
@RunWith(JMock.class)
public class PSSiteTemplateServiceValidationTest
{

    Mockery context = new JUnit4Mockery();

    PSSiteTemplateService sut;

    IPSTemplateService templateService;
    IPSSiteSectionMetaDataService siteSectionMetaDataService;
    IPSiteDao siteDao;
    IPSSiteImportService siteImportService;
    IPSSiteManager siteMgr;
    IPSAsyncJobService asyncJobService;
    IPSPageService pageService;
    IPSAssetService assetService; 
    IPSItemWorkflowService itemWorkflowService; 
    IPSWidgetAssetRelationshipService widgetAssetRelationshipService;
    IPSPageTemplateService pageTemplateService;
    IPSFolderHelper folderHelper;
    
    @Before
    public void setUp() throws Exception
    {
        templateService = context.mock(IPSTemplateService.class);
        siteSectionMetaDataService = context.mock(IPSSiteSectionMetaDataService.class);
        siteDao = context.mock(IPSiteDao.class);
        asyncJobService = context.mock(IPSAsyncJobService.class);
        pageService = context.mock(IPSPageService.class);
        assetService = context.mock(IPSAssetService.class); 
        itemWorkflowService = context.mock(IPSItemWorkflowService.class);
        widgetAssetRelationshipService = context.mock(IPSWidgetAssetRelationshipService.class);
        pageTemplateService = context.mock(IPSPageTemplateService.class);
        siteMgr = context.mock(IPSSiteManager.class);
        folderHelper = context.mock(IPSFolderHelper.class);

        
        
        sut = new PSSiteTemplateService(siteDao, siteSectionMetaDataService, templateService, asyncJobService, 
                pageService, assetService, itemWorkflowService, widgetAssetRelationshipService, pageTemplateService, siteMgr, folderHelper);
    }
    
    
    @Test
    public void shouldValidateSiteTemplatesAndNOTFailForEmptySiteTemplates() throws PSBeanValidationException {
        /*
         * Given: empty site templates.
         */
        PSSiteTemplates siteTemplates = new PSSiteTemplates();

        /* 
         * Expect:validation to be called but nothing should happen.
         */

        context.checking(new Expectations() {{
        }});

        /*
         * When: we call validation
         */
        sut.validate(siteTemplates);

        /*
         * Then: we should have nothing happen.
         */
    }
    
    @Test()
    public void shouldValidateCreateTemplatesAndFailIfNoSourceTemplateId() throws PSBeanValidationException {
        /*
         * Given: Site templates with a bad create template.
         */
        PSSiteTemplates siteTemplates = new PSSiteTemplates();
        CreateTemplate ct = new CreateTemplate();
        ct.setName("SetName");
        //null source template id is invalid.
        ct.setSourceTemplateId(null);
        siteTemplates.getCreateTemplates().add(ct);
        /* 
         * Expect:validation to be called but nothing should happen.
         */

        context.checking(new Expectations() {{
        }});

        /*
         * When: we call validation
         */
        sut.validate(siteTemplates);

        /*
         * Then: we should have an exception thrown as it is invalid.
         */
    }
    
    
    public void shouldValidateCreateTemplates() throws PSBeanValidationException {
        /*
         * Given: Site templates with a valid create template.
         */
        PSSiteTemplates siteTemplates = new PSSiteTemplates();
        CreateTemplate ct = new CreateTemplate();
        ct.setName("SetName");
        ct.setSourceTemplateId("sourceTemplateId");
        siteTemplates.getCreateTemplates().add(ct);
        /* 
         * Expect:validation to be called but nothing should happen.
         */

        context.checking(new Expectations() {{
        }});

        /*
         * When: we call validation
         */
        sut.validate(siteTemplates);

        /*
         * Then: we should have nothing happen.
         */
    }

}
