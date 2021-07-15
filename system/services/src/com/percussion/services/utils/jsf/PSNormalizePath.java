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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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
