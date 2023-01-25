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
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionParams;
import com.percussion.server.IPSRequestContext;

import java.io.File;
import java.util.regex.Pattern;

public class PSValidateStringPattern implements IPSFieldValidator
{

   public Object processUdf(Object[] params, IPSRequestContext request) throws PSConversionException
   {
      String value, regex;
      PSExtensionParams ep = new PSExtensionParams(params);
      value = ep.getStringParam(0, null, false);
      regex = ep.getStringParam(1, null, true);
      
      if (value == null)
      {
         return false;
      }
      
      if (value == null || !Pattern.matches(regex,value))
      {
         return false;
      }

      return true;
   }

   public void init(IPSExtensionDef def, File codeRoot) throws PSExtensionException
   {
      //
   }

}
