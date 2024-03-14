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

package com.percussion.rest.contenttypes;

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

@PSSiteManageBean(value="restContentTypesResource")
@Path("/contenttypes")
@XmlRootElement
@Tag(name = "Content Types", description = "Content Type operations")
public class ContentTypesResource {

    @Autowired
    private IContentTypesAdaptor adaptor;

    @Context
    private UriInfo uriInfo;

    public ContentTypesResource(){
        //noop
    }

    @GET
    @Path("/")
    @Produces(
            {MediaType.APPLICATION_JSON})
    @Operation(summary = "List available ContentTypes", description = "Lists all available Content Types on the system.  Not filtered by security."
            , responses = {
             @ApiResponse(responseCode = "200", description = "OK",
             content = @Content(array = @ArraySchema( schema = @Schema(implementation = ContentType.class)),
             examples=@ExampleObject(value = "{\n" +
                     "  \"ContentType\": [\n" +
                     "    {\n" +
                     "      \"description\": \"The Content Type for Page Template items\",\n" +
                     "      \"guid\": {\n" +
                     "        \"hostId\": 0,\n" +
                     "        \"longValue\": 303,\n" +
                     "        \"stringValue\": \"0-2-303\",\n" +
                     "        \"type\": 2,\n" +
                     "        \"untypedString\": \"0-303\",\n" +
                     "        \"uuid\": 303\n" +
                     "      },\n" +
                     "      \"hideFromMenu\": false,\n" +
                     "      \"label\": \"percPageTemplate\",\n" +
                     "      \"name\": \"percPageTemplate\"\n" +
                     "    },\n" +
                     "    {\n" +
                     "      \"description\": \"\",\n" +
                     "      \"guid\": {\n" +
                     "        \"hostId\": 0,\n" +
                     "        \"longValue\": 301,\n" +
                     "        \"stringValue\": \"0-2-301\",\n" +
                     "        \"type\": 2,\n" +
                     "        \"untypedString\": \"0-301\",\n" +
                     "        \"uuid\": 301\n" +
                     "      },\n" +
                     "      \"hideFromMenu\": false,\n" +
                     "      \"label\": \"page\",\n" +
                     "      \"name\": \"percPage\"\n" +
                     "    },\n" +
                     "    {\n" +
                     "      \"description\": \"\",\n" +
                     "      \"guid\": {\n" +
                     "        \"hostId\": 0,\n" +
                     "        \"longValue\": 307,\n" +
                     "        \"stringValue\": \"0-2-307\",\n" +
                     "        \"type\": 2,\n" +
                     "        \"untypedString\": \"0-307\",\n" +
                     "        \"uuid\": 307\n" +
                     "      },\n" +
                     "      \"hideFromMenu\": false,\n" +
                     "      \"label\": \"HTML\",\n" +
                     "      \"name\": \"percRawHtmlAsset\"\n" +
                     "    },\n" +
                     "    {\n" +
                     "      \"description\": \"\",\n" +
                     "      \"guid\": {\n" +
                     "        \"hostId\": 0,\n" +
                     "        \"longValue\": 305,\n" +
                     "        \"stringValue\": \"0-2-305\",\n" +
                     "        \"type\": 2,\n" +
                     "        \"untypedString\": \"0-305\",\n" +
                     "        \"uuid\": 305\n" +
                     "      },\n" +
                     "      \"hideFromMenu\": false,\n" +
                     "      \"label\": \"percUserProfile\",\n" +
                     "      \"name\": \"percUserProfile\"\n" +
                     "    },\n" +
                     "    {\n" +
                     "      \"description\": \"\",\n" +
                     "      \"guid\": {\n" +
                     "        \"hostId\": 0,\n" +
                     "        \"longValue\": 311,\n" +
                     "        \"stringValue\": \"0-2-311\",\n" +
                     "        \"type\": 2,\n" +
                     "        \"untypedString\": \"0-311\",\n" +
                     "        \"uuid\": 311\n" +
                     "      },\n" +
                     "      \"hideFromMenu\": false,\n" +
                     "      \"label\": \"Simple Text\",\n" +
                     "      \"name\": \"percSimpleTextAsset\"\n" +
                     "    },\n" +
                     "    {\n" +
                     "      \"description\": \"\",\n" +
                     "      \"guid\": {\n" +
                     "        \"hostId\": 0,\n" +
                     "        \"longValue\": 309,\n" +
                     "        \"stringValue\": \"0-2-309\",\n" +
                     "        \"type\": 2,\n" +
                     "        \"untypedString\": \"0-309\",\n" +
                     "        \"uuid\": 309\n" +
                     "      },\n" +
                     "      \"hideFromMenu\": false,\n" +
                     "      \"label\": \"Rich Text\",\n" +
                     "      \"name\": \"percRichTextAsset\"\n" +
                     "    },\n" +
                     "    {\n" +
                     "      \"description\": \"Content Type for basic Managed Navigation Content Items.\",\n" +
                     "      \"guid\": {\n" +
                     "        \"hostId\": 0,\n" +
                     "        \"longValue\": 314,\n" +
                     "        \"stringValue\": \"0-2-314\",\n" +
                     "        \"type\": 2,\n" +
                     "        \"untypedString\": \"0-314\",\n" +
                     "        \"uuid\": 314\n" +
                     "      },\n" +
                     "      \"hideFromMenu\": false,\n" +
                     "      \"label\": \"Navon\",\n" +
                     "      \"name\": \"rffNavon\"\n" +
                     "    },\n" +
                     "    {\n" +
                     "      \"description\": \"Managed Navigation Content Item used in the root Folder of a Site.  The Site root Folder must contain a NavTree Content Item for Managed Navigation to work.\",\n" +
                     "      \"guid\": {\n" +
                     "        \"hostId\": 0,\n" +
                     "        \"longValue\": 315,\n" +
                     "        \"stringValue\": \"0-2-315\",\n" +
                     "        \"type\": 2,\n" +
                     "        \"untypedString\": \"0-315\",\n" +
                     "        \"uuid\": 315\n" +
                     "      },\n" +
                     "      \"hideFromMenu\": false,\n" +
                     "      \"label\": \"NavTree\",\n" +
                     "      \"name\": \"rffNavTree\"\n" +
                     "    },\n" +
                     "    {\n" +
                     "      \"description\": \"A binary image file used in Managed Navigation.\",\n" +
                     "      \"guid\": {\n" +
                     "        \"hostId\": 0,\n" +
                     "        \"longValue\": 313,\n" +
                     "        \"stringValue\": \"0-2-313\",\n" +
                     "        \"type\": 2,\n" +
                     "        \"untypedString\": \"0-313\",\n" +
                     "        \"uuid\": 313\n" +
                     "      },\n" +
                     "      \"hideFromMenu\": false,\n" +
                     "      \"label\": \"Nav Image\",\n" +
                     "      \"name\": \"rffNavImage\"\n" +
                     "    },\n" +
                     "    {\n" +
                     "      \"description\": \"\",\n" +
                     "      \"guid\": {\n" +
                     "        \"hostId\": 0,\n" +
                     "        \"longValue\": 1726,\n" +
                     "        \"stringValue\": \"0-2-1726\",\n" +
                     "        \"type\": 2,\n" +
                     "        \"untypedString\": \"0-1726\",\n" +
                     "        \"uuid\": 1726\n" +
                     "      },\n" +
                     "      \"hideFromMenu\": false,\n" +
                     "      \"label\": \"percSocialButtons\",\n" +
                     "      \"name\": \"percSocialButtons\"\n" +
                     "    },\n" +
                     "    {\n" +
                     "      \"description\": \"A binary image file used in Managed Navigation.\",\n" +
                     "      \"guid\": {\n" +
                     "        \"hostId\": 0,\n" +
                     "        \"longValue\": 2239,\n" +
                     "        \"stringValue\": \"0-2-2239\",\n" +
                     "        \"type\": 2,\n" +
                     "        \"untypedString\": \"0-2239\",\n" +
                     "        \"uuid\": 2239\n" +
                     "      },\n" +
                     "      \"hideFromMenu\": false,\n" +
                     "      \"label\": \"Nav Image\",\n" +
                     "      \"name\": \"percNavImage\"\n" +
                     "    },\n" +
                     "    {\n" +
                     "      \"description\": \"Stores all property data for all widgets.\",\n" +
                     "      \"guid\": {\n" +
                     "        \"hostId\": 0,\n" +
                     "        \"longValue\": 2237,\n" +
                     "        \"stringValue\": \"0-2-2237\",\n" +
                     "        \"type\": 2,\n" +
                     "        \"untypedString\": \"0-2237\",\n" +
                     "        \"uuid\": 2237\n" +
                     "      },\n" +
                     "      \"hideFromMenu\": false,\n" +
                     "      \"label\": \"PSWidgetProperties\",\n" +
                     "      \"name\": \"PSWidgetProperties\"\n" +
                     "    },\n" +
                     "    {\n" +
                     "      \"description\": \"\",\n" +
                     "      \"guid\": {\n" +
                     "        \"hostId\": 0,\n" +
                     "        \"longValue\": 1730,\n" +
                     "        \"stringValue\": \"0-2-1730\",\n" +
                     "        \"type\": 2,\n" +
                     "        \"untypedString\": \"0-1730\",\n" +
                     "        \"uuid\": 1730\n" +
                     "      },\n" +
                     "      \"hideFromMenu\": false,\n" +
                     "      \"label\": \"Open Graph\",\n" +
                     "      \"name\": \"percOpenGraph\"\n" +
                     "    },\n" +
                     "    {\n" +
                     "      \"description\": \"\",\n" +
                     "      \"guid\": {\n" +
                     "        \"hostId\": 0,\n" +
                     "        \"longValue\": 1728,\n" +
                     "        \"stringValue\": \"0-2-1728\",\n" +
                     "        \"type\": 2,\n" +
                     "        \"untypedString\": \"0-1728\",\n" +
                     "        \"uuid\": 1728\n" +
                     "      },\n" +
                     "      \"hideFromMenu\": false,\n" +
                     "      \"label\": \"percImageSlider\",\n" +
                     "      \"name\": \"percImageSlider\"\n" +
                     "    },\n" +
                     "    {\n" +
                     "      \"description\": \"\",\n" +
                     "      \"guid\": {\n" +
                     "        \"hostId\": 0,\n" +
                     "        \"longValue\": 1223,\n" +
                     "        \"stringValue\": \"0-2-1223\",\n" +
                     "        \"type\": 2,\n" +
                     "        \"untypedString\": \"0-1223\",\n" +
                     "        \"uuid\": 1223\n" +
                     "      },\n" +
                     "      \"hideFromMenu\": false,\n" +
                     "      \"label\": \"percDirectory\",\n" +
                     "      \"name\": \"percDirectory\"\n" +
                     "    },\n" +
                     "    {\n" +
                     "      \"description\": \"Content Type for basic Managed Navigation Content Items.\",\n" +
                     "      \"guid\": {\n" +
                     "        \"hostId\": 0,\n" +
                     "        \"longValue\": 324,\n" +
                     "        \"stringValue\": \"0-2-324\",\n" +
                     "        \"type\": 2,\n" +
                     "        \"untypedString\": \"0-324\",\n" +
                     "        \"uuid\": 324\n" +
                     "      },\n" +
                     "      \"hideFromMenu\": false,\n" +
                     "      \"label\": \"Navon\",\n" +
                     "      \"name\": \"percNavon\"\n" +
                     "    },\n" +
                     "    {\n" +
                     "      \"description\": \"\",\n" +
                     "      \"guid\": {\n" +
                     "        \"hostId\": 0,\n" +
                     "        \"longValue\": 1732,\n" +
                     "        \"stringValue\": \"0-2-1732\",\n" +
                     "        \"type\": 2,\n" +
                     "        \"untypedString\": \"0-1732\",\n" +
                     "        \"uuid\": 1732\n" +
                     "      },\n" +
                     "      \"hideFromMenu\": false,\n" +
                     "      \"label\": \"Twitter Cards\",\n" +
                     "      \"name\": \"percTwitterCards\"\n" +
                     "    },\n" +
                     "    {\n" +
                     "      \"description\": \"Managed Navigation Content Item used in the root Folder of a Site.  The Site root Folder must contain a NavTree Content Item for Managed Navigation to work.\",\n" +
                     "      \"guid\": {\n" +
                     "        \"hostId\": 0,\n" +
                     "        \"longValue\": 325,\n" +
                     "        \"stringValue\": \"0-2-325\",\n" +
                     "        \"type\": 2,\n" +
                     "        \"untypedString\": \"0-325\",\n" +
                     "        \"uuid\": 325\n" +
                     "      },\n" +
                     "      \"hideFromMenu\": false,\n" +
                     "      \"label\": \"NavTree\",\n" +
                     "      \"name\": \"percNavTree\"\n" +
                     "    },\n" +
                     "    {\n" +
                     "      \"description\": \"\",\n" +
                     "      \"guid\": {\n" +
                     "        \"hostId\": 0,\n" +
                     "        \"longValue\": 1221,\n" +
                     "        \"stringValue\": \"0-2-1221\",\n" +
                     "        \"type\": 2,\n" +
                     "        \"untypedString\": \"0-1221\",\n" +
                     "        \"uuid\": 1221\n" +
                     "      },\n" +
                     "      \"hideFromMenu\": false,\n" +
                     "      \"label\": \"percDepartment\",\n" +
                     "      \"name\": \"percDepartment\"\n" +
                     "    },\n" +
                     "    {\n" +
                     "      \"description\": \"\",\n" +
                     "      \"guid\": {\n" +
                     "        \"hostId\": 0,\n" +
                     "        \"longValue\": 1227,\n" +
                     "        \"stringValue\": \"0-2-1227\",\n" +
                     "        \"type\": 2,\n" +
                     "        \"untypedString\": \"0-1227\",\n" +
                     "        \"uuid\": 1227\n" +
                     "      },\n" +
                     "      \"hideFromMenu\": false,\n" +
                     "      \"label\": \"percPerson\",\n" +
                     "      \"name\": \"percPerson\"\n" +
                     "    },\n" +
                     "    {\n" +
                     "      \"description\": \"Manages binary files other than images, such as .pdf or .doc.\",\n" +
                     "      \"guid\": {\n" +
                     "        \"hostId\": 0,\n" +
                     "        \"longValue\": 331,\n" +
                     "        \"stringValue\": \"0-2-331\",\n" +
                     "        \"type\": 2,\n" +
                     "        \"untypedString\": \"0-331\",\n" +
                     "        \"uuid\": 331\n" +
                     "      },\n" +
                     "      \"hideFromMenu\": false,\n" +
                     "      \"label\": \"File\",\n" +
                     "      \"name\": \"percFileAsset\"\n" +
                     "    },\n" +
                     "    {\n" +
                     "      \"description\": \"\",\n" +
                     "      \"guid\": {\n" +
                     "        \"hostId\": 0,\n" +
                     "        \"longValue\": 1225,\n" +
                     "        \"stringValue\": \"0-2-1225\",\n" +
                     "        \"type\": 2,\n" +
                     "        \"untypedString\": \"0-1225\",\n" +
                     "        \"uuid\": 1225\n" +
                     "      },\n" +
                     "      \"hideFromMenu\": false,\n" +
                     "      \"label\": \"percOrganization\",\n" +
                     "      \"name\": \"percOrganization\"\n" +
                     "    },\n" +
                     "    {\n" +
                     "      \"description\": \"\",\n" +
                     "      \"guid\": {\n" +
                     "        \"hostId\": 0,\n" +
                     "        \"longValue\": 329,\n" +
                     "        \"stringValue\": \"0-2-329\",\n" +
                     "        \"type\": 2,\n" +
                     "        \"untypedString\": \"0-329\",\n" +
                     "        \"uuid\": 329\n" +
                     "      },\n" +
                     "      \"hideFromMenu\": false,\n" +
                     "      \"label\": \"Event\",\n" +
                     "      \"name\": \"percEventAsset\"\n" +
                     "    },\n" +
                     "    {\n" +
                     "      \"description\": \"\",\n" +
                     "      \"guid\": {\n" +
                     "        \"hostId\": 0,\n" +
                     "        \"longValue\": 1358,\n" +
                     "        \"stringValue\": \"0-2-1358\",\n" +
                     "        \"type\": 2,\n" +
                     "        \"untypedString\": \"0-1358\",\n" +
                     "        \"uuid\": 1358\n" +
                     "      },\n" +
                     "      \"hideFromMenu\": false,\n" +
                     "      \"label\": \"percEmsEventList\",\n" +
                     "      \"name\": \"percEmsEventList\"\n" +
                     "    },\n" +
                     "    {\n" +
                     "      \"description\": \"Manages binary files other than images, such as .pdf or .doc.\",\n" +
                     "      \"guid\": {\n" +
                     "        \"hostId\": 0,\n" +
                     "        \"longValue\": 335,\n" +
                     "        \"stringValue\": \"0-2-335\",\n" +
                     "        \"type\": 2,\n" +
                     "        \"untypedString\": \"0-335\",\n" +
                     "        \"uuid\": 335\n" +
                     "      },\n" +
                     "      \"hideFromMenu\": false,\n" +
                     "      \"label\": \"Flash\",\n" +
                     "      \"name\": \"percFlashAsset\"\n" +
                     "    },\n" +
                     "    {\n" +
                     "      \"description\": \"A widget that displays an 'accept consent' dialog on the website.\",\n" +
                     "      \"guid\": {\n" +
                     "        \"hostId\": 0,\n" +
                     "        \"longValue\": 1356,\n" +
                     "        \"stringValue\": \"0-2-1356\",\n" +
                     "        \"type\": 2,\n" +
                     "        \"untypedString\": \"0-1356\",\n" +
                     "        \"uuid\": 1356\n" +
                     "      },\n" +
                     "      \"hideFromMenu\": false,\n" +
                     "      \"label\": \"percCookieConsent\",\n" +
                     "      \"name\": \"percCookieConsent\"\n" +
                     "    },\n" +
                     "    {\n" +
                     "      \"description\": \"\",\n" +
                     "      \"guid\": {\n" +
                     "        \"hostId\": 0,\n" +
                     "        \"longValue\": 333,\n" +
                     "        \"stringValue\": \"0-2-333\",\n" +
                     "        \"type\": 2,\n" +
                     "        \"untypedString\": \"0-333\",\n" +
                     "        \"uuid\": 333\n" +
                     "      },\n" +
                     "      \"hideFromMenu\": false,\n" +
                     "      \"label\": \"percFileAutoList\",\n" +
                     "      \"name\": \"percFileAutoList\"\n" +
                     "    },\n" +
                     "    {\n" +
                     "      \"description\": \"\",\n" +
                     "      \"guid\": {\n" +
                     "        \"hostId\": 0,\n" +
                     "        \"longValue\": 339,\n" +
                     "        \"stringValue\": \"0-2-339\",\n" +
                     "        \"type\": 2,\n" +
                     "        \"untypedString\": \"0-339\",\n" +
                     "        \"uuid\": 339\n" +
                     "      },\n" +
                     "      \"hideFromMenu\": false,\n" +
                     "      \"label\": \"Comments Form\",\n" +
                     "      \"name\": \"percCommentsFormAsset\"\n" +
                     "    },\n" +
                     "    {\n" +
                     "      \"description\": \"\",\n" +
                     "      \"guid\": {\n" +
                     "        \"hostId\": 0,\n" +
                     "        \"longValue\": 337,\n" +
                     "        \"stringValue\": \"0-2-337\",\n" +
                     "        \"type\": 2,\n" +
                     "        \"untypedString\": \"0-337\",\n" +
                     "        \"uuid\": 337\n" +
                     "      },\n" +
                     "      \"hideFromMenu\": false,\n" +
                     "      \"label\": \"Form\",\n" +
                     "      \"name\": \"percFormAsset\"\n" +
                     "    },\n" +
                     "    {\n" +
                     "      \"description\": \"\",\n" +
                     "      \"guid\": {\n" +
                     "        \"hostId\": 0,\n" +
                     "        \"longValue\": 343,\n" +
                     "        \"stringValue\": \"0-2-343\",\n" +
                     "        \"type\": 2,\n" +
                     "        \"untypedString\": \"0-343\",\n" +
                     "        \"uuid\": 343\n" +
                     "      },\n" +
                     "      \"hideFromMenu\": false,\n" +
                     "      \"label\": \"percBlogIndexAsset\",\n" +
                     "      \"name\": \"percBlogIndexAsset\"\n" +
                     "    },\n" +
                     "    {\n" +
                     "      \"description\": \"\",\n" +
                     "      \"guid\": {\n" +
                     "        \"hostId\": 0,\n" +
                     "        \"longValue\": 341,\n" +
                     "        \"stringValue\": \"0-2-341\",\n" +
                     "        \"type\": 2,\n" +
                     "        \"untypedString\": \"0-341\",\n" +
                     "        \"uuid\": 341\n" +
                     "      },\n" +
                     "      \"hideFromMenu\": false,\n" +
                     "      \"label\": \"Page Auto List\",\n" +
                     "      \"name\": \"percPageAutoList\"\n" +
                     "    },\n" +
                     "    {\n" +
                     "      \"description\": \"Category widget\",\n" +
                     "      \"guid\": {\n" +
                     "        \"hostId\": 0,\n" +
                     "        \"longValue\": 347,\n" +
                     "        \"stringValue\": \"0-2-347\",\n" +
                     "        \"type\": 2,\n" +
                     "        \"untypedString\": \"0-347\",\n" +
                     "        \"uuid\": 347\n" +
                     "      },\n" +
                     "      \"hideFromMenu\": false,\n" +
                     "      \"label\": \"Categories\",\n" +
                     "      \"name\": \"percCategoryList\"\n" +
                     "    },\n" +
                     "    {\n" +
                     "      \"description\": \"Tag widget\",\n" +
                     "      \"guid\": {\n" +
                     "        \"hostId\": 0,\n" +
                     "        \"longValue\": 345,\n" +
                     "        \"stringValue\": \"0-2-345\",\n" +
                     "        \"type\": 2,\n" +
                     "        \"untypedString\": \"0-345\",\n" +
                     "        \"uuid\": 345\n" +
                     "      },\n" +
                     "      \"hideFromMenu\": false,\n" +
                     "      \"label\": \"Tags\",\n" +
                     "      \"name\": \"percTagList\"\n" +
                     "    },\n" +
                     "    {\n" +
                     "      \"description\": \"\",\n" +
                     "      \"guid\": {\n" +
                     "        \"hostId\": 0,\n" +
                     "        \"longValue\": 351,\n" +
                     "        \"stringValue\": \"0-2-351\",\n" +
                     "        \"type\": 2,\n" +
                     "        \"untypedString\": \"0-351\",\n" +
                     "        \"uuid\": 351\n" +
                     "      },\n" +
                     "      \"hideFromMenu\": false,\n" +
                     "      \"label\": \"Archives\",\n" +
                     "      \"name\": \"percArchiveList\"\n" +
                     "    },\n" +
                     "    {\n" +
                     "      \"description\": \"\",\n" +
                     "      \"guid\": {\n" +
                     "        \"hostId\": 0,\n" +
                     "        \"longValue\": 861,\n" +
                     "        \"stringValue\": \"0-2-861\",\n" +
                     "        \"type\": 2,\n" +
                     "        \"untypedString\": \"0-861\",\n" +
                     "        \"uuid\": 861\n" +
                     "      },\n" +
                     "      \"hideFromMenu\": false,\n" +
                     "      \"label\": \"preRedirectWidget\",\n" +
                     "      \"name\": \"preRedirectWidget\"\n" +
                     "    },\n" +
                     "    {\n" +
                     "      \"description\": \"\",\n" +
                     "      \"guid\": {\n" +
                     "        \"hostId\": 0,\n" +
                     "        \"longValue\": 349,\n" +
                     "        \"stringValue\": \"0-2-349\",\n" +
                     "        \"type\": 2,\n" +
                     "        \"untypedString\": \"0-349\",\n" +
                     "        \"uuid\": 349\n" +
                     "      },\n" +
                     "      \"hideFromMenu\": false,\n" +
                     "      \"label\": \"Calendar\",\n" +
                     "      \"name\": \"percCalendarAsset\"\n" +
                     "    },\n" +
                     "    {\n" +
                     "      \"description\": \"Title Widget\",\n" +
                     "      \"guid\": {\n" +
                     "        \"hostId\": 0,\n" +
                     "        \"longValue\": 355,\n" +
                     "        \"stringValue\": \"0-2-355\",\n" +
                     "        \"type\": 2,\n" +
                     "        \"untypedString\": \"0-355\",\n" +
                     "        \"uuid\": 355\n" +
                     "      },\n" +
                     "      \"hideFromMenu\": false,\n" +
                     "      \"label\": \"percTitleAsset\",\n" +
                     "      \"name\": \"percTitleAsset\"\n" +
                     "    },\n" +
                     "    {\n" +
                     "      \"description\": \"\",\n" +
                     "      \"guid\": {\n" +
                     "        \"hostId\": 0,\n" +
                     "        \"longValue\": 353,\n" +
                     "        \"stringValue\": \"0-2-353\",\n" +
                     "        \"type\": 2,\n" +
                     "        \"untypedString\": \"0-353\",\n" +
                     "        \"uuid\": 353\n" +
                     "      },\n" +
                     "      \"hideFromMenu\": false,\n" +
                     "      \"label\": \"Image Auto List\",\n" +
                     "      \"name\": \"percImageAutoList\"\n" +
                     "    },\n" +
                     "    {\n" +
                     "      \"description\": \"A widget to read rss feeds.\",\n" +
                     "      \"guid\": {\n" +
                     "        \"hostId\": 0,\n" +
                     "        \"longValue\": 359,\n" +
                     "        \"stringValue\": \"0-2-359\",\n" +
                     "        \"type\": 2,\n" +
                     "        \"untypedString\": \"0-359\",\n" +
                     "        \"uuid\": 359\n" +
                     "      },\n" +
                     "      \"hideFromMenu\": false,\n" +
                     "      \"label\": \"RSS\",\n" +
                     "      \"name\": \"percRssAsset\"\n" +
                     "    },\n" +
                     "    {\n" +
                     "      \"description\": \"\",\n" +
                     "      \"guid\": {\n" +
                     "        \"hostId\": 0,\n" +
                     "        \"longValue\": 357,\n" +
                     "        \"stringValue\": \"0-2-357\",\n" +
                     "        \"type\": 2,\n" +
                     "        \"untypedString\": \"0-357\",\n" +
                     "        \"uuid\": 357\n" +
                     "      },\n" +
                     "      \"hideFromMenu\": false,\n" +
                     "      \"label\": \"Blog Post\",\n" +
                     "      \"name\": \"percBlogPostAsset\"\n" +
                     "    },\n" +
                     "    {\n" +
                     "      \"description\": \"This is a system contenttype for folders.\",\n" +
                     "      \"guid\": {\n" +
                     "        \"hostId\": 0,\n" +
                     "        \"longValue\": 101,\n" +
                     "        \"stringValue\": \"0-2-101\",\n" +
                     "        \"type\": 2,\n" +
                     "        \"untypedString\": \"0-101\",\n" +
                     "        \"uuid\": 101\n" +
                     "      },\n" +
                     "      \"hideFromMenu\": false,\n" +
                     "      \"label\": \"Folder\",\n" +
                     "      \"name\": \"Folder\"\n" +
                     "    },\n" +
                     "    {\n" +
                     "      \"description\": \"\",\n" +
                     "      \"guid\": {\n" +
                     "        \"hostId\": 0,\n" +
                     "        \"longValue\": 363,\n" +
                     "        \"stringValue\": \"0-2-363\",\n" +
                     "        \"type\": 2,\n" +
                     "        \"untypedString\": \"0-363\",\n" +
                     "        \"uuid\": 363\n" +
                     "      },\n" +
                     "      \"hideFromMenu\": false,\n" +
                     "      \"label\": \"Registration Asset\",\n" +
                     "      \"name\": \"percRegistrationAsset\"\n" +
                     "    },\n" +
                     "    {\n" +
                     "      \"description\": \"Login Asset for Content configuration.\",\n" +
                     "      \"guid\": {\n" +
                     "        \"hostId\": 0,\n" +
                     "        \"longValue\": 361,\n" +
                     "        \"stringValue\": \"0-2-361\",\n" +
                     "        \"type\": 2,\n" +
                     "        \"untypedString\": \"0-361\",\n" +
                     "        \"uuid\": 361\n" +
                     "      },\n" +
                     "      \"hideFromMenu\": false,\n" +
                     "      \"label\": \"Login\",\n" +
                     "      \"name\": \"percLoginAsset\"\n" +
                     "    },\n" +
                     "    {\n" +
                     "      \"description\": \"\",\n" +
                     "      \"guid\": {\n" +
                     "        \"hostId\": 0,\n" +
                     "        \"longValue\": 367,\n" +
                     "        \"stringValue\": \"0-2-367\",\n" +
                     "        \"type\": 2,\n" +
                     "        \"untypedString\": \"0-367\",\n" +
                     "        \"uuid\": 367\n" +
                     "      },\n" +
                     "      \"hideFromMenu\": false,\n" +
                     "      \"label\": \"Secure Login\",\n" +
                     "      \"name\": \"percSecureLogin\"\n" +
                     "    },\n" +
                     "    {\n" +
                     "      \"description\": \"\",\n" +
                     "      \"guid\": {\n" +
                     "        \"hostId\": 0,\n" +
                     "        \"longValue\": 365,\n" +
                     "        \"stringValue\": \"0-2-365\",\n" +
                     "        \"type\": 2,\n" +
                     "        \"untypedString\": \"0-365\",\n" +
                     "        \"uuid\": 365\n" +
                     "      },\n" +
                     "      \"hideFromMenu\": false,\n" +
                     "      \"label\": \"Polls\",\n" +
                     "      \"name\": \"percPollAsset\"\n" +
                     "    },\n" +
                     "    {\n" +
                     "      \"description\": \"\",\n" +
                     "      \"guid\": {\n" +
                     "        \"hostId\": 0,\n" +
                     "        \"longValue\": 1011,\n" +
                     "        \"stringValue\": \"0-2-1011\",\n" +
                     "        \"type\": 2,\n" +
                     "        \"untypedString\": \"0-1011\",\n" +
                     "        \"uuid\": 1011\n" +
                     "      },\n" +
                     "      \"hideFromMenu\": false,\n" +
                     "      \"label\": \"percLocalLanguage\",\n" +
                     "      \"name\": \"percLocalLanguage\"\n" +
                     "    },\n" +
                     "    {\n" +
                     "      \"description\": \"\",\n" +
                     "      \"guid\": {\n" +
                     "        \"hostId\": 0,\n" +
                     "        \"longValue\": 369,\n" +
                     "        \"stringValue\": \"0-2-369\",\n" +
                     "        \"type\": 2,\n" +
                     "        \"untypedString\": \"0-369\",\n" +
                     "        \"uuid\": 369\n" +
                     "      },\n" +
                     "      \"hideFromMenu\": false,\n" +
                     "      \"label\": \"Image Asset\",\n" +
                     "      \"name\": \"percImageAsset\"\n" +
                     "    },\n" +
                     "    {\n" +
                     "      \"description\": \"\",\n" +
                     "      \"guid\": {\n" +
                     "        \"hostId\": 0,\n" +
                     "        \"longValue\": 1009,\n" +
                     "        \"stringValue\": \"0-2-1009\",\n" +
                     "        \"type\": 2,\n" +
                     "        \"untypedString\": \"0-1009\",\n" +
                     "        \"uuid\": 1009\n" +
                     "      },\n" +
                     "      \"hideFromMenu\": false,\n" +
                     "      \"label\": \"percDefaultLanguage\",\n" +
                     "      \"name\": \"percDefaultLanguage\"\n" +
                     "    }\n" +
                     "  ]\n" +
                     "}"))),
             @ApiResponse(responseCode = "404", description = "No Content Types found"),
             @ApiResponse(responseCode = "500", description = "Error")
    })
    public List<ContentType> listContentTypes()
    {
        return new ContentTypeList(adaptor.listContentTypes(uriInfo.getBaseUri()));
    }

