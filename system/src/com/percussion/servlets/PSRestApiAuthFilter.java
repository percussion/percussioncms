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

package com.percussion.servlets;

import com.percussion.error.PSExceptionUtils;
import com.percussion.services.utils.jspel.PSRoleUtilities;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

/**
 * Provides an authorization filter for the REST API / API Docs
 */
public class PSRestApiAuthFilter implements ContainerRequestFilter {

    private static final Logger log = LogManager.getLogger(PSRestApiAuthFilter.class);


    /**
     * Filter method called before a request has been dispatched to a resource.
     *
     * <p>
     * Filters in the filter chain are ordered according to their {@code javax.annotation.Priority}
     * class-level annotation value.
     * If a request filter produces a response by calling {@link ContainerRequestContext#abortWith}
     * method, the execution of the (either pre-match or post-match) request filter
     * chain is stopped and the response is passed to the corresponding response
     * filter chain (either pre-match or post-match). For example, a pre-match
     * caching filter may produce a response in this way, which would effectively
     * skip any post-match request filters as well as post-match response filters.
     * Note however that a responses produced in this manner would still be processed
     * by the pre-match response filter chain.
     * </p>
     *
     * @param requestContext request context.
     * @throws IOException if an I/O exception occurs.
     */
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        String fullrolestr = PSRoleUtilities.getUserRoles();
        boolean auth=false;
        if(fullrolestr != null){
            auth = fullrolestr.contains("Admin");
        }

        if(!auth){
            URI uri=null;
            try{
                uri = new URI("../ui/default-error.html");
            } catch (URISyntaxException e) {
                log.error("Unable to set the default error URI: {}",
                        PSExceptionUtils.getMessageForLog(e));
                log.debug(e);
            }
            log.debug("Not allowing API access for user with Roles: {}", fullrolestr);
            Response.ResponseBuilder st= Response.status(Response.Status.NOT_FOUND);
            if(uri!=null) {
                st.location(uri);
                st.contentLocation(uri);
                st.entity(get404Content(uri));
            }
            st.type(MediaType.TEXT_HTML_TYPE);

            requestContext.abortWith(st.build());
        }
    }

    String get404Content(URI uri){
        String ret = "";

        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("/default-error.html")){
            if(is!=null) {
               ret =  IOUtils.toString(is, StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            log.error(PSExceptionUtils.getMessageForLog(e));
        }
        return ret;
    }
}
