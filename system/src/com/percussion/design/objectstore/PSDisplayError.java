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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.design.objectstore;

import com.percussion.utils.xml.PSInvalidXmlException;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This class represents the DisplayError element of the sys_ContentEditor.dtd.
 * 
 */
public class PSDisplayError
{
   /**
    * Ctor for constrcting the object from source element.
    * 
    * @param sourceNode The xml element that represents the display error.
    *           Expects the element as per the following DTD 
    *           &lt;!ELEMENT
    *           DisplayError (GenericMessage, Details*)&gt; 
    *           &lt;!ATTLIST
    *           DisplayError errorCount CDATA #REQUIRED&gt;
    * @throws PSUnknownNodeTypeException If the supplied source xml does not
    *            confirm to the above mentioned DTD.
    * @throws PSInvalidXmlException
    */
   public PSDisplayError(Element sourceNode) throws PSUnknownNodeTypeException,
         PSInvalidXmlException
   {
      if(sourceNode == null)
         throw new IllegalArgumentException("sourceNode must not be null");
      fromXml(sourceNode);
   }

   /**
    * Ctor for creating the object from string representation of DispalyError.
    * 
    * @param sourceDoc The xml doc that represents the display error.
    *           Expects the element as per the following DTD 
    *           &lt;!ELEMENT
    *           DisplayError (GenericMessage, Details*)&gt; 
    *           &lt;!ATTLIST
    *           DisplayError errorCount CDATA #REQUIRED&gt;
    * @throws PSUnknownNodeTypeException If the supplied source xml does not
    *            confirm to the above mentioned DTD.
    * @throws PSInvalidXmlException
    */
   public PSDisplayError(String sourceDoc) throws IOException, SAXException,
         PSUnknownNodeTypeException, PSInvalidXmlException
   {
      if (StringUtils.isBlank(sourceDoc))
         throw new IllegalArgumentException(
               "sourceDoc must not be null or empty");
      Document errorDoc = PSXmlDocumentBuilder.createXmlDocument(
            new ByteArrayInputStream(sourceDoc.getBytes()), false);
      fromXml(errorDoc.getDocumentElement());
   }
   
   /**
    * Creates object from xml representation.
    * @param sourceNode {@link #PSDisplayError(Element)}
    * @throws PSUnknownNodeTypeException
    * @throws PSInvalidXmlException
    */
   private void fromXml(Element sourceNode) throws PSUnknownNodeTypeException,
         PSInvalidXmlException
   {
      if (!XML_NODE_NAME.equals(sourceNode.getNodeName()))
      {
         Object[] args =
         {XML_NODE_NAME, sourceNode.getNodeName()};
         throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }
      m_errorCount = sourceNode.getAttribute(XML_ATTR_NAME_ERROR_COUNT);
      NodeList gms = sourceNode.getElementsByTagName(XML_ELEM_GENERIC_MESSAGE);
      if(gms!=null && gms.getLength()>0)
      {
         m_genericMessage = PSXmlTreeWalker.getElementData(gms.item(0));
      }
      m_details = new ArrayList<>();
      NodeList nl = sourceNode.getElementsByTagName(PSDetails.XML_NODE_NAME);
      for (int i = 0; nl != null && i < nl.getLength(); i++)
      {
         m_details.add(new PSDetails((Element) nl.item(i)));
      }
      
   }
   
   /**
    * Converts the object to xml.
    * @param doc The parent doc for this element, must not be <code>null</code>.
    * @return Element representing the this class object.
    */
   public Element toXml(Document doc)
   {
      if(doc == null)
         throw new IllegalArgumentException("doc must not be null");
      // create root and its attributes
      Element root = doc.createElement(XML_NODE_NAME);
      root.setAttribute(XML_ATTR_NAME_ERROR_COUNT, m_errorCount);
      Element genericMessage = doc.createElement(XML_ELEM_GENERIC_MESSAGE);
      genericMessage.appendChild(doc.createTextNode(m_genericMessage));
      root.appendChild(genericMessage);
      for(PSDetails d:m_details)
      {
         root.appendChild(d.toXml(doc));
      }
      return root;
   }

   /**
    * Gets the generic message of the display error.
    * @return String generic message never <code>null</code> may be empty.
    */
   public String getGenericMessage()
   {
      return StringUtils.defaultString(m_genericMessage);
   }

   /**
    * Gets the error count of the display error.
    * @return errorCount String never <code>null</code> may be empty.
    */
   public String getErrorCount()
   {
      return StringUtils.defaultString(m_errorCount);
   }

   /**
    * @return the list of
    *         {@link com.percussion.design.objectstore.PSDetails#PSDetails(Element)
    *         never <code>null</code> may be empty.
    */
   public List<PSDetails> getDetails()
   {
      return m_details;
   }
   
   /**
    * @see #getErrorCount()
    */
   private String m_errorCount;
   
   /**
    * @see #getDetails()
    */
   private List<PSDetails> m_details;

   /**
    * @see #getGenericMessage()
    */
   private String m_genericMessage;

   /**
    * Constant that represents root element of this class xml.
    */
   public static final String XML_NODE_NAME = "DisplayError";

   /**
    * Constant that represents the xml element name for GenericMessage
    */
   public static final String XML_ELEM_GENERIC_MESSAGE = "GenericMessage";
   
   /**
    * Constant that represents the xml element name for GenericMessage
    */
   public static final String XML_ATTR_NAME_ERROR_COUNT = "errorCount";
}
