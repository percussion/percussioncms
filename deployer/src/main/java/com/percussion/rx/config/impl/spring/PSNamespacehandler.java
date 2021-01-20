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
