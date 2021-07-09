/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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


import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.stream.IntStream;

/**
 * This class provides utility methods for converting between different java
 * datatypes.
 */
public class PSDataTypeConverter
{
   /**
    * Converts the supplied String representation of binary data to a byte
    * array.
    *
    * @param value The string containing the data, may not be <code>
    * null</code>.  May be empty.  Must be either Hex or Base64 encoded.
    *
    * @return The byte array, never <code>null</code>.
    *
    * @throws IllegalArgumentException if value is <code>null</code>.
    * @throws IOException if an error occurs.
    */
   public static byte[] getBinaryFromString(String value) throws IOException
   {
      if (value == null)
         throw new IllegalArgumentException("value may not be null");

       // if not Hex, assume it must be base64 encoded
      if (isHexData(value))
         return getBinaryFromHex(value);
      else
         return getBinaryFromBase64(value);
   }

   /**
    * Determines if the supplied string can be coverted as Hex data.
    *
    * @param value The string to check, may not be <code>null</code>.  May be
    * empty.
    *
    * @return <code>true</code> if the value contains a hex representation of
    * the data, <code>false</code> if not.
    *
    * @throws IllegalArgumentException if value is <code>null</code>.
    */
   public static boolean isHexData(String value)
   {
      if (value == null)
         throw new IllegalArgumentException("value may not be null");

      /*
      we run into an interesting dilemma as all the valid hex chars
      are also valid base64 chars. We'll just hope this thing has
      some base64 magic (like spaces) if it's base64
      */
       boolean isHex = true;
      for (int i = 0; i < value.length(); i++) {
         char ch = value.charAt(i);
         if (!(((ch >= '0') && (ch <= '9')) ||
               ((ch >= 'a') && (ch <= 'f')) ||
               ((ch >= 'A') && (ch <= 'F'))))
         {
            isHex = false;
            break;
         }
      }

      return isHex;
   }

   /**
    * Converts the supplied Hex data to a byte array.
    *
    * @param value The string containing the Hex data, may not be <code>null
    * </code>.  May be empty.
    *
    * @return The byte array, never <code>null</code>.
    *
    * @throws IllegalArgumentException if value is <code>null</code>.
    */
   public static byte[] getBinaryFromHex(String value)
   {
      if (value == null)
         throw new IllegalArgumentException("value may not be null");

      int size = value.length();
      if (size == 0)
         return new byte[0];

      if ((size % 2) != 0) {   // we need this to be even (left padded)
         value = "0" + value;
         size++;
      }

      byte[] ba = new byte[size / 2];
      int onByte = 0;
      for (int i = 0; i < size; ) {
         char c1 = value.charAt(i++);
         char c2 = value.charAt(i++);
         byte b = 0;

         if ((c1 - '0') < 10)
            b = (byte)(c1 - '0');
         else if ((c1 - 'a') < 6)
            b = (byte)(10 + (c1 - 'a'));
         else
            b = (byte)(10 + (c1 - 'A'));
         b <<= 4;   // shift over to the high nibble

         if ((c2 - '0') < 10)
            b += (byte)(c2 - '0');
         else if ((c2 - 'a') < 6)
            b += (byte)(10 + (c2 - 'a'));
         else
            b += (byte)(10 + (c2 - 'A'));

         ba[onByte++] = b;
      }

      return ba;
   }

   /**
    * Converts the supplied base64 encoded value to a byte array
    *
    * @param value The string containing the encoded data, may not be <code>
    * null</code>.  May be empty.
    *
    * @return The byte array, never <code>null</code>.
    *
    * @throws IllegalArgumentException if value is <code>null</code>.
    * @throws IOException if an error occurs.
    */
   public static byte[] getBinaryFromBase64(String value) throws IOException
   {
      if (value == null)
         throw new IllegalArgumentException("value may not be null");

      return Base64.getMimeDecoder().decode(value.getBytes(StandardCharsets.UTF_8));
   }

