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
