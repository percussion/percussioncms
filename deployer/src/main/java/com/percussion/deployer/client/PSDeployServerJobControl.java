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

package com.percussion.deployer.client;

import com.percussion.error.PSDeployException;

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
      StringBuilder statusMsg = new StringBuilder();
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
            Thread.currentThread().interrupt();
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
   private StringBuilder m_statusMsg = null;
}
