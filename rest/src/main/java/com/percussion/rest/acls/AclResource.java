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

package com.percussion.rest.acls;


import com.percussion.error.PSExceptionUtils;
import com.percussion.rest.Guid;
import com.percussion.rest.GuidList;
import com.percussion.rest.Status;
import com.percussion.util.PSSiteManageBean;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
@Tag(name = "ACLS", description = "ACL operations")
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
    @Operation(summary="retrieves the acl for the object with the supplied id")
    public UserAccessLevel getUserAccessLevel(@Parameter(description="The guid of the object for which the current user's effective access level needs to be computed. Must not be null.",
            required = true) Guid objectGuid){

        try{
            return adaptor.getUserAccessLevel(objectGuid);
        }catch(Exception e){
            log.error("An exception occurred getting User Access Level for Object: {}, Error: {}", objectGuid.getStringValue(),e.getMessage());
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            throw new WebApplicationException(e.getMessage(),
                    Response.serverError().build());
        }
    }

    @GET
    @Path("/user/{aclGuid}")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    @Operation(summary="Computes the current user's effective access level to the object protected\n" +
            "by the supplied ACL. The effective access level of a user is the highest\n" +
            "permission he or she can get on the associated object based on all entries\n" +
            "in the ACL.")
    public UserAccessLevel calculateUserAccessLevel(@PathParam("aclGuid") String aclGuid){

        try {
            return adaptor.calculateUserAccessLevel(aclGuid);
        }catch(Exception e){
            log.error("An error occurred checking the access level for Acl: {}, Error: {}", aclGuid,PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            throw new WebApplicationException(e.getMessage(),
                    Response.serverError().build());
        }
    }

    @POST
    @Path("/")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    @Operation(summary="Create an acl for the specified object.")
    public Acl createAcl(@Parameter(required=true, description="A valid CreateAclRequest object") CreateAclRequest request){

        try {
            return adaptor.createAcl(request.getObjectGuid(), request.getOwner());
        }catch(Exception e){
            log.error("An error occurred creating an Acl for Owner: {} and object: {}, Error: {}", request.getOwner().getName(), request.getObjectGuid().getStringValue(),PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            throw new WebApplicationException(e.getMessage(),
                    Response.serverError().build());
        }
    }

    @GET
    @Path("/bulk")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    @Operation(summary="Load ACLs for given list of ACL GUIDs. These objects are cached and shared\n" +
            "    * between threads and should be treated read-only. See the class description\n" +
            "    * for more details.")
    public AclList loadAcls(GuidList aclGuids){
        try {
            return adaptor.loadAcls(aclGuids);
        }catch(Exception e){
            log.error("An error occurred loading acls for guid list: {}, Error: {}", aclGuids.toString(),PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
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
            log.error("An error occurred loading acl for guid : {} {}", guid,PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
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
            log.error("An error occurred loading acl for guids : {} {}", objectGuids.toString(),PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
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
            log.debug("No ACL's found for object:{} {}", objectGuid, n.getMessage());
            log.debug(n.getMessage(), n);
            throw new WebApplicationException("No ACL's found for Object with GUID:" + objectGuid,Response.status(404).build());
        }
        catch(Exception e){
            log.error("An error occurred loading acl for Object guid : {}, {}", objectGuid,e.getMessage());
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
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
            log.error("An error occurred saving acl list {}",PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
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
        log.error("An error occurred deleting acl:{}, Error: {}",aclGuid.getStringValue(),PSExceptionUtils.getMessageForLog(e));
        log.debug(PSExceptionUtils.getDebugMessageForLog(e));
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
