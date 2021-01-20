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

import java.util.ArrayList;
import java.util.Iterator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implements the PSXConditionalRequest DTD defined in BasicObjects.dtd.
 */
public class PSConditionalRequest extends PSUrlRequest
{
   /**
    * Creates a new conditional request for the provided conditions.
    *
    * @param request a valid request object, not <code>null</code>.
    * @param conditions a collection of PSRule objects, never
    *    <code>null</code> or empty.
    * @throws IllegalArgumentException if the request is <code>null</code>,
    *    the provided conditions are <code>null</code>, empty or of a wrong
    *    type.
    */
   public PSConditionalRequest(PSUrlRequest request,
                               PSCollection conditions)
   {
      super(request);
      setConditions(conditions);
   }

   /**
    * Creates a new conditional request for the provided request URL parts
    * and conditions.
    *
    * @param name an optional name, must be unique within the document in
    *    which it is used. May be <code>null</code>, not empty.
    * @param href the base URL part with the query string, never
    *    <code>null</code> or empty.
    * @param parameters a collection of PSParam objects, never
    *    <code>null</code>, may be empty.
    * @throws IllegalArgumentException if the href is <code>null</code> or
    *    empty or if parameters is <code>null</code>.
    * @param conditions a collection of PSRule objects, never
    *    <code>null</code> or be empty.
    * @throws IllegalArgumentException if the href is <code>null</code> or
    *    empty or if parameters is <coed>null</code> or if the provided
    *    conditions are <code>null</code>, empty or are of a wrong object
    *    type.
    */
   public PSConditionalRequest(String name, String href,
                               PSCollection parameters,
                               PSCollection conditions)
   {
      super(name, href, parameters);
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
   public PSConditionalRequest(Element sourceNode, IPSDocument parentDoc,
                               ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      fromXml(sourceNode, parentDoc, parentComponents);
   }


   // see interface for description
   public Object clone()
   {
      PSConditionalRequest copy = (PSConditionalRequest) super.clone();
      // clone the PSCollection
      copy.m_conditions = new PSCollection( PSRule.class );
      for (int i = 0; i < m_conditions.size(); i++)
      {
         PSRule rule = (PSRule) m_conditions.elementAt( i );
         copy.m_conditions.add( i , rule.clone() );
      }
      return copy;
   }


   /**
    * Get the conditions.
    *
    * @return a collection of conditions for this request, never
    *    <code>null</code>, might be empty.
    */
   public Iterator getConditions()
   {
      return m_conditions.iterator();
   }

   /**
    * Set new conditions.
    *
    * @param conditions a collection of PSRule objects, never
    *    <code>null</code> or empty.
    * @throws IllegalArgumentException if the provided conditions are
    *    <code>null</code>, empty or of a wrong type.
    */
   public void setConditions(PSCollection conditions)
   {
      if (conditions == null || conditions.isEmpty())
         throw new IllegalArgumentException(
            "conditions cannot be null or empty");

      if (!conditions.getMemberClassName().equals(
          m_conditions.getMemberClassName()))
         throw new IllegalArgumentException(
            "PSRule collection expected");

      m_conditions.clear();
      m_conditions.addAll(conditions);
   }

   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component. Derived classes should implement this method for their data,
    * calling the base class method first.
    *
    * @param c a valid PSConditionalRequest, not <code>null</code>.
    * @throws IllegalArgumentException if c is <code>null</code>.
    */
   public void copyFrom(PSConditionalRequest c)
   {
      super.copyFrom(c);

      m_conditions = c.m_conditions;
   }

   /**
    * Test if the provided object and this are equal.
    *
    * @param o the object to compare to.
    * @return <code>true</code> if this and o are equal,
    *    <code>false</code> otherwise.
    */
   public boolean equals(Object o)
   {
      if (!(o instanceof PSConditionalRequest))
         return false;

      PSConditionalRequest t = (PSConditionalRequest) o;

      boolean equal = true;
      if (!compare(m_conditions, t.m_conditions))
         equal = false;
      else
         equal = super.equals(o);

      return equal;
   }

   /**
    * Generates code of the object. Overrides {@link Object#hashCode().
    */
   @Override
   public int hashCode()
   {
      return super.hashCode();
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

      String data = null;
      Element node = null;
      try
      {
         PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);

         node = tree.getNextElement(PSUrlRequest.XML_NODE_NAME, firstFlags);

         // restore PSUrlRequest
         super.fromXml(node, parentDoc, parentComponents);

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
      throws PSValidationException
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
               IPSObjectStoreErrors.INVALID_CONDITIONAL_REQUEST, null);
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
   public static final String XML_NODE_NAME = "PSXConditionalRequest";

   /**
    * A collection of PSRule objects, never <code>null</code>, might be empty
    * after construction.
    */
   private PSCollection m_conditions = new PSCollection( PSRule.class );

   /*
    * The following strings define all elements/attributes used to create the
    * XML output for this object. No Java documentation will be added to this.
    */
   private static final String CONDITIONS_ELEM = "Conditions";
}

