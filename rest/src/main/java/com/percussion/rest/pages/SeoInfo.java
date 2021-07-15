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

package com.percussion.rest.pages;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@XmlRootElement(name = "SeoInfo")
@ApiModel(value="SeoInfo",description="Represents information about the seo.")
public class SeoInfo
{
    @ApiModelProperty(value="browserTitle", required=false,notes="Title shown in the browser.")
    private String browserTitle;

    @ApiModelProperty(value="metaDescription", required=false,notes="Description of the Meta Data of the page.")
    private String metaDescription;

    @ApiModelProperty(value="hideSearch", required=false,notes="Flag to mark as searchable.")
    private Boolean hideSearch;

    @ApiModelProperty(value="tags", required=false,notes="List of tags marking the page.")
    private List<String> tags;

    @ApiModelProperty(value="categories", required=false,notes="List of categories within the page.")
    private List<String> categories;

    public String getBrowserTitle()
    {
        return browserTitle;
    }

    public void setBrowserTitle(String browserTitl)
    {
        this.browserTitle = browserTitl;
    }

    public String getMetaDescription()
    {
        return metaDescription;
    }

    public void setMetaDescription(String metaDescription)
    {
        this.metaDescription = metaDescription;
    }

    public Boolean getHideSearch()
    {
        return hideSearch;
    }

    public void setHideSearch(Boolean hideSearch)
    {
        this.hideSearch = hideSearch;
    }

    public List<String> getTags()
    {
        return tags;
    }

    public void setTags(List<String> tags)
    {
        this.tags = tags;
    }

    public List<String> getCategories()
    {
        return categories;
    }

    public void setCategories(List<String> categories)
    {
        this.categories = categories;
    }

}
