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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/**
 * The parser to parse <code>&lt;psx:map></code> elements (assume 
 * <code>psx</code> is the prefix) defined in Spring bean file. This parser is
 * registered by {@link PSNamespacehandler}. The format of the element is: 
 *
 * <pre><code>
 *    &lt;!ELEMENT map EMPTY
 *    &lt;!ATTLIST map
 *       lookupKey   CDATA #REQUIRED
 * </code></pre>
 *
 * @author YuBingChen
 */
public class PSMapBeanDefinitionParser extends
      AbstractSingleBeanDefinitionParser
{
   @SuppressWarnings("unchecked")
   @Override
   protected Class getBeanClass(@SuppressWarnings("unused")
   Element element)
   {
      return HashMap.class;
   }
   
   @SuppressWarnings("unchecked")
   @Override
   protected void doParse(Element element, BeanDefinitionBuilder bean)
   {
      // this will never be null since the schema explicitly requires that a value be supplied
      String lookupKey = element.getAttribute("lookupKey");
      
      IPSBeanProperties pMgr = PSBeanPropertiesLocator.getBeanProperties();
      PSPair<Object, Boolean> result = PSConfigMapper
            .resolveSimplePlaceholder(lookupKey, pMgr.getProperties());
      Map map = null;
      if (result.getSecond())
      {
         // treat null value as an empty map
         if (result.getFirst() == null)
         {
            map = Collections.emptyMap();
         }
         else if (!(result.getFirst() instanceof Map))
         {
            ms_log
                  .warn("The \"Map\" type is expected for the replaced value of \""
                        + lookupKey
                        + "\". However, the type of the replaced value is: "
                        + map.getClass().getName());
         }
         else
         {
            map = (Map) result.getFirst();
         }
      }

      bean.addConstructorArgValue(map);
   }
   
   /**
    * Logger for this class.
    */
   private static final Logger ms_log = LogManager.getLogger("PSMapBeanDefinitionParser");
   

}
