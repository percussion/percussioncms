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
