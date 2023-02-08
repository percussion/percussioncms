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

import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Iterator;
import java.util.List;

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
      List parentComponents) throws PSUnknownNodeTypeException
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
      List parentComponents) throws PSUnknownNodeTypeException
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
