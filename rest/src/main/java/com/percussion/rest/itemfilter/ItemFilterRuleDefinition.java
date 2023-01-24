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
import java.util.List;

@XmlRootElement(name = "ItemFilterRuleDefinition")
@Schema( description = "Represents an ItemFilter Rule")
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
