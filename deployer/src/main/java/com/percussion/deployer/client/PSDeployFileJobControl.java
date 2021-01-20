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

package com.percussion.deployer.client;

import com.percussion.deployer.error.IPSDeploymentErrors;
import com.percussion.deployer.error.PSDeployException;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Job control to use when copying a file to or from the server.
 */
public class PSDeployFileJobControl implements IPSDeployJobControl
{

   /**
    * Checks the status of the file copy. The result contains a 
    * value between <code>1-100</code> to indicate the % done.
    * <code>100</code> indicates that the job has completed. A result of -1 
    * indicates that there has been an error and the job completed abnormally. 
    * 
    * @return The status.
    * 
    * @throws PSDeployException if there are any errors.
    */
   public int getStatus() throws PSDeployException
   {
      int result = 1;
      
      synchronized (m_cancelledStatusMonitor)
      {
         if (m_cancelledStatus == IPSDeployJobControl.JOB_ABORTED)
            result = -1;
         else if (m_cancelledStatus == IPSDeployJobControl.JOB_COMPLETED)
            result = 100;
         else if (m_streamCounter != null)
         {
            m_bytes = m_streamCounter.getByteCount();
            result = (m_bytes * 100) / m_totalBytes;
            // not allowed a status less than 1
            if (result == 0)
               result = 1;
            if (result >= 100 && m_cancelledStatus != 
               IPSDeployJobControl.JOB_COMPLETED)
            {
               // wait till completed
               result = 99;
            }
         }
         updateStatusMessage();
      }
      
      return result;  
   }
   
   /**
    * Attempts to stop the file copy.  Since the copy is running in its 
    * own thread, it may complete on its own before the request to cancel can
    * be processed.   
    * 
    * @return  Returns one of 3 possible result codes:<br> 
    * {@link #JOB_CANCELLED} - Job cancelled successfully<br> 
    * {@link #JOB_COMPLETED} - Job completed before cancelled<br> 
    * {@link #JOB_ABORTED} - Job aborted<br>
    * 
    * @throws PSDeployException if there are any errors.
    */
   public int cancelDeployJob() throws PSDeployException
   {
      synchronized (m_cancelledStatusMonitor)
      {
         if (m_cancelledStatus == -1)
         {
            // if never started, then we're cancelled.
            if (m_streamCounter == null)
               m_cancelledStatus = IPSDeployJobControl.JOB_CANCELLED;
            else
            {
               m_streamCounter.closeStream();
               
               if (m_streamCounter.getByteCount() < m_totalBytes)
                  m_cancelledStatus = IPSDeployJobControl.JOB_CANCELLED;
               else
                  m_cancelledStatus = IPSDeployJobControl.JOB_COMPLETED;
            }
         }
      }
                   
      return m_cancelledStatus;
   }
   
   
   /**
    * Gets a message indicating how many bytes have been copied so far.
    * 
    * @return The status message, may be <code>null</code> or empty.  
    * 
    * @throws PSDeployException if there are any errors.
    */
   public String getStatusMessage() throws PSDeployException
   {
      return m_statusMessage;
   }
   
   /**
    * Gets the id of the job this controller is handling.
    * 
    * @return <code>-1</code> always, as file copy jobs do not use IDs.
    */
   public int getJobId()
   {
      return -1;
   }
   
   /**
   * Sets the stream to use to track status.  
   * 
   * @param streamCounter Used to track the status of the copy, may not be 
   * <code>null</code>.
   * @param totalBytes The total bytes that will be sent or received, must be 
   * greater than zero.
   * 
   * @throws IllegalArgumentException if either param is invalid.
   */
   void setStream(IPSStreamCounter streamCounter, int totalBytes)
   {
      if (streamCounter == null)
         throw new IllegalArgumentException("streamCounter may not be null");

      if (totalBytes <= 0)
         throw new IllegalArgumentException("bytes may not be < 0");
      
      m_streamCounter = streamCounter;
      m_totalBytes = totalBytes;
   }

   /**
    * Method used to set an error message on this object in the event that an
    * error occurs in the file copy.
    * 
    * @param msg The message, may not be <code>null</code> or empty.
    * 
    * @throws IllegalArgumentException if <code>msg</code> is <code>null</code> 
    * or empty.
    */
   void setErrorMessage(String msg)
   {
      if (msg == null || msg.trim().length() == 0)
         throw new IllegalArgumentException("error message may not be null or empty");
         
      synchronized (m_cancelledStatusMonitor)
      {
         m_cancelledStatus = IPSDeployJobControl.JOB_ABORTED;
         m_errorMessage = msg;
      }
   }
   
