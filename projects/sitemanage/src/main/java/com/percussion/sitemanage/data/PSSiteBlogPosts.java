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

import com.fasterxml.jackson.annotation.JsonRootName;
import com.percussion.share.data.PSAbstractDataObject;
import com.percussion.share.data.PSItemProperties;
import net.sf.oval.constraint.NotEmpty;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * This class contains post information for a blog.
 */
@XmlRootElement(name="SiteBlogPosts")
@JsonRootName("SiteBlogPosts")
public class PSSiteBlogPosts extends PSAbstractDataObject
{
    /**
     * @return the blog link text
     */
    public String getBlogTitle()
    {
        return blogTitle;
    }

    /**
     * @param blogTitle the blog link text to set
     */
    public void setBlogTitle(String blogTitle)
    {
        this.blogTitle = blogTitle;
    }

    /**
     * @return the folder path of the blog section
     */
    public String getBlogSectionPath()
    {
        return blogSectionPath;
    }

    /**
     * @param blogSectionPath the folder path of the blog section to set
     */
    public void setBlogSectionPath(String blogSectionPath)
    {
        this.blogSectionPath = blogSectionPath;
    }

    /**
     * @return the blog posts
     */
    public List<PSItemProperties> getPosts()
    {
        return posts;
    }

    /**
     * @return the id of the blog post template
     */
    public String getBlogPostTemplateId()
    {
        return blogPostTemplateId;
    }

    /**
     * @param blogPostTemplateId the id of the blog post template to set
     */
    public void setBlogPostTemplateId(String blogPostTemplateId)
    {
        this.blogPostTemplateId = blogPostTemplateId;
    }
    
    /**
     * @param posts the blog posts to set
     */
    public void setPosts(List<PSItemProperties> posts)
    {
        this.posts = posts;
    }    
    
    /**
     * See {@link #getBlogTitle()} for details.
     */
    @NotEmpty
    private String blogTitle;
    
    /**
     * See {@link #getBlogSectionPath()} for details.
     */
    @NotEmpty
    private String blogSectionPath; 
    
    /**
     * See {@link #getPosts()} for details.
     */
    private List<PSItemProperties> posts;
    
    /**
     * See {@link #getBlogPostTemplateId()} for details.
     */
    @NotEmpty
    private String blogPostTemplateId;

}
