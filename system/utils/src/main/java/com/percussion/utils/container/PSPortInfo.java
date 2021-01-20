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

