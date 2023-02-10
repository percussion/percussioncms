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
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "ExtensionMethod")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Represents an Extension Method")
public class ExtensionMethod {

    @Schema(name="name", description="The name of the Extension Method")
    private String name;
    @Schema(name="description", description="The description of the Extension method")
    private String description = null;
    @ArraySchema(schema = @Schema(implementation = ExtensionParameter.class))
    private List<ExtensionParameter> parameters;

    public ExtensionMethod(){}

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

    public List<ExtensionParameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<ExtensionParameter> parameters) {
        this.parameters = parameters;
    }
}
