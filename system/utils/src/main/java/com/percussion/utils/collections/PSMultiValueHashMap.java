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
package com.percussion.utils.collections;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This {@link java.util.HashMap} may contain more than one Entry for
 * each Key.<br>
 * It's internally backed by a
 * {@link java.util.concurrent.ConcurrentHashMap} that holds the
 * values as a list of the specified value-type and therefore may save
 * multiple values for the same key.
 * 
 * @param <K>
 *            Type of the keys for this {@link java.util.HashMap}
 * @param <V>
 *            Type of the values for this {@link java.util.HashMap}
 * 
 */
public class PSMultiValueHashMap<K, V> {

   /**
    * HashMap with a list of values
    */
   private ConcurrentHashMap<K, List<V>> m_map;

   /**
    * Construct a new empty PSMultiValueHashMap.
    * 
    * @see java.util.concurrent.ConcurrentHashMap#ConcurrentHashMap()
    */
   public PSMultiValueHashMap() {
      m_map = new ConcurrentHashMap<K, List<V>>( );
   }

   /**
    * Construct a new empty PSMultiValueHashMap with the given initial
    * capacity.
    * 
    * @param initialCapacity
    *            the initial capacity. The implementation performs
    *            internal sizing to accommodate this many elements.
    * @see java.util.concurrent.ConcurrentHashMap#ConcurrentHashMap(int)
    */
   public PSMultiValueHashMap(int initialCapacity) {
      m_map = new ConcurrentHashMap<K, List<V>>(initialCapacity);
   }

   /**
    * Construct a new empty PSMultiValueHashMap with the given initial
    * capacity.
    * 
    * @param initialCapacity
    *            the initial capacity. The implementation performs
    *            internal sizing to accommodate this many elements.
    * @param loadFactor
    *            the load factor threshold, used to control resizing.
    *            Resizing may be performed when the average number of
    *            elements per bin exceeds this threshold.
    * @param concurrencyLevel
    *            the estimated number of concurrently updating
    *            threads. The implementation performs internal sizing
    *            to try to accommodate this many threads.
    * @see java.util.concurrent.ConcurrentHashMap#ConcurrentHashMap(int,
    *      float, int)
    */
   public PSMultiValueHashMap(int initialCapacity, float loadFactor,
         int concurrencyLevel) {
      m_map = new ConcurrentHashMap<K, List<V>>(initialCapacity,
            loadFactor,
            concurrencyLevel);
   }

   /**
    * Construct a PSMultiValueHashMap filling it with the given map.
    * 
    * @param map
    *            the map which provides the data for the new map
    * @see java.util.concurrent.ConcurrentHashMap#ConcurrentHashMap(java.util.Map)
    */
   public PSMultiValueHashMap(Map<? extends K, ? extends List<V>> map) {
      this.m_map = new ConcurrentHashMap<K, List<V>>(map);
   }

   /**
    * Returns true if this map contains no key-value mappings. This
    * implementation returns size() == 0.
    * 
    * @return true if this map contains no key-value mappings.
    */
   public boolean isEmpty() {
      return m_map.isEmpty( );
   }

   /**
    * Legacy method testing if some key maps into the specified value
    * in this table. This method is identical in functionality to
    * containsValue(java.lang.Object), and exists solely to ensure
    * full compatibility with class ConcurrentHashMap, which supported this
    * method prior to introduction of the Java Collections framework.
    * 
    * @param key
    *            a key to search for.
    * @return true if and only if some key maps to the value argument
    *         in this table as determined by the equals method; false
    *         otherwise.
    */
   public boolean contains(K key) {
      return m_map.contains(key);
   }

   /**
    * Checks if the map contains the given value for the given key.
    * 
    * @param key
    *            key of the mapping
    * @param value
    *            value whose presence in this map at the given key is
    *            to be tested.
    * @return <code>true</code> if this map contains a value for
    *         the given key
    */
   public boolean containsValue(K key, V value) {
      if (m_map.containsKey(key)) {
         return m_map.get(key).contains(value);
      }
      return false;
   }

   /**
    * Returns an iterator of the values in this table.
    * 
    * @return an iterator of the values in this table.
    */
   public Iterator<List<V>> elements() {
      return m_map.values( ).iterator( );
   }

   /**
    * Returns an iterator of the keys in this table.
    * 
    * @return an iterator of the keys in this table.
    */
   public Iterator<K> keys() {
      return m_map.keySet( ).iterator( );
   }

   /**
    * Returns a set view of the keys contained in this map. The set
    * is backed by the map, so changes to the map are reflected in
    * the set, and vice-versa. The set supports element removal,
    * which removes the corresponding mapping from this map, via the
    * Iterator.remove, Set.remove, removeAll, retainAll, and clear
    * operations. It does not support the add or addAll operations.
    * The view's returned iterator is a "weakly consistent" iterator
    * that will never throw ConcurrentModificationException, and
    * guarantees to traverse elements as they existed upon
    * construction of the iterator, and may (but is not guaranteed
    * to) reflect any modifications subsequent to construction.
    * 
    * @return a set view of the keys contained in this map.
    */
   public Set<K> keySet() {
      return m_map.keySet( );
   }

