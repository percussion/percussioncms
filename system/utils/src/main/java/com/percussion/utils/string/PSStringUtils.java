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
package com.percussion.utils.string;

import static org.apache.commons.lang.Validate.*;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;

import com.percussion.utils.jsr170.PSPath;

/**
 * Useful string utilities
 * 
 * @author dougrand
 */
public class PSStringUtils
{
   private static final String ELIPSIS = ".../";

   /**
    * Get the character set or return utf-8. The charset is after the string
    * "charset=" if it is specified.
    * 
    * @param mimeType
    * @return the charset for the mimetype
    */
   public static Charset getCharsetFromMimeType(String mimeType)
   {
      int cset = mimeType.indexOf("charset");
      if (cset < 0)
         return Charset.forName("UTF-8");
      else
      {
         int eq = mimeType.indexOf('=', cset);
         if (eq < 0)
            return Charset.forName("UTF-8");
         String charsetname = mimeType.substring(eq + 1).trim();
         return Charset.forName(charsetname);
      }
   }

   /**
    * Remove any extra whitespace, converting the whitespace to space characters
    * 
    * @param input the input, never <code>null</code> or empty
    * @return the output will contain one space character for any sequence of
    *         whitespace characters in the input
    */
   public static String compressWhitespace(String input)
   {
      if (StringUtils.isBlank(input))
      {
         throw new IllegalArgumentException("input may not be null or empty");
      }

      StringBuilder rval = new StringBuilder();
      boolean skipping = false;

      for (int i = 0; i < input.length(); i++)
      {
         int ch = input.charAt(i);
         if (Character.isWhitespace(ch))
         {
            if (!skipping)
            {
               skipping = true;
               rval.append(' ');
            }
         }
         else
         {
            skipping = false;
            rval.append((char) ch);
         }
      }

      return rval.toString();
   }

   /**
    * Convert the supplied list of strings into a delimited string value.
    * 
    * @param input The list to convert, may not be <code>null</code>, may be
    *           empty.
    * @param delim The delimiter to use, may not be <code>null</code>.
    * 
    * @return The resulting string, never <code>null</code>, may be empty.
    */
   public static String listToString(List<String> input, String delim)
   {
      if (input == null)
         throw new IllegalArgumentException("input may not be null");

      if (delim == null)
         throw new IllegalArgumentException("delim may not be null");

      String result = "";

      for (String val : input)
      {
         if (result.length() > 0)
            result += delim;

         result += val;
      }

      return result;
   }

   /**
    * Convert the supplied array of strings into a delimited string value.
    * 
    * @param input The array to convert, may not be <code>null</code>, may be
    *           empty.
    * @return The resulting string, never <code>null</code>, may be empty.
    */
   public static String arrayToString(Object[] input)
   {
      notNull(input, "input");
      StringBuilder b = new StringBuilder();
      b.append('[');
      b.append(StringUtils.join(input, ","));
      b.append(']');
      return b.toString();
   }

   /**
    * Strip single or double quotes from a string. Quotes are stripped if they
    * match (starts and ends with the same quote character).
    * 
    * @param str the input string, never <code>null</code>
    * @return the stripped string, or the original if the input string had no
    *         quotes or the quotes didn't match
    */
   public static String stripQuotes(String str)
   {
      if (str == null)
      {
         throw new IllegalArgumentException("str may not be null");
      }
      if (str.length() == 0)
         return str;

      char first = str.charAt(0);
      char last = str.charAt(str.length() - 1);

      if (first == last && (first == '\'' || first == '\"'))
      {
         return str.substring(1, str.length() - 1);
      }
      else
      {
         return str;
      }
   }

   /**
    * Converts the supplied string to camel case using an underscore as the word
    * delimiter.
    * 
    * @param src The string to camel case, may be <code>null</code> or empty
    *           in which case the supplied value is returned unmodified.
    * 
    * @return The converted string, may be <code>null</code> or empty if that
    *         is what was supplied.
    */
   public static String toCamelCase(String src)
   {
      if (StringUtils.isBlank(src))
         return src;

      String[] words = WordUtils.capitalizeFully(src, new char[]
      {'_'}).split("_");

      String result = "";
      for (String word : words)
      {
         if (result.length() == 0)
            result += WordUtils.uncapitalize(word);
         else
            result += word;
      }

      return result;
   }

