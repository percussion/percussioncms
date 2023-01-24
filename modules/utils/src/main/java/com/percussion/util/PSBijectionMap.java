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
package com.percussion.util;

import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

/**
 *   A one-to-one object map (or bijection) backed by two ConcurrentHashMaps
 *   for time efficiency purposes. Like all hash tables, there is at most
 *   one value for a given key. However, unlike hash tables, this class
 *   guarantees that there is at most one key for a given value.
 */
public class PSBijectionMap
{
   public PSBijectionMap(int initialCapacity)
   {
      m_values = new ConcurrentHashMap(initialCapacity);
      m_keys = new ConcurrentHashMap(initialCapacity);
   }

   public void put(Object key, Object value)
   {
      m_values.put(key, value);
      m_keys.put(value, key);
   }

   /**
    *   Given a key, removes both the key and its value from the
    *   bijection. If the key is not paired with a value, it does
    *   nothing.
    *
    *   @return   The previous value associated with this key, or
    *   <CODE>null</CODE> if this key was not paired with a value.
    */
   public Object removePairingWithKey(Object key)
   {
      Object value = m_values.get(key);
      if (value != null)
      {
         m_keys.remove(value);
         m_values.remove(key);
      }
      return value;
   }

   /**
    *   Given a value, removes both the value and its key from the
    *   bijection. If the value is not paired with a key, it does
    *   nothing.
    *
    *   @return The previous key associated with this value, or
    *   <CODE>null</CODE> if this value was not paired with a key.
    */
   public Object removePairingWithValue(Object value)
   {
      Object key = m_keys.get(value);
      if (key != null)
      {
         m_values.remove(key);
         m_keys.remove(value);
      }
      return key;
   }

   /**
    *   Gets the key paired with this value.
    *   @return   the key paired with this value, or <CODE>null</CODE>
    *   if the value is not paired with a key.
    */
   public Object getKey(Object value)
   {
      return m_keys.get(value);
   }
   
   /**
    *   Gets the value paired with this key.
    *   @return   the value paired with this key, or <CODE>null</CODE>
    *   if the key is not paired with a value.
    */
   public Object getValue(Object key)
   {
      return m_values.get(key);
   }

   /**
    *   Returns an enumeration of all the values in the bijection map
    */
   public Enumeration values()
   {
      return m_values.elements();
   }

   /**
    *   Returns an enumeration of all the keys in the bijection map
    */
   public Enumeration keys()
   {
      return m_keys.elements();
   }

   private ConcurrentHashMap m_values;
   private ConcurrentHashMap m_keys;
}
