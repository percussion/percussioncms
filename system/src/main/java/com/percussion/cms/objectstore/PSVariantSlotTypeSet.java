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
package com.percussion.cms.objectstore;

import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.utils.guid.IPSGuid;
import org.w3c.dom.Element;

import java.util.Set;

/**
 * This is variant slot type collection class, which is a dependent
 * class of the PSSlotType. This wraps up a relationship between
 * the parent RXSLOTTYPE and the child RXVARIANTSLOTTYPE tables.
 * <p>
 * Physically this object represents a set of variants that are allowed 
 * to be put into a given slot. This object always refers to a specified 
 * slotid. 
 * @deprecated use the assembly service instead
 */
public class PSVariantSlotTypeSet extends PSDbComponentSet
{
   /**
    * Default constructor. See {@link PSDbComponentList#PSDbComponentList()}
    * for more details.
    */
   public PSVariantSlotTypeSet()
   {
      super(PSVariantSlotType.class);
   }

   /**
    * Ctor for reserializing. See {@link
    * PSDbComponentList#PSDbComponentList(Element) base ctor} for more details.
    */
   public PSVariantSlotTypeSet(Element src) throws PSUnknownNodeTypeException
   {
      super(src);
   }

   /**
    * Ctor for creation from assembly service data
    * @param guid guid for the variant, never <code>null</code>
    * @param slots the collection of related slots, never <code>null</code>
    */
   public PSVariantSlotTypeSet(IPSGuid guid, Set<IPSTemplateSlot> slots) {
      super(PSVariantSlotType.class);
      if (guid == null)
      {
         throw new IllegalArgumentException("variant guid may not be null");
      }
      if (slots == null)
      {
         throw new IllegalArgumentException("slots may not be null");
      }
      for(IPSTemplateSlot slot : slots)
      {
         add(new PSVariantSlotType(guid, slot));
      }
   }

   // see interface for description
   public String getNodeName()
   {
      return XML_NODE_NAME;
   }

   /**
    * Root element name for the xml representation of the object 
    */
   public static final String XML_NODE_NAME= "PSXVariantSlotTypeSet";
}
