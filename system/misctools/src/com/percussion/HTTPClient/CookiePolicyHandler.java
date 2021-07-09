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
 * This is the interface that a cookie policy handler must implement. A
 * policy handler allows you to control which cookies are accepted and
 * which are sent.
 *
 * @see HTTPClient.CookieModule#setCookiePolicyHandler(HTTPClient.CookiePolicyHandler)
 * @version	0.3-3  06/05/2001
 * @author	Ronald Tschalär
 * @since	V0.3
 */
@Deprecated
public interface CookiePolicyHandler
{
    /**
     * This method is called for each cookie that a server tries to set via
     * the Set-Cookie header. This enables you to implement your own
     * cookie acceptance policy.
     *
     * @param cookie the cookie in question
     * @param req    the request sent which prompted the response
     * @param resp   the response which is trying to set the cookie
     * @return true if this cookie should be accepted, false if it is to
     *         be rejected.
     */
    boolean acceptCookie(Cookie cookie, RoRequest req, RoResponse resp);

    /**
     * This method is called for each cookie that is eligible for sending
     * with a request (according to the matching rules for the path, domain,
     * protocol, etc). This enables you to control the sending of cookies.
     *
     * @param cookie the cookie in question
     * @param req    the request this cookie is to be sent with
     * @return true if this cookie should be sent, false if it is to be
     *         ignored.
     */
    boolean sendCookie(Cookie cookie, RoRequest req);
}
