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
package com.percussion.utils.container;

import org.apache.commons.lang.StringUtils;

/**
 * Defines the configuration of single named port.
 */
public class PSPortInfo
{
   /**
    * Construct a port info.
    * 
    * @param name The port name, may not be <code>null</code> or empty.
    * @param port The port value.
    * @param label The label, may be <code>null</code> or empty in which case
    * the name is used.
    */
   public PSPortInfo(String name, int port, String label)
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name may not be null or empty");
      
      m_name = name;
      m_value = port;
      m_origvalue = port;
      
      m_label = StringUtils.isBlank(label) ? name : label;
   }
   
   /**
    * Get the name that identifies this port.
    * 
    * @return The name, never <code>null</code> or empty.
    */
   public String getName()
   {
      return m_name;
   }
   
   /**
    * Get the port value.
    * 
    * @return The value.
    */
   public int getValue()
   {
      return m_value;
   }
   
   /**
    * Get the label to use when displaying the port value.
    * 
    * @return The label, never <code>null</code> or empty.
    */
   public String getLabel()
   {
      return m_label;
   }
   
   /**
    * Set the value of the port.
    * 
    * @param port The port value.
    */
   public void setValue(int port)
   {
      m_value = port;
   }
   
   /**
    * Determine if this ports value has been modified.
    * 
    * @return <code>true</code> if it has, <code>false</code> otherwise.
    */
   public boolean isModified()
   {
      return (m_origvalue != m_value);
   }
   
   /**
    * Port name, never <code>null</code> or empty.
    */
   private String m_name;
   
   /**
    * Port value.
    */
   private int m_value;
   
   /**
    * Port label, never <code>null</code> or empty.
    */
   private String m_label;
   
   /**
    * Port value this object was constructed with.
    */
   private int m_origvalue;
}

