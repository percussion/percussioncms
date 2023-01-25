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

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Monitor the number of pages queued for thumbnail generation
 * @author JaySeletz
 *
 */
public class PSThumbnailProcessMonitor
{
    private static final String THUMBNAILS_MSG = " pages queued for thumbnail";
    private static final String THUMBNAIL_MSG = "1 page queued for thumbnail";
    private static final String NO_THUMBNAIL_MSG = "No pages queued for thumbnail";
    
    private static IPSMonitor monitor = null;
    private static AtomicInteger curCount = new AtomicInteger(0);
    
    public PSThumbnailProcessMonitor()
    {
        monitor = PSMonitorService.registerMonitor("Thumbnail", "Thumbnails");
        setThumbnailCount(0);
    }

    private static void setThumbnailCount(int count)
    {
        if (monitor == null) {
            return;
        }
        
        String msg = NO_THUMBNAIL_MSG;
        if (count > 0) {
            msg = count == 1 ? THUMBNAIL_MSG : count + THUMBNAILS_MSG;
        }
        
        monitor.setMessage(msg);
    }
    
    /**
     * Add the specified number of thumbnails to the count
     * 
     * @param add The amount to add.
     */
    public static void incrementCount(int add)
    {
        setThumbnailCount(curCount.addAndGet(add));
    }
    
    public static void incrementCount()
    {
        setThumbnailCount(curCount.incrementAndGet());
    }
    
    public static void decrementCount()
    {
        setThumbnailCount(curCount.decrementAndGet());
    }
    
    public static void decrementCount(int remove)
    {
        setThumbnailCount(curCount.addAndGet((-1)*remove));
    }


    public int getCurrentCount()
    {
        return curCount.get();
    }
}
