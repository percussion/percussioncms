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

package com.percussion.delivery.metadata.extractor.data;

import com.percussion.delivery.metadata.IPSMetadataEntry;
import com.percussion.delivery.metadata.IPSMetadataProperty;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents metadata for a published page on the deleivery server.
 * 
 * @author miltonpividori
 * 
 */
@XmlType(propOrder={"name"})
public class PSMetadataEntry implements Serializable, IPSMetadataEntry
{
    private String pagepath;

    private String name;

    private String folder;

    private String linktext;

    private String type;

    private String site;
    
    @XmlElementWrapper(name="property")
    @XmlElement(type=PSMetadataProperty.class)
    private Set<IPSMetadataProperty> properties = new HashSet<>();

    public PSMetadataEntry()
    {

    }

    /**
     * Ctor
     * 
     * @param name the file name, cannot be <code>null</code> or empty.
     * @param folder the folder path of the containing folder without the site
     *            folder. Cannot be <code>null</code> or empty.
     * @param pagepath the path of the file including sitefolder. This is used
     *            as a unique key for the entry. Cannot be <code>null</code> or
     *            empty.
     * @param type
     */
    public PSMetadataEntry(String name, String folder, String pagepath, String type, String site)
    {
        if (name == null || name.length() == 0)
            throw new IllegalArgumentException("name cannot be null or empty");
        if (folder == null || folder.length() == 0)
            throw new IllegalArgumentException("folder cannot be null or empty");
        if (pagepath == null || pagepath.length() == 0)
            throw new IllegalArgumentException("pagepath cannot be null or empty");
        if (site == null || site.length() == 0)
            throw new IllegalArgumentException("site cannot be null or empty");
        this.name = name;
        this.folder = folder;
        this.type = type;
        this.pagepath = pagepath;
        this.site = site;
    }

    /* (non-Javadoc)
	 * @see com.percussion.delivery.metadata.extractor.data.IPSMetadataEntry#getName()
	 */
    public String getName()
    {
        return name;
    }

    /* (non-Javadoc)
	 * @see com.percussion.delivery.metadata.extractor.data.IPSMetadataEntry#setName(java.lang.String)
	 */
    public void setName(String name)
    {
        this.name = name;
    }

    /* (non-Javadoc)
	 * @see com.percussion.delivery.metadata.extractor.data.IPSMetadataEntry#getFolder()
	 */
    public String getFolder()
    {
        return folder;
    }

    /* (non-Javadoc)
	 * @see com.percussion.delivery.metadata.extractor.data.IPSMetadataEntry#setFolder(java.lang.String)
	 */
    public void setFolder(String folder)
    {
        this.folder = folder;
    }

    /* (non-Javadoc)
	 * @see com.percussion.delivery.metadata.extractor.data.IPSMetadataEntry#getPagepath()
	 */
    public String getPagepath()
    {
        return pagepath;
    }

    /* (non-Javadoc)
	 * @see com.percussion.delivery.metadata.extractor.data.IPSMetadataEntry#setPagepath(java.lang.String)
	 */
    public void setPagepath(String path)
    {
        this.pagepath = path;
    }

    /* (non-Javadoc)
	 * @see com.percussion.delivery.metadata.extractor.data.IPSMetadataEntry#getLinktext()
	 */
    public String getLinktext()
    {
        return linktext;
    }

    /* (non-Javadoc)
	 * @see com.percussion.delivery.metadata.extractor.data.IPSMetadataEntry#setLinktext(java.lang.String)
	 */
    public void setLinktext(String linktext)
    {
        this.linktext = linktext;
    }

    /* (non-Javadoc)
	 * @see com.percussion.delivery.metadata.extractor.data.IPSMetadataEntry#getType()
	 */
    public String getType()
    {
        return type;
    }

    /* (non-Javadoc)
	 * @see com.percussion.delivery.metadata.extractor.data.IPSMetadataEntry#setType(java.lang.String)
	 */
    public void setType(String type)
    {
        this.type = type;
    }

    /* (non-Javadoc)
	 * @see com.percussion.delivery.metadata.extractor.data.IPSMetadataEntry#getSite()
	 */
    public String getSite()
    {
        return site;
    }

    /* (non-Javadoc)
	 * @see com.percussion.delivery.metadata.extractor.data.IPSMetadataEntry#setSite(java.lang.String)
	 */
    public void setSite(String site)
    {
        this.site = site;
    }

    /* (non-Javadoc)
	 * @see com.percussion.delivery.metadata.extractor.data.IPSMetadataEntry#getProperties()
	 */
    public Set<IPSMetadataProperty> getProperties()
    {
        return properties;
    }

    /* (non-Javadoc)
	 * @see com.percussion.delivery.metadata.extractor.data.IPSMetadataEntry#setProperties(java.util.Set)
	 */
    public void setProperties(Set<IPSMetadataProperty> properties)
    {
        this.properties = properties;
    }

    public void addProperty(IPSMetadataProperty prop)
    {
       if(properties == null)
           properties = new HashSet<>();
       properties.add(prop);        
    }
    
    public void clearProperties()
    {
        if(properties != null)
            properties.clear();
    }
    
    
}
