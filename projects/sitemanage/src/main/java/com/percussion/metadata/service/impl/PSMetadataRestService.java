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
package com.percussion.metadata.service.impl;

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
                    data.getData(), e.getMessage());
            log.debug(e.getMessage(),e);
            throw new WebApplicationException(e);
        }
    }
   
   private final IPSMetadataService service;
   
   /**
    * Logger for this service.
    */
   private static final Logger log = LogManager.getLogger(PSMetadataRestService.class);
}
