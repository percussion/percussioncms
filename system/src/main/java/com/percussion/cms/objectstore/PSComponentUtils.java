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

import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The class that has static methods that are useful for the objectstore
 * classes.
 */
public class PSComponentUtils
{
   /**
    * Gets the value of the supplied attribute from the supplied element. If the
    * attribute does not exist, gets the first value in the list as default
    * value for the attribute. Uses case-sensitive comparison to get the
    * value.
    *
    * @param el the element whose attribute has to be obtained, may not be
    * <code>null</code>
    * @param attrib the attibute to get, may not be <code>null</code> or empty.
    * @param allowedValues the list of allowed values, may not be <code>null
    * </code> or empty.
    *
    * @return the attribute value if it exits, otherwise the default value for
    * the attribute.
    *
    * @throws IllegalArgumentException if any parameter is invalid.
    * @throws PSUnknownNodeTypeException if the attribute exists and its value
    * is not one of the allowed values.
    */
   public static String getEnumeratedAttribute(Element el, String attrib,
      List allowedValues) throws PSUnknownNodeTypeException
   {
      if(el == null)
         throw new IllegalArgumentException("el may not be null.");

      if(attrib == null || attrib.trim().length() == 0)
         throw new IllegalArgumentException("attrib may not be null or empty.");

      if(allowedValues == null || allowedValues.isEmpty())
         throw new IllegalArgumentException(
            "allowedValues may not be null or empty.");

      String value = el.getAttribute(attrib);

      if(value != null && value.trim().length() != 0 &&
         !allowedValues.contains(value))
      {
         Object[] args = { el.getTagName(), attrib,  value };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
      }

      if(value == null || value.trim().length() == 0)
         value = (String)allowedValues.get(0);

      return value;
   }

   /**
    * Gets the value of the supplied attribute of the supplied element.
    *
    * @param el the element whose attribute has to be obtained, may not be
    * <code>null</code>
    * @param attrib the attibute to get, may not be <code>null</code> or empty.
    *
    * @return the attribute value, never <code>null</code> or empty.
    *
    * @throws IllegalArgumentException if any parameter is invalid.
    * @throws PSUnknownNodeTypeException if the attribute is not found or it is
    * empty.
    */
   public static String getRequiredAttribute(Element el, String attrib)
      throws PSUnknownNodeTypeException
   {
      if(el == null)
         throw new IllegalArgumentException("el may not be null.");

      if(attrib == null || attrib.trim().length() == 0)
         throw new IllegalArgumentException("attrib may not be null or empty.");

      String value = el.getAttribute(attrib);

      if(value == null && value.trim().length() == 0)
      {
         Object[] args = { el.getTagName(), "null" };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
      }

      return value;
   }

   /**
    * Gets child element of the specified name from the supplied element.
    *
    * @param el the element whose child to get, may not be <code>null</code>
    * @param childName the tag name of the child element, may not be <code>null
    * </code> or empty.
    * @param required supply <code>true</code> if the element is required,
    * otherwise <code>false</code>
    *
    * @return the child element, may be <code>null</code>
    *
    * @throws IllegalArgumentException if any parameter is invalid.
    * @throws PSUnknownNodeTypeException if the child element is not found and
    * is required.
    */
   public static Element getChildElement(Element el, String childName,
      boolean required)
      throws PSUnknownNodeTypeException
   {
      if(el == null)
         throw new IllegalArgumentException("el may not be null.");

      if(childName == null || childName.trim().length() == 0)
         throw new IllegalArgumentException(
            "childName may not be null or empty.");

      Iterator list = getChildElements(el, childName);
      if(!list.hasNext() && required)
      {
         Object[] args = { el.getTagName(), "null" };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
      }

      Element child = null;
      if(list.hasNext())
         child = (Element)list.next();

      return child;
   }

   /**
    * Gets immediate child elements of the supplied element with specified child
    * name.
    *
    * @param element the parent element, may not be <code>null</code>
    * @param childName the name of the child, may not be <code>null</code> or
    * empty.
    *
    * @return the iterator over zero or more <code>Element</code>s, never <code>
    * null</code>
    *
    * @throws IllegalArgumentException if any parameter is invalid.
    */
   public static Iterator getChildElements(Element element, String childName)
   {
      if(element == null)
         throw new IllegalArgumentException("el may not be null.");

      if(childName == null || childName.trim().length() == 0)
         throw new IllegalArgumentException(
            "childName may not be null or empty.");

      List children = new ArrayList();
      NodeList list = element.getElementsByTagName(childName);
      int length = list.getLength();
      for (int i = 0; i < length; i++)
      {
         Node item = list.item(i);
         if(item.getParentNode() == element)
            children.add(item);
      }

      return children.iterator();
   }

   /**
    * The constant to indicate 'true' value in XML.
    */
   public static final String XML_BOOLEAN_TRUE = "true";

   /**
    * The constant to indicate 'false' value in XML.
    */
   public static final String XML_BOOLEAN_FALSE = "false";
}
