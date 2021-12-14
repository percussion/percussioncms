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

import org.apache.commons.lang3.time.FastDateFormat;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * The PSStringOperation class plays a supplementary role in String handling.
 * It extends the power of Java's String object.
 *
 * @author     Jian Huang
 * @version    2.0
 * @since      1.0
 */
public class PSStringOperation
{
   public PSStringOperation()
   {
      super();
   }

   /**
    * Given a source string and a pattern substring, use a replacement string
    * to substitute with every pattern substring within the source string.
    * <p>
    * For example, Let strSrc = "Simba is the name of a bear bear, he is a famous bear",
    * then strSrc = replace(strSrc, "bear", "lion") returns a string result
    * strSrc = "Simba is the name of a lion lion, he is a famous lion". And call
    * replace(strSrc, "lion lion", "lion") substitutes "lion lion" with "lion".
    * <p>
    * Also see the singleReplace method for single replacement.
    *
    * @param   strSrc   the source string
    * @param   strSub   the substring of the src string to be replaced with
    * @param   strRep   the replacement string
    *
    * @return     a string of the replacement result
    */
   public static String replace(String strSrc, String strSub, String strRep)
   {
      if (strSrc == null)
         return null;

      // Either a null "sub" or a null "rep" means no substitution
      if ((strSub == null) || (strRep == null))
         return strSrc;

      // Nothing is null now, we start the normal procedure
      String strRight = strSrc;
      String strLeft = "";

      int nLoc;
      while ((nLoc = strRight.indexOf(strSub)) != -1){
         strLeft += strRight.substring(0, nLoc);   // store the leftmost source string
         strLeft += strRep;              // add up the replacement string
         strRight = strRight.substring(nLoc+strSub.length()); // the rightmost source string
      }

      return (strLeft + strRight);
   }

   /**
    * Given a source string and a pattern substring, use a replacement string
    * to substitute with the first pattern substring within the source string.
    * <p>
    * For example, let SourceString = "Simba is the name of a lion lion cub",
    * PatternString = "lion ", and ReplacementString = "". Then calling
    * singleReplace(SourceString, PatternString, ReplacementString) results in
    * "Simba is the name of a lion cub".
    *
    * @param   strSrc   the source string
    * @param   strSub   the substring of the src string to be replaced with
    * @param   strRep   the replacement string
    *
    * @return     a string of the replacement result
    */
   public static String singleReplace(String strSrc, String strSub, String strRep)
   {
      if (strSrc == null)
         return null;

      // Either a null "sub" or a null "rep" means no substitution
      if ((strSub == null) || (strRep == null))
         return strSrc;

      // Nothing is null now, we start the normal procedure
      String strRight = strSrc;
      String strLeft = "";

      int nLoc;
      if ((nLoc = strRight.indexOf(strSub)) != -1){
         strLeft += strRight.substring(0, nLoc);   // store the leftmost source string
         strLeft += strRep;              // add up the replacement string
         strRight = strRight.substring(nLoc+strSub.length()); // the rightmost source string
      }

      return (strLeft + strRight);
   }


   /**
    * Returns a new string object resulting from replacing all occurrences of
    * <code>oldChr</code> in <code>text</code> with <code>newChr</code>.
    * If the <code>oldChr</code> is escaped by itself, it is not replaced, and
    * is un-escaped.
    * <p>
    * Example1, If strSrc = "string1;string2;string3;",
    * then strSrc = replace(strSrc, ';', ':') returns a string result
    * strSrc = "string1:string2:string3:" replacing all occurrences of ';' with
    * ":".
    * <p>
    * Example2, If strSrc = "string1;;string2;string3;",
    * then strSrc = replace(strSrc, ';', ':') returns a string result
    * strSrc = "string1;string2:string3:" un-escaping first ';' and replcing
    * last ';' with ':'.
    *
    * @param   text     the string in which <code>oldChr</code> should be
    * replaced with  <code>newChr</code>. If this is <code>null</code>, return
    * value is <code>null</code>.
    * @param   oldChr   the character to be replaced.
    * @param   newChr   the character to replace with.
    *
    * @return  a string after replacing every occurrence of <code>oldChr</code>
    * with <code>newChr</code> in <code>text</code> unless it is escaped. It
    * will be <code>null</code>, if passed in text is <code>null</code>.
    */
   public static String replaceChar(String text, char oldChr, char newChr)
   {
      if(text == null)
         return null;

      StringBuilder newtext = new StringBuilder();
      int index = -1;
      while((index=text.indexOf(oldChr)) != -1)
      {
         String substring = text.substring(0, index);
         newtext.append(substring);
         int length = text.length();

         if(index+1 < length)
         {
            if(text.charAt(index+1) == oldChr) //means it is escaped
            {
               newtext.append(oldChr);         //Keep one char out of these
               if(index+2 < length)
                  text = text.substring(index+2);
               else
                  text = "";
            }
            else {
               newtext.append(newChr);
               text = text.substring(index+1);
            }
         }
         else {
            newtext.append(newChr);
            text = "";
         }
      }
      newtext.append(text);

      return newtext.toString();
   }

