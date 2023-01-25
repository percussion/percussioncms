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

package com.percussion.rest.roles;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;


@XmlRootElement(name = "Role")
@Schema(name = "Role", description = "Represents a system Role that a user may belong to.")
public class Role {

	@Schema(name="name", required=true,description="A unique name for the role.")	
	private String name;
	
	@Schema(name="description", required=true,description="A friendly description of the Role's purpose.")	
	private String description;
	
	@Schema(name="homePage", required=true,description="The default home page for the Role.  Valid values are: Dashboard, Editor, or Home")
	private String homePage;
	
	@ArraySchema(schema=@Schema(implementation = String.class,name="users", required=true,description="A list of the user name's linked to this role."))
	private List<String> users;
	
	
	public String getName() {
		if(name == null)
			name = "";
		
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		if(description==null)
			description = "";
		
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getHomePage() {
		if(homePage == null)
			homePage = "";
		
		return homePage;
	}
	public void setHomePage(String homePage) {
		this.homePage = homePage;
	}
	public List<String> getUsers() {
		if(users == null)
			users = new ArrayList<>();
		return users;
	}

	public void setUsers(List<String> users) {
		this.users = users;
	}

	public Role(){}
}
