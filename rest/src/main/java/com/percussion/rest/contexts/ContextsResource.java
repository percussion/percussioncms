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

package com.percussion.rest.contexts;

import com.percussion.error.PSExceptionUtils;
import com.percussion.rest.Status;
import com.percussion.rest.errors.BackendException;
import com.percussion.rest.locationscheme.ILocationSchemeAdaptor;
import com.percussion.util.PSSiteManageBean;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@PSSiteManageBean(value="restContextResource")
@Path("/contexts")
@XmlRootElement
@Tag(name = "Delivery Contexts", description = "Publishing Context operations")
public class ContextsResource {

    private static final Logger log = LogManager.getLogger(ContextsResource.class);

    @Autowired
    private IContextsAdaptor adaptor;

    @Autowired
    private ILocationSchemeAdaptor locationSchemeAdaptor;

    @javax.ws.rs.core.Context
    private UriInfo uriInfo;

    public ContextsResource(){}

    /***
     * Delete a publishing Context by id
     * @param id guid of the Context to delete
     */
    @DELETE
    @Path("/{id}")
    @Operation(summary="Delete the specified publishing Context",
            responses = {@ApiResponse(responseCode = "200", content=@Content(schema=@Schema(implementation=Status.class))),
            @ApiResponse(responseCode="500",description = "Error")})
    public Status deleteContext(@PathParam(value="id") @Parameter(name="id",description="The id of the publishing Context to delete") String id) {
        Status response = null;
        try {
            adaptor.deleteContext(uriInfo.getBaseUri(), id);
            response = new Status(200,"OK");
        }catch(Exception e){
            response =  new Status(500,PSExceptionUtils.getMessageForLog(e));
        }
        return response;
    }

    /***
     * Get a publishing context by it's ID
     * @param id id of the context to lookup
     * @return The publishing Conext
     */
    @GET
    @Path("/{id}")
    @Operation(summary="Get a publishing Context by id"
            , responses = {
            @ApiResponse(responseCode="200", description="OK", content = @Content(
                    schema=@Schema(implementation = Context.class))
            ),
            @ApiResponse(responseCode="500", description = "Error")})
    public Context getContextById(@PathParam(value="id") @Parameter(name="id",description="The guid id for the Publishing Context to return") String id) {
        try {
            return adaptor.getContextById(uriInfo.getBaseUri(), id);
        } catch (BackendException e) {
            log.error(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            throw new WebApplicationException(e);
        }
    }

    /***
     * List all publishing contexts configured on the system
     * @return a list of publishing contexts
     */
    @GET
    @Path("/")
    @Operation(summary="Get the available Publishing Contexts",
            responses = {
                    @ApiResponse(responseCode="200", description="OK", content=@Content(
                            array = @ArraySchema(schema=@Schema(implementation = Context.class))
                    )),
                    @ApiResponse(responseCode = "500", description = "Error")})
    public List<Context> listContexts() {
        try {
            return new ContextList(adaptor.listContexts(uriInfo.getBaseUri()));
        } catch (BackendException e) {
            log.error(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            throw new WebApplicationException(e);
        }
    }

    /***
     * Create or update a publishing context
     * @param context a fully initialized Context
     * @return The updated context
     */
    @PUT
    @Path("/")
    @Operation(summary="Create or update the publishing Context.  Returns the updated Context, id should not be set for new Contexts"
            , responses = {
            @ApiResponse(responseCode="200", description="OK", content = @Content(
                    schema=@Schema(implementation = Context.class))
            ),
            @ApiResponse(responseCode="500", description = "Error")})
    public Context createOrUpdateContext(Context context){
        try {
            return adaptor.createOrUpdateContext(uriInfo.getBaseUri(), context);
        } catch (BackendException e) {
            log.error(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            throw new WebApplicationException(e);
        }
    }

}
