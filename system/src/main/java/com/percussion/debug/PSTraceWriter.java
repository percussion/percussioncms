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
