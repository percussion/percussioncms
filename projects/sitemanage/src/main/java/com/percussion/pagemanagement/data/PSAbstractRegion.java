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
package com.percussion.pagemanagement.data;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;

import net.sf.oval.constraint.NotBlank;
import net.sf.oval.constraint.NotNull;

/**
 * A data object that represents a region on the template/page.
 * @author adamgent
 *
 */
@XmlType(name = "", propOrder = {
        "regionId",
        "startTag",
        "attributes",
        "children",
        "cssClass",
        "endTag"
    })
public abstract class PSAbstractRegion extends PSRegionNode {
    
    private static final long serialVersionUID = 1L;
    @NotNull
    @NotBlank
    private String regionId;
    /**
     * Start xhtml tag of region.
     * maybe <code>null</code>.
     * @see #getStartTag()
     */
    private String startTag;
    /**
     * @see #getEndTag()
     */
    private String endTag;
    /**
     * @see #getCssClass()
     */
    private String cssClass;
    
   
    private List<PSRegionNode> children = new ArrayList<>();


    private List<PSRegionAttribute> attributes = new ArrayList<>();
    /**
     * Children of a region are either {@link PSRegionCode} or {@link PSRegion}
     * @return never <code>null</code>.
     */
    @XmlElementWrapper(name = "children")
    @XmlElements(
    {@XmlElement(name = "region", type = PSRegion.class), @XmlElement(name = "code", type = PSRegionCode.class)})
    public List<PSRegionNode> getChildren()
    {
        return children;
    }

    public void setChildren(List<PSRegionNode> children)
    {
        this.children = children;
    }

    @NotNull
    @NotBlank
    public String getRegionId()
    {
        return regionId;
    }

    public void setRegionId(String id)
    {
        this.regionId = id;
    }


    /**
     * The start tag of the region.
     * This should include the entire opening tag. 
     * This is all the content from 
     * '<code>&lt;</code>' to '<code>&gt;</code>'
     * inclusive.
     * <p>
     * Usually this is a div like:
     * <pre>
     * &lt;div id="regionId" class="perc-region" &gt;
     * </pre>
     * @return never <code>null</code> or empty.
     */
    public String getStartTag()
    {
        return startTag;
    }

    public void setStartTag(String startTag)
    {
        this.startTag = startTag;
    }

    /**
     * The end tag of the region.
     * <p>
     * Usually it is:
     * <pre>
     * &lt;/div&gt;
     * </pre>
     * @return never <code>null</code> or empty.
     */
    public String getEndTag()
    {
        return endTag;
    }

    public void setEndTag(String endTag)
    {
        this.endTag = endTag;
    }

    /**
     * The user defined CSS class selector for the 
     * region.
     * The selector may or may not be in the {@link #getStartTag() start tags}
     * class attribute but in general <strong>it should be</strong>.
     * @return maybe empty or <code>null</code>.
     */
    public String getCssClass()
    {
        return cssClass;
    }

    public void setCssClass(String rootClass)
    {
        this.cssClass = rootClass;
    }

    /**
     * The user defined attributes for the
     * region.
     * @return maybe empty or <code>null</code>.
     */
    @XmlElementWrapper(name = "attributes")
    @XmlElements(
            {@XmlElement(name = "attribute", type = PSRegionAttribute.class)})
    public  List<PSRegionAttribute> getAttributes()
    {
        return attributes;
    }

    public void setAttributes(List<PSRegionAttribute> attributes)
    {
        this.attributes = attributes;
    }


}
