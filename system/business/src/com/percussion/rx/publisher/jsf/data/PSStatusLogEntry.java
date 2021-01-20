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
