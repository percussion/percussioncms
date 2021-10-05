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
import org.w3c.dom.Element;

import java.util.Iterator;

/**
 * This is variant slot collection class, which is a dependent
 * class of the PSContentTypeVariant. This wrapps up a relationship between
 * the parent CONTENTVARIANTS and the child RXSLOTCONTENT tables.
 */
public class PSSlotTypeContentTypeVariantSet extends PSDbComponentSet
{
   /**
    * Default constructor. See {@link PSDbComponentSet#PSDbComponentSet(Class)}
    * for more details.
    */
   public PSSlotTypeContentTypeVariantSet()
   {
      super(PSSlotTypeContentTypeVariant.class);
   }

   /**
    * Ctor for reserializing. See {@link
    * PSDbComponentList#PSDbComponentList(Element) base ctor} for more details.
    */
   public PSSlotTypeContentTypeVariantSet(Element src) throws PSUnknownNodeTypeException
   {
      super(src);
   }
   
   /**
    * Ctor that takes array of elements, each must represent one PSSlotTypeContentTypeVariant.
    * See {@link PSDbComponentSet#PSDbComponentSet(Element[], Class)}.
    */
   public PSSlotTypeContentTypeVariantSet(Element[] items) throws PSUnknownNodeTypeException
   {
      super(items, PSSlotTypeContentTypeVariant.class);
   }

   // see interface for description
   public String getNodeName()
   {
      return XML_NODE_NAME;
   }

   /**
    * Helper method to check if the specified variant can go into the slot 
    * this object is associated with.
    * @param variant varint to check, must not be <code>null</code>.
    * @return <code>true</code> if this slot allowes the specified variant, 
    * <code>false</code> otherwise.
    */   
   public boolean isVariantAllowed(PSContentTypeVariant variant)
   {
      if (variant == null)
         throw new IllegalArgumentException("variant must not be null");
      return isVariantAllowed(variant.getVariantId());
   }

   /**
    * Helper method to check if the a variant with specified  variantid can 
    * go into the slot this object is associated with.
    * @param variantid varintis of the variant to check.
    * @return <code>true</code> if this slot allowes the specified variant, 
    * <code>false</code> otherwise.
    */   
   public boolean isVariantAllowed(int variantid)
   {
      Iterator iter = iterator();
      while (iter.hasNext())
      {
         PSSlotTypeContentTypeVariant element = (PSSlotTypeContentTypeVariant) iter.next();
         if (element.getVariantId() == variantid)
            return true;
      }
      return false;
   }

   /**
    * Root element name for the xml representation of the object 
    */
   public static final String XML_NODE_NAME= "PSXSlotTypeContentTypeVariantSet";
}
