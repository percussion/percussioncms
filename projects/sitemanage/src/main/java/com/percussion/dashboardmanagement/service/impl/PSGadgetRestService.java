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
