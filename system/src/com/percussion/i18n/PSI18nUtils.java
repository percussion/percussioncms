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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.i18n;

import com.percussion.util.PSDataTypeConverter;
import com.percussion.util.PSStringOperation;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * This class provides several utilty methods for internationalization support.
 * The primary objective of this class is to expose methods to XSL stylesheet
 * layer as XSLT extension functions. This class also provides definitions for
 * some string constants relevant to i18n. There is not much processing of data
 * in this class and methods make calls to appropriate methods from classes from
 * other packages.
 */
public class PSI18nUtils implements IPSI18nUtils {
   /**
    * Utility method that returns the last part of the lookup key.
    * @param key the key from which the last part is to be extracted. An empty
    * string is returned if key is <code>null</code> or <code>empty</code>.
    * @return the part of the key after {@link #LOOKUP_KEY_SEPARATOR_LAST}
    * character. Never <code>null</code> may be <code>empty</code>.
    *
    * @see #makeLookupKey
    */
   public static  String getLastSubKey(String key)
   {
      if(key==null || key.length() < 1)
         return "";
      int loc = key.indexOf(LOOKUP_KEY_SEPARATOR_LAST);
      if(loc > -1)
         key = key.substring(loc+1);
      return key;
   }

   /**
    * Get the value for the given key using the default language.
    * @param    key    lookup key string
    * @return lookup value for the given key.
    * @see PSTmxResourceBundle
    */
    public static String getString(String key)
   {
         return PSTmxResourceBundle.getInstance().getString(key);
   }

   /**
    * Gets a list of all of the keys for the provided language.
    *
    * @param language string, if <code>null</code> or <code>empty</code>,
    * default language is assumed.
    * @return all of the keys as <code>Strings</code>.  May be
    * <code>null</code> if language is not supported.
    * @see PSTmxResourceBundle
    */
   @SuppressWarnings("unchecked")
   static public Iterator getKeys(String language)
   {
      return PSTmxResourceBundle.getInstance().getKeys(language);
   }


   /**
    * Get the value for the given key and language string.
    * @param    key    lookup key string, should not be <code>null</code> or
    * <code>empty</code>.
    * @param    language    language string, may be <code>null</code> or
    * <code>empty</code> in which case default langugae is assumed.
    * @return lookup value for the given key. Not <code>null</code>, may be
    * <code>empty</code>.
    * @see PSTmxResourceBundle
    */
   static public String getString(String key, String language)
   {
         return PSTmxResourceBundle.getInstance().getString(key, language);
   }
   
   /**
    * Gets the string, with the appropriate character underlined for any 
    * registered mnemonic
    * @param    key    lookup key string, should not be <code>null</code> or
    * <code>empty</code>.
    * @param    language    language string, may be <code>null</code> or
    * <code>empty</code> in which case default langugae is assumed.
    * @return lookup value for the given key. Not <code>null</code>, may be
    * <code>empty</code>.
    * @see PSTmxResourceBundle
    */
   static public String getHtmlString(String key, String language)
   {
      String localizedString = getString(key, language);
      String mnemonic = getMnemonic(key, language);
      
      if (StringUtils.isNotBlank(mnemonic))
      {
         int i = localizedString.toUpperCase().indexOf(mnemonic.toUpperCase());
         if (i >= 0)
         {
            String before = localizedString.substring(0, i);
            String after = localizedString.substring(i+1);
            StringBuilder htmlString = new StringBuilder();
            htmlString.append(before);
            htmlString.append("<u>");
            htmlString.append(localizedString.charAt(i));
            htmlString.append("</u>");
            htmlString.append(after);
            localizedString = htmlString.toString();
         }
      }
      
      return localizedString;
   }

   /**
    * Get the mnemonic for a given key and language
    * @param    key    lookup key string, should not be <code>null</code> or
    * <code>empty</code>.
    * @param    language    language string, may be <code>null</code> or
    * <code>empty</code> in which case default langugae is assumed.
    * @return the mnemonic or an empty string if no mnemonic is defined
    * @see PSTmxResourceBundle
    */
   static public String getMnemonic(String key, String language)
   {
      return getMnemonic(key, language, 0);
   }
   
