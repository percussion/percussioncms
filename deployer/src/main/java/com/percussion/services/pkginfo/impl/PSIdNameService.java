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

package com.percussion.services.pkginfo.impl;

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.memory.IPSCacheAccess;
import com.percussion.services.pkginfo.IPSIdNameService;
import com.percussion.services.pkginfo.data.PSIdName;
import com.percussion.utils.guid.IPSGuid;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * Implementation of the id-name service.
 */
@Transactional
public class PSIdNameService
   implements IPSIdNameService
{

   private SessionFactory sessionFactory;

   public SessionFactory getSessionFactory() {
      return sessionFactory;
   }

   @Autowired
   public void setSessionFactory(SessionFactory sessionFactory) {
      this.sessionFactory = sessionFactory;
   }


   @SuppressWarnings("unchecked")
   synchronized public void deleteAll()
   {
      Session session = sessionFactory.getCurrentSession();

      loadAll().forEach(session::delete);
         
         clearCache();

      }
   
   synchronized public void saveIdName(PSIdName mapping)
   {
      if (mapping == null)
         throw new IllegalArgumentException("mapping may not be null");
      
      sessionFactory.getCurrentSession().saveOrUpdate(mapping);
      clearCache();
   }

   synchronized public IPSGuid findId(String name, PSTypeEnum type)
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
   
   synchronized public String findName(IPSGuid guid)
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
      Session session = sessionFactory.getCurrentSession();
      return session.createCriteria(PSIdName.class).list();

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
   @SuppressWarnings("unchecked")
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
           
      Map<String, IPSGuid> ntMap = new HashMap<String, IPSGuid>();
      Map<IPSGuid, String> idMap = new HashMap<IPSGuid, String>();
      
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
      return (HashMap<String, IPSGuid>) m_cache.get(NAME_TYPE_ID_MAP_KEY,
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
      return (HashMap<IPSGuid, String>) m_cache.get(ID_NAME_MAP_KEY,
            IPSCacheAccess.IN_MEMORY_STORE);
   }
   
   /**
    * Loads the name/type to id and id to name maps from the repository and
    * caches references.
    * 
    * @param ntMap The name/type to id map, assumed not <code>null</code>.
    * @param idMap The id to name map, assumed not <code>null</code>.
    */
   @SuppressWarnings("unchecked")
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

      m_cache.save(NAME_TYPE_ID_MAP_KEY, (Serializable) ntMap,
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
     
      m_cache.save(ID_NAME_MAP_KEY, (Serializable) idMap,
            IPSCacheAccess.IN_MEMORY_STORE);
   }
   
   /**
    * Removes the name/type to id and id to name maps from the cache.
    */
   private void clearCache()
   {
      if (m_cache != null)
      {
         m_cache.evict(NAME_TYPE_ID_MAP_KEY, IPSCacheAccess.IN_MEMORY_STORE);
         m_cache.evict(ID_NAME_MAP_KEY, IPSCacheAccess.IN_MEMORY_STORE);
      }
   }
   
   /**
    * Spring property accessor.
    *
    * @return get the cache service
    */
   public IPSCacheAccess getCache()
   {
      return m_cache;
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
      m_cache = cache;
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
   private IPSCacheAccess m_cache;
}
