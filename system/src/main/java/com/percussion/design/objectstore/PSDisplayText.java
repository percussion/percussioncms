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
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

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
                        List parentComponents)
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

