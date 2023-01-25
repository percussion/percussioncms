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
import com.percussion.util.PSStringTemplate.PSStringTemplateException;

import java.util.Map;

/**
 * Abstract base class for process parameter resolvers. Derived classes must
 * implement the <code>resolveString</code> method.
 */
public class PSBasicResolver
   implements IPSVariableResolver
{
   // see interface
   public String getValue(String value, Map ctx) throws PSResolveException
   {
      if (null == value)
         value = "";
         
      if (null == ctx)
      {
         throw new IllegalArgumentException("context cannot be null");
      }
      return resolve(value, ctx);
   }

   /**
    * Convenience method that calls {@link #resolveTemplate(String, 
    * PSStringTemplate.IPSTemplateDictionary) resolveTemplate(template, 
      new PSStringTemplate.PSMapDictionary(ctx))}.
      
    * @param template the actual string which needs resolution, may be 
    * <code>null</code> or empty
    *
    * @param ctx a {@link Map map}, contains data for executing the
    * process, may not be <code>null</code>. Each entry has <code>String</code>
    * keys which are case-sensitive to template variabled and values are
    * <code>toString</code>'d.
    */
   protected String resolve(String template, Map ctx)
      throws PSResolveException
   {
      if (null == ctx)
      {
         throw new IllegalArgumentException("context cannot be null");
      }
      return resolveTemplate(template, 
            new PSStringTemplate.PSMapDictionary(ctx));   
   }

   /**
    * This is the main resolver method. This method is called by the {@link
    * #getValue(Map) getValue} method to do the actual work. It uses the
    * {@link PSStringTemplate} class to do the work. See that class for 
    * details. The default delimiters are used.
    *
    * @param template the actual string which needs resolution, may be
    * <code>null</code> or empty
    *
    * @param ctx a {@link Map map}, contains data for executing the
    * process, may not be <code>null</code>
    *
    * @return The processed template, never <code>null</code>, may be empty.
    * 
    * @throws PSResolveException if any error occurs resolving the specified
    * string
    */
   protected String resolveTemplate(String template, 
         PSStringTemplate.IPSTemplateDictionary ctx)
      throws PSResolveException
   {
      if (null == ctx)
      {
         throw new IllegalArgumentException("context cannot be null");
      }

      if ((template == null) || (template.trim().length() < 1))
      {
         return "";
      }
      try
      {
         //    Expand variables
         PSStringTemplate templ = new PSStringTemplate(template);
         return templ.expand(ctx);
      }
      catch (PSStringTemplateException e)
      {
         throw new PSResolveException("Problem expanding variable: " +
            e.getMessage());
      }
   }
}




