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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSContentTypeSummaryChild)) return false;
      PSContentTypeSummaryChild that = (PSContentTypeSummaryChild) o;
      return Objects.equals(getName(), that.getName()) && Objects.equals(getChildFields(), that.getChildFields());
   }

   @Override
   public int hashCode() {
      return Objects.hash(getName(), getChildFields());
   }

   @Override
   public String toString() {
      final StringBuffer sb = new StringBuffer("PSContentTypeSummaryChild{");
      sb.append("name='").append(name).append('\'');
      sb.append(", childFields=").append(childFields);
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

