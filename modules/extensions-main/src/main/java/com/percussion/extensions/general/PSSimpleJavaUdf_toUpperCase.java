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
import com.percussion.extension.IPSFieldInputTransformer;
import com.percussion.extension.PSSimpleJavaUdfExtension;
import com.percussion.server.IPSRequestContext;

/**
 * The PSSimpleJavaUdf_toUpperCase class converts strings given by a user
 * defined function (UDF) to upper case.
 *
 * @author     Jian Huang
 * @version    1.1
 * @since      1.1
 */
public class PSSimpleJavaUdf_toUpperCase extends PSSimpleJavaUdfExtension
   implements IPSFieldInputTransformer
{
   /* ************ IPSUdfProcessor Interface Implementation ************ */

   /**
    * Converts the supplied object to a string using the toString method and
    * converts every character to upper case.
    *
    * @param      params         A single parameter to be converted.
    *
    * @param      request         Not used.
    *
    * @return                     params[0] converted to a string and
    *                            upper-cased, or <code>null</code> if <code>
    *                            null</code> is supplied.
    *
    * @exception  PSConversionException
    *                            if params is <code>null</code> or more than
    *                            1 argument is supplied.
    */
   public Object processUdf(Object[] params, IPSRequestContext request)
      throws PSConversionException
   {
      final int size = (params == null) ? 0 : params.length;

      if (size != 1){
         int errCode = 0;
         String arg0 = "expect 1 parameter, ";
         arg0 += String.valueOf(size) + " parameters were specified.";
         Object[] args = { arg0, "PSSimpleJavaUdf_toUpperCase/processUdf" };
         throw new PSConversionException(errCode, args);
      }

      Object o = params[0];

      if (o == null)
         return null;
      return o.toString().toUpperCase();
   }
}
