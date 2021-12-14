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
package com.percussion.test.http;

/**
 * Timing statistics for an HTTP request.
 * <P>
 * All timing values specify the actual system time for that event, <B>not</B>
 * durations. It is important for us to keep actual times so that we may
 * compile request statistics from multiple sources, correlating the times.
 *
 */
public class HttpRequestTimings implements Cloneable
{
   public HttpRequestTimings()
   {
   }

   /**
    * Gets the before connect time. The before connect time
    * is the time before the socket has been opened.
    *
    * @author   chad loder
    * 
    * @version 1.0 1999/10/25
    * 
    * @return   long The before connect time in milliseconds elapsed since
    * midnight, January 1, 1970 UTC.
    */
   public long beforeConnect()
   {
      return m_beforeConnect;
   }

   /**
    * Sets the before connect time. The before connect time
    * is the time before the socket has been opened.
    *
    * @author   chad loder
    * 
    * @version 1.0 1999/10/25
    * 
    * @param   time The before connect time in milliseconds elapsed since
    * midnight, January 1, 1970 UTC.
    */
   public void beforeConnect(long time)
   {
      m_beforeConnect = time;
   }

   /**
    * Gets the after connect time.
    *
    * @author   chad loder
    * 
    * @version 1.0 1999/10/25
    * 
    * @return   long The after connect time, in milliseconds elapsed since
    * midnight, January 1, 1970 UTC.
    */
   public long afterConnect()
   {
      return m_afterConnect;
   }

   /**
    * Sets the after connect time. The after connect time
    * is the time after the socket has been opened but before any
    * data has been sent or received.
    *
    * @author   chad loder
    * 
    * @version 1.0 1999/10/25
    * 
    * @param   time The after connect time in milliseconds elapsed since
    * midnight, January 1, 1970 UTC.
    */
   public void afterConnect(long time)
   {
      m_afterConnect = time;
   }

   public long afterRequest()
   {
      return m_afterRequest;
   }

   public void afterRequest(long time)
   {
      m_afterRequest = time;
   }

   /**
    * The current time that the first byte was received from the server in
    * response to a request.
    *
    * @param time The current time, using <code>System.currentTimeMillis()
    *    </code>.
    */
   public void setTimeAfterFirstByte( long time )
   {
      m_timeAfterFirstByte = time;
   }

   /**
    * See the {@link #setTimeAfterFirstByte(long) setter} method for a
    * description.
    *
    * @return The time set with the above mentioned method, or 0 if it has
    *    never been set.
    */
   public long getTimeAfterFirstByte()
   {
      return m_timeAfterFirstByte;
   }

   public long afterHeaders()
   {
      return m_afterHeaders;
   }

   public void afterHeaders(long time)
   {
      m_afterHeaders = time;
   }

   public long afterContent()
   {
      return m_afterContent;
   }

   public void afterContent(long time)
   {
      m_afterContent = time;
   }

   /**
    * The number of bytes composing the http headers.
    *
    * @return A value >= 0.
    */
   public long headerBytes()
   {
      return m_headerBytes;
   }

   public void headerBytes(long bytes)
   {
      m_headerBytes = bytes;
   }

   /**
    * The number of bytes in the response content, (not including the header
    * bytes).
    *
    * @see #headerBytes
    */
   public long contentBytes()
   {
      return m_contentBytes;
   }

   public void contentBytes(long bytes)
   {
      m_contentBytes = bytes;
   }

   public Object clone() throws CloneNotSupportedException
   {
      return super.clone();
   }

   /** this is set before the socket is opened */
   private long m_beforeConnect = 0L;

   /** this is set after the socket is opened, before any data
    *  has been sent or received */
   private long m_afterConnect  = 0L;

   /** this is set after the request has been sent, before any
    *  data has been received */
   private long m_afterRequest  = 0L;

   /** this is set after all the response headers have been
    *  received, before any body data has been received */
   private long m_afterHeaders  = 0L;

   /** this is set after all of the response content has
    *  been read */
   private long m_afterContent   = 0L;

   /** this is the number of header bytes returned,
    *  including the HTTP status line */
   private long m_headerBytes   = 0L;

   /** this is the number of content bytes returned after
    *  the last header */
   private long m_contentBytes  = 0L;

   /**
    * The time after the first byte has been received. 0 until it is set by
    * <code>setTimeAfterFirstByte</code>.
    */
   private long m_timeAfterFirstByte = 0L;
}
