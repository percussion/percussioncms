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
package com.percussion.delivery.utils.security;

import com.percussion.security.ToDoVulnerability;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

/**
 * Common helper class to return a HTTP client.
 * 
 * @author leonardohildt
 * 
 */
@ToDoVulnerability
@Deprecated
public class PSHttpClient
{
    public PSHttpClient()
    {
    }

    /**
     * Creates and returns an SSL enabled client.
     * 
     * @return the client, never <code>null</code>.
     * @throws Exception if any error occurs.
     */
    public Client getSSLClient() throws Exception
    {

        SSLContext ctx = SSLContext.getInstance("TLS");

        ctx.init(null, new TrustManager[]
        {new PSSimpleTrustManager(null)}, null);

        /* TODO: This looks like a security issue - isn't this whitelisting all ssl certificates?
        Seems so.  This is used in PSFeedsService and PSMembershipService. I am assuming this is to
        handle services calling each other with a self signed certificate.  Needs to be refactored.
         */
        Client client = ClientBuilder.newBuilder()
                .sslContext(ctx)
                .hostnameVerifier(new HostnameVerifier(){
                    @Override
                    public boolean verify(String s, SSLSession sslSession)
                    {
                        return true;
                    }
                })
                .build();

        return client;
    }

}
