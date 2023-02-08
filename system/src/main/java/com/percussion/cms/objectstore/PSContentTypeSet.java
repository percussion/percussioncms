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
  * represents a set of PSContentType objects.
 */
public class PSContentTypeSet extends PSDbComponentSet
{
   /**
    * Default constructor. See {@link PSDbComponentSet#PSDbComponentSet(Class)}
    * for more details.
    */
   @SuppressWarnings("unused")
   public PSContentTypeSet() throws PSCmsException
   {
      super(PSContentType.class);
   }

   /**
    * Ctor for reserializing. See {@link
    * PSDbComponentList#PSDbComponentList(Element) base ctor} for more details.
    */
   public PSContentTypeSet(Element src) throws PSUnknownNodeTypeException
   {
      super(src);
   }

   // see interface for description
   @Override
   public String getNodeName()
   {
      return XML_NODE_NAME;
   }
   
   /**
    * Find the content type by name.
    * @param name name of the content type to find, must not be 
    * <code>null</code> or empty.
    * @return matching content type if found. May be <code>null</code>.
    */
   public PSContentType getContentTypeByName(String name)
   {
      if(name==null || name.length() < 1)
         throw new IllegalArgumentException("name must not be null");
      
      Iterator iter = iterator();
      while (iter.hasNext())
      {
         PSContentType element = (PSContentType) iter.next();
         if(element.getName().equalsIgnoreCase(name))
            return element;
      }
      return null;
   }
   
   /**
    * Find the content type by content type id.
    * @param contentTypeId type id of the content type to find, must be > 0. 
    * @return matching content type if found. May be <code>null</code>.
    */
   public PSContentType getContentTypeById(int contentTypeId)
   {
      if (contentTypeId < 0)
         throw new IllegalArgumentException(
         "contentTypeId must be greater than 0");
      
      Iterator iter = iterator();
      while (iter.hasNext())
      {
         PSContentType element = (PSContentType) iter.next();
         if(element.getTypeId() == contentTypeId)
            return element;
      }
      return null;
   }
   
   /**
    * Ctor that takes array of elements, each must represent one PSContentType.
    * 
    * @see PSDbComponentSet#PSDbComponentSet(Element[], Class)
    */
   public PSContentTypeSet(Element[] items) throws PSUnknownNodeTypeException
   {
      super(items, PSContentType.class);
   }

   /**
    * Root element of the XML document representing this object;
    */
   public static final String XML_NODE_NAME= "PSXContentTypeSet";
}

