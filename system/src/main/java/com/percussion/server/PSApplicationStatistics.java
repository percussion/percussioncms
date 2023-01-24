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
 * This class is used to store the statistics for an application running
 * on the server.
 *
 * @see         com.percussion.server.PSApplicationHandler#getStatistics
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSApplicationStatistics extends PSHandlerStatistics
{
   /**
    * Construct an application statistics object with the specified
    *   application start time.
    *
    * @param   startTime      the time/date the application started
    */
   public PSApplicationStatistics(Date startTime)
   {
      super(startTime);
      m_ServerStatistics = PSServer.getStatistics();
   }

   /**
    * Construct an application statistics object using the current time as
    * the time the application was started.
    */
   public PSApplicationStatistics()
   {
      this(new Date());
   }

   /**
    * Add the statistics for the specified request.
    *
    * @param      stats         the statistics for the processed request
    */
   public synchronized void update(PSRequestStatistics stats)
   {
      super.update(stats);

      // now pass this on to the server to do it's reporting
      m_ServerStatistics.update(stats);
   }

   /**
    * Increment the pending event count.
    */
   public synchronized void incrementPendingEventCount()
   {
      super.incrementPendingEventCount();
      m_ServerStatistics.incrementPendingEventCount();   // update server stats
   }

   /**
    * Decrement the pending event count.
    */
   public synchronized void decrementPendingEventCount()
   {
      super.decrementPendingEventCount();
      m_ServerStatistics.decrementPendingEventCount();   // update server stats
   }

   // keep the server stats object so we can quickly refresh it as well
   private   PSServerStatistics   m_ServerStatistics;
}

