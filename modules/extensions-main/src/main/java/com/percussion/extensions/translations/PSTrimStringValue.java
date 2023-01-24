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
import com.percussion.extension.IPSFieldInputTransformer;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionParams;
import com.percussion.server.IPSRequestContext;

import java.io.File;

import org.apache.commons.lang.StringUtils;

/**
 * Evaluate a JEXL expression for input. The arguments are:
 * <table>
 * <tr>
 * <th>Param</th>
 * <th>Type</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>str</td>
 * <td>java.lang.String</td>
 * <td>The value to trim</td>
 * </tr>
 * <tr>
 * <td>trim</td>
 * <td>java.lang.String</td>
 * <td>What to trim, start, end or both. Defaults to both.</td>
 * </tr>
 * </table>
 * 
 * @author dougrand
 */
public class PSTrimStringValue implements IPSFieldInputTransformer
{

   public Object processUdf(Object[] params, 
         @SuppressWarnings("unused") IPSRequestContext request)
         throws PSConversionException
   {
      PSExtensionParams ep = new PSExtensionParams(params);
      String value = ep.getStringParam(0, null, true);
      String trim = ep.getStringParam(1, "both", false);
      
      if (trim.equals("both"))
      {
         return value.trim();
      }
      else if (trim.equals("start"))
      {
         return StringUtils.stripStart(value, " ");
      }
      else if (trim.equals("end"))
      {
         return StringUtils.stripEnd(value, " ");
      }
      else
      {
         throw new IllegalArgumentException("Unknown trim value " + trim);
      }
   }

   @SuppressWarnings("unused")
   public void init(IPSExtensionDef def, File codeRoot)
         throws PSExtensionException
   {
      // 
   }

}
