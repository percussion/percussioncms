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

package com.percussion.rest.displayformat;

import com.percussion.util.PSSiteManageBean;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.List;

@PSSiteManageBean(value="restDisplayFormatResource")
@Path("/displayformats")
@Tag(name = "Display Formats", description = "Display Format operations")
public class DisplayFormatResource {

    private static final Logger log = LogManager.getLogger(DisplayFormatResource.class);

    @Autowired
    private IDisplayFormatAdaptor adaptor;

    @Context
    private UriInfo uriInfo;

    public DisplayFormatResource(){}

    @GET
    @Path("/by-name/{name}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get a Display Format by name", description = "Returns a Display Format that matches the name."
            , responses =
            {
                    @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = DisplayFormat.class))),
                    @ApiResponse(responseCode = "404", description = "No DisplayFormat found"),
                    @ApiResponse(responseCode = "500", description = "Error searching for DisplayFormat")
    })
    public DisplayFormat getDisplayFormatByName(@PathParam("name") String name){
        DisplayFormat ret = null;

        try {
            ret= adaptor.findDisplayFormat(name);
        }catch(Exception e){
            log.error("Error retrieving DisplayFormat by name", e);
            throw new WebApplicationException(e);
        }finally{
            return ret;
        }
    }

    @GET
    @Path("/by-guid/{guid}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get a Display Format by GUID",
            description = "Returns a Display Format that matches the GUID."
            , responses =
            {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(schema = @Schema(implementation = DisplayFormat.class))),
                    @ApiResponse(responseCode = "404", description = "No DisplayFormat found"),
                    @ApiResponse(responseCode = "500", description = "Error searching for DisplayFormat")
            })
    public DisplayFormat getDisplayFormatByGuid(@PathParam("guid") String guid){
        DisplayFormat ret = null;

        try{
            ret = adaptor.findDisplayFormat(guid);
        }catch(Exception e){
            log.error("Error retrieving DisplayFormat by guid", e);
            throw new WebApplicationException(e);
        }finally {
            return ret;
        }
    }

    @GET
    @Path("/all")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Find Display Formats", description = "Returns a List of Display Formats."
            , responses =
            {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(array=
                                    @ArraySchema( schema = @Schema(implementation = DisplayFormat.class)))),
                    @ApiResponse(responseCode = "404", description = "No DisplayFormat found"),
                    @ApiResponse(responseCode = "500", description = "Error searching for DisplayFormat")
            })public DisplayFormatList findDisplayFormats(){
        List<DisplayFormat> ret = null;

        try{
            ret = adaptor.findAllDisplayFormats();
        }catch(Exception e){
            log.error("Error listing DisplayFormats", e);
            throw new WebApplicationException(e);
        }
        return new DisplayFormatList(ret);
    }

}
