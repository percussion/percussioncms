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

package com.percussion.publisher.runner;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/***
 * Provides a handler for basic authentication in the cxf client.
 */
@Provider
public class PSBasicAuthenticator implements ClientRequestFilter {

        private final String user;
        private final String password;

        public PSBasicAuthenticator(String user, String password) {
            this.user = user;
            this.password = password;
        }

        @Override
        public void filter(ClientRequestContext requestContext) throws IOException {
            requestContext.getHeaders().add(
                    "RX_USEBASICAUTH", true);

            requestContext.getHeaders().add(
                    HttpHeaders.AUTHORIZATION, getBasicAuthentication());

        }

        private String getBasicAuthentication() {
            byte[] userAndPasswordBytes;
            String userAndPassword = this.user + ":" + this.password;
            userAndPasswordBytes = userAndPassword.getBytes(StandardCharsets.UTF_8);
            return "Basic " + Base64.getEncoder().encodeToString(userAndPasswordBytes);
        }
}

