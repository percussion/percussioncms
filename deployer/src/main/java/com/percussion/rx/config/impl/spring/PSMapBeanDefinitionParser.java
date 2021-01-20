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

import com.percussion.rx.config.IPSBeanProperties;
import com.percussion.rx.config.PSBeanPropertiesLocator;
import com.percussion.rx.config.impl.PSConfigMapper;
import com.percussion.utils.types.PSPair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
   private static Log ms_log = LogFactory.getLog("PSMapBeanDefinitionParser");
   

}
