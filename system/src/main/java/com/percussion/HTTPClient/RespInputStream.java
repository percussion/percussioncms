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
import java.io.InputStream;
import java.io.InterruptedIOException;

/**
 * This is the InputStream that gets returned to the user. The extensions
 * consist of the capability to have the data pushed into a buffer if the
 * stream demux needs to.
 *
 * @version	0.3-3  06/05/2001
 * @author	Ronald Tschalär
 * @since	V0.2
 */
@Deprecated
final class RespInputStream extends InputStream implements GlobalConstants
{
    /** Use old behaviour: don't set a timeout when reading the response body */
    private static boolean dontTimeoutBody = false;

    /** the stream demultiplexor */
    private StreamDemultiplexor demux = null;

    /** our response handler */
    private ResponseHandler     resph;

    /** signals that the user has closed the stream and will therefore
	not read any further data */
	    boolean             closed = false;

    /** signals that the connection may not be closed prematurely */
    private boolean             dont_truncate = false;

    /** this buffer is used to buffer data that the demux has to get rid of */
    private byte[]              buffer = null;

    /** signals that we were interrupted and that the buffer is not complete */
    private boolean             interrupted = false;

    /** the offset at which the unread data starts in the buffer */
    private int                 offset = 0;

    /** the end of the data in the buffer */
    private int                 end = 0;

    /** the total number of bytes of entity data read from the demux so far */
            int                 count = 0;

    static
    {
	try
	{
	    dontTimeoutBody = Boolean.getBoolean("com.percussion.HTTPClient.dontTimeoutRespBody");
	    if (dontTimeoutBody)
		Log.write(Log.DEMUX, "RspIS: disabling timeouts when " +
				     "reading response body");
	}
	catch (Exception e)
	    { }
    }


    // Constructors

    RespInputStream(StreamDemultiplexor demux, ResponseHandler resph)
    {
	this.demux = demux;
	this.resph = resph;
    }


    // public Methods

    private byte[] ch = new byte[1];
    /**
     * Reads a single byte.
     *
     * @return the byte read, or -1 if EOF.
     * @exception IOException if any exception occurred on the connection.
     */
    public synchronized int read() throws IOException
    {
	int rcvd = read(ch, 0, 1);
	if (rcvd == 1)
	    return ch[0] & 0xff;
	else
	    return -1;
    }


    /**
     * Reads <var>len</var> bytes into <var>b</var>, starting at offset
     * <var>off</var>.
     *
     * @return the number of bytes actually read, or -1 if EOF.
     * @exception IOException if any exception occurred on the connection.
     */
    public synchronized int read(byte[] b, int off, int len) throws IOException
    {
	if (closed)
	    return -1;

	int left = end - offset;
	if (buffer != null  &&  !(left == 0  &&  interrupted))
	{
	    if (left == 0)  return -1;

	    len = (len > left ? left : len);
	    System.arraycopy(buffer, offset, b, off, len);
	    offset += len;

	    return len;
	}
	else
	{
	    if (resph.resp.cd_type != CD_HDRS)
		Log.write(Log.DEMUX, "RspIS: Reading stream " + this.hashCode());

	    int rcvd;
	    if (dontTimeoutBody  &&  resph.resp.cd_type != CD_HDRS)
		rcvd = demux.read(b, off, len, resph, 0);
	    else
		rcvd = demux.read(b, off, len, resph, resph.resp.timeout);
	    if (rcvd != -1  &&  resph.resp.got_headers)
		count += rcvd;

	    return rcvd;
	}
    }


    /**
     * skips <var>num</var> bytes.
     *
     * @return the number of bytes actually skipped.
     * @exception IOException if any exception occurred on the connection.
     */
    public synchronized long skip(long num) throws IOException
    {
	if (closed)
	    return 0;

	int left = end - offset;
	if (buffer != null  &&  !(left == 0  &&  interrupted))
	{
	    num = (num > left ? left : num);
	    offset  += num;
	    return num;
	}
	else
	{
	    long skpd = demux.skip(num, resph);
	    if (resph.resp.got_headers)
		count += skpd;
	    return skpd;
	}
    }


    /**
     * gets the number of bytes available for reading without blocking.
     *
     * @return the number of bytes available.
     * @exception IOException if any exception occurred on the connection.
     */
    public synchronized int available() throws IOException
    {
	if (closed)
	    return 0;

	if (buffer != null  &&  !(end-offset == 0  &&  interrupted))
	    return end-offset;
	else
	    return demux.available(resph);
    }


    /**
     * closes the stream.
     *
     * @exception if any exception occurred on the connection before or
     *            during close.
     */
    public synchronized void close()  throws IOException
    {
	if (!closed)
	{
	    closed = true;

	    if (dont_truncate  &&  (buffer == null  ||  interrupted))
		readAll(resph.resp.timeout);

	    Log.write(Log.DEMUX, "RspIS: User closed stream " + hashCode());

	    demux.closeSocketIfAllStreamsClosed();

	    if (dont_truncate)
	    {
		try
		    { resph.resp.http_resp.invokeTrailerHandlers(false); }
		catch (ModuleException me)
		    { throw new IOException(me.toString()); }
	    }
	}
    }


    /**
     * A safety net to clean up.
     */
    protected void finalize()  throws Throwable
    {
	try
	    { close(); }
	finally
	    { super.finalize(); }
    }


    // local Methods

    /**
     * Reads all remainings data into buffer. This is used to force a read
     * of upstream responses.
     *
     * <P>This is probably the most tricky and buggy method around. It's the
     * only one that really violates the strict top-down method invocation
     * from the Response through the ResponseStream to the StreamDemultiplexor.
     * This means we need to be awfully careful about what is synchronized
     * and what parameters are passed to whom.
     *
     * @param timeout the timeout to use for reading from the demux
     * @exception IOException If any exception occurs while reading stream.
     */
    void readAll(int timeout)  throws IOException
    {
	Log.write(Log.DEMUX, "RspIS: Read-all on stream " + this.hashCode());

	synchronized (resph.resp)
	{
	    if (!resph.resp.got_headers)	// force headers to be read
	    {
		int sav_to = resph.resp.timeout;
		resph.resp.timeout = timeout;
		resph.resp.getStatusCode();
		resph.resp.timeout = sav_to;
	    }
	}

	synchronized (this)
	{
	    if (buffer != null  &&  !interrupted)  return;

	    int rcvd = 0;
	    try
	    {
		if (closed)			// throw away
		{
		    buffer = new byte[10000];
		    do
		    {
			count += rcvd;
			rcvd   = demux.read(buffer, 0, buffer.length, resph,
					    timeout);
		    } while (rcvd != -1);
		    buffer = null;
		}
		else
		{
		    if (buffer == null)
		    {
			buffer = new byte[10000];
			offset = 0;
			end    = 0;
		    }

		    do
		    {
			rcvd = demux.read(buffer, end, buffer.length-end, resph,
					  timeout);
			if (rcvd < 0)  break;

			count  += rcvd;
			end    += rcvd;
			buffer  = Util.resizeArray(buffer, end+10000);
		    } while (true);
		}
	    }
	    catch (InterruptedIOException iioe)
	    {
		interrupted = true;
		throw iioe;
	    }
	    catch (IOException ioe)
	    {
		buffer = null;	// force a read on demux for exception
	    }

	    interrupted = false;
	}
    }


    /**
     * Sometime the full response body must be read, i.e. the connection may
     * not be closed prematurely (by us). Currently this is needed when the
     * chunked encoding with trailers is used in a response.
     */
    synchronized void dontTruncate()
    {
	dont_truncate = true;
    }
}