   /**
    * Returns a new string object resulting from replacing all occurrences of
    * <code>oldChr</code> in <code>text</code> with <code>newChr</code>.
    * Any existing occurences of <code>newChr</code> are escaped, even if there
    * are no occurences of <code>oldChr</code>
    *
    * <p>
    * Example1, If strSrc = "string1;string2;string3;",
    * then strSrc = replace(strSrc, ';', ':') returns a string result
    * strSrc = "string1:string2:string3:" 
    * <p>
    * Example2, If strSrc = "string1;;string2;string3;",
    * then strSrc = replace(strSrc, ';', ':') returns a string result
    * strSrc = "string1::::string2:string3:" 
    * <p>
    * Example3, If strSrc = "string1;string2:string3;",
    * then strSrc = replace(strSrc, ';', ':') returns a string result
    * strSrc = "string1:string2::string3:" 
    * 
    * @param   text     the string in which <code>oldChr</code> should be
    * replaced with  <code>newChr</code>. If this is <code>null</code>, return
    * value is <code>null</code>.  May be empty.
    * @param   oldChr   the character to be replaced.
    * @param   newChr   the character to replace with.
    *
    * @return  The modified string.  It will be <code>null</code> if the 
    * supplied <code>text</code> is <code>null</code>.
    */
   public static String replace(String text, char oldChr, char newChr)
   {
      if(text == null)
         return null;

      StringBuilder newtext = new StringBuilder();
      for (int i = 0; i < text.length(); i++)
      {
         char ch = text.charAt(i);
         if (ch == oldChr)
            newtext.append(newChr);
         else if (ch == newChr)
         {
            // escape existing newChr
            newtext.append(newChr);
            newtext.append(newChr);
         }
         else
            newtext.append(ch);

      }

      return newtext.toString();
   }

   /**
    * Gets list of values from given text by splitting it using the passed in
    * separator. If the separator is escaped by itself, it just unescapes that
    * and doesn't split at that location.
    * <br>
    * For Example if you call getSplittedList(text, ';') where text is
    * "Testing split text;and;not split;;text" will return you a list of strings
    * like these "Testing split text", "and", "not split;text".
    *
    * @param text the text which should be split, may not be <code>null</code>.
    * @param separator separator used for splitting.
    *
    * @return list of splitted strings, never <code>null</code>, may be empty.
    *
    * @throws IllegalArgumentException if <code>text</code> is <code>null</code>
    *
    **/
   public static List<String> getSplittedList(String text, char separator)
   {
      if(text == null)
         throw new IllegalArgumentException(
            "the text which should be splitted may not be null");

      List<String> list = new ArrayList<>();

      /* If passed in 'text' is empty string, add that to list and return. */
      if(text.length() == 0)
      {
         list.add(text);
         return list;
      }

      StringBuilder token = new StringBuilder();

      int index = -1;
      while((index=text.indexOf(separator)) != -1)
      {
         String substring = text.substring(0, index);

         token.append(substring);
         int length = text.length();
         if(index+1 < length)
         {
            if(text.charAt(index+1) == separator) //means it is escaped
            {
               token.append(separator);         //Keep one char out of these
               if(index+2 < length)
                  text = text.substring(index+2);
               else
                  text = "";
            }
            else {
               list.add(token.toString());
               token.setLength(0);
               text = text.substring(index+1);
            }
         }
         else {
            list.add(token.toString());
            token.setLength(0);
            text = "";
         }
      }

      /* If length of text is greater than 0 means, the last token is not added
       * to the list, so append text to token and add it to list. This happens
       * when there is some text after separator.
       */
      if(text.length() > 0)
      {
         token.append(text);
         list.add(token.toString());
      }

      return list;
   }


   /**
    * Convenience method that calls {@link #getSplittedList(String, char)
    * getSplittedList(text, separator.charAt(0))}.
    *  
    * @param separator Never <code>null</code> or empty. Only the first char
    * is used as the seperator.
    */
   public static List<String> getSplittedList(String text, String separator)
   {
      if (null == separator || separator.length() < 1)
      {
         throw new IllegalArgumentException("separator cannot be null or empty");
      }
      return getSplittedList(text, separator.charAt(0));
   }


   /**
    * Appends list of values by putting separator in between them. If separator
    * is part of the value in the list, it will be escaped by itself.
    * <p>
    * For Example, if the list of values to be appended are "Simple", "Test",
    * "For", "Append;List" and if append(values,";") is called it returns a
    * string like this "Simple;Test;For;Append;;List".
    *
    * @param values list of values to be appended, may not be <code>null</code>
    * @param separator the separator with which list of values to be appended,
    * If it is <code>null</code> default separator ";" will be used.
    *
    * @return appended list of values, may be empty, never <code>null</code>.
    *
    * @throws IllegalArgumentException if list of values is <code>null</code>.
    **/
   public static String append(List<String> values, String separator)
   {
      if(values == null)
         throw new IllegalArgumentException("values to append can not be null");

      if(separator == null)
         separator = ";";

      Iterator<String> iter = values.iterator();

      StringBuilder buffer = new StringBuilder();

      while(iter.hasNext())
      {
         //If separator is part of the value in the list, escape it by itself
         //sothat when we get back list of values from the string, it can be
         //identified and unescape instead of treating it as separator.
         String value = replace((String)iter.next(), separator,
            separator+separator);

         buffer.append(value);
         buffer.append(separator);
      }

      //exclude last separator and return string
      if(buffer.length() > 0)
         return buffer.substring(0, buffer.length()-1);
      else
         return buffer.toString();
   }


