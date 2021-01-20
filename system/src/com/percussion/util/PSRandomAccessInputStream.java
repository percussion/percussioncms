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

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;


/**
 * This class provides a wrapper on top of a RandomAccessFile object to
 * treat it as an input stream.
 *
 * @see        java.io.RandomAccessFile
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSRandomAccessInputStream extends InputStream
{
   public PSRandomAccessInputStream(RandomAccessFile file)
   {
      this(file, false);
   }

   public PSRandomAccessInputStream(RandomAccessFile file, boolean closeable)
   {
      m_file = file;
      m_closeable = closeable;
   }

   /**
    * Reads the next byte of data from the input stream. The value byte is
    * returned as an <code>int</code> in the range <code>0</code> to
    * <code>255</code>. If no byte is available because the end of the stream
    * has been reached, the value <code>-1</code> is returned. This method
    * blocks until input data is available, the end of the stream is detected,
    * or an exception is thrown.
    *
    * @return   the next byte of data, or <code>-1</code> if the end of the
    *             stream is reached.
    * @exception   IOException  if an I/O error occurs.
    */
   public int read() throws IOException
   {
      return m_file.read();
   }

   /**
    * Reads some number of bytes from the input stream and stores them into
    * the buffer array <code>b</code>. The number of bytes actually read is
    * returned as an integer.  This method blocks until input data is
    * available, end of file is detected, or an exception is thrown.
    *
    * <p> If <code>b</code> is <code>null</code>, a
    * <code>NullPointerException</code> is thrown.  If the length of
    * <code>b</code> is zero, then no bytes are read and <code>0</code> is
    * returned; otherwise, there is an attempt to read at least one byte. If
    * no byte is available because the stream is at end of file, the value
    * <code>-1</code> is returned; otherwise, at least one byte is read and
    * stored into <code>b</code>.
    *
    * <p> The first byte read is stored into element <code>b[0]</code>, the
    * next one into <code>b[1]</code>, and so on. The number of bytes read is,
    * at most, equal to the length of <code>b</code>. Let <i>k</i> be the
    * number of bytes actually read; these bytes will be stored in elements
    * <code>b[0]</code> through <code>b[</code><i>k</i><code>-1]</code>,
    * leaving elements <code>b[</code><i>k</i><code>]</code> through
    * <code>b[b.length-1]</code> unaffected.
    *
    * <p> If the first byte cannot be read for any reason other than end of
    * file, then an <code>IOException</code> is thrown. In particular, an
    * <code>IOException</code> is thrown if the input stream has been closed.
    *
    * @param   b    the buffer into which the data is read.
    * @return   the total number of bytes read into the buffer, or
    *             <code>-1</code> is there is no more data because the end of
    *             the stream has been reached.
    * @exception   IOException  if an I/O error occurs.
    * @see   java.io.InputStream#read(byte[], int, int)
    */
   public int read(byte b[]) throws IOException {
      return m_file.read(b);
   }

   /**
    * Reads up to <code>len</code> bytes of data from the input stream into
    * an array of bytes.  An attempt is made to read as many as
    * <code>len</code> bytes, but a smaller number may be read, possibly
    * zero. The number of bytes actually read is returned as an integer.
    *
    * <p> This method blocks until input data is available, end of file is
    * detected, or an exception is thrown.
    *
    * <p> If <code>b</code> is <code>null</code>, a
    * <code>NullPointerException</code> is thrown.
    *
    * <p> If <code>off</code> is negative, or <code>len</code> is negative, or
    * <code>off+len</code> is greater than the length of the array
    * <code>b</code>, then an <code>IndexOutOfBoundsException</code> is
    * thrown.
    *
    * <p> If <code>len</code> is zero, then no bytes are read and
    * <code>0</code> is returned; otherwise, there is an attempt to read at
    * least one byte. If no byte is available because the stream is at end of
    * file, the value <code>-1</code> is returned; otherwise, at least one
    * byte is read and stored into <code>b</code>.
    *
    * <p> The first byte read is stored into element <code>b[off]</code>, the
    * next one into <code>b[off+1]</code>, and so on. The number of bytes read
    * is, at most, equal to <code>len</code>. Let <i>k</i> be the number of
    * bytes actually read; these bytes will be stored in elements
    * <code>b[off]</code> through <code>b[off+</code><i>k</i><code>-1]</code>,
    * leaving elements <code>b[off+</code><i>k</i><code>]</code> through
    * <code>b[off+len-1]</code> unaffected.
    *
    * <p> In every case, elements <code>b[0]</code> through
    * <code>b[off]</code> and elements <code>b[off+len]</code> through
    * <code>b[b.length-1]</code> are unaffected.
    *
    * <p> If the first byte cannot be read for any reason other than end of
    * file, then an <code>IOException</code> is thrown. In particular, an
    * <code>IOException</code> is thrown if the input stream has been closed.
    *
    * @param   b      the buffer into which the data is read.
    * @param   off   the start offset in array <code>b</code>
    *                   at which the data is written.
    * @param   len   the maximum number of bytes to read.
    * @return   the total number of bytes read into the buffer, or
    *             <code>-1</code> if there is no more data because the end of
    *             the stream has been reached.
    * @exception   IOException  if an I/O error occurs.
    * @see   java.io.InputStream#read()
    */
   public int read(byte b[], int off, int len) throws IOException {
      return m_file.read(b, off, len);
   }

   /**
    * Skips over and discards <code>n</code> bytes of data from this input
    * stream. The <code>skip</code> method may, for a variety of reasons, end
    * up skipping over some smaller number of bytes, possibly <code>0</code>.
    * This may result from any of a number of conditions; reaching end of file
    * before <code>n</code> bytes have been skipped is only one possibility.
    * The actual number of bytes skipped is returned.  If <code>n</code> is
    * negative, no bytes are skipped.
    *
    * @param   n    the number of bytes to be skipped.
    * @return   the actual number of bytes skipped.
    * @exception   IOException  if an I/O error occurs.
    */
   public long skip(long n) throws IOException {
      long skip = Math.min(m_file.length() - m_file.getFilePointer(), n);

      if (skip > 0)
         m_file.seek(m_file.getFilePointer() + skip);
      
      return skip;
   }

   /**
    * Returns the number of bytes that can be read (or skipped over) from
    * this input stream without blocking by the next caller of a method for
    * this input stream.  The next caller might be the same thread or or
    * another thread.
    *
    * @return   the number of bytes that can be read from this input stream
    *             without blocking.
    * @exception   IOException  if an I/O error occurs.
    */
   public int available() throws IOException {
      long available = m_file.length() - m_file.getFilePointer();

      if (available < 0)
         return 0;

      if (available > Integer.MAX_VALUE)
         return Integer.MAX_VALUE;

      return (int)available;
   }

   /**
    * This method is not implemented. The underlying RandomAccessFile
    * object must be closed directly to close the file.
    *
    * @exception   IOException  if an I/O error occurs.
    */
   public void close() throws IOException
   {
      if (m_closeable)
      {
         m_file.close();
      }
   }

   /**
    * Marks the current position in this input stream. A subsequent call to
    * the <code>reset</code> method repositions this stream at the last marked
    * position so that subsequent reads re-read the same bytes.
    *
    * <p> The <code>readlimit</code> arguments tells this input stream to
    * allow that many bytes to be read before the mark position gets
    * invalidated.
    *
    * <p> The general contract of <code>mark</code> is that, if the method
    * <code>markSupported</code> returns <code>true</code>, the stream somehow
    * remembers all the bytes read after the call to <code>mark</code> and
    * stands ready to supply those same bytes again if and whenever the method
    * <code>reset</code> is called.  However, the stream is not required to
    * remember any data at all if more than <code>readlimit</code> bytes are
    * read from the stream before <code>reset</code> is called.
    *
    * @param   readlimit   the maximum limit of bytes that can be read before
    *                      the mark position becomes invalid.
    * @see   java.io.InputStream#reset()
    */
   public synchronized void mark(int readlimit)
   {
      try {
         m_mark = m_file.getFilePointer();
      } catch (IOException e) { /* must silently ignore this */ }
   }

   /**
    * Repositions this stream to the position at the time the
    * <code>mark</code> method was last called on this input stream.
    *
    * <p> The general contract of <code>reset</code> is:
    *
    * <p><ul>
    *
    * <li> If the method <code>markSupported</code> returns
    * <code>true</code>, then:
    *
    *     <ul><li> If the method <code>mark</code> has not been called since
    *     the stream was created, or the number of bytes read from the stream
    *     since <code>mark</code> was last called is larger than the argument
    *     to <code>mark</code> at that last call, then an
    *     <code>IOException</code> might be thrown.
    *
    *     <li> If such an <code>IOException</code> is not thrown, then the
    *     stream is reset to a state such that all the bytes read since the
    *     most recent call to <code>mark</code> (or since the start of the
    *     file, if <code>mark</code> has not been called) will be resupplied
    *     to subsequent callers of the <code>read</code> method, followed by
    *     any bytes that otherwise would have been the next input data as of
    *     the time of the call to <code>reset</code>. </ul>
    *
    * <li> If the method <code>markSupported</code> returns
    * <code>false</code>, then:
    *
    *     <ul><li> The call to <code>reset</code> may throw an
    *     <code>IOException</code>.
    *
    *     <li> If an <code>IOException</code> is not thrown, then the stream
    *     is reset to a fixed state that depends on the particular type of the
    *     input stream and how it was created. The bytes that will be supplied
    *     to subsequent callers of the <code>read</code> method depend on the
    *     particular type of the input stream. </ul></ul>
    *
    * @exception   IOException  if this stream has not been marked or if the
    *               mark has been invalidated.
    * @see   java.io.InputStream#mark(int)
    * @see   java.io.IOException
    */
   public synchronized void reset() throws IOException {
      m_file.seek(m_mark);
   }

   /**
    * Tests if this input stream supports the <code>mark</code> and
    * <code>reset</code> methods.
    *
    * @return   <code>true</code> if this true type supports the mark and reset
    *          method; <code>false</code> otherwise.
    * @see   java.io.InputStream#mark(int)
    * @see   java.io.InputStream#reset()
    */
   public boolean markSupported()
   {
      return true;
   }

   protected RandomAccessFile m_file;
   protected long m_mark = 0;
   protected boolean m_closeable;
}

