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
import com.percussion.extension.PSSimpleJavaUdfExtension;
import com.percussion.server.IPSRequestContext;
import com.percussion.util.PSCalculation;

/**
 * The PSSimpleJavaUdf_add class adds the 2 supplied numbers and returns
 * the result.
 *
 * @author     Jian Huang
 * @version    1.1
 * @since      1.1
 */
public class PSSimpleJavaUdf_add extends PSSimpleJavaUdfExtension
{
   /* ************ IPSUdfProcessor Interface Implementation ************ */

   /**
    * Adds params[0] and params[1] and returns the result as a Double.
    *
    * @param params An array of 2 non-null elements. Any numeric type is allowed,
    * as well as String, PSTextLiteral and PSNumericLiteral. Non numeric types are
    * parsed and converted to numbers.
    *
    * @param      request         the current request context
    *
    * @return The result of summing params[0] and params[1], as a Double. If both
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
         Object[] args = { arg0, "PSSimpleJavaUdf_add/processUdf" };
         throw new PSConversionException(errCode, args);
      }

      Object o1 = params[0];
      Object o2 = params[1];

      return PSCalculation.add( o1, o2 );
   }
}
