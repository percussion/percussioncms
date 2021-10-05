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

