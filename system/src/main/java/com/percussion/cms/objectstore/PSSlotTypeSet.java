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

import com.percussion.cms.PSCmsException;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import org.w3c.dom.Element;

import java.util.Iterator;

/**
 * This class is a thin wrapper of the PSDbComponentSet that
 * represents a set of PSSlotType objects.
 */
public class PSSlotTypeSet extends PSDbComponentSet
{
   /**
    * Default constructor. See {@link PSDbComponentSet#PSDbComponentSet(Class)}
    * for more details.
    */
   public PSSlotTypeSet() throws PSCmsException
   {
      super(PSSlotType.class);
   }

   /**
    * Ctor for reserializing.
    * See {@link PSDbComponentSet#PSDbComponentSet(Element) base ctor}.
    */
   public PSSlotTypeSet(Element src) throws PSUnknownNodeTypeException
   {
      super(src);
   }
   
   /**
    * Ctor that takes array of elements, each must represent one PSSlotType.
    * See {@link PSDbComponentSet#PSDbComponentSet(Element[], Class) base ctor}.
    */
   public PSSlotTypeSet(Element[] items) throws PSUnknownNodeTypeException
   {
      super(items, PSSlotType.class);
   }

   // see interface for description
   public String getNodeName()
   {
      return XML_NODE_NAME;
   }

   /**
    * Find the slot type by name.
    * @param slotName name of the slot type to find, must not be 
    * <code>null</code> or empty.
    * @return matching slot type if found. <code>null</code> if not found.
    */
   public PSSlotType getSlotTypeByName(String slotName)
   {
      if (slotName == null || slotName.length() < 1)
         throw new IllegalArgumentException("slotName must not be null");
      
      Iterator iter = iterator();
      while (iter.hasNext())
      {
         PSSlotType element = (PSSlotType) iter.next();
         if(element.getSlotName().equalsIgnoreCase(slotName))
            return element;
      }
      return null;
   }
   
   /**
    * Find the slot type by content type id.
    * @param slotId slot id of the slot type to find, must be > 0. 
    * @return matching slot type if found. <code>null</code> if not found.
    */
   public PSSlotType getSlotTypeById(int slotId)
   {
      if (slotId < 0)
         throw new IllegalArgumentException("slotId must be greater than 0");
      
      Iterator iter = iterator();
      while (iter.hasNext())
      {
         PSSlotType element = (PSSlotType) iter.next();
         if(element.getSlotId() == slotId)
            return element;
      }
      return null;
   }
   
   /**
    * Root element name for the xml representation of the object 
    */
   public static final String XML_NODE_NAME= "PSXSlotTypeSet";
}

