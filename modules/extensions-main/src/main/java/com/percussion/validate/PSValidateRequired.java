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
import com.percussion.extension.IPSFieldValidator;
import com.percussion.extension.PSExtensionParams;
import com.percussion.server.IPSRequestContext;

import java.io.File;

import org.apache.commons.lang.StringUtils;

/**
 * Check that the value argument exists
 * <table>
 * <tr>
 * <th>Param</th>
 * <th>Type</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>value</td>
 * <td>java.lang.Object</td>
 * <td>The value to check</td>
 * </tr>
 * </table>
 * 
 * @author dougrand
 */
public class PSValidateRequired implements IPSFieldValidator
{
   public Object processUdf(Object[] params,
         @SuppressWarnings("unused") IPSRequestContext request)
   {
      Object value;
      
      try
      {
         PSExtensionParams ep = new PSExtensionParams(params);
         
         value = ep.getUncheckedParam(0);
      }
      catch (PSConversionException e)
      {
         throw new IllegalArgumentException(e.getLocalizedMessage());
      }
      
      if(value==null || StringUtils.isBlank(value.toString()))
         return false;
      return true;
   }

   public void init(@SuppressWarnings("unused") IPSExtensionDef def,
         @SuppressWarnings("unused") File codeRoot)
   {
      //No initialization required for this UDF
   }

}
