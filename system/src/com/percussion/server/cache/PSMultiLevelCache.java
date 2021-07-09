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

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Provides caching of items using a key set.  The key mechanism is hierarchical 
 * in nature.  A fixed number of keys must be supplied, defined
 * when creating the cache, and the item will be stored using the key set as a 
 * unique identifier.  Items may only be retrieved by providing a full key (all 
 * keys supplied), but may be flushed using partial keys (<code>null</code> 
 * values in the keyset array). Any subset of the keyset may be used to identify 
 * a group of common items to be flushed.  Any omitted keys will cause all 
 * objects at the level to be included.  For example, say a set of items are 
 * stored using a key that is composed of the following:
 * <ol>
 * <li>server</li>
 * <li>appname</li>
 * <li>resource</li>
 * <li>sessionid</li>
 * </ol>
 * When flushing, if only server and sessionid are supplied, then that will 
 * select all items on the specified server with the specified sessionid, 
 * regardless of the appname and resource.  The order of the keys is critical 
 * and must be maintained when adding, accessing, and removing items from the 
 * cache.  To omit a key value, supply a <code>null</code> value in that 
 * position in the keyset array.  So for the above example, the keyset provided
 * for the flush command would be <code>{"myServer", null, null, 
 * "25c346b1e7b4e0ace4a1ea39f1adc6b8dabae267"}</code>.
 * <br>
 * Cache is multi-level in that it caches items both in memory and on disk,
 * depending on intial settings, resource usage, and item usage.  Handles 
 * automatically removing items from the cache that have aged or exceed 
 * memory restrictions (based on an LRU algorithm), and manages the swapping 
 * items from memory to disk and back to maintain memory restrictions.  Items
 * are initially stored in memory, and the items accessed least recently are
 * moved to disk when memory storage is full.  When disk storage has reached
 * its limitations, least recently used items on disk are flushed to free
 * resources.
 */
public class PSMultiLevelCache 
{
   /**
    * Constructs a cache using the specified settings.  
    * 
    * @param keySize The size of the array that must be used to specify a set
    * of keys that uniquely identifies an item in the cache. Must be greater 
    * than <code>0</code>.
    * @param agingTime The amount of time in minutes to allow an object to be 
    * idle in the cache before it is flushed. Provide <code>-1</code> to allow 
    * unlimited time (never expires), or else a number greater than 
    * <code>0</code>.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    */ 
   public PSMultiLevelCache(int keySize, long agingTime)
   {
      if (keySize <= 0)
         throw new IllegalArgumentException("keySize must be greater than 0");
         
      if (agingTime < -1 || agingTime == 0)
         throw new IllegalArgumentException("invalid aging time");
         
      m_keySize = keySize;
      m_cacheId = createCacheId();
      
      // set up keys to flush entire cache
      m_flushAllKeys = new Object[m_keySize];
      for (int i = 0; i < m_keySize; i++)
         m_flushAllKeys[i] = null;

      m_agingManager = new PSCacheAgingManager(agingTime);
      addCacheModifiedListener(m_agingManager);
   }
   
   /**
    * Get the id that identifies this cache.
    * 
    * @return The id. Each instance of this class will return a value 
    * that is unique among all current instances of this class.
    */
   public int getCacheId()
   {
      return m_cacheId;
   }
   
   /**
    * Gets the keysize for this cache.
    *  
    * @return The size of the array that must be used to specify a set
    * of keys that uniquely identifies an item in the cache.  Will be greater 
    * than <code>0</code>.
    */
   public int getKeySize()
   {
      return m_keySize;
   }
   
