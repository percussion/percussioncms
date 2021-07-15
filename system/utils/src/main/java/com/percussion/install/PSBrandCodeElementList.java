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

package com.percussion.install;

// java
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * This class stores an array of <code>PSBrandCodeElement</code> objects.
 */
public class PSBrandCodeElementList
{
   /**
    * Constructor.
    * @param sourceNode the Xml representation of this object, may not
    * be <code>null</code>
    * @param childElementName tag name of the child elements which will be
    * stored in this list, may not be <code>null</code> or empty.
    * @param childElementReqAttrNames array containing the names of required
    * attributes, may be <code>null</code> or empty array
    * @param childElementOptionalAttrNames array containing the names of
    * optional attributes, may be <code>null</code> or empty array
    * @throws IllegalArgumentException if sourceNode or childElementName is
    * <code>null</code> or if childElementName is empty or if any element of
    * childElementReqAttrNames or childElementOptionalAttrNames is
    * <code>null</code> or empty, if these arrays are not <code>null</code>.
    * @throws CodeException if any element in this list does not have all the
    * attributes defined as specified in the <code>reqAttrNames</code> array.
    */
   public PSBrandCodeElementList(Element sourceNode, String childElementName,
      String[] childElementReqAttrNames, String[] childElementOptionalAttrNames)
      throws CodeException
   {
      if ((childElementName == null) || (childElementName.trim().length() < 1))
         throw new IllegalArgumentException(
            "childElementName may not be null or empty");
      m_childElementName = childElementName;
      m_childElementReqAttrNames = childElementReqAttrNames;
      m_childElementOptionalAttrNames = childElementOptionalAttrNames;
      fromXml(sourceNode);
   }

   /**
    * Returns the element from the list having the specified value for
    * the specified attribute name.
    * If required is <code>true</code> and no matching element is found then
    * CodeException is thrown.
    * If required is <code>false</code> and no matching element is found then
    * <code>null</code> is returned.
    *
    * @param attrName the name of the attribute, may not be <code>null</code>
    * or empty
    * @param attrValue the value of the attribute, may not be
    * <code>null</code> or empty
    * @param required if <code>true</code> and no matching element is found
    * then CodeException is thrown. If <code>false</code> and no matching
    * element is found the <code>null</code> is returned.
    *
    * @return the element from the list having the specified value for
    * the specified attribute name, may be <code>null</code> if
    * required is <code>false</code>.
    * @throws CodeException if required is <code>true</code> and no matching
    * element is found
    * @throws IllegalArgumentException if attrName or attrValue is
    * <code>null</code> or empty
    */
   public PSBrandCodeElement getBrandCodeElement(
      String attrName, String attrValue, boolean required)
      throws CodeException
   {
      if ((attrName == null) || (attrName.trim().length() < 1))
         throw new IllegalArgumentException(
            "attrName may not be null or empty");

      if ((attrValue == null) || (attrValue.trim().length() < 1))
         throw new IllegalArgumentException(
            "attrValue may not be null or empty");

      PSBrandCodeElement ret = null;
      for (int i = 0; i < m_elementList.size(); i++)
      {
         PSBrandCodeElement bce = (PSBrandCodeElement)m_elementList.get(i);
         if (bce.hasAttributeWithValue(attrName, attrValue))
         {
            ret = bce;
            break;
         }
      }
      if ((ret == null) && (required))
      {
         throw new CodeException("No element found having attribute : " +
            attrName + " with value : " + attrValue);
      }
      return ret;
   }

