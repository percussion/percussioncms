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


    /**
     *
     * @param allowedMethodsPattern Regular expression listing excluded methods
     * @param unprotectedPaths comma seperated list of paths to ignore
     * @param caseInsensitive use case-insensitive comparison
     */
    public PSCsrfSecurityRequestMatcher(String allowedMethodsPattern, String unprotectedPaths, boolean caseInsensitive){
        this.allowedMethods = Pattern.compile(allowedMethodsPattern);


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
        for(String p : this.ignoredPaths){
            if(uri.contains(p)) {
                log.debug("Skipping CSRF for request URI: {}", request.getRequestURI());
                return false;
            }
        }

        log.debug("Request not filtered, requiring CSRF for request: {}", request);
        return true;
    }


}
