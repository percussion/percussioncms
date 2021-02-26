/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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
package com.percussion.extensions.translations;

import com.percussion.data.PSConversionException;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSFieldOutputTransformer;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionParams;
import com.percussion.server.IPSRequestContext;
import com.percussion.util.PSDataTypeConverter;

import java.io.File;
import java.text.ParseException;

/**
 * Takes a date string and transforms it to a new string using a supplied
 * format. See {@link java.text.SimpleDateFormat} for the supported format
 * patterns.
 * <table>
 * <tr>
 * <th>Param</th>
 * <th>Type</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>value</td>
 * <td>java.lang.String</td>
 * <td>The value to convert</td>
 * </tr>
 * <tr>
 * <td>format</td>
 * <td>java.lang.String</td>
 * <td>Any valid simple date format</td>
 * </tr>
 * </table>
 * 
 * @author dougrand
 * 
 */
public class PSFormatDate implements IPSFieldOutputTransformer
{
   public Object processUdf(Object[] params, 
         @SuppressWarnings("unused") IPSRequestContext request)
         throws PSConversionException
   {
      PSExtensionParams ep = new PSExtensionParams(params);
      String value = ep.getStringParam(0, null, true);
      String format = ep.getStringParam(1, null, true);

      String result;
      try
      {
         result = PSDataTypeConverter.transformDateString(value, null,
               format, true);
      }
      catch (ParseException e)
      {
         throw new PSConversionException(0, e.getLocalizedMessage());
      }

      return result;
   }

   @SuppressWarnings("unused")
   public void init(IPSExtensionDef def, File codeRoot)
         throws PSExtensionException
   {
      // nothing to initialize
   }
}
