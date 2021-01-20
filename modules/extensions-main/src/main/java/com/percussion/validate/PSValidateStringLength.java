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
