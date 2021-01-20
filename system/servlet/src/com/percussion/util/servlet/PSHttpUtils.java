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