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
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.xml.sax.SAXException;

/**
 * This object represents a single field description.
 */
public class PSFieldDescription
{
   /**
    * The field name, may be <code>null</code>, never empty.
    */
   private String name;
   
   /**
    * The field data type, may be <code>null</code>, never empty.
    */
   private String type;
   
   private Boolean exportable;
   
   /**
    * Default constructor.
    */
   public PSFieldDescription()
   {
   }
   
   /**
    * Construct a new field description for the supplied parameters.
    * 
    * @param name the name of the new field, not <code>null</code> or empty.
    * @param type the type of the new field, not <code>null</code> or empty.
    */
   public PSFieldDescription(String name, String type)
   {
      setName(name);
      setType(type);
   }
   
   public PSFieldDescription(String name, String type, Boolean exportable)
   {
      setName(name);
      setType(type);
      setExportable(exportable);
   }
   
   /**
    * Get the field name.
    * 
    * @return the field name, may be <code>null</code>, never empty.
    */
   public String getName()
   {
      return name;
   }
   
   /**
    * Set a new field name.
    * 
    * @param name the new field name, not <code>null</code> or empty.
    */
   public void setName(String name)
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name cannot be null or empty");
      
      this.name = name;
   }
   
   /**
    * Get the field data type.
    * 
    * @return the field data type, may be <code>null</code>, never empty.
    */
   public String getType()
   {
      return type.toString();
   }
   
   /**
    * Set a new field data type.
    * 
    * @param type the new field data type, not <code>null</code> or empty.
    */
   public void setType(String type)
   {
      if (StringUtils.isBlank(type))
         throw new IllegalArgumentException("type cannot be null or empty");
      
      this.type = PSFieldTypeEnum.valueOf(type).toString();
   }

   public Boolean getExportable()
   {
      return exportable;
   }

   public void setExportable(Boolean exportable)
   {
      this.exportable = exportable;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSFieldDescription)) return false;
      PSFieldDescription that = (PSFieldDescription) o;
      return Objects.equals(getName(), that.getName()) && Objects.equals(getType(), that.getType()) && Objects.equals(getExportable(), that.getExportable());
   }

   @Override
   public int hashCode() {
      return Objects.hash(getName(), getType(), getExportable());
   }

   @Override
   public String toString() {
      final StringBuffer sb = new StringBuffer("PSFieldDescription{");
      sb.append("name='").append(name).append('\'');
      sb.append(", type='").append(type).append('\'');
      sb.append(", exportable=").append(exportable);
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
   
   /**
    * The field data type enumeration.
    */
   public enum PSFieldTypeEnum
   {
      TEXT,
      DATE,
      NUMBER,
      BINARY
   }
}

