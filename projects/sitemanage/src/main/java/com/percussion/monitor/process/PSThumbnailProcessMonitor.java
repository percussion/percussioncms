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
