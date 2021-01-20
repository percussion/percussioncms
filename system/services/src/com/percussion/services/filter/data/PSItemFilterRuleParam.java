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
package com.percussion.services.filter.data;

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.PSGuidHelper;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a single parameter for the rule definition
 * 
 * @author dougrand
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, 
      region = "PSItemFilterRuleParam")
@Table(name = "PSX_ITEM_FILTER_RULE_PARAM")
public class PSItemFilterRuleParam implements Serializable
{
   /**
    * Serial id identifies versions of serialized data
    */
   private static final long serialVersionUID = 1L;
   
   /**
    * Primary key
    */
   @Id
   @Column(name = "FILTER_RULE_PARAM_ID")
   Long id;
   
   /**
    * Hibernate version column
    */
   @Version
   @Column(name = "VERSION", nullable = false)
   Integer version = 0;
   
   /**
    * The name for the given parameter
    */
   @Basic
   @Column(name = "NAME", nullable = false)
   String name;
   
   /**
    * The value for the given parameter
    */
   @Basic
   @Column(name = "VALUE", nullable = false)
   String value;

   /**
    * The parent rule definition that this parameter is associated with
    */
   @ManyToOne(targetEntity = PSItemFilterRuleDef.class)
   @JoinColumn(name = "FILTER_RULE_ID", nullable = false, insertable = false, updatable = false)
   PSItemFilterRuleDef ruleDef;

   /**
    * Default ctor
    */
   public PSItemFilterRuleParam()
   {
      id = PSGuidHelper.generateNextLong(PSTypeEnum.INTERNAL);
   }
   
   /**
    * @return Returns the id.
    */
   public Long getId()
   {
      return id;
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

   /**
    * @return Returns the ruleDef.
    */
   public PSItemFilterRuleDef getRuleDef()
   {
      return ruleDef;
   }

   /**
    * @param ruleDef The ruleDef to set.
    */
   public void setRuleDef(PSItemFilterRuleDef ruleDef)
   {
      this.ruleDef = ruleDef;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSItemFilterRuleParam)) return false;
      PSItemFilterRuleParam that = (PSItemFilterRuleParam) o;
      return Objects.equals(name, that.name) &&
              Objects.equals(ruleDef, that.ruleDef);
   }

   @Override
   public int hashCode() {
      return Objects.hash(name, ruleDef);
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
      return ToStringBuilder.reflectionToString(this);
   }
}
