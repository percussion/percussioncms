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

package com.percussion.sitemanage.data;

import com.percussion.share.data.IPSFolderPath;
import com.percussion.share.data.PSAbstractPersistantObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import net.sf.oval.constraint.NotNull;

/**
 * Represents the site section in the architecture page. The site section is
 * comprised of the folder, navon (navtree for site home page) and landing page.
 * 
 * @author bjoginipally
 * 
 */
@XmlRootElement(name = "SiteSection")
public class PSSiteSection extends PSAbstractPersistantObject implements IPSFolderPath
{
    private static final long serialVersionUID = -1L;

    /**
     * Gets the title of the section. It is navigation title of the navigation
     * node and the link title of the landing page of the node.
     * 
     * @return the title of the section. It should not be blank for a properly
     * configured section.
     */
    public String getTitle()
    {
        return title;
    }

    /**
     * Sets the title of the section.
     * 
     * @param title the new title. It should not be blank for a valid section. 
     */
    public void setTitle(String title)
    {
        this.title = title;
    }

    /**
     * Gets the IDs of the sub-sections.
     *  
     * @return the IDs of the sub-sections. It is in the displayed order of
     * the sub-sections. It may be empty if there is no sub-sections, but
     * never <code>null</code>.
     */
    public List<String> getChildIds()
    {
            if(!childIds.isEmpty()){
                return Collections.unmodifiableList(childIds);
            }else{
                return childIds;
            }

    }

    /**
     * Sets the IDs of sub sections.
     * 
     * @param ids the IDs of sub-sections, may be <code>null</code> or empty.
     */
    public void setChildIds(List<String> ids)
    {
        if (ids == null)
            this.childIds = Collections.emptyList();
        else
            this.childIds = ids;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.percussion.share.data.PSAbstractPersistantObject#getId()
     */
    @Override
    public String getId()
    {
        return id;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.percussion.share.data.PSAbstractPersistantObject#setId(java.io.Serializable
     * )
     */
    @Override
    public void setId(String id)
    {
        this.id = id;
    }

    public String getFolderPath()
    {
        return folderPath;
    }

    public void setFolderPath(String folderPath)
    {
        this.folderPath = folderPath;
    }

    /**
     * @return external link url may be <code>null</code> if not set. Meaningful only for external link sections.
     */
    public String getExternalLinkUrl()
    {
        return externalLinkUrl;
    }

    /**
     * @param externalLinkUrl external link url, may be <code>null</code> or empty.
     */
    public void setExternalLinkUrl(String externalLinkUrl)
    {
        this.externalLinkUrl = externalLinkUrl;
    }
    
    /**
     * @return the section type, never <code>null</code>.
     */
    public PSSectionTypeEnum getSectionType()
    {
       return sectionType;
    }

    /**
     * @param sectionType to set, if <code>null</code> set to {@link PSSectionTypeEnum#section}}
     */
    public void setSectionType(PSSectionTypeEnum sectionType)
    {
       if(sectionType == null)
    	   sectionType = PSSectionTypeEnum.section;
       this.sectionType = sectionType;
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
     * Setter for the display title property.
     * 
     * @param displayTitlePath the displayTitlePath to set, {@link String} may be <code>null</code> or empty.
     * 
     * @see #getDisplayTitlePath()
     */
    public void setDisplayTitlePath(String displayTitlePath)
    {
        this.displayTitlePath = displayTitlePath;
    }

    /**
     * The display title path of this section. The display title path is only available
     * for section link node. This path is formed by the 'display title' of the root
     * to the current node.
     * 
     * @return the display title path. It may be <code>null</code> or empty if the node is not a section link.
     */
    public String getDisplayTitlePath()
    {
        return displayTitlePath;
    }

    /**
     * Describes the type of section. 
     */
    public enum PSSectionTypeEnum {
        /**
         * regular section.
         */
        section,
        
        /**
         * link to a regular section.
         */
        sectionlink,
        
        /**
         * external link type section.
         */
        externallink,
        
        /**
         * blog section.
         */
        blog
    }

    public enum PSSectionTargetEnum {
    	/**
    	 * default option
    	 */
    	_self,
    	
    	/**
    	 * New window option
    	 */
    	_blank,
    	
    	/**
    	 * Top window option
    	 */
    	_top,

        /**
         * Parent window option
         */
        _parent
    	
    }
    /**
     * The navigation title. It is also the link title of the landing page.
     */
    private String title;

    /**
     * The string representation of the guid of the navon item of the section.
     */
    @XmlElement
    private String id;

    /**
     * The folder path for this section.
     */
    private String folderPath;

    /**
     * The url of the external link section, is meaningful only when type is External Link.
     */
    private String externalLinkUrl;

    @NotNull
    private PSSectionTypeEnum sectionType = PSSectionTypeEnum.section;

    @NotNull
    private PSSectionTargetEnum target = PSSectionTargetEnum._self;
    
    /**
     * The IDs of sub-sections.
     */
    private List<String> childIds = new ArrayList<>();
    
    /**
     * Field to note if the section requires login.
     */
    private boolean requiresLogin;
    
    /**
     * Field to save the groups that are allowed to enter the section.
     */
    private String allowAccessTo;

    /**
     * Field to save the css class names used when rendering navigation widgets.
     */
    private String cssClassNames;

    private String displayTitlePath;
}
