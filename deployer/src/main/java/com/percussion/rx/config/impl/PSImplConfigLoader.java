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
package com.percussion.rx.config.impl;

import com.percussion.rx.config.IPSConfigHandler;
import com.percussion.util.PSOsTool;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * Loads implementer's configuration file which is in Spring bean's format.
 * This class assumes the configuration file contains beans that implemented
 * {@link IPSConfigHandler}, provides API to retrieve these beans and ignore
 * the beans that are not instance of {@link IPSConfigHandler}
 *
 * @author YuBingChen
 */
public class PSImplConfigLoader
{
   /**
    * Creates an instance for a specified Spring bean configuration file.
    * @param configPath the path of the configuration file, it may be relative or
    *    absolute path. Never <code>null</code> or empty.
    */
   public PSImplConfigLoader(String configPath)
   {
      if (StringUtils.isBlank(configPath))
         throw new IllegalArgumentException(
               "configPath may not be null or emtpy.");

      String fixedFile = configPath;
      if (PSOsTool.isUnixPlatform())
      {
         //Bug in spring with absolute file paths.  Paths are repeated during
         //configuration which causes BeanDefinitionException.  Files cannot
         //be found.  Must prepend with '/' as workaround.
            fixedFile = "/" + fixedFile;
      }
      
      m_ctx = new FileSystemXmlApplicationContext(fixedFile);
   }

   /**
    * Gets all bean names with type of {@link IPSConfigHandler}.
    *
    * @return the bean names, may be empty, but not <code>null</code>.
    */
   public String[] getAllBeanNames()
   {
      return m_ctx.getBeanNamesForType(IPSConfigHandler.class);
   }

   /**
    * Gets the instance of the specified bean.
    *
    * @param name the name of the bean. It must be one of the element returned
    * from {@link #getAllBeanNames()}.
    *
    * @return the bean instance, never <code>null</code>.
    */
   public IPSConfigHandler getBean(String name)
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name cannot be null or empty.");

      Object bean = m_ctx.getBean(name);
      if (bean == null)
      {
         throw new IllegalArgumentException("Cannot find bean name: " + name);
      }

      if (!(bean instanceof IPSConfigHandler))
      {
         throw new IllegalArgumentException("Unexpected bean '" + name
               + "' type: " + bean.getClass().getName());
      }

      return (IPSConfigHandler) bean;
   }

   /**
    * Release all resources that are used by this loader.
    */
   public void close()
   {
      m_ctx.close();
   }

   /**
    * The context of the bean factory. Initialized by the constructor, never
    * null after that.
    */
   private FileSystemXmlApplicationContext m_ctx;
}
