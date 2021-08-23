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

package com.percussion.rest.itemfilter;

import com.percussion.rest.Guid;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Set;

@XmlRootElement(name = "ItemFilter")
@ApiModel(description = "Represents an ItemFilter")
public class ItemFilter {

    /**
     * Primary key for an item filter
     */
    @ApiModelProperty(name = "filterId", value = "The unique Item Filter GUID")
    private Guid filterId;

    /**
     * Name of the filter rule, never <code>null</code> or empty after
     * construction
     */
    @ApiModelProperty(name = "name", value = "A system unique name for this Item Filter", required = true)
    private String name;

    /**
     * Description of the rule, may be <code>null</code> or empty
     */
    @ApiModelProperty(name = "description", value = "A human friendly description for the Item Filter")
    private String description;

    /**
     * The associated authtype, may be <code>null</code>
     */
    @ApiModelProperty(name = "legacyAuthtype", value = "Legacy AuthType", allowableValues = "[0,1,2,101]", notes = "Values map to All Content, All Public Content,Custom, Site Folder")
    private Integer legacyAuthtype;

    /**
     * The filter is an aggregation of rules to be applied to the items being
     * filtered.
     */
    @ApiModelProperty(name = "rules")
    private Set<ItemFilterRuleDefinition> rules;

    /**
     * Item filters can be changed, this member points to the parent filter,
     * if there is one
     */
    @ApiModelProperty(name = "parentFilter", value = "The Parent Item Filter if there is a parent configured.")
    private ItemFilter parentFilter;

    public ItemFilter(){}

    public Guid getFilterId() {
        return filterId;
    }

    public void setFilter_id(Guid filter_id) {
        this.filterId = filterId;
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

    public Integer getLegacyAuthtype() {
        return legacyAuthtype;
    }

    public void setLegacyAuthtype(Integer legacyAuthtype) {
        this.legacyAuthtype = legacyAuthtype;
    }

    public Set<ItemFilterRuleDefinition> getRules() {
        return rules;
    }

    public void setRules(Set<ItemFilterRuleDefinition> rules) {
        this.rules = rules;
    }

    public ItemFilter getParentFilter() {
        return parentFilter;
    }

    public void setParentFilter(ItemFilter parentFilter) {
        this.parentFilter = parentFilter;
    }


}
