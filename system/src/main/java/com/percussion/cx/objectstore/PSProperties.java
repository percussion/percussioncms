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
package com.percussion.cx.objectstore;

import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * The class that is used to represent properties as defined by
 * 'sys_Props.dtd'.
 */
public class PSProperties implements IPSComponent
{
   /**
    * The default constructor to create properties with empty list.
    */
   public PSProperties()
   {
   }

   /**
    * Constructs this object from the supplied element. See {@link
    * #toXml(Document) } for the expected form of xml.
    *
    * @param element the element to load from, may not be <code>null</code>
    * 
    * @throws IllegalArgumentException if element is <code>null</code>
    * @throws PSUnknownNodeTypeException if element is invalid. 
    */
   public PSProperties(Element element) throws PSUnknownNodeTypeException
   {
      if(element == null)
         throw new IllegalArgumentException("element may not be null.");

      fromXml(element);
   }

   /**
    * Create this properties as a shallow copy of the supplied properties
    * 
    * @param other The properties to copy from, may not be <code>null</code>.
    */
   public PSProperties(PSProperties other)
   {
      if (other == null)
         throw new IllegalArgumentException("other may not be null");
      
      for (Map.Entry<String, Object> entry : other.m_props.entrySet())
      {
         m_props.put(entry.getKey(), entry.getValue());
      }
   }

   // implements interface method, see toXml(Document) for the expected format
   //of the xml element.
   public void fromXml(Element sourceNode)
      throws PSUnknownNodeTypeException
   {
      if(sourceNode == null)
         throw new IllegalArgumentException("sourceNode may not be null.");

      if (!XML_NODE_NAME.equals(sourceNode.getNodeName()))
      {
         Object[] args = { XML_NODE_NAME, sourceNode.getNodeName() };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }

      Iterator list = PSComponentUtils.getChildElements(
            sourceNode, PROP_NODE_NAME);
      if(!list.hasNext())
      {
         Object[] args = { XML_NODE_NAME, "null" };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
      }

      m_props.clear();
      while(list.hasNext())
      {
         Node prop = (Node)list.next();
         NamedNodeMap nodeMap = prop.getAttributes();
         Node attrib = nodeMap.getNamedItem(NAME_ATTR);
         if(attrib == null || attrib.getNodeValue() == null ||
            attrib.getNodeValue().trim().length() == 0)
         {
            Object[] args = { PROP_NODE_NAME, "null" };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
         }

         if(prop.getFirstChild() instanceof Text)
         {
            m_props.put(attrib.getNodeValue(),
               prop.getFirstChild().getNodeValue());
         }
         else
         {
            //Treat it as empty value
            m_props.put(attrib.getNodeValue(), "");
         }
      }
   }

   /**
    * Implements the IPSComponent interface method to produce XML representation
    * of this object. See the interface for description of the method and
    * parameters.
    * <p>
    * The xml format is:
    * <pre><code>
    * &lt;!ELEMENT Props (Prop+)>
    * &lt;!ELEMENT Prop (#PCDATA)>
    * &lt;!ATTLIST Prop
    *      name CDATA #REQUIRED
    * >
    * </code></pre>
    *
    * @return the element, may be <code>null</code> if no properties exist.
    */
   public Element toXml(Document doc)
   {
      if(doc == null)
         throw new IllegalArgumentException("doc may not be null.");

      Element props = null;

      if(!m_props.isEmpty())
      {
         props = doc.createElement(XML_NODE_NAME);
         Iterator properties = m_props.entrySet().iterator();
         while(properties.hasNext())
         {
            Map.Entry prop = (Map.Entry)properties.next();
            Element propEl = doc.createElement(PROP_NODE_NAME);
            propEl.setAttribute(NAME_ATTR, (String)prop.getKey());
            propEl.appendChild(doc.createTextNode((String)prop.getValue()));
            props.appendChild(propEl);
         }
      }

      return props;
   }

   /**
    * Gets the value of the specified property. Uses case-sensitive comparison
    * to get the property.
    *
    * @param name name of the property, may not be <code>null</code> or empty.
    *
    * @return the property value, may be <code>null</code> if the specified
    * property does not exist.
    */
   public String getProperty(String name)
   {
      if(name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null or empty.");

      return (String)m_props.get(name);
   }

   /**
    * Gets the value of the specified property. Uses case-sensitive comparison
    * to get the property. Returns the supplied default value if the requested
    * named property does not exist.
    *
    * @param name name of the property, may not be <code>null</code> or empty.
    *
    * @param defaultValue default value of the property to be returned in case
    * the property does not exist in the list, may be <code>null</code> or empty.
    *
    * @return the property value, may be <code>null</code> or <code>empty</code>
    * depending on whether the named property exists and the default value
    * supplied.
    */
   public String getProperty(String name, String defaultValue)
   {
      if(name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null or empty.");
      Object value = m_props.get(name);
      if(value == null)
         return defaultValue;
      return (String)value;
   }

   /**
    * Gets the object of the specified property. Uses case-sensitive comparison
    * to get the object. Returns <code>null</code> if the requested named 
    * property does not exist.
    *
    * @param name name of the property, may not be <code>null</code> or empty.
    *
    * @return the object value, may be <code>null</code> depending on whether 
    * the named property exists.
    */
   public Object getPropertyObj(String name)
   {
      if(name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null or empty.");
      Object obj = m_props.get(name);
      return obj;
   }
   
   /**
    * Sets the specified property with supplied value. If the property with
    * that name exists it will be replaced. The property name and values are
    * case-sensitive.
    *
    * @param name name of the property, may not be <code>null</code> or empty.
    * @param value value of the property, may be <code>null</code> or empty.
    */
   public void setProperty(String name, String value)
   {
      if(name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null or empty.");

      m_props.put(name, value);
   }

   /**
    * Sets the specified property with supplied object. If the property with
    * that name exists it will be replaced. The property name is
    * case-sensitive.
    *
    * @param name name of the property, may not be <code>null</code> or empty.
    * @param obj object data of the property, may be <code>null</code>.
    */
   public void setPropertyObj(String name, Object obj)
   {
      if(name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null or empty.");

      m_props.put(name, obj);
   }
      
   //implements interface method.
   public boolean equals(Object obj)
   {
      boolean equals = false;

      if( obj instanceof PSProperties )
      {
         PSProperties other = (PSProperties)obj;
         if(m_props.equals(other.m_props))
            equals = true;
      }

      return equals;
   }

   //implements interface method
   public Object clone()
   {
      PSProperties props = new PSProperties();
      props.m_props.putAll(m_props);

      return props;
   }

   //implements interface method.
   public int hashCode()
   {
      return m_props.hashCode();
   }
   
   /*
    * (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   public String toString()
   {
      return "<PSProperties " + m_props.toString() + ">";
   }

   /**
    * The map of properties with 'name' as key and 'value' as value .
    * Initialized to an empty map and gets filled as it reads from xml. May be
    * modified through calls to <code>
    * fromXml(Element)</code> and
    * <code>setProperty(String, String), but never
    * <code>null</code>
    */
   private Map<String, Object> m_props = new HashMap<>();

   /**
    * The constant to indicate root node name.
    */
   public static final String XML_NODE_NAME = "Props";

   //xml constants
   private static final String PROP_NODE_NAME = "Prop";
   private static final String NAME_ATTR = "name";
}
