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

package com.percussion.search;

import com.percussion.cms.objectstore.PSItemChildLocator;
import com.percussion.cms.objectstore.PSKey;
import com.percussion.design.objectstore.PSLocator;

/**
 * This key is used to uniquely identify indexable units of content. Each unit
 * must have a key that uniquely identifies it among all other possible units
 * that are submitted to the search indexer.
 * <p>Instances of this class are immutable.
 * 
 * @author paulhoward
 */
public class PSSearchKey
{
   /**
    * A composite key that uniquely identifies an item, or any complex child 
    * entry in an item. The child key is either absent or present. 
    * It only needs to be present when identifying a child entry fragment.
    * 
    * @param cTypeKey  See {@link #getContentTypeKey()} for description.
    * Always required. The value is not verified to identify a running/existing 
    * editor.
    * 
    * @param itemKey See {@link #getParentLocator()} for description.
    * Never <code>null</code>. The revision may be -1. The value is not 
    * verified that it points to a real item. 
    * <p>Note: some users of this class may require the revision to be properly
    * set, so read each classes doc.
    *  
    * @param itemChildKey Only required if this key identifies a child entry 
    * item fragment. [This is equivalent to the complex child submit name and 
    * sys_childrowid parameter.]
    */
   public PSSearchKey(PSKey cTypeKey, PSLocator itemKey, 
         PSItemChildLocator itemChildKey)
   {
      if (null == cTypeKey)
      {
         throw new IllegalArgumentException("Content type id cannot be null");
      }
      if (null == itemKey)
      {
         throw new IllegalArgumentException("Item's locator cannot be null");
      }
      
      m_contentTypeKey = cTypeKey;
      //Search index is always current revision of items
      m_itemKey = itemKey;
      m_itemChildKey = itemChildKey;
   }

   /**
    * The unique identifier for the content type of this item fragment.
    * 
    * @return Never <code>null</code>.
    */
   public PSKey getContentTypeKey()
   {
      return m_contentTypeKey;
   }
   
   /**
    * The unique locator for the item identified by this key.
    * 
    * @return Never <code>null</code>. Revision may be -1; 
    */
   public PSLocator getParentLocator()
   {
      return m_itemKey;
   }

   /**
    * A key that identifies the complex child w/in this item
    * and the specific child within an instance (table row).
    *  
    * @return May be <code>null</code> if this key is for the parent.
    */
   public PSItemChildLocator getChildId()
   {
      return m_itemChildKey;
   }
   
   /**
    * Provides deep clone functionality.
    * 
    * @return Never <code>null</code>.
    */
   public Object clone()
   {
      PSSearchKey key = (PSSearchKey) this.clone();
      key.m_itemKey = (PSLocator) m_itemKey.clone();
      key.m_contentTypeKey = (PSKey) m_contentTypeKey.clone();
      key.m_itemChildKey = (PSItemChildLocator) m_itemChildKey.clone();
      return key;
   }
   
   //see base class for desc - override default behavior
   public int hashCode()
   {
      /* We convert the ids to strings to avoid duplication of hash codes 
       * if the different keys happen to have the same value.
       */
      return ("" + m_itemKey.hashCode() + m_contentTypeKey.hashCode() 
            + m_itemChildKey.hashCode()).hashCode(); 
   }
   
   /**
    * See {@link #getParentLocator()} for a description.
    * Never <code>null</code> or modified after construction.
    */
   private PSLocator m_itemKey;
   
   /**
    * See {@link #getContentTypeKey()} for description.
    * Never <code>null</code> or modified after construction.
    */
   private PSKey m_contentTypeKey;

   /**
    * See {@link #getChildId()} for a description.
    * Never modified after construction. May be <code>null</code> if this key
    * is only for the parent item.
    */
   private PSItemChildLocator m_itemChildKey;
}
