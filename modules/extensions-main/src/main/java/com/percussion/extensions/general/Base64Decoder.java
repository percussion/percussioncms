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
package com.percussion.extensions.general;

import com.percussion.data.PSConversionException;
import com.percussion.extension.IPSUdfProcessor;
import com.percussion.extension.PSSimpleJavaUdfExtension;
import com.percussion.server.IPSRequestContext;
import com.percussion.util.PSBase64Decoder;

/**
 * This class implements the UDF processor interface so it can be used as a
 * Rhythmyx function. See {@link Base64Decoder#processUdf processUdf} for a
 * description.
 */
public class Base64Decoder extends PSSimpleJavaUdfExtension implements IPSUdfProcessor
{
   /**
    * The Base64Decoder class is used to decode data from the base64 format.
    * <p>
    * Base64 Encoding is defined in
    * <A HREF="http://www.cis.ohio-state.edu/htbin/rfc/rfc1521.html">RFC 1521</A>.
    * This class can be used to enhance the functionality of the server by
    * allowing updates to non-binary columns and queries from binary columns
    * containing text. For example, if you want to store an HTML fragment in
    * a text column by uploading it as a file to the server, the server will
    * base64 encode it (as it does all uploaded files). This UDF can then be
    * used to decode the data before it is stored in the database.
    * <p>Conversely, if the data is stored in a binary column, no adjustments
    * are needed on the update, but when the data is queried, the server will
    * base64 encode it before placing it in the XML element. If the binary
    * column contains text, this UDF can be used to convert it to an
    * unencoded format.
    *
    * @param params An array containing exactly 1 or 2 elements
    * the first of which contains the object that contains the encoded
    * text. A <code>toString</code> will be called against this object.
    * If <code>null</code>, <code>null</code> is returned.
    * If empty, the emtpy string is returned.  If the second parameter
    * is supplied and is not <code>null</code> or the empty string, then
    * this parameter will be used to determine how the bytes from the
    * decoded first argument are interpreted when creating the string
    * which is returned (this will be used as the character encoding).
    *
    * @param request The current request context.
    *
    * @return The URL created from the supplied base, parameters and values.
    *
    * @throws PSConversionException If the base name is empty or <code>null
    * </code>.
    */
   public Object processUdf(Object[] params, IPSRequestContext request)
      throws PSConversionException
   {
      if ( params.length < 1 )
         throw new PSConversionException( 0, "Required param missing." );
      else if ( null == params[0] )
         return null;

      String encodedText = params[0].toString().trim();
      if ( encodedText.length() == 0 )
         return encodedText;

      /* Second parameter to this function (optional) is the 
         character encoding to be used when turning this data into
         a string after decode */
      if (params.length > 1 && params[1] != null)
      {
         String charEncoding = params[1].toString().trim();
         if (charEncoding.length() > 0)
            return PSBase64Decoder.decode(encodedText, charEncoding);
      }

      return PSBase64Decoder.decode( encodedText );
   }
}

