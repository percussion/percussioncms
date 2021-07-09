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
import org.w3c.dom.Node;


/**
 * The class which defines a property in any Rx configuration.
 */
public class PSProperty extends PSComponent
{
   /**
    * Constructs this property from the supplied parameters.
    *
    * @param name name of the property, may not be <code>null</code> or empty.
    * @param type data type of the property value, must be one of the TYPE_xxx
    * values
    * @param value the  value of the property, may be <code>null
    * </code> or empty.
    * @param lock the flag to indicate lock status of a property, supply <code>
    * true</code> to lock the property, otherwise <code>false</code>. Locked
    * properties can not be overridden at runtime.
    * @param description the description of the property, may be <code>null
    * </code> or empty.
    *
    * @throws IllegalArgumentException if any parameter is invalid.
    */
   public PSProperty(String name, int type, Object value, boolean lock,
      String description)
   {
      setName(name);
      setType(type);
      setValue(value);
      setLock(lock);
      setDescription(description);
   }

   /**
    * Convenience constructor for
    * PSProperty(name, TYPE_STRING, null, false, null)}. See the link for more
    * description.
    */
   public PSProperty(String name)
   {
      setName(name);
   }

   /**
    * Sets the name of the property.
    *
    * @param name the name of the property, may not be <code>null</code> or
    * empty.
    *
    * @throws IllegalArgumentException if name is invalid.
    */
   public void setName(String name)
   {
      if(name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null or empty.");

      m_name = name;
   }

   /**
    * Sets the data type of the property value.
    *
    * @param type data type of the property value, must be one of the TYPE_xxx
    * values
    *
    * @throws IllegalArgumentException if type is invalid.
    */
   public void setType(int type)
   {
      if(type < 0 && type >= TYPE_ENUM.length)
         throw new IllegalArgumentException("type must be between 0 and " +
            TYPE_ENUM.length);

      m_type = type;
   }

   /**
    * Sets the  value of the property.
    *
    * @param value the value of the property, may be <code>null</code>.
    */
   public void setValue(Object value)
   {
      if (value != null)
      {
         if (m_type == TYPE_BOOLEAN)
         {
            if (value instanceof String)
            {
               m_value = ((String) value).equalsIgnoreCase(XML_BOOL_YES);
            }
            else if (value instanceof Boolean)
               m_value = value;
            else
               throw new IllegalArgumentException(
                  "invalid value for type PSProperty.TYPE_BOOLEAN");
         }
         else
         {
            if (value instanceof String)
               m_value = value;
            else
               throw new IllegalArgumentException(
                  "invalid value for type PSProperty.TYPE_DATE, " + 
                     "PSProperty.TYPE_FILE or PSProperty.TYPE_STRING");
         }
      }
      else
         m_value = null;
   }

   /**
    * Sets lock status of the property. See {@link #isLocked()} for more info.
    *
    * @param lock supply <code>true</code> to lock the property, otherwise
    * <code>false</code>
    */
   public void setLock(boolean lock)
   {
      m_lock = lock;
   }

   /**
    * Sets the description of the property.
    *
    * @param description the description of the property, may be <code>null
    * </code> or empty.
    */
   public void setDescription(String description)
   {
      m_description = description;
   }

   /**
    * Gets the name of the property.
    *
    * @return the name, never <code>null</code> or empty.
    */
   public String getName()
   {
      return m_name;
   }

   /**
    * Gets the allowed data type of the property value.
    *
    * @return the data type, never <code>null</code> or empty.
    */
   public String getTypeName()
   {
      return TYPE_ENUM[m_type];
   }

   /**
    * Sets the data type of the property value based on the supplied type name.
    *
    * @param typeName name of the data type, may not be <code>null</code> or
    * empty and must be one of the <code>TYPE_ENUM</code> elements.
    */
   public void setTypeName(String typeName)
   {
      if(typeName == null || typeName.trim().length() == 0)
         throw new IllegalArgumentException(
            "typeName may not be null or empty.");

      int type = -1;
      for (int i = 0; i < TYPE_ENUM.length; i++)
      {
         if(typeName.equals(TYPE_ENUM[i]))
         {
            type = i;
            break;
         }
      }

      if(type == -1)
         throw new IllegalArgumentException("typeName is not a valid type");

      m_type = type;
   }

   /**
    * Gets the allowed data type of the property value.
    *
    * @return the data type.
    */
   public int getType()
   {
      return m_type;
   }

   /**
    * Gets the value of this property.
    *
    * @return the  value, may be <code>null</code>.
    */
   public Object getValue()
   {
      return m_value;
   }

   /**
    * Gets the lock state of the property.
    *
    * @return <code>true</code> if the property is locked (does not allow to
    * override in runtime), otherwise <code>false</code>
    */
   public boolean isLocked()
   {
      return m_lock;
   }

   /**
    * Gets the description of the property.
    *
    * @return the description, may be <code>null</code> or empty.
    */
   public String getDescription()
   {
      return m_description;
   }

   /**
    * Constructs this property object from its xml representation. See {@link
    * #toXml(Document)} for expected xml format.
    *
    * @param propertyEl the xml representation of property, may not be <code>
    * null</code>
    *
    * @throws PSUnknownNodeTypeException if the xml format is not as expected.
    * @throws IllegalArgumentException if propertyEl is <code>null</code>
    */
   public PSProperty(Element propertyEl) throws PSUnknownNodeTypeException
   {
      if(propertyEl == null)
         throw new IllegalArgumentException("propertyEl may not be null.");

      fromXml(propertyEl, null, null);
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

      m_name = getRequiredElement(tree, XML_ATTR_NAME);
      m_lock = XML_BOOL_YES.equalsIgnoreCase(
         sourceNode.getAttribute(XML_ATTR_LOCKED));

      Node current = tree.getCurrent();

      Element valueEl = tree.getNextElement(XML_VALUE_NODE);
      if(valueEl == null)
      {
         Object[] args = { XML_NODE_NAME, null };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
      }

      String type = getEnumeratedAttribute(tree, XML_ATTR_TYPE, TYPE_ENUM);
      for (int i = 0; i < TYPE_ENUM.length; i++)
      {
         if (TYPE_ENUM[i].equals(type))
         {
            m_type = i;
            break;
         }
      }

      String value = tree.getElementData(valueEl);
      if(value != null && value.trim().length() > 0)
      {
         switch(m_type)
         {
            case TYPE_BOOLEAN:
               m_value = value.equalsIgnoreCase(XML_BOOL_YES); 
               break;

            case TYPE_DATE:
            case TYPE_STRING:
            case TYPE_FILE:
               m_value = value;
               break;
         }
      }
      else
         m_value = null;

      tree.setCurrent(current);

      Element descEl = tree.getNextElement(XML_DESC_NODE);
      if(descEl != null)
         m_description = tree.getElementData(descEl);
   }

   /**
    * The Format of the returned xml is:
    * <pre><code>
    * &lt;ELEMENT Property (Value, Description?)>
    * &lt;ATTLIST Property
    *    name CDATA #REQUIRED
    *    locked (yes|no) "no" >
    * &lt;ELEMENT Value (#PCDATA)>
    * &lt;ATTLIST Value
    *    type (String|Boolean|Date) "String" >
    * &lt;ELEMENT Description (#PCDATA)>
    * </code></pre>
    * See <code>IPSComponent</code> interface for description and parameters of
    * this method.
    */
   public Element toXml(Document doc)
   {
      Element root = doc.createElement(XML_NODE_NAME);
      root.setAttribute(XML_ATTR_NAME, m_name);
      root.setAttribute(XML_ATTR_LOCKED, m_lock ? XML_BOOL_YES : XML_BOOL_NO);

      String value;

      if(m_value == null)
         value = "";
      else if(m_type == TYPE_BOOLEAN)
      {
         if( ((Boolean)m_value).booleanValue())
            value = XML_BOOL_YES;
         else
            value = XML_BOOL_NO;
      }
      else
         value = m_value.toString();

      Element valueEl =
         PSXmlDocumentBuilder.addElement(doc, root, XML_VALUE_NODE, value);
      valueEl.setAttribute(XML_ATTR_TYPE, TYPE_ENUM[m_type]);

      if(m_description != null)
         PSXmlDocumentBuilder.addElement(doc, root, XML_DESC_NODE, m_description);

      return root;
   }
   
   /* (non-Javadoc)
    * @see java.lang.Object#clone()
    */
   public Object clone()
   {
      PSProperty prop = new PSProperty(m_name);
      prop.m_type = m_type;
      prop.m_description = m_description;
      prop.m_lock = m_lock;
      //This is OK for string type of values (immutable). We may have a problem 
      //for objects that are mutable
      prop.m_value = m_value;
      
      return prop;
   }

   /**
    * Test if the provided object and this are equal.
    *
    * @param o the object to compare to, may be <code>null</code>.
    * @return <code>true</code> if this and o are equal,
    *    <code>false</code> otherwise.
    */
   public boolean equals(Object o)
   {
      if (!(o instanceof PSProperty))
         return false;

      PSProperty t = (PSProperty) o;

      boolean equal = true;
      if (!compare(m_description, t.getDescription()))
         equal = false;
      else if (m_lock != t.isLocked())
         equal = false;
      else if (!compare(m_name, t.getName()))
         equal = false;
      else if (m_type != t.getType())
         equal = false;
      else if (!compare(m_value, t.getValue()))
         equal = false;

      return equal;
   }
   
   /**
    * @see java.lang.Object#hashCode()
    */
   public int hashCode()
   {
      return getName().hashCode() + m_type;
   }

   /**
    * Name of the property, initialized in the constructor and may be modified
    * through a call to <code>setName(String)</code>, never <code>null</code> or
    * empty after construction.
    */
   private String m_name;

   /**
    * The data type of the property value, initialized in the constructor and
    * may be modified through a call to <code>setType(int)</code>, will be one
    * of TYPE_xxx values.
    */
   private int m_type = TYPE_STRING;

   /**
    * The value of the property,  initialized in the constructor and may be
    * modified through a call to <code>setValue(Object)</code>, may be <code>
    * null</code>.
    */
   private Object m_value = null;

   /**
    * The lock state of the property (value), initialized in the constructor and
    * may be modified through a call to <code>setLock(boolean)</code>.
    */
   private boolean m_lock = false;

   /**
    * The value of the property,  initialized in the constructor and may be
    * modified through a call to <code>setDescription(String)</code>, may be
    * <code>null</code> or empty.
    */
   private String m_description = null;

   /**
    * The constant to indicate 'String' data type for property value.
    */
   public static final int TYPE_STRING = 0;

   /**
    * The constant to indicate 'Boolean' data type for property value.
    */
   public static final int TYPE_BOOLEAN = 1;

   /**
    * The constant to indicate 'Date' data type for property value.
    */
   public static final int TYPE_DATE = 2;

   /**
    * The constant to indicate 'File' data type for property value. This type
    * represents a file path as <code>String</code> relative to the server root.
    */
   public static final int TYPE_FILE = 3;

   /**
    * Enumeration that represents the allowed data types for property values.
    * The indices represent the TYPE_xxx constants correspondingly.
    */
   public static String[] TYPE_ENUM = {"String", "Boolean", "Date", "File"};

   /**
    * The node name of the xml element represented by this object.
    */
   public static final String XML_NODE_NAME = "PSXProperty";

   //xml constants
   private static final String XML_ATTR_NAME = "name";
   private static final String XML_ATTR_LOCKED = "locked";
   private static final String XML_VALUE_NODE = "Value";
   private static final String XML_ATTR_TYPE = "type";
   private static final String XML_DESC_NODE = "Description";
   public static final String XML_BOOL_YES = "yes";
   public static final String XML_BOOL_NO = "no";
}
