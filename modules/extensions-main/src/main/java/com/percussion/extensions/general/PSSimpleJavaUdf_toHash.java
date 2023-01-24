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
 * The PSSimpleJavaUdf_toHash class returns a hashcode for the object
 * passed into the UDF.
 */
public class PSSimpleJavaUdf_toHash extends PSSimpleJavaUdfExtension
   implements IPSFieldInputTransformer
{
   /* ************ IPSUdfProcessor Interface Implementation ************ */

   /**
    * Returns a hash value for the supplied parameters by concatenating them
    * into a string and returning the hash value of the string.
    *
    * @param      params         A single parameter to be converted.
    *
    * @param      request        the current request context
    *
    * @return                    params[0] converted to a string representing the objects
    *                            hashcode, or <code>null</code> if <code>
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

      if (size < 1){
         int errCode = 0;
         String arg0 = "expects at least one parameter, ";
         arg0 += String.valueOf(size) + " parameters were specified.";
         Object[] args = { arg0, "PSSimpleJavaUdf_toHash/processUdf" };
         throw new PSConversionException(errCode, args);
      }
      //Delimiter for concatinating object strings      
      final char DELIM = '|';

      // concat all parameters together with a delimiter
      StringBuilder buf = new StringBuilder();
      for (int i = 0; i < params.length; i++)
      {
         Object o = params[i];
         if (o != null)
         {
            buf.append(o);
            buf.append(DELIM);
         }
      }
      if (buf.length() == 0)
         return "0";

      int lastDelimLoc = buf.length() - 1;
      //Remove the delimiter
      if (buf.charAt(lastDelimLoc) == DELIM)
         buf = buf.deleteCharAt(lastDelimLoc);

      int hash = buf.toString().hashCode();
      return Integer.toString(hash);
   }
}
