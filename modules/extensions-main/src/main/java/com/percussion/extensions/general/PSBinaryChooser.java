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
import com.percussion.extension.IPSUdfProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.server.IPSRequestContext;


public class PSBinaryChooser extends PSDefaultExtension
   implements IPSUdfProcessor
{
   /**
    * Compares the first param to the 2nd param as Strings. If they are equal,
    * the 3rd param is returned, otherwise the 4th param is returned. The
    * comparison is performed case insensitive. If the first param is empty or
    * <code>null</code>, the 3rd param is returned w/o performing a compare.
    * <p>This was written to allow setting an attribute to different values
    * based on the data in a column.
    *
    * @param params The first one can be <code>null</code>, but the other 3
    *    must be non-<code>null</code>. toString is called on
    *    the first 2 before the comparison. 3 and 4 are returned as is.
    *
    * @param request Never <code>null</code>.
    *
    * @throws PSConversionException If 3, non-<code>null</code> params
    *    are not found.
    */
   public Object processUdf(Object[] params, IPSRequestContext request)
      throws PSConversionException
   {
      if (null == params || params.length != 4
            || params[1] == null || params[2] == null || params[3] == null)
      {
         throw new PSConversionException(0, "Need 4, non-null params");
      }

      if (params[0] == null || params[0].toString().trim().length() == 0
            || params[0].toString().equalsIgnoreCase(params[1].toString()))
      {
         return params[2];
      }
      else
         return params[3];
   }
}
