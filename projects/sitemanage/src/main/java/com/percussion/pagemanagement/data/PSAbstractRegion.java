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
package com.percussion.pagemanagement.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PSAbstractRegion)) return false;
        PSAbstractRegion that = (PSAbstractRegion) o;
        return Objects.equals(getRegionId(), that.getRegionId()) && Objects.equals(getStartTag(), that.getStartTag()) && Objects.equals(getEndTag(), that.getEndTag()) && Objects.equals(getCssClass(), that.getCssClass()) && Objects.equals(getChildren(), that.getChildren()) && Objects.equals(getAttributes(), that.getAttributes());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRegionId(), getStartTag(), getEndTag(), getCssClass(), getChildren(), getAttributes());
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("PSAbstractRegion{");
        sb.append("regionId='").append(regionId).append('\'');
        sb.append(", startTag='").append(startTag).append('\'');
        sb.append(", endTag='").append(endTag).append('\'');
        sb.append(", cssClass='").append(cssClass).append('\'');
        sb.append(", children=").append(children);
        sb.append(", attributes=").append(attributes);
        sb.append('}');
        return sb.toString();
    }
}
