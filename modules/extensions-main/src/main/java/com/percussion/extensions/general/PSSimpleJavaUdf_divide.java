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

package com.percussion.extensions.general;
 
import com.percussion.data.PSConversionException;
import com.percussion.extension.PSSimpleJavaUdfExtension;
import com.percussion.server.IPSRequestContext;
import com.percussion.util.PSCalculation;

/**
 * The PSSimpleJavaUdf_divide class divides 2 numbers and returns the result.
 * The first number should be the dividend and the second number should be the
 * divisor.
 *
 * @author     Jian Huang
 * @version    1.1
 * @since      1.1
 */
public class PSSimpleJavaUdf_divide extends PSSimpleJavaUdfExtension
{
   /* ************ IPSUdfProcessor Interface Implementation ************ */

   /**
    * Divides params[0] by params[1] and returns the result as a Double.
    *
    * @param params An array of 2 non-null elements. Any numeric type is allowed,
    * as well as String, PSTextLiteral and PSNumericLiteral. Non numeric types are
    * parsed and converted to numbers.
    *
    * @param      request         the current request context
    *
    * @return The result of dividing param[0] by param[1], as a Double. If both
    * are null, then null is returned.
    *
    * @exception  PSConversionException If 2 params are not supplied or the
    * supplied params are not supported or could not be converted to a numeric
    * type or 1 of the 2 params is null.
    *
    */
   public Object processUdf(Object[] params, IPSRequestContext request)
      throws PSConversionException
   {
      final int size = (params == null) ? 0 : params.length;

      if (size != 2){  // two parameters are required
         int errCode = 0;
         String arg0 = "expect 2 parameters, ";
         arg0 += String.valueOf(size) + " parameters were specified.";
         Object[] args = { arg0, "PSSimpleJavaUdf_divide/processUdf" };
         throw new PSConversionException(errCode, args);
      }

      Object o1 = params[0];
      Object o2 = params[1];

      return PSCalculation.divide(o1, o2);
   }
}
