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

import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A replacement value used to specify 'DisplayField' references.
 */
public class PSDisplayFieldRef
   extends PSComponent
   implements IPSMutatableReplacementValue
{

   /**
    * Creates a new display field reference for the provided name.
    *
    * @param fieldRef the field reference name, not <code>null</code> or
    *    empty.
    */
   public PSDisplayFieldRef(String fieldRef)
   {
      if (fieldRef == null || fieldRef.trim().length() == 0)
         throw new IllegalArgumentException("fieldRef cannot be null or empty");

      m_fieldRef = fieldRef;
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
   public PSDisplayFieldRef(
      Element sourceNode,
      IPSDocument parentDoc,
      ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Needed for serialization.
    */
   protected PSDisplayFieldRef()
   {
   }

   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component. Derived classes should implement this method for their data,
    * calling the base class method first.
    *
    * @param c a valid PSDisplayFieldRef, not <code>null</code>.
    */
   public void copyFrom(PSDisplayFieldRef c)
   {
      try
      {
         super.copyFrom(c);
      }
      catch (IllegalArgumentException e)
      {
         throw new IllegalArgumentException(e.getLocalizedMessage());
      };

      m_fieldRef = c.m_fieldRef;
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
      if (!(o instanceof PSDisplayFieldRef))
         return false;

      PSDisplayFieldRef t = (PSDisplayFieldRef) o;

      boolean equal = true;
      if (!compare(m_fieldRef, t.m_fieldRef))
         equal = false;

      return equal;
   }

   // see IPSReplacementValue for description
   public String getValueType()
   {
      return VALUE_TYPE;
   }

   // see IPSReplacementValue for description
   public String getValueDisplayText()
   {
      return m_fieldRef;
   }

   // see IPSReplacementValue for description
   public String getValueText()
   {
      return m_fieldRef;
   }

   /**
    * Sets the name of the object whose value will be used when this instance
    * resolves itself at runtime.
    * @param text the field reference name, not <code>null</code> or empty.
    */
   public void setValueText(String text)
   {
      if (text == null || text.trim().length() == 0)
         throw new IllegalArgumentException("fieldRef cannot be null or empty");

      m_fieldRef = text;
   }

   // see IPSComponent for description
   public void fromXml(
      Element sourceNode,
      IPSDocument parentDoc,
      ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      if (sourceNode == null)
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL,
            XML_NODE_NAME);

      if (!XML_NODE_NAME.equals(sourceNode.getNodeName()))
      {
         Object[] args = { XML_NODE_NAME, sourceNode.getNodeName()};
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE,
            args);
      }

      parentComponents = updateParentList(parentComponents);
      int parentSize = parentComponents.size() - 1;

      try
      {
         PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);

         // REQUIRED: get the table ref name
         String data = tree.getElementData(ID_ATTR);
         try
         {
            m_id = Integer.parseInt(data);
         }
         catch (Exception e)
         {
            Object[] args = { XML_NODE_NAME, ((data == null) ? "null" : data)};
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_ID,
               args);
         }

         m_fieldRef = tree.getElementData(FIELD_REF_ELEM);
         if (m_fieldRef == null)
         {
            Object[] args = { XML_NODE_NAME, FIELD_REF_ELEM, "null" };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD,
               args);
         }
      }
      finally
      {
         resetParentList(parentComponents, parentSize);
      }
   }

   // see IPSComponent for description
   public Element toXml(Document doc)
   {
      // create root and its attributes
      Element root = doc.createElement(XML_NODE_NAME);
      root.setAttribute(ID_ATTR, String.valueOf(m_id));

      PSXmlDocumentBuilder.addElement(doc, root, FIELD_REF_ELEM, m_fieldRef);

      return root;
   }

   // see IPSComponent for description
   public void validate(IPSValidationContext context)
      throws PSSystemValidationException
   {
      if (!context.startValidation(this, null))
         return;

      if (m_fieldRef == null || m_fieldRef.trim().length() == 0)
         context.validationError(
            this,
            IPSObjectStoreErrors.INVALID_FIELD,
            null);
   }

   /** The value type associated with instances of this class. */
   public static final String VALUE_TYPE = "DisplayFieldRef";

   /** the XML node name */
   public static final String XML_NODE_NAME = "PSXDisplayFieldRef";

   /**
    * The field name of the 'DisplayField' this replacement value is
    * referencing. Initialized in the constructor, never <code>null</code>
    * or empty after that.
    */
   private String m_fieldRef = null;

   /*
    * The following strings define all elements/attributes used to create the
    * XML output for this object. No Java documentation will be added to this.
    */
   private static final String FIELD_REF_ELEM = "FieldRef";

   /* (non-Javadoc)
    * @see java.lang.Object#hashCode()
    */
   public int hashCode()
   {
      if (m_fieldRef != null)
      {
         return m_fieldRef.hashCode();
      }
      else
      {
         return 0;
      }
   }
}
