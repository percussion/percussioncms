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

import com.percussion.share.data.PSAbstractPersistantObject;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.Validate;

/**
 * Additional information about a widget from the package that installed it.
 * The Id is the widget name.
 * 
 * @author JaySeletz
 *
 */
@XmlRootElement(name = "WidgetPackageInfo")
public class PSWidgetPackageInfo extends PSAbstractPersistantObject
{
    private String id;
    private String widgetName;
    private String providerUrl;
    private String version;

    @Override
    public String getId()
    {
        return id;
    }

    @Override
    public void setId(String id)
    {
        Validate.notEmpty(id);
        this.id = id;
    }
    
        public String getWidgetName()
    {
        return widgetName;
    }

    public void setWidgetName(String widgetName)
    {
        this.widgetName = widgetName;
    }

    /**
     * Get the provider url from the package.
     * 
     * @return The url, may be <code>null<code/> or empty.
     */
    public String getProviderUrl()
    {
        return providerUrl;
    }

    public void setProviderUrl(String providerUrl)
    {
        this.providerUrl = providerUrl;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion(String version)
    {
        this.version = version;
    }
    
    
}
