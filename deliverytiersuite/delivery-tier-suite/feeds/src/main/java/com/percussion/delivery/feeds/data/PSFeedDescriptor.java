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
package com.percussion.delivery.feeds.data;

/**
 * A transfer object impl of the IPSFeedDescriptor interface.
 *
 */
public class PSFeedDescriptor implements IPSFeedDescriptor
{

    private String name;
    private String site;
    private String description;
    private String link;
    private String title;
    private String query;


    private String type;

    /* (non-Javadoc)
     * @see com.percussion.feeds.data.IPSFeedDescriptor#getDescription()
     */
    public String getDescription()
    {
        return description;
    }

    /* (non-Javadoc)
     * @see com.percussion.feeds.data.IPSFeedDescriptor#getLink()
     */
    public String getLink()
    {
        return link;
    }

    /* (non-Javadoc)
     * @see com.percussion.feeds.data.IPSFeedDescriptor#getName()
     */
    public String getName()
    {
        return name;
    }

    /* (non-Javadoc)
     * @see com.percussion.feeds.data.IPSFeedDescriptor#getQuery()
     */
    public String getQuery()
    {
        return query;
    }

    /* (non-Javadoc)
     * @see com.percussion.feeds.data.IPSFeedDescriptor#getSite()
     */
    public String getSite()
    {
        return site;
    }

    /* (non-Javadoc)
     * @see com.percussion.feeds.data.IPSFeedDescriptor#getTitle()
     */
    public String getTitle()
    {
        return title;
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
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @param site the site to set
     */
    public void setSite(String site)
    {
        this.site = site;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description)
    {
        this.description = description;
    }

    /**
     * @param link the link to set
     */
    public void setLink(String link)
    {
        this.link = link;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title)
    {
        this.title = title;
    }

    /**
     * @param query the query to set
     */
    public void setQuery(String query)
    {
        this.query = query;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if(obj == null || !(obj instanceof IPSFeedDescriptor))
            return false;
        IPSFeedDescriptor desc = (IPSFeedDescriptor)obj;
        return (desc.getSite().equals(this.site) && desc.getName().equals(this.name));
    }

    public PSFeedDescriptor(){
        super();
    }

}
