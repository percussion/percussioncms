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
 * This class is used to define a macro definition.
 */
public class PSMacroDefinition extends PSComponent
{
   /**
    * Construct a Java object from it's XML representation.
    *
    * @param source the XML element node to construct this object from, not
    *    <code>null</code>, see {@link toXml(Document)} for the expected XML
    *    format.
    * @param parent the Java object which is the parent of this object, may be
    *    <code>null</code>.
    * @param parentComponents the parent objects of this object, may be 
    *    <code>null</code> or empty.
    * @throws PSUnknownNodeTypeException if the XML element node is not of the
    *    appropriate type.
    */
   public PSMacroDefinition(Element source, IPSDocument parent, 
      ArrayList parentComponents) throws PSUnknownNodeTypeException
   {
      fromXml(source, parent, parentComponents);
   }

   /**
    * Constructs a macro definition.
    *
    * @param name the name of the macro, not <code>null</code> or empty.
    * @param class the fully qualified class name used to extract the macro
    *    value, not <code>null</code> or empty.
    */
   public PSMacroDefinition(String name, String className)
   {
      setName(name);
      setClassName(className);
   }

   /**
    * @see PSComponent#copyFrom for descrition
    */
   public void copyFrom(PSMacroDefinition c)
   {
      super.copyFrom(c);
      
      setName(c.getName());
      setClassName(c.getClassName());
      setDescription(c.getDescription());
   }

   /**
    * Get the macro name.
    * 
    * @return the macro name, never <code>null</code> or empty.
    */
   public String getName()
   {
      return m_name;   
   }
   
   /**
    * Set the macro name.
    * 
    * @param name the new macro name, not <code>null</code> or empty.
    */
   public void setName(String name)
   {
      if (name == null)
         throw new IllegalArgumentException("name cannot be null");
         
      name = name.trim();
      if (name.length() == 0)
         throw new IllegalArgumentException("name cannot be empty");
         
      m_name = name;
   }
   
   /**
    * Get the fully qualified class name of the class used to extract the 
    * macro value at runtime.
    * 
    * @return the fully qualified class name, never <code>null</code> or
    *    empty.
    */
   public String getClassName()
   {
      return m_className;
   }
   
   /**
    * Set the name of the class used to extract the value for this macro.
    * 
    * @param className the fully qualified extractor class name, not 
    *    <code>null</code> or empty.
    */
   public void setClassName(String className)
   {
      if (className == null)
         throw new IllegalArgumentException("className cannot be null");
         
      className = className.trim();
      if (className.length() == 0)
         throw new IllegalArgumentException("className cannot be empty");
         
      m_className = className;
   }
   
   /**
    * Get the macro description.
    * 
    * @return the macro description, never <code>null</code>, may be empty.
    */
   public String getDescription()
   {
      return m_description;
   }
   
   /**
    * Set the macro description.
    * 
    * @param description the macro description, may be <code>null</code> or
    *    empty.
    */
   public void setDescription(String description)
   {
      if (description == null)
         description = "";
         
      m_description = description;
   }
   
   /**
    * This method is called to populate this instance from a XML 
    * representation. See the {@link #toXml(Document)} method for a description 
    * of the XML object.
    *
    * @param source the XML element node to construct this object from,
    *    must not be <code>null</code>.
    * @param parent may be <code>null</code>.
    * @param parentComponents may be <code>null</code>.
    * @throws PSUnknownNodeTypeException if the XML representation is not
    *    in the expected format.
    */
   public void fromXml(Element source, IPSDocument parent, 
      ArrayList parentComponents) throws PSUnknownNodeTypeException
   {
      PSXmlTreeWalker tree = new PSXmlTreeWalker(source);
      
      String strId = source.getAttribute(PSComponent.ID_ATTR);
      if (strId == null)
      {
         Object[] args = { XML_NODE_NAME, PSComponent.ID_ATTR, "null" };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
      }
      
      int id = 0;
      try
      {
         id = Integer.parseInt(strId);
      }
      catch (NumberFormatException e)
      {
         Object[] args = { XML_NODE_NAME, PSComponent.ID_ATTR, strId };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
      }
      setId(id);

      try
      {
         setName(tree.getElementData(ELEM_NAME));
      } 
      catch (IllegalArgumentException e)
      {
         Object[] args = { XML_NODE_NAME, ELEM_NAME, "null" };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
      }

      try
      {
         setClassName(tree.getElementData(ELEM_CLASS));
      } 
      catch (IllegalArgumentException e)
      {
         Object[] args = { XML_NODE_NAME, ELEM_CLASS, "null" };
         throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
      }
      
      setDescription(tree.getElementData(ELEM_DESCRIPTION));
   }

   /**
    * Creates the XML serialization for this class. The structure of the XML 
    * document conforms to this DTD:
    * <pre><code>
    * &lt;!ELEMENT PSXMacroDefinition (Name, Class, Description?)&lt;
    * &lt;!ATTLIST PSXMacroDefinition
    *    id CDATA #REQUIRED
    * >
    * &lt;!ELEMENT Name (#PCDATA)&lt;
    * &lt;!ELEMENT Class (#PCDATA)&lt;
    * &lt;!ELEMENT Description (#PCDATA)&lt;
    * </code></pre>
    *
    * @return a newly created Element, never <code>null</code>.
    */
   public Element toXml(Document doc)
   {
      Element root = doc.createElement(XML_NODE_NAME);
      root.setAttribute(PSComponent.ID_ATTR, Integer.toString(getId()));
      
      PSXmlDocumentBuilder.addElement(doc, root, ELEM_NAME, getName());
      PSXmlDocumentBuilder.addElement(doc, root, ELEM_CLASS, getClassName());
      if (getDescription().trim().length() > 0)
         PSXmlDocumentBuilder.addElement(doc, root, ELEM_DESCRIPTION, 
            getDescription());

      return root;
   }
   
   /**
    * @see java.lang.Object#equals(java.lang.Object)
    */
   public boolean equals(Object o)
   {
      if (!(o instanceof PSMacroDefinition))
         return false;
         
      PSMacroDefinition t = (PSMacroDefinition) o;
      if (!getName().equals(t.getName()))
         return false;
      if (!getClassName().equals(t.getClassName()))
         return false;
      if (!getDescription().equals(t.getDescription()))
         return false;
         
      return true;
   }
   
   /**
    * @see java.lang.Object#hashCode()
    */
   public int hashCode()
   {
      return getName().hashCode() + getClassName().hashCode();
   }

   /**
    * The XML node name, package access on this so they may reference each 
    * other in <code>fromXml</code>.
    */
   static final String XML_NODE_NAME = "PSXMacroDefinition";
   
   /**
    * The macro name as used to reference this macro definition in macro
    * repalcement values. Initialized in constructor, never <code>null</code>
    * or empty after that.
    */
   private String m_name = null;
   
   /**
    * The fully qualified class name used to extract the macro value, 
    * initialized in constructor, never <code>null</code> or empty after that.
    */
   private String m_className = null;
   
   /**
    * A description of this macro. Set through {@link setDescription(String)},
    * never <code>null</code>, may be empty.
    */
   private String m_description = "";
   
   // XML element and attributes names.
   private static final String ELEM_NAME = "Name";
   private static final String ELEM_CLASS = "Class";
   private static final String ELEM_DESCRIPTION = "Description";
}
