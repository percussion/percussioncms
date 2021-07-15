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

package com.percussion.util;

import com.percussion.error.PSRuntimeException;
import com.percussion.tools.PSCopyStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import java.util.Base64;

/**
 * The PSBase64Decoder class is used to decode data from the base64 format. It
 * is a wrapper for the <a href="http://jakarta.apache.org/commons/codec/">
 * Apache Commons Base64 codec</a>.
 * <p>
 * Base64 Encoding is defined in <A
 * HREF="http://www.cis.ohio-state.edu/htbin/rfc/rfc1521.html">RFC 1521</A>.
 * 
 * @author Tas Giakouminakis
 * @version 1.0
 * @since 1.0
 */
@Deprecated //user java.util.Base64
public class PSBase64Decoder
{
   /**
    * Decode the specified data.
    * 
    * @param in the string containing the base64 encoded data, never
    *           <code>null</code>
    * 
    * @return a string containing the plain text representation of the data,
    *         never <code>null</code>
    * @throws PSRuntimeException if decoding fails.
    */
   public static String decode(String in) throws PSRuntimeException
   {
      if (in == null)
         throw new IllegalArgumentException("input string may not be null");

      return decode(in, /* Default to none (use system default) */null);
   }

   /**
    * Decode the specified data. Convenience method for extracting strings with
    * a specified encoding.
    * 
    * @param in the string containing the base64 encoded data, never
    *           <code>null</code>.
    * @param encoding the encoding string for the decoded data string. if
    *           <code>null</code>, the platform's default charater set is
    *           used.
    * 
    * @return a string containing the plain text representation of the data,
    *         never <code>null</code>
    * @throws PSRuntimeException if an invalid encoding name is specified.
    */
   public static String decode(String in, String encoding)
         throws PSRuntimeException
   {
      if (in == null)
         throw new IllegalArgumentException("input string may not be null");

      /* Encoded, or Base64 string should always be Java based UTF-8 */
      try
      {
         byte[] encoded = in.getBytes(PSCharSetsConstants.rxJavaEnc());
         byte[] decoded = Base64.getMimeDecoder().decode(encoded);

         if (encoding != null && encoding.length() > 1)
            return new String(decoded, encoding);
         else
            return new String(decoded);
      }
      catch (UnsupportedEncodingException e)
      {
         throw new PSRuntimeException(IPSUtilErrors.BASE64_ENCODING_EXCEPTION,
               new Object[]
               {in, e.toString()});
      }
   }

   /**
    * Decode the specified data.
    * 
    * @param in the stream containing the base64 encoded data, never
    *           <code>null</code>
    * @param out the stream to store the plain text representation of the data,
    *           never <code>null</code>
    * 
    * @throws IOException if an I/O exception occurs
    */
   public static void decode(InputStream in, OutputStream out)
         throws IOException
   {
      if (in == null)
         throw new IllegalArgumentException("input stream may not be null");
      if (out == null)
         throw new IllegalArgumentException("output stream may not be null");

      // read stream into byte[]
      ByteArrayOutputStream encoded = new ByteArrayOutputStream(4096);
      PSCopyStream.copyStream(in, encoded);

      byte[] decodedData = Base64.getMimeDecoder().decode(encoded.toByteArray());
      ByteArrayInputStream bin = new ByteArrayInputStream(decodedData);
      PSCopyStream.copyStream(bin, out);
   }

   /**
    * Decodes a byte[] containing containing characters in the Base64 alphabet.
    * 
    * @param in A byte array containing Base64 character data, never
    *           <code>null</code>
    * @return a byte array containing decoded data
    */
   public static byte[] decode(byte[] in)
   {
      if (in == null)
         throw new IllegalArgumentException("array may not be null");
      return Base64.getMimeDecoder().decode(in);
   }

   /**
    * Provides a simple way to decode a base64 encoded string. Supply the text
    * as the only argument to this method. The decoded string is printed to the
    * console.
    */
   public static void main(String[] args)
   {
      if (args.length != 1)
      {
         System.out.println("Usage: java com.percussion.util.PSBase64Decoder"
               + " text");
         System.exit(-1);
      }
      System.out.println(decode(args[0]));
   }
}
