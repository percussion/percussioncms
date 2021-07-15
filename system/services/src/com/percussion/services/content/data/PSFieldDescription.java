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
package com.percussion.services.content.data;

import com.percussion.services.utils.xml.PSXmlSerializationHelper;

import java.io.IOException;

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

