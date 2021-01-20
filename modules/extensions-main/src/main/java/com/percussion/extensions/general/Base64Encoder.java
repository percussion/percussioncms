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

package com.percussion.extensions.general;

import com.percussion.data.PSConversionException;
import com.percussion.extension.IPSUdfProcessor;
import com.percussion.extension.PSSimpleJavaUdfExtension;
import com.percussion.server.IPSRequestContext;
import com.percussion.util.PSBase64Encoder;

/**
 * This class implements the UDF processor interface so it can be used as a
 * Rhythmyx function. See {@link Base64Encoder#processUdf processUdf} for a
 * description.
 */
public class Base64Encoder extends PSSimpleJavaUdfExtension 
   implements IPSUdfProcessor
{
   /**
    * The Base64Encoder class is used to encode data from the normal format.
    * <p>
    * Base64 Encoding is defined in
    * <A HREF="http://www.cis.ohio-state.edu/htbin/rfc/rfc1521.html">RFC 1521
    * </A>.<P>
    * This class can be used to store encoded passwords. For example, when we
    * register a site for publishing or a remote publisher, we want to store 
    * the FTP password and CMS passwords in the database in encoded format to 
    * avoid people seeing the plain text passwords. Use this UDF to base64 
    * encrypt the passwords that are posted in an HTML form.
    *
    * @param params An array containing exactly 1 or 2 elements
    * the first of which contains the object that contains the normal
    * text. A <code>toString</code> will be called against this object.
    * If <code>null</code>, <code>null</code> is returned.
    * If empty, the emtpy string is returned.  If the second parameter
    * is supplied and is not <code>null</code> or the empty string, then
    * this parameter will be used to determine how the bytes from the
    * encoded first argument are interpreted when creating the string
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

      String normalText = params[0].toString().trim();
      if ( normalText.length() == 0 )
         return normalText;

      /* Second parameter to this function (optional) is the 
         character encoding to be used when turning this data into
         a string after encoding */
      if (params.length > 1 && params[1] != null)
      {
         String charEncoding = params[1].toString().trim();
         if (charEncoding.length() > 0)
            return PSBase64Encoder.encode(normalText, charEncoding);
      }

      return PSBase64Encoder.encode(normalText);
   }
}

