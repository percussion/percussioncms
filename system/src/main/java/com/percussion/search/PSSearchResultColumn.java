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

package com.percussion.search;

import com.percussion.design.objectstore.IPSDocument;
import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.util.PSXMLDomUtil;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;

/**
 * Represents a column in the {@link com.percussion.search.IPSSearchResultRow}
 * object. The DTD for the XML representation is:
 * <p>
 * &lt;! ELEMENT ResultField (#PCDATA)&gt;
 * &lt;!ATTLIST ResultField
 *  name CDATA #REQUIRED
 *  displayValue CDATA #IMPLIED
 * &gt;
 */
public class PSSearchResultColumn
{
   /**
    * Ctor that takes name, display value and internal value for a column.
    * 
    * @param name name of the search result column. Must not be
    *           <code>null</code> or empty.
    * @param displayValue display value for the column, may be <code>null</code>
    *           or empty. If <code>null</code>, it is taken as empty string.
    * @param value inernal value for the column, may be <code>null</code>
    *           or empty. If <code>null</code>, it is taken as empty string.
    */
   public PSSearchResultColumn(String name, String displayValue, String value)
   {
      if (name == null || name.length() == 0)
         throw new IllegalArgumentException("name must not be null");
      m_name = name;
      m_displayValue = displayValue == null ? "" : displayValue;
      m_value = value == null ? "" : value;
   }

   /**
    * Ctor that takes the XML element. The DTD for the element must be that
    * outlined in the last description.
    * 
    * @param sourceNode source node for the search result column, must not be
    *           <code>null</code>.
    * 
    * @throws PSUnknownNodeTypeException if the DTD source element does not
    *            match with the expected.
    * @see #fromXml(Element, IPSDocument, ArrayList)
    */
   public PSSearchResultColumn(Element sourceNode)
         throws PSUnknownNodeTypeException
   {
      fromXml(sourceNode);
   }

   /**
    * Convert the object to its XML representation.
    * 
    * @param doc the parent document for the XML element for the object, must
    *           not be <code>null</code>.
    * @return XML element representing the object. See the DTD specified in the
    *         class description, never <code>null</code>.
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc must not be null");

      Element fieldElem = doc.createElement(XML_NODE_NAME);
      fieldElem.setAttribute(ATTR_NAME, m_name);
      fieldElem.setAttribute(ATTR_DISPLAY_VALUE, m_displayValue);
      fieldElem.appendChild(doc.createTextNode(m_value));
      return fieldElem;
   }

   /**
    * Construct the object from XML source element. Look at the DTD for the
    * element specified in the class description.
    * 
    * @param sourceNode source node as per the DTD.
    * @throws PSUnknownNodeTypeException if DTD does not match with the
    *            required.
    */
   public void fromXml(Element sourceNode) throws PSUnknownNodeTypeException
   {
      if (sourceNode == null)
         throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_NULL, XML_NODE_NAME);

      if (!XML_NODE_NAME.equals(sourceNode.getNodeName()))
      {
         Object[] args =
         {XML_NODE_NAME, sourceNode.getNodeName()};
         throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }

      PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);

      m_name = PSXMLDomUtil.checkAttribute(sourceNode, ATTR_NAME, true);
      m_value = tree.getElementData(XML_NODE_NAME);
      m_displayValue = PSXMLDomUtil.checkAttribute(sourceNode,
            ATTR_DISPLAY_VALUE, false);
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#clone()
    */
   public Object clone()
   {
      return new PSSearchResultColumn(m_name, m_displayValue, m_value);
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#equals(java.lang.Object)
    */
   public boolean equals(Object obj)
   {
      if (!super.equals(obj))
         return false;

      PSSearchResultColumn s2 = (PSSearchResultColumn) obj;

      return m_name.equals(s2.m_name) && m_value.equals(s2.m_value)
            && m_displayValue.equals(s2.m_displayValue);
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#hashCode()
    */
   public int hashCode()
   {
      String str = m_name + ":" + m_value + ":" + m_displayValue;
      return super.hashCode() + str.hashCode();
   }

   /**
    * Get the name of the search field column.
    * 
    * @return the name of the search result column, never <code>null</code> or
    *         empty.
    */
   public String getName()
   {
      return m_name;
   }

   /**
    * Get the display value for the search result column.
    * 
    * @return may be <code>null</code> or empty.
    */
   public String getDisplayValue()
   {
      if(m_displayValue == null || m_displayValue.length()==0)
         return m_value;
      return m_displayValue;
   }

   /**
    * Set the display value for the search result column.
    * 
    * @param value display value to set, may be <code>null</code> or empty.
    */
   public void setDisplayValue(String value)
   {
      m_displayValue = value;
   }

   /**
    * Get the internal value for the search result column.
    * 
    * @return may be <code>null</code> or empty.
    */
   public String getValue()
   {
      return m_value;
   }

   /**
    * Set the internal value for the search result column.
    * 
    * @param value internal value to set, may be <code>null</code> or empty.
    */
   public void setValue(String value)
   {
      m_value = value;
   }

   /**
    * Name of the field initialized in the ctor, never <code>null</code> or
    * empty after that.
    * 
    * @see #getName()
    */
   private String m_name = "";

   /**
    * Display value of the field, either initialized in the ctor or by
    * {@link #setDisplayValue(String)}, may be <code>null</code> or empty.
    * 
    * @see #getDisplayValue()
    */
   private String m_displayValue = "";

   /**
    * Internal value of the field, either initialized in the ctor or by
    * {@link #setValue(String)}, may be <code>null</code> or empty.
    * 
    * @see #getName()
    */
   private String m_value = "";

   /**
    * Name of the root element in the XMl representation of the object.
    */
   public static final String XML_NODE_NAME = "ResultField";

   /**
    * Name of the attribute of root element for the name of the result column 
    * in the XML representation of the object.
    */
   public static final String ATTR_NAME = "name";

   /**
    * Name of the attribute of root element for the display value of the result 
    * column in the XML representation of the object.
    */
   public static final String ATTR_DISPLAY_VALUE = "displayValue";
}
