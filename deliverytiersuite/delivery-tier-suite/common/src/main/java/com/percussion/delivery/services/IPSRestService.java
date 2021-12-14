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
package com.percussion.delivery.services;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Defines an interface that should be implemented by all public Rest services.
 * 
 * The interface defines common methods for things like retrieving the current version
 * of a service that are intended to be consistent between services. 
 * 
 * @author natechadwick
 *
 */
public interface IPSRestService {
	
	/***
	 * Returns the currently deployed version of the service. 
	 * 
	 * @return
	 */
	@GET
	@Path("/version")
	public String getVersion();

    /**
     * The purpose of this method is to fix behavior in the DTS db
     * after a site is renamed in CM1.  Starting this for the purpose
     * of deleting old DTS database information after a site is renamed in CM1.
     * Prior to fix all the old data after the rename is left behind.
     *
     * @param prevSiteName the old name for the site
     * @param newSiteName the new name for the site
     *
     * @see com.percussion.services.siterename.IPSSiteRenameService#deleteDTSEntries IPSSiteRenameService
     *
     * @return <code>204</code> if the process was successful.  Return error code otherwise.
     */
    @DELETE
    @Path("/updateOldSiteEntries/{prevSiteName}/{newSiteName}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("deliverymanager")
    public Response updateOldSiteEntries(@PathParam("prevSiteName") String prevSiteName, @PathParam("newSiteName") String newSiteName);

}
