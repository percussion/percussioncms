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
package com.percussion.test.io;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

/**
 * The IOTools class contains IO helper utilities.
 */
public class IOTools
{
   /**
    * Private constructor.
    *
    * This class doesn't need to ever be constructed.
    * It contains only static methods.
    */
   private IOTools()
   {
   }

   /**
    * Convenience method.
    * Copies all of the bytes from the InputStream to the
    * OutputStream using a default buffer size of 8k.
    *
    * @see  #copyStream(InputStream, OutputStream, int)
    */
   public static long copyStream(InputStream in, OutputStream out)
      throws IOException
   {
      return copyStream(in, out, 8192);
   }

   /**
    * Copies all of the bytes from the InputStream to the
    * OutputStream.  The output buffer will be flushed, but 
    * neither stream will be closed by this method.  It is
    * the responsibility of the caller to close both streams.
    *
    * @param   in  The input stream to get bytes from.
    *             Never <code>null</code>.
    *
    * @param   out The output stream to send bytes to.
    *             Never <code>null</code>.
    *
    * @param   bufSize The number of bytes to transfer
    *             at a time.
    *
    * @return  The number of bytes transferred.
    * 
    * @throws  IOException  If an I/O exception occurs during stream
    *          processing.
    *
    * @throws  IllegalArgumentException If any argument is invalid.
    */
   public static long copyStream(InputStream in, OutputStream out, int bufSize)
      throws IOException
   {
      if (bufSize <= 0)
         bufSize = 8192;   // Default to 8k.

      byte[] buf = new byte[bufSize];

      long bytesSent = 0L;

      if (in == null || out == null)
         throw new IllegalArgumentException(
            "Supplied streams must not be null.");

      while (true)
      {
         int read = in.read(buf);
         
         if (read < 0)
            break; // end of input stream reached

         out.write(buf, 0, read);

         bytesSent += read;
      }

      out.flush();

      return bytesSent;
   }
}
