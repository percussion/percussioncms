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
import com.percussion.services.assembly.IPSAssemblyTemplate;

import java.util.Iterator;
import java.util.Set;

import org.w3c.dom.Element;

/**
 * This is content variant Set class, which is a dependent
 * class of the PSContentType. This wrapps up a relationship between
 * the parent CONTENTTYPES and the child CONTENTVARIANTS tables.
 * @deprecated Use the assembly service to load and manipulate variant
 * information
 */
public class PSContentTypeVariantSet extends PSDbComponentSet
{
   /**
    * Default constructor. See {@link PSDbComponentSet#PSDbComponentSet(Class)}
    * for more details.
    */
   @SuppressWarnings("unused")
   public PSContentTypeVariantSet() throws PSCmsException
   {
      super(PSContentTypeVariant.class);
   }

   /**
    * Ctor for reserializing. See {@link
    * PSDbComponentSet#PSDbComponentSet(Element) base ctor} for more details.
    */
   public PSContentTypeVariantSet(Element src) throws PSUnknownNodeTypeException
   {
      super(src);
   }
   
   /**
    * Ctor that takes array of elements, each must represent one 
    * PSContentTypeVariant.
    * 
    * @see PSDbComponentSet#PSDbComponentSet(Element[], Class)
    */
   public PSContentTypeVariantSet(Element[] items) throws PSUnknownNodeTypeException
   {
      super(items, PSContentTypeVariant.class);
   }

   /**
    * Ctor that takes a list of assembly templates and creates the right 
    * deprecated structures
    * @param templates the list of templates, never <code>null</code>
    */
   public PSContentTypeVariantSet(Set<IPSAssemblyTemplate> templates) {
      super(PSContentTypeVariant.class);
      if (templates == null)
      {
         throw new IllegalArgumentException("templates may not be null");
      }
      for(IPSAssemblyTemplate template : templates)
      {
         add(new PSContentTypeVariant(template));
      }
   }

   // see interface for description
   @Override
   public String getNodeName()
   {
      return XML_NODE_NAME;
   }

   /**
    * Find the variant by name.
    * @param variantName name of the variant to find, must not be 
    * <code>null</code> or empty.
    * @return matching variant if found. <code>null</code> if not found.
    */
   public PSContentTypeVariant getContentVariantByName(String variantName)
   {
      if(variantName==null || variantName.length() < 1)
         throw new IllegalArgumentException("variantName must not be null");
      
      Iterator iter = iterator();
      while (iter.hasNext())
      {
         PSContentTypeVariant element = (PSContentTypeVariant) iter.next();
         if(element.getName().equalsIgnoreCase(variantName))
            return element;
      }
      return null;
   }
   
   /**
    * Find the variant by variantid.
    * @param variantId of the varint to find, must be > 0. 
    * @return matching variant if found. <code>null</code> if not found.
    */
   public PSContentTypeVariant getContentVariantById(int variantId)
   {
      if (variantId < 0)
         throw new IllegalArgumentException(
         "variantId must be greater than 0");
      
      Iterator iter = iterator();
      while (iter.hasNext())
      {
         PSContentTypeVariant element = (PSContentTypeVariant) iter.next();
         if(element.getVariantId() == variantId)
            return element;
      }
      return null;
   }

   /**
    * XML node name for th object's xml representation.
    */
   public static final String XML_NODE_NAME= "PSXContentTypeVariantSet";
}

