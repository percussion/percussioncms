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
