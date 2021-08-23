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

package com.percussion.rest.displayformat;

import com.percussion.rest.ValueList;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;


@ApiModel(description="Represents a property of a DisplayFormat. Properties may be multi valued or single valued.")
public class DisplayFormatProperty {

    @ApiModelProperty(name="propertyId", notes="The id for this property.")
    private String propertyId;
    @ApiModelProperty(name="propertyName", notes="The unique Name for this property.")
    private String propertyName;
    @ApiModelProperty(name="propertyValue", notes="For a single value property, the value of the property")
    private String propertyValue;
    @ApiModelProperty(name="description", notes="An optional description of this properties purpose")
    private String description;
    @ApiModelProperty(name="propertyValues", notes="For a multi value property, the list of current values of the property")
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
