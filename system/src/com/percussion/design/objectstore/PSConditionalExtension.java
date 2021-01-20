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

import com.percussion.util.PSCollection;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;

/**
 * Implements the PSXConditionalEffect element defined in
 * sys_RelationshipConfig.dtd.
 */
public class PSConditionalExtension extends PSComponent
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
    *    the appropriate type.
    */
   public PSConditionalExtension(Element sourceNode, IPSDocument parentDoc,
      ArrayList parentComponents) throws PSUnknownNodeTypeException
   {
      if (sourceNode == null)
         throw new IllegalArgumentException("sourceNode annot be null");

      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Constructs this object with supplied extension call.
    *
    * @param extension the extension to execute, may not be <code>null</code>.
    */
   public PSConditionalExtension(PSExtensionCall extension)
   {
      if(extension == null)
         throw new IllegalArgumentException("extension may not be null.");

      m_extension = extension;
   }

   /**
    * Sets the conditions to be satisfied to execute this object's extension.
    *
    * @param conds list of conditions, may not be <code>null</code>, can be
    * empty.
    */
   public void setConditions(Iterator conds)
   {
      if(conds == null)
         throw new IllegalArgumentException("conds may not be null.");

      m_conditions.clear();
      while(conds.hasNext())
         m_conditions.add(conds.next());
   }


   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component. Derived classes should implement this method for their data,
    * calling the base class method first.
    *
    * @param c a valid PSConditionalExtension, not <code>null</code>.
    */
   public void copyFrom(PSConditionalExtension c)
   {
      try
      {
         super.copyFrom(c);
      }
      catch (IllegalArgumentException e)
      {
         throw new IllegalArgumentException(e.getLocalizedMessage());
      }

      m_extension = c.m_extension;
      m_conditions = c.m_conditions;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSConditionalExtension)) return false;
      if (!super.equals(o)) return false;
      PSConditionalExtension that = (PSConditionalExtension) o;
      return Objects.equals(m_extension, that.m_extension) &&
              Objects.equals(m_conditions, that.m_conditions);
   }

   @Override
   public int hashCode() {
      return Objects.hash(super.hashCode(), m_extension, m_conditions);
   }

   /**
    * Makes a deep copy of this object.
    *
    * @return a deep copy of this object, never <code>null</code>.
    */
   public Object clone()
   {
      PSConditionalExtension exit = (PSConditionalExtension) super.clone();
      if (m_conditions != null)
         exit.m_conditions = (PSCollection) m_conditions.clone();
      if (m_extension != null)
         exit.m_extension = (PSExtensionCall) m_extension.clone();

      return exit;
   }

   /**
    * Returns the extension.
    *
    * @return the extension, never <code>null</code>.
    */
   public PSExtensionCall getExtension()
   {
      return m_extension;
   }

   /**
    * Get the current collection of conditions (a collection of {@link PSRule}
    * objects).
    *
    * @return the collection of conditions {@link PSRule}, never
    *    <code>null</code>, may be empty.
    */
   public Iterator getConditions()
   {
      return m_conditions.iterator();
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

         // REQUIRED: the PSXExtensionCall element
         node = tree.getNextElement(PSExtensionCall.ms_NodeType, firstFlags);
         if (node == null)
         {
            Object[] args =
            {
               XML_NODE_NAME,
               PSExtensionCall.ms_NodeType,
               "null"
            };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
         }
         m_extension = new PSExtensionCall(node, parentDoc, parentComponents);

         // OPTIONAL: get the conditions
         node = tree.getNextElement(CONDITIONS_ELEM, nextFlags);
         m_conditions.clear();
         if (node != null)
         {
            node = tree.getNextElement(PSRule.XML_NODE_NAME, firstFlags);
            while (node != null)
            {
               m_conditions.add(new PSRule(node, parentDoc, parentComponents));

               node = tree.getNextElement(PSRule.XML_NODE_NAME, nextFlags);
            }
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

      // store the effect
      root.appendChild(m_extension.toXml(doc));

      // store the conditions
      Iterator conditions = getConditions();
      if (conditions.hasNext())
      {
         Element elem = doc.createElement(CONDITIONS_ELEM);
         while (conditions.hasNext())
            elem.appendChild(((IPSComponent) conditions.next()).toXml(doc));

         root.appendChild(elem);
      }

      return root;
   }

   /** the XML node name */
   public static final String XML_NODE_NAME = "PSXConditionalExtension";

   /**
    * Holds the extension to be executed, initialized in ctor, never changed
    * or <code>null</code> after that.
    */
   private PSExtensionCall m_extension = null;

   /**
    * A collection of conditions ({@link PSRule} objects) that specify if the
    * extension is being executed or not. Initialized in the ctor, may be
    * modified through a call to <code>setConditions(Iterator)</code>.
    * Never <code>null</code>, might be empty.
    */
   private PSCollection m_conditions = new PSCollection(PSRule.class);

   /*
    * The following strings define all elements/attributes used to parse/create
    * the XML for this object. No Java documentation will be added to this.
    */
   private static final String CONDITIONS_ELEM = "Conditions";
}
