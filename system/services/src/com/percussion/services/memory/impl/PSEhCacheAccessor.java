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
package com.percussion.services.memory.impl;

import com.percussion.cms.IPSConstants;
import com.percussion.server.cache.PSCacheStatisticsSnapshot;
import com.percussion.services.memory.IPSCacheAccess;
import com.percussion.services.memory.PSCacheAccessLocator;
import com.percussion.services.notification.IPSNotificationListener;
import com.percussion.services.notification.IPSNotificationService;
import com.percussion.services.notification.PSNotificationEvent;
import com.percussion.services.notification.PSNotificationEvent.EventType;
import com.percussion.util.PSBaseBean;
import com.percussion.utils.guid.IPSGuid;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.statistics.LiveCacheStatistics;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Accesses the currently configured hibernate cache. Allows us to store
 * additional information in the cache beyond hibernate's second level cache.
 * 
 * @author dougrand
 */
@PSBaseBean("sys_cacheAccessor")
public class PSEhCacheAccessor implements IPSCacheAccess
{
   /**
    * Logger for this class
    */
   private static final Logger log = LogManager.getLogger(IPSConstants.CACHING_LOG);
   
   /**
    * Implements the notification service endpoint used to invalidate objects
    * that we're caching that have been saved or deleted. If we're being called,
    * it is an invalidation event and the target will contain a guid.
    */
   public class PSEhCacheNotificationListener
         implements
            IPSNotificationListener
   {
      public void notifyEvent(PSNotificationEvent notification)
      {
         IPSCacheAccess cache = PSCacheAccessLocator.getCacheAccess();
         IPSGuid g = (IPSGuid) notification.getTarget();
         cache.evict(g, IPSCacheAccess.IN_MEMORY_STORE);
      }
   }

   /**
    * Holds reference to the cache implementation
    */
   private CacheManager m_manager = null;

   /**
    * Notification service, initialized by Spring
    */
   private IPSNotificationService m_notificationService = null;

