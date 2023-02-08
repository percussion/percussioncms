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

import com.percussion.cms.objectstore.PSSlotType;
import com.percussion.cms.objectstore.PSSlotTypeSet;
import com.percussion.server.IPSRequestContext;
import com.percussion.services.assembly.impl.nav.PSNavConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * a collection of PSNavSlot objects.
 * 
 * @author DavidBenua
 *  
 */
public class PSNavSlotSet
{
   /**
    * Constructor that takes the request context object. Just loads all slots to
    * a local cache.
    * 
    * @param req request context object, must not be <code>null</code>.
    * @throws PSNavException if it cannot load all slots to cache.
    */
   public PSNavSlotSet(IPSRequestContext req) throws PSNavException
   {
      m_allSlots = PSNavUtil.loadAllSlots(req);
   }

   /**
    * Add the slot to slot set by name. Slot object is looked up in all slots by
    * name and then added to the nav slot set.
    * 
    * @param slotName name of the slot to add, must not be <code>null</code>
    *           or empty.
    * @throws PSNavException
    */
   public void addSlotByName(String slotName) throws PSNavException
   {
      if (slotName == null || slotName.length() < 1)
      {
         throw new IllegalArgumentException("slotName must not be null or empty");
      }
      m_log.debug("Adding Slot" + slotName);

      PSSlotType ourSlot = m_allSlots.getSlotTypeByName(slotName);

      if (ourSlot == null)
      {
         m_log.warn("slot " + slotName + " not found");
         return;
      }

      PSNavSlot navSlot = new PSNavSlot(ourSlot);

      m_slotSet.add(navSlot);

   }

   /**
    * Get the specified Nav slot by name.
    * 
    * @param name the name of the slot, must not be <code>null</code> or empty.
    * @return the slot or <code>null</code> if not found.
    */
   public PSNavSlot getSlotByName(String name)
   {
      if (name == null || name.length() < 1)
      {
         throw new IllegalArgumentException("name must not be null or empty");
      }
      /*
       * note: the expected number of NavSlots in a system is very small ( <10)
       * so, this implementation uses a linear search. The overhead of
       * maintaining a separate map is not worth it.
       */
      Iterator it = m_slotSet.iterator();
      while (it.hasNext())
      {
         PSNavSlot ourSlot = (PSNavSlot) it.next();
         if (ourSlot.getSlotName().equals(name))
         {
            return ourSlot;
         }
      }
      return null;
   }

   /**
    * @return an unmodifiable iterator of nav slots, never <code>null</code>. 
    */
   public Iterator iterator()
   {
      return Collections.unmodifiableSet(m_slotSet).iterator();
   }

   /**
    * Get the specified slot by id.
    * 
    * @param id the id of the desired slot
    * @return the slot or <code>null</code> if no slot is found
    */
   public PSNavSlot getSlotById(int id)
   {
      /*
       * note: the expected number of NavSlots in a system is very small ( <10)
       * so, this implementation uses a linear search. The overhead of
       * maintaining a separate map is not worth it.
       */
      Iterator it = m_slotSet.iterator();
      while (it.hasNext())
      {
         PSNavSlot ourSlot = (PSNavSlot) it.next();
         if (ourSlot.getSlotId() == id)
         {
            return ourSlot;
         }
      }
      return null;
   }

   /**
    * Set of navslots, never <code>null</code>
    */
   Set m_slotSet = new HashSet();

   /**
    * Reference to nav config singleton object.
    */
   private PSNavConfig m_config = PSNavConfig.getInstance();

   /**
    * Set of all slots registered used as cache mechanism. nitialized in the
    * ctor, never <code>null</code> after that.
    */
   private PSSlotTypeSet m_allSlots = null;

   /**
    * Reference to Log4j singleton object used to log any errors or debug info.
    */
   private static final Logger m_log = LogManager.getLogger(PSNavSlotSet.class);
}
