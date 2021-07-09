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
import com.percussion.utils.guid.IPSGuid;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.xml.sax.SAXException;

/**
 * This object represents a single content type summary.
 */
public class PSContentTypeSummary
{
   /**
    * The name of the content type fo which this represents the summary.
    * Initialized while constructed, never <code>null</code> after that.
    */
   private String contentType;
   
   /**
    * The content type description, may be <code>null</code>.
    */
   private String description;
   
   /**
    * The guid, initialized while constructed, never <code>null</code> after 
    * that.
    */
   private IPSGuid m_guid;
   
   /**
    * A list of children for this content type, never <code>null</code>, may
    * be empty.
    */
   private List<PSFieldDescription> fields = 
      new ArrayList<>();
   
   /**
    * A list of fields for this content type, never <code>null</code>, may
    * be empty.
    */
   private List<PSContentTypeSummaryChild> children = 
      new ArrayList<>();
   
   /**
    * Get the content type name.
    * 
    * @return the content type name, may be <code>null</code>, never empty.
    */
   public String getName()
   {
      return contentType;
   }
   
   /**
    * Set a new content type name.
    * 
    * @param name the new content type name, not <code>null</code> or
    *    empty.
    */
   public void setName(String name)
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException(
            "contentType cannot be null or empty");
      
      this.contentType = name;
   }
   
   /**
    * Get the description for this content type.
    * 
    * @return the content type description, may be <code>null</code> or empty.
    */
   public String getDescription()
   {
      return description;
   }
   
   /**
    * Set a new content type description.
    * 
    * @param description the new content type description, may be 
    *    <code>null</code> or empty.
    */
   public void setDescription(String description)
   {
      this.description = description;
   }
   
   /**
    * Get the list with all fields.
    * 
    * @return the list with all fields, never <code>null</code>, may
    *    be empty.
    */
   public List<PSFieldDescription> getFields()
   {
      return fields;
   }
   
   /**
    * Set a new list of fields.
    * 
    * @param fields the new list of fields, may be <code>null</code> or
    *    empty.
    */
   public void setFields(List<PSFieldDescription> fields)
   {
      if (fields == null)
         this.fields = new ArrayList<>();
      else
         this.fields = fields;
   }
   
   /**
    * Add a new field.
    * 
    * @param field the new field to add, not <code>null</code>.
    */
   public void addField(PSFieldDescription field)
   {
      if (field == null)
         throw new IllegalArgumentException("field cannot be null");
      
      fields.add(field);
   }
   
   /**
    * Get the list with all children.
    * 
    * @return the list with all children, never <code>null</code>, may
    *    be empty.
    */
   public List<PSContentTypeSummaryChild> getChildren()
   {
      return children;
   }
   
   /**
    * Set a new list of children.
    * 
    * @param children the new list of children, may be <code>null</code> or
    *    empty.
    */
   public void setChildren(List<PSContentTypeSummaryChild> children)
   {
      if (children == null)
         this.children = new ArrayList<>();
      else
         this.children = children;
   }
   
   /**
    * Add a new child.
    * 
    * @param child the new child to add, not <code>null</code>.
    */
   public void addChild(PSContentTypeSummaryChild child)
   {
      if (child == null)
         throw new IllegalArgumentException("child cannot be null");
      
      children.add(child);
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
    * Get the guid of this object.
    * 
    * @return The guid, never <code>null</code> if not set.
    */
   public IPSGuid getGuid()
   {
      return m_guid;
   }

   /**
    * Set this object's guid
    * 
    * @param guid The guid, may not be <code>null</code>.
    */
   public void setGuid(IPSGuid guid)
   {
      m_guid = guid;
   }
}

