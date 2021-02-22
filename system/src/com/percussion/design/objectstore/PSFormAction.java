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
 * Implementation for the PSFormAction DTD in BasicObjects.dtd. A form action
 * is a way for the designer to specify the 'action' attribute of the 'FORM'
 * command in an html form. It includes a url and a type.
 */
public class PSFormAction extends PSComponent
{
   /**
    * Creates a new form action for the provided parameters.
    *
    * @param method the form action method, one of METHOD_GET|METHOD_POST.
    * @param action the form action, not <code>null</code>.
    */
   public PSFormAction(int method, PSUrlRequest action)
   {
      setMethod(method);
      setAction(action);
   }

   /**
    * Construct a Java object from its XML representation. See the
    * {@link #toXml(Document) toXml} method for a description of the XML object.
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
   public PSFormAction(Element sourceNode, IPSDocument parentDoc,
                       ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      fromXml(sourceNode, parentDoc, parentComponents);
   }


   // see interface for description
   public Object clone()
   {
      PSFormAction copy = (PSFormAction) super.clone();
      copy.m_action = (PSUrlRequest) m_action.clone();
      return copy;
   }


   /**
    * Get the form method.
    *
    * @return int the form method, one of METHOD_GET|METHOD_POST.
    */
   public int getMethod()
   {
      return m_method;
   }

   /**
    * Set a new form method.
    *
    * @param method the new method, one of METHOD_GET|METHOD_POST.
    */
   public void setMethod(int method)
   {
      if (method != METHOD_GET && method != METHOD_POST)
         throw new IllegalArgumentException("unknown method");

      m_method = method;
   }

   /**
    * Get the form action.
    *
    * @return PSUrlRequest the form action, never <code>null</code>.
    */
   public PSUrlRequest getAction()
   {
      return m_action;
   }

   /**
    * Set a new form action.
    *
    * @param action the new form action, not <code>null</code>.
    */
   public void setAction(PSUrlRequest action)
   {
      if (action == null)
         throw new IllegalArgumentException("action cannot be null");

      m_action = action;
   }

   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component. Derived classes should implement this method for their data,
    * calling the base class method first.
    *
    * @param c a valid PSField, not <code>null</code>.
    */
   public void copyFrom(PSFormAction c)
   {
      try
      {
         super.copyFrom(c);
      }
      catch (IllegalArgumentException e)
      {
         throw new IllegalArgumentException(e.getLocalizedMessage());
      }

      m_method = c.m_method;
      m_action = c.m_action;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSFormAction)) return false;
      if (!super.equals(o)) return false;
      PSFormAction that = (PSFormAction) o;
      return m_method == that.m_method &&
              Objects.equals(m_action, that.m_action);
   }

   @Override
   public int hashCode() {
      return Objects.hash(super.hashCode(), m_method, m_action);
   }

   // see IPSComponent
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

         // OPTIONAL: get the method attribute
         data = tree.getElementData(METHOD_ATTR);
         if (data != null)
         {
            boolean found = false;
            for (int i=0; i<METHOD_ENUM.length; i++)
            {
               if (METHOD_ENUM[i].equalsIgnoreCase(data))
               {
                  m_method = i;
                  found = true;
                  break;
               }
            }
            if (!found)
            {
               Object[] args =
               {
                  XML_NODE_NAME,
                  METHOD_ATTR,
                  data
               };
               throw new PSUnknownNodeTypeException(
                  IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
            }
         }

         // REQUIRED: get the action
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
         else
            m_action = new PSUrlRequest(node, parentDoc, parentComponents);
      }
      finally
      {
         resetParentList(parentComponents, parentSize);
      }
   }

   // see IPSComponent
   public Element toXml(Document doc)
   {
      // create root and its attributes
      Element root = doc.createElement(XML_NODE_NAME);
      root.setAttribute(METHOD_ATTR, METHOD_ENUM[m_method]);

      // REQUIRED: create the action
      root.appendChild(m_action.toXml(doc));

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
         if (m_action != null)
            ((IPSComponent) m_action).validate(context);
         else
            context.validationError(this,
               IPSObjectStoreErrors.INVALID_FORM_ACTION, null);
      }
      finally
      {
         context.popParent();
      }
   }

   /** the XML node name */
   public static final String XML_NODE_NAME = "PSXFormAction";

   /**
    * GET method specifier. The index into the <code>METHOD_ENUM</code> array.
    */
   public static final int METHOD_GET = 0;
   /**
    * POST method specifier. The index into the <code>METHOD_ENUM</code> array.
    */
   public static final int METHOD_POST = 1;
   /**
    * An array of XML attribute values for the form method.
    * They are specified at the index of the specifier.
    */
   private static final String[] METHOD_ENUM =
   {
      "GET", "POST"
   };

   /**
    * The form action method.
    */
   private int m_method = METHOD_GET;

   /**
    * The form action, never <code>null</code> after construction.
    */
   private PSUrlRequest m_action;

   //
   //
   //
   private static final String METHOD_ATTR = "method";
}

