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
