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
                  throw new EncoderException(
                        "Found unterminated xml entity at pos " + i);
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
