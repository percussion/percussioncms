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

