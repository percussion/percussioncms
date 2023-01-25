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

package com.percussion.rest.itemfilter;

import com.percussion.rest.Guid;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Set;

@XmlRootElement(name = "ItemFilter")
@Schema(description = "Represents an ItemFilter")
public class ItemFilter {

    /**
     * Primary key for an item filter
     */
    @Schema(name = "filterId", description="The unique Item Filter GUID")
    private Guid filterId;

    /**
     * Name of the filter rule, never <code>null</code> or empty after
     * construction
     */
    @Schema(name = "name", description="A system unique name for this Item Filter", required = true)
    private String name;

    /**
     * Description of the rule, may be <code>null</code> or empty
     */
    @Schema(name = "description", description="A human friendly description for the Item Filter")
    private String description;

    /**
     * The associated authtype, may be <code>null</code>
     */
    @Schema(name = "legacyAuthtype", allowableValues = "[0,1,2,101]", description = "Values map to All Content, All Public Content,Custom, Site Folder")
    private Integer legacyAuthtype;

    /**
     * The filter is an aggregation of rules to be applied to the items being
     * filtered.
     */
    @Schema(name = "rules")
    private Set<ItemFilterRuleDefinition> rules;

    /**
     * Item filters can be changed, this member points to the parent filter,
     * if there is one
     */
    @Schema(name = "parentFilter", description="The Parent Item Filter if there is a parent configured.")
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
