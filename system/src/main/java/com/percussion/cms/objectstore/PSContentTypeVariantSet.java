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
import com.percussion.services.assembly.IPSAssemblyTemplate;
import org.w3c.dom.Element;

import java.util.Iterator;
import java.util.Set;

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
      super(PSContentTypeTemplate.class);
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
      super(items, PSContentTypeTemplate.class);
   }

   /**
    * Ctor that takes a list of assembly templates and creates the right 
    * deprecated structures
    * @param templates the list of templates, never <code>null</code>
    */
   public PSContentTypeVariantSet(Set<IPSAssemblyTemplate> templates) {
      super(PSContentTypeTemplate.class);
      if (templates == null)
      {
         throw new IllegalArgumentException("templates may not be null");
      }
      for(IPSAssemblyTemplate template : templates)
      {
         add(new PSContentTypeTemplate(template));
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
   public PSContentTypeTemplate getContentVariantByName(String variantName)
   {
      if(variantName==null || variantName.length() < 1)
         throw new IllegalArgumentException("variantName must not be null");
      
      Iterator iter = iterator();
      while (iter.hasNext())
      {
         PSContentTypeTemplate element = (PSContentTypeTemplate) iter.next();
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
   public PSContentTypeTemplate getContentVariantById(int variantId)
   {
      if (variantId < 0)
         throw new IllegalArgumentException(
         "variantId must be greater than 0");
      
      Iterator iter = iterator();
      while (iter.hasNext())
      {
         PSContentTypeTemplate element = (PSContentTypeTemplate) iter.next();
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

