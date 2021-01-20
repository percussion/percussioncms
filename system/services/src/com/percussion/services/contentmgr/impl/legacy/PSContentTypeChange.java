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