   /**
    * Add an item to the cache.
    * 
    * @param keys An array of keys that will uniquely identify this item, may 
    * not be <code>null</code>, may not contain any <code>null</code> entries,
    * and must contain the number of entries specified when the cache was
    * constructed.  If an item identified by the keys is already found in the 
    * cache, it will be replaced.
    * @param item The item to cache, may not be <code>null</code>
    * @param size The reported size (in bytes) the item will use in the cache.  
    * It is up to the caller to provide an accurate value in order to properly 
    * maintain the storage limitations of the cache.  Actual storage will exceed 
    * these values by a small amount due to a small overhead incurred by storing 
    * additional metadata for each item cached.  May not be less than 
    * <code>0</code>.
    * @param type The type of item to retrieve, may not be <code>null</code> or 
    * empty.   
    * 
    * @throws IllegalArgumentException if any param is invalid.
    */
   public void addItem(Object[] keys, Object item, long size, String type)
   {
      validateKeys(keys, false);
      if (item == null)
         throw new IllegalArgumentException("item may not be null");
      if (size < 0)
         throw new IllegalArgumentException("size may not be less than 0");
      if (type == null)
         throw new IllegalArgumentException("type may not be null");
      
      Map cacheMap = getCache();
      if (cacheMap != null)
      {
         // create item and add modified listeners
         PSCacheItem cacheItem = new PSCacheItem(m_cacheId, item, keys, size);
         Iterator listeners = m_cacheModifiedListeners.iterator();
         while (listeners.hasNext())         
            cacheItem.addCacheModifiedListener(
               (IPSCacheModifiedListener)listeners.next());
         
         for (int i = 0; i < keys.length; i++)
         {
            // if on last key, need to handle the item
            if (i == keys.length -1)
            {
               synchronized(cacheMap)
               {
                  Map itemMap = (Map)cacheMap.get(keys[i]);
                  if (itemMap == null)
                  {
                     itemMap = new Hashtable();
                     cacheMap.put(keys[i], itemMap);
                  }
                  PSCacheItem oldItem = (PSCacheItem)itemMap.put(type, 
                     cacheItem);
                  if (oldItem != null)
                  {
//                   notifiy listeners of flush event and release the item
                     oldItem.release();
                     notifyModifiedListeners(PSCacheEvent.CACHE_ITEM_REMOVED, 
                        oldItem);
                  }
                 
                  // notify listeners of add event
                  notifyModifiedListeners(PSCacheEvent.CACHE_ITEM_ADDED, 
                     cacheItem);
               }
            }
            else
            {
               // need to see if we have a map for this key already
               Map nextMap;
               Object next = cacheMap.get(keys[i]);
               if (next instanceof Map)
                  // have a map, so use it
                  nextMap = (Map)next;
               else 
               {
                  // no entry at this level, so add a map for this key
                  nextMap = new Hashtable();
                  synchronized(cacheMap)
                  {
                     cacheMap.put(keys[i], nextMap);
                  }
               }   
               cacheMap = nextMap;
            }
         }
      }
   }
   
   /**
    * Retrieves an item from the cache.
    * 
    * @param keys An array of keys that will uniquely identify the item, may 
    * not be <code>null</code>, may not contain any <code>null</code> entries,
    * and must contain the number of entries specified when the cache was
    * constructed.    
    * @param type The type of item to retrieve, may not be <code>null</code> or 
    * empty.   
    * 
    * @return The object matching the keys, or <code>null</code> if the object
    * is not found in the cache. 
    * 
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSCacheException if there are any errors retrieving the item
    */
   public Object retrieveItem(Object[] keys, String type) 
      throws PSCacheException
   {
      validateKeys(keys, false);
      
      Object item = null;
      Map cacheMap = getCache();
      PSCacheItem cacheItem = null;
      if (cacheMap != null)
      {
         for (int i = 0; i < keys.length; i++)
         {
            Object o = cacheMap.get(keys[i]);
            // if we hit a null, then we're done
            if (o == null)
               break;
               
            if (i == keys.length -1)
            {
               // on last key, must be the item map
               Map itemMap = (Map)o;
               cacheItem = (PSCacheItem)itemMap.get(type);
               if (cacheItem != null)
                  item = cacheItem.getObject();
            }
            else
            {
               // get the next map
               cacheMap = (Map)o;
            }
         }
         
         // notify listeners of miss event if item is null
         if (item == null)
            notifyAccessListeners(PSCacheEvent.CACHE_ITEM_NOT_FOUND, cacheItem);
      }
      
      return item;
   }
   
   /**
    * Removes all items from the cache.
    */
   public void flush()
   {
      flush(m_flushAllKeys);
   }
   
   /**
    * Removes all items associated with the supplied keys.  
    * 
    * @param keys An array of keys that will identify the set of items to flush.
    * Any subset of the keys may be provided, but <code>null</code> values must
    * be used as placeholders for the omitted keys, and it must contain the 
    * number of entries specified when the cache was constructed.  Passing in 
    * all null keys is equiv to calling flush().
    * 
    * @throws IllegalArgumentException if <code>keys</code> is 
    * <code>null</code> or the wrong size.
    */
   public void flush(Object[] keys)
   {
      validateKeys(keys, true);
      
      Map cache = getCache();
      if (cache != null)
         flush(cache, keys, 0);
   }
   
   /**
    * Recursively walks the tree of keys and flushes based on the provided
    * keyset.
    * 
    * @param cacheMap The Map representing the current branch of the key tree.
    * Assumed not <code>null</code>.
    * @param keys The array of keys, assumed not <code>null</code> and of the
    * correct length.
    * @param level The index into the <code>keys</code> array of the key that is 
    * currently being processed.
    */
   private void flush(Map cacheMap, Object[] keys, int level)
   {
      // if passed a null key for this level, need to traverse all entries at
      // this level and flush those "branches"
      if (keys[level] == null)
      {
         // walk keyset of current map and flush each entry.  Need to convert to 
         // an array to avoid concurrent modfication issues
         Object[] keyList = cacheMap.keySet().toArray();
         for (int i = 0; i < keyList.length; i++)
         {
            flushEntry(cacheMap, keys, keyList[i], level);
         }
      }
      else
      {
         // flush the entry specified by the key
         flushEntry(cacheMap, keys, keys[level], level);
      }
   }
   
