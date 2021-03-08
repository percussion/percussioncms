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

package com.percussion.server.job;

import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.PSRequest;
import com.percussion.utils.request.PSRequestInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.w3c.dom.Document;


/**
 * Abstract base class for all jobs, implementing most methods for convenience
 * and providing a constructor for specifying the job id and descriptor.
 * Executes a job on a separate thread and allows it's status to be polled and
 * allows the job to be cancelled as well.
 * <p>
 * Classes implementing this interface must provide an implementation of
 * the {@link java.lang.Thread#run() run()} method, which is where the
 * work of the job is executed.  If any exceptions are encountered in the run
 * method, they should be logged if possible, the status should
 * be set to <code>-1</code> (see {@link #ABORT_STATUS}), and the status message 
 * updated with the error before the thread terminates.  All exceptions
 * should be handled in the run method.  Whenever run() terminates, 
 * {@link #setCompleted()} should be called, even if the job was cancelled or
 * aborted due to an error, so that the job handler can remove itself as a
 * listener and be unlocked.
 */
public abstract class PSJobRunner extends Thread
{

   /**
    * Constructor for this class.
    */
   public PSJobRunner()
   {

   }

   /**
    * Any validation required before the job is started should be
    * performed here. Called by the PSJobHandler before trying to start the job.
    * Derived classes must implement this method to validates that the user has 
    * the authorization to perform the actions specified by the descriptor and 
    * aquires any required resources (i.e. locks).  Derived classes must call
    * {@link #setId(int)} to set the job id.
    * 
    * 
    * 
    * @param id The id used to identify this job. Must be used to then call
    * {@link #setId(int)} to set the job id.
    * @param descriptor The document containing the descriptor used to execute 
    * this job.  May not be <code>null</code>.
    * @param req The request used to determine the current user's security 
    * permissions.  May not be <code>null</code>.
    * @param initParams Set of name value pairs that this job may require, may 
    * be <code>null</code>.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSAuthenticationFailedException if the user cannot be 
    * authenticated.
    * @throws PSAuthorizationException if user is not authorized to run this 
    * job.
    * @throws PSJobException for any other errors.
    */
   public abstract void init(int id, Document descriptor, PSRequest req, 
      Properties initParams) 
         throws PSAuthenticationFailedException, PSAuthorizationException, 
            PSJobException;
   
   
   /**
    *  A wrapper for the run which sets the actual PSRequest on the threadlocal
    *  so that any internal requests can pass thru without any login 
    *  authentication
    *
    */
   public abstract void doRun();
   
   /**
    * Set's the current status of this job as a percentage. Should be called 
    * periodically by derived classes from the run() method to provide current 
    * status.
    * 
    * @param status A number between <code>1-100</code> inclusive to indicate 
    * the percentage of completness for this job, or <code>-1</code> if there 
    * has been a fatal error.
    * 
    * @throws IllegalArgumentException is status is invalid.
    */
   protected void setStatus(int status)
   {
      if ((status < 1 || status > 100) && status != -1)
         throw new IllegalArgumentException("invalid status: "+ status);
         
      m_status = status;
   }

   /**
    * Retreives the current status of this job.
    * 
    * @return the percent complete for this job as a number between
    * <code>1-100</code> inclusive. Returns <code>-1</code> if the job
    * has terminated abnormally (see {@link #ABORT_STATUS}).
    */
   public int getStatus()
   {
      return m_status;
   }

   /**
    * Marks the job as completed and notifies listeners.
    */
   protected void setCompleted()
   {
      m_isCompleted = true;
      
      // need to copy the listener list because as we call jobCompleted, each
      // listener will remove themselves, and it will cause a concurrent 
      // modification exception if we iterate on m_listeners
      List listeners = new ArrayList(m_listeners);      
      Iterator i = listeners.iterator();
      while (i.hasNext())
      {
         IPSJobListener listener = (IPSJobListener)i.next();
         listener.jobCompleted(getId());
      }
   }

   /**
    * Determines if this job has completed.
    * 
    * @return <code>true</code> if the job has completed normally or cancelled
    * sucessfully, <code>false</code> if it has not yet completed.
    */
   public boolean isCompleted()
   {
      return m_isCompleted;
   }

