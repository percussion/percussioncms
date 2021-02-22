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
