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
package com.percussion.pagemanagement.service.impl;

import java.util.Map;

import com.percussion.pagemanagement.data.PSWidgetDefinition;
import com.percussion.pagemanagement.data.PSWidgetItem;
import com.percussion.pagemanagement.data.PSWidgetDefinition.CssPref;
import com.percussion.pagemanagement.service.IPSWidgetService;

/**
 * Validates the Css properties of a widget.
 * 
 * @see PSWidgetItem#getCssProperties()
 * @author adamgent
 *
 */
public class PSWidgetCssPropertiesValidator extends PSWidgetPropertiesValidator<CssPref>
{

    public PSWidgetCssPropertiesValidator(IPSWidgetService widgetService)
    {
        super(widgetService);
    }

    @Override
    protected Map<String, Object> getProperties(PSWidgetItem widgetItem)
    {
        return widgetItem.getCssProperties();
    }

    @Override
    protected Map<String, CssPref> getPropertyDefinitions(PSWidgetDefinition definition)
    {
        return PSWidgetUtils.getCssPrefs(definition);
    }

}

