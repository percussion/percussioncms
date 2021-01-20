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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.tools;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.PushbackInputStream;


/**
 * This class is used to allow the readLine method to be called on an
 * input stream.
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSInputStreamReader extends PushbackInputStream
{
	/**
	 * Construct an input stream reader for the specified stream. Depending on the
    * noBuffer flag, the supplied stream can be wrapped in a buffered stream.
    * The underlying pushback stream buffer will default to 1 byte.
	 *
	 * @param	in				the input stream to wrapper
	 *
	 * @param	noBuffer		if <code>true</code> do not wrap in
	 *								with a BufferedInputStream
	 */
	public PSInputStreamReader(InputStream in, boolean noBuffer)
	{
      this( in, noBuffer, 1 );
	}

	/**
	 * Construct an input stream reader for the specified stream. This
	 * will be wrapped with a BufferedInputStream. The underlying pushback stream
    * buffer will default to 1 byte.
	 *
	 * @param	in				the input stream to wrapper
	 */
	public PSInputStreamReader(InputStream in)
	{
		this(in, false, 1);
	}

	/**
	 * Construct an input stream reader for the specified stream. Depending on the
    * noBuffer flag, the supplied stream can be wrapped in a buffered stream.
    * The underlying pushback stream buffer will default to 1 byte.
	 *
	 * @param	in				the input stream to wrapper
	 *
	 * @param	noBuffer		if <code>true</code> do not wrap in
	 *								with a BufferedInputStream
    *
    * @param pushbackBufSize The number of bytes in the pushback buffer in the
    * underlying PushbackInputStream. This value is passed to the constructor
    * of the base class.
	 */
   public PSInputStreamReader( InputStream in, boolean noBuffer, int pushbackBufSize )
   {
      super((noBuffer ? in : new BufferedInputStream(in)), pushbackBufSize );
//      System.out.println( "Creating pushback stream w/ " + pushbackBufSize );
   }

	/**
	 * Read a line from this stream.
	 *
	 * @return					the next line or null if no more lines exist
	 */
	public String readLine()
		throws java.io.IOException
	{
		return readLine(null);
	}

	/**
	 * Read a line from this stream.
	 *
	 * @param	The character encoding that will be used to transform the bytes
	 * to chars.
	 *
	 * @return					the next line or null if no more lines exist
	 */
	public String readLine(String enc)
		throws java.io.IOException
	{
		java.io.ByteArrayOutputStream bout = new java.io.ByteArrayOutputStream();
		int c;
		for (c = read(); c > 0; c = read()) {
			if (c == '\n') {
				break;
			}
			else if (c == '\r') {
				// if this is '\r', does '\n' follow (which should be skipped)
				c = read();
				if (c != '\n')
					unread(c);
				break;
			}
			else {
				bout.write(c);
			}
		}

		// was end of stream reached?
		if ((bout.size() == 0) && (c < 0))
			return null;

		if (enc == null)
			return bout.toString();
		else
			return bout.toString(enc);
	}
}

