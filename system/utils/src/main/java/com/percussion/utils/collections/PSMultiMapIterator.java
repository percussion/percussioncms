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
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.Predicate;

/**
 * Iterator for multi maps that supports result filtering. Remove is not
 * supported by this class.
 * 
 * @author dougrand
 *
 * @param <M>
 */
public class PSMultiMapIterator<M> implements Iterator
{
   /**
    * The current element iterator. Initialized and updated in the next method.
    */
   private Iterator m_iter = null;
   
   /**
    * The source key iterator. Initialized in the ctor and updated in the next
    * method.
    */
   private Iterator m_keyIter = null;
   
   /**
    * The source map, initialized in the ctor and never modified.
    */
   private MultiMap m_sourceMap = null;
   
   /**
    * The current next value, <code>null</code> initially, updated
    * in the {@link #findNext()} and {@link #next()} methods
    */
   private M m_next = null;
   
   /**
    * If specified, this predicate limits the values returned by the
    * iterator to those whose keys match the predicate. Initialized in the
    * ctor and never updated. May be <code>null</code>.
    */
   private Predicate m_filterPredicate = null;
   
   /**
    * Ctor 
    * @param map the map, never <code>null</code>
    * @param filter filter predicate, may be <code>null</code> if no filtering
    * is desired
    */
   public PSMultiMapIterator(MultiMap map, Predicate filter)
   {
      m_sourceMap = map;
      m_keyIter = map.keySet().iterator();
      m_filterPredicate = filter;
      m_next = null;
   }
   
   public boolean hasNext()
   {
      if (m_next == null)
      {
         m_next = findNext();
      }
      return m_next != null;
   }

   /**
    * Find the next element, or the next one that passes the predicate. Returns
    * <code>null</code> if there are no more elements to examine.
    * @return the next element or <code>null</code> if there are no more 
    * elements
    */
   @SuppressWarnings("unchecked")
   private M findNext()
   {
      while(true)
      {
         if (m_iter != null && m_iter.hasNext())
         {
            return (M) m_iter.next();            
         }
         else
         {
            if (m_keyIter.hasNext())
            {
               Object nextKey = m_keyIter.next();
               if (m_filterPredicate != null)
               {
                  if (m_filterPredicate.evaluate(nextKey))
                  {
                     // OK, we'll use this one
                  }
                  else
                  {
                     continue; // Skip to next
                  }
               }
               Collection values = (Collection) m_sourceMap.get(nextKey);
               // Copy values to a list avoid mutator issues
               Collection ivalues = new ArrayList();
               ivalues.addAll(values);
               m_iter = ivalues.iterator();
            }
            else
            {
               return null;
            }
         }
      }
   }

   public M next()
   {
      if (m_next == null)
      {
         m_next = findNext();
      }
      M rval = m_next;
      m_next = null;
      return rval;
   }

   public void remove()
   {
      throw new UnsupportedOperationException("Remove not supported");
   }

}
