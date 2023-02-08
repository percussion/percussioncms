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