   /**
    * Ctor
    *
    */
   public PSEhCacheAccessor()
   {
      m_manager = CacheManager.create();
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.memory.IPSCacheAccess#save(java.io.Serializable,
    *      java.io.Serializable, java.lang.String)
    */
   public void save(Serializable key, Serializable data, String region)
   {
      if (key == null)
      {
         throw new IllegalArgumentException("key may not be null");
      }
      if (data == null)
      {
         throw new IllegalArgumentException("data may not be null");
      }
      if (StringUtils.isBlank(region))
      {
         throw new IllegalArgumentException("region may not be null or empty");
      }
      if (m_manager == null)
         throw new IllegalStateException("Cache not configured");
      Ehcache cache= m_manager.getEhcache(region);
      if (cache == null)
         throw new IllegalArgumentException("Region " + region + " not found");
      cache.put(new Element(key, data));
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.memory.IPSCacheAccess#get(java.io.Serializable,
    *      java.lang.String)
    */
   public Serializable get(Serializable key, String region)
   {
      if (key == null)
      {
         throw new IllegalArgumentException("key may not be null");
      }
      if (StringUtils.isBlank(region))
      {
         throw new IllegalArgumentException("region may not be null or empty");
      }
      if (m_manager == null)
         throw new IllegalStateException("Cache not configured");
      Ehcache cache = m_manager.getEhcache(region);
      if (cache == null)
         throw new IllegalArgumentException("Region " + region + " not found");
      try
      {
         Element el = cache.get(key);
         if (el != null)
            return el.getValue();
         else
            return null;
      }
      catch (CacheException e)
      {
         throw new IllegalStateException("Problem with cache", e);
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.memory.IPSCacheAccess#evict(java.io.Serializable,
    *      java.lang.String)
    */
   public void evict(Serializable key, String region)
   {
      if (key == null)
      {
         throw new IllegalArgumentException("key may not be null");
      }
      if (StringUtils.isBlank(region))
      {
         throw new IllegalArgumentException("region may not be null or empty");
      }
      if (m_manager == null)
         throw new IllegalStateException("Cache not configured");
      Ehcache cache = m_manager.getEhcache(region);
      if (cache == null)
         throw new IllegalArgumentException("Region " + region + " not found");
      cache.remove(key);
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.memory.IPSCacheAccess#clear()
    */
   public void clear()
   {
      for (String name : m_manager.getCacheNames())
      {
         clear(name);
      }

      log.debug("Cleared all EhCache.");
   }
   
   /*
    * (non-Javadoc)
    * @see com.percussion.services.memory.IPSCacheAccess#clear(java.lang.String)
    */
   public void clear(String name)
   {
      if (name == null || StringUtils.isBlank(name))
      {
         throw new IllegalArgumentException("name may not be null or empty");
      }
      Ehcache cache= m_manager.getEhcache(name);
      if (cache != null)
      {
         cache.removeAll(); // Remove all elements from cache
      }
      else
      {
         log.warn("Cannot clear unknown cache: {}" ,name);
      }
   }

   public void clearRelationships()
   {
      clear(CONTENT_FINDER_RELS);
      clear(RELATIONSHIP_DATA);
   }
   
   /**
    * Get the notification service
    * 
    * @return the notification service, never <code>null</code> in a proper
    *         configuration
    */
   public IPSNotificationService getNotificationService()
   {
      return m_notificationService;
   }

   /**
    * Set the notification service
    * 
    * @param notificationService called by Spring to set the service, never
    *           <code>null</code>
    */
   @Autowired
   public void setNotificationService(IPSNotificationService notificationService)
   {
      if (notificationService == null)
      {
         throw new IllegalArgumentException(
               "notificationService may not be null");
      }
      m_notificationService = notificationService;
      m_notificationService.addListener(EventType.OBJECT_INVALIDATION,
            new PSEhCacheNotificationListener());
   }
   /*
    * (non-Javadoc)
    * @see com.percussion.services.memory.IPSCacheAccess#getStatistics()
    */
   public List<PSCacheStatisticsSnapshot> getStatistics()
   {
      List<PSCacheStatisticsSnapshot> statList = 
         new ArrayList<>();

      PSCacheStatisticsSnapshot cacheStat;
      for (String name : m_manager.getCacheNames())
      {
         cacheStat = getCacheStatistics(m_manager.getEhcache(name));
         cacheStat.setName(name);
         statList.add(cacheStat);
      }

      Collections.sort(statList, new Comparator<PSCacheStatisticsSnapshot>()
      {
         public int compare(PSCacheStatisticsSnapshot o1,
               PSCacheStatisticsSnapshot o2)
         {
            return o1.getName().compareToIgnoreCase(o2.getName());
         }
      });

      return statList;
   }

   /**
    * The maximum number of seconds an element can exist in the cache without being accessed.
    * The element expires at this limit and will no longer be returned from the cache.
    * The default value is 0, which means no TTI eviction takes place (infinite lifetime).
    *
    * @param key
    * @param region
    * @param timeToIdleSeconds
    */
   @Override
   public boolean setTimeToIdle(Serializable key, String region, int timeToIdleSeconds) {
      if (key == null)
      {
         throw new IllegalArgumentException("key may not be null");
      }
      if (StringUtils.isBlank(region))
      {
         throw new IllegalArgumentException("region may not be null or empty");
      }
      if (m_manager == null)
         throw new IllegalStateException("Cache not configured");
      Cache cache = m_manager.getCache(region);
      if (cache == null)
         throw new IllegalArgumentException("Region " + region + " not found");
      try
      {
         Element el = cache.get(key);
         if (el != null){
            el.setEternal(false);
            el.setTimeToIdle(timeToIdleSeconds);

            return true;
         }
      }
      catch (CacheException e)
      {
         throw new IllegalStateException("Problem with cache", e);
      }

      return false;
   }

   /**
    * The maximum number of seconds an element can exist in the cache regardless of use.
    * The element expires at this limit and will no longer be returned from the cache.
    * The default value is 0, which means no TTL eviction takes place (infinite lifetime).
    *
    * @param key
    * @param region
    * @param timeToLiveSeconds
    */
   @Override
   public boolean setTimeToLive(Serializable key, String region, int timeToLiveSeconds) {
      if (key == null)
      {
         throw new IllegalArgumentException("key may not be null");
      }
      if (StringUtils.isBlank(region))
      {
         throw new IllegalArgumentException("region may not be null or empty");
      }
      if (m_manager == null)
         throw new IllegalStateException("Cache not configured");
      Cache cache = m_manager.getCache(region);
      if (cache == null)
         throw new IllegalArgumentException("Region " + region + " not found");
      try
      {
         Element el = cache.get(key);
         if (el != null){
            el.setEternal(false);
            el.setTimeToLive(timeToLiveSeconds);

            return true;
         }
      }
      catch (CacheException e)
      {
         throw new IllegalStateException("Problem with cache", e);
      }

      return false;
   }

   /**
    * Get the statistics for the given cache region.
    * 
    * @param cache the cache region in question, assumed not <code>null</code>.
    * @return the statistics of the cache region, never <code>null</code>.
    */
   private PSCacheStatisticsSnapshot getCacheStatistics(Ehcache cache)
   {

      LiveCacheStatistics stat = cache.getLiveCacheStatistics();

      long memItems = stat.getSize();
      long memUsage = stat.getLocalHeapSizeInBytes();
      long misses = stat.getCacheMissCount();
      long totalHits = stat.getCacheHitCount();

      long diskHits = stat.getOnDiskHitCount();
      long diskItems = stat.getLocalDiskSize();
      long diskUsage = 0;
      if (diskItems > 0 && memItems > 0)
         diskUsage = diskItems * (memUsage / memItems);

      return new PSCacheStatisticsSnapshot(diskHits, diskItems, diskUsage,
            memItems, memUsage, misses, totalHits);      
   }
   
   public CacheManager getManager()
   {
      return m_manager;
   }
}
