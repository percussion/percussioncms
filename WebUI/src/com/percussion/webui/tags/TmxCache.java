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
package com.percussion.webui.tags;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A cache to hold Tmx key/value pairs for multiple languages.
 * Used for the tmx tag libs.
 * @author erikserating
 *
 */
public class TmxCache
{
   /**
    * Private ctor to prevent instantiation.
    */
   private TmxCache()
   {
      
   }
   
   /**
    * Returns the singleton instance of the TmxCache object.
    * @return the singleton object, never <code>null</code>.
    */
   public static TmxCache getInstance()
   {
      if(ms_instance == null)
      {
         ms_instance = new TmxCache();
      }
      return ms_instance;
   }
   
   /**
    * Sets the lang/prefixes that have been indexed.
    * @param lang cannot be <code>null</code> or empty.
    * @param prefixes cannot be <code>null</code> but may be
    * empty.
    */
   public void setIndexed(String lang, String prefixes)
   {
      if(lang == null || lang.length() == 0)
         throw new IllegalArgumentException("lang cannot be null or empty.");
      if(prefixes == null)
         throw new IllegalArgumentException("prefixes cannot be null");
      if(!mi_cachedIndex.containsKey(lang))
      {
         mi_cachedIndex.put(lang, new HashSet<String>());
      }
      Set<String> prefixSet = mi_cachedIndex.get(lang);
      prefixSet.add(prefixes);      
   }
   
   /**
    * Add a new entry to the cache.
    * @param lang cannot be <code>null</code> or empty.
    * @param key cannot be <code>null</code> or empty.
    * @param val may be <code>null</code> or empty.
    */
   public void addEntry(String lang, String key, String val)
   {
      if(lang == null || lang.length() == 0)
         throw new IllegalArgumentException("lang cannot be null or empty.");
      if(key == null || key.length() == 0)
         throw new IllegalArgumentException("key cannot be null or empty.");
      if(!mi_cache.containsKey(lang))
      {
         mi_cache.put(lang, new HashMap<String, String>());
      }
      Map<String, String> valueMap = mi_cache.get(lang);
      valueMap.put(key, val);
   }
   
   /**
    * Retrieve a set of all keys for a specified language.
    * @param lang cannot be <code>null</code> or empty.
    * @return set of keys, never <code>null</code>, may
    * be empty.
    */
   public Set<String> getKeys(String lang)
   {
      if(lang == null || lang.length() == 0)
         throw new IllegalArgumentException("lang cannot be null or empty.");
      if(!mi_cache.containsKey(lang))
         return new HashSet<String>();
      return mi_cache.get(lang).keySet();
   }
   
   /**
    * Retrieve the value based on the lang/key.
    * @param lang cannot be <code>null</code> or empty.
    * @param key cannot be <code>null</code> or empty.
    * @return the value or <code>null</code> if no found.
    */
   public String getValue(String lang, String key)
   {
      if(lang == null || lang.length() == 0)
         throw new IllegalArgumentException("lang cannot be null or empty.");
      if(key == null || key.length() == 0)
         throw new IllegalArgumentException("key cannot be null or empty.");
      if(!mi_cache.containsKey(lang))
         return null;
      return mi_cache.get(lang).get(key);
   }
   
   public boolean isIndexed(String lang, String prefixes)
   {
      if(!mi_cachedIndex.containsKey(lang))
        return false;
      Set<String> prefixSet = mi_cachedIndex.get(lang);
      if(prefixSet == null)
         return false;
      return prefixSet.contains(prefixes);
   }
   
   /**
    * Clear cache per lang or if no lang specified then
    * clear all.
    * @param lang may be <code>null</code> in which case everything
    * will be cleared.
    */
   public void clear(String lang)
   {
      if(lang != null)
      {
         if(mi_cachedIndex.containsKey(lang))
            mi_cachedIndex.put(lang, null);
         if(mi_cache.containsKey(lang))
            mi_cache.put(lang, null);
      }
      else
      {
         for(String l : mi_cachedIndex.keySet())
         {
            clear(l);
         }
      }
      
   }  
   
   /**
    * The singleton instance of the tmx cache.
    */
   private static TmxCache ms_instance;
   
   /**
    * The cached index used to keep track of which lang/prefixes have
    * been cached.
    */
   private Map<String, Set<String>> mi_cachedIndex =
      new HashMap<String, Set<String>>();
   
   /**
    * The cache holding the key/values for each lang indexed.
    */
   private Map<String, Map<String, String>> mi_cache = 
      new HashMap<String, Map<String, String>>();
}

