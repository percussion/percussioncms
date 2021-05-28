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

package com.percussion.widgetbuilder.service;

import com.percussion.servlets.PSSecurityFilter;
import com.percussion.share.spring.PSSpringWebApplicationContextUtils;
import com.percussion.utils.request.PSRequestInfo;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.widgetbuilder.data.PSWidgetBuilderDefinitionData;
import com.percussion.widgetbuilder.data.PSWidgetBuilderFieldData;
import com.percussion.widgetbuilder.data.PSWidgetBuilderFieldData.FieldType;
import com.percussion.widgetbuilder.data.PSWidgetBuilderResourceListData;
import com.percussion.widgetbuilder.data.PSWidgetBuilderSummaryData;
import com.percussion.widgetbuilder.data.PSWidgetBuilderValidationResults;
import com.percussion.widgetbuilder.utils.xform.PSContentTypeFileTransformerTest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.cactus.ServletTestCase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(IntegrationTest.class)
public class TestWidgetBuilderService extends ServletTestCase
{

    private static final Logger log = LogManager.getLogger(TestWidgetBuilderService.class);

    IPSWidgetBuilderService service;
    
    public void setService(IPSWidgetBuilderService service)
    {
        this.service = service;
    }
    
    
    @Override
    protected void setUp() throws Exception
    {
        PSSpringWebApplicationContextUtils.injectDependencies(this);
        PSRequestInfo.setRequestInfo(PSRequestInfo.KEY_USER, "Admin");
        PSSecurityFilter.authenticate(request, response, "Admin", "demo");
    }
    
    @Test
    public void testServiceCRUD()
    {
        int currentSize = service.loadAll().size();
        
        Set<String> ids = new HashSet<String>();
        long badId = 0;
        
        try
        {
            PSWidgetBuilderDefinitionData definition = new PSWidgetBuilderDefinitionData();
            assertEquals("Instantiated id is not equal to 0", 0, Long.parseLong(definition.getId()));
            assertNotNull("Instantiated fields is null", definition.getFieldsList().getFields());
            assertTrue("Instantiated fields is not empty", definition.getFieldsList().getFields().isEmpty());
            
            PSWidgetBuilderValidationResults results = service.saveWidgetBuilderDefinition(definition);
            badId = results.getDefinitionId();
            assertEquals("No Id assigned during persist", 0, badId);
            assertFalse(results.getResults().isEmpty());
                        
            definition.setDescription("a description");
            definition.setLabel("alabel");
            definition.setPrefix("perc");
            definition.setPublisherUrl("http://www.percussion.com");
            definition.setVersion("1.0.0");
            definition.setAuthor("Dr. Caligari");
            definition.setResponsive(true);
            definition = roundTrip(ids, definition);
            
            PSWidgetBuilderDefinitionData comparisonDefinition = service.loadWidgetDefinition(Long.parseLong(definition.getId()));
            assertTrue(definition.getDescription().equals(comparisonDefinition.getDescription()));
            assertTrue(definition.getLabel().equals(comparisonDefinition.getLabel()));
            assertTrue(definition.getPrefix().equals(comparisonDefinition.getPrefix()));
            assertTrue(definition.getPublisherUrl().equals(comparisonDefinition.getPublisherUrl()));
            assertTrue(definition.getVersion().equals(comparisonDefinition.getVersion()));
            assertTrue(definition.getAuthor().equals(comparisonDefinition.getAuthor()));
            service.deleteWidgetBuilderDefinition(Long.parseLong(definition.getId()));
            ids.remove(definition.getId());
            
            assertTrue(null == service.loadWidgetDefinition(Long.parseLong(definition.getId())));
            
            definition = roundTrip(ids, definition);
            PSWidgetBuilderDefinitionData definition2 = new PSWidgetBuilderDefinitionData();
            definition2.setDescription("a description");
            definition2.setLabel("alabel2");
            definition2.setPrefix("perc");
            definition2.setPublisherUrl("http://www.percussion.com");
            definition2.setVersion("1.0.0");
            definition2.setAuthor("Dr. Strangelove");
            definition2 =  roundTrip(ids, definition2);
            
            List<PSWidgetBuilderDefinitionData> definitions = service.loadAll();
            assertTrue(ids.size() + currentSize == definitions.size());
            
            List<PSWidgetBuilderSummaryData> sums = service.loadAllSummaries();
            assertEquals(definitions.size(), sums.size());
            List<PSWidgetBuilderSummaryData> expectedSums = new ArrayList<PSWidgetBuilderSummaryData>();
            for (PSWidgetBuilderDefinitionData data : definitions)
            {
                expectedSums.add(new PSWidgetBuilderSummaryData(data));
            }
            assertEquals(expectedSums, sums);
            
            // test fields & html
            List<PSWidgetBuilderFieldData> fields = definition.getFieldsList().getFields();
            PSWidgetBuilderFieldData textField = new PSWidgetBuilderFieldData();
            textField.setName("textField");
            textField.setLabel("Text Field");
            textField.setType(PSWidgetBuilderFieldData.FieldType.TEXT.toString());
            fields.add(textField);
            
            PSWidgetBuilderFieldData areaField = new PSWidgetBuilderFieldData();
            areaField.setName("textArea");
            areaField.setLabel("Text Area");
            areaField.setType(PSWidgetBuilderFieldData.FieldType.TEXT_AREA.toString());
            fields.add(areaField);
            
            PSWidgetBuilderFieldData dateField = new PSWidgetBuilderFieldData();
            dateField.setName("dateField");
            dateField.setLabel("Date Field");
            dateField.setType(PSWidgetBuilderFieldData.FieldType.DATE.toString());
            fields.add(dateField);
            
            PSWidgetBuilderFieldData richField = new PSWidgetBuilderFieldData();
            richField.setName("richText");
            richField.setLabel("Rich Text");
            richField.setType(PSWidgetBuilderFieldData.FieldType.RICH_TEXT.toString());
            fields.add(richField);
            
            PSWidgetBuilderFieldData imgField = new PSWidgetBuilderFieldData();
            imgField.setName("imgField");
            imgField.setLabel("Image Field");
            imgField.setType(PSWidgetBuilderFieldData.FieldType.IMAGE.toString());
            fields.add(imgField);
            
            String html = "<ul>";
            for (PSWidgetBuilderFieldData field : fields)
            {
                
                if (field.getType().equals(FieldType.IMAGE.name()))
                {
                    html += "<li><img src=\"$" + field.getName() + "_path\"/></li>";
                }
                else
                {
                    html += "<li>$" + field.getName() + "</li>";
                }
            }
            
            html += "</ul>";
            definition.setWidgetHtml(html);
            
            PSWidgetBuilderResourceListData jsFiles = new PSWidgetBuilderResourceListData();
            List<String> files = jsFiles.getResourceList();
            files.add("/foo/bar.js");
            files.add("/foo/bar2.js");
            definition.setJsFileList(jsFiles);
            
            PSWidgetBuilderResourceListData cssFiles = new PSWidgetBuilderResourceListData();
            files = cssFiles.getResourceList();
            files.add("/foo/bar.js");
            files.add("/foo/bar2.js");
            definition.setCssFileList(cssFiles);
            
            PSWidgetBuilderDefinitionData savedDef = roundTrip(ids, definition);
            assertEquals(definition, savedDef);
            
            service.deleteWidgetBuilderDefinition(Long.parseLong(definition.getId()));
            ids.remove(definition.getId());
            service.deleteWidgetBuilderDefinition(Long.parseLong(definition2.getId()));
            ids.remove(definition.getId());
            
            definitions = service.loadAll();
            assertTrue(currentSize == definitions.size());
        }
        catch (Exception e)
        {
            log.error(e.getMessage());
            log.debug(e.getMessage(), e);
            fail("Exception: " + e.getLocalizedMessage());
        }
        finally
        {
            if (badId > 0)
                deleteQuietly(badId);
            
            for (String defId : ids)
            {
                deleteQuietly(Long.parseLong(defId));
            }
        }
    }

