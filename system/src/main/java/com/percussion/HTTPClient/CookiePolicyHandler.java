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