   /**
    * Get the mnemonic for a given key and language
    * @param    key    lookup key string, should not be <code>null</code> or
    * <code>empty</code>.
    * @param    language    language string, may be <code>null</code> or
    * <code>empty</code> in which case default langugae is assumed.
    * @param    def the default mnemonic to return
    * @return the mnemonic, or if undefined return a string with the default
    * as the value, or an empty string if the default is <code>0</code>
    * @see PSTmxResourceBundle
    */
   static public String getMnemonic(String key, String language, int def)
   {
      int mnemonic 
         = PSTmxResourceBundle.getInstance().getMnemonic(key, language);
      
      if (mnemonic != 0)
      {
         char arr[] = new char[] {(char) mnemonic };
         return new String(arr);
      }
      else
      {
         if (def != 0)
         {
            char arr[] = new char[] {(char) def };
            return new String(arr);
         }
         else
         {
            return "";
         }
      }
   }
   
   /**
    * Get the tooltip for a given key and language
    * @param    key    lookup key string, should not be <code>null</code> or
    * <code>empty</code>.
    * @param    language    language string, may be <code>null</code> or
    * <code>empty</code> in which case default langugae is assumed.
    * @return the tooltip or <code>null</code> if no tooltip is defined
    * @see PSTmxResourceBundle
    */   
   static public String getTooltip(String key, String language)
   {
      return PSTmxResourceBundle.getInstance().getTooltip(key, language);
   }
   
   /**
    * Get the locale based string for the given key and the given mnemonic,
    * and underline the first occurance of the mnemonic in the locale string
    * using HTML.
    * 
    * @param key lookup key string, should not be <code>null</code> or
    * <code>empty</code>.
    * @param mnemonickey lookup mnemonic string, should not be 
    * <code>null</code> or <code>empty</code>.
    * @param language language string, may be <code>null</code> or
    * <code>empty</code> in which case default langugae is assumed.
    * @return the mnemonic string
    */
   static public String getMnemonicString(String key, String mnemonickey, 
      String language)
   {
      String localestring = getString(key, language);
      String mnemonic = null;
      if(!mnemonickey.endsWith("@"))
         mnemonic = getString(mnemonickey, language);
      String rval;
      
      if (mnemonic != null && mnemonic.trim().length() > 0)
      {
         char mnemonicchar = Character.toLowerCase(mnemonic.charAt(0));
         StringBuffer htmlstring = new StringBuffer(localestring.length() + 6);
         for(int i = 0; i < localestring.length(); i++)
         {
            char ch = localestring.charAt(i);
            
            if (Character.toLowerCase(ch) == mnemonicchar)
            {
               htmlstring.append("<U>");
               htmlstring.append(ch);
               htmlstring.append("</U>");
               i++;
               if (i < localestring.length())
               {
                  htmlstring.append(localestring.substring(i));
               }
               break; // Done
            }
            else
            {
               htmlstring.append(ch);
            }
         }
         
         rval = htmlstring.toString();
      }
      else
      {
         rval = localestring;
      }
      return rval;
   }

   /**
    * A generic method to format the given date string with a given patterrn and
    * locale to a given pattern and locale.
    * 
    * @param inputDate input date string to be formatted, must not be
    * <code>null</code> or <code>empty</code>.
    * @param inputPattern pattern of the input date string, if <code>null</code>
    * or <code>empty</code>, the matching pattern is guessed.
    * @param inputLanguage input locale string in the syntax "en-us" or "ja-jp".
    * If <code>null</code> or <code>empty</code>, system default locale is
    * assumed.
    * @param outputPattern output pattern to format the input date. If
    * <code>null</code> or <code>empty</code>, a default pattern is
    * assumed.
    * @param outputLanguage output locale string to be used to formatting the
    * date. If <code>null</code> or <code>empty</code>, system default
    * locale is used.
    * @return formatted date strig, never <code>null</code> or
    * <code>empty</code>.
    * @throws IllegalArgumentException if required parameters are missing or
    * invalid.
    * @throws ParseException if parsing the input date string is failed.
    */
   static public String formatDate(
      String inputDate, String inputPattern, String inputLanguage,
      String outputPattern, String outputLanguage
      )
      throws ParseException
   {
      Locale inputLocale = getLocaleFromString(inputLanguage);
      Locale outputLocale = getLocaleFromString(outputLanguage);

      Date inDate = null;
      String result = null;

      inDate = parseStringToDate(inputDate, inputPattern, inputLocale);
      result = formatDate(inDate, outputPattern, outputLocale);

      return result;
   }

