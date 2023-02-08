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
package com.percussion.utils.codec;

import org.apache.commons.codec.Encoder;
import org.apache.commons.codec.EncoderException;

import java.util.HashMap;
import java.util.Map;

/**
 * Take the string version of an xml document and replace the four entities with
 * their character equivalents.
 * 
 * @author dougrand
 */
public class PSXmlDecoder implements Encoder
{
   /**
    *  
    */
   public PSXmlDecoder() {
      super();
   }

   static Map<String, Character> ms_xmlentities = null;

   static
   {
      ms_xmlentities = new HashMap<String, Character>();
      ms_xmlentities.put("amp", '&');
      ms_xmlentities.put("lt", '<');
      ms_xmlentities.put("gt", '>');
      ms_xmlentities.put("quot", '"');
      ms_xmlentities.put("apos", '\'');
   }
   
   /**
    * (non-Javadoc)
    * 
    * @see org.apache.commons.codec.Encoder#encode(java.lang.Object)
    */
   public Object encode(Object arg0) throws EncoderException
   {
      String input = (String) arg0;
      StringBuilder rval = new StringBuilder((int) (input.length() * 1.5));
      for (int i = 0; i < input.length(); i++)
      {
         char ch = input.charAt(i);
         switch (ch)
         {
            case '&' : // Introduces an entity
               int semi = input.indexOf(';', i);
               if (semi == -1)
               {
                  break;
               }
               String ent = input.substring(i + 1, semi);
               if (ent.charAt(0) == '#')
               {
                  char x = (char) Integer.parseInt(ent.substring(1));
                  rval.append(x);
               }
               else
               {
                  Character matched = ms_xmlentities.get(ent);
                  if (matched != null)
                  {
                     rval.append(matched.charValue());
                  }
                  else
                  {
                     throw new EncoderException("Found unknown entity " + ent
                           + " at pos " + i);
                  }
               }
               i = semi;
               break;
            default :
               rval.append(ch);
         }
      }
      return rval.toString();
   }
}
