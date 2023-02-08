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
