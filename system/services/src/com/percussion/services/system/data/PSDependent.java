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
package com.percussion.services.system.data;

import com.percussion.services.catalog.PSTypeEnum;
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
 * This object represents a single design object dependent.
 */
public class PSDependent implements Serializable
{
   /**
    * Compiler generated serial version ID used for serialization.
    */
   private static final long serialVersionUID = -2436271056885991371L;
   
   /**
    * The id of the dependent design object.
    */
   private long id;
   
   /**
    * The type of the dependent design object, may be <code>null</code>, not
    * empty.
    */
   private String type;
   
   /**
    * Default construcctor.
    */
   public PSDependent()
   {
   }
   
   /**
    * Get the design object id for this dependent.
    * 
    * @return the design object id for this dependent.
    */
   public long getId()
   {
      return id;
   }
   
   /**
    * Set the new design object id for this dependent.
    * 
    * @param id the new design object id for this dependent.
    */
   public void setId(long id)
   {
      this.id = id;
   }
   
   /**
    * Get the dependents type.
    * 
    * @return the dependents type, may be <code>null</code>, not empty.
    */
   public String getType()
   {
      return type;
   }
   
   /**
    * Get the dependents type in a form suitable for display.
    * 
    * @return the dependents diplay type, may be <code>null</code>, not empty.
    */   
   public String getDisplayType()
   {
      return PSTypeEnum.valueOf(type).getDisplayName();
   }
   
   /**
    * Set the dependents type.
    * 
    * @param type the new type for this dependent, not <code>null</code> or
    *    empty.
    */
   public void setType(String type)
   {
      if (StringUtils.isBlank(type))
         throw new IllegalArgumentException("type cannot be null or empty");
      
      this.type = type;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSDependent)) return false;
      PSDependent that = (PSDependent) o;
      return getId() == that.getId() && Objects.equals(getType(), that.getType());
   }

   @Override
   public int hashCode() {
      return Objects.hash(getId(), getType());
   }

   @Override
   public String toString() {
      final StringBuffer sb = new StringBuffer("PSDependent{");
      sb.append("id=").append(id);
      sb.append(", type='").append(type).append('\'');
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

