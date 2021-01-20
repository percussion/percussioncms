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

package com.percussion.rest.contentlists;

import com.percussion.rest.extensions.Extension;
import com.percussion.rest.extensions.ExtensionFilterOptions;
import com.percussion.rest.extensions.ExtensionList;
import com.percussion.rest.extensions.IExtensionAdaptor;
import com.percussion.util.PSSiteManageBean;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@PSSiteManageBean(value="restContentListResource")
@Path("/contentlists")
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@Api(value = "/contentlists", description = "Content List operations")
public class ContentListsResource {

    private IContentListsAdaptor adaptor;
    private IExtensionAdaptor extensionAdaptor;

    @Context
    private UriInfo uriInfo;

    public ContentListsResource(){}

    @GET
    @Path("/{id}")
    @Produces(
            {MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Get a ContentList by id", notes = "Will return a ContentList if it exists."
            , response = ContentList.class, responseContainer = "List")
    public ContentList getContentListById(@PathParam("id") @ApiParam(name = "id", value = "The id of the Content List to lookup.") long id) {
        return adaptor.getContentListById(id);
    }

    /***
     * Get a list of available ContentListGenerators on the system.
     * @return A list of ContentListGenerators
     */
    @GET
    @Path("/generators")
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Get a list of available Content List Generators", notes = "Will return a list of all Content List Generators registerd on the system"
            , response = Extension.class, responseContainer = "List")
    public List<Extension> getContentListGenerators() {
        ExtensionFilterOptions filter = new ExtensionFilterOptions();

        filter.setInterfacePattern("com.percussion.services.publisher.IPSContentListGenerator");
        return new ExtensionList(extensionAdaptor.getExtensions(uriInfo.getBaseUri(), filter));
    }

    /***
     * Get a list of available TemplateExpanders on the system.
     * @return
     */
    @GET
    @Path("/expanders")
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Get a list of available Template Expanders", notes = "Will return a list of all Content List Template Expanders registered on the system"
            , response = Extension.class, responseContainer = "List")
    public List<Extension> getTemplateExpanders() {
        ExtensionFilterOptions filter = new ExtensionFilterOptions();
        filter.setInterfacePattern("com.percussion.services.publisher.IPSTemplateExpander");
        return new ExtensionList(extensionAdaptor.getExtensions(uriInfo.getBaseUri(), filter));
    }

    /***
     * Get  a list of content lists for the specified edition.
     * @param editionId
     * @return
     */
    @GET
    @Path("/by-edition/{id}")
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Get a list of Content Lists defined for the specified Edition", notes = "Will return a list of all Content Lists linked to the specified Edition"
            , response = ContentList.class, responseContainer = "List")
    public List<ContentList> getContentListsByEditionId(@PathParam(value = "id") @ApiParam(name = "id", value = "The id of the Edition to retrieve content lists for.") long editionId) {
        return new ContentListList(adaptor.getContentListsByEditionId(editionId));
    }

    /***
     * Get a list of content lists that are currently unused.
     * @return A list of unused ContentLists
     */
    @GET
    @Path("/unused/{id}")
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Get a list of Content Lists defined for the specified Edition", notes = "Will return a list of all Content Lists linked to the specified Edition"
            , response = ContentList.class, responseContainer = "List")
    public List<ContentList> getUnusedContentLists(@PathParam(value = "id") @ApiParam(name = "id", value = "A valid Site ID") long siteId) {
        return new ContentListList(adaptor.getUnusedContentLists(siteId));
    }

    /***
     * Create or update a ContentList
     * @param cl
     * @return The updated content list
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/")
    @ApiOperation(value = "Create or Update the specified ContentList", notes = "Will return the updated ContentList", response = ContentList.class)
    public ContentList createOrUpdateContentList(@ApiParam(allowEmptyValue = false, readOnly = false, name = "body") ContentList cl) {
        return adaptor.createOrUpdateContentList(cl);
    }

    /***
     * Delete the specified content list.
     * @param id
     */
    @DELETE
    @Path("/{id}")
    @ApiOperation(value = "Deletes the ContentList with the specified id")
    public void deleteContentList(@PathParam(value = "id") @ApiParam(name = "id", required = true, value = "The id of the ContentList to delete.  Must exist.") long id) {
        adaptor.deleteContentList(id);
    }


}
