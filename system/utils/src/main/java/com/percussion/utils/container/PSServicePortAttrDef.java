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
package com.percussion.utils.container;

import org.apache.commons.lang.StringUtils;

/**
 * Defines the mbean name and attribute to use to access a port value in a 
 * JBoss service config file.
 */
public class PSServicePortAttrDef
{
   /**
    * Construct a definition.
    * 
    * @param portName The name to use as the identifier of the port, may not be
    * <code>null</code> or empty. 
    * @param mbean The mbean name that has the port attribute, may not be 
    * <code>null</code> or empty.
    * @param attr The name of the port attribute, may not be <code>null</code> 
    * or empty.
    */
   public PSServicePortAttrDef(String portName, String mbean, String attr)
   {
      if (StringUtils.isBlank(portName))
         throw new IllegalArgumentException(
            "portName may not be null or empty");
      
      if (StringUtils.isBlank(mbean))
         throw new IllegalArgumentException("mbean may not be null or empty");
      
      if (StringUtils.isBlank(attr))
         throw new IllegalArgumentException("attr may not be null or empty");
      
      m_portName = portName;
      m_mbean = mbean;
      m_attr = attr;
   }
   
   /**
    * Get the attribute name supplied during construction.
    * 
    * @return The name, never <code>null</code> or empty.
    */
   public String getAttr()
   {
      return m_attr;
   }
   
   /**
    * Get the mbean name supplied during construction.
    * 
    * @return The name, never <code>null</code> or empty.
    */
   public String getMbean()
   {
      return m_mbean;
   }
   
   /**
    * Get the port name supplied during construction.
    * 
    * @return The name, never <code>null</code> or empty.
    */
   public String getPortName()
   {
      return m_portName;
   }
   
   /**
    * The port name supplied during construction, never <code>null</code> or 
    * empty.
    */
   String m_portName;

   /**
    * The mbean name supplied during construction, never <code>null</code> or 
    * empty.
    */
   String m_mbean;
   
   /**
    * The attr name supplied during construction, never <code>null</code> or 
    * empty.
    */
   String m_attr;

}
