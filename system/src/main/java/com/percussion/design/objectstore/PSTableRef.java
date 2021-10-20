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

import java.util.List;
import java.util.Objects;

/**
 * Implementation for the PSXTableRef DTD in BasicObjects.dtd.
 */
public class PSTableRef extends PSComponent
{
   /**
    * Create a new table refernece.
    *
    * @param name the table name, never <code>null</code> or empty.
    */
   public PSTableRef(String name)
   {
      this(name, null);
   }

   /**
    * Create a new table refernece.
    *
    * @param name the table name, never <code>null</code> or empty.
    * @param alias the table alias, might be <code>null</code> or empty.
    */
   public PSTableRef(String name, String alias)
   {
      setName(name);
      setAlias(alias);
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
   public PSTableRef(Element sourceNode, IPSDocument parentDoc,
                     List parentComponents)
      throws PSUnknownNodeTypeException
   {
      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Needed for serialization.
    */
   protected PSTableRef()
   {
   }

   /**
    * Get the table name.
    *
    * @return the table name, never <code>null</code> or empty.
    */
   public String getName()
   {
      return m_name;
   }

   /**
    * Set a new table name.
    *
    * @param name the new table name, not <code>null</code> or empty.
    */
   public void setName(String name)
   {
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name cannot be null or empty");

      m_name = name;
   }

   /**
    * Get the table alias.
    *
    * @return the table alias, never <code>null</code> or empty.
    */
   public String getAlias()
   {
      return m_alias;
   }

   /**
    * Set a new table alias. If <code>null</code> or empty is passed the
    * alias will be set to the name.
    *
    * @param alias the new table alias, might be <code>null</code> or empty.
    */
   public void setAlias(String alias)
   {
      if (alias == null || alias.trim().length() == 0)
         alias = getName();

      m_alias = alias;
   }

   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component. Derived classes should implement this method for their data,
    * calling the base class method first.
    *
    * @param c a valid PSTableRef, not <code>null</code>.
    */
   public void copyFrom(PSTableRef c)
   {
      try
      {
         super.copyFrom(c);
      }
      catch (IllegalArgumentException e)
      {
         throw new IllegalArgumentException(e.getLocalizedMessage());
      }

      m_alias = c.getAlias();
      m_name = c.getName();
   }

   /**
    * Output a human readable form of this object with the following format:
    * <p>
    * name (alias)
    */
   @Override
   public String toString()
   {
      return m_name + "(" + m_alias + ")";
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSTableRef)) return false;
      if (!super.equals(o)) return false;
      PSTableRef that = (PSTableRef) o;
      return Objects.equals(m_name, that.m_name) &&
              Objects.equals(m_alias, that.m_alias);
   }

   @Override
   public int hashCode() {
      return Objects.hash(super.hashCode(), m_name, m_alias);
   }

   /**
    *
    * @see IPSComponent
    */
   public void fromXml(Element sourceNode, 
         IPSDocument parentDoc,
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

         // REQUIRED: get the table ref name
         m_name = tree.getElementData(NAME_ATTR);
         if (m_name == null)
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

         // OPTIONAL: get the table ref alias
         setAlias(tree.getElementData(ALIAS_ATTR));
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
      root.setAttribute(NAME_ATTR, m_name);
      root.setAttribute(ALIAS_ATTR, m_alias);

      return root;
   }

   // see IPSComponent
   public void validate(IPSValidationContext context)
      throws PSSystemValidationException
   {
      if (!context.startValidation(this, null))
         return;

      if (m_name == null || m_name.trim().length() == 0)
         context.validationError(this,
            IPSObjectStoreErrors.INVALID_TABLE_REF, null);
   }

   /** the XML node name */
   public static final String XML_NODE_NAME = "PSXTableRef";

   /** The table name, never <code>null</code> or empty after construction. */
   private String m_name = null;

   /** The table alias, never <code>null</code> or empty after construction. */
   private String m_alias = null;

   /*
    * The following strings define all elements/attributes used to create the
    * XML output for this object. No Java documentation will be added to this.
    */
   private static final String NAME_ATTR = "name";
   private static final String ALIAS_ATTR = "alias";
}

