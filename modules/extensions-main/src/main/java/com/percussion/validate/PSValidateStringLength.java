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
package com.percussion.validate;

import com.percussion.data.PSConversionException;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionParams;
import com.percussion.server.IPSRequestContext;

import java.io.File;

/**
 * Validate a string's content. This exit can check that a string is of the
 * right length range, and matches the passed pattern. Note that the range check
 * is an inclusive test.
 * <table>
 * <tr>
 * <th>Param</th>
 * <th>Type</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>value</td>
 * <td>java.lang.String</td>
 * <td>The value to check</td>
 * </tr>
 * <tr>
 * <td>min</td>
 * <td>java.lang.Number</td>
 * <td>The minimum value length, omit for no lower limit</td>
 * </tr>
 * <tr>
 * <td>max</td>
 * <td>java.lang.Number</td>
 * <td>The maximum value length, omit for no upper limit</td>
 * </tr>
 * </table>
 * 
 * @author dougrand
 * 
 */
public class PSValidateStringLength extends PSRangeValidator
{

   public Object processUdf(Object[] params, IPSRequestContext request)
         throws PSConversionException
   {
      String value;
      Number min, max;
      PSExtensionParams ep = new PSExtensionParams(params);

      value = ep.getStringParam(0, null, false);
      min = ep.getNumberParam(1, 0, false);
      max = ep.getNumberParam(2, Long.MAX_VALUE, false);

      if (value == null)
      {
         return false;
      }

      return checkRange(toDouble(min), new Double(value.length()), toDouble(max),
            true, true);

   }

   public void init(IPSExtensionDef def, File codeRoot)
         throws PSExtensionException
   {

   }

}
