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
package com.percussion.services.publisher.impl;

import com.google.common.collect.AbstractIterator;

import java.util.Iterator;

/**
 * 
 * Chains iterators lazily by calling {@link #nextIterator()}.
 * This allows you to flatten an iterator of iterators or 
 * iterator of collections.
 * @author adamgent
 *
 * @param <T>
 */
public abstract class PSIteratorChain<T> extends AbstractIterator<T>
{

   private Iterator<T> currentIterator;
   
   /**
    * Implement to determine the next iterator. This method will be called on demand.
    * @return <code>null</code> indicates that there are no more iterators to chain.
    */
   protected abstract Iterator<T> nextIterator();
   @Override
   protected T computeNext()
   {
      if (currentIterator == null) 
         currentIterator = nextIterator();
      while (currentIterator != null && ! currentIterator.hasNext() ) {
         currentIterator = nextIterator();
      }
      if (currentIterator == null)
         return endOfData();
      return currentIterator.next();
   }
   
}
