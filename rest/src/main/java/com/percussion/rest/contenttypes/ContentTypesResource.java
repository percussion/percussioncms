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

package com.percussion.rest.contenttypes;

import com.percussion.util.PSSiteManageBean;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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
@Api(value = "/contenttypes", description = "Content Type operations")
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
    @ApiOperation(value = "List available ContentTypes", notes = "Lists all available Content Types on the system.  Not filtered by security."
            , response = ContentType.class, responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "No Content Types found")
    })
    public List<ContentType> getContentTypes()
    {
        return new ContentTypeList(adaptor.listContentTypes(uriInfo.getBaseUri()));
    }

    @GET
    @Path("/by-site/{id}")
    @Produces(
            {MediaType.APPLICATION_JSON})
    @ApiOperation(value = "List available Content Types by Site", notes = "Lists Content Types available for a site."
         , response = ContentType.class, responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "No Content Types found")
    })
    public List<ContentType> getContentTypesBySite( @PathParam("id") int siteId)
    {
        return new ContentTypeList(adaptor.listContentTypes(uriInfo.getBaseUri(), siteId));
    }
}
