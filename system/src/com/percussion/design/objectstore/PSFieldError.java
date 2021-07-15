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
package com.percussion.design.objectstore;

import com.percussion.xml.PSXmlTreeWalker;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class represents the FieldError element of the sys_ContentEditor.dtd.
 * 
 */
public class PSFieldError
{
   /**
    * Ctor
    * 
    * @param sourceNode The xml element that represents the FieldError element.
    *           Expects the element as per the following DTD 
    *           &lt;!ELEMENT FieldError (#PCDATA)&gt;
    *           &lt;!ATTLIST FieldError
    *             submitName CDATA #REQUIRED
    *             displayName CDATA #REQUIRED
    *           &gt;
    * @throws PSUnknownNodeTypeException If the supplied source xml does not
    *            confirm to the above mentioned DTD.
    */
   public PSFieldError(Element sourceNode) throws PSUnknownNodeTypeException
   {
      if (sourceNode == null)
         throw new IllegalArgumentException("sourceNode must not be null");
      fromXml(sourceNode);
   }

   /**
    * @see #PSFieldError(Element)
    * @param sourceNode assumed not <code>null</code>.
    * @throws PSUnknownNodeTypeException
    */
   private void fromXml(Element sourceNode) throws PSUnknownNodeTypeException
   {
      if (!XML_NODE_NAME.equals(sourceNode.getNodeName()))
      {
         Object[] args =
         {XML_NODE_NAME, sourceNode.getNodeName()};
         throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }
      m_submitName = sourceNode.getAttribute(XML_ATTR_NAME_SUBMIT_NAME);
      m_displayName = sourceNode.getAttribute(XML_ATTR_NAME_DISPLAY_NAME);
      m_errorText = StringUtils.defaultString(PSXmlTreeWalker
            .getElementData(sourceNode));
   }

   /**
    * Converts the object to xml.
    * @param doc The parent doc for this element, must not be <code>null</code>.
    * @return Element representing this class object.
    */
   public Element toXml(Document doc)
   {
      if(doc == null)
         throw new IllegalArgumentException("doc must not be null");
      // create root and its attributes
      Element root = doc.createElement(XML_NODE_NAME);
      root.appendChild(doc.createTextNode(m_errorText));
      root.setAttribute(XML_ATTR_NAME_SUBMIT_NAME, m_submitName);
      root.setAttribute(XML_ATTR_NAME_DISPLAY_NAME, m_displayName);
      return root;
   }

   /**
    * Gets the display name value. May be empty, but never <code>null</code>.
    * @return String displayName attribute value.
    */
   public String getDisplayName()
   {
      return m_displayName;
   }

   /**
    * Gets the error text value. May be empty, but never <code>null</code>.
    * @return String error text corresponding to this field error.
    */
   public String getErrorText()
   {
      return m_errorText;
   }

   /**
    * Gets the display name value. May be empty, but never <code>null</code>.
    * @return String submitName attribute value.
    */
   public String getSubmitName()
   {
      return m_submitName;
   }

   /**
    * Represents the submitName attribute of FieldError element.
    * Initalized in ctor and never <code>null</code> after that.
    */
   private String m_submitName;

   /**
    * Represents the displayName attribute of FieldError element.
    * Initalized in ctor and never <code>null</code> after that.
    */
   private String m_displayName;

   /**
    * Represents the error text value of FieldError element.
    * Initalized in ctor and never <code>null</code> after that.
    */
   private String m_errorText;

   /**
    * Constant for the root element name of this class.
    */
   public static final String XML_NODE_NAME = "FieldError";

   /**
    * Constant for the submitName attribute.
    */
   public static final String XML_ATTR_NAME_SUBMIT_NAME = "submitName";

   /**
    * Constant for the displayName attribute.
    */
   public static final String XML_ATTR_NAME_DISPLAY_NAME = "displayName";

}
