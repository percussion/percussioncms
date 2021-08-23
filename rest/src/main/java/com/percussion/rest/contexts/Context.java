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

package com.percussion.rest.contexts;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.percussion.rest.Guid;
import com.percussion.rest.locationscheme.LocationScheme;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "Context")
@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel(description = "Represents a publishing Context")
public class Context {

    @ApiModelProperty(name = "id", value = "The unique identifier for the Context use string value to persist the id.")
    private Guid id;

    @ApiModelProperty(name="version", value="Ignored")
    private Integer version;

    @ApiModelProperty(name="name", value="The unique name for the publishing Context",required=true)
    private
    String name;

    @ApiModelProperty(name="description", value="Human friendly description for the publishing Context")
    private
    String description;

    @ApiModelProperty(name="defaultScheme", value="The default Location Scheme to use when publishing to this Context")
    private
    LocationScheme defaultScheme;

    @ApiModelProperty(name="locationSchemes", value="The list of Location Schemes configured for this publishing Context")
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
