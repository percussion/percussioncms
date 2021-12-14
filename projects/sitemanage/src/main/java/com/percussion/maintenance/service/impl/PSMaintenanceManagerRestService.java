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
package com.percussion.maintenance.service.impl;

import com.percussion.maintenance.service.IPSMaintenanceManager;
import com.percussion.user.service.IPSUserService;
import com.percussion.utils.request.PSRequestInfo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 * @author JaySeletz
 *
 */
@Path("/manager")
@Component("maintenanceManagerRestService")
@Lazy
public class PSMaintenanceManagerRestService
{
    private IPSMaintenanceManager maintMgr;
    IPSUserService userService;

    @Autowired
    public PSMaintenanceManagerRestService(IPSMaintenanceManager maintMgr, IPSUserService userService)
    {
        this.maintMgr = maintMgr;
        this.userService = userService;
    }
    
    /**
     * Get the maintenance status of the server
     * 
     * @return A response with code 200 (OK) to indicate the server is ready to handle requests, and 409 (Conflict)
     * to indicate there are maintenance processes running and the server is not ready.
     */
    @GET
    @Path("/status/server")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getServerStatus()
    {
        Status status;
        if (maintMgr.isWorkInProgress()) {
            status = Status.CONFLICT;
        }
        else {
            status = Status.OK;
        }
        
        return Response.status(status).build();
    }
    
    /**
     * Get the status of the maintenance manager
     * 
     * @param clearErrors If <code>true</code>, will clear any errors.  Note that you must have Admin access to the server to perform this.
     * If not, returns Forbidden (403)
     * 
     * 
     * @return A response with code 200 (OK) to indicate no maintenance processes have failed, and 409 (Conflict)
     * to indicate there are maintenance processes that have failed.
     */
    @GET
    @Path("/status/process")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getMaintStatus(@QueryParam("clearErrors") boolean clearErrors)
    {
        Status status;
        if (maintMgr.hasFailures())
        {
            status = Status.CONFLICT;
            if (clearErrors)
            {
                String userName = (String) PSRequestInfo
                        .getRequestInfo(PSRequestInfo.KEY_USER);
                if (StringUtils.isBlank(userName) || !userService.isAdminUser(userName))
                {
                    return Response.status(Status.FORBIDDEN).build();
                }
                
                maintMgr.clearFailures();
            }
        }
        else {
            status = Status.OK;
        }
        
        return Response.status(status).build();
    }

}

