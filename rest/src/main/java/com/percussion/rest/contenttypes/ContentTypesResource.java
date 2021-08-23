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

package com.percussion.rest.contenttypes;

import com.percussion.util.PSSiteManageBean;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@PSSiteManageBean(value="restContentTypesResource")
@Path("/contenttypes")
@XmlRootElement
@Tag(name = "Content Types", description = "Content Type operations")
public class ContentTypesResource {

    @Autowired
    private IContentTypesAdaptor adaptor;

    @Context
    private UriInfo uriInfo;

    public ContentTypesResource(){}

    @GET
    @Path("/")
    @Produces(
            {MediaType.APPLICATION_JSON})
    @Operation(summary = "List available ContentTypes", description = "Lists all available Content Types on the system.  Not filtered by security."
            , responses = {
             @ApiResponse(responseCode = "200", description = "OK",
             content = @Content(array = @ArraySchema( schema = @Schema(implementation = ContentType.class)))),
             @ApiResponse(responseCode = "404", description = "No Content Types found"),
             @ApiResponse(responseCode = "500", description = "Error")
    })
    public List<ContentType> getContentTypes()
    {
        return new ContentTypeList(adaptor.listContentTypes(uriInfo.getBaseUri()));
    }

    @GET
    @Path("/by-site/{id}")
    @Produces(
            {MediaType.APPLICATION_JSON})
    @Operation(summary = "List available Content Types by Site", description = "Lists Content Types available for a site."
            , responses = {
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(array = @ArraySchema( schema = @Schema(implementation = ContentType.class)))),
            @ApiResponse(responseCode = "404", description = "No Content Types found"),
            @ApiResponse(responseCode = "500", description = "Error")
    })
    public List<ContentType> getContentTypesBySite( @PathParam("id") int siteId)
    {
        return new ContentTypeList(adaptor.listContentTypes(uriInfo.getBaseUri(), siteId));
    }
}
