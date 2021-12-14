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
import com.percussion.extension.IPSUdfProcessor;
import com.percussion.extension.PSSimpleJavaUdfExtension;
import com.percussion.server.IPSRequestContext;
import com.percussion.util.PSUrlUtils;

import java.util.HashMap;

/**
 * This class implements the UDF processor interface so it can be used as a
 * Rhythmyx function. See {@link #processUdf(Object[], IPSRequestContext)
 * processUdf} for a description.
 */
public class PSMakeLink extends PSSimpleJavaUdfExtension
   implements IPSUdfProcessor
{
   /**
    * Creates a URL from the supplied parameters and returns it. Upto 6 name/
    * value pairs may be specified for the arguments. For example,
    * if the following were supplied as arguments:
    * <ul>
    *   <li>base = query1.html</li>
    *   <li>param1 = city</li>
    *   <li>value1 = Boston</li>
    *   <li>param2 = state</li>
    *   <li>value2 = MA</li>
    * </ul>
    * then the following URL would be generated:
    *   <p>query1f.html?city=Boston&state=MA</p>
    *
    *   <p>Note: The base may contain parameters defined on it,
    *       in which case the supplied parameters will be appended right after
    *       the last parameter defined therein.
    *
    * @param params An array with upto 13 elements as defined below. The array
    * is processed from beginning to end. As soon as the first <code>null</code>
    * parameter is encountered (<code>null</code> values allowed), processing
    * of the parameters will stop.
    *
    * <table border="1">
    *   <tr><th>Param #</th><th>Description</th><th>Required?</th><tr>
    *   <tr>
    *     <td>1</td>
    *     <td>The URL except for the parameters.</td>
    *     <td>no</td>
    *   </tr>
    *   <tr>
    *     <td>2 * N</td>
    *     <td>The name of the Nth parameter</td>
    *     <td>no</td>
    *   </tr>
    *   <tr>
    *     <td>2 * N + 1</td>
    *     <td>The value of the Nth parameter</td>
    *     <td>no</td>
    *   </tr>
    * </table>
    *
    * @param request The current request context.
    *
    * @return The url-string created from the supplied base, parameters and
    *    values. If the resource is empty or <code>null</code>, an empty
    *    string will be returned.
    *
    * @throws PSConversionException if the url cannot be constructed.
    */
   public Object processUdf(Object[] params, IPSRequestContext request)
      throws PSConversionException
   {
      if ( null == params || params.length < 1 || null == params[0]
         || 0 == params[0].toString().trim().length())
      {
         return "";
      }

      String sourceUrl = params[0].toString().trim();

      // build params map
      HashMap paramMap = new HashMap();
      int paramMaxIndex = params.length - 1;
      for ( int paramIndex = 1;
         paramIndex < paramMaxIndex && null != params[paramIndex]
            && params[paramIndex].toString().trim().length() > 0;
         paramIndex+=2 )
      {

         int valIndex = paramIndex+1;
         Object o = params[valIndex];
         if (o != null)
            o = o.toString();
         paramMap.put(params[paramIndex].toString(), o);
      }

      // create the url
      String result = null;
      try
      {
         result = PSUrlUtils.createUrl(sourceUrl,
            paramMap.entrySet().iterator(), null);
      }
      catch (Throwable t)
      {
         throw new PSConversionException(0, t.toString());
      }

      return result;
   }
}
