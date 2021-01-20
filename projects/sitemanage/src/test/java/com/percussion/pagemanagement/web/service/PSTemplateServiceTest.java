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
package com.percussion.pagemanagement.web.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.percussion.pagemanagement.data.PSHtmlMetadata;
import com.percussion.pagemanagement.data.PSMetadataDocType;
import com.percussion.pagemanagement.data.PSTemplate;
import com.percussion.pagemanagement.data.PSTemplateSummary;
import com.percussion.share.test.PSRestTestCase;
import com.percussion.share.test.PSTestDataCleaner;

import java.util.List;

import com.percussion.utils.testing.IntegrationTest;
import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Test template service through rest.
 * @author adamgent
 * @author YubingChen
 *
 */
@Category(IntegrationTest.class)
public class PSTemplateServiceTest extends PSRestTestCase<PSTemplateServiceClient>
{
    private String baseTemplateId;
    
    PSTestDataCleaner<String> templateCleaner = new PSTestDataCleaner<String>()
    {

        @Override
        protected void clean(String name) throws Exception
        {
            String id = getTemplateId(name);
            if (id != null)
                restClient.deleteTemplate(id);
        }
        
        /**
         * Gets a template id from a name.
         */
        private String getTemplateId(String name)
        {
            
            List<PSTemplateSummary> summaries = restClient.findAll();
            for (PSTemplateSummary sum : summaries)
            {
                if (!sum.isReadOnly() && StringUtils.equals(name, sum.getName()))
                {
                    String id = sum.getId();
                    assertNotNull(id);
                    return id;
                }
            }
            
            return null;
        }

    };
    {
        templateCleaner.setFailOnErrors(true);
    }
    
    @Before
    public void setup() {
        List<PSTemplateSummary> readOnlySums = restClient.findAllReadOnly();
        assertNotNull(readOnlySums);
        assertFalse(readOnlySums.isEmpty());
        baseTemplateId = readOnlySums.get(0).getId();
    }

    @Override
    protected PSTemplateServiceClient getRestClient(String url)
    {
        return new PSTemplateServiceClient(url);
    }

    @Test
    public void testFindAllTemplates() throws Exception
    {
        List<PSTemplateSummary> sums = restClient.findAll();
        assertNotNull(sums);
    }

    @Test
    public void testTemplateMetadata() throws Exception
    {
        templateCleaner.add("TestMetadataTemplate");
        PSTemplate template = restClient.createTemplate("TestMetadataTemplate", baseTemplateId);
        String head = "headContent";
        String bodyA = "afterBody";
        String bodyB = "beforeBody"; 

        PSHtmlMetadata metadataSaved = new PSHtmlMetadata();
        PSHtmlMetadata metadataToSet = new PSHtmlMetadata();
        
        metadataToSet.setId(template.getId());
        metadataToSet.setAdditionalHeadContent(head);
        metadataToSet.setAfterBodyStartContent(bodyA);
        metadataToSet.setBeforeBodyCloseContent(bodyB);

        //save metadata
        restClient.saveHtmlMetadata(metadataToSet);
        
        //load it and verify it matches what was saved.
        metadataSaved = restClient.loadHtmlMetadata(template.getId());
        assertTrue(StringUtils.equals(metadataSaved.getAdditionalHeadContent(), head));
        assertTrue(StringUtils.equals(metadataSaved.getBeforeBodyCloseContent(), bodyB));
        assertTrue(StringUtils.equals(metadataSaved.getAfterBodyStartContent(), bodyA));
    }
    
    @Test
    public void testCreateTemplate() throws Exception
    {
        templateCleaner.add("TestTemplate1");
        templateCleaner.add("TestTemplate2");
        
        // create template from base template
        
        PSTemplate newTemplate = restClient.createTemplate("TestTemplate1", baseTemplateId);
        assertNotNull(newTemplate.getRegionTree());
        PSTemplateSummary newSum1 = restClient.findTemplate(newTemplate.getId());
        assertNotNull(newSum1);
        assertEquals(newSum1.getName(), "TestTemplate1");

        // create template from user template (save as)
        PSTemplateSummary newSum2 = restClient.createTemplate("TestTemplate2", newSum1.getId());
        newSum2 = restClient.findTemplate(newSum2.getId()); 
        assertNotNull(newSum2);
        assertEquals(newSum2.getDescription(), newSum1.getDescription());
        assertEquals(newSum2.getImageThumbPath(), newSum1.getImageThumbPath());
        assertEquals(newSum2.getName(), "TestTemplate2");
        assertEquals(newSum2.getLabel(), newSum1.getLabel());
        
    }
    
    /**
     * For now just validate the page id gets passed to the service method
     * @throws Exception
     */
    @Test
    public void testSaveWithPage() throws Exception
    {
        String name = "TestSaveTemplateWithPage";
        templateCleaner.add(name);
        
        // create template from base template        
        PSTemplate newTemplate = restClient.createTemplate(name, baseTemplateId);
        assertNotNull(newTemplate);
        
        String badPageId = "nosuchpageid";
        boolean didThrow = false;
        try
        {
            restClient.save(newTemplate, badPageId);
        }
        catch (Exception e)
        {
            didThrow = true;
        }
        
        assertTrue(didThrow);
        
        // make sure works for real
        restClient.save(newTemplate);
        
    }
    
    @Test
    public void testCreateTemplateWithDefaultDocType() throws Exception
    {
        templateCleaner.add("TestTemplateDocType1");
        
        // create template from base template
        PSTemplate newTemplate = restClient.createTemplate("TestTemplateDocType1", baseTemplateId);
        assertNotNull(newTemplate.getRegionTree());
        PSTemplateSummary newSum1 = restClient.findTemplate(newTemplate.getId());
        assertNotNull(newSum1);
        assertEquals(newSum1.getName(), "TestTemplateDocType1");
        assertEquals(newTemplate.getDocType().getSelected(), "html5");
    }
    
    @Test
    public void testTemplateChangeDocType() throws Exception
    {
        templateCleaner.add("TestTemplateDocType2");
        
        // create template from base template
        PSTemplate newTemplate = restClient.createTemplate("TestTemplateDocType2", baseTemplateId);
        assertNotNull(newTemplate.getRegionTree());
        PSTemplateSummary newSum1 = restClient.findTemplate(newTemplate.getId());
        assertNotNull(newSum1);
        assertEquals(newSum1.getName(), "TestTemplateDocType2");
        assertEquals(newTemplate.getDocType().getSelected(), "html5");
        
        // change to XTHML doc type
        PSMetadataDocType docType = new PSMetadataDocType();
        docType.setSelected("xhtml");
        
        // change the metadata
        PSHtmlMetadata metadataSaved = new PSHtmlMetadata();
        PSHtmlMetadata metadataToSet = new PSHtmlMetadata();
        
        metadataToSet.setId(newTemplate.getId());
        metadataToSet.setDocType(docType);
        
        //save metadata
        restClient.saveHtmlMetadata(metadataToSet);
        
        //load it and verify it matches what was saved.
        metadataSaved = restClient.loadHtmlMetadata(newTemplate.getId());
        assertTrue(StringUtils.equals(metadataSaved.getDocType().getSelected(), "xhtml"));
    }
    
    /**
     * Deletes user templates we created.
     */
    @After
    public void cleanUp()
    {
        templateCleaner.clean();
    }
}
