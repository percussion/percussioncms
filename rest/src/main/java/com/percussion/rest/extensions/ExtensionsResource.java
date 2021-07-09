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

package com.percussion.rest.extensions;

import com.percussion.util.PSSiteManageBean;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@PSSiteManageBean(value="restExtensionsResource")
@Path("/extensions")
@XmlRootElement
@Api(value = "/extensions", description = "Extension operations")
public class ExtensionsResource {

    @Autowired
    private IExtensionAdaptor adaptor;

    @Context
    private UriInfo uriInfo;

    public ExtensionsResource(){}
    
    @POST
    @Path("/list")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "List Extensions available on the system", notes = "Returns a list of Extensions that match the supplied ExtensionFilterOptions"
, response = ExtensionList.class)
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "No Extensions not found")
    })
    public List<Extension> getExtensions(@ApiParam(name="filter", value="An extension filter options object", required=true) ExtensionFilterOptions filter){
        return new ExtensionList(adaptor.getExtensions(uriInfo.getBaseUri(),filter));
    }

}
