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

