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

package com.percussion.pagemanagement.dao;

import java.util.List;

import com.percussion.pagemanagement.dao.impl.PSMetadataDocTypeUtils;
import com.percussion.pagemanagement.data.PSTemplate;
import com.percussion.pagemanagement.data.PSTemplate.PSTemplateTypeEnum;
import com.percussion.pagemanagement.service.IPSTemplateService;
import com.percussion.pagemanagement.service.PSSiteDataServletTestCaseFixture;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.share.spring.PSSpringWebApplicationContextUtils;
import com.percussion.test.PSServletTestCase;
import com.percussion.utils.testing.IntegrationTest;
import org.junit.experimental.categories.Category;

/**
 * Tests the template dao functionality.
 * @author federicoromanelli
 */
@Category(IntegrationTest.class)
public class PSTemplateDaoTest extends PSServletTestCase
{
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

    /**
     * Saves the template with type property null and retrieves it back to test
     */
    public void testSaveTypePropertyNull() throws PSDataServiceException {
        String templateName = "Template1";        
        PSTemplate template = createTemplate(templateName);
        
        template = templateDao.save(template, fixture.site1.getId());
        
        PSTemplate retrievedTemplate = templateDao.find(template.getId());
        assertEquals(retrievedTemplate.getName(), templateName);
        assertNull(retrievedTemplate.getType());
    }
    
    /**
     * Saves the template with type property (not null value) and retrieves it back to test
     */
    public void testSaveTypeProperty() throws PSDataServiceException {
        String templateName = "Template1";
        String templateNormalName = "TemplateNormal";
        
        PSTemplate template = createTemplate(templateName, PSTemplateTypeEnum.UNASSIGNED.getLabel());
        PSTemplate templateNormal = createTemplate(templateNormalName, PSTemplateTypeEnum.NORMAL.getLabel());

        template = templateDao.save(template, fixture.site1.getId());
        templateNormal = templateDao.save(templateNormal, fixture.site1.getId());
        
        PSTemplate retrievedTemplate = templateDao.find(template.getId());
        assertEquals(retrievedTemplate.getName(), templateName);
        assertEquals(retrievedTemplate.getType(), PSTemplateTypeEnum.UNASSIGNED.getLabel());
        
        PSTemplate retrievedTemplateNormal = templateDao.find(templateNormal.getId());
        assertEquals(retrievedTemplateNormal.getName(), templateNormalName);
        assertEquals(retrievedTemplateNormal.getType(), PSTemplateTypeEnum.NORMAL.getLabel());        
    }
    
    /**
     * Tests the retrieving of templates using the findTemplatesByType dao method.
     */    
    public void testFindTemplatesByType() throws PSDataServiceException {
        
        String templateName = "Template1";
        String template2Name = "Template2";
        String template3Name = "Template3";
        String template4Name = "Template4";
        
        PSTemplate template = createTemplate(templateName, PSTemplateTypeEnum.UNASSIGNED.getLabel());
        template = templateDao.save(template, fixture.site1.getId());
        
        PSTemplate template2 = createTemplate(template2Name, PSTemplateTypeEnum.UNASSIGNED.getLabel());
        template2 = templateDao.save(template2, fixture.site1.getId());
        
        PSTemplate template3 = createTemplate(template3Name, PSTemplateTypeEnum.NORMAL.getLabel());
        template3 = templateDao.save(template3, fixture.site1.getId());
        
        PSTemplate template4 = createTemplate(template4Name);
        template4 = templateDao.save(template4, fixture.site1.getId());        
        
        List<PSTemplate> retrievedTemplates = templateDao.findUserTemplatesByType(PSTemplateTypeEnum.UNASSIGNED);
        
        assertTrue(retrievedTemplates.contains(template));
        assertTrue(retrievedTemplates.contains(template2));
        assertFalse(retrievedTemplates.contains(template3));
        assertFalse(retrievedTemplates.contains(template4));
        
        retrievedTemplates = templateDao.findUserTemplatesByType(PSTemplateTypeEnum.NORMAL);
        
        assertFalse(retrievedTemplates.contains(template));
        assertFalse(retrievedTemplates.contains(template2));
        assertTrue(retrievedTemplates.contains(template3));
        assertTrue(retrievedTemplates.contains(template4));
        
        retrievedTemplates = templateDao.findUserTemplatesByType(null);
        
        assertFalse(retrievedTemplates.contains(template));
        assertFalse(retrievedTemplates.contains(template2));
        assertTrue(retrievedTemplates.contains(template3));
        assertTrue(retrievedTemplates.contains(template4));        
        
    }
    
    public void testContentMigrationVersion() throws PSDataServiceException {
        PSTemplate template = createTemplate("Template1", PSTemplateTypeEnum.NORMAL.getLabel());
        template = templateDao.save(template, fixture.site1.getId());
        
        template = templateDao.find(template.getId());
        assertTrue(template != null);
        assertEquals("0", template.getContentMigrationVersion());
        
        template.setContentMigrationVersion("1");
        template = templateDao.save(template, fixture.site1.getId());
        template = templateDao.find(template.getId());
        assertTrue(template != null);
        assertEquals("1", template.getContentMigrationVersion());   
        
        
        PSTemplate template2 = createTemplate("Template2", PSTemplateTypeEnum.NORMAL.getLabel());
        
        template2.setContentMigrationVersion("1");
        template2 = templateDao.save(template2, fixture.site1.getId());
        template2 = templateDao.find(template2.getId());
        assertTrue(template2 != null);
        assertEquals("1", template2.getContentMigrationVersion());        
    }
    
    /**
     * Helper method that creates untyped template (type is null)
     * @param name - the name of the new template. Never <code>null</code>
     * @return PSTemplate - the template object created. Never <code>null</code>
     */
    private PSTemplate createTemplate(String name) throws PSDataServiceException {
        return createTemplate(name, null);
    }

    /**
     * Helper method that creates template with a specific type
     * @param name - the name of the new template. Never <code>null</code>
     * @param type - the type of template. Eg: NORMAL, UNASSIGNED
     * @return PSTemplate - the template object created. Never <code>null</code>
     */
    private PSTemplate createTemplate(String name, String type) throws PSDataServiceException {
        PSTemplate fixtureTemplate = templateDao.find(fixture.template1.getId());
        
        PSTemplate template = new PSTemplate();
        template.setName(name);
        template.setType(type);
        
        template.setReadOnly(false);
        template.setDocType(PSMetadataDocTypeUtils.getDefaultDocType());
        template.setImageThumbPath(fixtureTemplate.getImageThumbPath());
        template.setHtmlHeader(fixtureTemplate.getHtmlHeader());
        template.setLabel(name);
        template.setDescription(name);
        template.setTheme(fixtureTemplate.getTheme());
        template.setSourceTemplateName(fixtureTemplate.getSourceTemplateName());
        
        return template;
    }
    
    public IPSTemplateDao getTemplateDao()
    {
        return templateDao;
    }

    public void setTemplateDao(IPSTemplateDao templateDao)
    {
        this.templateDao = templateDao;
    }
    
    private PSSiteDataServletTestCaseFixture fixture;
    private IPSTemplateDao templateDao;    
}
