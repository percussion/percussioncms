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

import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * This class de-chunks an input stream.
 *
 * @version	0.3-3  06/05/2001
 * @author	Ronald Tschalär
 */
@Deprecated
class ChunkedInputStream extends FilterInputStream
{
    /**
     * @param is the input stream to dechunk
     */
    ChunkedInputStream(InputStream is)
    {
	super(is);
    }


    byte[] one = new byte[1];
    public synchronized int read() throws IOException
    {
	int b = read(one, 0, 1);
	if (b == 1)
	    return (one[0] & 0xff);
	else
	    return -1;
    }


    private long chunk_len = -1;
    private boolean eof   = false;

    public synchronized int read(byte[] buf, int off, int len)
	    throws IOException
    {
	if (eof)  return -1;

	if (chunk_len == -1)    // it's a new chunk
	{
	    try
		{ chunk_len = Codecs.getChunkLength(in); }
	    catch (ParseException pe)
		{ throw new IOException(pe.toString()); }
	}

	if (chunk_len > 0)              // it's data
	{
	    if (len > chunk_len)  len = (int) chunk_len;
	    int rcvd = in.read(buf, off, len);
	    if (rcvd == -1)
		throw new EOFException("Premature EOF encountered");

	    chunk_len -= rcvd;
	    if (chunk_len == 0) // got the whole chunk
	    {
		in.read();  // CR
		in.read();  // LF
		chunk_len = -1;
	    }

	    return rcvd;
	}
	else    			// the footers (trailers)
	{
	    // discard
	    Request dummy =
		    new Request(null, null, null, null, null, null, false);
	    new Response(dummy, null).readTrailers(in);

	    eof = true;
	    return -1;
	}
    }


    public synchronized long skip(long num)  throws IOException
    {
	byte[] tmp = new byte[(int) num];
	int got = read(tmp, 0, (int) num);

	if (got > 0)
	    return (long) got;
	else
	    return 0L;
    }


    public synchronized int available()  throws IOException
    {
	if (eof)  return 0;

	if (chunk_len != -1)
	    return (int) chunk_len + in.available();
	else
	    return in.available();
    }
}