   /**
    * Replace or remove characters in the input string that are not alpha,
    * numeric, or certain other allowable characters.
    * 
    * @param str the input string, never <code>null</code> or empty
    * @return a string copy with only id characters
    */
   public static String replaceNonIdChars(String str)
   {
      StringBuilder rval = new StringBuilder(str.length());

      for (int i = 0; i < str.length(); i++)
      {
         char ch = str.charAt(i);

         if (Character.isWhitespace(ch))
         {
            rval.append('_');
         }
         else if (Character.isDigit(ch) || Character.isLetter(ch))
         {
            rval.append(ch);
         }
         else
         {
            // Ignore other characters
         }
      }

      return rval.toString();
   }

   /**
    * Loop over the strings, finding the first that matches the given left
    * substring
    * 
    * @param leftSubstring left substring to match, never <code>null</code> or
    *           empty
    * @param candidates a string array of candidates, never <code>null</code>
    * @return the first match, or <code>null</code> if nothing matched
    */
   public static String findMatchingLeftSubstring(String leftSubstring,
         String[] candidates)
   {
      for (String candidate : candidates)
      {
         if (candidate.startsWith(leftSubstring))
         {
            return candidate;
         }
      }
      return null;
   }

   /**
    * Using the passed font for metrics, compute a substring, which may be the
    * entire string, that can be shown in the given dimension's width. If the
    * string cannot be shown, first consider removing middle path elements. Then
    * remove the leading components. Removed components are replaced with an
    * elipsis.
    * 
    * @param path the path to trim
    * @param dimension the dimention, never <code>null</code>
    * @param font the font, never <code>null</code>
    * @return the trimed string, never <code>null</code>
    */
   public static String abbreviatePath(String path, Dimension dimension,
         Font font)
   {
      if (StringUtils.isBlank(path))
         return "";

      if (dimension == null)
      {
         throw new IllegalArgumentException("dimension may not be null");
      }
      if (font == null)
      {
         throw new IllegalArgumentException("font may not be null");
      }

      FontRenderContext ctx = new FontRenderContext(null, false, false);
      Rectangle2D bounds = font.getStringBounds(path, ctx);

      if (dimension.width >= bounds.getWidth())
      {
         return path;
      }

      PSPath ppath = new PSPath(path);
      int count = ppath.getCount();
      int middle = count / 2;

      // If the path is three components, and it was too wide, just give up
      // and return the last with an elipsis
      if (middle == 0)
         return ELIPSIS + ppath.subpath(ppath.getCount() - 1);
      else
         return abbreviatePath(ppath.subpath(0, middle), ppath
               .subpath(middle + 1), dimension, font, ctx);
   }

   /**
    * Abbreviate the passed path by pieces. If the passed path can fit when
    * rendered back. If it cannot fit, then we recurse, removing one component 
    * from each subpath
    * 
    * @param startpath the start of the path, may be <code>null</code>
    * @param endpath the end of the path, assumed not <code>null</code>
    * @param dimension the maximum dimension for the rendered string
    * @param font the font to use
    * @param ctx the font context to use
    * @return the string
    */
   private static String abbreviatePath(PSPath startpath, PSPath endpath,
         Dimension dimension, Font font, FontRenderContext ctx)
   {
      StringBuilder b = new StringBuilder();
      
      if (startpath == null || startpath.getCount() == 1)
      {
         return ELIPSIS + endpath.toString();
      }
      
      if (startpath != null)
      {
         b.append(startpath.toString());
         b.append('/');
      }
      b.append(ELIPSIS);
      b.append(endpath.toString());
      
      Rectangle2D bounds = font.getStringBounds(b.toString(), ctx);
      if (dimension.width >= bounds.getWidth())
      {
         return b.toString();
      }      
      
      // Prefer removing from the left
      if (startpath.getCount() >= endpath.getCount())
      {
         // Handle startpath too short by removing
         if (startpath.getCount() < 3 /* just slashes */)
            startpath = null;
         else
            startpath = startpath.subpath(0, startpath.getCount() - 1);
      }
      else
      {
         endpath = endpath.subpath(1);
      }
      return abbreviatePath(startpath, endpath, dimension, font, ctx);
   }
   
