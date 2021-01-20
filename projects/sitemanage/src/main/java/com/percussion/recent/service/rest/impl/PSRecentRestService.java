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

package com.percussion.recent.service.rest.impl;

import com.percussion.pagemanagement.data.PSTemplateSummary;
import com.percussion.pagemanagement.data.PSTemplateSummaryList;
import com.percussion.pagemanagement.data.PSWidgetContentType;
import com.percussion.pagemanagement.data.PSWidgetContentTypeList;
import com.percussion.pathmanagement.data.PSPathItem;
import com.percussion.pathmanagement.data.PSPathItemList;
import com.percussion.recent.service.rest.IPSRecentRestService;
import com.percussion.recent.service.rest.IPSRecentService;
import com.percussion.share.data.PSItemProperties;

import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.percussion.share.data.PSItemPropertiesList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Path("/recent")
@Transactional(propagation=Propagation.REQUIRED)
@Service("recentRestService")
public class PSRecentRestService implements IPSRecentRestService
{
    private IPSRecentService recentService;

    static Log ms_log = LogFactory.getLog(PSRecentRestService.class);

    @Autowired
    public PSRecentRestService(IPSRecentService recentService)
    {
        this.recentService = recentService;
    }

    @GET
    @Produces(
    {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Path("/item")
    public List<PSItemProperties> findRecentItem()
    {
        return new PSItemPropertiesList(recentService.findRecentItem(false));
    }

    @GET
    @Produces(
            {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Path("/non-archived-item")
    public List<PSItemProperties> findRecentNonArchivedItem()
    {
        return new PSItemPropertiesList(recentService.findRecentItem(true));
    }

    @GET
    @Produces(
    {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Path("/template/{siteName}")
    public List<PSTemplateSummary> findRecentTemplate(@PathParam("siteName")
    String siteName)
    {
        return new PSTemplateSummaryList(recentService.findRecentTemplate(siteName));
    }

    @GET
    @Produces(
    {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Path("/site-folder/{siteName}")
    public List<PSPathItem> findRecentSiteFolder(@PathParam("siteName")
    String siteName)
    {
        return new PSPathItemList(recentService.findRecentSiteFolder(siteName));
    }

    @GET
    @Produces(
    {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Path("/asset-folder")
    public List<PSPathItem> findRecentAssetFolder()
    {
        return new PSPathItemList(recentService.findRecentAssetFolder());
    }

    @GET
    @Produces(
    {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Path("/asset-type")
    public List<PSWidgetContentType> findRecentAssetType()
    {
        return new PSWidgetContentTypeList(recentService.findRecentAssetType());
    }

    @POST
    @Path("/item")
    public void addRecentItem(@FormParam("value")
    String value)
    {
        recentService.addRecentItem(value);
    }

    @POST
    @Path("/template/{siteName}")
    public void addRecentTemplate(@PathParam("siteName")
    String siteName, @FormParam("value")
    String value)
    {
        recentService.addRecentTemplate(siteName, value);
    }

    @POST
    @Path("/site-folder")
    public void addRecentSiteFolder(@FormParam("value")
    String value)
    {
        recentService.addRecentSiteFolder(value);
    }

    @POST
    @Path("/asset-folder")
    public void addRecentAssetFolder(@FormParam("value")
    String value)
    {
        recentService.addRecentAssetFolder(value);
    }

    @POST
    @Path("/item-by-user/{userName}")
    public void addRecentItemByUser(@PathParam("userName")String userName, @FormParam("value")
    String value)
    {
        recentService.addRecentItemByUser(userName,value);
    }

    @POST
    @Path("/template-by-user/{userName}/{siteName}")
    public void addRecentTemplateByUser(@PathParam("userName")String userName,@PathParam("siteName")
    String siteName, @FormParam("value")
    String value)
    {
        recentService.addRecentTemplateByUser(userName, siteName, value);
    }

    @POST
    @Path("/site-folder-by-user/{userName}")
    public void addRecentSiteFolderByUser(@PathParam("userName")String userName,@FormParam("value")
    String value)
    {
        recentService.addRecentSiteFolderByUser(userName,value);
    }

    @POST
    @Path("/asset-folder-by-user/{userName}")
    public void addRecentAssetFolderByUser(@PathParam("userName")String userName, @FormParam("value")
    String value)
    {
        recentService.addRecentAssetFolderByUser(userName,value);
    }
    
    @DELETE
    @Path("/user/{user}")
    public void deleteUserRecent(@PathParam("user") String user)
    {
        recentService.addRecentAssetFolder(user);
    }

    @DELETE
    @Path("/site/{siteName}")
    public void deleteSiteRecent(@PathParam("siteName") String siteName)
    {
        recentService.deleteSiteRecent(siteName);
    }
    
    @POST
    @Path("/asset-type")
    public void addRecentAssetType(@FormParam("value")
    String value)
    {
        recentService.addRecentAssetType(value);
    }
}
