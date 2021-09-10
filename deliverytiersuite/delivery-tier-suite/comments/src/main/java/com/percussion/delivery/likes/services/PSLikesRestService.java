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
package com.percussion.delivery.likes.services;

import com.percussion.delivery.services.PSAbstractRestService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * REST/Webservice layer used to access the likes service.
 * 
 * @author davidpardini
 * 
 */
@Path("/likes")
@Component
public class PSLikesRestService extends PSAbstractRestService
{

    /**
     * The likes service reference. Initialized in the ctor. Never
     * <code>null</code>.
     */
    private IPSLikesService likesService;
    private static final Logger log = LogManager.getLogger(PSLikesRestService.class);

    @Inject
    @Autowired
    public PSLikesRestService(IPSLikesService service)
    {
        likesService = service;
    }

    /**
     * Tally of how many users have Liked a page, a comment.
     *
     * @return int, never <code>null</code> may be empty.
     */
    @POST
    @Path("/total/{site}/{type}/{likeId:.*}")
    @Produces("application/json")
    public int getTotalLikes(@PathParam("site") String site, @PathParam("type") String type,
            @PathParam("likeId") String likeId)
    {
        try
        {
            return likesService.getTotalLikes(site, likeId, type);
        }
        catch (Exception e)
        {
            throw new WebApplicationException(e, Response.serverError().build());
        }
    }

    /**
     * To Like a page, a comment.
     *
     * @return int, never <code>null</code> may be empty.
     */
    @POST
    @Path("/like/{site}/{type}/{likeId:.*}")
    @Produces("application/json")
    public int like(@PathParam("site") String site, @PathParam("type") String type, @PathParam("likeId") String likeId)
    {
        try
        {
            return likesService.like(site, likeId, type);
        }
        catch (Exception e)
        {
            throw new WebApplicationException(e, Response.serverError().build());
        }
    }

    
    /**
     * To UnLike a page, a comment.
     *
     * @return int, never <code>null</code> may be empty.
     */
    @POST
    @Path("/unlike/{site}/{type}/{likeId:.*}")
    @Produces("application/json")
    public int unlike(@PathParam("site") String site, @PathParam("type") String type, @PathParam("likeId") String likeId)
    {
        try
        {
            return likesService.unlike(site, likeId, type);
        }
        catch (Exception e)
        {
            throw new WebApplicationException(e, Response.serverError().build());
        }
    }

    @Override
    public Response updateOldSiteEntries(String prevSiteName, String newSiteName) {
        log.info("Attempting to update likes service with site name: {}" , prevSiteName);
        likesService.updateLikesForSiteAfterRename(prevSiteName, newSiteName);
        return Response.status(Response.Status.NO_CONTENT).build();
    }

}
