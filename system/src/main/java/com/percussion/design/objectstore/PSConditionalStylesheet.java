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

import com.percussion.util.PSCollection;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Implements the PSXConditionalStylesheet DTD defined in BasicObjects.dtd.
 */
public class PSConditionalStylesheet extends PSStylesheet
{
   /**
    * Create a new conditional stylesheet for the provided request and
    * conditions.
    *
    * @param request the stylesheet request, not <code>null</code>.
    * @param conditions a collection of PSRule objects, never 
    *    <code>null</code> or empty.
    * @throws IllegalArgumentException if the stylesheet request or 
    *    conditions are <code>null</code> or if the conditions are not of 
    *    type PSRule.
    */
   public PSConditionalStylesheet(PSUrlRequest request,
                                  PSCollection conditions)
   {
      super(request);
      setConditions(conditions);
   }
   /**
    * Create a new conditional stylesheet for the provided stylesheet and
    * conditions.
    *
    * @param stylesheet the stylesheet, not <code>null</code>.
    * @param conditions a collection of PSRule objects, never 
    *    <code>null</code> or empty.
    * @throws IllegalArgumentException if the stylesheet or conditions are 
    *    <code>null</code> or if the conditions are not of type PSRule.
    */
   public PSConditionalStylesheet(PSStylesheet stylesheet,
                                  PSCollection conditions)
   {
      super(stylesheet.getRequest());
      setConditions(conditions);
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
   public PSConditionalStylesheet(Element sourceNode, IPSDocument parentDoc, 
                                  List parentComponents)
      throws PSUnknownNodeTypeException
   {
      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Needed for serialization.
    */
   protected PSConditionalStylesheet()
   {
   }

   /**
    * Get the current collection of conditions (a collection of PSRule 
    * objects).
    *
    * @return the collection (PSRule) of conditions, never 
    *    <code>null</code>, may be empty.
    */
   public Iterator getConditions()
   {
      return m_conditions.iterator();
   }
   
   /**
    * Set a new conditions collection.
    *
    * @param conditions the new collection of PSRule objects.
    * @throws IllegalArgumentException if the provided collection is
    *    <code>null</code>, empty or of the wrong object type.
    */
   public void setConditions(PSCollection conditions)
   {
      if (conditions == null)
         throw new IllegalArgumentException("conditions can't be null");
      
      if (conditions.isEmpty())
         throw new IllegalArgumentException("conditions can't be empty");
      
      if (!conditions.getMemberClassName().equals(
          m_conditions.getMemberClassName()))
         throw new IllegalArgumentException("PSRule collection expected");

      m_conditions.clear();
      m_conditions.addAll(conditions);
   }

   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component. Derived classes should implement this method for their data,
    * calling the base class method first.
    *
    * @param c a valid PSConditionalStylesheet, not <code>null</code>.
    * @throws IllegalArgumentException if c is <code>null</code>.
    */
   public void copyFrom(PSConditionalStylesheet c)
   {
      super.copyFrom(c);
      
      setConditions(c.m_conditions);
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSConditionalStylesheet)) return false;
      if (!super.equals(o)) return false;
      PSConditionalStylesheet that = (PSConditionalStylesheet) o;
      return Objects.equals(m_conditions, that.m_conditions);
   }

   @Override
   public int hashCode() {
      return Objects.hash(super.hashCode(), m_conditions);
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

      String data = null;
      Element node = null;
      try 
      {
         PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);

         node = tree.getNextElement(PSStylesheet.XML_NODE_NAME, firstFlags);

         // restore PSStylesheet
         super.fromXml(node, parentDoc, parentComponents);

         // REQUIRED: get the conditions
         node = tree.getNextElement(CONDITIONS_ELEM, nextFlags);
         if (node == null)
         {
            Object[] args =
            { 
               XML_NODE_NAME, 
               CONDITIONS_ELEM,
               "null"
            };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
         }
         node = tree.getNextElement(PSRule.XML_NODE_NAME, firstFlags);
         if (node == null)
         {
            Object[] args =
            { 
               XML_NODE_NAME, 
               PSRule.XML_NODE_NAME,
               "null"
            };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
         }
         while (node != null)
         {
            m_conditions.add(new PSRule(node, parentDoc, parentComponents));
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
      Element root = doc.createElement(XML_NODE_NAME);

      // store the base class info
      root.appendChild(super.toXml(doc));

      // REQUIRED: the conditions
      Element elem = doc.createElement(CONDITIONS_ELEM);
      Iterator it = getConditions();
      while (it.hasNext())
         elem.appendChild(((IPSComponent) it.next()).toXml(doc));
      root.appendChild(elem);

      return root;
   }

   // see IPSComponent
   public void validate(IPSValidationContext context) 
      throws PSSystemValidationException
   {
      if (!context.startValidation(this, null))
         return;

      // do children
      context.pushParent(this);
      try
      {
         super.validate(context);
         
         if (m_conditions == null)
         {
            context.validationError(this, 
               IPSObjectStoreErrors.INVALID_CONDITIONAL_STYLESHEET, null);
         }
         else
         {
            Iterator it = getConditions();
            while (it.hasNext())
               ((PSRule) it.next()).validate(context);
         }
      }
      finally
      {
         context.popParent();
      }
   }

   /** the XML node name */
   public static final String XML_NODE_NAME = "PSXConditionalStylesheet";

   /** 
    * A collection PSRule objects, never <code>null</code> after construction.
    */
   private PSCollection m_conditions = 
      new PSCollection((new PSRule()).getClass());
   
   /*
    * The following strings define all elements/attributes used to create the
    * XML output for this object. No Java documentation will be added to this.
    */
   private static final String CONDITIONS_ELEM = "Conditions";
}

