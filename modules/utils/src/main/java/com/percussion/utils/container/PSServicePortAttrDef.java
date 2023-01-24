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
