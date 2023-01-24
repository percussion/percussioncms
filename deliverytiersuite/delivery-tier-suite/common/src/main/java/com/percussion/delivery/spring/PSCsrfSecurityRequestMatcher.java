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

package com.percussion.delivery.spring;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.regex.Pattern;

@Component
public class PSCsrfSecurityRequestMatcher implements RequestMatcher {

    private static final Logger log = LogManager.getLogger(PSCsrfSecurityRequestMatcher.class);

    private Pattern allowedMethods = Pattern.compile("^(GET|HEAD|TRACE|OPTIONS)$");
    private String[] ignoredPaths;
    private boolean caseInsensitive=false;


    /**
     *
     * @param allowedMethodsPattern Regular expression listing excluded methods
     * @param unprotectedPaths comma seperated list of paths to ignore
     * @param caseInsensitive use case-insensitive comparison
     */
    public PSCsrfSecurityRequestMatcher(String allowedMethodsPattern, String unprotectedPaths, boolean caseInsensitive){
        this.allowedMethods = Pattern.compile(allowedMethodsPattern);

        this.caseInsensitive = caseInsensitive;
        if(caseInsensitive)
            unprotectedPaths = unprotectedPaths.toLowerCase();

        this.ignoredPaths = unprotectedPaths.split(",");

        log.debug("Initializing CSRF request matcher, Allowed Methods: {}, Ignored Paths: {}", allowedMethodsPattern, unprotectedPaths);
    }

    @Override
    public boolean matches(HttpServletRequest request) {
        if(allowedMethods.matcher(request.getMethod()).matches()){
            log.debug("Skipping CSRF for request method: {}",request.getMethod() );
            return false;
        }

        String uri = request.getRequestURI();
        if(caseInsensitive)
            uri = uri.toLowerCase();

        for(String p : this.ignoredPaths){
            if(caseInsensitive)
                p = p.toLowerCase();
            if(uri.contains(p)) {
                log.debug("Skipping CSRF for request URI: {}", request.getRequestURI());
                return false;
            }
        }

        log.debug("Request not filtered, requiring CSRF for request: {}", request);
        return true;
    }


}
