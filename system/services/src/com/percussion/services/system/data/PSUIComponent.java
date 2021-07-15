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

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a user interface component. The HTML user interface is built of
 * components. Components abstract a connection between a named part of the
 * interface and the implementation of that named part. This is a read-only
 * implementation at this time.
 * 
 * @author dougrand
 * 
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY, region = "PSUIComponent")
@Table(name = "RXSYSCOMPONENT")
public class PSUIComponent
{
   @Id
   Integer componentid;

   String name;

   String displayname;

   String description;

   String url;

   Integer type;

   Integer state;

   @OneToMany(targetEntity = PSUIComponentProperty.class, cascade =
   {CascadeType.ALL}, fetch = FetchType.EAGER, orphanRemoval = true)

   @JoinColumn(name = "COMPONENTID", nullable = false, insertable = false, updatable = false)
   @Fetch(FetchMode. SUBSELECT)
   Set<PSUIComponentProperty> properties = new HashSet<>();

   /**
    * Empty ctor
    */
   public PSUIComponent() {
      // Do nothing
   }

   /**
    * @return Returns the componentId.
    */
   public Integer getComponentId()
   {
      return componentid;
   }

   /**
    * @param componentId The componentId to set.
    */
   public void setComponentId(Integer componentId)
   {
      this.componentid = componentId;
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
    * @return Returns the displayname.
    */
   public String getDisplayname()
   {
      return displayname;
   }

   /**
    * @param displayname The displayname to set.
    */
   public void setDisplayname(String displayname)
   {
      this.displayname = displayname;
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
    * @return Returns the state.
    */
   public Integer getState()
   {
      return state;
   }

   /**
    * @param state The state to set.
    */
   public void setState(Integer state)
   {
      this.state = state;
   }

   /**
    * @return Returns the type.
    */
   public Integer getType()
   {
      return type;
   }

   /**
    * @param type The type to set.
    */
   public void setType(Integer type)
   {
      this.type = type;
   }

   /**
    * @return Returns the url.
    */
   public String getUrl()
   {
      return url;
   }

   /**
    * @param url The url to set.
    */
   public void setUrl(String url)
   {
      this.url = url;
   }

   /**
    * @return Returns the componentid.
    */
   public Integer getComponentid()
   {
      return componentid;
   }

   /**
    * @return Returns the properties.
    */
   public Set<PSUIComponentProperty> getProperties()
   {
      return properties;
   }
   
   

}
