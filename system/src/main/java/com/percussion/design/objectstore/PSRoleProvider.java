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
 * A component that holds one directory definition used to catalog information
 * from directory servers.
 */
public class PSRoleProvider extends PSComponent
{
   /**
    * java serial id 
    */
   private static final long serialVersionUID = 1L;
   
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
   public PSRoleProvider(Element sourceNode, IPSDocument parentDoc,
      ArrayList parentComponents) throws PSUnknownNodeTypeException
   {
      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Contructs a new object for the supplied parameters.
    *
    * @param name the directory name, not <code>null</code> or empty.
    * @param type the role provider type, not <code>null</code> or
    *    empty, must be one of <code>TYPE_ENUM</code>.
    * @param directoryRef the directory reference name if the constructed object
    *    is of type <code>TYPE_DIRECTORY</code>, <code>null</code> otherwise.
    */
   public PSRoleProvider(String name, String type, String directoryRef)
   {
      setName(name);
      setType(type);
      setDirectoryRef(directoryRef);
   }

   /**
    * Contructs a new object for the supplied parameters.
    *
    * @param name the directory name, not <code>null</code> or empty.
    * @param type the role provider type, not <code>null</code> or
    *    empty, must be one of <code>TYPE_ENUM</code>.
    * @param directoryRef the directory reference if the constructed object
    *    is of type <code>TYPE_DIRECTORY</code>, <code>null</code> otherwise.
    */
   public PSRoleProvider(String name, String type, PSReference directoryRef)
   {
      setName(name);
      setType(type);
      setDirectoryRef(directoryRef);
   }

   /**
    * @return the role provider name, never <code>null</code> or empty. This
    *    name may be used to reference this role provider from other contexts.
    */
   public String getName()
   {
      return m_name;
   }

   /**
    * Set a new role provider name.
    *
    * @param name the new name for this role provider, not <code>null</code> or
    *    empty.
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
    * Set a new value for the delimiter
    * @param delimiter the new value, <code>null</code> indicates that the 
    * delimiter is not in use for this provider
    */
   public void setDelimiter(String delimiter)
   {
      m_delimiter = delimiter;
   }

   /**
    * Is this a backend role provider?
    * 
    * @return <code>true</code> if this role provider is of type
    *    <code>TYPE_BACKEND</code>, <code>false</code> otherwise.
    */
   public boolean isBackendRoleProvider()
   {
      return m_type.equals(TYPE_BACKEND);
   }

   /**
    * Is this a directory role provider?
    * 
    * @return <code>true</code> if this role provider is of type
    *    <code>TYPE_DIRECTORY</code>, <code>false</code> otherwise.
    */
   public boolean isDirectoryRoleProvider()
   {
      return m_type.equals(TYPE_DIRECTORY);
   }

   /**
    * Is this a both role provider?
    * 
    * @return <code>true</code> if this role provider is of type
    *    <code>TYPE_BOTH</code>, <code>false</code> otherwise.
    */
   public boolean isBothProvider()
   {
      return m_type.equals(TYPE_BOTH);
   }
    
    /**
     * Does this role provider use delimited values. 
     * @return <code>true</code> if this provider does use delimited values. Use
     * {@link #getDelimiter()} to retrieve the delimiter.
     */
    public boolean isDelimited()
    {
       return m_delimiter != null;
    }

    /**
     * Get the delimiter that separates role values for this provider. 
     * @return the delimiter, may be <code>null</code> if this provider does
     * not store roles as delimited values.
     */
    public String getDelimiter()
    {
       return m_delimiter;
    }
    
   /**
    * Set the new role provider type. Role provider types are case insensitive
    * and stored lowercase.
    *
    * @param type the new role provider type, not <code>null</code> or
    *    empty. Must be one of <code>TYPE_ENUM</code>.
    */
   private void setType(String type)
   {
      if (type == null)
         throw new IllegalArgumentException("type cannot be null");

      type = type.trim().toLowerCase();
      if (type.length() == 0)
         throw new IllegalArgumentException("type cannot be empty");

      boolean valid = false;
      for (int i=0; i<TYPE_ENUM.length; i++)
      {
         if (TYPE_ENUM[i].equals(type))
         {
            valid = true;
            break;
         }
      }
      if (!valid)
         throw new IllegalArgumentException(
            "type must be one of TYPE_ENUM");

      m_type = type;
   }

   /**
    * @return the directory reference used to do the role membership lookups
    *    if this is of type <code>TYPE_DIRECTORY</code>. Always
    *    <code>null</code> if this is of type <code>TYPE_ROLE_MANAGER</code>.
    */
   public PSReference getDirectoryRef()
   {
      return m_directoryRef;
   }

   /**
    * Set a new directory reference. This method does nothing if the type is
    * <code>TYPE_ROLE_MANAGER</code>.
    *
    * @param directoryRef the directory reference name to set the reference for,
    *    not <code>null</code> or empty.
    */
   public void setDirectoryRef(String directoryRef)
   {
      if (!isBackendRoleProvider())
      {
         if (directoryRef == null)
            throw new IllegalArgumentException("directoryRef cannot be null");

         directoryRef = directoryRef.trim();
         if (directoryRef.length() == 0)
            throw new IllegalArgumentException("directoryRef cannot be null");

         setDirectoryRef(new PSReference(directoryRef,
            PSDirectorySet.class.getName()));
      }
   }

   /**
    * Set a new directory reference. This method does nothing if the type is
    * <code>TYPE_ROLE_MANAGER</code>.
    *
    * @param directoryRef the directory reference to set,
    *    not <code>null</code>, must be of type <code>PSDirecctorySet</code>.
    */
   public void setDirectoryRef(PSReference directoryRef)
   {
      if (!isBackendRoleProvider())
      {
         if (directoryRef == null)
            throw new IllegalArgumentException("directoryRef cannot be null");

         if (!directoryRef.getType().equals(PSDirectorySet.class.getName()))
            throw new IllegalArgumentException(
               "must be a PSDirectorySet reference");

         m_directoryRef = new PSReference(directoryRef);
      }
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
      setType(getEnumeratedAttribute(tree, XML_ATTR_TYPE, TYPE_ENUM));
      setDelimiter(getImpliedAttribute(tree, XML_ATTR_ROLE_DELIM));

      Element reference = tree.getNextElement(PSReference.XML_NODE_NAME);
      if (reference == null && isDirectoryRoleProvider())
      {
         Object[] args = { PSReference.XML_NODE_NAME, null };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
      }
      if (reference != null)
         m_directoryRef = new PSReference(reference, parentDoc, parentComponents);
   }

   /** @see IPSComponent */
   public Element toXml(Document doc)
   {
      Element root = doc.createElement(XML_NODE_NAME);
      root.setAttribute(XML_ATTR_NAME, getName());
      root.setAttribute(XML_ATTR_TYPE, m_type);
      if (m_delimiter != null)
      {
         root.setAttribute(XML_ATTR_ROLE_DELIM, m_delimiter);
      }

      if (m_directoryRef != null)
         root.appendChild(m_directoryRef.toXml(doc));

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

      if (!(c instanceof PSRoleProvider))
         throw new IllegalArgumentException("c must be a PSRoleProvider object");

      PSRoleProvider o = (PSRoleProvider) c;

      setName(o.getName());
      setType(o.m_type);
      setDirectoryRef(o.getDirectoryRef());
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSRoleProvider)) return false;
      if (!super.equals(o)) return false;
      PSRoleProvider that = (PSRoleProvider) o;
      return Objects.equals(m_name, that.m_name) &&
              Objects.equals(m_type, that.m_type) &&
              Objects.equals(m_directoryRef, that.m_directoryRef) &&
              Objects.equals(m_delimiter, that.m_delimiter);
   }

   @Override
   public int hashCode() {
      return Objects.hash(super.hashCode(), m_name, m_type, m_directoryRef, m_delimiter);
   }

   /** The XML node name */
   public static final String XML_NODE_NAME = "PSXRoleProvider";

   /**
    * Constant used to specify that role membership is determined through
    * a backend role provider.
    */
   public static final String TYPE_BACKEND = "backend";

   /**
    * Constant used to specify that the role membership is determined through
    * directory lookups.
    */
   public static final String TYPE_DIRECTORY = "directory";
   
   /**
    * Constant used to specify that the role membership is determined through
    * backend and directory lookups.
    */
   public static final String TYPE_BOTH = "both";

   /**
    * An array of all supported types.
    */
   public static final String[] TYPE_ENUM =
   {
      TYPE_BACKEND,
      TYPE_DIRECTORY,
      TYPE_BOTH
   };

   /**
    * The role provider name, must be unique across all role providers because
    * its used to reference this object from other contexts. Initialized
    * during construction, never <code>null</code> or empty after that.
    */
   private String m_name = null;

   /**
    * The role provider type. Initialized while constructed, never
    * <code>null</code> or empty after that. Must be one of
    * <code>TYPE_ENUM</code>, never changed after construction.
    */
   private String m_type = null;

   /**
    * The directory reference references a <code>PSDirectorySet</code> object
    * if this is of type <code>TYPE_DIRECTORY</code>. The referenced directory
    * set will do the role membership lookups. Must be specified if type
    * <code>TYPE_DIRECTORY</code>, <code>null</code> otherwise.
    */
   private PSReference m_directoryRef = null;
   
   /**
    * If the role provider has roles that are separated by a delimiter, the
    * delimeter is listed here. If this value is <code>null</code>, then
    * the provider does not use a delimiter.
    */
   private String m_delimiter = null;

   // XML element and attribute constants.
   private static final String XML_ATTR_NAME = "name";
   private static final String XML_ATTR_TYPE = "type";
   private static final String XML_ATTR_ROLE_DELIM = "separator";

}
