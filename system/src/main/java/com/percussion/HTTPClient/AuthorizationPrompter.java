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

package com.percussion.HTTPClient;

/**
 * This is the interface that a username/password prompter must implement. The
 * {@link HTTPClient.DefaultAuthHandler DefaultAuthHandler} invokes an instance
 * of this each time it needs a username and password to satisfy an
 * authorization challenge (for which it doesn't already have the necessary
 * info).
 *
 * This can be used to implement a different UI from the default popup box
 * for soliciting usernames and passwords, or for using an altogether
 * different way of getting the necessary auth info.
 *
 * @see DefaultAuthHandler#setAuthorizationPrompter(HTTPClient.AuthorizationPrompter)
 * @version	0.3-3  06/05/2001
 * @author	Ronald Tschalär
 * @since	V0.3-3
 */
@Deprecated
public interface AuthorizationPrompter
{
    /**
     * This method is invoked whenever a username and password is required
     * for an authentication challenge to proceed.
     *
     * @param challenge the parsed challenge from the server; the host,
     *                  port, scheme, realm and params are set to the
     *                  values given by the server in the challenge.
     * @param forProxy  true if the info is for a proxy (i.e. this is part of
     *                  handling a 407 response); false otherwise (i.e. the
     *                  response code was 401).
     * @return an NVPair containing the username and password in the name
     *         and value fields, respectively, or null if the authorization
     *         challenge handling is to be aborted (e.g. when the user
     *         hits the <var>Cancel</var> button).
     */
    NVPair getUsernamePassword(AuthorizationInfo challenge, boolean forProxy);
}
