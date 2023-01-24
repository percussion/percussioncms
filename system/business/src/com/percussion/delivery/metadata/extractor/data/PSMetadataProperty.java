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
