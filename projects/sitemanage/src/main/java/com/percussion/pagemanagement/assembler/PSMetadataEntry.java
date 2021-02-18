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

/**
 * 
 */
package com.percussion.pagemanagement.assembler;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Administrator
 * 
 */
public class PSMetadataEntry
{
    private String pagepath;

    private String name;

    private String folder;

    private String linktext;

    private String type;

    private String site;

    private Set<PSMetadataProperty> properties = new HashSet<>();

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

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @return the folder
     */
    public String getFolder()
    {
        return folder;
    }

    /**
     * @param folder the folder to set
     */
    public void setFolder(String folder)
    {
        this.folder = folder;
    }

    /**
     * @return the page path
     */
    public String getPagepath()
    {
        return pagepath;
    }

    /**
     * @param path the pagepath to set
     */
    public void setPagepath(String path)
    {
        this.pagepath = path;
    }

    /**
     * @return the linktext
     */
    public String getLinktext()
    {
        return linktext;
    }

    /**
     * @param linktext the linktext to set
     */
    public void setLinktext(String linktext)
    {
        this.linktext = linktext;
    }

    /**
     * @return the type
     */
    public String getType()
    {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type)
    {
        this.type = type;
    }

    /**
     * @return the site
     */
    public String getSite()
    {
        return site;
    }

    /**
     * @param site the site to set
     */
    public void setSite(String site)
    {
        this.site = site;
    }

    /**
     * @return the properties
     */
    public Set<PSMetadataProperty> getProperties()
    {
        return properties;
    }

    /**
     * @param properties the properties to set
     */
    public void setProperties(Set<PSMetadataProperty> properties)
    {
        this.properties = properties;
    }

    public void addProperty(PSMetadataProperty prop)
    {
        prop.setMetadataEntry(this);
        this.properties.add(prop);
    }
}