   /**
    * Marks the job as completed unless it has already been cancelled or 
    * aborted.  
    */
   void setCompleted()
   {
      synchronized (m_cancelledStatusMonitor)
      {
         if (m_cancelledStatus == -1)
         {
            m_cancelledStatus = IPSDeployJobControl.JOB_COMPLETED;
         }
      }
   }

   /**
    * Return the current cancelled status.
    * 
    * @return The current cancelled status, one of the 
    * <code>IPSDeployJobControl.JOB_xxx</code>  values, or <code>-1</code>
    * if the job is still executing.  See {@link #cancelDeployJob()} for more
    * information.
    */
   int getCancelledStatus()
   {
      return m_cancelledStatus;
   }
   
   /**
    * Updates the current status message.
    * 
    * @throws PSDeployException if there are any errors.
    */
   private void updateStatusMessage() throws PSDeployException
   {
      ResourceBundle bundle = getBundle();
      
      if (m_cancelledStatus == IPSDeployJobControl.JOB_ABORTED)
      {
         String format = bundle.getString("fileJobAborted");
         Object[] args = {m_errorMessage};
         m_statusMessage = MessageFormat.format(format, args);
      }
      else if (m_cancelledStatus == IPSDeployJobControl.JOB_CANCELLED)
      {
         m_statusMessage = bundle.getString("fileJobCancelled");
      }
      else if (m_cancelledStatus == IPSDeployJobControl.JOB_COMPLETED)
      {
         m_statusMessage = bundle.getString("fileJobCompleted");
      }
      else if (m_streamCounter == null)
      {
         m_statusMessage = bundle.getString("fileJobInit");
      }
      else
      {
         String format = bundle.getString("fileJobStatus");
         Object[] args = {String.valueOf(m_bytes), String.valueOf(
               m_totalBytes)};
         m_statusMessage = MessageFormat.format(format, args);
      }
   }
   
   /**
    * This method is used to get the string resources used for status messages.
    *
    * @return the bundle, never <code>null</code>.
    * 
    * @throws PSDeployException if there are any errors.
    */
   private static ResourceBundle getBundle() throws PSDeployException
   {
      try
      {
         if (ms_bundle == null)
         {
            ms_bundle = ResourceBundle.getBundle(
               "com.percussion.deployer.client.PSDeployStringResources");
         }
      }
      catch (MissingResourceException e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, 
            e.getLocalizedMessage());
      }

      return ms_bundle;
   }
   
   /**
    * The stream counter to use to get number of bytes processed.  
    * <code>null</code> until call to {@link #setStream(IPSStreamCounter, int)},
    * never <code>null</code> or modified after that.
    */
   private IPSStreamCounter m_streamCounter = null;
   
   /**
    * Number of total bytes expected to be processed, <code>-1</code> until
    * a call to {@link #setStream(IPSStreamCounter, int)}, never modified after 
    * that.
    */
   private int m_totalBytes = -1;
   
   /**
    * String bundle used for message formats.  <code>null</code> until loaded
    * by a call to {@link #getBundle()}, never <code>null</code> after that.
    */
   private static ResourceBundle ms_bundle = null;
   
   /**
    * Status of the job after it is cancelled.  <code>-1</code> until call to
    * {@link #cancelDeployJob()}, then one of the 
    * <code>IPSDeployJobControl.JOB_XXX</code> constant values.
    */
   private int m_cancelledStatus = -1;
   
   /**
    * Monitor object to synchronize access to {@link #m_cancelledStatus}.
    * Never <code>null</code>.
    */
   private Object m_cancelledStatusMonitor = new Object();
   
   /**
    * The current status message.  <code>null</code> until first call to
    * {@link #getStatus()}, modified each time that method is called, or when
    * {@link #cancelDeployJob()} is called.
    */
   private String m_statusMessage = null;
   
   /**
    * The count of the number of bytes updated during latest call to 
    * {@link #getStatus()}.
    */
   private int m_bytes = 0;
   
   /**
    * Errror message to use when getting status message and status indicates
    * job is aborted.  Initially an empty string, never <code>null</code>,
    * set by a call to {@link #setErrorMessage(String)}, never <code>null</code> 
    * or empty after that.
    */
   private String m_errorMessage = "";
}
