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

package com.percussion.rest.communities;

import com.percussion.rest.GuidList;
import com.percussion.rest.ObjectTypeEnum;
import com.percussion.rest.Status;
import com.percussion.util.PSSiteManageBean;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@PSSiteManageBean(value="restCommunityResource")
@Path("/communities")
@XmlRootElement
@Api(value = "communities", description = "Community operations")
@Lazy
public class CommunityResource implements  ICommunityResource{

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ICommunityAdaptor adaptor;

    @Context
    private UriInfo uriInfo;

    public CommunityResource(){}

    @Override
    @POST
    @Path("/bulk")
    @ApiOperation(value="Creates a set of communities from an array list of community names.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message="OK"),
            @ApiResponse(code = 500, message = "ERROR")
    })
    public CommunityList createCommunities(@ApiParam(example = "{\"List\":\n" +
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
    @ApiOperation(value = "Get a community by name or pattern", notes = "Get a community by name or pattern.  * is the wildcard.", response = CommunityList.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message="OK"),
            @ApiResponse(code = 500, message = "ERROR")
    })
    public CommunityList findCommunities(@ApiParam(name="name",required=true) @QueryParam("name") String name) {
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
    @ApiOperation(value = "Loads one or more communities by guid", notes = "Takes a GuidList of communities to retrieve.  Setting the lock header to true will lock the communities on the server.  Setting overrridelock header to true will return the list even if the communities are locked in another session.", response = CommunityList.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message="OK"),
            @ApiResponse(code = 500, message = "ERROR")
    })
    public CommunityList loadCommunities(@ApiParam(name="ids",required=true) GuidList ids, @ApiParam(name="lock") @HeaderParam("lock") boolean lock, @HeaderParam("overridelock")@ApiParam(name="overridelock") boolean overridelock) {
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
            @ApiResponse(code = 200, message="OK"),
            @ApiResponse(code = 500, message = "ERROR")
    })
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Saves the communities in the submitted list.  If the release header is set, any locks will be released.",notes="To avoid extended locks, please set the release header when calling this method unless you really want to keep them locked.")
    public void saveCommunities(@ApiParam(value = "communities", required = true) CommunityList communities, @ApiParam(name="release",defaultValue = "false")@HeaderParam(value="release") boolean release) {
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
            @ApiResponse(code = 200, message="OK"),
            @ApiResponse(code = 500, message = "ERROR")
    })
    @ApiOperation(value="Accepts a GuidList containing the ids of the Communities to delete.  If the ignoredepencies header is set on the request, then the system won't fail the delete if dependency is relying on this community.")
    public void deleteCommunities(@ApiParam("ids") GuidList ids, @ApiParam(name="ignoredepencies",defaultValue = "false") @HeaderParam("ignoredependencies") boolean ignoredependencies) {
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
            @ApiResponse(code = 200, message="OK"),
            @ApiResponse(code = 500, message = "ERROR")
    })
    @ApiOperation(value="Returns the Community visibility list for the specified communities.", notes="If the type header is null or missing, visibility for all object types will be returned.")
    public CommunityVisibilityList getVisibilityByCommunity(@ApiParam(value = "List of GUID",example = "{\"GuidList\": [\n" +
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
            "]}") GuidList ids, @ApiParam(name="type", type="string") @HeaderParam("type") ObjectTypeEnum type) {
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
    @ApiOperation(value="Switch the user's session to the specified community.", notes="May error out of user does not have access to a Role in specified community")
    @ApiResponses({@ApiResponse(code=200,message="OK", response=Status.class),
    @ApiResponse(code=500,message = "Error")})
    public Status switchCommunity(@ApiParam(required=true,value="Name of the community to switch to")@PathParam("name") String name){
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
