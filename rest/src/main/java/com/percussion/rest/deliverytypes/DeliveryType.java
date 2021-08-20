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
