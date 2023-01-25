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

package com.percussion.process;

import com.percussion.util.PSStringTemplate;

import java.util.Map;

/**
 * Class for resolving process parameter values. This class resolves the
 * value (specified using the "value" attribute) using the
 * <code>System.getProperty</code> method.
 */
public class PSEnvironmentResolver extends PSBasicResolver
{
   /**
    * See base class for more details.
    * 
    * @param ctx Unused.
    */
   public String resolve(String var, Map ctx)
      throws PSResolveException
   {
      //suppress eclipse warning
      if (null == ctx);
      
      return resolveTemplate(var, new PSStringTemplate.IPSTemplateDictionary()
      {
         public String lookup(String key)
         {
            return System.getProperty(key, "");
         }
      });
   }
}




