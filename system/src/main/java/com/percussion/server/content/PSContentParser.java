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

package com.percussion.server.content;

import com.percussion.server.PSRequest;
import com.percussion.server.PSRequestParsingException;
import com.percussion.util.PSInputStreamReader;
import com.percussion.util.PSPurgableTempFile;

import java.io.FileOutputStream;
import java.io.IOException;



/**
 * The PSContentParser abstract class defines the format for parsing content
 * received as part of a request. This is used primarily by the
 * PSRequestParser. All subclasses must override the parse and 
 * getSupportedContentTypes methods to perform the appropriate actions
 * for their content type(s). Be sure to return content types using
 * all lower case letters!
 *
 * @see        com.percussion.server.PSRequestParser
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public abstract class PSContentParser {

   /**
    * The minimum size of the pushback buffer passed to the parse method. This
    * is required to allow derived classes to optimize their stream reading.
    * This will allow look-aheads of this size.
    * For a particular implementation of this class, the parse may be successful
    * with a lower value and its possible that higher values may fail (if current
    * standard specs change), but for the current state of affairs, failure
    * should not occur.
   **/
   public static final int MIN_PUSHBACK_BUF_SIZE = 100;

   /**
    * Parse the specified input stream and add it to the appropriate
    * place in the request structure.
    *
    * @param      request         the request object to store the contents in
    *
    * @param      contentType      the Content-Type HTTP header value
    *
    * @param      charset         the character encoding of the content
    *
    * @param      content         the input stream containing the data. This
    *                            stream must have a minimum pushback buffer size
    *                            of MIN_PUSHBACK_BUF_SIZE. If the pushback buffer
    *                            is too small, an IOException will be thrown.
    *
    * @param      length         the amount of data to read
    *
    * @exception   IOException      if an i/o error occurs.
    *
    * @exception   PSRequestParsingException
    *                              if the content is invalid, the contentType is
    *                            malformed or the contentType is not supported
    */
   public abstract void parse(
      PSRequest request, String contentType, String charset,
      PSInputStreamReader content, int length)
      throws   IOException, PSRequestParsingException;

   /**
    * Get the content (mime) type(s) supported by this driver.
    *
    * @return      an array containing the supported content type(s). These are
    * the mime types only. No parameters that may be included as part of
    * the Content-Type HTTP header value are included.
    */
   public abstract String[] getSupportedContentTypes();

   /**
    * Is this a supported content type?
    *
    * @param      contentType      the content type to check, i.e. the mime type
    *                            or media-type. Must not include any parameters
    *                            that are part of the Content-Type HTTP header
    *                            value. The comparison is case insensitive, as
    *                            specified by RFC 2045.
    *
    * @return                     <code>true</code> if it's supported
    */
   public boolean isSupportedContentType( String contentType )
   {
      String [] supportedTypes = getSupportedContentTypes();
      for (int i = 0;  i < supportedTypes.length; i++) {
         if (0 == contentType.compareToIgnoreCase(supportedTypes[i]))
            return true;
      }

      return false;
   }

   /**
    * The Reader classes in java don't always read the length you
    * want. As such we need to loop until we get what we want.
    *
    * @param   content   the reader containing the content
    *
    * @param   buf      a buffer of at least <code>length</code> chars
    *
    * @param   length   the content length (amount of chars to read)
    *
    * @return            the amount of data we were able to read
    *
    * @exception   java.io.IOException      if an I/O error occurs
    */
   protected int getContentFromReader(
      PSInputStreamReader content, byte[] buf, int length)
      throws java.io.IOException
   {
      int n;
      int bytesRead = 0;

      while (bytesRead < length) {
         n = content.read(buf, bytesRead, (length - bytesRead));
         if (n <= 0)
            break;
         else
            bytesRead += n;
      }

      return bytesRead;
   }

   /**
    * Read the input data to a purgable temp file.
    *
    * @param   content   the reader containing the content
    *
    * @param   length   the content length (amount of chars to read)
    *
    * @return            the purgable temp file
    *
    * @exception   java.io.IOException      if an I/O error occurs
    */
   protected PSPurgableTempFile readContentIntoPurgableTempFile(
      String prefix, String suffix, java.io.File dir,
      PSInputStreamReader content, int length)
      throws java.io.IOException
   {
      byte[] buf = new byte[2048];
      PSPurgableTempFile f = new PSPurgableTempFile(prefix, suffix, dir);
      try(FileOutputStream fout = new java.io.FileOutputStream(f)){
         int read = 0;
         while (read < length) {
            int curRead;
            if ((length - read) < buf.length)
               curRead = length - read;
            else
               curRead = buf.length;

            curRead = getContentFromReader(content, buf, curRead);
            read += curRead;               // bump the bytes read
            fout.write(buf, 0, curRead);   // and store them to file

            if (curRead < buf.length)
               break;
         }

         fout.flush();
      }
      return f;
   }
}

