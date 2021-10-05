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
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

/**
 * Collection container for <code>PSReference</code> objects which all
 * reference a <code>PSDirectory</code> object.
 */
public class PSDirectorySet extends PSCollectionComponent
{
   /**
    * Construct a Java object from its XML representation.
    *
    * @param sourceNode   the XML element node to construct this object from,
    *    not <code>null</code>.
    * @throws PSUnknownNodeTypeException if the XML element node is not of
    *    the appropriate type
    */
   public PSDirectorySet(Element sourceNode) throws PSUnknownNodeTypeException
   {
      super(PSReference.class);

      initRequiredAttributeNames();
      fromXml(sourceNode, null, null);
   }

   /**
    * Constucts an empty property set.
    * 
    * @param name the name of this property set, see {@link setName(String)} for
    *    more information.
    * @param userAttributeName the attribute name under which users are found
    *    in this directory set, see {@link setUserAttributeName(String)} for
    *    more information.
    */
   public PSDirectorySet(String name, String userAttributeName)
   {
      super(PSReference.class);

      initRequiredAttributeNames();
      setName(name);
      setObjectAttributeName(userAttributeName);
   }

   /**
    * @return the directory set name, never <code>null</code> or empty. This
    *    name may be used to reference this directory set from other contexts.
    */
   public String getName()
   {
      return m_name;
   }

   /**
    * Set a new directory set name.
    *
    * @param name the new name for this directory set, not <code>null</code> or
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
    * Get the requested required attribute name.
    * 
    * @param key the key of the attribute name, not <code>null<code> or empty,
    *    must be one oc <code>REQUIRED_ATTRIBUTE_NAMES_ENUM</code>.
    * @return the requested attribute name or <code>null<code> if no attribute
    *    name was set yet.
    */
   public String getRequiredAttributeName(String key)
   {
      if (key == null)
         throw new IllegalArgumentException("key cannot be null");
         
      key = key.trim();
      if (key.length() == 0)
         throw new IllegalArgumentException("key cannot be empty");

      boolean valid = false;
      for (int i=0; i<REQUIRED_ATTRIBUTE_NAMES_ENUM.length && !valid; i++)
      {
         if (key.equals(REQUIRED_ATTRIBUTE_NAMES_ENUM[i]))
            valid = true;
      }
      if (!valid)
         throw new IllegalArgumentException("key is not known");

      return (String) m_requiredAttributeNames.get(key);
   }
   
   /**
    * Set a new attribute name for the email address.
    * 
    * @param name the new attribute name, may be <code>null<code> but not empty.
    *    Supply <code>null<code> to clear the email attribute name.
    */
   public void setEmailAttributeName(String name)
   {
      if (name != null)
      {   
         name = name.trim();   
         if (name.length() == 0)
            throw new IllegalArgumentException("name cannot be empty");
      }

      m_requiredAttributeNames.put(EMAIL_ATTRIBUTE_KEY, name);
   }
   
   /**
    * Set a new attribute name for the role attribute.
    * 
    * @param name the new attribute name, may be <code>null<code> but not empty.
    *    Supply <code>null<code> to clear the role attribute name.
    */
   public void setRoleAttributeName(String name)
   {
      if (name != null)
      {   
         name = name.trim();   
         if (name.length() == 0)
            throw new IllegalArgumentException("name cannot be empty");
      }

      m_requiredAttributeNames.put(ROLE_ATTRIBUTE_KEY, name);
   }
   
