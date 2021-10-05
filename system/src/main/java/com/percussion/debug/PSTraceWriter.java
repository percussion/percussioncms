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

package com.percussion.debug;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

/**
 * This class provides writer functionality to the trace output stream, and
 * additional functionality required by tracing.
 */
public class PSTraceWriter extends OutputStreamWriter
{
   /**
    * Constructs a trace writer for the specified output stream, using the
    * specified encoding.
    *
    * @param out The output stream to write to.  May not be <code>null</code>
    * and must be open.
    *
    * @param encoding The java name for the character encoding to use.  May
    * not be <code>null</code> or emtpy.
    *
    * @throws IllegalArgumentException if any param is invalid.
    * @throws UnsupportedEncodingException If the named encoding is not
    * supported.
    */
   public PSTraceWriter(OutputStream out, String encoding)
      throws UnsupportedEncodingException
   {
      super(out, encoding);
      m_outputStream = out;
   }


   /**
    * Force any system buffers associated with the trace output stream to be
    * synchronized with the underlying device (if there is one).  For example
    * if the OutputStream is an instance of a FileOutputStream, this will cause
    * all in-memory modified copies of buffers associated with it's
    * FileDesecriptor to be written to the hard disk, and it's timestamp
    * updated.  If any in-memory buffering is being done by the application
    * (for example, by a BufferedOutputStream object), those buffers must be
    * flushed into the FileDescriptor (for example, by invoking
    * OutputStream.flush) before that data will be affected by this method.
    *
    * @throws IOException if any error occurs.
    */
   public void syncOutputStream() throws IOException
   {
      // currently only do something if OutputStream is a FileOutputStream
      if (m_outputStream instanceof FileOutputStream)
      {
         FileOutputStream fos = (FileOutputStream)m_outputStream;
         fos.getFD().sync();
      }
   }


   /**
    * The output stream passed in the ctor.  Stored to be able to support
    * synchronization with the system device.
    */
   private OutputStream m_outputStream = null;
}
