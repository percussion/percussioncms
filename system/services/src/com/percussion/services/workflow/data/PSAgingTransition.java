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
package com.percussion.services.workflow.data;

import com.percussion.services.utils.xml.PSXmlSerializationHelper;

import java.io.IOException;

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

