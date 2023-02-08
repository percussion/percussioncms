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
