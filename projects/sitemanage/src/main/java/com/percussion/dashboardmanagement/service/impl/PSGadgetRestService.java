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

import com.percussion.dashboardmanagement.data.PSGadget;
import com.percussion.dashboardmanagement.data.PSGadgetList;
import com.percussion.dashboardmanagement.service.IPSGadgetService;
import com.percussion.dashboardmanagement.service.IPSGadgetService.PSGadgetNotFoundException;
import com.percussion.dashboardmanagement.service.IPSGadgetService.PSGadgetServiceException;

import java.util.List;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Path("/gadget")
@Component("gadgetRestService")
public class PSGadgetRestService
{
    IPSGadgetService gadgetService;
    @Autowired
    public PSGadgetRestService(IPSGadgetService gadgetService)
    {
        this.gadgetService = gadgetService;
    }

    @POST
    @Path("/")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSGadget save(PSGadget gadget) throws PSGadgetServiceException
    {
        return gadgetService.save(gadget);
    }

    @GET
    @Path("/")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public List<PSGadget> findAll() throws PSGadgetNotFoundException, PSGadgetServiceException
    {
        return new PSGadgetList(gadgetService.findAll());
    }

    @GET
    @Path("/{id}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSGadget find(String id) throws PSGadgetNotFoundException, PSGadgetServiceException
    {
        return gadgetService.find(id);
    }

    @DELETE
    @Path("/{id}")
    public void delete(String id) throws PSGadgetNotFoundException, PSGadgetServiceException
    {
        gadgetService.delete(id);
    }

}
