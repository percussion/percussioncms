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
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
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

    public ContextsResource(){
        //NOOP
    }

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
    @Produces({MediaType.APPLICATION_JSON})
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
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/")
    @Operation(summary="Create or update the publishing Context.  Returns the updated Context, id should not be set for new Contexts"
            , responses = {
            @ApiResponse(responseCode="200", description="OK", content = @Content(
                    schema=@Schema(implementation = Context.class),
            examples = @ExampleObject(value = "{\n" +
                    "  \"ContextList\": [\n" +
                    "    {\n" +
                    "      \"id\": {\n" +
                    "        \"hostId\": 0,\n" +
                    "        \"longValue\": 0,\n" +
                    "        \"stringValue\": \"0-113-0\",\n" +
                    "        \"type\": 113,\n" +
                    "        \"untypedString\": \"0-0\",\n" +
                    "        \"uuid\": 0\n" +
                    "      }\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"id\": {\n" +
                    "        \"hostId\": 0,\n" +
                    "        \"longValue\": 1,\n" +
                    "        \"stringValue\": \"0-113-1\",\n" +
                    "        \"type\": 113,\n" +
                    "        \"untypedString\": \"0-1\",\n" +
                    "        \"uuid\": 1\n" +
                    "      },\n" +
                    "      \"locationSchemes\": [\n" +
                    "        {\n" +
                    "          \"contentTypeId\": 311,\n" +
                    "          \"context\": {\n" +
                    "            \"hostId\": 0,\n" +
                    "            \"longValue\": 314,\n" +
                    "            \"stringValue\": \"0-10-314\",\n" +
                    "            \"type\": 10,\n" +
                    "            \"untypedString\": \"0-314\",\n" +
                    "            \"uuid\": 314\n" +
                    "          },\n" +
                    "          \"description\": \"Generic location generation for publishing\",\n" +
                    "          \"locationSchemeGenerator\": \"Java/global/percussion/contentassembler/sys_JexlAssemblyLocation\",\n" +
                    "          \"name\": \"Generic\",\n" +
                    "          \"schemeId\": {\n" +
                    "            \"hostId\": 0,\n" +
                    "            \"longValue\": 314,\n" +
                    "            \"stringValue\": \"0-10-314\",\n" +
                    "            \"type\": 10,\n" +
                    "            \"untypedString\": \"0-314\",\n" +
                    "            \"uuid\": 314\n" +
                    "          },\n" +
                    "          \"templateId\": 505\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"contentTypeId\": 312,\n" +
                    "          \"context\": {\n" +
                    "            \"hostId\": 0,\n" +
                    "            \"longValue\": 317,\n" +
                    "            \"stringValue\": \"0-10-317\",\n" +
                    "            \"type\": 10,\n" +
                    "            \"untypedString\": \"0-317\",\n" +
                    "            \"uuid\": 317\n" +
                    "          },\n" +
                    "          \"description\": \"EI Home page location\",\n" +
                    "          \"locationSchemeGenerator\": \"Java/global/percussion/contentassembler/sys_JexlAssemblyLocation\",\n" +
                    "          \"name\": \"EI_Home\",\n" +
                    "          \"schemeId\": {\n" +
                    "            \"hostId\": 0,\n" +
                    "            \"longValue\": 317,\n" +
                    "            \"stringValue\": \"0-10-317\",\n" +
                    "            \"type\": 10,\n" +
                    "            \"untypedString\": \"0-317\",\n" +
                    "            \"uuid\": 317\n" +
                    "          },\n" +
                    "          \"templateId\": 530\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"contentTypeId\": 312,\n" +
                    "          \"context\": {\n" +
                    "            \"hostId\": 0,\n" +
                    "            \"longValue\": 318,\n" +
                    "            \"stringValue\": \"0-10-318\",\n" +
                    "            \"type\": 10,\n" +
                    "            \"untypedString\": \"0-318\",\n" +
                    "            \"uuid\": 318\n" +
                    "          },\n" +
                    "          \"description\": \"CI Home page location\",\n" +
                    "          \"locationSchemeGenerator\": \"Java/global/percussion/contentassembler/sys_JexlAssemblyLocation\",\n" +
                    "          \"name\": \"CI_Home\",\n" +
                    "          \"schemeId\": {\n" +
                    "            \"hostId\": 0,\n" +
                    "            \"longValue\": 318,\n" +
                    "            \"stringValue\": \"0-10-318\",\n" +
                    "            \"type\": 10,\n" +
                    "            \"untypedString\": \"0-318\",\n" +
                    "            \"uuid\": 318\n" +
                    "          },\n" +
                    "          \"templateId\": 545\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"contentTypeId\": 313,\n" +
                    "          \"context\": {\n" +
                    "            \"hostId\": 0,\n" +
                    "            \"longValue\": 323,\n" +
                    "            \"stringValue\": \"0-10-323\",\n" +
                    "            \"type\": 10,\n" +
                    "            \"untypedString\": \"0-323\",\n" +
                    "            \"uuid\": 323\n" +
                    "          },\n" +
                    "          \"description\": \"Publishing location for the rollover navigation image\",\n" +
                    "          \"locationSchemeGenerator\": \"Java/global/percussion/contentassembler/sys_JexlAssemblyLocation\",\n" +
                    "          \"name\": \"RolloverNavImage\",\n" +
                    "          \"schemeId\": {\n" +
                    "            \"hostId\": 0,\n" +
                    "            \"longValue\": 323,\n" +
                    "            \"stringValue\": \"0-10-323\",\n" +
                    "            \"type\": 10,\n" +
                    "            \"untypedString\": \"0-323\",\n" +
                    "            \"uuid\": 323\n" +
                    "          },\n" +
                    "          \"templateId\": 515\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"contentTypeId\": 313,\n" +
                    "          \"context\": {\n" +
                    "            \"hostId\": 0,\n" +
                    "            \"longValue\": 324,\n" +
                    "            \"stringValue\": \"0-10-324\",\n" +
                    "            \"type\": 10,\n" +
                    "            \"untypedString\": \"0-324\",\n" +
                    "            \"uuid\": 324\n" +
                    "          },\n" +
                    "          \"description\": \"Inactive navigation image publishing location\",\n" +
                    "          \"locationSchemeGenerator\": \"Java/global/percussion/contentassembler/sys_JexlAssemblyLocation\",\n" +
                    "          \"name\": \"InactiveNavImage\",\n" +
                    "          \"schemeId\": {\n" +
                    "            \"hostId\": 0,\n" +
                    "            \"longValue\": 324,\n" +
                    "            \"stringValue\": \"0-10-324\",\n" +
                    "            \"type\": 10,\n" +
                    "            \"untypedString\": \"0-324\",\n" +
                    "            \"uuid\": 324\n" +
                    "          },\n" +
                    "          \"templateId\": 517\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"defaultScheme\": {\n" +
                    "        \"contentTypeId\": 311,\n" +
                    "        \"context\": {\n" +
                    "          \"hostId\": 0,\n" +
                    "          \"longValue\": 100,\n" +
                    "          \"stringValue\": \"0-10-100\",\n" +
                    "          \"type\": 10,\n" +
                    "          \"untypedString\": \"0-100\",\n" +
                    "          \"uuid\": 100\n" +
                    "        },\n" +
                    "        \"description\": \"Resource file path for publishing\",\n" +
                    "        \"locationSchemeGenerator\": \"Java/global/percussion/contentassembler/perc_ResourceAssemblyLocation\",\n" +
                    "        \"name\": \"ResourceLocation\",\n" +
                    "        \"schemeId\": {\n" +
                    "          \"hostId\": 0,\n" +
                    "          \"longValue\": 100,\n" +
                    "          \"stringValue\": \"0-10-100\",\n" +
                    "          \"type\": 10,\n" +
                    "          \"untypedString\": \"0-100\",\n" +
                    "          \"uuid\": 100\n" +
                    "        },\n" +
                    "        \"templateId\": 505\n" +
                    "      },\n" +
                    "      \"description\": \"Create the appropriate File path\",\n" +
                    "      \"id\": {\n" +
                    "        \"hostId\": 0,\n" +
                    "        \"longValue\": 10,\n" +
                    "        \"stringValue\": \"0-113-10\",\n" +
                    "        \"type\": 113,\n" +
                    "        \"untypedString\": \"0-10\",\n" +
                    "        \"uuid\": 10\n" +
                    "      },\n" +
                    "      \"locationSchemes\": {\n" +
                    "        \"contentTypeId\": 311,\n" +
                    "        \"context\": {\n" +
                    "          \"hostId\": 0,\n" +
                    "          \"longValue\": 100,\n" +
                    "          \"stringValue\": \"0-10-100\",\n" +
                    "          \"type\": 10,\n" +
                    "          \"untypedString\": \"0-100\",\n" +
                    "          \"uuid\": 100\n" +
                    "        },\n" +
                    "        \"description\": \"Resource file path for publishing\",\n" +
                    "        \"locationSchemeGenerator\": \"Java/global/percussion/contentassembler/perc_ResourceAssemblyLocation\",\n" +
                    "        \"name\": \"ResourceLocation\",\n" +
                    "        \"schemeId\": {\n" +
                    "          \"hostId\": 0,\n" +
                    "          \"longValue\": 100,\n" +
                    "          \"stringValue\": \"0-10-100\",\n" +
                    "          \"type\": 10,\n" +
                    "          \"untypedString\": \"0-100\",\n" +
                    "          \"uuid\": 100\n" +
                    "        },\n" +
                    "        \"templateId\": 505\n" +
                    "      },\n" +
                    "      \"name\": \"ResourceLocation\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"defaultScheme\": {\n" +
                    "        \"contentTypeId\": 311,\n" +
                    "        \"context\": {\n" +
                    "          \"hostId\": 0,\n" +
                    "          \"longValue\": 200,\n" +
                    "          \"stringValue\": \"0-10-200\",\n" +
                    "          \"type\": 10,\n" +
                    "          \"untypedString\": \"0-200\",\n" +
                    "          \"uuid\": 200\n" +
                    "        },\n" +
                    "        \"description\": \"Resource url path for publishing\",\n" +
                    "        \"locationSchemeGenerator\": \"Java/global/percussion/contentassembler/perc_ResourceAssemblyLocation\",\n" +
                    "        \"name\": \"ResourceLink\",\n" +
                    "        \"schemeId\": {\n" +
                    "          \"hostId\": 0,\n" +
                    "          \"longValue\": 200,\n" +
                    "          \"stringValue\": \"0-10-200\",\n" +
                    "          \"type\": 10,\n" +
                    "          \"untypedString\": \"0-200\",\n" +
                    "          \"uuid\": 200\n" +
                    "        },\n" +
                    "        \"templateId\": 505\n" +
                    "      },\n" +
                    "      \"description\": \"Create the appropriate URL path\",\n" +
                    "      \"id\": {\n" +
                    "        \"hostId\": 0,\n" +
                    "        \"longValue\": 20,\n" +
                    "        \"stringValue\": \"0-113-20\",\n" +
                    "        \"type\": 113,\n" +
                    "        \"untypedString\": \"0-20\",\n" +
                    "        \"uuid\": 20\n" +
                    "      },\n" +
                    "      \"locationSchemes\": {\n" +
                    "        \"contentTypeId\": 311,\n" +
                    "        \"context\": {\n" +
                    "          \"hostId\": 0,\n" +
                    "          \"longValue\": 200,\n" +
                    "          \"stringValue\": \"0-10-200\",\n" +
                    "          \"type\": 10,\n" +
                    "          \"untypedString\": \"0-200\",\n" +
                    "          \"uuid\": 200\n" +
                    "        },\n" +
                    "        \"description\": \"Resource url path for publishing\",\n" +
                    "        \"locationSchemeGenerator\": \"Java/global/percussion/contentassembler/perc_ResourceAssemblyLocation\",\n" +
                    "        \"name\": \"ResourceLink\",\n" +
                    "        \"schemeId\": {\n" +
                    "          \"hostId\": 0,\n" +
                    "          \"longValue\": 200,\n" +
                    "          \"stringValue\": \"0-10-200\",\n" +
                    "          \"type\": 10,\n" +
                    "          \"untypedString\": \"0-200\",\n" +
                    "          \"uuid\": 200\n" +
                    "        },\n" +
                    "        \"templateId\": 505\n" +
                    "      },\n" +
                    "      \"name\": \"ResourceLink\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"id\": {\n" +
                    "        \"hostId\": 0,\n" +
                    "        \"longValue\": 301,\n" +
                    "        \"stringValue\": \"0-113-301\",\n" +
                    "        \"type\": 113,\n" +
                    "        \"untypedString\": \"0-301\",\n" +
                    "        \"uuid\": 301\n" +
                    "      },\n" +
                    "      \"locationSchemes\": [\n" +
                    "        {\n" +
                    "          \"contentTypeId\": 311,\n" +
                    "          \"context\": {\n" +
                    "            \"hostId\": 0,\n" +
                    "            \"longValue\": 315,\n" +
                    "            \"stringValue\": \"0-10-315\",\n" +
                    "            \"type\": 10,\n" +
                    "            \"untypedString\": \"0-315\",\n" +
                    "            \"uuid\": 315\n" +
                    "          },\n" +
                    "          \"description\": \"Generic location scheme for assembly\",\n" +
                    "          \"locationSchemeGenerator\": \"Java/global/percussion/contentassembler/sys_JexlAssemblyLocation\",\n" +
                    "          \"name\": \"Generic\",\n" +
                    "          \"schemeId\": {\n" +
                    "            \"hostId\": 0,\n" +
                    "            \"longValue\": 315,\n" +
                    "            \"stringValue\": \"0-10-315\",\n" +
                    "            \"type\": 10,\n" +
                    "            \"untypedString\": \"0-315\",\n" +
                    "            \"uuid\": 315\n" +
                    "          },\n" +
                    "          \"templateId\": 505\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"contentTypeId\": 312,\n" +
                    "          \"context\": {\n" +
                    "            \"hostId\": 0,\n" +
                    "            \"longValue\": 320,\n" +
                    "            \"stringValue\": \"0-10-320\",\n" +
                    "            \"type\": 10,\n" +
                    "            \"untypedString\": \"0-320\",\n" +
                    "            \"uuid\": 320\n" +
                    "          },\n" +
                    "          \"description\": \"EI Home page link location\",\n" +
                    "          \"locationSchemeGenerator\": \"Java/global/percussion/contentassembler/sys_JexlAssemblyLocation\",\n" +
                    "          \"name\": \"EI_Home\",\n" +
                    "          \"schemeId\": {\n" +
                    "            \"hostId\": 0,\n" +
                    "            \"longValue\": 320,\n" +
                    "            \"stringValue\": \"0-10-320\",\n" +
                    "            \"type\": 10,\n" +
                    "            \"untypedString\": \"0-320\",\n" +
                    "            \"uuid\": 320\n" +
                    "          },\n" +
                    "          \"templateId\": 530\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"contentTypeId\": 312,\n" +
                    "          \"context\": {\n" +
                    "            \"hostId\": 0,\n" +
                    "            \"longValue\": 321,\n" +
                    "            \"stringValue\": \"0-10-321\",\n" +
                    "            \"type\": 10,\n" +
                    "            \"untypedString\": \"0-321\",\n" +
                    "            \"uuid\": 321\n" +
                    "          },\n" +
                    "          \"description\": \"CI Home page link location\",\n" +
                    "          \"locationSchemeGenerator\": \"Java/global/percussion/contentassembler/sys_JexlAssemblyLocation\",\n" +
                    "          \"name\": \"CI_Home\",\n" +
                    "          \"schemeId\": {\n" +
                    "            \"hostId\": 0,\n" +
                    "            \"longValue\": 321,\n" +
                    "            \"stringValue\": \"0-10-321\",\n" +
                    "            \"type\": 10,\n" +
                    "            \"untypedString\": \"0-321\",\n" +
                    "            \"uuid\": 321\n" +
                    "          },\n" +
                    "          \"templateId\": 545\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"contentTypeId\": 313,\n" +
                    "          \"context\": {\n" +
                    "            \"hostId\": 0,\n" +
                    "            \"longValue\": 325,\n" +
                    "            \"stringValue\": \"0-10-325\",\n" +
                    "            \"type\": 10,\n" +
                    "            \"untypedString\": \"0-325\",\n" +
                    "            \"uuid\": 325\n" +
                    "          },\n" +
                    "          \"description\": \"Rollover navigation image url\",\n" +
                    "          \"locationSchemeGenerator\": \"Java/global/percussion/contentassembler/sys_JexlAssemblyLocation\",\n" +
                    "          \"name\": \"RolloverNavImage\",\n" +
                    "          \"schemeId\": {\n" +
                    "            \"hostId\": 0,\n" +
                    "            \"longValue\": 325,\n" +
                    "            \"stringValue\": \"0-10-325\",\n" +
                    "            \"type\": 10,\n" +
                    "            \"untypedString\": \"0-325\",\n" +
                    "            \"uuid\": 325\n" +
                    "          },\n" +
                    "          \"templateId\": 515\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"contentTypeId\": 313,\n" +
                    "          \"context\": {\n" +
                    "            \"hostId\": 0,\n" +
                    "            \"longValue\": 326,\n" +
                    "            \"stringValue\": \"0-10-326\",\n" +
                    "            \"type\": 10,\n" +
                    "            \"untypedString\": \"0-326\",\n" +
                    "            \"uuid\": 326\n" +
                    "          },\n" +
                    "          \"description\": \"Inactive navigation image url\",\n" +
                    "          \"locationSchemeGenerator\": \"Java/global/percussion/contentassembler/sys_JexlAssemblyLocation\",\n" +
                    "          \"name\": \"InactiveNavImage\",\n" +
                    "          \"schemeId\": {\n" +
                    "            \"hostId\": 0,\n" +
                    "            \"longValue\": 326,\n" +
                    "            \"stringValue\": \"0-10-326\",\n" +
                    "            \"type\": 10,\n" +
                    "            \"untypedString\": \"0-326\",\n" +
                    "            \"uuid\": 326\n" +
                    "          },\n" +
                    "          \"templateId\": 517\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}"))
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
