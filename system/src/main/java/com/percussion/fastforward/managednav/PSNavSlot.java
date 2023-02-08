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
package com.percussion.fastforward.managednav;

import com.percussion.cms.IPSConstants;
import com.percussion.cms.objectstore.PSContentTypeTemplate;
import com.percussion.cms.objectstore.PSContentTypeVariantSet;
import com.percussion.cms.objectstore.PSSlotType;
import com.percussion.cms.objectstore.PSSlotTypeContentTypeVariant;
import com.percussion.cms.objectstore.PSSlotTypeContentTypeVariantSet;
import com.percussion.services.assembly.impl.nav.PSNavConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Navigation slot definition. Nav Slots are inserted in page variants in the
 * implemenation contnet types. This class is a wrapper for
 * <code>PSXlotType</code>
 * 
 * @author DavidBenua
 *  
 */
public class PSNavSlot
{
   /**
    * Constructor for the enclosed slot type.
    * 
    * @param slot the slot to encapsulate in this wrapper.
    * @throws PSNavException
    */
   public PSNavSlot(PSSlotType slot) throws PSNavException
   {
      m_slotDef = slot;
      PSNavConfig config = PSNavConfig.getInstance();

      PSContentTypeVariantSet navonVariants = config.getNavonTemplates();
      if (navonVariants == null)
      {
         log.debug("no navon variants defined");
         return;
      }

      PSSlotTypeContentTypeVariantSet childVars = m_slotDef
            .getSlotVariants();
      if (childVars == null)
      {
         log.debug("no child variants");
         return;
      }
      Iterator children = childVars.iterator();
      while (children.hasNext())
      { // loop through all allowed variants in this slot
         PSSlotTypeContentTypeVariant childVar = (PSSlotTypeContentTypeVariant) children
               .next();
         PSContentTypeTemplate navonVar = navonVariants
               .getContentVariantById(childVar.getVariantId());
         if (navonVar != null
               && config.getNavonTypeIds().contains(childVar.getContentTypeId()))
         { // we found a variant of type Navon
            m_slotVariantSet.add(navonVar); // add it to our collection
         }
      }
   }

   /**
    * returns an iterator of PSContentTypeVariant objects that represent the
    * Navon variants that are allowed in this slot. May be <code>EMPTY</code>
    * but never <code>null</code>. This iterator must never be used to modify
    * the child variants.
    * 
    * @return the allowed variants in this slot.
    */
   public Iterator getVariantIterator()
   {
      return Collections.unmodifiableSet(m_slotVariantSet).iterator();
   }

   /**
    * Gets the slot id.
    * 
    * @return the slot id
    */
   public int getSlotId()
   {
      return m_slotDef.getSlotId();
   }

   /**
    * Gets the slot name.
    * 
    * @return the slot name.
    */
   public String getSlotName()
   {
      return m_slotDef.getSlotName();
   }

   /**
    * The underlying slot
    */
   private PSSlotType m_slotDef;

   /**
    * Set of all navslot variants. Initialized in the ctor, never
    * <code>null</code>.
    */
   private Set<PSContentTypeTemplate> m_slotVariantSet = new HashSet<>();

   /**
    * Reference to Log4j singleton object used to log any errors or debug info.
    */
   private static final Logger log = LogManager.getLogger(IPSConstants.NAVIGATION_LOG);

}
