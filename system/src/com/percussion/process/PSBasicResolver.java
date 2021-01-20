/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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




