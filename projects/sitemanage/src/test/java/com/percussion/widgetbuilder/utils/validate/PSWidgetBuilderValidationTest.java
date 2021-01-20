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
package com.percussion.widgetbuilder.utils.validate;

import static org.junit.Assert.*;

import com.percussion.widgetbuilder.data.PSWidgetBuilderDefinitionData;
import com.percussion.widgetbuilder.data.PSWidgetBuilderFieldData;
import com.percussion.widgetbuilder.data.PSWidgetBuilderFieldData.FieldType;
import com.percussion.widgetbuilder.data.PSWidgetBuilderFieldsListData;
import com.percussion.widgetbuilder.data.PSWidgetBuilderValidationResult;
import com.percussion.widgetbuilder.data.PSWidgetBuilderValidationResult.ValidationCategory;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * @author JaySeletz
 *
 */
public class PSWidgetBuilderValidationTest
{

    private static final String VERSION_50_CHARS = "1234567890123456.1234567890123456.1234567890123456";
    private static final String CHARS_50 = "A1234567890123456789012345678901234567890123456789";
    private static final String CHARS_100 = "A123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789";
    private static final String CHARS_255 = "A12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234";
    private static final String CHARS_1024 = CHARS_255 + CHARS_255 + CHARS_255 + CHARS_255 + "A012";


    @Test
    public void testGeneral()
    {
        PSWidgetBuilderDefinitionData data = new PSWidgetBuilderDefinitionData();
        List<PSWidgetBuilderDefinitionData> current = new ArrayList<PSWidgetBuilderDefinitionData>();
        List<PSWidgetBuilderValidationResult> results = PSWidgetBuilderGeneralValidator.validate(data, current);
        assertFalse(results.isEmpty());
        assertTrue(hasError(ValidationCategory.GENERAL, "label", results));
        assertTrue(hasError(ValidationCategory.GENERAL, "prefix", results));
        assertTrue(hasError(ValidationCategory.GENERAL, "author", results));
        assertTrue(hasError(ValidationCategory.GENERAL, "publisherUrl", results));
        assertTrue(hasError(ValidationCategory.GENERAL, "version", results));
        
        data.setLabel("");
        data.setPrefix("");
        data.setAuthor("");
        data.setPublisherUrl("");
        data.setVersion("");
        results = PSWidgetBuilderGeneralValidator.validate(data, current);
        assertFalse(results.isEmpty());
        assertTrue(hasError(ValidationCategory.GENERAL, "label", results));
        assertTrue(hasError(ValidationCategory.GENERAL, "prefix", results));
        assertTrue(hasError(ValidationCategory.GENERAL, "author", results));
        assertTrue(hasError(ValidationCategory.GENERAL, "publisherUrl", results));
        assertTrue(hasError(ValidationCategory.GENERAL, "version", results));
        
        data.setLabel("widget1");
        data.setPrefix("test");
        data.setAuthor("author");
        data.setPublisherUrl("www.test.com");
        data.setVersion("1.0.0");
        testGeneralValue(data, current, "label", true);
        
        data.setPrefix("test_");
        testGeneralValue(data, current, "prefix", false);
        data.setPrefix("test ");
        testGeneralValue(data, current, "prefix", false);
        data.setPrefix("test me");
        testGeneralValue(data, current, "prefix", false);
        data.setPrefix("1test");
        testGeneralValue(data, current, "prefix", false);
        data.setPrefix("test1");
        testGeneralValue(data, current, "prefix", true);
        data.setPrefix("test");
        testGeneralValue(data, current, "prefix", true);
        
        
        data.setVersion("foo");
        testGeneralValue(data, current, "version", false);
        
        data.setVersion("1.0");
        testGeneralValue(data, current, "version", false);
        
        data.setVersion("1");
        testGeneralValue(data, current, "version", false);
        
        data.setVersion("2.10.100");
        testGeneralValue(data, current, "version", true);
        
        data.setLabel("1widget");
        testGeneralValue(data, current, "label", false);
        
        data.setLabel(" widget");
        testGeneralValue(data, current, "label", false);

        data.setLabel("widget1");
        testGeneralValue(data, current, "label", true);

        
        // copy
        PSWidgetBuilderDefinitionData data2 = new PSWidgetBuilderDefinitionData(PSWidgetBuilderDefinitionData.createDaoObject(data));
        
        // test dupe name
        current.add(data2);
        testGeneralValue(data, current, "label", false);
        data.setPrefix("foo");
        testGeneralValue(data, current, "label", true);
        
        data.setPrefix(data2.getPrefix());
        data2.setId("1");
        testGeneralValue(data, current, "label", false);
        
        // set id the same so it's the same object
        data.setId(data2.getId());
        testGeneralValue(data, current, "label", true);
        
        // test length
        data.setLabel(CHARS_100);
        data.setPrefix(CHARS_100);
        data.setAuthor(CHARS_100);
        data.setPublisherUrl(CHARS_100 );
        data.setVersion(VERSION_50_CHARS);
        data.setDescription(CHARS_1024);
        results = PSWidgetBuilderGeneralValidator.validate(data, current);
        assertTrue(results.isEmpty());
        
        data.setLabel(CHARS_100 + "a");
        data.setPrefix(CHARS_100 + "a");
        data.setAuthor(CHARS_100 + "a");
        data.setPublisherUrl(CHARS_100 + "a");
        data.setVersion(VERSION_50_CHARS + "0");
        data.setDescription(CHARS_1024 + "a");
        results = PSWidgetBuilderGeneralValidator.validate(data, current);
        assertFalse(results.isEmpty());
        assertTrue(hasError(ValidationCategory.GENERAL, "label", results));
        assertTrue(hasError(ValidationCategory.GENERAL, "prefix", results));
        assertTrue(hasError(ValidationCategory.GENERAL, "author", results));
        assertTrue(hasError(ValidationCategory.GENERAL, "publisherUrl", results));
        assertTrue(hasError(ValidationCategory.GENERAL, "version", results));
        assertTrue(hasError(ValidationCategory.GENERAL, "description", results));

    }

