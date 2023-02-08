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
import com.percussion.pagemanagement.data.PSWidgetDefinition.UserPref;
import com.percussion.pagemanagement.service.IPSWidgetService;

/**
 * 
 * @author adamgent
 *
 */
public class PSWidgetUserPropertiesValidator extends PSWidgetPropertiesValidator<UserPref>
{

    public PSWidgetUserPropertiesValidator(IPSWidgetService widgetService)
    {
        super(widgetService);
    }

    @Override
    protected Map<String, Object> getProperties(PSWidgetItem widgetItem)
    {
        return widgetItem.getProperties();
    }

    @Override
    protected Map<String, UserPref> getPropertyDefinitions(PSWidgetDefinition definition)
    {
        return PSWidgetUtils.getUserPrefs(definition);
    }

}

