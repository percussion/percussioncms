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

import com.percussion.error.PSExceptionUtils;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.FileReader;
import java.util.Iterator;
import java.util.List;

/**
 * @author RammohanVangapalli
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class PSCloneOverrideFieldList extends PSCollectionComponent
{

   private static final Logger log = LogManager.getLogger(PSCloneOverrideFieldList.class);

   /**
    * Construct a Java object from its XML representation. See the
    * {@link #toXml(Document) toXml} method for a description of the XML object.
    *
    * @param      sourceNode     the XML element node to construct this
    *                            object from
    *
    * @param      parentDoc      the Java object which is the parent of this
    *                            object
    *
    * @param      parentComponents  the parent objects of this object
    *
    * @exception  PSUnknownNodeTypeException
    *                            if the XML element node is not of the
    *                            appropriate type
    */
   public PSCloneOverrideFieldList(
      Element sourceNode,
      IPSDocument parentDoc,
      List parentComponents)
      throws PSUnknownNodeTypeException
   {
      this();
      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Constructs an empty <code>PSExtensionCallSet</code>.
    */
   public PSCloneOverrideFieldList()
   {
      super(PSCloneOverrideField.class);
   }

   /* (non-Javadoc)
    * @see com.percussion.design.objectstore.IPSComponent#toXml(org.w3c.dom.Document)
    */
   public Element toXml(Document doc)
   {
      Element root = doc.createElement(XML_NODE_NAME);
      root.setAttribute("id", String.valueOf(m_id));

      int size = size();
      for (int i = 0; i < size; i++)
      {
         IPSComponent exit = (IPSComponent) get(i);
         root.appendChild(exit.toXml(doc));
      }

      return root;
   }

   /* (non-Javadoc)
    * @see com.percussion.design.objectstore.IPSComponent#fromXml(org.w3c.dom.Element, com.percussion.design.objectstore.IPSDocument, java.util.ArrayList)
    */
   public void fromXml(
      Element sourceNode,
      IPSDocument parentDoc,
      List parentComponents)
      throws PSUnknownNodeTypeException
   {
      if (sourceNode == null)
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL,
            XML_NODE_NAME);

      if (false == XML_NODE_NAME.equals(sourceNode.getNodeName()))
      {
         Object[] args = { XML_NODE_NAME, sourceNode.getNodeName()};
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE,
            args);
      }

      PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);

      String sTemp = sourceNode.getAttribute("id");
      try
      {
         m_id = Integer.parseInt(sTemp);
      }
      catch (Exception e)
      {
         m_id = 0;
      }

      NodeList nl =
         sourceNode.getElementsByTagName(PSCloneOverrideField.XML_NODE_NAME);
      Element curElem = null;
      for (int i = 0; i < nl.getLength(); i++)
      {
         curElem = (Element) nl.item(i);
         PSCloneOverrideField field =
            new PSCloneOverrideField(curElem, parentDoc, parentComponents);
         add(field);
      }
   }
   
   /* (non-Javadoc)
    * @see java.lang.Object#clone()
    */
   public Object clone()
   {
      PSCloneOverrideFieldList filedList = new PSCloneOverrideFieldList();
      Iterator iter = iterator();
      while (iter.hasNext())
         filedList.add(((PSCloneOverrideField) iter.next()).clone());
      return filedList;
   }

   /**
    * Name of the root element in the XML representation of this object.  
    */
   public static final String XML_NODE_NAME = "PSXCloneOverrideFieldList";

   /**
    * Main method for testing
    * @param args
    */
   public static void main(String[] args)
   {
      try
      {
         Document doc =
            PSXmlDocumentBuilder.createXmlDocument(
               new FileReader("c:/PSXCloneOverrideFieldList.xml"),
               false);

         Element elem = doc.getDocumentElement();
         PSCloneOverrideFieldList list = 
               new PSCloneOverrideFieldList(elem, null,null);

            System.out.println(
               "xml = " + PSXmlDocumentBuilder.toString(list.toXml(doc)));
      }
      catch (Exception e)
      {
         log.error(PSExceptionUtils.getMessageForLog(e));
         log.debug(PSExceptionUtils.getDebugMessageForLog(e));
      }
   }
}