   /**
    * Copies all of the mappings from the specified map to this one.
    * These mappings replace any mappings that this map had for any
    * of the keys currently in the specified map.
    * 
    * @param map
    *            Mappings to be stored in this map.
    */
   public void putAll(@SuppressWarnings("hiding")
   Map<? extends K, ? extends List<V>> map) {
      this.m_map.putAll(map);
   }

   /**
    * If the specified key is not already associated with a value,
    * associate it with the given value. This is equivalent to<br>
    * <code>
    * if (!map.containsKey(key))
    *  return map.put(key, value);
    * else
    *  return map.get(key);</code>
    * Except that the action is performed atomically.
    * 
    * @param key
    *            key with which the specified value is to be
    *            associated.
    * @param values
    *            value to be associated with the specified key.
    * @return previous value associated with specified key, or null
    *         if there was no mapping for key.
    */
   public List<V> putIfAbsent(K key, List<V> values) {
      return m_map.putIfAbsent(key, values);
   }

   /**
    * Removes the value from the key.
    * 
    * @param key
    *            the key that needs to be removed.
    * @param value
    *            the value to be removed
    * @return <code>true</code> if the value has been removed
    */
   public boolean remove(K key, V value) {
      if (value == null) {
         return true;
      }
      if (m_map.containsKey(key)) {
         return m_map.get(key).remove(value);
      }
      return false;
   }

   /**
    * Removes the value from any key.
    * 
    * @param value
    *            the value to be removed
    * @return true if the value has been removed
    */
   public boolean removeValue(V value) {
      for (List<V> keyValues : m_map.values( )) {
         keyValues.remove(value);
      }
      return true;
   }

   /**
    * Returns the number of key-value mappings in this map. If the
    * map contains more than {@link java.lang.Integer#MAX_VALUE}
    * elements, returns {@link java.lang.Integer#MAX_VALUE}. This
    * implementation returns entrySet().size().
    * 
    * @return the number of key-value mappings in this map.
    */
   public int size() {
      return m_map.size( );
   }

   /**
    * Adds the key - value mapping to this map.<br>
    * The value is added to the list of values associated with this
    * key even if it does already exist.
    * 
    * @param key
    *            key of this mapping
    * @param value
    *            value of this mapping
    */
   public void put(K key, V value) {
      if (value != null) {
         List<V> values = m_map.get(key);
         if (values == null) {
            values = new ArrayList<V>( );
         }
         values.add(value);
         m_map.put(key, values);
      }
   }

   /**
    * Gets the values for this key.
    * 
    * @param key
    *            key of the mapping
    * @return unmodifiable list of values. If the key was not in the
    *         map the list is empty
    */
   public List<V> get(K key) {
      if (key != null) {
         List<V> ret = m_map.get(key);
         if (ret != null) {
            return Collections.unmodifiableList(ret);
         }
      }
      return Collections.emptyList( );
   }

   /**
    * Returns a collection view of the mappings contained in this
    * map. Each element in the returned collection is a Map.Entry.
    * The collection is backed by the map, so changes to the map are
    * reflected in the collection, and vice-versa. The collection
    * supports element removal, which removes the corresponding
    * mapping from the map, via the Iterator.remove,
    * Collection.remove, removeAll, retainAll, and clear operations.
    * It does not support the add or addAll operations. The view's
    * returned iterator is a "weakly consistent" iterator that will
    * never throw ConcurrentModificationException, and guarantees to
    * traverse elements as they existed upon construction of the
    * iterator, and may (but is not guaranteed to) reflect any
    * modifications subsequent to construction.
    * 
    * @return a collection view of the mappings contained in this
    *         map.
    */
   public Set<java.util.Map.Entry<K, List<V>>> entrySet() {
      return m_map.entrySet( );
   }

   /**
    * Checks if the given key is contained in this map.
    * 
    * @param key
    *            the key to check
    * @return <code>true</code> if the key is contained in this map
    */
   public boolean containsKey(K key) {
      return m_map.containsKey(key);
   }

   /**
    * Removes the mapping for this key from this map if present.
    * 
    * @param key
    *            key whose mapping is to be removed from the map.
    * @return previous value associated with specified key, or an
    *         empty list if there was no mapping for key.
    */
   public List<V> removeKey(K key) {
      List<V> removed = m_map.remove(key);
      if (removed != null) {
         return removed;
      }
      return Collections.emptyList( );
   }

   /**
    * Removes all mappings from this map.
    */
   public void clear() {
      m_map.clear( );
   }

   /**
    * Returns a collection view of the values contained in this map.
    * The collection is backed by the map, so changes to the map are
    * reflected in the collection, and vice-versa. The collection
    * supports element removal, which removes the corresponding
    * mapping from this map, via the Iterator.remove,
    * Collection.remove, removeAll, retainAll, and clear operations.
    * It does not support the add or addAll operations. The view's
    * returned iterator is a "weakly consistent" iterator that will
    * never throw ConcurrentModificationException, and guarantees to
    * traverse elements as they existed upon construction of the
    * iterator, and may (but is not guaranteed to) reflect any
    * modifications subsequent to construction.
    * 
    * @return a collection view of the values contained in this map.
    */
   public List<V> values() {
      List<V> v = new ArrayList<V>( );
      for (List<V> keyValues : m_map.values( )) {
         v.addAll(keyValues);
      }
      return Collections.unmodifiableList(v);
   }
}
