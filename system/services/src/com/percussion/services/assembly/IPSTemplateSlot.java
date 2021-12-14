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
package com.percussion.services.assembly;

import com.percussion.services.catalog.IPSCatalogItem;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.types.PSPair;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * A template may have one or more slots. Each slot is populated in
 * assembly by an object that implements the interface
 * {@link com.percussion.services.assembly.IPSSlotContentFinder}.
 * 
 * @author dougrand
 */
public interface IPSTemplateSlot extends IPSCatalogItem
{
   /**
    * Slot type defines the usage of the slot
    */
   enum SlotType {
      /**
       * A slot that is used as part of the layout of a page or snippet
       */
      REGULAR, 
      /**
       * Generally one of three predefined slots that represent inline links,
       * images and templates
       */
      INLINE;

      /**
       * Lookup value by ordinal
       * 
       * @param ordinal the ordinal
       * @return the matching enum value, or Page as a default
       */
      public static SlotType valueOf(int ordinal)
      {
         for (SlotType t : values())
         {
            if (t.ordinal() == ordinal)
            {
               return t;
            }
         }
         return REGULAR;
      }
   }

   /**
    * The name of the slot
    * 
    * @return the name, will never be <code>null</code>
    */
   String getName();
   
   /**
    * The label of the slot. If no label is currently set, the name is returned.
    * 
    * @return the label, will never be <code>null</code> or empty.
    */
   String getLabel();   

   /**
    * The description of the slot
    * 
    * @return the description, will never be <code>null</code>
    */
   String getDescription();

   /**
    * Get the type of this slot
    * 
    * @return the type
    */
   SlotType getSlottypeEnum();

   /**
    * @param description The description to set, may be <code>null</code> or
    *           empty
    */
   void setDescription(String description);

   /**
    * @param finder The finder to set, may be <code>null</code> or empty
    */
   void setFinderName(String finder);

   /**
    * @param name The name to set, may not be <code>null</code> or empty
    */
   void setName(String name);

   /**
    * @param label The label to set, may be <code>null</code> or empty, in 
    * which case, the name will be used as the label.
    */
   void setLabel(String label);
   
   /**
    * Set the type of the slot
    * 
    * @param type the type, never <code>null</code>
    */
   void setSlottype(SlotType type);

   /**
    * Is this slot a system slot?
    * 
    * @return <code>true</code> if the slot is a system slot
    */
   boolean isSystemSlot();

   /**
    * Set if this is a system slot
    * 
    * @param sslot the value
    */
   void setSystemSlot(boolean sslot);

   /**
    * The relationship name is deprecated in favor of assigning a slot content
    * finder with parameters that include the relationship name.
    * 
    * @return Returns the relationshipName.
    */
   String getRelationshipName();

   /**
    * Set a new relationship name
    * 
    * @param relationshipName The relationshipName to set, may be
    *           <code>null</code> or empty
    */
   void setRelationshipName(String relationshipName);

   /**
    * Get the finder that will fill the slot contents for this slot. The actual
    * finder instance will be looked up using the extensions manager. 
    * Note that PSO, development or a customer can
    * extend the available finders at any point by registering a new finder that
    * implements {@link IPSSlotContentFinder} with the extensions manager. 
    * The context should be "global/percussion/slotcontentfinder/".
    * 
    * @return the name of the finder, never <code>null</code> or empty
    */
   String getFinderName();

   /**
    * Get the arguments to pass to the finder instance's <code>find</code>
    * method. The arguments are dependent on the particular finder.
    * 
    * @return the a copy of the finder arguments, may be empty, but never
    *         <code>null</code>
    */
   Map<String, String> getFinderArguments();

   /**
    * Set the given argument to the given value.
    * 
    * @param name the name of the argument, never <code>null</code> or empty
    * @param value the value, never <code>null</code> or empty
    */
   void addFinderArgument(String name, String value);

   /**
    * Remove the given argument from the finder arguments
    * 
    * @param name the name of the argument, never <code>null</code> or empty
    */
   void removeFinderArgument(String name);

   /**
    * Set the new arguments, see {@link #getFinderArguments()} for the
    * semantic details.
    * 
    * @param arguments The new arguments to set, it may be <code>null</code>
    *   if wanting to empty the existing arguments.
    */
   void setFinderArguments(Map<String, String> arguments);

   /**
    * Get the collection of content type and templates that this slot is
    * associated with. The returned collection can be modified, but no change to
    * the underlying association will be made until the corresponding set method
    * is called with the new data.
    * <p>
    * The first element in each pair is the guid to the content type, the second
    * is the template's guid.
    * 
    * @return get the slotAssociations set, never <code>null</code>
    */
   Collection<PSPair<IPSGuid, IPSGuid>> getSlotAssociations();

   /**
    * Set the new slot associations, see {@link #getSlotAssociations()} for the
    * semantic details.
    * 
    * @param slotAssociations The slotAssociations to set never
    *           <code>null</code>.
    */
   void setSlotAssociations(
           Collection<PSPair<IPSGuid, IPSGuid>> slotAssociations);

   /**
    * Only getter relationship is set through IPSAssemblyTemplate

    */
   Set<IPSAssemblyTemplate> getSlotTemplates();

}
