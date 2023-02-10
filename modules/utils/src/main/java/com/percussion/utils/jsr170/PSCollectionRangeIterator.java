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

import javax.jcr.RangeIterator;
import java.util.Collection;
import java.util.Iterator;

/**
 * A concrete implementation of the JSR-170 range iterator using an abstract
 * collection of T
 * @param <T> the class of the data in the collection
 * @author dougrand
 */
public class PSCollectionRangeIterator<T> implements RangeIterator
{
   /**
    * The collection to iterate over
    */
   private Collection<T> m_collection;
   
   /**
    * The internal iterator for the collection, never <code>null</code>
    */
   private Iterator<T> m_iter;
   
   /**
    * The current position in the collection
    */
   private long m_position = 0;

   /**
    * Ctor
    * @param collection the collection to iterate over, never <code>null</code>
    */
   public PSCollectionRangeIterator(Collection<T> collection)
   {
      if (collection == null)
      {
         throw new IllegalArgumentException("collection may not be null");
      }
      m_collection = collection;
      m_iter = m_collection.iterator();
   }
   
   public boolean hasNext()
   {
      return m_iter.hasNext();
   }

   public T next()
   {
      m_position++;
      return m_iter.next();
   }

   public void remove()
   {
      m_iter.remove();
   }

   public void skip(long count)
   {
      if (count < 0)
      {
         throw new IllegalArgumentException("Count must not be negative");
      }
      while(count > 0)
      {
         next();
         count--;
      }
   }

   public long getSize()
   {
      return m_collection.size();
   }

   public long getPosition()
   {
      return m_position;
   }

}
