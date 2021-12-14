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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Contains the configuration of a set of ports.
 */
public class PSPortConfiguration
{
   /**
    * Add a port configuration.
    * 
    * @param portConfig The config to add, may not be <code>null</code>.
    */
   public void addPortConfiguration(PSPortInfo portConfig)
   {
      if (portConfig == null)
         throw new IllegalArgumentException("portConfig may not be null");
      
      m_portList.add(portConfig);
   }
   
   /**
    * Get read-only list of port configurations.  Members may be modified, but
    * the list cannot be modified.
    *    
    * @return The list, never <code>null</code>.
    */
   public List<PSPortInfo> getPortConfigurations()
   {
      return Collections.unmodifiableList(m_portList);
   }

   /**
    * List of port configs, never <code>null</code>, initially empty.  See
    * {@link #addPortConfiguration(PSPortInfo)}.
    */
   private List<PSPortInfo> m_portList = new ArrayList<PSPortInfo>();
}

