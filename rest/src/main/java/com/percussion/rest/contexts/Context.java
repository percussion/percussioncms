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

package com.percussion.rest.contexts;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.percussion.rest.Guid;
import com.percussion.rest.locationscheme.LocationScheme;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "Context")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Represents a publishing Context")
public class Context {

    @Schema(name = "id", description = "The unique identifier for the Context use string value to persist the id.")
    private Guid id;

    @Schema(name="version", description="Ignored")
    private Integer version;

    @Schema(name="name", description="The unique name for the publishing Context",required=true)
    private
    String name;

    @Schema(name="description", description="Human friendly description for the publishing Context")
    private
    String description;

    @Schema(name="defaultScheme", description="The default Location Scheme to use when publishing to this Context")
    private
    LocationScheme defaultScheme;

    @Schema(name="locationSchemes", description="The list of Location Schemes configured for this publishing Context")
    private
    List<LocationScheme> locationSchemes;

    public Context(){}

    public Guid getId() {
        return id;
    }

    public void setId(Guid id) {
        this.id = id;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
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

    public LocationScheme getDefaultScheme() {
        return defaultScheme;
    }

    public void setDefaultScheme(LocationScheme defaultScheme) {
        this.defaultScheme = defaultScheme;
    }

    public List<LocationScheme> getLocationSchemes() {
        return locationSchemes;
    }

    public void setLocationSchemes(List<LocationScheme> locationSchemes) {
        this.locationSchemes = locationSchemes;
    }
}
