/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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
package com.percussion.delivery.metadata.impl;

import com.percussion.delivery.metadata.IPSMetadataIndexerService;
import com.percussion.delivery.metadata.IPSMetadataProperty;
import com.percussion.delivery.metadata.IPSMetadataProperty.VALUETYPE;
import com.percussion.delivery.metadata.extractor.data.PSMetadataEntry;
import com.percussion.delivery.metadata.extractor.data.PSMetadataProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
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
        CsrfToken csrfToken = new HttpSessionCsrfTokenRepository().generateToken(request);

        response.setHeader("X-CSRF-HEADER", csrfToken.getHeaderName());
        response.setHeader("X-CSRF-PARAM", csrfToken.getParameterName());
        response.setHeader("X-CSRF-TOKEN", csrfToken.getToken());
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
            log.error("An error when saving index {}", e.getMessage());
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
            log.error("An error when deleting the file {}", e.getMessage());
            throw new WebApplicationException(e, Response.serverError().build());
        }
    }
}
