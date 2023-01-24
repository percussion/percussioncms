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
