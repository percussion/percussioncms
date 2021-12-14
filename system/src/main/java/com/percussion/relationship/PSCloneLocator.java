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
package com.percussion.relationship;

import com.percussion.design.objectstore.PSLocator;

/**
 * This class is extends {@link com.percussion.design.objectstore.PSLocator 
 * PSLocator} class to store an additional piece of information useful to 
 * relationship processing. One can specify/know if this locator is belongs 
 * to an existing clone or a newly created one. This is particularly useful 
 * when we want to process the relationships around the owner differently 
 * when clone of this owner is just created during current request versus 
 * it was already existing.
 *   
 * @author RammohanVangapalli
 *
 */
public class PSCloneLocator extends PSLocator
{
   /**
    * Ctor that takes the locator of the cloned item and flag indicating 
    * whether the clone was created just now or existing previously.
    * @param locator locator of the cloned item, must not be <code>null</code>.
    * @param isExisting flag to indicate if the clone was existing earlier 
    * (<code>true</code>) or just created (<code>false</code>).
    */
   public PSCloneLocator(PSLocator locator, boolean isExisting)
   {
      super(locator.getId(), locator.getRevision());
      m_isExisting = isExisting;
   }
   
   /**
    * Is this clone created new or existing.
    * @return <code>true</code> if this cloned item already existed, 
    * <code>false</code> if it is just created.
    */
   public boolean isExisting()
   {
      return m_isExisting;
   }

   /**
    * Is this cloned item existsed before or just created. Set in the ctor.
    */
   private boolean m_isExisting = false;

}
