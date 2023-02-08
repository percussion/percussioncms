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
 * Interface to allow classes to listen for cache events such as adding and 
 * removing cached items, or swapping items between memory and disk storage.
 */
public interface IPSCacheModifiedListener
{
   /**
    * Callback method that provides the listener with an instance of each cache
    * it is listening to for events.  
    * 
    * @param cache The cache the listener is registered with.  Never 
    * <code>null</code>.
    * 
    * @throws IllegalArgumentException if <code>cache</code> is 
    * <code>null</code>.
    */
   public void setCache(PSMultiLevelCache cache);

   /**
    * Called to notify listeners when an item is added to the cache, removed
    * from the cache, or moved between memory and disk.
    * 
    * @param e The event, never <code>null</code>.
    * 
    * @throws IllegalArgumentException if <code>e</code> is  <code>null</code>.
    */
   public void cacheModified(PSCacheEvent e);
}
