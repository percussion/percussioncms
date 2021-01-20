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

package com.percussion.rest.deliverytypes;

import com.percussion.rest.Status;
import com.percussion.util.PSSiteManageBean;
import io.swagger.annotations.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@PSSiteManageBean(value="restDeliveryTypesResource")
@Path("/deliverytypes")
@XmlRootElement
@Api(value = "/deliverytypes", description = "Delivery Type operations")
public class DeliveryTypesResource {

	@Autowired
	private IDeliveryTypeAdaptor deliveryTypeAdaptor;

    @Context
    private UriInfo uriInfo;
	
    private Log log = LogFactory.getLog(DeliveryTypesResource.class);

    public DeliveryTypesResource(){}

	  	@GET
	    @Path("/{id}")
	    @Produces(
	    {MediaType.APPLICATION_JSON})
        @ApiOperation(value="Get a DeliveryType by id",notes="ID is expected to be in Guid untyped String form")
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "No DeliveryType found"),
            @ApiResponse(code = 500, message = "Error searching for DeliveryType")
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
	     @ApiOperation(value = "Delete a DeliveryType", response = Status.class, notes="ID should be in untyped Guid String form")
	     @ApiResponses(value = {
            @ApiResponse(code = 404, message = "DeliveryType not found"),
            @ApiResponse(code = 500, message = "Error deleting DeliveryType")
	     })
	  	 public Status deleteDeliveryType(@ApiParam(value= "The untyped Guid string of the DeliveryType to delete" ,  name="id") @PathParam("id")
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
	     @ApiOperation(value = "Create or update a delivery type"
	             , response = DeliveryType.class)
	     @ApiResponses(value = {
	       @ApiResponse(code = 404, message = "DeliveryType not found") 
	     })
	     public DeliveryType updateDeliveryType(@ApiParam(value= "The body containing a JSON payload" ,  name="body" )DeliveryType deliveryType){
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
    @ApiOperation(value = "List all Delivery Types available on the system."
            , response = DeliveryType.class, responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "No DeliveryTypes found"),
            @ApiResponse(code = 500, message = "Error searching for Delivery Types")
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
