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

package com.percussion.services.pkginfo.impl;

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.memory.IPSCacheAccess;
import com.percussion.services.pkginfo.IPSIdNameService;
import com.percussion.services.pkginfo.data.PSIdName;
import com.percussion.util.PSBaseBean;
import com.percussion.utils.guid.IPSGuid;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * Implementation of the id-name service.
 */
@Transactional
@PSBaseBean("sys_idNameService")
public class PSIdNameService
   implements IPSIdNameService
{
   @PersistenceContext
   private EntityManager entityManager;

   private Session getSession(){
      return entityManager.unwrap(Session.class);
   }


   public synchronized  void deleteAll()
   {
      Session session = getSession();

      loadAll().forEach(session::delete);
         
         clearCache();

      }

   public synchronized  void saveIdName(PSIdName mapping)
   {
      if (mapping == null)
         throw new IllegalArgumentException("mapping may not be null");
      
      getSession().saveOrUpdate(mapping);
      clearCache();
   }

   public synchronized  IPSGuid findId(String name, PSTypeEnum type)
   {
      if (StringUtils.isBlank(name))
      {
         throw new IllegalArgumentException("name may not be null or empty");
      }
      
      if (type == null)
      {
         throw new IllegalArgumentException("type may not be null");
      }
      
      String key = getNameTypeKey(name, type);
            
      return loadNameTypeToIdMap().get(key);
   }

   public synchronized  String findName(IPSGuid guid)
   {
      if (guid == null)
      {
         throw new IllegalArgumentException("guid may not be null");
      }
      
      return loadIdToNameMap().get(guid);
   }
   
   /**
    * Load all id-name mappings from the repository.
    * 
    * @return A collection of id-name mappings, not <code>null</code>, may be
    * empty.
    */
   @SuppressWarnings("unchecked")
   private Collection<PSIdName> loadAll()
   {
      Session session = getSession();
      return session.createCriteria(PSIdName.class).list();

//      Reverting the changes as Package Install is failing
//      Session session = getSession();
//      CriteriaBuilder builder = session.getCriteriaBuilder();
//      CriteriaQuery<PSIdName> criteria = builder.createQuery(PSIdName.class);
//      return entityManager.createQuery(criteria).getResultList();

   }
   
   /**
    * Get the name/type id map.
    *
    * @return Map of dependency names/types to guids, with a name-type
    * <code>String</code> as key, see
    * {@link #getNameTypeKey(String, PSTypeEnum)}, and the <code>IPSGuid</code>
    * object as value.  Never <code>null</code>.
    */
   @SuppressWarnings("unchecked")
   private Map<String, IPSGuid> loadNameTypeToIdMap()
   {
      return loadMap(NAME_TYPE_ID_MAP);
   }
   
   /**
    * Get the id to name map.
    *
    * @return Map of guids to dependency names, with an <code>IPSGuid</code> as
    * key and name as value.  Never <code>null</code>.
    */
   @SuppressWarnings("unchecked")
   private Map<IPSGuid, String> loadIdToNameMap()
   {
      return loadMap(ID_NAME_MAP);
   }
   
   /**
    * Load the specified map from the cache, create and load if missing.
    * 
    * @param requiredMap Indicates the map which is being requested.  Must be
    * one of {@link #NAME_TYPE_ID_MAP}, {@link #ID_NAME_MAP}.
    * 
    * @return The desired map.  Never <code>null</code>.
    */
   private Map loadMap(int requiredMap)
   {
      if (requiredMap == NAME_TYPE_ID_MAP)
      {
         Map m = getNameTypeToIdMap();
         if (m != null)
         {
            return m;
         }
      }
      else if (requiredMap == ID_NAME_MAP)
      {
         Map m = getIdToNameMap();
         if (m != null)
         {
            return m;
         }
      }
      else
      {
         throw new IllegalArgumentException("unknown map required for load: "
               + requiredMap);
      }
           
      Map<String, IPSGuid> ntMap = new HashMap<>();
      Map<IPSGuid, String> idMap = new HashMap<>();
      
      loadMaps(ntMap, idMap);

      if (requiredMap == NAME_TYPE_ID_MAP)
      {
         return ntMap;
      }
      else
      {
         return idMap;
      }
   }
   
   /**
    * Get the name/type id map from the cache.
    *
    * @return Map of dependency names/types to guids, with a name-type
    * <code>String</code> as key, see
    * {@link #getNameTypeKey(String, PSTypeEnum)}, and the <code>IPSGuid</code>
    * object as value.  May be <code>null</code>.
    */
   @SuppressWarnings("unchecked")
   private Map<String, IPSGuid> getNameTypeToIdMap()
   {
      return (HashMap<String, IPSGuid>) cache.get(NAME_TYPE_ID_MAP_KEY,
            IPSCacheAccess.IN_MEMORY_STORE);
   }
   
   /**
    * Get the id name map from the cache.
    *
    * @return Map of guids to dependency names, with an <code>IPSGuid</code>
    * object as key and name as value.  May be <code>null</code>.
    */
   @SuppressWarnings("unchecked")
   private Map<IPSGuid, String> getIdToNameMap()
   {
      return (HashMap<IPSGuid, String>) cache.get(ID_NAME_MAP_KEY,
            IPSCacheAccess.IN_MEMORY_STORE);
   }
   
   /**
    * Loads the name/type to id and id to name maps from the repository and
    * caches references.
    * 
    * @param ntMap The name/type to id map, assumed not <code>null</code>.
    * @param idMap The id to name map, assumed not <code>null</code>.
    */
   private void loadMaps(Map<String, IPSGuid> ntMap, Map<IPSGuid, String> idMap)
   {
      Collection<PSIdName> mappings = loadAll();
      
      populateNameTypeToIdMap(mappings, ntMap);
      populateIdToNameMap(mappings, idMap);
   }
   
   /**
    * Populates the specified name/type to id map using the specified collection
    * of mappings.
    *  
    * @param mappings The collection of id-name mappings, assumed not
    * <code>null</code>.
    * @param ntMap The name/type to id map, assumed not <code>null</code>.
    */
   private void populateNameTypeToIdMap(
         Collection<PSIdName> mappings, Map<String, IPSGuid> ntMap)
   {
      for (PSIdName mapping : mappings)
      {
         IPSGuid guid = new PSGuid(mapping.getId());
         String name = mapping.getName();

         ntMap.put(getNameTypeKey(name, mapping.getType()), guid);
      }

      cache.save(NAME_TYPE_ID_MAP_KEY, (Serializable) ntMap,
            IPSCacheAccess.IN_MEMORY_STORE);
   }
   
   /**
    * Populates the specified id to name map using the specified collection
    * of mappings.
    *  
    * @param mappings The collection of id-name mappings, assumed not
    * <code>null</code>.
    * @param idMap The id to name map, assumed not <code>null</code>.
    */
   private void populateIdToNameMap(Collection<PSIdName> mappings, 
         Map<IPSGuid, String> idMap)
   {
      for (PSIdName mapping : mappings)
      {
         IPSGuid guid = new PSGuid(mapping.getId());
         String name = mapping.getName();

         idMap.put(guid, name);
      }
     
      cache.save(ID_NAME_MAP_KEY, (Serializable) idMap,
            IPSCacheAccess.IN_MEMORY_STORE);
   }
   
   /**
    * Removes the name/type to id and id to name maps from the cache.
    */
   private void clearCache()
   {
      if (cache != null)
      {
         cache.evict(NAME_TYPE_ID_MAP_KEY, IPSCacheAccess.IN_MEMORY_STORE);
         cache.evict(ID_NAME_MAP_KEY, IPSCacheAccess.IN_MEMORY_STORE);
      }
   }
   
   /**
    * Spring property accessor.
    *
    * @return get the cache service
    */
   public IPSCacheAccess getCache()
   {
      return cache;
   }

   /**
    * Set the cache service.
    *
    * @param cache the service, never <code>null</code>
    */
   public void setCache(IPSCacheAccess cache)
   {
      if (cache == null)
      {
         throw new IllegalArgumentException("cache may not be null");
      }
      this.cache = cache;
   }
   
   /**
    * Generate the name/type <code>String</code> used as the key in the
    * name/type id map, see {@link #loadNameTypeToIdMap()}.  The given name
    * will be converted to lowercase in the key.
    * 
    * @param name The dependency name, assumed not <code>null</code> or empty.
    * @param type The system type, assumed not <code>null</code>.
    * 
    * @return Key in the form 'name-type'.
    */
   private String getNameTypeKey(String name, PSTypeEnum type)
   {
      return name.toLowerCase() + '-' + type.toString();
   }
   
   /**
    * Cache key for name/type to id map key.
    */
   private static final String NAME_TYPE_ID_MAP_KEY =
      "com.percussion.deployer.services.impl.PSIdNameService.name_type_id_map";

   /**
    * Cache key for the id to name map key.
    */
   private static final String ID_NAME_MAP_KEY = 
      "com.percussion.deployer.services.impl.PSIdnameService.id_name_map";
   
   /**
    * Constant for the name/type to id map.  See {@link #loadMap(int)}.
    */
   private static final int NAME_TYPE_ID_MAP = 0;
   
   /**
    * Constant for the id to name map.  See {@link #loadMap(int)}.
    */
   private static final int ID_NAME_MAP = 1;
   
   /**
    * Cache service, used to invalidate mapping information.
    */
   private IPSCacheAccess cache;
}
