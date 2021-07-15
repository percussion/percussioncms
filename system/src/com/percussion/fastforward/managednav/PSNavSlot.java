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
package com.percussion.fastforward.managednav;

import com.percussion.cms.objectstore.PSContentTypeVariant;
import com.percussion.cms.objectstore.PSContentTypeVariantSet;
import com.percussion.cms.objectstore.PSSlotType;
import com.percussion.cms.objectstore.PSSlotTypeContentTypeVariant;
import com.percussion.cms.objectstore.PSSlotTypeContentTypeVariantSet;
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

      PSContentTypeVariantSet navonVariants = config.getNavonVariants();
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
         PSContentTypeVariant navonVar = navonVariants
               .getContentVariantById(childVar.getVariantId());
         if (navonVar != null
               && childVar.getContentTypeId() == config.getNavonType())
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
   private Set m_slotVariantSet = new HashSet();

   /**
    * Reference to Log4j singleton object used to log any errors or debug info.
    */
   private Logger log = LogManager.getLogger(getClass());

}
