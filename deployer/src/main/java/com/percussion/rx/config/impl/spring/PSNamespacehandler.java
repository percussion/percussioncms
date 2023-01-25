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
package com.percussion.rx.config.impl.spring;


import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * The NamespaceHandler used for registering custom XML elements specified
 * in Spring bean files, such as &lt;psx:list>, &lt;psx:map>, ...etc.
 * <p>
 * Note, the registered custom XML elements must be defined in 
 * com.percussion.rx.config.impl.spring.ConfigPropertyValue.xsd.
 * </p>
 * <p>
 * This class is referenced by <code>spring.handlers</code> file.
 * ConfigPropertyValue.xsd is referenced by <code>spring.schemas</code> file.
 * Both <code>spring.handlers</code> and <code>spring.schemas</code> files are 
 * placed under <code>META-INF</code> directory.
 * </p>
 * @author YuBingChen
 */
public class PSNamespacehandler extends NamespaceHandlerSupport
{
   /**
    * Register parsers for all custom elements.
    */
   public void init()
   {
      registerBeanDefinitionParser("list",
            new PSListBeanDefinitionParser());
      registerBeanDefinitionParser("map",
            new PSMapBeanDefinitionParser());
   }

}
