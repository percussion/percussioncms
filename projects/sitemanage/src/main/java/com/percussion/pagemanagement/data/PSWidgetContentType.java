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

package com.percussion.pagemanagement.data;

import com.fasterxml.jackson.annotation.JsonRootName;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * POJO class to hold the widget and content type information. 
 */
@XmlRootElement(name = "WidgetContentType")
@JsonRootName("WidgetContentType")
public class PSWidgetContentType
{
    public String getWidgetId()
    {
        return widgetId;
    }
    public void setWidgetId(String widgetId)
    {
        this.widgetId = widgetId;
    }
    public String getWidgetLabel()
    {
        return widgetLabel;
    }
    public void setWidgetLabel(String widgetLabel)
    {
        this.widgetLabel = widgetLabel;
    }
    public String getContentTypeId()
    {
        return contentTypeId;
    }
    public void setContentTypeId(String contentTypeId)
    {
        this.contentTypeId = contentTypeId;
    }
    public String getContentTypeName()
    {
        return contentTypeName;
    }
    public void setContentTypeName(String contentTypeName)
    {
        this.contentTypeName = contentTypeName;
    }
    public String getIcon()
    {
        return icon;
    }
    public void setIcon(String icon)
    {
        this.icon = icon;
    }
    String widgetId;
    String widgetLabel;
    String contentTypeId;
    String contentTypeName;
    String icon;
}
