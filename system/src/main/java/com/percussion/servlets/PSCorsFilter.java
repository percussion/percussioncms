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

import com.percussion.server.PSServer;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.util.Arrays;

@Component(value = "corsFilter")
public class PSCorsFilter implements Filter {


    private CorsFilter filter;

    /**
     * Called by the web container to indicate to a filter that it is
     * being placed into service.
     *
     * <p>The servlet container calls the init
     * method exactly once after instantiating the filter. The init
     * method must complete successfully before the filter is asked to do any
     * filtering work.
     *
     * <p>The web container cannot place the filter into service if the init
     * method either
     * <ol>
     * <li>Throws a ServletException
     * <li>Does not return within a time period defined by the web container
     * </ol>
     *
     * @param filterConfig
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        init();
    }

    private void init(){

        CorsConfiguration config = new CorsConfiguration();

        String  allowed =  PSServer.getProperty("allowedOrigins","*").trim();
        if(!StringUtils.isEmpty(allowed))
            allowed = allowed.trim();

        if(!"*".equalsIgnoreCase(allowed)) {
            config.setAllowedOrigins(Arrays.asList(allowed.split(",", -1)));
        }
        String allowedMethods = PSServer.getProperty("allowedMethods","GET,POST,OPTIONS,HEAD,PUT,DELETE,PATCH").trim();
        if(!StringUtils.isEmpty(allowedMethods)) {
            allowedMethods = allowedMethods.trim();
            for (String s : allowedMethods.split(",", -1)) {
                config.addAllowedMethod(s.trim());
            }
        }

        String allowedHeaders = PSServer.getProperty("allowedHeaders","Content-Type, Access-Control-Allow-Origin, Access-Control-Allow-Headers, Authorization, X-Requested-With, X-UA-Compatible, OWASP-CSRFTOKEN,User-Agent").trim();
        if(!StringUtils.isEmpty(allowedHeaders)) {
            allowedHeaders = allowedHeaders.trim();

            for (String s : allowedHeaders.split(",", -1)) {
                config.addAllowedHeader(s.trim());
            }
        }

        String allowCredentials = PSServer.getProperty("allowCredentials","false").trim();
        if(!StringUtils.isEmpty(allowCredentials)){
            config.setAllowCredentials(Boolean.parseBoolean(allowCredentials));
        }

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        filter = new CorsFilter(source);
    }

    /**
     * The <code>doFilter</code> method of the Filter is called by the
     * container each time a request/response pair is passed through the
     * chain due to a client request for a resource at the end of the chain.
     * The FilterChain passed in to this method allows the Filter to pass
     * on the request and response to the next entity in the chain.
     *
     * <p>A typical implementation of this method would follow the following
     * pattern:
     * <ol>
     * <li>Examine the request
     * <li>Optionally wrap the request object with a custom implementation to
     * filter content or headers for input filtering
     * <li>Optionally wrap the response object with a custom implementation to
     * filter content or headers for output filtering
     * <li>
     * <ul>
     * <li><strong>Either</strong> invoke the next entity in the chain
     * using the FilterChain object
     * (<code>chain.doFilter()</code>),
     * <li><strong>or</strong> not pass on the request/response pair to
     * the next entity in the filter chain to
     * block the request processing
     * </ul>
     * <li>Directly set headers on the response after invocation of the
     * next entity in the filter chain.
     * </ol>
     *
     * @param request
     * @param response
     * @param chain
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if(filter == null)
            init();

        filter.doFilter(request,response,chain);
    }

    /**
     * Called by the web container to indicate to a filter that it is being
     * taken out of service.
     *
     * <p>This method is only called once all threads within the filter's
     * doFilter method have exited or after a timeout period has passed.
     * After the web container calls this method, it will not call the
     * doFilter method again on this instance of the filter.
     *
     * <p>This method gives the filter an opportunity to clean up any
     * resources that are being held (for example, memory, file handles,
     * threads) and make sure that any persistent state is synchronized
     * with the filter's current state in memory.
     */
    @Override
    public void destroy() {
        if(filter!=null)
            filter.destroy();
    }
}