    @GET
    @Path("/by-site/{id}")
    @Produces(
            {MediaType.APPLICATION_JSON})
    @Operation(summary = "List available Content Types by Site", description = "Lists Content Types available for a site."
            , responses = {
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(array = @ArraySchema( schema = @Schema(implementation = ContentType.class)))),
            @ApiResponse(responseCode = "404", description = "No Content Types found"),
            @ApiResponse(responseCode = "500", description = "Error")
    })
    public List<ContentType> getContentTypesBySite( @PathParam("id") int siteId)
    {
        return new ContentTypeList(adaptor.listContentTypes(uriInfo.getBaseUri(), siteId));
    }


    @GET
    @Consumes({MediaType.APPLICATION_JSON})
    @Path("/by-filter")
    @Produces(
            {MediaType.APPLICATION_JSON})
    @Operation(summary = "List available Content Types by Filter", description = "Lists Content Types available for a given filter."
            , responses = {
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(array = @ArraySchema( schema = @Schema(implementation = ContentType.class)))),
            @ApiResponse(responseCode = "404", description = "No Content Types found"),
            @ApiResponse(responseCode = "500", description = "Error")
    })
    public List<ContentType> listContentTypesByFilter(@RequestBody(description = "ContentTypeFilter to use for listing content types.",
            required = true,
            content = @Content(
                    schema=@Schema(implementation = ContentTypeFilter.class)))
                                                          @Valid ContentTypeFilter filter) {
        try{
            List<ContentType> results = adaptor.listContentTypesByFilter(uriInfo.getBaseUri(), filter);
            if(results == null || results.isEmpty()){
                throw new WebApplicationException("Not Found.", 404);
            }
            return results;
        }catch (Exception e){
            throw new WebApplicationException(e.getMessage(), 500);
        }
    }
}