   /**
    * This helper method returns the <code>java.util.Locale object</code> from
    * the  locale string supplied. The locale string must be in the syntax of
    * <language>-<country>-<varaint>, e.g. "en-us"
    * or "ja-jp". The language and country strings must follow the ISO codes.
    * @param languageString
    * @return <code>Locale</code> object
    */
   static public Locale getLocaleFromString(String languageString)
   {
      if(languageString == null || languageString.trim().length() < 1)
         return null;

      StringTokenizer tokenizer = new StringTokenizer(languageString, "-");
      int index = 0;
      String lang = "";
      String country = "";
      String variant = "";
      String token = "";
      while(tokenizer.hasMoreTokens() && index++ < 3)
      {
         token = tokenizer.nextToken();
         switch(index)
         {
            case 1:
               lang = token;
               break;
            case 2:
               country = token;
               break;
            case 3:
               variant = token;
               break;
            default:
         }
      }
      return new Locale(lang, country, variant);
   }

   /**
    * This helper method parses a date string to date object. It itself does not
    * do anything but delegate to an equivalent method in 
    * {@link PSDataTypeConverter}.
    * @param inputDateString date string to be parsed.
    * @param inputLocale the Locale to be used while parsing.
    * @return Date object parsed, never <code>null</code>.
    * @throws IllegalArgumentException
    * @see PSDataTypeConverter
    */
   static public Date parseStringToDate(String inputDateString,
      Locale inputLocale)
   {
      if(inputDateString == null || inputDateString.trim().length() < 1)
         throw new IllegalArgumentException(
            "Input Date String must not be empty");

      Locale locale = inputLocale;
      if(locale == null)
         locale = Locale.getDefault();

      StringBuilder patternUsed = new StringBuilder();
      return PSDataTypeConverter.parseStringToDate(inputDateString,
         patternUsed, locale);
   }

   /**
    * Method to parse the given date string as per the given pattern and locale.
    * 
    * @param inpuDateString date string to be parsed, must not be
    * <code>null</code> or <code>empty</code>.
    * 
    * @param inputPattern pattern to use to while parsing, if supplied must
    * match the input date string pattern, may be <code>null</code> or
    * <code>empty</code>, in which case method will try to parse using a
    * predefined list of matching patterns as deifined in
    * {@link PSDataTypeConverter}
    * 
    * @param inputLocale locale to use while parsing, if <code>null</code>,
    * system default locale is used.
    * 
    * @return parsed Date object
    * 
    * @see com.percussion.util.PSDataTypeConverter#parseStringToDate(String,
    * StringBuilder, Locale)
    * 
    * @throws IllegalArgumentException if the supplied date string is
    * <code>null</code> or empty.
    * @throws ParseException
    */
   static public Date parseStringToDate(String inpuDateString,
      String inputPattern, Locale inputLocale)
      throws ParseException
   {
      if(inpuDateString == null || inpuDateString.trim().length() < 1)
      {
         throw new IllegalArgumentException(
            "Date string to parse must not be null or empty");
      }
      Locale locale = inputLocale;
      if(locale == null)
         locale = Locale.getDefault();
      if(inputPattern == null || inputPattern.trim().length() < 1)
         return parseStringToDate(inpuDateString, locale);

      return FastDateFormat.getInstance(inputPattern, locale).parse(inpuDateString);
   }

