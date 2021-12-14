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
