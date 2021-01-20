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
package com.percussion.services.assembly.jexl;

import com.percussion.extension.IPSJexlMethod;
import com.percussion.extension.IPSJexlParam;
import com.percussion.extension.PSJexlUtilBase;
import com.percussion.util.PSBase64Encoder;
import com.percussion.util.PSBase64Decoder;
import com.percussion.utils.codec.PSXmlDecoder;
import com.percussion.utils.codec.PSXmlEncoder;

/**
 * Various coders and decoders (codecs) for use in the bindings and velocity
 * 
 * @author dougrand
 */
public class PSCodecUtils extends PSJexlUtilBase
{
   /**
    * @param encodedString
    * @return decoded string
    */
   @IPSJexlMethod(description = "decode a string from base 64 encoding", params =
   {@IPSJexlParam(name = "encodedString", description = "the input string")})
   public String base64Decoder(String encodedString)
   {
      return PSBase64Decoder.decode(encodedString);
   }

   /**
    * @param string
    * @return encoded string
    */
   @IPSJexlMethod(description = "encode a string to base 64 encoding", params =
   {@IPSJexlParam(name = "string", description = "the input string")})
   public String base64Encoder(String string)
   {
      return PSBase64Encoder.encode(string);
   }
   
   /**
    * @param string
    * @return encoded string
    */
   @IPSJexlMethod(description = "encode a string to xml escaping", params =
   {@IPSJexlParam(name = "string", description = "the input string")})
   public String escapeForXml(String string)
   {
      PSXmlEncoder decode = new PSXmlEncoder();
      return (String) decode.encode(string);
   }
   
   
   /**
    * Decode xml encoded string
    * @param source the source, never <code>null</code>
    * @return the decoded string
    * @throws Exception 
    */
   @IPSJexlMethod(description = "decode a string from xml escaping", params =
   {@IPSJexlParam(name = "string", description = "the input string")})
   public String decodeFromXml(String source) throws Exception
   {
      if (source == null)
      {
         throw new IllegalArgumentException("source may not be null");
      }
      PSXmlDecoder enc = new PSXmlDecoder();
      return (String) enc.encode(source);
   }
}
