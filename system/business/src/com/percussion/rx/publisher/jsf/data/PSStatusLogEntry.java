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
package com.percussion.rx.publisher.jsf.data;

import com.percussion.rx.publisher.jsf.beans.PSRuntimeNavigation;

/**
 * A simple java bean that sets the right job id in the pub log bean and 
 * implements an action to go to the pub log view.
 * 
 * @author dougrand
 *
 */
public class PSStatusLogEntry
{
   /**
    * The job id, set in the ctor.
    */
   private long m_jobid;
   
   /**
    * The outcome, which determines the next page to navigate to.
    */
   private final String m_outcome = "pub-runtime-job-log";
   
   /**
    * See {@link #isTerminated()}.
    */   
   private boolean m_isTerminated = false;
   
   /**
    * The navigator, set in the ctor.
    */
   private PSRuntimeNavigation m_navigator;
   
   /**
    * Ctor
    * @param jobid the job id (a.k.a. the status id).
    * @param nav the navigator, never <code>null</code>.
    */
   public PSStatusLogEntry(long jobid, PSRuntimeNavigation nav)
   {
      if (nav == null)
      {
         throw new IllegalArgumentException("nav may not be null");
      }
      m_jobid = jobid;
      m_navigator = nav;
   }
 
   /**
    * Determines if the job is terminated.
    * @return <code>true</code> if the job is terminated.
    */
   public boolean isTerminated()
   {
      return m_isTerminated;
   }

   /**
    * Set the terminated status.
    * @param isTerminated <code>true</code> if the job is terminated.
    */
   public void setTerminated(boolean isTerminated)
   {
      m_isTerminated = isTerminated;
   }

   /**
    * @return the jobid
    */
   public long getJobid()
   {
      return m_jobid;
   }

   /**
    * Action to setup the job in the navigator.
    * 
    * @return the outcome, never <code>null</code> or empty.
    */
   public String perform()
   {
      m_navigator.setJobId(m_jobid);
      return m_outcome;
   }
}
