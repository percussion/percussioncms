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



/**
 * The PSURLEncoder class is a utility class which supports encoding URLs.
 * The process of encoding URLs involves take special characters and
 * converting them to an escaped representation. The escape syntax is
 * %HH where HH is the hex representation of the byte. This even supports
 * unicode as the unicode character may end up being two or three escaped
 * chars.
 *
 * The special characters are:
 *
 * 
 * @author      Tas Giakouminakis
 * @version      1.0
 * @since      1.0
 */
public class PSURLEncoder
{
   /**
    * This is used as a flag to indicate to encode the PATH part of a URL.
    * This encoding process will replace space characters (' ') to its  
    * hex representation ("%20"). 
    */
   private static final int SCHEME_PATH  = 1;
   
   /**
    * This is used as a flag to indicate to encode the query string of a URL
    * This encoding process will replace space characters (' ') to plus  
    * characters ('+'). 
    */
   private static final int SCHEME_PARAM = 2;
   

   /**
    * URL-8 encodes the given string, which should be in the part of
    * the URL after the server:port and before the "?" that starts the
    * query. Note that forward slashes (/) will not be escaped, but
    * other characters which are not allowed in a URI will be escaped,
    * including colons (:), question marks (?), ampersands (&), etc.
    * <p>
    * It replaces space character (' ') to its hex representation ("%20").
    *
    * @param data The string to be encoded
    * @return The encoded string, with all reserved characters escaped.
    * Unreserved characters will not be escaped.
    * @throws IllegalArgumentException if the provided string is
    *    <code>null</code>.
    */
   public static String encodePath(String data)
   {
      if (data == null)
         throw new IllegalArgumentException("data cannot be null");
      
      return encode(data, SCHEME_PATH);
   }

   /**
    * URL-8 encodes the given string, which should be in the part of
    * the URL after the "?" that starts the query. Note that any
    * ampersands (&) and questions marks (?) will be not be encoded.
    * <p>
    * It replaces space character (' ') to plus character ('+').
    *
    * @param data The string to be encoded
    * @return The encoded string, with all reserved characters escaped.
    * Unreserved characters will not be escaped.
    * @throws IllegalArgumentException if the provided string is
    *    <code>null</code>.
    */
   public static String encodeQuery(String data)
   {
      if (data == null)
         throw new IllegalArgumentException("data cannot be null");
      
      return encode(data, SCHEME_PARAM);
   }

   /**
    * Encode a string to the "x-www-form-urlencoded" form, enhanced
    * with the UTF-8-in-URL proposal. See RFC 2396 for a more complete
    * definition of the URI standard.
    *
    * This is what happens:
    *
    * <ul>
    * <li>The ASCII characters 'a' through 'z', 'A' through 'Z',
    *        and '0' through '9' remain the same.</li>
    *
    * <li>The space character ' ' is converted into a plus sign '+'.</li>
    *
    * <li>Unreserverd characters ("-", "_", ".", "!", "~", "*", "'", "(", ")")
    * are not encoded.</li>
    *
    * <li>All other ASCII characters are converted into the
    *        3-character string "%xy", where xy is
    *        the two-digit hexadecimal representation of the character
    *        code</li>
    *
    * <li>All non-ASCII characters are encoded in two steps: first
    *        to a sequence of 2 or 3 bytes, using the UTF-8 algorithm;
    *        secondly each of these bytes is encoded as "%xx".</li>
    * </ul>
    *
    * <p>
    * The algorithm used for converting non-ASCII characters is as follows:
    *
    * <ul>
    * <li>Characters in the range '\u0001' to '\u007f' are encoded as a single
    * byte:
    *
    * <table border=3>
    * <tr><th colspan=2>byte 0</th></tr>
    * <tr><td><tt>0</tt></td><td>bits 0-6</td></tr>
    * </table><p></li>
    *
    * <li>Characters in the range '\u0080' to '\u07ff' are encoded as two bytes:
    *
    * <table border=3>
    * <tr><th colspan=4>byte 0</th><th colspan=3>byte 1</th></tr>
    * <tr><td><tt>1</tt></td><td><tt>1</tt></td><td><tt>0</tt></td><td>bits 6-10
    * </td>
    * <td><tt>1</tt></td><td><tt>0</tt></td><td>bits 0-5</td></tr>
    * </table><p></li>
    *
    * <li>Characters in the range '\u0800' to '\uffff' are encoded as three
    * bytes:
    *
    * <table border=3>
    * <tr><th colspan=5>byte 0</th><th colspan=3>byte 1</th><th colspan=3>byte 2
    * </th></tr>
    * <tr><td><tt>1</tt></td><td><tt>1</tt></td><td><tt>1</tt></td><td><tt>0
    * </tt></td><td>bits 12-15</td>
    * <td><tt>1</tt></td><td><tt>0</tt></td><td>bits 6-11</td>
    * <td><tt>1</tt></td><td><tt>0</tt></td><td>bits 0-5</td></tr>
    * </table></li>
    * </ul>
    *
    * The algorithm we use will handle USC-2 characters properly, but will not
    * properly handle USC-4 characters nor will it properly handle UCS-2 values
    * between D800 and DFFF, being actually UCS-4 characters transformed through
    * UTF-16, which require special treatment.  This is very unlikely to be
    * a problem for us at this time (see RFC-2044: "UTF-8, a transformation
    * format of Unicode and ISO 10646" for more info).
    *
    * @param s The string to be encoded
    * @return The encoded string
    */
   private static String encode(String data, int scheme)
   {
      final char[] chars = data.toCharArray();
      final int len = chars.length;
      StringBuffer sbuf = new StringBuffer(len + len);
      for (int i = 0; i < len; i++)
      {
         final int ch = chars[i];
         
         if ('A' <= ch && ch <= 'Z')
         {
            // 'A'..'Z'
            sbuf.append((char)ch);
         }
         else if ('a' <= ch && ch <= 'z')
         {   // 'a'..'z'
            sbuf.append((char)ch);
         }
         else if ('0' <= ch && ch <= '9')
         {   // '0'..'9'
            sbuf.append((char)ch);
         }
         else if (ch == ' ')
         {         // space
            if (SCHEME_PATH == scheme)
               sbuf.append("%20");
            else
               sbuf.append('+');
         }
         else if (ch == '-' || ch == '_' || ch == '.' || ch == '!' || ch == '~'
            || ch == '*' || ch == '\'' || ch == '(' || ch == ')')
         {
            // don't escape unreserved characters
            sbuf.append((char)ch);
         }
         else if (ch <= 0x007f)
         {
            // don't escape slashes in paths
            if (SCHEME_PATH == scheme)
            {
               if ('/' == ch)
               {
                  sbuf.append((char)ch);
               }
               else
                  sbuf.append(hex[ch]);
            }
            else if (SCHEME_PARAM == scheme)
            {
               if ('?' == ch || '&' == ch)
               {
                  sbuf.append((char)ch);
               }
               else
                  sbuf.append(hex[ch]);
            }
            else // this should really not happen
            {
               sbuf.append(hex[ch]);
            }
         }
         else if (ch <= 0x07FF)
         {
            // non-ASCII <= 0x7FF
            sbuf.append(hex[0xc0 | (ch >> 6)]);
            sbuf.append(hex[0x80 | (ch & 0x3F)]);
         }
         else
         {
            // 0x7FF < ch <= 0xFFFF
            sbuf.append(hex[0xe0 | (ch >> 12)]);
            sbuf.append(hex[0x80 | ((ch >> 6) & 0x3F)]);
            sbuf.append(hex[0x80 | (ch & 0x3F)]);
         }
      }

      return sbuf.toString();
   }

