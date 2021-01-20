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
package com.percussion.services.content.data;

import com.percussion.services.utils.xml.PSXmlSerializationHelper;

import java.io.IOException;
import java.io.Serializable;

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
   public boolean equals(Object b)
   {
      return EqualsBuilder.reflectionEquals(this, b);
   }

   @Override
   public int hashCode()
   {
      return HashCodeBuilder.reflectionHashCode(this);
   }

   @Override
   public String toString()
   {
      return ToStringBuilder.reflectionToString(this);
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

