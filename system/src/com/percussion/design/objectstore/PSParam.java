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

import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation for the PSXParam DTD in BasicObjects.dtd.
 */
public class PSParam extends PSComponent implements IPSParameter
{
   /**
    * Constructs a new parameter for the provided name.
    *
    * @param name the parameter name, not <code>null</code> or empty.
    * @param value the parameter value, not <code>null</code>.
    */
   public PSParam(String name, IPSReplacementValue value)
   {
      setName(name);
      setValue(value);
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
   public PSParam(Element sourceNode, IPSDocument parentDoc,
                  ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Needed for serialization.
    */
   protected PSParam()
   {
   }

   // see interface for description
   public Object clone()
   {
      PSParam copy = (PSParam) super.clone();
      copy.m_value = (IPSReplacementValue) m_value.clone();
      copy.m_sourceType = m_sourceType;

      return copy;
   }

   /**
    * Get the parameter name.
    *
    * @return the parameter name, never <code>null</code> or empty.
    */
   public String getName()
   {
      return m_name;
   }

   /**
    * Set the parameter name.
    *
    * @param name the new parameter name, not <code>null</code> or empty.
    */
   public void setName(String name)
   {
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name cannot be null or empty");

      m_name = name;
   }

   /**
    * Get the parameter type.
    *
    * @return the parameter type.
    */
   public String getType()
   {
      return m_value.getValueType();
   }

   /**
    * Get the parameter value.
    *
    * @return the current parameter value, never
    *    <code>null</code>.
    */
   public IPSReplacementValue getValue()
   {
      return m_value;
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
    * Get the parameter value as string.
    *
    * @return the current parameter string value, might be
    *    <code>null</code>.
    */
   public String toString()
   {
      return m_value.getValueText();
   }

   /**
    * Set the new parameter value.
    *
    * @param value the new parameter value, not <code>null</code>.
    */
   public void setValue(IPSReplacementValue value)
   {
      if (value == null)
         throw new IllegalArgumentException("the value cannot be null");

      m_value = value;
   }

   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component. Derived classes should implement this method for their data,
    * calling the base class method first.
    *
    * @param c a valid PSParam, not <code>null</code>.
    */
   public void copyFrom(PSParam c)
   {
      try
      {
         super.copyFrom(c);
      }
      catch (IllegalArgumentException e)
      {
         throw new IllegalArgumentException(e.getMessage());
      }

      setName(c.getName());
      setValue(c.getValue());
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
      if (!(o instanceof PSParam))
         return false;

      PSParam t = (PSParam) o;

      boolean equal = true;
      if (!compare(getName(), t.getName()))
         equal = false;
      else if (!compare(m_value, t.m_value))
         equal = false;
      else if (!compare(m_sourceType, t.m_sourceType))
         equal = false;

      return equal;
   }
   
   /* (non-Javadoc)
    * @see java.lang.Object#hashCode()
    */
   public int hashCode()
   {
      int hash = 0;
      
      if (m_name != null)
      {
         hash += m_name.hashCode();
      }
      if (m_sourceType != null)
      {
         hash += m_sourceType.hashCode();
      }
      if (m_value != null)
      {
         hash += m_value.hashCode();
      }
      return hash;
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

         // REQUIRED: get the name attribute
         m_name = tree.getElementData(NAME_ATTR);
         if (m_name == null || m_name.trim().length() == 0)
         {
            Object[] args =
            {
               XML_NODE_NAME,
               NAME_ATTR,
               "null"
            };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
         }

         // REQUIRED: get the parameter value
         node = tree.getNextElement(DATA_LOCATOR_ELEM, firstFlags);
         if (node == null)
         {
            Object[] args =
            {
               XML_NODE_NAME,
               DATA_LOCATOR_ELEM,
               "null"
            };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
         }
         node = tree.getNextElement(true);
         if (node == null)
         {
            Object[] args =
            {
               XML_NODE_NAME,
               DATA_LOCATOR_ELEM,
               "null"
            };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
         }
         m_value = PSReplacementValueFactory.getReplacementValueFromXml(
            parentDoc, parentComponents, node, XML_NODE_NAME, XML_NODE_NAME);
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
      root.setAttribute(NAME_ATTR, m_name);

      // REQUIRED: the parameter value
      Element elem = doc.createElement(DATA_LOCATOR_ELEM);
      elem.appendChild(((IPSComponent) m_value).toXml(doc));
      root.appendChild(elem);

      return root;
   }

   // see IPSComponent
   public void validate(IPSValidationContext context)
      throws PSValidationException
   {
      if (!context.startValidation(this, null))
         return;

      if (m_name == null || m_name.trim().length() == 0)
         context.validationError(this,
            IPSObjectStoreErrors.INVALID_PARAM, null);

      // do children
      context.pushParent(this);
      try
      {
         if (m_value != null)
         {
            if (m_value instanceof IPSComponent)
               ((IPSComponent) m_value).validate(context);
         }
         else
            context.validationError(this,
               IPSObjectStoreErrors.INVALID_PARAM, null);
      }
      finally
      {
         context.popParent();
      }
   }

   /** the XML node name */
   public static final String XML_NODE_NAME = "PSXParam";

   /** The paramter name, never <code>null</code> or empty after construction */
   private String m_name = null;

   /** The parameter value, never <code>null</code> after construction  */
   private IPSReplacementValue m_value = null;

  /**
   * Indicates where the definition of this parameter was located. If a
   * parameter is originally defined in the system def, then overridden in the
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
   private static final String DATA_LOCATOR_ELEM = "DataLocator";
   private static final String NAME_ATTR = "name";


}

