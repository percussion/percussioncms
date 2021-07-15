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

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "ItemFilterRuleDefinition")
@ApiModel( description = "Represents an ItemFilter Rule")
public class ItemFilterRuleDefinition {

    private Guid ruleId;

    private String name;

    private List<ItemFilterRuleDefinitionParam> params;


    public ItemFilterRuleDefinition(){}


    /**
     * Primary key
     */
    public Guid getRuleId() {
        return ruleId;
    }

    public void setRuleId(Guid ruleId) {
        this.ruleId = ruleId;
    }

    /**
     * Name of the rule referenced from the extensions manager, never
     * <code>null</code> or empty after construction
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * A rule can reference parameters that control how the rule will  be
     * invoked. The parameters can be overridden when the rule is invoked.
     */
    public List<ItemFilterRuleDefinitionParam> getParams() {
        return params;
    }

    public void setParams(List<ItemFilterRuleDefinitionParam> params) {
        this.params = params;
    }
}
