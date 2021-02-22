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

package com.percussion.sitemanage.data;


import javax.xml.bind.annotation.XmlRootElement;
import com.percussion.share.data.PSAbstractDataObject;

/**
 * This class contains information for a blog of a site
 *
 */
@XmlRootElement(name="SiteBlogProperties")
public class PSSiteBlogProperties extends PSAbstractDataObject
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * @return the id
     */
    public String getId()
    {
        return id;
    }
    /**
     * @param id the blog id to set
     */
    public void setId(String id)
    {
        this.id = id;
    }
    /**
     * @return the blogPostTemplateId
     */
    public String getBlogPostTemplateId()
    {
        return blogPostTemplateId;
    }
    /**
     * @param blogPostTemplateId the blogPostTemplateId to set
     */
    public void setBlogPostTemplateId(String blogPostTemplateId)
    {
        this.blogPostTemplateId = blogPostTemplateId;
    }
    /**
     * @return the title
     */
    public String getTitle()
    {
        return title;
    }
    /**
     * @param title the title to set
     */
    public void setTitle(String title)
    {
        this.title = title;
    }
    
    /**
     * @return the description
     */
    public String getDescription()
    {
        return description;
    }
    /**
     * @param description the description to set
     */
    public void setDescription(String description)
    {
        this.description = description;
    }
    /**
     * @return the blogPostcount
     */
    public int getBlogPostcount()
    {
        return blogPostcount;
    }
    /**
     * @param blogPostcount the blogPostcount to set
     */
    public void setBlogPostcount(int blogPostcount)
    {
        this.blogPostcount = blogPostcount;
    }
    /**
     * @return the lastPublishDate
     */
    public String getLastPublishDate()
    {
        return lastPublishDate;
    }
    /**
     * @param lastPublishDate the lastPublishDate to set
     */
    public void setLastPublishDate(String lastPublishDate)
    {
        this.lastPublishDate = lastPublishDate;
    }
     
    /**
     * @return the finder path of the blog index page
     */
    public String getPath()
    {
        return path;
    }
    /**
     * @param path the finder path of the blog index page to set
     */
    public void setPath(String path)
    {
        this.path = path;
    }
    
    /**
     * @return the id of the index page for this blog
     */
    public String getPageId()
    {
        return pageId;
    }
    /**
     * @param pageId the id of the index page for this blog to set
     */
    public void setPageId(String pageId)
    {
        this.pageId = pageId;
    }
    
    /**
     * Blog Id which is nothing but a section id
     */
    private String id;
    
    /**
     * Id of the blog index page
     */
    private String pageId;
    
    /**
     * Template id for the blog post
     */
    private String blogPostTemplateId;
    
    /**
     * Blog title
     */
    private String title;
    
    /**
     * Blog description
     */
    private String description;
    
    /**
     * number of posts for the blog
     */
    private int blogPostcount;
    
    /**
     * Blog's last publish date
     */
    private String lastPublishDate;
    
    /**
     * Path of the blog index page
     */
    private String path;
   
}

