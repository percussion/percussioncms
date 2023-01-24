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
import com.percussion.sitemanage.data.PSSiteSection.PSSectionTargetEnum;
import com.percussion.sitemanage.data.PSSiteSection.PSSectionTypeEnum;
import net.sf.oval.constraint.NotBlank;
import net.sf.oval.constraint.NotNull;

import javax.xml.bind.annotation.XmlRootElement;


/**
 * Request object for creating external link section..
 * @author bjoginipally
 *
 */
@XmlRootElement(name="CreateExternalLinkSection")
@JsonRootName("CreateExternalLinkSection")
public class PSCreateExternalLinkSection 
{
    /**
     * Gets the external URL of the external link section.
     * 
     * @return the URL, may be <code>null</code> or blank .
     */
    public String getExternalUrl()
    {
       return externalUrl;
    }
    
    /**
     * Sets the URL for an external link section.
     * 
     * @param url the url of the section, may be <code>null</code> or blank.
     */
    public void setExternalUrl(String url)
    {
       this.externalUrl = url;
    }

    /**
     * Gets the page link title of the section.
     * 
     * @return the page link title, should not be blank for a valid request.
     */
    public String getLinkTitle()
    {
       return linkTitle;
    }
    
    /**
     * Sets the page link title
     * 
     * @param linkTitle the new navigation title, should not be blank for a 
     * valid request.
     */
    public void setLinkTitle(String linkTitle)
    {
       this.linkTitle = linkTitle;
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
     * @return the section type
     */
    public PSSectionTargetEnum getTarget()
    {
       return target;
    }

    /**
     * @param sectionType to set, if <code>null</code> set to {@link PSSectionTypeEnum#section}}
     */
    public void setTarget(PSSectionTargetEnum target)
    {
       if(target == null)
    	   target = PSSectionTargetEnum._self;
       this.target = target;
    }
    
    /**
     * @param cssClassNames the class names used with navigation widget.
     */
    public void setCssClassNames(String cssClassNames)
    {
        this.cssClassNames = cssClassNames;
    }

    /**
     * Gets the css class names of the section folder.
     * 
     * @return the css class names used with navigation widget.
     */
    public String getCssClassNames()
    {
        return cssClassNames;
    }

    /**
     * The name of the section, see {@link #getexternalUrl()} for detail.
     */
    private String externalUrl;

    /**
     * The link title of the external section, see {@link #getLinkTitle()} for detail.
     */
    private String linkTitle;

    /**
     * The parent folder path of the section, should not be blank for a
     * valid section request. See {@link #getFolderPath()} for detail.
     */
    @NotBlank
    @NotNull
    private String folderPath;

    /**
     * The type of the section initialized to be a regular section .
     */
    private PSSectionTypeEnum sectionType = PSSectionTypeEnum.externallink;

    @NotNull
    private PSSectionTargetEnum target = PSSectionTargetEnum._self;
    
    /**
     * Field to save the css class names used when rendering navigation widgets.
     */
    private String cssClassNames;
}
