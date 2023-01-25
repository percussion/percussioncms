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
import com.percussion.pathmanagement.data.PSFolderPermission;
import com.percussion.share.data.PSAbstractDataObject;
import com.percussion.sitemanage.data.PSSiteSection.PSSectionTargetEnum;

import javax.xml.bind.annotation.XmlRootElement;

import net.sf.oval.constraint.NotBlank;
import net.sf.oval.constraint.NotNull;

/**
 * This class contains request information for updating a site section.
 *
 * @author yubingchen
 */
@XmlRootElement(name="SiteSectionProperties")
@JsonRootName("SiteSectionProperties")
public class PSSiteSectionProperties extends PSAbstractDataObject
{
    /**
     * Gets the ID of the section.
     * 
     * @return section ID. It must not be blank if has been persisted.
     */
    public String getId()
    {
        return id;
    }
    
    /**
     * Sets the ID of the section.
     * 
     * @param id the new ID of the section, should not be blank for
     * a valid request.
     */
    public void setId(String id)
    {
        this.id = id;
    }
    
    /**
     * Gets the title of the section. It is the link title
     * of the landing page of this section.
     * 
     * @return the title, should not be blank for a valid request.
     */
    public String getTitle()
    {
        return title;
    }
    
    /**
     * Sets the title, which is the link title of the landing page
     * of the section.
     *  
     * @param title the new title, should not be blank for a valid request.
     */
    public void setTitle(String title)
    {
        this.title = title;
    }

    /**
     * Gets the folder name.
     *  
     * @return the folder name, should not be blank for a valid request.
     */
    public String getFolderName()
    {
        return folderName;
    }

    /**
     * Sets the folder name.
     * 
     * @param folderName the new folder name, should not be blank for
     * a valid request.
     */
    public void setFolderName(String folderName)
    {
        this.folderName = folderName;
    }
    
    /**
     * Gets the permission of the section folder.
     * 
     * @return the folder permission, not <code>null</code> for a valid section.
     */
    public PSFolderPermission getFolderPermission()
    {
        return folderPermission;
    }
    
    /**
     * Sets the permission of the section folder.
     * 
     * @param permission the new folder permission, not <code>null</code> for a valid section.
     */
    public void setFolderPermission(PSFolderPermission permission)
    {
        folderPermission = permission;
    }
    
    /**
     * @return the target type of the section, never <code>null</code>.
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

    public boolean isRequiresLogin()
    {
        return requiresLogin;
    }

    public void setRequiresLogin(boolean requiresLogin)
    {
        this.requiresLogin = requiresLogin;
    }

    public String getAllowAccessTo()
    {
        return allowAccessTo;
    }

    public void setAllowAccessTo(String allowAccessTo)
    {
        this.allowAccessTo = allowAccessTo;
    }

    public boolean isSecureSite()
    {
        return secureSite;
    }

    public void setSecureSite(boolean secureSite)
    {
        this.secureSite = secureSite;
    }
        
    public void setSecureAncestor(boolean secureAncestor)
    {
        this.secureAncestor = secureAncestor;
    }

    public boolean isSecureAncestor()
    {
        return secureAncestor;
    }

    /**
     * @param siteRootSection the siteRootSection to set
     */
    public void setSiteRootSection(boolean siteRootSection)
    {
        this.siteRootSection = siteRootSection;
    }

    /**
     * @return the siteRootSection
     */
    public boolean isSiteRootSection()
    {
        return siteRootSection;
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
     * The ID of the section, not blank for a valid request.
     */
    @NotBlank
    @NotNull
    private String id;
    
    /**
     * The title of the section, it is also the link title of the related landing
     * page, not blank for a valid request.
     */
    @NotBlank
    @NotNull
    private String title; 
    
    /**
     * The folder name of the section, not blank for a valid request.
     */
    @NotBlank
    @NotNull
    private String folderName;
    
    @NotNull
    private PSFolderPermission folderPermission;

    @NotNull
    private PSSectionTargetEnum target = PSSectionTargetEnum._self;
    
    /**
     * Field to note if the section requires login.
     */
    private boolean requiresLogin;

    /**
     * Field to save the groups that are allowed to enter the section.
     */
    private String allowAccessTo;

    /**
     * <code>true</code> if the site that the section belongs to is secure.
     * <code>false</code> otherwise.
     */
    private boolean secureSite;

    /**
     * <code>true</code> if one ancestor section is secure. <code>false</code>
     * otherwise.
     */
    private boolean secureAncestor;
 
    /**
     * This means that this section belongs to the root of the site section.
     */
    private boolean siteRootSection = false;
    
    /**
     * Field to save the css class names used when rendering navigation widgets.
     */
    private String cssClassNames;
}
