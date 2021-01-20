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
package com.percussion.delivery.caching.impl;

import com.percussion.delivery.caching.IPSCacheManager;
import com.percussion.delivery.caching.data.PSCacheConfig;
import com.percussion.delivery.caching.data.PSInvalidateRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * REST layer used to access the cache manager services.
 * 
 * @author miltonpividori
 *
 */
@Path("/manager")
@Component
@Scope("singleton")
public class PSCacheRestService
{

    @Autowired
    private IPSCacheManager cacheManager;
    private final static Logger log = LogManager.getLogger(PSCacheRestService.class);
    
    @Autowired
    public PSCacheRestService(IPSCacheManager cacheManager)
    {
        this.cacheManager = cacheManager;
    }
    
    @Path("/invalidate")
    @RolesAllowed("deliverymanager")
    @POST
    public void invalidate(PSInvalidateRequest request)
    {
        try
        {
            cacheManager.invalidate(request);
        }
        catch (Exception ex)
        {
       		log.error("Could not invalidate the cache manager : " + ex.getLocalizedMessage());
        	
            throw new WebApplicationException(ex, Response.serverError().build());
        }
    }
    
    @Path("/config")
    @RolesAllowed("deliverymanager")
    @GET
    public PSCacheConfig getCacheConfig()
    {
        try
        {
            return cacheManager.getConfig();
        }
        catch (Exception ex)
        {
       		log.error("Could not get cache config : " + ex.getLocalizedMessage());
        	
            throw new WebApplicationException(ex, Response.serverError().build());
        }
    }
}
