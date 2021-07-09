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

import com.percussion.rest.Status;
import com.percussion.rest.errors.BackendException;
import com.percussion.rest.users.User;
import com.percussion.util.PSSiteManageBean;
import io.swagger.annotations.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

@PSSiteManageBean(value="restRolesResource")
@Path("/roles")
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@Api(value = "/roles", description = "Role operations")
public class RolesResource {

	private static final Logger log = LogManager.getLogger(RolesResource.class);

	 @Autowired
	 private IRoleAdaptor roleAdaptor;

    @Context
	 private UriInfo uriInfo;
	 
	 public RolesResource(){}

	   	@GET
	    @Path("/{roleName}")
	    @Produces(
	    {MediaType.APPLICATION_JSON})
	    @ApiOperation(value = "Get a Role", notes = "Will get the Role specified by the roleName.", response = User.class)
	    @ApiResponses(value = {
	      @ApiResponse(code = 200, message = "OK"),		
	      @ApiResponse(code = 404, message = "Role not found"),
	      @ApiResponse(code = 500, message = "Error message") 
	    })
	    public Role getRoleByName(@PathParam("roleName") String roleName)
	    {
			try {
				Role r = null;

				roleName = java.net.URLDecoder.decode(roleName, "UTF-8");

				r = roleAdaptor.getRole(uriInfo.getBaseUri(), roleName);
				return r;
			} catch (BackendException | UnsupportedEncodingException e) {
				log.error(e.getMessage());
				log.debug(e.getMessage(),e);
				throw new WebApplicationException(e);
			}
		}
	   	
	    @DELETE
	    @Path("/{roleName}")
	    @Consumes(MediaType.APPLICATION_JSON)
	    @Produces(MediaType.APPLICATION_JSON)
	    @ApiOperation(value = "Delete a Role", notes = "Will delete the specified roleName from the system.  If the Role is a Directory based Group, the link to the directory is removed but the Group will not be removed from the remote Directory."
	          , response = Status.class)
	    @ApiResponses(value = {
	      @ApiResponse(code = 200, message = "OK"),		
	      @ApiResponse(code = 404, message = "Role not found"),
	      @ApiResponse(code = 500, message = "Error message") 
	    })
	    public Status deleteRole(@ApiParam(value= "The roleName of the Role to delete." ,  name="roleName" ) @PathParam("roleName")
	    String roleName)
	    {
	    	int retCode = 404;
	    	String message = "User not found";
	    	
	        // Path param should be url decoded by default.  CXF jars interacting when running in cm1
	        try
	        {
	        	roleName = java.net.URLDecoder.decode(roleName, "UTF-8");
	        }
	        catch (UnsupportedEncodingException e)
	        {
	           retCode = 500;
	           message = e.getMessage();
	        }
	        
	        try{
	        	
	        	roleAdaptor.deleteRole(uriInfo.getBaseUri(), roleName);
	        	retCode = 200;
	        	message = "OK";
	        }catch(Exception e){
	        	retCode = 500;
	        	message = e.getMessage();
	        }
	        return new Status(retCode,  message);
	    }
	 
	    @PUT
	    @Path("/")
	    @Consumes(MediaType.APPLICATION_JSON)
	    @Produces(MediaType.APPLICATION_JSON)
	    @ApiOperation(value = "Create or update a Role", notes = "Creates or Updates the specified Role.  Returns the resulting Role.", response = Role.class)
	    @ApiResponses(value = {
	      @ApiResponse(code = 500, message = "Error"),
	      @ApiResponse(code = 200, message = "OK")
	      
	    })
	    public Role updateRole(@ApiParam(value= "The body containing a JSON payload" ,
                name="body" ) Role role)
	    {
	    	Role ret = null;
	   
	        ret = roleAdaptor.updateRole(uriInfo.getBaseUri(), role);
	        
	        return ret;
	 	 }
	    
	    
	    @GET
	    @Path("/list/{pattern}")
	    @Produces(
	    {MediaType.APPLICATION_JSON})
	    public RoleList findRoles()
	    {
	    	try {
				List<Role> ret = new ArrayList<>();

				ret = roleAdaptor.findRoles(uriInfo.getBaseUri(), "%");

				return new RoleList(ret);
			} catch (BackendException e) {
				log.error(e.getMessage());
				log.debug(e.getMessage(),e);
				throw new WebApplicationException(e);
			}

		}
	    
	    public IRoleAdaptor getRoleAdaptor()
	    {
	        return roleAdaptor;
	    }

	    public void setRoleAdaptor(IRoleAdaptor roleAdaptor)
	    {
	        this.roleAdaptor = roleAdaptor;
	    }

}
