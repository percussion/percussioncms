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