   /**
    * Takes a string possibly containing a date and an optional input format and
    * parses it. If this succeeds, it then reformats it to the requested output
    * format.
    * 
    * @param value The date as a string, {@link Date} or
    * {@link java.sql.Timestamp}. May be <code>null</code> or empty, in which
    * case, <code>null</code> is returned.
    * 
    * @param inputFormat One of the formats as specified by the
    * {@link org.apache.commons.lang3.time.FastDateFormat} class. If <code>null</code> or blank, the
    * formats supported by the {@link #parseStringToDate(String)} method are
    * used.
    * 
    * @param outputFormat One of the formats as specified by the
    * {@link org.apache.commons.lang3.time.FastDateFormat} class. If <code>null</code> or blank,
    * <code>yyyy-MM-dd HH:mm:ss.SSS</code> is used if a non-zero time component
    * is present, otherwise, <code>yyyy-MM-dd</code> is used.
    * 
    * @return The input date formated as specified by the
    * <code>outputFormat</code> specifier, or <code>null</code> if the input
    * date cannot be parsed. If the default output format is used, it is
    * guaranteed parseable by the {@link #parseStringToDate(String)} method.
    * 
    * @throws ParseException If <code>throwEx</code> is <code>true</code> and
    * the input date cannot be parsed.
    */
   public static String transformDateString(Object value, String inputFormat, 
         String outputFormat, boolean throwEx)
      throws ParseException
   {
      if (value == null || StringUtils.isBlank(value.toString()))
         return null;
      
      Date parsedDate = null;
      if (value instanceof Date || value instanceof Timestamp)
         parsedDate = (Date) value;
      else if (StringUtils.isBlank(inputFormat))
      {
         parsedDate = parseStringToDate(value.toString());
      }
      else
      {
         FastDateFormat dfmt = FastDateFormat.getInstance(inputFormat);
         try
         {
            parsedDate = dfmt.parse(value.toString());
         }
         catch (ParseException e)
         {
            if (throwEx)
            {
               throw new ParseException(e.getLocalizedMessage()
                     + ": expected " + inputFormat, e.getErrorOffset());
            }
            parsedDate = null;
         }
      }
      
      if (parsedDate == null)
      {
         return null;
      }

      if (StringUtils.isBlank(outputFormat))
      {
         Calendar cal = Calendar.getInstance();
         cal.setTime(parsedDate);
         if (IntStream.of(
                 Calendar.HOUR_OF_DAY,
                 Calendar.MINUTE,
                 Calendar.SECOND,
                 Calendar.MILLISECOND)
                 .allMatch(i -> cal.get(i) == 0))
         {
            outputFormat = "yyyy-MM-dd";
         }
         else
            outputFormat = "yyyy-MM-dd HH:mm:ss.SSS";
      }
      FastDateFormat dfmt = FastDateFormat.getInstance(outputFormat);
      return dfmt.format(parsedDate);
   }
   
   /**
    * Convenience version of {@link #parseStringToDate(String, StringBuilder)}
    * that calls <code>parseStringToDate(myText, null)</code> and returns the
    * result.
    */
   public static Date parseStringToDate(String myText)
   {
      return parseStringToDate(myText, null);
   }


   /**
    * Convenience version of {@link #parseStringToDate(String, StringBuilder,
    * Locale)} that calls <code>parseStringToDate(myText, patternUsed, null)</code>
    * and returns the result.
    */
   public static Date parseStringToDate(String myText, StringBuilder patternUsed)
   {
      return parseStringToDate(myText, patternUsed, null);
   }



   /**
    * Try to parse a given string to a date, using a pre-defined set of formats
    * for a given locale. Tries to parse the string using each one, and returns
    * the Date obtained by the first successful attempt. If all formats fail,
    * tries to parse the string using {@link FastDateFormat} without a pattern
    * specified.
    * 
    * @param myText a given string to be parsed. May not be <code>null</code>.
    * 
    * @param patternUsed If successfully parsed, the pattern used will be
    * appended on the supplied StringBuffer. If myText cannot be parsed, the
    * buffer is not modified. May be <code>null</code> if the pattern need not
    * be returned.
    * 
    * @param locale Locale to parse the date. May be <code>null</code>, in
    * which case the system default locale is used.
    * 
    * @return A date object, <code>null</code> if the String cannot be parsed.
    * 
    * @throws IllegalArgumentException if myText is <code>null</code>.
    */
   public static Date parseStringToDate(String myText, StringBuilder patternUsed,
      Locale locale)
   {
      if (myText == null || myText.trim().length() < 1)
         throw new IllegalArgumentException(
            "Date string to parse may not be null or empty");

      String[] datePatternArray = isYearFirstPattern(myText)
         ? ms_datePatternArray1 
         : ms_datePatternArray2;
      FastDateFormat dateFormat = null;
      Date day = null;
      for (String aDatePatternArray : datePatternArray) {
         if (locale == null)
            dateFormat = FastDateFormat.getInstance(aDatePatternArray);
         else
            /*
             * Every locale might not be supported, but we do not have choice here
             * No factory method takes the pattern. Even if a particluar locale is
             * not supported, the constructor deos nto thorugh exception and
             * formatting will be successful. Result may not be desired one.
             * Tested with most popular locales, works fine.
             */
            dateFormat = FastDateFormat.getInstance(aDatePatternArray, locale);

         try {
            day = dateFormat.parse(myText);
            if (patternUsed != null)
               patternUsed.append(aDatePatternArray);
            break;
         } catch (ParseException ignored) {
         }
      }

      if (day == null)
      {
         // make one last try using the default pattern and default locale
         dateFormat = FastDateFormat.getInstance();
         try
         {
            day = dateFormat.parse(myText);
            if (patternUsed != null)
               patternUsed.append(dateFormat.getPattern());
         }
         catch (ParseException e)
         {
            day = null;
         }
      }

      return day;
   }


