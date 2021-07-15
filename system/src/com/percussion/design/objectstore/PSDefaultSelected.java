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
import java.util.Objects;

/**
 * Implementation for the PSXDefaultSelected DTD in BasicObjects.dtd.
 */
public class PSDefaultSelected extends PSComponent
{
   /**
    * Creates a new default selected of type 'nullEntry'.
    */
   public PSDefaultSelected()
   {
      m_type = TYPE_NULL_ENTRY;
   }

   /**
    * Creates a new default selected of type 'sequence'.
    *
    * @param sequence the sequence number, must be greater or equals to 0.
    */
   public PSDefaultSelected(int sequence)
   {
      if (sequence < 0)
         throw new IllegalArgumentException("sequence cannot be lower than 0");

      m_sequence = sequence;
      m_type = TYPE_SEQUENCE;
   }

   /**
    * Creates a new default selected of type 'text'.
    *
    * @param text the default selected text, not <code>null</code> or empty.
    */
   public PSDefaultSelected(String text)
   {
      if (text == null || text.trim().length() == 0)
         throw new IllegalArgumentException("text cannot be null or empty");

      m_text = text;
      m_type = TYPE_TEXT;
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
   public PSDefaultSelected(Element sourceNode, IPSDocument parentDoc,
                            ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Get the sequence.
    *
    * @return the sequence, is -1 if type is not 'sequence'.
    */
   public int getSequence()
   {
      return m_sequence;
   }

   /**
    * Get the text.
    *
    * @return the text, <code>null</code> if the type is not 'text'.
    */
   public String getText()
   {
      return m_text;
   }

   /**
    * Get the type.
    *
    * @return the type of this default selected object.
    */
   public int getType()
   {
      return m_type;
   }

   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component. Derived classes should implement this method for their data,
    * calling the base class method first.
    *
    * @param c a valid PSDefaultSelected, not <code>null</code>.
    */
   public void copyFrom(PSDefaultSelected c)
   {
      try
      {
         super.copyFrom(c);
      }
      catch (IllegalArgumentException e)
      {
         throw new IllegalArgumentException(e.getLocalizedMessage());
      }

      m_type = c.getType();
      m_sequence = c.getSequence();
      m_text = c.getText();
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSDefaultSelected)) return false;
      if (!super.equals(o)) return false;
      PSDefaultSelected that = (PSDefaultSelected) o;
      return m_type == that.m_type &&
              m_sequence == that.m_sequence &&
              Objects.equals(m_text, that.m_text);
   }

   @Override
   public int hashCode() {
      return Objects.hash(super.hashCode(), m_type, m_sequence, m_text);
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

         // OPTIONAL: get the type attribute
         data = tree.getElementData(TYPE_ATTR);
         if (data != null)
         {
            boolean found = false;
            for (int i=0; i<TYPE_ENUM.length; i++)
            {
               if (TYPE_ENUM[i].equalsIgnoreCase(data))
               {
                  m_type = i;
                  found = true;
                  break;
               }
            }
            if (!found)
            {
               Object[] args =
               {
                  XML_NODE_NAME,
                  TYPE_ATTR,
                  data
               };
               throw new PSUnknownNodeTypeException(
                  IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
            }
         }

         // REQUIRED: get the default selected depending on type
         if (m_type == TYPE_SEQUENCE)
         {
            if (sourceNode == null)
            {
               Object[] args =
               {
                  XML_NODE_NAME,
                  "Default Number",
                  "null"
               };
               throw new PSUnknownNodeTypeException(
                  IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
            }
            m_sequence = Integer.parseInt(tree.getElementData(sourceNode));
         }
         else if (m_type == TYPE_TEXT)
         {
            if (sourceNode == null)
            {
               Object[] args =
               {
                  XML_NODE_NAME,
                  "Default Text",
                  "null"
               };
               throw new PSUnknownNodeTypeException(
                  IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
            }
            m_text = tree.getElementData(sourceNode);
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
      root.setAttribute(TYPE_ATTR, TYPE_ENUM[m_type]);

      String value = null;
      if (m_type == TYPE_SEQUENCE)
         value = Integer.toString(m_sequence);
      else if (m_type == TYPE_TEXT)
         value = m_text;

      if (value != null)
         root.appendChild(doc.createTextNode(value));

      return root;
   }

   // see IPSComponent
   public void validate(IPSValidationContext context)
      throws PSSystemValidationException
   {
      if (!context.startValidation(this, null))
         return;

      if (m_type != TYPE_NULL_ENTRY &&
          m_type != TYPE_SEQUENCE &&
          m_type != TYPE_TEXT)
      {
         Object[] args = { TYPE_ENUM };
         context.validationError(this,
            IPSObjectStoreErrors.UNSUPPORTED_DEFAULT_SELECTED_TYPE, args);
      }

      if (m_type == TYPE_SEQUENCE && m_sequence < 0)
      {
         context.validationError(this,
            IPSObjectStoreErrors.INVALID_DEFAULT_SELECTED, null);
      }
      else if (m_type == TYPE_TEXT &&
               (m_text == null || m_text.trim().length() == 0))
      {
         context.validationError(this,
            IPSObjectStoreErrors.INVALID_DEFAULT_SELECTED, null);
      }
   }

   /** the XML node name */
   public static final String XML_NODE_NAME = "PSXDefaultSelected";

   /**
    * Null entry type specifier. Specify this type to choose the associated
    * PSNullEntry as the default.
    */
   public static final int TYPE_NULL_ENTRY = 0;

   /**
    * Sequence type specifier. Specify this type to choose the element with
    * the specified sequence number as default.
    */
   public static final int TYPE_SEQUENCE = 1;

   /**
    * Text type specifier. Specify this to check the value against each entry.
    * The first matching entry (case insensitive) will become the default.
    */
   public static final int TYPE_TEXT = 2;

   /**
    * An array of XML attribute values for the type. They are
    * specified at the index of the specifier.
    */
   private static final String[] TYPE_ENUM =
   {
      "nullEntry", "sequence", "text"
   };

   /**
    * If an entry in the database is <code>null</code> or empty, this
    * specifies how to choose the default value.
    */
   private int m_type = TYPE_NULL_ENTRY;

   /**
    * The sequence number is only used if type is 'sequence'. -1 means not
    * specified.
    */
   private int m_sequence = -1;

   /**
    * The text string is only used if type is 'text', might be
    * <code>null</code>.
    */
   private String m_text = null;

   /*
    * The following strings define all elements/attributes used to create the
    * XML output for this object. No Java documentation will be added to this.
    */
   private static final String TYPE_ATTR = "type";
}

