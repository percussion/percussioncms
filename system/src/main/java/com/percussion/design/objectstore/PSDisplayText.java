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
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;

/**
 * Implementation for the PSXDisplayText DTD in BasicObjects.dtd.
 */
public class PSDisplayText extends PSComponent
{
   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   /**
    * Creates a new display text object.
    *
    * @param text the display text, not <code>null</code>, may be empty.
    */
   public PSDisplayText(String text)
   {
      setText(text);
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
   public PSDisplayText(Element sourceNode, IPSDocument parentDoc,
                        ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Needed for serialization.
    */
   protected PSDisplayText()
   {
   }

   /**
    * Get the display text.
    *
    * @return the display text, never <code>null</code>, may be empty.
    */
   public String getText()
   {
      return m_text;
   }

   /**
    * Set the new display text.
    *
    * @param text the new display text, not <code>null</code>, may be empty.
    */
   public void setText(String text)
   {
      if (text == null)
         throw new IllegalArgumentException("the text cannot be null");

      m_text = text;
   }

   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component. Derived classes should implement this method for their data,
    * calling the base class method first.
    *
    * @param c a valid PSDisplayText, not <code>null</code>.
    */
   public void copyFrom(PSDisplayText c)
   {
      try
      {
         super.copyFrom(c);
      }
      catch (IllegalArgumentException e)
      {
         throw new IllegalArgumentException(e.getLocalizedMessage());
      }

      setText(c.getText());
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
      if (!(o instanceof PSDisplayText))
         return false;

      PSDisplayText t = (PSDisplayText) o;

      boolean equal = true;
      if (!compare(getText(), t.getText()))
         equal = false;

      return equal;
   }

   /*
    *  (non-Javadoc)
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode()
   {
      return new HashCodeBuilder().append(m_text).toHashCode();
   }


   /*
    *  (non-Javadoc)
    * @see com.percussion.design.objectstore.IPSComponent#fromXml(org.w3c.dom.Element, com.percussion.design.objectstore.IPSDocument, java.util.ArrayList)
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

         setText(tree.getElementData(sourceNode));
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

      root.appendChild(doc.createTextNode(getText()));

      return root;
   }

   // see IPSComponent
   public void validate(IPSValidationContext context)
      throws PSSystemValidationException
   {
      if (!context.startValidation(this, null))
         return;

      if (m_text == null)
      {
         context.validationError(this,
            IPSObjectStoreErrors.INVALID_DISPLAY_TEXT, null);
      }
   }

   /** the XML node name */
   public static final String XML_NODE_NAME = "PSXDisplayText";

   /**
    * The display text, never <code>null</code> after construction, may be
    * empty.
    */
   private String m_text = null;
}

