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

import com.percussion.widgetbuilder.data.PSWidgetBuilderFieldData;
import com.percussion.widgetbuilder.data.PSWidgetBuilderFieldData.FieldType;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.Validate;

import java.io.IOException;
import java.text.MessageFormat;

/**
 * @author JaySeletz
 *
 */
public class PSRichTextFieldValueGenerator extends PSBasicFieldValueGenerator implements IPSBindingGenerator
{
    private static String template;
    
    @Override
    public boolean accept(PSWidgetBuilderFieldData field)
    {
        return FieldType.RICH_TEXT.name().equals(field.getType());
    }


    @Override
    public String generateBinding(PSWidgetBuilderFieldData field)
    {
        Validate.isTrue(accept(field));
        return MessageFormat.format(getTemplate(), field.getName());
    }
    
    /**
     * Get the cached template, Lazily loading from a resource file and caching on first access.
     * 
     * @return The template, not <code>null</code>.
     */
    private String getTemplate()
    {
        if (template == null)
        {

            try
            {
                template = IOUtils.toString(this.getClass().getResourceAsStream("RichTextFieldTemplate.txt"));
            }
            catch (IOException e)
            {
                throw new RuntimeException("Failed to load rich text field binding template:" + e.getMessage(),e);
            }
        }
        
        return template;
        
    }
}
