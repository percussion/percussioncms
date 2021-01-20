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
package com.percussion.share.async.impl;

import com.percussion.server.PSRequest;
import com.percussion.share.async.IPSAsyncJob;
import com.percussion.share.async.IPSAsyncJobListener;
import com.percussion.utils.request.PSRequestInfo;
import com.percussion.utils.thread.PSThreadUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author JaySeletz
 * 
 */
public abstract class PSAsyncJob implements IPSAsyncJob
{
    /**
     * A wrapper for the run which sets the actual PSRequest on the threadlocal
     * so that any internal requests can pass thru without any login
     * authentication
     * 
     */
    public abstract void doRun();
    
    /**
     * Provides the configuration object passed into the base class {@link #init(Object)} method. 
     * 
     * @param config The configuration object, never <code>null</code>.
     */
    protected abstract void doInit(Object config);
    
    private static final Log log = LogFactory.getLog(PSAsyncJob.class);

    /**
     * Required implementation for jobs running within the server that need to access the current request info.  
     * Derived classes should implement {@link #doInit(Object)}.
     */
    public void init(Object config)
    {
        m_requestInfoMap = PSRequestInfo.copyRequestInfoMap();
        PSRequest request = (PSRequest) m_requestInfoMap.get(PSRequestInfo.KEY_PSREQUEST);
        m_requestInfoMap.put(PSRequestInfo.KEY_PSREQUEST, request.cloneRequest());
    	/*
    	Workaround for    https://issues.jboss.org/browse/JBAS-1234
    	 We set our own interrupt and Check.  Can revert to old mechanism after Jetty.
    	*/
        m_requestInfoMap.put(PSThreadUtils.JOB_CANCELLED, m_cancelled);
        doInit(config);
    }

    /**
     * Set's the current status of this job as a percentage. Should be called
     * periodically by derived classes from the run() method to provide current
     * status.
     * 
     * @param status A number between <code>1-100</code> inclusive to indicate
     *            the percentage of completeness for this job, or {@link #ABORT_STATUS}
     *            if there has been a fatal error.
     * 
     * @throws IllegalArgumentException is status is invalid.
     */
    protected void setStatus(int status)
    {
        if ((status < 1 || status > 100) && status != ABORT_STATUS)
            throw new IllegalArgumentException("invalid status: " + status);

        m_status = status;
    }

    @Override
    public int getStatus()
    {
        return m_status;
    }

    /**
     * Marks the job as completed and notifies listeners.
     */
    protected void setCompleted()
    {
        m_isCompleted.set(true);

        // Using CopyOnWriteArryList to prevent concurrent modification
        for (IPSAsyncJobListener listener : m_listeners)
        {
            listener.jobCompleted(getId());
        }
    }

    @Override
    public boolean isCompleted()
    {
        return m_isCompleted.get();
    }

    @Override
    public boolean isCancelled()
    {
        return m_cancelled.get();
    }

    @Override
    public long getId()
    {
        if (m_id == -1)
            throw new IllegalStateException("setId() has not been called.");

        return m_id;
    }
    

    @Override
    public void setId(long id)
    {
        m_id = id;
    }

    @Override
    public void cancelJob()
    {
        m_cancelled.set(true);
    }

    @Override
    public void addJobListener(IPSAsyncJobListener listener)
    {
        if (listener == null)
            throw new IllegalArgumentException("listener may not be null");

        m_listeners.add(listener);
    }

    @Override
    public void removeJobListener(IPSAsyncJobListener listener)
    {
        if (listener == null)
            throw new IllegalArgumentException("listener may not be null");

        m_listeners.remove(listener);
    }

    /**
     * Sets the current status message for this job. Should be called
     * periodically by derived classes from the <code>run()</code> method to
     * provide a current status message.
     * 
     * @param msg The status message, may not be <code>null</code> or empty.
     * 
     * @throws IllegalArgumentException if <code>msg</code> is invalid.
     */
    protected void setStatusMessage(String msg)
    {
        if (msg == null || msg.trim().length() == 0)
            throw new IllegalArgumentException("msg may not be null or empty");

        m_statusMessage = msg;
    }

