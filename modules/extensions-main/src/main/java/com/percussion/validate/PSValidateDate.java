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
package com.percussion.validate;

import com.percussion.data.PSConversionException;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionParams;
import com.percussion.server.IPSRequestContext;

import java.io.File;
import java.util.Date;

/**
 * Check a date argument for a range, supplied in the parameters. Note that the
 * range check is an inclusive test. The parameters to this udf consist of the
 * following: 
 * <table>
 * <tr>
 * <th>Param</th>
 * <th>Type</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>value</td>
 * <td>java.util.Date</td>
 * <td>The value to check</td>
 * </tr>
 * <tr>
 * <td>min</td>
 * <td>java.util.Date</td>
 * <td>The minimum value, omit for no lower limit</td>
 * </tr>
 * <tr>
 * <td>minlimitinclusive</td>
 * <td>java.util.Date</td>
 * <td>If supplied and the value is true then the minimum value is included in
 * the range</td>
 * </tr>
 * <tr>
 * <td>max</td>
 * <td>java.util.Date</td>
 * <td>The maximum value, omit for no upper limit</td>
 * </tr>
 * <tr>
 * <td>maxlimitinclusive</td>
 * <td>java.lang.Boolean</td>
 * <td>If supplied and the value is true then the maximum value is included in
 * the range</td>
 * </tr>
 * </table>
 * <p>
 * The values are compared as long time values.
 * 
 * @author dougrand
 * 
 */
public class PSValidateDate extends PSRangeValidator
{

   public Object processUdf(Object[] params, IPSRequestContext request)
         throws PSConversionException
   {
      Date value, min, max;
      PSExtensionParams ep = new PSExtensionParams(params);
      Boolean mininclusive, maxinclusive;

      value = ep.getDateParam(0, null, false);
      min = ep.getDateParam(1, new Date(0), false);
      mininclusive = ep.getBooleanParam(2, false, false);
      max = ep.getDateParam(3, new Date(Long.MAX_VALUE), false);
      maxinclusive = ep.getBooleanParam(4, false, false);

      if (value == null)
      {
         return false;
      }

      return checkRange(toDouble(min), toDouble(value), toDouble(max),
            mininclusive, maxinclusive);
   }

   /**
    * Extract the time as a double value
    * 
    * @param value the input value
    * @return the double value or <code>null</code> if the input is
    *         <code>null</code>
    */
   private Double toDouble(Date value)
   {
      if (value == null)
         return null;
      
      return new Double((double) value.getTime());
   }

   public void init(IPSExtensionDef def, File codeRoot)
         throws PSExtensionException
   {
      // 
   }

}
