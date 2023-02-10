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
