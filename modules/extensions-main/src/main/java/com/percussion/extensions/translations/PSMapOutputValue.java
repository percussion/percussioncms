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
package com.percussion.extensions.translations;

import com.percussion.data.PSConversionException;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSFieldOutputTransformer;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionParams;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.IPSServerErrors;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * Map an input value using a map of name value pairs. The passed value is
 * matched against the values, and the matching key returned. <table>
 * <tr>
 * <th>Param</th>
 * <th>Type</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>value</td>
 * <td>java.lang.Object</td>
 * <td>The value to translate</td>
 * </tr>
 * <tr>
 * <td>map</td>
 * <td>java.lang.String</td>
 * <td>The map, encoded using URL encoding</td>
 * </tr>
 * </table>
 * 
 * @author dougrand
 * 
 */
public class PSMapOutputValue implements IPSFieldOutputTransformer
{

   public Object processUdf(Object[] params, IPSRequestContext request)
         throws PSConversionException
   {
      PSExtensionParams ep = new PSExtensionParams(params);
      String value = ep.getStringParam(0, null, false);
      if(value == null)
         return null;
      String urlmap = ep.getStringParam(1, null, true);
      String pairs[] = urlmap.split("&");
      try
      {
         for (int i = 0; i < pairs.length; i++)
         {
            String parts[] = pairs[i].split("=");
            String key = URLDecoder.decode(parts[1], "UTF8");
            if (value.equals(key))
            {
               String res = parts.length > 1 ? URLDecoder.decode(parts[0],
                     "UTF8") : "";
               return res;
            }
         }
      }
      catch (UnsupportedEncodingException e)
      {
         throw new PSConversionException(
               IPSServerErrors.UNEXPECTED_EXCEPTION_LOG,
               "Unsupported encoding", null);
      }

      return null;
   }

   public void init(IPSExtensionDef def, File codeRoot)
         throws PSExtensionException
   {
      // 
   }

}
