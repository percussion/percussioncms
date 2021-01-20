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

package com.percussion.log;

import java.util.Map;

/**
 * The PSLogApplicationStatistics class is used to log the application
 * statistics when an application is shut down.
 * <p>
 * The following information is logged:
 * <ul>
 * <li>the amount of time the application was up for</li>
 * <li>the number of events processed</li>
 * <li>the number of events failed</li>
 * <li>the number of events left pending</li>
 * <li>the number of query cache hits</li>
 * <li>the number of query cache misses</li>
 * <li>the minimum amount of time to process an event</li>
 * <li>the maximum amount of time to process an event</li>
 * <li>the average amount of time to process an event</li>
 * </ul>
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSLogApplicationStatistics extends PSLogInformation {
   /**
    * Construct a log message for application statistics.
    * <p>
    * The following list contains the supported keys and values:
    * <ul>
    * <li>elapsedTime - (subtype 1) the amount of time the application was
    *     up for</li>
    * <li>eventsProcessed - (subtype 2) the number of events processed</li>
    * <li>eventsPending - (subtype 3) the number of events left pending</li>
    * <li>eventsFailed - (subtype 4) the number of events failed</li>
    * <li>cacheHits - (subtype 5) the number of query cache hits</li>
    * <li>cacheMisses - (subtype 6) the number of query cache misses</li>
    * <li>minProcTime - (subtype 7) the minimum amount of time to process
    *     an event</li>
    * <li>maxProcTime - (subtype 8) the maximum amount of time to process
    *     an event</li>
    * <li>avgProcTime - (subtype 9) the average amount of time to process
    *     an event</li>
    * </ul>
    *
    * @param   id       the id of the application these statistics represent
    * @param   stats    the statistics to be reported
    */
   public PSLogApplicationStatistics(int id, Map stats)
   {
      super(LOG_TYPE, id);

      String elapsedTime = (String)stats.get("elapsedTime");
      if (elapsedTime == null)
         elapsedTime = "";

      String eventsProcessed = (String)stats.get("eventsProcessed");
      if (eventsProcessed == null)
         eventsProcessed = "";

      String eventsPending = (String)stats.get("eventsPending");
      if (eventsPending == null)
         eventsPending = "";

      String eventsFailed = (String)stats.get("eventsFailed");
      if (eventsFailed == null)
         eventsFailed = "";

      String cacheHits = (String)stats.get("cacheHits");
      if (cacheHits == null)
         cacheHits = "";

      String cacheMisses = (String)stats.get("cacheMisses");
      if (cacheMisses == null)
         cacheMisses = "";

      String minProcTime = (String)stats.get("minProcTime");
      if (minProcTime == null)
         minProcTime = "";

      String maxProcTime = (String)stats.get("maxProcTime");
      if (maxProcTime == null)
         maxProcTime = "";

      String avgProcTime = (String)stats.get("avgProcTime");
      if (avgProcTime == null)
         avgProcTime = "";

      m_subs      = new PSLogSubMessage[9];
      m_subs[0]   = new PSLogSubMessage(1, elapsedTime);
      m_subs[1]   = new PSLogSubMessage(2, eventsProcessed);
      m_subs[2]   = new PSLogSubMessage(3, eventsPending);
      m_subs[3]   = new PSLogSubMessage(4, eventsFailed);
      m_subs[4]   = new PSLogSubMessage(5, cacheHits);
      m_subs[5]   = new PSLogSubMessage(6, cacheMisses);
      m_subs[6]   = new PSLogSubMessage(7, minProcTime);
      m_subs[7]   = new PSLogSubMessage(8, maxProcTime);
      m_subs[8]   = new PSLogSubMessage(9, avgProcTime);
   }

   /**
    * Get the sub-messages (type and text). A sub-message is created
    * for each statistic reported when this object was created. See the
    * {@link #PSLogApplicationStatistics(int, java.util.Map) constructor}
    * for more details.
    *
    * @return  an array of sub-messages (PSLogSubMessage)
    */
   public PSLogSubMessage[] getSubMessages()
   {
      return m_subs;
   }

   /**
    * Application statistics is set as type 6.
    */
   private static final int LOG_TYPE = 6;
   private PSLogSubMessage[] m_subs = null;
}

