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
