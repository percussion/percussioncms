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

import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

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
      ArrayList parentComponents)
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
      ArrayList parentComponents)
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
         log.error(e.getMessage());
         log.debug(e.getMessage(), e);
      }
   }
}
