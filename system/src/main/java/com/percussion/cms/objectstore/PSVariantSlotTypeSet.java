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
