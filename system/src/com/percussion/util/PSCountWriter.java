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

package com.percussion.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

/**
 * The PSCountWriter is used to count the bytes which would be written to
 * a stream. It does not actually write anything.
 * 
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSCountWriter extends Writer
{

   public PSCountWriter()
      throws UnsupportedEncodingException
   {
      this(null, 1024);
   }

   public PSCountWriter(String encoding)
      throws UnsupportedEncodingException
   {
      this(encoding, 1024);
   }

   public PSCountWriter(String encoding, int size)
      throws UnsupportedEncodingException
   {
      super();
      m_length = 0L;
      m_numChars = 0L;
      m_closed = false;
      if (encoding == null)
         m_enc = PSCharSets.getLocalJavaName();
      else
         m_enc = encoding;

      m_bout = new ByteArrayOutputStream(size);
      m_writer = new OutputStreamWriter(m_bout, m_enc);
   }

   /**
    * Reset the counter (length) to 0.
    */
   public void clear()
   {
      m_length = 0L;
   }

   /**
    * Get the counter (length) in bytes that were written. Note that the
    * length is not in chars, but in BYTES.
    *
    * @return      the counter (length)
    */
   public long getLength()
   {
      return m_length;
   }

   /**
    * Write a portion of an array of characters.
    *
    * @param      cbuf            Array of characters
    *
    * @param      off            Offset from which to start writing characters
    *
    * @param      len            Number of characters to write
    *
    * @exception   IOException      If an I/O error occurs
    */
   public void write(char[] cbuff, int off, int len)
      throws IOException
   {
      if (m_closed)
         throw new IOException("stream already closed");

      m_writer.write(cbuff, off, len);
      m_writer.flush();
      m_length += m_bout.size(); // how many bytes were just written ?
      m_bout.reset(); // discard bytes and set length back to 0
      m_numChars += len; // keep track of the number of chars written
   }

   /**
    * @return The number of characters that were written to the writer.
    *
    */
   public long getNumChars()
   {
      return m_numChars;
   }

   /**
    * Flush the stream. If the stream has saved any characters from the
    * various write() methods in a buffer, write them immediately to
    * their intended destination. Then, if that destination is another
    * character or byte stream, flush it. Thus one flush() invocation
    * will flush all the buffers in a chain of Writers and OutputStreams.
    *
    * @exception   IOException      If an I/O error occurs
    */
    public void flush()
      throws IOException
   {
      if (m_closed)
         throw new IOException("stream already closed");
   }

   /**
    * Close the stream, flushing it first. Once a stream has been closed,
    * further write() or flush() invocations will cause an IOException to
    * be thrown. Closing a previously-closed stream, however, has no effect.
    *
    * The length and character count properties of this object will not be
    * changed by this method and will remain valid after it is called.
    */
    public void close()
      throws IOException
   {
      m_closed = true;
      if (m_writer != null)
         m_writer.close();

      // help the garbage collector
      m_enc = null;
      m_bout = null;
      m_writer = null;
   }

   /** the count of how many bytes we have written */
   private long m_length;

   /** true if this object is closed */
   private boolean m_closed;

   /** this is the encoding that we use */
   private String m_enc;

   /** where we dump the bytes to for purposes of counting */
   ByteArrayOutputStream m_bout;

   /** the writer that handles char to byte conversions */
   OutputStreamWriter m_writer;

   /** the number of chars that were written */
   private long m_numChars;
}

