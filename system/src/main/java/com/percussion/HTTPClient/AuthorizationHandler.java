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

import java.io.IOException;


/**
 * This is the interface that an Authorization handler must implement. You
 * can implement your own auth handler to add support for auth schemes other
 * than the ones handled by the default handler, to use a different UI for
 * soliciting usernames and passwords, or for using an altogether different
 * way of getting the necessary auth info.
 *
 * @see AuthorizationInfo#setAuthHandler(HTTPClient.AuthorizationHandler)
 * @version	0.3-3  06/05/2001
 * @author	Ronald Tschalär
 */
@Deprecated
public interface AuthorizationHandler
{
    /**
     * This method is called whenever a 401 or 407 response is received and
     * no candidate info is found in the list of known auth info. Usually
     * this method will query the user for the necessary info.
     *
     * <P>If the returned info is not null it will be added to the list of
     * known info. If the info is valid for more than one (host, port, realm,
     * scheme) tuple then this method must add the corresponding auth infos
     * itself.
     *
     * <P>This method must check <var>req.allow_ui</var> and only attempt
     * user interaction if it's <var>true</var>.
     *
     * @param challenge the parsed challenge from the server; the host,
     *                  port, scheme, realm and params are set to the
     *                  values given by the server in the challenge.
     * @param req       the request which provoked this response.
     * @param resp      the full response.
     * @return the authorization info to use when retrying the request,
     *         or null if the request is not to be retried. The necessary
     *         info includes the host, port, scheme and realm as given in
     *         the <var>challenge</var> parameter, plus either the basic
     *         cookie or any necessary params.
     * @exception AuthSchemeNotImplException if the authorization scheme
     *             in the challenge cannot be handled.
     * @exception IOException if an exception occurs while processing the
     *                        challenge
     */
    AuthorizationInfo getAuthorization(AuthorizationInfo challenge,
				       RoRequest req, RoResponse resp)
	    throws AuthSchemeNotImplException, IOException;


    /**
     * This method is called whenever auth info is chosen from the list of
     * known info in the AuthorizationInfo class to be sent with a request.
     * This happens when either auth info is being preemptively sent or if
     * a 401 response is retrieved and a matching info is found in the list
     * of known info. The intent of this method is to allow the handler to
     * fix up the info being sent based on the actual request (e.g. in digest
     * authentication the digest-uri, nonce and response-digest usually need
     * to be recalculated).
     *
     * @param info      the authorization info retrieved from the list of
     *                  known info.
     * @param req       the request this info is targeted for.
     * @param challenge the authorization challenge received from the server
     *                  if this is in response to a 401, or null if we are
     *                  preemptively sending the info.
     * @param resp      the full 401 response received, or null if we are
     *                  preemptively sending the info.
     * @return the authorization info to be sent with the request, or null
     *         if none is to be sent.
     * @exception AuthSchemeNotImplException if the authorization scheme
     *             in the info cannot be handled.
     * @exception IOException if an exception occurs while fixing up the
     *                        info
     */
    AuthorizationInfo fixupAuthInfo(AuthorizationInfo info, RoRequest req,
				   AuthorizationInfo challenge, RoResponse resp)
	    throws AuthSchemeNotImplException, IOException;


    /**
     * Sometimes even non-401 responses will contain headers pertaining to
     * authorization (such as the "Authentication-Info" header). Therefore
     * this method is invoked for each response received, even if it is not
     * a 401 or 407 response. In case of a 401 or 407 response the methods
     * <code>fixupAuthInfo()</code> and <code>getAuthorization()</code> are
     * invoked <em>after</em> this method.
     *
     * @param resp the full Response
     * @param req  the Request which provoked this response
     * @param prev the previous auth info sent, or null if none was sent
     * @param prxy the previous proxy auth info sent, or null if none was sent
     * @exception IOException if an exception occurs during the reading of
     *            the headers.
     */
    void handleAuthHeaders(Response resp, RoRequest req,
			   AuthorizationInfo prev, AuthorizationInfo prxy)
	    throws IOException;


    /**
     * This method is similar to <code>handleAuthHeaders</code> except that
     * it is called if any headers in the trailer were sent. This also
     * implies that it is invoked after any <code>fixupAuthInfo()</code> or
     * <code>getAuthorization()</code> invocation.
     *
     * @param resp the full Response
     * @param req  the Request which provoked this response
     * @param prev the previous auth info sent, or null if none was sent
     * @param prxy the previous proxy auth info sent, or null if none was sent
     * @exception IOException if an exception occurs during the reading of
     *            the trailers.
     * @see #handleAuthHeaders(HTTPClient.Response, HTTPClient.RoRequest, HTTPClient.AuthorizationInfo, HTTPClient.AuthorizationInfo)
     */
    void handleAuthTrailers(Response resp, RoRequest req,
			    AuthorizationInfo prev, AuthorizationInfo prxy)
	    throws IOException;
}
