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

package com.percussion.rest.velocity;

import com.percussion.rest.extensions.Extension;
import com.percussion.rest.extensions.ExtensionFilterOptions;
import com.percussion.rest.extensions.ExtensionList;
import com.percussion.rest.extensions.IExtensionAdaptor;
import com.percussion.util.PSSiteManageBean;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/***
 * Provides a resource for dealing with Velocity related activities
 */
@PSSiteManageBean(value="restVelocityResource")
@Path("/velocity")
@XmlRootElement
@Tag(name = "Velocity Template Engine", description = "Velocity related operations")
public class VelocityResource {

    @Autowired
    IExtensionAdaptor extensionAdaptor;

    @Context
    private UriInfo uriInfo;

    @GET
    @Path("/tools")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary="Returns a list of all registered Jexl extensions on the System",
            responses= {
            @ApiResponse(responseCode = "200", description = "OK", content=@Content(
                    array=@ArraySchema(schema=@Schema(implementation = Extension.class))
            ))
            })
    public List<Extension> listVelocityExtensions(){
        ExtensionFilterOptions filter = new ExtensionFilterOptions();

        filter.setContext("global/percussion/velocity/");
        return new ExtensionList(extensionAdaptor.getExtensions(uriInfo.getBaseUri(),filter));
    }

}
