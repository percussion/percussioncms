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

