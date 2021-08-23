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

package com.percussion.rest.contenttypes;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.percussion.rest.Guid;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "ContentType")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description="Represents a Content Type")
public class ContentType {

    @Schema(required=true,description="The Content Type Id")
    private Guid guid;


	@Schema(required=false,description="Guid for the Object Type content Type")
	private Guid objectType;

	@Schema(required=false,description="A system unique name for the Content Type")
	private String name;

	@Schema(required=false,description="A human friendly label for this Content Type")
	private String label;

	@Schema(required=false,description="A human friendly Description of this Content Type's purpose")
	private String description;

	@Schema(required=false,description="The url to use to request a new Item of this Content Type")
	private String newRequest;

	@Schema(required=false,description="The url to use when searching for Items of this Content Type")
	private String queryRequest;

	@Schema(required=false,description="The url to use for updating an Item of this Content Type")
	private String updateRequest;

	@Schema(required=false,description="When true, this Content Type should be hidden from the user interface")
	private boolean hideFromMenu;

    public ContentType(){}

	public Guid getObjectType() {
		return objectType;
	}

	public void setObjectType(Guid objectType) {
		this.objectType = objectType;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getNewRequest() {
		return newRequest;
	}

	public void setNewRequest(String newRequest) {
		this.newRequest = newRequest;
	}

	public String getQueryRequest() {
		return queryRequest;
	}

	public void setQueryRequest(String queryRequest) {
		this.queryRequest = queryRequest;
	}

	public String getUpdateRequest() {
		return updateRequest;
	}

	public void setUpdateRequest(String updateRequest) {
		this.updateRequest = updateRequest;
	}

	public boolean isHideFromMenu() {
		return hideFromMenu;
	}

	public void setHideFromMenu(boolean hideFromMenu) {
		this.hideFromMenu = hideFromMenu;
	}

    public Guid getGuid() {
        return guid;
    }

    public void setGuid(Guid guid) {
        this.guid = guid;
    }
}
