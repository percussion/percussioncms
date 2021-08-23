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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.delivery.caching.data;

import com.percussion.delivery.caching.data.xmladapters.PSMapAdapter;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PSCacheProvider", propOrder = {
    "properties"
})
public class PSCacheProvider
{
    @XmlElement(name = "Properties")
    @XmlJavaTypeAdapter(PSMapAdapter.class)
    protected Map<String, String> properties = new HashMap<>();

    @XmlAttribute
    protected String plugin;

    public Map<String, String> getProperties()
    {
        return properties;
    }

    public String getProperty(String key)
    {
        if (properties == null || properties.isEmpty())
            return null;
        return properties.get(key);
    }

    public void setProperties(Map<String, String> value)
    {
        this.properties = value;
    }

    public String getPlugin()
    {
        return plugin;
    }

    public void setPlugin(String value)
    {
        this.plugin = value;
    }

}
