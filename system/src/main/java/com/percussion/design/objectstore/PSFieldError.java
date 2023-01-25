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