   /**
    * This method formats the date object as per the supplied pattern and
    * locale. It itself does not do anything but delegate to an equivalent
    * method in {@link PSStringOperation}.
    * 
    * @param dateToFormat input date (<code>java.util.Date</code>) object to
    * format. If <code>null</code> supplied, empty string is returned.
    * @param outputPattern pattren to be used to formatting the date.
    * @param outputLocale locale (<code>java.util.Locale</code>) object to
    * use while formatting. If <code>null</code> specified, system default
    * locale is used.
    * @return formatted date string, never <code>null</code>.
    * @see PSStringOperation#dateFormat(String, Date, Locale)
    */
   public static String formatDate(Date dateToFormat, String outputPattern,
      Locale outputLocale)
   {
      String result = "";
      if (dateToFormat == null)
       return result;

      return PSStringOperation.dateFormat(outputPattern, dateToFormat,
       outputLocale);
   }

   /**
    * This method returns formatted message given the resource lookup key and
    * the list of arguments to fill into the pattern. This is a wrapper on
    * {#link MessageFormat}'s format static method to facilitate calling from a
    * stylesheet. The argument list is a '|' speparated string.
    * 
    * @param messageKey lookup key to find the pattern string from the resource
    * bundle. One of the message patterns accepted by {#link MessageFormat}. All
    * the arguments are strings and type specification is not allowed. In the
    * example below <br>
    * "At {1} on {2}, there was {3} on planet {0}." <br>
    * all the arguments in the flower brackets are strings. {#link
    * MessageFormat} Allows types for the arguments in the pattern. If
    * <code>null</code> or <code>empty</code> an empty string is returned.
    * 
    * @param argList list of arguments to put in the pattern. May be
    * <code>null</code> or <code>empty</code> in which case message is
    * returned as it is.
    * 
    * @param languageString labguage string such as "en-us", "fr-fr" etc. If
    * <code>null</code> or <code>empty</code> "en-us" is assumed.
    * 
    * @return the formatted message text, never <code>null</code> may be
    * <code>empty</code>
    */

   @SuppressWarnings("unchecked")
   public static String formatMessage(String messageKey, String argList,
      String languageString)
   {
      if(messageKey == null || messageKey.length() < 1)
         return "";

      List list = null;
      //parse the argument list string to a list.
      if(argList != null && argList.length() > 0)
      {
         list = new ArrayList();
         StringTokenizer tokenizer = new StringTokenizer(argList, "|");
         while(tokenizer.hasMoreTokens())
            list.add(tokenizer.nextToken());
      }
      //If there is no argument list return the message itself without
      //formatting
      if(list == null)
         return getString(messageKey, languageString);

      return MessageFormat.format(getString(messageKey, languageString)
            , list.toArray());
   }
   
   /**
    * Get the default locale for the system. If the locale returned by
    * {@link Locale#getDefault()} is configured as an active locale in Rx, that
    * language string is returned, otherwise the {@link #DEFAULT_LANG} is
    * returned.
    * 
    * @return The language string to use for the system locale, never
    * <code>null</code> or empty.
    */
   public static String getSystemLanguage()
   {
      String lang = 
         Locale.getDefault().toString().replace('_','-').toLowerCase();
      Set<String> langs = PSTmxResourceBundle.getInstance().getLanguages();
      if (langs.contains(lang))
         return lang;
      else
         return DEFAULT_LANG;
   }

