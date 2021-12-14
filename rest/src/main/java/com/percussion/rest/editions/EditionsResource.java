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

package com.percussion.rest.editions;

import com.percussion.util.PSSiteManageBean;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlRootElement;

@PSSiteManageBean(value="restEditionsResource")
@Path("/editions")
@XmlRootElement
@Tag(name = "Publishing Editions")
public class EditionsResource {

    @Autowired
    private IEditionsAdaptor adaptor;

    public EditionsResource(){
        //Default ctor
    }


    @POST
    @Path("/{id}/publish")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Executes the publish action for the specified Edition", description = "Returns a PublishStatus record that can be used to monitor status"
            , responses = {
            @ApiResponse(responseCode = "404", description = "Edition not found"),
            @ApiResponse(responseCode =  "403", description = "Publish permission denied"),
            @ApiResponse(responseCode = "500", description = "Error executing publish")
    })
    public PublishResponse publish(@PathParam("id") String id){

        //Sanitize and validate id
        //not empty, must be numeric

        PublishResponse ret = new PublishResponse();

        ret = adaptor.publish(id);

        return ret;
    }


}
