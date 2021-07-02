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

package com.percussion.rest.displayformat;

import com.percussion.util.PSSiteManageBean;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.List;

@PSSiteManageBean(value="restDisplayFormatResource")
@Path("/displayformats")
@Api(value = "/displayformats", description = "Display Format operations")
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
    @ApiOperation(value = "Get a Display Format by name", notes = "Returns a Display Format that matches the name."
            , response = DisplayFormat.class)
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "No DisplayFormat found"),
            @ApiResponse(code = 500, message = "Error searching for DisplayFormat")
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
    @ApiOperation(value = "Get a Display Format by GUID", notes = "Returns a Display Format that matches the GUID."
            , response = DisplayFormat.class)
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "Display Format not found")
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
    @ApiOperation(value = "Find Display Formats", notes = "Returns a List of Display Formats."
            , response = List.class)
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "Display Format not found")
    })public DisplayFormatList findDisplayFormats(){
        List<DisplayFormat> ret = null;

        try{
            ret = adaptor.findAllDisplayFormats();
        }catch(Exception e){
            log.error("Error listing DisplayFormats", e);
            throw new WebApplicationException(e);
        }finally {
            return new DisplayFormatList(ret);
        }
    }

}
