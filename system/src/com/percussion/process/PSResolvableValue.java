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
