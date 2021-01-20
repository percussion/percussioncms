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
package com.percussion.services.utils.jexl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;

/**
 * Various convenient methods for Velocity Template.
 *
 * @author Yu-Bing Chen
 */
public class PSVelocityUtils
{
   /**
    * Not to expose the default constructor.
    */
   private PSVelocityUtils()
   {
   }
   
   /**
    * A resource loader for creating templates from runtime data
    * 
    * @author dougrand
    *
    */
   private static class StringResourceLoader extends ResourceLoader
   {
      private String m_templateSource = null;
      
      /**
       * Ctor
       * @param templateSource the source, never <code>null</code> or empty
       */
      public StringResourceLoader(String templateSource)
      {
         if (StringUtils.isBlank(templateSource))
         {
            throw new IllegalArgumentException("templateSource may not be null or empty");
         }
         m_templateSource = templateSource;
      }
      
      @Override
      public void init(@SuppressWarnings("unused") ExtendedProperties arg0)
      {
         // Ignore
      }

      @Override
      public InputStream getResourceStream(@SuppressWarnings("unused") String arg0)
      {
         try
         {
            return new ByteArrayInputStream(m_templateSource.getBytes("UTF-8"));
         }
         catch (UnsupportedEncodingException e)
         {
            throw new RuntimeException("Impossible - Java must support utf-8", e);
         }
      }

      @Override
      public boolean isSourceModified(@SuppressWarnings("unused") Resource arg0)
      {
         return false;
      }

      @Override
      public long getLastModified(@SuppressWarnings("unused") Resource arg0)
      {
         return 0;
      }
   }
      
   /**
    * Compile the specified Velocity Template.
    * 
    * @param content the template in question, may be <code>null</code> or empty.
    * @param name the name of the above template, may not be <code>null</code>
    *    or empty.
    * @param rs the runtime service of the Velocity Engine, never 
    *    <code>null</code>.
    *    
    * @return the compiled template, never <code>null</code>
    * 
    * @throws ResourceNotFoundException if failed to find resource.
    * @throws ParseErrorException if parse error occurred. 
    * @throws Exception if any other error occurred.
    */
   public static Template compileTemplate(String content, String name,
         RuntimeServices rs)
      throws ResourceNotFoundException, ParseErrorException, Exception
   {
      Template t = new Template();
      t.setRuntimeServices(rs);
      t.setResourceLoader(new StringResourceLoader(content));
      t.setEncoding("UTF-8");
      t.setName(name);
      t.process();

      return t;
   }
   
   /**
    * Get Velocity Context from a supplied bindings.
    * @param bindings the bindings, may not be <code>null</code>, may be empty.
    * @return the Velocity Context, never <code>null</code>.
    */
   public static VelocityContext getContext(Map<String, Object> bindings)
   {
      if (bindings == null)
         throw new IllegalArgumentException("bindings may not be null.");
      
      VelocityContext ctx = new VelocityContext();
      
      // Transfer bindings, removing the '$' from variable names
      // the '$' is understood by Velocity
      for (Map.Entry<String, Object> entry : bindings.entrySet())
      {
         String name = entry.getKey();
         if (name.startsWith("$"))
         {
            name = name.substring(1);
         }
         ctx.put(name, entry.getValue());
      }
      return ctx;
   }
   
}
