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

package com.percussion.rest.contentlists;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.percussion.rest.Guid;
import com.percussion.rest.extensions.Extension;
import com.percussion.rest.itemfilter.ItemFilter;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "ContentList")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Represents a Content List")
public class ContentList {

    @Schema(required = false, description = "The unique ID for this ContentList.")
    private
    Guid contentListId;

    @Schema(required = false, description = "Ignored.")
    private Integer version;

    @Schema( required = false, description = "The name of the Content List. Must be unique.")
    private
    String name;

    @Schema(name = "description", required = false, description = "A human friendly description of the Content List.")
    private
    String description;

    @Schema(name = "type", required = false, description = "The type of the ContentList.", allowableValues = "[Normal,Incremental]")
    private
    String type = "Normal";

    @Schema(name = "url", required = false, description = "The URL for this ContentList")
    private
    String url;

    @Schema(name = "generator", required = false, description = "The ContentList Generator configured for this ContentList")
    private
    Extension generator;

    @Schema(name = "expander", required = false, description = "The ContentList Template Expander configured for this ContentList")
    private
    Extension expander;

    @Schema(name = "editionType", required = true, description = "Indicates the type of Edition (Publish or Unpublish then Publish)", allowableValues = "[Publish,Unpublish Then Publish]")
    private
    String editionType;

    @Schema(name = "itemFilter", required = true, description = "The ItemFilter used to filter content returned by this Content List.")
    private
    ItemFilter itemFilter = null;

    public ContentList(){}

    public Guid getContentListId() {
        return contentListId;
    }

    public void setContentListId(Guid contentListId) {
        this.contentListId = contentListId;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Extension getGenerator() {
        return generator;
    }

    public void setGenerator(Extension generator) {
        this.generator = generator;
    }

    public Extension getExpander() {
        return expander;
    }

    public void setExpander(Extension expander) {
        this.expander = expander;
    }

    public String getEditionType() {
        return editionType;
    }

    public void setEditionType(String editionType) {
        this.editionType = editionType;
    }

    public ItemFilter getItemFilter() {
        return itemFilter;
    }

    public void setItemFilter(ItemFilter itemFilter) {
        this.itemFilter = itemFilter;
    }
}