   /**
    * Used to synchronize getting the reference to the main cache object.  
    * 
    * @return The main cache object, will be <code>null</code> if cache is
    * shutting down.
    */
   private Map getCache()
   {
      Map cache = null;
      synchronized (m_cacheMonitor)
      {
         if (!m_shutdown)
            cache = m_cache;
      }
      return cache;
   }
   
   /**
    * Flushes the entry in the provided map using the specified key.  If the
    * entry is an item, it is flushed, otherwise if it is another map, it is 
    * recursively flushed using the provided keyset and level.
    * 
    * @param cacheMap The map that contains the specified entry.  Assumed not
    * <code>null</code>.
    * @param keys The keyset being used to identify the particular item to 
    * flush.
    * @param key The key identifying the specific item or "branch" to flush.
    * Assumed not <code>null</code>.
    * @param level The index into the <code>keys</code> array of the key that is 
    * currently being processed.
    */
   private void flushEntry(Map cacheMap, Object[] keys, Object key, int level) 
   {
      
      if (level == keys.length - 1)
      {
         // key identifies the item itself, flush it
         Object o;
         synchronized (cacheMap)
         {
            Map itemMap = (Map)cacheMap.remove(key);
            if (itemMap != null)
            {
               Iterator items = itemMap.values().iterator();
               while (items.hasNext())
               {
                  PSCacheItem item = (PSCacheItem)items.next();
                  item.release();
                  notifyModifiedListeners(PSCacheEvent.CACHE_ITEM_REMOVED, 
                     item);                  
               }
            }
         }
      }
      else
      {
         // flush from this branch down
         Object o = cacheMap.get(key);
         if (o instanceof Map)
         {
            Map nextMap = (Map)o;
            flush(nextMap, keys, level + 1);
         }
      }
   }
   
   /**
    * Add's the supplied listener to this objects notifier list so that it may
    * be notified each time an attempt to access an item results in a miss. 
    * 
    * @param listener The listener to notify, may not be <code>null</code>.
    * 
    * @throws IllegalArgumentException if listener is <code>null</code>.
    */
   void addCacheAccessedListener(IPSCacheAccessedListener listener)
   {
      if (listener == null)
         throw new IllegalArgumentException("listener may not be null");
         
      m_cacheAccessListeners.add(listener);
   }   
   
   /**
    * Adds the supplied listener to this object's notifier list so that it may
    * be notified each time an item is added or removed from the cache. 
    * 
    * @param listener The listener to notify, may not be <code>null</code>.
    * 
    * @throws IllegalArgumentException if listener is <code>null</code>.
    */
   void addCacheModifiedListener(IPSCacheModifiedListener listener)
   {
      if (listener == null)
         throw new IllegalArgumentException("listener may not be null");
         
      m_cacheModifiedListeners.add(listener);
      listener.setCache(this);
   }   
   
   /**
    * Removes the supplied listener from this object's notifier list. 
    * 
    * @param listener The listener to remove, may not be <code>null</code>.
    * 
    * @throws IllegalArgumentException if listener is <code>null</code>.
    */
   void removeCacheAccessedListener(IPSCacheAccessedListener listener)
   {
      if (listener == null)
         throw new IllegalArgumentException("listener may not be null");
         
      m_cacheAccessListeners.remove(listener);
   }
   
   /**
    * Removes the supplied listener from this object's notifier list. 
    * 
    * @param listener The listener to remove, may not be <code>null</code>.
    * 
    * @throws IllegalArgumentException if listener is <code>null</code>.
    */
   void removeCacheModifiedListener(IPSCacheModifiedListener listener)
   {
      if (listener == null)
         throw new IllegalArgumentException("listener may not be null");
         
      m_cacheModifiedListeners.remove(listener);
   }
   
   /**
    * Flushes the cache, and frees all resources.  This method is idempotent.
    */
   public void shutdown()
   {
      synchronized(m_cacheMonitor)
      {
         if (m_shutdown)
            return;

         m_shutdown = true;
      }
         
      //shutdown the aging manager
      removeCacheModifiedListener(m_agingManager);
      m_agingManager.shutdown();
      
      // do a full flush
      flush(m_cache, m_flushAllKeys, 0);
   }  
   
