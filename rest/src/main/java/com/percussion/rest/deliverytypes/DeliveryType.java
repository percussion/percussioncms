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

package com.percussion.rest.deliverytypes;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.percussion.rest.Guid;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "DeliveryType")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description="Represents a Delivery Type.")
public class DeliveryType {

	   @Schema(required=false,description="id must match id supplied on url.  Typically not sent to the server.")
	   Guid id;

	   @Schema(required=true,description="The name of the DeliveryType.  Must be unique.")
	   String name;

	   @Schema(required=false,description="A friendly description of this DeliveryType")
	   String description;

	   @Schema(required=false,description="The Spring bean that implements this DeliveryType.  Typically configured in Rhythmyx/WEB-INF/config/user/spring/publisher-beans.xml")		  
	   String beanName;

	   @Schema( required=false,description="When set to true, Assembly will be invoked during Unpublishing operations for this DeliveryType")
	   boolean unpublishingRequiresAssembly;

	   public DeliveryType(){}

	public Guid getId() {
		return id;
	}

	public void setId(Guid id) {
		this.id = id;
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

	public String getBeanName() {
		return beanName;
	}

	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}

	public boolean getUnpublishingRequiresAssembly() {
		return unpublishingRequiresAssembly;
	}

	public void setUnpublishingRequiresAssembly(boolean unpublishingRequiresAssembly) {
		this.unpublishingRequiresAssembly = unpublishingRequiresAssembly;
	}
	   
	   
	

}
