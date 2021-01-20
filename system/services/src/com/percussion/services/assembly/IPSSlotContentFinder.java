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
package com.percussion.services.assembly;

import java.util.Map;

/**
 * Slot finders calculate what items are related to a particular content item.
 * Each slot finder is responsible for filtering the returned list of assembly
 * items for the authtype passed into the find method. Information needed for a
 * particular use of a slot finder is passed to the
 * {@link #find(IPSAssemblyItem, IPSTemplateSlot, Map)} method
 * <p>
 * Slot finders are reusable across templates, they are referenced by the slots
 * associated with a template.
 * 
 * @author dougrand
 */
public interface IPSSlotContentFinder extends IPSContentFinder<IPSTemplateSlot>
{
   /**
    * The type of the slot content finder. Calculated slots are not editable.
    */
   enum Type {
      /**
       * This type means that the slot finder is used for related content. 
       * The slot is used in AA, but doesn't require incremental publishing.
       */
      ACTIVE_ASSEMBLY(true, false),
      /**
       * An autoslot is computed from a query. This means that items cannot
       * be directly assigned, but it needs to be republished always, hence
       * the second arg is <code>true</code>.
       */
      AUTOSLOT(false, true), 
      /**
       * A computed slot is current used for managed nav. Like an autoslot, 
       * content cannot be directly added. Unlike an autoslot, it does not 
       * require republishing on incremental.
       */
      COMPUTED(false, false);

      /**
       * Content finders that can be activated via the AA interface will set
       * this to <code>true</code> via the ctor
       */
      private boolean m_activateable;
      /**
       * If the content finder should trigger incremental publishing updates,
       * then this member will be <code>true</code>, set via the ctor
       */
      private boolean m_incrementallyPublished;
      
      /**
       * The ctor
       * 
       * @param activateable <code>true</code> if this content finder type can
       *           be activated via the AA interfaith
       * @param mustIncrementallyPublish <code>true</code> if this content
       *           finder type should trigger incremental publishing
       */
      Type(boolean activateable, boolean mustIncrementallyPublish)
      {
         m_activateable = activateable;
         m_incrementallyPublished = mustIncrementallyPublish;
         
      }
      
      /**
       * Does this type require that the active assembly interface show the
       * slot and items as activateable. If <code>false</code> the aa interface
       * will not show the items or slot as activateable.
       * @return <code>true</code> if the finder can be activated
       */
      public boolean isActivateable()
      {
         return m_activateable;
      }
      
      /**
       * Does this type require incremental publishing? If this is 
       * <code>true</code> then page templates that use slots that use this
       * finder, or use snippets that display slots using this finder will
       * always be published on incremental. In addition, parent items may 
       * be published on incremental.
       * @return <code>true</code> if this slot contains data that changes from
       * call to call and therefore always requires new publishing.
       */
      public boolean isMustIncrementallyPublish()
      {
         return m_incrementallyPublished;
      }
   }
   

   /**
    * The application resource to be used for a legacy auto slot
    */
   static final String PARAM_RESOURCE = "resource";
     
   /**
    * Specify a template name or id for use with the finder
    */
   static final String PARAM_TEMPLATE = "template";
   
   /**
    * Get the type of the slot content finder. Used in the UI to present
    * appropriate decoration where needed
    * 
    * @return the type of the slot content finder, this value is the ordinal of
    *         the enumerated type {@link Type}.
    */
   Type getType();
}
