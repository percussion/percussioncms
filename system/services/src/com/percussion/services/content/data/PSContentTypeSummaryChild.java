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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.xml.sax.SAXException;

/**
 * This object represents a single content type summary child.
 */
public class PSContentTypeSummaryChild
{
   /**
    * The name of the child, may be <code>null</code>, never empty.
    */
   private String name;
   
   /**
    * A list with all fields that are part of this child, never 
    * <code>null</code>, may be empty.
    */
   private List<PSFieldDescription> childFields = 
      new ArrayList<>();
   
   /**
    * Default constructor.
    */
   public PSContentTypeSummaryChild()
   {
   }
   
   /**
    * Construct a new child for the supplied name.
    * 
    * @param name the name of the child, not <code>null</code> or empty.
    */
   public PSContentTypeSummaryChild(String name)
   {
      setName(name);
   }

   /**
    * Get the child name.
    * 
    * @return the child name, may be <code>null</code>, never empty.
    */
   public String getName()
   {
      return name;
   }
   
   /**
    * Set a new name.
    * 
    * @param name the new child name, not <code>null</code> or empty.
    */
   public void setName(String name)
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name cannot be null or empty");
      
      this.name = name;
   }
   
   /**
    * Get the list with all child fields.
    * 
    * @return the list with all child fields, never <code>null</code>, may
    *    be empty.
    */
   public List<PSFieldDescription> getChildFields()
   {
      return childFields;
   }
   
   /**
    * Set a new list of child fields.
    * 
    * @param childFields the new list of child fields, may be 
    *    <code>null</code> or empty.
    */
   public void setChildFields(List<PSFieldDescription> childFields)
   {
      if (childFields == null)
         this.childFields = new ArrayList<>();
      else
         this.childFields = childFields;
   }
   
   /**
    * Add a new child field.
    * 
    * @param childField the new child field to add, not <code>null</code>.
    */
   public void addField(PSFieldDescription childField)
   {
      if (childField == null)
         throw new IllegalArgumentException("childField cannot be null");
      
      childFields.add(childField);
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

