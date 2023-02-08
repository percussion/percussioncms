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
