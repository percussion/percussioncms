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

package com.percussion.rest.preferences;


import com.percussion.error.PSExceptionUtils;
import com.percussion.rest.Status;
import com.percussion.util.PSSiteManageBean;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@PSSiteManageBean(value="restPreferencesResource")
@Path("/preferences")
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@Tag(name = "User Preferences", description = "User Preference operations")
public class PreferenceResource {

    private  static final Logger log = LogManager.getLogger(PreferenceResource.class);

    @Autowired
    IPreferenceAdaptor adaptor;

    @Path("/")
    @GET
    @Produces(value= MediaType.APPLICATION_JSON)
    @Operation(summary="Get all stored user preferences for the given user",
        responses = {
            @ApiResponse(responseCode = "200", description = "OK",content = @Content(
                    array=@ArraySchema(schema = @Schema(implementation = UserPreference.class))
            )),
            @ApiResponse(responseCode = "404", description = "No preferences found"),
            @ApiResponse(responseCode = "500", description = "Error message")
    })
    public UserPreferenceList getAllUserPreferences(){

        try {
            return adaptor.getAllUserPreferences();
        }catch(NotFoundException e){
            log.debug("Didn't find any preferences");
            throw new WebApplicationException("No preferences found.",404);
        }catch(Exception e){
            log.error("An unexpected error occurred getting preferences. {}",PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            throw new WebApplicationException(e.getMessage(),e,500);
        }
   }

    @Path("/all")
    @PUT
    @Produces(value= MediaType.APPLICATION_JSON)
    @Consumes(value=MediaType.APPLICATION_JSON)
    @Operation(summary="Save all  user preferences for the current user",
    responses= {
            @ApiResponse(responseCode = "200", description = "OK", content=@Content(
                    array = @ArraySchema(schema=@Schema(implementation = UserPreference.class))
            )),
            @ApiResponse(responseCode = "404", description = "No preferences found"),
            @ApiResponse(responseCode = "500", description = "Error message")
    })
    public UserPreferenceList saveAllUserPreferences(@Parameter(required=true) UserPreferenceList preferences){

        try {
            return adaptor.saveAllUserPreferences(preferences);
        }catch(NotFoundException e){
            log.debug("Didn't find any preferences");
            throw new WebApplicationException("No preferences found.",404);
        }catch(Exception e){
            log.error("An unexpected error occurred saving preferences. {}",PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            throw new WebApplicationException(e.getMessage(),e,500);
        }
    }

    @Path("/{preference}")
    @GET
    @Produces(value= MediaType.APPLICATION_JSON)
    @Operation(summary="Get a specific user preferences for the logged in user",
        responses= {
            @ApiResponse(responseCode = "200", description = "OK", content=@Content(
                    schema = @Schema(implementation = UserPreference.class))),
            @ApiResponse(responseCode = "404", description = "No preference found"),
            @ApiResponse(responseCode = "500", description = "Error message")
    })
    public UserPreference loadPreference(
            @PathParam("preference") String preference){
        try {
            return adaptor.loadPreference(preference);
        }catch(NotFoundException e){
        log.debug("Didn't find a match for preference {}", preference );
        throw new WebApplicationException("No preference found.",404);
    }catch(Exception e){
        log.error("An unexpected error occurred getting preference: {}, Error: {}",preference ,e.getMessage());
        log.debug(PSExceptionUtils.getDebugMessageForLog(e));
        throw new WebApplicationException(e.getMessage(),e,500);
    }
    }


    @PUT
    @Path("/")
    @Produces(value= MediaType.APPLICATION_JSON)
    @Consumes(value= MediaType.APPLICATION_JSON)
    @Operation(summary="Saves a specific user preference for the given user. username must be set on preference."
        ,responses={
            @ApiResponse(responseCode =  "200", description = "OK",content=
            @Content(schema = @Schema(implementation = UserPreference.class))),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Error message")
    })
    public UserPreference savePreference(UserPreference pref){
        try{
            if(StringUtils.isEmpty(pref.getCategory()))
                pref.setCategory("sys_preferences");

            if(StringUtils.isEmpty(pref.getContext()))
                pref.setContext("private");

            return adaptor.savePreference(pref);
        }catch(NotFoundException e){
            log.debug("Didn't find a match for preference {} for user: {}", pref.getName(), pref.getUserName());
            throw new WebApplicationException("No preference found.",404);
        }catch(Exception e){
            log.error("An unexpected error occurred saving preference: {} for userName: {}, Error: {}",pref.getName(), pref.getUserName(),e.getMessage());
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            throw new WebApplicationException(e.getMessage(),e,500);
        }
    }

    @DELETE
    @Path("/")
    @Produces(value= MediaType.APPLICATION_JSON)
    @Consumes(value= MediaType.APPLICATION_JSON)
    @Operation(summary="Delete a specific user preferences for the given user",
    responses = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "No preference found"),
            @ApiResponse(responseCode = "500", description = "Error message")
    })
    public Status deletePreference(UserPreference pref){
        Status ret = new Status(200,"OK");

        try{
            adaptor.deletePreference(pref);
        }catch(NotFoundException e){
            log.debug("Didn't find a match for preference {} for user: {}", pref.getName(), pref.getUserName());
            throw new WebApplicationException("No preference found.",404);
        }catch(Exception e){
            log.error("An unexpected error occurred saving preference: {} for userName: {}, Error: {}",pref.getName(), pref.getUserName(),e.getMessage());
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            throw new WebApplicationException(e.getMessage(),e,500);
        }

        return ret;
    }
}