    @Override
    public String getStatusMessage()
    {
        return m_statusMessage;
    }

    /**
     * Override the Thread.run() from Runnable. Derived classes should not override this
     * method, and instead implement {@link #doRun()}.
     * 
     * @throws IllegalStateException if {@link #setId()} has not already been called. 
     */
    @Override
    public void run()
    {
        if (m_id == -1)
        {
            throw new IllegalStateException("setId() has not been called");
        }
        
        if (PSRequestInfo.isInited())
        {
            PSRequestInfo.resetRequestInfo();
        }

        PSRequestInfo.initRequestInfo(m_requestInfoMap);
     
        try
        {
            runningThread = Thread.currentThread();
            doRun();
        }
        catch (Exception e) 
        {
            log.error("Error running async job", e);
        }
        finally
        {
            setCompleted();
        }

    }
    
    /**
     * Default implementation, returns <code>true</code> if {@link #getResult()} has been called at least once after 
     * calling {@link #setCompleted()}.
     */
    @Override
    public boolean isDiscarded()
    {
        return m_isDiscarded.get();
    }

    @Override
    public Object getResult()
    {
        if (isCompleted())
            m_isDiscarded.set(true);
        return m_result;
    }

    /**
     * Set the result to return from {@link #getResult()}.
     * 
     * @param result the result, may be <code>null</code>.
     */
    public void setResult(Object result)
    {
        m_result = result;
    }

    /**
     * Allows the current running job's thread to be gracefully interrupted.  Caller must
     * still manage job state and status properly.
     */
    protected void interruptJob()
    {

        try
        {
            if (runningThread != null && runningThread.isAlive() && !runningThread.isInterrupted())
            {
            	/*
            	Workaround for    https://issues.jboss.org/browse/JBAS-1234
            	 We set our own interrupt and Check.  Can revert to old mechanism after Jetty.
            	*/
                runningThread.interrupt();
            }
            else
            {
                log.debug("Cannot interrupt async job, thread is not running.");
            }
        }
        catch (Exception e)
        {
            log.error("Unable to interrupt async job", e);
        }
    }
    
    /**
     * Tracks the percentage complete for this job as a number between
     * <code>1-100</code> inclusive. Initialized to <code>1</code>, modified by
     * calls to {@link #setStatus(int)}.
     */
    private volatile int m_status = 1;

    /**
     * Provides a message indicating the current status of this job.
     * Never <code>null</code>, empty until first call to {@link #setStatusMessage(String)},
     * never <code>null</code> or empty after that.
     */
    private volatile String m_statusMessage = "";

    /**
     * Indicates if this job is currently active. Initially <code>false</code>,
     * set to <code>true</code> once the job completes normally.
     */
    private AtomicBoolean m_isCompleted = new AtomicBoolean(false);

    /**
     * Indicates if an attempt to cancel this job has been made. This should be
     * checked periodically while running through isCancelled().
     * 
     * @see #isCancelled()
     */
    private AtomicBoolean m_cancelled = new AtomicBoolean(false);
    
    /**
     * Indicates if the job can be discarded.
     */
    private AtomicBoolean m_isDiscarded =  new AtomicBoolean(false);
    
    /**
     * The result to be returned.
     */
    private Object m_result = null;

    /**
     * The id used to identify this job. Must be set by derived classes during
     * the <code>init()</code> method.
     */
    protected long m_id = -1;

    /**
     * List of job listeners, never <code>null</code>, may be empty.
     */
    private CopyOnWriteArrayList<IPSAsyncJobListener> m_listeners = new CopyOnWriteArrayList<IPSAsyncJobListener>();

    /**
     * The actual request that will result in spawning a thread.
     */
    private Map<String, Object> m_requestInfoMap;
    
    /**
     * The running thread, set in the {@link #run()} method.
     */
    private Thread runningThread;
}
