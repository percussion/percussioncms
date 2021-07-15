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

package com.percussion.sitemanage.importer.helpers.impl;

import java.util.concurrent.ConcurrentHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

class PerformanceStats
{
    public String identifier;

    public String className;

    public long count;

    public long totalTime;

    public long lastTotalTime;

    public long maxTime;

    public PerformanceStats(String identifier)
    {
        this.identifier = identifier;
    }
}


public class PSHelperPerformanceMonitor
{

    public static final String SEPARATOR = "::";

    private static long statLogFrequency = 100;

    private static long methodWarningThreshold = 3000;

    private static ConcurrentHashMap<String, PerformanceStats> performanceStats = new ConcurrentHashMap<>();

    private static final Logger log = LogManager.getLogger(PSHelperPerformanceMonitor.class);

    public static void updateStats(String identifier, long elapsedTime)
    {
    	/*
        PerformanceStats stats = performanceStats.get(identifier);
        if (stats == null)
        {
            stats = new PerformanceStats(identifier);
            stats.className = identifier;
            stats.count = 0;
            stats.totalTime = 0;
            stats.lastTotalTime = 0;
            stats.maxTime = 0;
            performanceStats.put(identifier, stats);
        }
        stats.count++;
        stats.totalTime += elapsedTime;
        if (elapsedTime > stats.maxTime)
        {
            stats.maxTime = elapsedTime;
        }

        if (elapsedTime > methodWarningThreshold)
        {
            log.debug("Wow, a portion of import took much longer than anticipated: " + identifier + "(), cnt = " + stats.count + ", lastTime = " + elapsedTime
                    + ", maxTime = " + stats.maxTime);
        }

        if (stats.count % statLogFrequency == 0)
        {
            long avgTime = stats.totalTime / stats.count;
            log.info("Performance Statistics for: " + identifier + " at execution count " + stats.count + 
                    ", average Time = " + avgTime + ", maximum time = " + stats.maxTime);
            // reset the last total time
            stats.lastTotalTime = stats.totalTime;
        }
    */
    }

   

}
