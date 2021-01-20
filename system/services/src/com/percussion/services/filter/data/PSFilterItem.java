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
package com.percussion.services.filter.data;

import com.percussion.design.objectstore.PSLocator;
import com.percussion.services.filter.IPSFilterItem;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.utils.guid.IPSGuid;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

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

   /* (non-Javadoc)
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj)
   {
      return EqualsBuilder.reflectionEquals(this, obj);
   }

   /* (non-Javadoc)
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode()
   {
      return HashCodeBuilder.reflectionHashCode(this);
   }

   /* (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
      return ToStringBuilder.reflectionToString(this);
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
