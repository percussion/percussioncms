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

import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import java.util.ArrayList;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation for the PSXDisplayMapping DTD in BasicObjects.dtd.
 */
public class PSDisplayMapping extends PSComponent
{
   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   /**
    * Creates a new display mapping for the provided field reference and UI
    * set.
    *
    * @param fieldRef a field reference, not <code>null</code> or empty.
    * @param uiSet a UI set, not <code>null</code>.
    */
   public PSDisplayMapping(String fieldRef, PSUISet uiSet)
   {
      setFieldRef(fieldRef);
      setUISet(uiSet);
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
   public PSDisplayMapping(Element sourceNode, IPSDocument parentDoc,
                           ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Needed for serialization.
    */
   protected PSDisplayMapping()
   {
   }


   // see interface for description
   public Object clone()
   {
      PSDisplayMapping copy = (PSDisplayMapping) super.clone();
      copy.m_uiSet = (PSUISet) m_uiSet.clone();
      if (m_displayMapper != null)
         copy.m_displayMapper = (PSDisplayMapper) m_displayMapper.clone();
      return copy;
   }


   /**
    * Get the current field reference.
    *
    * @return the field reference, never <code>null</code> or empty.
    */
   public String getFieldRef()
   {
      return m_fieldRef;
   }

   /**
    * Set a new field reference.
    *
    * @param fieldRef the new field reference, not <code>null</code> or empty.
    */
   public void setFieldRef(String fieldRef)
   {
      if (fieldRef == null || fieldRef.trim().length() == 0)
         throw new IllegalArgumentException(
         "the fieldRef cannot be null or empty");

      m_fieldRef = fieldRef;
   }

   /**
    * Get the current UI set.
    *
    * @return the UI set, never <code>null</code>.
    */
   public PSUISet getUISet()
   {
      return m_uiSet;
   }

   /**
    * Set a new UI set.
    *
    * @param uiSet the new UI set, never <code>null</code>.
    */
   public void setUISet(PSUISet uiSet)
   {
      if (uiSet == null)
         throw new IllegalArgumentException("the uiSet cannot be null");

      m_uiSet = uiSet;
   }

   /**
    * Get the display mapper.
    *
    * @return the display mapper, may be <code>null</code>.
    */
   public PSDisplayMapper getDisplayMapper()
   {
      return m_displayMapper;
   }

   /**
    * Set a new display mapper.
    *
    * @param displayMapper the new display mapper, may be <code>null</code>.
    */
   public void setDisplayMapper(PSDisplayMapper displayMapper)
   {
      m_displayMapper = displayMapper;
   }

   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component. Derived classes should implement this method for their data,
    * calling the base class method first.
    *
    * @param c a valid PSDisplayMapping, not <code>null</code>.
    */
   public void copyFrom(PSDisplayMapping c)
   {
      try
      {
         super.copyFrom(c);
      }
      catch (IllegalArgumentException e)
      {
         throw new IllegalArgumentException(e.getLocalizedMessage());
      }

      setDisplayMapper(c.getDisplayMapper());
      setFieldRef(c.getFieldRef());
      setUISet(c.getUISet());
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
      if (!(o instanceof PSDisplayMapping))
         return false;

      PSDisplayMapping t = (PSDisplayMapping) o;

      boolean equal = true;
      
      if (!compare(m_displayMapper, t.m_displayMapper))
         equal = false;
      else if (!compare(m_fieldRef, t.m_fieldRef))
         equal = false;
      else if (!compare(m_uiSet, t.m_uiSet))
         equal = false;

      return equal;
   }

   /**
    * Generates code of the object. Overrides {@link Object#hashCode().
    */
   @Override
   public int hashCode()
   {
      return new HashCodeBuilder().append(m_fieldRef).append(m_uiSet).toHashCode();
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

      try
      {
         PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);

         int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN |
            PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
         int nextFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS |
            PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

         Element node = tree.getNextElement(FIELD_REF_ELEM, firstFlags);

         // REQUIRED: get the field reference element
         m_fieldRef = tree.getElementData(node);
         if (m_fieldRef == null || m_fieldRef.trim().length() == 0)
         {
            Object[] args =
            {
               XML_NODE_NAME,
               FIELD_REF_ELEM,
               "null"
            };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
         }

         // REQUIRED: get the UI set
         node = tree.getNextElement(PSUISet.XML_NODE_NAME, nextFlags);
         if (node != null)
         {
            m_uiSet = new PSUISet(
               node, parentDoc, parentComponents);
         }
         else
         {
            Object[] args =
            {
               XML_NODE_NAME,
               PSUISet.XML_NODE_NAME,
               "null"
            };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
         }

         // OPTIONAL: get the display mapper
         node = tree.getNextElement(PSDisplayMapper.XML_NODE_NAME, nextFlags);
         if (node != null)
         {
            m_displayMapper = new PSDisplayMapper(
               node, parentDoc, parentComponents);
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

      // create the field reference
      PSXmlDocumentBuilder.addElement(doc, root, FIELD_REF_ELEM, m_fieldRef);

      // create the UI set
      root.appendChild(m_uiSet.toXml(doc));

      // create the display mapper
      if (m_displayMapper != null)
         root.appendChild(m_displayMapper.toXml(doc));

      return root;
   }

   // see IPSComponent
   public void validate(IPSValidationContext context)
      throws PSValidationException
   {
      if (!context.startValidation(this, null))
         return;

      if (m_fieldRef == null || m_fieldRef.trim().length() == 0)
      {
         context.validationError(this,
            IPSObjectStoreErrors.INVALID_DISPLAY_MAPPING, null);
      }

      // do children
      context.pushParent(this);
      try
      {
         if (m_uiSet != null)
            m_uiSet.validate(context);
         else
            context.validationError(this,
               IPSObjectStoreErrors.INVALID_DISPLAY_MAPPING, null);

         if (m_displayMapper != null)
            m_displayMapper.validate(context);
      }
      finally
      {
         context.popParent();
      }
   }

   /** the XML node name */
   public static final String XML_NODE_NAME = "PSXDisplayMapping";

   /**
    * The name of a field definition, must match an existing field name.
    * Never <code>null</code> or empty after construction.
    */
   private String m_fieldRef;

   /** The UI set, never <code>null</code> after construction. */
   private PSUISet m_uiSet;

   /** The display mapper, may be <code>null</code>. */
   private PSDisplayMapper m_displayMapper = null;

   /*
    * The following strings define all elements/attributes used to create the
    * XML output for this object. No Java documentation will be added to this.
    */
   private static final String FIELD_REF_ELEM = "FieldRef";
}

