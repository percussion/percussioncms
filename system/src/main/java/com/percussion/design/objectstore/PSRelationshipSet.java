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
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Iterator;
import java.util.List;

/**
 * A collection of PSRelationship objects. See PSXRelationshipSet.dtd.
 */
public class PSRelationshipSet extends PSCollectionComponent
{
   /**
    * Constucts an empty relationship set.
    */
   public PSRelationshipSet()
   {
      super(PSRelationship.class);
   }
   
   /**
    * Constucts an empty relationship set with the specified initial capacity 
    * and with its capacity increment equal to zero.
    * 
    * @param initialCapacity   the initial capacity of the set.
    */
   public PSRelationshipSet(int initialCapacity)
   {
      super(PSRelationship.class, initialCapacity);
   }
   
   /*
    * (non-Javadoc)
    * @see java.util.AbstractList#iterator()
    */
   @Override
   @SuppressWarnings("unchecked")
   public Iterator<PSRelationship> iterator()
   {
      return super.iterator();
   }
   
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
   public PSRelationshipSet(Element sourceNode, IPSDocument parentDoc, 
      List parentComponents) throws PSUnknownNodeTypeException
   {
      super(PSRelationship.class);

      fromXml(sourceNode, parentDoc, parentComponents);
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
         
         node = tree.getNextElement(PSRelationship.XML_NODE_NAME, firstFlags);
         while (node != null)
         {
            PSRelationship relationship = new PSRelationship(
               (Element) tree.getCurrent(), parentDoc, parentComponents);

            add(relationship);
            
            node = tree.getNextElement(
               PSRelationship.XML_NODE_NAME, nextFlags);
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
         IPSComponent relationship = (IPSComponent) get(i);
         root.appendChild(relationship.toXml(doc));
      }

      return root;
   }

   /** the XML node name */
   public static final String XML_NODE_NAME = "PSXRelationshipSet";
}
