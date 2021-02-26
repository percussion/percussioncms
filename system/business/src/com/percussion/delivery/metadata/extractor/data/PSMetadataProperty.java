/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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

package com.percussion.delivery.metadata.extractor.data;

import com.percussion.delivery.metadata.IPSMetadataProperty;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * Represents a metadata property name value pair.
 * 
 * @author miltonpividori
 * 
 */
@XmlRootElement(name = "property")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class PSMetadataProperty implements Serializable, IPSMetadataProperty
{

   /**
    * Property name. For example: dcterms:creator
    */
   private String name;

   /**
    * Value of the metadata property. It may be a String, Date or Double. You
    * can get the value type by reading the "valuetype" field.
    */
   @XmlElement
   private String value;

   public PSMetadataProperty()
   {
      // Default constructor
   }

   /**
    * Ctor to create a property of the specified valuetype.
    * 
    * @param name the property name, cannot be <code>null</code> or empty.
    * @param type the {@link #valuetype} for the property. Cannot be
    *           <code>null</code>.
    * @param value the value to be stored in the property. May be
    *           <code>null</code> or empty.
    */
   public PSMetadataProperty(String name, String value)
   {
      this.name = name;
      this.value = value;
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * com.percussion.delivery.metadata.extractor.data.IPSMetadataProperty#getName
    * ()
    */
   @XmlElement
   public String getName()
   {
      return name;
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * com.percussion.delivery.metadata.extractor.data.IPSMetadataProperty#setName
    * (java.lang.String)
    */
   public void setName(String name)
   {
      this.name = name;
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * com.percussion.delivery.metadata.extractor.data.IPSMetadataProperty#getValue
    * ()
    */

   @XmlElement
   public String getValue()
   {
      return value;
   }

   @Override
   public String toString()
   {
      return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("name", name).append("value", value)
            .toString();
   }

   @XmlElement
   public void setValue(String val)
   {
      this.value = val;
   }
}
