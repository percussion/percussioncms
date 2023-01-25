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
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * A collection of PSAaRelationship objects. See PSXAaRelationshipList.dtd. The
 * sort rank of the each relationship is implicit in that the order of
 * appearance in the list is assumed to the sort rank.
 *
 * @author Ram
 */
public class PSAaRelationshipList
   extends PSCollectionComponent
{
   /**
    * Constucts an empty active assembly relationship list.
    */
   public PSAaRelationshipList()
   {
      super(PSAaRelationship.class);
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
   public PSAaRelationshipList(Element sourceNode, IPSDocument parentDoc,
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

   /**
    * Override the base class version to set the sort rank of the relationship
    * just beforeaccessing.
    * @param index index of the object (relationship) to access.
    * @return Relationship object for the specified index, may be <code>null</code>
    * @exception ArrayIndexOutOfBoundsException index is out of range (index
    *         &lt; 0 || index &gt;= size()).
    * @see java.util.List#get(int) for more details.
    */
   public Object get(int index)
   {
      PSAaRelationship rel = (PSAaRelationship)super.get(index);
      rel.setProperty(IPSHtmlParameters.SYS_SORTRANK, "" + index);
      return rel;
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
   public static final String XML_NODE_NAME = "PSXAaRelationshipList";
}
