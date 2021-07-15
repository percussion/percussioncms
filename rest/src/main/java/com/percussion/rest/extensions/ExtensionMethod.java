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
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "ExtensionMethod")
@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel(description = "Represents an Extension Method")
public class ExtensionMethod {

    @ApiModelProperty(name="name", value="name", notes="The name of the Extension Method")
    private String name;
    @ApiModelProperty(name="description", value="description", notes="The description of the Extension method")
    private String description = null;
    @ApiModelProperty(name="parameters", value="parameters", notes="A list of ExtensionParamater objects holding the parameters required by the method")
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
