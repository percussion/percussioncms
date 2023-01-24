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

package com.percussion.rest.displayformat;

import com.percussion.rest.ValueList;
import io.swagger.v3.oas.annotations.media.Schema;


@Schema(name="DisplayFormatProperty", description="Represents a property of a DisplayFormat. Properties may be multi valued or single valued.")
public class DisplayFormatProperty {

    @Schema(name="propertyId", description="The id for this property.")
    private String propertyId;
    @Schema(name="propertyName", description="The unique Name for this property.")
    private String propertyName;
    @Schema(name="propertyValue", description="For a single value property, the value of the property")
    private String propertyValue;
    @Schema(name="description", description="An optional description of this properties purpose")
    private String description;
    @Schema(name="propertyValues", description="For a multi value property, the list of current values of the property")
    private ValueList propertyValues;

    public DisplayFormatProperty(){}

    public String getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(String propertyId) {
        this.propertyId = propertyId;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getPropertyValue() {
        return propertyValue;
    }

    public void setPropertyValue(String propertyValue) {
        this.propertyValue = propertyValue;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ValueList getPropertyValues() {
        return propertyValues;
    }

    public void setPropertyValues(ValueList propertyValues) {
        this.propertyValues = propertyValues;
    }
}
