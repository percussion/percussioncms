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
