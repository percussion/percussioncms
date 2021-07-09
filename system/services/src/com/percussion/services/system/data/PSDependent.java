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
package com.percussion.services.system.data;

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.utils.xml.PSXmlSerializationHelper;

import java.io.IOException;
import java.io.Serializable;

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

