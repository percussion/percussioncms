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
package com.percussion.widgetbuilder.data;

import static com.percussion.share.test.PSDataObjectTestUtils.assertXmlSerialization;

import static org.junit.Assert.assertEquals;

import com.percussion.share.dao.PSSerializerUtils;
import com.percussion.share.data.PSDataObjectTestCase;

import java.util.List;

import org.junit.Test;

/**
 * @author JaySeletz
 *
 */
public class PSWidgetBuilderDefinitionDataTest extends PSDataObjectTestCase<PSWidgetBuilderDefinitionData>
{

    /* (non-Javadoc)
     * @see com.percussion.share.data.PSDataObjectTestCase#getObject()
     */
    @Override
    public PSWidgetBuilderDefinitionData getObject() throws Exception
    {
        PSWidgetBuilderDefinitionData definition = new PSWidgetBuilderDefinitionData();
        definition.setDescription("a description");
        definition.setLabel("a label");
        definition.setPrefix("perc");
        definition.setPublisherUrl("http://www.percussion.com");
        definition.setVersion("42");
        definition.setAuthor("Dr. Caligari");
        definition.setId("1");
        definition.setResponsive(true);
        
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
            html += "<li>$" + field.getName() + "</li>";
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
        
        return definition;
    }

    @Test
    public void testJsonSerialization() throws Exception
    {
        String json = PSSerializerUtils.getJsonFromObject(object);        
    }
    
    @Test
    public void testToFromDao() throws Exception
    {
        PSWidgetBuilderDefinitionData data = new PSWidgetBuilderDefinitionData(PSWidgetBuilderDefinitionData.createDaoObject(object));
        assertEquals(object, data);
    }
    
    @Test
    public void testSummaryData() throws Exception
    {
        PSWidgetBuilderSummaryData sum = new PSWidgetBuilderSummaryData(object);
        assertXmlSerialization(sum);
    }
}
