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

import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Implementation for the PSXStylesheet DTD in BasicObjects.dtd.
 */
public class PSStylesheet extends PSComponent
{
   /**
    * Creates a new stylesheet object.
    *
    * @param request the stylesheet request, not <code>null</code>.
    */
   public PSStylesheet(PSUrlRequest request)
   {
      setRequest(request);
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
   public PSStylesheet(Element sourceNode, IPSDocument parentDoc,
                       ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Needed for serialization.
    */
   protected PSStylesheet()
   {
   }

   /**
    * Get the stylesheet request.
    *
    * @return the stylesheet request, never <code>null</code>.
    */
   public PSUrlRequest getRequest()
   {
      return m_request;
   }

   /**
    * Set a new stylesheet request.
    *
    * @param request the new stylesheet request, not <code>null</code>.
    */
   public void setRequest(PSUrlRequest request)
   {
      if (request == null)
         throw new IllegalArgumentException("the request cannot be null");

      m_request = request;
   }

   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component. Derived classes should implement this method for their data,
    * calling the base class method first.
    *
    * @param c a valid PSStylesheet, not <code>null</code>.
    */
   public void copyFrom(PSStylesheet c)
   {
      try
      {
         super.copyFrom(c);
      }
      catch (IllegalArgumentException e)
      {
         throw new IllegalArgumentException(e.getLocalizedMessage());
      };

      setRequest(c.getRequest());
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSStylesheet)) return false;
      if (!super.equals(o)) return false;
      PSStylesheet that = (PSStylesheet) o;
      return Objects.equals(m_request, that.m_request);
   }

   @Override
   public int hashCode() {
      return Objects.hash(super.hashCode(), m_request);
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

         // REQUIRED: get the request
         node = tree.getNextElement(PSUrlRequest.XML_NODE_NAME, firstFlags);
         if (node == null)
         {
            Object[] args =
            {
               XML_NODE_NAME,
               PSUrlRequest.XML_NODE_NAME,
               "null"
            };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
         }
         m_request = new PSUrlRequest(node, parentDoc, parentComponents);
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

      // REQUIRED: the request
      root.appendChild(m_request.toXml(doc));

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
         if (m_request != null)
            m_request.validate(context);
         else
            context.validationError(this,
               IPSObjectStoreErrors.INVALID_STYLESHEET, null);
      }
      finally
      {
         context.popParent();
      }
   }

   /** the XML node name */
   public static final String XML_NODE_NAME = "PSXStylesheet";

   /** The stylesheet request, never <code>null</code> after construction. */
   private PSUrlRequest m_request = null;
}

