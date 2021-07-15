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
package com.percussion.delivery.feeds.services.rdbms;

import com.percussion.delivery.feeds.data.FeedType;
import com.percussion.delivery.feeds.data.IPSFeedDescriptor;

import java.io.Serializable;


import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author erikserating
 *
 */
@Entity
@Table(name = "PERC_FEED_DESCRIPTORS")
public class PSFeedDescriptor implements IPSFeedDescriptor, Serializable
{

    /**
	 * 
	 */
	private static final long serialVersionUID = 2756156009184830398L;

	@Id
    @Column(length = 255)
    private String site;
    
    @Id
    @Column(length = 255)
    private String name;
    
    @Basic
    @Column(length = 2000)
    private String title;
    
    @Basic
    @Column(length = 4000)
    private String description;
    
    @Basic
    @Column(length = 2000)
    private String link;
    
    @Basic
    @Column(length = 2000)
    private String type;
    
    @Basic
    @Column(length = 4000)
    private String query;
    
    public PSFeedDescriptor()
    {
        
    }
    
    public PSFeedDescriptor(IPSFeedDescriptor descriptor)
    {
        this.name = descriptor.getName();
        this.site = descriptor.getSite();
        this.title = descriptor.getTitle();
        this.description = descriptor.getDescription();
        this.link = descriptor.getLink();
        this.type = descriptor.getType();
        this.query = descriptor.getQuery();
    }
    
    /* (non-Javadoc)
     * @see com.percussion.feeds.data.IPSFeedDescriptor#getDescription()
     */
    public String getDescription()
    {
        return description;
    }

    /* (non-Javadoc)
     * @see com.percussion.feeds.data.IPSFeedDescriptor#getFeedType()
     */
    public FeedType getFeedType()
    {
        return FeedType.valueOf(type);
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
     * @param site the site to set
     */
    public void setSite(String site)
    {
        this.site = site;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title)
    {
        this.title = title;
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
     * @param query the query to set
     */
    public void setQuery(String query)
    {
        this.query = query;
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((site == null) ? 0 : site.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PSFeedDescriptor other = (PSFeedDescriptor) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (site == null) {
            return other.site == null;
		} else {
            return site.equals(other.site);
        }
    }

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PSFeedDescriptor [");
		if (site != null) {
			builder.append("site=");
			builder.append(site);
			builder.append(", ");
		}
		if (name != null) {
			builder.append("name=");
			builder.append(name);
			builder.append(", ");
		}
		if (title != null) {
			builder.append("title=");
			builder.append(title);
			builder.append(", ");
		}
		if (description != null) {
			builder.append("description=");
			builder.append(description);
			builder.append(", ");
		}
		if (link != null) {
			builder.append("link=");
			builder.append(link);
			builder.append(", ");
		}
		if (type != null) {
			builder.append("type=");
			builder.append(type);
			builder.append(", ");
		}
		if (query != null) {
			builder.append("query=");
			builder.append(query);
		}
		builder.append("]");
		return builder.toString();
	}


    
    

}
