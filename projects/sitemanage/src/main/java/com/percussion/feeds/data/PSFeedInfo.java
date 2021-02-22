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
package com.percussion.feeds.data;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;

/**
 * Data class to hold information about a feed.
 * @author erikserating
 *
 */
public class PSFeedInfo
{
    private String name;
    private String title;
    private String desc;
    private String query;
    private String ownerpagelocation;
    private String type = "RSS2"; // Defaults to RSS2
    private int ownerpageid;
    private int ownerfolderid;
    private int contentId;
    private Set<Integer> pages = new HashSet<>();
    private Set<Integer> templates = new HashSet<>();
    
    public PSFeedInfo(int contentId, String name, String title, String desc)
    {
       this.contentId = contentId;
       this.name = name;
       this.title = title;
       this.desc = desc;
    }
          
    public int getId()
    {
       return contentId;
    }
    
    public String getName()
    {
       return name;
    }     
          
    public String getTitle()
    {
       return title;
    }     
    
    public String getDesc()
    {
       return desc;
    } 
    
    public Set<Integer>getPages()
    {
       return pages;
    }
    
    public Set<Integer> getTemplates()
    {
       return templates;
    }
    
    public int getOwnerPageId()
    {
       return ownerpageid;
    }
    
    public void setOwnerPageId(int id)
    {
       ownerpageid = id;
    }
    
    public String getOwnerPageLocation()
    {
       return ownerpagelocation;
    }
    
    public void setOwnerPageLocation(String location)
    {
       ownerpagelocation = location;
    }
    
    public int getOwnerFolderId()
    {
       return ownerfolderid;
    }
    
    public void setOwnerFolderId(int folderid)
    {
       ownerfolderid = folderid;
    }

    /**
     * @return the query
     */
    public String getQuery()
    {
        return query;
    }

    /**
     * @param query the query to set
     */
    public void setQuery(String query)
    {
        this.query = query;
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
    
    //FB: HE_EQUALS_USE_HASHCODE NC 1-16-16

    /* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + contentId;
		result = prime * result + ((desc == null) ? 0 : desc.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ownerfolderid;
		result = prime * result + ownerpageid;
		result = prime
				* result
				+ ((ownerpagelocation == null) ? 0 : ownerpagelocation
						.hashCode());
		result = prime * result + ((pages == null) ? 0 : pages.hashCode());
		result = prime * result + ((query == null) ? 0 : query.hashCode());
		result = prime * result
				+ ((templates == null) ? 0 : templates.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof PSFeedInfo)) {
			return false;
		}
		PSFeedInfo other = (PSFeedInfo) obj;
		if (contentId != other.contentId) {
			return false;
		}
		if (desc == null) {
			if (other.desc != null) {
				return false;
			}
		} else if (!desc.equals(other.desc)) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (ownerfolderid != other.ownerfolderid) {
			return false;
		}
		if (ownerpageid != other.ownerpageid) {
			return false;
		}
		if (ownerpagelocation == null) {
			if (other.ownerpagelocation != null) {
				return false;
			}
		} else if (!ownerpagelocation.equals(other.ownerpagelocation)) {
			return false;
		}
		if (pages == null) {
			if (other.pages != null) {
				return false;
			}
		} else if (!pages.equals(other.pages)) {
			return false;
		}
		if (query == null) {
			if (other.query != null) {
				return false;
			}
		} else if (!query.equals(other.query)) {
			return false;
		}
		if (templates == null) {
			if (other.templates != null) {
				return false;
			}
		} else if (!templates.equals(other.templates)) {
			return false;
		}
		if (title == null) {
			if (other.title != null) {
				return false;
			}
		} else if (!title.equals(other.title)) {
			return false;
		}
		if (type == null) {
			if (other.type != null) {
				return false;
			}
		} else if (!type.equals(other.type)) {
			return false;
		}
		return true;
	}
    
    
    
    
}
