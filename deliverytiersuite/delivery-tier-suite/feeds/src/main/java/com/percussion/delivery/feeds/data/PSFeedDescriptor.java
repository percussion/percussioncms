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
