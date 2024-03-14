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
import io.swagger.v3.oas.annotations.media.ExampleObject;
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
import java.util.ArrayList;
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

    public DisplayFormatResource(){
        //NOOP
    }

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
        }

        return ret;

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
        }

        return ret;
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
                                    @ArraySchema( schema = @Schema(implementation = DisplayFormat.class)),
                            examples=@ExampleObject(value="{\n" +
                                    "  \"DisplayFormatList\": [\n" +
                                    "    {\n" +
                                    "      \"ascendingSort\": false,\n" +
                                    "      \"columns\": [\n" +
                                    "        {\n" +
                                    "          \"ascendingSort\": true,\n" +
                                    "          \"categorized\": false,\n" +
                                    "          \"dateType\": false,\n" +
                                    "          \"descendingSort\": false,\n" +
                                    "          \"description\": \"The title of the item.\",\n" +
                                    "          \"displayId\": 4,\n" +
                                    "          \"displayName\": \"Content Title\",\n" +
                                    "          \"groupingType\": 0,\n" +
                                    "          \"imageType\": false,\n" +
                                    "          \"numberType\": false,\n" +
                                    "          \"position\": 0,\n" +
                                    "          \"renderType\": \"Text\",\n" +
                                    "          \"sortOrder\": true,\n" +
                                    "          \"source\": \"sys_title\",\n" +
                                    "          \"textType\": true,\n" +
                                    "          \"width\": -1\n" +
                                    "        },\n" +
                                    "        {\n" +
                                    "          \"ascendingSort\": true,\n" +
                                    "          \"categorized\": false,\n" +
                                    "          \"dateType\": false,\n" +
                                    "          \"descendingSort\": false,\n" +
                                    "          \"description\": \"The status whether or not the content is checked out.\",\n" +
                                    "          \"displayId\": 4,\n" +
                                    "          \"displayName\": \"Checkout status\",\n" +
                                    "          \"groupingType\": 0,\n" +
                                    "          \"imageType\": true,\n" +
                                    "          \"numberType\": false,\n" +
                                    "          \"position\": 1,\n" +
                                    "          \"renderType\": \"Image\",\n" +
                                    "          \"sortOrder\": true,\n" +
                                    "          \"source\": \"sys_checkoutstatus\",\n" +
                                    "          \"textType\": false,\n" +
                                    "          \"width\": -1\n" +
                                    "        },\n" +
                                    "        {\n" +
                                    "          \"ascendingSort\": true,\n" +
                                    "          \"categorized\": true,\n" +
                                    "          \"dateType\": false,\n" +
                                    "          \"descendingSort\": false,\n" +
                                    "          \"description\": \"The current document workflow state.\",\n" +
                                    "          \"displayId\": 4,\n" +
                                    "          \"displayName\": \"State\",\n" +
                                    "          \"groupingType\": 0,\n" +
                                    "          \"imageType\": false,\n" +
                                    "          \"numberType\": false,\n" +
                                    "          \"position\": 2,\n" +
                                    "          \"renderType\": \"Text\",\n" +
                                    "          \"sortOrder\": true,\n" +
                                    "          \"source\": \"sys_statename\",\n" +
                                    "          \"textType\": true,\n" +
                                    "          \"width\": -1\n" +
                                    "        },\n" +
                                    "        {\n" +
                                    "          \"ascendingSort\": true,\n" +
                                    "          \"categorized\": false,\n" +
                                    "          \"dateType\": false,\n" +
                                    "          \"descendingSort\": false,\n" +
                                    "          \"description\": \"The document content type.\",\n" +
                                    "          \"displayId\": 4,\n" +
                                    "          \"displayName\": \"Content Type\",\n" +
                                    "          \"groupingType\": 0,\n" +
                                    "          \"imageType\": false,\n" +
                                    "          \"numberType\": false,\n" +
                                    "          \"position\": 3,\n" +
                                    "          \"renderType\": \"Text\",\n" +
                                    "          \"sortOrder\": true,\n" +
                                    "          \"source\": \"sys_contenttypename\",\n" +
                                    "          \"textType\": true,\n" +
                                    "          \"width\": -1\n" +
                                    "        },\n" +
                                    "        {\n" +
                                    "          \"ascendingSort\": true,\n" +
                                    "          \"categorized\": false,\n" +
                                    "          \"dateType\": false,\n" +
                                    "          \"descendingSort\": false,\n" +
                                    "          \"description\": \"The name of the author.\",\n" +
                                    "          \"displayId\": 4,\n" +
                                    "          \"displayName\": \"Author\",\n" +
                                    "          \"groupingType\": 0,\n" +
                                    "          \"imageType\": false,\n" +
                                    "          \"numberType\": false,\n" +
                                    "          \"position\": 4,\n" +
                                    "          \"renderType\": \"Text\",\n" +
                                    "          \"sortOrder\": true,\n" +
                                    "          \"source\": \"sys_contentcreatedby\",\n" +
                                    "          \"textType\": true,\n" +
                                    "          \"width\": -1\n" +
                                    "        },\n" +
                                    "        {\n" +
                                    "          \"ascendingSort\": true,\n" +
                                    "          \"categorized\": false,\n" +
                                    "          \"dateType\": true,\n" +
                                    "          \"descendingSort\": false,\n" +
                                    "          \"description\": \"The date when last modified.\",\n" +
                                    "          \"displayId\": 4,\n" +
                                    "          \"displayName\": \"Last Modified\",\n" +
                                    "          \"groupingType\": 0,\n" +
                                    "          \"imageType\": false,\n" +
                                    "          \"numberType\": false,\n" +
                                    "          \"position\": 5,\n" +
                                    "          \"renderType\": \"Date\",\n" +
                                    "          \"sortOrder\": true,\n" +
                                    "          \"source\": \"sys_contentlastmodifieddate\",\n" +
                                    "          \"textType\": false,\n" +
                                    "          \"width\": -1\n" +
                                    "        }\n" +
                                    "      ],\n" +
                                    "      \"descendingSort\": false,\n" +
                                    "      \"description\": \"Views & Search results by status.\",\n" +
                                    "      \"displayId\": 4,\n" +
                                    "      \"displayName\": \"By Status\",\n" +
                                    "      \"guid\": {\n" +
                                    "        \"hostId\": 0,\n" +
                                    "        \"longValue\": 4,\n" +
                                    "        \"stringValue\": \"0-31-4\",\n" +
                                    "        \"type\": 31,\n" +
                                    "        \"untypedString\": \"0-4\",\n" +
                                    "        \"uuid\": 4\n" +
                                    "      },\n" +
                                    "      \"internalName\": \"By_Status\",\n" +
                                    "      \"invalidFolderFieldNames\": \"sys_folderid, sys_siteid\",\n" +
                                    "      \"label\": \"By Status\",\n" +
                                    "      \"name\": \"By_Status\",\n" +
                                    "      \"validForFolder\": false,\n" +
                                    "      \"validForRelatedContent\": false,\n" +
                                    "      \"validForViewsAndSearches\": false\n" +
                                    "    },\n" +
                                    "    {\n" +
                                    "      \"ascendingSort\": false,\n" +
                                    "      \"columns\": [\n" +
                                    "        {\n" +
                                    "          \"ascendingSort\": true,\n" +
                                    "          \"categorized\": false,\n" +
                                    "          \"dateType\": false,\n" +
                                    "          \"descendingSort\": false,\n" +
                                    "          \"description\": \"The title of the item.\",\n" +
                                    "          \"displayId\": 3,\n" +
                                    "          \"displayName\": \"Content Title\",\n" +
                                    "          \"groupingType\": 0,\n" +
                                    "          \"imageType\": false,\n" +
                                    "          \"numberType\": false,\n" +
                                    "          \"position\": 0,\n" +
                                    "          \"renderType\": \"Text\",\n" +
                                    "          \"sortOrder\": true,\n" +
                                    "          \"source\": \"sys_title\",\n" +
                                    "          \"textType\": true,\n" +
                                    "          \"width\": -1\n" +
                                    "        },\n" +
                                    "        {\n" +
                                    "          \"ascendingSort\": true,\n" +
                                    "          \"categorized\": false,\n" +
                                    "          \"dateType\": false,\n" +
                                    "          \"descendingSort\": false,\n" +
                                    "          \"description\": \"The status whether or not the content is checked out.\",\n" +
                                    "          \"displayId\": 3,\n" +
                                    "          \"displayName\": \"Checkout status\",\n" +
                                    "          \"groupingType\": 0,\n" +
                                    "          \"imageType\": true,\n" +
                                    "          \"numberType\": false,\n" +
                                    "          \"position\": 1,\n" +
                                    "          \"renderType\": \"Image\",\n" +
                                    "          \"sortOrder\": true,\n" +
                                    "          \"source\": \"sys_checkoutstatus\",\n" +
                                    "          \"textType\": false,\n" +
                                    "          \"width\": -1\n" +
                                    "        },\n" +
                                    "        {\n" +
                                    "          \"ascendingSort\": true,\n" +
                                    "          \"categorized\": false,\n" +
                                    "          \"dateType\": false,\n" +
                                    "          \"descendingSort\": false,\n" +
                                    "          \"description\": \"The current document workflow state.\",\n" +
                                    "          \"displayId\": 3,\n" +
                                    "          \"displayName\": \"State\",\n" +
                                    "          \"groupingType\": 0,\n" +
                                    "          \"imageType\": false,\n" +
                                    "          \"numberType\": false,\n" +
                                    "          \"position\": 2,\n" +
                                    "          \"renderType\": \"Text\",\n" +
                                    "          \"sortOrder\": true,\n" +
                                    "          \"source\": \"sys_statename\",\n" +
                                    "          \"textType\": true,\n" +
                                    "          \"width\": -1\n" +
                                    "        },\n" +
                                    "        {\n" +
                                    "          \"ascendingSort\": true,\n" +
                                    "          \"categorized\": true,\n" +
                                    "          \"dateType\": false,\n" +
                                    "          \"descendingSort\": false,\n" +
                                    "          \"description\": \"The document content type.\",\n" +
                                    "          \"displayId\": 3,\n" +
                                    "          \"displayName\": \"Content Type\",\n" +
                                    "          \"groupingType\": 0,\n" +
                                    "          \"imageType\": false,\n" +
                                    "          \"numberType\": false,\n" +
                                    "          \"position\": 3,\n" +
                                    "          \"renderType\": \"Text\",\n" +
                                    "          \"sortOrder\": true,\n" +
                                    "          \"source\": \"sys_contenttypename\",\n" +
                                    "          \"textType\": true,\n" +
                                    "          \"width\": -1\n" +
                                    "        },\n" +
                                    "        {\n" +
                                    "          \"ascendingSort\": true,\n" +
                                    "          \"categorized\": false,\n" +
                                    "          \"dateType\": false,\n" +
                                    "          \"descendingSort\": false,\n" +
                                    "          \"description\": \"The name of the author.\",\n" +
                                    "          \"displayId\": 3,\n" +
                                    "          \"displayName\": \"Author\",\n" +
                                    "          \"groupingType\": 0,\n" +
                                    "          \"imageType\": false,\n" +
                                    "          \"numberType\": false,\n" +
                                    "          \"position\": 4,\n" +
                                    "          \"renderType\": \"Text\",\n" +
                                    "          \"sortOrder\": true,\n" +
                                    "          \"source\": \"sys_contentcreatedby\",\n" +
                                    "          \"textType\": true,\n" +
                                    "          \"width\": -1\n" +
                                    "        },\n" +
                                    "        {\n" +
                                    "          \"ascendingSort\": true,\n" +
                                    "          \"categorized\": false,\n" +
                                    "          \"dateType\": true,\n" +
                                    "          \"descendingSort\": false,\n" +
                                    "          \"description\": \"The date when last modified.\",\n" +
                                    "          \"displayId\": 3,\n" +
                                    "          \"displayName\": \"Last Modified\",\n" +
                                    "          \"groupingType\": 0,\n" +
                                    "          \"imageType\": false,\n" +
                                    "          \"numberType\": false,\n" +
                                    "          \"position\": 5,\n" +
                                    "          \"renderType\": \"Date\",\n" +
                                    "          \"sortOrder\": true,\n" +
                                    "          \"source\": \"sys_contentlastmodifieddate\",\n" +
                                    "          \"textType\": false,\n" +
                                    "          \"width\": -1\n" +
                                    "        }\n" +
                                    "      ],\n" +
                                    "      \"descendingSort\": false,\n" +
                                    "      \"description\": \"Views & Search results by type.\",\n" +
                                    "      \"displayId\": 3,\n" +
                                    "      \"displayName\": \"By Type\",\n" +
                                    "      \"guid\": {\n" +
                                    "        \"hostId\": 0,\n" +
                                    "        \"longValue\": 3,\n" +
                                    "        \"stringValue\": \"0-31-3\",\n" +
                                    "        \"type\": 31,\n" +
                                    "        \"untypedString\": \"0-3\",\n" +
                                    "        \"uuid\": 3\n" +
                                    "      },\n" +
                                    "      \"internalName\": \"By_Type\",\n" +
                                    "      \"invalidFolderFieldNames\": \"sys_folderid, sys_siteid\",\n" +
                                    "      \"label\": \"By Type\",\n" +
                                    "      \"name\": \"By_Type\",\n" +
                                    "      \"validForFolder\": false,\n" +
                                    "      \"validForRelatedContent\": false,\n" +
                                    "      \"validForViewsAndSearches\": false\n" +
                                    "    },\n" +
                                    "    {\n" +
                                    "      \"ascendingSort\": false,\n" +
                                    "      \"columns\": [\n" +
                                    "        {\n" +
                                    "          \"ascendingSort\": true,\n" +
                                    "          \"categorized\": false,\n" +
                                    "          \"dateType\": false,\n" +
                                    "          \"descendingSort\": false,\n" +
                                    "          \"description\": \"The title of the item.\",\n" +
                                    "          \"displayId\": 2,\n" +
                                    "          \"displayName\": \"Content Title\",\n" +
                                    "          \"groupingType\": 0,\n" +
                                    "          \"imageType\": false,\n" +
                                    "          \"numberType\": false,\n" +
                                    "          \"position\": 0,\n" +
                                    "          \"renderType\": \"Text\",\n" +
                                    "          \"sortOrder\": true,\n" +
                                    "          \"source\": \"sys_title\",\n" +
                                    "          \"textType\": true,\n" +
                                    "          \"width\": -1\n" +
                                    "        },\n" +
                                    "        {\n" +
                                    "          \"ascendingSort\": true,\n" +
                                    "          \"categorized\": false,\n" +
                                    "          \"dateType\": false,\n" +
                                    "          \"descendingSort\": false,\n" +
                                    "          \"description\": \"The status whether or not the content is checked out.\",\n" +
                                    "          \"displayId\": 2,\n" +
                                    "          \"displayName\": \"Checkout status\",\n" +
                                    "          \"groupingType\": 0,\n" +
                                    "          \"imageType\": true,\n" +
                                    "          \"numberType\": false,\n" +
                                    "          \"position\": 1,\n" +
                                    "          \"renderType\": \"Image\",\n" +
                                    "          \"sortOrder\": true,\n" +
                                    "          \"source\": \"sys_checkoutstatus\",\n" +
                                    "          \"textType\": false,\n" +
                                    "          \"width\": -1\n" +
                                    "        },\n" +
                                    "        {\n" +
                                    "          \"ascendingSort\": true,\n" +
                                    "          \"categorized\": false,\n" +
                                    "          \"dateType\": false,\n" +
                                    "          \"descendingSort\": false,\n" +
                                    "          \"description\": \"The current document workflow state.\",\n" +
                                    "          \"displayId\": 2,\n" +
                                    "          \"displayName\": \"State\",\n" +
                                    "          \"groupingType\": 0,\n" +
                                    "          \"imageType\": false,\n" +
                                    "          \"numberType\": false,\n" +
                                    "          \"position\": 2,\n" +
                                    "          \"renderType\": \"Text\",\n" +
                                    "          \"sortOrder\": true,\n" +
                                    "          \"source\": \"sys_statename\",\n" +
                                    "          \"textType\": true,\n" +
                                    "          \"width\": -1\n" +
                                    "        },\n" +
                                    "        {\n" +
                                    "          \"ascendingSort\": true,\n" +
                                    "          \"categorized\": false,\n" +
                                    "          \"dateType\": false,\n" +
                                    "          \"descendingSort\": false,\n" +
                                    "          \"description\": \"The document content type.\",\n" +
                                    "          \"displayId\": 2,\n" +
                                    "          \"displayName\": \"Content Type\",\n" +
                                    "          \"groupingType\": 0,\n" +
                                    "          \"imageType\": false,\n" +
                                    "          \"numberType\": false,\n" +
                                    "          \"position\": 3,\n" +
                                    "          \"renderType\": \"Text\",\n" +
                                    "          \"sortOrder\": true,\n" +
                                    "          \"source\": \"sys_contenttypename\",\n" +
                                    "          \"textType\": true,\n" +
                                    "          \"width\": -1\n" +
                                    "        },\n" +
                                    "        {\n" +
                                    "          \"ascendingSort\": true,\n" +
                                    "          \"categorized\": false,\n" +
                                    "          \"dateType\": false,\n" +
                                    "          \"descendingSort\": false,\n" +
                                    "          \"description\": \"The name of the author.\",\n" +
                                    "          \"displayId\": 2,\n" +
                                    "          \"displayName\": \"Author\",\n" +
                                    "          \"groupingType\": 0,\n" +
                                    "          \"imageType\": false,\n" +
                                    "          \"numberType\": false,\n" +
                                    "          \"position\": 4,\n" +
                                    "          \"renderType\": \"Text\",\n" +
                                    "          \"sortOrder\": true,\n" +
                                    "          \"source\": \"sys_contentcreatedby\",\n" +
                                    "          \"textType\": true,\n" +
                                    "          \"width\": -1\n" +
                                    "        },\n" +
                                    "        {\n" +
                                    "          \"ascendingSort\": true,\n" +
                                    "          \"categorized\": false,\n" +
                                    "          \"dateType\": true,\n" +
                                    "          \"descendingSort\": false,\n" +
                                    "          \"description\": \"The date when last modified.\",\n" +
                                    "          \"displayId\": 2,\n" +
                                    "          \"displayName\": \"Last Modified\",\n" +
                                    "          \"groupingType\": 0,\n" +
                                    "          \"imageType\": false,\n" +
                                    "          \"numberType\": false,\n" +
                                    "          \"position\": 5,\n" +
                                    "          \"renderType\": \"Date\",\n" +
                                    "          \"sortOrder\": true,\n" +
                                    "          \"source\": \"sys_contentlastmodifieddate\",\n" +
                                    "          \"textType\": false,\n" +
                                    "          \"width\": -1\n" +
                                    "        }\n" +
                                    "      ],\n" +
                                    "      \"descendingSort\": false,\n" +
                                    "      \"description\": \"Extended folder display format.\",\n" +
                                    "      \"displayId\": 2,\n" +
                                    "      \"displayName\": \"Extended\",\n" +
                                    "      \"guid\": {\n" +
                                    "        \"hostId\": 0,\n" +
                                    "        \"longValue\": 2,\n" +
                                    "        \"stringValue\": \"0-31-2\",\n" +
                                    "        \"type\": 31,\n" +
                                    "        \"untypedString\": \"0-2\",\n" +
                                    "        \"uuid\": 2\n" +
                                    "      },\n" +
                                    "      \"internalName\": \"Extended\",\n" +
                                    "      \"invalidFolderFieldNames\": \"sys_folderid, sys_siteid\",\n" +
                                    "      \"label\": \"Extended\",\n" +
                                    "      \"name\": \"Extended\",\n" +
                                    "      \"validForFolder\": false,\n" +
                                    "      \"validForRelatedContent\": false,\n" +
                                    "      \"validForViewsAndSearches\": false\n" +
                                    "    },\n" +
                                    "    {\n" +
                                    "      \"ascendingSort\": false,\n" +
                                    "      \"columns\": [\n" +
                                    "        {\n" +
                                    "          \"ascendingSort\": true,\n" +
                                    "          \"categorized\": false,\n" +
                                    "          \"dateType\": false,\n" +
                                    "          \"descendingSort\": false,\n" +
                                    "          \"description\": \"The title of the item.\",\n" +
                                    "          \"displayId\": 1,\n" +
                                    "          \"displayName\": \"Content Title\",\n" +
                                    "          \"groupingType\": 0,\n" +
                                    "          \"imageType\": false,\n" +
                                    "          \"numberType\": false,\n" +
                                    "          \"position\": 0,\n" +
                                    "          \"renderType\": \"Text\",\n" +
                                    "          \"sortOrder\": true,\n" +
                                    "          \"source\": \"sys_title\",\n" +
                                    "          \"textType\": true,\n" +
                                    "          \"width\": -1\n" +
                                    "        },\n" +
                                    "        {\n" +
                                    "          \"ascendingSort\": true,\n" +
                                    "          \"categorized\": false,\n" +
                                    "          \"dateType\": false,\n" +
                                    "          \"descendingSort\": false,\n" +
                                    "          \"description\": \"The status whether or not the content is checked out.\",\n" +
                                    "          \"displayId\": 1,\n" +
                                    "          \"displayName\": \"Checkout status\",\n" +
                                    "          \"groupingType\": 0,\n" +
                                    "          \"imageType\": true,\n" +
                                    "          \"numberType\": false,\n" +
                                    "          \"position\": 1,\n" +
                                    "          \"renderType\": \"Image\",\n" +
                                    "          \"sortOrder\": true,\n" +
                                    "          \"source\": \"sys_checkoutstatus\",\n" +
                                    "          \"textType\": false,\n" +
                                    "          \"width\": -1\n" +
                                    "        },\n" +
                                    "        {\n" +
                                    "          \"ascendingSort\": true,\n" +
                                    "          \"categorized\": false,\n" +
                                    "          \"dateType\": false,\n" +
                                    "          \"descendingSort\": false,\n" +
                                    "          \"description\": \"The current document workflow state.\",\n" +
                                    "          \"displayId\": 1,\n" +
                                    "          \"displayName\": \"State\",\n" +
                                    "          \"groupingType\": 0,\n" +
                                    "          \"imageType\": false,\n" +
                                    "          \"numberType\": false,\n" +
                                    "          \"position\": 2,\n" +
                                    "          \"renderType\": \"Text\",\n" +
                                    "          \"sortOrder\": true,\n" +
                                    "          \"source\": \"sys_statename\",\n" +
                                    "          \"textType\": true,\n" +
                                    "          \"width\": -1\n" +
                                    "        },\n" +
                                    "        {\n" +
                                    "          \"ascendingSort\": true,\n" +
                                    "          \"categorized\": false,\n" +
                                    "          \"dateType\": false,\n" +
                                    "          \"descendingSort\": false,\n" +
                                    "          \"description\": \"The document content type.\",\n" +
                                    "          \"displayId\": 1,\n" +
                                    "          \"displayName\": \"Content Type\",\n" +
                                    "          \"groupingType\": 0,\n" +
                                    "          \"imageType\": false,\n" +
                                    "          \"numberType\": false,\n" +
                                    "          \"position\": 3,\n" +
                                    "          \"renderType\": \"Text\",\n" +
                                    "          \"sortOrder\": true,\n" +
                                    "          \"source\": \"sys_contenttypename\",\n" +
                                    "          \"textType\": true,\n" +
                                    "          \"width\": -1\n" +
                                    "        }\n" +
                                    "      ],\n" +
                                    "      \"descendingSort\": false,\n" +
                                    "      \"description\": \"Simple folder display format.\",\n" +
                                    "      \"displayId\": 1,\n" +
                                    "      \"displayName\": \"Simple\",\n" +
                                    "      \"guid\": {\n" +
                                    "        \"hostId\": 0,\n" +
                                    "        \"longValue\": 1,\n" +
                                    "        \"stringValue\": \"0-31-1\",\n" +
                                    "        \"type\": 31,\n" +
                                    "        \"untypedString\": \"0-1\",\n" +
                                    "        \"uuid\": 1\n" +
                                    "      },\n" +
                                    "      \"internalName\": \"Simple\",\n" +
                                    "      \"invalidFolderFieldNames\": \"sys_folderid, sys_siteid\",\n" +
                                    "      \"label\": \"Simple\",\n" +
                                    "      \"name\": \"Simple\",\n" +
                                    "      \"validForFolder\": false,\n" +
                                    "      \"validForRelatedContent\": false,\n" +
                                    "      \"validForViewsAndSearches\": false\n" +
                                    "    },\n" +
                                    "    {\n" +
                                    "      \"ascendingSort\": false,\n" +
                                    "      \"columns\": [\n" +
                                    "        {\n" +
                                    "          \"ascendingSort\": true,\n" +
                                    "          \"categorized\": false,\n" +
                                    "          \"dateType\": false,\n" +
                                    "          \"descendingSort\": false,\n" +
                                    "          \"description\": \"The title of the item.\",\n" +
                                    "          \"displayId\": 0,\n" +
                                    "          \"displayName\": \"Content Title\",\n" +
                                    "          \"groupingType\": 0,\n" +
                                    "          \"imageType\": false,\n" +
                                    "          \"numberType\": false,\n" +
                                    "          \"position\": 0,\n" +
                                    "          \"renderType\": \"Text\",\n" +
                                    "          \"sortOrder\": true,\n" +
                                    "          \"source\": \"sys_title\",\n" +
                                    "          \"textType\": true,\n" +
                                    "          \"width\": -1\n" +
                                    "        },\n" +
                                    "        {\n" +
                                    "          \"ascendingSort\": true,\n" +
                                    "          \"categorized\": false,\n" +
                                    "          \"dateType\": false,\n" +
                                    "          \"descendingSort\": false,\n" +
                                    "          \"description\": \"The status whether or not the content is checked out.\",\n" +
                                    "          \"displayId\": 0,\n" +
                                    "          \"displayName\": \"Checkout status\",\n" +
                                    "          \"groupingType\": 0,\n" +
                                    "          \"imageType\": true,\n" +
                                    "          \"numberType\": false,\n" +
                                    "          \"position\": 1,\n" +
                                    "          \"renderType\": \"Image\",\n" +
                                    "          \"sortOrder\": true,\n" +
                                    "          \"source\": \"sys_checkoutstatus\",\n" +
                                    "          \"textType\": false,\n" +
                                    "          \"width\": -1\n" +
                                    "        },\n" +
                                    "        {\n" +
                                    "          \"ascendingSort\": true,\n" +
                                    "          \"categorized\": false,\n" +
                                    "          \"dateType\": false,\n" +
                                    "          \"descendingSort\": false,\n" +
                                    "          \"description\": \"The current document workflow state.\",\n" +
                                    "          \"displayId\": 0,\n" +
                                    "          \"displayName\": \"State\",\n" +
                                    "          \"groupingType\": 0,\n" +
                                    "          \"imageType\": false,\n" +
                                    "          \"numberType\": false,\n" +
                                    "          \"position\": 2,\n" +
                                    "          \"renderType\": \"Text\",\n" +
                                    "          \"sortOrder\": true,\n" +
                                    "          \"source\": \"sys_statename\",\n" +
                                    "          \"textType\": true,\n" +
                                    "          \"width\": -1\n" +
                                    "        },\n" +
                                    "        {\n" +
                                    "          \"ascendingSort\": true,\n" +
                                    "          \"categorized\": false,\n" +
                                    "          \"dateType\": false,\n" +
                                    "          \"descendingSort\": false,\n" +
                                    "          \"description\": \"The document content type.\",\n" +
                                    "          \"displayId\": 0,\n" +
                                    "          \"displayName\": \"Content Type\",\n" +
                                    "          \"groupingType\": 0,\n" +
                                    "          \"imageType\": false,\n" +
                                    "          \"numberType\": false,\n" +
                                    "          \"position\": 3,\n" +
                                    "          \"renderType\": \"Text\",\n" +
                                    "          \"sortOrder\": true,\n" +
                                    "          \"source\": \"sys_contenttypename\",\n" +
                                    "          \"textType\": true,\n" +
                                    "          \"width\": -1\n" +
                                    "        }\n" +
                                    "      ],\n" +
                                    "      \"descendingSort\": false,\n" +
                                    "      \"description\": \"Default display format.\",\n" +
                                    "      \"displayId\": 0,\n" +
                                    "      \"displayName\": \"Default\",\n" +
                                    "      \"guid\": {\n" +
                                    "        \"hostId\": 0,\n" +
                                    "        \"longValue\": 0,\n" +
                                    "        \"stringValue\": \"0-31-0\",\n" +
                                    "        \"type\": 31,\n" +
                                    "        \"untypedString\": \"0-0\",\n" +
                                    "        \"uuid\": 0\n" +
                                    "      },\n" +
                                    "      \"internalName\": \"Default\",\n" +
                                    "      \"invalidFolderFieldNames\": \"sys_folderid, sys_siteid\",\n" +
                                    "      \"label\": \"Default\",\n" +
                                    "      \"name\": \"Default\",\n" +
                                    "      \"validForFolder\": false,\n" +
                                    "      \"validForRelatedContent\": false,\n" +
                                    "      \"validForViewsAndSearches\": false\n" +
                                    "    },\n" +
                                    "    {\n" +
                                    "      \"ascendingSort\": false,\n" +
                                    "      \"columns\": [\n" +
                                    "        {\n" +
                                    "          \"ascendingSort\": true,\n" +
                                    "          \"categorized\": false,\n" +
                                    "          \"dateType\": false,\n" +
                                    "          \"descendingSort\": false,\n" +
                                    "          \"description\": \"The name of the item.\",\n" +
                                    "          \"displayId\": 10,\n" +
                                    "          \"displayName\": \"Name\",\n" +
                                    "          \"groupingType\": 0,\n" +
                                    "          \"imageType\": false,\n" +
                                    "          \"numberType\": false,\n" +
                                    "          \"position\": 0,\n" +
                                    "          \"renderType\": \"Text\",\n" +
                                    "          \"sortOrder\": true,\n" +
                                    "          \"source\": \"sys_title\",\n" +
                                    "          \"textType\": true,\n" +
                                    "          \"width\": -1\n" +
                                    "        },\n" +
                                    "        {\n" +
                                    "          \"ascendingSort\": true,\n" +
                                    "          \"categorized\": false,\n" +
                                    "          \"dateType\": false,\n" +
                                    "          \"descendingSort\": false,\n" +
                                    "          \"description\": \"The item size.\",\n" +
                                    "          \"displayId\": 10,\n" +
                                    "          \"displayName\": \"Size\",\n" +
                                    "          \"groupingType\": 0,\n" +
                                    "          \"imageType\": false,\n" +
                                    "          \"numberType\": true,\n" +
                                    "          \"position\": 1,\n" +
                                    "          \"renderType\": \"Number\",\n" +
                                    "          \"sortOrder\": true,\n" +
                                    "          \"source\": \"sys_size\",\n" +
                                    "          \"textType\": false,\n" +
                                    "          \"width\": 80\n" +
                                    "        },\n" +
                                    "        {\n" +
                                    "          \"ascendingSort\": true,\n" +
                                    "          \"categorized\": false,\n" +
                                    "          \"dateType\": true,\n" +
                                    "          \"descendingSort\": false,\n" +
                                    "          \"description\": \"The last modified date of the item.\",\n" +
                                    "          \"displayId\": 10,\n" +
                                    "          \"displayName\": \"Modified\",\n" +
                                    "          \"groupingType\": 0,\n" +
                                    "          \"imageType\": false,\n" +
                                    "          \"numberType\": false,\n" +
                                    "          \"position\": 2,\n" +
                                    "          \"renderType\": \"Date\",\n" +
                                    "          \"sortOrder\": true,\n" +
                                    "          \"source\": \"sys_contentlastmodifieddate\",\n" +
                                    "          \"textType\": false,\n" +
                                    "          \"width\": 80\n" +
                                    "        }\n" +
                                    "      ],\n" +
                                    "      \"descendingSort\": false,\n" +
                                    "      \"description\": \"Design List View display format.\",\n" +
                                    "      \"displayId\": 10,\n" +
                                    "      \"displayName\": \"Design\",\n" +
                                    "      \"guid\": {\n" +
                                    "        \"hostId\": 0,\n" +
                                    "        \"longValue\": 10,\n" +
                                    "        \"stringValue\": \"0-31-10\",\n" +
                                    "        \"type\": 31,\n" +
                                    "        \"untypedString\": \"0-10\",\n" +
                                    "        \"uuid\": 10\n" +
                                    "      },\n" +
                                    "      \"internalName\": \"CM1_Design\",\n" +
                                    "      \"invalidFolderFieldNames\": \"sys_folderid, sys_siteid\",\n" +
                                    "      \"label\": \"Design\",\n" +
                                    "      \"name\": \"CM1_Design\",\n" +
                                    "      \"validForFolder\": false,\n" +
                                    "      \"validForRelatedContent\": false,\n" +
                                    "      \"validForViewsAndSearches\": false\n" +
                                    "    },\n" +
                                    "    {\n" +
                                    "      \"ascendingSort\": false,\n" +
                                    "      \"columns\": [\n" +
                                    "        {\n" +
                                    "          \"ascendingSort\": true,\n" +
                                    "          \"categorized\": false,\n" +
                                    "          \"dateType\": false,\n" +
                                    "          \"descendingSort\": false,\n" +
                                    "          \"description\": \"The name of the item.\",\n" +
                                    "          \"displayId\": 9,\n" +
                                    "          \"displayName\": \"Name\",\n" +
                                    "          \"groupingType\": 0,\n" +
                                    "          \"imageType\": false,\n" +
                                    "          \"numberType\": false,\n" +
                                    "          \"position\": 0,\n" +
                                    "          \"renderType\": \"Text\",\n" +
                                    "          \"sortOrder\": true,\n" +
                                    "          \"source\": \"sys_title\",\n" +
                                    "          \"textType\": true,\n" +
                                    "          \"width\": -1\n" +
                                    "        },\n" +
                                    "        {\n" +
                                    "          \"ascendingSort\": true,\n" +
                                    "          \"categorized\": false,\n" +
                                    "          \"dateType\": false,\n" +
                                    "          \"descendingSort\": false,\n" +
                                    "          \"description\": \"The item type.\",\n" +
                                    "          \"displayId\": 9,\n" +
                                    "          \"displayName\": \"Type\",\n" +
                                    "          \"groupingType\": 0,\n" +
                                    "          \"imageType\": false,\n" +
                                    "          \"numberType\": false,\n" +
                                    "          \"position\": 1,\n" +
                                    "          \"renderType\": \"Text\",\n" +
                                    "          \"sortOrder\": true,\n" +
                                    "          \"source\": \"sys_contenttypename\",\n" +
                                    "          \"textType\": true,\n" +
                                    "          \"width\": 80\n" +
                                    "        },\n" +
                                    "        {\n" +
                                    "          \"ascendingSort\": true,\n" +
                                    "          \"categorized\": false,\n" +
                                    "          \"dateType\": false,\n" +
                                    "          \"descendingSort\": false,\n" +
                                    "          \"description\": \"The current item workflow status.\",\n" +
                                    "          \"displayId\": 9,\n" +
                                    "          \"displayName\": \"Status\",\n" +
                                    "          \"groupingType\": 0,\n" +
                                    "          \"imageType\": false,\n" +
                                    "          \"numberType\": false,\n" +
                                    "          \"position\": 2,\n" +
                                    "          \"renderType\": \"Text\",\n" +
                                    "          \"sortOrder\": true,\n" +
                                    "          \"source\": \"sys_statename\",\n" +
                                    "          \"textType\": true,\n" +
                                    "          \"width\": 80\n" +
                                    "        },\n" +
                                    "        {\n" +
                                    "          \"ascendingSort\": true,\n" +
                                    "          \"categorized\": false,\n" +
                                    "          \"dateType\": false,\n" +
                                    "          \"descendingSort\": false,\n" +
                                    "          \"description\": \"The current item workflow name.\",\n" +
                                    "          \"displayId\": 9,\n" +
                                    "          \"displayName\": \"Workflow\",\n" +
                                    "          \"groupingType\": 0,\n" +
                                    "          \"imageType\": false,\n" +
                                    "          \"numberType\": false,\n" +
                                    "          \"position\": 3,\n" +
                                    "          \"renderType\": \"Text\",\n" +
                                    "          \"sortOrder\": true,\n" +
                                    "          \"source\": \"sys_workflow\",\n" +
                                    "          \"textType\": true,\n" +
                                    "          \"width\": 80\n" +
                                    "        },\n" +
                                    "        {\n" +
                                    "          \"ascendingSort\": true,\n" +
                                    "          \"categorized\": false,\n" +
                                    "          \"dateType\": true,\n" +
                                    "          \"descendingSort\": false,\n" +
                                    "          \"description\": \"The item's last modified date.\",\n" +
                                    "          \"displayId\": 9,\n" +
                                    "          \"displayName\": \"Modified\",\n" +
                                    "          \"groupingType\": 0,\n" +
                                    "          \"imageType\": false,\n" +
                                    "          \"numberType\": false,\n" +
                                    "          \"position\": 4,\n" +
                                    "          \"renderType\": \"Date\",\n" +
                                    "          \"sortOrder\": true,\n" +
                                    "          \"source\": \"sys_contentlastmodifieddate\",\n" +
                                    "          \"textType\": false,\n" +
                                    "          \"width\": 80\n" +
                                    "        },\n" +
                                    "        {\n" +
                                    "          \"ascendingSort\": true,\n" +
                                    "          \"categorized\": false,\n" +
                                    "          \"dateType\": true,\n" +
                                    "          \"descendingSort\": false,\n" +
                                    "          \"description\": \"The item's publish date.\",\n" +
                                    "          \"displayId\": 9,\n" +
                                    "          \"displayName\": \"Published\",\n" +
                                    "          \"groupingType\": 0,\n" +
                                    "          \"imageType\": false,\n" +
                                    "          \"numberType\": false,\n" +
                                    "          \"position\": 5,\n" +
                                    "          \"renderType\": \"Date\",\n" +
                                    "          \"sortOrder\": true,\n" +
                                    "          \"source\": \"sys_postdate\",\n" +
                                    "          \"textType\": false,\n" +
                                    "          \"width\": 80\n" +
                                    "        },\n" +
                                    "        {\n" +
                                    "          \"ascendingSort\": true,\n" +
                                    "          \"categorized\": false,\n" +
                                    "          \"dateType\": true,\n" +
                                    "          \"descendingSort\": false,\n" +
                                    "          \"description\": \"The item's creation date.\",\n" +
                                    "          \"displayId\": 9,\n" +
                                    "          \"displayName\": \"Created\",\n" +
                                    "          \"groupingType\": 0,\n" +
                                    "          \"imageType\": false,\n" +
                                    "          \"numberType\": false,\n" +
                                    "          \"position\": 6,\n" +
                                    "          \"renderType\": \"Date\",\n" +
                                    "          \"sortOrder\": true,\n" +
                                    "          \"source\": \"sys_contentcreateddate\",\n" +
                                    "          \"textType\": false,\n" +
                                    "          \"width\": 80\n" +
                                    "        },\n" +
                                    "        {\n" +
                                    "          \"ascendingSort\": true,\n" +
                                    "          \"categorized\": false,\n" +
                                    "          \"dateType\": false,\n" +
                                    "          \"descendingSort\": false,\n" +
                                    "          \"description\": \"The item's creator.\",\n" +
                                    "          \"displayId\": 9,\n" +
                                    "          \"displayName\": \"Author\",\n" +
                                    "          \"groupingType\": 0,\n" +
                                    "          \"imageType\": false,\n" +
                                    "          \"numberType\": false,\n" +
                                    "          \"position\": 7,\n" +
                                    "          \"renderType\": \"Text\",\n" +
                                    "          \"sortOrder\": true,\n" +
                                    "          \"source\": \"sys_contentcreatedby\",\n" +
                                    "          \"textType\": true,\n" +
                                    "          \"width\": 80\n" +
                                    "        }\n" +
                                    "      ],\n" +
                                    "      \"descendingSort\": false,\n" +
                                    "      \"description\": \"Default display format.\",\n" +
                                    "      \"displayId\": 9,\n" +
                                    "      \"displayName\": \"Default\",\n" +
                                    "      \"guid\": {\n" +
                                    "        \"hostId\": 0,\n" +
                                    "        \"longValue\": 9,\n" +
                                    "        \"stringValue\": \"0-31-9\",\n" +
                                    "        \"type\": 31,\n" +
                                    "        \"untypedString\": \"0-9\",\n" +
                                    "        \"uuid\": 9\n" +
                                    "      },\n" +
                                    "      \"internalName\": \"CM1_Default\",\n" +
                                    "      \"invalidFolderFieldNames\": \"sys_folderid, sys_siteid\",\n" +
                                    "      \"label\": \"Default\",\n" +
                                    "      \"name\": \"CM1_Default\",\n" +
                                    "      \"validForFolder\": false,\n" +
                                    "      \"validForRelatedContent\": false,\n" +
                                    "      \"validForViewsAndSearches\": false\n" +
                                    "    },\n" +
                                    "    {\n" +
                                    "      \"ascendingSort\": false,\n" +
                                    "      \"columns\": [\n" +
                                    "        {\n" +
                                    "          \"ascendingSort\": true,\n" +
                                    "          \"categorized\": false,\n" +
                                    "          \"dateType\": false,\n" +
                                    "          \"descendingSort\": false,\n" +
                                    "          \"description\": \"The system title of the content item.\",\n" +
                                    "          \"displayId\": 8,\n" +
                                    "          \"displayName\": \"Content Title\",\n" +
                                    "          \"groupingType\": 0,\n" +
                                    "          \"imageType\": false,\n" +
                                    "          \"numberType\": false,\n" +
                                    "          \"position\": 0,\n" +
                                    "          \"renderType\": \"Text\",\n" +
                                    "          \"sortOrder\": true,\n" +
                                    "          \"source\": \"sys_title\",\n" +
                                    "          \"textType\": true,\n" +
                                    "          \"width\": -1\n" +
                                    "        },\n" +
                                    "        {\n" +
                                    "          \"ascendingSort\": true,\n" +
                                    "          \"categorized\": false,\n" +
                                    "          \"dateType\": false,\n" +
                                    "          \"descendingSort\": false,\n" +
                                    "          \"description\": \"The relevancy ranking of the item within the results.\",\n" +
                                    "          \"displayId\": 8,\n" +
                                    "          \"displayName\": \"Rank\",\n" +
                                    "          \"groupingType\": 0,\n" +
                                    "          \"imageType\": false,\n" +
                                    "          \"numberType\": true,\n" +
                                    "          \"position\": 1,\n" +
                                    "          \"renderType\": \"Number\",\n" +
                                    "          \"sortOrder\": true,\n" +
                                    "          \"source\": \"sys_relevancy\",\n" +
                                    "          \"textType\": false,\n" +
                                    "          \"width\": -1\n" +
                                    "        },\n" +
                                    "        {\n" +
                                    "          \"ascendingSort\": true,\n" +
                                    "          \"categorized\": false,\n" +
                                    "          \"dateType\": false,\n" +
                                    "          \"descendingSort\": false,\n" +
                                    "          \"description\": \"The status whether or not the content is checked out.\",\n" +
                                    "          \"displayId\": 8,\n" +
                                    "          \"displayName\": \"Checkout status\",\n" +
                                    "          \"groupingType\": 0,\n" +
                                    "          \"imageType\": true,\n" +
                                    "          \"numberType\": false,\n" +
                                    "          \"position\": 2,\n" +
                                    "          \"renderType\": \"Image\",\n" +
                                    "          \"sortOrder\": true,\n" +
                                    "          \"source\": \"sys_checkoutstatus\",\n" +
                                    "          \"textType\": false,\n" +
                                    "          \"width\": -1\n" +
                                    "        },\n" +
                                    "        {\n" +
                                    "          \"ascendingSort\": true,\n" +
                                    "          \"categorized\": false,\n" +
                                    "          \"dateType\": false,\n" +
                                    "          \"descendingSort\": false,\n" +
                                    "          \"description\": \"The current document workflow state.\",\n" +
                                    "          \"displayId\": 8,\n" +
                                    "          \"displayName\": \"State\",\n" +
                                    "          \"groupingType\": 0,\n" +
                                    "          \"imageType\": false,\n" +
                                    "          \"numberType\": false,\n" +
                                    "          \"position\": 3,\n" +
                                    "          \"renderType\": \"Text\",\n" +
                                    "          \"sortOrder\": true,\n" +
                                    "          \"source\": \"sys_statename\",\n" +
                                    "          \"textType\": true,\n" +
                                    "          \"width\": -1\n" +
                                    "        },\n" +
                                    "        {\n" +
                                    "          \"ascendingSort\": true,\n" +
                                    "          \"categorized\": false,\n" +
                                    "          \"dateType\": false,\n" +
                                    "          \"descendingSort\": false,\n" +
                                    "          \"description\": \"The document content type.\",\n" +
                                    "          \"displayId\": 8,\n" +
                                    "          \"displayName\": \"Content Type\",\n" +
                                    "          \"groupingType\": 0,\n" +
                                    "          \"imageType\": false,\n" +
                                    "          \"numberType\": false,\n" +
                                    "          \"position\": 4,\n" +
                                    "          \"renderType\": \"Text\",\n" +
                                    "          \"sortOrder\": true,\n" +
                                    "          \"source\": \"sys_contenttypename\",\n" +
                                    "          \"textType\": true,\n" +
                                    "          \"width\": -1\n" +
                                    "        }\n" +
                                    "      ],\n" +
                                    "      \"descendingSort\": true,\n" +
                                    "      \"description\": \"Search results sorted descending by rank.\",\n" +
                                    "      \"displayId\": 8,\n" +
                                    "      \"displayName\": \"Ranked Search Results\",\n" +
                                    "      \"guid\": {\n" +
                                    "        \"hostId\": 0,\n" +
                                    "        \"longValue\": 8,\n" +
                                    "        \"stringValue\": \"0-31-8\",\n" +
                                    "        \"type\": 31,\n" +
                                    "        \"untypedString\": \"0-8\",\n" +
                                    "        \"uuid\": 8\n" +
                                    "      },\n" +
                                    "      \"internalName\": \"RankedSearchResults\",\n" +
                                    "      \"invalidFolderFieldNames\": \"sys_folderid, sys_siteid\",\n" +
                                    "      \"label\": \"Ranked Search Results\",\n" +
                                    "      \"name\": \"RankedSearchResults\",\n" +
                                    "      \"validForFolder\": false,\n" +
                                    "      \"validForRelatedContent\": false,\n" +
                                    "      \"validForViewsAndSearches\": false\n" +
                                    "    },\n" +
                                    "    {\n" +
                                    "      \"ascendingSort\": false,\n" +
                                    "      \"columns\": [\n" +
                                    "        {\n" +
                                    "          \"ascendingSort\": true,\n" +
                                    "          \"categorized\": false,\n" +
                                    "          \"dateType\": false,\n" +
                                    "          \"descendingSort\": false,\n" +
                                    "          \"description\": \"The title of the item.\",\n" +
                                    "          \"displayId\": 7,\n" +
                                    "          \"displayName\": \"Content Title\",\n" +
                                    "          \"groupingType\": 0,\n" +
                                    "          \"imageType\": false,\n" +
                                    "          \"numberType\": false,\n" +
                                    "          \"position\": 0,\n" +
                                    "          \"renderType\": \"Text\",\n" +
                                    "          \"sortOrder\": true,\n" +
                                    "          \"source\": \"sys_title\",\n" +
                                    "          \"textType\": true,\n" +
                                    "          \"width\": -1\n" +
                                    "        },\n" +
                                    "        {\n" +
                                    "          \"ascendingSort\": true,\n" +
                                    "          \"categorized\": false,\n" +
                                    "          \"dateType\": false,\n" +
                                    "          \"descendingSort\": false,\n" +
                                    "          \"description\": \"The status whether or not the content is checked out.\",\n" +
                                    "          \"displayId\": 7,\n" +
                                    "          \"displayName\": \"Checkout status\",\n" +
                                    "          \"groupingType\": 0,\n" +
                                    "          \"imageType\": true,\n" +
                                    "          \"numberType\": false,\n" +
                                    "          \"position\": 1,\n" +
                                    "          \"renderType\": \"Image\",\n" +
                                    "          \"sortOrder\": true,\n" +
                                    "          \"source\": \"sys_checkoutstatus\",\n" +
                                    "          \"textType\": false,\n" +
                                    "          \"width\": -1\n" +
                                    "        },\n" +
                                    "        {\n" +
                                    "          \"ascendingSort\": true,\n" +
                                    "          \"categorized\": false,\n" +
                                    "          \"dateType\": false,\n" +
                                    "          \"descendingSort\": false,\n" +
                                    "          \"description\": \"The current document workflow state.\",\n" +
                                    "          \"displayId\": 7,\n" +
                                    "          \"displayName\": \"State\",\n" +
                                    "          \"groupingType\": 0,\n" +
                                    "          \"imageType\": false,\n" +
                                    "          \"numberType\": false,\n" +
                                    "          \"position\": 2,\n" +
                                    "          \"renderType\": \"Text\",\n" +
                                    "          \"sortOrder\": true,\n" +
                                    "          \"source\": \"sys_statename\",\n" +
                                    "          \"textType\": true,\n" +
                                    "          \"width\": -1\n" +
                                    "        },\n" +
                                    "        {\n" +
                                    "          \"ascendingSort\": true,\n" +
                                    "          \"categorized\": false,\n" +
                                    "          \"dateType\": false,\n" +
                                    "          \"descendingSort\": false,\n" +
                                    "          \"description\": \"The document content type.\",\n" +
                                    "          \"displayId\": 7,\n" +
                                    "          \"displayName\": \"Content Type\",\n" +
                                    "          \"groupingType\": 0,\n" +
                                    "          \"imageType\": false,\n" +
                                    "          \"numberType\": false,\n" +
                                    "          \"position\": 3,\n" +
                                    "          \"renderType\": \"Text\",\n" +
                                    "          \"sortOrder\": true,\n" +
                                    "          \"source\": \"sys_contenttypeid\",\n" +
                                    "          \"textType\": true,\n" +
                                    "          \"width\": -1\n" +
                                    "        },\n" +
                                    "        {\n" +
                                    "          \"ascendingSort\": true,\n" +
                                    "          \"categorized\": true,\n" +
                                    "          \"dateType\": false,\n" +
                                    "          \"descendingSort\": false,\n" +
                                    "          \"description\": \"The document variant.\",\n" +
                                    "          \"displayId\": 7,\n" +
                                    "          \"displayName\": \"Template\",\n" +
                                    "          \"groupingType\": 0,\n" +
                                    "          \"imageType\": false,\n" +
                                    "          \"numberType\": false,\n" +
                                    "          \"position\": 4,\n" +
                                    "          \"renderType\": \"Text\",\n" +
                                    "          \"sortOrder\": true,\n" +
                                    "          \"source\": \"sys_variantid\",\n" +
                                    "          \"textType\": true,\n" +
                                    "          \"width\": -1\n" +
                                    "        },\n" +
                                    "        {\n" +
                                    "          \"ascendingSort\": true,\n" +
                                    "          \"categorized\": false,\n" +
                                    "          \"dateType\": false,\n" +
                                    "          \"descendingSort\": false,\n" +
                                    "          \"description\": \"The document site.\",\n" +
                                    "          \"displayId\": 7,\n" +
                                    "          \"displayName\": \"Site\",\n" +
                                    "          \"groupingType\": 0,\n" +
                                    "          \"imageType\": false,\n" +
                                    "          \"numberType\": false,\n" +
                                    "          \"position\": 5,\n" +
                                    "          \"renderType\": \"Text\",\n" +
                                    "          \"sortOrder\": true,\n" +
                                    "          \"source\": \"sys_siteid\",\n" +
                                    "          \"textType\": true,\n" +
                                    "          \"width\": -1\n" +
                                    "        }\n" +
                                    "      ],\n" +
                                    "      \"descendingSort\": false,\n" +
                                    "      \"description\": \"Related Content search results by template name.\",\n" +
                                    "      \"displayId\": 7,\n" +
                                    "      \"displayName\": \"Related Content By Template\",\n" +
                                    "      \"guid\": {\n" +
                                    "        \"hostId\": 0,\n" +
                                    "        \"longValue\": 7,\n" +
                                    "        \"stringValue\": \"0-31-7\",\n" +
                                    "        \"type\": 31,\n" +
                                    "        \"untypedString\": \"0-7\",\n" +
                                    "        \"uuid\": 7\n" +
                                    "      },\n" +
                                    "      \"internalName\": \"Related_Content_By_Template\",\n" +
                                    "      \"invalidFolderFieldNames\": \"sys_folderid, sys_siteid\",\n" +
                                    "      \"label\": \"Related Content By Template\",\n" +
                                    "      \"name\": \"Related_Content_By_Template\",\n" +
                                    "      \"validForFolder\": false,\n" +
                                    "      \"validForRelatedContent\": false,\n" +
                                    "      \"validForViewsAndSearches\": false\n" +
                                    "    },\n" +
                                    "    {\n" +
                                    "      \"ascendingSort\": false,\n" +
                                    "      \"columns\": [\n" +
                                    "        {\n" +
                                    "          \"ascendingSort\": true,\n" +
                                    "          \"categorized\": false,\n" +
                                    "          \"dateType\": false,\n" +
                                    "          \"descendingSort\": false,\n" +
                                    "          \"description\": \"The title of the item.\",\n" +
                                    "          \"displayId\": 6,\n" +
                                    "          \"displayName\": \"Content Title\",\n" +
                                    "          \"groupingType\": 0,\n" +
                                    "          \"imageType\": false,\n" +
                                    "          \"numberType\": false,\n" +
                                    "          \"position\": 0,\n" +
                                    "          \"renderType\": \"Text\",\n" +
                                    "          \"sortOrder\": true,\n" +
                                    "          \"source\": \"sys_title\",\n" +
                                    "          \"textType\": true,\n" +
                                    "          \"width\": -1\n" +
                                    "        },\n" +
                                    "        {\n" +
                                    "          \"ascendingSort\": true,\n" +
                                    "          \"categorized\": false,\n" +
                                    "          \"dateType\": false,\n" +
                                    "          \"descendingSort\": false,\n" +
                                    "          \"description\": \"The status whether or not the content is checked out.\",\n" +
                                    "          \"displayId\": 6,\n" +
                                    "          \"displayName\": \"Checkout status\",\n" +
                                    "          \"groupingType\": 0,\n" +
                                    "          \"imageType\": true,\n" +
                                    "          \"numberType\": false,\n" +
                                    "          \"position\": 1,\n" +
                                    "          \"renderType\": \"Image\",\n" +
                                    "          \"sortOrder\": true,\n" +
                                    "          \"source\": \"sys_checkoutstatus\",\n" +
                                    "          \"textType\": false,\n" +
                                    "          \"width\": -1\n" +
                                    "        },\n" +
                                    "        {\n" +
                                    "          \"ascendingSort\": true,\n" +
                                    "          \"categorized\": false,\n" +
                                    "          \"dateType\": false,\n" +
                                    "          \"descendingSort\": false,\n" +
                                    "          \"description\": \"The current document workflow state.\",\n" +
                                    "          \"displayId\": 6,\n" +
                                    "          \"displayName\": \"State\",\n" +
                                    "          \"groupingType\": 0,\n" +
                                    "          \"imageType\": false,\n" +
                                    "          \"numberType\": false,\n" +
                                    "          \"position\": 2,\n" +
                                    "          \"renderType\": \"Text\",\n" +
                                    "          \"sortOrder\": true,\n" +
                                    "          \"source\": \"sys_statename\",\n" +
                                    "          \"textType\": true,\n" +
                                    "          \"width\": -1\n" +
                                    "        },\n" +
                                    "        {\n" +
                                    "          \"ascendingSort\": true,\n" +
                                    "          \"categorized\": true,\n" +
                                    "          \"dateType\": false,\n" +
                                    "          \"descendingSort\": false,\n" +
                                    "          \"description\": \"The document content type.\",\n" +
                                    "          \"displayId\": 6,\n" +
                                    "          \"displayName\": \"Content Type\",\n" +
                                    "          \"groupingType\": 0,\n" +
                                    "          \"imageType\": false,\n" +
                                    "          \"numberType\": false,\n" +
                                    "          \"position\": 3,\n" +
                                    "          \"renderType\": \"Text\",\n" +
                                    "          \"sortOrder\": true,\n" +
                                    "          \"source\": \"sys_contenttypeid\",\n" +
                                    "          \"textType\": true,\n" +
                                    "          \"width\": -1\n" +
                                    "        },\n" +
                                    "        {\n" +
                                    "          \"ascendingSort\": true,\n" +
                                    "          \"categorized\": false,\n" +
                                    "          \"dateType\": false,\n" +
                                    "          \"descendingSort\": false,\n" +
                                    "          \"description\": \"The document variant.\",\n" +
                                    "          \"displayId\": 6,\n" +
                                    "          \"displayName\": \"Template\",\n" +
                                    "          \"groupingType\": 0,\n" +
                                    "          \"imageType\": false,\n" +
                                    "          \"numberType\": false,\n" +
                                    "          \"position\": 4,\n" +
                                    "          \"renderType\": \"Text\",\n" +
                                    "          \"sortOrder\": true,\n" +
                                    "          \"source\": \"sys_variantid\",\n" +
                                    "          \"textType\": true,\n" +
                                    "          \"width\": -1\n" +
                                    "        },\n" +
                                    "        {\n" +
                                    "          \"ascendingSort\": true,\n" +
                                    "          \"categorized\": false,\n" +
                                    "          \"dateType\": false,\n" +
                                    "          \"descendingSort\": false,\n" +
                                    "          \"description\": \"The document site.\",\n" +
                                    "          \"displayId\": 6,\n" +
                                    "          \"displayName\": \"Site\",\n" +
                                    "          \"groupingType\": 0,\n" +
                                    "          \"imageType\": false,\n" +
                                    "          \"numberType\": false,\n" +
                                    "          \"position\": 5,\n" +
                                    "          \"renderType\": \"Text\",\n" +
                                    "          \"sortOrder\": true,\n" +
                                    "          \"source\": \"sys_siteid\",\n" +
                                    "          \"textType\": true,\n" +
                                    "          \"width\": -1\n" +
                                    "        }\n" +
                                    "      ],\n" +
                                    "      \"descendingSort\": false,\n" +
                                    "      \"description\": \"Related Content search results by type.\",\n" +
                                    "      \"displayId\": 6,\n" +
                                    "      \"displayName\": \"Related Content By Type\",\n" +
                                    "      \"guid\": {\n" +
                                    "        \"hostId\": 0,\n" +
                                    "        \"longValue\": 6,\n" +
                                    "        \"stringValue\": \"0-31-6\",\n" +
                                    "        \"type\": 31,\n" +
                                    "        \"untypedString\": \"0-6\",\n" +
                                    "        \"uuid\": 6\n" +
                                    "      },\n" +
                                    "      \"internalName\": \"Related_Content_By_Type\",\n" +
                                    "      \"invalidFolderFieldNames\": \"sys_folderid, sys_siteid\",\n" +
                                    "      \"label\": \"Related Content By Type\",\n" +
                                    "      \"name\": \"Related_Content_By_Type\",\n" +
                                    "      \"validForFolder\": false,\n" +
                                    "      \"validForRelatedContent\": false,\n" +
                                    "      \"validForViewsAndSearches\": false\n" +
                                    "    },\n" +
                                    "    {\n" +
                                    "      \"ascendingSort\": false,\n" +
                                    "      \"columns\": [\n" +
                                    "        {\n" +
                                    "          \"ascendingSort\": true,\n" +
                                    "          \"categorized\": false,\n" +
                                    "          \"dateType\": false,\n" +
                                    "          \"descendingSort\": false,\n" +
                                    "          \"description\": \"The title of the item.\",\n" +
                                    "          \"displayId\": 5,\n" +
                                    "          \"displayName\": \"Content Title\",\n" +
                                    "          \"groupingType\": 0,\n" +
                                    "          \"imageType\": false,\n" +
                                    "          \"numberType\": false,\n" +
                                    "          \"position\": 0,\n" +
                                    "          \"renderType\": \"Text\",\n" +
                                    "          \"sortOrder\": true,\n" +
                                    "          \"source\": \"sys_title\",\n" +
                                    "          \"textType\": true,\n" +
                                    "          \"width\": -1\n" +
                                    "        },\n" +
                                    "        {\n" +
                                    "          \"ascendingSort\": true,\n" +
                                    "          \"categorized\": false,\n" +
                                    "          \"dateType\": false,\n" +
                                    "          \"descendingSort\": false,\n" +
                                    "          \"description\": \"The status whether or not the content is checked out.\",\n" +
                                    "          \"displayId\": 5,\n" +
                                    "          \"displayName\": \"Checkout status\",\n" +
                                    "          \"groupingType\": 0,\n" +
                                    "          \"imageType\": true,\n" +
                                    "          \"numberType\": false,\n" +
                                    "          \"position\": 1,\n" +
                                    "          \"renderType\": \"Image\",\n" +
                                    "          \"sortOrder\": true,\n" +
                                    "          \"source\": \"sys_checkoutstatus\",\n" +
                                    "          \"textType\": false,\n" +
                                    "          \"width\": -1\n" +
                                    "        },\n" +
                                    "        {\n" +
                                    "          \"ascendingSort\": true,\n" +
                                    "          \"categorized\": false,\n" +
                                    "          \"dateType\": false,\n" +
                                    "          \"descendingSort\": false,\n" +
                                    "          \"description\": \"The current document workflow state.\",\n" +
                                    "          \"displayId\": 5,\n" +
                                    "          \"displayName\": \"State\",\n" +
                                    "          \"groupingType\": 0,\n" +
                                    "          \"imageType\": false,\n" +
                                    "          \"numberType\": false,\n" +
                                    "          \"position\": 2,\n" +
                                    "          \"renderType\": \"Text\",\n" +
                                    "          \"sortOrder\": true,\n" +
                                    "          \"source\": \"sys_statename\",\n" +
                                    "          \"textType\": true,\n" +
                                    "          \"width\": -1\n" +
                                    "        },\n" +
                                    "        {\n" +
                                    "          \"ascendingSort\": true,\n" +
                                    "          \"categorized\": false,\n" +
                                    "          \"dateType\": false,\n" +
                                    "          \"descendingSort\": false,\n" +
                                    "          \"description\": \"The document content type.\",\n" +
                                    "          \"displayId\": 5,\n" +
                                    "          \"displayName\": \"Content Type\",\n" +
                                    "          \"groupingType\": 0,\n" +
                                    "          \"imageType\": false,\n" +
                                    "          \"numberType\": false,\n" +
                                    "          \"position\": 3,\n" +
                                    "          \"renderType\": \"Text\",\n" +
                                    "          \"sortOrder\": true,\n" +
                                    "          \"source\": \"sys_contenttypename\",\n" +
                                    "          \"textType\": true,\n" +
                                    "          \"width\": -1\n" +
                                    "        },\n" +
                                    "        {\n" +
                                    "          \"ascendingSort\": true,\n" +
                                    "          \"categorized\": true,\n" +
                                    "          \"dateType\": false,\n" +
                                    "          \"descendingSort\": false,\n" +
                                    "          \"description\": \"The name of the author.\",\n" +
                                    "          \"displayId\": 5,\n" +
                                    "          \"displayName\": \"Author\",\n" +
                                    "          \"groupingType\": 0,\n" +
                                    "          \"imageType\": false,\n" +
                                    "          \"numberType\": false,\n" +
                                    "          \"position\": 4,\n" +
                                    "          \"renderType\": \"Text\",\n" +
                                    "          \"sortOrder\": true,\n" +
                                    "          \"source\": \"sys_contentcreatedby\",\n" +
                                    "          \"textType\": true,\n" +
                                    "          \"width\": -1\n" +
                                    "        },\n" +
                                    "        {\n" +
                                    "          \"ascendingSort\": true,\n" +
                                    "          \"categorized\": false,\n" +
                                    "          \"dateType\": true,\n" +
                                    "          \"descendingSort\": false,\n" +
                                    "          \"description\": \"The date when last modified.\",\n" +
                                    "          \"displayId\": 5,\n" +
                                    "          \"displayName\": \"Last Modified\",\n" +
                                    "          \"groupingType\": 0,\n" +
                                    "          \"imageType\": false,\n" +
                                    "          \"numberType\": false,\n" +
                                    "          \"position\": 5,\n" +
                                    "          \"renderType\": \"Date\",\n" +
                                    "          \"sortOrder\": true,\n" +
                                    "          \"source\": \"sys_contentlastmodifieddate\",\n" +
                                    "          \"textType\": false,\n" +
                                    "          \"width\": -1\n" +
                                    "        }\n" +
                                    "      ],\n" +
                                    "      \"descendingSort\": false,\n" +
                                    "      \"description\": \"Views & Search results by author.\",\n" +
                                    "      \"displayId\": 5,\n" +
                                    "      \"displayName\": \"By Author\",\n" +
                                    "      \"guid\": {\n" +
                                    "        \"hostId\": 0,\n" +
                                    "        \"longValue\": 5,\n" +
                                    "        \"stringValue\": \"0-31-5\",\n" +
                                    "        \"type\": 31,\n" +
                                    "        \"untypedString\": \"0-5\",\n" +
                                    "        \"uuid\": 5\n" +
                                    "      },\n" +
                                    "      \"internalName\": \"By_Author\",\n" +
                                    "      \"invalidFolderFieldNames\": \"sys_folderid, sys_siteid\",\n" +
                                    "      \"label\": \"By Author\",\n" +
                                    "      \"name\": \"By_Author\",\n" +
                                    "      \"validForFolder\": false,\n" +
                                    "      \"validForRelatedContent\": false,\n" +
                                    "      \"validForViewsAndSearches\": false\n" +
                                    "    }\n" +
                                    "  ]\n" +
                                    "}"))),
                    @ApiResponse(responseCode = "404", description = "No DisplayFormat found"),
                    @ApiResponse(responseCode = "500", description = "Error searching for DisplayFormat")
            })public DisplayFormatList findDisplayFormats(){
        List<DisplayFormat> ret = new ArrayList<>();

        try{
            ret = adaptor.findAllDisplayFormats();
        }catch(Exception e){
            log.error("Error listing DisplayFormats", e);
            throw new WebApplicationException(e);
        }
        return new DisplayFormatList(ret);
    }

}
