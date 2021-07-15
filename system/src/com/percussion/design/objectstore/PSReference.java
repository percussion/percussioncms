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
 * A component that holds one reference to an other XML element.
 */
public class PSReference extends PSComponent
{
   /**
    * Construct a Java object from its XML representation.
    *
    * @param sourceNode   the XML element node to construct this object from,
    *    not <code>null</code>.
    * @param parentDoc the Java object which is the parent of this object,
    *    may be <code>null</code>.
    * @param parentComponents   the parent objects of this object, may be
    *    <code>null</code>.
    * @throws PSUnknownNodeTypeException if the XML element node is not of
    *    the appropriate type
    */
   public PSReference(Element sourceNode, IPSDocument parentDoc,
      ArrayList parentComponents) throws PSUnknownNodeTypeException
   {
      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Construct a new reference with the supplied parameters.
    *
    * @param name the name of the object that this references, not
    *    <code>null</code> or empty.
    * @param type an XML element name, specifying which XML element this
    *    references, not <code>null</code> or empty.
    */
   public PSReference(String name, String type)
   {
      setName(name);
      setType(type);
   }

   /**
    * Copy constructor.
    *
    * @param source the source object that is being copied,
    *    not <code>null</code>.
    */
   public PSReference(PSReference source)
   {
      copyFrom(source);
   }

   /**
    * @return the XML element type which this refereences, never
    *    <code>null</code> or empty.
    */
   public String getType()
   {
      return m_type;
   }

   /**
    * Test if this object is of the same type as the type provided.
    *
    * @param type the type to test for, may be <code>null</code> or empty.
    * @return <code>true</code> if the supplied type is the same as the type
    *    of this object, <code>false</code> otherwise.
    */
   public boolean isType(String type)
   {
      return getType().equals(type);
   }

   /**
    * Set a new object type.
    *
    * @param type the new object type that this should reference, not
    *    <code>null</code> or empty.
    */
   private void setType(String type)
   {
      if (type == null)
         throw new IllegalArgumentException("type cannot be null");

      type = type.trim();
      if (type.length() == 0)
         throw new IllegalArgumentException("type cannot be empty");

      m_type = type;
   }

   /**
    * @return the name of the object that this references, never
    *    <code>null</code> or empty.
    */
   public String getName()
   {
      return m_name;
   }

   /**
    * Set a new reference name.
    *
    * @param name the new name of the object that this should reference, not
    *    <code>null</code> or eempty.
    */
   public void setName(String name)
   {
      if (name == null)
         throw new IllegalArgumentException("name cannot be null");

      name = name.trim();
      if (name.length() == 0)
         throw new IllegalArgumentException("name cannot bee empty");

      m_name = name;
   }

   /** @see IPSComponent */
   public void fromXml(Element sourceNode, IPSDocument parentDoc,
      ArrayList parentComponents) throws PSUnknownNodeTypeException
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

      PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);
      setName(getRequiredElement(tree, XML_ATTR_NAME, false));
      setType(getRequiredElement(tree, XML_ATTR_TYPE, false));
   }

   /** @see IPSComponent */
   public Element toXml(Document doc)
   {
      Element root = doc.createElement(XML_NODE_NAME);
      root.setAttribute(XML_ATTR_NAME, getName());
      root.setAttribute(XML_ATTR_TYPE, getType());

      return root;
   }

   /** @see IPSComponent */
   public Object clone()
   {
      return super.clone();
   }

   /** @see PSComponent */
   public void copyFrom(PSComponent c)
   {
      super.copyFrom(c);

      if (!(c instanceof PSReference))
         throw new IllegalArgumentException("c must be a PSReference object");

      PSReference o = (PSReference) c;

      setName(o.getName());
      setType(o.getType());
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSReference)) return false;
      if (!super.equals(o)) return false;
      PSReference that = (PSReference) o;
      return Objects.equals(m_name, that.m_name) &&
              Objects.equals(m_type, that.m_type);
   }

   @Override
   public int hashCode() {
      return Objects.hash(super.hashCode(), m_name, m_type);
   }

   /** The XML node name */
   public static final String XML_NODE_NAME = "PSXReference";

   /**
    * The name of the object that this references. Initialized during
    * construction, never <code>null</code> or empty after that.
    */
   private String m_name = null;

   /**
    * The XML elemeent type that this references. Initialized during
    * construction, never <code>null</code> or empty, never changed after that.
    */
   private String m_type = null;

   // XML element and attribute constants.
   private static final String XML_ATTR_NAME = "name";
   private static final String XML_ATTR_TYPE = "type";
}
