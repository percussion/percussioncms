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

package com.percussion.rest.users;

import com.percussion.rest.Status;
import com.percussion.rest.errors.BackendException;
import com.percussion.util.PSSiteManageBean;
import io.swagger.annotations.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

@PSSiteManageBean(value="restUsersResource")
@Path("/users")
@XmlRootElement
@Api(value = "/users", description = "User operations")
public class UsersResource {

	private static final Logger log = LogManager.getLogger(UsersResource.class);

	 @Autowired
     private IUserAdaptor userAdaptor;
     

    @Context
	 private UriInfo uriInfo;
	 
	 
	   	@GET
	    @Path("/{userName}")
	    @Produces(
	    {MediaType.APPLICATION_JSON})
	    @ApiOperation(value = "Get a User", notes = "Will get the User specified by the userName.", response = User.class)
	    @ApiResponses(value = {
	      @ApiResponse(code = 200, message = "OK"),		
	      @ApiResponse(code = 404, message = "User not found"),
	      @ApiResponse(code = 500, message = "Error message") 
	    })
	    public User getUserByName(@PathParam("userName") String userName) {
            User u = null;

            try {
                userName = java.net.URLDecoder.decode(userName, "UTF-8");


                u = userAdaptor.getUser(uriInfo.getBaseUri(), userName);

                return u;
            } catch (Exception e) {
                throw new WebApplicationException(e.getMessage(),e,500);
            }
        }

	   	
	    @DELETE
	    @Path("/{userName}")
	    @Consumes(MediaType.APPLICATION_JSON)
	    @Produces(MediaType.APPLICATION_JSON)
	    @ApiOperation(value = "Delete a user", notes = "Will delete the specified userName from the system.  If the user is a Directory based user, the link to the directory is removed but the user will not be removed from the remote Directory."
	            + "<br/> Send a DELETE request using the userName"
	            + "<br/> Example URL: http://localhost:9992/Rhythmyx/rest/users/userNameToDelete .", response = Status.class)
	    @ApiResponses(value = {
	      @ApiResponse(code = 200, message = "OK"),		
	      @ApiResponse(code = 404, message = "User not found"),
	      @ApiResponse(code = 500, message = "Error message") 
	    })
	    public Status deleteUser(@ApiParam(value= "The userName of the user to delete." ,  name="userName" ) @PathParam("userName")
	    String userName)
	    {
	    	int retCode = 404;
	    	String message = "User not found";
	    	
	        // Path param should be url decoded by default.  CXF jars interacting when running in cm1
	        try
	        {
	            userName = java.net.URLDecoder.decode(userName, "UTF-8");
	        }
	        catch (UnsupportedEncodingException e)
	        {
	           retCode = 500;
	           message = e.getMessage();
	        }
	        
	        try{
	        	
	        	userAdaptor.deleteUser(uriInfo.getBaseUri(), userName);
	        	retCode = 200;
	        	message = "OK";
	        }catch(Exception e){
	        	retCode = 500;
	        	message = e.getMessage();
	        }
	        return new Status(retCode,  message);
	    }
	 
	    @PUT
	    @Path("/{userName}")
	    @Consumes(MediaType.APPLICATION_JSON)
	    @Produces(MediaType.APPLICATION_JSON)
	    @ApiOperation(value = "Create or update a User", notes = "Creates or Updates the specified User.  Returns the resulting User.", response = User.class)
	    @ApiResponses(value = {
	      @ApiResponse(code = 500, message = "Error"),
	      @ApiResponse(code = 200, message = "OK")
	      
	    })
	    public User updateUser(@ApiParam(value= "The body containing a JSON payload" ,  name="body" )User user, 
	            @ApiParam(value= "The userName to be updated or created" ,  name="userName" ) @PathParam("userName")
	    String userName)
	    {
	    	try {
				User ret = null;

				userName = java.net.URLDecoder.decode(userName, "UTF-8");

				ret = userAdaptor.updateOrCreateUser(uriInfo.getBaseUri(), user);

				return ret;
			} catch (BackendException | UnsupportedEncodingException e) {
	    		log.error(e.getMessage());
	    		log.debug(e.getMessage(),e);
				throw new WebApplicationException(e);
			}
		}
	    
	    
	    @GET
	    @Path("/list/{pattern}")
	    @Produces(
	    {MediaType.APPLICATION_JSON})
	    @ApiOperation(value = "Find a list of users by name", notes = "Returns a list of user names. The % Wildcard character may be used in searches.  The wildcard should be URL encoded as %25 on the URL.", response = List.class)
	    @ApiResponses(value = {
	      @ApiResponse(code = 500, message="Error"),
	      @ApiResponse(code = 404, message = "Not Found"),
	      @ApiResponse(code = 200, message = "OK")
	      
	    })
	    public List<String> findUsers(@PathParam("pattern") String pattern)
	    {
	    	try {
				List<String> ret = new ArrayList<>();

				pattern = java.net.URLDecoder.decode(pattern, "UTF-8");


				ret = userAdaptor.findUsers(uriInfo.getBaseUri(), pattern);

				return ret;
			} catch (BackendException | UnsupportedEncodingException e) {
				log.error(e.getMessage());
				log.debug(e.getMessage(),e);
				throw new WebApplicationException(e);
			}

		}
	    

	    @GET
	    @Path("/directory/status")
	    @Produces(
	    {MediaType.APPLICATION_JSON})
	    @ApiOperation(value = "Check the status of the Active Directory or LDAP connection for external directories.", notes = "Directory configuration is optional, use this method to verify that the Directory connection is configured and working.", response = Status.class)
	    @ApiResponses(value = {
	      @ApiResponse(code = 500, message="Error"),
	      @ApiResponse(code = 404, message = "Not Found"),
	      @ApiResponse(code = 200, message = "OK")
	      
	    })
	 
	    public Status checkDirectoryStatus()
	    {
	    	//Assume failure
	    	Status status = null;
	    	
	    	status = userAdaptor.checkDirectoryStatus();
	    	if(status == null){
	    		status = new Status(404,"Not Found");
	    	}
	    	
	    	return status;
	    }
	    
	    
	    @GET
	    @Path("/directory/list/{pattern}")
	    @Produces(
	    {MediaType.APPLICATION_JSON})
	    @ApiOperation(value = "Search for users in External Directories", notes = "Searches results are limited to 200 users. Prior to calling this method, check the directory status.  Search pattern may include a Wildcard with the % character.  This should be URL encoded on the URL with a %25 character.", response = List.class)
	    @ApiResponses(value = {
	      @ApiResponse(code = 200, message = "OK")
	      
	    })
	    public List<String> searchDirectoryService(@PathParam("pattern") String pattern)
	    {
	    	List<String> ret = null;
	    	
	    	   try
		        {
		            pattern = java.net.URLDecoder.decode(pattern, "UTF-8");
		        }
		        catch (UnsupportedEncodingException e)
		        {
		            // UTF-8 always supported
		        }
	    	
	    	ret = userAdaptor.searchDirectory(pattern);
	    
	    	if(ret == null)
	    		ret = new ArrayList<>();
	    	return ret;
	    }
	    
	    
	    public IUserAdaptor getUserAdaptor()
	    {
	        return userAdaptor;
	    }

	    public void setUserAdaptor(IUserAdaptor userAdaptor)
	    {
	        this.userAdaptor = userAdaptor;
	    }

}



