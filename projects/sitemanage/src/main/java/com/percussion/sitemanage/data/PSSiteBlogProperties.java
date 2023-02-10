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

