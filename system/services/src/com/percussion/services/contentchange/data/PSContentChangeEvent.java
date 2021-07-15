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
