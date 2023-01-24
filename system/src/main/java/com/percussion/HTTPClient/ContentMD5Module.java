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
 * This module handles the Content-MD5 response header. If this header was
 * sent with a response and the entity isn't encoded using an unknown
 * transport encoding then an MD5InputStream is wrapped around the response
 * input stream. The MD5InputStream keeps a running digest and checks this
 * against the expected digest from the Content-MD5 header the stream is
 * closed. An IOException is thrown at that point if the digests don't match.
 *
 * @version	0.3-3  06/05/2001
 * @author	Ronald Tschalär
 */
@Deprecated
class ContentMD5Module implements HTTPClientModule
{
    // Constructors

    ContentMD5Module()
    {
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
    {
	return RSP_CONTINUE;
    }


    /**
     * Invoked by the HTTPClient.
     */
    public void responsePhase3Handler(Response resp, RoRequest req)
		throws IOException, ModuleException
    {
	if (req.getMethod().equals("HEAD"))
	    return;

	String md5_digest = resp.getHeader("Content-MD5");
	String trailer    = resp.getHeader("Trailer");
	boolean md5_tok = false;
	try
	{
	    if (trailer != null)
		md5_tok = Util.hasToken(trailer, "Content-MD5");
	}
	catch (ParseException pe)
	    { throw new ModuleException(pe.toString()); }

	if ((md5_digest == null  &&  !md5_tok)  ||
	    resp.getHeader("Transfer-Encoding") != null)
	    return;

	if (md5_digest != null)
	    Log.write(Log.MODS, "CMD5M: Received digest: " + md5_digest +
				" - pushing md5-check-stream");
	else
	    Log.write(Log.MODS, "CMD5M: Expecting digest in trailer " +
				" - pushing md5-check-stream");

	resp.inp_stream = new MD5InputStream(resp.inp_stream,
					     new VerifyMD5(resp));
    }


    /**
     * Invoked by the HTTPClient.
     */
    public void trailerHandler(Response resp, RoRequest req)
    {
    }
}


class VerifyMD5 implements HashVerifier
{
    RoResponse resp;


    public VerifyMD5(RoResponse resp)
    {
	this.resp = resp;
    }


    public void verifyHash(byte[] hash, long len)  throws IOException
    {
	String hdr;
	try
	{
	    if ((hdr = resp.getHeader("Content-MD5")) == null)
		hdr = resp.getTrailer("Content-MD5");
	}
	catch (IOException ioe)
	    { return; }		// shouldn't happen

	if (hdr == null)  return;

	byte[] ContMD5 = Codecs.base64Decode(hdr.trim().getBytes("8859_1"));

	for (int idx=0; idx<hash.length; idx++)
	{
	    if (hash[idx] != ContMD5[idx])
		throw new IOException("MD5-Digest mismatch: expected " +
				      hex(ContMD5) + " but calculated " +
				      hex(hash));
	}

	Log.write(Log.MODS, "CMD5M: hash successfully verified");
    }


    /**
     * Produce a string of the form "A5:22:F1:0B:53"
     */
    private static String hex(byte[] buf)
    {
	StringBuilder str = new StringBuilder(buf.length*3);
	for (int idx=0; idx<buf.length; idx++)
	{
	    str.append(Character.forDigit((buf[idx] >>> 4) & 15, 16));
	    str.append(Character.forDigit(buf[idx] & 15, 16));
	    str.append(':');
	}
	str.setLength(str.length()-1);

	return str.toString();
    }
}
