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
package com.percussion.widgetbuilder.utils;

import static org.junit.Assert.*;

import com.percussion.widgetbuilder.data.PSWidgetBuilderFieldData;
import com.percussion.widgetbuilder.data.PSWidgetBuilderFieldData.FieldType;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

/**
 * @author JaySeletz
 *
 */
public class PSWidgetPackageResolverTest
{

    /**
     * Test method for {@link com.percussion.widgetbuilder.utils.PSWidgetPackageResolver#generateFieldBindings(java.util.List)}.
     */
    @Test
    public void testGenerateFieldBindings() throws Exception
    {
        PSWidgetPackageSpec packageSpec = new PSWidgetPackageSpec("test", "www.test.com", "Custom Widget 2", "a 2nd test widget", "1.0.0", "3.1.0");
        List<PSWidgetBuilderFieldData> fields = new ArrayList<PSWidgetBuilderFieldData>();
        PSWidgetBuilderFieldData field;
        field = new PSWidgetBuilderFieldData();
        field.setName("Author");
        field.setLabel(field.getName());
        field.setType(FieldType.TEXT.name());
        fields.add(field);
        
        packageSpec.setFields(fields);
        
        String html = "<div>$Author</div>";
        packageSpec.setWidgetHtml(html);
        PSWidgetPackageResolver resolver = new PSWidgetPackageResolver(packageSpec);
        
        String expected = "$Author = $assetItem.getNode().getProperty('Author').String;\n";
        assertEquals(expected, resolver.resolveToken("FIELD_BINDINGS"));

        assertEquals(html, resolver.resolveToken("WIDGET_HTML"));
        
        field = new PSWidgetBuilderFieldData();
        field.setName("Image");
        field.setLabel(field.getName());
        field.setType(FieldType.IMAGE.name());
        fields.clear();
        fields.add(field);
        resolver = new PSWidgetPackageResolver(packageSpec);
        expected = IOUtils.toString(this.getClass().getResourceAsStream("expectedFieldBindings.txt"));
        assertEquals(expected.trim(), resolver.resolveToken("FIELD_BINDINGS").trim());
        
    }

    /**
     * Test method for {@link com.percussion.widgetbuilder.utils.PSWidgetPackageResolver#resolveToken(java.lang.String)}.
     */
    @Test
    public void testResolveToken()
    {
        PSWidgetPackageSpec packageSpec = new PSWidgetPackageSpec("test", "www.test.com", "Custom Widget 2", "a 2nd test widget", "1.0.0", "3.1.0");
        PSWidgetPackageResolver resolver = new PSWidgetPackageResolver(packageSpec);
        assertResolves(resolver, "WIDGET_PKG_NAME", packageSpec.getPackageName());
        assertResolves(resolver, "PROPERCASE_WIDGET_NAME", packageSpec.getFullWidgetName());
        assertResolves(resolver, "WIDGET_VERSION", packageSpec.getWidgetVersion());
        assertResolves(resolver, "WIDGET_TITLE", packageSpec.getTitle());
        assertResolves(resolver, "WIDGET_DESCRIPTION", packageSpec.getDescription());
        assertResolves(resolver, "WIDGET_AUTHOR", packageSpec.getAuthorUrl());
        assertResolves(resolver, "WIDGET_AUTHOR_URL", packageSpec.getAuthorUrl());
        assertResolves(resolver, "UPPERCASE_WIDGET_NAME", packageSpec.getFullWidgetName().toUpperCase());
        assertResolves(resolver, "CM1_VERSION", packageSpec.getCm1Version());
        assertResolves(resolver, "IS_RESPONSIVE", Boolean.toString(packageSpec.isResponsive()));
        
        // test empty description
        packageSpec = new PSWidgetPackageSpec("test", "www.test.com", "Custom Widget 2", "", "1.0.0", "3.1.0");
        resolver = new PSWidgetPackageResolver(packageSpec);
        assertEquals(" ", resolver.resolveToken("WIDGET_DESCRIPTION"));
        
        // test responsive
        packageSpec.setResponsive(true);
        resolver = new PSWidgetPackageResolver(packageSpec);
        assertResolves(resolver, "IS_RESPONSIVE", Boolean.toString(packageSpec.isResponsive()));
    }

    /**
     * @param resolver 
     * @param token
     * @param expectedValue
     */
    private void assertResolves(PSWidgetPackageResolver resolver, String token, String expectedValue)
    {
        assertEquals(expectedValue, resolver.resolveToken(token));
    }

}
