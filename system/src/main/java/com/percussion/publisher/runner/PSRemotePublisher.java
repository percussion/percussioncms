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

import com.percussion.error.PSExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

/**
 * This class provides a means of initiating publication of a specified
 * edition. This is basically done by sending an HTTP request with all
 * parameters required to start publishing an edition.
 * @deprecated Use REST api's instead.
 */
@Deprecated
public class PSRemotePublisher
{

    private static final Logger log = LogManager.getLogger(PSRemotePublisher.class);

    /**
     * This method is the one which does actual publishing. It initiates
     * publication of an edition by making an HTTP request to the Rhythmyx
     * Publisher Manager on the server.
     *
     * @param server - http(s)://server:port/ name or IPAddress of the  server, must not be
     * <code>null</code> or <code>empty</code>.
     *
     * @param editionid - EditionID to publish (as String) of the
     * server, must not be <code>null</code> or <code>empty</code>.
     *
     * @param userid - UserId to access the  CMS, may not be
     * <code>null</code> or <code>empty</code>.
     *
     * @param password - password to access the  CMS, may not be
     * <code>null</code> or <code>empty</code>.
     *
     * @return output (as String) resulting from the HTTP request to the
     * Publisher Manager on the server.
     *
     * @throws IllegalArgumentException if invalid server or edition id is
     * specified.
     *
     */
    private static String publish(String server, String editionid,
                                String userid, String password) throws PSPublisherRunnerException {
        Client client = ClientBuilder.newBuilder().newClient().register(new PSBasicAuthenticator(userid,password));

        WebTarget target = client.target(server);
        target = target.path( "/rest/editions/" + editionid + "/publish");
        Invocation.Builder builder = target.request();
        Response response = builder.post(null);

        switch(response.getStatus()){
            case 403:
                throw new PSPublisherRunnerException("403: Permission Denied");
            case 404:
                throw new PSPublisherRunnerException("404: Edition not found");
            case 500:
                throw new PSPublisherRunnerException("500: Unexpected server error");
            case 200:
            default:
        }

        return String.valueOf(response.getStatus());
    }

    /**
     * The main method
     */
    public static void main(String[] args)
    {
        if(args.length < 4)
        {
            System.err.println("Usage: java PSRemotePublisher <http(s)://server>:<port>/ " +
                    "<editionid> [cmsuserid] [password]");
            System.err.println();
            System.err.println("where");
            System.err.println("  server is url of the" +
                    "CMS server to initiate publish from e.g. https://127.0.0.1:8443/");
            System.err.println("  editionid  is the numeric id of the edition " +
                    "to be published");
            System.err.println("  cmsuserid  is the UserID to access " +
                    "CMS applications, optional");
            System.err.println("  password   is the Password to access "
                    + "CMS applications");
            System.exit(1);
        }

        String server = args[0];
        String editionid = args[1];
        String userid = args[2];
        String password = args[3];

        try
        {
            System.out.println(
                    publish(server, editionid, userid, password));
        }
        catch(Exception e)
        {
            log.error(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
        }

    }
}
