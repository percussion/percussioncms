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
   public boolean isVariantAllowed(PSContentTypeTemplate variant)
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
