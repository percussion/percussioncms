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

