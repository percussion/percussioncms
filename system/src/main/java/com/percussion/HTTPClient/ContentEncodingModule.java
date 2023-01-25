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
import java.util.Vector;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;


/**
 * This module handles the Content-Encoding response header. It currently
 * handles the "gzip", "deflate", "compress" and "identity" tokens.
 *
 * @version	0.3-3  06/05/2001
 * @author	Ronald Tschalär
 */
@Deprecated
class ContentEncodingModule implements HTTPClientModule
{
    // Methods

    /**
     * Invoked by the HTTPClient.
     */
    public int requestHandler(Request req, Response[] resp)
	    throws ModuleException
    {
	// parse Accept-Encoding header

	int idx;
	NVPair[] hdrs = req.getHeaders();
	for (idx=0; idx<hdrs.length; idx++)
	    if (hdrs[idx].getName().equalsIgnoreCase("Accept-Encoding"))
		break;

	Vector pae;
	if (idx == hdrs.length)
	{
	    hdrs = Util.resizeArray(hdrs, idx+1);
	    req.setHeaders(hdrs);
	    pae = new Vector();
	}
	else
	{
	    try
		{ pae = Util.parseHeader(hdrs[idx].getValue()); }
	    catch (ParseException pe)
		{ throw new ModuleException(pe.toString()); }
	}


	// done if "*;q=1.0" present

	HttpHeaderElement all = Util.getElement(pae, "*");
	if (all != null)
	{
	    NVPair[] params = all.getParams();
	    for (idx=0; idx<params.length; idx++)
		if (params[idx].getName().equalsIgnoreCase("q"))  break;

	    if (idx == params.length)	// no qvalue, i.e. q=1.0
		return REQ_CONTINUE;

	    if (params[idx].getValue() == null  ||
		params[idx].getValue().length() == 0)
		throw new ModuleException("Invalid q value for \"*\" in " +
					  "Accept-Encoding header: ");

	    try
	    {
		if (Float.valueOf(params[idx].getValue()).floatValue() > 0.)
		    return REQ_CONTINUE;
	    }
	    catch (NumberFormatException nfe)
	    {
		throw new ModuleException("Invalid q value for \"*\" in " +
				"Accept-Encoding header: " + nfe.getMessage());
	    }
	}


	// Add gzip, deflate and compress tokens to the Accept-Encoding header

	if (!pae.contains(new HttpHeaderElement("deflate")))
	    pae.addElement(new HttpHeaderElement("deflate"));
	if (!pae.contains(new HttpHeaderElement("gzip")))
	    pae.addElement(new HttpHeaderElement("gzip"));
	if (!pae.contains(new HttpHeaderElement("x-gzip")))
	    pae.addElement(new HttpHeaderElement("x-gzip"));
	if (!pae.contains(new HttpHeaderElement("compress")))
	    pae.addElement(new HttpHeaderElement("compress"));
	if (!pae.contains(new HttpHeaderElement("x-compress")))
	    pae.addElement(new HttpHeaderElement("x-compress"));

	hdrs[idx] = new NVPair("Accept-Encoding", Util.assembleHeader(pae));

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
    {
	return RSP_CONTINUE;
    }


    /**
     * Invoked by the HTTPClient.
     */
    public void responsePhase3Handler(Response resp, RoRequest req)
		throws IOException, ModuleException
    {
	String ce = resp.getHeader("Content-Encoding");
	if (ce == null  ||  req.getMethod().equals("HEAD")  ||
	    resp.getStatusCode() == 206)
		return;

	Vector pce;
	try
	    { pce = Util.parseHeader(ce); }
	catch (ParseException pe)
	    { throw new ModuleException(pe.toString()); }

	if (pce.size() == 0)
	    return;

	String encoding = ((HttpHeaderElement) pce.firstElement()).getName();
	if (encoding.equalsIgnoreCase("gzip")  ||
	    encoding.equalsIgnoreCase("x-gzip"))
	{
	    Log.write(Log.MODS, "CEM:   pushing gzip-input-stream");

	    resp.inp_stream = new GZIPInputStream(resp.inp_stream);
	    pce.removeElementAt(pce.size()-1);
	    resp.deleteHeader("Content-length");
	}
	else if (encoding.equalsIgnoreCase("deflate"))
	{
	    Log.write(Log.MODS, "CEM:   pushing inflater-input-stream");

	    resp.inp_stream = new InflaterInputStream(resp.inp_stream);
	    pce.removeElementAt(pce.size()-1);
	    resp.deleteHeader("Content-length");
	}
	else if (encoding.equalsIgnoreCase("compress")  ||
		 encoding.equalsIgnoreCase("x-compress"))
	{
	    Log.write(Log.MODS, "CEM:   pushing uncompress-input-stream");

	    resp.inp_stream = new UncompressInputStream(resp.inp_stream);
	    pce.removeElementAt(pce.size()-1);
	    resp.deleteHeader("Content-length");
	}
	else if (encoding.equalsIgnoreCase("identity"))
	{
	    Log.write(Log.MODS, "CEM:   ignoring 'identity' token");
	    pce.removeElementAt(pce.size()-1);
	}
	else
	{
	    Log.write(Log.MODS, "CEM:   Unknown content encoding '" +
				encoding + "'");
	}

	if (pce.size() > 0)
	    resp.setHeader("Content-Encoding", Util.assembleHeader(pce));
	else
	    resp.deleteHeader("Content-Encoding");
    }


    /**
     * Invoked by the HTTPClient.
     */
    public void trailerHandler(Response resp, RoRequest req)
    {
    }
}
