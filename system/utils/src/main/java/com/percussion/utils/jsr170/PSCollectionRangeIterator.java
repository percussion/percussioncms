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
package com.percussion.utils.jsr170;

import java.util.Collection;
import java.util.Iterator;

import javax.jcr.RangeIterator;

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
