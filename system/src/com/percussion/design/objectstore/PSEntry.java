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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation for the PSXEntry DTD in BasicObjects.dtd.
 */
public class PSEntry extends PSComponent
{
   /**
    * Creates a new entry for the provided parameters.
    *
    * @param value the value of this entry, not <code>null</code>,may be empty.
    * @param label the label for this entry, not <code>null</code>, may be
    *    empty.
    */
   public PSEntry(String value, String label)
   {
      this(value, new PSDisplayText(label));
   }

   /**
    * Creates a new entry for the provided parameters.
    *
    * @param value the value of this entry, not <code>null</code> or empty.
    * @param label the label for this entry, not <code>null</code>, may be
    *    empty.
    */
   public PSEntry(String value, PSDisplayText label)
   {
      setValue(value);
      setLabel(label);
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
   public PSEntry(Element sourceNode, IPSDocument parentDoc,
                  ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      // allow subclasses to override (don't use "this")
      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Constructor for XML serialization by subclasses.
    */
   protected PSEntry()
   {
   }

   // see interface for description
   public Object clone()
   {
      PSEntry copy = (PSEntry) super.clone();
      copy.m_label = (PSDisplayText) m_label.clone();
      copy.m_sourceType = m_sourceType;

      return copy;
   }


   /**
    * Is this the default entry.
    *
    * @return <code>true</code> if default, <code>false</code> otherwise.
    */
   public boolean isDefault()
   {
      return m_default;
   }

   /**
    * Set a new default status.
    *
    * @param defaultStatus <code>true</code> to set this as default,
    *    <code>false</code> otherwise.
    */
   public void setDefault(boolean defaultStatus)
   {
      m_default = defaultStatus;
   }

   /**
    * Get the value of this entry.
    *
    * @return the entry value, never <code>null</code>.
    */
   public String getValue()
   {
      return m_value;
   }

   /**
    * Set the value of this entry.
    *
    * @param value the new value, not <code>null</code>.
    * @throws IllegalArgumentException if the provided value is
    *    <code>null</code>.
    */
   public void setValue(String value)
   {
      if (value == null)
         throw new IllegalArgumentException("the value cannot be null");

      m_value = value;
   }

   /**
    * Set the source type. See {#link getSourceType()} for allowed types.
    *
    * @param sourceType the new source type to set, may be <code>null</code>
    *    but not empty.
    */
   public void setSourceType(String sourceType)
   {
      PSContentEditorMapper.validateSourceType(sourceType);
      m_sourceType = sourceType;
   }

   /**
    * Get the source type, one of
    * <code>PSContentEditorMapper.SYSTEM</code>,
    * <code>PSContentEditorMapper.SHARED</code> or
    * <code>PSContentEditorMapper.LOCAL</code>, defaults to
    * <code>PSContentEditorMapper.SYSTEM</code>.
    *
    * @return the source type. Can be <code>null</code> but not empty.
    */
   public String getSourceType()
   {
      return m_sourceType;
   }

   /**
    * Converts this <code>PSEntry</code> object to a String by getting the
    * display text for the label.
    *
    * @return the display text, never <code>null</code>, may be empty.
    */
   public String toString()
   {
      return getLabel().getText();
   }


   /**
    * Get the display text of this entry.
    *
    * @return the current display text, never <code>null</code>.
    */
   public PSDisplayText getLabel()
   {
      return m_label;
   }

   /**
    * Set a new display text for this entry.
    *
    * @param label the new display text, never <code>null</code>.
    */
   public void setLabel(PSDisplayText label)
   {
      if (label == null)
         throw new IllegalArgumentException("the label cannot be null");

      m_label = label;
   }

   /**
    * Get the sequence number for this entry.
    *
    * @return the current sequence number, -1 if not specified.
    */
   public int getSequence()
   {
      return m_sequence;
   }

   /**
    * Set a new sequence number, -1 to unspecify.
    *
    * @param sequence the new sequence number.
    */
   public void setSequence(int sequence)
   {
      if (sequence < 0)
         m_sequence = -1;
      else
         m_sequence = sequence;
   }

   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component. Derived classes should implement this method for their data,
    * calling the base class method first.
    *
    * @param c a valid PSEntry, not <code>null</code>.
    */
   public void copyFrom(PSEntry c)
   {
      try
      {
         super.copyFrom(c);
      }
      catch (IllegalArgumentException e)
      {
         throw new IllegalArgumentException(e.getLocalizedMessage());
      };

      setLabel(c.getLabel());
      setValue(c.getValue());
      m_default = c.m_default;
      m_sequence = c.m_sequence;
      m_sourceType = c.m_sourceType;
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
      if (!(o instanceof PSEntry))
         return false;

      PSEntry t = (PSEntry) o;

      boolean equal = true;
      if (!compare(m_label, t.m_label))
         equal = false;
      else if (!compare(getValue(), t.getValue()))
         equal = false;
      else if (m_default != t.m_default)
         equal = false;
      else if (m_sequence != t.m_sequence)
         equal = false;
      else if (!compare(m_sourceType, t.m_sourceType))
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
      int bool = m_default ? 1 : 0;
      int type = 
         m_sourceType != null ? m_sourceType.hashCode() : 0;
      return super.hashCode() +
      m_label.hashCode() +
      getValue().hashCode() +
      bool +
      m_sequence +
      type;            
      
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

         // OPTIONAL: get sequence attribute
         data = tree.getElementData(SEQUENCE_ATTR);
         if (data != null)
         {
            //If the data is not a number catch NumberFormatException and
            //continue assuming sequence attribute is not specified
            int test = -1;
            try {
               test = Integer.parseInt(data);
            }
            catch(NumberFormatException nfe){
               // ignore the exception
            }
            m_sequence = test;
         }

         // OPTIONAL: get default attribute
         data = tree.getElementData(DEFAULT_ATTR);
         if (data != null)
            m_default =
               data.equalsIgnoreCase(BOOLEAN_ENUM[0]) ? true : false;

         // REQUIRED: get the label
         node = tree.getNextElement(PSDisplayText.XML_NODE_NAME, firstFlags);
         if (node == null)
         {
            Object[] args =
            {
               XML_NODE_NAME,
               PSDisplayText.XML_NODE_NAME,
               "null"
            };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
         }
         m_label = new PSDisplayText(node, parentDoc, parentComponents);

         // REQUIRED: get the entry value
         node = tree.getNextElement(VALUE_ELEM, nextFlags);
         if (node == null)
         {
            Object[] args =
            {
               XML_NODE_NAME,
               VALUE_ELEM,
               "null"
            };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
         }
         m_value = tree.getElementData(node);
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
      root.setAttribute(SEQUENCE_ATTR, Integer.toString(m_sequence));
      root.setAttribute(DEFAULT_ATTR,
         m_default ? BOOLEAN_ENUM[0] : BOOLEAN_ENUM[1]);

      // REQUIRED: create label
      root.appendChild(m_label.toXml(doc));

      // REQUIRED: create value
      PSXmlDocumentBuilder.addElement(doc, root, VALUE_ELEM, m_value);

      return root;
   }

   // see IPSComponent
   public void validate(IPSValidationContext context)
      throws PSSystemValidationException
   {
      if (!context.startValidation(this, null))
         return;

      if (m_value == null)
      {
         context.validationError(this,
            IPSObjectStoreErrors.INVALID_ENTRY, null);
      }

      // do children
      context.pushParent(this);
      try
      {
         if (m_label == null)
         {
            context.validationError(this,
               IPSObjectStoreErrors.INVALID_ENTRY, null);
         }
         else
            m_label.validate(context);
      }
      finally
      {
         context.popParent();
      }
   }

   /** the XML node name */
   public static final String XML_NODE_NAME = "PSXEntry";

   /**
    * An array of XML attribute values for all boolean attributes. They are
    * ordered as <code>true</code>, <code>false</code>.
    */
   private static final String[] BOOLEAN_ENUM =
   {
      "yes", "no"
   };

   /**
    * This is used for output documents. Whichever entries are marked as
    * default by the definition, the output document will set this attribute
    * on those entries.
    */
   private boolean m_default = false;

   /**
    * What order is this entry relative to others. Whether it is used is
    * determined by the container. -1 specifies not used.
    */
   private int m_sequence = -1;

   /**
    * The value of this entry, never <code>null</code>, may be  empty after
    * construction.
    */
   private String m_value = null;

   /**
    * The display text for this entry, never <code>null</code> after
    * construction.
    */
   private PSDisplayText m_label = null;

  /**
   * Indicates where the definition of this entry was located. If an
   * entry is originally defined in the system def, then overridden in the
   * local def, this value will be <code>PSContentEditorMapper.LOCAL</code>.
   * Allowed values are <code>PSContentEditorMapper.SYSTEM</code>,
   * <code>PSContentEditorMapper.SHARED</code> and
   * <code>PSContentEditorMapper.LOCAL</code>. This attribute will not be
   * persisted, and is therefore excluded from to/from XML methods. It will
   * be included in all other operations like cloning, comparing, etc.
   */
   private String m_sourceType = null;

   /*
    * The following strings define all elements/attributes used to create the
    * XML output for this object. No Java documentation will be added to this.
    */
   private static final String VALUE_ELEM = "Value";
   private static final String SEQUENCE_ATTR = "sequence";
   private static final String DEFAULT_ATTR = "default";
}

