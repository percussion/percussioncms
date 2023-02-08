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
import java.net.ProtocolException;


/**
 * This is the default module which gets called after all other modules
 * have done their stuff.
 *
 * @version	0.3-3  06/05/2001
 * @author	Ronald Tschalär
 */
@Deprecated
class DefaultModule implements HTTPClientModule
{
    /** number of times the request will be retried */
    private int req_timeout_retries;


    // Constructors

    /**
     * Three retries upon receipt of a 408.
     */
    DefaultModule()
    {
	req_timeout_retries = 3;
    }


    // Methods

    /**
     * Invoked by the HTTPClient.
     */
    public int requestHandler(Request req, Response[] resp)
    {
	return REQ_CONTINUE;
    }


    /**
     * Invoked by the HTTPClient.
     */
    public void responsePhase1Handler(Response resp, RoRequest req)
    {
    }


    /**
     * Invoked by the HTTPClient.
     */
    public int responsePhase2Handler(Response resp, Request req)
	    throws IOException
    {
	/* handle various response status codes until satisfied */

	int sts  = resp.getStatusCode();
	switch(sts)
	{
	    case 408: // Request Timeout

		if (req_timeout_retries-- == 0  ||  req.getStream() != null)
		{
		    Log.write(Log.MODS, "DefM:  Status " + sts + " " +
				    resp.getReasonLine() + " not handled - " +
				    "maximum number of retries exceeded");

		    return RSP_CONTINUE;
		}
		else
		{
		    Log.write(Log.MODS, "DefM:  Handling " + sts + " " +
					resp.getReasonLine() + " - " +
					"resending request");

		    return RSP_REQUEST;
		}

	    case 411: // Length Required
		if (req.getStream() != null  &&
		    req.getStream().getLength() == -1)
		    return RSP_CONTINUE;

		try { resp.getInputStream().close(); }
		catch (IOException ioe) { }
		if (req.getData() != null)
		    throw new ProtocolException("Received status code 411 even"+
					    " though Content-Length was sent");

		Log.write(Log.MODS, "DefM:  Handling " + sts + " " +
				    resp.getReasonLine() + " - resending " +
				    "request with 'Content-length: 0'");

		req.setData(new byte[0]);	// will send Content-Length: 0
		return RSP_REQUEST;

	    case 505: // HTTP Version not supported
		return RSP_CONTINUE;

	    default:
		return RSP_CONTINUE;
	}
    }


    /**
     * Invoked by the HTTPClient.
     */
    public void responsePhase3Handler(Response resp, RoRequest req)
    {
    }


    /**
     * Invoked by the HTTPClient.
     */
    public void trailerHandler(Response resp, RoRequest req)
    {
    }
}
