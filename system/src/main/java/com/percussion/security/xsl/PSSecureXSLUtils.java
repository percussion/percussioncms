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

package com.percussion.security.xsl;

import com.percussion.server.PSRequest;
import org.owasp.csrfguard.CsrfGuard;
import org.owasp.csrfguard.session.LogicalSession;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 * An XSL extension class.  Provides security functions for
 * use in XSL style sheets.
 */
public class PSSecureXSLUtils {

    /**
     * Utility function to get the CSRF token header name.
     *
     * @return The configured name based on OwaspCSRFGuard.properties
     */
    public static String getCSRFTokenName(){
        return CsrfGuard.getInstance().getTokenName();
    }


    /**
     * Utility function to return the CSRF token value for the
     * current session.  Intended for use from an active application server
     * session not for use outside of a servlet context.
     *
     * @return The session's csrf token value or an empty string, never null.
     */
    public static String getCSRFTokenValue(){
        final CsrfGuard csrfGuard = CsrfGuard.getInstance();

        HttpServletRequest request = (HttpServletRequest) PSRequest.getContextForRequest().getServletRequest();

        final LogicalSession logicalSession = csrfGuard.getLogicalSessionExtractor().extract(request);

        return Objects.nonNull(logicalSession) ?
                csrfGuard.getTokenService().getTokenValue(logicalSession.getKey(),
                        request.getRequestURI()) : "";

    }
}
