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
package com.percussion.extension;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.Serializable;
import java.util.Objects;

/**
 * A representation of an extension method parameter. 
 */
public class PSExtensionMethodParam implements Serializable
{
   /**
    * Compiler generated serial version ID used for serialization.
    */
   private static final long serialVersionUID = 4417119994571577998L;

   /**
    * The parameter name, never <code>null</code> or empty after construction.
    */
   private String m_name = null;
   
   /**
    * The parameter type, never <code>null</code> or empty after construction.
    */
   private String m_type = null;
   
   /**
    * The parameter description, never <code>null</code> after construcrion, 
    * may be empty.
    */
   private String m_description = null;
   
   /**
    * Convenience constructor that calls {@link #PSExtensionMethodParam(String, 
    * String, String) PSExtensionMethodParam(name, type, null)}.
    */
   public PSExtensionMethodParam(String name, String type)
   {
      this(name, type, null);
   }
   
   /**
    * Construct a new extension method parameter for the supplied parameters.
    * 
    * @param name the parameter name, not <code>null</code> or empty.
    * @param type the parameter type, not <code>null</code> or empty.
    * @param description the parameter description, may be <code>null</code>
    *    or empty.
    */
   public PSExtensionMethodParam(String name, String type, String description)
   {
      // parameter contracts are checked in setters
      setName(name);
      setType(type);
      setDescription(description);
   }
   
   /**
    * Construct an extension method parameter from its xml representation.
    * 
    * @param source the source element from which to construct this, not
    *    <code>null</code>.
    * @throws PSExtensionException for any error deserializing the supplied 
    *    element.
    */
   public PSExtensionMethodParam(Element source) throws PSExtensionException
   {
      // parameter contract is checked in fromXML
      fromXML(source);
   }
   
   /**
    * Set the parameter name.
    * 
    * @param name the new name, not <code>null</code> or empty.
    */
   public void setName(String name)
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name cannot be null or empty");
      
      m_name = name;
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
    * Set the parameter type.
    * 
    * @param type the new type, not <code>null</code> or empty.
    */
   public void setType(String type)
   {
      if (StringUtils.isBlank(type))
         throw new IllegalArgumentException("type cannot be null or empty");
      
      m_type = type;
   }
   
   /**
    * Get the parameter type.
    * 
    * @return the parameter type, never <code>null</code> or empty.
    */
   public String getType()
   {
      return m_type;
   }
   
   /**
    * Set the parameter description.
    * 
    * @param description the new description, may be <code>null</code> or empty.
    */
   public void setDescription(String description)
   {
      if (description == null)
         description = "";
      
      m_description = description;
   }
   
   /**
    * Get the parameter description.
    * 
    * @return the parameter description, never <code>null</code>, may be empty.
    */
   public String getDescription()
   {
      return m_description;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSExtensionMethodParam)) return false;
      PSExtensionMethodParam that = (PSExtensionMethodParam) o;
      return Objects.equals(m_name, that.m_name) && Objects.equals(m_type, that.m_type) && Objects.equals(m_description, that.m_description);
   }

   @Override
   public int hashCode() {
      return Objects.hash(m_name, m_type, m_description);
   }

   @Override
   public String toString() {
      final StringBuffer sb = new StringBuffer("PSExtensionMethodParam{");
      sb.append("m_name='").append(m_name).append('\'');
      sb.append(", m_type='").append(m_type).append('\'');
      sb.append(", m_description='").append(m_description).append('\'');
      sb.append('}');
      return sb.toString();
   }

   /**
    * Constructs this extension method parameter from its xml representation.
    * 
    * @param source element the xml element from which to construct this object,
    *    nor <code>null</code>.
    * @throws PSExtensionException for any error deserializing the supplied xml.
    */
   public void fromXML(Element source) throws PSExtensionException
   {
      if (source == null)
         throw new IllegalArgumentException("source cannot be null");

      if (!source.getTagName().equals(XML_NAME))
         throw new PSExtensionException(
            IPSExtensionErrors.INVALID_XML_ELEMENT, 
            new Object[] { source.getTagName(), XML_NAME });
      
      String test = source.getAttribute(NAME_ATTR);
      if (StringUtils.isBlank(test))
         throw new PSExtensionException(
            IPSExtensionErrors.MISSING_REQUIRED_ATTRIBUTE, NAME_ATTR);
      setName(test);
      
      test = source.getAttribute(TYPE_ATTR);
      if (StringUtils.isBlank(test))
         throw new PSExtensionException(
            IPSExtensionErrors.MISSING_REQUIRED_ATTRIBUTE, TYPE_ATTR);
      setType(test);

      setDescription(source.getAttribute(DESCRIPTION_ATTR));
   }

   /**
    * Returns the xml representation for this extension method parameter.
    * 
    * @return the xml representation of this object, never <code>null</code>.
    */
   public Element toXML(Document doc)
   {
      Element element = doc.createElement(XML_NAME);

      element.setAttribute(NAME_ATTR, getName());
      element.setAttribute(TYPE_ATTR, getType());
      if (!StringUtils.isBlank(getDescription()))
         element.setAttribute(DESCRIPTION_ATTR, getDescription());
      
      return element;
   }
   
   /**
    * The name used for the xml representation of this object.
    */
   public static final String XML_NAME = "PSExtensionMethodParam";

   // Constants used for xml representation.
   private static final String NAME_ATTR = "name";
   private static final String TYPE_ATTR = "type";
   private static final String DESCRIPTION_ATTR = "description";
}

