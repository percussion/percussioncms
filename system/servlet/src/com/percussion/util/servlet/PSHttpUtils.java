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
package com.percussion.util.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A set of static methods useful for managing HTTP connections.
 * Eureka always sends data to the Rhythmyx server with the
 * <code>multipart/form-data</code> form type.
 **/
class PSHttpUtils
{
  /**
   * This class is never constructed, it contains only static methods.
   */
   private PSHttpUtils()
   {
   }

   /**
    * Copies a byte stream from the input to the output.  In Eureka, this is
    * normally used when appending a file attachment into the post body. This
    * method synchronizes on both the input and output streams.
    * @param in  the input byte stream
    * @param out the output byte stream
    * @throws IOException if an error occurs reading or writing the streams.
    */
   public static synchronized void copyStream(InputStream in, OutputStream out)
      throws IOException
   {
      byte[] buffer = new byte[1024];
      int bytesRead = 0;

      synchronized (in)
      {
         synchronized (out)
         {
            while (true)
            {
               if ((bytesRead = in.read(buffer)) == -1)
               {
                  out.flush();
                  break;
               }
               out.write(buffer, 0, bytesRead);
            }
         }
      }
   }

   public static final String DEFAULT_ENCODING = "ISO-8859-1";
}
