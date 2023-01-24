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
package com.percussion.metadata.service.impl;

import com.percussion.error.PSExceptionUtils;
import com.percussion.metadata.data.PSMetadata;
import com.percussion.metadata.data.PSMetadataList;
import com.percussion.metadata.service.IPSMetadataService;
import com.percussion.server.PSServer;
import com.percussion.share.dao.IPSGenericDao;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author erikserating
 *
 */
@Path("/metadata")
@Component("metadataRestService")
@Lazy
public class PSMetadataRestService
{
   @Autowired
   public PSMetadataRestService(IPSMetadataService service)
   {
      this.service = service;
   }
   
   @GET
   @Path("/{key}")
   @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
   public PSMetadata find(@PathParam("key") String key)
   {
       try {
           return service.find(key);
       } catch (IPSGenericDao.LoadException e) {

           throw new WebApplicationException(e);
       }
   }
   
   @GET
   @Path("/byprefix/{prefix}")
   @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
   public List<PSMetadata> findByPrefix(@PathParam("prefix") String prefix)
   {
       try {
           return new PSMetadataList(service.findByPrefix(prefix));
       } catch (IPSGenericDao.LoadException e) {
           throw new WebApplicationException(e);
       }
   }
   
   @DELETE
   @Path("/{key}")
   public void delete(@PathParam("key") String key)
   {
       try {
           service.delete(key);
       } catch (IPSGenericDao.DeleteException | IPSGenericDao.LoadException e) {
           throw new WebApplicationException(e);
       }
   }
   
   @DELETE
   @Path("/byprefix/{prefix}")
   public void deleteByPrefix(@PathParam("prefix") String prefix)
   {
       try {
           service.deleteByPrefix(prefix);
       } catch (IPSGenericDao.DeleteException | IPSGenericDao.LoadException e) {
           throw new WebApplicationException(e);
       }
   }
   
   @POST
   @Path("/")
   @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
   public void save(PSMetadata data)
   {
       try {
           service.save(data);
       } catch (IPSGenericDao.SaveException | IPSGenericDao.LoadException e) {
           throw new WebApplicationException(e);
       }
   }
   
   @POST
   @Path("/globalvariables")
   @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public void saveGlobalVariables(PSMetadata data)
    {
        try
        {
            // First save the metadata using the metadata service
            service.save(data);

            String msg = "/**** This is a system generated content, any modifications will be overwritten by the next save of global variables. *****/\n";
            String msg1 = "var PercGlobalVariablesData = ";
            FileUtils.writeStringToFile(new File(PSServer.getRxDir().getAbsolutePath()
                            + "/web_resources/cm/common/js/PercGlobalVariablesData.js"),
                            msg + msg1 + data.getData() + ";", StandardCharsets.UTF_8);
        }
        catch (IOException | IPSGenericDao.LoadException | IPSGenericDao.SaveException e)
        {
            log.warn("Error saving the global variables: {} Error: {}",
                    data.getData(), PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            throw new WebApplicationException(e);
        }
    }
   
   private final IPSMetadataService service;
   
   /**
    * Logger for this service.
    */
   private static final Logger log = LogManager.getLogger(PSMetadataRestService.class);
}