   /**
    * Search the passed source string for the given sequence ignoring the case
    * of the characters in the sequence and the string.
    * @param src the source sequence, never <code>null</code> 
    * @param sequence the search sequence, never <code>null</code> or empty
    * @param start the start position, greater or equals to <code>0</code>
    * @return the position of the next match starting at <code>pos</code>, or
    * <code>-1</code> if there is no match
    */
   public static int indexOfIgnoringCase(String src, String sequence, int start)
   {
      if (StringUtils.isBlank(sequence))
         throw new IllegalArgumentException(
               "sequence may not be null or empty");
      if (start < 0)
         throw new IllegalArgumentException(
               "start must be greater than or equal to zero");
      if (src == null)
         throw new IllegalArgumentException("src may not be null");

      int slen = sequence.length();
      for(int i = start; i < src.length(); i++)
      {
         int j = 0;
         
         while(j < slen)
         {
            char cur = src.charAt(i + j);
            char comp = sequence.charAt(j);
            cur = Character.toUpperCase(cur);
            comp = Character.toUpperCase(comp);
            if (cur != comp) break;
            j++;
         }
         
         // if j == slen we have a match
         if (j == slen) return i;
      }
      
      return -1;
   }

   /**
    * Validates the starting of a given name. The name must not start
    * with any characters defined in {@link #INVALID_NAME_START_CHARS}.
    * Note, an object name must start with a letter, but not a digit or any
    * characters defined in {@link #INVALID_NAME_START_CHARS}. This is 
    * assumed the caller has or will be validate the name for other invalid
    * characters, such as {@link #INVALID_NAME_CHARS} and {@link #SPACE_CHARS},
    * ...etc.
    * 
    * @param name the name in question, not <code>null</code> or empty.
    * 
    * @return <code>true</code> if the 1st character of the name is valid;
    *    otherwise <code>false</code>.
    */
   public static boolean validateNameStart(final String name)
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name may not be null or empty.");

      final char ch0 = name.charAt(0);
      for (char ch : INVALID_NAME_START_CHARS_ARRAY)
      {
         if (ch0 == ch)
            return false;
      }
      
