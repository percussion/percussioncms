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

package com.percussion.sitemanage.service.impl;

import com.percussion.error.PSExceptionUtils;
import com.percussion.share.service.IPSDataService;
import com.percussion.share.service.IPSDataService.DataServiceLoadException;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.sitemanage.data.PSSiteArchitecture;
import com.percussion.sitemanage.service.IPSSiteArchitectureDataService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

@Path("/siteArchitecture")
@Component("siteArchitectureDataRestService")
@Lazy
public class PSSiteArchitectureDataRestService
{
    private final IPSSiteArchitectureDataService ds;

    @Autowired
    public PSSiteArchitectureDataRestService(IPSSiteArchitectureDataService ds)
    {
        this.ds = ds;
    }
    
    @GET
    @Path("/{id}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSSiteArchitecture find(@PathParam("id")
    String id)
    {
        try {
            return ds.find(id);
        } catch (DataServiceLoadException | IPSDataService.DataServiceNotFoundException | PSValidationException e) {
            log.error(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            throw new WebApplicationException();
        }
    }

    private static final Logger log = LogManager.getLogger(PSSiteArchitectureDataRestService.class);
}
