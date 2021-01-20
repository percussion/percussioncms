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
package com.percussion.services.sitemgr.data;

import com.percussion.services.sitemgr.IPSLocationScheme;

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * Represents a location scheme parameter
 * 
 * @author dougrand
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, 
      region = "PSLocationScheme_Parameters")
@Table(name = "RXLOCATIONSCHEMEPARAMS")
public class PSLocationSchemeParameter implements Serializable
{
   /**
    * Serial id identifies versions of serialized data
    */
   private static final long serialVersionUID = 1L;
   
   @Id
   @Column(name = "SCHEMEPARAMID")
   Integer  parameterId;
   
   @ManyToOne(targetEntity = PSLocationScheme.class)
   @JoinColumn(name = "SCHEMEID", nullable = false, insertable = false, updatable = false)
   IPSLocationScheme scheme;
   
   @Basic
   Integer  sequence;
   
   @Basic
   String   type;
   
   @Basic
   String   name;
   
   @Basic
   String   value;
   
   /**
    * @param parameterId The parameterId to set.
    */
   public void setParameterId(Integer parameterId)
   {
      this.parameterId = parameterId;
   }

   /**
    * Get the name of the parameter
    * @return Returns the name, never <code>null</code> or empty
    */
   public String getName()
   {
      return name;
   }

   /**
    * @param name The name to set, never <code>null</code> or empty
    */
   public void setName(String name)
   {
      if (StringUtils.isBlank(name))
      {
         throw new IllegalArgumentException("name may not be null or empty");
      }
      this.name = name;
   }

   /**
    * Get the primary key used to store this parameter
    * @return Returns the parameterId.
    */
   public Integer getParameterId()
   {
      return parameterId;
   }

   /**
    * Get the parent location scheme for this parameter
    * @return Returns the scheme, never <code>null</code>
    */
   public IPSLocationScheme getScheme()
   {
      return scheme;
   }

   /**
    * Set a new location scheme, should only be used on a newly constructed
    * object.
    * @param scheme The scheme to set, never <code>null</code>
    */
   public void setScheme(IPSLocationScheme scheme)
   {
      if (scheme == null)
      {
         throw new IllegalArgumentException("scheme may not be null");
      }
      this.scheme = scheme;
   }

   /**
    * The sequence is really only used in the user interface to order the
    * parameters. 
    * @return Returns the sequence.
    */
   public Integer getSequence()
   {
      return sequence;
   }

   /**
    * Set a new sequence
    * @param sequence The sequence to set, never <code>null</code>
    */
   public void setSequence(Integer sequence)
   {
      if (sequence == null)
      {
         throw new IllegalArgumentException("sequence may not be null");
      }
      this.sequence = sequence;
   }

   /**
    * Get the type of the value.
    * @return Returns the type.
    */
   public String getType()
   {
      return type;
   }

   /**
    * Set a new type
    * @param type The type to set, never <code>null</code> or empty
    */
   public void setType(String type)
   {
      if (StringUtils.isBlank(type))
      {
         throw new IllegalArgumentException("type may not be null or empty");
      }
      this.type = type;
   }

   /**
    * Get the value of the particular parameter
    * @return Returns the value, never <code>null</code> or empty
    */
   public String getValue()
   {
      return value;
   }

   /**
    * Set a new value
    * @param value The value to set, never <code>null</code> or empty
    */
   public void setValue(String value)
   {
      if (StringUtils.isBlank(value))
      {
         throw new IllegalArgumentException("value may not be null or empty");
      }
      this.value = value;
   }
   
   /** (non-Javadoc)
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj)
   {
      PSLocationSchemeParameter pb = (PSLocationSchemeParameter) obj;
      
      return new EqualsBuilder()
         .append(parameterId, pb.parameterId)
         .append(sequence, pb.sequence)
         .append(type, pb.type)
         .append(name, pb.name)
         .append(value, pb.value)
         .isEquals();
   }
   
   /** (non-Javadoc)
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode()
   {
      return name.hashCode();
   }

   /** (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
      return ToStringBuilder.reflectionToString(this).toString();
   }
}
