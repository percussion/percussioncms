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
