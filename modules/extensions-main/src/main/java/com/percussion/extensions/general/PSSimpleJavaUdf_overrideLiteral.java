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
 * This UDF converts the supplied parameter to a <code>String</code> and 
 * returns that or the override value supplied through the request parameters.
 * The override value is removed from the request once it was consumed.
 */
public class PSSimpleJavaUdf_overrideLiteral extends PSSimpleJavaUdfExtension
   implements IPSFieldInputTransformer
{
   /**
    * Returns the supplied literal (<code>params[0]</code>) as
    * <code>String</code> or the override value if an override parameter (<code>params[1]</code>)
    * is specified and was found on the supplied request. If found, the override
    * parameter is removed from the request.
    * 
    * @params[0] the object which will be returned as <code>String</code>,
    * required, may be <code>null</code> or empty.
    * @params[1] the request parameter name used to allow overrides through the
    * HTML request, optional, may be <code>null</code> or empty.
    * @see com.percussion.extension.IPSUdfProcessor#processUdf(Object[],
    * IPSRequestContext) for additional documentation.
    */
   public Object processUdf(Object[] params, IPSRequestContext request)
      throws PSConversionException
   {
      final int size = (params == null) ? 0 : params.length;

      if (size < 1)
      {
         int errCode = 0;
         String arg0 = "expect 1 parameter, ";
         arg0 += String.valueOf(size) + " parameters were specified.";
         Object[] args = { arg0, "PSSimpleJavaUdf_overrideLiteral/processUdf" };
         throw new PSConversionException(errCode, args);
      }
      
      /*
       * If the override parameter name was supplied and a value for that was
       * found on the current request, the override value will be returned.
       */
      if (size > 1 && params[1] != null)
      {
         String parameterName = params[1].toString().trim();
         String parameterValue = request.getParameter(parameterName);
         if (parameterValue != null)
         {
            request.removeParameter(parameterName);
            return parameterValue.trim();
         }
      }

      Object o = params[0];
      if (o == null)
         return null;
      
      return o.toString();
   }
}
