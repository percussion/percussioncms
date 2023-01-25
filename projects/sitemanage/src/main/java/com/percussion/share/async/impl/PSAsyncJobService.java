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
package com.percussion.share.async.impl;

import com.percussion.foldermanagement.service.IPSFolderService;
import com.percussion.share.async.IPSAsyncJob;
import com.percussion.share.async.IPSAsyncJobFactory;
import com.percussion.share.async.IPSAsyncJobListener;
import com.percussion.share.async.IPSAsyncJobService;
import com.percussion.share.async.PSAsyncJobStatus;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author JaySeletz
 *
 */
public class PSAsyncJobService implements IPSAsyncJobService, IPSAsyncJobListener
{
    private IPSAsyncJobFactory m_jobFactory;
    
    private ConcurrentMap<Long, IPSAsyncJob> m_jobMap = new ConcurrentHashMap<>();
    
    private final AtomicLong m_jobIdCounter = new AtomicLong();

    
    // actual implementation provided by the Spring container 
    public void setAsyncJobFactory(IPSAsyncJobFactory jobFactory) 
    {
        m_jobFactory = jobFactory;
    }
    
    @Override
    public long startJob(String jobType, Object config) throws IPSFolderService.PSWorkflowNotFoundException {
        IPSAsyncJob job = m_jobFactory.getJob(jobType);
        long jobId = m_jobIdCounter.incrementAndGet();
        job.setId(jobId);
        job.init(config);
        
        // add self as listener
        job.addJobListener(this);
        
        // start job
        Thread thread = new Thread(job);
        thread.setDaemon(true);
        thread.start();

        m_jobMap.put(jobId, job);
        
        return jobId;
    }

    @Override
    public PSAsyncJobStatus getJobStatus(long jobId)
    {
        PSAsyncJobStatus status = null;
        IPSAsyncJob job = m_jobMap.get(jobId);
        if (job != null)
        {
            status = new PSAsyncJobStatus(jobId, job.getStatus(), job.getStatusMessage());
        }
        else
        {
            status = new PSAsyncJobStatus(jobId,IPSAsyncJob.COMPLETE_STATUS,"");
        }
        
        return status;
    }

    /* (non-Javadoc)
     * @see com.percussion.share.async.IPSAsyncJobService#cancelJob(long)
     */
    @Override
    public void cancelJob(long jobId)
    {
        IPSAsyncJob job = m_jobMap.get(jobId);
        if (job == null) {
            return;
        }
        
        // cancel the job
        job.cancelJob();

        // now wait till it's completed
        while(!job.isCompleted())
        {
           try
           {
              // sleep before we check again
              Thread.sleep(1000);
           }
           catch (InterruptedException e)
           {
               Thread.currentThread().interrupt();
              if (job.isCompleted()) {
                  break;
              }
           }
        }

        // remove self as listener
        job.removeJobListener(this);

    }

    /* (non-Javadoc)
     * @see com.percussion.share.async.IPSAsyncJobListener#jobCompleted(long)
     */
    @Override
    public void jobCompleted(long jobId)
    {
        IPSAsyncJob job = m_jobMap.get(jobId);
        if (job == null) {
            return;
        }
        
        // remove self as listener
        job.removeJobListener(this);        
        
        // handle grooming old jobs from list
        groomJobList();
    }
    
    @Override
    public Object getJobResult(long jobId)
    {
        Object result = null;
        
        IPSAsyncJob job = m_jobMap.get(jobId);
        if (job != null)
        {
            result = job.getResult();
        }
        
        return result;
    }    

    /**
     * Removes any expired jobs from the list
     */
    private void groomJobList()
    {
        for (Entry<Long, IPSAsyncJob> entry : m_jobMap.entrySet())
        {
            if (entry.getValue().isDiscarded()) {
                m_jobMap.remove(entry.getKey());
            }
        }
    }

}
