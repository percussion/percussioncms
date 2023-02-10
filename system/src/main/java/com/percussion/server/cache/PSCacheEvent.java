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
