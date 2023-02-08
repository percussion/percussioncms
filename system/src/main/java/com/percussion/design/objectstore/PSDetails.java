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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents the Details element of the sys_ContentEditor.dtd.
 * 
 */
public class PSDetails
{
   /**
    * Ctor
    * 
    * @param sourceNode The xml element that represents the Details element.
    *           Expects the element as per the following DTD 
    *           &lt;!ELEMENT Details (FieldError+)&gt; 
    * @throws PSUnknownNodeTypeException If the supplied source xml does not
    *            confirm to the above mentioned DTD.
    */
   public PSDetails(Element sourceNode) throws PSUnknownNodeTypeException
   {
      if (sourceNode == null)
         throw new IllegalArgumentException("sourceNode must not be null");
      fromXml(sourceNode);
   }

   /**
    * @see #PSDetails(Element)
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
      m_fieldErrors = new ArrayList<>();
      NodeList fe = sourceNode.getElementsByTagName(PSFieldError.XML_NODE_NAME);
      for (int j = 0; fe != null && j < fe.getLength(); j++)
      {
         m_fieldErrors.add(new PSFieldError((Element) fe.item(j)));
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
      Element root = doc.createElement(XML_NODE_NAME);
      for(PSFieldError fe:m_fieldErrors)
      {
         root.appendChild(fe.toXml(doc));
      }
      return root;
   }

   /**
    * Gets the list of PSFieldError objects never <code>null</code> may be empty.
    * @return List<PSFieldError> objects.
    */
   public List<PSFieldError> getFieldErrors()
   {
      return m_fieldErrors;
   }

   /**
    * Represents the list of the FieldError elements from the sourceNode.
    * Initialized in ctor, may be empty but never <code>null</code> afetr that.
    */
   private List<PSFieldError> m_fieldErrors;

   /**
    * XML element node name for the root element of this class.
    */
   public static final String XML_NODE_NAME = "Details";
}