   private final static String[] hex = {
      "%00", "%01", "%02", "%03", "%04", "%05", "%06", "%07",
      "%08", "%09", "%0A", "%0B", "%0C", "%0D", "%0E", "%0F",
      "%10", "%11", "%12", "%13", "%14", "%15", "%16", "%17",
      "%18", "%19", "%1A", "%1B", "%1C", "%1D", "%1E", "%1F",
      "%20", "%21", "%22", "%23", "%24", "%25", "%26", "%27",
      "%28", "%29", "%2A", "%2B", "%2C", "%2D", "%2E", "%2F",
      "%30", "%31", "%32", "%33", "%34", "%35", "%36", "%37",
      "%38", "%39", "%3A", "%3B", "%3C", "%3D", "%3E", "%3F",
      "%40", "%41", "%42", "%43", "%44", "%45", "%46", "%47",
      "%48", "%49", "%4A", "%4B", "%4C", "%4D", "%4E", "%4F",
      "%50", "%51", "%52", "%53", "%54", "%55", "%56", "%57",
      "%58", "%59", "%5A", "%5B", "%5C", "%5D", "%5E", "%5F",
      "%60", "%61", "%62", "%63", "%64", "%65", "%66", "%67",
      "%68", "%69", "%6A", "%6B", "%6C", "%6D", "%6E", "%6F",
      "%70", "%71", "%72", "%73", "%74", "%75", "%76", "%77",
      "%78", "%79", "%7A", "%7B", "%7C", "%7D", "%7E", "%7F",
      "%80", "%81", "%82", "%83", "%84", "%85", "%86", "%87",
      "%88", "%89", "%8A", "%8B", "%8C", "%8D", "%8E", "%8F",
      "%90", "%91", "%92", "%93", "%94", "%95", "%96", "%97",
      "%98", "%99", "%9A", "%9B", "%9C", "%9D", "%9E", "%9F",
      "%A0", "%A1", "%A2", "%A3", "%A4", "%A5", "%A6", "%A7",
      "%A8", "%A9", "%AA", "%AB", "%AC", "%AD", "%AE", "%AF",
      "%B0", "%B1", "%B2", "%B3", "%B4", "%B5", "%B6", "%B7",
      "%B8", "%B9", "%BA", "%BB", "%BC", "%BD", "%BE", "%BF",
      "%C0", "%C1", "%C2", "%C3", "%C4", "%C5", "%C6", "%C7",
      "%C8", "%C9", "%CA", "%CB", "%CC", "%CD", "%CE", "%CF",
      "%D0", "%D1", "%D2", "%D3", "%D4", "%D5", "%D6", "%D7",
      "%D8", "%D9", "%DA", "%DB", "%DC", "%DD", "%DE", "%DF",
      "%E0", "%E1", "%E2", "%E3", "%E4", "%E5", "%E6", "%E7",
      "%E8", "%E9", "%EA", "%EB", "%EC", "%ED", "%EE", "%EF",
      "%F0", "%F1", "%F2", "%F3", "%F4", "%F5", "%F6", "%F7",
      "%F8", "%F9", "%FA", "%FB", "%FC", "%FD", "%FE", "%FF"
   };


}
