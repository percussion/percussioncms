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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.delivery.utils.security;

import com.percussion.utils.security.ToDoVulnerability;

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
