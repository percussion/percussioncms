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
 *   PSDoubleList is meant to mimic the JDK 1.2 LinkedList class as closely
 *   as possible, but can be used without using JDK 1.2.
 *   In the future, we may want to replace use of this class with use of
 *   the JDK 1.2 LinkedList class (or another Collection). This should be
 *   fairly easy because we implement a proper subset of the Collection
 *   interface.
 */
public class PSDoubleList
{
   /**
    *   Constructs an empty list.
    */
   public PSDoubleList()
   {
      m_head = null;
      m_tail = null;
      m_count = 0;
   }

   /**
    *   Appends element to the m_tail of the list.
    *   @param   value   must not be null
    */
   public void add(Object value)
   {
      addLast(value);
   }

   /**
    *   Inserts the given value at the m_head of the list.
    *   @param   value   must not be null
    */
   public void addFirst(Object value)
   {
      m_head = new PSDoubleListNode(value, m_head, null);
      if (m_tail == null) m_tail = m_head;
      m_count++;
   }

   /**
    *   Appends element to the m_tail of the list.
    *   @param   value   must not be null
    */
   public void addLast(Object value)
   {
      m_tail = new PSDoubleListNode(value, null, m_tail);
      if (m_head == null) m_head = m_tail;
      m_count++;
   }

   /**
    *   Removes and returns the first element from this list.
    *   @return   The first element from the list, or null if the list is empty
    */
   public Object removeFirst()
   {
      if (isEmpty())
         return null;
      PSDoubleListNode temp = m_head;
      m_head = m_head.next();
      if (m_head != null)
      {
         m_head.setPrevious(null);
      }
      else
      {
         m_tail = null; // remove final value
      }
      temp.setNext(null);
      m_count--;
      return temp.value();
   }

   /**
    *   Removes and returns the last element from this list.
    *   @return   The last element from the list, or null if the list is empty
    */
   public Object removeLast()
   {
      if (isEmpty())
         return null;
      PSDoubleListNode temp = m_tail;
      m_tail = m_tail.previous();
      if (m_tail == null)
      {
         m_head = null;
      }
      else
      {
         m_tail.setNext(null);
      }
      m_count--;
      return temp.value();
   }

   /**
    *   Returns true if this list contains the specified element.
    *   More formally, returns true if and only if this list contains at least one element
    *   e such that (o==null ? e==null : o.equals(e)).
    *   @param   value   must not be null
    *   @return   true if the list contains the specified element, false otherwise
    */
   public boolean contains(Object value)
   {
      PSDoubleListNode temp = m_head;
      while ((temp != null) && (!temp.value().equals(value)))
      {
         temp = temp.next();
      }
      return temp != null;
   }

   /**
    *   Removes and the first occurrence of the specified element in this list. If the list does not
    *   contain the element, it is unchanged. More formally, removes the element with the lowest index
    *   i such that (o==null ? get(i)==null : o.equals(get(i))) (if such an element exists). If the list
    *   is empty, nothing will happen.
    *   @param   value   must not be null
    *   @return   true if the list contained the specified element
    */
   public boolean remove(Object value)
   {
      PSDoubleListNode temp = m_head;
      while (temp != null && !temp.value().equals(value))
      {
         temp = temp.next();
      }
      if (temp != null)
      {
         // fix next field of element above
         if (temp.previous() != null)
         {
            temp.previous().setNext(temp.next());
         }
         else
         {
            m_head = temp.next();
         }
         // fix previous field of element below
         if (temp.next() != null)
         {
            temp.next().setPrevious(temp.previous());
         }
         else
         {
            m_tail = temp.previous();
         }
         m_count--;         // fewer elements
         return true;
      }
      return false;
   }

   /**
    *   Returns the number of elements in this list.
    *   @return   the number of elements in this list
    */
   public int size()
   {
      return m_count;
   }

   /**
    *   Returns true if and only if this list contains no elements.
    *   @return   true if the list contains no elements, false otherwise
    */
   public boolean isEmpty()
   {
      return (size() == 0);
   }

   /**
    *   Removes all of the elements from this list. It is not an error to clear an empty list.
    */
   public void clear()
   {
      // this is all that should be needed by garbage collection, because by setting both
      // m_head and m_tail to null, we orphan every element.
      m_head = m_tail = null;
      m_count = 0;
   }

   /**
    *   Copy all the elements in this list to an array.
    *   @return   Object array containing all the list members
    */
   public Object[] toArray()
   {
      if (isEmpty())
         return null;

      int size = size();
      Object[] ret = new Object[size];

      PSDoubleListNode temp = m_head;
      for (int i = 0; i < size; i++)
      {
         ret[i] = temp.value();
         temp = temp.next();
      }
      return ret;
   }


   private PSDoubleListNode m_head = null;
   private PSDoubleListNode m_tail = null;
   private int m_count = 0;

   private class PSDoubleListNode
   {
   /**
   *   Constructs a new node between the given next and previous nodes.
   *   Next's previous and previous's next are fixed to point to this node.
      */
      public PSDoubleListNode(Object v,
         PSDoubleListNode next,
         PSDoubleListNode previous)
      {
         m_data = v;
         m_next = next;
         if (m_next != null)
            m_next.m_previous = this;
         m_previous = previous;
         if (m_previous != null)
            m_previous.m_next = this;
      }
      
      /**
      *   Constructs an ungrounded node with the given data
      */
      public PSDoubleListNode(Object v)
      {
         this(v,null,null);
      }
      
      /**
      *   Returns the node that follows this node in the list.
      *   @return   the node that follows this node in the list, or null if this
      *   the tail node or if this node is ungrounded
      */
      public PSDoubleListNode next()
      {
         return m_next;
      }
      
      /**
      *   Returns the node that precedes this node in the list.
      *   @return   the node that precedes this node in the list, or null if this
      *   the head node or if this node is ungrounded
      */
      public PSDoubleListNode previous()
      {
         return m_previous;
      }
      
      /**
      *   Returns the value stored in this node.
      *   @return   the value stored in this node
      */
      public Object value()
      {
         return m_data;
      }
      
      /**
      *   Sets the next node.
      *   @param   next   the next node
      */
      public void setNext(PSDoubleListNode next)
      {
         m_next = next;
      }
      
      /**
      *   Sets the previous node.
      *   @param   previous   the previous node
      */
      public void setPrevious(PSDoubleListNode previous)
      {
         m_previous = previous;
      }
      
      /**
      *   Sets the value of the data.
      *   @param   value
      */
      public void setValue(Object value)
         // post: sets a new value for this object
      {
         m_data = value;
      }
      
      private Object m_data;
      private PSDoubleListNode m_next;
      private PSDoubleListNode m_previous;
   }
}
