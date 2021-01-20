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
package com.percussion.fastforward.managednav;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;

import com.percussion.cms.objectstore.PSSlotType;
import com.percussion.cms.objectstore.PSSlotTypeSet;
import com.percussion.server.IPSRequestContext;

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
   Logger m_log = Logger.getLogger(getClass());
}