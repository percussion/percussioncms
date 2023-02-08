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