   /**
    * This utility makes the resource text lookup keys given the array list of
    * subkeys. The schem followed is as follows:
    * <p>Remove all null or empty keys from the array list of keys. This means
    * all empty keys are ignored in building the final lookup key</p>
    * <p>
    * <ul>
    * <li>Every subkey is appended with {@link #LOOKUP_KEY_SEPARATOR} except the
    * last two subkeys.</li>
    * <li>The second subkey from the last is appended with
    * {@link #LOOKUP_KEY_SEPARATOR_LAST}.</li>
    * <li>
    * All the subkeys then are concatenated.
    * </li>
    * </ul>
    * </p>
    * @param subkeys array list of string objects, if <code>null</code> or
    * <code>empty</code>, the result is an empty string
    *
    * @return the final text lookup key, never <code>null</code>.
    *
    * @see #LOOKUP_KEY_SEPARATOR
    * @see #LOOKUP_KEY_SEPARATOR_LAST
    */
   @SuppressWarnings("unchecked")
   static public String makeLookupKey(List subkeys)
   {
      StringBuffer buf = new StringBuffer();
      if(subkeys == null || subkeys.size() < 1)
         return buf.toString();
      //Make sure to remove all null/empty keys out of the list
      int index = subkeys.size()-1;
      while( index > -1 &&
            (subkeys.get(index) == null ||
             subkeys.get(index).toString().length() < 1))
      {
         subkeys.remove(index--);
      }
      //Generate the text lookup key from individual subkeys.
      Object obj = null;
      String temp = null;
      for(int i=0; i<subkeys.size(); i++)
      {
         obj = subkeys.get(i);
         if(obj == null)
            continue;
         temp = obj.toString().toString();
         if(temp.trim().length() < 1)
            continue;
         //if it is not in the beginning add the key separator
         if(buf.length() > 0)
         {
            if(i == subkeys.size()-1)
               buf.append(PSI18nUtils.LOOKUP_KEY_SEPARATOR_LAST);
            else
               buf.append(PSI18nUtils.LOOKUP_KEY_SEPARATOR);
         }
         buf.append(temp);
      }
      String res = buf.toString();
      //Make sure to append @ at the end if missing for crazy reasons
      if(res.indexOf(PSI18nUtils.LOOKUP_KEY_SEPARATOR_LAST) == -1)
         res = res + PSI18nUtils.LOOKUP_KEY_SEPARATOR_LAST;
      return res;
   }

   /**
    * String constant for lookup key separator. Text lookup key is has a syntax
    * of for example, <b>psx.key1.key2.key3@keyLast</b>. The string "." in this
    * the lookup key separator.
    */
   static public final String LOOKUP_KEY_SEPARATOR = ".";

   /**
    * String constant for last lookup key separator. Text lookup key is has a
    * syntax of for example, <b>psx.key1.key2.key3@keyLast</b>. The String "@"
    * in this the lookup key separator.
    */
   static public final String LOOKUP_KEY_SEPARATOR_LAST = "@";

   /**
    * String constant for the name of the key to get the user logged in language
    * string from the user context information. This is used using the syntax:
    * <p>
    * request.getUserContextInformation(USER_CONTEXT_VAR_SYS_LANG, "")
    * </p>.
    */
   static public final String USER_CONTEXT_VAR_SYS_LANG =
      "User/SessionObject/sys_lang";

   /**
    * String constant for the name of the private session variable to store the
    * language string in th euser's session.key to get the user logged in
    * language This is used using the syntax:
    * <p>
    * request.getUserSession().getPrivateObject(
    * PSI18nUtils.USER_SESSION_OBJECT_SYS_LANG)
    * </p>.
    */
   static public final String USER_SESSION_OBJECT_SYS_LANG = "sys_lang";

   /**
    * String constant representing prefix for the lookup key for Content Editor
    * actions such as , New Version, Insert, Edit, Preview etc..
    */
   static public final String PSX_CE_ACTION =
      "psx" + LOOKUP_KEY_SEPARATOR +
      "ce" + LOOKUP_KEY_SEPARATOR + "action";

   /**
    * String constant representing prefix for translation key for workflow
    * transitions
    */
   static public final String PSX_WORKFLOW_TRANSITION =
      "psx" + LOOKUP_KEY_SEPARATOR +
      "workflow" + LOOKUP_KEY_SEPARATOR + "transition";

   /**
    * String constant representing the language US English.
    */
   static public final String LANG_EN_US = "en-us";

   /**
    * Default language used in the process of i18n.
    */
   static public final String DEFAULT_LANG = LANG_EN_US;

   /**
    * main method for testing
    * @param args
    */
   @SuppressWarnings("unchecked")
   static public void main(String[] args)
   {
      ArrayList list = new ArrayList();
      list.add("psx.workflow.state");
      list.add("");
      
      makeLookupKey(list);
      Date date = new Date();
      Locale locale = new Locale("en", "us");

      DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.DEFAULT,
         DateFormat.DEFAULT, locale);

      String pattern = "At {2} on {1}, there was {3} on planet {0}.";
      String argList = "7|12:30 PM|07/23/2020|a disturbance in the Force";
   System.out.println(formatMessage(pattern, argList, "ja-jp"));
      try
      {
         System.out.println(formatDate(formatter.format(date), null, "en-us", null, "ja-jp"));
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }
   }
}
