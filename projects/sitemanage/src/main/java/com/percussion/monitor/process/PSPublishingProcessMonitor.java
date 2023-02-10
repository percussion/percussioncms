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
        if (monitor == null) {
            return;
        }
        
        int count;
        synchronized (jobIds)
        {
            count = jobIds.size();
        }
        
        StringBuilder buf = new StringBuilder();

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
     * @param jobId
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