    private void deleteQuietly(long id)
    {
        try
        {
            service.deleteWidgetBuilderDefinition(id);
        }
        catch (Exception e)
        {
            System.out.println(e.getLocalizedMessage());
        }
    }

    private PSWidgetBuilderDefinitionData roundTrip(Set<String> ids, PSWidgetBuilderDefinitionData definition)
    {
        PSWidgetBuilderValidationResults results;
        results = service.saveWidgetBuilderDefinition(definition);
        assertTrue(results.getResults().isEmpty());
        ids.add(String.valueOf(results.getDefinitionId()));
        definition = service.loadWidgetDefinition(results.getDefinitionId());
        assertNotNull(definition);
        return definition;
    }
    
    @Test
    public void testPackagingandDeploy()
    {
        long id = 0;
        try
        {
            PSWidgetBuilderDefinitionData definition = new PSWidgetBuilderDefinitionData();
            definition.setDescription("a description");
            definition.setLabel("alabel");
            definition.setPrefix("perc");
            definition.setPublisherUrl("http://www.percussion.com");
            definition.setVersion("3.2.1");
            definition.setAuthor("Dr. Caligari");
            definition.setResponsive(true);
            definition.getFieldsList().setFields(PSContentTypeFileTransformerTest.setupPackageSpec().getFields());

            String html = "<ul>";
            for (PSWidgetBuilderFieldData field : definition.getFieldsList().getFields())
            {
                if (field.getType().equals(FieldType.IMAGE.name()))
                {
                    html += "<li><img src=\"$" + field.getName() + "_path\" title=\"$"+ field.getName() + "_title\" alt=\"$" + field.getName() + "_alt_text\" /></li>";
                }
                else
                {
                    html += "<li>$" + field.getName() + "</li>";
                }
            }
            
            html += "</ul>";
            definition.setWidgetHtml(html);
            
            definition = service.loadWidgetDefinition(service.saveWidgetBuilderDefinition(definition).getDefinitionId());
                    
            id = Long.parseLong(definition.getId());
            service.deployWidget(id);
        }
        finally
        {
            if (id > 0)
                service.deleteWidgetBuilderDefinition(id);
        }
        
    }

}
