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
