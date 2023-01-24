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
package com.percussion.process;

import com.percussion.util.PSXMLDomUtil;
import org.w3c.dom.Element;

/**
 * Class to encapsulate a value and it's resolver.
 */
public class PSResolvableValue
{
   /**
    * Restores this value from its XML representation.
    * 
    * @param source The element to use, may not be null.  See 
    * {@link #toXml(Element)} for more info.
    */
   public PSResolvableValue(Element source)
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");
      
      m_value = PSXMLDomUtil.getAttributeTrimmed(source, ATTR_VALUE);
      m_resolver = PSXMLDomUtil.getAttributeTrimmed(source, ATTR_RESOLVER);
   }
   
   /**
    * Set the resolver and value on the supplied element.
    * 
    * @param el The element, may not be <code>null</code>.  Adds the following
    * attributes:
    * <pre><code>
    *    resolver CDATA #IMPLIED
    *    value CDATA #IMPLIED
    * </code></pre>
    */
   public void toXml(Element el)
   {
      if (el == null)
         throw new IllegalArgumentException("el may not be null");

      if (m_value != null)
         el.setAttribute(ATTR_VALUE, m_value);
      if (m_resolver != null)
         el.setAttribute(ATTR_RESOLVER, m_resolver);
   }
   
   /**
    * Get the value of this param.
    * 
    * @return The value, may be <code>null</code>, never empty. 
    */
   public String getValue()
   {
      return m_value;
   }
   
   /**
    * Get the resolver used to resolve this value.
    * 
    * @return The class name, may be <code>null</code>, never empty.
    */
   public String getResolver()
   {
      return m_resolver;
   }

   /**
    * The value, may be <code>null</code>, never empty or modified
    * after construction.
    */
   private String m_value;
   
   /**
    * Fully qualified class mame of the resolver used to resolve this value.  
    * May be <code>null</code>, never empty or modified after construction.
    */
   private String m_resolver;   
   
   // private xml constants   
   public static final String ATTR_RESOLVER = "resolver";
   public static final String ATTR_VALUE = "value";

}
