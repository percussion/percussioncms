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
package com.percussion.services.system.data;

import com.percussion.services.utils.xml.PSXmlSerializationHelper;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.xml.sax.SAXException;

/**
 * This object represents a single design object dependency.
 */
public class PSDependency implements Serializable
{
   /**
    * Compiler generated serial version ID used for serialization.
    */
   private static final long serialVersionUID = -5933263029349676677L;

   /**
    * The id of the design object for which this object shows all dependencies.
    */
   private long id;
   
   /**
    * All dependent design object, never <code>null</code>, may be empty. 
    */
   private List<PSDependent> dependents = new ArrayList<>();

   /**
    * Default construcctor.
    */
   public PSDependency()
   {
   }
   
   /**
    * Get the design object id for which this shows the dependencies.
    * 
    * @return the design object id for which this shows the dependencies.
    */
   public long getId()
   {
      return id;
   }
   
   /**
    * Set the new design object id for which this shows the dependencies.
    * 
    * @param id the new design object id for which this shows the dependencies.
    */
   public void setId(long id)
   {
      this.id = id;
   }
   
   /**
    * Get the list with all dependents.
    * 
    * @return the list with all dependents, never <code>null</code>, may
    *    be empty.
    */
   public List<PSDependent> getDependents()
   {
      return dependents;
   }
   
   /**
    * Set a new list of dependents.
    * 
    * @param dependents the new list of dependents, may be <code>null</code> or
    *    empty.
    */
   public void setDependents(List<PSDependent> dependents)
   {
      if (dependents == null)
         this.dependents = new ArrayList<>();
      else
         this.dependents = dependents;
   }
   
   /**
    * Add a new dependent.
    * 
    * @param dependent the new dependent to add, not <code>null</code>.
    */
   public void addDependent(PSDependent dependent)
   {
      if (dependent == null)
         throw new IllegalArgumentException("dependent cannot be null");
      
      dependents.add(dependent);
   }
   
   /**
    * Get unique list of the dependent types, comma delimited.
    * 
    * @return The list, never empty, <code>null</code> if 
    * {@link #getDependents()} returns an empty list.
    */
   public String getDependentTypes()
   {
      Set<String> types = new HashSet<>();
      for (PSDependent dependent : dependents)
      {
         types.add(dependent.getDisplayType());
      }
      
      String typeList = null;
      if (!types.isEmpty())
      {
         for (String type : types)
         {
            if (typeList == null)
               typeList = "";
            else
               typeList += ", ";
            
            typeList += type;
         }
      }
      
      return typeList;
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

