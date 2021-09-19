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

package com.percussion.test;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class HTTPBasicAuthFilter implements ClientRequestFilter {

        private final String user;
        private final String password;

        public HTTPBasicAuthFilter(String user, String password) {
            this.user = user;
            this.password = password;
        }

        public void filter(ClientRequestContext requestContext) throws IOException {
            MultivaluedMap<String, Object> headers = requestContext.getHeaders();
            final String basicAuthentication = getBasicAuthentication();
            headers.add("Authorization", basicAuthentication);

        }

        private String getBasicAuthentication() {
            String token = this.user + ":" + this.password;
                return "BASIC " + DatatypeConverter.printBase64Binary(token.getBytes(StandardCharsets.UTF_8));
        }
    }
