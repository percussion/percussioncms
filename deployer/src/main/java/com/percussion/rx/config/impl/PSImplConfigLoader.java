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
