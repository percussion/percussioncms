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
package com.percussion.services.utils.jsf;

import org.apache.commons.lang.StringUtils;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

/**
 * This converter takes a string that is meant to be a fully qualified Rx path
 * and 'cleans it up.' It does this by making sure it has 2 leading slashes
 * (adding them if necessary), stripping off the trailing slash, trimming
 * leading/trailing whitespace and converting backslashes to forward slashes.
 * 
 * @author paulhoward
 */
public class PSNormalizePath implements Converter
{
   /**
    * Performs the transformations noted in the class description.
    * 
    * @param ctx Never used.
    * @param comp Never used.
    * @param value May be <code>null</code> or empty.
    * @return The normalized path. Matches the supplied value if
    * <code>null</code> or empty.
    */
   public Object getAsObject(
         @SuppressWarnings("unused") FacesContext ctx, 
         @SuppressWarnings("unused") UIComponent comp, 
         String value)
      throws ConverterException
   {
      if (StringUtils.isBlank(value))
         return value;
      String normalizedSlash = value.trim().replace('\\', '/');
      int slashes = 0;
      while (slashes < normalizedSlash.length()
            && normalizedSlash.charAt(slashes) == '/')
      {
         slashes++;
      }
      StringBuilder result = new StringBuilder();
      int start = 0;
      if (slashes > 2)
         start = slashes - 2;
      
      if (slashes == 1)
      {
         result.append("/");
      }
      else if (slashes == 0)
         result.append("//");
      int end = normalizedSlash.length();
      if (normalizedSlash.endsWith("/"))
         end--;
      result.append(normalizedSlash.substring(start, end));
      
      return result.toString();
   }

   /**
    * This is a no-op method since we don't need to clean up anything on the 
    * way out.
    */
   public String getAsString(
         @SuppressWarnings("unused") FacesContext ctx, 
         @SuppressWarnings("unused") UIComponent comp, 
         Object value)
      throws ConverterException
   {
      return value.toString();
   }
}
