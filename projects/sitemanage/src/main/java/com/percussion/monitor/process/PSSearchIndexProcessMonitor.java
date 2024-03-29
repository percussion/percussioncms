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
package com.percussion.monitor.process;

import com.percussion.monitor.service.IPSMonitor;
import com.percussion.monitor.service.PSMonitorService;
import com.percussion.search.PSSearchIndexEventQueue;
import com.percussion.services.notification.IPSNotificationListener;
import com.percussion.services.notification.IPSNotificationService;
import com.percussion.services.notification.PSNotificationEvent;
import com.percussion.services.notification.PSNotificationEvent.EventType;
import com.percussion.services.notification.PSNotificationServiceLocator;

import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Monitor the status and size of the search index queue
 * 
 * @author JaySeletz
 *
 */
public class PSSearchIndexProcessMonitor implements IPSNotificationListener
{
    private static final String STATUS_MSG_SOME = " items in queue";

    private static final String STATUS_MSG_ONE = " item in queue";

    private static final String STATUS_MSG_NONE = ", no items in queue";

    private static IPSMonitor monitor = null;

    private static AtomicInteger curCount = new AtomicInteger(0);

    private static String status;

    ScheduledExecutorService executor = null;

    private volatile AtomicBoolean running = new AtomicBoolean(false);

    private Object changeMonitor = new Object();

    private volatile AtomicBoolean changed = new AtomicBoolean(false);

    public PSSearchIndexProcessMonitor()
    {
        monitor = PSMonitorService.registerMonitor("SearchIndex", "Search indexing");
        IPSNotificationService notificationService = PSNotificationServiceLocator.getNotificationService();
        notificationService.addListener(EventType.SEARCH_INDEX_ITEM_PROCESSED, this);
        notificationService.addListener(EventType.SEARCH_INDEX_ITEM_QUEUED, this);
        notificationService.addListener(EventType.SEARCH_INDEX_STATUS_CHANGE, this);
        monitor.setMessage("Index queue not initialized");
    }

    /**
     * Get the current status
     */
    public static String getStatus()
    {
        return status;
    }

    /**
     * Get the current count
     */
    public static int getCount()
    {
        return curCount.get();
    }

    private void updateStatusMessage()
    {
        int count = curCount.get();
        String tmpStatus = status;

        StringBuilder buf = new StringBuilder();

        buf.append(tmpStatus);

        if (count == 0)
        {
            buf.append(STATUS_MSG_NONE);
        }
        else
        {
            buf.append(", ");
            buf.append(count);
            buf.append(count == 1 ? STATUS_MSG_ONE : STATUS_MSG_SOME);
        }

        monitor.setMessage(buf.toString());
    }

    @Override
    public void notifyEvent(PSNotificationEvent notification)
    {
        EventType type = notification.getType();

        switch (type)
        {
            case SEARCH_INDEX_STATUS_CHANGE :
                PSSearchIndexEventQueue indexQueue = PSSearchIndexEventQueue.getInstance();
                    status = indexQueue.getStatus();
                    synchronized (changeMonitor)
                    {
                        if (indexQueue.getStatus().equals("Running") || indexQueue.getStatus().equals("Paused") )
                        {
                            if (running.compareAndSet(false, true))
                            {                       
                                if (executor == null)
                                {
                                    executor = Executors.newScheduledThreadPool(1);
                                    executor.scheduleAtFixedRate(updater, 0, 5, TimeUnit.SECONDS);
                                    changed.set(true);
                                } 
                            }
                        } 
                        else
                        {
                            if (running.compareAndSet(true, false))
                            {
                          
                                executor.shutdown();
                                executor = null;
                                running.set(false);
                               
                             }
                        }
                }
            case SEARCH_INDEX_ITEM_QUEUED :
            case SEARCH_INDEX_ITEM_PROCESSED :
                changed.compareAndSet(false, true);
            default :
                break;
        }
    }

    
    TimerTask updater = new TimerTask()
    {
        public void run()
        {
            if (changed.compareAndSet(true, false))
            {
                PSSearchIndexEventQueue indexQueue = PSSearchIndexEventQueue.getInstance();
       
                curCount.set(indexQueue.size());
                updateStatusMessage();
            }
        }
    };

}
