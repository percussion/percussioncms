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
package com.percussion.services.system.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Immutable;

/**
 * Represent a component property. System component properties are used to 
 * determine visibility. Only used for a couple of actual components. Read only
 * implementation at this time.
 * 
 * @author dougrand
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY, 
      region = "PSUIComponentProperty")
@Table(name = "RXSYSCOMPONENTPROPERTY")
@Immutable
public class PSUIComponentProperty
{
   @Id
   Integer propertyid;
   
   @ManyToOne(targetEntity=PSUIComponent.class)
   @JoinColumn(name = "COMPONENTID", nullable=false, insertable=false, updatable=false)
   PSUIComponent component;
   
   @Column(name = "PROPNAME")
   String name;

   @Column(name = "PROPVALUE")
   String value;

   @Column(name = "PROPDESC")
   String description;

   /**
    * Empty ctor
    */
   public PSUIComponentProperty()
   {
      // 
   }

   /**
    * @return Returns the component.
    */
   public PSUIComponent getComponent()
   {
      return component;
   }

   /**
    * @param component The component to set.
    */
   public void setComponent(PSUIComponent component)
   {
      this.component = component;
   }

   /**
    * @return Returns the description.
    */
   public String getDescription()
   {
      return description;
   }

   /**
    * @param description The description to set.
    */
   public void setDescription(String description)
   {
      this.description = description;
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
    * @return Returns the propertyid.
    */
   public Integer getPropertyid()
   {
      return propertyid;
   }

   /**
    * @param propertyid The propertyid to set.
    */
   public void setPropertyid(Integer propertyid)
   {
      this.propertyid = propertyid;
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
   
   
}
