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

import java.io.IOException;
import java.text.MessageFormat;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.Validate;

/**
 * @author Stephen Bolton
 *
 */
public class PSPageFieldValueGenerator extends PSBasicFieldValueGenerator implements IPSBindingGenerator
{
    private static String template;
    
    /* (non-Javadoc)
     * @see com.percussion.widgetbuilder.utils.IPSBindingGenerator#accept(com.percussion.widgetbuilder.data.PSWidgetBuilderFieldData)
     */
    @Override
    public boolean accept(PSWidgetBuilderFieldData field)
    {
        return FieldType.PAGE.name().equals(field.getType());
    }

    /* (non-Javadoc)
     * @see com.percussion.widgetbuilder.utils.IPSBindingGenerator#generateBinding(com.percussion.widgetbuilder.data.PSWidgetBuilderFieldData)
     */
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
                template = IOUtils.toString(this.getClass().getResourceAsStream("PageFieldTemplate.txt"));
            }
            catch (IOException e)
            {
                throw new RuntimeException("Failed to load file field binding template: " + e.getLocalizedMessage(), e);
            }
        }
        
        return template;
        
    }

}
