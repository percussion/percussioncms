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

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.percussion.share.data.PSAbstractDataObject;
import org.json.JSONObject;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

//import javax.xml.bind.annotation.XmlAttribute;

//FB: CN_IMPLEMENTS_CLONE_BUT_NOT_CLONEABLE NC 1-16-16
@XmlRootElement(name = "CategoryTree")
@JsonIgnoreProperties(ignoreUnknown = true)
@XmlAccessorType(XmlAccessType.PROPERTY)
@JsonRootName(value = "CategoryTree")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PSCategory extends PSAbstractDataObject implements Cloneable {

    @JsonProperty
    private String title;

    @JsonProperty
    private String allowedSites;

    private List<PSCategoryNode> topLevelNodes = new ArrayList<PSCategoryNode>();

    @XmlElement(name = "Children")
    @JsonProperty("topLevelNodes")
    @XmlElementWrapper(nillable=true)
    public List<PSCategoryNode> getTopLevelNodes() {
        return topLevelNodes;
    }

    public void setTopLevelNodes(List<PSCategoryNode> children) {
        this.topLevelNodes = children;
    }

    @XmlAttribute(name = "title")
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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
        return "PSCategory [ title=" + title
                + ", allowedSite=" + allowedSites + ", topLevelNodes=" + topLevelNodes + "]";
    }

    @Override
    public PSCategory clone() {
        PSCategory category = (PSCategory) super.clone();
        category.setTitle(this.getTitle());
        if (this.getTopLevelNodes() != null) {
            category.setTopLevelNodes(new ArrayList<PSCategoryNode>(this.getTopLevelNodes()));
        }
        return category;
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
