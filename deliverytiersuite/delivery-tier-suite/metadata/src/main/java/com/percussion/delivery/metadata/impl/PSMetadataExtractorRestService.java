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
package com.percussion.delivery.metadata.impl;

import com.percussion.delivery.metadata.IPSMetadataIndexerService;
import com.percussion.delivery.metadata.IPSMetadataProperty;
import com.percussion.delivery.metadata.IPSMetadataProperty.VALUETYPE;
import com.percussion.delivery.metadata.extractor.data.PSMetadataEntry;
import com.percussion.delivery.metadata.extractor.data.PSMetadataProperty;
import com.percussion.error.PSExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collections;

/**
 * REST/Webservice layer for metadata extractor services.
 * 
 * @author miltonpividori
 */
@Path("/indexer")
@Component
public class PSMetadataExtractorRestService
{
    public static final Logger log = LogManager.getLogger(PSMetadataExtractorRestService.class);

    private final PSPropertyDatatypeMappings datatypeMappings;
    private IPSMetadataIndexerService indexer;

    @HEAD
    @Path("/csrf")
    public void csrf(@Context HttpServletRequest request, @Context HttpServletResponse response)  {
        Cookie[] cookies = request.getCookies();
        if(cookies == null){
            return;
        }
        for(Cookie cookie: cookies){
            if("XSRF-TOKEN".equals(cookie.getName())){
                response.setHeader("X-CSRF-HEADER", "X-XSRF-TOKEN");
                response.setHeader("X-CSRF-TOKEN", cookie.getValue());
            }
        }
    }

    @Inject
    @Autowired
    public PSMetadataExtractorRestService(IPSMetadataIndexerService indexer, PSPropertyDatatypeMappings datatypeMappings)
    {
        this.indexer = indexer;
        this.datatypeMappings = datatypeMappings;
    }
    
    @Path("/entry/{pagePath:.*}")
    @POST
    @RolesAllowed("deliverymanager")
    @Consumes({MediaType.APPLICATION_JSON,MediaType.TEXT_PLAIN})
    public void index(@Context HttpHeaders headers,
            @PathParam("pagePath") String path,
            PSMetadataEntry entry)
    {
        log.debug("Indexing file: {}", path);
        try {
            String contentType = headers.getMediaType().toString();
            log.debug("Content type: {}" , contentType);

            if (entry != null){
                // Get property value type

                for (IPSMetadataProperty ipsMetadataProperty : entry.getProperties()) {
                    PSMetadataProperty prop = (PSMetadataProperty) ipsMetadataProperty;

                    // Namespace prefix is not being used in datatype lookup.

                    String testval = prop.getName();
                    int indx = testval.indexOf(':');
                    if (indx > 0)
                        testval = testval.substring(indx + 1);

                    VALUETYPE propertyValueType = datatypeMappings.getDatatype(testval);
                    prop.setValuetype(propertyValueType);
                }
                indexer.save(entry);
            }
              
            else
                log.debug("File has no metadata (no link tags)");
        }
        catch (Exception e)
        {
            log.error("An error when saving index {}",PSExceptionUtils.getMessageForLog(e));
            throw new WebApplicationException(e, Response.serverError().build());
        }
       
    }
    
    @Path("/entry/{pagePath:.*}")
    @DELETE
    @RolesAllowed("deliverymanager")
    public void delete(@PathParam("pagePath") String path)
    {
        log.debug("Deleting file: {}" , path);
        
        try
        {
            indexer.delete(Collections.singleton(path));
            // TODO: implement blogPostService.delete here
        }
        catch (Exception e)
        {
            log.error("An error when deleting the file {}", PSExceptionUtils.getMessageForLog(e));
            throw new WebApplicationException(e, Response.serverError().build());
        }
    }
}
