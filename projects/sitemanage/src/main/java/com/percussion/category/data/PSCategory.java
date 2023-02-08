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

    private List<PSCategoryNode> topLevelNodes = new ArrayList<>();

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
    public PSCategory clone() throws CloneNotSupportedException {
        PSCategory category = (PSCategory) super.clone();
        category.setTitle(this.getTitle());
        if (this.getTopLevelNodes() != null) {
            category.setTopLevelNodes(new ArrayList<>(this.getTopLevelNodes()));
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
