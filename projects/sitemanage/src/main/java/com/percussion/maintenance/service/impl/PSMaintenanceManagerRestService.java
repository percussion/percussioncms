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

