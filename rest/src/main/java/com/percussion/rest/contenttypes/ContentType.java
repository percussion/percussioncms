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
