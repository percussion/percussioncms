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