   /**
    * Determines if the keyset provided has the correct number of entries.
    * 
    * @param keys The keyset to validate. 
    * @param allowNulls <code>true</code> if <code>keys</code> may contain
    *  <code>null</code> values, <code>false</code> if not.
    * 
    * @throws IllegalArgumentException if keys is <code>null</code>, if it
    * is not the same size as specified by the value passed to the ctor, or
    * it contains <code>null</code> values and <code>allowNulls</code> is 
    * <code>false</code>.
    */
   private void validateKeys(Object[] keys, boolean allowNulls)
   {
      if (keys == null || keys.length != m_keySize)
         throw new IllegalArgumentException("invalid keys");
         
      if (!allowNulls)
      {
         for (int i = 0; i < keys.length; i++) 
         {
            if (keys[i] == null)
               throw new IllegalArgumentException("keys may not contain nulls");
         }
         
      }
   }
   
   /**
    * Notifies all <code>IPSCacheAccessedListener</code>s with the supplied 
    * action and item.
    * 
    * @param action Assumed to be one of the 
    * <code>PSCacheEvent.ACTION_xxx</code> types.
    * @param object The item involved in the event, may be <code>null</code>.
    */
   private void notifyAccessListeners(int action, Object object)
   {
      if (!m_cacheAccessListeners.isEmpty())
      {
         PSCacheEvent event = new PSCacheEvent(
            action, object);
         Iterator i = m_cacheAccessListeners.iterator();
         while (i.hasNext()) 
         {
            IPSCacheAccessedListener listener = 
               (IPSCacheAccessedListener)i.next();
            listener.cacheAccessed(event);
         }
      }
   }
   
   /**
    * Notifies all <code>IPSCacheModifiedListener</code>s with the supplied 
    * event.
    * 
    * @param action Assumed to be one of the 
    * <code>PSCacheEvent.ACTION_xxx</code> types.
    * @param object The object involved in the event, may be <code>null</code>.
    */
   private void notifyModifiedListeners(int action, Object object)
   {
      if (!m_cacheModifiedListeners.isEmpty())
      {
         PSCacheEvent event = new PSCacheEvent(
            action, object);
         Iterator i = m_cacheModifiedListeners.iterator();
         while (i.hasNext()) 
         {
            IPSCacheModifiedListener listener = 
               (IPSCacheModifiedListener)i.next();
            listener.cacheModified(event);
         }
      }
   }
   
   
   /**
    * Called to get a new cacheid to use for each new instance of this class.
    * 
    * @return The id to use.  Each call to this method will return a value that
    * is unique among all current instances of this class.
    */
   private static synchronized int createCacheId()
   {
      return ms_nextCacheId++;
   }
   
   /**
    * Contains the next cacheId to use.  Should only be modified through calls
    * to {@link #createCacheId()} to ensure uniqueness.  
    */
   private static int ms_nextCacheId = 0;
   
   /**
    * Indicates the size of the keyset arrays this cache will use.  All keyset
    * arrays passed must be this size.  Set during ctor, never modified after 
    * that.
    */
   private int m_keySize;
   
   /**
    * The id that uniquely identifies this cache.  Set during the ctor, never
    * modified after that.
    */
   private int m_cacheId;
   
   /**
    * Top level Map that holds the entire cache.  Key is the first key object
    * provided by the keyset of a stored item. If keysize is <code>1</code>,
    * then value is the <code>PSCacheItem</code> object, otherwise it is another 
    * Map whose key is the second key object, and the value is either the 
    * item or another Map depending on the keysize, etc.  Never 
    * <code>null</code>.
    */
   private Map m_cache = new Hashtable();
   
   /**
    * List of <code>IPSCacheAccessedListener</code> objects that should be
    * informed of unsuccessful attempts to retrieve an item.  Never 
    * <code>null</code>, may be empty.
    */
   private List m_cacheAccessListeners = new ArrayList();
   
   /**
    * List of <code>IPSCacheModifiedListener</code> objects that should be
    * informed when items are added to or flushed from the cache.  Never 
    * <code>null</code>, may be empty.
    */
   private List m_cacheModifiedListeners = new ArrayList();
   
   /**
    * Flag to indicate if cache is shutting down.  If <code>true</code>, cache
    * has begun shutdown, otherwise <code>false</code>.
    */
   private boolean m_shutdown = false;
   
   /**
    * Keys to use to flush all items from the map.  Will be the correct
    * length for this cache and contain all <code>null</code> values.  
    * Intialized during construction, never <code>null</code> or modified
    * after that.  
    */
   private Object[] m_flushAllKeys;
   
   /**
    * Object to use to synchronize cache access while flushing the entire
    * cache.  Never <code>null</code>.
    */
   private Object m_cacheMonitor = new Object();
   
   /**
    * Monitors cache for expired items and flushes them.  Initialized in ctor,
    * never <code>null</code> after that.
    */
   private PSCacheAgingManager m_agingManager;
}
