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

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@XmlRootElement(name = "SeoInfo")
@Schema(name="SeoInfo",description="Represents information about the seo.")
public class SeoInfo
{
    @Schema(name="browserTitle", required=false,description="Title shown in the browser.")
    private String browserTitle;

    @Schema(name="metaDescription", required=false,description="Description of the Meta Data of the page.")
    private String metaDescription;

    @Schema(name="hideSearch", required=false,description="Flag to mark as searchable.")
    private Boolean hideSearch;

    @Schema(name="tags", required=false,description="List of tags marking the page.")
    private List<String> tags;

    @Schema(name="categories", required=false,description="List of categories within the page.")
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
