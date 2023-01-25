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

import com.percussion.rx.config.IPSBeanProperties;
import com.percussion.rx.config.PSBeanPropertiesLocator;
import com.percussion.rx.config.impl.PSConfigMapper;
import com.percussion.utils.types.PSPair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The parser to parse <code>&lt;psx:list></code> elements (assume 
 * <code>psx</code> is the prefix) defined in Spring bean file. This parser is
 * registered by {@link PSNamespacehandler}. The format of the element is: 
 *
 * <pre><code>
 *    &lt;!ELEMENT list EMPTY
 *    &lt;!ATTLIST list
 *       lookupKey   CDATA #REQUIRED
 * </code></pre>
 *
 * @see PSNamespacehandler
 * @author YuBingChen
 */
public class PSListBeanDefinitionParser extends
      AbstractSingleBeanDefinitionParser
{
   @SuppressWarnings("unchecked")
   @Override
   protected Class getBeanClass(@SuppressWarnings("unused")
   Element element)
   {
      return ArrayList.class;
   }
   
   @SuppressWarnings("unchecked")
   @Override
   protected void doParse(Element element, BeanDefinitionBuilder bean)
   {
      String lookupKey = element.getAttribute("lookupKey");
      
      
      IPSBeanProperties pMgr = PSBeanPropertiesLocator.getBeanProperties();
      PSPair<Object, Boolean> result = PSConfigMapper
            .resolveSimplePlaceholder(lookupKey, pMgr.getProperties());
      List list = null;
      if (result.getSecond())
      {
         // treat null value as an empty list
         if (result.getFirst() == null)
         {
            list = Collections.emptyList();
         }
         else if (!(result.getFirst() instanceof List))
         {
            ms_log
                  .warn("The \"List\" type is expected for the replaced value of \""
                        + lookupKey
                        + "\". However, the type of the replaced value is: "
                        + list.getClass().getName());
         }
         else
         {
            list = (List) result.getFirst();
         }
      }

      bean.addConstructorArgValue(list);
   }
   
   /**
    * Logger for this class.
    */
   private static final Logger ms_log = LogManager.getLogger("PSListBeanDefinitionParser");
   
}
