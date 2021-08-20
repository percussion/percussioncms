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
