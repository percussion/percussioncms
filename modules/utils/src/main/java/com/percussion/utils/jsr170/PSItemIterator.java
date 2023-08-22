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
package com.percussion.utils.jsr170;

import com.percussion.utils.collections.PSMultiMapIterator;
//TODO: Replace with commons collections 4
import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.iterators.FilterIterator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * A base abstract class used to implement the JCR iterators required for the
 * implementation. The base iterator implements a wrapper on the standard 
 * Java iterator that allows the additional common methods to be implemented.
 * 
 * @param <M>
 * 
 * @author dougrand
 */
public abstract class PSItemIterator<M> implements Iterator
{
   /**
    * Hold the current position
    */
   int m_current = 0;

   /**
    * The current "real" iterator on the values
    */
   Iterator<M> m_iter = null;
   
   /**
    * The original map that's being iterated. Needs to be kept to enable
    * this to return the size of the collection.
    */
   Map m_map = null;
   
   /**
    * The filter, may be <code>null</code> as the filter is not required
    */
   Predicate m_filter = null;
   
   /**
    * Ctor, may only be used from subclasses
    * @param things the map of things, never <code>null</code>
    * @param filterpattern the filter pattern, may be <code>null</code>
    */
   @SuppressWarnings("unchecked")
   protected PSItemIterator(Map things,
         String filterpattern) {
      if (things == null)
      {
         throw new IllegalArgumentException("things may not be null");
      }
      // If there's a filter, then filter the set up front
      if (filterpattern != null)
      {
         m_filter = new PSNamePatternFilter(filterpattern);
      }
      m_map = things;
      m_iter = calculateIter();
   }

   /**
    * Create the right iterator over the values
    * @return an iterator, never <code>null</code>
    */
   @SuppressWarnings("unchecked")
   private Iterator<M> calculateIter()
   {
      if (m_map instanceof MultiMap)
      {
         return new PSMultiMapIterator((MultiMap) m_map, m_filter);
      }
      else if (m_filter != null)
      {
         Collection<M> values = new ArrayList<M>();
         values.addAll(m_map.values());
         return new FilterIterator(values.iterator(), m_filter);
      }
      else
      {
         Collection<M> values = new ArrayList<M>();
         values.addAll(m_map.values());
         return values.iterator();
      }
   }
   
   public M next()
   {
      m_current++;
      return (M) m_iter.next();
   }
   
   /**
    * Base implementation for node and property iterators from JCR
    * 
    * @param count skip a count of entries, must be greater than zero to have
    * an effect.
    */
   public void skip(long count)
   {
      for (int i = 0; i < count; i++)
      {
         next();
      }
   }

   /**
    * Base implementation for node and property iterators from JCR.
    * 
    * @return the total count of items in the represented group. Does not affect
    * the existing iterator. 
    */
   public long getSize()
   {
      // Get a fresh iterator and exhaust it
      int count = 0;
      Iterator i = calculateIter();
      while(i.hasNext())
      {
         i.next();
         count++;
      }
      
      return count;
   }

   /**
    * Get the current position.
    * 
    * @return the current position in the group held by the iterator.
    */
   public long getPosition()
   {
      return m_current;
   }

   public boolean hasNext()
   {
      return m_iter.hasNext();
   }

   public void remove()
   {
      throw new RuntimeException("Cannot remove properties");
   }
   
   /**
    * Method made available to allow access to the underlying structure
    * being iterated. This is for internal use of the assembly engine only.
    * <b>Never modify this map! Always use the set method with a copy!</b>
    * 
    * @return the map, never <code>null</code>
    */
   public Map getMap()
   {
      return m_map;
   }
   
   /**
    * Method made available to allow replacement to the underlying structure
    * being iterated. This is for internal use of the assembly engine only.
    * <b>You must copy the map from {@link #getMap()}!</b>
    * @param newmap
    */
   public void setMap(Map newmap)
   {
      m_map = newmap;
      m_iter = calculateIter();
   }

}