      return true;
   }
   
   /**
    * Validate the string to make sure it does not contain the characters in the
    * supplied string.
    * 
    * @param string string to validate, must not be <code>null</code> or
    * empty.
    * @param invalidChars string of characters to be avoided in the string, may
    * not be <code>null</code> or empty. If <code>null</code> or empty he
    * validation succeeds.
    * @return <code>null</code> if validation succeeds or the first character
    * from the start of the string that is not allowed.
    */
   public static Character validate(String string, String invalidChars)
   {
      if (string == null || string.length() == 0)
      {
         throw new IllegalArgumentException("string must not be null or empty");
      }
      for (int i = 0; i < string.length(); i++)
         for (int j = 0; j < invalidChars.length(); j++)
            if (string.charAt(i) == invalidChars.charAt(j))
               return invalidChars.charAt(j);
      return null;
   }

   /**
    * Validate the given Content Type name, to make sure it contains only 
    * alphanumeric and "_" characters.
    * <p>
    * Note, beside the characters defined in {@link #INVALID_NAME_CHARS},
    * the ".()" characters may causing issues when creating database tables for 
    * the specified Content Type. The "." causing issues when identifying RX 
    * applications. Any other characters, such as non-ASCII (for non-English
    * languages) may cause issues in parsing XML/DTD when starting RX 
    * application for the specified Content Type. These issues are mostly caused
    * by the current implementation of Rhythmyx, but not the limitation of
    * the database or XML parser.
    *  
    * @param name the name in question, must not be <code>null</code> or empty.
    * 
    * @return <code>null</code> if validation succeeds or the first character
    * from the start of the string that is not allowed.
    */
   public static Character validateContentTypeName(String name)
   {
      if (name == null || name.length() == 0)
      {
         throw new IllegalArgumentException("name must not be null or empty");
      }
      
      for (int i = 0; i < name.length(); i++)
      {
         final char ch = name.charAt(i);
         if (isValidCharForContentTypeName(ch))
            continue;
         return ch;
      }

      return null;
   }
   
   /**
    * Determines if the given character is valid for Content Type.
    * @param ch the character in question.
    * @return <code>true</code> if it is valid; otherwise <code>false</code>.
    */
   private static boolean isValidCharForContentTypeName(char ch)
   {
      return (ch == '_' || (ch >= '0' && ch <= '9') || ch == '.'
            || (ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z'));
   }
   
   /**
    * Validates the user name to warn of any special character from the string
    * {@link #INVALID_USER_NAME_CHARS}. Calls {@link #validate(String, String)} 
    * with {@link #INVALID_USER_NAME_CHARS} as the second parameter.
    * 
    * @param userName user name to validate, must not be <code>null</code> or
    * empty.
    * 
    * @return <code>null</code> if successfully validated the given user name;
    *    otherwise return the 1st invalid character that is not allowed in a
    *    user name.
    * 
    * @see #validate(String, String)
    */
   public static Character validateUserName(String userName)
   {
      if (userName == null || userName.length() == 0)
      {
         throw new IllegalArgumentException(
            "userName must not be null or empty");
      }
      return validate(userName, INVALID_USER_NAME_CHARS);
   }

   /**
    * Determines if a given string contains any invalid characters used in a 
    * name.  The invalid name characters are specified by 
    * {@link #INVALID_NAME_CHARS}.
    * 
    * @param str The string, may not be <code>null</code>.
    * 
    * @return <code>true</code> if the string contains any characters which are
    * invalid for a name, <code>false</code> otherwise.
    */
   public static boolean containsInvalidNameChars(String str)
   {
      if (str == null)
         throw new IllegalArgumentException("name may not be null");
      
      return containsInvalidNameChars(str, INVALID_NAME_CHARS_ARRAY);
   }

   /**
    * Determines if a given string contains any (supplied) invalid characters. 
    * 
    * @param str The string in question, assumed not <code>null</code>.
    * @param invalidChars the array of invalid characters.
    * 
    * @return <code>true</code> if the string contains any characters, 
    *    <code>false</code> otherwise.
    */
   private static boolean containsInvalidNameChars(String str,
         char[] invalidChars)
   {
      if (str == null)
         throw new IllegalArgumentException("name may not be null");
      
      boolean invalidChar = false;
      for (int i = 0; i < invalidChars.length; i++)
      {
         if (str.indexOf(invalidChars[i]) != -1)
         {
            invalidChar = true;
            break;
         }
      }
      
      return invalidChar;
   }

   /**
    * This API is used for logging purposes to hide the password from log file.
    * @param string the password that needs to be hidden
    * @return string [password unset] in case password passed is null or empty, and
    *           [password hidden] incase password passed in has a value.
    */
    public static String hidePass(String string)
    {
        return StringUtils.isNotEmpty(string) ? "[password hidden]" : "[password unset]";
    }

   /**
    * Converts a given content type name to be valid for a name.  Any invalid 
    * characters identified by {@link #isValidCharForContentTypeName(char)} 
    * will be replaced by '_'.
    * 
    * @param name The name in question, may not be <code>null</code> or empty.
    * 
    * @return The converted name, never <code>null</code> or empty.
    */
   public static String makeValidContentTypeName(String name)
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name may not be null or empty");
      
      if (validateContentTypeName(name) == null)
         return name;
      
      StringBuffer nameBuff = new StringBuffer();
      for (int i = 0; i < name.length(); i++)
      {
         final char ch = name.charAt(i);
         if (isValidCharForContentTypeName(ch))
            nameBuff.append(ch);
         else
            nameBuff.append('_');
      }

      return nameBuff.toString();
   }
   
   /**
    * Same as calling <code>notBlank(s, null)</code>.
    * @param s the string to validate.
    * @see #notBlank(String, String)
    */
   public static void notBlank(String s)
   {
      notBlank(s, null);
   }
   
   /**
    * Insures that the provided string is not blank.
    * Should be used for assertions only.
    * Throws <code>IllegalArgumentException</code> if the string is null, empty,
    * or contains only whitespace characters.
    * @param s the string to validate. Can be <code>null</code>.
    * @param message the message for the exception, when the validation fails.
    * If blank, the exception is thrown with standard message. 
    */
   public static void notBlank(String s, String message)
   {
      if (StringUtils.isBlank(s))
      {
         final String m = StringUtils.isBlank(message)
               ? "The value should not be null, empty, or blank."
               : message;
         throw new IllegalArgumentException(m);
      }
   }

   /**
    * Used to split the comma separated groups list entered by the user to
    * secure a section. It assumes that the groups are separated by a comma
    * character (with no spaces). If the group should contain the comma, it can
    * be scaped with the backslash character ('\').
    * <table>
    * <tr>
    * <td>String entered</td>
    * <td>Splitted groups</td>
    * </tr>
    * <tr>
    * <td><code>null</code></td>
    * <td>{}</td>
    * </tr>
    * <tr>
    * <td>''</td>
    * <td>{}</td>
    * </tr>
    * <tr>
    * <td>'group1'</td>
    * <td>{'group1'}</td>
    * </tr>
    * <tr>
    * <td>'group1,group2'</td>
    * <td>{'group1', 'group2'}</td>
    * </tr>
    * <tr>
    * <td>'group1\,group1,group2'</td>
    * <td>{'group1\,group1', 'group2'}</td>
    * </tr>
    * </table>
    * 
    * @param allowAccessTo the string that the user has entered in the UI. May
    *            be blank.
    * @return a {@link String}[] object, never <code>null</code>.
    */
   public static String[] getAllowedGroups(String allowAccessTo)
   {
       if(StringUtils.isBlank(allowAccessTo))
       {
           return new String[]{};
       }
       
       String allowed = StringUtils.replace(allowAccessTo, ALLOWED_GROUPS_ESCAPE_CHAR + ALLOWED_GROUPS_SPLIT_CHAR,
               ESCAPE_REPLACEMENT_STRING);
       String[] groups = StringUtils.splitPreserveAllTokens(allowed, ALLOWED_GROUPS_SPLIT_CHAR);

       String[] returnGroups = new String[groups.length];
       int i = 0;
       for (String group : groups)
       {
           returnGroups[i] = StringUtils.strip(StringUtils.replace(group, ESCAPE_REPLACEMENT_STRING, ALLOWED_GROUPS_ESCAPE_CHAR
                   + ALLOWED_GROUPS_SPLIT_CHAR));
           i++;
       }

       return returnGroups;
   }

   /**
    * Converts a null Long object to 0
    * @param arg 0 or the value
    * @return
    */
   public static long NullToZero(Long arg){
      if(null == arg)
         return 0;
      else
         return arg;
   }

   public static int NullToZero(Integer arg){
      if(null == arg)
         return 0;
      else
         return arg;
   }
   private static final String ALLOWED_GROUPS_SPLIT_CHAR = ",";

   private static final String ALLOWED_GROUPS_ESCAPE_CHAR = "\\";

   private static final String ESCAPE_REPLACEMENT_STRING = "<perc-escaped-comma-character>";

   /**
    * Reserved characters for URL: ";", "/", "?", ":", "@", "=" and "&"
    * See http://www.rfc-editor.org/rfc/rfc1738.txt for detail.
    */
   private final static String RESERVED_CHAR_FOR_URL = ";/?:@=&";
   
   /**
    * The unsafe characters "<", ">", """, "#", "%", "{", "}", "|", "\", "^",
    * "~", "[", "]", "`" and "+".
    * See http://www.rfc-editor.org/rfc/rfc1738.txt for detail.
    */
   private final static String UNSAFE_CHAR = "<>\"#%{}|\\^~[]`+";

   /**
    * Invalid characters for the file names in Windows are 
    * "\\", "/", ":", "*", "?", "\"", "<", ">", "|". Most of the characters are
    * covered by either {@link #RESERVED_CHAR_FOR_URL} or {@link #UNSAFE_CHAR},
    * except "*".
    */
   private final static String INVALID_CHAR_IN_WINDOWS_FILENAME = "*";
   
   /**
    * The space characters. These characters are not allowed for object names,
    * such as template, site, edition names, ...etc.
    */
   public final static String SPACE_CHARS = " \t\r\n";
   
   /**
    * The invalid characters used in an object name (such as Template, Site
    * or Edition names, ...etc) are in the following categories:
    * <ul>
    *    <li>Reserved characters for URL</li>
    *    <li>Unsafe characters used in a URL</li>
    *    <li>Invalid characters for the file names in Windows</li>
    * </ul>
    * Note, these are basically all the reserved and unsafe to use in a URL,
    * plus a "*" character. The characters in {@link #SPACE_CHARS} are also
    * invalid for object names.
    */
   public final static String INVALID_NAME_CHARS = RESERVED_CHAR_FOR_URL
         + UNSAFE_CHAR + INVALID_CHAR_IN_WINDOWS_FILENAME;

   /**
    * The invalid characters used at the beginning of an object name.
    */
   public final static String INVALID_NAME_START_CHARS = 
      "0123456789_-()$'.";
   
   /**
    * Array of character strings of {@link #INVALID_NAME_START_CHARS}.
    */
   private final static char[] INVALID_NAME_START_CHARS_ARRAY = INVALID_NAME_START_CHARS
         .toCharArray();

   /**
    * String of characters not allowed in a user name. Used to validate user
    * name.
    */
   private static final String INVALID_USER_NAME_CHARS = 
      "~`!@#$%^&*()-+=[{]}|\\;:\'\",<.>/?";

   /**
    * Array of character strings of {@link #INVALID_NAME_CHARS}.
    */
   private static final char[] INVALID_NAME_CHARS_ARRAY = INVALID_NAME_CHARS
         .toCharArray();

}
