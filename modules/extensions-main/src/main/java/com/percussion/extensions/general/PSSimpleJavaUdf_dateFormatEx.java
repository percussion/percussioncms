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
 * The PSSimpleJavaUdf_dateFormatEx class formats a date according to a user
 * supplied pattern.
 * <p>
 * <strong>Time Format Syntax:</strong>
 * <p>
 * To specify the time format use a <em>time pattern</em> string.
 * In this pattern, all ASCII letters are reserved as pattern letters,
 * which are defined as the following:
 * <blockquote>
 * <pre>
 * Symbol   Meaning                 Presentation        Example
 * ------   -------                 ------------        -------
 * G        era designator          (Text)              AD
 * y        year                    (Number)            1996
 * M        month in year           (Text & Number)     July & 07
 * d        day in month            (Number)            10
 * h        hour in am/pm (1~12)    (Number)            12
 * H        hour in day (0~23)      (Number)            0
 * m        minute in hour          (Number)            30
 * s        second in minute        (Number)            55
 * S        millisecond             (Number)            978
 * E        day in week             (Text)              Tuesday
 * D        day in year             (Number)            189
 * F        day of week in month    (Number)            2 (2nd Wed in July)
 * w        week in year            (Number)            27
 * W        week in month           (Number)            2
 * a        am/pm marker            (Text)              PM
 * k        hour in day (1~24)      (Number)            24
 * K        hour in am/pm (0~11)    (Number)            0
 * z        time zone               (Text)              Pacific Standard Time
 * '        escape for text         (Delimiter)
 * ''       single quote            (Literal)           '
 * </pre>
 * </blockquote>
 * The count of pattern letters determine the format.
 * <p>
 * <strong>(Text)</strong>: 4 or more pattern letters--use full form,
 * &lt; 4--use short or abbreviated form if one exists.
 * <p>
 * <strong>(Number)</strong>: the minimum number of digits. Shorter
 * numbers are zero-padded to this amount. Year is handled specially;
 * that is, if the count of 'y' is 2, the Year will be truncated to 2 digits.
 * <p>
 * <strong>(Text & Number)</strong>: 3 or over, use text, otherwise use number.

 * <p>
 * Any characters in the pattern that are not in the ranges of ['a'..'z']
 * and ['A'..'Z'] will be treated as quoted text. For instance, characters
 * like ':', '.', ' ', '#' and '@' will appear in the resulting time text
 * even they are not embraced within single quotes.
 * <p>
 * A pattern containing any invalid pattern letter will result in a thrown
 * exception during formatting or parsing.
 *
 * <p>
 * <strong>Examples Using the US Locale:</strong>
 * <blockquote>
 * <pre>
 * Format Pattern                         Result
 * --------------                         -------
 * "yyyy.MM.dd G 'at' hh:mm:ss z"    ->>  1996.07.10 AD at 15:08:56 PDT
 * "EEE, MMM d, ''yy"                ->>  Wed, July 10, '96
 * "h:mm a"                          ->>  12:08 PM
 * "hh 'o''clock' a, zzzz"           ->>  12 o'clock PM, Pacific Daylight Time
 * "K:mm a, z"                       ->>  0:00 PM, PST
 * "yyyyy.MMMMM.dd GGG hh:mm aaa"    ->>  1996.July.10 AD 12:08 PM
 * </pre>
 * </blockquote>
 *
 * @since      2.0
 */
public class PSSimpleJavaUdf_dateFormatEx extends PSSimpleJavaUdfExtension
{
   /* ************ IPSUdfProcessor Interface Implementation ************ */

   /**
    * Formats a date (param[1]), according to the output pattern (param[0]) and
    * returns it as a String.
    * If the input pattern (param[2]) is specified it is used to parse the input
    * date (only if specified as String). If no input pattern is provided, the
    * defaults (see PSDataConverter.java) are used.
    *
    * @param params The parameter values to use in the UDF. Three, values
    * are expected. All parameters can be any Object. If a backend date is supplied,
    * it will be used directly. Any other object will be converted to a string
    * and then parsed into a Date object. The third parameter is optional. If
    * specified it must containing a formatting pattern, as described in this
    * class&apos; description. If omitted the defaults are used.
    * If <code>null</code> is supplied for a param, the default value shown
    * in the following table will be used.
    * <table border="1">
    *    <tr>
    *       <th>Param#</th><th>Required?</th><th>Description</th><th>Default value</th>
    *    </tr>
    *    <tr>
    *       <td>0</td> <td>no</td> <td>The output format pattern</td> <td>yyyy/mm/dd hh:mm:ss</td>
    *    </tr>
    *    <tr>
    *       <td>1</td> <td>no</td> <td>Date to format. Any object type</td> <td>Current date/time</td>
    *    </tr>
    *    <tr>
    *       <td>2</td> <td>no</td> <td>The input format pattern</td> <td>A predefined pattern array (see PSDataConverter)</td>
    *    </tr>
    *    <tr>
    *       <td>3</td>  <td>no</td> <td>Whether to return null on empty or null input</td>  <td>false</td>
    *    </tr>
    * </table>
    *
    * @param      request         the current request context
    *
    * @return A String that contains the date formatted as requested, or null if
    * either of the supplied parameters is null. A missing param is not
    * considered a null param.
    *
    * @exception  PSConversionException If the supplied params are not correct
    * or any parsing or formatting errors occur. The text will indicate the problem.
    */
   public Object processUdf(Object[] params, 
         @SuppressWarnings("unused") IPSRequestContext request)
      throws PSConversionException
   {
      PSExtensionParams ep = new PSExtensionParams(params);
      String outputFormat = ep.getStringParam(0, "yyyy/MM/dd HH:mm:ss", false);
      Object value = ep.getUncheckedParam(1);
      String inputFormat = ep.getStringParam(2, null, false);
      boolean returnNullForEmpty = ep.getBooleanParam(3, false, false);
      if (value == null || StringUtils.isBlank(value.toString()))
      {
         if (returnNullForEmpty)
            return null;
         value = new Date();
      }
      try
      {
         return PSDataTypeConverter.transformDateString(value, inputFormat,
               outputFormat, true);
      }
      catch (ParseException e)
      {
         //todo i18n
         throw new PSConversionException(0, e.getLocalizedMessage());
      }
   }
}
