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
package com.percussion.dashboardmanagement.service.impl;

import com.percussion.dashboardmanagement.data.PSUserProfile;
import com.percussion.dashboardmanagement.service.IPSUserProfileService;
import com.percussion.dashboardmanagement.service.IPSUserProfileService.PSUserProfileNotFoundException;
import com.percussion.dashboardmanagement.service.IPSUserProfileService.PSUserProfileServiceException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Path("/userprofile")
@Component("userProfileRestService")
public class PSUserProfileRestService {

    IPSUserProfileService userProfileService;
    
    @Autowired
    public PSUserProfileRestService(IPSUserProfileService userProfileService)
    {
        this.userProfileService = userProfileService;
    }
    
    @POST
    @Path("/")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSUserProfile save(PSUserProfile userProfile) throws PSUserProfileServiceException {
        return userProfileService.save(userProfile);
    }

 
    
    @GET
    @Path("/user/{userName}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSUserProfile find(@PathParam("userName") String userName) throws PSUserProfileNotFoundException,
            PSUserProfileServiceException {
        return userProfileService.find(userName);
        
    }
    



}
