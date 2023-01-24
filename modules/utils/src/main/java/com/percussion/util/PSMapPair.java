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

/**
 * A simple implementation of the Map.Entry class which is not available
 * publicly. It forms an immutable pairing of 2 objects, one of which may
 * be <code>null</code>. Useful for storing param/value pairings.
 */
public class PSMapPair
{
   /**
    * The only constructor.
    *
    * @param key A non-<code>null</code> object.
    *
    * @param value The other half of the pair. May be <code>null</code>.
    *
    * @throws IllegalArgumentException if key is <code>null</code>.
    */
   public PSMapPair( Object key, Object value )
   {
      if ( null == key )
         throw new IllegalArgumentException( "key cannot be null" );
      m_key = key;
      m_value = value;
   }


   /**
    * Accessor for left half of pair.
    *
    * @return the key set in the constructor. Never <code>null</code>.
    */
   public Object getKey()
   {
      return m_key;
   }


   /**
    * Accessor for right half of pair.
    *
    * @return The value set in the constructor. May be <code>null</code>.
    */
   public Object getValue()
   {
      return m_value;
   }


   /**
    * The key part of the pairing. Never <code>null</code> after construction.
    */
   private Object m_key;

   /**
    * The value part of the pairing. May be <code>null</code>.
    */
   private Object m_value;
}

