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

package com.percussion.rest.locationscheme;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.percussion.rest.Guid;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "LocationScheme")
@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel(description="Represents a Location Scheme")
public class LocationScheme {

	@ApiModelProperty(name="schemeId",value="A unique guid for the Location scheme.")
	private Guid schemeId;

	@ApiModelProperty(name="name",value="A unique name for this location scheme.", required=true)
	private
	String name;

	@ApiModelProperty(name="description", value="Human friendly description describing the location scheme")
	private
	String description;

	@ApiModelProperty(name="template", value="The Template that this location scheme is configured for")
	private
	long templateId;

	@ApiModelProperty(name="contentType", value= "The Content Type id that this location scheme is configured for")
	private
	long contentTypeId;

	@ApiModelProperty(name="context", value="The Publishing Context that this location scheme is linked to")
	private
	Guid context;

	@ApiModelProperty(name = "locationSchemeGenerator", value="The Location Scheme Generator")
	private
	String locationSchemeGenerator;

	@ApiModelProperty(name ="parameters", value="Location Scheme Parameters" )
	private
	LocationSchemeParameterList parameters;

    public LocationScheme(){}

	public Guid getSchemeId() {
		return schemeId;
	}

	public void setSchemeId(Guid schemeId) {
		this.schemeId = schemeId;
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

	public long getTemplateId() {
		return templateId;
	}

	public void setTemplateId(long templateId) {
		this.templateId = templateId;
	}

	public long getContentTypeId() {
		return contentTypeId;
	}

	public void setContentTypeId(long contentTypeId) {
		this.contentTypeId = contentTypeId;
	}

	public Guid getContext() {
		return context;
	}

	public void setContext(Guid context) {
		this.context = context;
	}

	public String getLocationSchemeGenerator() {
		return locationSchemeGenerator;
	}

	public void setLocationSchemeGenerator(String locationSchemeGenerator) {
		this.locationSchemeGenerator = locationSchemeGenerator;
	}

	public LocationSchemeParameterList getParameters() {
		return parameters;
	}

	public void setParameters(LocationSchemeParameterList parameters) {
		this.parameters = parameters;
	}
}
