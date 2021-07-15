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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**
 * This class calculates a running md5 digest of the data read. When the
 * stream is closed the calculated digest is passed to a HashVerifier which
 * is expected to verify this digest and to throw an Exception if it fails.
 *
 * @version	0.3-3  06/05/2001
 * @author	Ronald Tschalär
 */
@Deprecated
class MD5InputStream extends FilterInputStream
{
    private HashVerifier verifier;
    private MessageDigest md5;
    private long rcvd = 0;
    private boolean closed = false;


    /**
     * @param is the input stream over which the md5 hash is to be calculated
     * @param verifier the HashVerifier to invoke when the stream is closed
     */
    @SuppressFBWarnings("WEAK_MESSAGE_DIGEST_MD5")
	public MD5InputStream(InputStream is, HashVerifier verifier)
    {
	super(is);
	this.verifier = verifier;
	try
	    { md5 = MessageDigest.getInstance("MD5"); }
	catch (NoSuchAlgorithmException nsae)
	    { throw new Error(nsae.toString()); }
    }


    public synchronized int read() throws IOException
    {
	int b = in.read();
	if (b != -1)
	    md5.update((byte) b);
	else
	    real_close();

	rcvd++;
	return b;
    }


    public synchronized int read(byte[] buf, int off, int len)
	    throws IOException
    {
	int num = in.read(buf, off, len);
	if (num > 0)
	    md5.update(buf, off, num);
	else
	    real_close();

	rcvd += num;
	return num;
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


    /**
     * Close the stream and check the digest. If the stream has not been
     * fully read then the rest of the data will first be read (and discarded)
     * to complete the digest calculation.
     *
     * @exception IOException if the close()'ing the underlying stream throws
     *                        an IOException, or if the expected digest and
     *                        the calculated digest don't match.
     */
    public synchronized void close()  throws IOException
    {
	while (skip(10000) > 0) ;
	real_close();
    }


    /**
     * Close the stream and check the digest.
     *
     * @exception IOException if the close()'ing the underlying stream throws
     *                        an IOException, or if the expected digest and
     *                        the calculated digest don't match.
     */
    private void real_close()  throws IOException
    {
	if (closed)  return;
	closed = true;

	in.close();
	verifier.verifyHash(md5.digest(), rcvd);
    }
}
