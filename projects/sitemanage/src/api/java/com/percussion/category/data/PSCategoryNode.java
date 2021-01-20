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

package com.percussion.category.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.percussion.share.data.PSAbstractDataObject;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.text.Collator;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

//FB: CN_IMPLEMENTS_CLONE_BUT_NOT_CLONEABLE NC 1-16-16
@XmlRootElement(name = "Category")
@JsonRootName("")
@JsonIgnoreProperties(ignoreUnknown = true)
@XmlAccessorType(XmlAccessType.PROPERTY)
@JsonInclude()
public class PSCategoryNode extends PSAbstractDataObject implements Comparable<PSCategoryNode>, Cloneable {

    @JsonProperty(value="id")
    private String id;

    @JsonProperty(value="title")
    private String title;

    @JsonProperty(value="selectable",defaultValue = "true")
    private boolean selectable = true;

    @JsonProperty(value="previousCategoryName")
    private String previousCategoryName;

    @JsonProperty
    private boolean showInPgMetaData = true;

    @JsonProperty
    private boolean initialViewCollapsed = true;

    @JsonProperty
    private String createdBy;

    @JsonProperty("creationDate")
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDateTime creationDate;

    @JsonProperty
    private String lastModifiedBy;

    @JsonProperty("lastModifiedDate")
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDateTime lastModifiedDate;

    @JsonProperty
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDateTime publishDate;

    @JsonProperty
    private boolean deleted = false;

    @JsonProperty("children")
    private List<PSCategoryNode> childNodes = new ArrayList<PSCategoryNode>();

    @JsonProperty
    private boolean selected = false;

    @JsonProperty
    private String oldId;

    @JsonProperty
    private String allowedSites;

    public PSCategoryNode() {
        super();
    }

    @XmlElement(name = "Child")
    public List<PSCategoryNode> getChildNodes() {
        return childNodes;
    }

    public void setChildNodes(List<PSCategoryNode> children) {
        this.childNodes = children;
    }

    @XmlAttribute(name = "id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @XmlAttribute(name = "title")
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @XmlAttribute(name = "selectable")
    public boolean isSelectable() {
        return selectable;
    }

    public void setSelectable(boolean selectable) {
        this.selectable = selectable;
    }

    @XmlAttribute(name = "previousCategoryName")
    public String getPreviousCategoryName() {
        return previousCategoryName;
    }

    public void setPreviousCategoryName(String previousCategoryName) {
        this.previousCategoryName = previousCategoryName;
    }

    @XmlAttribute(name = "showInPgMetaData")
    public boolean isShowInPgMetaData() {
        return showInPgMetaData;
    }

    public void setShowInPgMetaData(boolean showInPgMetaData) {
        this.showInPgMetaData = showInPgMetaData;
    }

    @XmlAttribute(name = "initialViewCollapsed")
    public boolean isInitialViewCollapsed() {
        return initialViewCollapsed;
    }

    public void setInitialViewCollapsed(boolean initialViewCollapsed) {
        this.initialViewCollapsed = initialViewCollapsed;
    }

    @XmlAttribute(name = "createdBy")
    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    @XmlAttribute(name = "creationDate")
    @XmlJavaTypeAdapter(PSDateAdapter.class)
    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    @XmlAttribute(name = "lastModifiedBy")
    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    @XmlAttribute(name = "lastModifiedDate")
    @XmlJavaTypeAdapter(PSDateAdapter.class)
    public LocalDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(LocalDateTime lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }


    @XmlAttribute(name = "publishDate")
    @XmlJavaTypeAdapter(PSDateAdapter.class)
    public LocalDateTime getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(LocalDateTime publishDate) {
        this.publishDate = publishDate;
    }

    @XmlAttribute(name = "deleted")
    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    @XmlTransient
    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @XmlTransient
    public String getOldId() {
        return oldId;
    }

    public void setOldId(String oldId) {
        this.oldId = oldId;
    }

    @XmlAttribute(name = "allowedSites")
    public String getAllowedSites() {
        return allowedSites;
    }

    public void setAllowedSites(String allowedSites) {
        this.allowedSites = allowedSites;
    }

    @Override
    public String toString() {
        return "PSCategoryNode [id=" + id + ", title=" + title
                + ", selectable=" + selectable + ", previousCategoryName="
                + previousCategoryName + ", showInPgMetaData="
                + showInPgMetaData + ", initialViewCollapsed="
                + initialViewCollapsed + ", createdBy=" + createdBy
                + ", creationDate=" + creationDate + ", lastModifiedBy="
                + lastModifiedBy + ", lastModifiedDate=" + lastModifiedDate
                + ", allowedSites=" + allowedSites
                + ", publishDate=" + publishDate + ", deleted=" + deleted
                + ", childNodes=" + childNodes + ", selected=" + selected + ", oldId=" + oldId + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        PSCategoryNode other = (PSCategoryNode) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    @Override
    public PSCategoryNode clone() {
        PSCategoryNode categoryNode = (PSCategoryNode) super.clone();

        categoryNode.setId(this.getId());
        categoryNode.setTitle(this.getTitle());
        categoryNode.setCreatedBy(this.getCreatedBy());
        categoryNode.setCreationDate(this.getCreationDate());
        categoryNode.setDeleted(this.isDeleted());
        categoryNode.setInitialViewCollapsed(this.isInitialViewCollapsed());
        categoryNode.setLastModifiedBy(this.getLastModifiedBy());
        categoryNode.setLastModifiedDate(this.getLastModifiedDate());
        //categoryNode.setPrevCatId(this.getPrevCatId());
        categoryNode.setPreviousCategoryName(this.getPreviousCategoryName());
        categoryNode.setSelectable(this.isSelectable());
        categoryNode.setShowInPgMetaData(this.isShowInPgMetaData());
        categoryNode.setOldId(this.getOldId());
        categoryNode.setAllowedSites(this.getAllowedSites());
        if (this.getChildNodes() != null) {
            categoryNode.setChildNodes(new ArrayList<PSCategoryNode>(this.getChildNodes()));
        }
        return categoryNode;
    }

    public int compareTo(PSCategoryNode o) {
        return Collator.getInstance().compare(this.getId(), o.getId());
    }


    /***
     * Hydrate this object from a josn string
     * @param json
     */
    public void fromJSON(String json){

    }

    /***
     * Convert this object to a JSON string
     * @return
     */
    public String toJSON(){
        String ret = null;
        try{
            ObjectMapper mapper = new ObjectMapper();
            ret= mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            System.out.println(e.getMessage());
        }
        return ret;
    }
}
