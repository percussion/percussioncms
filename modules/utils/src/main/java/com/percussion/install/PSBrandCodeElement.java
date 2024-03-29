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

package com.percussion.install;

// java

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This class stores the tag name of the wrapped element and the name and value
 * of all the attribute of this element defined in the component map xml.
 */
public class PSBrandCodeElement
{
   /**
    * Constructor.
    * @param sourceNode the Xml representation of this object, may not
    * be <code>null</code>
    * @param reqAttrNames array containing the names of required attributes,
    * may be <code>null</code> or empty array
    * @param optionalAttrNames array containing the names of optional attributes,
    * may be <code>null</code> or empty array
    * @throws IllegalArgumentException if sourceNode is <code>null</code>
    * or if any element of reqAttrNames or optionalAttrNames is
    * <code>null</code> or empty, if these arrays are not <code>null</code>
    * @throws CodeException if the element does not have all the attributes
    * defined as specified in the <code>reqAttrNames</code> array.
    */
   public PSBrandCodeElement(Element sourceNode,
      String[] reqAttrNames, String[] optionalAttrNames)
      throws CodeException
   {
      m_reqAttrNames = reqAttrNames;
      m_optionalAttrNames = optionalAttrNames;
      if (reqAttrNames != null)
      {
         for (int i = 0; i < reqAttrNames.length; i++)
         {
            String reqAttrName = reqAttrNames[i];
            if ((reqAttrName == null) || (reqAttrName.trim().length() < 1))
               throw new IllegalArgumentException("reqAttrName may not be null or empty");
         }
      }
      if (optionalAttrNames != null)
      {
         for (int i = 0; i < optionalAttrNames.length; i++)
         {
            String optionalAttrName = optionalAttrNames[i];
            if ((optionalAttrName == null) || (optionalAttrName.trim().length() < 1))
               throw new IllegalArgumentException(
                  "optionalAttrName may not be null or empty");
         }
      }
      fromXml(sourceNode);
   }

   /**
    * Restore this object from an Xml representation.
    * @param sourceNode reference of the element which should be wrapped by
    * this object
    * @throws IllegalArgumentException if sourceNode is <code>null</code>
    * @throws CodeException if the element does not have all the attributes
    * defined as specified in the <code>m_reqAttrNames</code> array.
    */
   public void fromXml(Element sourceNode)
      throws CodeException
   {
      if (sourceNode == null)
         throw new IllegalArgumentException("sourceNode may not be null");

      m_name = sourceNode.getTagName();

      if (m_reqAttrNames != null)
      {
         for (int i = 0; i < m_reqAttrNames.length; i++)
         {
            String attrValue = PSBrandCodeUtil.getAttributeValue(sourceNode,
               m_reqAttrNames[i], true);
            m_attrMap.put(m_reqAttrNames[i], attrValue);
         }
      }
      if (m_optionalAttrNames != null)
      {
         for (int i = 0; i < m_optionalAttrNames.length; i++)
         {
            String attrValue = PSBrandCodeUtil.getAttributeValue(sourceNode,
               m_optionalAttrNames[i], false);
            if (attrValue != null)
               m_attrMap.put(m_optionalAttrNames[i], attrValue);
         }
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
      Iterator it = m_attrMap.entrySet().iterator();
      while (it.hasNext())
      {
         Map.Entry item = (Map.Entry)it.next();
         root.setAttribute((String)item.getKey(), (String)item.getValue());
      }
      return root;
   }

   /**
    * Returns <code>true</code> if this element has the specified value
    * for the specified attribute name, <code>false</code> otherwise. The
    * comparison of attribute value is case-insensitive.
    *
    * @param attrName the name of the attribute, may not be <code>null</code>
    * or empty
    * @param attrValue the value of the attribute, may not be
    * <code>null</code> or empty
    *
    * @return <code>true</code> if this element has the specified value
    * for the specified attribute name. Returns <code>false</code> if this
    * element does not have the specified attribute or if the value of the
    * attribute does not match the specified attribute.
    *
    * @throws IllegalArgumentException if attrName or attrValue is
    * <code>null</code> or empty
    */
   public boolean hasAttributeWithValue(String attrName, String attrValue)
   {
      if ((attrValue == null) || (attrValue.trim().length() < 1))
         throw new IllegalArgumentException(
            "attrValue may not be null or empty");
      if (hasAttribute(attrName))
      {
         String mapAttrValue = (String)m_attrMap.get(attrName);
         if (mapAttrValue.equalsIgnoreCase(attrValue))
            return true;
      }
      return false;
   }

   /**
    * Returns the value of the specified attribute.
    * @param attrName the name of the attribute, may not be <code>null</code>
    * or empty
    * @param required If <code>true</code> and this element does not have the
    * specified attribute, then CodeException is thrown.
    * If required is <code>false</code> and this element does not have the
    * specified attribute then <code>null</code> is returned.
    *
    * @return the value of the specified attribute, may be <code>null</code>
    * if required is <code>true</code>.
    * @throws IllegalArgumentException if attrName is <code>null</code> or empty
    * @throws CodeException if required is <code>true</code> and this element
    * does not have the specified attribute.
    */
   public String getAttributeValue(String attrName, boolean required)
      throws CodeException
   {
      if (hasAttribute(attrName))
         return (String)m_attrMap.get(attrName);
      // this element does not have the specified attribute
      if (required)
      {
         throw new CodeException(
            "Attribute : " + attrName +
            " not found for element : " + m_name);
      }
      return null;
   }

   /**
    * Returns <code>true</code> if this element has the specified attribute,
    * <code>false</code> otherwise.
    * @param attrName the name of the attribute, may not be <code>null</code>
    * or empty
    * @return <code>true</code> if this element has the specified attribute,
    * <code>false</code> otherwise.
    * @throws IllegalArgumentException if attrName is <code>null</code> or empty
    */
   private boolean hasAttribute(String attrName)
   {
      if ((attrName == null) || (attrName.trim().length() < 1))
         throw new IllegalArgumentException(
            "attrName may not be null or empty");
      if (m_attrMap.containsKey(attrName))
         return true;
      return false;
   }

   /**
    * array containing the names of required attributes,
    * initialized in the constructor,
    * may be <code>null</code> or empty.
    */
   private String[] m_reqAttrNames = null;

   /**
    * array containing the names of optional attributes,
    * initialized in the constructor,
    * may be <code>null</code> or empty.
    */
   private String[] m_optionalAttrNames = null;

   /**
    * Map for storing the name and value of the attributes of this element.
    * initialized in the <code>fromXml</code> method, never empty after
    * initialization. The attribute name is used as the key of the map,
    * and the attribute value is used as the value.
    */
   private Map m_attrMap = new HashMap();

   /**
    * tag name of the elememt, initialized in the <code>fromXml</code> method,
    * never <code>null</code> or empty after initialization.
    */
   private String m_name = null;


}


