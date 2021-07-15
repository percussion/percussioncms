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

package com.percussion.rest.acls;


import com.percussion.rest.Guid;
import com.percussion.rest.GuidList;
import com.percussion.rest.Status;
import com.percussion.util.PSSiteManageBean;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@PSSiteManageBean(value="restAclResource")
@Path("/acls")
@XmlRootElement
@Api(value = "acls", description = "ACL operations")
@Lazy
public class AclResource {

    private Logger log = LogManager.getLogger(this.getClass());

    @Autowired
    private IAclAdaptor adaptor;
    public AclResource(){}


    @POST
    @Path("/object")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    @ApiOperation(value="retrieves the acl for the object with the supplied id")
    public UserAccessLevel getUserAccessLevel(@ApiParam(value="The guid of the object for which the current user's effective access level needs to be computed. Must not be null.",
            required = true) Guid objectGuid){

        try{
            return adaptor.getUserAccessLevel(objectGuid);
        }catch(Exception e){
            log.error("An exception occurred getting User Access Level for Object: " + objectGuid.getStringValue(),e);
            throw new WebApplicationException(e.getMessage(),
                    Response.serverError().build());
        }
    }

    @GET
    @Path("/user/{aclGuid}")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    @ApiOperation(value="Computes the current user's effective access level to the object protected\n" +
            "by the supplied ACL. The effective access level of a user is the highest\n" +
            "permission he or she can get on the associated object based on all entries\n" +
            "in the ACL.")
    public UserAccessLevel calculateUserAccessLevel(@PathParam("aclGuid") String aclGuid){

        try {
            return adaptor.calculateUserAccessLevel(aclGuid);
        }catch(Exception e){
            log.error("An error occurred checking the access level for Acl: " + aclGuid +"",e);
            throw new WebApplicationException(e.getMessage(),
                    Response.serverError().build());
        }
    }

    @POST
    @Path("/")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    @ApiOperation(value="Create an acl for the specified object.")
    public Acl createAcl(@ApiParam(required=true, value="A valid CreateAclRequest object") CreateAclRequest request){

        try {
            return adaptor.createAcl(request.getObjectGuid(), request.getOwner());
        }catch(Exception e){
            log.error("An error occurred creating an Acl for Owner:" + request.getOwner().getName() +
                    " and Object: " + request.getObjectGuid().getStringValue()
                    ,e);
            throw new WebApplicationException(e.getMessage(),
                    Response.serverError().build());
        }
    }

    @GET
    @Path("/bulk")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    @ApiOperation(value="Load ACLs for given list of ACL GUIDs. These objects are cached and shared\n" +
            "    * between threads and should be treated read-only. See the class description\n" +
            "    * for more details.")
    public AclList loadAcls(GuidList aclGuids){
        try {
            return adaptor.loadAcls(aclGuids);
        }catch(Exception e){
            log.error("An error occurred loading acls for guid list: " + aclGuids.toString());
            throw new WebApplicationException(e.getMessage(),
                    Response.serverError().build());
        }
    }

    @GET
    @Path("/{guid}")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Acl loadAcl(@PathParam(value="guid") String guid){

        try {
            return adaptor.loadAcl(new Guid(guid));
        }catch(Exception e){
            log.error("An error occurred loading acl for guid : " + guid);
            throw new WebApplicationException(e.getMessage(),
                    Response.serverError().build());
        }
    }


    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    @Path("/bulk")
    @POST
    public AclList loadAclsForObjects(GuidList objectGuids){
        try {
            return adaptor.loadAclsForObjects(objectGuids);
        }catch(Exception e){
            log.error("An error occurred loading acl for guids : " + objectGuids.toString());
            throw new WebApplicationException(e.getMessage(),
                    Response.serverError().build());
        }
    }

    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    @Path("/object/{objectGuid}")
    @GET
    public Acl loadAclForObject(@PathParam(value="objectGuid")String objectGuid){
        try {
            return adaptor.loadAclForObject(new Guid(objectGuid));
        }catch(NotFoundException n){
            log.debug("No ACL's found for object:" + objectGuid);
            throw new WebApplicationException("No ACL's found for Object with GUID:" + objectGuid,Response.status(404).build());
        }
        catch(Exception e){
            log.error("An error occurred loading acl for Object guid : " + objectGuid,e);
            throw new WebApplicationException(e.getMessage(),
                    Response.serverError().build());
        }
    }

    @PUT
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    @Path("/bulk")
    public Status saveAcls(AclList aclList){
        Status ret = new Status(200, "OK");
        try {
             adaptor.saveAcls(aclList);
        }catch(Exception e){
            log.error("An error occurred saving acl list", e);
            throw new WebApplicationException(e.getMessage(),
                    Response.serverError().build());
        }

        return  ret;
    }

    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Status deleteAcl(Guid aclGuid){
        Status ret = new Status(200,"OK");

        try {
            adaptor.deleteAcl(aclGuid);
        } catch(Exception e){
        log.error("An error occurred deleting acl:" +aclGuid.getStringValue(), e);
        throw new WebApplicationException(e.getMessage(),
                Response.serverError().build());
    }
        return ret;
    }

    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    @Path("/community/filter")
    public GuidList filterByCommunities(GuidList aclList, List<String> communityNames){
        return null;
    }

    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    @Path("/community")
    public GuidList findObjectsVisibleToCommunities(){
    return null;}
}
