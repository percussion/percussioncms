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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.commons.lang.StringUtils;

/**
 * Takes a string and encodes it so it is safe to use as a URL query param value.
 *
 * @author paulhoward
 */
public class PSSimpleJavaUdf_encodeForUrl extends PSSimpleJavaUdfExtension
{
   /**
    * See class description.
    * 
    * @param params Expect 1 param. Will do a <code>toString</code> on it and
    * then encode it for URL use. If <code>null</code> or empty, the empty
    * string is returned.
    * 
    *  @param request Not used.
    */
   public Object processUdf(Object[] params, 
         @SuppressWarnings("unused") IPSRequestContext request)
   {
      try
      {
         if (params == null || params.length == 0)
            return StringUtils.EMPTY;
         String s = params[0].toString();
         String result = URLEncoder.encode(s, "UTF-8");
         //we don't want to encode the path separators
         result = result.replace("%2F", "/");
         return result;
      }
      catch (UnsupportedEncodingException e)
      {
         //should never happen
         throw new RuntimeException(e);
      }
   }
}
