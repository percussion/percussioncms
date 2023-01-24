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
