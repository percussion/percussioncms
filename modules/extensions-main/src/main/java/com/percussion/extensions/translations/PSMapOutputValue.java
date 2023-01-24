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
