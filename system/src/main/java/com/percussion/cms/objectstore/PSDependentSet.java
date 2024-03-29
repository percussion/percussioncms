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
package com.percussion.cms.objectstore;

import com.percussion.design.objectstore.IPSComponent;
import com.percussion.design.objectstore.IPSDocument;
import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSCollectionComponent;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;

/**
 * This class encapsulates a collection of <code>PSDependent</code> objects as
 * a <code>PSCollectionComponent</code>. 
 */
public class PSDependentSet extends PSCollectionComponent
{
   /**
    * Constucts an empty dependent set.
    */
   public PSDependentSet()
   {
      super(PSDependent.class);
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
   public PSDependentSet(Element sourceNode, IPSDocument parentDoc, 
      List parentComponents) throws PSUnknownNodeTypeException
   {
      super(PSDependent.class);

      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /** @see IPSComponent */
   public void fromXml(Element sourceNode, IPSDocument parentDoc, 
      List parentComponents) throws PSUnknownNodeTypeException
   {
      if (sourceNode == null)
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL, getNodeName());

      if (!getNodeName().equals(sourceNode.getNodeName()))
      {
         Object[] args = { getNodeName(), sourceNode.getNodeName() };
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
         
         node = tree.getNextElement(PSDependent.getNodeName(), firstFlags);
         while (node != null)
         {
            PSDependent dependent = new PSDependent(
               (Element) tree.getCurrent());

            add(dependent);
            
            node = tree.getNextElement(
               PSDependent.getNodeName(), nextFlags);
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
      Element root = doc.createElement(getNodeName());
      for (int i=0; i<size(); i++)
      {
         IPSComponent dependent = (IPSComponent) get(i);
         root.appendChild(dependent.toXml(doc));
      }

      return root;
   }

   /**
    * This is the name of the root element in the serialized version of this
    * object.
    *
    * @return the root name, never <code>null</code> or empty.
    */
   public static String getNodeName()
   {
      return "PSXDependentSet";
   }
}
