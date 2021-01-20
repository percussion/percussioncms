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

package com.percussion.rest.contentlists;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.percussion.rest.Guid;
import com.percussion.rest.extensions.Extension;
import com.percussion.rest.itemfilter.ItemFilter;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "ContentList")
@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel(description = "Represents a Content List")
public class ContentList {

    @ApiModelProperty(value = "contentListId", required = false, notes = "The unique ID for this ContentList.")
    private
    Guid contentListId;

    @ApiModelProperty(value = "version", required = false, notes = "Ignored.")
    private Integer version;

    @ApiModelProperty(value = "name", required = false, notes = "The name of the Content List. Must be unique.")
    private
    String name;

    @ApiModelProperty(value = "description", required = false, notes = "A human friendly description of the Content List.")
    private
    String description;

    @ApiModelProperty(value = "type", required = false, notes = "The type of the ContentList.", allowableValues = "[Normal,Incremental]")
    private
    String type = "Normal";

    @ApiModelProperty(value = "url", required = false, notes = "The URL for this ContentList")
    private
    String url;

    @ApiModelProperty(value = "generator", required = false, notes = "The ContentList Generator configured for this ContentList")
    private
    Extension generator;

    @ApiModelProperty(value = "expander", required = false, notes = "The ContentList Template Expander configured for this ContentList")
    private
    Extension expander;

    @ApiModelProperty(value = "editionType", required = true, notes = "Indicates the type of Edition (Publish or Unpublish then Publish)", allowableValues = "[Publish,Unpublish Then Publish]")
    private
    String editionType;

    @ApiModelProperty(value = "itemFilter", required = true, notes = "The ItemFilter used to filter content returned by this Content List.")
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
