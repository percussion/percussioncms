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

package com.percussion.delivery.caching.data;

import com.percussion.delivery.caching.data.xmladapters.PSMapAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PSInvalidateRequest", propOrder = {
    "paths",
    "customProperties"
})
@XmlRootElement(name = "InvalidateRequest")
public class PSInvalidateRequest
{

    /**
     * Cache region name. Will have a corresponding entry in the cache configuration xml.
     */
    @XmlAttribute
    private String regionName;
    
    /**
     * Type of invalidation request, directory, urls or region. Defaults to urls.
     */
    @XmlAttribute
    private Type type = Type.URLS;
    
    /**
     * List of paths (urls) for invalidation, ignored for "region" type requests.
     * May be <code>null</code> or empty.
     */
    @XmlElementWrapper(name = "Paths")
    @XmlElement(name = "Path")
    private List<String> paths = new ArrayList<String>();
    
    /**
     * Map of custom properties. These will be specific for a particular plug-in 
     * implementation.
     */
    @XmlElement(name = "CustomProperties")
    @XmlJavaTypeAdapter(PSMapAdapter.class)
    private Map<String, String> customProperties = new HashMap<String, String>();

    /**
     * The invalidation request type.
     */
    public enum Type {
        DIRECTORY,
        URLS,
        REGION
    }
    
    /**
     * Clone this request 
     * @return
     */
    @Override
    public Object clone()
    {
        PSInvalidateRequest clone = new PSInvalidateRequest();
        clone.setRegionName(regionName);
        clone.setCustomProperties(customProperties);
        clone.setPaths(paths);
        clone.setType(type);
        return clone;
    }

    /**
     * @return the regionName
     */
    public String getRegionName()
    {
        return regionName;
    }

    /**
     * @param regionName the regionName to set
     */
    public void setRegionName(String regionName)
    {
        this.regionName = regionName;
    }

    /**
     * @return the type
     */
    public Type getType()
    {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(Type type)
    {
        this.type = type;
    }

    /**
     * @return the paths
     */
    public List<String> getPaths()
    {
        return paths;
    }

    /**
     * @param paths the paths to set
     */
    public void setPaths(List<String> paths)
    {
        if(paths == null)
            return;
        this.paths = paths;
    }

    /**
     * @return the customProperties
     */
    public Map<String, String> getCustomProperties()
    {
        return customProperties;
    }

    /**
     * @param customProperties the customProperties to set
     */
    public void setCustomProperties(Map<String, String> customProperties)
    {
        if(customProperties == null)
            return;
        this.customProperties = customProperties;
    }
}
