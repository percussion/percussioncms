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
