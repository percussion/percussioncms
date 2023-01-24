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

/**
 * The PSSimpleJavaUdf_literal class converts literals given by a user
 * defined function (UDF) to strings.
 *
 * @author     Jian Huang
 * @version    1.1
 * @since      1.1
 */
public class PSSimpleJavaUdf_literal extends PSSimpleJavaUdfExtension
{
   /* ************ IPSUdfProcessor Interface Implementation ************ */

   /**
    * Returns params[0] as a String object by performing a toString()
    * operation on it.
    *
    * @param      params         the parameter values to use in the UDF
    * @param      request         the current request context
    *
    * @return                     The string representation of the supplied
    *                            object, or <code>null</code> if <code>null
    *                            </code> is supplied.
    *
    * @exception  PSConversionException
    *                            if params[0] is <code>null</code> or contains
    *                            more than 1 parameter.
    */
   public Object processUdf(Object[] params, IPSRequestContext request)
      throws PSConversionException
   {
      final int size = (params == null) ? 0 : params.length;

      if (size != 1){  // one parameter is required
         int errCode = 0;
         String arg0 = "expect 1 parameter, ";
         arg0 += String.valueOf(size) + " parameters were specified.";
         Object[] args = { arg0, "PSSimpleJavaUdf_literal/processUdf" };
         throw new PSConversionException(errCode, args);
      }

      Object o = params[0];
      if (o == null)
         return null;
      return o.toString();
   }
}
