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
package com.percussion.delivery.metadata.data;

import com.percussion.delivery.metadata.IPSMetadataProperty;
import com.percussion.utils.date.PSConcurrentDateFormat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Represents a metadata entry in the REST layer. It's used to return
 * exactly what's needed.
 *
 */
public class PSMetadataRestEntry
{
    /**
     * Date format used for string serialized date. 2011-01-21T09:36:05
     */
    PSConcurrentDateFormat dateFormat = new PSConcurrentDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    
    private String pagepath;

    private String name;

    private String folder;

    private String linktext;

    private String type;

    private String site;

    private HashMap<String, Object> properties = new HashMap<>();

    public String getPagepath()
    {
        return pagepath;
    }

    public void setPagepath(String pagepath)
    {
        this.pagepath = pagepath;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getFolder()
    {
        return folder;
    }

    public void setFolder(String folder)
    {
        this.folder = folder;
    }

    public String getLinktext()
    {
        return linktext;
    }

    public void setLinktext(String linktext)
    {
        this.linktext = linktext;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public String getSite()
    {
        return site;
    }

    public void setSite(String site)
    {
        this.site = site;
    }

    public HashMap<String, Object> getProperties()
    {
        return properties;
    }

    public void setProperties(HashMap<String, Object> properties)
    {
        this.properties = properties;
    }

    /**
     * Adds a PSMetadataProperty to the Map 'properties', so it's converted
     * to String as desired with this format:
     * <code>
     * {
     *      "propertyName" : "propertyValue"
     * }
     * </code>.
     * @param metadataProperty A PSMetadataProperty instance that will be
     * added to the 'properties' Map.
     */
    public void addMetadataProperty(IPSMetadataProperty metadataProperty)
    {
    	String newValue = "";
        if (metadataProperty.getValuetype().equals(IPSMetadataProperty.VALUETYPE.NUMBER))
        {
        	newValue = metadataProperty.getNumbervalue().toString();
        }
        else if (metadataProperty.getValuetype().equals(IPSMetadataProperty.VALUETYPE.DATE))
        {
        	newValue = dateFormat.format(metadataProperty.getDatevalue());
        }
        else
        {
        	newValue = metadataProperty.getStringvalue();
        }
        if (!this.properties.containsKey(metadataProperty.getName())){
    		this.properties.put(metadataProperty.getName(),	newValue);	
    	}
    	else{
    		Object value = this.properties.get(metadataProperty.getName());
    		if (value instanceof String)
    		{
    			List<String> multiValued = new ArrayList<>();
    			multiValued.add((String)value);
    			multiValued.add(newValue);
    			this.properties.put(metadataProperty.getName(), multiValued);
    		}
    		else
    		{
    			((List<String>)value).add(newValue);
    			this.properties.put(metadataProperty.getName(), value);
    		}
    		
    	}
    }
}
