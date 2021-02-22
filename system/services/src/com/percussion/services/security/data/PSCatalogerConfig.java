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

package com.percussion.services.security.data;

import com.percussion.security.IPSRoleCataloger;
import com.percussion.security.IPSSubjectCataloger;
import com.percussion.utils.spring.IPSBeanConfig;
import com.percussion.utils.spring.PSSpringBeanUtils;
import com.percussion.utils.xml.PSInvalidXmlException;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Object representation of the Spring bean configuration of an 
 * {@link IPSSubjectCataloger} or an
 * {@link IPSRoleCataloger}.
 */
public class PSCatalogerConfig implements IPSBeanConfig, Cloneable
{
   /**
    * The types of configurations that are supported.
    */
   public enum ConfigTypes 
   {
      /**
       * Represents an <code>IPSInternalRoleCataloger</code> configuration.
       */
      ROLE, 
      
      /**
       * Represents an <code>IPSSubjectCataloger</code> configuration.
       */
      SUBJECT;
   }

   /**
    * Construct a configuration specifying the configuration details.
    * 
    * @param name The name of the cataloger bean, may not be <code>null</code> 
    * or empty.
    * @param type The type of cataloger.
    * @param className The name of the class implementing the cataloger, may
    * not be <code>null</code> or empty
    * @param description Optional description, may be <code>null</code> or 
    * empty.
    * @param props The properties to set during intialization, may be empty,
    * never <code>null</code>.  The key is the property name, the value is
    * the property value.
    */
   public PSCatalogerConfig(String name, ConfigTypes type, String className, 
      String description, Map<String, String> props)
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name may not be null or empty");
      
      if (type == null)
         throw new IllegalArgumentException("type may not be null");
      
      if (StringUtils.isBlank(className))
         throw new IllegalArgumentException(
            "className may not be null or empty");
      
      if (props == null)
         throw new IllegalArgumentException("props may not be null");
      
