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
    
    @Test(expected=PSBeanValidationException.class)
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
