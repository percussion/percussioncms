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

package com.percussion.rest.extensions;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "ExtensionParameter")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description="Represents an Extension Parameter with value")
public class ExtensionParameter {

    @Schema(name="name", required=true,description="The name of the Extension Parameter")
    private      String          name = "";

    @Schema(name="description", required=false,description="The description of the Extension Parameter")
    private      String          description = "";

    @Schema(name="dataType", required=false,description="The Data Type of the Extension Parameter")
    private      String          dataType = "";

    @Schema(name="required", required=false,description="When true, indicates that this is a required parameter for the Extension")
    private boolean required;

    @Schema(name="value", required=false,description="The current value of the type in String form. Use the data type for client side type conversion. May be null or empty.")
    private String value;

    public ExtensionParameter(){}

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

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getValue(){
        return value;
    }

    public void setValue(String value){
        this.value = value;
    }
}