      m_name = name;
      m_type = type;
      m_className = className;
      setDescription(description);
      m_props = props;
   }

   /**
    * Construct this object from its XML bean representation.
    * 
    * @param source The source node, may not be <code>null</code> and must 
    * conform to the Spring bean XML schema, and must also represent one of
    * the allowed <code>ConfigTypes</code>.
    * @param type The type of this configuration, may not be
    * <code>null</code>.
    * 
    * @throws PSInvalidXmlException if the source node is malformed. 
    */
   public PSCatalogerConfig(Element source, ConfigTypes type) 
      throws PSInvalidXmlException
   {
      if (source == null)
         throw new IllegalArgumentException("sourceNode may not be null");
      
      if (type == null)
         throw new IllegalArgumentException("type may not be null");
      
      m_type = type;
      fromXml(source);
   }

   // See IPSBeanConfig interface
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");
      
      Element root = PSSpringBeanUtils.createBeanRootElement(
         this, doc);
      
      PSSpringBeanUtils.addBeanProperty(root, NAME_PROP, m_name);
      PSSpringBeanUtils.addBeanProperty(root, DESC_PROP, m_description);
      
      for (Map.Entry<String, String> entry : m_props.entrySet())
      {
         PSSpringBeanUtils.addBeanProperty(root, entry.getKey(), 
            entry.getValue());
      }
      
      return root;
   }

   // See IPSBeanConfig interface
   public void fromXml(Element source) throws PSInvalidXmlException
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");

      // get name and class first
      m_className = PSSpringBeanUtils.getClassName(source);
      
      Element propEl;
      propEl = PSSpringBeanUtils.getNextPropertyElement(source, null, 
         NAME_PROP);
      m_name = PSSpringBeanUtils.getBeanPropertyValue(propEl, true);
      
      // now make sure name and bean name match as we expect
      PSSpringBeanUtils.validateBeanRootElement(getBeanName(), getClassName(), 
         source);
      
      // get description
      propEl = PSSpringBeanUtils.getNextPropertyElement(source, propEl, 
         DESC_PROP);
      m_description = PSSpringBeanUtils.getBeanPropertyValue(propEl, false);
      
      // now get any user defined properties that remain
      m_props = new HashMap<>();
      propEl = PSSpringBeanUtils.getNextPropertyElement(source, propEl);
      while (propEl != null)
      {
         String name = PSSpringBeanUtils.getBeanPropertyName(propEl);
         String val = PSSpringBeanUtils.getBeanPropertyValue(propEl, false);
         m_props.put(name, val);
         propEl = PSSpringBeanUtils.getNextPropertyElement(source, propEl);
      }
   }

   // See IPSBeanConfig interface
   public String getBeanName()
   {
      return m_name.replace(' ', '_') + "_" + m_type;
   }

   // See IPSBeanConfig interface   
   public String getClassName()
   {
      return m_className;
   }
   
   /**
    * Get the name of this cataloger
    * 
    * @return The name, never <code>null</code> or empty.
    */
   public String getName()
   {
      return m_name;
   }   

   /**
    * Sets the name of this cataloger.
    * 
    * @param name The name, may not be <code>null</code> or empty.
    */
   public void setName(String name)
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name may not be null or empty");
      
      m_name = name;
   }

   /**
    * Sets the name of the class implementing the cataloger.
    * 
    * @param className The class name, may not be <code>null</code> or empty.
    */
   public void setClassName(String className)
   {
      if (StringUtils.isBlank(className))
         throw new IllegalArgumentException(
            "className may not be null or empty");
      
      m_className = className;
   }

   /**
    * Get the cataloger's description.
    * 
    * @return The description, may be empty, never <code>null</code>.
    */
   public String getDescription()
   {
      return m_description;
   }

   /**
    * Set the description.
    * 
    * @param description The description, may be <code>null</code> or empty.
    */
   public void setDescription(String description)
   {
      m_description = (description == null ? "" : description);
   }

   /**
    * Get the initialization params of this cataloger.
    * 
    * @return The params, where the key is the property name, the value is
    * the property value.  Never <code>null</code>, may be empty.
    */
   public Map<String, String> getProperties()
   {
      return m_props;
   }

   /**
    * Set the initialization params of this cataloger.  See 
    * {@link #getProperties()} for parameter details.
    * 
    * @param props The properties, may not be <code>null</code>, may be empty.
    */
   public void setProperties(Map<String, String> props)
   {
      if (props == null)
         throw new IllegalArgumentException("props may not be null");
      
      m_props = props;
   }

   /**
    * Get the configuration type of this cataloger.  Note that this property is
    * immutable and cannot be changed once the configuration is constructed.
    * 
    * @return The type supplied during construction.
    */
   public ConfigTypes getConfigType()
   {
      return m_type;
   }

   //see base class
   public Object clone() throws CloneNotSupportedException
   {
      // need to manually deep clone the props Map
      PSCatalogerConfig clone = (PSCatalogerConfig) super.clone();
      Map<String, String> cloneProps = new HashMap<>();
      for (Map.Entry<String, String> entry : m_props.entrySet())
      {
         cloneProps.put(entry.getKey(), entry.getValue());
      }
      clone.setProperties(cloneProps);
      
      return clone;
   }

   //see base class   
   public boolean equals(Object o)
   {
      return EqualsBuilder.reflectionEquals(this, o);
   }

   //see base class
   public String toString()
   {
      return getBeanName();
   }
   
   //see base class
   public int hashCode()
   {
      return HashCodeBuilder.reflectionHashCode(this);
   }   
   
   /**
    * The name of the cataloger, never <code>null</code> or empty after
    * construction.
    */
   private String m_name;
   
   /**
    * The type of the cataloger, never <code>null</code> after construction.
    */
   private ConfigTypes m_type;
   
   /**
    * The name of the cataloger class, never <code>null</code> or empty after
    * construction.
    */
   private String m_className;
   
   /**
    * The cataloger description, never <code>null</code> after construction, may
    * be empty.
    */
   private String m_description;
   
   /**
    * The cataloger properties, never <code>null</code> after construction, may
    * be empty.
    */
   private Map<String, String> m_props;
   
   // private xml prop names
   private static final String NAME_PROP = "name";
   private static final String DESC_PROP = "description";
}
