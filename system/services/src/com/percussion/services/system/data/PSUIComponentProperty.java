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
