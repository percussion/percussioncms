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
package com.percussion.services.workflow.data;

import com.percussion.services.utils.xml.PSXmlSerializationHelper;

import java.io.IOException;
import java.util.Objects;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.xml.sax.SAXException;

/**
 * Represents an aging transition
 */
public class PSAgingTransition extends PSTransitionBase implements IPSAgingTransition
{
   /**
    * Compiler generated serial version ID used for serialization.
    */
   private static final long serialVersionUID = 1L;
   
   private int type = PSAgingTypeEnum.ABSOLUTE.getValue();
   
   private Long interval = 1L;
   
   private String systemField;
   

   /*
    * (non-Javadoc)
    * @see com.percussion.services.workflow.data.IPSAgingTransition#getType()
    */
   public PSAgingTypeEnum getType()
   {
      return PSAgingTypeEnum.valueOf(type);
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.services.workflow.data.IPSAgingTransition#setType(com.percussion.services.workflow.data.PSAgingTransition.PSAgingTypeEnum)
    */
   public void setType(PSAgingTypeEnum agingType)
   {
      if (agingType == null)
         throw new IllegalArgumentException("agingType may not be null");
      
      type = agingType.getValue();
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.services.workflow.data.IPSAgingTransition#getInterval()
    */
   public long getInterval()
   {
      return interval == null ? 1 : interval;
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.services.workflow.data.IPSAgingTransition#setInterval(long)
    */
   public void setInterval(long agingInterval)
   {
      interval = agingInterval;
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.services.workflow.data.IPSAgingTransition#getSystemField()
    */
   public String getSystemField()
   {
      return systemField;
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.services.workflow.data.IPSAgingTransition#setSystemField(java.lang.String)
    */
   public void setSystemField(String name)
   {
      systemField = name;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSAgingTransition)) return false;
      PSAgingTransition that = (PSAgingTransition) o;
      return getType() == that.getType() && Objects.equals(getInterval(), that.getInterval()) && Objects.equals(getSystemField(), that.getSystemField());
   }

   @Override
   public int hashCode() {
      return Objects.hash(getType(), getInterval(), getSystemField());
   }

   @Override
   public String toString() {
      final StringBuffer sb = new StringBuffer("PSAgingTransition{");
      sb.append("type=").append(type);
      sb.append(", interval=").append(interval);
      sb.append(", systemField='").append(systemField).append('\'');
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
    * Class to enumerate the aging types.
    */
   public enum PSAgingTypeEnum
   {
      /**
       * Represents an absolute aging type
       */
      ABSOLUTE(1),
      
      /**
       * Represents a repeating aging type
       */
      REPEATED(2),
      /**
       * Represents a system field aging type
       */
      SYSTEM_FIELD(3);
      
      private PSAgingTypeEnum(int value)
      {
         mi_value = value;
      }
      
      /**
       * Get the integer value of the enum
       * 
       * @return The value.
       */
      public int getValue()
      {
         return mi_value;
      }
      
      /**
       * Get the corresponding enum from a value
       * 
       * @param value The value of the enum to get.
       * 
       * @return The type, or <code>null</code> if no match is found.
       */
      public static PSAgingTypeEnum valueOf(int value)
      {
         for (PSAgingTypeEnum type : values())
         {
            if (type.mi_value == value)
            {
               return type;
            }
         }
         
         return null;
      }
      
      private int mi_value;
   }
   
   static
   {
      // Register types with XML serializer for read creation of objects
      PSXmlSerializationHelper.addType("notification", PSNotification.class);
   }
}

