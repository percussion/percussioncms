/*
 * Copyright 1999-2022 Percussion Software, Inc.
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

package com.percussion.util;

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
    * @param   in            the input stream to wrapper
    *
    * @param   noBuffer      if <code>true</code> do not wrap in
    *                        with a BufferedInputStream
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
    * @param   in            the input stream to wrapper
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
    * @param   in            the input stream to wrapper
    *
    * @param   noBuffer      if <code>true</code> do not wrap in
    *                        with a BufferedInputStream
    *
    * @param pushbackBufSize The number of bytes in the pushback buffer in the
    * underlying PushbackInputStream. This value is passed to the constructor
    * of the base class.
    */
   public PSInputStreamReader( InputStream in, boolean noBuffer, int pushbackBufSize )
   {
      super((noBuffer ? in : new BufferedInputStream(in)), pushbackBufSize );
   }

   /**
    * Read a line from this stream.
    *
    * @return               the next line or null if no more lines exist
    */
   public String readLine()
      throws java.io.IOException
   {
      return readLine(null);
   }

   /**
    * Read a line from this stream.
    *
    * @param enc The character encoding that will be used to transform the bytes
    * to chars.
    *
    * @return               the next line or null if no more lines exist
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

