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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * PSIteratorUtils is a factory for many kinds of Iterators, including
 * Iterators over a single object, Iterators over two objects, empty
 * Iterators, and counted Iterators over a single object.
 */
public abstract class PSIteratorUtils
{
   /**
    * Returns an iterator over this object.
    */
   public static Iterator iterator(Object o)
   {
      return iterator(o, 1);
   }

   /** 
    * Returns an iterator over this object N times.
    */
   public static Iterator iterator(Object o, int numIterations)
   {
      return new CountedIterator(o, numIterations);
   }

   /**
    * Returns an empty iterator.
    */
   public static Iterator emptyIterator()
   {
      return new CountedIterator(null, 0);
   }

   /**
    * Returns a double iterator over the two given objects.
    */
   public static Iterator iterator(Object a, Object b)
   {
      return new DoubleIterator(a, b);
   }

   /**
    * Returns a protected (non-destructive) version of the current iterator.
    *
    * @param i The iterator to wrap.  May not be <code>null</code>.
    *
    * @throws IllegalArgumentException If i is <code>null</code>
    */
   public static Iterator protectedIterator(Iterator i)
   {
      if (i == null)
         throw new IllegalArgumentException("Iterator must be supplied.");

      return new ProtectedIterator(i);
   }

   /**
    * Returns an iterator over the given array.
    */
   public static Iterator iterator(Object[] array)
   {
      return new ArrayIterator(array);
   }

   public static Iterator joinedIterator(Iterator first, Iterator second)
   {
      return new JoinedIterator(first, second);
   }
   
   /**
    * Gets the list from an iterator.
    * 
    * @param iter the iter, may not be <code>null</code>
    * 
    * @return the list, never <code>null</code>, may be empty if supplied <code>
    * iter</code> is empty.
    */
   public static List cloneList(Iterator iter)
   {
      if(iter == null)
         throw new IllegalArgumentException("iter may not be null.");
         
      List entries = new ArrayList();
      while(iter.hasNext())
         entries.add(iter.next());
         
      return entries;
   }

   private static class CountedIterator implements Iterator
   {
      /**
       * Constructs a counted iterator over a single object
       * N times.
       */
      CountedIterator(Object o, int iterations)
      {
         if (iterations < 0)
            throw new IllegalArgumentException("iterations must be >= 0");

         m_ob = o;
         m_remainingIterations = iterations;
      }

      public boolean hasNext()
      {
         return (m_remainingIterations > 0);
      }

      public Object next()
      {
         if (!hasNext())
         {
            throw new NoSuchElementException();
         }

         m_remainingIterations--;
         
         // aid the garbage collector
         Object ret = m_ob;
         if (!hasNext())
         {
            m_ob = null;
         }

         return ret;
      }

      @Override
      public void remove()
      {
         throw new UnsupportedOperationException();
      }

      /** The number of iterations supported */
      private int m_remainingIterations;

      /** The object over which we iterate */
      private Object m_ob;
   }

   /**
    * An iterator over two objects.
    */
   private static class DoubleIterator implements Iterator
   {
      DoubleIterator(Object a, Object b) { m_a = a; m_b = b; m_next = 0; }
      
      public boolean hasNext() { return (m_next != 2); }

      @Override
      public void remove() { throw new UnsupportedOperationException(); }
      
      public Object next()
      {
         if (!hasNext())
            throw new NoSuchElementException();

         Object o;
         if (m_next == 0)
         {
            o = m_a;
            m_a = null;
         }
         else
         {
            o = m_b;
            m_b = null;
         }

         m_next++;
         return o;
      }

      private byte m_next;
      private Object m_a;
      private Object m_b;
   }

   private static class ArrayIterator implements Iterator
   {
      ArrayIterator(Object[] array)
      {
         if (array == null)
            m_arr = new Object[0];
         else
            m_arr = array;

         m_len = m_arr.length;

         m_idx = 0;
      }

      public boolean hasNext() { return (m_idx < m_len); }

      @Override
      public void remove() { throw new UnsupportedOperationException(); }
      
      public Object next()
      {
         if (!hasNext())
            throw new NoSuchElementException();

         return m_arr[m_idx++];
      }

      private Object[] m_arr;
      private int m_idx;
      private int m_len;
   }

   private static class JoinedIterator implements Iterator
   {
      JoinedIterator(Iterator first, Iterator second)
      {
         m_first = first;
         m_second = second;
      }

      public boolean hasNext()
      {
         if (m_first != null)
         {
            return (m_first.hasNext() || m_second.hasNext());
         }
         else if (m_second != null)
         {
            return m_second.hasNext();
         }
         return false;
      }
      @Override
      public void remove() { throw new UnsupportedOperationException(); }
      
      public Object next()
      {
         if (m_first != null)
         {
            if (m_first.hasNext())
               return m_first.next();
            else
               m_first = null;
         }

         if (m_second != null)
         {
            if (m_second.hasNext())
               return m_second.next();
            else
               m_second = null;
         }

         throw new NoSuchElementException();
      }

      private Iterator m_first;
      private Iterator m_second;
   }

   /**
    * This class wraps an iterator and does not allow remove() to be 
    * called.
    */
   private static class ProtectedIterator
      implements Iterator
   {
      /**
       * Create a protected iterator by wrapping the specified iterator
       * and delegating all non-destructive calls to that iterator.
       *
       * @param i The iterator to wrap, may not be <code>null</code>.
       * 
       * @throws IllegalArgumentException If i is <code>null</code>.
       */
      public ProtectedIterator(Iterator i)
      {
         if (i == null)
           throw new IllegalArgumentException("Iterator must be supplied");

         m_iterator = i;
      }
      
      /**
       * Delegate <code>next()</code> to the wrapped iterator.
       */
      public Object next()
      {
         return m_iterator.next();
      }
      
      /**
       * Delegate <code>hasNext()</code> to the wrapped iterator.
       */
      public boolean hasNext()
      {
         return m_iterator.hasNext();
      }
      
      /**
       *
       * @throws UnsupportedOperationException always.
       */
      @Override
      public void remove()
      {
         throw new UnsupportedOperationException(
            "This operation is not supported.");
      }
      
      /**
       * The iterator to proxy.  Initialized in constructor,
       * never <code>null</code> thereafter.
       */
      Iterator m_iterator = null;
   }
}
