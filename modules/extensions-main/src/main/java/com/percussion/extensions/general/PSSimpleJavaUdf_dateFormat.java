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

package com.percussion.extensions.general;

import com.percussion.data.PSConversionException;
import com.percussion.extension.PSExtensionParams;
import com.percussion.extension.PSSimpleJavaUdfExtension;
import com.percussion.server.IPSRequestContext;
import com.percussion.util.PSDataTypeConverter;

import java.text.ParseException;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

/**
 * The PSSimpleJavaUdf_dateFormat class formats a date according to a user
 * supplied pattern.
 * <p>
 * <strong>Time Format Syntax:</strong>
 * <p>
 * To specify the time format use a <em>time pattern</em> string. In this
 * pattern, all ASCII letters are reserved as pattern letters, which are defined
 * as the following: <blockquote>
 * 
 * <pre>
 *  Symbol   Meaning                 Presentation        Example
 *  ------   -------                 ------------        -------
 *  G        era designator          (Text)              AD
 *  y        year                    (Number)            1996
 *  M        month in year           (Text &amp; Number)     July &amp; 07
 *  d        day in month            (Number)            10
 *  h        hour in am/pm (1&tilde;12)    (Number)            12
 *  H        hour in day (0&tilde;23)      (Number)            0
 *  m        minute in hour          (Number)            30
 *  s        second in minute        (Number)            55
 *  S        millisecond             (Number)            978
 *  E        day in week             (Text)              Tuesday
 *  D        day in year             (Number)            189
 *  F        day of week in month    (Number)            2 (2nd Wed in July)
 *  w        week in year            (Number)            27
 *  W        week in month           (Number)            2
 *  a        am/pm marker            (Text)              PM
 *  k        hour in day (1&tilde;24)      (Number)            24
 *  K        hour in am/pm (0&tilde;11)    (Number)            0
 *  z        time zone               (Text)              Pacific Standard Time
 *  '        escape for text         (Delimiter)
 *  ''       single quote            (Literal)           '
 * </pre>
 * 
 * </blockquote> The count of pattern letters determine the format.
 * <p>
 * <strong>(Text)</strong>: 4 or more pattern letters--use full form, &lt;
 * 4--use short or abbreviated form if one exists.
 * <p>
 * <strong>(Number)</strong>: the minimum number of digits. Shorter numbers are
 * zero-padded to this amount. Year is handled specially; that is, if the count
 * of 'y' is 2, the Year will be truncated to 2 digits.
 * <p>
 * <strong>(Text & Number)</strong>: 3 or over, use text, otherwise use
 * number.
 * 
 * <p>
 * Any characters in the pattern that are not in the ranges of ['a'..'z'] and
 * ['A'..'Z'] will be treated as quoted text. For instance, characters like ':',
 * '.', ' ', '#' and '@' will appear in the resulting time text even they are
 * not embraced within single quotes.
 * <p>
 * A pattern containing any invalid pattern letter will result in a thrown
 * exception during formatting or parsing.
 * 
 * <p>
 * <strong>Examples Using the US Locale:</strong> <blockquote>
 * 
 * <pre>
 *  Format Pattern                         Result
 *  --------------                         -------
 *  &quot;yyyy.MM.dd G 'at' hh:mm:ss z&quot;    -&gt;&gt;  1996.07.10 AD at 15:08:56 PDT
 *  &quot;EEE, MMM d, ''yy&quot;                -&gt;&gt;  Wed, July 10, '96
 *  &quot;h:mm a&quot;                          -&gt;&gt;  12:08 PM
 *  &quot;hh 'o''clock' a, zzzz&quot;           -&gt;&gt;  12 o'clock PM, Pacific Daylight Time
 *  &quot;K:mm a, z&quot;                       -&gt;&gt;  0:00 PM, PST
 *  &quot;yyyyy.MMMMM.dd GGG hh:mm aaa&quot;    -&gt;&gt;  1996.July.10 AD 12:08 PM
 * </pre>
 * 
 * </blockquote>
 * 
 * @author Jian Huang
 * @version 1.1
 * @since 1.1
 */
public class PSSimpleJavaUdf_dateFormat extends PSSimpleJavaUdfExtension
{
   /* ************ IPSUdfProcessor Interface Implementation ************ */

   /**
    * Formats a date (param[1]), according to a pattern (param[0]) and returns
    * it as a String.
    * 
    * @param params The parameter values to use in the UDF. Two, values are
    * expected. The first must be a String, PSLiteral or PSTextLiteral
    * containing a formatting pattern, as described in this class&apos;
    * description. The second can be any object. If a backend date is supplied,
    * it will be used directly. Any other object will be converted to a string
    * and then parsed into a Date object. If <code>null</code> is supplied for
    * a param, the default value shown in the following table will be used.
    * <table border="1">
    * <tr>
    * <th>Param#</th>
    * <th>Required?</th>
    * <th>Description</th>
    * <th>Default value</th>
    * </tr>
    * <tr>
    * <td>0</td>
    * <td>no</td>
    * <td>The output format pattern</td>
    * <td>yyyy/mm/dd hh:mm:ss</td>
    * </tr>
    * <tr>
    * <td>1</td>
    * <td>no</td>
    * <td>Date to format</td>
    * <td>Current date/time</td>
    * </tr>
    * <tr>
    * <td>2</td>
    * <td>no</td>
    * <td>Whether to return null on empty or null input</td>
    * <td>false</td>
    * </tr> * </table>
    * 
    * @param request the current request context
    * 
    * @return A String that contains the date formatted as requested, or null if
    * either of the supplied parameters is null. A missing param is not
    * considered a null param.
    * 
    * @exception PSConversionException If the supplied params are not correct or
    * any parsing or formatting errors occur. The text will indicate the
    * problem.
    */
   public Object processUdf(Object[] params, 
         @SuppressWarnings("unused") IPSRequestContext request)
      throws PSConversionException
   {
      PSExtensionParams ep = new PSExtensionParams(params);
      String outputFormat = ep.getStringParam(0, "yyyy/MM/dd HH:mm:ss", false);
      Object value = ep.getUncheckedParam(1);
      boolean returnNullForEmpty = ep.getBooleanParam(2, false, false);
      if (value == null || StringUtils.isBlank(value.toString()))
      {
         if (returnNullForEmpty)
            return null;
         value = new Date();
      }
      try
      {
         return PSDataTypeConverter.transformDateString(value, null,
               outputFormat, true);
      }
      catch (ParseException e)
      {
         //todo i18n
         throw new PSConversionException(0, e.getLocalizedMessage());
      }
   }
}
