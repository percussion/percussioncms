/*******************************************************************************
 *  [ PSRecurrenceIterator ]
 * 
 * COPYRIGHT (c) 1999 - 2004 by Percussion Software, Inc., Woburn, MA USA. All
 * rights reserved. This material contains unpublished, copyrighted work
 * including confidential and proprietary information of Percussion.
 *  
 ******************************************************************************/
package com.percussion.fastforward.calendar;

import java.util.Iterator;

/**
 * Extension of {@link java.util.Iterator iterator} to specify the enent object 
 * and recurrence value.
 */
public class PSRecurrenceIterator implements Iterator
{

   /**
    * Constructor. Takes the recusrring even object.
    * @param event ebent may be <code>null</code>.
    */
   public PSRecurrenceIterator(PSRecurringEvent event)
   {
      m_event = event;
      m_recurrence = 1;
   }

   /* (non-Javadoc)
    * @see java.util.Iterator#hasNext()
    */
   public boolean hasNext()
   {
      return m_event.getRecurrence(m_recurrence) != null;
   }

   /* (non-Javadoc)
    * @see java.util.Iterator#next()
    */
   public Object next()
   {
      return m_event.getRecurrence(m_recurrence++);
   }

   /* (non-Javadoc)
    * @see java.util.Iterator#remove()
    */
   public void remove()
   {
      throw new UnsupportedOperationException();
   }

   /**
    * Recurring even object, initialized in the ctor, may be <code>null</code>.
    */
   private PSRecurringEvent m_event;

   /**
    * Recurrence of the event, initialized to 1 in the ctor and does not change
    * currently.
    */
   private int m_recurrence;
}