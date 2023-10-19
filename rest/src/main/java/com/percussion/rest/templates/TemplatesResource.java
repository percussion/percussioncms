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

package com.percussion.rest.templates;

import com.percussion.util.PSSiteManageBean;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@PSSiteManageBean(value="restTemplatesResource")
@Path("/templates")
@XmlRootElement
@Tag(name = "Templates", description = "Template operations")
public class TemplatesResource {

    @Autowired
    private ITemplatesAdaptor adaptor;

    @Context
    private UriInfo uriInfo;

    public TemplatesResource(){
        //NOOP
    }

    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    @Path("/summaries-by-filter")
    @Produces(
            {MediaType.APPLICATION_JSON})
    @Operation(summary = "List available Templates by Filter", description = "Lists Templates available for a given filter."
            , responses = {
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(array = @ArraySchema( schema = @Schema(implementation = TemplateSummary.class),
                            arraySchema = @Schema(implementation = TemplateSummaryList.class)),
                    examples = {@ExampleObject(value="{\n" +
                            "  \"TemplateSummaryList\": [\n" +
                            "    {\n" +
                            "      \"templateDescription\": \"Template used to render the page as XML.\",\n" +
                            "      \"templateId\": 347,\n" +
                            "      \"templateLabel\": \"perc.pageXml\",\n" +
                            "      \"templateName\": \"perc.pageXml\"\n" +
                            "    },\n" +
                            "    {\n" +
                            "      \"templateId\": 343,\n" +
                            "      \"templateLabel\": \"perc.title\",\n" +
                            "      \"templateName\": \"perc.pageDatabase\"\n" +
                            "    },\n" +
                            "    {\n" +
                            "      \"templateDescription\": \"The dispatch template for page assembly\",\n" +
                            "      \"templateId\": 345,\n" +
                            "      \"templateLabel\": \"Percussion Page Dispatch\",\n" +
                            "      \"templateName\": \"perc.pageDispatcher\"\n" +
                            "    },\n" +
                            "    {\n" +
                            "      \"templateId\": 335,\n" +
                            "      \"templateLabel\": \"Plain\",\n" +
                            "      \"templateName\": \"perc.base.plain\"\n" +
                            "    }\n" +
                            "  ]\n" +
                            "}")})),
            @ApiResponse(responseCode = "404", description = "No Templates found"),
            @ApiResponse(responseCode = "500", description = "Error")
    })
    public TemplateSummaryList listTemplateSummariesByFilter(@RequestBody(
            description = "TemplateFilter to use for listing TemplateSummaries.",
            required = true,
            content = @Content(
                    schema=@Schema(implementation = TemplateFilter.class),
                    examples = {@ExampleObject(value="{\"TemplateFilter\":{\"contentId\":\"27308\"}}")}))
                                                      @Valid TemplateFilter filter) {
        try{
            List<TemplateSummary> temp = adaptor.listTemplateSummaries(uriInfo.getBaseUri(), filter);
            if(temp == null || temp.isEmpty()){
                throw new WebApplicationException("Not Found.", 404);
            }
            return new TemplateSummaryList(temp);
        }catch (Exception e){
            throw new WebApplicationException(e.getMessage(), 500);
        }
    }

}
