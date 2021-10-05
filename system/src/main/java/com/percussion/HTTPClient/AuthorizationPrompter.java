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
