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
import com.percussion.util.PSStringOperation;

/**
 * The PSSimpleJavaUdf_replace class replaces strings given by a user
 * defined function (UDF).
 *
 * @author     Jian Huang
 * @version    1.1
 * @since      1.1
 */
public class PSSimpleJavaUdf_replace extends PSSimpleJavaUdfExtension
   implements IPSFieldInputTransformer
{
   /* ************ IPSUdfProcessor Interface Implementation ************ */

   /**
    * Replaces every occurrence of params[1] in params[0] with the string in
    * params[2].
    *
    * @param      params         the parameter values to use in the UDF
    *
    * @param      request         the current request context
    *
    * @return                     The string after the substitution has been
    *                            completed. If params[1] or params[2] is null,
    *                            params[0] is returned w/o modification.
    *
    * @exception  PSConversionException
    *                            if params[0] is <code>null</code> or more than
    *                            3 argument are supplied.
    */
   public Object processUdf(Object[] params, IPSRequestContext request)
      throws PSConversionException
   {
      final int size = (params == null) ? 0 : params.length;

      if (size != 3){  // three parameters are required
         int errCode = 0;
         String arg0 = "expect 3 parameters, ";
         arg0 += String.valueOf(size) + " parameters were specified.";
         Object[] args = { arg0, "PSSimpleJavaUdf_replace/processUdf" };
         throw new PSConversionException(errCode, args);
      }

      Object o1 = params[0];
      Object o2 = params[1];
      Object o3 = params[2];

      if (o1 == null){
         return null;
      }

      // Either a null "sub" or a null "rep" means no substitution
      if ((o2 == null) || (o3 == null)){
         try{
            return o1.toString();
         } catch (Exception e){
            int errCode = 0;
            Object[] args = { e.toString(), "PSSimpleJavaUdf_replace/processUdf" };
            throw new PSConversionException(errCode, args);
         }
      }

      // Nothing is null now, we start the normal procedure
      try{
         String strSrc = o1.toString();
         String strSub = o2.toString();
         String strRep = o3.toString();

         return PSStringOperation.replace(strSrc, strSub, strRep);
      } catch (Exception e){
         int errCode = 0;
         Object[] args = { e.toString(), "PSSimpleJavaUdf_replace/processUdf" };
         throw new PSConversionException(errCode, args);
      }
   }
}
