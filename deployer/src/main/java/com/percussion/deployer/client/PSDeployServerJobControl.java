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

import com.percussion.deployer.error.PSDeployException;

/**
 * The job controller that represents a job started on the server. Provides 
 * ability to query the job status, cancel the job, and get the job results.
 */
public class PSDeployServerJobControl  implements IPSDeployJobControl
{
   /**
    * Constructs this object using the supplied parameters. 
    * @param id The job id that identifies the job on the server, must be > 0.
    * @param dm The deployment manager that will forward request to the server,
    * may not be <code>null</code>.
    * 
    * @throws IllegalArgumentException if any parameter is invalid.
    */
   public PSDeployServerJobControl(int id, PSDeploymentManager dm)
   {
      if(id <= 0)
         throw new IllegalArgumentException("id must be > 0.");
         
      if(dm == null)
         throw new IllegalArgumentException("dm may not be null.");
      
      m_jobId = id;
      m_deployMgr = dm;      
   }
      
   //IPSDeployJobControl interface implementation
   public int getStatus() 
      throws PSDeployException
   {
      StringBuffer statusMsg = new StringBuffer();
      int status;

      try
      {
         status = m_deployMgr.getJobStatus(m_jobId, statusMsg);
      }
      catch (PSDeployException e)
      {
         // might be non-fatal read error - wait and retry one time
         try
         {
            Thread.sleep(1000);
         }
         catch (InterruptedException e1)
         {

         }
         
         try
         {
            status = m_deployMgr.getJobStatus(m_jobId, statusMsg);
         }
         catch (PSDeployException e2)
         {
            // throw original excetpion
            throw e;
         }         
      }
      
      m_statusMsg = statusMsg;
      
      return status;
   }
   
   //IPSDeployJobControl interface implementation   
   public int cancelDeployJob() 
      throws PSDeployException   
   {
      return m_deployMgr.cancelJob(m_jobId);
   }

   //IPSDeployJobControl interface implementation   
   public String getStatusMessage()
   {
      if(m_statusMsg != null)
         return m_statusMsg.toString();
         
      return null;
   }

   //IPSDeployJobControl interface implementation
   public int getJobId()
   {
      return m_jobId;
   }

   /**
    * The job id this controller is handling. Initialized in constructor and 
    * never modified after that.
    */
   private int m_jobId;
   
   /**
    * The deployment manager that will be used to query the job status and 
    * cancel the job. Initialized in constructor and never <code>null</code> or
    * modified after that.
    */
   private PSDeploymentManager m_deployMgr;
   
   /**
    * The status message that holds the message got from the deployment
    * manager with the last call to the {@link #getStatus() getStatus} method.
    * Gets modified with each call to <code>getStatus()</code> method.
    */
   private StringBuffer m_statusMsg = null;
}
