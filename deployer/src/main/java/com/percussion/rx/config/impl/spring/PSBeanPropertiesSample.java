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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.Map;

/**
 * A sample bean used to demonstrate how to use the custom XML bean to retrieve
 * data from meta-data service. An example is defined in 
 * <code>user-beans.xml</code>. 
 *
 * @author YuBingChen
 */
public class PSBeanPropertiesSample
{
   /**
    * Returns the string property, which may set by 
    * {@link #setStringValue(String)}.
    * 
    * @return the string. It may be <code>null</code> or empty.
    */
   public String getStringValue()
   {
      return m_stringValue;
   }
   
   /**
    * Sets string property.
    *  
    * @param stringValue the new string, it may be <code>null</code> or empty.
    */
   public void setStringValue(String stringValue)
   {
      ms_log.info("setStringValue(): "
            + (stringValue == null ? "null" : stringValue));
         
      m_stringValue = stringValue;
   }
   
   /**
    * Gets the integer property.
    * 
    * @return the integer.
    */
   public int getIntValue()
   {
      return m_intValue;
   }

   /**
    * Sets the integer property.
    * 
    * @param intValue the new integer.
    */
   public void setIntValue(int intValue)
   {
      ms_log.info("setIntValue(): " + intValue);

      m_intValue = intValue;
   }
   
   /**
    * Gets the list property.
    * 
    * @return the list, it may be <code>null</code> or empty.
    */
   @SuppressWarnings("unchecked")
   public List getListValue()
   {
      return m_listValue;
   }

   /**
    * Sets the list property.
    * 
    * @param listValue the new list property, it may be <code>null</code> or
    * empty.
    */
   @SuppressWarnings({ "unchecked", "unchecked" })
   public void setListValue(List listValue)
   {
      ms_log.info("setListValue(): "
            + (listValue == null ? "null" : listValue.toString()));

      m_listValue = listValue;
   }

   /**
    * Gets the map property.
    * 
    * @return the map, may be <code>null</code> or empty.
    */
   @SuppressWarnings("unchecked")
   public Map getMapValue()
   {
      return m_mapValue;
   }
   
   /**
    * Sets the map property.
    * 
    * @param mapValue the new map property, it may be <code>null</code> or
    * empty.
    */
   @SuppressWarnings("unchecked")
   public void setMapValue(Map mapValue)
   {
      ms_log.info("setMapValue(): "
            + (mapValue == null ? "null" : mapValue.toString()));

      m_mapValue = mapValue;
   }

   /**
    * The integer property.
    */
   private int m_intValue;

   /**
    * The string property. It may be <code>null</code> or empty.
    */
   private String m_stringValue;
   
   /**
    * The list property. It may be <code>null</code> or empty.
    */
   @SuppressWarnings("unchecked")
   private List m_listValue;
   
   /**
    * The map property. It may be <code>null</code> or empty.
    */
   @SuppressWarnings("unchecked")
   private Map m_mapValue;
   
   /**
    * Logger for this class.
    */
   private static Log ms_log = LogFactory.getLog("PSPropertyValueBeanSample");

   /**
    * The following can be used to find out the bean loading sequence.
    * 
public class PSPropertyValueBeanSample implements BeanPostProcessor
{

   public Object postProcessAfterInitialization(Object bean, String name)
      throws BeansException
   {
      System.out.println("AFTER Bean name = " + name);
      return bean;
   }

   public Object postProcessBeforeInitialization(Object bean, String name)
      throws BeansException
   {
      System.out.println("Before Bean name = " + name);
      return bean;         
   }
    */
}
