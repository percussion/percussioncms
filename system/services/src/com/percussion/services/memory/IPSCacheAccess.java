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
package com.percussion.services.memory;

import com.percussion.server.cache.PSCacheStatisticsSnapshot;
import net.sf.ehcache.CacheManager;

import java.io.Serializable;
import java.util.List;

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

   /**
	 * The maximum number of seconds an element can exist in the cache without being accessed.
	 * The element expires at this limit and will no longer be returned from the cache.
	 * The default value is 0, which means no TTI eviction takes place (infinite lifetime).
	 *
	 * @param key
	 * @param region
	 * @param timeToIdleSeconds
	 */
   boolean setTimeToIdle(Serializable key, String region, int timeToIdleSeconds);

   /**
	 * The maximum number of seconds an element can exist in the cache regardless of use.
	 * The element expires at this limit and will no longer be returned from the cache.
	 * The default value is 0, which means no TTL eviction takes place (infinite lifetime).
	 *
	 * @param key
	 * @param region
	 * @param timeToLiveSeconds
	 */
   boolean setTimeToLive(Serializable key, String region, int timeToLiveSeconds);

    CacheManager getManager();
}

