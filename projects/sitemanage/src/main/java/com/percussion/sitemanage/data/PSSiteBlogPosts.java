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
