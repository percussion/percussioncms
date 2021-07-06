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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.rest.preferences;


import com.percussion.rest.Status;
import com.percussion.util.PSSiteManageBean;
import io.swagger.annotations.*;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@PSSiteManageBean(value="restPreferencesResource")
@Path("/preferences")
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@Api(value = "/preferences", description = "User Preference operations")
public class PreferenceResource {

    final static Logger log = LogManager.getLogger(PreferenceResource.class);

    @Autowired
    IPreferenceAdaptor adaptor;

    @Path("/")
    @GET
    @ApiOperation(value="Get all stored user preferences for the given user")
    @Produces(value= MediaType.APPLICATION_JSON)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "No preferences found"),
            @ApiResponse(code = 500, message = "Error message")
    })
    public UserPreferenceList getAllUserPreferences(){

        try {
            return adaptor.getAllUserPreferences();
        }catch(NotFoundException e){
            log.debug("Didn't find any preferences");
            throw new WebApplicationException("No preferences found.",404);
        }catch(Exception e){
            log.error("An unexpected error occurred getting preferences.",e);
            throw new WebApplicationException(e.getMessage(),e,500);
        }
   }

    @Path("/all")
    @PUT
    @ApiOperation(value="Save all  user preferences for the current user")
    @Produces(value= MediaType.APPLICATION_JSON)
    @Consumes(value=MediaType.APPLICATION_JSON)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "No preferences found"),
            @ApiResponse(code = 500, message = "Error message")
    })
    public UserPreferenceList saveAllUserPreferences(@ApiParam(required=true) UserPreferenceList preferences){

        try {
            return adaptor.saveAllUserPreferences(preferences);
        }catch(NotFoundException e){
            log.debug("Didn't find any preferences");
            throw new WebApplicationException("No preferences found.",404);
        }catch(Exception e){
            log.error("An unexpected error occurred saving preferences.",e);
            throw new WebApplicationException(e.getMessage(),e,500);
        }
    }

    @Path("/{preference}")
    @GET
    @ApiOperation(value="Get a specific user preferences for the logged in user")
    @Produces(value= MediaType.APPLICATION_JSON)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "No preference found"),
            @ApiResponse(code = 500, message = "Error message")
    })
    public UserPreference loadPreference(
            @PathParam("preference") String preference){
        try {
            return adaptor.loadPreference(preference);
        }catch(NotFoundException e){
        log.debug("Didn't find a match for preference" + preference );
        throw new WebApplicationException("No preference found.",404);
    }catch(Exception e){
        log.error("An unexpected error occurred getting preference: " +preference ,e);
        throw new WebApplicationException(e.getMessage(),e,500);
    }
    }


    @PUT
    @Path("/")
    @ApiOperation(value="Saves a specific user preference for the given user. username must be set on preference.")
    @Produces(value= MediaType.APPLICATION_JSON)
    @Consumes(value= MediaType.APPLICATION_JSON)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "User not found"),
            @ApiResponse(code = 500, message = "Error message")
    })
    public UserPreference savePreference(UserPreference pref){
        try{
            if(StringUtils.isEmpty(pref.getCategory()))
                pref.setCategory("sys_preferences");

            if(StringUtils.isEmpty(pref.getContext()))
                pref.setContext("private");

            return adaptor.savePreference(pref);
        }catch(NotFoundException e){
            log.debug("Didn't find a match for preference" + pref.getName() + " for user:" + pref.getUserName());
            throw new WebApplicationException("No preference found.",404);
        }catch(Exception e){
            log.error("An unexpected error occurred saving preference: " +pref.getName() + " for userName: " + pref.getUserName(),e);
            throw new WebApplicationException(e.getMessage(),e,500);
        }
    }

    @DELETE
    @Path("/")
    @ApiOperation(value="Delete a specific user preferences for the given user")
    @Produces(value= MediaType.APPLICATION_JSON)
    @Consumes(value= MediaType.APPLICATION_JSON)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "No preference found"),
            @ApiResponse(code = 500, message = "Error message")
    })
    public Status deletePreference(UserPreference pref){
        Status ret = new Status(200,"OK");

        try{
            adaptor.deletePreference(pref);
        }catch(NotFoundException e){
            log.debug("Didn't find a match for preference" + pref.getName() + " for user:" + pref.getUserName());
            throw new WebApplicationException("No preference found.",404);
        }catch(Exception e){
            log.error("An unexpected error occurred deleting preference: " +pref.getName() + " for userName: " + pref.getUserName(),e);
            throw new WebApplicationException(e.getMessage(),e,500);
        }

        return ret;
    }
}
