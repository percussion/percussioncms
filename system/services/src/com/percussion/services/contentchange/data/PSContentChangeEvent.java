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
package com.percussion.services.contentchange.data;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * Represents a changed item in the system
 * @author JaySeletz
 *
 */
@Entity
@Cache (usage=CacheConcurrencyStrategy.READ_WRITE, 
region = "PSContentChangeEvent")
@Table(name = "PSX_CONTENTCHANGEEVENT")
@IdClass(PSContentChangePK.class)
public class PSContentChangeEvent
{

   @Id
   @Column(name = "CONTENTID")
   private int contentId;
   
   @Id
   @Column(name = "CHANGE_TYPE")
   private String changeType;

   @Id
   @Column(name = "SITEID")
   private long siteId;

   /**
    * @param contentId
    */
   public void setContentId(int contentId)
   {
      this.contentId = contentId;
   }

   public int getContentId()
   {
      return contentId;
   }

   public PSContentChangeType getChangeType()
   {
      return PSContentChangeType.valueOf(changeType);
   }
   
   public void setChangeType(PSContentChangeType changeType)
   {
      this.changeType = changeType.name();
   }
   
   public void setChangeType(String changeType)
   {
      this.changeType = changeType;
   }

   public long getSiteId()
   {
      return siteId;
   }

   public void setSiteId(long siteId)
   {
      this.siteId = siteId;
   }

}
