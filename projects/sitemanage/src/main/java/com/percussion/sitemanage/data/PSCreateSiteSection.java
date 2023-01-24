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
import com.percussion.sitemanage.data.PSSiteSection.PSSectionTargetEnum;
import com.percussion.sitemanage.data.PSSiteSection.PSSectionTypeEnum;

import javax.xml.bind.annotation.XmlRootElement;

import net.sf.oval.constraint.NotBlank;
import net.sf.oval.constraint.NotEmpty;
import net.sf.oval.constraint.NotNull;

/**
 * This class contains information for creating a site section.
 * A section contains a folder, landing page and a navon item.
 *
 * @author YuBingChen
 */
@JsonRootName("CreateSiteSection")
public class PSCreateSiteSection extends PSAbstractDataObject
{
    /**
     * Gets the landing page name of the section.
     * 
     * @return the landing page name, should not be blank for a valid request.
     */
    public String getPageName()
    {
       return pageName;
    }
    
    /**
     * Sets the landing page name.
     * 
     * @param name the new landing page name of the section, should not be 
     * blank for a valid request.
     * 
     * @see #getPageName()
     */
    public void setPageName(String name)
    {
       this.pageName = name;
    }
    
    /**
     * Gets the landing page title of the created section.
     * 
     * @return the landing page title, should not be blank for a valid request.
     */
    public String getPageTitle()
    {
       return pageTitle;
    }
    
    /**
     * Sets the landing page title of the created section.
     * 
     * @param title the new landing page title, should not be blank for a valid
     * request.
     * 
     * @see #getPageTitle()
     */
    public void setPageTitle(String title)
    {
       this.pageTitle = title;
    }
    
    /**
     * Gets the page link title of the section.
     * 
     * @return the page link title, should not be blank for a valid request.
     */
    public String getPageLinkTitle()
    {
       return pageLinkTitle;
    }
    
    /**
     * Sets the page link title
     * 
     * @param linkTitle the new navigation title, should not be blank for a 
     * valid request.
     */
    public void setPageLinkTitle(String linkTitle)
    {
       this.pageLinkTitle = linkTitle;
    }
    
    /**
     * Gets the URL identifier of the landing page.
     * 
     * @return the URL identifier, should not be blank for a valid request.
     */
    public String getPageUrlIdentifier()
    {
       return pageUrlIdentifier;
    }
    
    /**
     * Sets the URL identifier of the landing page.
     * 
     * @param urlIdentifier the new URL identifier, should not be blank for a
     * valid request.
     */
    public void setPageUrlIdentifier(String urlIdentifier)
    {
       this.pageUrlIdentifier = urlIdentifier;
    }
    
    /**
     * Gets the ID of the template used to create the landing page of the 
     * site section.
     * 
     * @return the ID of the template, should not be blank for a valid request.
     */
    public String getTemplateId()
    {
        return templateId;
    }
    
    /**
     * Sets the ID of the template used to create the landing page of the 
     * site section.
     * 
     * @param id the template ID, should not be blank for a valid request.
     */
    public void setTemplateId(String id)
    {
        templateId = id;
    }
    
    /**
     * Gets the folder path that will contain the section. This folder is also
     * the parent folder of the section.
     *  
     * @return the parent folder path, should not be blank for a valid 
     * request.
     */
    public String getFolderPath()
    {
       return folderPath;
    }
    
    /**
     * Sets the parent folder of the section.
     * 
     * @param folderPath the parent folder, should not be blank for a valid
     * request.
     * 
     * @see #getFolderPath()
     */
    public void setFolderPath(String folderPath)
    {
       this.folderPath = folderPath;
    }

    
    /**
     * @return the section type
     */
    public PSSectionTypeEnum getSectionType()
    {
       return sectionType;
    }

    /**
     * @param sectionType to set, if <code>null</code> initialized to {@link PSSectionTypeEnum#section}
     */
    public void setSectionType(PSSectionTypeEnum sectionType)
    {
       if(sectionType == null)
    	   sectionType = PSSectionTypeEnum.section;
       this.sectionType = sectionType;
    }

    /**
     * @return the target never <code>null</code>.
     */
    public PSSectionTargetEnum getTarget()
    {
       return target;
    }

    /**
     * @param target The target window type to set, if <code>null</code>
     * initialized to {@link PSSectionTargetEnum#_self}
     */
    public void setTarget(PSSectionTargetEnum target)
    {
       if(target == null)
    	   target = PSSectionTargetEnum._self;
       this.target = target;
    }

    /**
     * For blog sections.
     * 
     * @return the template id for new blog posts
     */
    public String getBlogPostTemplateId()
    {
        return blogPostTemplateId;
    }

    /**
     * For blog sections.
     * 
     * @param blogPostTemplateId the template id for new blog posts
     */
    public void setBlogPostTemplateId(String blogPostTemplateId)
    {
        this.blogPostTemplateId = blogPostTemplateId;
    }
    
    /**
     * For blog templates
     * 
     * @param copyTemplates determine if we will copy or not the templates
     */
    public void setCopyTemplates(Boolean copyTemplates)
    {
        this.copyTemplates = copyTemplates;
    }
    
    /**
     * For blog templates
     * 
     * @param copyTemplates determine if we will copy or not the templates
     */
    public Boolean getCopyTemplates()
    {
        return this.copyTemplates;
    }
    
    /**
     * The name of the section, see {@link #getPageName()} for detail.
     * If null will get default from site
     */
    private String pageName;

    /**
     * The title of the section, see {@link #getPageTitle()} for detail.
     */
    @NotBlank
    @NotNull
    private String pageTitle;

    /**
     * The URL identifier of the landing page, should not be blank for a
     * valid section request.  See {@link #getPageUrlIdentifier()} for detail.
     */
    @NotBlank
    @NotNull
    private String pageUrlIdentifier;

    /**
     * The navon title of the section, should not be blank for a
     * valid section request. See {@link #getPageLinkTitle()} for detail.
     */
    @NotBlank
    @NotNull
    private String pageLinkTitle;

    /**
     * The ID of the template used to create the landing page, it should not be
     * blank for a valid request.
     */
    @NotBlank
    @NotNull
    private String templateId;
    
    /**
     * The parent folder path of the section, should not be blank for a
     * valid section request. See {@link #getFolderPath()} for detail.
     */
    @NotBlank
    @NotNull
    private String folderPath;
    
    /**
     * See {@link #getBlogPostTemplateId()} for detail.
     */    
    @NotEmpty
    private String blogPostTemplateId;
    
    /**
     * The type of the section initialized to be a regular section .
     */
    private PSSectionTypeEnum sectionType = PSSectionTypeEnum.section;

    /**
     * The target type of the section initialized to be a regular section .
     */
    private PSSectionTargetEnum target = PSSectionTargetEnum._self;
    
    /**
     * Determine if a new template will be created or will be used the selected ones
     */
    @NotBlank
    @NotNull
    private Boolean copyTemplates;
    
}
