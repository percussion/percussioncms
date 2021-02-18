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
import com.percussion.rx.publisher.IPSPublisherJobStatus;
import com.percussion.rx.publisher.IPSPublishingJobStatusCallback;

import java.util.HashSet;
import java.util.Set;

/**
 * Monitor the number of publishing jobs running
 * 
 * @author JaySeletz
 *
 */
public class PSPublishingProcessMonitor implements IPSPublishingJobStatusCallback
{
    private static final String STATUS_MSG_SOME = " publishing jobs running";
    private static final String STATUS_MSG_ONE = " publishing job running";
    private static final String STATUS_MSG_NONE = "No publishing jobs running";
    
    private static IPSMonitor monitor = null;
    
    private static Set<Long> jobIds = new HashSet<>();
    
    private static IPSPublishingJobStatusCallback callback = null;
    
    public PSPublishingProcessMonitor()
    {
        monitor = PSMonitorService.registerMonitor("Publishing", "Publishing");
        updateStatusMessage();
        callback = this;
    }
    
    private static void updateStatusMessage()
    {
        if (monitor == null)
            return;
        
        int count;
        synchronized (jobIds)
        {
            count = jobIds.size();
        }
        
        StringBuffer buf = new StringBuffer();

        if (count == 0)
        {
            buf.append(STATUS_MSG_NONE);
        }
        else
        {
            buf.append(count);
            buf.append(count == 1 ? STATUS_MSG_ONE : STATUS_MSG_SOME);
        }
        
        monitor.setMessage(buf.toString());
    }
    
    

    /**
     * Get the callback to use for pub status updates
     * 
     * @return The callback, not <code>null</code>.
     */
    public static IPSPublishingJobStatusCallback getPublishingJobStatusCallback()
    {
        return callback;
    }
    
    @Override
    public void notifyStatus(IPSPublisherJobStatus status)
    {
        synchronized (jobIds)
        {
            jobIds.remove(status.getJobId());
        }
        
        updateStatusMessage();
    }

    /**
     * @param editionId
     */
    public static void startPublishingJob(long jobId)
    {
        synchronized (jobIds)
        {
            jobIds.add(jobId);
        }
        
        updateStatusMessage();
    }

}
