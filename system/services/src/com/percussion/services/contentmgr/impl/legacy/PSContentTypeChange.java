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
package com.percussion.services.contentmgr.impl.legacy;

import com.percussion.cms.objectstore.PSItemDefinition;

/**
 * Encapsulate a single item defintion to add or remove
 * 
 * @author dougrand
 */
public class PSContentTypeChange
{
   private PSItemDefinition   m_definition;
   private boolean            m_register;
   
   /**
    * Ctor
    * @param def definition to store, never <code>null</code>
    * @param reg if <code>true</code>, then this is a registration
    */
   public PSContentTypeChange(PSItemDefinition def, boolean reg)
   {
      if (def == null)
      {
         throw new IllegalArgumentException("def may not be null");
      }
      setDefinition(def);
      setRegister(reg);
   }
   
   /**
    * @return Returns the definition.
    */
   public PSItemDefinition getDefinition()
   {
      return m_definition;
   }
   /**
    * @param definition The definition to set.
    */
   private void setDefinition(PSItemDefinition definition)
   {
      m_definition = definition;
   }
   /**
    * @return Returns the register.
    */
   public boolean isRegister()
   {
      return m_register;
   }
   /**
    * @param register The register to set.
    */
   private void setRegister(boolean register)
   {
      m_register = register;
   }
   
   
}
