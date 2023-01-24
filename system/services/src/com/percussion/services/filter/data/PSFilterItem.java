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

import com.percussion.design.objectstore.PSLocator;
import com.percussion.services.filter.IPSFilterItem;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.utils.guid.IPSGuid;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.Objects;

/**
 * Implementation class for a filter item. This is used as a base class for
 * other implementations of a filter item.
 * 
 * @author dougrand
 */
public class PSFilterItem implements IPSFilterItem, Cloneable
{
   /**
    * The guid manager to use.
    */
   private static IPSGuidManager ms_gmgr = null;
   
   /**
    * Item id to be filtered, set through the constructor, never modified or
    * <code>null</code> after construction.
    */
   IPSGuid m_itemId;

   /**
    * Folder id to be filtered, set through the constructor, may be  
    * <code>null</code>. Never modified after construction.
    */
   IPSGuid m_folderId;
   
   /**
    * Site id to be filtered, set through the constructor, may be 
    * <code>null</code>. Never modified after construction.
    */
   IPSGuid m_siteId;
   
   /**
    * Key to use when mapping or putting into a set, calculate on demand
    */
   String m_key = null;
   
   /**
    * For overriding classes that do not want to use the public constructor
    */
   protected PSFilterItem()
   {
   }
   
   /**
    * Ctor 
    * @param itemId the item id, never <code>null</code>
    * @param folderId the folder id, may be <code>null</code>
    * @param siteId the site id, may be <code>null</code>
    */
   public PSFilterItem(IPSGuid itemId, IPSGuid folderId, IPSGuid siteId)
   {
      if (itemId == null)
      {
         throw new IllegalArgumentException("itemId may not be null");
      }
      m_itemId = itemId;
      m_folderId = folderId;
      m_siteId = siteId;
   }
   
   /*
    * (non-Javadoc)
    * @see com.percussion.services.filter.IPSFilterItem#getItemId()
    */
   public IPSGuid getItemId()
   {
      return m_itemId;
   }

   //see base class
   @Override
   public PSFilterItem clone()
   {
      try
      {
         return (PSFilterItem)super.clone();
      }
      catch (CloneNotSupportedException e)
      {
         // this is not possible
         throw new RuntimeException("Impossible clone error", e);
      }
   }


   /*
    * (non-Javadoc)
    * @see com.percussion.services.filter.IPSFilterItem#getFolderId()
    */
   public IPSGuid getFolderId()
   {
      return m_folderId;
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.services.filter.IPSFilterItem#getSiteId()
    */
   public IPSGuid getSiteId()
   {
      return m_siteId;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSFilterItem)) return false;
      PSFilterItem that = (PSFilterItem) o;
      return Objects.equals(m_itemId, that.m_itemId) && Objects.equals(m_folderId, that.m_folderId) && Objects.equals(m_siteId, that.m_siteId) && Objects.equals(m_key, that.m_key);
   }

   @Override
   public int hashCode() {
      return Objects.hash(m_itemId, m_folderId, m_siteId, m_key);
   }

   @Override
   public String toString() {
      final StringBuffer sb = new StringBuffer("PSFilterItem{");
      sb.append("m_itemId=").append(m_itemId);
      sb.append(", m_folderId=").append(m_folderId);
      sb.append(", m_siteId=").append(m_siteId);
      sb.append(", m_key='").append(m_key).append('\'');
      sb.append('}');
      return sb.toString();
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.services.filter.IPSFilterItem#clone(com.percussion.utils.guid.IPSGuid)
    */
   public IPSFilterItem clone(IPSGuid newItemId)
   {
      if (newItemId == null)
      {
         throw new IllegalArgumentException("newItemId may not be null");
      }
      PSFilterItem copy = clone();
      copy.m_itemId = newItemId;
      return copy;
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.services.filter.IPSFilterItem#setFolderId(com.percussion.utils.guid.IPSGuid)
    */
   public void setFolderId(IPSGuid newId)
   {
      m_folderId = newId;
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.services.filter.IPSFilterItem#setItemId(com.percussion.utils.guid.IPSGuid)
    */
   public void setItemId(IPSGuid newId)
   {
      if (newId == null)
      {
         throw new IllegalArgumentException("newId may not be null");
      }
      m_itemId = newId;
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.services.filter.IPSFilterItem#setSiteId(com.percussion.utils.guid.IPSGuid)
    */
   public void setSiteId(IPSGuid newId)
   {
      m_siteId = newId;
   }

   public Object getKey()
   {
      if (m_key == null)
      {
         synchronized(PSFilterItem.class)
         {
            if (ms_gmgr == null)
            {
               ms_gmgr = PSGuidManagerLocator.getGuidMgr();
            }
         }
         StringBuilder b = new StringBuilder();
         PSLocator cloc = ms_gmgr.makeLocator(m_itemId);
         b.append(cloc.getId());
         b.append('-');
         if (m_folderId != null)
         {
            PSLocator floc = ms_gmgr.makeLocator(m_folderId);
            b.append(floc.getId());
         }
         b.append('-');
         if (m_siteId != null)
         {
            b.append(m_siteId.longValue());
         }
         
         m_key = b.toString();
      }
      return m_key;
   }
}