    @Test
    public void testFields()
    {
        PSWidgetBuilderFieldsListData fields = new PSWidgetBuilderFieldsListData();
        List<PSWidgetBuilderFieldData> fieldList = new ArrayList<PSWidgetBuilderFieldData>();
        fields.setFields(fieldList);
        
        PSWidgetBuilderFieldData field = new PSWidgetBuilderFieldData();
        fieldList.add(field);
        
        List<PSWidgetBuilderValidationResult> results = PSWidgetBuilderFieldsValidator.validate(fields);
        assertEquals(3, results.size());
        assertTrue(hasError(ValidationCategory.CONTENT, "name", results));
        assertTrue(hasError(ValidationCategory.CONTENT, "label", results));
        assertTrue(hasError(ValidationCategory.CONTENT, "type", results));
        
        field.setLabel("label");
        field.setName("myname");
        field.setType(FieldType.TEXT.name());
        testField(fields, "name", true);
        
        field.setName("name with space");
        testField(fields, "name", false);
        
        field.setName("1name");
        testField(fields, "name", false);

        field.setName("name_foo");
        testField(fields, "name", false);

        field.setName("name.");
        testField(fields, "name", false);
        
        field.setName("myname");
        testField(fields, "name", true);
        
        field.setName("prodName");
        testField(fields, "name", true);
        
        field.setName("pName");
        testField(fields, "name", false);
        
        field.setName("pN");
        testField(fields, "name", false);

        field.setName("N\u00FAmerodesegmentos");
        testField(fields, "name", false);
        
        field.setName("p");
        testField(fields, "name", true);

        field.setName("ADD");
        testField(fields,"name",false);
          
        field.setName("add");
        testField(fields,"name",false);
        
        //set field to something that will pass
        field.setName("p");      
        
        PSWidgetBuilderFieldData field2 = new PSWidgetBuilderFieldData();
        fieldList.add(field2);
        field2.setLabel("label2");
        field2.setName("name2");
        field2.setType(FieldType.RICH_TEXT.name());
        testField(fields, "name", true);
        
        field2.setName(field.getName());
        testField(fields, "name", false);
        
        field2.setName(" name2");
        field2.setLabel("");
        
        results = PSWidgetBuilderFieldsValidator.validate(fields);
        assertEquals(2, results.size());
        assertTrue(hasError(ValidationCategory.CONTENT, "name", results));
        assertTrue(hasError(ValidationCategory.CONTENT, "label", results));
        
        fieldList.clear();
        fieldList.add(field);
        field.setName(CHARS_50);
        field.setLabel(CHARS_50);
        testField(fields, "name", true);
        
        field.setName(CHARS_50 + "a");
        field.setLabel(CHARS_50 + "a");
        assertTrue(hasError(ValidationCategory.CONTENT, "name", results));
        assertTrue(hasError(ValidationCategory.CONTENT, "label", results));
    }
    
    private void testGeneralValue(PSWidgetBuilderDefinitionData data, List<PSWidgetBuilderDefinitionData> current, String name, boolean isValid)
    {
        List<PSWidgetBuilderValidationResult> results = PSWidgetBuilderGeneralValidator.validate(data, current);
        if (isValid)
            assertTrue(results.isEmpty());
        else
        {
            assertEquals(1, results.size());
            assertTrue(hasError(ValidationCategory.GENERAL, name, results));
        }
    }

    private void testField(PSWidgetBuilderFieldsListData fields, String name, boolean isValid)
    {
        List<PSWidgetBuilderValidationResult> results = PSWidgetBuilderFieldsValidator.validate(fields);
        
        if (isValid)
            assertTrue(results.isEmpty());
        else
        {
            assertEquals(1, results.size());
            assertTrue(hasError(ValidationCategory.CONTENT, name, results));
        }
    }
    

    private boolean hasError(ValidationCategory category, String name, List<PSWidgetBuilderValidationResult> results)
    {
        for (PSWidgetBuilderValidationResult result : results)
        {
            if (category.name().equals(result.getCategory()) && name.equals(result.getName()))
            {
                return true;
            }
        }
        return false;
    }

}