   /**
    * Get a recognized date format for the supplied String.  Calls {@link
    * #parseStringToDate(String, StringBuilder)} passing the supplied text and
    * an empty StringBuffer, and returns the result contained in the
    * StringBuffer.
    *
    * @param text A given string to be parsed.  May not be <code>null</code>.
    *
    * @return The recognized format, or <code>null</code> if the supplied text
    * cannot be parsed.
    */
   public static String getRecognizedDateFormat(String text)
   {
      if (text == null)
         throw new IllegalArgumentException("text may not be null");

      StringBuilder buf = new StringBuilder();
      Date date = parseStringToDate(text, buf);

      return date == null ? null : buf.toString();
   }
   
   /**
    * Determines if the passed in string has a year value as its first "token",
    * i.e. it is a 4 digit integer that occurs before the ./- delimiters.
    * @param value the date value, assumed not <code>null</code>
    * @return <code>true</code> if this is a date with the year first pattern type.
    */
   private static boolean isYearFirstPattern(String value)
   {
      StringTokenizer st = new StringTokenizer(value, "-./");
      String token;
      if(st.hasMoreTokens())
      {
         token = st.nextToken();
         if(token.length() != 4)
            return false;
         try
         {
            Integer.parseInt(token);
            return true;
         }
         catch(NumberFormatException ignore){}         
      }
      return false;
         
   }

   /**
    * An array of pre-set date pattern string to be used to determine whether a
    * given string/text is recognizable as a date. In order to be recognized as
    * a date more efficiently, it is better for a string to include year, month,
    * and date. Some popular date patterns are NOT supported here, such as
    * "dd/MM/yyyy" and any pattern using a two digit year cause confusion. 
    * That's because in JAVA, for example,
    * "03/30/1999" and "03/30/99" would be recognized recognized respectively as
    * March 30, 1999 AD and March 30, 99 AD. But in daily life, people tend to
    * regard both expression as the same.
    */
   private static String[] ms_datePatternArray1 = {
      // Accurate ones should be listed first
      "yyyy-MMMM-dd 'at' hh:mm:ss aaa",
      "yyyy-MMMM-dd HH:mm:ss",
      "yyyy.MMMM.dd 'at' hh:mm:ss aaa",
      "yyyy.MMMM.dd HH:mm:ss",
      "yyyy.MMMM.dd 'at' hh:mm aaa",
      "yyyy-MM-dd G 'at' HH:mm:ss",
      "yyyy-MM-dd HH:mm:ss.SSS",
      "yyyy-MM-dd HH:mm:ss",
      "yyyy.MM.dd G 'at' HH:mm:ss",
      "yyyy.MM.dd HH:mm:ss.SSS",
      "yyyy.MM.dd HH:mm:ss",
      "yyyy/MM/dd G 'at' HH:mm:ss",
      "yyyy/MM/dd HH:mm:ss.SSS",
      "yyyy/MM/dd HH:mm:ss",
      "yyyy/MM/dd HH:mm",
      "yyyy-MM-dd HH:mm",
      "yyyy-MM-dd",
      "yyyy.MM.dd",
      "yyyy/MM/dd",
      "yyyy-MMMM-dd",
      "yyyy.MMMM.dd",     
      "yyyy"};
   
   private static String[] ms_datePatternArray2 = {
      // Accurate ones should be listed first
      "MM-dd-yyyy G 'at' HH:mm:ss",
      "MM-dd-yyyy HH:mm:ss.SSS",
      /* special ISO-8601 format; even though it has the year first, it must be
       * listed with this group because the isYearFirstPattern method can't
       * recognize it as such because of the missing separators.
       */
      "yyyyMMdd'T'HHmmssSSS",
      "MM-dd-yyyy HH:mm:ss",
      "MM.dd.yyyy G 'at' HH:mm:ss",
      "MM.dd.yyyy HH:mm:ss.SSS",
      "MM.dd.yyyy HH:mm:ss",
      "MM/dd/yyyy G 'at' HH:mm:ss",
      "MM/dd/yyyy HH:mm:ss.SSS",
      "MM/dd/yyyy HH:mm:ss",
      "MM/dd/yyyy HH:mm",
      "MM-dd-yyyy",
      "MM.dd.yyyy",
      "MM/dd/yyyy",
      "yyyyMMdd HH:mm:ss",
      "EEE, d MMM yyyy HH:mm:ss",
      "EEEE, MMM d, yyyy",
      "MMM d, yyyy",
      "MMM yyyy", 
      "HH:mm:ss",
      "HH:mm" };


}

