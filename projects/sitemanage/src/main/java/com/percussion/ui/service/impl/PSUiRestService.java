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
package com.percussion.ui.service.impl;

import com.percussion.ui.data.PSSimpleDisplayFormat;
import com.percussion.ui.service.IPSUiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * @author erikserating
 *
 */
@Path("/uicomps")
@Component("uiRestService")
@Lazy
public class PSUiRestService
{
    
    private IPSUiService service;
    @Autowired
    public PSUiRestService(IPSUiService service)
    {
        this.service = service;
    }
    
    @GET
    @Path("/displayformat/id/{id}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSSimpleDisplayFormat getDisplayFormat(@PathParam("id") int id)
    {
        return service.getDisplayFormat(id);
    }

    @GET
    @Path("/displayformat/name/{name:.*}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSSimpleDisplayFormat getDisplayFormatByName(@PathParam("name") String name)
    {
        return service.getDisplayFormatByName(name);
    }
}