   /**
    * Determines if this job has been cancelled. Should be polled by derived
    * classes from the run() method to determine if the this job should cancel.
    * 
    * @return <code>true</code> if <code>cancelJob()</code> has been called, but
    * does not indicate if the job was successfully cancelled - this can only be
    * determined by a call to <code>isCompleted()</code>.
    */
   public boolean isCancelled()
   {
      return m_cancelled;
   }

   /**
    * Retreives the ID of this job.
    * 
    * @return the ID.
    * 
    * @throws IllegalStateException if the id has not been initialized.
    */
   public long getId()
   {
      if (m_id == -1)
         throw new IllegalStateException("Id has not been set.");
         
      return m_id;
   }
   
   

   /**
    * Sets flag for this job to stop processing regardless of it's status and
    * perform any cleanup required. Callers should then check
    * {@link #isCompleted() isCompleted} to determine when the job has stopped.
    * At this point, if the return from {@link #getStatus()} is less than 100,
    * the job was cancelled, if it is equal to 100, then the cancel request was
    * too late and the job has already run to completion.
    */
   public void cancelJob()
   {
      m_cancelled = true;
   }

   /**
    * Adds a listener to this job
    * 
    * @param listener The listener, may not be <code>null</code>.
    * 
    * @throws IllegalArgumentException If <code>listener</code> is 
    * <code>null</code>.
    */
   public void addJobListener(IPSJobListener listener)
   {
      if (listener == null)
         throw new IllegalArgumentException("listener may not be null");
         
      m_listeners.add(listener);
   }

   /**
    * Removes a listener from this job.
    * 
    * @param listener The listener, may not be <code>null</code>.
    * 
    * @throws IllegalArgumentException If <code>listener</code> is 
    * <code>null</code>.
    */
   public void removeJobListener(IPSJobListener listener)
   {
      if (listener == null)
         throw new IllegalArgumentException("listener may not be null");
         
      m_listeners.remove(listener);
   }

   /**
    * Sets the current status message for this job.  Should be called 
    * periodically by
    * derived classes from the <code>run()</code> method to provide a current 
    * status message.
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

   /**
    * Get this job's current status message.
    * 
    * @return The message, may be <code>null</code>, never empty.
    */
   public String getStatusMessage()
   {
      return m_statusMessage;
   }


   /**
    * Override the Thread.run() from Runnable. This will set the actual 
    * PSRequest
    */
   public void run()
   {
      if ( !PSRequestInfo.isInited() )
      {
         Map<String,Object> initMap = new HashMap<>();
         PSRequestInfo.initRequestInfo(initMap);
      }
      PSRequestInfo.setRequestInfo(PSRequestInfo.KEY_PSREQUEST, m_request);
      
      doRun();

   }
   /**
    * Status to indicate that job is aborted.
    */
   public static final int ABORT_STATUS = -1;

   /**
    * Tracks the percentage complete for this job as a number between
    * <code>1-100</code> inclusive. Initialized to <code>1</code>, modified by
    * calls to {@link #setStatus(int)}.
    */
   private int m_status = 1;
   
   /**
    * Provides a message indicating the current status of this job.  
    * <code>null</code> until first call to {@link #setStatusMessage(String)},
    * never <code>null</code> or empty after that.
    */
   private String m_statusMessage = null;

   /**
    * Indicates if this job is currently active.  Initially <code>false</code>,
    * set to <code>true</code> once the job completes normally.
    */
   private boolean m_isCompleted = false;

   /**
    * Indicates if an attempt to cancel this job has been made.  This should
    * be checked periodically while running through isCancelled().
    * @see #isCancelled()
    */
   private boolean m_cancelled = false;

   /**
    * The id used to identify this job.  Must be set by derived classes during
    * the <code>init()</code> method.
    */
   protected int m_id = -1;

   /**
    * List of job listeners, never <code>null</code>, may be empty.
    */
   private List m_listeners = new ArrayList();  
   
   /**
    * the actual request that will result in spawning a thread. The consumer i.e
    * the subclass may set it to non-null and if it is set, this is used
    * to in setting the context information (PSRequestInfo)
    * Cannot be <code>null</code> after init().
    */
   protected PSRequest m_request = null;

}
