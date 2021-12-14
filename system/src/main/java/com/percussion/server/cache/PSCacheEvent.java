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

package com.percussion.server.cache;

/**
 * This class encapsulates information generated when events occur in the cache.
 * This includes adding items, removing items, accessing items, and moving and
 * retrieving items to and from disk.
 */
public class PSCacheEvent 
{
   /**
    * Constructs a cache event, specifying the action that has taken place, and
    * providing the cache item that was involved in the event.
    * 
    * @param action The type of action, must be one of the CACHE_xxx values.
    * @param object The object involved in the event, may be <code>null</code> 
    * if no object was involved in the event.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    */
   public PSCacheEvent(int action, Object object)
   {
      if (action != CACHE_ITEM_ACCESSED_FROM_DISK && 
         action != CACHE_ITEM_ACCESSED_FROM_MEMORY && 
         action != CACHE_ITEM_ADDED &&
         action != CACHE_ITEM_NOT_FOUND &&
         action != CACHE_ITEM_REMOVED &&
         action != CACHE_ITEM_STORED_TO_DISK)
      {
         throw new IllegalArgumentException("invalid action");
      }
      
      m_action = action;
      m_object = object;
   }
   
   /**
    * Gets the action that occurred.
    * 
    * @return One of the CACHE_xxx values.
    */
   public int getAction()
   {
      return m_action;
   }
   
   /**
    * Gets the object involved in the action.
    * 
    * @return The item, may be <code>null</code> if no item was involved.  
    */
   public Object getObject()
   {
      return m_object;
   }
   
   /**
    * Event type to indicate an item has been addded to the cache.  
    * {@link #getObject()} will return an instance of a <code>PSCacheItem</code> 
    * when {@link #getAction()} returns this type.
    */
   public static final int CACHE_ITEM_ADDED = 0;
   
   /**
    * Event type to indicate an item has been removed from the cache.
    * {@link #getObject()} will return an instance of a <code>PSCacheItem</code> 
    * when {@link #getAction()} returns this type.
    */
   public static final int CACHE_ITEM_REMOVED = 1;
   
   /**
    * Event type to indicate an item has been retrieved from the cache that was
    * stored in memory.  {@link #getObject()} will return an instance of a 
    * <code>PSCacheItem</code> when {@link #getAction()} returns this type.
    */
   public static final int CACHE_ITEM_ACCESSED_FROM_MEMORY = 2;
   
   /**
    * Event type to indicate an item has been retrieved from the cache that was
    * stored on disk. {@link #getObject()} will return an instance of a 
    * <code>PSCacheItem</code> when {@link #getAction()} returns this type.
    */
   public static final int CACHE_ITEM_ACCESSED_FROM_DISK = 3;
   
   /**
    * Event type to indicate an attempt to access an object from the cache 
    * resulted in no object found.  {@link #getObject()} will return 
    * <code>null</code> when {@link #getAction()} returns this type.
    */
   public static final int CACHE_ITEM_NOT_FOUND = 4;
   
   /**
    * Event type to indicate that an item has been moved from memory storage to
    * disk storage. {@link #getObject()} will return an instance of a 
    * <code>PSCacheItem</code> when {@link #getAction()} returns this type.
    */
   public static final int CACHE_ITEM_STORED_TO_DISK = 5;
   
   /**
    * Type of action this event denotes.  Set during ctor, never modified after
    * that.
    */
   private int m_action;
   
   /**
    * The object causing the event, set during ctor, may be <code>null</code>, 
    * never modified after that.  
    */
   private Object m_object;
}
