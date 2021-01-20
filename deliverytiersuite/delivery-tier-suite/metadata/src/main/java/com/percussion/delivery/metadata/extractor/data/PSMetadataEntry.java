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
    private Set<IPSMetadataProperty> properties = new HashSet<IPSMetadataProperty>();

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
           properties = new HashSet<IPSMetadataProperty>();
       properties.add(prop);        
    }
    
    public void clearProperties()
    {
        if(properties != null)
            properties.clear();
    }
    
    
}
