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
import java.util.List;
import java.util.stream.Stream;

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

        boolean allowedURLParam = false;
        allowedURLParam = isPathAllowed(requestContext);
        if(!auth || !allowedURLParam){
            URI uri=null;
            try{
                uri = new URI("../ui/default-error.html");
            } catch (URISyntaxException e) {
                log.error("Unable to set the default error URI: {}",
                        PSExceptionUtils.getMessageForLog(e));
                log.debug(PSExceptionUtils.getDebugMessageForLog(e));
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

    private boolean isPathAllowed(ContainerRequestContext requestContext) {
        if(requestContext.getUriInfo().getPath(true).equalsIgnoreCase("api-docs")) {
            List<String> urls = requestContext.getUriInfo().getQueryParameters().get("url");
            String url = null;


            if (urls != null) {
                url = urls.get(0);
            }

            if (url != null) {
                if (Stream.of(allowedPaths).anyMatch(url::equalsIgnoreCase)) {
                    return true;
                } else {
                    return false;
                }
            }
            return false;
        }else{
            return true;
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

    private static String[] allowedPaths = {
            "/rest/openapi.json"
    };
}