   /**
    * Converts the input string to proper case. For example, strVal = "Simba is a lion";
    * toProperCase(strVal) returns a string "Simba Is A Lion".
    *
    * @param   strVal   the input string
    *
    * @return           the string having been properly cased
    */
   public static String toProperCase(String strVal)
   {
      if (strVal == null)
         return null;

      String strRight = strVal;
      String strL = "";
      String strR = strL;
      String strTemp = strL;
      String strRes = strL;
      String strSep = " ";  // there is a space here!!!

      int nLoc;
      while ((nLoc = strRight.indexOf(strSep)) != -1){
         if (nLoc == 0){  // handle leading white spaces
            strRight = strRight.substring(strSep.length());
            continue;
         }

         strTemp = strRight.substring(0, nLoc);
         strRight = strRight.substring(nLoc + strSep.length());
         strL = strTemp.substring(0, 1);
         strL = strL.toUpperCase();

         strR = strTemp.substring(1);
         strR = strR.toLowerCase();

         if (strRes.length() > 0)
            strRes += strSep + (strL + strR);
         else
            strRes = strL + strR;
      }

      strL = strRight.substring(0, 1);
      strL = strL.toUpperCase();

      strR = strRight.substring(1);
      strR = strR.toLowerCase();

      if (strRes.length() > 0)
         strRes += strSep + (strL + strR);
      else
         strRes = strL + strR;

      return strRes;
   }

   /**
    * Convenience version of {@link #dateFormat(String, java.util.Date, Locale)}
    * that calls <code>dateFormat(strFormat, oneDate, null)</code> and returns
    * the result.
    */
   public static String dateFormat(String strFormat, java.util.Date oneDate)
   {
      return dateFormat(strFormat, oneDate, null);
   }

   /**
    * Format a reference date into a desired form. A valid format pattern, in this
    * case, strFormat, should follow the rule defined in the document comment of
    * java.text.SimpleDateFormat. In addition to follow the rule, the user bears
    * the responsibility for making a valid pattern good. MM-dd-yyyy is a good
    * pattern, but yy-MM-yyyy-dd is not a good one, although it could be a valid one.
    * <p>
    * Warning: try to avoid two-digit-year pattern like yy, instead, use yyyy to
    * play safe. Remember, 99 as year is ambiguous, no one knows whether the year is
    * 1999, 2099, or just 99.
    * <p>
    * Example 1, suppose strFormat = "EEE, d MMM yyyy HH:mm:ss"; and oneDate is the
    * object representing "1999-12-15 19:32:12", then dateFormat(strFormat, oneDate)
    * returns a string "Wed, 15 Dec 1999 19:32:12".
    * <p>
    * Example 2, suppose strFormat = "yyyy-MMMM-dd 'at' hh:mm:ss aaa"; and oneDate is
    * the object representing "1999-12-15 19:32:12", then dateFormat(strFormat, oneDate)
    * returns a string "1999-December-15 at 07:32:12 PM".
    * <p>
    *
    * @param   strFormat   the intended new format to display date
    * @param   oneDate     the reference date in any known format
    *
    * @param   outputLocale     Locale to use to formatting the date, may be
    * <code>null</code>, in which case system default locale is used.
    *
    * @return              a string of date with the format defined by strFormat
    *
    * @throws  IllegalArgumentException    if oneDate is <code>null</code>.
    */
   public static String dateFormat(String strFormat, Date oneDate,
      Locale outputLocale)
   {
      if (oneDate == null)
      {
         throw new IllegalArgumentException(
            "Date to format must not be null or empty");
      }

      Locale locale = outputLocale;
      if (locale == null)
         locale = Locale.getDefault();

      FastDateFormat formatter = null;
      if (strFormat == null || strFormat.trim().length() < 1)
      {
         formatter = FastDateFormat.getDateTimeInstance(DateFormat.DEFAULT,
            DateFormat.DEFAULT, locale);
      }
      else
      {
         /*
          * Every locale might not be supported, but we do not have choice here
          * No factory method takes the pattern. Even if a particluar locale is
          * not supported, the constructor deos nto thorugh exception and
          * formatting will be successful. Result may not be desired one.
          * Tested with most popular locales, works fine.
          */
         formatter = FastDateFormat.getInstance(strFormat, locale);
      }
      return formatter.format(oneDate);
   }
}
