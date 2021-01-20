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
