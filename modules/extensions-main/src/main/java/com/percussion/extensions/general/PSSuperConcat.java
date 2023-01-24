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
package com.percussion.extensions.general;

import com.percussion.extension.PSSimpleJavaUdfExtension;
import com.percussion.server.IPSRequestContext;


public class PSSuperConcat extends PSSimpleJavaUdfExtension
{
   public Object processUdf(Object[] params, IPSRequestContext request)
   {
      StringBuilder result = new StringBuilder(100);
      int parmCount = params.length;
      for(int i = 0; i < parmCount; i++)
      {
         if(null != params[i] && params[i].toString().trim().length() > 0)
         {
            result.append(params[i].toString());
         }
      }

      return result.toString();
   }
}