   /**
    * Returns a list of attribute values for all the elements in this list.
    * If required is <code>true</code> and any element in this list does not have
    * all the specified attributes, then CodeException is thrown.
    * If required is <code>false</code> and any element in the list does not have
    * any specified attribute then <code>null</code> is used as value for the
    * attribute.
    *
    * @param attrNames the attribute whose value for all the elements in this
    * list is to be returned.
    * @param required <code>true</code> if all the attributes in attrNames
    * should exist for all the elements in this list.
    * @return the a list of attribute values for all the elements in this list,
    * never <code>null</code>
    * @throws CodeException if required is <code>true</code> and any element in
    * this list does not have all the specified attributes.
    * @throws IllegalArgumentException if attrName is <code>null</code> or
    * empty array.
    */
   public List getAttributeList(String[] attrNames, boolean required)
      throws CodeException
   {
      if ((attrNames == null) || (attrNames.length < 1))
         throw new IllegalArgumentException(
            "attrNames may not be null or empty");

      List attrValList = new ArrayList();
      int len = attrNames.length;
      for (int i = 0; i < m_elementList.size(); i++)
      {
         PSBrandCodeElement bce = (PSBrandCodeElement)m_elementList.get(i);
         String[] attrValues = new String[len];
         for (int j = 0; j < len; j++)
            attrValues[j] = bce.getAttributeValue(attrNames[j], required);
         attrValList.add(attrValues);
      }
      return attrValList;
   }

   /**
    * Returns a list of attribute values for all the elements in this list.
    * @param attrName the attribute whose value for all the elements in this
    * list is to be returned, may not be <code>null</code> or empty
    * @return the a list of attribute values for all the elements in this list,
    * never <code>null</code>
    * @throws CodeException if any element in this list does not have the
    * specified attribute
    * @throws IllegalArgumentException if attrName is <code>null</code> or empty
    */
   public List getAttributeList(String attrName)
      throws CodeException
   {
      if ((attrName == null) || (attrName.trim().length() < 1))
         throw new IllegalArgumentException(
            "attrName may not be null or empty");

      List attrValList = new ArrayList();
      for (int i = 0; i < m_elementList.size(); i++)
      {
         PSBrandCodeElement bce = (PSBrandCodeElement)m_elementList.get(i);
         attrValList.add(bce.getAttributeValue(attrName, true));
      }
      return attrValList;
   }

   /**
    * Restore this object from an Xml representation.
    * @param sourceNode reference of the element which should be wrapped by
    * this object
    * @throws IllegalArgumentException if sourceNode is <code>null</code>
    * @throws CodeException if the elements in this list does not have all
    * the required attributes defined.
    */
   public void fromXml(Element sourceNode)
      throws CodeException
   {
      if (sourceNode == null)
         throw new IllegalArgumentException("sourceNode may not be null");

      Element el = null;
      String str = null;

      m_name = sourceNode.getTagName();
      NodeList nl = sourceNode.getElementsByTagName(m_childElementName);
      if ((nl == null) || (nl.getLength() == 0))
      {
         throw new CodeException("Failed to find child element : " +
            m_childElementName + " under the parent element : " +
            m_name);
      }
      for (int i = 0; i < nl.getLength(); i++)
      {
         el = (Element)nl.item(i);
         m_elementList.add(new PSBrandCodeElement(el,
            m_childElementReqAttrNames, m_childElementOptionalAttrNames));
      }
   }

   /**
    * Serializes this object's state to Xml.
    * @param doc The document to use when creating elements, may not be
    * <code>null</code>
    * @return the element containing this object's state,
    * never <code>null</code>
    * @throws IllegalArgumentException if doc is <code>null</code>.
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");
      // create the root element
      Element   root = doc.createElement(m_name);
      for (int i = 0; i < m_elementList.size(); i++)
      {
         PSBrandCodeElement bce = (PSBrandCodeElement)m_elementList.get(i);
         root.appendChild(bce.toXml(doc));
      }
      return root;
   }

   /**
    * list for storing the <code>PSBrandCodeElement</code> objects,
    * initialized in the <code>fromXml</code> method, never empty after
    * initialization.
    */
   private List m_elementList = new ArrayList();

   /**
    * tag name of the element, initialized in the <code>fromXml</code> method,
    * never <code>null</code> or empty after initialization.
    */
   private String m_name = null;

   /**
    * tag name of the child elements, initialized in the constructor,
    * never <code>null</code> or empty after initialization.
    */
   private String m_childElementName = null;

   /**
    * array containing the names of required attributes of the child elements,
    * initialized in the constructor,
    * may be <code>null</code> or empty.
    */
   private String[] m_childElementReqAttrNames = null;

   /**
    * array containing the names of optional attributes of the child elements,
    * initialized in the constructor,
    * may be <code>null</code> or empty.
    */
   private String[] m_childElementOptionalAttrNames = null;

}

