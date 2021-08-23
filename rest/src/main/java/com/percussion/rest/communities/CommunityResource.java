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

package com.percussion.rest.communities;

import com.percussion.rest.GuidList;
import com.percussion.rest.ObjectTypeEnum;
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
import org.springframework.context.annotation.Lazy;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@PSSiteManageBean(value="restCommunityResource")
@Path("/communities")
@XmlRootElement
@Tag(name = "Communities", description = "Community operations")
@Lazy
public class CommunityResource implements  ICommunityResource{

    private static final Logger log = LogManager.getLogger(CommunityResource.class);

    @Autowired
    private ICommunityAdaptor adaptor;

    @Context
    private UriInfo uriInfo;

    public CommunityResource(){}

    @Override
    @POST
    @Path("/bulk")
    @Operation(summary="Creates a set of communities from an array list of community names.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description="OK"),
            @ApiResponse(responseCode = "500", description = "ERROR")
    })
    public CommunityList createCommunities(@Parameter(example = "{\"List\":\n" +
            "[\"Comm1\",\"Comm2\",\"Comm3\"]\n" +
            "}") List<String> names) {
        try {
            return adaptor.createCommunities(names);
        }catch(Exception e){
            log.error("An error occurred calling createCommunities", e);
            throw new WebApplicationException(e);
        }
    }

    @Override
    @GET
    @Path("/find")
    @Produces(
            {MediaType.APPLICATION_JSON})
    @Operation(summary = "Get a community by name or pattern.  Get a community by name or pattern.  * is the wildcard.", responses = { @ApiResponse(
            responseCode = "200", description = "OK",
            content = @Content(schema=@Schema(implementation = CommunityList.class)))})
    @ApiResponses(value = {
            @ApiResponse(responseCode="200", description="OK"),
            @ApiResponse(responseCode="500", description="ERROR")
    })
    public CommunityList findCommunities(@Parameter(name="name",required=true) @QueryParam("name") String name) {
        try {
            return adaptor.findCommunities(name);
        }catch(Exception e){
            log.error("An error occurred calling findCommunities.", e);
            throw new WebApplicationException(e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @POST
    @Path("/bulk/load")
    @Produces(
            {MediaType.APPLICATION_JSON})
    @Operation(summary = "Loads one or more communities by guid. Takes a GuidList of communities to retrieve.  Setting the lock header to true will lock the communities on the server.  Setting overrridelock header to true will return the list even if the communities are locked in another session.",
            responses = {
                    @ApiResponse(responseCode="200", description="OK",
                            content=@Content(array= @ArraySchema(
                                    schema=@Schema(implementation =  Community.class)))),
                    @ApiResponse(responseCode="500", description="ERROR")})
    public CommunityList loadCommunities(@Parameter(name="ids",required=true) GuidList ids, @Parameter(name="lock") @HeaderParam("lock") boolean lock, @HeaderParam("overridelock")@Parameter(name="overridelock") boolean overridelock) {
        try {
            return  adaptor.loadCommunities(ids,lock,overridelock);

        } catch (Exception e) {
            log.error("An error occurred calling loadCommunities",e);
            throw new WebApplicationException(e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @PUT
    @Path("/bulk")
    @ApiResponses(value = {
            @ApiResponse(responseCode="200", description="OK"),
            @ApiResponse(responseCode="500", description="ERROR")
    })
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Saves the communities in the submitted list.  If the release header is set, any locks will be released. To avoid extended locks, please set the release header when calling this method unless you really want to keep them locked.")
    public void saveCommunities(@Parameter(description = "communities", required = true) CommunityList communities,
                                @Parameter(name="release",
                                        allowEmptyValue = false,required = true)@HeaderParam(value="release") boolean release) {
        try {
            adaptor.saveCommunities(communities, release);
        }catch(Exception e){
            log.error("An error occurred call save communities",e);
            throw new WebApplicationException(e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @DELETE
    @Path("/bulk")
    @Consumes({MediaType.APPLICATION_JSON})
    @ApiResponses(value = {
            @ApiResponse(responseCode="200", description="OK"),
            @ApiResponse(responseCode="500", description="ERROR")
    })
    @Operation(summary="Accepts a GuidList containing the ids of the Communities to delete.  If the ignoredepencies header is set on the request, then the system won't fail the delete if dependency is relying on this community.")
    public void deleteCommunities(@Parameter(name="ids") GuidList ids, @Parameter(name="ignoredepencies",required = true) @HeaderParam("ignoredependencies") boolean ignoredependencies) {
        try {
            adaptor.deleteCommunities(ids, ignoredependencies);
        }catch(Exception e){
            log.error("An error occurred calling deleteCommunities", e);
            throw new WebApplicationException(e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Path("/visibility")
    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @ApiResponses(value = {
            @ApiResponse(responseCode="200", description="OK"),
            @ApiResponse(responseCode="500", description="ERROR")
    })
    @Operation(summary="Returns the Community visibility list for the specified communities.  If the type header is null or missing, visibility for all object types will be returned.")
    public CommunityVisibilityList getVisibilityByCommunity(@Parameter(description = "List of GUID",example = "{\"GuidList\": [\n" +
            "   {\n" +
            "        \"stringValue\": \"0-13-1003\",\n" +
            "        \"untypedString\": \"0-1003\",\n" +
            "        \"hostId\": 0,\n" +
            "        \"type\": 13,\n" +
            "        \"uuid\": 1003,\n" +
            "        \"longValue\": 1003\n" +
            "      },\n" +
            "      {\n" +
            "        \"stringValue\": \"0-13-10\",\n" +
            "        \"untypedString\": \"0-10\",\n" +
            "        \"hostId\": 0,\n" +
            "        \"type\": 13,\n" +
            "        \"uuid\": 10,\n" +
            "        \"longValue\": 10\n" +
            "      }\n" +
            "]}") GuidList ids, @Parameter(name="type") @HeaderParam("type") ObjectTypeEnum type) {
        CommunityVisibilityList list = null;

        try {
            list = adaptor.getVisibilityByCommunity(ids,type);
        } catch (Exception e) {
            log.error("An error occurred calling getVisibilityBuCommunity",e);
            throw new WebApplicationException(e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR);
        }

        return list;
    }

    @POST
    @Path("/switch/{name}")
    @Operation(summary="Switch the user's session to the specified community. May error out of user does not have access to a Role in specified community",
    responses={@ApiResponse(responseCode="200",description="OK", content=@Content(schema = @Schema(implementation = Status.class))),
    @ApiResponse(responseCode="500",description = "Error")})
    public Status switchCommunity(@Parameter(required=true,description="Name of the community to switch to")@PathParam("name") String name){
        Status ret = new Status(200,"OK");
        try{
            adaptor.switchCommunity(name);
        }catch(Exception e){
            log.error("An error occurred calling switchCommunity",e);
            throw new WebApplicationException(e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR);
        }
        return ret;
    }
}
