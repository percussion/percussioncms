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
package com.percussion.services.memory;

import java.io.Serializable;
import java.util.List;

import net.sf.ehcache.CacheManager;

import com.percussion.server.cache.PSCacheStatisticsSnapshot;

/**
 * This service provides access to the runtime cache mechanism. The
 * implementation details are known only to the service implementation.
 * 
 * @author dougrand
 */
public interface IPSCacheAccess
{
   /**
    * The identifier for the cache region used to store relationships
    * used for relationship content finder.
    */
   public final String CONTENT_FINDER_RELS = "slot";
   
   /**
    * The identifier for the cache region used by hibernate 2nd level cache
    * to cache individual relationship object.
    */
   public final String RELATIONSHIP_DATA = "PSRelationshipData";
   
   /**
    * The identifier for the cache region used to store general in memory 
    * objects. These objects are used in a generally read-only fashion. The
    * only modifications performed are those done to transient data. Such 
    * modifications must, of course, be properly guarded against multi-thread
    * access. See the <code>ehcache.xml</code> file for the definition of the
    * cache regions.
    */
   public final String IN_MEMORY_STORE = "memory";

   /**
    * Store the given object into the cache using the specified key and region.
    * 
    * @param key the key to store the object. The semantics of a key are
    *           identical to the key used for <code>Map</code> access.
    * @param data the data to be stored, never <code>null</code>
    * @param region the region to use for storage, never <code>null</code> or
    *           empty
    */
   void save(Serializable key, Serializable data, String region);

   /**
    * Retrieve the given object from the cache using the specified key and
    * region.
    * 
    * @param key the key to store the object. The semantics of a key are
    *           identical to the key used for <code>Map</code> access.
    * @param region the region to use for storage, never <code>null</code> or
    *           empty
    * @return the stored data or <code>null</code> if the data is no longer
    *         present.
    */
   Serializable get(Serializable key, String region);

   /**
    * Remove the given object from the cache using the specified key and region.
    * Has no effect if the object has expired from the cache.
    * 
    * @param key the key to store the object. The semantics of a key are
    *           identical to the key used for <code>Map</code> access.
    * @param region the region to use for storage, never <code>null</code> or
    *           empty
    */
   void evict(Serializable key, String region);

   /**
    * Clear all elements from the cache, regardless of region. This primarily
    * is for use by the cache cleanup from the console, although other uses are
    * valid.
    *
    */
   public void clear();

   /**
    * Clear the specified region in the cache.
    * @param region the region, never <code>null</code> or empty
    */
   void clear(String region);
   
   /**
    * Clear regions that relate to relationship caches. 
    */
   void clearRelationships();
   
   /**
    * Get the statistics for all cache regions.
    * Note, the diskUage is estimated.
    * 
    * @return the statistics of all cache regions. It is sorted by 
    *    region name, never <code>null</code> by may be empty 
    *    (if the cache is not used at all).
    */
   List<PSCacheStatisticsSnapshot> getStatistics();
   
   public CacheManager getManager();
}
