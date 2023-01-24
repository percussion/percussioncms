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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Implements the PSXApplyWhen DTD in BasicObjects.dtd.
 */
public class PSApplyWhen extends PSCollectionComponent
{
   /**
    * Create a new apply when collection (a collection of PSRule objects).
    */
   public PSApplyWhen()
   {
      super((new PSRule()).getClass());
   }

   /**
    * Construct a Java object from its XML representation.
    *
    * @param sourceNode   the XML element node to construct this object from,
    *    not <code>null</code>.
    * @param parentDoc the Java object which is the parent of this object,
    *    not <code>null</code>.
    * @param parentComponents   the parent objects of this object, not
    *    <code>null</code>.
    * @throws PSUnknownNodeTypeException if the XML element node is not of
    *    the appropriate type
    */
   public PSApplyWhen(Element sourceNode, IPSDocument parentDoc,
                      List parentComponents)
      throws PSUnknownNodeTypeException
   {
      this();
      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Get the status of the ifFieldEmpty attribute.
    *
    * @return the ifFieldEmpty status.
    */
   public boolean ifFieldEmpty()
   {
      return m_ifFieldEmpty;
   }

   /**
    * Set a new ifFieldEmpty status.
    *
    * @param ifFieldEmpty the new ifFieldEmpty status.
    */
   public void setIfFieldEmpty(boolean ifFieldEmpty)
   {
      m_ifFieldEmpty = ifFieldEmpty;
   }

   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component. Derived classes should implement this method for their data,
    * calling the base class method first.
    *
    * @param c a valid PSApplyWhen, not <code>null</code>.
    */
   public void copyFrom(PSApplyWhen c)
   {
      try
      {
         super.copyFrom(c);
      }
      catch (IllegalArgumentException e)
      {
         throw new IllegalArgumentException(e.getLocalizedMessage());
      }

      setIfFieldEmpty(c.ifFieldEmpty());
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSApplyWhen)) return false;
      if (!super.equals(o)) return false;
      PSApplyWhen that = (PSApplyWhen) o;
      return m_ifFieldEmpty == that.m_ifFieldEmpty;
   }

   @Override
   public int hashCode() {
      return Objects.hash(super.hashCode(), m_ifFieldEmpty);
   }

   /**
    *
    * @see IPSComponent
    */
   public void fromXml(Element sourceNode, IPSDocument parentDoc,
                       List parentComponents)
      throws PSUnknownNodeTypeException
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

      try
      {
         PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);

         // OPTIONAL: get the ifFieldEmpty attribute
         String ifFieldEmpty = tree.getElementData(IS_FIELD_EMPTY_ATTR);
         if (ifFieldEmpty != null)
            m_ifFieldEmpty = ifFieldEmpty.equalsIgnoreCase(BOOLEAN_ENUM[0]);

         // OPTIONAL: get all rules
         Element node = tree.getNextElement(
            PSRule.XML_NODE_NAME, firstFlags);
         while (node != null)
         {
            add(new PSRule(node, parentDoc, parentComponents));

            node = tree.getNextElement(PSRule.XML_NODE_NAME, nextFlags);
         }
      }
      finally
      {
         resetParentList(parentComponents, parentSize);
      }
   }

   /**
    *
    * @see IPSComponent
    */
   public Element toXml(Document doc)
   {
      // create root and its attributes
      Element root = doc.createElement(XML_NODE_NAME);
      root.setAttribute(IS_FIELD_EMPTY_ATTR,
                        ifFieldEmpty() ? BOOLEAN_ENUM[0] : BOOLEAN_ENUM[1]);

      // create the rules
      Iterator it = iterator();
      while (it.hasNext())
         root.appendChild(((IPSComponent) it.next()).toXml(doc));

      return root;
   }

   /** the XML node name */
   public static final String XML_NODE_NAME = "PSXApplyWhen";

   /**
    * By default, the rules won't be applied if the field has no
    * value and it is not a required field.
    **/
   private boolean m_ifFieldEmpty = false;

   /**
    * An array of XML attribute values for all boolean attributes. They are
    * ordered as <code>true</code>, <code>false</code>.
    */
   private static final String[] BOOLEAN_ENUM =
   {
      "yes", "no"
   };

   /*
    * The following strings define all elements/attributes used to create the
    * XML output for this object. No Java documentation will be added to this.
    */
   private static final String IS_FIELD_EMPTY_ATTR = "ifFieldEmpty";
}
