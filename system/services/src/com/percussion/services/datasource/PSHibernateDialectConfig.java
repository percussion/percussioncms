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

package com.percussion.services.datasource;

import com.percussion.utils.container.IPSHibernateDialectConfig;
import com.percussion.utils.spring.IPSBeanConfig;
import com.percussion.utils.spring.PSSpringBeanUtils;
import com.percussion.utils.xml.PSInvalidXmlException;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Object representation of the hibernate dialect map configuration.  This 
 * specifies a set of mappings between JDBC driver names and their corresponding
 * default Hibernate dialect class name.  
 */
public class PSHibernateDialectConfig implements IPSBeanConfig, IPSHibernateDialectConfig
{
   /**
    * Set the dialect class for a driver.  If dialect is already mapped to
    * the supplied driver, it is replaced by the new dialect value.
    * 
    * @param driverName The name of the JDBC driver, may not be 
    * <code>null</code> or empty.
    * @param dialectClassName The dialect class name, may not be 
    * <code>null</code> or empty.
    */
   public void setDialect(String driverName, String dialectClassName)
   {
      if (StringUtils.isBlank(driverName))
         throw new IllegalArgumentException(
            "driverName may not be null or empty");
      
      if (StringUtils.isBlank(dialectClassName))
         throw new IllegalArgumentException(
            "dialectClassName may not be null or empty");
      
      m_sqlDialects.put(driverName, dialectClassName);
   }

   /**
    * Set the dialect classes for multiple drivers.  All current mappings are 
    * cleared and replaced with the supplied dialects.
    * 
    * @param dialects Map of dialects where key is the driver name, and value
    * is the dialect class name, may not be <code>null</code>, and keys and 
    * values may not be <code>null</code> or empty.  See 
    * {@link #setDialect(String, String)} for more info.
    */
   public void setDialects(Map<String, String> dialects)
   {
      if (dialects == null)
         throw new IllegalArgumentException("dialects may not be null");
      
      m_sqlDialects.clear();
      for (Map.Entry<String, String> entry : dialects.entrySet())
      {
         setDialect(entry.getKey(), entry.getValue());
      }
   }
   
   /**
    * Get a copy of the internal dialect map.  See {@link #setDialects(Map)}.
    * 
    * @return The map, never <code>null</code>.
    */
   public Map<String, String> getDialects()
   {
      return new HashMap<>(m_sqlDialects);
   }

   /**
    * Get the dialect class name mapped to the supplied JDBC driver name.
    * 
    * @param driverName The name of the JDBC driver, may not be 
    * <code>null</code> or empty.
    * 
    * @return The dialect, or <code>null</code> if no mapping is found.
    */
   public String getDialectClassName(String driverName)
   {
      return m_sqlDialects.get(driverName);
   }

   // see IPSBeanConfig 
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");
      
      Element root = PSSpringBeanUtils.createBeanRootElement(
         this, doc);
      
      PSSpringBeanUtils.addBeanProperty(root, DIALECTS_PROP_NAME, 
         m_sqlDialects);
      
      return root;
   }

   // see IPSBeanConfig 
   public void fromXml(Element source) throws PSInvalidXmlException
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");

      PSSpringBeanUtils.validateBeanRootElement(getBeanName(), getClassName(), 
         source);
      
      Element propEl = PSSpringBeanUtils.getNextPropertyElement(source, null, 
         DIALECTS_PROP_NAME);
      m_sqlDialects = PSSpringBeanUtils.getBeanPropertyValueMap(propEl);
   }

   // see IPSBeanConfig
   public String getBeanName()
   {
      return "sys_hibernateDialects";
   }

   // see IPSBeanConfig
   public String getClassName()
   {
      return getClass().getName();
   }
   
   /**
    * Map of jdbc driver name to hibernate sql dialect, never <code>null</code>,
    * may be empty. Modified by calls to {@link #setDialects(Map)}.
    */
   private Map<String, String> m_sqlDialects = new HashMap<>();
   
   /** 
    * Dialect property name
    */
   private static final String DIALECTS_PROP_NAME = "dialects";
}
