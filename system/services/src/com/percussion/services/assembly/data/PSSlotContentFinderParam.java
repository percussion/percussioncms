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
package com.percussion.services.assembly.data;


import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.annotations.*;
import org.hibernate.annotations.Cache;

import java.util.Objects;

/**
 * Data class for parameters that are associated with a given slot definition
 * and supplied to a slot content finder to customize its behavior.
 * 
 * @author dougrand
 */
@Entity
@NaturalIdCache
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "PSSlotContentFinderParam" )
@Table(name = "PSX_SLOT_FINDER_PARAM" )
public class PSSlotContentFinderParam
{
   @Id
   @GenericGenerator(name = "id", strategy = "com.percussion.data.utils.PSGuidHibernateGenerator")
   @GeneratedValue(generator = "id")
   @Column(name = "PARAM_ID", nullable = false)
   private long id;
   
   @SuppressWarnings("unused")
   @Version
   private Integer version;
   
   @Basic
   @NaturalId
   private String name;
   
   @Basic
   private String value;
   
   @ManyToOne(targetEntity = PSTemplateSlot.class)
   @JoinColumn(name = "SLOTID", nullable=false, insertable=false, updatable=false)
   private PSTemplateSlot containingSlot;

   public PSSlotContentFinderParam(PSTemplateSlot psTemplateSlot, String n, String value) {
      this.setContainingSlot(psTemplateSlot);
      this.setName(n);
      this.setValue(value);
   }

   public PSSlotContentFinderParam() {
   }

   /**
    * @return Returns the containingSlot.
    */
   public PSTemplateSlot getContainingSlot()
   {
      return containingSlot;
   }

   /**
    * @param containingSlot The containingSlot to set.
    */
   public void setContainingSlot(PSTemplateSlot containingSlot)
   {
      this.containingSlot = containingSlot;
   }

   /**
    * @return Returns the id.
    */
   public long getId()
   {
      return id;
   }

   /**
    * @param id The id to set.
    */
   public void setId(long id)
   {
      this.id = id;
   }

   /**
    * @return Returns the name.
    */
   public String getName()
   {
      return name;
   }

   /**
    * @param name The name to set.
    */
   public void setName(String name)
   {
      this.name = name;
   }

   /**
    * @return Returns the value.
    */
   public String getValue()
   {
      return value;
   }

   /**
    * @param value The value to set.
    */
   public void setValue(String value)
   {
      this.value = value;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSSlotContentFinderParam)) return false;
      PSSlotContentFinderParam param = (PSSlotContentFinderParam) o;
      return Objects.equals(name, param.name);
   }

   @Override
   public int hashCode() {
      return Objects.hash(name);
   }

   /* (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
      return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE).append("name", name)
      .append("value", value).toString();
   }
   
   
}
