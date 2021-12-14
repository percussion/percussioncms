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
package com.percussion.utils.jsr170;


/**
 * The value encoder has methods to convert values to and from encoded form. In
 * encoding strings, special characters are encoded by the sequence '_xhhhh_',
 * which denotes a specific unicode value for the given character. If the
 * sequence appears without the terminating '_' then the value is not encoded.
 * 
 * @author dougrand
 * 
 */
public class PSStringEncoder
{

   /**
    * Encode the string. The following characters are escaped:
    * <ul>
    * <li>&lt;
    * <li>&gt;
    * <li>&quot;
    * <li>&amp;
    * <li>'
    * <li>Whitespace
    * <li>_
    * </ul>
    * The xml entities are escaped as &amp;xyz;, but whitespace and underscore
    * are escaped in the form _xHHHH_ where HHHH is the hex encoding of the
    * character as a UTF-16 character.
    * 
    * @param input the string to be encoded, never <code>null</code>
    * @return the encoded output string, never <code>null</code> or empty, may
    *         be identical to the input string.
    */
   public static String encode(String input)
   {
      if (input == null)
      {
         throw new IllegalArgumentException("input may not be null");
      }
      StringBuilder rval = new StringBuilder(input.length());

      for (int i = 0; i < input.length(); i++)
      {
         char ch = input.charAt(i);
         switch (ch)
         {
            case '<' :
               rval.append("&lt;");
               continue;
            case '>' :
               rval.append("&gt;");
               continue;
            case '&' :
               rval.append("&amp;");
               continue;
            case '"' :
               rval.append("&quot;");
               continue;
            case '\'' :
               rval.append("&apos;");
               continue;
            case '_' :
            case ' ' :
            case '\t' :
            case '\f' :
               String hex = Integer.toHexString(ch);
               if (hex.length() < 4)
               {
                  hex = "0000".substring(0, 4 - hex.length()) + hex;
               }
               rval.append("_x");
               rval.append(hex);
               rval.append("_");
               continue;
            default :
               rval.append(ch);
         }
      }
      return rval.toString();
   }

   /**
    * Decode the string. The following characters are escaped:
    * <ul>
    * <li>&lt;
    * <li>&gt;
    * <li>&quot;
    * <li>&amp;
    * <li>'
    * <li>Whitespace
    * <li>_
    * </ul>
    * The xml entities are escaped as &amp;xyz;, but whitespace and underscore
    * are escaped in the form _xHHHH_ where HHHH is the hex encoding of the
    * character as a UTF-16 character. An introductory underscore does not
    * escape anything if it is not followed by 'x', four hex digits and a
    * completing underscore.
    * <p>
    * Note that any unicode character specified as _xHHHH_ will be unescaped.
    * 
    * @param input the string to be decoded, never <code>null</code>
    * @return the decoded string, never <code>null</code> or empty, may match
    *         the input string.
    */
   public static String decode(String input)
   {
      if (input == null)
      {
         throw new IllegalArgumentException("input may not be null");
      }
      StringBuilder rval = new StringBuilder(input.length());

      for (int i = 0; i < input.length(); i++)
      {
         char ch = input.charAt(i);
         if (ch == '_')
         {
            if (((input.length() - i) >= 6) && input.charAt(i + 1) == 'x'
                  && input.charAt(i + 6) == '_')
            {
               String hex = input.substring(i + 2, i + 6);
               try
               {
                  ch = (char) Integer.parseInt(hex, 16);
                  i += 6;
               }
               catch (NumberFormatException e)
               {
                  // Fall through
               }
            }
         }
         else if (ch == '&')
         {
            if (input.startsWith("&amp;",i))
            {
               ch = '&';
               i += 4;
            }
            else if (input.startsWith("&quot;",i))
            {
               ch = '"';
               i += 5;
            }
            else if (input.startsWith("&lt;",i))
            {
               ch = '<';
               i += 3;
            }
            else if (input.startsWith("&gt;",i))
            {
               ch = '>';
               i += 3;
            }
            else if (input.startsWith("&apos;",i))
            {
               ch = '\'';
               i += 5;
            }
         }

         rval.append(ch);
      }
      return rval.toString();
   }
}
