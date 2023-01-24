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
package com.percussion.services.filter.data;

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.PSGuidHelper;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;
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

   public PSItemFilterRuleParam(boolean clientSide)
   {
      if(!clientSide)
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

   @Override
   public String toString() {
      final StringBuffer sb = new StringBuffer("PSItemFilterRuleParam{");
      sb.append("id=").append(id);
      sb.append(", version=").append(version);
      sb.append(", name='").append(name).append('\'');
      sb.append(", value='").append(value).append('\'');
      sb.append(", ruleDef=").append(ruleDef);
      sb.append('}');
      return sb.toString();
   }
}
