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

package com.percussion.server;

import java.util.Date;


/**
 * This is the base class for statistical reporting on a handler. It
 * implements timers for min/avg/max processing time, time up and
 * counters for events successful/failed/pending, cache hits/misses.
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public abstract class PSHandlerStatistics
{
   /**
    * Construct a statistics object with the specified start time
    *
    * @param   startTime      the time/date the handler was started
    */
   protected PSHandlerStatistics(Date startTime)
   {
      super();
      m_startTime = startTime;
   }

   /**
    * Construct a statistics object using the current time as
    * the time the handler was started.
    */
   protected PSHandlerStatistics()
   {
      super();
      m_startTime = new Date();
   }

   /**
    * Add the statistics for the specified request.
    *
    * @param      stats         the statistics for the processed request
    */
   public synchronized void update(PSRequestStatistics stats)
   {
      m_cacheHits       += stats.getCacheHits();
      m_cacheMisses   += stats.getCacheMisses();

      if (stats.isFailure())
         m_eventsFailed++;
      else
         m_eventsSucceeded++;

      int eventTime = stats.getProcessingTime();

      if (eventTime > m_eventTimeMax)
         m_eventTimeMax = eventTime;

      if (eventTime < m_eventTimeMin)
         m_eventTimeMin = eventTime;

      m_eventTimeTotal += eventTime;

      /* this is no longer a pending event */
      m_eventsPending--;   // DO NOT call decrementPendingEventCount();
                           // as chainded stats will go negative!!!
   }

   /**
    * Get the number of database requests which could not be handled
    * from the cache.
    *
    * @return      the number of cache hits
    */
   public int getCacheHits()
   {
      return m_cacheHits;
   }

   /**
    * Get the number of database requests which could not be handled
    * from the cache.
    *
    * @return      the number of cache misses
    */
   public int getCacheMisses()
   {
      return m_cacheMisses;
   }

   /**
    * Get the number of events (requests) processed successfully.
    *
    * @return      the number of requests
    */
   public int getSuccessfulEventCount()
   {
      return m_eventsSucceeded;
   }

   /**
    * Get the number of events (requests) that failed during processing.
    *
    * @return      the number of requests
    */
   public int getFailedEventCount()
   {
      return m_eventsFailed;
   }

   /**
    * Increment the pending event count.
    */
   public synchronized void incrementPendingEventCount()
   {
      m_eventsPending++;
   }

   /**
    * Decrement the pending event count.
    */
   public synchronized void decrementPendingEventCount()
   {
      m_eventsPending--;
   }

   /**
    * Get the number of events (requests) pending (currently being
    * processed).
    *
    * @return      the number of requests
    */
   public int getPendingEventCount()
   {
      return m_eventsPending;
   }

   /**
    * Get the maximum amount of time taken to process a request.
    *
    * @return      the amount of time in milliseconds
    */
   public int getMaximumEventTime()
   {
      return m_eventTimeMax;
   }

   /**
    * Get the minimum amount of time taken to process a request.
    *
    * @return      the amount of time in milliseconds
    */
   public int getMinimumEventTime()
   {
      return m_eventTimeMin;
   }

   /**
    * Get the average amount of time taken to process a request.
    *
    * @return      the amount of time in milliseconds
    */
   public int getAverageEventTime()
   {
      int totalEvents = m_eventsSucceeded + m_eventsFailed;

      if (totalEvents == 0)   /* don't divide by zero! */
         return 0;

      return (int)(m_eventTimeTotal / totalEvents);
   }

   /**
    * Set the time the server was shutdown.
    *
    * @param      endTime      the time/date the Server was shutdown
    */
   public void setShutdownTime(Date endTime)
   {
      m_endTime = endTime;
   }

   /**
    * Set the time the was shutdown to the current time.
    */
   public void setShutdownTime()
   {
      m_endTime = new Date();
   }

   /**
    * Get the time the server was shutdown.
    *
    * @return      the time/date the Server was shutdown
    */
   public Date getShutdownTime()
   {
      return m_endTime;
   }

   /**
    * Get the time the server was started.
    *
    * @return      the time/date the Server was started
    */
   public Date getStartTime()
   {
      return m_startTime;
   }

   /**
    * Get the amount of time, in milliseconds, the server was up.
    *
    * @return      the amount of time, in milliseconds or -1 if the 
    *               Server is still running or was never started
    */
   public long getProcessingTime()
   {
      if ((m_startTime == null) || (m_endTime == null))
         return -1;

      return m_endTime.getTime() - m_startTime.getTime();
   }


   protected java.util.Date   m_startTime         = null;
   protected java.util.Date   m_endTime         = null;
   protected int               m_cacheHits          = 0;
   protected int               m_cacheMisses      = 0;
   protected int               m_eventsSucceeded   = 0;
   protected int               m_eventsFailed      = 0;
   protected int               m_eventsPending   = 0;
   protected int               m_eventTimeMax      = Integer.MIN_VALUE;
   protected int               m_eventTimeMin      = Integer.MAX_VALUE;
   protected long               m_eventTimeTotal   = 0;
}

