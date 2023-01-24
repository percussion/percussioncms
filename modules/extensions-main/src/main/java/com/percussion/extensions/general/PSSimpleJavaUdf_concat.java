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
 * The PSSimpleJavaUdf_concat class concatenates two strings and returns the
 * result.
 *
 * @author     Jian Huang
 * @version    1.1
 * @since      1.1
 */
public class PSSimpleJavaUdf_concat extends PSSimpleJavaUdfExtension
{
   /* ************ IPSUdfProcessor Interface Implementation ************ */

   /**
    * Converts params[0] and params[1] to strings and appends params[1] to
    * params[0]. If either (or both) is <code>null</code>, the empty string is
    * substituted for the <code>null</code> parameter(s).
    *
    * @param      params         2 objects, which will be converted to strings
    *
    * @param      request         the current request context
    *
    * @return                     The concatenated string.
    *
    * @exception  PSConversionException
    *                            if params are not appropriately set
    */
   public Object processUdf(Object[] params, IPSRequestContext request)
      throws PSConversionException
   {
      final int size = (params == null) ? 0 : params.length;

      if (size != 2){  // two parameters are required
         int errCode = 0;
         String arg0 = "expect 2 parameters, ";
         arg0 += String.valueOf(size) + " parameters were specified.";
         Object[] args = { arg0, "PSSimpleJavaUdf_concat/processUdf" };
         throw new PSConversionException(errCode, args);
      }

      Object o1 = params[0];
      Object o2 = params[1];

      if ((o1 == null) && (o2 == null))
         return null;

      String str1 = "";
      String str2 = "";

      try{
         if (o1 != null)
            str1 = o1.toString();
         if (o2 != null)
            str2 = o2.toString();

         return (str1 + str2);
      } catch (Exception e){
         int errCode = 0;
         Object[] args = { e.toString(), "PSSimpleJavaUdf_concat/processUdf" };
         throw new PSConversionException(errCode, args);
      }
   }
}
