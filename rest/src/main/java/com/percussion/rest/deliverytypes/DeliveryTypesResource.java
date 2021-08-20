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

package com.percussion.rest.deliverytypes;

import com.percussion.rest.Status;
import com.percussion.util.PSSiteManageBean;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@PSSiteManageBean(value="restDeliveryTypesResource")
@Path("/deliverytypes")
@XmlRootElement
@Tag(name = "Delivery Types", description = "Delivery Type operations")
public class DeliveryTypesResource {

	@Autowired
	private IDeliveryTypeAdaptor deliveryTypeAdaptor;

    @Context
    private UriInfo uriInfo;
	
    private static final Logger log = LogManager.getLogger(DeliveryTypesResource.class);

    public DeliveryTypesResource(){}

	  	@GET
	    @Path("/{id}")
	    @Produces(
	    {MediaType.APPLICATION_JSON})
        @Operation(summary="Get a DeliveryType by id. ID is expected to be in Guid untyped String form"
    , responses = {
                @ApiResponse(responseCode = "404", description = "No DeliveryType found"),
                @ApiResponse(responseCode = "500", description = "Error searching for DeliveryType"),
                @ApiResponse(responseCode = "200", description = "OK", content = @Content(
                        schema=@Schema(implementation = DeliveryType.class)
                ))
    })
    public DeliveryType getDeliveryTypeById(@PathParam("id") String id) {
        DeliveryType ret;
        try {
            ret = deliveryTypeAdaptor.getDeliveryTypeById(uriInfo.getBaseUri(), id);
	    }
        catch(Exception e){
            log.error("Error retrieving DeliveryType by ID", e);
            throw new WebApplicationException(e);
        }
        return ret;
    }
	  	
	  	 @DELETE
	     @Path("/{id}")
	     @Consumes(MediaType.APPLICATION_JSON)
	     @Produces(MediaType.APPLICATION_JSON)
	     @Operation(summary = "Delete a DeliveryType. ID should be in untyped Guid String form")
	     @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "DeliveryType not found",
            content=@Content(schema=@Schema(implementation = Status.class))),
            @ApiResponse(responseCode = "500", description = "Error deleting DeliveryType")
	     })
	  	 public Status deleteDeliveryType(@Parameter(description= "The untyped Guid string of the DeliveryType to delete" ,  name="id") @PathParam("id")
	     String id){
        try {
			 deliveryTypeAdaptor.deleteDeliveryTypeById(uriInfo.getBaseUri(), id);
        }
        catch(Exception e) {
            log.error("Error deleting DeliveryType", e);
            throw new WebApplicationException(e);
        }
	         return new Status("Deleted"); 
	  	 }
	  	 
	  	 /**
	      * Update or create the DeliveryType
	      * 
	      * @param deliveryType The DeliveryType to create or update.
	      * @return The updated or created DeliveryType representation.
	      */
	     @PUT
	     @Path("/")
	     @Consumes(MediaType.APPLICATION_JSON)
	     @Produces(MediaType.APPLICATION_JSON)
	     @Operation(summary = "Create or update a delivery type"
	             , responses = {
                 @ApiResponse(responseCode = "404", description = "No DeliveryType found"),
                 @ApiResponse(responseCode = "500", description = "Error searching for DeliveryType"),
                 @ApiResponse(responseCode = "200", description = "OK", content = @Content(
                         schema=@Schema(implementation = DeliveryType.class)
                 ))
         })
	     public DeliveryType updateDeliveryType(@Parameter(description= "The body containing a JSON payload" ,
                 name="body" )DeliveryType deliveryType){
        DeliveryType ret;
        try{
            ret = deliveryTypeAdaptor.updateDeliveryType(uriInfo.getBaseUri(), deliveryType);
        }
        catch(Exception e){
            log.error("Error updating DeliveryType", e);
            throw new WebApplicationException(e);
        }
        return ret;
	     }

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "List all Delivery Types available on the system."
            ,
    responses = {
            @ApiResponse(responseCode = "404", description = "No DeliveryTypes found"),
            @ApiResponse(responseCode = "500", description = "Error searching for Delivery Types"),
            @ApiResponse(responseCode = "200", description = "OK", content=@Content(
                    array=@ArraySchema(schema=@Schema(implementation = DeliveryType.class))
            ))
    })
    public List<DeliveryType> getDeliveryTypes() {
        List<DeliveryType> ret;
        try {
            ret = deliveryTypeAdaptor.getDeliveryTypes(uriInfo.getBaseUri());
            if(ret.size()<=0){
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            }
        }catch(Exception e){
            log.error("Error listing DeliveryTypes", e);
            throw new WebApplicationException(e);
        }
        return new DeliveryTypeList(ret);
    }
}
