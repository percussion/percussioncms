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
