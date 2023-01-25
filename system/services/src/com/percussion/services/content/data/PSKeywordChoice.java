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
package com.percussion.services.content.data;

import com.percussion.services.utils.xml.PSXmlSerializationHelper;

import java.io.IOException;
import java.io.Serializable;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.xml.sax.SAXException;

/**
 * This object represents a single keyword choice.
 */
public class PSKeywordChoice implements Serializable
{
   /**
    * Compiler generated serial version ID used for serialization.
    */
   private static final long serialVersionUID = 5676687536687532224L;

   /**
    * The value for this keyword choice, never <code>null</code>, may be empty.
    */
   private String value;

   /**
    * The label for this keyword choice, never <code>null</code> or empty.
    */
   private String label;
   
   /**
    * A description for this keyword choice, may be <code>null</code> or empty.
    */
   private String description;

   /**
    * The 0 based display sequence for this keyword choice, always >= 0.
    */
   private Integer sequence = 0;

   /**
    * Bean pattern requires the default constructor. Do not use this to create
    * new objects.
    */
   public PSKeywordChoice()
   {
   }
   
   /**
    * Construct a new keyword choice for the supplied keyword.
    * 
    * @param keyword the keyword for which to construct a new choice, not
    *    <code>null</code>.
    */
   public PSKeywordChoice(PSKeyword keyword)
   {
      if (keyword == null)
         throw new IllegalArgumentException("keyword cannot be null");
      
      setValue(keyword.getValue());
      setLabel(keyword.getLabel());
      setDescription(keyword.getDescription());
      setSequence(keyword.getSequence());
   }
   
   /**
    * Get the keyword choice value.
    * 
    * @return the keyword choice value, never <code>null</code>, may be empty.
    */
   public String getValue()
   {
      return value;
   }
   
   /**
    * Set a new keyword choice value.
    * 
    * @param value the new keyword choice value, not <code>null</code>.
    */
   public void setValue(String value)
   {
      if (value == null)
         throw new IllegalArgumentException("value cannot be null");
      
      this.value = value;
   }
   
   /**
    * Get the keyword choice label.
    * 
    * @return the keyword choice label, never <code>null</code> or empty.
    */
   public String getLabel()
   {
      return label;
   }
   
   /**
    * Set a new keyword choice label.
    * 
    * @param label the new keyword choice label, not <code>null</code> or
    *    empty.
    */
   public void setLabel(String label)
   {
      if (StringUtils.isBlank(label))
         throw new IllegalArgumentException("label cannot be null or empty");
      
      this.label = label;
   }
   
   /**
    * Get the keyword choice description.
    * 
    * @return the keyword choice description, may be <code>null</code> or empty.
    */
   public String getDescription()
   {
      return description;
   }
   
   /**
    * Set a new keyword choice description.
    * 
    * @param description the new keyword choice description, may be 
    *    <code>null</code> or empty.
    */
   public void setDescription(String description)
   {
      this.description = description;
   }
   
   /**
    * Get the sortrank for this keyword choice.
    * 
    * @return the 0 based display sequence of this keyword choice.
    */
   public Integer getSequence()
   {
      return sequence;
   }
   
   /**
    * Set a new display sequence for this keyword choice.
    * 
    * @param sequence the new display sequence for this keyword choice, may
    *    be <code>null</code>, must >= 0 if provided.
    */
   public void setSequence(Integer sequence)
   {
      if (sequence == null)
         sequence = 0;
      
      if (sequence < 0)
         throw new IllegalArgumentException("sortrank must be >= 0");
      
      this.sequence = sequence;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSKeywordChoice)) return false;
      PSKeywordChoice that = (PSKeywordChoice) o;
      return Objects.equals(getValue(), that.getValue()) && Objects.equals(getLabel(), that.getLabel()) && Objects.equals(getDescription(), that.getDescription()) && Objects.equals(getSequence(), that.getSequence());
   }

   @Override
   public int hashCode() {
      return Objects.hash(getValue(), getLabel(), getDescription(), getSequence());
   }

   @Override
   public String toString() {
      final StringBuffer sb = new StringBuffer("PSKeywordChoice{");
      sb.append("value='").append(value).append('\'');
      sb.append(", label='").append(label).append('\'');
      sb.append(", description='").append(description).append('\'');
      sb.append(", sequence=").append(sequence);
      sb.append('}');
      return sb.toString();
   }

   /* (non-Javadoc)
    * @see IPSCatalogItem#fromXML(String)
    */
   public void fromXML(String xmlsource) throws IOException, SAXException
   {
      PSXmlSerializationHelper.readFromXML(xmlsource, this);
   }

   /* (non-Javadoc)
    * @see IPSCatalogItem#toXML()
    */
   public String toXML() throws IOException, SAXException
   {
      return PSXmlSerializationHelper.writeToXml(this);
   }
}

