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
