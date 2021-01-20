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

package com.percussion.services.contentchange.data;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.IdClass;
/**
 * Creates a compound primary key for the content change service
 * this allows for quick lookups and hibernate caching of the entry
 * 
 * @author stephenbolton
 *
 */
@IdClass(PSContentChangePK.class)
public class PSContentChangePK implements Serializable
{
 
   @Column(name = "CONTENTID")
   private int contentId;
   
   @Column(name = "CHANGE_TYPE")
   private String changeType;
   
   @Column(name = "SITEID")
   private long siteId;
   

   public PSContentChangePK() {}
   
   public PSContentChangePK(int contentId, long siteId, String changeType)
   {
      this.contentId=contentId;
      this.siteId=siteId;
      this.changeType=changeType;
   }
   
   public int getContentId()
   {
      return contentId;
   }

   public void setContentId(int contentId)
   {
      this.contentId = contentId;
   }

   public String getChangeType()
   {
      return changeType;
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

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((changeType == null) ? 0 : changeType.hashCode());
      result = prime * result + contentId;
      result = prime * result + (int) (siteId ^ (siteId >>> 32));
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      PSContentChangePK other = (PSContentChangePK) obj;
      if (changeType == null)
      {
         if (other.changeType != null)
            return false;
      }
      else if (!changeType.equals(other.changeType))
         return false;
      if (contentId != other.contentId)
         return false;
      if (siteId != other.siteId)
         return false;
      return true;
   }
   
   
}
