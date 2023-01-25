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
