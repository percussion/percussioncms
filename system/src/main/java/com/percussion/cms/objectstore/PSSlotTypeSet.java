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

