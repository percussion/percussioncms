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
package com.percussion.share.async;

import com.percussion.share.async.impl.PSAsyncJob;
import com.percussion.utils.thread.PSThreadInterruptedException;
import com.percussion.utils.thread.PSThreadUtils;

import java.util.Date;

import org.apache.commons.lang.Validate;

/**
 * A test job that takes in a number as it's config, and each time {@link #getStatus()} is called, the % complete
 * is incremented by that amount.
 * 
 * @author JaySeletz
 */
public class PSTestAsyncJob extends PSAsyncJob
{
    public static String ABORT_MESSAGE = "ABORT";
    public static String CANCEL_MESSAGE = "CANCEL";
    public static String DONE_MESSAGE = "COMPLETED";
    public static String STATUS_MESSAGE = "STATUS-";

    
    private int m_increment;
    private boolean useInterrupt = false;
    private boolean started = false;
    
    /* (non-Javadoc)
     * @see com.percussion.share.async.impl.PSAsyncJob#doRun()
     */
    @Override
    public void doRun()
    {
        started = true;
        try
        {

            try
            {
                while (!isCompleted())
                {
                    if (isCancelled() && !useInterrupt)
                        break;
                    if (useInterrupt)
                        countTo(5);
                    else
                        Thread.sleep(5);
                    PSThreadUtils.checkForInterrupt();
                }
            }
            catch (InterruptedException e)
            {
                if (!isCancelled())
                {
                    doAbort();
                    return;
                }
            }
            catch (PSThreadInterruptedException e)
            {
                if (!isCancelled())
                {
                    doAbort();
                    return;
                }
            }

            if (isCancelled())
            {
                setStatusMessage(CANCEL_MESSAGE);
            }
        }
        catch (Exception e)
        {
            doAbort();
        }
        finally
        {
            setCompleted();
        }
    }
    
    

    /**
     * Non-blocking wait method
     * 
     * @param wait number of millis to wait.
     * 
     * @return The number of iterations waited
     */
    private int countTo(int wait)
    {
        int count = 0;
        long time = (new Date()).getTime();
        long next = time + (wait);
        while(next > (new Date()).getTime())
        {
            count += wait;
        }
        
        return count;
    }



    @Override
    public void cancelJob()
    {
        super.cancelJob();
        if (useInterrupt)
            interruptJob();
    }

    

    public boolean isStarted()
    {
        return started;
    }



    /* (non-Javadoc)
     * @see com.percussion.share.async.impl.PSAsyncJob#doInit(java.lang.Object)
     */
    @Override
    protected void doInit(Object config)
    {
        Validate.isTrue(config instanceof Integer);
        
        m_increment = (Integer) config;
        setStatusMessage("Initialized");
        
    }

    public void setUseInterrupt(boolean useInterrupt)
    {
        this.useInterrupt = useInterrupt;
    }

    /*
     * This method should not normally be overridden, only overriding for test purposes to increment status
     * in a predictable way for testing.
     */
    @Override
    public int getStatus()
    {
        int status = super.getStatus();
        if (status == ABORT_STATUS)
            return status;
        
        if (!isCancelled())
        {
            int newStatus = status + m_increment;
            if (newStatus > 100)
                newStatus = 100;
            
            if (newStatus == 100)
            {
                setResult(m_increment);
                setCompleted();
            }
            
            setStatus(newStatus);
            
            if (status < 100)
                setStatusMessage(STATUS_MESSAGE + status);
            else
            {
                setStatusMessage(DONE_MESSAGE);
            }
        }
        
        return status;
    }
    
    
    /**
     * override to capture current status for testing purposes only
     */
    @Override
    protected void setStatus(int status)
    {
        super.setStatus(status);
    }


    /*
     * Overriden for testing purposes, in order to bypass attempts to access the request info.
     */
    @Override
    public void init(Object config)
    {
        doInit(config);
    }

    
    /**
     * test method
     */
    private void doAbort()
    {
        System.out.println("aborted");
        setStatus(ABORT_STATUS);
        setStatusMessage(ABORT_MESSAGE);
        return;
    }
}
