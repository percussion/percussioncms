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

import java.util.ArrayList;
import java.util.Iterator;
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
                      ArrayList parentComponents)
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
                       ArrayList parentComponents)
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
