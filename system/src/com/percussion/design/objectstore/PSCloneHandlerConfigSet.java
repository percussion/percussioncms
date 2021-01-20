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

import java.util.ArrayList;
import java.util.Iterator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A collection of PSCloneHandlerConfig objects. It creates or
 * reads documents conforming to the sys_CloneHandlerConfig.dtd.
 */
public class PSCloneHandlerConfigSet extends PSCollectionComponent 
   implements IPSConfig
{
   /**
    * Construct a Java object from its XML representation.
    *
    * @param sourceNode   the XML element node to construct this object from,
    *    not <code>null</code>.
    * @param parentDoc the Java object which is the parent of this object,
    *    may be <code>null</code>.
    * @param parentComponents   the parent objects of this object, may be
    *    <code>null</code>.
    * @throws PSUnknownNodeTypeException if the XML element node is not of 
    *    the appropriate type
    */
   public PSCloneHandlerConfigSet(Element sourceNode, IPSDocument parentDoc, 
      ArrayList parentComponents) throws PSUnknownNodeTypeException
   {
      super(PSCloneHandlerConfig.class);

      fromXml(sourceNode, parentDoc, parentComponents);
   }
   
   /**
    * Get the clone handler configuration for the supplied name.
    * 
    * @param name the clone handler configuration name, not <code>null</code>
    *    or empty.
    * @return the clone handler configuration, might be <code>null</code>
    *    if not found for name.
    * @throws IllegalArgumentException if the supplied name is <code>null</code>
    *    or empty.
    */
   public PSCloneHandlerConfig getConfig(String name)
   {
      if (name == null || name.trim().length() == 0)
        throw new IllegalArgumentException("name cannot be null or empty");
        
      Iterator configs = iterator();
      while (configs.hasNext())
      {
         PSCloneHandlerConfig config = (PSCloneHandlerConfig) configs.next();
         if (config.getName().equals(name))
            return config;
      }
      
      return null;
   }
   
   /** @see IPSComponent */
   public void fromXml(Element sourceNode, IPSDocument parentDoc, 
      ArrayList parentComponents) throws PSUnknownNodeTypeException
   {
      if (sourceNode == null)
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL, XML_NODE_NAME);

      if (!XML_NODE_NAME.equals(sourceNode.getNodeName()))
      {
         Object[] args = { XML_NODE_NAME, sourceNode.getNodeName() };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }

      parentComponents = updateParentList(parentComponents);
      int parentSize = parentComponents.size() - 1;

      int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN |
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
      int nextFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS |
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

      String data = null;
      Element node = null;
      try 
      {
         PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);
         
         node = tree.getNextElement(
            PSCloneHandlerConfig.XML_NODE_NAME, firstFlags);
         while (node != null)
         {
            PSCloneHandlerConfig config = new PSCloneHandlerConfig(
               (Element) tree.getCurrent(), parentDoc, parentComponents);
            
            // check duplicates
            if (getConfig(config.getName()) != null)
            {
               Object[] args =
               { 
                  XML_NODE_NAME, 
                  PSCloneHandlerConfig.XML_NODE_NAME,
                  "Duplicate entry, must be unique server wide: " + 
                     config.getName()
               };
               throw new PSUnknownNodeTypeException(
                  IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
            }
            
            add(config);
            
            node = tree.getNextElement(
               PSCloneHandlerConfig.XML_NODE_NAME, nextFlags);
         }
      } 
      finally 
      {
         resetParentList(parentComponents, parentSize);
      }
   }

   /** @see IPSComponent */
   public Element toXml(Document doc)
   {
      Element root = doc.createElement(XML_NODE_NAME);
      for (int i=0; i<size(); i++)
      {
         IPSComponent config = (IPSComponent) get(i);
         root.appendChild(config.toXml(doc));
      }

      return root;
   }
   
   //implements IPSConfig interface method
   public String getConfigString()
   {
      Document configDoc = PSXmlDocumentBuilder.createXmlDocument();
      Element config = toXml(configDoc);      
      return PSXmlDocumentBuilder.toString(config);
   }

   /** the XML node name */
   public static final String XML_NODE_NAME = "PSXCloneHandlerConfigSet";
}
