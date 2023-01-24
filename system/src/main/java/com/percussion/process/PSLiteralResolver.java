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

import java.util.Map;

/**
 * PSLiteralResolver is the default resolver to be used when no resolver is
 * specified using the "resolver" attribute. The resolver just returns the
 * value passed to it.
 */
public class PSLiteralResolver extends PSBasicResolver
{
   /**
    * Just returns the supplied template except if it was <code>null</code>,
    * in which case "" is returned.
    */   
   protected String resolve(String template, Map ctx)
      throws PSResolveException
   {
      if ((template == null))
      {
         template = "";
      }
      //suppress eclipse warning
      if (null == ctx);
      return template;
   }
}

