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
package com.percussion.pagemanagement.data;

import com.percussion.pagemanagement.data.PSResourceDefinitionGroup.PSResourceDefinition;
import com.percussion.pagemanagement.data.PSResourceDefinitionGroup.PSResourceDefinitionType;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Represents an inline link inside a rich text editor.
 * <p>
 * There are many legacy properties that are needed for the inline link parser
 * that will be removed someday.
 * 
 * @author adamgent
 * 
 */
@XmlRootElement(name = "InlineRenderLink")
public class PSInlineRenderLink extends PSRenderLink
{

    private String targetId;

    private String thumbUrl;

    private String title;

    private String altText;

    private transient PSResourceDefinition thumbResourceDefinition;

    private PSResourceDefinitionType thumbResourceType;

    private String thumbResourceDefinitionId;

    @Deprecated
    private Integer legacyDependentVariantId;

    @Deprecated
    private Integer legacyThumbDependentVariantId;

    @Deprecated
    private String legacyRxInlineSlot;

    @Deprecated
    private Integer legacyDependentId;

    @Deprecated
    private String inlineType;

	private String stateClass;

    public PSInlineRenderLink()
    {
        setUrl("");
        setThumbUrl("");
        setTitle("");
        setStateClass("");
    }

    /**
     * Gets value set by setter. see setTitle
     * 
     * @return maybe <code>null</code>.
     */
    public String getTitle()
    {
        return title;
    }

    /**
     * Set title for tool tip from item. Never empty or <code>null</code>.
     * 
     * @param title
     */
    public void setTitle(String title)
    {
        this.title = title;
    }

    /**
     * Gets value set by setter. see setSys_dependentvariantid
     * 
     * @return maybeNull
     */
    @Deprecated
    @XmlElement(name = "sys_dependentvariantid")
    public Integer getLegacyDependentVariantId()
    {
        return legacyDependentVariantId;
    }

    /**
     * Legacy - Set Legacy sys_dependentvariantid User by link parser until
     * parser is updated.
     * 
     * @param sys_dependentvariantid
     */
    @Deprecated
    public void setLegacyDependentVariantId(Integer sys_dependentvariantid)
    {
        this.legacyDependentVariantId = sys_dependentvariantid;
    }

    /**
     * Gets value set by setter. see setRxinlineslot
     * 
     * @return maybe <code>null</code>.
     */
    @Deprecated
    @XmlElement(name = "rxinlineslot")
    public String getLegacyRxInlineSlot()
    {
        return legacyRxInlineSlot;
    }

    /**
     * Legacy - Set Legacy rxinlineslot Used by link parser until parser is
     * updated.
     * 
     * @param rxinlineslot
     */
    @Deprecated
    public void setLegacyRxInlineSlot(String rxinlineslot)
    {
        this.legacyRxInlineSlot = rxinlineslot;
    }

    /**
     * Gets value set by setter. see setSys_dependentid
     * 
     * @return maybe <code>null</code>.
     */
    @Deprecated
    @XmlElement(name = "sys_dependentid")
    public Integer getLegacyDependentId()
    {
        return legacyDependentId;
    }

    /**
     * Legacy - Set Legacy sys_dependentid Used by link parser until parser is
     * updated.
     * 
     * @param sys_dependentid
     */
    @Deprecated
    public void setLegacyDependentId(Integer sys_dependentid)
    {
        this.legacyDependentId = sys_dependentid;
    }

    /**
     * Gets value set by setter. see setInlinetype
     * 
     * @return maybe <code>null</code>.
     */
    @XmlElement(name = "inlinetype")
    @Deprecated
    public String getInlineType()
    {
        return inlineType;
    }

    /**
     * Legacy - Set Legacy inlinetype Used by link parser until parser is
     * updated.
     * 
     * @param inlinetype
     */
    @Deprecated
    public void setInlineType(String inlinetype)
    {
        this.inlineType = inlinetype;
    }

    /**
     * Gets value set by setter. see setThumbUrl
     * 
     * @return maybe <code>null</code>.
     */
    public String getThumbUrl()
    {
        return thumbUrl;
    }

    /**
     * Set thumbnail url if image has a thumbnail. Empty if there isn't a
     * thumbnail. Never <code>null</code>
     * 
     * @param thumbUrl
     */
    public void setThumbUrl(String thumbUrl)
    {
        this.thumbUrl = thumbUrl;
    }

    /**
     * Gets value set by setter. see setAltText
     * 
     * @return never <code>null</code>.
     */
    public String getAltText()
    {
        return altText;
    }

    /**
     * Sets altText use to set alt attribute on img html tag.
     * 
     * @param altText
     */
    public void setAltText(String altText)
    {
        this.altText = altText;
    }

    /**
     * Gets value set by setter. see setThumbsys_dependentvariantid
     * 
     * @return maybe <code>null</code>.
     */
    @Deprecated
    @XmlElement(name = "thumbsys_dependentvariantid")
    public Integer getLegacyThumbDependentVariantId()
    {
        return legacyThumbDependentVariantId;
    }

    /**
     * Legacy - Sets sys_dependentvariantid Used by link parser until parser is
     * updated.
     * 
     * @param thumbsys_dependentvariantid
     */
    @Deprecated
    public void setLegacyThumbDependentVariantId(Integer thumbsys_dependentvariantid)
    {
        this.legacyThumbDependentVariantId = thumbsys_dependentvariantid;
    }

    /**
     * The id of the object that link is pointing to.
     * 
     * @return never <code>null</code>.
     */
    public String getTargetId()
    {
        return targetId;
    }

    public void setTargetId(String pageId)
    {
        this.targetId = pageId;
    }

    @XmlTransient
    public PSResourceDefinition getThumbResourceDefinition()
    {
        return thumbResourceDefinition;
    }

    public void setThumbResourceDefinition(PSResourceDefinition thumbResourceDefinition)
    {
        this.thumbResourceDefinition = thumbResourceDefinition;
        if (thumbResourceDefinition != null) {
            setResourceType(thumbResourceDefinition.getResourceType());
            setThumbResourceDefinitionId(thumbResourceDefinition.getUniqueId());
        }
    }

    public PSResourceDefinitionType getThumbResourceType()
    {
        return thumbResourceType;
    }

    public void setThumbResourceType(PSResourceDefinitionType thumbResourceType)
    {
        this.thumbResourceType = thumbResourceType;
    }

    public String getThumbResourceDefinitionId()
    {
        return thumbResourceDefinitionId;
    }

    public void setThumbResourceDefinitionId(String thumbResourceDefinitionId)
    {
        this.thumbResourceDefinitionId = thumbResourceDefinitionId;
    }

    public String getStateClass()
    {
    	return this.stateClass;
    }
    
	public void setStateClass(String stateClass) {
		this.stateClass = stateClass;	
	}
	

}
