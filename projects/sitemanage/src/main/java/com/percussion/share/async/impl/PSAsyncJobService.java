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
