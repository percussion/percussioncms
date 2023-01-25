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
import com.percussion.dashboardmanagement.service.IPSGadgetUserService;
import com.percussion.dashboardmanagement.service.IPSGadgetUserService.PSGadgetNotFoundException;
import com.percussion.dashboardmanagement.service.IPSGadgetUserService.PSGadgetServiceException;
import com.percussion.error.PSExceptionUtils;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.share.service.exception.PSDataServiceException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/gadgetuser")
@Component("gadgetUserRestService")
public class PSGadgetUserRestService {

        private static final Logger log = LogManager.getLogger(PSGadgetUserRestService.class);
        private IPSGadgetUserService gadgetUserService;
        @Autowired
        public PSGadgetUserRestService(IPSGadgetUserService gadgetUserService)
        {
            this.gadgetUserService = gadgetUserService;
        }
        
        @GET
        @Path("/{id}")
        @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
        public PSGadget load(String id){
                try {
                        return gadgetUserService.load(id);
                } catch (PSDataServiceException e) {
                        log.error(PSExceptionUtils.getMessageForLog(e));
                        log.debug(PSExceptionUtils.getDebugMessageForLog(e));
                        throw new WebApplicationException(e);
                }
        }
        
        @GET
        @Path("/{username}")
        @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
        public List<PSGadget> findAll(String username)throws PSGadgetNotFoundException, PSGadgetServiceException {
            return new PSGadgetList(gadgetUserService.findAll(username));
        }
        
        @GET
        @Path("/")
        @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
        public List<PSGadget> findAll() {
                try {
                        return new PSGadgetList(gadgetUserService.findAll());
                } catch (PSDataServiceException e) {
                        log.error(PSExceptionUtils.getMessageForLog(e));
                        log.debug(PSExceptionUtils.getDebugMessageForLog(e));
                        throw new WebApplicationException(e);
                }
        }
        
        public PSGadget find(String username) throws PSGadgetNotFoundException, PSGadgetServiceException, PSDataServiceException {
            return gadgetUserService.find(username);
        }
        
        @POST
        @Path("/{username}")
        @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
        @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
        public PSGadget save(String username, PSGadget gadget) {
            return gadgetUserService.save(username,gadget);
        }
        
        @DELETE
        @Path("/{username}/{id}")
        public void delete(String username, String id) {
            gadgetUserService.delete(username,id);
        }
        
        @DELETE
        @Path("/{id}")
        public void delete(String id) {
        try {
                gadgetUserService.delete(id);
        } catch (PSDataServiceException | PSNotFoundException e) {
                log.error(PSExceptionUtils.getMessageForLog(e));
                log.debug(PSExceptionUtils.getDebugMessageForLog(e));
                throw new WebApplicationException(e);
        }
        }
        
}