   /**
    * Set a new object attribute name. This attribute name is used to lookup
    * objects in the directory.
    * 
    * @param name the new object attribute name, not <code>null<code> or empty.
    */
   public void setObjectAttributeName(String name)
   {
      if (name == null)
         throw new IllegalArgumentException("name cannot be null");
         
      name = name.trim();
      if (name.length() == 0)
         throw new IllegalArgumentException("name cannot be empty");
         
      m_requiredAttributeNames.put(OBJECT_ATTRIBUTE_KEY, name);
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

         data = tree.getElementData(XML_ATTR_NAME, false);
         if (data == null || data.trim().length() == 0)
         {
            String parentName = tree.getCurrent().getNodeName();
            Object[] args = {  parentName, XML_ATTR_NAME, "null or empty" };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
         }
         setName(data);

         node = tree.getNextElement(PSReference.XML_NODE_NAME, firstFlags);
         while (node != null)
         {
            PSReference reference = new PSReference((Element) tree.getCurrent(),
               parentDoc, parentComponents);
            add(reference);

            node = tree.getNextElement(PSReference.XML_NODE_NAME, nextFlags);
         }
         
         tree.setCurrent(sourceNode);
         initRequiredAttributeNames();
         Element attributes = tree.getNextElement(XML_ELEM_ATTRIBUTES);
         if (attributes != null)
         {
            Element attribute = tree.getNextElement(XML_ELEM_ATTRIBUTE);
            while (attribute != null)
            {
               data = tree.getElementData().trim();
               m_requiredAttributeNames.put(attribute.getAttribute(XML_ATTR_NAME), 
                  (data.length() == 0) ? null : data);
               
               attribute = tree.getNextElement(XML_ELEM_ATTRIBUTE);
            }
         }
      }
      finally
      {
         resetParentList(parentComponents, parentSize);
      }
   }

   /** @see IPSComponent */
   public Element toXml(Document doc)
   {
      Element root = doc.createElement(XML_NODE_NAME);
      root.setAttribute(XML_ATTR_NAME, getName());

      for (int i=0; i<size(); i++)
      {
         IPSComponent reference = (IPSComponent) get(i);
         root.appendChild(reference.toXml(doc));
      }

      if (m_requiredAttributeNames != null)
      {
         Element attributes = PSXmlDocumentBuilder.addEmptyElement(doc, root,
            XML_ELEM_ATTRIBUTES);

         Iterator keys = m_requiredAttributeNames.keySet().iterator();
         while (keys.hasNext())
         {
            String key = (String) keys.next();
            String value = (String) m_requiredAttributeNames.get(key);
            
            Element attr = null;
            if (value != null)
               attr = PSXmlDocumentBuilder.addElement(doc, attributes,
                  XML_ELEM_ATTRIBUTE, value);
            else
               attr = PSXmlDocumentBuilder.addEmptyElement(doc, attributes,
                  XML_ELEM_ATTRIBUTE);
            attr.setAttribute(XML_ATTR_NAME, key);
         }
      }

      return root;
   }

   /** @see PSCollectionComponent */
   public void copyFrom(PSCollectionComponent c)
   {
      super.copyFrom(c);

      if (!(c instanceof PSDirectorySet))
         throw new IllegalArgumentException("c must be a PSDirectorySet object");

      PSDirectorySet o = (PSDirectorySet) c;

      setName(o.getName());
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSDirectorySet)) return false;
      if (!super.equals(o)) return false;
      PSDirectorySet that = (PSDirectorySet) o;
      return Objects.equals(m_name, that.m_name) &&
              Objects.equals(m_requiredAttributeNames, that.m_requiredAttributeNames);
   }

   @Override
   public int hashCode() {
      return Objects.hash(super.hashCode(), m_name, m_requiredAttributeNames);
   }

   /**
    * Get the directory reference for the supplied name. Names are compared case
    * sensitive.
    *
    * @param name the directory name, may be <code>null</code> or empty in
    *    which case this always returns <code>null</code>.
    * @return the directory refernence if found, <code>null</code> otherwise.
    */
   public PSReference getDirectoryRef(String name)
   {
      if (name == null || name.trim().length() == 0)
         return null;

      Iterator references = iterator();
      while (references.hasNext())
      {
         PSReference reference = (PSReference) references.next();
         if (reference.getName().equals(name))
            return reference;
      }

      return null;
   }

   /**
    * Adds the supplied directory reference. If a directory reference with
    * the same name as the supplied one exists, it is replaced with the new
    * directory reference, otherwise it is appendend, the comparison is done
    * using the directory name in a case-sensitive manner.
    *
    * @param reference the directory refernece to set, not <code>null</code>.
    */
   public void addDirectoryRef(PSReference reference)
   {
      if (reference == null)
         throw new IllegalArgumentException("reference cannot be null");

      PSReference current = getDirectoryRef(reference.getName());
      if (current != null)
         remove(current);

      add(reference);
   }

   /**
    * Overridden to make sure that only references of type
    * <code>PSDirectory</code> are added.
    *
    * @see PSCollection for more details.
    */
   protected void checkType(Object o) throws ClassCastException
   {
      super.checkType(o);

      PSReference ref = (PSReference) o;
      if (!ref.getType().equals(PSDirectory.class.getName()))
         throw new IllegalArgumentException(
            "reference must be of type " + PSDirectory.class.getName());
   }
   
   /**
    * Initializes all required attribute names with <code>null<code> values.
    *
    */
   private void initRequiredAttributeNames()
   {
      m_requiredAttributeNames = new HashMap();
      
      for (int i=0; i<REQUIRED_ATTRIBUTE_NAMES_ENUM.length; i++)
         m_requiredAttributeNames.put(REQUIRED_ATTRIBUTE_NAMES_ENUM[i], null);
   }

   /** The XML node name */
   public static final String XML_NODE_NAME = "PSXDirectorySet";
   
   /**
    * The key used to store the object's attribute name.
    */
   public final static String OBJECT_ATTRIBUTE_KEY = "objectAttributeName";
   
   /**
    * The default object attribute name.
    */
   public final static String DEFAULT_OBJECT_ATTRIBUTE_NAME = "uid";
   
   /**
    * The key used to store the email attribute name.
    */
   public final static String EMAIL_ATTRIBUTE_KEY = "emailAttributeName";
   
   /**
    * The key used to store the role attribute name.
    */
   public final static String ROLE_ATTRIBUTE_KEY = "roleAttributeName";
   
   /**
    * An array with key's of all required attribute names. 
    */
   public static final String[] REQUIRED_ATTRIBUTE_NAMES_ENUM =
   {
      OBJECT_ATTRIBUTE_KEY,
      EMAIL_ATTRIBUTE_KEY,
      ROLE_ATTRIBUTE_KEY
   };

   /**
    * Holds the directory set name. This name must be unique across all defined
    * directory sets because its used to reference it from other contexts.
    * Initialized during construction, never <code>null</code> or empty after
    * that.
    */
   private String m_name = null;
   
   /**
    * A map that holds all required attribute names. All attribute names are 
    * initialized to <code>null<code> and can be set through the appropriate
    * set method such as {@link setEmailAttributeName(String)}. Initialized
    * while constructed, never <code>null<code> or empty after that.
    */
   private Map m_requiredAttributeNames = null;

   // XML element and attribute constants.
   private static final String XML_ATTR_NAME = "name";
   private static final String XML_ELEM_ATTRIBUTES = "RequiredAttributeNames";
   private static final String XML_ELEM_ATTRIBUTE = "Attribute";
}
